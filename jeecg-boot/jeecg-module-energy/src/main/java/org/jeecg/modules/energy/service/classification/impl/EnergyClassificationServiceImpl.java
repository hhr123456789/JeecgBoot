package org.jeecg.modules.energy.service.classification.impl;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.modules.energy.entity.classification.TbEnergyClassificationSummary;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.jeecg.modules.energy.vo.classification.*;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.stereotype.Service;

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
    
    // Removed unused autowired fields
    
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
        // Mock data for testing when database is not available
        List<EnergyTypeVO> mockEnergyTypes = new ArrayList<>();
        
        EnergyTypeVO electric = new EnergyTypeVO();
        electric.setEnergyType(1);
        electric.setEnergyName("电能");
        electric.setEnergyUnit("kWh");
        electric.setPricePerUnit(0.85);
        electric.setCarbonFactor(0.785);
        electric.setCoalFactor(0.1229);
        mockEnergyTypes.add(electric);
        
        EnergyTypeVO water = new EnergyTypeVO();
        water.setEnergyType(2);
        water.setEnergyName("水能");
        water.setEnergyUnit("m³");
        water.setPricePerUnit(3.50);
        water.setCarbonFactor(0.0);
        water.setCoalFactor(0.0);
        mockEnergyTypes.add(water);
        
        EnergyTypeVO gas = new EnergyTypeVO();
        gas.setEnergyType(3);
        gas.setEnergyName("燃气");
        gas.setEnergyUnit("m³");
        gas.setPricePerUnit(2.80);
        gas.setCarbonFactor(1.96);
        gas.setCoalFactor(1.33);
        mockEnergyTypes.add(gas);
        
        return mockEnergyTypes;
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
        
        // 模拟X轴数据
        List<String> xAxisData = Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月");
        xAxis.setData(xAxisData);
        result.setXAxis(xAxis);
        
        // 构建系列数据
        List<TrendDataVO.SeriesDataVO> series = new ArrayList<>();
        
        // 电能趋势
        TrendDataVO.SeriesDataVO electricSeries = new TrendDataVO.SeriesDataVO();
        electricSeries.setName("电能");
        electricSeries.setType("line");
        electricSeries.setData(Arrays.asList(150000, 160000, 145000, 155000, 165000, 170000));
        series.add(electricSeries);
        
        // 水能趋势
        TrendDataVO.SeriesDataVO waterSeries = new TrendDataVO.SeriesDataVO();
        waterSeries.setName("水能");
        waterSeries.setType("line");
        waterSeries.setData(Arrays.asList(35000, 38000, 36000, 40000, 42000, 43000));
        series.add(waterSeries);
        
        // 燃气趋势
        TrendDataVO.SeriesDataVO gasSeries = new TrendDataVO.SeriesDataVO();
        gasSeries.setName("燃气");
        gasSeries.setType("line");
        gasSeries.setData(Arrays.asList(25000, 28000, 26000, 29000, 30000, 31000));
        series.add(gasSeries);
        
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
        
        // 模拟统计数据，实际应该从数据库查询
        if ("all".equals(param.getEnergyType())) {
            statisticsData.setTotalConsumption(new BigDecimal("1256789.45"));
            statisticsData.setElectricConsumption(new BigDecimal("856432.12"));
            statisticsData.setWaterConsumption(new BigDecimal("234567.89"));
            statisticsData.setGasConsumption(new BigDecimal("165789.44"));
            statisticsData.setTotalCost(new BigDecimal("908781.15"));
            statisticsData.setTotalCarbonEmission(new BigDecimal("1234.56"));
        } else if ("1".equals(param.getEnergyType())) {
            statisticsData.setTotalConsumption(new BigDecimal("856432.12"));
            statisticsData.setElectricConsumption(new BigDecimal("856432.12"));
            statisticsData.setWaterConsumption(BigDecimal.ZERO);
            statisticsData.setGasConsumption(BigDecimal.ZERO);
            statisticsData.setTotalCost(new BigDecimal("685145.70"));
            statisticsData.setTotalCarbonEmission(new BigDecimal("853.86"));
        } else if ("2".equals(param.getEnergyType())) {
            statisticsData.setTotalConsumption(new BigDecimal("234567.89"));
            statisticsData.setElectricConsumption(BigDecimal.ZERO);
            statisticsData.setWaterConsumption(new BigDecimal("234567.89"));
            statisticsData.setGasConsumption(BigDecimal.ZERO);
            statisticsData.setTotalCost(new BigDecimal("140740.73"));
            statisticsData.setTotalCarbonEmission(BigDecimal.ZERO);
        } else if ("3".equals(param.getEnergyType())) {
            statisticsData.setTotalConsumption(new BigDecimal("165789.44"));
            statisticsData.setElectricConsumption(BigDecimal.ZERO);
            statisticsData.setWaterConsumption(BigDecimal.ZERO);
            statisticsData.setGasConsumption(new BigDecimal("165789.44"));
            statisticsData.setTotalCost(new BigDecimal("414473.60"));
            statisticsData.setTotalCarbonEmission(new BigDecimal("358.93"));
        }
        
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
        
        // 模拟表格数据
        if ("all".equals(param.getEnergyType())) {
            TableDataVO data1 = new TableDataVO();
            data1.setTime("2024-01");
            data1.setElectric(new BigDecimal("856432.12"));
            data1.setWater(new BigDecimal("234567.89"));
            data1.setGas(new BigDecimal("165789.44"));
            data1.setElectricCost(new BigDecimal("685145.70"));
            data1.setWaterCost(new BigDecimal("140740.73"));
            data1.setGasCost(new BigDecimal("82894.72"));
            data1.setTotalCost(new BigDecimal("908781.15"));
            tableData.add(data1);
            
            TableDataVO data2 = new TableDataVO();
            data2.setTime("2024-02");
            data2.setElectric(new BigDecimal("845678.34"));
            data2.setWater(new BigDecimal("225678.90"));
            data2.setGas(new BigDecimal("158976.23"));
            data2.setElectricCost(new BigDecimal("676542.67"));
            data2.setWaterCost(new BigDecimal("135407.34"));
            data2.setGasCost(new BigDecimal("79488.12"));
            data2.setTotalCost(new BigDecimal("891438.13"));
            tableData.add(data2);
        } else {
            // 单一能源类型
            TableDataVO data1 = new TableDataVO();
            data1.setTime("2024-01-01");
            data1.setElectric(statisticsData.getTotalConsumption());
            data1.setElectricCost(statisticsData.getTotalCost());
            tableData.add(data1);
            
            TableDataVO data2 = new TableDataVO();
            data2.setTime("2024-01-02");
            data2.setElectric(statisticsData.getTotalConsumption().multiply(new BigDecimal("0.95")));
            data2.setElectricCost(statisticsData.getTotalCost().multiply(new BigDecimal("0.95")));
            tableData.add(data2);
        }
        
        return tableData;
    }
}