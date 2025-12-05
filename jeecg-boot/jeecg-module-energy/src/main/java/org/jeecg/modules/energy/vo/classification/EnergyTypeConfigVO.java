package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 能源类型配置VO
 * @author jeecg
 */
@Data
@ApiModel(value = "能源类型配置VO", description = "能源类型配置信息")
public class EnergyTypeConfigVO {

    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "能源类型编码")
    private Integer energyType;

    @ApiModelProperty(value = "能源类型名称")
    private String energyName;

    @ApiModelProperty(value = "计量单位")
    private String energyUnit;

    @ApiModelProperty(value = "单价")
    private BigDecimal pricePerUnit;

    @ApiModelProperty(value = "碳排放系数")
    private BigDecimal carbonFactor;

    @ApiModelProperty(value = "标准煤系数")
    private BigDecimal coalFactor;

    @ApiModelProperty(value = "状态(1:启用 0:禁用)")
    private String status;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;
}