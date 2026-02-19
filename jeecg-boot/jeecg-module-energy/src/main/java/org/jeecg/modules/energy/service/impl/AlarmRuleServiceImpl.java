package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.energy.entity.alarm.AlarmRule;
import org.jeecg.modules.energy.entity.alarm.AlarmTemplate;
import org.jeecg.modules.energy.mapper.alarm.AlarmRuleMapper;
import org.jeecg.modules.energy.mapper.alarm.AlarmTemplateMapper;
import org.jeecg.modules.energy.service.IAlarmRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Description: 告警规则Service实现
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class AlarmRuleServiceImpl extends ServiceImpl<AlarmRuleMapper, AlarmRule> implements IAlarmRuleService {

    private static final Set<String> ALARM_TYPES = new HashSet<>(Arrays.asList("device", "energy"));
    private static final Set<String> ENERGY_TYPES = new HashSet<>(Arrays.asList("1", "2", "8", "5"));
    private static final Set<String> DEVICE_TYPES = new HashSet<>(Arrays.asList("GFMT", "CEC", "ACOP", "WMCT", "METE", "ELEV"));
    private static final Set<String> LEVEL_TYPES = new HashSet<>(Arrays.asList("high", "medium", "low"));

    @Autowired
    private AlarmRuleMapper alarmRuleMapper;

    @Autowired
    private AlarmTemplateMapper alarmTemplateMapper;

    @Override
    public IPage<AlarmRule> queryPageList(String name, String ruleType, String energyType,
                                           Integer status, String deptId, String targetNodeId,
                                           Integer dimensionType, Integer pageNo, Integer pageSize) {
        Page<AlarmRule> page = new Page<>(pageNo, pageSize);

        // 使用自定义查询获取带模板名称的列表
        List<AlarmRule> list = alarmRuleMapper.selectRuleListWithTemplate(
                name, ruleType, energyType, status, deptId, targetNodeId, dimensionType);

        // 手动分页
        int total = list.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<AlarmRule> records = fromIndex < total ? list.subList(fromIndex, toIndex) : list.subList(0, 0);

        page.setRecords(records);
        page.setTotal(total);

        return page;
    }

    @Override
    public void saveRule(AlarmRule rule) {
        validateAndNormalizeRule(rule, false);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        rule.setCreateBy(loginUser != null ? loginUser.getUsername() : "system");
        rule.setCreateTime(new Date());
        rule.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        rule.setUpdateTime(new Date());
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
        this.save(rule);
    }

    @Override
    public void updateRule(AlarmRule rule) {
        validateAndNormalizeRule(rule, true);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        rule.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        rule.setUpdateTime(new Date());
        this.updateById(rule);
    }

    private void validateAndNormalizeRule(AlarmRule rule, boolean update) {
        if (rule == null) {
            throw new RuntimeException("规则参数不能为空");
        }
        if (update && !StringUtils.hasText(rule.getId())) {
            throw new RuntimeException("规则ID不能为空");
        }
        if (!StringUtils.hasText(rule.getName())) {
            throw new RuntimeException("规则名称不能为空");
        }
        if (!StringUtils.hasText(rule.getRuleType()) || !ALARM_TYPES.contains(rule.getRuleType())) {
            throw new RuntimeException("告警类型不合法");
        }
        if (!StringUtils.hasText(rule.getEnergyType()) || !ENERGY_TYPES.contains(rule.getEnergyType())) {
            throw new RuntimeException("能源类型不合法");
        }
        if (!StringUtils.hasText(rule.getConditions())) {
            throw new RuntimeException("告警条件不能为空");
        }
        if (StringUtils.hasText(rule.getLevel()) && !LEVEL_TYPES.contains(rule.getLevel())) {
            throw new RuntimeException("告警等级不合法");
        }

        if (!StringUtils.hasText(rule.getTargetNodeId())) {
            throw new RuntimeException("必须选择具体监控设备");
        }
        if (!StringUtils.hasText(rule.getTargetNodeName())) {
            rule.setTargetNodeName(rule.getTargetNodeId());
        }

        if ("device".equals(rule.getRuleType())) {
            if (!StringUtils.hasText(rule.getTargetType()) || !DEVICE_TYPES.contains(rule.getTargetType())) {
                throw new RuntimeException("设备告警规则必须选择设备类型");
            }
            if (!StringUtils.hasText(rule.getTargetScope())) {
                rule.setTargetScope("selected");
            }
        } else {
            rule.setTargetType(null);
            rule.setTargetScope("device");
        }

        if (StringUtils.hasText(rule.getTemplateId())) {
            AlarmTemplate template = alarmTemplateMapper.selectById(rule.getTemplateId());
            if (template == null) {
                throw new RuntimeException("关联模板不存在");
            }
            if (!Objects.equals(template.getType(), rule.getRuleType())) {
                throw new RuntimeException("规则类型与模板类型不一致");
            }
            if (!Objects.equals(template.getEnergyType(), rule.getEnergyType())) {
                throw new RuntimeException("规则能源类型与模板不一致");
            }
            if ("device".equals(rule.getRuleType()) && !StringUtils.hasText(rule.getTargetType())) {
                rule.setTargetType(template.getDeviceType());
            }
        }

        if (!StringUtils.hasText(rule.getLevel())) {
            rule.setLevel("medium");
        }
        if (rule.getSilencePeriod() == null || rule.getSilencePeriod() < 0) {
            rule.setSilencePeriod(60);
        }
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
    }

    @Override
    public Result<?> deleteRule(String id) {
        this.removeById(id);
        return Result.OK("删除成功");
    }

    @Override
    public Result<?> deleteRuleBatch(String ids) {
        if (!StringUtils.hasText(ids)) {
            return Result.error("请选择要删除的规则");
        }
        List<String> idList = Arrays.asList(ids.split(","));
        this.removeByIds(idList);
        return Result.OK("删除成功");
    }

    @Override
    public Result<?> changeStatus(String id, Integer status) {
        AlarmRule rule = this.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        rule.setStatus(status);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        rule.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        rule.setUpdateTime(new Date());
        this.updateById(rule);
        return Result.OK(status == 1 ? "规则已启用" : "规则已禁用");
    }
}
