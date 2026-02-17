package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IProcessEnergyService;
import org.jeecg.modules.energy.vo.processenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工序能耗分析控制器
 */
@Api(tags = "工序能耗分析")
@RestController
@RequestMapping("/energy/processEnergy")
@Slf4j
public class ProcessEnergyController {

    @Autowired
    private IProcessEnergyService processEnergyService;

    /**
     * 获取生产线树形数据
     */
    @ApiOperation(value = "获取生产线树形数据", notes = "获取生产线树形数据")
    @GetMapping("/getProductionLineTree")
    public Result<List<ProductionLineTreeVO>> getProductionLineTree() {
        Result<List<ProductionLineTreeVO>> result = new Result<>();
        try {
            List<ProductionLineTreeVO> treeData = processEnergyService.getProductionLineTree();
            result.setResult(treeData);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取生产线树形数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取统计数据
     */
    @ApiOperation(value = "获取工序能耗统计数据", notes = "获取工序能耗统计数据")
    @GetMapping("/getStatistics")
    public Result<ProcessEnergyStatisticsVO> getStatistics(ProcessEnergyQueryRequest request) {
        log.info("获取工序能耗统计数据, 请求参数: {}", request);
        Result<ProcessEnergyStatisticsVO> result = new Result<>();
        try {
            ProcessEnergyStatisticsVO statistics = processEnergyService.getStatistics(request);
            result.setResult(statistics);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取饼图数据
     */
    @ApiOperation(value = "获取过程能耗分布饼图数据", notes = "获取过程能耗分布饼图数据")
    @GetMapping("/getPieData")
    public Result<List<ProcessEnergyPieVO>> getPieData(ProcessEnergyQueryRequest request) {
        log.info("获取饼图数据, 请求参数: {}", request);
        Result<List<ProcessEnergyPieVO>> result = new Result<>();
        try {
            List<ProcessEnergyPieVO> pieData = processEnergyService.getPieData(request);
            result.setResult(pieData);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取饼图数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取趋势数据
     */
    @ApiOperation(value = "获取过程能耗趋势数据", notes = "获取过程能耗趋势数据")
    @GetMapping("/getTrendData")
    public Result<ProcessEnergyTrendVO> getTrendData(ProcessEnergyQueryRequest request) {
        log.info("获取趋势数据, 请求参数: {}", request);
        Result<ProcessEnergyTrendVO> result = new Result<>();
        try {
            ProcessEnergyTrendVO trendData = processEnergyService.getTrendData(request);
            result.setResult(trendData);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取表格数据
     */
    @ApiOperation(value = "获取过程能耗表格数据", notes = "获取过程能耗表格数据")
    @GetMapping("/getTableData")
    public Result<List<ProcessEnergyTableVO>> getTableData(ProcessEnergyQueryRequest request) {
        log.info("获取表格数据, 请求参数: {}", request);
        Result<List<ProcessEnergyTableVO>> result = new Result<>();
        try {
            List<ProcessEnergyTableVO> tableData = processEnergyService.getTableData(request);
            result.setResult(tableData);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取表格数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }
}
