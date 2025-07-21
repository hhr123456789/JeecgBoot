package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 统一显示结果VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "统一显示结果VO", description = "统一显示结果VO")
public class UnifiedDisplayResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**显示模式*/
    @ApiModelProperty(value = "显示模式")
    private String displayMode;

    /**时间范围*/
    @ApiModelProperty(value = "时间范围")
    private TimeRangeVO timeRange;

    /**系列数据列表*/
    @ApiModelProperty(value = "系列数据列表")
    private List<SeriesDataVO> series;
}
