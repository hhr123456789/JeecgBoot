package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 仪表信息VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ModuleInfoVO", description = "仪表基础信息")
public class ModuleInfoVO {
    
    @ApiModelProperty(value = "仪表ID")
    private String moduleId;
    
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;
    
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;
    
    @ApiModelProperty(value = "维度编码")
    private String dimensionCode;
    
    @ApiModelProperty(value = "维度名称")
    private String dimensionName;
    
    @ApiModelProperty(value = "额定功率")
    private Double ratedPower;

    @ApiModelProperty(value = "当前功率")
    private Double currentPower;

    @ApiModelProperty(value = "负荷率")
    private Double loadRate;

    @ApiModelProperty(value = "是否在线")
    private Boolean isOnline;

    @ApiModelProperty(value = "是否启用")
    private String isAction;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;
}
