package org.jeecg.modules.energy.controller.alarm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.alarm.AlarmRule;
import org.jeecg.modules.energy.service.IAlarmRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 告警规则管理Controller
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Api(tags = "告警规则管理")
@RestController
@RequestMapping("/energy/alarm/rule")
@Slf4j
public class AlarmRuleController {

    @Autowired
    private IAlarmRuleService alarmRuleService;

    /**
     * 分页查询规则列表
     */
    @ApiOperation(value = "分页查询规则列表", notes = "分页查询告警规则列表")
    @GetMapping("/list")
    public Result<?> list(
            @ApiParam("规则名称") @RequestParam(required = false) String name,
            @ApiParam("告警类型") @RequestParam(required = false) String ruleType,
            @ApiParam("能源类型") @RequestParam(required = false) String energyType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("部门ID") @RequestParam(required = false) String deptId,
            @ApiParam("监控节点ID") @RequestParam(required = false) String targetNodeId,
            @ApiParam("维度类型") @RequestParam(required = false) Integer dimensionType,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<AlarmRule> page = alarmRuleService.queryPageList(
                    name, ruleType, energyType, status, deptId, targetNodeId, dimensionType, pageNo, pageSize);
            return Result.OK(page);
        } catch (Exception e) {
            log.error("查询规则列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增规则
     */
    @ApiOperation(value = "新增规则", notes = "新增告警规则")
    @PostMapping("/add")
    public Result<?> add(@RequestBody AlarmRule rule) {
        try {
            alarmRuleService.saveRule(rule);
            return Result.OK("保存成功");
        } catch (Exception e) {
            log.error("新增规则失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 编辑规则
     */
    @ApiOperation(value = "编辑规则", notes = "编辑告警规则")
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody AlarmRule rule) {
        try {
            alarmRuleService.updateRule(rule);
            return Result.OK("更新成功");
        } catch (Exception e) {
            log.error("编辑规则失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除规则
     */
    @ApiOperation(value = "删除规则", notes = "删除告警规则")
    @DeleteMapping("/delete")
    public Result<?> delete(@ApiParam("规则ID") @RequestParam String id) {
        try {
            return alarmRuleService.deleteRule(id);
        } catch (Exception e) {
            log.error("删除规则失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除规则
     */
    @ApiOperation(value = "批量删除规则", notes = "批量删除告警规则")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@ApiParam("规则ID列表，逗号分隔") @RequestParam String ids) {
        try {
            return alarmRuleService.deleteRuleBatch(ids);
        } catch (Exception e) {
            log.error("批量删除规则失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 修改规则状态
     */
    @ApiOperation(value = "修改规则状态", notes = "启用或禁用告警规则")
    @PutMapping("/changeStatus")
    public Result<?> changeStatus(
            @ApiParam("规则ID") @RequestParam String id,
            @ApiParam("状态：1-启用，0-禁用") @RequestParam Integer status) {
        try {
            return alarmRuleService.changeStatus(id, status);
        } catch (Exception e) {
            log.error("修改规则状态失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询规则详情
     */
    @ApiOperation(value = "查询规则详情", notes = "根据ID查询规则详情")
    @GetMapping("/queryById")
    public Result<?> queryById(@ApiParam("规则ID") @RequestParam String id) {
        try {
            AlarmRule rule = alarmRuleService.getById(id);
            if (rule == null) {
                return Result.error("规则不存在");
            }
            return Result.OK(rule);
        } catch (Exception e) {
            log.error("查询规则详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
