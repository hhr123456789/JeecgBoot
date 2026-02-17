package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工序能耗统计数据VO
 */
@Data
@ApiModel(value = "工序能耗统计数据", description = "工序能耗统计数据")
public class ProcessEnergyStatisticsVO {

    @ApiModelProperty(value = "总能耗")
    private Double totalConsumption;

    @ApiModelProperty(value = "生产用能")
    private Double productionConsumption;

    @ApiModelProperty(value = "辅助用能")
    private Double auxiliaryConsumption;

    @ApiModelProperty(value = "单位产品能耗")
    private Double unitConsumption;
}
