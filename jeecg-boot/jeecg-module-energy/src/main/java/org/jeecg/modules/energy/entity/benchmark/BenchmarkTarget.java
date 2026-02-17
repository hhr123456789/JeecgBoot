package org.jeecg.modules.energy.entity.benchmark;

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
 * @Description: 能效对标对象表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Data
@TableName("tb_benchmark_target")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_benchmark_target对象", description="能效对标对象表")
public class BenchmarkTarget implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**配置ID*/
    @TableField("config_id")
    @ApiModelProperty(value = "配置ID")
    private String configId;

    /**对象编码*/
    @TableField("target_code")
    @ApiModelProperty(value = "对象编码")
    private String targetCode;

    /**对象名称*/
    @TableField("target_name")
    @ApiModelProperty(value = "对象名称")
    private String targetName;

    /**对象类型*/
    @TableField("target_type")
    @ApiModelProperty(value = "对象类型")
    private String targetType;

    /**父级编码*/
    @TableField("parent_code")
    @ApiModelProperty(value = "父级编码")
    private String parentCode;

    /**维度ID*/
    @TableField("dimension_id")
    @ApiModelProperty(value = "维度ID")
    private String dimensionId;

    /**排序号*/
    @TableField("sort_order")
    @ApiModelProperty(value = "排序号")
    private Integer sortOrder;

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
}
