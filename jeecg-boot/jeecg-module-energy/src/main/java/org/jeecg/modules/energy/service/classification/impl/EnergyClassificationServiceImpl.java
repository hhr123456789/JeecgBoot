package org.jeecg.modules.energy.service.classification.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.entity.classification.TbEnergyClassificationSummary;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.mapper.TbEnergyRatioInfoMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.mapper.classification.TbEnergyClassificationSummaryMapper;
import org.jeecg.modules.energy.mapper.classification.TbEnergyTypeConfigMapper;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationSyncService;
import org.jeecg.modules.energy.vo.classification.*;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业分类分区统计服务实现类
 * @author jeecg
 */
@Slf4j
@Service
public class EnergyClassificationServiceImpl implements IEnergyClassificationService {
    
    @Autowired
    private TbEnergyClassificationSummaryMapper summaryMapper;
    
    @Autowired
    private TbEnergyTypeConfigMapper energyTypeConfigMapper;
    
    @Autowired
    private IEnergyClassificationSyncService syncService;
    
    @Autowired
    private SysDepartMapper sysDepartMapper;
    
    @Autowired
    private TbEnergyRatioInfoMapper energyRatioInfoMapper;
    
    @Autowired
    private TbModuleMapper moduleMapper;
    
    @Override
    public List<OrgTreeVO> getOrgTree() {
        log.info("==== 开始从sys_depart表查询部门树形结构(只展示到二级) ====");
        
        try {
            // 查询所有启用的部门
            LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
            query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
            query.eq(SysDepart::getStatus, "1"); // 启用状态
            
            // 如果开启多租户,过滤租户
            if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
                int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
                query.eq(SysDepart::getTenantId, tenantId);
                log.info("多租户模式已启用,租户ID: {}", tenantId);
            } else {
                log.info("多租户模式未启用");
            }
            
            query.orderByAsc(SysDepart::getDepartOrder);
            List<SysDepart> allDepartList = sysDepartMapper.selectList(query);
            
            if(allDepartList == null || allDepartList.isEmpty()){
                log.warn("==== 未查询到部门数据,返回空列表 ====");
                return new ArrayList<>();
            }
            
            log.info("==== 查询到部门数量: {} ====", allDepartList.size());
            // 打印前3个部门信息用于调试
            for(int i = 0; i < Math.min(3, allDepartList.size()); i++) {
                SysDepart dept = allDepartList.get(i);
                log.info("部门{}: id={}, orgCode={}, departName={}, parentId={}", 
                    i+1, dept.getId(), dept.getOrgCode(), dept.getDepartName(), dept.getParentId());
            }
            
            // 构建树形结构
            List<SysDepartTreeModel> treeList = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(allDepartList);
            log.info("构建树形结构完成,根节点数量: {}", treeList.size());
            
            // 转换为OrgTreeVO并只保留到二级
            List<OrgTreeVO> result = convertToOrgTreeVO(treeList, 0, 2);
            
            log.info("==== 返回部门树节点数量: {} ====", result.size());
            // 打印结果树结构
            for(int i = 0; i < Math.min(2, result.size()); i++) {
                OrgTreeVO vo = result.get(i);
                log.info("结果树节点{}: id={}, orgCode={}, orgName={}, 子节点数={}", 
                    i+1, vo.getId(), vo.getOrgCode(), vo.getOrgName(), 
                    vo.getChildren() != null ? vo.getChildren().size() : 0);
            }
            return result;
            
        } catch (Exception e) {
            log.error("==== 查询部门树形结构失败 ====", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 将SysDepartTreeModel转换为OrgTreeVO,只保留到指定级别
     * @param treeList 源树列表
     * @param currentLevel 当前级别
     * @param maxLevel 最大级别
     * @return 转换后的树列表
     */
    private List<OrgTreeVO> convertToOrgTreeVO(List<SysDepartTreeModel> treeList, int currentLevel, int maxLevel) {
        if(treeList == null || treeList.isEmpty() || currentLevel >= maxLevel) {
            return new ArrayList<>();
        }
        
        List<OrgTreeVO> result = new ArrayList<>();
        for(SysDepartTreeModel model : treeList) {
            OrgTreeVO vo = new OrgTreeVO();
            vo.setId(model.getId());
            vo.setOrgCode(model.getOrgCode());
            vo.setOrgName(model.getDepartName());
            vo.setParentId(model.getParentId());
            
            // 如果还没到达最大级别且有子节点,递归处理
            if(currentLevel < maxLevel - 1 && model.getChildren() != null && !model.getChildren().isEmpty()) {
                List<OrgTreeVO> children = convertToOrgTreeVO(model.getChildren(), currentLevel + 1, maxLevel);
                vo.setChildren(children);
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public List<EnergyTypeVO> getEnergyTypes() {
        log.info("开始从tb_energy_ratio_info表查询能源类型配置");
        
        try {
            List<EnergyTypeVO> result = energyRatioInfoMapper.selectAllEnergyTypes();
            log.info("查询到能源类型数量: {}", result != null ? result.size() : 0);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询能源类型失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<EnergyTypeVO> getEnergyTypesByOrgCode(String orgCode) {
        log.info("根据orgCode查询该部门下设备的能源类型: {}", orgCode);
        
        if(orgCode == null || orgCode.trim().isEmpty()) {
            log.warn("部门编码为空,返回所有能源类型");
            return getEnergyTypes();
        }
        
        try {
            List<EnergyTypeVO> result = energyRatioInfoMapper.selectEnergyTypesByOrgCode(orgCode);
            log.info("查询到该部门下设备的能源类型数量: {}", result != null ? result.size() : 0);
            
            if(result == null || result.isEmpty()) {
                log.warn("该部门下没有启用的设备,返回空列表");
                return new ArrayList<>();
            }
            
            return result;
        } catch (Exception e) {
            log.error("根据orgCode查询能源类型失败: {}", orgCode, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public ComparisonDataVO getComparisonData(ClassificationQueryParam param) {
        log.info("获取横向对比数据: {}", param);
        
        ComparisonDataVO result = new ComparisonDataVO();
        
        if(param == null || param.getOrgCode() == null || param.getOrgCode().trim().isEmpty()) {
            log.warn("部门编码为空,返回空对比数据");
            return result;
        }
        
        try {
            // 1. 查询当前部门信息,判断层级
            LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
            departQuery.eq(SysDepart::getOrgCode, param.getOrgCode());
            departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
            SysDepart currentDepart = sysDepartMapper.selectOne(departQuery);
            
            if(currentDepart == null) {
                log.warn("未找到部门信息: {}", param.getOrgCode());
                return result;
            }
            
            // 2. 判断层级: 如果parentId为空或为"0",则为一级部门; 否则为二级部门
            boolean isFirstLevel = (currentDepart.getParentId() == null || 
                                   "0".equals(currentDepart.getParentId()) || 
                                   currentDepart.getParentId().trim().isEmpty());
            
            if(isFirstLevel) {
                // 一级部门: 查询其下所有二级子部门的统计数据
                result = getSubDepartmentComparison(currentDepart, param);
            } else {
                // 二级部门: 查询该部门下所有设备的统计数据
                result = getDeviceComparison(currentDepart, param);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取横向对比数据失败: {}", param.getOrgCode(), e);
            return result;
        }
    }
    
    /**
     * 获取子部门横向对比数据(一级部门展示其下所有二级部门对比)
     */
    private ComparisonDataVO getSubDepartmentComparison(SysDepart parentDepart, ClassificationQueryParam param) {
        ComparisonDataVO result = new ComparisonDataVO();
        result.setComparisonType("department");
        result.setParentOrgCode(parentDepart.getOrgCode());
        result.setParentOrgName(parentDepart.getDepartName());

        // 1. 查询该一级部门下的所有二级子部门
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysDepart::getParentId, parentDepart.getId());
        query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.eq(SysDepart::getStatus, "1");
        query.orderByAsc(SysDepart::getDepartOrder);

        List<SysDepart> subDeparts = sysDepartMapper.selectList(query);
        if(subDeparts == null || subDeparts.isEmpty()) {
            log.warn("一级部门 {} 下没有二级子部门", parentDepart.getOrgCode());
            return result;
        }

        // 2. 构建X轴数据(部门名称列表)
        List<String> xAxisData = subDeparts.stream()
            .map(SysDepart::getDepartName)
            .collect(Collectors.toList());
        result.setXAxisData(xAxisData);

        // 3. 查询每个子部门的统计数据
        List<ComparisonItemVO> comparisonItems = new ArrayList<>();
        Map<Integer, List<BigDecimal>> energyTypeDataMap = new HashMap<>();

        for(SysDepart subDepart : subDeparts) {
            // 查询该子部门的统计汇总数据 - 使用部门ID而不是orgCode
            LambdaQueryWrapper<TbEnergyClassificationSummary> summaryQuery = new LambdaQueryWrapper<>();
            summaryQuery.eq(TbEnergyClassificationSummary::getOrgCode, subDepart.getId()); // 修改：使用部门ID
            summaryQuery.eq(TbEnergyClassificationSummary::getTimeDimension, param.getTimeDimension());
            
            // 如果指定了能源类型
            if(!"all".equalsIgnoreCase(param.getEnergyType())) {
                summaryQuery.eq(TbEnergyClassificationSummary::getEnergyType, Integer.parseInt(param.getEnergyType()));
            }
            
            // 时间范围过滤
            if(param.getStartDate() != null && param.getEndDate() != null) {
                summaryQuery.between(TbEnergyClassificationSummary::getStatDate, param.getStartDate(), param.getEndDate());
            }
            
            List<TbEnergyClassificationSummary> summaries = summaryMapper.selectList(summaryQuery);
            
            // 按能源类型汇总
            Map<Integer, ComparisonItemVO> energyTypeMap = new HashMap<>();
            for(TbEnergyClassificationSummary summary : summaries) {
                Integer energyType = summary.getEnergyType();
                ComparisonItemVO item = energyTypeMap.get(energyType);
                if(item == null) {
                    item = new ComparisonItemVO();
                    item.setId(subDepart.getOrgCode());
                    item.setName(subDepart.getDepartName());
                    item.setEnergyType(energyType);
                    item.setEnergyTypeName(summary.getEnergyTypeName());
                    item.setTotalConsumption(BigDecimal.ZERO);
                    item.setTotalCost(BigDecimal.ZERO);
                    item.setCarbonEmission(BigDecimal.ZERO);
                    item.setStandardCoal(BigDecimal.ZERO);
                    item.setPeakConsumption(BigDecimal.ZERO);
                    item.setFlatConsumption(BigDecimal.ZERO);
                    item.setValleyConsumption(BigDecimal.ZERO);
                    item.setMeterCount(0);
                    energyTypeMap.put(energyType, item);
                }
                
                // 累加各项数据
                item.setTotalConsumption(item.getTotalConsumption().add(summary.getTotalConsumption() != null ? summary.getTotalConsumption() : BigDecimal.ZERO));
                item.setTotalCost(item.getTotalCost().add(summary.getTotalCost() != null ? summary.getTotalCost() : BigDecimal.ZERO));
                item.setCarbonEmission(item.getCarbonEmission().add(summary.getCarbonEmission() != null ? summary.getCarbonEmission() : BigDecimal.ZERO));
                item.setStandardCoal(item.getStandardCoal().add(summary.getStandardCoal() != null ? summary.getStandardCoal() : BigDecimal.ZERO));
                
                if(energyType == 1) { // 电力数据才有峰谷平
                    item.setPeakConsumption(item.getPeakConsumption().add(summary.getPeakConsumption() != null ? summary.getPeakConsumption() : BigDecimal.ZERO));
                    item.setFlatConsumption(item.getFlatConsumption().add(summary.getFlatConsumption() != null ? summary.getFlatConsumption() : BigDecimal.ZERO));
                    item.setValleyConsumption(item.getValleyConsumption().add(summary.getValleyConsumption() != null ? summary.getValleyConsumption() : BigDecimal.ZERO));
                }
                
                item.setMeterCount(item.getMeterCount() + (summary.getMeterCount() != null ? summary.getMeterCount() : 0));
            }
            
            comparisonItems.addAll(energyTypeMap.values());
            
            // 为图表准备数据 - 按能源类型分组
            for(Map.Entry<Integer, ComparisonItemVO> entry : energyTypeMap.entrySet()) {
                Integer energyType = entry.getKey();
                BigDecimal consumption = entry.getValue().getTotalConsumption();
                
                if(!energyTypeDataMap.containsKey(energyType)) {
                    energyTypeDataMap.put(energyType, new ArrayList<>());
                }
                energyTypeDataMap.get(energyType).add(consumption);
            }
        }
        
        result.setComparisonItems(comparisonItems);
        
        // 4. 构建系列数据(用于图表展示)
        List<ComparisonSeriesVO> seriesData = new ArrayList<>();
        for(Map.Entry<Integer, List<BigDecimal>> entry : energyTypeDataMap.entrySet()) {
            ComparisonSeriesVO series = new ComparisonSeriesVO();
            series.setType("bar");
            series.setData(entry.getValue());
            
            // 设置系列名称
            if(entry.getKey() == 1) {
                series.setName("电能");
                series.setUnit("kWh");
            } else if(entry.getKey() == 2) {
                series.setName("水能");
                series.setUnit("t");
            } else if(entry.getKey() == 3) {
                series.setName("燃气");
                series.setUnit("m³");
            } else {
                series.setName("能源类型" + entry.getKey());
                series.setUnit("");
            }
            
            seriesData.add(series);
        }
        result.setSeriesData(seriesData);
        
        return result;
    }
    
    /**
     * 获取设备横向对比数据(二级部门展示其下所有设备对比)
     */
    private ComparisonDataVO getDeviceComparison(SysDepart depart, ClassificationQueryParam param) {
        ComparisonDataVO result = new ComparisonDataVO();
        result.setComparisonType("device");
        result.setParentOrgCode(depart.getOrgCode());
        result.setParentOrgName(depart.getDepartName());

        // 1. 查询该部门下的所有启用设备 - 使用部门ID而不是orgCode
        LambdaQueryWrapper<TbModule> moduleQuery = new LambdaQueryWrapper<>();
        moduleQuery.eq(TbModule::getSysOrgCode, depart.getId()); // 修改：使用部门ID
        moduleQuery.eq(TbModule::getIsaction, "Y"); // 启用的设备
        
        // 如果指定了能源类型
        if(!"all".equalsIgnoreCase(param.getEnergyType())) {
            moduleQuery.eq(TbModule::getEnergyType, Integer.parseInt(param.getEnergyType()));
        }
        
        List<TbModule> modules = moduleMapper.selectList(moduleQuery);
        if(modules == null || modules.isEmpty()) {
            log.warn("二级部门 {} 下没有启用的设备", depart.getOrgCode());
            return result;
        }
        
        // 2. 构建X轴数据(设备名称列表)
        List<String> xAxisData = modules.stream()
            .map(TbModule::getModuleName)
            .collect(Collectors.toList());
        result.setXAxisData(xAxisData);
        
        // 3. 查询每个设备的统计数据
        // 注意: 由于当前 tb_energy_classification_summary 表可能不支持设备级统计
        // 这里需要从 tb_ep_equ_energy_daycount 等表查询实时数据
        // 暂时返回基础结构,实际数据查询逻辑需要根据实际表结构调整
        
        List<ComparisonItemVO> comparisonItems = new ArrayList<>();
        Map<Integer, List<BigDecimal>> energyTypeDataMap = new HashMap<>();
        
        for(TbModule module : modules) {
            ComparisonItemVO item = new ComparisonItemVO();
            item.setId(module.getModuleId());
            item.setName(module.getModuleName());
            item.setEnergyType(module.getEnergyType());
            
            // 设置能源类型名称
            if(module.getEnergyType() == 1) {
                item.setEnergyTypeName("电能");
            } else if(module.getEnergyType() == 2) {
                item.setEnergyTypeName("水能");
            } else if(module.getEnergyType() == 3) {
                item.setEnergyTypeName("燃气");
            }
            
            // TODO: 这里需要查询设备的实际统计数据
            // 暂时设置为0,后续需要根据实际业务调整
            item.setTotalConsumption(BigDecimal.ZERO);
            item.setTotalCost(BigDecimal.ZERO);
            item.setCarbonEmission(BigDecimal.ZERO);
            item.setStandardCoal(BigDecimal.ZERO);
            item.setPeakConsumption(BigDecimal.ZERO);
            item.setFlatConsumption(BigDecimal.ZERO);
            item.setValleyConsumption(BigDecimal.ZERO);
            item.setMeterCount(1);
            
            comparisonItems.add(item);
            
            // 为图表准备数据
            Integer energyType = module.getEnergyType();
            if(!energyTypeDataMap.containsKey(energyType)) {
                energyTypeDataMap.put(energyType, new ArrayList<>());
            }
            energyTypeDataMap.get(energyType).add(item.getTotalConsumption());
        }
        
        result.setComparisonItems(comparisonItems);
        
        // 4. 构建系列数据
        List<ComparisonSeriesVO> seriesData = new ArrayList<>();
        for(Map.Entry<Integer, List<BigDecimal>> entry : energyTypeDataMap.entrySet()) {
            ComparisonSeriesVO series = new ComparisonSeriesVO();
            series.setType("bar");
            series.setData(entry.getValue());
            
            if(entry.getKey() == 1) {
                series.setName("电能");
                series.setUnit("kWh");
            } else if(entry.getKey() == 2) {
                series.setName("水能");
                series.setUnit("t");
            } else if(entry.getKey() == 3) {
                series.setName("燃气");
                series.setUnit("m³");
            }
            
            seriesData.add(series);
        }
        result.setSeriesData(seriesData);
        
        log.info("设备对比数据查询完成,部门: {}, 设备数量: {}", depart.getDepartName(), modules.size());
        
        return result;
    }
    
    @Override
    public ClassificationSummaryVO getSummaryData(ClassificationQueryParam param) {
        log.info("查询分类分区统计汇总数据: {}", param);
        
        ClassificationSummaryVO result = new ClassificationSummaryVO();
        
        // 1. 获取统计数据
        StatisticsDataVO statisticsData = getStatisticsData(param);
        result.setStatisticsData(statisticsData);
        
        // 2. 获取饼图数据
        PieChartDataVO pieChartData = getPieChartData(param, statisticsData);
        result.setPieChartData(pieChartData);
        
        // 3. 获取表格数据
        List<TableDataVO> tableData = getTableData(param, statisticsData);
        result.setTableData(tableData);
        
        return result;
    }
    
    @Override
    public TrendDataVO getTrendData(ClassificationQueryParam param) {
        log.info("查询趋势对比数据: {}", param);
        
        TrendDataVO result = new TrendDataVO();
        
        // 构建X轴数据
        TrendDataVO.XAxisDataVO xAxis = new TrendDataVO.XAxisDataVO();
        xAxis.setType("category");
        
        // 构建查询条件
        QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();

        // 部门条件 - 需要将 orgCode 转换为部门ID
        if (StringUtils.hasText(param.getOrgCode())) {
            // 根据 orgCode 查询部门信息，获取部门ID
            LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
            departQuery.eq(SysDepart::getOrgCode, param.getOrgCode());
            departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
            SysDepart depart = sysDepartMapper.selectOne(departQuery);

            if (depart != null) {
                // 使用部门ID进行查询
                if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                    // 包含子部门，查询该部门及其所有子部门
                    LambdaQueryWrapper<SysDepart> childQuery = new LambdaQueryWrapper<>();
                    childQuery.eq(SysDepart::getParentId, depart.getId());
                    childQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
                    List<SysDepart> childDeparts = sysDepartMapper.selectList(childQuery);

                    // 收集所有部门ID
                    List<String> departIds = new ArrayList<>();
                    departIds.add(depart.getId());
                    if (childDeparts != null && !childDeparts.isEmpty()) {
                        departIds.addAll(childDeparts.stream().map(SysDepart::getId).collect(Collectors.toList()));
                    }

                    // 使用 FIND_IN_SET 匹配逗号分隔的 org_code 字段
                    StringBuilder findInSetCondition = new StringBuilder();
                    for (int i = 0; i < departIds.size(); i++) {
                        if (i > 0) {
                            findInSetCondition.append(" OR ");
                        }
                        findInSetCondition.append("FIND_IN_SET('").append(departIds.get(i)).append("', org_code)");
                    }
                    queryWrapper.and(wrapper -> wrapper.apply(findInSetCondition.toString()));
                } else {
                    // 使用 FIND_IN_SET 匹配
                    queryWrapper.apply("FIND_IN_SET({0}, org_code)", depart.getId());
                }
            } else {
                log.warn("未找到部门信息: orgCode={}", param.getOrgCode());
            }
        }

        // 注意：time_dimension 条件已移除，因为数据固定为 'day'
        // queryWrapper.eq("time_dimension", param.getTimeDimension());

        // 时间范围条件
        if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
            queryWrapper.between("stat_date", param.getStartDate(), param.getEndDate());
        }

        // 根据时间维度选择字段
        String timeField;
        if ("day".equals(param.getTimeDimension())) {
            timeField = "stat_date";
        } else if ("month".equals(param.getTimeDimension())) {
            timeField = "stat_month";
        } else {
            timeField = "stat_year";
        }
        
        // 按时间和能源类型分组查询
        queryWrapper.select(timeField + ", energy_type, SUM(total_consumption) as total_consumption")
                   .groupBy(timeField + ", energy_type")
                   .orderByAsc(timeField);
        
        System.out.println("========== getTrendData 调试信息 ==========");
        System.out.println("查询参数: " + param);
        System.out.println("时间字段: " + timeField);
        System.out.println("SQL WHERE条件: " + queryWrapper.getCustomSqlSegment());
        System.out.println("SQL参数: " + queryWrapper.getParamNameValuePairs());
        
        List<Map<String, Object>> trendData = summaryMapper.selectMaps(queryWrapper);
        System.out.println("查询到趋势数据条数: " + trendData.size());
        
        if (trendData.isEmpty()) {
            System.out.println("警告：没有查询到趋势数据");
            // 尝试查询所有数据看看是否有数据
            QueryWrapper<TbEnergyClassificationSummary> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("time_dimension", param.getTimeDimension())
                       .last("LIMIT 5");
            List<TbEnergyClassificationSummary> checkData = summaryMapper.selectList(checkWrapper);
            System.out.println("数据库中该时间维度的数据示例: " + checkData.size() + " 条");
            for (TbEnergyClassificationSummary item : checkData) {
                System.out.println("  - " + item.getOrgCode() + ", " + item.getStatDate() + ", " + item.getTimeDimension());
            }
        }
        
        // 构建X轴数据
        Set<String> timePoints = new TreeSet<>();
        for (Map<String, Object> data : trendData) {
            Object timeValue = data.get(timeField);
            if (timeValue != null) {
                if ("day".equals(param.getTimeDimension()) && timeValue instanceof java.util.Date) {
                    // 对于日数据，格式化日期为YYYY-MM-DD
                    timePoints.add(DateUtil.format((java.util.Date) timeValue, "yyyy-MM-dd"));
                } else {
                    timePoints.add(String.valueOf(timeValue));
                }
            }
        }
        List<String> xAxisData = new ArrayList<>(timePoints);
        xAxis.setData(xAxisData);
        result.setXAxis(xAxis);
        System.out.println("X轴数据点数: " + xAxisData.size());
        
        // 构建系列数据
        List<TrendDataVO.SeriesDataVO> series = new ArrayList<>();
        
        // 按能源类型分组
        Map<Integer, List<BigDecimal>> energyTypeData = new HashMap<>();
        for (Map<String, Object> data : trendData) {
            Integer energyType = (Integer) data.get("energy_type");
            BigDecimal consumption = (BigDecimal) data.get("total_consumption");
            
            if (!energyTypeData.containsKey(energyType)) {
                energyTypeData.put(energyType, new ArrayList<>());
            }
            energyTypeData.get(energyType).add(consumption);
        }
        
        System.out.println("按能源类型分组后的数据: " + energyTypeData.keySet());
        
        // 动态处理所有能源类型
        for (Map.Entry<Integer, List<BigDecimal>> entry : energyTypeData.entrySet()) {
            Integer energyType = entry.getKey();
            List<BigDecimal> consumptions = entry.getValue();
            
            TrendDataVO.SeriesDataVO seriesItem = new TrendDataVO.SeriesDataVO();
            seriesItem.setType("line");
            seriesItem.setData(consumptions.stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));
            
            // 根据能源类型设置系列名称
            switch (energyType) {
                case 1:
                    seriesItem.setName("电能");
                    break;
                case 2:
                    seriesItem.setName("水能");
                    break;
                case 3:
                    seriesItem.setName("燃气");
                    break;
                case 5:
                    seriesItem.setName("压缩空气");
                    break;
                case 8:
                    seriesItem.setName("天然气");
                    break;
                default:
                    seriesItem.setName("能源类型" + energyType);
                    break;
            }
            
            series.add(seriesItem);
            System.out.println("添加系列: " + seriesItem.getName() + ", 数据点数: " + consumptions.size());
        }
        
        result.setSeries(series);
        return result;
    }
    
    @Override
    public void exportData(ClassificationQueryParam param, HttpServletResponse response) {
        try {
            // 构建查询条件
            QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();

            // 部门条件 - 需要将 orgCode 转换为部门ID，并使用 FIND_IN_SET 匹配
            if (StringUtils.hasText(param.getOrgCode())) {
                // 根据 orgCode 查询部门信息，获取部门ID
                LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
                departQuery.eq(SysDepart::getOrgCode, param.getOrgCode());
                departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
                SysDepart depart = sysDepartMapper.selectOne(departQuery);

                if (depart != null) {
                    log.info("导出数据 - 找到部门: id={}, departName={}, orgCode={}",
                            depart.getId(), depart.getDepartName(), depart.getOrgCode());
                    
                    // 使用部门ID进行查询，使用 FIND_IN_SET 匹配逗号分隔的 org_code 字段
                    if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                        // 包含子部门，查询该部门及其所有子部门
                        LambdaQueryWrapper<SysDepart> childQuery = new LambdaQueryWrapper<>();
                        childQuery.eq(SysDepart::getParentId, depart.getId());
                        childQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
                        List<SysDepart> childDeparts = sysDepartMapper.selectList(childQuery);

                        // 收集所有部门ID
                        List<String> departIds = new ArrayList<>();
                        departIds.add(depart.getId());
                        if (childDeparts != null && !childDeparts.isEmpty()) {
                            departIds.addAll(childDeparts.stream().map(SysDepart::getId).collect(Collectors.toList()));
                        }

                        // 使用 FIND_IN_SET 匹配逗号分隔的 org_code 字段
                        StringBuilder findInSetCondition = new StringBuilder();
                        for (int i = 0; i < departIds.size(); i++) {
                            if (i > 0) {
                                findInSetCondition.append(" OR ");
                            }
                            findInSetCondition.append("FIND_IN_SET('").append(departIds.get(i)).append("', org_code)");
                        }
                        queryWrapper.and(wrapper -> wrapper.apply(findInSetCondition.toString()));
                        log.info("导出数据 - 查询部门ID列表(FIND_IN_SET): {}", departIds);
                    } else {
                        // 使用 FIND_IN_SET 匹配
                        queryWrapper.apply("FIND_IN_SET({0}, org_code)", depart.getId());
                        log.info("导出数据 - 查询部门ID(FIND_IN_SET): {}", depart.getId());
                    }
                } else {
                    log.warn("导出数据 - 未找到部门信息: orgCode={}", param.getOrgCode());
                }
            }
            
            // 注意：time_dimension 条件已移除，因为数据固定为 'day'
            // queryWrapper.eq("time_dimension", param.getTimeDimension());
            
            // 时间范围条件
            if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
                queryWrapper.between("stat_date", param.getStartDate(), param.getEndDate());
            }
            
            // 能源类型条件
            if (!"all".equals(param.getEnergyType())) {
                queryWrapper.eq("energy_type", param.getEnergyType());
            }
            
            log.info("导出数据查询条件: {}", param);
            log.info("SQL WHERE条件: {}", queryWrapper.getCustomSqlSegment());
            
            // 查询实际数据
            List<TbEnergyClassificationSummary> exportList = summaryMapper.selectList(queryWrapper);
            
            if (exportList.isEmpty()) {
                log.warn("没有查询到符合条件的数据，导出空数据");
                // 如果没有数据，创建一个空数据的记录用于导出
                TbEnergyClassificationSummary emptyData = new TbEnergyClassificationSummary();
                emptyData.setOrgName("暂无数据");
                emptyData.setEnergyTypeName("暂无数据");
                emptyData.setTotalConsumption(BigDecimal.ZERO);
                emptyData.setTotalCost(BigDecimal.ZERO);
                emptyData.setCarbonEmission(BigDecimal.ZERO);
                exportList.add(emptyData);
            } else {
                log.info("查询到 {} 条数据用于导出", exportList.size());
            }
            
            // 设置响应头
            String fileName = "企业分类分区统计_" + DateUtil.format(new Date(), "yyyyMMdd_HHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            
            // 导出Excel
            ExportParams exportParams = new ExportParams("企业分类分区统计", "统计数据");
            exportParams.setType(ExcelType.XSSF);
            
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, TbEnergyClassificationSummary.class, exportList);
            workbook.write(response.getOutputStream());
            workbook.close();
            
            log.info("导出企业分类分区统计数据成功，共 {} 条数据", exportList.size());
            
        } catch (IOException e) {
            log.error("导出企业分类分区统计数据失败", e);
            throw new RuntimeException("导出失败");
        }
    }
    
    private StatisticsDataVO getStatisticsData(ClassificationQueryParam param) {
        log.info("========== getStatisticsData 开始 ==========");
        log.info("接收到的查询参数: orgCode={}, energyType={}, timeDimension={}, startDate={}, endDate={}, includeChildren={}",
                param.getOrgCode(), param.getEnergyType(), param.getTimeDimension(),
                param.getStartDate(), param.getEndDate(), param.getIncludeChildren());

        StatisticsDataVO statisticsData = new StatisticsDataVO();

        // 构建查询条件
        QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();

        // 部门条件 - 需要将 orgCode 转换为部门ID
        if (StringUtils.hasText(param.getOrgCode())) {
            log.info("开始根据 orgCode 查询部门信息: {}", param.getOrgCode());

            // 根据 orgCode 查询部门信息，获取部门ID
            LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
            departQuery.eq(SysDepart::getOrgCode, param.getOrgCode());
            departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
            SysDepart depart = sysDepartMapper.selectOne(departQuery);

            if (depart == null) {
                log.error("❌ 未找到部门信息: orgCode={}", param.getOrgCode());
                log.error("请检查: 1.orgCode是否正确 2.部门是否被删除 3.部门状态是否启用");
                // 返回空数据
                statisticsData.setTotalConsumption(BigDecimal.ZERO);
                statisticsData.setElectricConsumption(BigDecimal.ZERO);
                statisticsData.setWaterConsumption(BigDecimal.ZERO);
                statisticsData.setGasConsumption(BigDecimal.ZERO);
                statisticsData.setTotalCost(BigDecimal.ZERO);
                statisticsData.setTotalCarbonEmission(BigDecimal.ZERO);
                return statisticsData;
            }

            log.info("✓ 找到部门: id={}, departName={}, orgCode={}",
                    depart.getId(), depart.getDepartName(), depart.getOrgCode());

            // 使用部门ID进行查询
            if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                // 包含子部门，查询该部门及其所有子部门
                log.info("查询模式: 包含子部门");

                // 查询所有子部门
                LambdaQueryWrapper<SysDepart> childQuery = new LambdaQueryWrapper<>();
                childQuery.eq(SysDepart::getParentId, depart.getId());
                childQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
                List<SysDepart> childDeparts = sysDepartMapper.selectList(childQuery);

                // 收集所有部门ID（包括当前部门和子部门）
                List<String> departIds = new ArrayList<>();
                departIds.add(depart.getId());
                if (childDeparts != null && !childDeparts.isEmpty()) {
                    departIds.addAll(childDeparts.stream().map(SysDepart::getId).collect(Collectors.toList()));
                    log.info("找到 {} 个子部门", childDeparts.size());
                }

                // 使用 FIND_IN_SET 匹配逗号分隔的 org_code 字段
                // org_code 格式如: "id1,id2,id3"，需要用 FIND_IN_SET 匹配
                StringBuilder findInSetCondition = new StringBuilder();
                for (int i = 0; i < departIds.size(); i++) {
                    if (i > 0) {
                        findInSetCondition.append(" OR ");
                    }
                    findInSetCondition.append("FIND_IN_SET('").append(departIds.get(i)).append("', org_code)");
                }
                queryWrapper.and(wrapper -> wrapper.apply(findInSetCondition.toString()));
                log.info("✓ 查询部门ID列表(FIND_IN_SET): {}", departIds);
            } else {
                // 不包含子部门，使用 FIND_IN_SET 匹配
                log.info("查询模式: 仅查询当前部门");
                queryWrapper.apply("FIND_IN_SET({0}, org_code)", depart.getId());
                log.info("✓ 查询部门ID(FIND_IN_SET): {}", depart.getId());
            }
        } else {
            log.warn("⚠️ orgCode 参数为空，将查询所有部门数据");
        }
        
        // 注意：time_dimension 条件已移除
        // 因为同步数据时 time_dimension 固定为 'day'，但前端可能查询 'month' 或 'year'
        // 改为使用日期范围过滤，然后根据 timeDimension 参数聚合结果
        // queryWrapper.eq("time_dimension", param.getTimeDimension());
        log.info("时间维度参数(用于聚合): {}", param.getTimeDimension());

        // 时间范围条件
        if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
            queryWrapper.between("stat_date", param.getStartDate(), param.getEndDate());
            log.info("添加时间范围条件: {} 到 {}", param.getStartDate(), param.getEndDate());
        }

        // 能源类型条件
        if (!"all".equals(param.getEnergyType())) {
            queryWrapper.eq("energy_type", param.getEnergyType());
            log.info("添加能源类型条件: {}", param.getEnergyType());
        } else {
            log.info("查询所有能源类型");
        }

        log.info("========== 开始执行查询 ==========");
        log.info("MyBatis-Plus QueryWrapper: {}", queryWrapper.getCustomSqlSegment());

        // 查询数据
        List<TbEnergyClassificationSummary> summaryList = summaryMapper.selectList(queryWrapper);

        log.info("========== 查询结果 ==========");
        log.info("查询到 {} 条记录", summaryList != null ? summaryList.size() : 0);

        if (summaryList == null || summaryList.isEmpty()) {
            log.warn("⚠️⚠️⚠️ 没有查询到任何数据！");
            log.warn("可能的原因:");
            log.warn("1. tb_energy_classification_summary 表中没有数据");
            log.warn("2. org_code 字段值与部门ID不匹配");
            log.warn("3. 时间范围内没有数据");
            log.warn("4. 能源类型不匹配");
            log.warn("建议: 请运行 debug_query.sql 脚本检查数据库数据");
        } else {
            log.info("✓ 成功查询到数据，前3条记录:");
            for (int i = 0; i < Math.min(3, summaryList.size()); i++) {
                TbEnergyClassificationSummary item = summaryList.get(i);
                log.info("  [{}] orgCode={}, orgName={}, energyType={}, consumption={}, date={}",
                        i + 1, item.getOrgCode(), item.getOrgName(), item.getEnergyTypeName(),
                        item.getTotalConsumption(), item.getStatDate());
            }
        }
        
        // 统计汇总
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal electricConsumption = BigDecimal.ZERO;
        BigDecimal waterConsumption = BigDecimal.ZERO;
        BigDecimal gasConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalCarbonEmission = BigDecimal.ZERO;
        
        for (TbEnergyClassificationSummary summary : summaryList) {
            totalConsumption = totalConsumption.add(summary.getTotalConsumption());
            totalCost = totalCost.add(summary.getTotalCost());
            totalCarbonEmission = totalCarbonEmission.add(summary.getCarbonEmission());
            
            // 只有查询全部能源类型时，才按能源类型分类统计
            // 否则，所有消耗都算入总能耗，不拆分到分项
            if ("all".equals(param.getEnergyType())) {
                // 按能源类型分类统计
                if (summary.getEnergyType() == 1) {
                    electricConsumption = electricConsumption.add(summary.getTotalConsumption());
                } else if (summary.getEnergyType() == 2) {
                    waterConsumption = waterConsumption.add(summary.getTotalConsumption());
                } else if (summary.getEnergyType() == 3) {
                    gasConsumption = gasConsumption.add(summary.getTotalConsumption());
                }
            }
        }
        
        statisticsData.setTotalConsumption(totalConsumption);
        statisticsData.setElectricConsumption(electricConsumption);
        statisticsData.setWaterConsumption(waterConsumption);
        statisticsData.setGasConsumption(gasConsumption);
        statisticsData.setTotalCost(totalCost);
        statisticsData.setTotalCarbonEmission(totalCarbonEmission);
        
        log.info("统计数据汇总完成: 总能耗={}, 电能={}, 水能={}, 燃气={}", 
                totalConsumption, electricConsumption, waterConsumption, gasConsumption);
        
        return statisticsData;
    }
    
    private PieChartDataVO getPieChartData(ClassificationQueryParam param, StatisticsDataVO statisticsData) {
        PieChartDataVO pieChartData = new PieChartDataVO();
        List<PieChartDataVO.SeriesDataVO> series = new ArrayList<>();
        
        PieChartDataVO.SeriesDataVO seriesData = new PieChartDataVO.SeriesDataVO();
        seriesData.setName("能源分类占比");
        seriesData.setType("pie");
        
        List<PieChartDataVO.DataItemVO> data = new ArrayList<>();
        
        if ("all".equals(param.getEnergyType())) {
            // 全部能源类型 - 使用实际数据
            BigDecimal totalConsumption = statisticsData.getTotalConsumption();
            
            // 电能
            if (statisticsData.getElectricConsumption().compareTo(BigDecimal.ZERO) > 0) {
                PieChartDataVO.DataItemVO electric = new PieChartDataVO.DataItemVO();
                electric.setValue(statisticsData.getElectricConsumption().doubleValue());
                electric.setName("电能");
                double percentage = statisticsData.getElectricConsumption().divide(totalConsumption, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
                electric.setPercentage(percentage);
                data.add(electric);
            }
            
            // 水能
            if (statisticsData.getWaterConsumption().compareTo(BigDecimal.ZERO) > 0) {
                PieChartDataVO.DataItemVO water = new PieChartDataVO.DataItemVO();
                water.setValue(statisticsData.getWaterConsumption().doubleValue());
                water.setName("水能");
                double percentage = statisticsData.getWaterConsumption().divide(totalConsumption, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
                water.setPercentage(percentage);
                data.add(water);
            }
            
            // 燃气
            if (statisticsData.getGasConsumption().compareTo(BigDecimal.ZERO) > 0) {
                PieChartDataVO.DataItemVO gas = new PieChartDataVO.DataItemVO();
                gas.setValue(statisticsData.getGasConsumption().doubleValue());
                gas.setName("燃气");
                double percentage = statisticsData.getGasConsumption().divide(totalConsumption, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
                gas.setPercentage(percentage);
                data.add(gas);
            }
        } else {
            // 单一能源类型
            PieChartDataVO.DataItemVO item = new PieChartDataVO.DataItemVO();
            item.setValue(statisticsData.getTotalConsumption().doubleValue());
            
            Integer energyType = Integer.parseInt(param.getEnergyType());
            switch (energyType) {
                case 1:
                    item.setName("电能");
                    break;
                case 2:
                    item.setName("水能");
                    break;
                case 3:
                    item.setName("燃气");
                    break;
            }
            item.setPercentage(100.0);
            data.add(item);
        }
        
        seriesData.setData(data);
        series.add(seriesData);
        pieChartData.setSeries(series);
        
        return pieChartData;
    }
    
    private List<TableDataVO> getTableData(ClassificationQueryParam param, StatisticsDataVO statisticsData) {
        List<TableDataVO> tableData = new ArrayList<>();

        // 构建查询条件
        QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();

        // 部门条件 - 需要将 orgCode 转换为部门ID
        if (StringUtils.hasText(param.getOrgCode())) {
            // 根据 orgCode 查询部门信息，获取部门ID
            LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
            departQuery.eq(SysDepart::getOrgCode, param.getOrgCode());
            departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
            SysDepart depart = sysDepartMapper.selectOne(departQuery);

            if (depart == null) {
                log.warn("未找到部门信息: orgCode={}", param.getOrgCode());
                return tableData;
            }

            // 使用部门ID进行查询
            if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                // 包含子部门，查询该部门及其所有子部门
                LambdaQueryWrapper<SysDepart> childQuery = new LambdaQueryWrapper<>();
                childQuery.eq(SysDepart::getParentId, depart.getId());
                childQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
                List<SysDepart> childDeparts = sysDepartMapper.selectList(childQuery);

                // 收集所有部门ID
                List<String> departIds = new ArrayList<>();
                departIds.add(depart.getId());
                if (childDeparts != null && !childDeparts.isEmpty()) {
                    departIds.addAll(childDeparts.stream().map(SysDepart::getId).collect(Collectors.toList()));
                }

                // 使用 FIND_IN_SET 匹配逗号分隔的 org_code 字段
                StringBuilder findInSetCondition = new StringBuilder();
                for (int i = 0; i < departIds.size(); i++) {
                    if (i > 0) {
                        findInSetCondition.append(" OR ");
                    }
                    findInSetCondition.append("FIND_IN_SET('").append(departIds.get(i)).append("', org_code)");
                }
                queryWrapper.and(wrapper -> wrapper.apply(findInSetCondition.toString()));
            } else {
                queryWrapper.apply("FIND_IN_SET({0}, org_code)", depart.getId());
            }
        }
        
        // 注意：time_dimension 条件已移除，因为数据固定为 'day'
        // queryWrapper.eq("time_dimension", param.getTimeDimension());
        
        // 时间范围条件
        if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
            queryWrapper.between("stat_date", param.getStartDate(), param.getEndDate());
        }
        
        // 能源类型条件
        if (!"all".equals(param.getEnergyType())) {
            queryWrapper.eq("energy_type", param.getEnergyType());
        }
        
        // 按时间分组查询
        String selectTimeField;
        if ("day".equals(param.getTimeDimension())) {
            selectTimeField = "stat_date";
        } else if ("month".equals(param.getTimeDimension())) {
            selectTimeField = "stat_month";
        } else {
            selectTimeField = "stat_year";
        }
        
        // 按时间分组整理数据
        Map<String, Map<Integer, BigDecimal[]>> timeGroupData = new HashMap<>();
        
        try {
            queryWrapper.select(selectTimeField + " as time, energy_type, SUM(total_consumption) as total_consumption, SUM(total_cost) as total_cost, SUM(carbon_emission) as carbon_emission")
                       .groupBy(selectTimeField + ", energy_type")
                       .orderByAsc(selectTimeField);
            
            log.info("========== getTableData 调试信息 ==========");
            log.info("查询参数: {}", param);
            log.info("时间字段: {}", selectTimeField);
            log.info("SQL WHERE条件: {}", queryWrapper.getCustomSqlSegment());
            
            List<Map<String, Object>> rawData = summaryMapper.selectMaps(queryWrapper);
            log.info("查询到原始数据条数: {}", rawData.size());
            
            // 如果没有数据，返回空列表
            if (rawData.isEmpty()) {
                log.warn("警告：getTableData没有查询到数据");
                log.warn("查询参数: orgCode={}, energyType={}, timeDimension={}, startDate={}, endDate={}", 
                        param.getOrgCode(), param.getEnergyType(), param.getTimeDimension(), 
                        param.getStartDate(), param.getEndDate());
                return tableData;
            }
            
            for (Map<String, Object> data : rawData) {
                String time;
                Object timeValue = data.get("time");
                
                // 处理不同时间维度的数据格式
                if ("day".equals(param.getTimeDimension()) && timeValue instanceof java.util.Date) {
                    // 对于日数据，格式化日期为YYYY-MM-DD
                    time = DateUtil.format((java.util.Date) timeValue, "yyyy-MM-dd");
                } else {
                    time = String.valueOf(timeValue);
                }
                
                Integer energyType = (Integer) data.get("energy_type");
                BigDecimal consumption = (BigDecimal) data.get("total_consumption");
                BigDecimal cost = (BigDecimal) data.get("total_cost");
                BigDecimal carbonEmission = (BigDecimal) data.get("carbon_emission");
                
                if (!timeGroupData.containsKey(time)) {
                    timeGroupData.put(time, new HashMap<>());
                }
                // 存储: [consumption, cost, carbonEmission]
                timeGroupData.get(time).put(energyType, new BigDecimal[]{consumption, cost, carbonEmission != null ? carbonEmission : BigDecimal.ZERO});
            }
        } catch (Exception e) {
            System.err.println("getTableData查询失败: " + e.getMessage());
            e.printStackTrace();
            return tableData;
        }
        
        // 转换为TableDataVO
        for (Map.Entry<String, Map<Integer, BigDecimal[]>> entry : timeGroupData.entrySet()) {
            String time = entry.getKey();
            Map<Integer, BigDecimal[]> energyData = entry.getValue();
            
            TableDataVO tableRow = new TableDataVO();
            tableRow.setTime(time);
            
            // 判断是查询所有能源类型还是单一能源类型
            if ("all".equals(param.getEnergyType())) {
                // 查询所有能源类型：填充 electric, water, gas 等字段
                if (energyData.containsKey(1)) {
                    tableRow.setElectric(energyData.get(1)[0]);
                    tableRow.setElectricCost(energyData.get(1)[1]);
                }
                
                if (energyData.containsKey(2)) {
                    tableRow.setWater(energyData.get(2)[0]);
                    tableRow.setWaterCost(energyData.get(2)[1]);
                }
                
                if (energyData.containsKey(3)) {
                    tableRow.setGas(energyData.get(3)[0]);
                    tableRow.setGasCost(energyData.get(3)[1]);
                }
                
                // 计算总费用
                BigDecimal totalCost = BigDecimal.ZERO;
                if (tableRow.getElectricCost() != null) {
                    totalCost = totalCost.add(tableRow.getElectricCost());
                }
                if (tableRow.getWaterCost() != null) {
                    totalCost = totalCost.add(tableRow.getWaterCost());
                }
                if (tableRow.getGasCost() != null) {
                    totalCost = totalCost.add(tableRow.getGasCost());
                }
                tableRow.setTotalCost(totalCost);
            } else {
                // 查询单一能源类型：填充 consumption, cost, carbonEmission 字段
                Integer selectedEnergyType = Integer.parseInt(param.getEnergyType());
                if (energyData.containsKey(selectedEnergyType)) {
                    BigDecimal[] data = energyData.get(selectedEnergyType);
                    tableRow.setConsumption(data[0]);  // 消耗量
                    tableRow.setCost(data[1]);          // 成本
                    tableRow.setCarbonEmission(data[2]); // 碳排放
                }
            }
            
            tableData.add(tableRow);
        }
        
        return tableData;
    }

    // ==================== 定时任务相关方法实现 ====================

    @Override
    public Map<String, Object> triggerDataSync(Date startDate, Date endDate) {
        try {
            log.info("手动触发数据同步任务: startDate={}, endDate={}", startDate, endDate);
            return syncService.syncClassificationData(startDate, endDate);
        } catch (Exception e) {
            log.error("手动触发数据同步任务失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncByDate(Date targetDate) {
        try {
            log.info("按日期同步数据: targetDate={}", targetDate);
            return syncService.syncByDate(targetDate);
        } catch (Exception e) {
            log.error("按日期同步数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncByMonth(Integer year, Integer month) {
        try {
            log.info("按月份同步数据: year={}, month={}", year, month);
            return syncService.syncByMonth(year, month);
        } catch (Exception e) {
            log.error("按月份同步数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncByYear(Integer year) {
        try {
            log.info("按年份同步数据: year={}", year);
            return syncService.syncByYear(year);
        } catch (Exception e) {
            log.error("按年份同步数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> incrementalSync(Date startDate, Date endDate) {
        try {
            log.info("增量同步数据: startDate={}, endDate={}", startDate, endDate);
            return syncService.incrementalSync(startDate, endDate);
        } catch (Exception e) {
            log.error("增量同步数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncAllUnsyncedData() {
        try {
            log.info("同步所有未同步的数据");
            return syncService.syncAllUnsyncedData();
        } catch (Exception e) {
            log.error("同步所有未同步数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> recalculateStatistics(Date startDate, Date endDate) {
        try {
            log.info("重新计算统计数据: startDate={}, endDate={}", startDate, endDate);
            return syncService.recalculateStatistics(startDate, endDate);
        } catch (Exception e) {
            log.error("重新计算统计数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> cleanupOldData(Date startDate, Date endDate) {
        try {
            log.info("清理历史数据: startDate={}, endDate={}", startDate, endDate);
            return syncService.cleanupOldData(startDate, endDate);
        } catch (Exception e) {
            log.error("清理历史数据失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Date getLatestStatisticsDate() {
        try {
            return syncService.getLatestStatisticsDate();
        } catch (Exception e) {
            log.error("获取最新统计日期失败", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> validateDataCompleteness(Date startDate, Date endDate) {
        try {
            log.info("验证数据完整性: startDate={}, endDate={}", startDate, endDate);
            return syncService.validateDataCompleteness(startDate, endDate);
        } catch (Exception e) {
            log.error("验证数据完整性失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> getRealTimeDataStatistics(Date startDate, Date endDate) {
        try {
            log.debug("获取实时表数据统计信息: startDate={}, endDate={}", startDate, endDate);
            return syncService.getRealTimeDataStatistics(startDate, endDate);
        } catch (Exception e) {
            log.error("获取实时表数据统计信息失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean hasUnsyncedData(Date targetDate) {
        try {
            return syncService.hasUnsyncedData(targetDate);
        } catch (Exception e) {
            log.error("检查未同步数据失败: targetDate={}", targetDate, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getSyncTaskStatus() {
        try {
            log.debug("获取同步任务状态信息");
            
            Map<String, Object> status = new HashMap<>();
            status.put("taskStatus", "RUNNING"); // 模拟任务状态
            status.put("lastSyncTime", new Date());
            status.put("nextSyncTime", new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5分钟后
            status.put("isPaused", false);
            status.put("totalTasks", 0);
            status.put("completedTasks", 0);
            status.put("failedTasks", 0);
            
            return status;
        } catch (Exception e) {
            log.error("获取同步任务状态失败", e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("taskStatus", "ERROR");
            errorStatus.put("errorMessage", e.getMessage());
            return errorStatus;
        }
    }

    @Override
    public Map<String, Object> pauseSyncTask() {
        try {
            log.info("暂停同步任务");
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("message", "同步任务已暂停");
            result.put("pauseTime", new Date());
            
            return result;
        } catch (Exception e) {
            log.error("暂停同步任务失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> resumeSyncTask() {
        try {
            log.info("恢复同步任务");
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("message", "同步任务已恢复");
            result.put("resumeTime", new Date());
            
            return result;
        } catch (Exception e) {
            log.error("恢复同步任务失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "FAILED");
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> getSyncProgress(String taskId) {
        try {
            log.debug("获取同步进度: taskId={}", taskId);
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("taskId", taskId);
            progress.put("status", "COMPLETED"); // 模拟完成状态
            progress.put("progress", 100);
            progress.put("startTime", new Date(System.currentTimeMillis() - 10 * 60 * 1000)); // 10分钟前
            progress.put("endTime", new Date());
            progress.put("totalRecords", 0);
            progress.put("processedRecords", 0);
            progress.put("successRecords", 0);
            progress.put("failedRecords", 0);
            
            return progress;
        } catch (Exception e) {
            log.error("获取同步进度失败: taskId={}", taskId, e);
            Map<String, Object> errorProgress = new HashMap<>();
            errorProgress.put("taskId", taskId);
            errorProgress.put("status", "ERROR");
            errorProgress.put("errorMessage", e.getMessage());
            return errorProgress;
        }
    }

    @Override
    public Map<String, Object> getDebugSummaryData(Integer limit) {
        log.info("获取汇总表调试数据，limit={}", limit);
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // 1. 获取汇总表总记录数
            long totalCount = summaryMapper.selectCount(null);
            debugInfo.put("totalRecordCount", totalCount);
            
            // 2. 获取最近的记录样例
            QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("stat_date")
                       .last("LIMIT " + limit);
            List<TbEnergyClassificationSummary> recentRecords = summaryMapper.selectList(queryWrapper);
            
            // 3. 转换为简化格式
            List<Map<String, Object>> records = new ArrayList<>();
            for (TbEnergyClassificationSummary record : recentRecords) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId());
                item.put("orgCode", record.getOrgCode());
                item.put("orgName", record.getOrgName());
                item.put("energyType", record.getEnergyType());
                item.put("energyTypeName", record.getEnergyTypeName());
                item.put("statDate", record.getStatDate());
                item.put("statMonth", record.getStatMonth());
                item.put("statYear", record.getStatYear());
                item.put("timeDimension", record.getTimeDimension());
                item.put("totalConsumption", record.getTotalConsumption());
                item.put("totalCost", record.getTotalCost());
                item.put("meterCount", record.getMeterCount());
                records.add(item);
            }
            debugInfo.put("recentRecords", records);
            
            // 4. 统计各 time_dimension 的记录数
            QueryWrapper<TbEnergyClassificationSummary> dimQuery = new QueryWrapper<>();
            dimQuery.select("time_dimension, COUNT(*) as count")
                   .groupBy("time_dimension");
            List<Map<String, Object>> dimensionStats = summaryMapper.selectMaps(dimQuery);
            debugInfo.put("timeDimensionStats", dimensionStats);
            
            // 5. 统计各 org_code 的记录数（前10个）
            QueryWrapper<TbEnergyClassificationSummary> orgQuery = new QueryWrapper<>();
            orgQuery.select("org_code, COUNT(*) as count")
                   .groupBy("org_code")
                   .orderByDesc("count")
                   .last("LIMIT 10");
            List<Map<String, Object>> orgStats = summaryMapper.selectMaps(orgQuery);
            debugInfo.put("topOrgCodeStats", orgStats);
            
            // 6. 获取 sys_depart 表的部门ID样例
            LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
            departQuery.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString())
                      .last("LIMIT 10");
            List<SysDepart> departs = sysDepartMapper.selectList(departQuery);
            List<Map<String, Object>> departSamples = new ArrayList<>();
            for (SysDepart d : departs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", d.getId());
                item.put("departName", d.getDepartName());
                item.put("orgCode", d.getOrgCode());
                item.put("parentId", d.getParentId());
                departSamples.add(item);
            }
            debugInfo.put("departSamples", departSamples);
            
            log.info("调试数据获取成功: 总记录数={}, 样例数={}", totalCount, records.size());
            
        } catch (Exception e) {
            log.error("获取调试数据失败", e);
            debugInfo.put("error", e.getMessage());
        }
        
        return debugInfo;
    }
}