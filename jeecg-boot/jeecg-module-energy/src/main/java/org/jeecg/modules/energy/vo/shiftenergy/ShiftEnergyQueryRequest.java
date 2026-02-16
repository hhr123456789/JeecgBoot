package org.jeecg.modules.energy.vo.shiftenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班次用能查询请求参数VO
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "ShiftEnergyQueryRequest", description = "班次用能查询请求参数VO")
public class ShiftEnergyQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "维度编码(如A01B03)")
    private String dimensionCode;

    @ApiModelProperty(value = "维度类型(1-按部门用电,2-按线路用电,3-天然气,4-压缩空气,5-企业用水)")
    private Integer dimensionType;

    @ApiModelProperty(value = "时间维度(day/month/year)")
    private String timeUnit;

    @ApiModelProperty(value = "查询日期(格式根据timeUnit: yyyy-MM-dd/yyyy-MM/yyyy)")
    private String queryDate;

    @ApiModelProperty(value = "班次类型(all/morning/middle/night)")
    private String shiftType;

    @ApiModelProperty(value = "能源类型(all/1/2/8/5)")
    private String energyType;
}
