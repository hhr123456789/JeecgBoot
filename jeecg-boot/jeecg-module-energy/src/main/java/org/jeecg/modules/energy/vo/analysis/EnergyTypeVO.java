package org.jeecg.modules.energy.vo.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 能源类型VO
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Data
@ApiModel("能源类型VO")
public class EnergyTypeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "能源类型")
    private Integer energyType;

    @ApiModelProperty(value = "能源类型名称")
    private String energyTypeName;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "图标")
    private String icon;
}
