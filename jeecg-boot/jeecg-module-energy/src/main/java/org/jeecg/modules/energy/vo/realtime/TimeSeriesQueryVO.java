package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 时序数据查询请求VO
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Data
@ApiModel(value = "TimeSeriesQueryVO", description = "时序数据查询请求参数")
public class TimeSeriesQueryVO {
    
    @ApiModelProperty(value = "仪表ID列表", required = true, example = "[\"yj0001_13\", \"yj0001_14\"]")
    @NotEmpty(message = "仪表ID列表不能为空")
    private List<String> moduleIds;
    
    @ApiModelProperty(value = "参数编码列表", required = true, example = "[1, 2, 7]")
    @NotEmpty(message = "参数编码列表不能为空")
    private List<Integer> parameters;
    
    @ApiModelProperty(value = "时间粒度", required = true, example = "day", allowableValues = "day,month,year")
    @NotNull(message = "时间粒度不能为空")
    private String timeGranularity;
    
    @ApiModelProperty(value = "查询日期", required = true, example = "2025-07-25")
    @NotNull(message = "查询日期不能为空")
    private String queryDate;
    
    @ApiModelProperty(value = "开始时间（可选，用于精确时间范围）", example = "2025-07-25 00:00:00")
    private String startTime;
    
    @ApiModelProperty(value = "结束时间（可选，用于精确时间范围）", example = "2025-07-25 23:59:59")
    private String endTime;
}
