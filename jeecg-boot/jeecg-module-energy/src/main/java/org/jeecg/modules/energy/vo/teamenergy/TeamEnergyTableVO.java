package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班组能源明细表数据VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamEnergyTableVO", description="班组能源明细表数据VO")
public class TeamEnergyTableVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "班组名称")
    private String teamName;

    @ApiModelProperty(value = "班次")
    private String shiftType;

    @ApiModelProperty(value = "统计时间")
    private String statTime;

    @ApiModelProperty(value = "能耗")
    private String consumption;

    @ApiModelProperty(value = "费用")
    private String cost;

    @ApiModelProperty(value = "碳排放")
    private String carbon;

    @ApiModelProperty(value = "标准煤")
    private String coal;

    @ApiModelProperty(value = "峰时段")
    private String peak;

    @ApiModelProperty(value = "平时段")
    private String flat;

    @ApiModelProperty(value = "谷时段")
    private String valley;
}
