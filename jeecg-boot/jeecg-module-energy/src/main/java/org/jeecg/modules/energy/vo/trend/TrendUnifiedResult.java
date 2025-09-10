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

    @ApiModelProperty("系列（长度=设备数×指标数）")
    private List<Series> series;

    @Data
    public static class Series implements Serializable {
        private String name;       // 系列名：设备名-指标名（例如：1#空压机-折标煤）
        private String moduleId;   // 设备ID
        private String moduleName; // 设备名称
        private String metric;     // energy/standardCoal/carbon
        private String unit;       // kWh/kgce/kgCO2e
        private List<Point> data;  // [{x:"2025-08-01", y:123.45}, ...]
    }

    @Data
    public static class Point implements Serializable {
        private String x;
        private Double y;
    }
}

