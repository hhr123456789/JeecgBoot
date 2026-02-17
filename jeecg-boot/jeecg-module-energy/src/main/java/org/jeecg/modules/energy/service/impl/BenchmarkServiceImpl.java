package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.energy.entity.benchmark.*;
import org.jeecg.modules.energy.mapper.benchmark.*;
import org.jeecg.modules.energy.service.IBenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 能效对标服务实现
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class BenchmarkServiceImpl implements IBenchmarkService {

    @Autowired
    private BenchmarkConfigMapper configMapper;

    @Autowired
    private BenchmarkTargetMapper targetMapper;

    @Autowired
    private BenchmarkResultDayMapper resultDayMapper;

    @Autowired
    private BenchmarkResultMonthMapper resultMonthMapper;

    @Autowired
    private BenchmarkResultYearMapper resultYearMapper;

    @Override
    public List<BenchmarkConfig> getConfigList(Integer benchmarkType, String energyType) {
        log.info("获取对标配置列表 - benchmarkType: {}, energyType: {}", benchmarkType, energyType);

        LambdaQueryWrapper<BenchmarkConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BenchmarkConfig::getStatus, 1);

        if (benchmarkType != null) {
            wrapper.eq(BenchmarkConfig::getBenchmarkType, benchmarkType);
        }
        if (StringUtils.isNotBlank(energyType)) {
            wrapper.eq(BenchmarkConfig::getEnergyType, energyType);
        }

        return configMapper.selectList(wrapper);
    }

    @Override
    public List<BenchmarkTarget> getTargetList(String configId) {
        log.info("获取对标对象列表 - configId: {}", configId);

        LambdaQueryWrapper<BenchmarkTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BenchmarkTarget::getStatus, 1);

        if (StringUtils.isNotBlank(configId)) {
            wrapper.eq(BenchmarkTarget::getConfigId, configId);
        }
        wrapper.orderByAsc(BenchmarkTarget::getSortOrder);

        return targetMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getStatistics(String targetCode, String timeUnit, String startTime, String endTime, String energyType) {
        log.info("获取对标统计数据 - targetCode: {}, timeUnit: {}, startTime: {}, endTime: {}, energyType: {}",
                targetCode, timeUnit, startTime, endTime, energyType);

        Map<String, Object> result = new HashMap<>();

        // 根据时间粒度查询数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> trendData = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();

        if ("day".equals(timeUnit)) {
            dataList = queryDayData(targetCode, startTime, endTime, energyType);
        } else if ("month".equals(timeUnit)) {
            dataList = queryMonthData(targetCode, startTime, endTime, energyType);
        } else if ("year".equals(timeUnit)) {
            dataList = queryYearData(targetCode, startTime, endTime, energyType);
        }

        // 计算统计指标
        BigDecimal avgIntensity = BigDecimal.ZERO;
        BigDecimal minIntensity = BigDecimal.ZERO;
        BigDecimal varianceCoeff = BigDecimal.ZERO;

        if (!dataList.isEmpty()) {
            // 计算平均能耗强度
            BigDecimal sumIntensity = dataList.stream()
                    .map(d -> (BigDecimal) d.getOrDefault("energyIntensity", BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgIntensity = sumIntensity.divide(new BigDecimal(dataList.size()), 4, RoundingMode.HALF_UP);

            // 计算最优能耗强度
            minIntensity = dataList.stream()
                    .map(d -> (BigDecimal) d.getOrDefault("energyIntensity", BigDecimal.ZERO))
                    .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // 计算方差系数
            if (avgIntensity.compareTo(BigDecimal.ZERO) > 0) {
                final BigDecimal finalAvgIntensity = avgIntensity;
                BigDecimal sumSquaredDiff = dataList.stream()
                        .map(d -> {
                            BigDecimal intensity = (BigDecimal) d.getOrDefault("energyIntensity", BigDecimal.ZERO);
                            BigDecimal diff = intensity.subtract(finalAvgIntensity);
                            return diff.multiply(diff);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal variance = sumSquaredDiff.divide(new BigDecimal(dataList.size()), 6, RoundingMode.HALF_UP);
                BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
                varianceCoeff = stdDev.divide(avgIntensity, 4, RoundingMode.HALF_UP);
            }

            // 构建趋势数据
            trendData = dataList.stream().map(d -> {
                Map<String, Object> trend = new HashMap<>();
                trend.put("time", d.get("time"));
                trend.put("value", d.get("energyIntensity"));
                trend.put("targetName", d.get("targetName"));
                return trend;
            }).collect(Collectors.toList());

            // 构建表格数据
            tableData = dataList.stream().map(d -> {
                Map<String, Object> row = new HashMap<>();
                row.put("targetCode", d.get("targetCode"));
                row.put("targetName", d.get("targetName"));
                row.put("energyConsumption", d.get("energyConsumption"));
                row.put("productionOutput", d.get("productionOutput"));
                row.put("energyIntensity", d.get("energyIntensity"));
                row.put("ranking", d.get("ranking"));
                return row;
            }).collect(Collectors.toList());
        }

        // 统计卡片数据
        Map<String, Object> cards = new HashMap<>();
        cards.put("avgIntensity", avgIntensity);
        cards.put("minIntensity", minIntensity);
        cards.put("varianceCoeff", varianceCoeff);

        result.put("cards", cards);
        result.put("trendData", trendData);
        result.put("tableData", tableData);

        return result;
    }

    @Override
    public List<Map<String, Object>> exportData(String targetCode, String timeUnit, String startTime, String endTime, String energyType) {
        log.info("导出对标数据 - targetCode: {}, timeUnit: {}", targetCode, timeUnit);

        if ("day".equals(timeUnit)) {
            return queryDayData(targetCode, startTime, endTime, energyType);
        } else if ("month".equals(timeUnit)) {
            return queryMonthData(targetCode, startTime, endTime, energyType);
        } else if ("year".equals(timeUnit)) {
            return queryYearData(targetCode, startTime, endTime, energyType);
        }

        return new ArrayList<>();
    }

    private List<Map<String, Object>> queryDayData(String targetCode, String startTime, String endTime, String energyType) {
        LambdaQueryWrapper<BenchmarkResultDay> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(targetCode)) {
            wrapper.eq(BenchmarkResultDay::getTargetCode, targetCode);
        }
        if (StringUtils.isNotBlank(energyType)) {
            wrapper.eq(BenchmarkResultDay::getEnergyType, energyType);
        }
        wrapper.orderByAsc(BenchmarkResultDay::getStatDate);

        List<BenchmarkResultDay> list = resultDayMapper.selectList(wrapper);

        return list.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("targetCode", d.getTargetCode());
            map.put("targetName", d.getTargetName());
            map.put("time", d.getStatDate());
            map.put("energyConsumption", d.getEnergyConsumption());
            map.put("productionOutput", d.getProductionOutput());
            map.put("energyIntensity", d.getEnergyIntensity());
            map.put("ranking", d.getRanking());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryMonthData(String targetCode, String startTime, String endTime, String energyType) {
        LambdaQueryWrapper<BenchmarkResultMonth> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(targetCode)) {
            wrapper.eq(BenchmarkResultMonth::getTargetCode, targetCode);
        }
        if (StringUtils.isNotBlank(energyType)) {
            wrapper.eq(BenchmarkResultMonth::getEnergyType, energyType);
        }
        wrapper.orderByAsc(BenchmarkResultMonth::getStatYear)
               .orderByAsc(BenchmarkResultMonth::getStatMonth);

        List<BenchmarkResultMonth> list = resultMonthMapper.selectList(wrapper);

        return list.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("targetCode", d.getTargetCode());
            map.put("targetName", d.getTargetName());
            map.put("time", d.getStatYear() + "-" + String.format("%02d", d.getStatMonth()));
            map.put("energyConsumption", d.getEnergyConsumption());
            map.put("productionOutput", d.getProductionOutput());
            map.put("energyIntensity", d.getEnergyIntensity());
            map.put("ranking", d.getRanking());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryYearData(String targetCode, String startTime, String endTime, String energyType) {
        LambdaQueryWrapper<BenchmarkResultYear> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(targetCode)) {
            wrapper.eq(BenchmarkResultYear::getTargetCode, targetCode);
        }
        if (StringUtils.isNotBlank(energyType)) {
            wrapper.eq(BenchmarkResultYear::getEnergyType, energyType);
        }
        wrapper.orderByAsc(BenchmarkResultYear::getStatYear);

        List<BenchmarkResultYear> list = resultYearMapper.selectList(wrapper);

        return list.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("targetCode", d.getTargetCode());
            map.put("targetName", d.getTargetName());
            map.put("time", String.valueOf(d.getStatYear()));
            map.put("energyConsumption", d.getEnergyConsumption());
            map.put("productionOutput", d.getProductionOutput());
            map.put("energyIntensity", d.getEnergyIntensity());
            map.put("ranking", d.getRanking());
            return map;
        }).collect(Collectors.toList());
    }
}
