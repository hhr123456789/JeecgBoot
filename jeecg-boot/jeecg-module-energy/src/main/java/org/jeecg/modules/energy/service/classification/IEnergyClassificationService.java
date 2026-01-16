package org.jeecg.modules.energy.service.classification;

import org.jeecg.modules.energy.vo.classification.ClassificationQueryParam;
import org.jeecg.modules.energy.vo.classification.ClassificationSummaryVO;
import org.jeecg.modules.energy.vo.classification.ComparisonDataVO;
import org.jeecg.modules.energy.vo.classification.EnergyTypeVO;
import org.jeecg.modules.energy.vo.classification.OrgTreeVO;
import org.jeecg.modules.energy.vo.classification.TrendDataVO;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 企业分类分区统计服务接口
 * @author jeecg
 */
public interface IEnergyClassificationService {
    
    /**
     * 获取部门树形结构
     * @return 部门树列表
     */
    List<OrgTreeVO> getOrgTree();
    
    /**
     * 获取能源类型列表(从tb_energy_ratio_info表查询)
     * @return 能源类型列表
     */
    List<EnergyTypeVO> getEnergyTypes();
    
    /**
     * 根据orgCode获取该部门下设备的能源类型列表
     * @param orgCode 部门编码
     * @return 该部门下设备的能源类型列表
     */
    List<EnergyTypeVO> getEnergyTypesByOrgCode(String orgCode);
    
    /**
     * 查询分类分区统计汇总数据
     * @param param 查询参数
     * @return 统计汇总数据
     */
    ClassificationSummaryVO getSummaryData(ClassificationQueryParam param);
    
    /**
     * 查询趋势对比数据
     * @param param 查询参数
     * @return 趋势数据
     */
    TrendDataVO getTrendData(ClassificationQueryParam param);
    
    /**
     * 导出分类分区统计数据
     * @param param 查询参数
     * @param response HTTP响应
     */
    void exportData(ClassificationQueryParam param, HttpServletResponse response);

    // ==================== 定时任务相关方法 ====================

    /**
     * 手动触发数据同步任务
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果
     */
    Map<String, Object> triggerDataSync(Date startDate, Date endDate);

    /**
     * 按日期手动同步数据
     * @param targetDate 目标日期
     * @return 同步结果
     */
    Map<String, Object> syncByDate(Date targetDate);

    /**
     * 按月份手动同步数据
     * @param year 年份
     * @param month 月份
     * @return 同步结果
     */
    Map<String, Object> syncByMonth(Integer year, Integer month);

    /**
     * 按年份手动同步数据
     * @param year 年份
     * @return 同步结果
     */
    Map<String, Object> syncByYear(Integer year);

    /**
     * 增量同步未同步的数据
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

    /**
     * 重新计算指定时间范围内的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 重算结果
     */
    Map<String, Object> recalculateStatistics(Date startDate, Date endDate);

    /**
     * 清理指定时间范围内的历史数据
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
     * 获取同步任务状态信息
     * @return 任务状态信息
     */
    Map<String, Object> getSyncTaskStatus();

    /**
     * 暂停同步任务
     * @return 暂停结果
     */
    Map<String, Object> pauseSyncTask();

    /**
     * 恢复同步任务
     * @return 恢复结果
     */
    Map<String, Object> resumeSyncTask();

    /**
     * 获取数据同步进度
     * @param taskId 任务ID
     * @return 进度信息
     */
    Map<String, Object> getSyncProgress(String taskId);
    
    /**
     * 获取横向对比数据(部门或设备级别)
     * @param param 查询参数
     * @return 对比数据
     */
    ComparisonDataVO getComparisonData(ClassificationQueryParam param);

    /**
     * 获取汇总表调试数据
     * @param limit 返回记录数限制
     * @return 调试数据
     */
    Map<String, Object> getDebugSummaryData(Integer limit);
}