package org.jeecg.modules.energy.vo.shiftenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班次用能统计数据VO
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ShiftEnergyStatisticsVO", description = "班次用能统计数据VO")
public class ShiftEnergyStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总能耗")
    private String totalConsumption;

    @ApiModelProperty(value = "早班能耗")
    private String morningConsumption;

    @ApiModelProperty(value = "中班能耗")
    private String middleConsumption;

    @ApiModelProperty(value = "晚班能耗")
    private String nightConsumption;

    @ApiModelProperty(value = "总费用")
    private String totalCost;

    @ApiModelProperty(value = "碳排放")
    private String totalCarbon;

    @ApiModelProperty(value = "折标煤")
    private String totalCoal;

    @ApiModelProperty(value = "能源单位")
    private String energyUnit;
}
