package org.jeecg.modules.energy.vo.trend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("分开显示-趋势结果")
public class TrendSeparatedResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("显示模式")
    private String displayMode = "separated";

    @ApiModelProperty("时间范围描述")
    private String timeRange;

    @ApiModelProperty("图表：每个设备一张图，图内是所选指标多条曲线")
    private List<Chart> charts;

    @Data
    public static class Chart implements Serializable {
        @ApiModelProperty("图标题，建议为设备名")
        private String title;
        @ApiModelProperty("设备ID")
        private String moduleId;
        @ApiModelProperty("设备名称")
        private String moduleName;
        // 兼容旧结构：若需要也可设置单一metric/unit，但推荐在Series中区分
        private String metric;    // 可选
        private String unit;      // 可选
        @ApiModelProperty("曲线集合：每个指标一条曲线")
        private List<Series> series;
    }

    @Data
    public static class Series implements Serializable {
        @ApiModelProperty("曲线名称，例如：折标煤/碳排放")
        private String name;
        @ApiModelProperty("指标标识：energy/standardCoal/carbon")
        private String metric;
        @ApiModelProperty("单位：kWh/kgce/kgCO2e")
        private String unit;
        private String moduleId; // 可选
        private List<Point> data;
    }

    @Data
    public static class Point implements Serializable {
        private String x;
        private Double y;
    }
}

