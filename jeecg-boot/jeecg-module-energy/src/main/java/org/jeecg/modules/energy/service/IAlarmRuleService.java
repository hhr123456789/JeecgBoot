package org.jeecg.modules.energy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.alarm.AlarmRule;

/**
 * @Description: 告警规则Service接口
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
public interface IAlarmRuleService extends IService<AlarmRule> {

    /**
     * 分页查询规则列表
     */
    IPage<AlarmRule> queryPageList(String name, String ruleType, String energyType,
                                    Integer status, String deptId, Integer pageNo, Integer pageSize);

    /**
     * 保存规则
     */
    void saveRule(AlarmRule rule);

    /**
     * 更新规则
     */
    void updateRule(AlarmRule rule);

    /**
     * 删除规则
     */
    Result<?> deleteRule(String id);

    /**
     * 批量删除规则
     */
    Result<?> deleteRuleBatch(String ids);

    /**
     * 修改规则状态
     */
    Result<?> changeStatus(String id, Integer status);
}
