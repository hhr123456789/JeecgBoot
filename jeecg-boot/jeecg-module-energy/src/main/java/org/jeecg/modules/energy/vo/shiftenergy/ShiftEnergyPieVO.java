package org.jeecg.modules.energy.vo.shiftenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "ShiftEnergyPieVO", description = "\u73ed\u6b21\u7528\u80fd\u5360\u6bd4\u6570\u636eVO")
public class ShiftEnergyPieVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "\u73ed\u6b21\u540d\u79f0")
    private String name;

    @ApiModelProperty(value = "\u80fd\u8017\u503c")
    private Double value;

    @ApiModelProperty(value = "\u989c\u8272")
    private String color;
}
