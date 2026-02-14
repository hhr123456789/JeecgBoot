package org.jeecg.modules.energy.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 班组排班表
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@TableName("tb_team_shift_schedule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_team_shift_schedule对象", description="班组排班表")
public class TeamShiftSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**班组编码*/
    @TableField("team_code")
    @ApiModelProperty(value = "班组编码")
    private String teamCode;

    /**排班日期*/
    @TableField("shift_date")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "排班日期")
    private Date shiftDate;

    /**班次类型*/
    @TableField("shift_type")
    @ApiModelProperty(value = "班次类型(早班/中班/晚班/夜班)")
    private String shiftType;

    /**开始时间*/
    @TableField("start_time")
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    /**结束时间*/
    @TableField("end_time")
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    /**工作时长*/
    @TableField("work_hours")
    @ApiModelProperty(value = "工作时长(小时)")
    private BigDecimal workHours;

    /**状态*/
    @TableField("status")
    @ApiModelProperty(value = "状态(0-取消,1-正常)")
    private Integer status;

    /**备注*/
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**创建人*/
    @TableField("create_by")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**创建时间*/
    @TableField("create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**更新人*/
    @TableField("update_by")
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**更新时间*/
    @TableField("update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**备用字段1*/
    @TableField("standby1")
    @ApiModelProperty(value = "备用字段1")
    private String standby1;

    /**备用字段2*/
    @TableField("standby2")
    @ApiModelProperty(value = "备用字段2")
    private String standby2;

    /**备用字段3*/
    @TableField("standby3")
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
}
