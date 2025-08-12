package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IRealtimeMonitorService;
import org.jeecg.modules.energy.vo.realtime.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 实时数据监控测试Controller
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Api(tags = "实时数据监控测试")
@RestController
@RequestMapping("/energy/realtime/test")
@Slf4j
public class RealtimeTestController {
    
    @Autowired
    private IRealtimeMonitorService realtimeMonitorService;
    
    /**
     * 测试获取仪表列表
     */
    @ApiOperation(value = "测试获取仪表列表", notes = "测试根据维度获取仪表列表功能")
    @GetMapping("/testGetModules")
    public Result<List<ModuleInfoVO>> testGetModules() {
        log.info("测试获取仪表列表");
        
        try {
            // 测试注塑部门的电力仪表
            List<ModuleInfoVO> result = realtimeMonitorService.getModulesByDimension("A02A02A01", 1, true);
            log.info("测试结果：查询到仪表数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("测试获取仪表列表失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试获取参数配置
     */
    @ApiOperation(value = "测试获取参数配置", notes = "测试获取电力参数配置功能")
    @GetMapping("/testGetParameters")
    public Result<List<ParameterConfigVO>> testGetParameters() {
        log.info("测试获取参数配置");
        
        try {
            List<ParameterConfigVO> result = realtimeMonitorService.getParameterConfig(1);
            log.info("测试结果：查询到参数配置数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("测试获取参数配置失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试查询时序数据
     */
    @ApiOperation(value = "测试查询时序数据", notes = "测试查询时序数据功能")
    @GetMapping("/testGetTimeSeriesData")
    public Result<TimeSeriesResultVO> testGetTimeSeriesData() {
        log.info("测试查询时序数据");
        
        try {
            TimeSeriesQueryVO query = new TimeSeriesQueryVO();
            query.setModuleIds(Arrays.asList("yj0001_13", "yj0001_14"));
            query.setParameters(Arrays.asList(1, 2, 7));
            query.setTimeGranularity("day");
            query.setQueryDate("2025-07-25");
            
            TimeSeriesResultVO result = realtimeMonitorService.getTimeSeriesData(query);
            log.info("测试结果：时序数据查询完成");
            return Result.OK(result);
        } catch (Exception e) {
            log.error("测试查询时序数据失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试获取实时状态
     */
    @ApiOperation(value = "测试获取实时状态", notes = "测试获取实时状态功能")
    @GetMapping("/testGetCurrentStatus")
    public Result<List<ModuleStatusVO>> testGetCurrentStatus() {
        log.info("测试获取实时状态");
        
        try {
            List<String> moduleIds = Arrays.asList("yj0001_13", "yj0001_14");
            List<Integer> parameters = Arrays.asList(1, 2, 7);
            
            List<ModuleStatusVO> result = realtimeMonitorService.getCurrentStatus(moduleIds, parameters);
            log.info("测试结果：查询到实时状态数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("测试获取实时状态失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 热部署测试接口
     */
    @ApiOperation(value = "热部署测试", notes = "用于测试热部署功能是否正常工作")
    @GetMapping("/hotdeploy")
    public Result<String> testHotDeploy() {
        String timestamp = new Date().toString();
        log.info("🔥 热部署测试 - 当前时间: {}", timestamp);
        return Result.OK("热部署测试成功！当前时间: " + timestamp);
    }

    /**
     * 日志测试接口
     */
    @ApiOperation(value = "日志测试", notes = "用于测试日志文件写入是否正常")
    @GetMapping("/log-test")
    public Result<String> testLogging() {
        String timestamp = new Date().toString();

        log.debug("📝 DEBUG级别日志测试 - {}", timestamp);
        log.info("📋 INFO级别日志测试 - {}", timestamp);
        log.warn("⚠️ WARN级别日志测试 - {}", timestamp);
        log.error("❌ ERROR级别日志测试 - {}", timestamp);

        String message = "日志测试完成！请检查logs目录中的日志文件。时间: " + timestamp;
        log.info("🎯 {}", message);

        return Result.OK(message);
    }
}
