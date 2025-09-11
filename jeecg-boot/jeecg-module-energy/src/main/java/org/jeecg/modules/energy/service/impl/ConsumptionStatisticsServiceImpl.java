package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.modules.energy.mapper.TrendAnalysisMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IConsumptionStatisticsService;
import org.jeecg.modules.energy.util.ExcelExportUtil;
import org.jeecg.modules.energy.vo.consumption.ConsumptionRequest;
import org.jeecg.modules.energy.vo.consumption.ConsumptionSeparatedResult;
import org.jeecg.modules.energy.vo.consumption.ConsumptionUnifiedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConsumptionStatisticsServiceImpl implements IConsumptionStatisticsService {

    @Autowired
    private TrendAnalysisMapper mapper;
    @Autowired
    private TbModuleMapper tbModuleMapper;

    @Override
    public Object getConsumptionStatistics(ConsumptionRequest request) {
        validate(request);
        String table = resolveTable(request.getTimeType());
        String labelExpr = resolveLabelExpr(request.getTimeType());
        String startDt = normalizeStartDt(request.getStartDate(), request.getTimeType());
        String endDt = normalizeEndDt(request.getEndDate(), request.getTimeType());

        // 查询：按 label+module 聚合的能耗
        List<Map<String, Object>> rows = mapper.selectEnergyTrendByModule(
                table, labelExpr, request.getModuleIds(), startDt, endDt);

        // moduleId -> (moduleName, energyType)
        Map<String, Map<String,Object>> moduleInfo = tbModuleMapper.selectEnergyTypeByModuleIds(request.getModuleIds())
                .stream().collect(Collectors.toMap(m -> Objects.toString(m.get("moduleId"), ""), m -> m, (a,b)->a));
        Map<Integer, EnergyUnit> unitMap = loadEnergyUnitMap();

        // 按设备归组数据点
        Map<String, List<Map<String,Object>>> byModule = rows.stream()
                .collect(Collectors.groupingBy(r -> Objects.toString(r.get("moduleId"), "")));

        if (Objects.equals(request.getDisplayMode(), 2)) {
            // 分开显示：每个设备一张图
            ConsumptionSeparatedResult res = new ConsumptionSeparatedResult();
            res.setTimeRange(buildTimeRange(request));
            List<ConsumptionSeparatedResult.Chart> charts = new ArrayList<>();
            for (String moduleId : request.getModuleIds()) {
                List<Map<String, Object>> points = byModule.getOrDefault(moduleId, Collections.emptyList());
                Map<String,Object> info = moduleInfo.get(moduleId);
                String moduleName = info == null ? moduleId : Objects.toString(info.get("moduleName"), moduleId);
                Integer energyType = info == null ? null : (info.get("energyType") == null ? null : ((Number) info.get("energyType")).intValue());
                
                ConsumptionSeparatedResult.Chart chart = new ConsumptionSeparatedResult.Chart();
                chart.setModuleId(moduleId);
                chart.setModuleName(moduleName);
                chart.setTitle(moduleName);

                List<ConsumptionSeparatedResult.Series> series = new ArrayList<>();
                ConsumptionSeparatedResult.Series s = new ConsumptionSeparatedResult.Series();
                s.setName("能耗");
                s.setMetric("energy");
                s.setUnit(getEnergyUnit(energyType, unitMap));
                s.setModuleId(moduleId);
                s.setData(convertPointsSeparated(points));
                series.add(s);
                
                chart.setSeries(series);
                charts.add(chart);
            }
            res.setCharts(charts);
            return res;
        } else {
            // 统一显示：每个设备一条曲线
            ConsumptionUnifiedResult res = new ConsumptionUnifiedResult();
            res.setTimeRange(buildTimeRange(request));
            List<ConsumptionUnifiedResult.Series> series = new ArrayList<>();
            for (String moduleId : request.getModuleIds()) {
                List<Map<String, Object>> points = byModule.getOrDefault(moduleId, Collections.emptyList());
                Map<String,Object> info = moduleInfo.get(moduleId);
                String moduleName = info == null ? moduleId : Objects.toString(info.get("moduleName"), moduleId);
                Integer energyType = info == null ? null : (info.get("energyType") == null ? null : ((Number) info.get("energyType")).intValue());
                
                ConsumptionUnifiedResult.Series s = new ConsumptionUnifiedResult.Series();
                s.setModuleId(moduleId);
                s.setModuleName(moduleName);
                s.setMetric("energy");
                s.setUnit(getEnergyUnit(energyType, unitMap));
                s.setName(moduleName + "-能耗");
                s.setData(convertPointsUnified(points));
                series.add(s);
            }
            res.setSeries(series);
            return res;
        }
    }

    @Override
    public void exportConsumptionStatistics(ConsumptionRequest request, HttpServletResponse response) {
        Object obj = getConsumptionStatistics(request);
        String fileName = ExcelExportUtil.generateFileName("设备能源统计");
        
        if (obj instanceof ConsumptionUnifiedResult) {
            exportUnifiedConsumption((ConsumptionUnifiedResult) obj, response, fileName);
        } else if (obj instanceof ConsumptionSeparatedResult) {
            exportSeparatedConsumption((ConsumptionSeparatedResult) obj, response, fileName);
        }
    }

    /**
     * 导出统一显示模式的Excel
     */
    private void exportUnifiedConsumption(ConsumptionUnifiedResult result, HttpServletResponse response, String fileName) {
        // 构建表头
        List<String> headers = new ArrayList<>();
        headers.add("时间");
        for (ConsumptionUnifiedResult.Series series : result.getSeries()) {
            headers.add(series.getName() + "(" + series.getUnit() + ")");
        }
        
        // 构建数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // 获取所有时间点（以第一个系列为准）
        List<String> timeLabels = result.getSeries().isEmpty() ? Collections.emptyList() :
                result.getSeries().get(0).getData().stream()
                        .map(ConsumptionUnifiedResult.Point::getX)
                        .collect(Collectors.toList());
        
        // 按时间点组织数据
        for (int timeIndex = 0; timeIndex < timeLabels.size(); timeIndex++) {
            Map<String, Object> rowData = new HashMap<>();
            
            // 时间列
            rowData.put("col_0", timeLabels.get(timeIndex));
            
            // 数据列
            for (int seriesIndex = 0; seriesIndex < result.getSeries().size(); seriesIndex++) {
                ConsumptionUnifiedResult.Series series = result.getSeries().get(seriesIndex);
                String colKey = "col_" + (seriesIndex + 1);
                
                if (timeIndex < series.getData().size()) {
                    Double value = series.getData().get(timeIndex).getY();
                    rowData.put(colKey, value != null ? value : "-");
                } else {
                    rowData.put(colKey, "-");
                }
            }
            
            dataList.add(rowData);
        }
        
        // 使用ExcelExportUtil导出
        ExcelExportUtil.exportRealTimeData(response, fileName, headers, dataList);
    }
    
    /**
     * 导出分开显示模式的Excel
     */
    private void exportSeparatedConsumption(ConsumptionSeparatedResult result, HttpServletResponse response, String fileName) {
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        for (int chartIndex = 0; chartIndex < result.getCharts().size(); chartIndex++) {
            ConsumptionSeparatedResult.Chart chart = result.getCharts().get(chartIndex);
            
            // 如果是第一个图表，设置表头
            if (chartIndex == 0) {
                headers.add("时间");
                headers.add("能耗(" + (chart.getSeries().isEmpty() ? "" : chart.getSeries().get(0).getUnit()) + ")");
            }
            
            // 添加设备标题行
            Map<String, Object> titleRow = new HashMap<>();
            titleRow.put("col_0", "设备: " + chart.getTitle());
            titleRow.put("col_1", "");
            dataList.add(titleRow);
            
            // 获取时间点
            List<String> timeLabels = chart.getSeries().isEmpty() ? Collections.emptyList() :
                    chart.getSeries().get(0).getData().stream()
                            .map(ConsumptionSeparatedResult.Point::getX)
                            .collect(Collectors.toList());
            
            // 添加数据行
            for (int timeIndex = 0; timeIndex < timeLabels.size(); timeIndex++) {
                Map<String, Object> rowData = new HashMap<>();
                
                // 时间列
                rowData.put("col_0", timeLabels.get(timeIndex));
                
                // 能耗列
                if (!chart.getSeries().isEmpty()) {
                    ConsumptionSeparatedResult.Series series = chart.getSeries().get(0);
                    if (timeIndex < series.getData().size()) {
                        Double value = series.getData().get(timeIndex).getY();
                        rowData.put("col_1", value != null ? value : "-");
                    } else {
                        rowData.put("col_1", "-");
                    }
                } else {
                    rowData.put("col_1", "-");
                }
                
                dataList.add(rowData);
            }
            
            // 添加空行分隔（除了最后一个图表）
            if (chartIndex < result.getCharts().size() - 1) {
                Map<String, Object> emptyRow = new HashMap<>();
                emptyRow.put("col_0", "");
                emptyRow.put("col_1", "");
                dataList.add(emptyRow);
            }
        }
        
        // 使用ExcelExportUtil导出
        ExcelExportUtil.exportRealTimeData(response, fileName, headers, dataList);
    }

    // ---------------- private helpers ----------------

    private void validate(ConsumptionRequest req) {
        if (req == null || CollectionUtils.isEmpty(req.getModuleIds())) {
            throw new IllegalArgumentException("moduleIds不能为空");
        }
        if (!Arrays.asList("day", "month", "year").contains(req.getTimeType())) {
            throw new IllegalArgumentException("timeType必须为day|month|year");
        }
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new IllegalArgumentException("startDate/endDate不能为空");
        }
        // 只做字符串比较会有误，这里按timeType拼接具体时间后比较
        String s = normalizeStartDt(req.getStartDate(), req.getTimeType());
        String e = normalizeEndDt(req.getEndDate(), req.getTimeType());
        if (s.compareTo(e) > 0) {
            throw new IllegalArgumentException("开始时间不能大于结束时间");
        }
    }

    private String resolveTable(String timeType) {
        switch (timeType) {
            case "day": return "tb_ep_equ_energy_daycount";
            case "month": return "tb_ep_equ_energy_monthcount";
            case "year": return "tb_ep_equ_energy_yearcount";
        }
        throw new IllegalArgumentException("非法timeType");
    }

    private String resolveLabelExpr(String timeType) {
        switch (timeType) {
            case "day": return "DATE_FORMAT(dt, '%Y-%m-%d')";
            case "month": return "DATE_FORMAT(dt, '%Y-%m')";
            case "year": return "DATE_FORMAT(dt, '%Y')";
        }
        throw new IllegalArgumentException("非法timeType");
    }

    private String normalizeStartDt(String start, String timeType) {
        if ("day".equals(timeType)) return start + " 00:00:00";
        if ("month".equals(timeType)) return start + "-01 00:00:00";
        if ("year".equals(timeType)) return start + "-01-01 00:00:00";
        return start;
    }

    private String normalizeEndDt(String end, String timeType) {
        if ("day".equals(timeType)) return end + " 23:59:59";
        if ("month".equals(timeType)) return end + "-31 23:59:59";
        if ("year".equals(timeType)) return end + "-12-31 23:59:59";
        return end;
    }

    private String buildTimeRange(ConsumptionRequest req) {
        return String.format("%s ~ %s (%s)", req.getStartDate(), req.getEndDate(), req.getTimeType());
    }

    private List<ConsumptionUnifiedResult.Point> convertPointsUnified(List<Map<String, Object>> points) {
        List<ConsumptionUnifiedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : points) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            ConsumptionUnifiedResult.Point p = new ConsumptionUnifiedResult.Point();
            p.setX(label);
            p.setY(round(energy, 2));
            list.add(p);
        }
        return list;
    }

    private List<ConsumptionSeparatedResult.Point> convertPointsSeparated(List<Map<String, Object>> points) {
        List<ConsumptionSeparatedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : points) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            ConsumptionSeparatedResult.Point p = new ConsumptionSeparatedResult.Point();
            p.setX(label);
            p.setY(round(energy, 2));
            list.add(p);
        }
        return list;
    }

    private String getEnergyUnit(Integer energyType, Map<Integer, EnergyUnit> unitMap) {
        EnergyUnit unit = unitMap.getOrDefault(energyType == null ? 1 : energyType, new EnergyUnit("kWh"));
        return unit.unit;
    }

    private Map<Integer, EnergyUnit> loadEnergyUnitMap() {
        Map<Integer, EnergyUnit> map = new HashMap<>();
        for (Map<String, Object> r : mapper.selectEnergyRatio()) {
            Integer type = r.get("energyType") == null ? null : ((Number) r.get("energyType")).intValue();
            String unit = Objects.toString(r.get("unit"), "kWh");
            if (type != null) map.put(type, new EnergyUnit(unit));
        }
        return map;
    }

    private Double getDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Double) return (Double) o;
        if (o instanceof BigDecimal) return ((BigDecimal) o).doubleValue();
        if (o instanceof Number) return ((Number) o).doubleValue();
        return 0.0;
    }

    private double round(double v, int scale) {
        return new BigDecimal(v).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static class EnergyUnit {
        String unit;
        EnergyUnit(String unit) { this.unit = unit; }
    }
}