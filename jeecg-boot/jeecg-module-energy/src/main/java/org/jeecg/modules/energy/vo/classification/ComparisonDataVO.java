package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 横向对比数据VO
 * 用于展示同级部门或同级设备的能耗对比数据
 * @author jeecg
 * @date 2025-12-05
 */
@Data
@ApiModel(value = "横向对比数据VO", description = "横向对比数据,用于展示同级部门或设备的能耗对比")
public class ComparisonDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**对比类型: department(部门对比) / device(设备对比)*/
    @ApiModelProperty(value = "对比类型: department(部门对比) / device(设备对比)")
    private String comparisonType;
    
    /**父级部门编码(当comparisonType=department时使用)*/
    @ApiModelProperty(value = "父级部门编码")
    private String parentOrgCode;
    
    /**父级部门名称*/
    @ApiModelProperty(value = "父级部门名称")
    private String parentOrgName;
    
    /**X轴数据(部门名称或设备名称列表)*/
    @ApiModelProperty(value = "X轴数据(部门名称或设备名称列表)")
    private List<String> xAxisData;
    
    /**Y轴数据系列*/
    @ApiModelProperty(value = "Y轴数据系列")
    private List<ComparisonSeriesVO> seriesData;
    
    /**对比项列表(用于表格展示)*/
    @ApiModelProperty(value = "对比项列表")
    private List<ComparisonItemVO> comparisonItems;
}
