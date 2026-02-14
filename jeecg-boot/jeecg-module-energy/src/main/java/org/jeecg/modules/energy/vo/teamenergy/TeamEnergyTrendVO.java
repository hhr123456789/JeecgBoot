package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 班组能源趋势图数据VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamEnergyTrendVO", description="班组能源趋势图数据VO")
public class TeamEnergyTrendVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "X轴数据")
    private List<String> xAxisData;

    @ApiModelProperty(value = "系列数据")
    private List<SeriesData> seriesData;

    @Data
    @ApiModel(value="SeriesData", description="系列数据")
    public static class SeriesData implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "系列名称")
        private String name;

        @ApiModelProperty(value = "系列类型(bar/line)")
        private String type;

        @ApiModelProperty(value = "数据")
        private List<Double> data;

        @ApiModelProperty(value = "颜色")
        private String color;
    }
}
