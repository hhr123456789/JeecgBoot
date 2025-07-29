package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description: 时序数据查询结果VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@ApiModel(value = "TimeSeriesResultVO", description = "时序数据查询结果")
public class TimeSeriesResultVO {
    
    @ApiModelProperty(value = "图表数据")
    private ChartDataVO chartData;
    
    @ApiModelProperty(value = "表格数据")
    private List<TableRowVO> tableData;
    
    @ApiModelProperty(value = "汇总信息")
    private SummaryVO summary;
    
    @Data
    @ApiModel(value = "ChartDataVO", description = "图表数据")
    public static class ChartDataVO {
        @ApiModelProperty(value = "时间标签")
        private List<String> timeLabels;
        
        @ApiModelProperty(value = "数据系列")
        private List<SeriesVO> series;
    }
    
    @Data
    @ApiModel(value = "SeriesVO", description = "数据系列")
    public static class SeriesVO {
        @ApiModelProperty(value = "仪表ID")
        private String moduleId;
        
        @ApiModelProperty(value = "仪表名称")
        private String moduleName;
        
        @ApiModelProperty(value = "参数编码")
        private Integer paramCode;
        
        @ApiModelProperty(value = "参数名称")
        private String paramName;
        
        @ApiModelProperty(value = "单位")
        private String unit;
        
        @ApiModelProperty(value = "数据值")
        private List<Double> data;
        
        @ApiModelProperty(value = "颜色")
        private String color;
    }
    
    @Data
    @ApiModel(value = "TableRowVO", description = "表格行数据")
    public static class TableRowVO {
        @ApiModelProperty(value = "时间")
        private String time;
        
        @ApiModelProperty(value = "时间标签")
        private String timeLabel;
        
        @ApiModelProperty(value = "仪表数据")
        private List<ModuleDataVO> modules;
    }
    
    @Data
    @ApiModel(value = "ModuleDataVO", description = "仪表数据")
    public static class ModuleDataVO {
        @ApiModelProperty(value = "仪表ID")
        private String moduleId;
        
        @ApiModelProperty(value = "仪表名称")
        private String moduleName;
        
        @ApiModelProperty(value = "参数数据")
        private List<ParameterDataVO> parameters;
    }
    
    @Data
    @ApiModel(value = "ParameterDataVO", description = "参数数据")
    public static class ParameterDataVO {
        @ApiModelProperty(value = "参数编码")
        private Integer paramCode;
        
        @ApiModelProperty(value = "参数名称")
        private String paramName;
        
        @ApiModelProperty(value = "数值")
        private Double value;
        
        @ApiModelProperty(value = "单位")
        private String unit;
    }
    
    @Data
    @ApiModel(value = "SummaryVO", description = "汇总信息")
    public static class SummaryVO {
        @ApiModelProperty(value = "总数据点数")
        private Integer totalDataPoints;
        
        @ApiModelProperty(value = "时间范围")
        private String timeRange;
        
        @ApiModelProperty(value = "粒度描述")
        private String granularity;
        
        @ApiModelProperty(value = "仪表数量")
        private Integer moduleCount;
        
        @ApiModelProperty(value = "参数数量")
        private Integer parameterCount;
    }
}
