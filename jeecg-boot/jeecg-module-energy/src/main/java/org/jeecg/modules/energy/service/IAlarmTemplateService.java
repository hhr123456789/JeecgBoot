package org.jeecg.modules.energy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.entity.alarm.AlarmTemplate;

import java.util.List;
import java.util.Map;

/**
 * @Description: 告警模板Service接口
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
public interface IAlarmTemplateService extends IService<AlarmTemplate> {

    /**
     * 分页查询模板列表
     */
    IPage<AlarmTemplate> queryPageList(String name, String type, String energyType,
                                        Integer status, Integer pageNo, Integer pageSize);

    /**
     * 保存模板
     */
    void saveTemplate(AlarmTemplate template);

    /**
     * 更新模板
     */
    void updateTemplate(AlarmTemplate template);

    /**
     * 删除模板
     */
    Result<?> deleteTemplate(String id);

    /**
     * 获取模板使用情况
     */
    Map<String, Object> getTemplateUsage(String id);

    /**
     * 获取模板选项列表（用于下拉选择）
     */
    List<AlarmTemplate> getTemplateOptions();
}
