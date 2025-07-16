package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.job.DataSyncJob;
import org.jeecg.modules.energy.job.InfluxDBSyncJob;
import org.jeecg.modules.energy.service.IInfluxDBService;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * InfluxDB测试控制器
 */
@Slf4j
@Api(tags = "InfluxDB测试接口")
@RestController
@RequestMapping("/energy/influxdb")
public class InfluxDBTestController {
    
    @Autowired
    private IInfluxDBService influxDBService;
    
    @Autowired
    private InfluxDBConfig influxDBConfig;
    
    @Autowired
    private InfluxDB influxDB;
    
    @Autowired
    private InfluxDBSyncJob influxDBSyncJob;
    
    @Autowired
    private DataSyncJob dataSyncJob;
    
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 测试InfluxDB连接
     */
    @GetMapping("/test")
    @ApiOperation(value = "测试InfluxDB连接", notes = "测试InfluxDB连接")
    public Result<String> testConnection() {
        try {
            // 测试连接
            String version = influxDB.version();
            return Result.OK("连接成功，InfluxDB版本: " + version);
        } catch (Exception e) {
            log.error("连接失败", e);
            return Result.error("连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有数据库
     */
    @GetMapping("/databases")
    @ApiOperation(value = "获取所有数据库", notes = "获取所有数据库")
    public Result<List<String>> getDatabases() {
        try {
            List<String> databases = influxDBService.getDatabases();
            return Result.OK(databases);
        } catch (Exception e) {
            log.error("获取数据库列表失败", e);
            return Result.error("获取数据库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建当前月份的数据库
     */
    @GetMapping("/create-db")
    @ApiOperation(value = "创建当前月份的数据库", notes = "创建当前月份的数据库")
    public Result<String> createCurrentMonthDatabase() {
        try {
            String dbName = influxDBConfig.getCurrentMonthDatabaseName();
            if (!influxDB.databaseExists(dbName)) {
                influxDB.createDatabase(dbName);
                return Result.OK("创建数据库成功: " + dbName);
            } else {
                return Result.OK("数据库已存在: " + dbName);
            }
        } catch (Exception e) {
            log.error("创建数据库失败", e);
            return Result.error("创建数据库失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发InfluxDBSyncJob
     */
    @GetMapping("/sync")
    @ApiOperation(value = "手动触发InfluxDBSyncJob", notes = "手动触发InfluxDBSyncJob")
    public Result<String> triggerInfluxDBSyncJob() {
        try {
            influxDBSyncJob.syncRealTimeData();
            return Result.OK("触发InfluxDBSyncJob成功");
        } catch (Exception e) {
            log.error("触发InfluxDBSyncJob失败", e);
            return Result.error("触发InfluxDBSyncJob失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发DataSyncJob
     */
    @GetMapping("/data-sync")
    @ApiOperation(value = "手动触发DataSyncJob", notes = "手动触发DataSyncJob")
    public Result<String> triggerDataSyncJob() {
        try {
            dataSyncJob.syncRealTimeData();
            return Result.OK("触发DataSyncJob成功");
        } catch (Exception e) {
            log.error("触发DataSyncJob失败", e);
            return Result.error("触发DataSyncJob失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询指定模块的最新数据
     */
    @GetMapping("/query-module")
    @ApiOperation(value = "查询指定模块的最新数据", notes = "查询指定模块的最新数据")
    public Result<List<Map<String, Object>>> queryModuleData(@RequestParam String moduleId) {
        try {
            // 获取当前时间
            Date now = new Date();
            String endTime = dateTimeFormat.format(now);
            
            // 获取5分钟前的时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, -5);
            Date fiveMinutesAgo = calendar.getTime();
            String startTime = dateTimeFormat.format(fiveMinutesAgo);
            
            // 获取当前月份的数据库名
            String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
            
            // 确保数据库存在
            if (!influxDB.databaseExists(currentMonthDB)) {
                return Result.error("数据库 " + currentMonthDB + " 不存在");
            }
            
            // 查询InfluxDB中的数据
            QueryResult queryResult = influxDBService.queryByModuleIdAndTimeRange(moduleId, startTime, endTime);
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
            
            return Result.OK(resultList);
        } catch (Exception e) {
            log.error("查询模块数据失败", e);
            return Result.error("查询模块数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试时区调整后的数据查询
     */
    @GetMapping("/test-timezone")
    @ApiOperation(value = "测试时区调整后的数据查询", notes = "测试时区调整后的数据查询")
    public Result<Map<String, Object>> testTimezoneQuery() {
        try {
            // 获取当前时间
            Date now = new Date();
            
            // 获取5分钟前的时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, -5);
            Date fiveMinutesAgo = calendar.getTime();
            
            // 获取当前月份的数据库名
            String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
            
            // 确保数据库存在
            if (!influxDB.databaseExists(currentMonthDB)) {
                return Result.error("数据库 " + currentMonthDB + " 不存在");
            }
            
            // 原始时间范围
            String startTime = dateTimeFormat.format(fiveMinutesAgo);
            String endTime = dateTimeFormat.format(now);
            
            // 调整后的时间范围（考虑8小时时区差异）
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(fiveMinutesAgo);
            startCalendar.add(Calendar.HOUR, -8);
            String adjustedStartTime = dateTimeFormat.format(startCalendar.getTime());
            
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(now);
            endCalendar.add(Calendar.HOUR, -8);
            String adjustedEndTime = dateTimeFormat.format(endCalendar.getTime());
            
            // 使用原始时间范围查询
            String originalCommand = String.format("SELECT * FROM %s WHERE time >= '%s' AND time <= '%s'",
                    influxDBConfig.getMeasurement(), startTime, endTime);
            QueryResult originalResult = influxDBService.queryInDatabase(originalCommand, currentMonthDB);
            List<Map<String, Object>> originalResultList = InfluxDBUtil.parseQueryResult(originalResult);
            
            // 使用调整后的时间范围查询
            String adjustedCommand = String.format("SELECT * FROM %s WHERE time >= '%s' AND time <= '%s'",
                    influxDBConfig.getMeasurement(), adjustedStartTime, adjustedEndTime);
            QueryResult adjustedResult = influxDBService.queryInDatabase(adjustedCommand, currentMonthDB);
            List<Map<String, Object>> adjustedResultList = InfluxDBUtil.parseQueryResult(adjustedResult);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            
            Map<String, String> originalTimeRange = new HashMap<>();
            originalTimeRange.put("start", startTime);
            originalTimeRange.put("end", endTime);
            result.put("originalTimeRange", originalTimeRange);
            
            Map<String, String> adjustedTimeRange = new HashMap<>();
            adjustedTimeRange.put("start", adjustedStartTime);
            adjustedTimeRange.put("end", adjustedEndTime);
            result.put("adjustedTimeRange", adjustedTimeRange);
            
            result.put("originalResultCount", originalResultList.size());
            result.put("adjustedResultCount", adjustedResultList.size());
            result.put("originalResults", originalResultList.isEmpty() ? "无数据" : originalResultList.subList(0, Math.min(10, originalResultList.size())));
            result.put("adjustedResults", adjustedResultList.isEmpty() ? "无数据" : adjustedResultList.subList(0, Math.min(10, adjustedResultList.size())));
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("测试时区查询失败", e);
            return Result.error("测试时区查询失败: " + e.getMessage());
        }
    }
} 