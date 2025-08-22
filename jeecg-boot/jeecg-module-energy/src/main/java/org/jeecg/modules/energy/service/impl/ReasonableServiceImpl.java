package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.mapper.ReasonableAnalysisMapper;
import org.jeecg.modules.energy.service.IReasonableService;
import org.jeecg.modules.energy.vo.reasonable.ReasonableRequest;
import org.jeecg.modules.energy.vo.reasonable.ReasonableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ReasonableServiceImpl implements IReasonableService {

    @Autowired
    private ReasonableAnalysisMapper mapper;

    @Override
    public ReasonableResponse analyze(ReasonableRequest request) {
        validate(request);

        String table = resolveTable(request.getTimeType());
        String labelExpr = resolveLabelExpr(request.getTimeType());

        // 转换起止时间为 dt 对齐（仅用作SQL过滤，不做时区换算，遵循统计表标准化口径）
        String startDt = normalizeStartDt(request.getStartDate(), request.getTimeType());
        String endDt = normalizeEndDt(request.getEndDate(), request.getTimeType());

        // 1) 汇总
        Map<String, Object> sumMap = mapper.selectSummary(table, request.getModuleIds(), startDt, endDt);
        ReasonableResponse resp = new ReasonableResponse();
        ReasonableResponse.Summary summary = new ReasonableResponse.Summary();
        summary.setCuspCount(getDouble(sumMap.get("cuspCount")));
        summary.setPeakCount(getDouble(sumMap.get("peakCount")));
        summary.setLevelCount(getDouble(sumMap.get("levelCount")));
        summary.setValleyCount(getDouble(sumMap.get("valleyCount")));
        summary.setTotalCount(getDouble(sumMap.get("totalCount")));
        resp.setSummary(summary);

        // 2) 占比
        List<ReasonableResponse.RingItem> ratio = buildRatio(summary);
        resp.setRatio(ratio);

        // 3) 总能耗趋势
        List<Map<String, Object>> totalTrendList = mapper.selectTotalTrend(table, labelExpr,
                request.getModuleIds(), startDt, endDt);
        List<ReasonableResponse.TotalTrendItem> totalTrend = new ArrayList<>();
        for (Map<String, Object> m : totalTrendList) {
            ReasonableResponse.TotalTrendItem it = new ReasonableResponse.TotalTrendItem();
            it.setDate(String.valueOf(m.get("label")));
            it.setEnergyCount(getDouble(m.get("energyCount")));
            totalTrend.add(it);
        }
        resp.setTotalTrend(totalTrend);

        // 4) 尖峰平谷趋势
        List<Map<String, Object>> touList = mapper.selectTouTrend(table, labelExpr,
                request.getModuleIds(), startDt, endDt);
        ReasonableResponse.TouTrend touTrend = new ReasonableResponse.TouTrend();
        touTrend.setCusp(new ArrayList<>());
        touTrend.setPeak(new ArrayList<>());
        touTrend.setLevel(new ArrayList<>());
        touTrend.setValley(new ArrayList<>());
        for (Map<String, Object> m : touList) {
            String label = String.valueOf(m.get("label"));
            touTrend.getCusp().add(point(label, getDouble(m.get("cusp"))));
            touTrend.getPeak().add(point(label, getDouble(m.get("peak"))));
            touTrend.getLevel().add(point(label, getDouble(m.get("level"))));
            touTrend.getValley().add(point(label, getDouble(m.get("valley"))));
        }
        resp.setTouTrend(touTrend);

        return resp;
    }

    private void validate(ReasonableRequest req) {
        if (req == null || CollectionUtils.isEmpty(req.getModuleIds())) {
            throw new IllegalArgumentException("moduleIds不能为空");
        }
        if (!StringUtils.hasText(req.getStartDate()) || !StringUtils.hasText(req.getEndDate())) {
            throw new IllegalArgumentException("startDate/endDate不能为空");
        }
        if (!Arrays.asList("day","month","year").contains(req.getTimeType())) {
            throw new IllegalArgumentException("timeType非法，必须为 day|month|year");
        }
    }

    private String resolveTable(String timeType) {
        switch (timeType) {
            case "day": return "tb_ep_equ_energy_daycount";
            case "month": return "tb_ep_equ_energy_monthcount";
            case "year": return "tb_ep_equ_energy_yearcount";
            default: throw new IllegalArgumentException("非法timeType");
        }
    }

    private String resolveLabelExpr(String timeType) {
        switch (timeType) {
            case "day": return "DATE_FORMAT(dt, '%Y-%m-%d')";
            case "month": return "DATE_FORMAT(dt, '%Y-%m')";
            case "year": return "DATE_FORMAT(dt, '%Y')";
            default: throw new IllegalArgumentException("非法timeType");
        }
    }

    private String normalizeStartDt(String start, String timeType) {
        if ("day".equals(timeType)) return start + " 00:00:00";
        if ("month".equals(timeType)) return start + "-01 00:00:00";
        if ("year".equals(timeType)) return start + "-01-01 00:00:00";
        return start;
    }

    private String normalizeEndDt(String end, String timeType) {
        if ("day".equals(timeType)) return end + " 23:59:59"; // 闭区间
        if ("month".equals(timeType)) return end + "-31 23:59:59"; // MySQL会处理不存在日期为最后一天
        if ("year".equals(timeType)) return end + "-12-31 23:59:59";
        return end;
    }

    private ReasonableResponse.Point point(String x, Double y) {
        ReasonableResponse.Point p = new ReasonableResponse.Point();
        p.setX(x);
        p.setY(y);
        return p;
    }

    private Double getDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Double) return (Double) o;
        if (o instanceof BigDecimal) return ((BigDecimal) o).doubleValue();
        if (o instanceof Number) return ((Number) o).doubleValue();
        return 0.0;
    }

    private List<ReasonableResponse.RingItem> buildRatio(ReasonableResponse.Summary s) {
        double total = Optional.ofNullable(s.getTotalCount()).orElse(0.0);
        List<ReasonableResponse.RingItem> items = new ArrayList<>();
        items.add(ring("尖", s.getCuspCount(), total));
        items.add(ring("峰", s.getPeakCount(), total));
        items.add(ring("平", s.getLevelCount(), total));
        items.add(ring("谷", s.getValleyCount(), total));
        return items;
    }

    private ReasonableResponse.RingItem ring(String name, Double value, double total) {
        ReasonableResponse.RingItem it = new ReasonableResponse.RingItem();
        it.setName(name);
        double v = Optional.ofNullable(value).orElse(0.0);
        it.setValue(v);
        it.setPercent(total > 0 ? round(v * 100.0 / total, 2) : 0.0);
        return it;
    }

    private double round(double v, int scale) {
        return new BigDecimal(v).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}

