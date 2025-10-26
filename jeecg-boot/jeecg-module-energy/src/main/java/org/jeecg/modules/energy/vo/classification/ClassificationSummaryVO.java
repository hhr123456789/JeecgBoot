package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类分区统计汇总
 * @author jeecg
 */
@Data
@ApiModel(value = "分类分区统计汇总")
public class ClassificationSummaryVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "统计数据")
    private StatisticsDataVO statisticsData;
    
    @ApiModelProperty(value = "饼图数据")
    private PieChartDataVO pieChartData;
    
    @ApiModelProperty(value = "表格数据")
    private List<TableDataVO> tableData;
}