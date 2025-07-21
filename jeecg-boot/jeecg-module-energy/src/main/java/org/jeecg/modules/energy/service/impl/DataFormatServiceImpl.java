package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.config.IntervalConfig;
import org.jeecg.modules.energy.config.ParameterConfig;
import org.jeecg.modules.energy.service.IDataFormatService;
import org.jeecg.modules.energy.vo.monitor.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 数据格式化服务实现类
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Slf4j
@Service
public class DataFormatServiceImpl implements IDataFormatService {

    @Override
    public UnifiedDisplayResult formatUnifiedDisplay(
            List<Map<String, Object>> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval) {

        UnifiedDisplayResult result = new UnifiedDisplayResult();
        result.setDisplayMode("unified");
        result.setTimeRange(buildTimeRange(startTime, endTime, interval));

        List<SeriesDataVO> seriesList = new ArrayList<>();

        // 按tagname分组处理数据 (过滤掉tagname为null的记录)
        Map<String, List<Map<String, Object>>> groupedData = influxResults.stream()
                .filter(r -> r.get("tagname") != null) // 过滤掉tagname为null的记录
                .collect(Collectors.groupingBy(r -> (String) r.get("tagname")));

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
            String tagname = entry.getKey();  // 格式: yj0001_1202#IA
            if (tagname == null || !tagname.contains("#")) {
                continue;
            }

            String[] parts = tagname.split("#");
            String moduleId = parts[0].toLowerCase(); // 转换为小写匹配MySQL
            String paramField = parts[1];

            // 查找参数信息
            ParameterConfig.ParameterInfo paramInfo = ParameterConfig.findParameterByField(paramField);
            if (paramInfo == null) {
                log.warn("未找到参数信息: {}", paramField);
                continue;
            }

            String moduleName = moduleNameMap.get(moduleId);
            if (moduleName == null) {
                log.warn("未找到仪表名称: {}", moduleId);
                moduleName = moduleId; // 使用moduleId作为默认名称
            }

            SeriesDataVO series = new SeriesDataVO();
            series.setName(moduleName + "-" + paramInfo.getDisplayName());
            series.setModuleId(moduleId);
            series.setModuleName(moduleName);
            series.setParameter(paramInfo.getDisplayName());
            series.setParameterCode(paramInfo.getFieldName());
            series.setUnit(paramInfo.getUnit());

            // 转换数据点 (UTC时间转本地时间，null值处理为0)
            List<Object[]> dataPoints = entry.getValue().stream()
                    .filter(r -> r.get("time") != null) // 只过滤掉时间为null的记录
                    .map(r -> {
                        Object value = r.get("value");
                        // 将null值转换为0，保持数据连续性
                        if (value == null) {
                            value = 0.0;
                        }
                        return new Object[]{
                                convertToLocalTime((String) r.get("time")),
                                value
                        };
                    })
                    .collect(Collectors.toList());
            series.setData(dataPoints);

            seriesList.add(series);

            log.info("🔍 统一显示 - 创建系列: tagname={}, name={}, 数据点数量={}",
                tagname, series.getName(), series.getData() != null ? series.getData().size() : 0);
        }

        log.info("🔍 统一显示 - 最终结果: 系列数量={}", seriesList.size());
        result.setSeries(seriesList);
        return result;
    }

    @Override
    public SeparatedDisplayResult formatSeparatedDisplay(
            List<Map<String, Object>> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval) {

        SeparatedDisplayResult result = new SeparatedDisplayResult();
        result.setDisplayMode("separated");
        result.setTimeRange(buildTimeRange(startTime, endTime, interval));

        List<ChartDataVO> chartsList = new ArrayList<>();

        // 按参数分组
        Map<String, List<Map<String, Object>>> paramGroupedData = influxResults.stream()
                .filter(r -> r.get("tagname") != null && ((String) r.get("tagname")).contains("#"))
                .collect(Collectors.groupingBy(r -> ((String) r.get("tagname")).split("#")[1]));

        log.info("🔍 分开显示 - 按参数分组结果: {}", paramGroupedData.keySet());

        for (String paramField : paramGroupedData.keySet()) {
            log.info("🔍 分开显示 - 处理参数字段: {}", paramField);
            ParameterConfig.ParameterInfo paramInfo = ParameterConfig.findParameterByField(paramField);
            if (paramInfo == null) {
                log.warn("❌ 分开显示 - 未找到参数信息: {}", paramField);
                continue;
            }
            log.info("✅ 分开显示 - 找到参数信息: {} -> {}", paramField, paramInfo.getDisplayName());

            ChartDataVO chart = new ChartDataVO();
            chart.setTitle(paramInfo.getDisplayName());
            chart.setParameter(paramInfo.getDisplayName());
            chart.setParameterCode(paramInfo.getFieldName());
            chart.setUnit(paramInfo.getUnit());

            // 按仪表分组该参数的数据
            List<SeriesDataVO> seriesList = new ArrayList<>();
            Map<String, List<Map<String, Object>>> moduleGroupedData =
                    paramGroupedData.get(paramField).stream()
                            .collect(Collectors.groupingBy(r -> ((String) r.get("tagname")).split("#")[0].toLowerCase()));

            for (Map.Entry<String, List<Map<String, Object>>> moduleEntry : moduleGroupedData.entrySet()) {
                String moduleId = moduleEntry.getKey();
                String moduleName = moduleNameMap.get(moduleId);
                if (moduleName == null) {
                    log.warn("未找到仪表名称: {}", moduleId);
                    moduleName = moduleId;
                }

                SeriesDataVO series = new SeriesDataVO();
                series.setName(moduleName);
                series.setModuleId(moduleId);

                List<Object[]> dataPoints = moduleEntry.getValue().stream()
                        .filter(r -> r.get("time") != null) // 只过滤掉时间为null的记录
                        .map(r -> {
                            Object value = r.get("value");
                            // 将null值转换为0，保持数据连续性
                            if (value == null) {
                                value = 0.0;
                            }
                            return new Object[]{
                                    convertToLocalTime((String) r.get("time")),
                                    value
                            };
                        })
                        .collect(Collectors.toList());
                series.setData(dataPoints);

                seriesList.add(series);
            }

            chart.setSeries(seriesList);
            chartsList.add(chart);

            log.info("🔍 分开显示 - 创建图表: 参数={}, 系列数量={}", paramField, seriesList.size());
            for (int i = 0; i < seriesList.size(); i++) {
                SeriesDataVO series = seriesList.get(i);
                log.info("  系列[{}]: name={}, 数据点数量={}", i, series.getName(),
                    series.getData() != null ? series.getData().size() : 0);
            }
        }

        log.info("🔍 分开显示 - 最终结果: 图表数量={}", chartsList.size());
        result.setCharts(chartsList);
        return result;
    }

    /**
     * 构建时间范围信息
     */
    private TimeRangeVO buildTimeRange(String startTime, String endTime, Integer interval) {
        TimeRangeVO timeRange = new TimeRangeVO();
        timeRange.setStartTime(startTime);
        timeRange.setEndTime(endTime);
        timeRange.setInterval(IntervalConfig.getIntervalDisplayName(interval));
        return timeRange;
    }

    /**
     * UTC时间转本地时间
     */
    private String convertToLocalTime(String utcTime) {
        try {
            Instant instant = Instant.parse(utcTime);
            ZonedDateTime local = instant.atZone(ZoneId.of("Asia/Shanghai"));
            return local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.error("时间转换失败: {}", utcTime, e);
            return utcTime; // 返回原始时间
        }
    }
}
