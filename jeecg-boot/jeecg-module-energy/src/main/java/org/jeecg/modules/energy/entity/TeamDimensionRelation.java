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
 * @Description: 班组维度关联表
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@TableName("tb_team_dimension_relation")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_team_dimension_relation对象", description="班组维度关联表")
public class TeamDimensionRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**班组编码*/
    @TableField("team_code")
    @ApiModelProperty(value = "班组编码")
    private String teamCode;

    /**维度编码*/
    @TableField("dimension_code")
    @ApiModelProperty(value = "维度编码(如A01B03)")
    private String dimensionCode;

    /**维度类型*/
    @TableField("dimension_type")
    @ApiModelProperty(value = "维度类型(1-按部门用电,2-按线路用电,3-天然气,4-压缩空气,5-企业用水)")
    private Integer dimensionType;

    /**能源类型*/
    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型(1-电,2-水,8-天然气,5-压缩空气)")
    private Integer energyType;

    /**关联的仪表ID列表*/
    @TableField("module_ids")
    @ApiModelProperty(value = "关联的仪表ID列表(逗号分隔)")
    private String moduleIds;

    /**状态*/
    @TableField("status")
    @ApiModelProperty(value = "状态(0-停用,1-启用)")
    private Integer status;

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
