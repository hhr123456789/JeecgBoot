package org.jeecg.modules.energy.service.classification;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 企业分类分区统计同步服务接口
 * 专门用于定时任务中的数据聚合和同步操作
 * @author jeecg
 */
public interface IEnergyClassificationSyncService {

    /**
     * 按部门编码+能源类型+时间维度聚合统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<Map<String, Object>> aggregateStatisticsByOrgAndEnergyType(Date startDate, Date endDate);

    /**
     * 同步指定时间范围内的数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果统计
     */
    Map<String, Object> syncClassificationData(Date startDate, Date endDate);

    /**
     * 按日期同步企业分类分区统计数据
     * @param targetDate 目标日期
     * @return 同步结果
     */
    Map<String, Object> syncByDate(Date targetDate);

    /**
     * 按月份同步企业分类分区统计数据
     * @param year 年份
     * @param month 月份
     * @return 同步结果
     */
    Map<String, Object> syncByMonth(Integer year, Integer month);

    /**
     * 按年份同步企业分类分区统计数据
     * @param year 年份
     * @return 同步结果
     */
    Map<String, Object> syncByYear(Integer year);

    /**
     * 清理指定时间范围的历史数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 清理结果
     */
    Map<String, Object> cleanupOldData(Date startDate, Date endDate);

    /**
     * 获取最新统计数据的日期
     * @return 最新统计日期
     */
    Date getLatestStatisticsDate();

    /**
     * 验证数据完整性
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 验证结果
     */
    Map<String, Object> validateDataCompleteness(Date startDate, Date endDate);

    /**
     * 重新计算指定时间范围内的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 重算结果
     */
    Map<String, Object> recalculateStatistics(Date startDate, Date endDate);

    /**
     * 获取实时表数据统计信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getRealTimeDataStatistics(Date startDate, Date endDate);

    /**
     * 检查是否存在未同步的数据
     * @param targetDate 目标日期
     * @return true:存在未同步数据 false:无未同步数据
     */
    boolean hasUnsyncedData(Date targetDate);

    /**
     * 增量同步指定时间范围内的数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果
     */
    Map<String, Object> incrementalSync(Date startDate, Date endDate);

    /**
     * 同步所有未同步的数据
     * @return 同步结果
     */
    Map<String, Object> syncAllUnsyncedData();
}