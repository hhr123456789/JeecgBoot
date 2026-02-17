package org.jeecg.modules.energy.entity.alarm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 告警模板表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Data
@TableName("tb_alarm_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "tb_alarm_template对象", description = "告警模板表")
public class AlarmTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "模板类型：device-设备告警,energy-能源告警")
    private String type;

    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型：1-电,2-水,8-天然气,5-压缩空气")
    private String energyType;

    @TableField("device_type")
    @ApiModelProperty(value = "设备类型（设备告警用）")
    private String deviceType;

    @TableField("target_scope")
    @ApiModelProperty(value = "监控范围（能源告警用）：department/line/workshop/device")
    private String targetScope;

    @ApiModelProperty(value = "告警条件配置JSON数组")
    private String conditions;

    @ApiModelProperty(value = "默认告警级别：high/medium/low")
    private String level;

    @TableField("notify_methods")
    @ApiModelProperty(value = "默认通知方式JSON数组")
    private String notifyMethods;

    @TableField("silence_period")
    @ApiModelProperty(value = "默认静默期（分钟）")
    private Integer silencePeriod;

    @ApiModelProperty(value = "模板说明")
    private String description;

    @TableField("dept_id")
    @ApiModelProperty(value = "所属部门ID")
    private String deptId;

    @ApiModelProperty(value = "状态：1-启用,0-禁用")
    private Integer status;

    @TableField("create_by")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    @TableField("create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField("update_by")
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @TableField("update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    // 非数据库字段 - 使用次数
    @TableField(exist = false)
    @ApiModelProperty(value = "使用次数")
    private Integer usageCount;
}
