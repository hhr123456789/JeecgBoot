package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IShiftEnergyService;
import org.jeecg.modules.energy.vo.shiftenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "\u73ed\u6b21\u7528\u80fd\u7edf\u8ba1")
@RestController
@RequestMapping("/energy/shiftEnergy")
@Slf4j
public class ShiftEnergyController {

    @Autowired
    private IShiftEnergyService shiftEnergyService;

    @ApiOperation(value = "\u83b7\u53d6\u73ed\u6b21\u7edf\u8ba1\u6570\u636e")
    @GetMapping("/getStatistics")
    public Result<ShiftEnergyStatisticsVO> getStatistics(ShiftEnergyQueryRequest request) {
        Result<ShiftEnergyStatisticsVO> result = new Result<>();
        try {
            ShiftEnergyStatisticsVO statistics = shiftEnergyService.getStatistics(request);
            result.setResult(statistics);
            result.setSuccess(true);
            result.setMessage("\u67e5\u8be2\u6210\u529f");
        } catch (Exception e) {
            log.error("\u83b7\u53d6\u73ed\u6b21\u7edf\u8ba1\u6570\u636e\u5931\u8d25", e);
            result.setSuccess(false);
            result.setMessage("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "\u83b7\u53d6\u73ed\u6b21\u8d8b\u52bf\u6570\u636e")
    @GetMapping("/getTrendData")
    public Result<ShiftEnergyTrendVO> getTrendData(ShiftEnergyQueryRequest request) {
        Result<ShiftEnergyTrendVO> result = new Result<>();
        try {
            ShiftEnergyTrendVO trendData = shiftEnergyService.getTrendData(request);
            result.setResult(trendData);
            result.setSuccess(true);
            result.setMessage("\u67e5\u8be2\u6210\u529f");
        } catch (Exception e) {
            log.error("\u83b7\u53d6\u73ed\u6b21\u8d8b\u52bf\u6570\u636e\u5931\u8d25", e);
            result.setSuccess(false);
            result.setMessage("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "\u83b7\u53d6\u73ed\u6b21\u5360\u6bd4\u6570\u636e")
    @GetMapping("/getPieData")
    public Result<List<ShiftEnergyPieVO>> getPieData(ShiftEnergyQueryRequest request) {
        Result<List<ShiftEnergyPieVO>> result = new Result<>();
        try {
            List<ShiftEnergyPieVO> pieData = shiftEnergyService.getPieData(request);
            result.setResult(pieData);
            result.setSuccess(true);
            result.setMessage("\u67e5\u8be2\u6210\u529f");
        } catch (Exception e) {
            log.error("\u83b7\u53d6\u73ed\u6b21\u5360\u6bd4\u6570\u636e\u5931\u8d25", e);
            result.setSuccess(false);
            result.setMessage("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "\u83b7\u53d6\u73ed\u6b21\u8868\u683c\u6570\u636e")
    @GetMapping("/getTableData")
    public Result<List<ShiftEnergyTableVO>> getTableData(ShiftEnergyQueryRequest request) {
        Result<List<ShiftEnergyTableVO>> result = new Result<>();
        try {
            List<ShiftEnergyTableVO> tableData = shiftEnergyService.getTableData(request);
            result.setResult(tableData);
            result.setSuccess(true);
            result.setMessage("\u67e5\u8be2\u6210\u529f");
        } catch (Exception e) {
            log.error("\u83b7\u53d6\u73ed\u6b21\u8868\u683c\u6570\u636e\u5931\u8d25", e);
            result.setSuccess(false);
            result.setMessage("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
        return result;
    }
}
