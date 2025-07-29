package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description: 负荷时序数据结果VO
 * @Author: jeecg-boot
 * @Date: 2025-07-26
 * @Version: V1.0
 */
@Data
@ApiModel(value = "LoadTimeSeriesResultVO", description = "负荷时序数据结果")
public class LoadTimeSeriesResultVO {
    
    @ApiModelProperty(value = "有功功率图表数据")
    private LoadChartDataVO powerChartData;
    
    @ApiModelProperty(value = "负荷率图表数据")
    private LoadChartDataVO loadRateChartData;
    
    @ApiModelProperty(value = "表格数据")
    private List<LoadTableRowVO> tableData;
    
    @ApiModelProperty(value = "汇总信息")
    private LoadSummaryVO summary;
    
    /**
     * 负荷图表数据VO
     */
    @Data
    @ApiModel(value = "LoadChartDataVO", description = "负荷图表数据")
    public static class LoadChartDataVO {
        
        @ApiModelProperty(value = "图表标题")
        private String title;
        
        @ApiModelProperty(value = "时间标签")
        private List<String> timeLabels;
        
        @ApiModelProperty(value = "数据系列")
        private List<LoadSeriesVO> series;
    }
    
    /**
     * 负荷数据系列VO
     */
    @Data
    @ApiModel(value = "LoadSeriesVO", description = "负荷数据系列")
    public static class LoadSeriesVO {
        
        @ApiModelProperty(value = "仪表ID")
        private String moduleId;
        
        @ApiModelProperty(value = "仪表名称")
        private String moduleName;
        
        @ApiModelProperty(value = "单位")
        private String unit;
        
        @ApiModelProperty(value = "数据")
        private List<Double> data;
        
        @ApiModelProperty(value = "颜色")
        private String color;
    }
    
    /**
     * 负荷表格行数据VO
     */
    @Data
    @ApiModel(value = "LoadTableRowVO", description = "负荷表格行数据")
    public static class LoadTableRowVO {
        
        @ApiModelProperty(value = "时间")
        private String time;
        
        @ApiModelProperty(value = "时间标签")
        private String timeLabel;
        
        @ApiModelProperty(value = "仪表数据")
        private List<LoadModuleDataVO> modules;
    }
    
    /**
     * 负荷仪表数据VO
     */
    @Data
    @ApiModel(value = "LoadModuleDataVO", description = "负荷仪表数据")
    public static class LoadModuleDataVO {
        
        @ApiModelProperty(value = "仪表ID")
        private String moduleId;
        
        @ApiModelProperty(value = "仪表名称")
        private String moduleName;
        
        @ApiModelProperty(value = "额定功率")
        private Double ratedPower;
        
        @ApiModelProperty(value = "当前功率")
        private Double currentPower;
        
        @ApiModelProperty(value = "负荷率")
        private Double loadRate;
        
        @ApiModelProperty(value = "功率单位")
        private String powerUnit;
        
        @ApiModelProperty(value = "负荷率单位")
        private String loadRateUnit;
    }
    
    /**
     * 负荷汇总信息VO
     */
    @Data
    @ApiModel(value = "LoadSummaryVO", description = "负荷汇总信息")
    public static class LoadSummaryVO {
        
        @ApiModelProperty(value = "总数据点数")
        private Integer totalDataPoints;
        
        @ApiModelProperty(value = "时间范围")
        private String timeRange;
        
        @ApiModelProperty(value = "时间粒度")
        private String granularity;
        
        @ApiModelProperty(value = "仪表数量")
        private Integer moduleCount;
        
        @ApiModelProperty(value = "数据类型")
        private String dataType;
    }
}
