package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 分类统计查询结果VO
 * @author jeecg
 */
@Data
@ApiModel(value = "分类统计查询结果VO", description = "按部门+能源类型统计的结果")
public class ClassificationStatisticsVO {

    @ApiModelProperty(value = "部门编码")
    private String orgCode;

    @ApiModelProperty(value = "能源类型")
    private Integer energyType;

    @ApiModelProperty(value = "统计日期")
    private Date statDate;

    @ApiModelProperty(value = "统计月份")
    private String statMonth;

    @ApiModelProperty(value = "统计年份")
    private String statYear;

    @ApiModelProperty(value = "总消耗量")
    private BigDecimal totalConsumption;

    @ApiModelProperty(value = "峰时段消耗")
    private BigDecimal peakConsumption;

    @ApiModelProperty(value = "平时段消耗")
    private BigDecimal flatConsumption;

    @ApiModelProperty(value = "谷时段消耗")
    private BigDecimal valleyConsumption;

    @ApiModelProperty(value = "尖时段消耗")
    private BigDecimal cuspConsumption;

    @ApiModelProperty(value = "仪表数量")
    private Integer meterCount;
}