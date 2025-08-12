package org.jeecg.modules.energy.vo.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 能源分析对比响应数据
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Data
@ApiModel("能源分析对比响应数据")
public class CompareDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("汇总数据")
    private SummaryData summary;

    @ApiModelProperty("图表数据")
    private ChartData chartData;

    @ApiModelProperty("表格数据")
    private List<TableData> tableData;

    @ApiModelProperty("仪表信息")
    private ModuleInfo moduleInfo;

    @Data
    @ApiModel("汇总数据")
    public static class SummaryData implements Serializable {
        @ApiModelProperty("总消耗量")
        private Double totalConsumption;

        @ApiModelProperty("对比期消耗量")
        private Double previousConsumption;

        @ApiModelProperty("增长率(%)")
        private Double growthRate;

        @ApiModelProperty("单位")
        private String unit;
    }

    @Data
    @ApiModel("图表数据")
    public static class ChartData implements Serializable {
        @ApiModelProperty("时间轴数据")
        private List<String> categories;

        @ApiModelProperty("系列数据")
        private List<SeriesData> series;
    }

    @Data
    @ApiModel("系列数据")
    public static class SeriesData implements Serializable {
        @ApiModelProperty("系列名称")
        private String name;

        @ApiModelProperty("数据值")
        private List<Double> data;

        @ApiModelProperty("单位")
        private String unit;
    }

    @Data
    @ApiModel("表格数据")
    public static class TableData implements Serializable {
        @ApiModelProperty("日期")
        private String date;

        @ApiModelProperty("本期消耗量")
        private Double currentConsumption;

        @ApiModelProperty("对比期消耗量")
        private Double previousConsumption;

        @ApiModelProperty("差值")
        private Double difference;

        @ApiModelProperty("增长率(%)")
        private Double growthRate;
    }

    @Data
    @ApiModel("仪表信息")
    public static class ModuleInfo implements Serializable {
        @ApiModelProperty("仪表编号")
        private String moduleId;

        @ApiModelProperty("仪表名称")
        private String moduleName;

        @ApiModelProperty("能源类型")
        private Integer energyType;

        @ApiModelProperty("单位")
        private String unit;

        @ApiModelProperty("维度名称")
        private String dimensionName;
    }
}
