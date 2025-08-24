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

    @ApiModelProperty("图表：每个指标一张图")
    private List<Chart> charts;

    @Data
    public static class Chart implements Serializable {
        private String title;     // 折标煤/碳排放/能耗
        private String metric;    // 标识
        private String unit;      // 单位
        private List<Series> series; // 每个仪表为一个系列
    }

    @Data
    public static class Series implements Serializable {
        private String name;      // 仪表名
        private String moduleId;
        private List<Point> data;
    }

    @Data
    public static class Point implements Serializable {
        private String x;
        private Double y;
    }
}

