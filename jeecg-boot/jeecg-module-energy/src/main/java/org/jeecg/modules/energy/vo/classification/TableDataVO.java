package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 表格数据VO
 * @author jeecg
 */
@Data
@ApiModel(value = "表格数据")
public class TableDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "时间")
    private String time;
    
    @ApiModelProperty(value = "电能消耗")
    private BigDecimal electric;
    
    @ApiModelProperty(value = "水能消耗")
    private BigDecimal water;
    
    @ApiModelProperty(value = "燃气消耗")
    private BigDecimal gas;
    
    @ApiModelProperty(value = "电能成本")
    private BigDecimal electricCost;
    
    @ApiModelProperty(value = "水能成本")
    private BigDecimal waterCost;
    
    @ApiModelProperty(value = "燃气成本")
    private BigDecimal gasCost;
    
    @ApiModelProperty(value = "总成本")
    private BigDecimal totalCost;
    
    // 单个能源类型字段（用于筛选单一能源类型时）
    @ApiModelProperty(value = "能源消耗(单一类型)")
    private BigDecimal consumption;
    
    @ApiModelProperty(value = "成本(单一类型)")
    private BigDecimal cost;
    
    @ApiModelProperty(value = "碳排放")
    private BigDecimal carbonEmission;
}