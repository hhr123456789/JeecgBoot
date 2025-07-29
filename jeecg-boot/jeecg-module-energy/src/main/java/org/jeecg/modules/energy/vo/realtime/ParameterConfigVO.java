package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 参数配置VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ParameterConfigVO", description = "参数配置信息")
public class ParameterConfigVO {
    
    @ApiModelProperty(value = "参数编码")
    private Integer paramCode;
    
    @ApiModelProperty(value = "参数名称")
    private String paramName;
    
    @ApiModelProperty(value = "字段名称")
    private String fieldName;
    
    @ApiModelProperty(value = "单位")
    private String unit;
    
    @ApiModelProperty(value = "参数类别")
    private String category;
    
    @ApiModelProperty(value = "是否默认选中")
    private Boolean isDefault;
    
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;
}
