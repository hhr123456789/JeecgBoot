package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班组能源查询请求参数VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamEnergyQueryRequest", description="班组能源查询请求参数VO")
public class TeamEnergyQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "组织编码")
    private String orgCode;

    @ApiModelProperty(value = "班组编码(all表示全部)")
    private String teamCode;

    @ApiModelProperty(value = "时间维度(day/month/year)")
    private String timeUnit;

    @ApiModelProperty(value = "查询日期(格式根据timeUnit: yyyy-MM-dd/yyyy-MM/yyyy)")
    private String queryDate;

    @ApiModelProperty(value = "能源类型(all/1/2/8/5)")
    private String energyType;

    @ApiModelProperty(value = "维度类型(1-按部门用电,2-按线路用电,3-天然气,4-压缩空气,5-企业用水)")
    private Integer dimensionType;

    @ApiModelProperty(value = "指标类型(consumption-能耗/cost-费用/carbon-碳排放)")
    private String metricType;
}
