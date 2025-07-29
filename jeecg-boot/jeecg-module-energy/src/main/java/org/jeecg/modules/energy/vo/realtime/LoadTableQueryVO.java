package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 负荷表格查询VO
 * @Author: jeecg-boot
 * @Date: 2025-07-26
 * @Version: V1.0
 */
@Data
@ApiModel(value = "LoadTableQueryVO", description = "负荷表格查询参数")
public class LoadTableQueryVO {
    
    @ApiModelProperty(value = "仪表ID列表", required = true, example = "[\"yj0001_1202\", \"yj0001_1203\"]")
    @NotEmpty(message = "仪表ID列表不能为空")
    private List<String> moduleIds;
    
    @ApiModelProperty(value = "时间类型", required = true, example = "day", notes = "day/month/year")
    @NotNull(message = "时间类型不能为空")
    private String timeType;
    
    @ApiModelProperty(value = "开始时间", required = true, example = "2025-07-25 00:00:00")
    @NotNull(message = "开始时间不能为空")
    private String startTime;
    
    @ApiModelProperty(value = "结束时间", required = true, example = "2025-07-25 23:59:59")
    @NotNull(message = "结束时间不能为空")
    private String endTime;
    
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;
    
    @ApiModelProperty(value = "每页条数", example = "100")
    private Integer pageSize = 100;
}
