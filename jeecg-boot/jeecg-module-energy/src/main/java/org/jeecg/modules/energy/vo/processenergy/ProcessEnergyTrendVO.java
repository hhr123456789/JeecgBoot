package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 趋势图数据VO
 */
@Data
@ApiModel(value = "趋势图数据", description = "趋势图数据")
public class ProcessEnergyTrendVO {

    @ApiModelProperty(value = "X轴数据")
    private List<String> xAxisData;

    @ApiModelProperty(value = "系列数据")
    private List<SeriesData> series;

    @Data
    public static class SeriesData {
        @ApiModelProperty(value = "系列名称")
        private String name;

        @ApiModelProperty(value = "图表类型")
        private String type = "line";

        @ApiModelProperty(value = "数据")
        private List<Double> data;
    }
}
