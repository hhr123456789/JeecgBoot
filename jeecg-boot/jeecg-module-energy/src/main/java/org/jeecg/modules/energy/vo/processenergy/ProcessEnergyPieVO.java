package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 饼图数据项VO
 */
@Data
@ApiModel(value = "饼图数据项", description = "饼图数据项")
public class ProcessEnergyPieVO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "数值")
    private Double value;
}
