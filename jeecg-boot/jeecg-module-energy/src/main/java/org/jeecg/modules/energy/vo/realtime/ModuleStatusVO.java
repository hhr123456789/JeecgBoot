package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description: 仪表状态VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ModuleStatusVO", description = "仪表实时状态信息")
public class ModuleStatusVO {
    
    @ApiModelProperty(value = "仪表ID")
    private String moduleId;
    
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;
    
    @ApiModelProperty(value = "是否在线")
    private Boolean isOnline;
    
    @ApiModelProperty(value = "最后更新时间")
    private String lastUpdateTime;
    
    @ApiModelProperty(value = "参数状态列表")
    private List<ParameterStatusVO> parameters;
    
    @Data
    @ApiModel(value = "ParameterStatusVO", description = "参数状态")
    public static class ParameterStatusVO {
        @ApiModelProperty(value = "参数编码")
        private Integer paramCode;
        
        @ApiModelProperty(value = "参数名称")
        private String paramName;
        
        @ApiModelProperty(value = "当前值")
        private Double currentValue;
        
        @ApiModelProperty(value = "单位")
        private String unit;
        
        @ApiModelProperty(value = "状态")
        private String status;
        
        @ApiModelProperty(value = "更新时间")
        private String updateTime;
    }
}
