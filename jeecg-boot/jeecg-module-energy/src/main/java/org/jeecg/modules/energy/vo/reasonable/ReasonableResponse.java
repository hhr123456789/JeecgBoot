package org.jeecg.modules.energy.vo.reasonable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 合理用能 - 返回结果
 */
@Data
@ApiModel(value = "ReasonableResponse", description = "合理用能-返回结果")
public class ReasonableResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("汇总")
    private Summary summary;

    @ApiModelProperty("占比环图")
    private List<RingItem> ratio;

    @ApiModelProperty("总能耗趋势（柱状）")
    private List<TotalTrendItem> totalTrend;

    @ApiModelProperty("尖峰平谷趋势（折线）")
    private TouTrend touTrend;

    @Data
    public static class Summary implements Serializable {
        private Double cuspCount;   // 尖
        private Double peakCount;   // 峰
        private Double levelCount;  // 平
        private Double valleyCount; // 谷
        private Double totalCount;  // 总能耗
    }

    @Data
    public static class RingItem implements Serializable {
        private String name;
        private Double value;
        private Double percent;
    }

    @Data
    public static class TotalTrendItem implements Serializable {
        private String date; // x轴标签（yyyy-MM-dd / yyyy-MM / yyyy）
        private Double energyCount;
    }

    @Data
    public static class TouTrend implements Serializable {
        private List<Point> cusp;
        private List<Point> peak;
        private List<Point> level;
        private List<Point> valley;
    }

    @Data
    public static class Point implements Serializable {
        private String x;
        private Double y;
    }
}

