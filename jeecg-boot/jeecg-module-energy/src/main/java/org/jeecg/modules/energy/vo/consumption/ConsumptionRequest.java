package org.jeecg.modules.energy.vo.consumption;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "ConsumptionRequest", description = "设备能源统计-查询请求")
public class ConsumptionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仪表ID集合", required = true)
    @NotEmpty(message = "moduleIds不能为空")
    private List<String> moduleIds;

    @ApiModelProperty(value = "开始日期(yyyy-MM-dd|yyyy-MM|yyyy)", required = true)
    @NotBlank(message = "startDate不能为空")
    private String startDate;

    @ApiModelProperty(value = "结束日期(yyyy-MM-dd|yyyy-MM|yyyy)", required = true)
    @NotBlank(message = "endDate不能为空")
    private String endDate;

    @ApiModelProperty(value = "时间粒度 day|month|year", required = true, example = "month")
    @NotBlank(message = "timeType不能为空")
    private String timeType;

    @ApiModelProperty(value = "显示方式(1=统一显示,2=分开显示)", required = true, example = "1")
    private Integer displayMode = 1;
}