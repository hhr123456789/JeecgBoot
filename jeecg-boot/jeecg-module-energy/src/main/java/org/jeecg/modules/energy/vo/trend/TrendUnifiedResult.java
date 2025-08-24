package org.jeecg.modules.energy.vo.trend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("统一显示-趋势结果")
public class TrendUnifiedResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("显示模式")
    private String displayMode = "unified";

    @ApiModelProperty("时间范围描述")
    private String timeRange;

    @ApiModelProperty("系列")
    private List<Series> series;

    @Data
    public static class Series implements Serializable {
        private String name;      // 系列名（如：折标煤(yj0001_1202) 或 碳排放-2号注塑机）
        private String moduleId;  // 可选
        private String metric;    // energy/standardCoal/carbon
        private String unit;      // kWh/kgce/kgCO2e
        private List<Point> data; // [ ["2025-08", 123.45], ... ]
    }

    @Data
    public static class Point implements Serializable {
        private String x;
        private Double y;
    }
}

