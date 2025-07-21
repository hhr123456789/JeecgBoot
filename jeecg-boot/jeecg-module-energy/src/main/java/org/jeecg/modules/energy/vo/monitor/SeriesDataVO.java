package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 系列数据VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "系列数据VO", description = "系列数据VO")
public class SeriesDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**系列名称*/
    @ApiModelProperty(value = "系列名称")
    private String name;

    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;

    /**仪表名称*/
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;

    /**参数名称*/
    @ApiModelProperty(value = "参数名称")
    private String parameter;

    /**参数编码*/
    @ApiModelProperty(value = "参数编码")
    private String parameterCode;

    /**单位*/
    @ApiModelProperty(value = "单位")
    private String unit;

    /**数据点列表 [时间, 数值]*/
    @ApiModelProperty(value = "数据点列表")
    private List<Object[]> data;
}
