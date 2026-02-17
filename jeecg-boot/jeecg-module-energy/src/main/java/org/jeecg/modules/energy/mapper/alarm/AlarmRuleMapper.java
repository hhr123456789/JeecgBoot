package org.jeecg.modules.energy.mapper.alarm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.energy.entity.alarm.AlarmRule;

import java.util.List;
import java.util.Map;

/**
 * @Description: 告警规则Mapper接口
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {

    /**
     * 查询规则列表（带模板名称）
     */
    @Select("<script>" +
            "SELECT r.*, t.name as template_name " +
            "FROM tb_alarm_rule r " +
            "LEFT JOIN tb_alarm_template t ON r.template_id = t.id " +
            "WHERE 1=1 " +
            "<if test='name != null and name != \"\"'> AND r.name LIKE CONCAT('%', #{name}, '%') </if>" +
            "<if test='ruleType != null and ruleType != \"\" and ruleType != \"all\"'> AND r.rule_type = #{ruleType} </if>" +
            "<if test='energyType != null and energyType != \"\" and energyType != \"all\"'> AND r.energy_type = #{energyType} </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='deptId != null and deptId != \"\"'> AND r.dept_id = #{deptId} </if>" +
            "ORDER BY r.update_time DESC" +
            "</script>")
    List<AlarmRule> selectRuleListWithTemplate(@Param("name") String name,
                                                @Param("ruleType") String ruleType,
                                                @Param("energyType") String energyType,
                                                @Param("status") Integer status,
                                                @Param("deptId") String deptId);

    /**
     * 根据模板ID查询使用该模板的规则
     */
    @Select("SELECT id, name, status, create_time FROM tb_alarm_rule WHERE template_id = #{templateId} ORDER BY create_time DESC")
    List<Map<String, Object>> selectRulesByTemplateId(@Param("templateId") String templateId);
}
