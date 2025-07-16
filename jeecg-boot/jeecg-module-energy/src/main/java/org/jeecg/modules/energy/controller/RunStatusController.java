package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IRunStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description: 设备运行状态控制器
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@RestController
@RequestMapping("/energy/monitor")
@Api(tags = "设备运行状态")
@Slf4j
public class RunStatusController {

    @Autowired
    private IRunStatusService runStatusService;
    
    /**
     * 获取设备运行状态
     *
     * @param orgCode 部门编码
     * @param deviceStatus 设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障)
     * @return 设备运行状态数据
     */
    @ApiOperation(value = "获取设备运行状态", notes = "获取设备运行状态数据")
    @GetMapping("/getRunStatus")
    public Result<List<Map<String, Object>>> getRunStatus(
            @ApiParam(value = "部门编码", required = true) @RequestParam String orgCode,
            @ApiParam(value = "设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障)") @RequestParam(required = false, defaultValue = "0") Integer deviceStatus) {
        log.info("获取设备运行状态，部门编码：{}，设备状态：{}", orgCode, deviceStatus);
        List<Map<String, Object>> result = runStatusService.getRunStatus(orgCode, deviceStatus);
        log.info("查询结果条数：{}", result.size());
        return Result.OK(result);
    }
} 