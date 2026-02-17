package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.benchmark.*;
import org.jeecg.modules.energy.service.IBenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 能效对标控制器
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Api(tags = "能效对标")
@RestController
@RequestMapping("/energy/benchmark")
@Slf4j
public class BenchmarkController {

    @Autowired
    private IBenchmarkService benchmarkService;

    /**
     * 获取对标配置列表
     */
    @ApiOperation(value = "获取对标配置列表", notes = "获取对标配置列表")
    @GetMapping("/getConfig")
    public Result<List<BenchmarkConfig>> getConfig(
            @RequestParam(name = "benchmarkType", required = false) Integer benchmarkType,
            @RequestParam(name = "energyType", required = false) String energyType) {
        Result<List<BenchmarkConfig>> result = new Result<>();
        try {
            List<BenchmarkConfig> configList = benchmarkService.getConfigList(benchmarkType, energyType);
            result.setResult(configList);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取对标配置失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取对标对象列表
     */
    @ApiOperation(value = "获取对标对象列表", notes = "获取对标对象列表")
    @GetMapping("/getTargets")
    public Result<List<BenchmarkTarget>> getTargets(
            @RequestParam(name = "configId", required = false) String configId) {
        Result<List<BenchmarkTarget>> result = new Result<>();
        try {
            List<BenchmarkTarget> targetList = benchmarkService.getTargetList(configId);
            result.setResult(targetList);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取对标对象失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取对标统计数据
     */
    @ApiOperation(value = "获取对标统计数据", notes = "获取对标统计数据")
    @GetMapping("/getStatistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(name = "targetCode") String targetCode,
            @RequestParam(name = "timeUnit", defaultValue = "month") String timeUnit,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime,
            @RequestParam(name = "energyType", required = false) String energyType) {
        log.info("获取对标统计数据 - targetCode: {}, timeUnit: {}", targetCode, timeUnit);
        Result<Map<String, Object>> result = new Result<>();
        try {
            Map<String, Object> statistics = benchmarkService.getStatistics(targetCode, timeUnit, startTime, endTime, energyType);
            result.setResult(statistics);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取对标统计数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 导出对标数据
     */
    @ApiOperation(value = "导出对标数据", notes = "导出对标数据")
    @GetMapping("/exportData")
    public Result<List<Map<String, Object>>> exportData(
            @RequestParam(name = "targetCode") String targetCode,
            @RequestParam(name = "timeUnit", defaultValue = "month") String timeUnit,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime,
            @RequestParam(name = "energyType", required = false) String energyType) {
        log.info("导出对标数据 - targetCode: {}, timeUnit: {}", targetCode, timeUnit);
        Result<List<Map<String, Object>>> result = new Result<>();
        try {
            List<Map<String, Object>> exportData = benchmarkService.exportData(targetCode, timeUnit, startTime, endTime, energyType);
            result.setResult(exportData);
            result.setSuccess(true);
            result.setMessage("导出成功");
        } catch (Exception e) {
            log.error("导出对标数据失败", e);
            result.setSuccess(false);
            result.setMessage("导出失败: " + e.getMessage());
        }
        return result;
    }
}
