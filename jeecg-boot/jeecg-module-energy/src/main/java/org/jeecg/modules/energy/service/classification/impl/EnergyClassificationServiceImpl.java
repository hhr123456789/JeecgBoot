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
                queryWrapper.likeRight("org_code", param.getOrgCode());
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
        
        // 按时间和能源类型分组查询
        queryWrapper.select("stat_month, energy_type, SUM(total_consumption) as total_consumption")
                   .groupBy("stat_month, energy_type")
                   .orderByAsc("stat_month");
        
        List<Map<String, Object>> trendData = summaryMapper.selectMaps(queryWrapper);
        
        // 构建X轴数据（月份）
        Set<String> months = new TreeSet<>();
        for (Map<String, Object> data : trendData) {
            months.add((String) data.get("stat_month"));
        }
        List<String> xAxisData = new ArrayList<>(months);
        xAxis.setData(xAxisData);
        result.setXAxis(xAxis);
        
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
            // 获取汇总数据
            ClassificationSummaryVO summaryData = getSummaryData(param);
            
            // 构建导出数据 - 使用模拟数据
            List<TbEnergyClassificationSummary> exportList = new ArrayList<>();
            
            // 创建模拟导出数据
            TbEnergyClassificationSummary summary1 = new TbEnergyClassificationSummary();
            summary1.setOrgName("生产部门");
            summary1.setEnergyTypeName("电能");
            summary1.setTotalConsumption(new BigDecimal("856432.12"));
            summary1.setTotalCost(new BigDecimal("685145.70"));
            summary1.setCarbonEmission(new BigDecimal("853.86"));
            exportList.add(summary1);
            
            TbEnergyClassificationSummary summary2 = new TbEnergyClassificationSummary();
            summary2.setOrgName("生产部门");
            summary2.setEnergyTypeName("水能");
            summary2.setTotalConsumption(new BigDecimal("234567.89"));
            summary2.setTotalCost(new BigDecimal("140740.73"));
            summary2.setCarbonEmission(new BigDecimal("0.00"));
            exportList.add(summary2);
            
            // 设置响应头
            String fileName = "企业分类分区统计_" + DateUtil.format(new Date(), "yyyyMMdd_HHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            
            // 导出Excel - 使用简单方式
            ExportParams exportParams = new ExportParams("企业分类分区统计", "统计数据");
            exportParams.setType(ExcelType.XSSF);
            
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, TbEnergyClassificationSummary.class, exportList);
            workbook.write(response.getOutputStream());
            workbook.close();
            
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
                // 包含子部门，使用模糊查询
                queryWrapper.likeRight("org_code", param.getOrgCode());
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
            // 全部能源类型
            PieChartDataVO.DataItemVO electric = new PieChartDataVO.DataItemVO();
            electric.setValue(856432.12);
            electric.setName("电能");
            electric.setPercentage(68.15);
            data.add(electric);
            
            PieChartDataVO.DataItemVO water = new PieChartDataVO.DataItemVO();
            water.setValue(234567.89);
            water.setName("水能");
            water.setPercentage(18.66);
            data.add(water);
            
            PieChartDataVO.DataItemVO gas = new PieChartDataVO.DataItemVO();
            gas.setValue(165789.44);
            gas.setName("燃气");
            gas.setPercentage(13.19);
            data.add(gas);
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
                queryWrapper.likeRight("org_code", param.getOrgCode());
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
        String timeField = "day".equals(param.getTimeDimension()) ? "stat_date" : "stat_month";
        queryWrapper.select(timeField + " as time, energy_type, SUM(total_consumption) as total_consumption, SUM(total_cost) as total_cost")
                   .groupBy(timeField + ", energy_type")
                   .orderByAsc(timeField);
        
        List<Map<String, Object>> rawData = summaryMapper.selectMaps(queryWrapper);
        
        // 按时间分组整理数据
        Map<String, Map<Integer, BigDecimal[]>> timeGroupData = new HashMap<>();
        for (Map<String, Object> data : rawData) {
            String time = (String) data.get("time");
            Integer energyType = (Integer) data.get("energy_type");
            BigDecimal consumption = (BigDecimal) data.get("total_consumption");
            BigDecimal cost = (BigDecimal) data.get("total_cost");
            
            if (!timeGroupData.containsKey(time)) {
                timeGroupData.put(time, new HashMap<>());
            }
            timeGroupData.get(time).put(energyType, new BigDecimal[]{consumption, cost});
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
}