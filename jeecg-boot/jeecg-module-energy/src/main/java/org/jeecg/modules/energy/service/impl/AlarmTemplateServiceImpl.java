package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.energy.entity.alarm.AlarmTemplate;
import org.jeecg.modules.energy.mapper.alarm.AlarmTemplateMapper;
import org.jeecg.modules.energy.mapper.alarm.AlarmRuleMapper;
import org.jeecg.modules.energy.service.IAlarmTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.*;

/**
 * @Description: 告警模板Service实现
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class AlarmTemplateServiceImpl extends ServiceImpl<AlarmTemplateMapper, AlarmTemplate> implements IAlarmTemplateService {

    private static final Set<String> ALARM_TYPES = new HashSet<>(Arrays.asList("device", "energy"));
    private static final Set<String> ENERGY_TYPES = new HashSet<>(Arrays.asList("1", "2", "8", "5"));
    private static final Set<String> DEVICE_TYPES = new HashSet<>(Arrays.asList("GFMT", "CEC", "ACOP", "WMCT", "METE", "ELEV"));
    private static final Set<String> LEVEL_TYPES = new HashSet<>(Arrays.asList("high", "medium", "low"));

    @Autowired
    private AlarmTemplateMapper alarmTemplateMapper;

    @Autowired
    private AlarmRuleMapper alarmRuleMapper;

    @Override
    public IPage<AlarmTemplate> queryPageList(String name, String type, String energyType,
                                               Integer status, Integer pageNo, Integer pageSize) {
        Page<AlarmTemplate> page = new Page<>(pageNo, pageSize);
        QueryWrapper<AlarmTemplate> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        if (StringUtils.hasText(type) && !"all".equals(type)) {
            queryWrapper.eq("type", type);
        }
        if (StringUtils.hasText(energyType) && !"all".equals(energyType)) {
            queryWrapper.eq("energy_type", energyType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("update_time");

        IPage<AlarmTemplate> result = this.page(page, queryWrapper);

        // 填充使用次数
        for (AlarmTemplate template : result.getRecords()) {
            Integer usageCount = alarmTemplateMapper.countUsageByTemplateId(template.getId());
            template.setUsageCount(usageCount != null ? usageCount : 0);
        }

        return result;
    }

    @Override
    public void saveTemplate(AlarmTemplate template) {
        validateTemplate(template);
        normalizeTemplate(template);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        template.setCreateBy(loginUser != null ? loginUser.getUsername() : "system");
        template.setCreateTime(new Date());
        template.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        template.setUpdateTime(new Date());
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        this.save(template);
    }

    @Override
    public void updateTemplate(AlarmTemplate template) {
        if (!StringUtils.hasText(template.getId())) {
            throw new RuntimeException("模板ID不能为空");
        }
        validateTemplate(template);
        normalizeTemplate(template);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        template.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        template.setUpdateTime(new Date());
        this.updateById(template);
    }

    private void validateTemplate(AlarmTemplate template) {
        if (template == null) {
            throw new RuntimeException("模板参数不能为空");
        }
        if (!StringUtils.hasText(template.getName())) {
            throw new RuntimeException("模板名称不能为空");
        }
        if (!StringUtils.hasText(template.getType()) || !ALARM_TYPES.contains(template.getType())) {
            throw new RuntimeException("模板类型不合法");
        }
        if (!StringUtils.hasText(template.getEnergyType()) || !ENERGY_TYPES.contains(template.getEnergyType())) {
            throw new RuntimeException("能源类型不合法");
        }
        if (!StringUtils.hasText(template.getConditions())) {
            throw new RuntimeException("告警条件不能为空");
        }
        if (StringUtils.hasText(template.getLevel()) && !LEVEL_TYPES.contains(template.getLevel())) {
            throw new RuntimeException("告警等级不合法");
        }

        if ("device".equals(template.getType())) {
            if (!StringUtils.hasText(template.getDeviceType()) || !DEVICE_TYPES.contains(template.getDeviceType())) {
                throw new RuntimeException("设备告警模板必须选择设备类型");
            }
        }
    }

    private void normalizeTemplate(AlarmTemplate template) {
        if (!StringUtils.hasText(template.getLevel())) {
            template.setLevel("medium");
        }
        if (template.getSilencePeriod() == null || template.getSilencePeriod() < 0) {
            template.setSilencePeriod(60);
        }

        if ("energy".equals(template.getType())) {
            template.setDeviceType(null);
            template.setTargetScope("device");
        } else {
            template.setTargetScope(null);
        }
    }

    @Override
    public Result<?> deleteTemplate(String id) {
        // 检查是否被规则引用
        Integer usageCount = alarmTemplateMapper.countUsageByTemplateId(id);
        if (usageCount != null && usageCount > 0) {
            return Result.error("该模板已被 " + usageCount + " 条规则引用，请先解除关联后再删除");
        }
        this.removeById(id);
        return Result.OK("删除成功");
    }

    @Override
    public Map<String, Object> getTemplateUsage(String id) {
        Map<String, Object> result = new HashMap<>();
        Integer usageCount = alarmTemplateMapper.countUsageByTemplateId(id);
        result.put("usageCount", usageCount != null ? usageCount : 0);
        result.put("rules", alarmRuleMapper.selectRulesByTemplateId(id));
        return result;
    }

    @Override
    public List<AlarmTemplate> getTemplateOptions() {
        QueryWrapper<AlarmTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.select("id", "name", "type", "energy_type", "device_type", "target_scope",
                "conditions", "level", "notify_methods", "silence_period", "description");
        queryWrapper.orderByDesc("update_time");
        return this.list(queryWrapper);
    }
}
