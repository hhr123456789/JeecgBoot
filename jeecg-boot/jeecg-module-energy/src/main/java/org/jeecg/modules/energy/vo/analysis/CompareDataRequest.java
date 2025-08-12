package org.jeecg.modules.energy.vo.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description: 能源分析对比查询请求
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Data
@ApiModel("能源分析对比查询请求")
public class CompareDataRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仪表编号", required = true)
    @NotBlank(message = "仪表编号不能为空")
    private String moduleId;

    @ApiModelProperty(value = "时间类型：day/month/year", required = true)
    @NotBlank(message = "时间类型不能为空")
    private String timeType;

    @ApiModelProperty(value = "开始时间", required = true)
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @ApiModelProperty(value = "对比类型：current(当期)/compare(同比)")
    private String compareType;
}
