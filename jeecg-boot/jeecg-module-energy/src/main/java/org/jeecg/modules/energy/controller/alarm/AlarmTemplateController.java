package org.jeecg.modules.energy.controller.alarm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.alarm.AlarmTemplate;
import org.jeecg.modules.energy.service.IAlarmTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 告警模板管理Controller
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Api(tags = "告警模板管理")
@RestController
@RequestMapping("/energy/alarm/template")
@Slf4j
public class AlarmTemplateController {

    @Autowired
    private IAlarmTemplateService alarmTemplateService;

    /**
     * 分页查询模板列表
     */
    @ApiOperation(value = "分页查询模板列表", notes = "分页查询告警模板列表")
    @GetMapping("/list")
    public Result<?> list(
            @ApiParam("模板名称") @RequestParam(required = false) String name,
            @ApiParam("模板类型") @RequestParam(required = false) String type,
            @ApiParam("能源类型") @RequestParam(required = false) String energyType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<AlarmTemplate> page = alarmTemplateService.queryPageList(
                    name, type, energyType, status, pageNo, pageSize);
            return Result.OK(page);
        } catch (Exception e) {
            log.error("查询模板列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增模板
     */
    @ApiOperation(value = "新增模板", notes = "新增告警模板")
    @PostMapping("/add")
    public Result<?> add(@RequestBody AlarmTemplate template) {
        try {
            alarmTemplateService.saveTemplate(template);
            return Result.OK("保存成功");
        } catch (Exception e) {
            log.error("新增模板失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 编辑模板
     */
    @ApiOperation(value = "编辑模板", notes = "编辑告警模板")
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody AlarmTemplate template) {
        try {
            alarmTemplateService.updateTemplate(template);
            return Result.OK("更新成功");
        } catch (Exception e) {
            log.error("编辑模板失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @ApiOperation(value = "删除模板", notes = "删除告警模板")
    @DeleteMapping("/delete")
    public Result<?> delete(@ApiParam("模板ID") @RequestParam String id) {
        try {
            return alarmTemplateService.deleteTemplate(id);
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询模板使用情况
     */
    @ApiOperation(value = "查询模板使用情况", notes = "查询模板被哪些规则引用")
    @GetMapping("/usage")
    public Result<?> usage(@ApiParam("模板ID") @RequestParam String id) {
        try {
            return Result.OK(alarmTemplateService.getTemplateUsage(id));
        } catch (Exception e) {
            log.error("查询模板使用情况失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板选项列表
     */
    @ApiOperation(value = "获取模板选项列表", notes = "获取可用的模板列表，用于下拉选择")
    @GetMapping("/options")
    public Result<?> options() {
        try {
            return Result.OK(alarmTemplateService.getTemplateOptions());
        } catch (Exception e) {
            log.error("获取模板选项失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询模板详情
     */
    @ApiOperation(value = "查询模板详情", notes = "根据ID查询模板详情")
    @GetMapping("/queryById")
    public Result<?> queryById(@ApiParam("模板ID") @RequestParam String id) {
        try {
            AlarmTemplate template = alarmTemplateService.getById(id);
            if (template == null) {
                return Result.error("模板不存在");
            }
            return Result.OK(template);
        } catch (Exception e) {
            log.error("查询模板详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
