package org.jeecg.modules.energy.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.TbEnergyDimensionConfig;
import org.jeecg.modules.energy.entity.TbEnergyTeam;
import org.jeecg.modules.energy.service.ITbEnergyDimensionConfigService;
import org.jeecg.modules.energy.service.ITbEnergyTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * @Description: 能耗班组控制器
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@RestController
@RequestMapping("/energy/team")
@Slf4j
public class EnergyTeamController {
    
    @Autowired
    private ITbEnergyTeamService tbEnergyTeamService;
    
    @Autowired
    private ITbEnergyDimensionConfigService tbEnergyDimensionConfigService;

    /**
     * 获取维度配置列表
     * @return
     */
    @GetMapping(value = "/getDimensionConfigs")
    public Result<List<TbEnergyDimensionConfig>> getDimensionConfigs() {
        Result<List<TbEnergyDimensionConfig>> result = new Result<>();
        try {
            QueryWrapper<TbEnergyDimensionConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_enable", 1);
            queryWrapper.orderByAsc("sort_order");
            List<TbEnergyDimensionConfig> list = tbEnergyDimensionConfigService.list(queryWrapper);
            result.setResult(list);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
        }
        return result;
    }

    /**
     * 根据维度查询班组列表
     * @param dimensionCode 维度编码 (如部门ID)
     * @param dimensionType 维度类型 (如 org)
     * @return
     */
    @GetMapping(value = "/listByDimension")
    public Result<List<TbEnergyTeam>> listByDimension(
            @RequestParam(name="dimensionCode") String dimensionCode,
            @RequestParam(name="dimensionType") String dimensionType) {
        Result<List<TbEnergyTeam>> result = new Result<>();
        try {
            List<TbEnergyTeam> list = tbEnergyTeamService.queryTeamsByDimension(dimensionCode, dimensionType);
            result.setResult(list);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
        }
        return result;
    }
}
