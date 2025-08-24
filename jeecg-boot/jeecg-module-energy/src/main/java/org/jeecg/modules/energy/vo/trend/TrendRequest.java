package org.jeecg.modules.energy.vo.trend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "TrendRequest", description = "能耗趋势-查询请求(碳排放/折标煤)")
public class TrendRequest implements Serializable {
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

    @ApiModelProperty(value = "指标：energy(能耗),standardCoal(折标煤),carbon(碳排放)。默认[standardCoal,carbon]")
    private List<String> metrics; // 可为空
}

