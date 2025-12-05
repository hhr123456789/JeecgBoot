package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// 导入相关的VO类
import org.jeecg.modules.energy.vo.classification.StatisticsDataVO;
import org.jeecg.modules.energy.vo.classification.PieChartDataVO;
import org.jeecg.modules.energy.vo.classification.TableDataVO;

/**
 * 企业分类分区统计汇总VO
 * @author jeecg
 */
@Data
@ApiModel(value = "企业分类分区统计汇总VO", description = "企业分类分区统计汇总数据")
public class ClassificationSummaryVO {

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

    @ApiModelProperty(value = "碳排放量")
    private BigDecimal totalCarbonEmission;

    @ApiModelProperty(value = "峰时段消耗")
    private BigDecimal peakConsumption;

    @ApiModelProperty(value = "峰时段费用")
    private BigDecimal peakCost;

    @ApiModelProperty(value = "平时段消耗")
    private BigDecimal flatConsumption;

    @ApiModelProperty(value = "平时段费用")
    private BigDecimal flatCost;

    @ApiModelProperty(value = "谷时段消耗")
    private BigDecimal valleyConsumption;

    @ApiModelProperty(value = "谷时段费用")
    private BigDecimal valleyCost;

    @ApiModelProperty(value = "尖时段消耗")
    private BigDecimal cuspConsumption;

    @ApiModelProperty(value = "尖时段费用")
    private BigDecimal cuspCost;

    @ApiModelProperty(value = "仪表数量")
    private Integer meterCount;

    @ApiModelProperty(value = "统计数据")
    private StatisticsDataVO statisticsData;

    @ApiModelProperty(value = "饼图数据")
    private PieChartDataVO pieChartData;

    @ApiModelProperty(value = "表格数据")
    private List<TableDataVO> tableData;
}