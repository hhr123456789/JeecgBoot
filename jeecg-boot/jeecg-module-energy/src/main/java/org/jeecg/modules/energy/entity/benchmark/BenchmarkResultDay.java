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
 * @Description: 能效对标结果日表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Data
@TableName("tb_benchmark_result_day")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_benchmark_result_day对象", description="能效对标结果日表")
public class BenchmarkResultDay implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**对象编码*/
    @TableField("target_code")
    @ApiModelProperty(value = "对象编码")
    private String targetCode;

    /**对象名称*/
    @TableField("target_name")
    @ApiModelProperty(value = "对象名称")
    private String targetName;

    /**统计日期*/
    @TableField("stat_date")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "统计日期")
    private Date statDate;

    /**能源类型*/
    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型")
    private String energyType;

    /**能源消耗量*/
    @TableField("energy_consumption")
    @ApiModelProperty(value = "能源消耗量")
    private BigDecimal energyConsumption;

    /**能源费用*/
    @TableField("energy_cost")
    @ApiModelProperty(value = "能源费用")
    private BigDecimal energyCost;

    /**产量*/
    @TableField("production_output")
    @ApiModelProperty(value = "产量")
    private BigDecimal productionOutput;

    /**能耗强度*/
    @TableField("energy_intensity")
    @ApiModelProperty(value = "能耗强度")
    private BigDecimal energyIntensity;

    /**对标值*/
    @TableField("benchmark_value")
    @ApiModelProperty(value = "对标值")
    private BigDecimal benchmarkValue;

    /**偏差率*/
    @TableField("deviation_rate")
    @ApiModelProperty(value = "偏差率")
    private BigDecimal deviationRate;

    /**排名*/
    @TableField("ranking")
    @ApiModelProperty(value = "排名")
    private Integer ranking;

    /**创建时间*/
    @TableField("create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**更新时间*/
    @TableField("update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
