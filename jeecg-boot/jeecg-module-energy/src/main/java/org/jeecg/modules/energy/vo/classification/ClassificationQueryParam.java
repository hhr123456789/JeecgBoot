package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分类分区查询参数
 * @author jeecg
 */
@Data
@ApiModel(value = "分类分区查询参数")
public class ClassificationQueryParam implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "部门编码", required = true)
    @NotBlank(message = "部门编码不能为空")
    private String orgCode;
    
    @ApiModelProperty(value = "能源类型(all/1/2/3)", example = "all")
    private String energyType = "all";
    
    @ApiModelProperty(value = "时间维度(day/month/year)", example = "month")
    private String timeDimension = "month";
    
    @ApiModelProperty(value = "开始日期", example = "2024-01-01")
    private String startDate;
    
    @ApiModelProperty(value = "结束日期", example = "2024-01-31")
    private String endDate;
    
    @ApiModelProperty(value = "是否包含子部门", example = "true")
    private Boolean includeChildren = true;
}