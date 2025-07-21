package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 仪表信息VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "仪表信息VO", description = "仪表信息VO")
public class ModuleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;

    /**仪表名称*/
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;

    /**维度编码*/
    @ApiModelProperty(value = "维度编码")
    private String orgCode;

    /**维度名称*/
    @ApiModelProperty(value = "维度名称")
    private String departName;

    /**能源类型*/
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;

    /**是否启用*/
    @ApiModelProperty(value = "是否启用")
    private String isAction;
}
