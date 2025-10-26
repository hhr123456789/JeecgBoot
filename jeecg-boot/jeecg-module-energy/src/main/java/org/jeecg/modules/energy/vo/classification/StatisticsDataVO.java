package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计数据VO
 * @author jeecg
 */
@Data
@ApiModel(value = "统计数据")
public class StatisticsDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "总消耗量")
    private BigDecimal totalConsumption;
    
    @ApiModelProperty(value = "电能消耗")
    private BigDecimal electricConsumption;
    
    @ApiModelProperty(value = "水能消耗")
    private BigDecimal waterConsumption;
    
    @ApiModelProperty(value = "燃气消耗")
    private BigDecimal gasConsumption;
    
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalCost;
    
    @ApiModelProperty(value = "总碳排放量")
    private BigDecimal totalCarbonEmission;
}