package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 对比项VO
 * @author jeecg
 * @date 2025-12-05
 */
@Data
@ApiModel(value = "对比项VO", description = "单个对比项的详细数据")
public class ComparisonItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**对比项ID(部门编码或设备ID)*/
    @ApiModelProperty(value = "对比项ID")
    private String id;
    
    /**对比项名称(部门名称或设备名称)*/
    @ApiModelProperty(value = "对比项名称")
    private String name;
    
    /**能源类型*/
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;
    
    /**能源类型名称*/
    @ApiModelProperty(value = "能源类型名称")
    private String energyTypeName;
    
    /**总消耗量*/
    @ApiModelProperty(value = "总消耗量")
    private BigDecimal totalConsumption;
    
    /**总费用*/
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalCost;
    
    /**碳排放量*/
    @ApiModelProperty(value = "碳排放量")
    private BigDecimal carbonEmission;
    
    /**标准煤当量*/
    @ApiModelProperty(value = "标准煤当量")
    private BigDecimal standardCoal;
    
    /**峰时段消耗(仅电力)*/
    @ApiModelProperty(value = "峰时段消耗")
    private BigDecimal peakConsumption;
    
    /**平时段消耗(仅电力)*/
    @ApiModelProperty(value = "平时段消耗")
    private BigDecimal flatConsumption;
    
    /**谷时段消耗(仅电力)*/
    @ApiModelProperty(value = "谷时段消耗")
    private BigDecimal valleyConsumption;
    
    /**设备数量(仅部门对比)*/
    @ApiModelProperty(value = "设备数量")
    private Integer meterCount;
}
