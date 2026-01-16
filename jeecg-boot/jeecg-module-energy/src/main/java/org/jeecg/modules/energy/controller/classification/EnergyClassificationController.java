package org.jeecg.modules.energy.controller.classification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationSyncService;
import org.jeecg.modules.energy.vo.classification.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 企业分类分区统计控制器
 * @author jeecg
 */
@RestController
@RequestMapping("/energy/classification")
@Api(tags = "企业分类分区统计")
@Slf4j
@Validated
public class EnergyClassificationController {
    
    @Autowired
    private IEnergyClassificationService energyClassificationService;

    @Autowired
    private IEnergyClassificationSyncService syncService;

    /**
     * 获取部门树形结构
     */
    @ApiOperation(value = "获取部门树形结构", notes = "获取企业的部门树形结构数据")
    @GetMapping("/getOrgTree")
    public Result<List<OrgTreeVO>> getOrgTree() {
        List<OrgTreeVO> treeData = energyClassificationService.getOrgTree();
        return Result.OK(treeData);
    }
    
    /**
     * 获取能源类型列表
     */
    @ApiOperation(value = "获取能源类型列表", notes = "获取启用的能源类型配置")
    @GetMapping("/getEnergyTypes")
    public Result<List<EnergyTypeVO>> getEnergyTypes() {
        List<EnergyTypeVO> energyTypes = energyClassificationService.getEnergyTypes();
        return Result.OK(energyTypes);
    }
    
    /**
     * 根据部门编码获取该部门下设备的能源类型列表
     */
    @ApiOperation(value = "根据部门获取能源类型", notes = "根据部门编码获取该部门下实际设备的能源类型")
    @GetMapping("/getEnergyTypesByOrgCode")
    public Result<List<EnergyTypeVO>> getEnergyTypesByOrgCode(@RequestParam("orgCode") String orgCode) {
        log.info("根据部门编码获取能源类型: {}", orgCode);
        List<EnergyTypeVO> energyTypes = energyClassificationService.getEnergyTypesByOrgCode(orgCode);
        return Result.OK(energyTypes);
    }
    
    /**
     * 查询分类分区统计汇总
     */
    @ApiOperation(value = "查询分类分区统计汇总", notes = "按条件查询分类分区统计汇总数据")
    @PostMapping("/getSummaryData")
    public Result<ClassificationSummaryVO> getSummaryData(@Valid @RequestBody ClassificationQueryParam param) {
        log.info("查询分类分区统计汇总: {}", param);
        ClassificationSummaryVO data = energyClassificationService.getSummaryData(param);
        return Result.OK(data);
    }
    
    /**
     * 查询趋势对比数据
     */
    @ApiOperation(value = "查询趋势对比数据", notes = "获取能源趋势对比数据")
    @PostMapping("/getTrendData")
    public Result<TrendDataVO> getTrendData(@Valid @RequestBody ClassificationQueryParam param) {
        log.info("查询趋势对比数据: {}", param);
        TrendDataVO data = energyClassificationService.getTrendData(param);
        return Result.OK(data);
    }
    
    /**
     * 导出分类分区统计数据
     */
    @ApiOperation(value = "导出分类分区统计数据", notes = "导出分类分区统计数据到Excel")
    @GetMapping("/exportData")
    public void exportData(ClassificationQueryParam param, HttpServletResponse response) {
        log.info("导出分类分区统计数据: {}", param);
        energyClassificationService.exportData(param, response);
    }
    
    /**
     * 获取横向对比数据
     */
    @ApiOperation(value = "获取横向对比数据", notes = "获取部门或设备级别的横向对比数据")
    @PostMapping("/getComparisonData")
    public Result<ComparisonDataVO> getComparisonData(@Valid @RequestBody ClassificationQueryParam param) {
        log.info("获取横向对比数据: {}", param);
        ComparisonDataVO data = energyClassificationService.getComparisonData(param);
        return Result.OK(data);
    }

    // ==================== 数据同步相关接口 ====================

    /**
     * 手动触发数据同步
     */
    @ApiOperation(value = "手动触发数据同步", notes = "手动触发指定时间范围的数据同步")
    @PostMapping("/triggerDataSync")
    public Result<Map<String, Object>> triggerDataSync(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("手动触发数据同步: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.syncClassificationData(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 按日期同步数据
     */
    @ApiOperation(value = "按日期同步数据", notes = "同步指定日期的数据")
    @PostMapping("/syncByDate")
    public Result<Map<String, Object>> syncByDate(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date targetDate) {
        log.info("按日期同步数据: targetDate={}", targetDate);
        Map<String, Object> result = syncService.syncByDate(targetDate);
        return Result.OK(result);
    }

    /**
     * 按月份同步数据
     */
    @ApiOperation(value = "按月份同步数据", notes = "同步指定月份的数据")
    @PostMapping("/syncByMonth")
    public Result<Map<String, Object>> syncByMonth(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        log.info("按月份同步数据: year={}, month={}", year, month);
        Map<String, Object> result = syncService.syncByMonth(year, month);
        return Result.OK(result);
    }

    /**
     * 按年份同步数据
     */
    @ApiOperation(value = "按年份同步数据", notes = "同步指定年份的数据")
    @PostMapping("/syncByYear")
    public Result<Map<String, Object>> syncByYear(@RequestParam("year") Integer year) {
        log.info("按年份同步数据: year={}", year);
        Map<String, Object> result = syncService.syncByYear(year);
        return Result.OK(result);
    }

    /**
     * 增量同步数据
     */
    @ApiOperation(value = "增量同步数据", notes = "增量同步指定时间范围的数据")
    @PostMapping("/incrementalSync")
    public Result<Map<String, Object>> incrementalSync(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("增量同步数据: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.incrementalSync(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 同步所有未同步的数据
     */
    @ApiOperation(value = "同步所有未同步的数据", notes = "自动检测并同步所有未同步的数据")
    @PostMapping("/syncAllUnsyncedData")
    public Result<Map<String, Object>> syncAllUnsyncedData() {
        log.info("同步所有未同步的数据");
        Map<String, Object> result = syncService.syncAllUnsyncedData();
        return Result.OK(result);
    }

    /**
     * 重新计算统计数据
     */
    @ApiOperation(value = "重新计算统计数据", notes = "清理并重新计算指定时间范围的统计数据")
    @PostMapping("/recalculateStatistics")
    public Result<Map<String, Object>> recalculateStatistics(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("重新计算统计数据: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.recalculateStatistics(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 清理历史数据
     */
    @ApiOperation(value = "清理历史数据", notes = "清理指定时间范围的历史数据")
    @PostMapping("/cleanupOldData")
    public Result<Map<String, Object>> cleanupOldData(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("清理历史数据: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.cleanupOldData(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 获取最新统计日期
     */
    @ApiOperation(value = "获取最新统计日期", notes = "获取统计表中最新的统计日期")
    @GetMapping("/getLatestStatisticsDate")
    public Result<Date> getLatestStatisticsDate() {
        log.info("获取最新统计日期");
        Date latestDate = syncService.getLatestStatisticsDate();
        return Result.OK(latestDate);
    }

    /**
     * 验证数据完整性
     */
    @ApiOperation(value = "验证数据完整性", notes = "验证指定时间范围的数据完整性")
    @GetMapping("/validateDataCompleteness")
    public Result<Map<String, Object>> validateDataCompleteness(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("验证数据完整性: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.validateDataCompleteness(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 获取实时表数据统计信息
     */
    @ApiOperation(value = "获取实时表数据统计信息", notes = "获取实时表中指定时间范围的数据统计信息")
    @GetMapping("/getRealTimeDataStatistics")
    public Result<Map<String, Object>> getRealTimeDataStatistics(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("获取实时表数据统计信息: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = syncService.getRealTimeDataStatistics(startDate, endDate);
        return Result.OK(result);
    }

    /**
     * 检查是否有未同步的数据
     */
    @ApiOperation(value = "检查是否有未同步的数据", notes = "检查指定日期是否有未同步的数据")
    @GetMapping("/hasUnsyncedData")
    public Result<Boolean> hasUnsyncedData(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date targetDate) {
        log.info("检查是否有未同步的数据: targetDate={}", targetDate);
        boolean hasUnsynced = syncService.hasUnsyncedData(targetDate);
        return Result.OK(hasUnsynced);
    }

    // ==================== 快速测试接口（GET方法，便于浏览器直接访问） ====================

    /**
     * 快速同步指定日期数据（GET方法，便于测试）
     * 使用方式: /energy/classification/quickSyncByDate?targetDate=2025-01-13
     */
    @ApiOperation(value = "快速同步指定日期数据", notes = "GET方法便于在浏览器直接测试")
    @GetMapping("/quickSyncByDate")
    public Result<Map<String, Object>> quickSyncByDate(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date targetDate) {
        log.info("快速同步指定日期数据: targetDate={}", targetDate);
        Map<String, Object> result = syncService.syncByDate(targetDate);
        return Result.OK(result);
    }

    /**
     * 快速数据诊断
     * 返回系统当前的数据状态，用于快速定位问题
     */
    @ApiOperation(value = "快速数据诊断", notes = "返回系统数据状态，用于快速定位问题")
    @GetMapping("/quickDiagnose")
    public Result<Map<String, Object>> quickDiagnose() {
        log.info("执行快速数据诊断");
        
        java.util.Map<String, Object> diagnosis = new java.util.HashMap<>();
        
        try {
            // 获取最新统计日期
            Date latestDate = syncService.getLatestStatisticsDate();
            diagnosis.put("latestStatisticsDate", latestDate);
            
            // 检查昨天和今天是否有未同步数据
            java.util.Calendar cal = java.util.Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
            Date yesterday = cal.getTime();
            
            boolean hasUnsyncedYesterday = syncService.hasUnsyncedData(yesterday);
            boolean hasUnsyncedToday = syncService.hasUnsyncedData(today);
            
            diagnosis.put("yesterdayNeedsSync", hasUnsyncedYesterday);
            diagnosis.put("todayNeedsSync", hasUnsyncedToday);
            diagnosis.put("checkTime", new Date());
            
            // 获取实时表数据统计
            cal.add(java.util.Calendar.DAY_OF_MONTH, -6); // 7天前
            Date weekAgo = cal.getTime();
            Map<String, Object> realTimeStats = syncService.getRealTimeDataStatistics(weekAgo, today);
            diagnosis.put("realTimeDataStats", realTimeStats);
            
            // 验证数据完整性
            Map<String, Object> completeness = syncService.validateDataCompleteness(weekAgo, today);
            diagnosis.put("dataCompleteness", completeness);
            
            // 诊断建议
            java.util.List<String> suggestions = new java.util.ArrayList<>();
            if (latestDate == null) {
                suggestions.add("汇总表没有数据，建议执行: POST /energy/classification/syncAllUnsyncedData");
            }
            if (hasUnsyncedYesterday) {
                suggestions.add("昨天数据未同步，建议执行: GET /energy/classification/quickSyncByDate?targetDate=" + 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(yesterday));
            }
            if (hasUnsyncedToday) {
                suggestions.add("今天数据未同步，建议执行: GET /energy/classification/quickSyncByDate?targetDate=" + 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(today));
            }
            Long realTimeCount = realTimeStats.get("totalRecords") != null ? 
                ((Number) realTimeStats.get("totalRecords")).longValue() : 0L;
            if (realTimeCount == 0) {
                suggestions.add("警告：实时表(tb_ep_equ_energy_daycount)最近7天没有数据，请检查数据采集系统");
            }
            
            diagnosis.put("suggestions", suggestions);
            diagnosis.put("status", suggestions.isEmpty() ? "HEALTHY" : "NEEDS_ATTENTION");
            
        } catch (Exception e) {
            log.error("快速数据诊断失败", e);
            diagnosis.put("status", "ERROR");
            diagnosis.put("errorMessage", e.getMessage());
        }
        
        return Result.OK(diagnosis);
    }

    /**
     * 查看汇总表原始数据（调试用）
     * 返回最近的汇总表数据，用于检查 org_code 和 time_dimension 字段
     */
    @ApiOperation(value = "查看汇总表原始数据", notes = "调试用，返回汇总表的原始数据样例")
    @GetMapping("/debugSummaryData")
    public Result<Map<String, Object>> debugSummaryData(
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        log.info("查看汇总表原始数据，limit={}", limit);
        
        Map<String, Object> debugInfo = new java.util.HashMap<>();
        
        try {
            // 调用service获取调试数据
            Map<String, Object> rawData = energyClassificationService.getDebugSummaryData(limit);
            debugInfo.putAll(rawData);
            debugInfo.put("status", "SUCCESS");
        } catch (Exception e) {
            log.error("获取调试数据失败", e);
            debugInfo.put("status", "ERROR");
            debugInfo.put("errorMessage", e.getMessage());
        }
        
        return Result.OK(debugInfo);
    }
}