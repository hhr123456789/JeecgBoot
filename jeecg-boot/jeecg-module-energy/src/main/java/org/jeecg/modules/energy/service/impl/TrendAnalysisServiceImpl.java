package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.modules.energy.mapper.TrendAnalysisMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.ITrendAnalysisService;
import org.jeecg.modules.energy.util.ExcelExportUtil;
import org.jeecg.modules.energy.vo.trend.TrendRequest;
import org.jeecg.modules.energy.vo.trend.TrendSeparatedResult;
import org.jeecg.modules.energy.vo.trend.TrendUnifiedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrendAnalysisServiceImpl implements ITrendAnalysisService {

    @Autowired
    private TrendAnalysisMapper mapper;
    @Autowired
    private TbModuleMapper tbModuleMapper;

    @Override
    public Object getTrend(TrendRequest request) {
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
        Map<Integer, Ratio> ratioMap = loadRatioMap();
        Set<String> metrics = resolveMetrics(request.getMetrics());

        // 按设备归组数据点
        Map<String, List<Map<String,Object>>> byModule = rows.stream()
                .collect(Collectors.groupingBy(r -> Objects.toString(r.get("moduleId"), "")));

        if (Objects.equals(request.getDisplayMode(), 2)) {
            // 分开显示：每个设备一张图；图内“指标”为series
            TrendSeparatedResult res = new TrendSeparatedResult();
            res.setTimeRange(buildTimeRange(request));
            List<TrendSeparatedResult.Chart> charts = new ArrayList<>();
            for (String moduleId : request.getModuleIds()) {
                List<Map<String, Object>> points = byModule.getOrDefault(moduleId, Collections.emptyList());
                Map<String,Object> info = moduleInfo.get(moduleId);
                String moduleName = info == null ? moduleId : Objects.toString(info.get("moduleName"), moduleId);
                Integer energyType = info == null ? null : (info.get("energyType") == null ? null : ((Number) info.get("energyType")).intValue());
                TrendSeparatedResult.Chart chart = new TrendSeparatedResult.Chart();
                chart.setModuleId(moduleId);
                chart.setModuleName(moduleName);
                chart.setTitle(moduleName);

                List<TrendSeparatedResult.Series> series = new ArrayList<>();
                for (String metric : metrics) {
                    TrendSeparatedResult.Series s = new TrendSeparatedResult.Series();
                    s.setName(metricTitle(metric));
                    s.setMetric(metric);
                    s.setUnit(metricUnit(metric, null));
                    s.setModuleId(moduleId);
                    s.setData(convertPoints(points, metric, energyType, ratioMap));
                    series.add(s);
                }
                chart.setSeries(series);
                charts.add(chart);
            }
            res.setCharts(charts);
            return res;
        } else {
            // 统一显示：series=设备×指标
            TrendUnifiedResult res = new TrendUnifiedResult();
            res.setTimeRange(buildTimeRange(request));
            List<TrendUnifiedResult.Series> series = new ArrayList<>();
            for (String moduleId : request.getModuleIds()) {
                List<Map<String, Object>> points = byModule.getOrDefault(moduleId, Collections.emptyList());
                Map<String,Object> info = moduleInfo.get(moduleId);
                String moduleName = info == null ? moduleId : Objects.toString(info.get("moduleName"), moduleId);
                Integer energyType = info == null ? null : (info.get("energyType") == null ? null : ((Number) info.get("energyType")).intValue());
                for (String metric : metrics) {
                    TrendUnifiedResult.Series s = new TrendUnifiedResult.Series();
                    s.setModuleId(moduleId);
                    s.setModuleName(moduleName);
                    s.setMetric(metric);
                    s.setUnit(metricUnit(metric, null));
                    s.setName(moduleName + "-" + metricTitle(metric)); // 命名固定为 设备名-指标名
                    s.setData(convertPointsUnified(points, metric, energyType, ratioMap));
                    series.add(s);
                }
            }
            res.setSeries(series);
            return res;
        }
    }

    @Override
    public void exportTrend(TrendRequest request, HttpServletResponse response) {
        Object obj = getTrend(request);
        String fileName = ExcelExportUtil.generateFileName("趋势分析导出");
        
        if (obj instanceof TrendUnifiedResult) {
            exportUnifiedTrend((TrendUnifiedResult) obj, response, fileName);
        } else if (obj instanceof TrendSeparatedResult) {
            exportSeparatedTrend((TrendSeparatedResult) obj, response, fileName);
        }
    }
    
    /**
     * 导出统一显示模式的Excel
     */
    private void exportUnifiedTrend(TrendUnifiedResult result, HttpServletResponse response, String fileName) {
        // 构建表头
        List<String> headers = new ArrayList<>();
        headers.add("时间");
        for (TrendUnifiedResult.Series series : result.getSeries()) {
            headers.add(series.getName() + "(" + series.getUnit() + ")");
        }
        
        // 构建数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // 获取所有时间点（以第一个系列为准）
        List<String> timeLabels = result.getSeries().isEmpty() ? Collections.emptyList() :
                result.getSeries().get(0).getData().stream()
                        .map(TrendUnifiedResult.Point::getX)
                        .collect(Collectors.toList());
        
        // 按时间点组织数据
        for (int timeIndex = 0; timeIndex < timeLabels.size(); timeIndex++) {
            Map<String, Object> rowData = new HashMap<>();
            
            // 时间列
            rowData.put("col_0", timeLabels.get(timeIndex));
            
            // 数据列
            for (int seriesIndex = 0; seriesIndex < result.getSeries().size(); seriesIndex++) {
                TrendUnifiedResult.Series series = result.getSeries().get(seriesIndex);
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
     * 导出分开显示模式的Excel - 创建多个工作表
     */
    private void exportSeparatedTrend(TrendSeparatedResult result, HttpServletResponse response, String fileName) {
        // 对于分开显示，我们将所有设备的数据合并到一个Excel文件中，但用空行分隔
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        for (int chartIndex = 0; chartIndex < result.getCharts().size(); chartIndex++) {
            TrendSeparatedResult.Chart chart = result.getCharts().get(chartIndex);
            
            // 如果是第一个图表，设置表头
            if (chartIndex == 0) {
                headers.add("时间");
                for (TrendSeparatedResult.Series series : chart.getSeries()) {
                    headers.add(series.getName() + "(" + series.getUnit() + ")");
                }
            }
            
            // 添加设备标题行
            Map<String, Object> titleRow = new HashMap<>();
            titleRow.put("col_0", "设备: " + chart.getTitle());
            for (int i = 1; i < headers.size(); i++) {
                titleRow.put("col_" + i, "");
            }
            dataList.add(titleRow);
            
            // 获取时间点
            List<String> timeLabels = chart.getSeries().isEmpty() ? Collections.emptyList() :
                    chart.getSeries().get(0).getData().stream()
                            .map(TrendSeparatedResult.Point::getX)
                            .collect(Collectors.toList());
            
            // 添加数据行
            for (int timeIndex = 0; timeIndex < timeLabels.size(); timeIndex++) {
                Map<String, Object> rowData = new HashMap<>();
                
                // 时间列
                rowData.put("col_0", timeLabels.get(timeIndex));
                
                // 数据列
                for (int seriesIndex = 0; seriesIndex < chart.getSeries().size(); seriesIndex++) {
                    TrendSeparatedResult.Series series = chart.getSeries().get(seriesIndex);
                    String colKey = "col_" + (seriesIndex + 1);
                    
                    if (timeIndex < series.getData().size()) {
                        Double value = series.getData().get(timeIndex).getY();
                        rowData.put(colKey, value != null ? value : "-");
                    } else {
                        rowData.put(colKey, "-");
                    }
                }
                
                // 填充剩余列
                for (int i = chart.getSeries().size() + 1; i < headers.size(); i++) {
                    rowData.put("col_" + i, "");
                }
                
                dataList.add(rowData);
            }
            
            // 添加空行分隔（除了最后一个图表）
            if (chartIndex < result.getCharts().size() - 1) {
                Map<String, Object> emptyRow = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    emptyRow.put("col_" + i, "");
                }
                dataList.add(emptyRow);
            }
        }
        
        // 使用ExcelExportUtil导出
        ExcelExportUtil.exportRealTimeData(response, fileName, headers, dataList);
    }

    // ---------------- private helpers ----------------

    private void validate(TrendRequest req) {
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

    private String buildTimeRange(TrendRequest req) {
        return String.format("%s ~ %s (%s)", req.getStartDate(), req.getEndDate(), req.getTimeType());
    }

    private List<TrendUnifiedResult.Point> convertPointsUnified(List<Map<String, Object>> points,
                                                                String metric,
                                                                Integer energyType,
                                                                Map<Integer, Ratio> ratioMap) {
        List<TrendUnifiedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : points) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            double y = applyMetric(energy, metric, energyType, ratioMap);
            TrendUnifiedResult.Point p = new TrendUnifiedResult.Point();
            p.setX(label);
            p.setY(round(y, 2));
            list.add(p);
        }
        return list;
    }

    private List<TrendSeparatedResult.Point> convertPoints(List<Map<String, Object>> points,
                                                           String metric,
                                                           Integer energyType,
                                                           Map<Integer, Ratio> ratioMap) {
        List<TrendSeparatedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : points) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            double y = applyMetric(energy, metric, energyType, ratioMap);
            TrendSeparatedResult.Point p = new TrendSeparatedResult.Point();
            p.setX(label);
            p.setY(round(y, 2));
            list.add(p);
        }
        return list;
    }

    private double applyMetric(Double energy,
                               String metric,
                               Integer energyType,
                               Map<Integer, Ratio> ratioMap) {
        double e = Optional.ofNullable(energy).orElse(0.0);
        Ratio r = ratioMap.getOrDefault(energyType == null ? 1 : energyType, new Ratio("kWh", 1.0, 1.0));
        switch (metric) {
            case "standardCoal": return e * Optional.ofNullable(r.standardCoalFactor).orElse(1.0);
            case "carbon": return e * Optional.ofNullable(r.carbonFactor).orElse(1.0);
            default: return e; // energy
        }
    }

    private String metricTitle(String metric) {
        switch (metric) {
            case "energy": return "能耗";
            case "standardCoal": return "折标煤";
            case "carbon": return "碳排放";
        }
        return metric;
    }

    private String metricUnit(String metric, Ratio ratio) {
        switch (metric) {
            case "energy": return ratio != null ? ratio.unit : ""; // 比如 kWh / m3
            case "standardCoal": return "kgce"; // 千克标准煤
            case "carbon": return "kgCO2e"; // 温室气体当量
        }
        return "";
    }

    private Set<String> resolveMetrics(List<String> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return new LinkedHashSet<>(Arrays.asList("standardCoal", "carbon"));
        }
        LinkedHashSet<String> set = new LinkedHashSet<>(metrics);
        set.retainAll(Arrays.asList("energy","standardCoal","carbon"));
        if (set.isEmpty()) set.add("standardCoal");
        return set;
    }

    private Map<Integer, Ratio> loadRatioMap() {
        Map<Integer, Ratio> map = new HashMap<>();
        for (Map<String, Object> r : mapper.selectEnergyRatio()) {
            Integer type = r.get("energyType") == null ? null : ((Number) r.get("energyType")).intValue();
            String unit = Objects.toString(r.get("unit"), "kWh");
            Double sc = getDouble(r.get("standardCoalFactor"));
            Double cf = getDouble(r.get("carbonFactor"));
            if (type != null) map.put(type, new Ratio(unit, sc, cf));
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

    private static class Ratio {
        String unit;
        Double standardCoalFactor;
        Double carbonFactor;
        Ratio(String unit, Double sc, Double cf) { this.unit = unit; this.standardCoalFactor = sc; this.carbonFactor = cf; }
    }
}

