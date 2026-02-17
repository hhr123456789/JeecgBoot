package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.ITeamEnergyService;
import org.jeecg.modules.energy.vo.teamenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 班组能源统计控制器
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Api(tags = "班组能源统计")
@RestController
@RequestMapping("/energy/teamEnergy")
@Slf4j
public class TeamEnergyController {

    @Autowired
    private ITeamEnergyService teamEnergyService;

    /**
     * 根据维度获取班组列表
     */
    @ApiOperation(value = "根据维度获取班组列表", notes = "根据维度获取班组列表")
    @GetMapping("/getTeamList")
    public Result<List<TeamInfoVO>> getTeamList(
            @RequestParam(name = "dimensionCode") String dimensionCode,
            @RequestParam(name = "dimensionType") Integer dimensionType) {
        Result<List<TeamInfoVO>> result = new Result<>();
        try {
            List<TeamInfoVO> teamList = teamEnergyService.getTeamListByDimension(dimensionCode, dimensionType);
            result.setResult(teamList);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取班组列表失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取班组能源统计数据
     */
    @ApiOperation(value = "获取班组能源统计数据", notes = "获取班组能源统计数据")
    @GetMapping("/getStatistics")
    public Result<TeamEnergyStatisticsVO> getStatistics(TeamEnergyQueryRequest request) {
        log.info("========== Controller: 接收统计数据请求 ==========");
        log.info("请求参数: {}", request);
        Result<TeamEnergyStatisticsVO> result = new Result<>();
        try {
            TeamEnergyStatisticsVO statistics = teamEnergyService.getStatistics(request);
            log.info("Service返回数据: {}", statistics);
            result.setResult(statistics);
            result.setSuccess(true);
            result.setMessage("查询成功");
            log.info("Controller返回结果: success={}, result={}", result.isSuccess(), result.getResult());
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        log.info("========== Controller: 统计数据请求处理完成 ==========");
        return result;
    }

    /**
     * 获取班组能源趋势图数据
     */
    @ApiOperation(value = "获取班组能源趋势图数据", notes = "获取班组能源趋势图数据")
    @GetMapping("/getTrendData")
    public Result<TeamEnergyTrendVO> getTrendData(TeamEnergyQueryRequest request) {
        Result<TeamEnergyTrendVO> result = new Result<>();
        try {
            TeamEnergyTrendVO trendData = teamEnergyService.getTrendData(request);
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
     * 获取班组能源排名数据
     */
    @ApiOperation(value = "获取班组能源排名数据", notes = "获取班组能源排名数据")
    @GetMapping("/getRankingData")
    public Result<List<TeamEnergyRankingVO>> getRankingData(TeamEnergyQueryRequest request) {
        Result<List<TeamEnergyRankingVO>> result = new Result<>();
        try {
            List<TeamEnergyRankingVO> rankingData = teamEnergyService.getRankingData(request);
            result.setResult(rankingData);
            result.setSuccess(true);
            result.setMessage("查询成功");
        } catch (Exception e) {
            log.error("获取排名数据失败", e);
            result.setSuccess(false);
            result.setMessage("查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取班组能源明细表数据
     */
    @ApiOperation(value = "获取班组能源明细表数据", notes = "获取班组能源明细表数据")
    @GetMapping("/getTableData")
    public Result<List<TeamEnergyTableVO>> getTableData(TeamEnergyQueryRequest request) {
        Result<List<TeamEnergyTableVO>> result = new Result<>();
        try {
            List<TeamEnergyTableVO> tableData = teamEnergyService.getTableData(request);
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
