package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.product.TbProductCategory;
import org.jeecg.modules.energy.entity.product.TbProductEnergyConsumption;
import org.jeecg.modules.energy.mapper.product.TbProductCategoryMapper;
import org.jeecg.modules.energy.mapper.product.TbProductEnergyConsumptionMapper;
import org.jeecg.modules.energy.service.IProductEnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 产品能耗分析Service实现
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Slf4j
@Service
public class ProductEnergyServiceImpl implements IProductEnergyService {

    @Autowired
    private TbProductEnergyConsumptionMapper productEnergyMapper;

    @Autowired
    private TbProductCategoryMapper productCategoryMapper;

    @Override
    public Map<String, Object> getStatistics(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId) {
        try {
            Map<String, Object> result = productEnergyMapper.getStatistics(timeDimension, startDate, endDate, energyType, categoryId);
            if (result == null) {
                result = new HashMap<>();
                result.put("totalConsumption", 0);
                result.put("totalProduction", 0);
                result.put("qualifiedProduction", 0);
                result.put("qualificationRate", 0);
                result.put("unitConsumption", 0);
                result.put("energyUnit", getEnergyUnit(energyType));
            }
            return result;
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            throw new RuntimeException("获取统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDistribution(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId) {
        try {
            List<Map<String, Object>> data = productEnergyMapper.getDistribution(timeDimension, startDate, endDate, energyType, categoryId);

            Map<String, Object> result = new HashMap<>();
            result.put("series", Arrays.asList(
                new HashMap<String, Object>() {{
                    put("name", "产品能耗分布");
                    put("type", "pie");
                    put("radius", Arrays.asList("50%", "70%"));
                    put("data", data);
                }}
            ));

            return result;
        } catch (Exception e) {
            log.error("获取能耗分布数据失败", e);
            throw new RuntimeException("获取能耗分布数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTrend(List<String> productCodes, String timeDimension, String startDate, String endDate, Integer energyType, String categoryId) {
        try {
            // 如果没有指定产品编码，根据分类ID查询产品编码
            if (productCodes == null || productCodes.isEmpty()) {
                productCodes = productEnergyMapper.getProductCodesByCategory(categoryId);
                if (productCodes == null || productCodes.isEmpty()) {
                    // 如果还是没有，返回空数据
                    Map<String, Object> emptyResult = new HashMap<>();
                    emptyResult.put("xAxis", new HashMap<String, Object>() {{
                        put("type", "category");
                        put("data", new ArrayList<>());
                    }});
                    emptyResult.put("series", new ArrayList<>());
                    return emptyResult;
                }
            }

            // 查询趋势数据
            QueryWrapper<TbProductEnergyConsumption> qw = new QueryWrapper<>();
            qw.in("product_code", productCodes);
            qw.eq("time_dimension", timeDimension);
            qw.between("stat_date", startDate, endDate);
            if (energyType != null) {
                qw.eq("energy_type", energyType);
            }
            qw.orderByAsc("stat_date");

            List<TbProductEnergyConsumption> list = productEnergyMapper.selectList(qw);

            // 按产品分组
            Map<String, List<TbProductEnergyConsumption>> groupedByProduct = list.stream()
                .collect(Collectors.groupingBy(TbProductEnergyConsumption::getProductCode));

            // 提取时间轴
            List<String> xAxisData = list.stream()
                .map(item -> {
                    if ("month".equals(timeDimension)) {
                        return item.getStatMonth();
                    } else if ("year".equals(timeDimension)) {
                        return item.getStatYear();
                    } else {
                        return item.getStatDate().toString();
                    }
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            // 构建series数据
            List<Map<String, Object>> seriesList = new ArrayList<>();
            for (String productCode : productCodes) {
                List<TbProductEnergyConsumption> productData = groupedByProduct.get(productCode);
                if (productData != null && !productData.isEmpty()) {
                    Map<String, Object> series = new HashMap<>();
                    series.put("name", productData.get(0).getProductCode());
                    series.put("type", "line");
                    series.put("data", productData.stream()
                        .map(TbProductEnergyConsumption::getUnitConsumption)
                        .collect(Collectors.toList()));
                    seriesList.add(series);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("xAxis", new HashMap<String, Object>() {{
                put("type", "category");
                put("data", xAxisData);
            }});
            result.put("series", seriesList);

            return result;
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            throw new RuntimeException("获取趋势数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getComparison(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId) {
        try {
            List<Map<String, Object>> data = productEnergyMapper.getComparison(timeDimension, startDate, endDate, energyType, categoryId);

            List<String> productNames = new ArrayList<>();
            List<Object> productionData = new ArrayList<>();
            List<Object> consumptionData = new ArrayList<>();
            String energyUnit = getEnergyUnit(energyType);

            for (Map<String, Object> item : data) {
                productNames.add((String) item.get("name"));
                productionData.add(item.get("production"));
                // 能耗转换
                BigDecimal consumption = (BigDecimal) item.get("consumption");
                if (consumption != null) {
                    // 电能耗转换为万kWh，其他保持原值
                    if (energyType == null || energyType == 1) {
                        consumptionData.add(consumption.divide(new BigDecimal("10000"), 2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        consumptionData.add(consumption);
                    }
                } else {
                    consumptionData.add(BigDecimal.ZERO);
                }
            }

            String consumptionLabel = energyType == null || energyType == 1 ? "能耗(万kWh)" : "能耗(" + energyUnit + ")";

            Map<String, Object> result = new HashMap<>();
            result.put("xAxis", new HashMap<String, Object>() {{
                put("type", "category");
                put("data", productNames);
            }});
            result.put("series", Arrays.asList(
                new HashMap<String, Object>() {{
                    put("name", "产量(件)");
                    put("type", "bar");
                    put("yAxisIndex", 0);
                    put("data", productionData);
                }},
                new HashMap<String, Object>() {{
                    put("name", consumptionLabel);
                    put("type", "bar");
                    put("yAxisIndex", 1);
                    put("data", consumptionData);
                }}
            ));

            return result;
        } catch (Exception e) {
            log.error("获取对比数据失败", e);
            throw new RuntimeException("获取对比数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRanking(String timeDimension, String startDate, String endDate, String order, Integer energyType, String categoryId) {
        try {
            List<Map<String, Object>> data = productEnergyMapper.getRanking(timeDimension, startDate, endDate, energyType, categoryId);

            if ("desc".equals(order)) {
                Collections.reverse(data);
            }

            List<String> productNames = data.stream()
                .map(item -> (String) item.get("name"))
                .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("yAxis", new HashMap<String, Object>() {{
                put("type", "category");
                put("data", productNames);
            }});
            result.put("series", Arrays.asList(
                new HashMap<String, Object>() {{
                    put("name", "单位产品能耗");
                    put("type", "bar");
                    put("data", data.stream()
                        .map(item -> item.get("value"))
                        .collect(Collectors.toList()));
                }}
            ));

            return result;
        } catch (Exception e) {
            log.error("获取排名数据失败", e);
            throw new RuntimeException("获取排名数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDetailList(String timeDimension, String startDate, String endDate, Integer pageNo, Integer pageSize, Integer energyType, String categoryId) {
        try {
            List<Map<String, Object>> list = productEnergyMapper.getDetailList(timeDimension, startDate, endDate, energyType, categoryId);

            // 简单分页
            int total = list.size();
            int fromIndex = Math.min((pageNo - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<Map<String, Object>> records = fromIndex < total ? list.subList(fromIndex, toIndex) : new ArrayList<>();

            // 添加key字段
            for (int i = 0; i < records.size(); i++) {
                records.get(i).put("key", String.valueOf(fromIndex + i + 1));
                records.get(i).put("chainRatio", 0); // 环比暂时为0
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);

            return result;
        } catch (Exception e) {
            log.error("获取明细列表失败", e);
            throw new RuntimeException("获取明细列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据能源类型获取单位
     */
    private String getEnergyUnit(Integer energyType) {
        if (energyType == null) return "kWh";
        switch (energyType) {
            case 1: return "kWh";
            case 2: return "m³";
            case 3: return "m³";
            case 4: return "t";
            case 5: return "m³";
            default: return "kWh";
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        try {
            List<Map<String, Object>> allCategories = productCategoryMapper.getCategoryTree();
            return buildTree(allCategories, null);
        } catch (Exception e) {
            log.error("获取产品分类树失败", e);
            throw new RuntimeException("获取产品分类树失败: " + e.getMessage());
        }
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<Map<String, Object>> allCategories, String parentId) {
        return allCategories.stream()
            .filter(item -> Objects.equals(item.get("parentId"), parentId))
            .map(item -> {
                Map<String, Object> node = new HashMap<>(item);
                List<Map<String, Object>> children = buildTree(allCategories, (String) item.get("id"));
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                return node;
            })
            .collect(Collectors.toList());
    }
}
