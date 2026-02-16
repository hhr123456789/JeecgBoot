package org.jeecg.modules.energy.vo.shiftenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 班次能源趋势数据VO
 */
@Data
@ApiModel(value = "班次能源趋势数据", description = "班次能源趋势数据")
public class ShiftEnergyTrendVO {

    @ApiModelProperty(value = "X轴数据")
    private List<String> xAxisData;

    @ApiModelProperty(value = "系列数据")
    private List<SeriesData> seriesData;

    @Data
    public static class SeriesData {
        @ApiModelProperty(value = "名称")
        private String name;

        @ApiModelProperty(value = "数据")
        private List<Object> data;

        @ApiModelProperty(value = "颜色")
        private String color;
    }
}
