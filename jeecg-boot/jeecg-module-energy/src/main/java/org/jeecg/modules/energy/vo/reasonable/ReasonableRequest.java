package org.jeecg.modules.energy.vo.reasonable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 合理用能 - 查询请求
 */
@Data
@ApiModel(value = "ReasonableRequest", description = "合理用能-查询请求")
public class ReasonableRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仪表ID集合", required = true, example = "[\"yj0001_1202\",\"yj0001_12\"]")
    @NotEmpty(message = "moduleIds不能为空")
    private List<String> moduleIds;

    @ApiModelProperty(value = "开始日期(根据timeType不同传不同格式)", required = true, example = "2025-08-01 或 2025-08 或 2025")
    @NotBlank(message = "startDate不能为空")
    private String startDate;

    @ApiModelProperty(value = "结束日期(根据timeType不同传不同格式)", required = true, example = "2025-08-19 或 2025-08 或 2025")
    @NotBlank(message = "endDate不能为空")
    private String endDate;

    @ApiModelProperty(value = "时间粒度：day|month|year", required = true, example = "day")
    @NotBlank(message = "timeType不能为空")
    private String timeType;
}

