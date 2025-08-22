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
 * @Description: 年统计表实体
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Data
@TableName("tb_ep_equ_energy_yearcount")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_ep_equ_energy_yearcount对象", description="年统计表实体")
public class TbEpEquEnergyYearcount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;
    
    @TableField("module_id")
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    @TableField("dt")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date dt;
    
    @TableField("energy_count")
    @ApiModelProperty(value = "能耗值")
    private BigDecimal energyCount;
    
    @TableField("strat_count")
    @ApiModelProperty(value = "开始值")
    private BigDecimal stratCount;
    
    @TableField("end_count")
    @ApiModelProperty(value = "结束值")
    private BigDecimal endCount;
    
    // 分时电能（仅电力）：尖、峰、平、谷 —— 开始值/结束值/区间能耗
    @TableField("start_cusp_count")
    private BigDecimal startCuspCount;
    @TableField("end_cusp_count")
    private BigDecimal endCuspCount;
    @TableField("cusp_count")
    private BigDecimal cuspCount;

    @TableField("start_peak_count")
    private BigDecimal startPeakCount;
    @TableField("end_peak_count")
    private BigDecimal endPeakCount;
    @TableField("peak_count")
    private BigDecimal peakCount;

    @TableField("start_level_count")
    private BigDecimal startLevelCount;
    @TableField("end_level_count")
    private BigDecimal endLevelCount;
    @TableField("level_count")
    private BigDecimal levelCount;

    @TableField("start_valley_count")
    private BigDecimal startValleyCount;
    @TableField("end_valley_count")
    private BigDecimal endValleyCount;
    // 注意：表字段为 Valley_count（V 大写）
    @TableField("Valley_count")
    private BigDecimal valleyCount;

    @TableField("standby1")
    @ApiModelProperty(value = "备用字段1")
    private String standby1;

    @TableField("standby2")
    @ApiModelProperty(value = "备用字段2")
    private String standby2;

    @TableField("standby3")
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
}