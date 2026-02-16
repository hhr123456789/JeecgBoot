package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.energy.entity.*;
import org.jeecg.modules.energy.mapper.*;
import org.jeecg.modules.energy.service.IShiftEnergyService;
import org.jeecg.modules.energy.vo.shiftenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShiftEnergyServiceImpl implements IShiftEnergyService {

    private static final BigDecimal SHIFT_RATIO = new BigDecimal("0.3333");

    private static final Map<String, String> SHIFT_NAME_MAP = new LinkedHashMap<>();
    static {
        SHIFT_NAME_MAP.put("morning", "\u65e9\u73ed");
        SHIFT_NAME_MAP.put("middle", "\u4e2d\u73ed");
        SHIFT_NAME_MAP.put("night", "\u665a\u73ed");
    }

    private static final Map<String, String> SHIFT_COLOR_MAP = new LinkedHashMap<>();
    static {
        SHIFT_COLOR_MAP.put("morning", "#4B7BE5");
        SHIFT_COLOR_MAP.put("middle", "#23C343");
        SHIFT_COLOR_MAP.put("night", "#FF9F40");
    }

    @Autowired
    private TeamDimensionRelationMapper teamDimensionRelationMapper;
    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;
    @Autowired
    private TbEpEquEnergyMonthcountMapper monthcountMapper;
    @Autowired
    private TbEpEquEnergyYearcountMapper yearcountMapper;
    @Autowired
    private TbEnergyRatioInfoMapper energyRatioInfoMapper;

    @Override
    public ShiftEnergyStatisticsVO getStatistics(ShiftEnergyQueryRequest request) {
        log.info("获取班次统计数据 - dimensionCode={}, timeUnit={}, queryDate={}, shiftType={}",
                request.getDimensionCode(), request.getTimeUnit(), request.getQueryDate(), request.getShiftType());

        List<String> moduleIds = getModuleIdsByDimension(request.getDimensionCode(), request.getDimensionType());
        if (moduleIds.isEmpty()) {
            return createEmptyStatistics(request.getEnergyType());
        }

        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            return createEmptyStatistics(request.getEnergyType());
        }

        BigDecimal totalConsumption = queryTotalEnergy(moduleIds, dateRange[0], dateRange[1], request.getTimeUnit());
        BigDecimal morningConsumption = totalConsumption.multiply(SHIFT_RATIO);
        BigDecimal middleConsumption = totalConsumption.multiply(SHIFT_RATIO);
        BigDecimal nightConsumption = totalConsumption.subtract(morningConsumption).subtract(middleConsumption);

        TbEnergyRatioInfo ratioInfo = getEnergyRatioInfo(request.getEnergyType());
        BigDecimal pricePerUnit = getSafeValue(ratioInfo, TbEnergyRatioInfo::getPricePerUnit, "0.80");
        BigDecimal carbonFactor = getSafeValue(ratioInfo, TbEnergyRatioInfo::getTpfxsValue, "0.997");
        BigDecimal coalFactor = getSafeValue(ratioInfo, TbEnergyRatioInfo::getZbmxsValue, "0.1229");

        ShiftEnergyStatisticsVO vo = new ShiftEnergyStatisticsVO();
        vo.setTotalConsumption(scale2(totalConsumption));
        vo.setMorningConsumption(scale2(morningConsumption));
        vo.setMiddleConsumption(scale2(middleConsumption));
        vo.setNightConsumption(scale2(nightConsumption));
        vo.setTotalCost(scale2(totalConsumption.multiply(pricePerUnit)));
        vo.setTotalCarbon(scale2(totalConsumption.multiply(carbonFactor)));
        vo.setTotalCoal(scale2(totalConsumption.multiply(coalFactor)));
        vo.setEnergyUnit(getEnergyUnit(request.getEnergyType()));
        return vo;
    }

    @Override
    public ShiftEnergyTrendVO getTrendData(ShiftEnergyQueryRequest request) {
        log.info("获取班次趋势数据 - dimensionCode={}, timeUnit={}, queryDate={}",
                request.getDimensionCode(), request.getTimeUnit(), request.getQueryDate());

        ShiftEnergyTrendVO vo = new ShiftEnergyTrendVO();
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            vo.setXAxisData(new ArrayList<>());
            vo.setSeriesData(new ArrayList<>());
            return vo;
        }
        List<String> xAxisData = generateXAxisData(request.getTimeUnit(), dateRange[0], dateRange[1]);
        vo.setXAxisData(xAxisData);

        List<String> moduleIds = getModuleIdsByDimension(request.getDimensionCode(), request.getDimensionType());
        List<String> shiftTypes = getShiftTypes(request.getShiftType());

        List<ShiftEnergyTrendVO.SeriesData> seriesList = new ArrayList<>();
        for (String shiftType : shiftTypes) {
            List<Object> dataValues = new ArrayList<>();
            for (String dateLabel : xAxisData) {
                BigDecimal totalValue = moduleIds.isEmpty() ? BigDecimal.ZERO
                        : queryEnergyByDateLabel(moduleIds, dateLabel, request.getTimeUnit(), request.getQueryDate());
                BigDecimal shiftValue = totalValue.multiply(SHIFT_RATIO);
                dataValues.add(shiftValue.setScale(2, RoundingMode.HALF_UP).doubleValue());
            }
            ShiftEnergyTrendVO.SeriesData series = new ShiftEnergyTrendVO.SeriesData();
            series.setName(SHIFT_NAME_MAP.getOrDefault(shiftType, shiftType));
            series.setData(dataValues);
            series.setColor(SHIFT_COLOR_MAP.getOrDefault(shiftType, "#4B7BE5"));
            seriesList.add(series);
        }
        vo.setSeriesData(seriesList);
        return vo;
    }

    @Override
    public List<ShiftEnergyPieVO> getPieData(ShiftEnergyQueryRequest request) {
        log.info("获取班次占比数据 - dimensionCode={}, timeUnit={}, queryDate={}",
                request.getDimensionCode(), request.getTimeUnit(), request.getQueryDate());

        List<ShiftEnergyPieVO> pieData = new ArrayList<>();
        List<String> moduleIds = getModuleIdsByDimension(request.getDimensionCode(), request.getDimensionType());
        if (moduleIds.isEmpty()) {
            return pieData;
        }
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            return pieData;
        }

        BigDecimal total = queryTotalEnergy(moduleIds, dateRange[0], dateRange[1], request.getTimeUnit());
        BigDecimal morningVal = total.multiply(SHIFT_RATIO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal middleVal = total.multiply(SHIFT_RATIO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal nightVal = total.subtract(morningVal).subtract(middleVal).setScale(2, RoundingMode.HALF_UP);

        String[] keys = {"morning", "middle", "night"};
        BigDecimal[] vals = {morningVal, middleVal, nightVal};
        for (int i = 0; i < keys.length; i++) {
            ShiftEnergyPieVO item = new ShiftEnergyPieVO();
            item.setName(SHIFT_NAME_MAP.get(keys[i]));
            item.setValue(vals[i].doubleValue());
            item.setColor(SHIFT_COLOR_MAP.get(keys[i]));
            pieData.add(item);
        }
        return pieData;
    }

    @Override
    public List<ShiftEnergyTableVO> getTableData(ShiftEnergyQueryRequest request) {
        log.info("获取班次表格数据 - dimensionCode={}, timeUnit={}, queryDate={}",
                request.getDimensionCode(), request.getTimeUnit(), request.getQueryDate());

        List<ShiftEnergyTableVO> tableData = new ArrayList<>();
        List<String> moduleIds = getModuleIdsByDimension(request.getDimensionCode(), request.getDimensionType());
        if (moduleIds.isEmpty()) {
            return tableData;
        }
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            return tableData;
        }

        TbEnergyRatioInfo ratioInfo = getEnergyRatioInfo(request.getEnergyType());
        BigDecimal pricePerUnit = getSafeValue(ratioInfo, TbEnergyRatioInfo::getPricePerUnit, "0.80");
        BigDecimal carbonFactor = getSafeValue(ratioInfo, TbEnergyRatioInfo::getTpfxsValue, "0.997");
        BigDecimal coalFactor = getSafeValue(ratioInfo, TbEnergyRatioInfo::getZbmxsValue, "0.1229");

        LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TbEpEquEnergyDaycount::getModuleId, moduleIds);
        wrapper.between(TbEpEquEnergyDaycount::getDt, dateRange[0], dateRange[1]);
        List<TbEpEquEnergyDaycount> dataList = daycountMapper.selectList(wrapper);

        Map<String, List<TbEpEquEnergyDaycount>> groupedData = dataList.stream()
                .collect(Collectors.groupingBy(d -> new SimpleDateFormat("yyyy-MM-dd").format(d.getDt())));

        for (Map.Entry<String, List<TbEpEquEnergyDaycount>> entry : groupedData.entrySet()) {
            BigDecimal dayTotal = sumBigDecimal(entry.getValue(), TbEpEquEnergyDaycount::getEnergyCount);
            BigDecimal morning = dayTotal.multiply(SHIFT_RATIO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal middle = dayTotal.multiply(SHIFT_RATIO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal night = dayTotal.subtract(morning).subtract(middle).setScale(2, RoundingMode.HALF_UP);

            ShiftEnergyTableVO vo = new ShiftEnergyTableVO();
            vo.setDate(entry.getKey());
            vo.setMorningConsumption(morning);
            vo.setMiddleConsumption(middle);
            vo.setNightConsumption(night);
            vo.setTotalConsumption(dayTotal.setScale(2, RoundingMode.HALF_UP));
            vo.setTotalCost(dayTotal.multiply(pricePerUnit).setScale(2, RoundingMode.HALF_UP));
            vo.setCarbon(dayTotal.multiply(carbonFactor).setScale(2, RoundingMode.HALF_UP));
            vo.setCoal(dayTotal.multiply(coalFactor).setScale(4, RoundingMode.HALF_UP));
            tableData.add(vo);
        }

        tableData.sort(Comparator.comparing(ShiftEnergyTableVO::getDate));
        return tableData;
    }

    // ==================== 辅助方法 ====================

    private List<String> getModuleIdsByDimension(String dimensionCode, Integer dimensionType) {
        if (StringUtils.isBlank(dimensionCode)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TeamDimensionRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamDimensionRelation::getDimensionCode, dimensionCode);
        if (dimensionType != null) {
            wrapper.eq(TeamDimensionRelation::getDimensionType, dimensionType);
        }
        wrapper.eq(TeamDimensionRelation::getStatus, 1);
        List<TeamDimensionRelation> relations = teamDimensionRelationMapper.selectList(wrapper);
        log.info("查询维度关联: dimensionCode={}, 找到 {} 条记录", dimensionCode, relations.size());
        return relations.stream()
                .map(TeamDimensionRelation::getModuleIds)
                .filter(StringUtils::isNotBlank)
                .flatMap(ids -> Arrays.stream(ids.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getShiftTypes(String shiftType) {
        if (StringUtils.isBlank(shiftType) || "all".equals(shiftType)) {
            return Arrays.asList("morning", "middle", "night");
        }
        return Collections.singletonList(shiftType);
    }

    private int getShiftOrder(String shiftType) {
        switch (shiftType) {
            case "morning": return 1;
            case "middle": return 2;
            case "night": return 3;
            default: return 9;
        }
    }

    private String scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private BigDecimal getSafeValue(TbEnergyRatioInfo ratioInfo, Function<TbEnergyRatioInfo, BigDecimal> getter, String defaultVal) {
        if (ratioInfo != null) {
            BigDecimal val = getter.apply(ratioInfo);
            if (val != null) return val;
        }
        return new BigDecimal(defaultVal);
    }

    private Date[] parseDateRange(String timeUnit, String queryDate) {
        try {
            Calendar cal = Calendar.getInstance();
            if ("day".equals(timeUnit)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            } else if ("month".equals(timeUnit)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            } else if ("year".equals(timeUnit)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.MONTH, Calendar.JANUARY); cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.MONTH, Calendar.DECEMBER); cal.set(Calendar.DAY_OF_MONTH, 31);
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            }
        } catch (ParseException e) {
            log.error("日期解析失败: timeUnit={}, queryDate={}", timeUnit, queryDate, e);
        }
        return null;
    }

    private List<String> generateXAxisData(String timeUnit, Date startDate, Date endDate) {
        List<String> xAxisData = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        if ("day".equals(timeUnit)) {
            for (int i = 0; i < 24; i++) {
                xAxisData.add(String.format("%02d:00", i));
            }
        } else if ("month".equals(timeUnit)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            while (!cal.getTime().after(endDate)) {
                xAxisData.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if ("year".equals(timeUnit)) {
            for (int i = 1; i <= 12; i++) {
                xAxisData.add(i + "月");
            }
        }
        return xAxisData;
    }

    private BigDecimal queryTotalEnergy(List<String> moduleIds, Date startDate, Date endDate, String timeUnit) {
        if (moduleIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if ("day".equals(timeUnit)) {
            LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyDaycount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyDaycount::getDt, startDate, endDate);
            List<TbEpEquEnergyDaycount> list = daycountMapper.selectList(wrapper);
            return sumBigDecimal(list, TbEpEquEnergyDaycount::getEnergyCount);
        } else if ("month".equals(timeUnit)) {
            LambdaQueryWrapper<TbEpEquEnergyMonthcount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyMonthcount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyMonthcount::getDt, startDate, endDate);
            List<TbEpEquEnergyMonthcount> list = monthcountMapper.selectList(wrapper);
            return list.stream().map(TbEpEquEnergyMonthcount::getEnergyCount)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        } else if ("year".equals(timeUnit)) {
            LambdaQueryWrapper<TbEpEquEnergyYearcount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyYearcount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyYearcount::getDt, startDate, endDate);
            List<TbEpEquEnergyYearcount> list = yearcountMapper.selectList(wrapper);
            return list.stream().map(TbEpEquEnergyYearcount::getEnergyCount)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal queryEnergyByDateLabel(List<String> moduleIds, String dateLabel, String timeUnit, String queryDate) {
        try {
            if ("day".equals(timeUnit)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(queryDate);
                BigDecimal dayTotal = queryTotalEnergy(moduleIds, date, date, "day");
                if (dayTotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                return dayTotal.divide(new BigDecimal(24), 2, RoundingMode.HALF_UP);
            } else if ("month".equals(timeUnit)) {
                String fullDate = queryDate + "-" + dateLabel.split("-")[1];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(fullDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                return queryTotalEnergy(moduleIds, start, cal.getTime(), "day");
            } else if ("year".equals(timeUnit)) {
                String monthStr = dateLabel.replace("月", "");
                String yearMonth = queryDate + "-" + String.format("%02d", Integer.parseInt(monthStr));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date date = sdf.parse(yearMonth);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                return queryTotalEnergy(moduleIds, start, cal.getTime(), "month");
            }
        } catch (ParseException e) {
            log.error("趋势数据日期解析失败: dateLabel={}, timeUnit={}, queryDate={}", dateLabel, timeUnit, queryDate, e);
        }
        return BigDecimal.ZERO;
    }

    private TbEnergyRatioInfo getEnergyRatioInfo(String energyType) {
        if (StringUtils.isBlank(energyType) || "all".equals(energyType)) {
            energyType = "1";
        }
        try {
            LambdaQueryWrapper<TbEnergyRatioInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbEnergyRatioInfo::getIsenergyType, Integer.parseInt(energyType));
            return energyRatioInfoMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("获取能源系数失败, energyType={}", energyType, e);
            return null;
        }
    }

    private ShiftEnergyStatisticsVO createEmptyStatistics(String energyType) {
        ShiftEnergyStatisticsVO vo = new ShiftEnergyStatisticsVO();
        vo.setTotalConsumption("0.00");
        vo.setMorningConsumption("0.00");
        vo.setMiddleConsumption("0.00");
        vo.setNightConsumption("0.00");
        vo.setTotalCost("0.00");
        vo.setTotalCarbon("0.00");
        vo.setTotalCoal("0.00");
        vo.setEnergyUnit(getEnergyUnit(energyType));
        return vo;
    }

    private BigDecimal sumBigDecimal(List<TbEpEquEnergyDaycount> dataList, Function<TbEpEquEnergyDaycount, BigDecimal> getter) {
        return dataList.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getEnergyUnit(String energyType) {
        if (energyType == null) return "kWh";
        switch (energyType) {
            case "1": return "kWh";
            case "2": case "8": case "5": return "m\u00b3";
            case "all": return "tce";
            default: return "kWh";
        }
    }
}
