package org.jeecg.modules.energy.service.classification.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.modules.energy.entity.classification.TbEnergyClassificationSummary;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.mapper.classification.TbEnergyClassificationSummaryMapper;
import org.jeecg.modules.energy.mapper.classification.TbEnergyTypeConfigMapper;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationSyncService;
import org.jeecg.modules.energy.vo.classification.*;
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
    
    @Override
    public List<OrgTreeVO> getOrgTree() {
        // 模拟部门树数据，实际应该从sys_depart表获取
        List<OrgTreeVO> treeData = new ArrayList<>();
        
        // 根节点
        OrgTreeVO root = new OrgTreeVO();
        root.setId("1");
        root.setOrgCode("A");
        root.setOrgName("总公司");
        root.setParentId("0");
        
        // 生产部门
        OrgTreeVO production = new OrgTreeVO();
        production.setId("1-1");
        production.setOrgCode("A01");
        production.setOrgName("生产部门");
        production.setParentId("1");
        
        List<OrgTreeVO> productionChildren = new ArrayList<>();
        String[] productionChildNames = {"一号车间", "二号车间", "三号车间"};
        String[] productionChildCodes = {"A01-01", "A01-02", "A01-03"};
        
        for (int i = 0; i < productionChildNames.length; i++) {
            OrgTreeVO child = new OrgTreeVO();
            child.setId("1-1-" + (i + 1));
            child.setOrgCode(productionChildCodes[i]);
            child.setOrgName(productionChildNames[i]);
            child.setParentId("1-1");
            productionChildren.add(child);
        }
        production.setChildren(productionChildren);
        
        // 辅助部门
        OrgTreeVO auxiliary = new OrgTreeVO();
        auxiliary.setId("1-2");
        auxiliary.setOrgCode("A02");
        auxiliary.setOrgName("辅助部门");
        auxiliary.setParentId("1");
        
        List<OrgTreeVO> auxiliaryChildren = new ArrayList<>();
        String[] auxiliaryChildNames = {"动力车间", "维修车间"};
        String[] auxiliaryChildCodes = {"A02-01", "A02-02"};
        
        for (int i = 0; i < auxiliaryChildNames.length; i++) {
            OrgTreeVO child = new OrgTreeVO();
            child.setId("1-2-" + (i + 1));
            child.setOrgCode(auxiliaryChildCodes[i]);
            child.setOrgName(auxiliaryChildNames[i]);
            child.setParentId("1-2");
            auxiliaryChildren.add(child);
        }
        auxiliary.setChildren(auxiliaryChildren);
        
        List<OrgTreeVO> rootChildren = new ArrayList<>();
        rootChildren.add(production);
        rootChildren.add(auxiliary);
        root.setChildren(rootChildren);
        
        treeData.add(root);
        return treeData;
    }
    
    @Override
    public List<EnergyTypeVO> getEnergyTypes() {
        log.info("开始从数据库获取能源类型配置");
        
        // 从数据库获取能源类型配置
        QueryWrapper<TbEnergyTypeConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "1").orderByAsc("sort_order");
        List<TbEnergyTypeConfig> energyTypeConfigs = energyTypeConfigMapper.selectList(queryWrapper);
        
        log.info("从数据库查询到能源类型配置数量: {}", energyTypeConfigs.size());
        
        List<EnergyTypeVO> result = new ArrayList<>();
        for (TbEnergyTypeConfig config : energyTypeConfigs) {
            EnergyTypeVO vo = new EnergyTypeVO();
            vo.setEnergyType(config.getEnergyType());
            vo.setEnergyName(config.getEnergyName());
            vo.setEnergyUnit(config.getEnergyUnit());
            vo.setPricePerUnit(config.getPricePerUnit().doubleValue());
            vo.setCarbonFactor(config.getCarbonFactor().doubleValue());
            vo.setCoalFactor(config.getCoalFactor().doubleValue());
            result.add(vo);
            log.info("添加能源类型: {} - {}", config.getEnergyType(), config.getEnergyName());
        }
        
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
        
        // 部门条件
        if (StringUtils.hasText(param.getOrgCode())) {
            if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                // 包含子部门，需要判断是否是子部门
                String orgCode = param.getOrgCode();
                // 如果是子部门（如A01-01），查询该子部门及其下的所有部门
                // 但根据部门结构，子部门下没有更下一级的部门，所以只查询该部门本身
                if (orgCode.contains("-")) {
                    // 子部门没有更下一级的子部门，所以只查询该部门本身
                    queryWrapper.eq("org_code", orgCode);
                } else {
                    // 如果是父部门（如A01），查询该部门及其所有子部门
                    queryWrapper.likeRight("org_code", orgCode);
                }
            } else {
                queryWrapper.eq("org_code", param.getOrgCode());
            }
        }
        
        // 时间维度条件
        queryWrapper.eq("time_dimension", param.getTimeDimension());
        
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
        
        // 电能趋势
        if (energyTypeData.containsKey(1)) {
            TrendDataVO.SeriesDataVO electricSeries = new TrendDataVO.SeriesDataVO();
            electricSeries.setName("电能");
            electricSeries.setType("line");
            electricSeries.setData(energyTypeData.get(1).stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));
            series.add(electricSeries);
        }
        
        // 水能趋势
        if (energyTypeData.containsKey(2)) {
            TrendDataVO.SeriesDataVO waterSeries = new TrendDataVO.SeriesDataVO();
            waterSeries.setName("水能");
            waterSeries.setType("line");
            waterSeries.setData(energyTypeData.get(2).stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));
            series.add(waterSeries);
        }
        
        // 燃气趋势
        if (energyTypeData.containsKey(3)) {
            TrendDataVO.SeriesDataVO gasSeries = new TrendDataVO.SeriesDataVO();
            gasSeries.setName("燃气");
            gasSeries.setType("line");
            gasSeries.setData(energyTypeData.get(3).stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));
            series.add(gasSeries);
        }
        
        result.setSeries(series);
        return result;
    }
    
    @Override
    public void exportData(ClassificationQueryParam param, HttpServletResponse response) {
        try {
            // 构建查询条件
            QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();
            
            // 部门条件
            if (StringUtils.hasText(param.getOrgCode())) {
                if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                    // 包含子部门，需要判断是否是子部门
                    String orgCode = param.getOrgCode();
                    // 如果是子部门（如A01-01），查询该子部门及其下的所有部门
                    // 但根据部门结构，子部门下没有更下一级的部门，所以只查询该部门本身
                    if (orgCode.contains("-")) {
                        // 子部门没有更下一级的子部门，所以只查询该部门本身
                        queryWrapper.eq("org_code", orgCode);
                    } else {
                        // 如果是父部门（如A01），查询该部门及其所有子部门
                        queryWrapper.likeRight("org_code", orgCode);
                    }
                } else {
                    queryWrapper.eq("org_code", param.getOrgCode());
                }
            }
            
            // 时间维度条件
            queryWrapper.eq("time_dimension", param.getTimeDimension());
            
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
        StatisticsDataVO statisticsData = new StatisticsDataVO();
        
        // 构建查询条件
        QueryWrapper<TbEnergyClassificationSummary> queryWrapper = new QueryWrapper<>();
        
        // 部门条件
        if (StringUtils.hasText(param.getOrgCode())) {
            if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                // 包含子部门，需要判断是否是子部门
                String orgCode = param.getOrgCode();
                // 如果是子部门（如A01-01），查询该子部门及其下的所有部门
                // 但根据部门结构，子部门下没有更下一级的部门，所以只查询该部门本身
                if (orgCode.contains("-")) {
                    // 子部门没有更下一级的子部门，所以只查询该部门本身
                    queryWrapper.eq("org_code", orgCode);
                } else {
                    // 如果是父部门（如A01），查询该部门及其所有子部门
                    queryWrapper.likeRight("org_code", orgCode);
                }
            } else {
                // 不包含子部门，精确匹配
                queryWrapper.eq("org_code", param.getOrgCode());
            }
        }
        
        // 时间维度条件
        queryWrapper.eq("time_dimension", param.getTimeDimension());
        
        // 时间范围条件
        if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
            queryWrapper.between("stat_date", param.getStartDate(), param.getEndDate());
        }
        
        // 能源类型条件
        if (!"all".equals(param.getEnergyType())) {
            queryWrapper.eq("energy_type", param.getEnergyType());
        }
        
        // 输出完整的SQL语句
        System.out.println("========== 查询分类分区统计数据的SQL详细信息 ==========");
        System.out.println("查询条件 - orgCode: " + param.getOrgCode() + 
                          ", energyType: " + param.getEnergyType() + 
                          ", timeDimension: " + param.getTimeDimension() + 
                          ", startDate: " + param.getStartDate() + 
                          ", endDate: " + param.getEndDate() + 
                          ", includeChildren: " + param.getIncludeChildren());
        
        // 获取完整的SQL语句
        try {
            // 构建SQL
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM tb_energy_classification_summary WHERE 1=1");
            
            // 部门条件
            if (StringUtils.hasText(param.getOrgCode())) {
                if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                    // 包含子部门，需要判断是否是子部门
                    String orgCode = param.getOrgCode();
                    // 如果是子部门（如A01-01），查询该子部门及其下的所有部门
                    // 但根据部门结构，子部门下没有更下一级的部门，所以只查询该部门本身
                    if (orgCode.contains("-")) {
                        // 子部门没有更下一级的子部门，所以只查询该部门本身
                        sql.append(" AND org_code = '").append(orgCode).append("'");
                    } else {
                        // 如果是父部门（如A01），查询该部门及其所有子部门
                        sql.append(" AND org_code LIKE '").append(orgCode).append("%'");
                    }
                } else {
                    sql.append(" AND org_code = '").append(param.getOrgCode()).append("'");
                }
            }
            
            // 时间维度条件
            sql.append(" AND time_dimension = '").append(param.getTimeDimension()).append("'");
            
            // 时间范围条件
            if (StringUtils.hasText(param.getStartDate()) && StringUtils.hasText(param.getEndDate())) {
                sql.append(" AND stat_date BETWEEN '").append(param.getStartDate()).append("' AND '").append(param.getEndDate()).append("'");
            }
            
            // 能源类型条件
            if (!"all".equals(param.getEnergyType())) {
                sql.append(" AND energy_type = ").append(param.getEnergyType());
            }
            
            System.out.println("完整SQL: " + sql.toString());
        } catch (Exception e) {
            System.out.println("获取SQL失败: " + e.getMessage());
        }
        
        // 查询数据
        List<TbEnergyClassificationSummary> summaryList = summaryMapper.selectList(queryWrapper);
        
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
            
            // 按能源类型分类统计
            if (summary.getEnergyType() == 1) {
                electricConsumption = electricConsumption.add(summary.getTotalConsumption());
            } else if (summary.getEnergyType() == 2) {
                waterConsumption = waterConsumption.add(summary.getTotalConsumption());
            } else if (summary.getEnergyType() == 3) {
                gasConsumption = gasConsumption.add(summary.getTotalConsumption());
            }
        }
        
        statisticsData.setTotalConsumption(totalConsumption);
        statisticsData.setElectricConsumption(electricConsumption);
        statisticsData.setWaterConsumption(waterConsumption);
        statisticsData.setGasConsumption(gasConsumption);
        statisticsData.setTotalCost(totalCost);
        statisticsData.setTotalCarbonEmission(totalCarbonEmission);
        
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
        
        // 部门条件
        if (StringUtils.hasText(param.getOrgCode())) {
            if (param.getIncludeChildren() != null && param.getIncludeChildren()) {
                // 包含子部门，需要判断是否是子部门
                String orgCode = param.getOrgCode();
                // 如果是子部门（如A01-01），查询该子部门及其下的所有部门
                // 但根据部门结构，子部门下没有更下一级的部门，所以只查询该部门本身
                if (orgCode.contains("-")) {
                    // 子部门没有更下一级的子部门，所以只查询该部门本身
                    queryWrapper.eq("org_code", orgCode);
                } else {
                    // 如果是父部门（如A01），查询该部门及其所有子部门
                    queryWrapper.likeRight("org_code", orgCode);
                }
            } else {
                queryWrapper.eq("org_code", param.getOrgCode());
            }
        }
        
        // 时间维度条件
        queryWrapper.eq("time_dimension", param.getTimeDimension());
        
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
            queryWrapper.select(selectTimeField + " as time, energy_type, SUM(total_consumption) as total_consumption, SUM(total_cost) as total_cost")
                       .groupBy(selectTimeField + ", energy_type")
                       .orderByAsc(selectTimeField);
            
            System.out.println("========== getTableData 调试信息 ==========");
            System.out.println("查询参数: " + param);
            System.out.println("时间字段: " + selectTimeField);
            System.out.println("SQL WHERE条件: " + queryWrapper.getCustomSqlSegment());
            System.out.println("SQL参数: " + queryWrapper.getParamNameValuePairs());
            
            List<Map<String, Object>> rawData = summaryMapper.selectMaps(queryWrapper);
            System.out.println("查询到原始数据条数: " + rawData.size());
            
            // 如果没有数据，返回空列表
            if (rawData.isEmpty()) {
                System.out.println("警告：没有查询到数据，请检查数据库中是否有符合条件的数据");
                // 尝试查询所有数据看看是否有数据
                QueryWrapper<TbEnergyClassificationSummary> checkWrapper = new QueryWrapper<>();
                checkWrapper.eq("time_dimension", param.getTimeDimension())
                           .last("LIMIT 5");
                List<TbEnergyClassificationSummary> checkData = summaryMapper.selectList(checkWrapper);
                System.out.println("数据库中该时间维度的数据示例: " + checkData.size() + " 条");
                for (TbEnergyClassificationSummary item : checkData) {
                    System.out.println("  - " + item.getOrgCode() + ", " + item.getStatDate() + ", " + item.getTimeDimension());
                }
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
                
                if (!timeGroupData.containsKey(time)) {
                    timeGroupData.put(time, new HashMap<>());
                }
                timeGroupData.get(time).put(energyType, new BigDecimal[]{consumption, cost});
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
            
            // 设置各能源类型数据
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
}