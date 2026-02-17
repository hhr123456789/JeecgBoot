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
import java.util.Date;
import java.util.List;

/**
 * @Description: 告警规则Service实现
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class AlarmRuleServiceImpl extends ServiceImpl<AlarmRuleMapper, AlarmRule> implements IAlarmRuleService {

    @Autowired
    private AlarmRuleMapper alarmRuleMapper;

    @Autowired
    private AlarmTemplateMapper alarmTemplateMapper;

    @Override
    public IPage<AlarmRule> queryPageList(String name, String ruleType, String energyType,
                                           Integer status, String deptId, Integer pageNo, Integer pageSize) {
        Page<AlarmRule> page = new Page<>(pageNo, pageSize);

        // 使用自定义查询获取带模板名称的列表
        List<AlarmRule> list = alarmRuleMapper.selectRuleListWithTemplate(name, ruleType, energyType, status, deptId);

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
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        rule.setUpdateBy(loginUser != null ? loginUser.getUsername() : "system");
        rule.setUpdateTime(new Date());
        this.updateById(rule);
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
