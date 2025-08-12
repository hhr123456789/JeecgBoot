package org.jeecg.modules.energy.vo.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 仪表信息VO
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Data
@ApiModel("仪表信息VO")
public class ModuleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仪表编号")
    private String moduleId;

    @ApiModelProperty(value = "仪表名称")
    private String moduleName;

    @ApiModelProperty(value = "能源类型")
    private Integer energyType;

    @ApiModelProperty(value = "能源类型名称")
    private String energyTypeName;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "维度名称")
    private String dimensionName;

    @ApiModelProperty(value = "维度ID")
    private String dimensionId;

    @ApiModelProperty(value = "采集器名称")
    private String gatewayCode;

    @ApiModelProperty(value = "仪表ID")
    private String meterId;

    @ApiModelProperty(value = "额定功率")
    private Double ratedPower;
}
