package org.jeecg.modules.energy.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 企业分类分区统计定时任务
 * 每5分钟执行一次，从实时表同步数据到分类分区统计表
 * 注意：此版本移除了自动历史数据清理功能
 * @author jeecg
 */
@Slf4j
@Component
public class EnergyClassificationSyncJob {

    @Autowired
    private IEnergyClassificationService energyClassificationService;

    /**
     * 每5分钟执行一次数据同步任务
     * 同步前一日和当日的数据
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncClassificationData() {
        log.info("开始执行企业分类分区统计定时同步任务...");
        
        try {
            // 获取当前时间
            Date now = new Date();
            log.info("定时任务执行时间: {}", now);
            
            // 同步前一日的数据
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date today = calendar.getTime();
            
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar.getTime();
            
            syncDateData(yesterday, "前一日");
            syncDateData(today, "当日");
            
            log.info("企业分类分区统计定时同步任务执行完成");
            
        } catch (Exception e) {
            log.error("企业分类分区统计定时同步任务执行失败", e);
        }
    }

    /**
     * 每天凌晨1点执行全量数据同步
     * 确保所有历史数据都得到正确同步
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fullDataSync() {
        log.info("开始执行企业分类分区统计全量数据同步任务...");
        
        try {
            Date now = new Date();
            
            // 获取最新统计日期
            Date latestStatisticsDate = energyClassificationService.getLatestStatisticsDate();
            
            Date startDate;
            if (latestStatisticsDate == null) {
                // 如果没有统计数据，从上个月开始同步
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                log.info("未找到历史统计数据，从 {} 开始全量同步", startDate);
            } else {
                // 从最新统计日期的次日开始同步
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(latestStatisticsDate);
                startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                startDate = startCalendar.getTime();
                log.info("从最新统计日期 {} 的次日开始同步", startDate);
            }
            
            Date endDate = now;
            
            // 如果有数据需要同步
            if (startDate.before(endDate)) {
                log.info("开始全量同步数据: startDate={}, endDate={}", startDate, endDate);
                
                Map<String, Object> result = energyClassificationService.triggerDataSync(startDate, endDate);
                
                String status = (String) result.get("status");
                if ("SUCCESS".equals(status)) {
                    int successCount = (Integer) result.get("successCount");
                    int failCount = (Integer) result.get("failCount");
                    int totalRecords = (Integer) result.get("totalRecords");
                    
                    log.info("全量数据同步完成: 成功={}, 失败={}, 总数={}", successCount, failCount, totalRecords);
                } else {
                    log.error("全量数据同步失败: {}", result);
                }
            } else {
                log.info("无需进行全量数据同步");
            }
            
        } catch (Exception e) {
            log.error("企业分类分区统计全量数据同步任务执行失败", e);
        }
    }

    /**
     * 每周日凌晨2点执行数据验证任务
     * 检查数据完整性和一致性
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void validateDataConsistency() {
        log.info("开始执行企业分类分区统计数据一致性验证任务...");
        
        try {
            Date now = new Date();
            
            // 验证最近一周的数据
            Calendar validationCalendar = Calendar.getInstance();
            validationCalendar.setTime(now);
            validationCalendar.set(Calendar.HOUR_OF_DAY, 0);
            validationCalendar.set(Calendar.MINUTE, 0);
            validationCalendar.set(Calendar.SECOND, 0);
            validationCalendar.set(Calendar.MILLISECOND, 0);
            validationCalendar.add(Calendar.DAY_OF_MONTH, -7);
            Date startDate = validationCalendar.getTime();
            Date endDate = now;
            
            log.info("验证数据一致性: startDate={}, endDate={}", startDate, endDate);
            
            Map<String, Object> validationResult = energyClassificationService
                .validateDataCompleteness(startDate, endDate);
            
            String status = (String) validationResult.get("status");
            if ("SUCCESS".equals(status)) {
                boolean isComplete = (Boolean) validationResult.get("isComplete");
                long realTimeRecordCount = (Long) validationResult.get("realTimeRecordCount");
                long summaryRecordCount = (Long) validationResult.get("summaryRecordCount");
                
                log.info("数据一致性验证完成: 完整={}, 实时数据={}, 汇总数据={}", 
                        isComplete, realTimeRecordCount, summaryRecordCount);
                
                if (!isComplete) {
                    log.warn("发现数据不一致，执行自动修复...");
                    
                    // 执行增量同步修复数据
                    Map<String, Object> repairResult = energyClassificationService
                        .incrementalSync(startDate, endDate);
                    
                    String repairStatus = (String) repairResult.get("status");
                    if ("COMPLETED".equals(repairStatus)) {
                        int successCount = (Integer) repairResult.get("successCount");
                        int failCount = (Integer) repairResult.get("failCount");
                        log.info("数据修复完成: 成功={}, 失败={}", successCount, failCount);
                    }
                }
            } else {
                log.error("数据一致性验证失败: {}", validationResult);
            }
            
        } catch (Exception e) {
            log.error("企业分类分区统计数据一致性验证任务执行失败", e);
        }
    }

    /**
     * 注意：此版本移除了自动历史数据清理任务
     * 如需清理历史数据，请手动调用 energyClassificationService.cleanupOldData() 方法
     * 或在数据库中直接执行 DELETE 语句
     */

    /**
     * 同步指定日期的数据
     * @param targetDate 目标日期
     * @param dateDesc 日期描述
     */
    private void syncDateData(Date targetDate, String dateDesc) {
        try {
            log.info("========================================");
            log.info("开始同步{}的数据", dateDesc);
            log.info("目标日期: {}", targetDate);
            log.info("========================================");

            // 检查是否存在未同步的数据
            log.info("步骤1: 检查是否存在未同步的数据...");
            boolean hasUnsyncedData = energyClassificationService.hasUnsyncedData(targetDate);
            log.info("检查结果: {}", hasUnsyncedData ? "存在未同步数据" : "数据已是最新");

            if (hasUnsyncedData) {
                log.info("步骤2: 发现{}存在未同步数据，开始执行同步...", dateDesc);
                log.info("同步日期: {}", targetDate);

                Map<String, Object> result = energyClassificationService.syncByDate(targetDate);

                log.info("步骤3: 同步执行完成，分析结果...");
                log.info("同步结果详情: {}", result);

                String status = (String) result.get("status");
                log.info("同步状态: {}", status);

                if ("SUCCESS".equals(status)) {
                    Integer successCount = (Integer) result.get("successCount");
                    Integer failCount = (Integer) result.get("failCount");
                    Integer totalRecords = (Integer) result.get("totalRecords");

                    log.info("========================================");
                    log.info("{}数据同步成功!", dateDesc);
                    log.info("成功记录数: {}", successCount);
                    log.info("失败记录数: {}", failCount);
                    log.info("总记录数: {}", totalRecords);
                    log.info("========================================");

                    if (failCount != null && failCount > 0) {
                        log.warn("警告: 有 {} 条记录同步失败，请检查日志", failCount);
                    }
                } else if ("NO_DATA".equals(status)) {
                    log.warn("========================================");
                    log.warn("警告: {}未查询到待同步的数据", dateDesc);
                    log.warn("可能原因:");
                    log.warn("1. tb_ep_equ_energy_daycount 表中没有该日期的数据");
                    log.warn("2. tb_module 表中仪表未启用 (isaction != 'Y')");
                    log.warn("3. tb_module 表中仪表缺少 sys_org_code 或 energy_type");
                    log.warn("建议: 执行 db/check_data.sql 诊断数据问题");
                    log.warn("========================================");
                } else {
                    log.error("========================================");
                    log.error("错误: {}数据同步失败", dateDesc);
                    log.error("失败原因: {}", result.get("errorMessage"));
                    log.error("完整结果: {}", result);
                    log.error("========================================");
                }
            } else {
                log.info("========================================");
                log.info("{}数据已是最新，无需同步", dateDesc);
                log.info("========================================");
            }

        } catch (Exception e) {
            log.error("========================================");
            log.error("异常: 同步{}数据时发生异常", dateDesc);
            log.error("目标日期: {}", targetDate);
            log.error("异常信息: {}", e.getMessage());
            log.error("异常堆栈:", e);
            log.error("========================================");
        }
    }
}