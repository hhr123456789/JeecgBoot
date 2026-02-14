package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班组能源统计数据VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamEnergyStatisticsVO", description="班组能源统计数据VO")
public class TeamEnergyStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总能耗")
    private String totalConsumption;

    @ApiModelProperty(value = "总费用")
    private String totalCost;

    @ApiModelProperty(value = "碳排放")
    private String carbonEmission;

    @ApiModelProperty(value = "标准煤")
    private String standardCoal;

    @ApiModelProperty(value = "能源单位")
    private String energyUnit;
}
