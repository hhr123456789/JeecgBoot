package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.ITrendAnalysisService;
import org.jeecg.modules.energy.vo.trend.TrendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/energy/analysis")
@Api(tags = "能耗趋势分析(折标煤/碳排放)")
@Slf4j
@Validated
public class TrendAnalysisController {

    @Autowired
    private ITrendAnalysisService trendAnalysisService;

    @ApiOperation(value = "查询趋势数据", notes = "根据日/月/年统计与能源系数计算折标煤/碳排放，支持统一显示与分开显示")
    @PostMapping("/trend")
    public Result<Object> getTrend(@Valid @RequestBody TrendRequest request) {
        return Result.OK(trendAnalysisService.getTrend(request));
    }

    @ApiOperation(value = "导出趋势数据(Excel)", notes = "导出查询结果")
    @PostMapping("/trend/export")
    public void export(@Valid @RequestBody TrendRequest request, HttpServletResponse response) {
        trendAnalysisService.exportTrend(request, response);
    }
}

