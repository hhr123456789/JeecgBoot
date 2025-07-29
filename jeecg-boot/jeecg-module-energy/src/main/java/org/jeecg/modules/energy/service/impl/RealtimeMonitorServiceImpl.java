package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IRealtimeMonitorService;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.util.InfluxDBQueryBuilder;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.jeecg.modules.energy.util.TimeZoneUtil;
import org.jeecg.modules.energy.vo.realtime.*;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 实时数据监控Service实现
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Service
@Slf4j
public class RealtimeMonitorServiceImpl implements IRealtimeMonitorService {
    
    @Autowired
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;
    
    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private InfluxDB influxDB;

    @Autowired
    private TimeZoneUtil timeZoneUtil;
    
    @Autowired
    private InfluxDBQueryBuilder queryBuilder;

    @Autowired
    private InfluxDBConfig influxDBConfig;


    @Override
    public List<ModuleInfoVO> getModulesByDimension(String dimensionCode, Integer energyType, Boolean includeChildren) {
        log.info("根据维度获取仪表列表，维度编码：{}，能源类型：{}，包含子维度：{}", dimensionCode, energyType, includeChildren);
        
        List<ModuleInfoVO> result = new ArrayList<>();
        
        try {
            // 1. 根据维度编码获取部门ID列表
            List<String> departIds = getDepartIdsByDimensionCode(dimensionCode, includeChildren);
            log.info("查询到部门ID列表：{}", departIds);
            
            if (departIds.isEmpty()) {
                log.warn("未找到维度编码 {} 对应的部门", dimensionCode);
                return result;
            }
            
            // 2. 根据部门ID列表查询仪表
            List<TbModule> modules = getModulesByDepartIdsAndEnergyType(departIds, energyType);
            log.info("查询到仪表数量：{}", modules.size());
            
            // 3. 获取维度名称映射
            Map<String, String> departNameMap = getDepartNameMapByIds(departIds);
            
            // 4. 转换为VO
            for (TbModule module : modules) {
                ModuleInfoVO vo = new ModuleInfoVO();
                vo.setModuleId(module.getModuleId());
                vo.setModuleName(module.getModuleName());
                vo.setEnergyType(module.getEnergyType());
                vo.setDimensionCode(module.getSysOrgCode());
                vo.setDimensionName(departNameMap.get(module.getSysOrgCode()));
                vo.setRatedPower(module.getRatedPower());
                vo.setIsAction(module.getIsaction());
                vo.setUpdateTime(module.getUpdateTime() != null ? 
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(module.getUpdateTime()) : null);
                result.add(vo);
            }
            
        } catch (Exception e) {
            log.error("根据维度获取仪表列表失败", e);
            throw new RuntimeException("查询仪表列表失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    @Override
    public List<ParameterConfigVO> getParameterConfig(Integer energyType) {
        log.info("获取参数配置，能源类型：{}", energyType);
        
        List<ParameterConfigVO> result = new ArrayList<>();
        
        // 根据能源类型返回对应的参数配置
        if (energyType == 1) { // 电力
            result.addAll(getElectricParameterConfig());
        } else if (energyType == 2) { // 天然气
            result.addAll(getGasParameterConfig());
        } else if (energyType == 3) { // 压缩空气
            result.addAll(getAirParameterConfig());
        } else if (energyType == 4) { // 企业用水
            result.addAll(getWaterParameterConfig());
        }
        
        return result;
    }
    
    @Override
    public TimeSeriesResultVO getTimeSeriesData(TimeSeriesQueryVO query) {
        log.info("查询时序数据，参数：{}", query);
        
        try {
            // 1. 参数验证
            validateTimeSeriesQuery(query);
            
            // 2. 构建时间范围
            String[] timeRange = buildTimeRange(query.getQueryDate(), query.getTimeGranularity());
            String startTime = query.getStartTime() != null ? query.getStartTime() : timeRange[0];
            String endTime = query.getEndTime() != null ? query.getEndTime() : timeRange[1];
            
            // 3. 构建InfluxDB查询语句
            String sql = queryBuilder.buildTimeSeriesQuery(
                query.getModuleIds(), 
                query.getParameters(), 
                query.getTimeGranularity(), 
                startTime, 
                endTime
            );
            
            // 4. 执行查询
            QueryResult queryResult = influxDB.query(new Query(sql, "hist"));
            
            // 5. 处理查询结果
            TimeSeriesResultVO result = processQueryResult(queryResult, query);
            
            log.info("时序数据查询完成，返回数据点数：{}", 
                result.getChartData() != null ? result.getChartData().getTimeLabels().size() : 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("查询时序数据失败", e);
            throw new RuntimeException("查询时序数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ModuleStatusVO> getCurrentStatus(List<String> moduleIds, List<Integer> parameters) {
        log.info("获取实时状态，仪表ID：{}，参数：{}", moduleIds, parameters);
        
        List<ModuleStatusVO> result = new ArrayList<>();
        
        try {
            for (String moduleId : moduleIds) {
                ModuleStatusVO statusVO = new ModuleStatusVO();
                
                // 1. 获取仪表基础信息
                TbModule module = getModuleByModuleId(moduleId);
                if (module == null) {
                    log.warn("未找到仪表：{}", moduleId);
                    continue;
                }
                
                statusVO.setModuleId(moduleId);
                statusVO.setModuleName(module.getModuleName());
                
                // 2. 获取实时数据并判断在线状态
                List<ModuleStatusVO.ParameterStatusVO> parameterStatuses = new ArrayList<>();
                boolean isOnline = false;
                String lastUpdateTime = null;
                
                if (module.getEnergyType() == 1) { // 电力
                    TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(moduleId);
                    if (eleData != null) {
                        isOnline = isDeviceOnline(eleData.getEquElectricDT());
                        lastUpdateTime = eleData.getEquElectricDT() != null ?
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(eleData.getEquElectricDT()) : null;
                        parameterStatuses = buildElectricParameterStatus(eleData, parameters);
                    }
                } else { // 其他能源
                    TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(moduleId);
                    if (energyData != null) {
                        isOnline = isDeviceOnline(energyData.getEquEnergyDt());
                        lastUpdateTime = energyData.getEquEnergyDt() != null ?
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(energyData.getEquEnergyDt()) : null;
                        parameterStatuses = buildEnergyParameterStatus(energyData, parameters);
                    }
                }
                
                statusVO.setIsOnline(isOnline);
                statusVO.setLastUpdateTime(lastUpdateTime);
                statusVO.setParameters(parameterStatuses);
                
                result.add(statusVO);
            }
            
        } catch (Exception e) {
            log.error("获取实时状态失败", e);
            throw new RuntimeException("获取实时状态失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 根据维度编码获取部门ID列表
     */
    private List<String> getDepartIdsByDimensionCode(String dimensionCode, Boolean includeChildren) {
        List<String> departIds = new ArrayList<>();
        
        try {
            if (includeChildren) {
                // 包含子维度：查询以该维度编码开头的所有部门
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.likeRight("org_code", dimensionCode);
                List<SysDepart> departs = sysDepartService.list(queryWrapper);
                
                departIds = departs.stream()
                        .filter(depart -> StringUtils.hasText(depart.getId()))
                        .map(SysDepart::getId)
                        .collect(Collectors.toList());
                        
                log.info("维度编码 {} 包含子维度查询：部门数量={}", dimensionCode, departIds.size());
            } else {
                // 不包含子维度：只查询当前维度
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("org_code", dimensionCode);
                SysDepart depart = sysDepartService.getOne(queryWrapper);
                
                if (depart != null && StringUtils.hasText(depart.getId())) {
                    departIds.add(depart.getId());
                    log.info("维度编码 {} 精确查询：部门ID={}", dimensionCode, depart.getId());
                }
            }
        } catch (Exception e) {
            log.error("根据维度编码查询部门ID失败: {}", dimensionCode, e);
        }
        
        return departIds;
    }
    
    /**
     * 根据部门ID列表和能源类型查询仪表
     */
    private List<TbModule> getModulesByDepartIdsAndEnergyType(List<String> departIds, Integer energyType) {
        List<TbModule> modules = new ArrayList<>();
        
        for (String departId : departIds) {
            List<TbModule> moduleList = tbModuleMapper.selectModulesByOrgCode(departId);
            
            // 根据能源类型过滤
            List<TbModule> filteredModules = moduleList.stream()
                    .filter(module -> energyType.equals(module.getEnergyType()))
                    .collect(Collectors.toList());
                    
            modules.addAll(filteredModules);
        }
        
        // 去重并排序
        return modules.stream()
                .collect(Collectors.toMap(TbModule::getModuleId, m -> m, (existing, replacement) -> existing))
                .values()
                .stream()
                .sorted(Comparator.comparing(TbModule::getModuleName))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取部门名称映射
     */
    private Map<String, String> getDepartNameMapByIds(List<String> departIds) {
        Map<String, String> departNameMap = new HashMap<>();
        
        for (String departId : departIds) {
            SysDepart depart = sysDepartService.getById(departId);
            if (depart != null) {
                departNameMap.put(departId, depart.getDepartName());
            }
        }
        
        return departNameMap;
    }
    
    /**
     * 获取电力参数配置
     */
    private List<ParameterConfigVO> getElectricParameterConfig() {
        List<ParameterConfigVO> configs = new ArrayList<>();
        
        configs.add(createParameterConfig(1, "A相电流", "IA", "A", "电流", true, 1));
        configs.add(createParameterConfig(2, "B相电流", "IB", "A", "电流", true, 1));
        configs.add(createParameterConfig(3, "C相电流", "IC", "A", "电流", false, 1));
        configs.add(createParameterConfig(4, "A相电压", "UA", "V", "电压", false, 1));
        configs.add(createParameterConfig(5, "B相电压", "UB", "V", "电压", false, 1));
        configs.add(createParameterConfig(6, "C相电压", "UC", "V", "电压", false, 1));
        configs.add(createParameterConfig(7, "总有功功率", "PP", "kW", "功率", true, 1));
        configs.add(createParameterConfig(8, "总无功功率", "QQ", "kVar", "功率", false, 1));
        configs.add(createParameterConfig(9, "总视在功率", "SS", "kVA", "功率", false, 1));
        configs.add(createParameterConfig(10, "总功率因数", "PFS", "", "功率因数", false, 1));
        configs.add(createParameterConfig(11, "频率", "HZ", "Hz", "频率", false, 1));
        configs.add(createParameterConfig(12, "正向有功总电能", "KWH", "kWh", "电能", false, 1));
        
        return configs;
    }
    
    /**
     * 获取天然气参数配置
     */
    private List<ParameterConfigVO> getGasParameterConfig() {
        List<ParameterConfigVO> configs = new ArrayList<>();
        
        configs.add(createParameterConfig(20, "温度", "TEMP", "℃", "温度", true, 2));
        configs.add(createParameterConfig(21, "压力", "PRESS", "MPa", "压力", true, 2));
        configs.add(createParameterConfig(22, "瞬时流量", "FLOW", "m³/h", "流量", true, 2));
        configs.add(createParameterConfig(23, "累计值", "ACCUM", "m³", "累计", false, 2));
        
        return configs;
    }
    
    /**
     * 获取压缩空气参数配置
     */
    private List<ParameterConfigVO> getAirParameterConfig() {
        List<ParameterConfigVO> configs = new ArrayList<>();
        
        configs.add(createParameterConfig(20, "温度", "TEMP", "℃", "温度", false, 3));
        configs.add(createParameterConfig(21, "压力", "PRESS", "MPa", "压力", true, 3));
        configs.add(createParameterConfig(22, "瞬时流量", "FLOW", "m³/h", "流量", true, 3));
        configs.add(createParameterConfig(23, "累计值", "ACCUM", "m³", "累计", false, 3));
        
        return configs;
    }
    
    /**
     * 获取企业用水参数配置
     */
    private List<ParameterConfigVO> getWaterParameterConfig() {
        List<ParameterConfigVO> configs = new ArrayList<>();
        
        configs.add(createParameterConfig(20, "温度", "TEMP", "℃", "温度", false, 4));
        configs.add(createParameterConfig(21, "压力", "PRESS", "MPa", "压力", true, 4));
        configs.add(createParameterConfig(22, "瞬时流量", "FLOW", "m³/h", "流量", true, 4));
        configs.add(createParameterConfig(23, "累计值", "ACCUM", "m³", "累计", false, 4));
        
        return configs;
    }
    
    /**
     * 创建参数配置
     */
    private ParameterConfigVO createParameterConfig(Integer paramCode, String paramName, String fieldName, 
                                                   String unit, String category, Boolean isDefault, Integer energyType) {
        ParameterConfigVO config = new ParameterConfigVO();
        config.setParamCode(paramCode);
        config.setParamName(paramName);
        config.setFieldName(fieldName);
        config.setUnit(unit);
        config.setCategory(category);
        config.setIsDefault(isDefault);
        config.setEnergyType(energyType);
        return config;
    }
    
    /**
     * 验证时序查询参数
     */
    private void validateTimeSeriesQuery(TimeSeriesQueryVO query) {
        if (query.getModuleIds() == null || query.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表ID列表不能为空");
        }
        if (query.getParameters() == null || query.getParameters().isEmpty()) {
            throw new IllegalArgumentException("参数编码列表不能为空");
        }
        if (!Arrays.asList("day", "month", "year").contains(query.getTimeGranularity())) {
            throw new IllegalArgumentException("时间粒度必须是 day、month 或 year");
        }
        if (query.getQueryDate() == null || query.getQueryDate().trim().isEmpty()) {
            throw new IllegalArgumentException("查询日期不能为空");
        }
    }
    
    /**
     * 构建时间范围
     */
    private String[] buildTimeRange(String queryDate, String timeGranularity) {
        String startTime, endTime;
        
        switch (timeGranularity) {
            case "day":
                startTime = queryDate + " 00:00:00";
                endTime = queryDate + " 23:59:59";
                break;
            case "month":
                // 获取月份的第一天和最后一天
                String[] dateParts = queryDate.split("-");
                String year = dateParts[0];
                String month = dateParts[1];
                startTime = year + "-" + month + "-01 00:00:00";
                
                // 计算月份最后一天
                LocalDateTime lastDay = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, 0, 0, 0)
                        .plusMonths(1).minusDays(1);
                endTime = lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
                break;
            case "year":
                String yearStr = queryDate.substring(0, 4);
                startTime = yearStr + "-01-01 00:00:00";
                endTime = yearStr + "-12-31 23:59:59";
                break;
            default:
                throw new IllegalArgumentException("不支持的时间粒度: " + timeGranularity);
        }
        
        return new String[]{startTime, endTime};
    }
    
    /**
     * 处理查询结果
     */
    private TimeSeriesResultVO processQueryResult(QueryResult queryResult, TimeSeriesQueryVO query) {
        TimeSeriesResultVO result = new TimeSeriesResultVO();

        // 生成时间标签
        List<String> timeLabels = generateTimeLabels(query.getQueryDate(), query.getTimeGranularity());

        // 初始化图表数据
        TimeSeriesResultVO.ChartDataVO chartData = new TimeSeriesResultVO.ChartDataVO();
        chartData.setTimeLabels(timeLabels);

        List<TimeSeriesResultVO.SeriesVO> seriesList = new ArrayList<>();
        List<TimeSeriesResultVO.TableRowVO> tableData = new ArrayList<>();

        // 获取仪表信息映射
        Map<String, String> moduleNameMap = getModuleNameMap(query.getModuleIds());
        Map<Integer, ParameterConfigVO> paramConfigMap = getParameterConfigMap(query.getParameters());

        // 处理InfluxDB查询结果
        if (queryResult != null && queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
            QueryResult.Result influxResult = queryResult.getResults().get(0);
            if (influxResult.getSeries() != null && !influxResult.getSeries().isEmpty()) {

                // 按仪表和参数组织数据
                Map<String, Map<Integer, List<Double>>> dataMap = organizeInfluxData(influxResult, query);

                // 生成图表系列数据
                String[] colors = {"#1890ff", "#52c41a", "#fa8c16", "#722ed1", "#eb2f96", "#13c2c2", "#faad14", "#f5222d"};
                int colorIndex = 0;

                for (String moduleId : query.getModuleIds()) {
                    String moduleName = moduleNameMap.getOrDefault(moduleId, moduleId);

                    for (Integer paramCode : query.getParameters()) {
                        ParameterConfigVO paramConfig = paramConfigMap.get(paramCode);
                        if (paramConfig == null) continue;

                        TimeSeriesResultVO.SeriesVO series = new TimeSeriesResultVO.SeriesVO();
                        series.setModuleId(moduleId);
                        series.setModuleName(moduleName);
                        series.setParamCode(paramCode);
                        series.setParamName(paramConfig.getParamName());
                        series.setUnit(paramConfig.getUnit());
                        series.setColor(colors[colorIndex % colors.length]);

                        // 获取数据，如果没有数据则填充默认值
                        List<Double> data = dataMap.getOrDefault(moduleId, new HashMap<>())
                                .getOrDefault(paramCode, generateDefaultData(timeLabels.size()));
                        series.setData(data);

                        seriesList.add(series);
                        colorIndex++;
                    }
                }
            }
        }

        // 如果没有从InfluxDB获取到数据，生成模拟数据用于测试
        if (seriesList.isEmpty()) {
            seriesList = generateMockSeriesData(query, moduleNameMap, paramConfigMap);
        }

        chartData.setSeries(seriesList);
        result.setChartData(chartData);

        // 生成表格数据
        tableData = generateTableData(timeLabels, query, moduleNameMap, paramConfigMap);
        result.setTableData(tableData);

        // 设置汇总信息
        TimeSeriesResultVO.SummaryVO summary = new TimeSeriesResultVO.SummaryVO();
        summary.setTotalDataPoints(timeLabels.size() * query.getModuleIds().size() * query.getParameters().size());
        summary.setModuleCount(query.getModuleIds().size());
        summary.setParameterCount(query.getParameters().size());
        summary.setGranularity(getGranularityDescription(query.getTimeGranularity()));

        String[] timeRange = buildTimeRange(query.getQueryDate(), query.getTimeGranularity());
        summary.setTimeRange(timeRange[0] + " ~ " + timeRange[1]);
        result.setSummary(summary);

        return result;
    }
    
    /**
     * 生成时间标签
     */
    private List<String> generateTimeLabels(String queryDate, String timeGranularity) {
        List<String> labels = new ArrayList<>();
        
        switch (timeGranularity) {
            case "day":
                for (int i = 0; i < 24; i++) {
                    labels.add(String.format("%02d:00", i));
                }
                break;
            case "month":
                // 根据月份生成每天的标签
                String[] dateParts = queryDate.split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                LocalDateTime firstDay = LocalDateTime.of(year, month, 1, 0, 0, 0);
                LocalDateTime lastDay = firstDay.plusMonths(1).minusDays(1);
                
                for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
                    labels.add(String.format("%02d-%02d", month, day));
                }
                break;
            case "year":
                for (int monthNum = 1; monthNum <= 12; monthNum++) {
                    labels.add(String.format("%s-%02d", queryDate.substring(0, 4), monthNum));
                }
                break;
        }
        
        return labels;
    }
    
    /**
     * 获取粒度描述
     */
    private String getGranularityDescription(String timeGranularity) {
        switch (timeGranularity) {
            case "day": return "每小时";
            case "month": return "每天";
            case "year": return "每月";
            default: return "未知";
        }
    }
    
    /**
     * 根据仪表ID获取仪表信息
     */
    private TbModule getModuleByModuleId(String moduleId) {
        QueryWrapper<TbModule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("module_id", moduleId);
        queryWrapper.eq("isaction", "Y");
        return tbModuleMapper.selectOne(queryWrapper);
    }
    
    /**
     * 判断设备是否在线
     */
    private boolean isDeviceOnline(Date lastUpdateTime) {
        if (lastUpdateTime == null) {
            return false;
        }
        
        // 如果最后更新时间在5分钟内，认为设备在线
        long diffMinutes = (System.currentTimeMillis() - lastUpdateTime.getTime()) / (1000 * 60);
        return diffMinutes <= 5;
    }
    
    /**
     * 构建电力参数状态
     */
    private List<ModuleStatusVO.ParameterStatusVO> buildElectricParameterStatus(TbEquEleData eleData, List<Integer> parameters) {
        List<ModuleStatusVO.ParameterStatusVO> statuses = new ArrayList<>();
        
        for (Integer paramCode : parameters) {
            ModuleStatusVO.ParameterStatusVO status = new ModuleStatusVO.ParameterStatusVO();
            status.setParamCode(paramCode);
            status.setUpdateTime(eleData.getEquElectricDT() != null ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(eleData.getEquElectricDT()) : null);
            status.setStatus("normal");
            
            switch (paramCode) {
                case 1:
                    status.setParamName("A相电流");
                    status.setCurrentValue(eleData.getIA() != null ? eleData.getIA().doubleValue() : null);
                    status.setUnit("A");
                    break;
                case 2:
                    status.setParamName("B相电流");
                    status.setCurrentValue(eleData.getIB() != null ? eleData.getIB().doubleValue() : null);
                    status.setUnit("A");
                    break;
                case 3:
                    status.setParamName("C相电流");
                    status.setCurrentValue(eleData.getIC() != null ? eleData.getIC().doubleValue() : null);
                    status.setUnit("A");
                    break;
                case 4:
                    status.setParamName("A相电压");
                    status.setCurrentValue(eleData.getUA() != null ? eleData.getUA().doubleValue() : null);
                    status.setUnit("V");
                    break;
                case 5:
                    status.setParamName("B相电压");
                    status.setCurrentValue(eleData.getUB() != null ? eleData.getUB().doubleValue() : null);
                    status.setUnit("V");
                    break;
                case 6:
                    status.setParamName("C相电压");
                    status.setCurrentValue(eleData.getUC() != null ? eleData.getUC().doubleValue() : null);
                    status.setUnit("V");
                    break;
                case 7:
                    status.setParamName("总有功功率");
                    status.setCurrentValue(eleData.getPp() != null ? eleData.getPp().doubleValue() : null);
                    status.setUnit("kW");
                    break;
                case 8:
                    status.setParamName("总无功功率");
                    status.setCurrentValue(eleData.getQq() != null ? eleData.getQq().doubleValue() : null);
                    status.setUnit("kVar");
                    break;
                case 9:
                    status.setParamName("总视在功率");
                    status.setCurrentValue(eleData.getSs() != null ? eleData.getSs().doubleValue() : null);
                    status.setUnit("kVA");
                    break;
                case 10:
                    status.setParamName("总功率因数");
                    status.setCurrentValue(eleData.getPFS() != null ? eleData.getPFS().doubleValue() : null);
                    status.setUnit("");
                    break;
                case 11:
                    status.setParamName("频率");
                    status.setCurrentValue(eleData.getHZ() != null ? eleData.getHZ().doubleValue() : null);
                    status.setUnit("Hz");
                    break;
                case 12:
                    status.setParamName("正向有功总电能");
                    status.setCurrentValue(eleData.getKWH() != null ? eleData.getKWH().doubleValue() : null);
                    status.setUnit("kWh");
                    break;
                // 可以继续添加其他参数
                default:
                    status.setParamName("未知参数");
                    status.setCurrentValue(null);
                    status.setUnit("");
                    break;
            }
            
            statuses.add(status);
        }
        
        return statuses;
    }
    
    /**
     * 构建能源参数状态
     */
    private List<ModuleStatusVO.ParameterStatusVO> buildEnergyParameterStatus(TbEquEnergyData energyData, List<Integer> parameters) {
        List<ModuleStatusVO.ParameterStatusVO> statuses = new ArrayList<>();
        
        for (Integer paramCode : parameters) {
            ModuleStatusVO.ParameterStatusVO status = new ModuleStatusVO.ParameterStatusVO();
            status.setParamCode(paramCode);
            status.setUpdateTime(energyData.getEquEnergyDt() != null ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(energyData.getEquEnergyDt()) : null);
            status.setStatus("normal");
            
            switch (paramCode) {
                case 20:
                    status.setParamName("温度");
                    status.setCurrentValue(energyData.getEnergyTemperature() != null ? energyData.getEnergyTemperature().doubleValue() : null);
                    status.setUnit("℃");
                    break;
                case 21:
                    status.setParamName("压力");
                    status.setCurrentValue(energyData.getEnergyPressure() != null ? energyData.getEnergyPressure().doubleValue() : null);
                    status.setUnit("MPa");
                    break;
                case 22:
                    status.setParamName("瞬时流量");
                    status.setCurrentValue(energyData.getEnergyWinkvalue() != null ? energyData.getEnergyWinkvalue().doubleValue() : null);
                    status.setUnit("m³/h");
                    break;
                case 23:
                    status.setParamName("累计值");
                    status.setCurrentValue(energyData.getEnergyAccumulatevalue() != null ? energyData.getEnergyAccumulatevalue().doubleValue() : null);
                    status.setUnit("m³");
                    break;
                default:
                    status.setParamName("未知参数");
                    status.setCurrentValue(null);
                    status.setUnit("");
                    break;
            }
            
            statuses.add(status);
        }
        
        return statuses;
    }

    /**
     * 获取仪表名称映射
     */
    private Map<String, String> getModuleNameMap(List<String> moduleIds) {
        Map<String, String> moduleNameMap = new HashMap<>();

        for (String moduleId : moduleIds) {
            TbModule module = getModuleByModuleId(moduleId);
            if (module != null) {
                moduleNameMap.put(moduleId, module.getModuleName());
            } else {
                moduleNameMap.put(moduleId, moduleId); // 如果找不到，使用ID作为名称
            }
        }

        return moduleNameMap;
    }

    /**
     * 获取参数配置映射
     */
    private Map<Integer, ParameterConfigVO> getParameterConfigMap(List<Integer> parameters) {
        Map<Integer, ParameterConfigVO> paramConfigMap = new HashMap<>();

        // 获取所有参数配置
        List<ParameterConfigVO> allConfigs = new ArrayList<>();
        allConfigs.addAll(getElectricParameterConfig());
        allConfigs.addAll(getGasParameterConfig());
        allConfigs.addAll(getAirParameterConfig());
        allConfigs.addAll(getWaterParameterConfig());

        // 过滤需要的参数
        for (ParameterConfigVO config : allConfigs) {
            if (parameters.contains(config.getParamCode())) {
                paramConfigMap.put(config.getParamCode(), config);
            }
        }

        return paramConfigMap;
    }

    /**
     * 组织InfluxDB数据
     */
    private Map<String, Map<Integer, List<Double>>> organizeInfluxData(QueryResult.Result influxResult, TimeSeriesQueryVO query) {
        Map<String, Map<Integer, List<Double>>> dataMap = new HashMap<>();

        // 这里需要根据实际的InfluxDB返回格式来解析
        // 由于InfluxDB的数据格式比较复杂，这里提供一个基础框架
        // 实际使用时需要根据具体的数据格式来调整

        try {
            for (QueryResult.Series series : influxResult.getSeries()) {
                // 解析tagname，提取moduleId和参数
                String tagname = series.getTags() != null ? series.getTags().get("tagname") : "";
                if (tagname.contains("#")) {
                    String[] parts = tagname.split("#");
                    String moduleId = parts[0].toLowerCase();
                    String fieldName = parts[1];

                    // 根据字段名获取参数编码
                    Integer paramCode = getParamCodeByFieldName(fieldName);
                    if (paramCode != null && query.getModuleIds().contains(moduleId) && query.getParameters().contains(paramCode)) {

                        // 初始化数据结构
                        dataMap.putIfAbsent(moduleId, new HashMap<>());

                        // 解析数据值
                        List<Double> values = new ArrayList<>();
                        if (series.getValues() != null) {
                            for (List<Object> value : series.getValues()) {
                                if (value.size() > 1 && value.get(1) != null) {
                                    try {
                                        values.add(Double.parseDouble(value.get(1).toString()));
                                    } catch (NumberFormatException e) {
                                        values.add(null);
                                    }
                                } else {
                                    values.add(null);
                                }
                            }
                        }

                        dataMap.get(moduleId).put(paramCode, values);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析InfluxDB数据失败", e);
        }

        return dataMap;
    }

    /**
     * 根据字段名获取参数编码
     */
    private Integer getParamCodeByFieldName(String fieldName) {
        switch (fieldName.toUpperCase()) {
            case "IA": return 1;
            case "IB": return 2;
            case "IC": return 3;
            case "UA": return 4;
            case "UB": return 5;
            case "UC": return 6;
            case "PP": return 7;
            case "QQ": return 8;
            case "SS": return 9;
            case "PFS": return 10;
            case "HZ": return 11;
            case "KWH": return 12;
            case "TEMP": return 20;
            case "PRESS": return 21;
            case "FLOW": return 22;
            case "ACCUM": return 23;
            default: return null;
        }
    }

    /**
     * 生成默认数据
     */
    private List<Double> generateDefaultData(int size) {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(null);
        }
        return data;
    }

    /**
     * 生成模拟数据用于测试
     */
    private List<TimeSeriesResultVO.SeriesVO> generateMockSeriesData(TimeSeriesQueryVO query,
                                                                    Map<String, String> moduleNameMap,
                                                                    Map<Integer, ParameterConfigVO> paramConfigMap) {
        List<TimeSeriesResultVO.SeriesVO> seriesList = new ArrayList<>();
        String[] colors = {"#1890ff", "#52c41a", "#fa8c16", "#722ed1", "#eb2f96", "#13c2c2", "#faad14", "#f5222d"};
        int colorIndex = 0;

        List<String> timeLabels = generateTimeLabels(query.getQueryDate(), query.getTimeGranularity());

        for (String moduleId : query.getModuleIds()) {
            String moduleName = moduleNameMap.getOrDefault(moduleId, moduleId);

            for (Integer paramCode : query.getParameters()) {
                ParameterConfigVO paramConfig = paramConfigMap.get(paramCode);
                if (paramConfig == null) continue;

                TimeSeriesResultVO.SeriesVO series = new TimeSeriesResultVO.SeriesVO();
                series.setModuleId(moduleId);
                series.setModuleName(moduleName);
                series.setParamCode(paramCode);
                series.setParamName(paramConfig.getParamName());
                series.setUnit(paramConfig.getUnit());
                series.setColor(colors[colorIndex % colors.length]);

                // 生成模拟数据
                List<Double> data = generateMockData(timeLabels.size(), paramCode);
                series.setData(data);

                seriesList.add(series);
                colorIndex++;
            }
        }

        return seriesList;
    }

    /**
     * 生成模拟数据
     */
    private List<Double> generateMockData(int size, Integer paramCode) {
        List<Double> data = new ArrayList<>();
        Random random = new Random();

        // 根据参数类型生成不同范围的模拟数据
        double baseValue = getBaseValueByParamCode(paramCode);
        double variance = baseValue * 0.1; // 10%的变化范围

        for (int i = 0; i < size; i++) {
            double value = baseValue + (random.nextGaussian() * variance);
            data.add(Math.max(0, value)); // 确保值不为负
        }

        return data;
    }

    /**
     * 根据参数编码获取基础值
     */
    private double getBaseValueByParamCode(Integer paramCode) {
        switch (paramCode) {
            case 1: case 2: case 3: return 95.0;  // 电流 A
            case 4: case 5: case 6: return 220.0; // 电压 V
            case 7: return 50.0;                  // 有功功率 kW
            case 8: return 20.0;                  // 无功功率 kVar
            case 9: return 55.0;                  // 视在功率 kVA
            case 10: return 0.9;                  // 功率因数
            case 11: return 50.0;                 // 频率 Hz
            case 12: return 1000.0;               // 电能 kWh
            case 20: return 25.0;                 // 温度 ℃
            case 21: return 0.5;                  // 压力 MPa
            case 22: return 100.0;                // 流量 m³/h
            case 23: return 5000.0;               // 累计值 m³
            default: return 100.0;
        }
    }

    /**
     * 生成表格数据
     */
    private List<TimeSeriesResultVO.TableRowVO> generateTableData(List<String> timeLabels,
                                                                 TimeSeriesQueryVO query,
                                                                 Map<String, String> moduleNameMap,
                                                                 Map<Integer, ParameterConfigVO> paramConfigMap) {
        List<TimeSeriesResultVO.TableRowVO> tableData = new ArrayList<>();

        for (int i = 0; i < timeLabels.size(); i++) {
            TimeSeriesResultVO.TableRowVO row = new TimeSeriesResultVO.TableRowVO();
            row.setTime(timeLabels.get(i));
            row.setTimeLabel(generateFullTimeLabel(query.getQueryDate(), query.getTimeGranularity(), i));

            List<TimeSeriesResultVO.ModuleDataVO> modules = new ArrayList<>();

            for (String moduleId : query.getModuleIds()) {
                TimeSeriesResultVO.ModuleDataVO moduleData = new TimeSeriesResultVO.ModuleDataVO();
                moduleData.setModuleId(moduleId);
                moduleData.setModuleName(moduleNameMap.getOrDefault(moduleId, moduleId));

                List<TimeSeriesResultVO.ParameterDataVO> parameters = new ArrayList<>();

                for (Integer paramCode : query.getParameters()) {
                    ParameterConfigVO paramConfig = paramConfigMap.get(paramCode);
                    if (paramConfig == null) continue;

                    TimeSeriesResultVO.ParameterDataVO paramData = new TimeSeriesResultVO.ParameterDataVO();
                    paramData.setParamCode(paramCode);
                    paramData.setParamName(paramConfig.getParamName());
                    paramData.setUnit(paramConfig.getUnit());

                    // 这里应该从实际数据中获取值，暂时使用模拟数据
                    paramData.setValue(getBaseValueByParamCode(paramCode));

                    parameters.add(paramData);
                }

                moduleData.setParameters(parameters);
                modules.add(moduleData);
            }

            row.setModules(modules);
            tableData.add(row);
        }

        return tableData;
    }

    /**
     * 生成完整时间标签
     */
    private String generateFullTimeLabel(String queryDate, String timeGranularity, int index) {
        switch (timeGranularity) {
            case "day":
                return queryDate + " " + String.format("%02d:00", index);
            case "month":
                String[] dateParts = queryDate.split("-");
                return dateParts[0] + "-" + dateParts[1] + "-" + String.format("%02d", index + 1);
            case "year":
                return queryDate.substring(0, 4) + "-" + String.format("%02d", index + 1) + "-01";
            default:
                return queryDate;
        }
    }

    /**
     * 查询负荷时序数据
     */
    @Override
    public LoadTimeSeriesResultVO getLoadTimeSeriesData(LoadTimeSeriesQueryVO query) {
        log.info("开始查询负荷时序数据，参数：{}", query);

        try {
            // 1. 参数验证
            validateLoadTimeSeriesQuery(query);

            // 2. 从MySQL获取仪表的额定功率信息
            List<TbModule> modules = getModulesByIds(query.getModuleIds());
            Map<String, Double> ratedPowerMap = modules.stream()
                .collect(Collectors.toMap(TbModule::getModuleId,
                    module -> module.getRatedPower() != null ? module.getRatedPower() : 100.0));

            // 3. 构建InfluxDB查询语句（只查询P字段）
            String influxQuery = buildLoadInfluxQuery(query);
            log.info("InfluxDB查询语句：{}", influxQuery);

            // 4. 执行查询并处理结果
            String databaseName = influxDBConfig.getCurrentMonthDatabaseName();
            QueryResult queryResult = influxDB.query(new Query(influxQuery, databaseName));

            // 5. 处理查询结果并计算负荷率
            LoadTimeSeriesResultVO result = processLoadTimeSeriesResult(queryResult, query, modules, ratedPowerMap);

            log.info("负荷时序数据查询完成，仪表数量：{}", query.getModuleIds().size());
            return result;

        } catch (Exception e) {
            log.error("查询负荷时序数据失败", e);
            throw new RuntimeException("查询负荷时序数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时负荷状态
     */
    @Override
    public List<ModuleLoadStatusVO> getCurrentLoadStatus(List<String> moduleIds) {
        log.info("开始获取实时负荷状态，仪表ID：{}", moduleIds);

        try {
            List<ModuleLoadStatusVO> result = new ArrayList<>();

            // 1. 获取仪表基本信息
            List<TbModule> modules = getModulesByIds(moduleIds);
            Map<String, TbModule> moduleMap = modules.stream()
                .collect(Collectors.toMap(TbModule::getModuleId, module -> module));

            // 2. 查询最新的功率数据（从tb_equ_ele_data的P字段）
            for (String moduleId : moduleIds) {
                TbModule module = moduleMap.get(moduleId);
                if (module == null) continue;

                ModuleLoadStatusVO status = new ModuleLoadStatusVO();
                status.setModuleId(moduleId);
                status.setModuleName(module.getModuleName());
                status.setRatedPower(module.getRatedPower() != null ? module.getRatedPower() : 100.0);
                status.setPowerUnit("kW");
                status.setLoadRateUnit("%");

                // 查询最新功率数据
                TbEquEleData latestData = tbEquEleDataMapper.selectLatestDataByModuleId(moduleId);
                if (latestData != null && latestData.getPp() != null) {
                    status.setCurrentPower(latestData.getPp().doubleValue());
                    status.setLoadRate(latestData.getPp().doubleValue() / status.getRatedPower() * 100);

                    // 使用现有的设备在线判断逻辑
                    boolean isOnline = isDeviceOnline(latestData.getEquElectricDT());
                    status.setIsOnline(isOnline);
                    status.setLastUpdateTime(latestData.getEquElectricDT() != null ?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(latestData.getEquElectricDT()) : "");

                    if (isOnline) {
                        status.setStatus("normal");
                        // 判断负荷等级
                        double loadRate = status.getLoadRate();
                        if (loadRate < 50) {
                            status.setLoadLevel("低负荷");
                        } else if (loadRate <= 80) {
                            status.setLoadLevel("中等负荷");
                        } else {
                            status.setLoadLevel("高负荷");
                        }
                    } else {
                        status.setStatus("offline");
                        status.setLoadLevel("离线");
                    }
                } else {
                    status.setCurrentPower(0.0);
                    status.setLoadRate(0.0);
                    status.setIsOnline(false);
                    status.setLastUpdateTime("");
                    status.setStatus("offline");
                    status.setLoadLevel("离线");
                }

                result.add(status);
            }

            log.info("实时负荷状态查询完成，数量：{}", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取实时负荷状态失败", e);
            throw new RuntimeException("获取实时负荷状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取负荷数据表格
     */
    @Override
    public LoadTableResultVO getLoadTableData(LoadTableQueryVO query) {
        log.info("开始获取负荷数据表格，参数：{}", query);

        try {
            // 1. 参数验证
            validateLoadTableQuery(query);

            // 2. 从MySQL获取仪表基本信息和额定功率
            List<TbModule> modules = getModulesByIds(query.getModuleIds());
            Map<String, TbModule> moduleMap = modules.stream()
                .collect(Collectors.toMap(TbModule::getModuleId, module -> module));

            // 3. 构建InfluxDB查询语句，查询指定时间范围内的P字段数据
            String influxQuery = buildLoadTableInfluxQuery(query);
            log.info("InfluxDB查询语句：{}", influxQuery);

            // 4. 执行查询并处理结果
            String databaseName = influxDBConfig.getCurrentMonthDatabaseName();
            QueryResult queryResult = influxDB.query(new Query(influxQuery, databaseName));

            // 5. 处理查询结果并计算统计数据
            LoadTableResultVO result = processLoadTableResult(queryResult, query, modules);

            log.info("负荷数据表格查询完成，数据条数：{}", result.getTableData().size());
            return result;

        } catch (Exception e) {
            log.error("获取负荷数据表格失败", e);
            throw new RuntimeException("获取负荷数据表格失败: " + e.getMessage());
        }
    }

    /**
     * 验证负荷时序查询参数
     */
    private void validateLoadTimeSeriesQuery(LoadTimeSeriesQueryVO query) {
        if (query.getModuleIds() == null || query.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表ID列表不能为空");
        }
        if (!StringUtils.hasText(query.getTimeGranularity())) {
            throw new IllegalArgumentException("时间粒度不能为空");
        }
        if (!StringUtils.hasText(query.getQueryDate())) {
            throw new IllegalArgumentException("查询日期不能为空");
        }
    }

    /**
     * 验证负荷表格查询参数
     */
    private void validateLoadTableQuery(LoadTableQueryVO query) {
        if (query.getModuleIds() == null || query.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表ID列表不能为空");
        }
        if (!StringUtils.hasText(query.getTimeType())) {
            throw new IllegalArgumentException("时间类型不能为空");
        }
        if (!StringUtils.hasText(query.getStartTime())) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (!StringUtils.hasText(query.getEndTime())) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
    }

    /**
     * 根据仪表ID列表获取仪表信息
     */
    private List<TbModule> getModulesByIds(List<String> moduleIds) {
        QueryWrapper<TbModule> wrapper = new QueryWrapper<>();
        wrapper.in("module_id", moduleIds);
        wrapper.eq("isaction", "Y"); // 只查询启用的仪表
        return tbModuleMapper.selectList(wrapper);
    }

    /**
     * 构建负荷InfluxDB查询语句
     */
    private String buildLoadInfluxQuery(LoadTimeSeriesQueryVO query) {
        StringBuilder sql = new StringBuilder();

        // 根据时间粒度设置GROUP BY间隔
        String groupByTime;
        switch (query.getTimeGranularity()) {
            case "day":
                groupByTime = "1h";
                break;
            case "month":
                groupByTime = "1d";
                break;
            case "year":
                groupByTime = "1M";
                break;
            default:
                groupByTime = "1h";
        }

        // 构建tagname条件，查询P字段（有功功率）
        List<String> tagConditions = new ArrayList<>();
        for (String moduleId : query.getModuleIds()) {
            String tagname = moduleId.toUpperCase() + "#P";
            tagConditions.add("tagname = '" + tagname + "'");
        }
        String tagnameCondition = "(" + String.join(" OR ", tagConditions) + ")";

        sql.append("SELECT mean(value) as value FROM \"").append(influxDBConfig.getMeasurement()).append("\" WHERE ");

        // 添加时间条件
        String startTime = query.getStartTime() != null ? query.getStartTime() : query.getQueryDate() + " 00:00:00";
        String endTime = query.getEndTime() != null ? query.getEndTime() : query.getQueryDate() + " 23:59:59";

        // 转换为UTC时间
        String utcStartTime = timeZoneUtil.convertBeijingToUTC(startTime);
        String utcEndTime = timeZoneUtil.convertBeijingToUTC(endTime);

        sql.append("time >= '").append(utcStartTime).append("' ");
        sql.append("AND time <= '").append(utcEndTime).append("' ");

        // 添加tagname条件
        sql.append("AND ").append(tagnameCondition).append(" ");

        // 添加状态条件
        sql.append("AND status = 1 ");

        // 添加GROUP BY
        sql.append("GROUP BY time(").append(groupByTime).append("), tagname ");
        sql.append("ORDER BY time ASC");

        return sql.toString();
    }

    /**
     * 从tagname中提取moduleId
     */
    private String extractModuleIdFromTagname(String tagname) {
        if (tagname != null && tagname.contains("#")) {
            return tagname.split("#")[0].toLowerCase();
        }
        return null;
    }

    /**
     * 构建负荷表格InfluxDB查询语句
     */
    private String buildLoadTableInfluxQuery(LoadTableQueryVO query) {
        StringBuilder sql = new StringBuilder();

        // 构建tagname条件，查询P字段（有功功率）
        List<String> tagConditions = new ArrayList<>();
        for (String moduleId : query.getModuleIds()) {
            String tagname = moduleId.toUpperCase() + "#P";
            tagConditions.add("tagname = '" + tagname + "'");
        }
        String tagnameCondition = "(" + String.join(" OR ", tagConditions) + ")";

        sql.append("SELECT value, tagname FROM \"").append(influxDBConfig.getMeasurement()).append("\" WHERE ");

        // 添加时间条件
        String utcStartTime = timeZoneUtil.convertBeijingToUTC(query.getStartTime());
        String utcEndTime = timeZoneUtil.convertBeijingToUTC(query.getEndTime());

        sql.append("time >= '").append(utcStartTime).append("' ");
        sql.append("AND time <= '").append(utcEndTime).append("' ");

        // 添加tagname条件
        sql.append("AND ").append(tagnameCondition).append(" ");

        // 添加状态条件
        sql.append("AND status = 1 ");

        sql.append("ORDER BY time ASC");

        return sql.toString();
    }

    /**
     * 处理负荷时序查询结果
     */
    private LoadTimeSeriesResultVO processLoadTimeSeriesResult(QueryResult queryResult,
            LoadTimeSeriesQueryVO query, List<TbModule> modules, Map<String, Double> ratedPowerMap) {

        LoadTimeSeriesResultVO result = new LoadTimeSeriesResultVO();

        // 创建有功功率图表数据
        LoadTimeSeriesResultVO.LoadChartDataVO powerChartData = new LoadTimeSeriesResultVO.LoadChartDataVO();
        powerChartData.setTitle("有功功率");

        // 创建负荷率图表数据
        LoadTimeSeriesResultVO.LoadChartDataVO loadRateChartData = new LoadTimeSeriesResultVO.LoadChartDataVO();
        loadRateChartData.setTitle("负荷率");

        // 处理InfluxDB查询结果
        List<String> timeLabels = new ArrayList<>();
        List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries = new ArrayList<>();
        List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries = new ArrayList<>();

        // 生成示例时间序列数据
        generateTimeSeriesData(query, modules, ratedPowerMap, timeLabels, powerSeries, loadRateSeries);

        // 设置图表数据
        powerChartData.setTimeLabels(timeLabels);
        powerChartData.setSeries(powerSeries);
        loadRateChartData.setTimeLabels(timeLabels);
        loadRateChartData.setSeries(loadRateSeries);

        result.setPowerChartData(powerChartData);
        result.setLoadRateChartData(loadRateChartData);

        // 创建汇总信息
        LoadTimeSeriesResultVO.LoadSummaryVO summary = new LoadTimeSeriesResultVO.LoadSummaryVO();
        summary.setTotalDataPoints(timeLabels.size());
        summary.setGranularity(query.getTimeGranularity());
        summary.setModuleCount(modules.size());
        summary.setDataType("负荷监控数据");
        result.setSummary(summary);

        return result;
    }

    /**
     * 处理负荷表格查询结果
     */
    private LoadTableResultVO processLoadTableResult(QueryResult queryResult,
            LoadTableQueryVO query, List<TbModule> modules) {

        LoadTableResultVO result = new LoadTableResultVO();
        List<LoadTableResultVO.LoadStatisticsRowVO> tableData = new ArrayList<>();

        // 为每个仪表计算统计数据
        for (int i = 0; i < modules.size(); i++) {
            TbModule module = modules.get(i);
            LoadTableResultVO.LoadStatisticsRowVO row = new LoadTableResultVO.LoadStatisticsRowVO();

            row.set序号(i + 1);
            row.set设备名称(module.getModuleName());

            // 这里应该从InfluxDB查询结果中计算统计数据
            // 为了简化，先设置示例数据，使用完整的日期时间格式

            // 生成基于查询时间范围的示例时间
            String baseDate = extractDateFromDateTime(query.getStartTime());

            // 为不同设备生成不同的示例数据
            double basePower = 70.0 + (i * 10); // 基础功率递增
            Double ratedPower = module.getRatedPower() != null ? module.getRatedPower() : 100.0;

            row.set最大功率(basePower + 20.25);
            row.set最大功率率((basePower + 20.25) / ratedPower * 100);
            row.set最大功率发生时间(baseDate + " " + String.format("%02d:30:00", 14 + (i % 3)));

            row.set最小功率(basePower - 15.12);
            row.set最小功率率((basePower - 15.12) / ratedPower * 100);
            row.set最小功率发生时间(baseDate + " " + String.format("%02d:30:00", 2 + (i % 3)));

            row.set平均功率(basePower + 2.45);
            row.set平均功率率((basePower + 2.45) / ratedPower * 100);

            tableData.add(row);
        }

        result.setTableData(tableData);

        // 设置分页信息
        LoadTableResultVO.PaginationVO pagination = new LoadTableResultVO.PaginationVO();
        pagination.setTotal(tableData.size());
        pagination.setPageNum(query.getPageNum());
        pagination.setPageSize(query.getPageSize());
        pagination.setPages((tableData.size() + query.getPageSize() - 1) / query.getPageSize());
        result.setPagination(pagination);

        // 设置汇总信息
        LoadTableResultVO.LoadTableSummaryVO summary = new LoadTableResultVO.LoadTableSummaryVO();
        summary.setTotalModules(modules.size());
        summary.setTimeRange(query.getStartTime() + " ~ " + query.getEndTime());
        summary.setDataType("负荷统计数据");
        result.setSummary(summary);

        return result;
    }

    /**
     * 从日期时间字符串中提取日期部分
     */
    private String extractDateFromDateTime(String dateTime) {
        if (dateTime != null && dateTime.length() >= 10) {
            return dateTime.substring(0, 10);
        }
        return "2025-07-25"; // 默认日期
    }



    /**
     * 转换对象为Double
     */
    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从InfluxDB查询真实的时间序列数据
     */
    private void generateTimeSeriesData(LoadTimeSeriesQueryVO query, List<TbModule> modules,
            Map<String, Double> ratedPowerMap, List<String> timeLabels,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries) {

        try {
            // 1. 使用原有的查询方法（查询 energy_data 表）
            String influxQuery = buildLoadInfluxQuery(query);
            log.info("查询负荷时序数据的InfluxDB语句：{}", influxQuery);

            // 2. 获取正确的数据库名称
            String databaseName = influxDBConfig.getCurrentMonthDatabaseName();
            log.info("🔍 使用数据库：{}", databaseName);

            // 2.1 执行查询
            QueryResult queryResult = influxDB.query(new Query(influxQuery, databaseName));

            // 2.2 添加调试查询，检查数据库中是否有数据
            String debugTagname = query.getModuleIds().get(0).toUpperCase() + "#P";
            String debugQuery = String.format("SELECT * FROM \"%s\" WHERE tagname = '%s' LIMIT 5",
                influxDBConfig.getMeasurement(), debugTagname);
            log.info("🔍 调试查询语句：{}", debugQuery);
            QueryResult debugResult = influxDB.query(new Query(debugQuery, databaseName));
            List<Map<String, Object>> debugList = InfluxDBUtil.parseQueryResult(debugResult);
            log.info("🔍 调试查询结果数量：{}", debugList.size());
            if (!debugList.isEmpty()) {
                log.info("🔍 调试查询第一条数据：{}", debugList.get(0));
            }

            // 2.3 检查数据库中的时间范围
            String timeRangeQuery = String.format("SELECT * FROM \"%s\" WHERE tagname = '%s' ORDER BY time DESC LIMIT 3",
                influxDBConfig.getMeasurement(), debugTagname);
            log.info("🔍 时间范围查询语句：{}", timeRangeQuery);
            QueryResult timeRangeResult = influxDB.query(new Query(timeRangeQuery, databaseName));
            List<Map<String, Object>> timeRangeList = InfluxDBUtil.parseQueryResult(timeRangeResult);
            log.info("🔍 最新数据时间范围：{}", timeRangeList.size());
            for (int i = 0; i < Math.min(3, timeRangeList.size()); i++) {
                Map<String, Object> data = timeRangeList.get(i);
                log.info("🔍 数据{}：time={}, tagname={}, value={}", i+1, data.get("time"), data.get("tagname"), data.get("value"));
            }

            // 3. 解析查询结果
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
            log.info("从InfluxDB查询到 {} 条负荷数据", resultList.size());

            // 4. 按时间和模块分组数据
            Map<String, Map<String, Double>> timeModuleDataMap = new HashMap<>();
            Set<String> allTimePoints = new TreeSet<>();

            for (Map<String, Object> data : resultList) {
                String tagname = (String) data.get("tagname");
                String timeStr = (String) data.get("time");
                Object valueObj = data.get("value"); // 查询的是value字段

                if (tagname != null && timeStr != null && valueObj != null) {
                    // 从tagname中提取moduleId (格式: YJ0001_1202#P)
                    String moduleId = extractModuleIdFromTagname(tagname);
                    if (moduleId != null && query.getModuleIds().contains(moduleId)) {
                        // 转换时间格式
                        String localTime = TimeZoneUtil.convertUTCToLocal(timeStr);
                        allTimePoints.add(localTime);

                        // 转换数值
                        Double value = convertToDouble(valueObj);
                        if (value != null) {
                            timeModuleDataMap.computeIfAbsent(localTime, k -> new HashMap<>())
                                .put(moduleId, value);
                        }
                    }
                }
            }

            // 5. 生成时间标签
            timeLabels.addAll(allTimePoints);

            // 6. 为每个仪表创建数据系列
            for (TbModule module : modules) {
                String moduleId = module.getModuleId();
                Double ratedPower = ratedPowerMap.getOrDefault(moduleId, 100.0);

                // 创建有功功率系列
                LoadTimeSeriesResultVO.LoadSeriesVO powerSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
                powerSeriesItem.setModuleId(moduleId);
                powerSeriesItem.setModuleName(module.getModuleName());
                powerSeriesItem.setUnit("kW");

                // 创建负荷率系列
                LoadTimeSeriesResultVO.LoadSeriesVO loadRateSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
                loadRateSeriesItem.setModuleId(moduleId);
                loadRateSeriesItem.setModuleName(module.getModuleName());
                loadRateSeriesItem.setUnit("%");

                // 填充数据
                List<Double> powerData = new ArrayList<>();
                List<Double> loadRateData = new ArrayList<>();

                for (String timePoint : timeLabels) {
                    Double power = timeModuleDataMap.getOrDefault(timePoint, new HashMap<>()).get(moduleId);
                    if (power != null) {
                        // 计算负荷率
                        double loadRate = (power / ratedPower) * 100;
                        powerData.add(Math.round(power * 100.0) / 100.0);
                        loadRateData.add(Math.round(loadRate * 100.0) / 100.0);
                    } else {
                        // 如果没有数据，填充null
                        powerData.add(null);
                        loadRateData.add(null);
                    }
                }

                powerSeriesItem.setData(powerData);
                loadRateSeriesItem.setData(loadRateData);

                powerSeries.add(powerSeriesItem);
                loadRateSeries.add(loadRateSeriesItem);
            }

        } catch (Exception e) {
            log.error("查询负荷时序数据失败", e);
            // 如果查询失败，生成空的数据结构
            generateEmptyTimeSeriesData(query, modules, timeLabels, powerSeries, loadRateSeries);
        }
    }

    /**
     * 生成空的时间序列数据（当查询失败时使用）
     */
    private void generateEmptyTimeSeriesData(LoadTimeSeriesQueryVO query, List<TbModule> modules,
            List<String> timeLabels, List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries) {

        log.warn("生成空的时间序列数据作为备用");

        // 为每个仪表创建空的数据系列
        for (TbModule module : modules) {
            // 创建有功功率系列
            LoadTimeSeriesResultVO.LoadSeriesVO powerSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
            powerSeriesItem.setModuleId(module.getModuleId());
            powerSeriesItem.setModuleName(module.getModuleName());
            powerSeriesItem.setUnit("kW");
            powerSeriesItem.setData(new ArrayList<>());

            // 创建负荷率系列
            LoadTimeSeriesResultVO.LoadSeriesVO loadRateSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
            loadRateSeriesItem.setModuleId(module.getModuleId());
            loadRateSeriesItem.setModuleName(module.getModuleName());
            loadRateSeriesItem.setUnit("%");
            loadRateSeriesItem.setData(new ArrayList<>());

            powerSeries.add(powerSeriesItem);
            loadRateSeries.add(loadRateSeriesItem);
        }
    }
}
