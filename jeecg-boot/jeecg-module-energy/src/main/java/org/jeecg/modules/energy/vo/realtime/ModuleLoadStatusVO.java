package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 仪表负荷状态VO
 * @Author: jeecg-boot
 * @Date: 2025-07-26
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ModuleLoadStatusVO", description = "仪表负荷状态")
public class ModuleLoadStatusVO {
    
    @ApiModelProperty(value = "仪表ID")
    private String moduleId;
    
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;
    
    @ApiModelProperty(value = "是否在线")
    private Boolean isOnline;
    
    @ApiModelProperty(value = "最后更新时间")
    private String lastUpdateTime;
    
    @ApiModelProperty(value = "额定功率")
    private Double ratedPower;
    
    @ApiModelProperty(value = "当前功率")
    private Double currentPower;
    
    @ApiModelProperty(value = "负荷率")
    private Double loadRate;
    
    @ApiModelProperty(value = "功率单位")
    private String powerUnit;
    
    @ApiModelProperty(value = "负荷率单位")
    private String loadRateUnit;
    
    @ApiModelProperty(value = "状态")
    private String status;
    
    @ApiModelProperty(value = "负荷等级")
    private String loadLevel;
}
