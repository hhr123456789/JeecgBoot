package org.jeecg.modules.energy.vo.consumption;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "ConsumptionUnifiedResult", description = "设备能源统计-统一显示结果")
public class ConsumptionUnifiedResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "显示模式", example = "unified")
    private String displayMode = "unified";

    @ApiModelProperty(value = "时间范围")
    private String timeRange;

    @ApiModelProperty(value = "数据系列")
    private List<Series> series;

    @Data
    @ApiModel(value = "Series", description = "数据系列")
    public static class Series implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "系列名称", example = "1#空压机-能耗")
        private String name;

        @ApiModelProperty(value = "指标类型", example = "energy")
        private String metric;

        @ApiModelProperty(value = "单位", example = "kWh")
        private String unit;

        @ApiModelProperty(value = "设备ID")
        private String moduleId;

        @ApiModelProperty(value = "设备名称")
        private String moduleName;

        @ApiModelProperty(value = "数据点")
        private List<Point> data;
    }

    @Data
    @ApiModel(value = "Point", description = "数据点")
    public static class Point implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "时间标签", example = "2025-08-01")
        private String x;

        @ApiModelProperty(value = "数值", example = "1234.5")
        private Double y;
    }
}