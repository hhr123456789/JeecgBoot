package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IEnergyMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description: 能源监控控制器
 * @Author: 
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Api(tags = "能源实时监控")
@RestController
@RequestMapping("/energy/monitor")
@Slf4j
public class EnergyMonitorController {

    @Autowired
    private IEnergyMonitorService energyMonitorService;
    
    /**
     * 获取实时监控数据
     *
     * @param orgCode 部门编码
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 实时监控数据
     */
    @ApiOperation(value = "获取实时监控数据", notes = "获取右侧实时监控数据")
    @GetMapping("/getRealTimeData")
    public Result<List<Map<String, Object>>> getRealTimeData(
            @ApiParam(value = "部门编码", required = true) @RequestParam String orgCode,
            @ApiParam(value = "维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)", required = true) @RequestParam Integer nowtype) {
        log.info("获取实时监控数据，部门编码：{}，能源类型：{}", orgCode, nowtype);
        List<Map<String, Object>> result = energyMonitorService.getRealTimeData(orgCode, nowtype);
        log.info("查询结果条数：{}", result.size());
        return Result.OK(result);
    }
} 