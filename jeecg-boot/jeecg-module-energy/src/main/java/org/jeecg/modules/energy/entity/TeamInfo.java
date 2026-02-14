package org.jeecg.modules.energy.entity;

import java.io.Serializable;
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
 * @Description: 班组基础信息表
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@TableName("tb_team_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_team_info对象", description="班组基础信息表")
public class TeamInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**班组编码*/
    @TableField("team_code")
    @ApiModelProperty(value = "班组编码")
    private String teamCode;

    /**班组名称*/
    @TableField("team_name")
    @ApiModelProperty(value = "班组名称")
    private String teamName;

    /**班次类型*/
    @TableField("shift_type")
    @ApiModelProperty(value = "班次类型(早班/中班/晚班/夜班)")
    private String shiftType;

    /**所属组织编码*/
    @TableField("org_code")
    @ApiModelProperty(value = "所属组织编码")
    private String orgCode;

    /**所属组织名称*/
    @TableField("org_name")
    @ApiModelProperty(value = "所属组织名称")
    private String orgName;

    /**班组长*/
    @TableField("team_leader")
    @ApiModelProperty(value = "班组长")
    private String teamLeader;

    /**班组人数*/
    @TableField("team_members")
    @ApiModelProperty(value = "班组人数")
    private Integer teamMembers;

    /**状态*/
    @TableField("status")
    @ApiModelProperty(value = "状态(0-停用,1-启用)")
    private Integer status;

    /**排序号*/
    @TableField("sort_order")
    @ApiModelProperty(value = "排序号")
    private Integer sortOrder;

    /**描述*/
    @TableField("description")
    @ApiModelProperty(value = "描述")
    private String description;

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
