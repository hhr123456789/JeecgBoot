package org.jeecg.modules.energy.monitor;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 企业分类分区统计监控和健康检查类
 * 提供系统状态监控、健康检查和性能指标收集
 * @author jeecg
 */
@Slf4j
@Component
public class EnergyClassificationMonitor {

    @Autowired
    private IEnergyClassificationService energyClassificationService;

    // 监控状态
    private volatile boolean isSystemHealthy = true;
    private volatile String lastHealthCheckTime;
    private volatile String lastSyncStatus = "UNKNOWN";
    private volatile int syncSuccessCount = 0;
    private volatile int syncFailureCount = 0;
    private volatile long lastExecutionTime = 0;
    private volatile Date lastSyncTime;
    private volatile String errorMessage;

    /**
     * 每分钟执行一次健康检查
     */
    @Scheduled(cron = "0 * * * * ?")
    public void performHealthCheck() {
        log.debug("开始执行系统健康检查...");
        
        try {
            Date now = new Date();
            lastHealthCheckTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 检查同步任务状态
            Map<String, Object> syncStatus = energyClassificationService.getSyncTaskStatus();
            String taskStatus = (String) syncStatus.get("taskStatus");
            
            // 检查数据完整性
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            Date checkStartDate = calendar.getTime();
            Date checkEndDate = now;
            
            Map<String, Object> completenessResult = energyClassificationService
                .validateDataCompleteness(checkStartDate, checkEndDate);
            
            // 综合健康检查结果
            boolean databaseHealthy = isDatabaseHealthy();
            boolean dataConsistent = isDataConsistent(completenessResult);
            boolean syncFunctioning = isSyncFunctioning(syncStatus);
            
            // 更新健康状态
            isSystemHealthy = databaseHealthy && dataConsistent && syncFunctioning;
            lastSyncStatus = taskStatus;
            
            if (isSystemHealthy) {
                log.debug("系统健康检查通过");
            } else {
                log.warn("系统健康检查发现问题: 数据库={}, 数据一致性={}, 同步功能={}", 
                        databaseHealthy, dataConsistent, syncFunctioning);
            }
            
        } catch (Exception e) {
            log.error("健康检查执行失败", e);
            isSystemHealthy = false;
            errorMessage = e.getMessage();
        }
    }

    /**
     * 每10分钟收集一次性能指标
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void collectPerformanceMetrics() {
        log.debug("开始收集性能指标...");
        
        try {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, -1); // 最近1小时
            Date startTime = calendar.getTime();
            
            // 获取实时数据统计
            Map<String, Object> realTimeStats = energyClassificationService
                .getRealTimeDataStatistics(startTime, now);
            
            // 获取同步任务状态
            Map<String, Object> syncStatus = energyClassificationService.getSyncTaskStatus();
            
            // 记录性能指标
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("collectionTime", now);
            metrics.put("realTimeDataCount", realTimeStats.get("totalRecords"));
            metrics.put("syncTaskStatus", syncStatus.get("taskStatus"));
            metrics.put("systemHealthy", isSystemHealthy);
            metrics.put("syncSuccessRate", calculateSyncSuccessRate());
            metrics.put("averageExecutionTime", getAverageExecutionTime());
            
            log.info("性能指标收集完成: {}", metrics);
            
        } catch (Exception e) {
            log.error("性能指标收集失败", e);
        }
    }

    /**
     * 获取系统健康状态
     * @return 健康状态信息
     */
    public Map<String, Object> getSystemHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        healthStatus.put("isHealthy", isSystemHealthy);
        healthStatus.put("lastCheckTime", lastHealthCheckTime);
        healthStatus.put("syncStatus", lastSyncStatus);
        healthStatus.put("syncSuccessCount", syncSuccessCount);
        healthStatus.put("syncFailureCount", syncFailureCount);
        healthStatus.put("lastExecutionTime", lastExecutionTime);
        healthStatus.put("lastSyncTime", lastSyncTime);
        healthStatus.put("errorMessage", errorMessage);
        healthStatus.put("checkTime", new Date());
        
        return healthStatus;
    }

    /**
     * 获取系统性能指标
     * @return 性能指标信息
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Date now = new Date();
        
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, -1); // 最近1小时
            Date startTime = calendar.getTime();
            
            // 获取实时数据统计
            Map<String, Object> realTimeStats = energyClassificationService
                .getRealTimeDataStatistics(startTime, now);
            
            metrics.put("collectionTime", now);
            metrics.put("realTimeDataCount", realTimeStats.get("totalRecords"));
            metrics.put("energyTypeDistribution", realTimeStats.get("energyTypeCount"));
            metrics.put("orgCodeDistribution", realTimeStats.get("orgCodeCount"));
            metrics.put("systemHealthy", isSystemHealthy);
            metrics.put("syncSuccessRate", calculateSyncSuccessRate());
            metrics.put("averageExecutionTime", getAverageExecutionTime());
            metrics.put("uptime", getSystemUptime());
            
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            metrics.put("error", e.getMessage());
            metrics.put("collectionTime", now);
        }
        
        return metrics;
    }

    /**
     * 获取同步任务详情
     * @return 同步任务详情
     */
    public Map<String, Object> getSyncTaskDetails() {
        Map<String, Object> taskDetails = new HashMap<>();
        
        try {
            // 获取同步状态
            Map<String, Object> syncStatus = energyClassificationService.getSyncTaskStatus();
            
            // 获取最近的数据完整性检查结果
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date checkStartDate = calendar.getTime();
            Date checkEndDate = now;
            
            Map<String, Object> completenessResult = energyClassificationService
                .validateDataCompleteness(checkStartDate, checkEndDate);
            
            taskDetails.put("taskStatus", syncStatus);
            taskDetails.put("dataCompleteness", completenessResult);
            taskDetails.put("lastSyncTime", lastSyncTime);
            taskDetails.put("nextScheduledSync", getNextScheduledSyncTime());
            taskDetails.put("successRate", calculateSyncSuccessRate());
            taskDetails.put("averageExecutionTime", getAverageExecutionTime());
            taskDetails.put("updateTime", new Date());
            
        } catch (Exception e) {
            log.error("获取同步任务详情失败", e);
            taskDetails.put("error", e.getMessage());
        }
        
        return taskDetails;
    }

    /**
     * 执行系统诊断
     * @return 诊断结果
     */
    public Map<String, Object> performSystemDiagnosis() {
        log.info("开始执行系统诊断...");
        
        Map<String, Object> diagnosis = new HashMap<>();
        diagnosis.put("startTime", new Date());
        
        try {
            // 1. 检查数据库连接
            Map<String, Object> dbCheck = checkDatabaseConnection();
            diagnosis.put("databaseCheck", dbCheck);
            
            // 2. 检查数据完整性
            Map<String, Object> dataCheck = checkDataIntegrity();
            diagnosis.put("dataIntegrityCheck", dataCheck);
            
            // 3. 检查同步功能
            Map<String, Object> syncCheck = checkSyncFunctionality();
            diagnosis.put("syncFunctionalityCheck", syncCheck);
            
            // 4. 检查性能指标
            Map<String, Object> perfCheck = checkPerformanceMetrics();
            diagnosis.put("performanceCheck", perfCheck);
            
            // 5. 生成诊断报告
            String overallStatus = generateDiagnosisReport(diagnosis);
            diagnosis.put("overallStatus", overallStatus);
            diagnosis.put("endTime", new Date());
            diagnosis.put("duration", ((Date) diagnosis.get("endTime")).getTime() - 
                             ((Date) diagnosis.get("startTime")).getTime());
            
            log.info("系统诊断完成: 状态={}", overallStatus);
            
        } catch (Exception e) {
            log.error("系统诊断执行失败", e);
            diagnosis.put("overallStatus", "ERROR");
            diagnosis.put("error", e.getMessage());
            diagnosis.put("endTime", new Date());
        }
        
        return diagnosis;
    }

    /**
     * 重置监控统计数据
     * @return 重置结果
     */
    public Map<String, Object> resetMonitoringStatistics() {
        log.info("重置监控统计数据");
        
        syncSuccessCount = 0;
        syncFailureCount = 0;
        lastExecutionTime = 0;
        lastSyncTime = null;
        errorMessage = null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("message", "监控统计数据已重置");
        result.put("resetTime", new Date());
        
        return result;
    }

    /**
     * 更新同步成功计数
     * @param success 是否成功
     * @param executionTime 执行时间(毫秒)
     */
    public void updateSyncStatistics(boolean success, long executionTime) {
        if (success) {
            syncSuccessCount++;
        } else {
            syncFailureCount++;
        }
        
        lastExecutionTime = executionTime;
        lastSyncTime = new Date();
    }

    // ==================== 私有辅助方法 ====================

    private boolean isDatabaseHealthy() {
        try {
            // 简单的数据库健康检查
            Date latestDate = energyClassificationService.getLatestStatisticsDate();
            return latestDate != null;
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return false;
        }
    }

    private boolean isDataConsistent(Map<String, Object> completenessResult) {
        try {
            String status = (String) completenessResult.get("status");
            if (!"SUCCESS".equals(status)) {
                return false;
            }
            
            boolean isComplete = (Boolean) completenessResult.get("isComplete");
            return isComplete;
        } catch (Exception e) {
            log.error("数据一致性检查失败", e);
            return false;
        }
    }

    private boolean isSyncFunctioning(Map<String, Object> syncStatus) {
        try {
            String status = (String) syncStatus.get("taskStatus");
            return "RUNNING".equals(status) || "READY".equals(status);
        } catch (Exception e) {
            log.error("同步功能检查失败", e);
            return false;
        }
    }

    private double calculateSyncSuccessRate() {
        int total = syncSuccessCount + syncFailureCount;
        if (total == 0) {
            return 100.0;
        }
        return (double) syncSuccessCount / total * 100;
    }

    private long getAverageExecutionTime() {
        return lastExecutionTime;
    }

    private String getSystemUptime() {
        // 简单的系统运行时间计算
        return "运行正常";
    }

    private Date getNextScheduledSyncTime() {
        // 计算下次定时同步时间（每5分钟）
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        int nextMinute = ((minute / 5) + 1) * 5;
        
        if (nextMinute >= 60) {
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.set(Calendar.MINUTE, nextMinute);
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        return calendar.getTime();
    }

    private Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Date startTime = new Date();
            Date latestDate = energyClassificationService.getLatestStatisticsDate();
            Date endTime = new Date();
            
            long responseTime = endTime.getTime() - startTime.getTime();
            
            result.put("status", "HEALTHY");
            result.put("responseTime", responseTime);
            result.put("latestDataDate", latestDate);
            result.put("checkTime", endTime);
            
        } catch (Exception e) {
            result.put("status", "UNHEALTHY");
            result.put("error", e.getMessage());
            result.put("checkTime", new Date());
        }
        
        return result;
    }

    private Map<String, Object> checkDataIntegrity() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            Date startDate = calendar.getTime();
            Date endDate = now;
            
            Map<String, Object> completenessResult = energyClassificationService
                .validateDataCompleteness(startDate, endDate);
            
            result.put("status", completenessResult.get("status"));
            result.put("isComplete", completenessResult.get("isComplete"));
            result.put("realTimeRecordCount", completenessResult.get("realTimeRecordCount"));
            result.put("summaryRecordCount", completenessResult.get("summaryRecordCount"));
            result.put("checkTime", now);
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("checkTime", new Date());
        }
        
        return result;
    }

    private Map<String, Object> checkSyncFunctionality() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> syncStatus = energyClassificationService.getSyncTaskStatus();
            
            result.put("status", syncStatus.get("taskStatus"));
            result.put("isPaused", syncStatus.get("isPaused"));
            result.put("lastSyncTime", syncStatus.get("lastSyncTime"));
            result.put("successRate", calculateSyncSuccessRate());
            result.put("checkTime", new Date());
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("checkTime", new Date());
        }
        
        return result;
    }

    private Map<String, Object> checkPerformanceMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Date now = new Date();
            Calendar startTimeCalendar = Calendar.getInstance();
            startTimeCalendar.setTime(now);
            startTimeCalendar.add(Calendar.HOUR, -1);
            Date startTime = startTimeCalendar.getTime();
            
            Map<String, Object> metrics = energyClassificationService
                .getRealTimeDataStatistics(startTime, now);
            
            result.put("realTimeDataCount", metrics.get("totalRecords"));
            result.put("averageExecutionTime", getAverageExecutionTime());
            result.put("systemHealthy", isSystemHealthy);
            result.put("checkTime", now);
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("checkTime", new Date());
        }
        
        return result;
    }

    private String generateDiagnosisReport(Map<String, Object> diagnosis) {
        try {
            Object dbCheckObj = diagnosis.get("databaseCheck");
            Object dataCheckObj = diagnosis.get("dataIntegrityCheck");
            Object syncCheckObj = diagnosis.get("syncFunctionalityCheck");
            Object perfCheckObj = diagnosis.get("performanceCheck");
            
            // 类型安全检查
            if (!(dbCheckObj instanceof Map) || !(dataCheckObj instanceof Map) || 
                !(syncCheckObj instanceof Map) || !(perfCheckObj instanceof Map)) {
                log.warn("诊断数据格式不正确");
                return "ERROR";
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> dbCheck = (Map<String, Object>) dbCheckObj;
            @SuppressWarnings("unchecked")
            Map<String, Object> dataCheck = (Map<String, Object>) dataCheckObj;
            @SuppressWarnings("unchecked")
            Map<String, Object> syncCheck = (Map<String, Object>) syncCheckObj;
            @SuppressWarnings("unchecked")
            Map<String, Object> perfCheck = (Map<String, Object>) perfCheckObj;
            
            boolean dbHealthy = "HEALTHY".equals(dbCheck.get("status"));
            Object isCompleteObj = dataCheck.get("isComplete");
            boolean dataHealthy = "SUCCESS".equals(dataCheck.get("status")) && 
                                 (isCompleteObj instanceof Boolean) && (Boolean) isCompleteObj;
            boolean syncHealthy = "RUNNING".equals(syncCheck.get("status")) || 
                                 "READY".equals(syncCheck.get("status"));
            Object perfStatus = perfCheck.get("status");
            boolean perfHealthy = perfStatus == null || 
                                 !"ERROR".equals(perfStatus);
            
            if (dbHealthy && dataHealthy && syncHealthy && perfHealthy) {
                return "HEALTHY";
            } else if (dbHealthy && (dataHealthy || syncHealthy)) {
                return "WARNING";
            } else {
                return "CRITICAL";
            }
            
        } catch (Exception e) {
            log.error("生成诊断报告失败", e);
            return "ERROR";
        }
    }
}