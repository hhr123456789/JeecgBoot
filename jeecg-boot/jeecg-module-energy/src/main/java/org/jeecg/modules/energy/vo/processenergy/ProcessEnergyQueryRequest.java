package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工序能耗查询请求参数
 */
@Data
@ApiModel(value = "工序能耗查询请求参数", description = "工序能耗查询请求参数")
public class ProcessEnergyQueryRequest {

    @ApiModelProperty(value = "生产线ID")
    private String lineId;

    @ApiModelProperty(value = "时间单位: day/month/year")
    private String timeUnit;

    @ApiModelProperty(value = "日期")
    private String date;

    @ApiModelProperty(value = "能源类型: electricity/water/gas/steam")
    private String energyType;
}
