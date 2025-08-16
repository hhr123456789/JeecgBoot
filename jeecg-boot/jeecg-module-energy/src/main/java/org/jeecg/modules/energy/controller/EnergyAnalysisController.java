package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IEnergyAnalysisService;
import org.jeecg.modules.energy.vo.analysis.CompareDataRequest;
import org.jeecg.modules.energy.vo.analysis.CompareDataVO;
import org.jeecg.modules.energy.vo.analysis.ModuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 能源分析对比 - 右侧功能接口
 */
@RestController
@RequestMapping("/energy/analysis")
@Api(tags = "能源分析对比")
@Slf4j
@Validated
public class EnergyAnalysisController {

    @Autowired
    private IEnergyAnalysisService energyAnalysisService;

    @ApiOperation(value = "根据维度获取仪表列表", notes = "根据orgCode获取该维度(可选含子维度)的启用仪表，用于右侧仪表下拉")
    @GetMapping("/getModulesByDimension")
    public Result<List<ModuleVO>> getModulesByDimension(@RequestParam String orgCode,
                                                        @RequestParam(required = false) Integer energyType,
                                                        @RequestParam(defaultValue = "false") Boolean includeChildren) {
        List<ModuleVO> modules = energyAnalysisService.getModulesByDimension(orgCode, energyType, includeChildren);
        return Result.OK(modules);
    }

    @ApiOperation(value = "查询设备能效对比数据", notes = "获取本期(基准)与同比(对比)的趋势、汇总和明细数据")
    @PostMapping("/getCompareData")
    public Result<CompareDataVO> getCompareData(@Valid @RequestBody CompareDataRequest request) {
        log.info("设备能效对比查询: {}", request);
        CompareDataVO data = energyAnalysisService.getCompareData(request);
        return Result.OK(data);
    }

    @ApiOperation(value = "导出设备能效对比(Excel)", notes = "根据查询条件导出Excel")
    @GetMapping("/exportCompareData")
    public void exportCompareData(@RequestParam String moduleId,
                                  @RequestParam String timeType,
                                  @RequestParam String baselineStartTime,
                                  @RequestParam String baselineEndTime,
                                  @RequestParam String compareStartTime,
                                  @RequestParam String compareEndTime,
                                  HttpServletResponse response) {
        CompareDataRequest req = new CompareDataRequest();
        req.setModuleId(moduleId);
        req.setTimeType(timeType);
        req.setBaselineStartTime(baselineStartTime);
        req.setBaselineEndTime(baselineEndTime);
        req.setCompareStartTime(compareStartTime);
        req.setCompareEndTime(compareEndTime);
        energyAnalysisService.exportCompareData(req, response);
    }
}

