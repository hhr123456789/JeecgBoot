package org.jeecg.modules.energy.mapper.alarm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.energy.entity.alarm.AlarmTemplate;

/**
 * @Description: 告警模板Mapper接口
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface AlarmTemplateMapper extends BaseMapper<AlarmTemplate> {

    /**
     * 统计模板被规则引用的次数
     */
    @Select("SELECT COUNT(*) FROM tb_alarm_rule WHERE template_id = #{templateId}")
    Integer countUsageByTemplateId(@Param("templateId") String templateId);
}
