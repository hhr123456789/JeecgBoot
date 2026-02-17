package org.jeecg.modules.energy.service;

import java.util.List;
import java.util.Map;

/**
 * @Description: 产品能耗分析Service接口
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
public interface IProductEnergyService {

    /**
     * 获取统计数据
     * @param timeDimension 时间维度 (day/month/year)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param energyType 能源类型 (1:电 2:水 3:天然气 4:蒸汽 5:压缩空气)
     * @param categoryId 产品分类ID
     * @return 统计数据
     */
    Map<String, Object> getStatistics(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId);

    /**
     * 获取产品能耗分布数据（饼图）
     * @param timeDimension 时间维度
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param energyType 能源类型
     * @param categoryId 产品分类ID
     * @return 分布数据
     */
    Map<String, Object> getDistribution(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId);

    /**
     * 获取产品单耗趋势数据（折线图）
     * @param productCodes 产品编码列表
     * @param timeDimension 时间维度
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param energyType 能源类型
     * @param categoryId 产品分类ID
     * @return 趋势数据
     */
    Map<String, Object> getTrend(List<String> productCodes, String timeDimension, String startDate, String endDate, Integer energyType, String categoryId);

    /**
     * 获取产量与能耗对比数据（柱状图）
     * @param timeDimension 时间维度
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param energyType 能源类型
     * @param categoryId 产品分类ID
     * @return 对比数据
     */
    Map<String, Object> getComparison(String timeDimension, String startDate, String endDate, Integer energyType, String categoryId);

    /**
     * 获取产品单耗排名数据（横向柱状图）
     * @param timeDimension 时间维度
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param order 排序方式 (asc/desc)
     * @param energyType 能源类型
     * @param categoryId 产品分类ID
     * @return 排名数据
     */
    Map<String, Object> getRanking(String timeDimension, String startDate, String endDate, String order, Integer energyType, String categoryId);

    /**
     * 获取明细列表数据（表格）
     * @param timeDimension 时间维度
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param energyType 能源类型
     * @param categoryId 产品分类ID
     * @return 明细数据
     */
    Map<String, Object> getDetailList(String timeDimension, String startDate, String endDate, Integer pageNo, Integer pageSize, Integer energyType, String categoryId);

    /**
     * 获取产品分类树（左侧树）
     * @return 树形数据
     */
    List<Map<String, Object>> getCategoryTree();
}
