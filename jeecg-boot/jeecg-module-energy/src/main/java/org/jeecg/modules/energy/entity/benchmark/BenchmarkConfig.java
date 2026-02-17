package org.jeecg.modules.energy.entity.benchmark;

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
 * @Description: 能效对标配置表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Data
@TableName("tb_benchmark_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_benchmark_config对象", description="能效对标配置表")
public class BenchmarkConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**配置编码*/
    @TableField("config_code")
    @ApiModelProperty(value = "配置编码")
    private String configCode;

    /**配置名称*/
    @TableField("config_name")
    @ApiModelProperty(value = "配置名称")
    private String configName;

    /**对标类型*/
    @TableField("benchmark_type")
    @ApiModelProperty(value = "对标类型")
    private Integer benchmarkType;

    /**能源类型*/
    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型")
    private String energyType;

    /**指标类型*/
    @TableField("indicator_type")
    @ApiModelProperty(value = "指标类型")
    private String indicatorType;

    /**单位*/
    @TableField("unit")
    @ApiModelProperty(value = "单位")
    private String unit;

    /**基准值*/
    @TableField("baseline_value")
    @ApiModelProperty(value = "基准值")
    private BigDecimal baselineValue;

    /**目标值*/
    @TableField("target_value")
    @ApiModelProperty(value = "目标值")
    private BigDecimal targetValue;

    /**预警阈值*/
    @TableField("warning_threshold")
    @ApiModelProperty(value = "预警阈值")
    private BigDecimal warningThreshold;

    /**状态*/
    @TableField("status")
    @ApiModelProperty(value = "状态(0-停用,1-启用)")
    private Integer status;

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
}
