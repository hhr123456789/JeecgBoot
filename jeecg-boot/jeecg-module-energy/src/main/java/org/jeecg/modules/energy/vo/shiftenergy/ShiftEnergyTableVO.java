package org.jeecg.modules.energy.vo.shiftenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 班次能源表格数据VO
 */
@Data
@ApiModel(value = "班次能源表格数据", description = "班次能源表格数据")
public class ShiftEnergyTableVO {

    @ApiModelProperty(value = "日期")
    private String date;

    @ApiModelProperty(value = "早班能耗")
    private BigDecimal morningConsumption;

    @ApiModelProperty(value = "中班能耗")
    private BigDecimal middleConsumption;

    @ApiModelProperty(value = "晚班能耗")
    private BigDecimal nightConsumption;

    @ApiModelProperty(value = "总能耗")
    private BigDecimal totalConsumption;

    @ApiModelProperty(value = "总费用")
    private BigDecimal totalCost;

    @ApiModelProperty(value = "碳排放")
    private BigDecimal carbon;

    @ApiModelProperty(value = "折标煤")
    private BigDecimal coal;
}
