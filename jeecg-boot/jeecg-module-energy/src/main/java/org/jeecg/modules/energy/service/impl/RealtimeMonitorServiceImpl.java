package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IRealtimeMonitorService;
import org.jeecg.modules.energy.util.InfluxDBQueryBuilder;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.jeecg.modules.energy.util.TimeZoneUtil;
import org.jeecg.modules.energy.vo.realtime.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 实时数据监控Service实现类
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
    private SysDepartMapper sysDepartMapper;

    @Autowired
    private InfluxDBConfig influxDBConfig;

    @Autowired
    private InfluxDB influxDB;

    @Autowired
    private InfluxDBQueryBuilder influxDBQueryBuilder;

    @Autowired
    private TimeZoneUtil timeZoneUtil;

    @Override
    public List<ModuleInfoVO> getModulesByDimension(String dimensionCode, Integer energyType, Boolean includeChildren) {
        log.info("根据维度获取仪表列表，维度编码：{}，能源类型：{}，包含子维度：{}", dimensionCode, energyType, includeChildren);

        try {
            // 1. 先通过维度编码查找对应的部门ID
            List<String> departIds = getDepartIdsByOrgCode(dimensionCode, includeChildren);
            
            if (departIds.isEmpty()) {
                log.warn("❌ 未找到维度编码 {} 对应的部门", dimensionCode);
                return new ArrayList<>();
            }
            
            log.info("✅ 找到 {} 个部门ID：{}", departIds.size(), departIds);
            
            // 2. 使用与EnergyMonitorServiceImpl相同的查询逻辑
            List<TbModule> allModules = new ArrayList<>();
            
            for (String departId : departIds) {
                // 使用现有的 Mapper 方法查询仪表（支持 FIND_IN_SET）
                List<TbModule> moduleList = tbModuleMapper.selectModulesByOrgCode(departId);
                log.info("🔍 部门ID {} 查询到 {} 个仪表", departId, moduleList.size());
                
                // 根据能源类型过滤仪表
                List<TbModule> filteredModules = moduleList.stream()
                        .filter(module -> energyType.equals(module.getEnergyType()))
                        .filter(module -> "Y".equals(module.getIsaction()))
                        .collect(Collectors.toList());
                
                log.info("🔍 部门ID {} 过滤后得到 {} 个符合条件的仪表", departId, filteredModules.size());
                allModules.addAll(filteredModules);
            }
            
            // 去重（防止同一个仪表被多次添加）
            List<TbModule> modules = allModules.stream()
                    .collect(Collectors.toMap(TbModule::getModuleId, m -> m, (existing, replacement) -> existing))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(TbModule::getModuleName))
                    .collect(Collectors.toList());
            
            log.info("✅ 最终查询到 {} 个仪表", modules.size());
            
            // 对于负荷监控，需要有额定功率的电力仪表
            if (energyType == 1) {
                List<TbModule> powerModules = modules.stream()
                        .filter(module -> module.getRatedPower() != null && module.getRatedPower() > 0)
                        .collect(Collectors.toList());
                log.info("🔍 电力仪表中有额定功率的仪表数量：{}/{}", powerModules.size(), modules.size());
                modules = powerModules;
            }
            
            // 如果仍然没有查到仪表，提供详细的调试信息
            if (modules.isEmpty()) {
                log.warn("❌ 最终未查询到任何仪表，调试信息：");
                log.warn("   - 维度编码：{}", dimensionCode);
                log.warn("   - 部门ID列表：{}", departIds);
                log.warn("   - 能源类型：{}", energyType);
                log.warn("   - 包含子维度：{}", includeChildren);
                
                // 查询该部门下的所有仪表进行调试
                for (String departId : departIds) {
                    List<TbModule> debugModules = tbModuleMapper.selectModulesByOrgCode(departId);
                    log.warn("🔍 调试 - 部门ID {} 下的所有仪表数量：{}", departId, debugModules.size());
                    
                    if (!debugModules.isEmpty()) {
                        Map<Integer, Long> energyTypeCount = debugModules.stream()
                            .collect(Collectors.groupingBy(TbModule::getEnergyType, Collectors.counting()));
                        log.warn("🔍 调试 - 按能源类型统计：{}", energyTypeCount);
                        
                        long activeCount = debugModules.stream()
                            .filter(m -> "Y".equals(m.getIsaction()))
                            .count();
                        log.warn("🔍 调试 - 启用状态的仪表数量：{}/{}", activeCount, debugModules.size());
                    }
                }
            }
            
            return modules.stream().map(this::convertToModuleInfoVO).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("根据维度获取仪表列表失败", e);
            throw new RuntimeException("查询仪表列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ParameterConfigVO> getParameterConfig(Integer energyType) {
        log.info("获取参数配置，能源类型：{}", energyType);

        List<ParameterConfigVO> configs = new ArrayList<>();
        
        switch (energyType) {
            case 1: // 电力
                configs.add(new ParameterConfigVO(1, "A相电流", "IA", "A"));
                configs.add(new ParameterConfigVO(2, "B相电流", "IB", "A"));
                configs.add(new ParameterConfigVO(3, "C相电流", "IC", "A"));
                configs.add(new ParameterConfigVO(4, "A相电压", "UA", "V"));
                configs.add(new ParameterConfigVO(5, "B相电压", "UB", "V"));
                configs.add(new ParameterConfigVO(6, "C相电压", "UC", "V"));
                configs.add(new ParameterConfigVO(7, "总有功功率", "PP", "kW"));
                configs.add(new ParameterConfigVO(8, "总无功功率", "QQ", "kVar"));
                configs.add(new ParameterConfigVO(9, "总视在功率", "SS", "kVA"));
                configs.add(new ParameterConfigVO(10, "总功率因数", "PFS", ""));
                configs.add(new ParameterConfigVO(11, "频率", "HZ", "Hz"));
                configs.add(new ParameterConfigVO(12, "正向有功总电能", "KWH", "kWh"));
                configs.add(new ParameterConfigVO(13, "正向无功总电能", "KVARH", "kVarh"));
                break;
            case 2: // 天然气
                configs.add(new ParameterConfigVO(1, "温度", "TEMP", "℃"));
                configs.add(new ParameterConfigVO(2, "压力", "PRESS", "MPa"));
                configs.add(new ParameterConfigVO(3, "瞬时流量", "FLOW", "m³/h"));
                configs.add(new ParameterConfigVO(4, "累计值", "ACCUM", "m³"));
                break;
            case 3: // 压缩空气
                configs.add(new ParameterConfigVO(1, "温度", "TEMP", "℃"));
                configs.add(new ParameterConfigVO(2, "压力", "PRESS", "MPa"));
                configs.add(new ParameterConfigVO(3, "瞬时流量", "FLOW", "m³/h"));
                configs.add(new ParameterConfigVO(4, "累计值", "ACCUM", "m³"));
                break;
            case 4: // 企业用水
                configs.add(new ParameterConfigVO(1, "温度", "TEMP", "℃"));
                configs.add(new ParameterConfigVO(2, "压力", "PRESS", "MPa"));
                configs.add(new ParameterConfigVO(3, "瞬时流量", "FLOW", "m³/h"));
                configs.add(new ParameterConfigVO(4, "累计值", "ACCUM", "m³"));
                break;
            default:
                log.warn("未知的能源类型: {}", energyType);
        }
        
        log.info("返回 {} 个参数配置", configs.size());
        return configs;
    }

    @Override
    public TimeSeriesResultVO getTimeSeriesData(TimeSeriesQueryVO query) {
        log.info("查询时序数据，参数：{}", query);

        // 参数验证
        validateTimeSeriesQuery(query);

        try {
            // 构建时间范围
            String[] timeRange = buildTimeRange(query.getQueryDate(), query.getTimeGranularity());
            String startTime = query.getStartTime() != null ? query.getStartTime() : timeRange[0];
            String endTime = query.getEndTime() != null ? query.getEndTime() : timeRange[1];

            // 根据时间粒度选择查询策略
            List<Map<String, Object>> influxResults;
            if ("year".equals(query.getTimeGranularity())) {
                // 年查询需要跨月查询
                influxResults = queryTimeSeriesDataCrossMonth(query.getModuleIds(), query.getParameters(),
                        query.getTimeGranularity(), startTime, endTime);
            } else {
                // 日/月查询使用单月查询
                influxResults = queryTimeSeriesDataSingleMonth(query.getModuleIds(), query.getParameters(),
                        query.getTimeGranularity(), startTime, endTime);
            }

            // 处理查询结果
            return processTimeSeriesResult(influxResults, query, startTime, endTime);

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
                // 查询仪表基本信息
                TbModule module = getModuleById(moduleId);
                if (module == null) {
                    log.warn("未找到仪表：{}", moduleId);
                    continue;
                }

                ModuleStatusVO statusVO = new ModuleStatusVO();
                statusVO.setModuleId(moduleId);
                statusVO.setModuleName(module.getModuleName());
                
                // 查询仪表最新数据
                boolean isOnline = false;
                String lastUpdateTime = "";
                
                // 从InfluxDB查询最新数据
                QueryResult queryResult = influxDB.query(new Query(
                    String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/ GROUP BY tagname ORDER BY time DESC LIMIT 1", 
                    influxDBConfig.getMeasurement(), moduleId.toUpperCase()),
                    influxDBConfig.getCurrentMonthDatabaseName()));
                
                // 解析查询结果
                Map<String, Object> latestData = new HashMap<>();
                if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
                    for (QueryResult.Result result1 : queryResult.getResults()) {
                        if (result1.getSeries() != null) {
                            for (QueryResult.Series series : result1.getSeries()) {
                                String tagname = series.getTags().get("tagname");
                                if (tagname != null && series.getValues() != null && !series.getValues().isEmpty()) {
                                    List<Object> values = series.getValues().get(0);
                                    List<String> columns = series.getColumns();
                                    
                                    // 获取时间和值
                                    int timeIndex = columns.indexOf("time");
                                    int valueIndex = columns.indexOf("value");
                                    
                                    if (timeIndex >= 0 && valueIndex >= 0 && values.size() > Math.max(timeIndex, valueIndex)) {
                                        String time = (String) values.get(timeIndex);
                                        Object value = values.get(valueIndex);
                                        
                                        // 提取参数字段名
                                        String fieldName = tagname.substring(tagname.indexOf("#") + 1);
                                        latestData.put(fieldName, value);
                                        
                                        // 更新最后更新时间
                                        if (lastUpdateTime.isEmpty() || time.compareTo(lastUpdateTime) > 0) {
                                            lastUpdateTime = time;
                                            isOnline = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 设置在线状态和最后更新时间
                statusVO.setIsOnline(isOnline);
                if (!lastUpdateTime.isEmpty()) {
                    // 转换UTC时间为北京时间
                    lastUpdateTime = timeZoneUtil.convertUTCToBeijing(lastUpdateTime);
                    statusVO.setLastUpdateTime(lastUpdateTime);
                } else {
                    statusVO.setLastUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }

                // 查询各参数的最新值
                List<ModuleStatusVO.ParameterStatusVO> parameterStatuses = new ArrayList<>();
                for (Integer paramCode : parameters) {
                    ModuleStatusVO.ParameterStatusVO paramStatus = new ModuleStatusVO.ParameterStatusVO();
                    paramStatus.setParamCode(paramCode);
                    paramStatus.setParamName(getParameterName(paramCode));
                    
                    // 获取参数字段名
                    String fieldName = getParameterFieldName(paramCode);
                    
                    // 从查询结果中获取值
                    if (latestData.containsKey(fieldName)) {
                        Object valueObj = latestData.get(fieldName);
                        Double value = convertToDouble(valueObj);
                        paramStatus.setCurrentValue(value);
                    } else {
                        // 如果没有数据，设置为null
                        paramStatus.setCurrentValue(null);
                    }
                    
                    paramStatus.setUnit(getParameterUnit(paramCode));
                    paramStatus.setStatus(isOnline ? "normal" : "offline");
                    parameterStatuses.add(paramStatus);
                }

                statusVO.setParameters(parameterStatuses);
                result.add(statusVO);
            }

        } catch (Exception e) {
            log.error("获取实时状态失败", e);
            throw new RuntimeException("获取实时状态失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public LoadTimeSeriesResultVO getLoadTimeSeriesData(LoadTimeSeriesQueryVO query) {
        log.info("查询负荷时序数据，参数：{}", query);

        // 参数验证
        validateLoadTimeSeriesQuery(query);

        try {
            // 查询仪表信息和额定功率
            List<TbModule> modules = getModulesByIds(query.getModuleIds());
            Map<String, Double> ratedPowerMap = modules.stream()
                    .filter(m -> m.getRatedPower() != null && m.getRatedPower() > 0)
                    .collect(Collectors.toMap(TbModule::getModuleId, TbModule::getRatedPower));

            // 构建时间范围
            String[] timeRange = buildTimeRange(query.getQueryDate(), query.getTimeGranularity());
            String startTime = query.getStartTime() != null ? query.getStartTime() : timeRange[0];
            String endTime = query.getEndTime() != null ? query.getEndTime() : timeRange[1];

            // 根据时间粒度选择查询策略
            List<Map<String, Object>> influxResults;
            if ("year".equals(query.getTimeGranularity())) {
                // 年查询需要跨月查询，只查询PP字段（有功功率）
                List<Integer> powerParams = Arrays.asList(7); // 7代表PP字段
                influxResults = queryTimeSeriesDataCrossMonth(query.getModuleIds(), powerParams,
                        query.getTimeGranularity(), startTime, endTime);
            } else {
                // 日/月查询使用单月查询
                List<Integer> powerParams = Arrays.asList(7); // 7代表PP字段
                influxResults = queryTimeSeriesDataSingleMonth(query.getModuleIds(), powerParams,
                        query.getTimeGranularity(), startTime, endTime);
            }

            // 处理负荷查询结果
            return processLoadTimeSeriesResultFromData(influxResults, query, modules, ratedPowerMap);

        } catch (Exception e) {
            log.error("查询负荷时序数据失败", e);
            throw new RuntimeException("查询负荷时序数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ModuleLoadStatusVO> getCurrentLoadStatus(List<String> moduleIds) {
        log.info("获取实时负荷状态，仪表ID：{}", moduleIds);

        List<ModuleLoadStatusVO> result = new ArrayList<>();

        try {
            for (String moduleId : moduleIds) {
                // 查询仪表基本信息
                TbModule module = getModuleById(moduleId);
                if (module == null) {
                    log.warn("未找到仪表：{}", moduleId);
                    continue;
                }

                ModuleLoadStatusVO statusVO = new ModuleLoadStatusVO();
                statusVO.setModuleId(moduleId);
                statusVO.setModuleName(module.getModuleName());
                
                // 设置额定功率
                Double ratedPower = module.getRatedPower() != null ? module.getRatedPower() : 100.0;
                statusVO.setRatedPower(ratedPower);
                
                // 从InfluxDB查询最新的有功功率数据
                boolean isOnline = false;
                String lastUpdateTime = "";
                Double currentPower = null;
                
                // 构建查询语句，查询有功功率(P)字段的最新值
                String tagname = moduleId.toUpperCase() + "#P";
                QueryResult queryResult = influxDB.query(new Query(
                    String.format("SELECT * FROM %s WHERE tagname = '%s' ORDER BY time DESC LIMIT 1", 
                    influxDBConfig.getMeasurement(), tagname),
                    influxDBConfig.getCurrentMonthDatabaseName()));
                
                // 解析查询结果
                if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
                    for (QueryResult.Result result1 : queryResult.getResults()) {
                        if (result1.getSeries() != null && !result1.getSeries().isEmpty()) {
                            QueryResult.Series series = result1.getSeries().get(0);
                            if (series.getValues() != null && !series.getValues().isEmpty()) {
                                List<Object> values = series.getValues().get(0);
                                List<String> columns = series.getColumns();
                                
                                // 获取时间和值
                                int timeIndex = columns.indexOf("time");
                                int valueIndex = columns.indexOf("value");
                                
                                if (timeIndex >= 0 && valueIndex >= 0 && values.size() > Math.max(timeIndex, valueIndex)) {
                                    String time = (String) values.get(timeIndex);
                                    Object value = values.get(valueIndex);
                                    
                                    // 转换为Double
                                    currentPower = convertToDouble(value);
                                    
                                    // 更新最后更新时间
                                    lastUpdateTime = time;
                                    isOnline = true;
                                }
                            }
                        }
                    }
                }
                
                // 设置在线状态和最后更新时间
                statusVO.setIsOnline(isOnline);
                if (!lastUpdateTime.isEmpty()) {
                    // 转换UTC时间为北京时间
                    lastUpdateTime = timeZoneUtil.convertUTCToBeijing(lastUpdateTime);
                    statusVO.setLastUpdateTime(lastUpdateTime);
                } else {
                    statusVO.setLastUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                
                // 设置当前功率
                if (currentPower != null) {
                    // 保留两位小数
                    currentPower = Math.round(currentPower * 100.0) / 100.0;
                    statusVO.setCurrentPower(currentPower);
                    
                    // 计算负荷率
                    double loadRate = (currentPower / ratedPower) * 100;
                    loadRate = Math.round(loadRate * 100.0) / 100.0;
                    statusVO.setLoadRate(loadRate);
                    
                    // 判断负荷等级
                    if (loadRate < 50) {
                        statusVO.setLoadLevel("低负荷");
                    } else if (loadRate < 80) {
                        statusVO.setLoadLevel("中等负荷");
                    } else {
                        statusVO.setLoadLevel("高负荷");
                    }
                } else {
                    // 如果没有数据，设置为0
                    statusVO.setCurrentPower(0.0);
                    statusVO.setLoadRate(0.0);
                    statusVO.setLoadLevel("无负荷");
                }

                statusVO.setPowerUnit("kW");
                statusVO.setLoadRateUnit("%");
                statusVO.setStatus(isOnline ? "normal" : "offline");

                result.add(statusVO);
            }

        } catch (Exception e) {
            log.error("获取实时负荷状态失败", e);
            throw new RuntimeException("获取实时负荷状态失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public byte[] exportLoadData(LoadDataExportVO exportVO) throws Exception {
        log.info("导出负荷数据，参数：{}", exportVO);
        
        try {
            // 构建查询参数
            LoadTimeSeriesQueryVO query = new LoadTimeSeriesQueryVO();
            query.setModuleIds(exportVO.getModuleIds());
            query.setTimeGranularity(exportVO.getTimeGranularity());
            query.setQueryDate(exportVO.getQueryDate());
            
            // 查询负荷时序数据
            LoadTimeSeriesResultVO loadData = getLoadTimeSeriesData(query);
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            
            // 创建有功功率工作表
            Sheet powerSheet = workbook.createSheet("有功功率");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // 创建标题行
            Row headerRow = powerSheet.createRow(0);
            Cell timeHeaderCell = headerRow.createCell(0);
            timeHeaderCell.setCellValue("时间");
            timeHeaderCell.setCellStyle(headerStyle);
            
            // 添加仪表名称到标题行
            List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries = loadData.getPowerChartData().getSeries();
            for (int i = 0; i < powerSeries.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(powerSeries.get(i).getModuleName() + "(kW)");
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据行
            List<String> timeLabels = loadData.getPowerChartData().getTimeLabels();
            for (int i = 0; i < timeLabels.size(); i++) {
                Row row = powerSheet.createRow(i + 1);
                row.createCell(0).setCellValue(timeLabels.get(i));
                
                for (int j = 0; j < powerSeries.size(); j++) {
                    Cell cell = row.createCell(j + 1);
                    Double value = powerSeries.get(j).getData().get(i);
                    if (value != null) {
                        cell.setCellValue(value);
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < powerSeries.size() + 1; i++) {
                powerSheet.autoSizeColumn(i);
            }
            
            // 创建负荷率工作表
            Sheet loadRateSheet = workbook.createSheet("负荷率");
            
            // 创建标题行
            headerRow = loadRateSheet.createRow(0);
            timeHeaderCell = headerRow.createCell(0);
            timeHeaderCell.setCellValue("时间");
            timeHeaderCell.setCellStyle(headerStyle);
            
            // 添加仪表名称到标题行
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries = loadData.getLoadRateChartData().getSeries();
            for (int i = 0; i < loadRateSeries.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(loadRateSeries.get(i).getModuleName() + "(%)");
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据行
            for (int i = 0; i < timeLabels.size(); i++) {
                Row row = loadRateSheet.createRow(i + 1);
                row.createCell(0).setCellValue(timeLabels.get(i));
                
                for (int j = 0; j < loadRateSeries.size(); j++) {
                    Cell cell = row.createCell(j + 1);
                    Double value = loadRateSeries.get(j).getData().get(i);
                    if (value != null) {
                        cell.setCellValue(value);
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < loadRateSeries.size() + 1; i++) {
                loadRateSheet.autoSizeColumn(i);
            }
            
            // 创建统计数据工作表
            Sheet statsSheet = workbook.createSheet("统计数据");
            
            // 创建标题行
            headerRow = statsSheet.createRow(0);
            String[] statHeaders = {"序号", "设备名称", "最大功率(kW)", "最大功率率(%)", "最大功率发生时间", 
                                   "最小功率(kW)", "最小功率率(%)", "最小功率发生时间", "平均功率(kW)", "平均功率率(%)"};
            
            for (int i = 0; i < statHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(statHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 构建统计数据查询参数
            LoadTableQueryVO tableQuery = new LoadTableQueryVO();
            tableQuery.setModuleIds(exportVO.getModuleIds());
            tableQuery.setTimeType(exportVO.getTimeGranularity());
            
            // 构建时间范围
            String[] timeRange = buildTimeRange(exportVO.getQueryDate(), exportVO.getTimeGranularity());
            tableQuery.setStartTime(timeRange[0]);
            tableQuery.setEndTime(timeRange[1]);
            tableQuery.setPageNum(1);
            tableQuery.setPageSize(1000);
            
            // 查询统计数据
            LoadTableResultVO tableData = getLoadTableData(tableQuery);
            
            // 填充统计数据
            List<LoadTableResultVO.LoadStatisticsRowVO> stats = tableData.getTableData();
            for (int i = 0; i < stats.size(); i++) {
                LoadTableResultVO.LoadStatisticsRowVO stat = stats.get(i);
                Row row = statsSheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(stat.get序号());
                row.createCell(1).setCellValue(stat.get设备名称());
                row.createCell(2).setCellValue(stat.get最大功率());
                row.createCell(3).setCellValue(stat.get最大功率率());
                row.createCell(4).setCellValue(stat.get最大功率发生时间());
                row.createCell(5).setCellValue(stat.get最小功率());
                row.createCell(6).setCellValue(stat.get最小功率率());
                row.createCell(7).setCellValue(stat.get最小功率发生时间());
                row.createCell(8).setCellValue(stat.get平均功率());
                row.createCell(9).setCellValue(stat.get平均功率率());
            }
            
            // 自动调整列宽
            for (int i = 0; i < statHeaders.length; i++) {
                statsSheet.autoSizeColumn(i);
            }
            
            // 写入到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("导出负荷数据失败", e);
            throw new RuntimeException("导出负荷数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public LoadTableResultVO getLoadTableData(LoadTableQueryVO query) {
        log.info("获取负荷数据表格，参数：{}", query);

        // 参数验证
        validateLoadTableQuery(query);

        try {
            // 查询仪表信息
            List<TbModule> modules = getModulesByIds(query.getModuleIds());

            // 构建InfluxDB查询
            String influxQuery = buildLoadInfluxQuery(query);
            String databaseName = influxDBConfig.getCurrentMonthDatabaseName();
log.info("获取数据表格sql_hhr", influxQuery);
            // 执行查询
            QueryResult queryResult = influxDB.query(new Query(influxQuery, databaseName));

            // 处理查询结果
            return processLoadTableResult(queryResult, query, modules);

        } catch (Exception e) {
            log.error("获取负荷数据表格失败", e);
            throw new RuntimeException("获取负荷数据表格失败: " + e.getMessage(), e);
        }
    }

    /**
     * 跨月查询时序数据（用于年查询）
     */
    private List<Map<String, Object>> queryTimeSeriesDataCrossMonth(List<String> moduleIds, List<Integer> parameters,
            String timeGranularity, String startTime, String endTime) {
        
        log.info("执行跨月查询，时间范围：{} ~ {}", startTime, endTime);
        
        List<Map<String, Object>> allResults = new ArrayList<>();
        
        try {
            // 解析开始和结束时间
            LocalDate startDate = LocalDate.parse(startTime.substring(0, 10));
            LocalDate endDate = LocalDate.parse(endTime.substring(0, 10));
            
            // 计算需要查询的月份范围
            YearMonth startYearMonth = YearMonth.from(startDate);
            YearMonth endYearMonth = YearMonth.from(endDate);
            
            log.info("需要查询的月份范围：{} ~ {}", startYearMonth, endYearMonth);
            
            // 遍历每个月份进行查询
            for (YearMonth yearMonth = startYearMonth; 
                 !yearMonth.isAfter(endYearMonth); 
                 yearMonth = yearMonth.plusMonths(1)) {
                
                String dbName = influxDBConfig.getDatabaseName(yearMonth.getYear(), yearMonth.getMonthValue());
                log.info("查询数据库：{}", dbName);
                
            // 检查数据库是否存在 - 使用查询方式替代已弃用的方法
            try {
                QueryResult result = influxDB.query(new Query("SHOW DATABASES"));
                boolean dbExists = result.getResults().stream()
                    .flatMap(r -> r.getSeries() != null ? r.getSeries().stream() : Stream.empty())
                    .flatMap(s -> s.getValues() != null ? s.getValues().stream() : Stream.empty())
                    .anyMatch(values -> values.size() > 0 && dbName.equals(values.get(0)));
                
                if (!dbExists) {
                    log.warn("数据库 {} 不存在，跳过", dbName);
                    continue;
                }
            } catch (Exception e) {
                log.warn("检查数据库 {} 是否存在时出错: {}", dbName, e.getMessage());
                continue;
            }
                
                // 构建该月的查询语句
                String monthStartTime, monthEndTime;
                if (yearMonth.equals(startYearMonth)) {
                    monthStartTime = startTime;
                    monthEndTime = yearMonth.atEndOfMonth().toString() + " 23:59:59";
                } else if (yearMonth.equals(endYearMonth)) {
                    monthStartTime = yearMonth.atDay(1).toString() + " 00:00:00";
                    monthEndTime = endTime;
                } else {
                    monthStartTime = yearMonth.atDay(1).toString() + " 00:00:00";
                    monthEndTime = yearMonth.atEndOfMonth().toString() + " 23:59:59";
                }
                
                // 构建查询语句
                String queryStr = influxDBQueryBuilder.buildTimeSeriesQuery(moduleIds, parameters,
                        timeGranularity, monthStartTime, monthEndTime);
                
                log.info("执行月份 {} 的查询：{}", yearMonth, queryStr);
                
                // 执行查询
                QueryResult queryResult = influxDB.query(new Query(queryStr, dbName));
                
                // 解析结果
                List<Map<String, Object>> monthResults = InfluxDBUtil.parseQueryResult(queryResult);
                log.info("月份 {} 查询到 {} 条数据", yearMonth, monthResults.size());
                
                allResults.addAll(monthResults);
            }
            
            log.info("跨月查询完成，总共查询到 {} 条数据", allResults.size());
            
        } catch (Exception e) {
            log.error("跨月查询失败", e);
            throw new RuntimeException("跨月查询失败: " + e.getMessage(), e);
        }
        
        return allResults;
    }

    /**
     * 单月查询时序数据（用于日/月查询）
     */
    private List<Map<String, Object>> queryTimeSeriesDataSingleMonth(List<String> moduleIds, List<Integer> parameters,
            String timeGranularity, String startTime, String endTime) {
        
        log.info("执行单月查询，时间范围：{} ~ {}", startTime, endTime);
        
        try {
            // 构建查询语句
            String queryStr = influxDBQueryBuilder.buildTimeSeriesQuery(moduleIds, parameters,
                    timeGranularity, startTime, endTime);
            
            // 获取数据库名称
            String databaseName = influxDBConfig.getCurrentMonthDatabaseName();
            log.info("使用数据库：{}", databaseName);
            
            // 检查数据库是否存在 - 使用查询方式替代已弃用的方法
            try {
                QueryResult result = influxDB.query(new Query("SHOW DATABASES"));
                boolean dbExists = result.getResults().stream()
                    .flatMap(r -> r.getSeries() != null ? r.getSeries().stream() : Stream.empty())
                    .flatMap(s -> s.getValues() != null ? s.getValues().stream() : Stream.empty())
                    .anyMatch(values -> values.size() > 0 && databaseName.equals(values.get(0)));
                
                if (!dbExists) {
                    log.warn("数据库 {} 不存在", databaseName);
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                log.warn("检查数据库 {} 是否存在时出错: {}", databaseName, e.getMessage());
                return new ArrayList<>();
            }
            
            // 执行查询
            QueryResult queryResult = influxDB.query(new Query(queryStr, databaseName));
            
            // 解析结果
            List<Map<String, Object>> results = InfluxDBUtil.parseQueryResult(queryResult);
            log.info("单月查询完成，查询到 {} 条数据", results.size());
            
            return results;
            
        } catch (Exception e) {
            log.error("单月查询失败", e);
            throw new RuntimeException("单月查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从解析后的数据处理负荷时序查询结果
     */
    private LoadTimeSeriesResultVO processLoadTimeSeriesResultFromData(List<Map<String, Object>> influxResults,
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

        // 从实际数据生成时间序列数据
        generateTimeSeriesDataFromInfluxResults(influxResults, query, modules, ratedPowerMap, 
                                               timeLabels, powerSeries, loadRateSeries);

        // 设置图表数据
        powerChartData.setTimeLabels(timeLabels);
        powerChartData.setSeries(powerSeries);
        loadRateChartData.setTimeLabels(timeLabels);
        loadRateChartData.setSeries(loadRateSeries);

        result.setPowerChartData(powerChartData);
        result.setLoadRateChartData(loadRateChartData);

        // 生成表格数据
        List<LoadTimeSeriesResultVO.LoadTableRowVO> tableData = generateLoadTableData(timeLabels, powerSeries, loadRateSeries);
        result.setTableData(tableData);

        // 创建汇总信息
        LoadTimeSeriesResultVO.LoadSummaryVO summary = new LoadTimeSeriesResultVO.LoadSummaryVO();
        summary.setTotalDataPoints(timeLabels.size() * modules.size());
        summary.setTimeRange(buildTimeRange(query.getQueryDate(), query.getTimeGranularity())[0] + 
                           " ~ " + buildTimeRange(query.getQueryDate(), query.getTimeGranularity())[1]);
        summary.setGranularity(getGranularityDescription(query.getTimeGranularity()));
        summary.setModuleCount(modules.size());
        summary.setDataType("负荷监控数据");
        result.setSummary(summary);

        return result;
    }

    /**
     * 从InfluxDB结果生成时间序列数据
     */
    private void generateTimeSeriesDataFromInfluxResults(List<Map<String, Object>> influxResults,
            LoadTimeSeriesQueryVO query, List<TbModule> modules, Map<String, Double> ratedPowerMap,
            List<String> timeLabels, List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries) {

        log.info("开始处理 {} 条InfluxDB查询结果", influxResults.size());

        try {
            // 1. 按时间和模块分组数据
            Map<String, Map<String, Double>> timeModuleDataMap = new HashMap<>();
            Set<String> allTimePoints = new TreeSet<>();

            for (Map<String, Object> data : influxResults) {
                String tagname = (String) data.get("tagname");
                String timeStr = (String) data.get("time");
                Object valueObj = data.get("avg_value"); // 使用平均值

                if (tagname != null && timeStr != null && valueObj != null) {
                    // 从tagname中提取moduleId (格式: YJ0001_1202#PP)
                    String moduleId = extractModuleIdFromTagname(tagname);
                    if (moduleId != null && query.getModuleIds().contains(moduleId)) {
                        // 转换时间格式为显示格式
                        String displayTime = formatTimeForDisplay(timeStr, query.getTimeGranularity());
                        allTimePoints.add(displayTime);

                        // 转换数值
                        Double value = convertToDouble(valueObj);
                        if (value != null) {
                            timeModuleDataMap.computeIfAbsent(displayTime, k -> new HashMap<>())
                                .put(moduleId, value);
                        }
                    }
                }
            }

            // 2. 生成时间标签
            if (allTimePoints.isEmpty()) {
                // 如果没有数据，生成默认时间标签
                timeLabels.addAll(generateTimeLabels(query.getQueryDate(), query.getTimeGranularity()));
            } else {
                timeLabels.addAll(allTimePoints);
            }

            log.info("生成时间标签数量：{}", timeLabels.size());

            // 3. 为每个仪表创建数据系列
            String[] colors = {"#1890ff", "#52c41a", "#fa8c16", "#722ed1", "#eb2f96", "#13c2c2"};
            int colorIndex = 0;

            for (TbModule module : modules) {
                String moduleId = module.getModuleId();
                Double ratedPower = ratedPowerMap.getOrDefault(moduleId, 100.0);

                // 创建有功功率系列
                LoadTimeSeriesResultVO.LoadSeriesVO powerSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
                powerSeriesItem.setModuleId(moduleId);
                powerSeriesItem.setModuleName(module.getModuleName());
                powerSeriesItem.setUnit("kW");
                powerSeriesItem.setColor(colors[colorIndex % colors.length]);

                // 创建负荷率系列
                LoadTimeSeriesResultVO.LoadSeriesVO loadRateSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
                loadRateSeriesItem.setModuleId(moduleId);
                loadRateSeriesItem.setModuleName(module.getModuleName());
                loadRateSeriesItem.setUnit("%");
                loadRateSeriesItem.setColor(colors[colorIndex % colors.length]);

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
                
                colorIndex++;
            }

            log.info("生成数据系列完成，功率系列：{}，负荷率系列：{}", powerSeries.size(), loadRateSeries.size());

        } catch (Exception e) {
            log.error("处理InfluxDB结果失败", e);
            // 如果处理失败，生成空的数据结构
            generateEmptyTimeSeriesData(query, modules, timeLabels, powerSeries, loadRateSeries);
        }
    }

    /**
     * 格式化时间用于显示
     */
    private String formatTimeForDisplay(String utcTimeStr, String timeGranularity) {
        try {
            // 先转换为本地时间
            String localTime = timeZoneUtil.convertUTCToBeijing(utcTimeStr);
            
            // 根据时间粒度格式化显示
            LocalDateTime dateTime = LocalDateTime.parse(localTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            switch (timeGranularity) {
                case "day":
                    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                case "month":
                    return dateTime.format(DateTimeFormatter.ofPattern("MM-dd"));
                case "year":
                    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                default:
                    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            log.error("格式化时间失败: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }

    /**
     * 生成负荷表格数据
     */
    private List<LoadTimeSeriesResultVO.LoadTableRowVO> generateLoadTableData(List<String> timeLabels,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries) {
        
        List<LoadTimeSeriesResultVO.LoadTableRowVO> tableData = new ArrayList<>();
        
        for (int i = 0; i < timeLabels.size(); i++) {
            LoadTimeSeriesResultVO.LoadTableRowVO row = new LoadTimeSeriesResultVO.LoadTableRowVO();
            row.setTime(timeLabels.get(i));
            row.setTimeLabel(timeLabels.get(i));
            
            List<LoadTimeSeriesResultVO.LoadModuleDataVO> modules = new ArrayList<>();
            
            // 为每个仪表添加数据
            for (int j = 0; j < powerSeries.size(); j++) {
                LoadTimeSeriesResultVO.LoadSeriesVO powerSerie = powerSeries.get(j);
                LoadTimeSeriesResultVO.LoadSeriesVO loadRateSerie = loadRateSeries.get(j);
                
                LoadTimeSeriesResultVO.LoadModuleDataVO moduleData = new LoadTimeSeriesResultVO.LoadModuleDataVO();
                moduleData.setModuleId(powerSerie.getModuleId());
                moduleData.setModuleName(powerSerie.getModuleName());
                
                // 获取对应时间点的数据
                Double powerValue = null;
                Double loadRateValue = null;
                
                if (i < powerSerie.getData().size()) {
                    powerValue = powerSerie.getData().get(i);
                }
                if (i < loadRateSerie.getData().size()) {
                    loadRateValue = loadRateSerie.getData().get(i);
                }
                
                moduleData.setCurrentPower(powerValue);
                moduleData.setLoadRate(loadRateValue);
                moduleData.setPowerUnit("kW");
                moduleData.setLoadRateUnit("%");
                
                modules.add(moduleData);
            }
            
            row.setModules(modules);
            tableData.add(row);
        }
        
        return tableData;
    }

    /**
     * 生成空的时间序列数据（当查询失败时使用）
     */
    private void generateEmptyTimeSeriesData(LoadTimeSeriesQueryVO query, List<TbModule> modules,
            List<String> timeLabels, List<LoadTimeSeriesResultVO.LoadSeriesVO> powerSeries,
            List<LoadTimeSeriesResultVO.LoadSeriesVO> loadRateSeries) {

        log.warn("生成空的时间序列数据作为备用");

        // 生成默认时间标签
        timeLabels.addAll(generateTimeLabels(query.getQueryDate(), query.getTimeGranularity()));

        // 为每个仪表创建空的数据系列
        for (TbModule module : modules) {
            // 创建有功功率系列
            LoadTimeSeriesResultVO.LoadSeriesVO powerSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
            powerSeriesItem.setModuleId(module.getModuleId());
            powerSeriesItem.setModuleName(module.getModuleName());
            powerSeriesItem.setUnit("kW");
            
            List<Double> emptyPowerData = new ArrayList<>();
            for (int i = 0; i < timeLabels.size(); i++) {
                emptyPowerData.add(null);
            }
            powerSeriesItem.setData(emptyPowerData);

            // 创建负荷率系列
            LoadTimeSeriesResultVO.LoadSeriesVO loadRateSeriesItem = new LoadTimeSeriesResultVO.LoadSeriesVO();
            loadRateSeriesItem.setModuleId(module.getModuleId());
            loadRateSeriesItem.setModuleName(module.getModuleName());
            loadRateSeriesItem.setUnit("%");
            
            List<Double> emptyLoadRateData = new ArrayList<>();
            for (int i = 0; i < timeLabels.size(); i++) {
                emptyLoadRateData.add(null);
            }
            loadRateSeriesItem.setData(emptyLoadRateData);

            powerSeries.add(powerSeriesItem);
            loadRateSeries.add(loadRateSeriesItem);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换TbModule为ModuleInfoVO
     */
    private ModuleInfoVO convertToModuleInfoVO(TbModule module) {
        ModuleInfoVO vo = new ModuleInfoVO();
        vo.setModuleId(module.getModuleId());
        vo.setModuleName(module.getModuleName());
        vo.setEnergyType(module.getEnergyType());
        
        // 根据部门ID获取维度编码
        String dimensionCode = getDimensionCodeByDepartId(module.getSysOrgCode());
        vo.setDimensionCode(dimensionCode != null ? dimensionCode : "");
        
        vo.setRatedPower(module.getRatedPower());
        vo.setIsAction(module.getIsaction());
        vo.setUpdateTime(module.getUpdateTime() != null ? 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(module.getUpdateTime()) : null);
        
        // 设置当前功率和负荷率（示例数据）
        if (module.getRatedPower() != null && module.getRatedPower() > 0) {
            double currentPower = 70.0 + Math.random() * 30.0;
            vo.setCurrentPower(Math.round(currentPower * 100.0) / 100.0);
            vo.setLoadRate(Math.round((currentPower / module.getRatedPower() * 100) * 100.0) / 100.0);
        }
        vo.setIsOnline(true);
        
        return vo;
    }

    /**
     * 根据部门ID获取维度编码
     */
    private String getDimensionCodeByDepartId(String departId) {
        if (departId == null || departId.trim().isEmpty()) {
            return null;
        }
        
        try {
            QueryWrapper<SysDepart> query = new QueryWrapper<>();
            query.eq("id", departId);
            query.eq("del_flag", "0");
            
            SysDepart depart = sysDepartMapper.selectOne(query);
            return depart != null ? depart.getOrgCode() : null;
            
        } catch (Exception e) {
            log.warn("根据部门ID {} 获取维度编码失败: {}", departId, e.getMessage());
            return null;
        }
    }

    /**
     * 验证时序查询参数
     */
    private void validateTimeSeriesQuery(TimeSeriesQueryVO query) {
        if (query.getModuleIds() == null || query.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表ID列表不能为空");
        }
        if (query.getParameters() == null || query.getParameters().isEmpty()) {
            throw new IllegalArgumentException("参数列表不能为空");
        }
        if (query.getTimeGranularity() == null) {
            throw new IllegalArgumentException("时间粒度不能为空");
        }
        if (query.getQueryDate() == null) {
            throw new IllegalArgumentException("查询日期不能为空");
        }
    }

    /**
     * 验证负荷时序查询参数
     */
    private void validateLoadTimeSeriesQuery(LoadTimeSeriesQueryVO query) {
        if (query.getModuleIds() == null || query.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表ID列表不能为空");
        }
        if (query.getTimeGranularity() == null) {
            throw new IllegalArgumentException("时间粒度不能为空");
        }
        if (query.getQueryDate() == null) {
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
        if (query.getStartTime() == null || query.getEndTime() == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
    }

    /**
     * 构建时间范围
     */
    private String[] buildTimeRange(String queryDate, String timeGranularity) {
        try {
            String startTime, endTime;
            
            // 根据时间粒度处理不同格式的日期
            switch (timeGranularity) {
                case "day":
                    // 日查询需要完整的日期格式 yyyy-MM-dd
                    LocalDate date;
                    if (queryDate.length() == 10) { // yyyy-MM-dd 格式
                        date = LocalDate.parse(queryDate);
                    } else {
                        throw new IllegalArgumentException("日查询需要完整的日期格式(yyyy-MM-dd)");
                    }
                    startTime = date.toString() + " 00:00:00";
                    endTime = date.toString() + " 23:59:59";
                    break;
                    
                case "month":
                    // 月查询可以接受 yyyy-MM 或 yyyy-MM-dd 格式
                    YearMonth yearMonth;
                    if (queryDate.length() == 7) { // yyyy-MM 格式
                        yearMonth = YearMonth.parse(queryDate);
                    } else if (queryDate.length() == 10) { // yyyy-MM-dd 格式
                        LocalDate monthDate = LocalDate.parse(queryDate);
                        yearMonth = YearMonth.from(monthDate);
                    } else {
                        throw new IllegalArgumentException("月查询需要yyyy-MM或yyyy-MM-dd格式");
                    }
                    startTime = yearMonth.atDay(1).toString() + " 00:00:00";
                    endTime = yearMonth.atEndOfMonth().toString() + " 23:59:59";
                    break;
                    
                case "year":
                    // 年查询可以接受 yyyy、yyyy-MM 或 yyyy-MM-dd 格式
                    int year;
                    if (queryDate.length() == 4) { // yyyy 格式
                        year = Integer.parseInt(queryDate);
                    } else if (queryDate.length() == 7) { // yyyy-MM 格式
                        year = Integer.parseInt(queryDate.substring(0, 4));
                    } else if (queryDate.length() == 10) { // yyyy-MM-dd 格式
                        LocalDate yearDate = LocalDate.parse(queryDate);
                        year = yearDate.getYear();
                    } else {
                        throw new IllegalArgumentException("年查询需要yyyy、yyyy-MM或yyyy-MM-dd格式");
                    }
                    startTime = year + "-01-01 00:00:00";
                    endTime = year + "-12-31 23:59:59";
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的时间粒度: " + timeGranularity);
            }
            
            log.info("构建时间范围成功: {} ~ {}, 原始日期: {}, 时间粒度: {}", startTime, endTime, queryDate, timeGranularity);
            return new String[]{startTime, endTime};
        } catch (Exception e) {
            log.error("构建时间范围失败: {}", queryDate, e);
            throw new IllegalArgumentException("时间格式错误: " + queryDate, e);
        }
    }

    /**
     * 根据ID获取仪表信息
     */
    private TbModule getModuleById(String moduleId) {
        QueryWrapper<TbModule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("module_id", moduleId);
        List<TbModule> modules = tbModuleMapper.selectList(queryWrapper);
        return modules.isEmpty() ? null : modules.get(0);
    }

    /**
     * 根据ID列表获取仪表信息
     */
    private List<TbModule> getModulesByIds(List<String> moduleIds) {
        QueryWrapper<TbModule> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("module_id", moduleIds);
        return tbModuleMapper.selectList(queryWrapper);
    }

    /**
     * 获取参数名称
     */
    private String getParameterName(Integer paramCode) {
        switch (paramCode) {
            case 1: return "A相电流";
            case 2: return "B相电流";
            case 3: return "C相电流";
            case 4: return "A相电压";
            case 5: return "B相电压";
            case 6: return "C相电压";
            case 7: return "总有功功率";
            case 8: return "总无功功率";
            case 9: return "总视在功率";
            case 10: return "总功率因数";
            case 11: return "频率";
            case 12: return "正向有功总电能";
            case 13: return "正向无功总电能";
            default: return "未知参数";
        }
    }

    /**
     * 获取参数单位
     */
    private String getParameterUnit(Integer paramCode) {
        switch (paramCode) {
            case 1:
            case 2:
            case 3: return "A";
            case 4:
            case 5:
            case 6: return "V";
            case 7: return "kW";
            case 8: return "kVar";
            case 9: return "kVA";
            case 10: return "";
            case 11: return "Hz";
            case 12: return "kWh";
            case 13: return "kVarh";
            default: return "";
        }
    }

    // 已删除未使用的generateRandomValue方法

    /**
     * 处理时序查询结果
     */
    private TimeSeriesResultVO processTimeSeriesResult(List<Map<String, Object>> influxResults,
            TimeSeriesQueryVO query, String startTime, String endTime) {
        
        log.info("处理时序查询结果，查询到 {} 条数据", influxResults.size());
        TimeSeriesResultVO result = new TimeSeriesResultVO();
        
        try {
            // 创建图表数据
            TimeSeriesResultVO.ChartDataVO chartData = new TimeSeriesResultVO.ChartDataVO();
            
            // 1. 按时间和模块参数分组数据
            Map<String, Map<String, Map<Integer, Double>>> timeModuleParamDataMap = new HashMap<>();
            Set<String> allTimePoints = new TreeSet<>();
            
            // 查询所有仪表信息，用于获取仪表名称
            List<TbModule> modules = getModulesByIds(query.getModuleIds());
            Map<String, String> moduleNameMap = modules.stream()
                    .collect(Collectors.toMap(TbModule::getModuleId, TbModule::getModuleName));
            
            // 处理InfluxDB查询结果
            for (Map<String, Object> data : influxResults) {
                String tagname = (String) data.get("tagname");
                String timeStr = (String) data.get("time");
                Object valueObj = data.get("avg_value"); // 使用平均值
                
                if (tagname != null && timeStr != null && valueObj != null) {
                    // 从tagname中提取moduleId和参数 (格式: YJ0001_1202#IA)
                    String[] parts = tagname.split("#");
                    if (parts.length == 2) {
                        String moduleId = parts[0].toLowerCase();
                        String fieldName = parts[1];
                        
                        // 获取参数编码
                        Integer paramCode = getParamCodeByFieldName(fieldName);
                        
                        if (moduleId != null && paramCode != null && 
                            query.getModuleIds().contains(moduleId) && 
                            query.getParameters().contains(paramCode)) {
                            
                            // 转换时间格式为显示格式
                            String displayTime = formatTimeForDisplay(timeStr, query.getTimeGranularity());
                            allTimePoints.add(displayTime);
                            
                            // 转换数值
                            Double value = convertToDouble(valueObj);
                            if (value != null) {
                                // 按时间、模块ID和参数编码存储数据
                                timeModuleParamDataMap
                                    .computeIfAbsent(displayTime, k -> new HashMap<>())
                                    .computeIfAbsent(moduleId, k -> new HashMap<>())
                                    .put(paramCode, value);
                            }
                        }
                    }
                }
            }
            
            // 2. 生成时间标签
            List<String> timeLabels;
            if (allTimePoints.isEmpty()) {
                // 如果没有数据，生成默认时间标签
                timeLabels = generateTimeLabels(query.getQueryDate(), query.getTimeGranularity());
            } else {
                timeLabels = new ArrayList<>(allTimePoints);
            }
            chartData.setTimeLabels(timeLabels);
            
            log.info("生成时间标签数量：{}", timeLabels.size());
            
            // 3. 为每个仪表和参数创建数据系列
            List<TimeSeriesResultVO.SeriesVO> series = new ArrayList<>();
            String[] colors = {"#1890ff", "#52c41a", "#fa8c16", "#722ed1", "#eb2f96", "#13c2c2"};
            int colorIndex = 0;
            
            for (String moduleId : query.getModuleIds()) {
                String moduleName = moduleNameMap.getOrDefault(moduleId, "设备" + moduleId);
                
                for (Integer paramCode : query.getParameters()) {
                    TimeSeriesResultVO.SeriesVO seriesItem = new TimeSeriesResultVO.SeriesVO();
                    seriesItem.setModuleId(moduleId);
                    seriesItem.setModuleName(moduleName);
                    seriesItem.setParamCode(paramCode);
                    seriesItem.setParamName(getParameterName(paramCode));
                    seriesItem.setUnit(getParameterUnit(paramCode));
                    seriesItem.setColor(colors[colorIndex % colors.length]);
                    
                    // 填充数据
                    List<Double> data = new ArrayList<>();
                    for (String timePoint : timeLabels) {
                        Double value = null;
                        if (timeModuleParamDataMap.containsKey(timePoint) && 
                            timeModuleParamDataMap.get(timePoint).containsKey(moduleId) && 
                            timeModuleParamDataMap.get(timePoint).get(moduleId).containsKey(paramCode)) {
                            
                            value = timeModuleParamDataMap.get(timePoint).get(moduleId).get(paramCode);
                            // 保留两位小数
                            if (value != null) {
                                value = Math.round(value * 100.0) / 100.0;
                            }
                        }
                        data.add(value);
                    }
                    
                    seriesItem.setData(data);
                    series.add(seriesItem);
                    colorIndex++;
                }
            }
            
            chartData.setSeries(series);
            result.setChartData(chartData);
            
            log.info("生成数据系列完成，系列数量：{}", series.size());
            
            // 4. 生成表格数据
            List<TimeSeriesResultVO.TableRowVO> tableData = new ArrayList<>();
            for (int i = 0; i < timeLabels.size(); i++) {
                String timePoint = timeLabels.get(i);
                TimeSeriesResultVO.TableRowVO row = new TimeSeriesResultVO.TableRowVO();
                row.setTime(timePoint);
                row.setTimeLabel(timePoint);
                
                // 按仪表分组数据
                Map<String, List<TimeSeriesResultVO.ParameterDataVO>> moduleParameterMap = new HashMap<>();
                
                // 从时间点数据中提取每个仪表的参数数据
                if (timeModuleParamDataMap.containsKey(timePoint)) {
                    Map<String, Map<Integer, Double>> moduleParamMap = timeModuleParamDataMap.get(timePoint);
                    
                    for (String moduleId : query.getModuleIds()) {
                        if (moduleParamMap.containsKey(moduleId)) {
                            Map<Integer, Double> paramValueMap = moduleParamMap.get(moduleId);
                            
                            for (Integer paramCode : query.getParameters()) {
                                TimeSeriesResultVO.ParameterDataVO paramData = new TimeSeriesResultVO.ParameterDataVO();
                                paramData.setParamCode(paramCode);
                                paramData.setParamName(getParameterName(paramCode));
                                
                                Double value = paramValueMap.get(paramCode);
                                if (value != null) {
                                    value = Math.round(value * 100.0) / 100.0;
                                }
                                paramData.setValue(value);
                                paramData.setUnit(getParameterUnit(paramCode));
                                
                                moduleParameterMap.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(paramData);
                            }
                        } else {
                            // 如果该时间点没有该仪表的数据，添加空数据
                            for (Integer paramCode : query.getParameters()) {
                                TimeSeriesResultVO.ParameterDataVO paramData = new TimeSeriesResultVO.ParameterDataVO();
                                paramData.setParamCode(paramCode);
                                paramData.setParamName(getParameterName(paramCode));
                                paramData.setValue(null);
                                paramData.setUnit(getParameterUnit(paramCode));
                                
                                moduleParameterMap.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(paramData);
                            }
                        }
                    }
                } else {
                    // 如果该时间点没有数据，为所有仪表添加空数据
                    for (String moduleId : query.getModuleIds()) {
                        for (Integer paramCode : query.getParameters()) {
                            TimeSeriesResultVO.ParameterDataVO paramData = new TimeSeriesResultVO.ParameterDataVO();
                            paramData.setParamCode(paramCode);
                            paramData.setParamName(getParameterName(paramCode));
                            paramData.setValue(null);
                            paramData.setUnit(getParameterUnit(paramCode));
                            
                            moduleParameterMap.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(paramData);
                        }
                    }
                }
                
                // 创建仪表数据列表
                List<TimeSeriesResultVO.ModuleDataVO> moduleDataList = new ArrayList<>();
                for (Map.Entry<String, List<TimeSeriesResultVO.ParameterDataVO>> entry : moduleParameterMap.entrySet()) {
                    String moduleId = entry.getKey();
                    TimeSeriesResultVO.ModuleDataVO moduleData = new TimeSeriesResultVO.ModuleDataVO();
                    moduleData.setModuleId(moduleId);
                    moduleData.setModuleName(moduleNameMap.getOrDefault(moduleId, "设备" + moduleId));
                    moduleData.setParameters(entry.getValue());
                    moduleDataList.add(moduleData);
                }
                
                row.setModules(moduleDataList);
                tableData.add(row);
            }
            
            result.setTableData(tableData);
            
            // 5. 添加汇总信息
            TimeSeriesResultVO.SummaryVO summary = new TimeSeriesResultVO.SummaryVO();
            summary.setTotalDataPoints(timeLabels.size() * query.getModuleIds().size() * query.getParameters().size());
            summary.setTimeRange(startTime + " ~ " + endTime);
            summary.setGranularity(getGranularityDescription(query.getTimeGranularity()));
            summary.setModuleCount(query.getModuleIds().size());
            summary.setParameterCount(query.getParameters().size());
            // 注意：SummaryVO类中可能没有setDataType方法
            // summary.setDataType("时序监控数据");
            
            result.setSummary(summary);
            
        } catch (Exception e) {
            log.error("处理时序查询结果失败", e);
            // 如果处理失败，返回空结果
            result = new TimeSeriesResultVO();
            result.setChartData(new TimeSeriesResultVO.ChartDataVO());
            result.setTableData(new ArrayList<>());
        }
        
        return result;
    }
    
    /**
     * 根据字段名获取参数编码
     */
    private Integer getParamCodeByFieldName(String fieldName) {
        switch (fieldName) {
            case "IA": return 1;
            case "IB": return 2;
            case "IC": return 3;
            case "UA": return 4;
            case "UB": return 5;
            case "UC": return 6;
            case "P": return 7;  // 总有功功率
            case "QQ": return 8;
            case "SS": return 9;
            case "PFS": return 10;
            case "HZ": return 11;
            case "KWH": return 12;
            case "KVARH": return 13;
            case "TEMP": return 20;
            case "PRE": return 21;
            case "PV": return 22;
            case "SV": return 23;
            default: return null;
        }
    }

    /**
     * 处理负荷表格查询结果
     */
    private LoadTableResultVO processLoadTableResult(QueryResult queryResult,
            LoadTableQueryVO query, List<TbModule> modules) {

        log.info("处理负荷表格查询结果");
        LoadTableResultVO result = new LoadTableResultVO();
        List<LoadTableResultVO.LoadStatisticsRowVO> tableData = new ArrayList<>();

        try {
            // 解析InfluxDB查询结果
            Map<String, List<Map<String, Object>>> moduleDataMap = new HashMap<>();
            
            if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
                for (QueryResult.Result result1 : queryResult.getResults()) {
                    if (result1.getSeries() != null) {
                        for (QueryResult.Series series : result1.getSeries()) {
                            // 获取tagname
                            String tagname = series.getTags().get("tagname");
                            if (tagname != null && tagname.contains("#")) {
                                // 提取moduleId
                                String moduleId = tagname.substring(0, tagname.indexOf("#")).toLowerCase();
                                
                                // 确保是我们要查询的仪表
                                if (query.getModuleIds().contains(moduleId)) {
                                    List<Map<String, Object>> moduleData = new ArrayList<>();
                                    
                                    // 获取列名
                                    List<String> columns = series.getColumns();
                                    int timeIndex = columns.indexOf("time");
                                    int valueIndex = columns.indexOf("value");
                                    
                                    if (timeIndex >= 0 && valueIndex >= 0 && series.getValues() != null) {
                                        // 处理每一行数据
                                        for (List<Object> values : series.getValues()) {
                                            if (values.size() > Math.max(timeIndex, valueIndex)) {
                                                Map<String, Object> dataPoint = new HashMap<>();
                                                dataPoint.put("time", values.get(timeIndex));
                                                dataPoint.put("value", values.get(valueIndex));
                                                moduleData.add(dataPoint);
                                            }
                                        }
                                    }
                                    
                                    moduleDataMap.put(moduleId, moduleData);
                                }
                            }
                        }
                    }
                }
            }
            
            log.info("解析查询结果完成，共 {} 个仪表有数据", moduleDataMap.size());
            
            // 为每个仪表计算统计数据
            for (int i = 0; i < modules.size(); i++) {
                TbModule module = modules.get(i);
                String moduleId = module.getModuleId();
                LoadTableResultVO.LoadStatisticsRowVO row = new LoadTableResultVO.LoadStatisticsRowVO();

                row.set序号(i + 1);
                row.set设备名称(module.getModuleName());

                // 获取仪表额定功率
                Double ratedPower = module.getRatedPower() != null ? module.getRatedPower() : 100.0;
                
                // 从查询结果中计算统计数据
                List<Map<String, Object>> moduleData = moduleDataMap.get(moduleId);
                
                if (moduleData != null && !moduleData.isEmpty()) {
                    // 计算最大功率、最小功率和平均功率
                    double maxPower = Double.MIN_VALUE;
                    double minPower = Double.MAX_VALUE;
                    double totalPower = 0;
                    int validDataCount = 0;
                    String maxPowerTime = "";
                    String minPowerTime = "";
                    
                    for (Map<String, Object> dataPoint : moduleData) {
                        Object valueObj = dataPoint.get("value");
                        String timeStr = (String) dataPoint.get("time");
                        
                        if (valueObj != null && timeStr != null) {
                            Double value = convertToDouble(valueObj);
                            
                            if (value != null) {
                                // 更新最大功率
                                if (value > maxPower) {
                                    maxPower = value;
                                    maxPowerTime = timeStr;
                                }
                                
                                // 更新最小功率
                                if (value < minPower) {
                                    minPower = value;
                                    minPowerTime = timeStr;
                                }
                                
                                // 累加总功率
                                totalPower += value;
                                validDataCount++;
                            }
                        }
                    }
                    
                    // 计算平均功率
                    double avgPower = validDataCount > 0 ? totalPower / validDataCount : 0;
                    
                    // 转换时间格式
                    if (!maxPowerTime.isEmpty()) {
                        maxPowerTime = timeZoneUtil.convertUTCToBeijing(maxPowerTime);
                    }
                    
                    if (!minPowerTime.isEmpty()) {
                        minPowerTime = timeZoneUtil.convertUTCToBeijing(minPowerTime);
                    }
                    
                    // 保留两位小数
                    maxPower = Math.round(maxPower * 100.0) / 100.0;
                    minPower = Math.round(minPower * 100.0) / 100.0;
                    avgPower = Math.round(avgPower * 100.0) / 100.0;
                    
                    // 计算负荷率
                    double maxPowerRate = (maxPower / ratedPower) * 100;
                    double minPowerRate = (minPower / ratedPower) * 100;
                    double avgPowerRate = (avgPower / ratedPower) * 100;
                    
                    // 保留两位小数
                    maxPowerRate = Math.round(maxPowerRate * 100.0) / 100.0;
                    minPowerRate = Math.round(minPowerRate * 100.0) / 100.0;
                    avgPowerRate = Math.round(avgPowerRate * 100.0) / 100.0;
                    
                    // 设置统计数据
                    row.set最大功率(maxPower);
                    row.set最大功率率(maxPowerRate);
                    row.set最大功率发生时间(maxPowerTime);
                    
                    row.set最小功率(minPower);
                    row.set最小功率率(minPowerRate);
                    row.set最小功率发生时间(minPowerTime);
                    
                    row.set平均功率(avgPower);
                    row.set平均功率率(avgPowerRate);
                } else {
                    // 如果没有数据，设置默认值
                    row.set最大功率(0.0);
                    row.set最大功率率(0.0);
                    row.set最大功率发生时间("");
                    
                    row.set最小功率(0.0);
                    row.set最小功率率(0.0);
                    row.set最小功率发生时间("");
                    
                    row.set平均功率(0.0);
                    row.set平均功率率(0.0);
                }

                tableData.add(row);
            }

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
            
            // 应用分页
            int startIndex = (query.getPageNum() - 1) * query.getPageSize();
            int endIndex = Math.min(startIndex + query.getPageSize(), tableData.size());
            
            if (startIndex < tableData.size()) {
                result.setTableData(tableData.subList(startIndex, endIndex));
            } else {
                result.setTableData(new ArrayList<>());
            }
            
        } catch (Exception e) {
            log.error("处理负荷表格查询结果失败", e);
            // 如果处理失败，返回空结果
            result.setTableData(new ArrayList<>());
            
            // 设置分页信息
            LoadTableResultVO.PaginationVO pagination = new LoadTableResultVO.PaginationVO();
            pagination.setTotal(0);
            pagination.setPageNum(query.getPageNum());
            pagination.setPageSize(query.getPageSize());
            pagination.setPages(0);
            result.setPagination(pagination);
            
            // 设置汇总信息
            LoadTableResultVO.LoadTableSummaryVO summary = new LoadTableResultVO.LoadTableSummaryVO();
            summary.setTotalModules(modules.size());
            summary.setTimeRange(query.getStartTime() + " ~ " + query.getEndTime());
            summary.setDataType("负荷统计数据");
            result.setSummary(summary);
        }

        return result;
    }

    /**
     * 构建负荷InfluxDB查询语句
     */
    private String buildLoadInfluxQuery(LoadTableQueryVO query) {
        // 构建查询PP字段的语句
        List<Integer> powerParams = Arrays.asList(7); // 7代表PP字段
        return influxDBQueryBuilder.buildTimeSeriesQuery(query.getModuleIds(), powerParams,
                query.getTimeType(), query.getStartTime(), query.getEndTime());
    }

    /**
     * 生成时间标签
     */
    private List<String> generateTimeLabels(String queryDate, String timeGranularity) {
        List<String> labels = new ArrayList<>();
        
        try {
            LocalDate date = LocalDate.parse(queryDate);
            
            switch (timeGranularity) {
                case "day":
                    for (int hour = 0; hour < 24; hour++) {
                        labels.add(String.format("%02d:00", hour));
                    }
                    break;
                case "month":
                    YearMonth yearMonth = YearMonth.from(date);
                    for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                        labels.add(String.format("%02d-%02d", yearMonth.getMonthValue(), day));
                    }
                    break;
                case "year":
                    for (int month = 1; month <= 12; month++) {
                        labels.add(String.format("%04d-%02d", date.getYear(), month));
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("生成时间标签失败", e);
        }
        
        return labels;
    }

    /**
     * 获取时间粒度描述
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
     * 从tagname中提取moduleId
     */
    private String extractModuleIdFromTagname(String tagname) {
        if (tagname == null || !tagname.contains("#")) {
            return null;
        }
        return tagname.split("#")[0].toLowerCase();
    }

    // 已删除未使用的extractDateFromDateTime方法

    /**
     * 转换对象为Double
     */
    private Double convertToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            } else if (obj instanceof String) {
                return Double.parseDouble((String) obj);
            }
        } catch (Exception e) {
            log.warn("转换数值失败: {}", obj, e);
        }
        return null;
    }

    /**
     * 根据维度编码获取部门ID列表
     */
    private List<String> getDepartIdsByOrgCode(String dimensionCode, Boolean includeChildren) {
        List<String> departIds = new ArrayList<>();
        
        try {
            QueryWrapper<SysDepart> departQuery = new QueryWrapper<>();
            
            if (includeChildren != null && includeChildren) {
                // 包含子维度：查找以该编码开头的所有部门
                departQuery.likeRight("org_code", dimensionCode);
            } else {
                // 不包含子维度：精确匹配
                departQuery.eq("org_code", dimensionCode);
            }
            
            // 只查询未删除的部门
            departQuery.eq("del_flag", "0");
            
            List<SysDepart> departs = sysDepartMapper.selectList(departQuery);
            
            for (SysDepart depart : departs) {
                if (depart.getId() != null) {
                    departIds.add(depart.getId());
                }
            }
            
            log.info("维度编码 {} 对应的部门数量：{}", dimensionCode, departIds.size());
            
        } catch (Exception e) {
            log.error("根据维度编码查找部门ID失败", e);
        }
        
        return departIds;
    }

    /**
     * 根据参数编码获取字段名
     */
    private String getParameterFieldName(Integer paramCode) {
        switch (paramCode) {
            case 1: return "IA";
            case 2: return "IB";
            case 3: return "IC";
            case 4: return "UA";
            case 5: return "UB";
            case 6: return "UC";
            case 7: return "P";  // 修复：总有功功率字段名从PP改为P
            case 8: return "QQ";
            case 9: return "SS";
            case 10: return "PFS";
            case 11: return "HZ";
            case 12: return "KWH";
            case 13: return "KVARH";
            default: return "VALUE";
        }
    }
}
