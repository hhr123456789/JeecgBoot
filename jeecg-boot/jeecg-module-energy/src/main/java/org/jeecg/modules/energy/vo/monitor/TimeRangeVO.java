package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 时间范围VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "时间范围VO", description = "时间范围VO")
public class TimeRangeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**开始时间*/
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    /**结束时间*/
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    /**查询间隔*/
    @ApiModelProperty(value = "查询间隔")
    private String interval;
}
