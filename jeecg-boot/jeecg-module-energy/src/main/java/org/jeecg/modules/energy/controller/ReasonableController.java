package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IReasonableService;
import org.jeecg.modules.energy.vo.reasonable.ReasonableRequest;
import org.jeecg.modules.energy.vo.reasonable.ReasonableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 合理用能 - 控制器
 */
@RestController
@RequestMapping("/energy/analysis")
@Api(tags = "合理用能")
@Slf4j
@Validated
public class ReasonableController {

    @Autowired
    private IReasonableService reasonableService;

    @ApiOperation(value = "合理用能分析", notes = "根据模块集合+时间粒度+起止日期返回汇总、占比与趋势数据")
    @PostMapping("/reasonable")
    public Result<ReasonableResponse> analyze(@Valid @RequestBody ReasonableRequest request) {
        log.info("合理用能分析请求：{}", request);
        ReasonableResponse data = reasonableService.analyze(request);
        return Result.OK(data);
    }
}

