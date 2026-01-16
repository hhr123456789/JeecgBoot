package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 对比数据系列VO
 * @author jeecg
 * @date 2025-12-05
 */
@Data
@ApiModel(value = "对比数据系列VO", description = "对比图表的数据系列")
public class ComparisonSeriesVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**系列名称*/
    @ApiModelProperty(value = "系列名称")
    private String name;
    
    /**图表类型: bar(柱状图) / line(折线图)*/
    @ApiModelProperty(value = "图表类型")
    private String type;
    
    /**数据值列表*/
    @ApiModelProperty(value = "数据值列表")
    private List<BigDecimal> data;
    
    /**单位*/
    @ApiModelProperty(value = "单位")
    private String unit;
}
