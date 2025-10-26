package org.jeecg.modules.energy.controller.classification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationService;
import org.jeecg.modules.energy.vo.classification.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 企业分类分区统计控制器
 * @author jeecg
 */
@RestController
@RequestMapping("/energy/classification")
@Api(tags = "企业分类分区统计")
@Slf4j
@Validated
public class EnergyClassificationController {
    
    @Autowired
    private IEnergyClassificationService energyClassificationService;
    
    /**
     * 获取部门树形结构
     */
    @ApiOperation(value = "获取部门树形结构", notes = "获取企业的部门树形结构数据")
    @GetMapping("/getOrgTree")
    public Result<List<OrgTreeVO>> getOrgTree() {
        List<OrgTreeVO> treeData = energyClassificationService.getOrgTree();
        return Result.OK(treeData);
    }
    
    /**
     * 获取能源类型列表
     */
    @ApiOperation(value = "获取能源类型列表", notes = "获取启用的能源类型配置")
    @GetMapping("/getEnergyTypes")
    public Result<List<EnergyTypeVO>> getEnergyTypes() {
        List<EnergyTypeVO> energyTypes = energyClassificationService.getEnergyTypes();
        return Result.OK(energyTypes);
    }
    
    /**
     * 查询分类分区统计汇总
     */
    @ApiOperation(value = "查询分类分区统计汇总", notes = "按条件查询分类分区统计汇总数据")
    @PostMapping("/getSummaryData")
    public Result<ClassificationSummaryVO> getSummaryData(@Valid @RequestBody ClassificationQueryParam param) {
        log.info("查询分类分区统计汇总: {}", param);
        ClassificationSummaryVO data = energyClassificationService.getSummaryData(param);
        return Result.OK(data);
    }
    
    /**
     * 查询趋势对比数据
     */
    @ApiOperation(value = "查询趋势对比数据", notes = "获取能源趋势对比数据")
    @PostMapping("/getTrendData")
    public Result<TrendDataVO> getTrendData(@Valid @RequestBody ClassificationQueryParam param) {
        log.info("查询趋势对比数据: {}", param);
        TrendDataVO data = energyClassificationService.getTrendData(param);
        return Result.OK(data);
    }
    
    /**
     * 导出分类分区统计数据
     */
    @ApiOperation(value = "导出分类分区统计数据", notes = "导出分类分区统计数据到Excel")
    @GetMapping("/exportData")
    public void exportData(ClassificationQueryParam param, HttpServletResponse response) {
        log.info("导出分类分区统计数据: {}", param);
        energyClassificationService.exportData(param, response);
    }
}