package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 参数配置VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "参数配置VO", description = "参数配置信息")
public class ParameterConfigVO {

    @ApiModelProperty(value = "参数编码", example = "1")
    private Integer paramCode;

    @ApiModelProperty(value = "参数名称", example = "A相电流")
    private String paramName;

    @ApiModelProperty(value = "字段名称", example = "IA")
    private String fieldName;

    @ApiModelProperty(value = "单位", example = "A")
    private String unit;
}