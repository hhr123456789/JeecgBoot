package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IConsumptionStatisticsService;
import org.jeecg.modules.energy.vo.consumption.ConsumptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/energy/consumption")
@Api(tags = "设备能源统计")
@Slf4j
@Validated
public class ConsumptionStatisticsController {

    @Autowired
    private IConsumptionStatisticsService consumptionStatisticsService;

    @ApiOperation(value = "查询设备能源统计数据", notes = "根据日/月/年统计设备原始能耗量，支持统一显示与分开显示")
    @PostMapping("/statistics")
    public Result<Object> getConsumptionStatistics(@Valid @RequestBody ConsumptionRequest request) {
        return Result.OK(consumptionStatisticsService.getConsumptionStatistics(request));
    }

    @ApiOperation(value = "导出设备能源统计数据(Excel)", notes = "导出查询结果")
    @PostMapping("/statistics/export")
    public void export(@Valid @RequestBody ConsumptionRequest request, HttpServletResponse response) {
        consumptionStatisticsService.exportConsumptionStatistics(request, response);
    }
}