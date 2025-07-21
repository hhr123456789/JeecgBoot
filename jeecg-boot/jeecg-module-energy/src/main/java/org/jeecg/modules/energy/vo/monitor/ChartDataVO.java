package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 图表数据VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "图表数据VO", description = "图表数据VO")
public class ChartDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**图表标题*/
    @ApiModelProperty(value = "图表标题")
    private String title;

    /**参数名称*/
    @ApiModelProperty(value = "参数名称")
    private String parameter;

    /**参数编码*/
    @ApiModelProperty(value = "参数编码")
    private String parameterCode;

    /**单位*/
    @ApiModelProperty(value = "单位")
    private String unit;

    /**系列数据列表*/
    @ApiModelProperty(value = "系列数据列表")
    private List<SeriesDataVO> series;
}
