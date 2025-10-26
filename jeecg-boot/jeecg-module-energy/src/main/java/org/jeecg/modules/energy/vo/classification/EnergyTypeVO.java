package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 能源类型VO
 * @author jeecg
 */
@Data
@ApiModel(value = "能源类型")
public class EnergyTypeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "能源类型编码")
    private Integer energyType;
    
    @ApiModelProperty(value = "能源类型名称")
    private String energyName;
    
    @ApiModelProperty(value = "计量单位")
    private String energyUnit;
    
    @ApiModelProperty(value = "单价")
    private Double pricePerUnit;
    
    @ApiModelProperty(value = "碳排放系数")
    private Double carbonFactor;
    
    @ApiModelProperty(value = "标准煤系数")
    private Double coalFactor;
}