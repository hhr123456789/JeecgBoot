package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工序能耗表格数据VO
 */
@Data
@ApiModel(value = "工序能耗表格数据", description = "工序能耗表格数据")
public class ProcessEnergyTableVO {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "主工艺过程能耗")
    private Double mainProcess;

    @ApiModelProperty(value = "辅助工艺过程能耗")
    private Double auxiliaryProcess;

    @ApiModelProperty(value = "公用工程系统能耗")
    private Double utilitySystem;

    @ApiModelProperty(value = "附属生产系统能耗")
    private Double subsidiarySystem;

    @ApiModelProperty(value = "总能耗")
    private Double total;
}
