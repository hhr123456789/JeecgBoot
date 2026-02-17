package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IProductEnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 产品能耗分析Controller
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Api(tags = "产品能耗分析")
@RestController
@RequestMapping("/energy/product")
@Slf4j
public class ProductEnergyController {

    @Autowired
    private IProductEnergyService productEnergyService;

    /**
     * 获取统计数据
     */
    @ApiOperation(value = "获取统计数据", notes = "获取总能耗、总产量、合格率、单位产品能耗等统计数据")
    @GetMapping(value = "/statistics")
    public Result<Map<String, Object>> getStatistics(
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            Map<String, Object> data = productEnergyService.getStatistics(timeDimension, startDate, endDate, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取能耗分布数据（饼图）
     */
    @ApiOperation(value = "获取能耗分布", notes = "获取产品能耗分布数据,用于饼图展示")
    @GetMapping(value = "/distribution")
    public Result<Map<String, Object>> getDistribution(
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            Map<String, Object> data = productEnergyService.getDistribution(timeDimension, startDate, endDate, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取能耗分布失败", e);
            return Result.error("获取能耗分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取单耗趋势数据（折线图）
     */
    @ApiOperation(value = "获取单耗趋势", notes = "获取产品单耗趋势数据,用于折线图展示")
    @GetMapping(value = "/trend")
    public Result<Map<String, Object>> getTrend(
            @ApiParam("产品编码列表") @RequestParam(value = "productCodes", required = false) String productCodes,
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            List<String> codeList = productCodes != null ?
                Arrays.asList(productCodes.split(",")) : null;

            Map<String, Object> data = productEnergyService.getTrend(codeList, timeDimension, startDate, endDate, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取单耗趋势失败", e);
            return Result.error("获取单耗趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取产量能耗对比数据（柱状图）
     */
    @ApiOperation(value = "获取产量能耗对比", notes = "获取产量与能耗对比数据,用于双轴柱状图展示")
    @GetMapping(value = "/comparison")
    public Result<Map<String, Object>> getComparison(
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            Map<String, Object> data = productEnergyService.getComparison(timeDimension, startDate, endDate, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取对比数据失败", e);
            return Result.error("获取对比数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取单耗排名数据（横向柱状图）
     */
    @ApiOperation(value = "获取单耗排名", notes = "获取产品单耗排名数据,用于横向柱状图展示")
    @GetMapping(value = "/ranking")
    public Result<Map<String, Object>> getRanking(
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("排序方式") @RequestParam(value = "order", defaultValue = "asc") String order,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            Map<String, Object> data = productEnergyService.getRanking(timeDimension, startDate, endDate, order, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取排名数据失败", e);
            return Result.error("获取排名数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取明细列表数据（表格）
     */
    @ApiOperation(value = "获取明细列表", notes = "获取产品单耗明细数据,用于表格展示")
    @GetMapping(value = "/detail-list")
    public Result<Map<String, Object>> getDetailList(
            @ApiParam("时间维度") @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", defaultValue = "2024-01-01") String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", defaultValue = "2024-12-31") String endDate,
            @ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @ApiParam("能源类型") @RequestParam(value = "energyType", required = false) Integer energyType,
            @ApiParam("产品分类ID") @RequestParam(value = "categoryId", required = false) String categoryId) {
        try {
            Map<String, Object> data = productEnergyService.getDetailList(timeDimension, startDate, endDate, pageNo, pageSize, energyType, categoryId);
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取明细列表失败", e);
            return Result.error("获取明细列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取产品分类树（左侧树）
     */
    @ApiOperation(value = "获取产品分类树", notes = "获取产品分类树形数据,用于左侧树形菜单展示")
    @GetMapping(value = "/category-tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        try {
            List<Map<String, Object>> data = productEnergyService.getCategoryTree();
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取产品分类树失败", e);
            return Result.error("获取产品分类树失败: " + e.getMessage());
        }
    }
}
