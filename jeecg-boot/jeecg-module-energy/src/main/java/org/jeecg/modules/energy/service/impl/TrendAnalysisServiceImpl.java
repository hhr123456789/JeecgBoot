package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.modules.energy.mapper.TrendAnalysisMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.ITrendAnalysisService;
import org.jeecg.modules.energy.vo.trend.TrendRequest;
import org.jeecg.modules.energy.vo.trend.TrendSeparatedResult;
import org.jeecg.modules.energy.vo.trend.TrendUnifiedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
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

        // 1) 先查每个时间标签的总能耗（合并多仪表）
        List<Map<String, Object>> energyTrend = mapper.selectEnergyTrend(
                table, labelExpr, request.getModuleIds(), startDt, endDt);

        // 2) 准备 moduleId->energyType 映射与能源系数
        Map<String, Integer> moduleEnergyType = tbModuleMapper.selectEnergyTypeByModuleIds(request.getModuleIds())
                .stream().collect(Collectors.toMap(m -> Objects.toString(m.get("moduleId"), ""),
                        m -> m.get("energyType") == null ? null : ((Number) m.get("energyType")).intValue(), (a,b)->a));
        Map<Integer, Ratio> ratioMap = loadRatioMap();

        // 3) 默认指标集合
        Set<String> metrics = resolveMetrics(request.getMetrics());

        // 统一：为每个指标生成一条合计曲线（多仪表已合并）；
        // 分开：每个指标一个图，图内暂时提供“合计”一条曲线（后续可按module拆分）。
        if (Objects.equals(request.getDisplayMode(), 2)) {
            TrendSeparatedResult res = new TrendSeparatedResult();
            res.setTimeRange(buildTimeRange(request));
            List<TrendSeparatedResult.Chart> charts = new ArrayList<>();
            for (String metric : metrics) {
                TrendSeparatedResult.Chart chart = new TrendSeparatedResult.Chart();
                chart.setMetric(metric);
                chart.setTitle(metricTitle(metric));
                chart.setUnit(metricUnit(metric, null));
                TrendSeparatedResult.Series series = new TrendSeparatedResult.Series();
                series.setName("合计");
                series.setModuleId("-");
                series.setData(convertPointsByMetric(energyTrend, request.getModuleIds(), moduleEnergyType, ratioMap, metric));
                chart.setSeries(Collections.singletonList(series));
                charts.add(chart);
            }
            res.setCharts(charts);
            return res;
        } else {
            TrendUnifiedResult res = new TrendUnifiedResult();
            res.setTimeRange(buildTimeRange(request));
            List<TrendUnifiedResult.Series> series = new ArrayList<>();
            for (String metric : metrics) {
                TrendUnifiedResult.Series s = new TrendUnifiedResult.Series();
                s.setName(metricTitle(metric));
                s.setMetric(metric);
                s.setUnit(metricUnit(metric, null));
                s.setData(convertPointsUnifiedByMetric(energyTrend, request.getModuleIds(), moduleEnergyType, ratioMap, metric));
                series.add(s);
            }
            res.setSeries(series);
            return res;
        }
    }

    @Override
    public void exportTrend(TrendRequest request, HttpServletResponse response) {
        // 简单CSV导出（避免引入依赖）。若需xlsx，可集成 EasyPOI/Apache POI。
        try {
            Object obj = getTrend(request);
            String fileName = URLEncoder.encode("趋势分析导出.csv", "UTF-8");
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            StringBuilder sb = new StringBuilder();
            if (obj instanceof TrendUnifiedResult) {
                TrendUnifiedResult r = (TrendUnifiedResult) obj;
                sb.append("label");
                for (TrendUnifiedResult.Series s : r.getSeries()) {
                    sb.append(',').append(s.getName()).append('(').append(s.getUnit()).append(')');
                }
                sb.append('\n');
                List<String> labels = r.getSeries().isEmpty() ? Collections.emptyList() :
                        r.getSeries().get(0).getData().stream().map(TrendUnifiedResult.Point::getX).collect(Collectors.toList());
                for (int i = 0; i < labels.size(); i++) {
                    sb.append(labels.get(i));
                    for (TrendUnifiedResult.Series s : r.getSeries()) {
                        Double y = i < s.getData().size() ? s.getData().get(i).getY() : null;
                        sb.append(',').append(y == null ? "" : y);
                    }
                    sb.append('\n');
                }
            } else if (obj instanceof TrendSeparatedResult) {
                TrendSeparatedResult r = (TrendSeparatedResult) obj;
                for (TrendSeparatedResult.Chart c : r.getCharts()) {
                    sb.append(c.getTitle()).append('(').append(c.getUnit()).append(')').append('\n');
                    sb.append("label,合计\n");
                    for (TrendSeparatedResult.Point p : c.getSeries().get(0).getData()) {
                        sb.append(p.getX()).append(',').append(p.getY()).append('\n');
                    }
                    sb.append('\n');
                }
            }
            response.getWriter().write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    // ---------------- private helpers ----------------

    private void validate(TrendRequest req) {
        if (req == null || CollectionUtils.isEmpty(req.getModuleIds())) {
            throw new IllegalArgumentException("moduleIds不能为空");
        }
        if (!Arrays.asList("day", "month", "year").contains(req.getTimeType())) {
            throw new IllegalArgumentException("timeType必须为day|month|year");
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

    private List<TrendUnifiedResult.Point> convertPointsUnifiedByMetric(List<Map<String, Object>> energyTrend,
                                                                        List<String> moduleIds,
                                                                        Map<String, Integer> moduleEnergyType,
                                                                        Map<Integer, Ratio> ratioMap,
                                                                        String metric) {
        List<TrendUnifiedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : energyTrend) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            double y = applyMetricWithMixedTypes(energy, moduleIds, moduleEnergyType, ratioMap, metric);
            TrendUnifiedResult.Point p = new TrendUnifiedResult.Point();
            p.setX(label);
            p.setY(round(y, 2));
            list.add(p);
        }
        return list;
    }

    private List<TrendSeparatedResult.Point> convertPointsByMetric(List<Map<String, Object>> energyTrend,
                                                                   List<String> moduleIds,
                                                                   Map<String, Integer> moduleEnergyType,
                                                                   Map<Integer, Ratio> ratioMap,
                                                                   String metric) {
        List<TrendSeparatedResult.Point> list = new ArrayList<>();
        for (Map<String, Object> m : energyTrend) {
            String label = String.valueOf(m.get("label"));
            Double energy = getDouble(m.get("energy"));
            double y = applyMetricWithMixedTypes(energy, moduleIds, moduleEnergyType, ratioMap, metric);
            TrendSeparatedResult.Point p = new TrendSeparatedResult.Point();
            p.setX(label);
            p.setY(round(y, 2));
            list.add(p);
        }
        return list;
    }

    private double applyMetricWithMixedTypes(Double totalEnergy,
                                             List<String> moduleIds,
                                             Map<String, Integer> moduleEnergyType,
                                             Map<Integer, Ratio> ratioMap,
                                             String metric) {
        // 简化：将总能耗按模块平均分配再乘各自系数再求和（若前端常选同一能源类型，此项影响很小）
        // 更精准的做法：按 label & module 聚合能耗（需要修改SQL加上 module_id 维度）。
        double e = Optional.ofNullable(totalEnergy).orElse(0.0);
        if (e == 0 || moduleIds.isEmpty()) return 0.0;
        double per = e / moduleIds.size();
        double sum = 0.0;
        for (String id : moduleIds) {
            Integer type = moduleEnergyType.get(id);
            Ratio r = ratioMap.getOrDefault(type == null ? 1 : type, new Ratio("kWh", 1.0, 1.0));
            switch (metric) {
                case "standardCoal": sum += per * Optional.ofNullable(r.standardCoalFactor).orElse(1.0); break;
                case "carbon": sum += per * Optional.ofNullable(r.carbonFactor).orElse(1.0); break;
                default: sum += per; // energy
            }
        }
        return sum;
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

