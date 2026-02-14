package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班组能源排名数据VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamEnergyRankingVO", description="班组能源排名数据VO")
public class TeamEnergyRankingVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "班组名称")
    private String name;

    @ApiModelProperty(value = "能耗值")
    private Double value;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "排名")
    private Integer rank;
}
