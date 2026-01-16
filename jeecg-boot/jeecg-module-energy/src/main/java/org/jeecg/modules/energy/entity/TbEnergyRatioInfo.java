package org.jeecg.modules.energy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 能源比例信息表
 * @author jeecg
 * @date 2025-12-05
 */
@Data
@TableName("tb_energy_ratio_info")
@ApiModel(value="能源比例信息表", description="能源比例信息表，存储各类能源的系数配置和单位信息")
public class TbEnergyRatioInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "能量比例记录唯一标识符")
    private String id;
    
    /**能源类型*/
    @Excel(name = "能源类型", width = 15)
    @ApiModelProperty(value = "能源类型编码")
    private Integer isenergyType;
    
    /**能源名称*/
    @Excel(name = "能源名称", width = 15)
    @ApiModelProperty(value = "能源名称")
    private String energyName;
    
    /**能源计量单位*/
    @Excel(name = "计量单位", width = 15)
    @ApiModelProperty(value = "能源计量单位")
    private String energyUnit;
    
    /**折标煤系数*/
    @Excel(name = "折标煤系数", width = 15)
    @ApiModelProperty(value = "折标煤系数")
    private BigDecimal zbmxsValue;
    
    /**碳排放系数*/
    @Excel(name = "碳排放系数", width = 15)
    @ApiModelProperty(value = "碳排放系数")
    private BigDecimal tpfxsValue;
    
    /**单价*/
    @Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
    private BigDecimal pricePerUnit;
    
    /**备注信息*/
    @Excel(name = "备注", width = 30)
    @ApiModelProperty(value = "备注信息")
    private String isremark;
    
    /**备用字段1*/
    @ApiModelProperty(value = "备用字段1")
    private String standby1;
    
    /**备用字段2*/
    @ApiModelProperty(value = "备用字段2")
    private String standby2;
    
    /**备用字段3*/
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
}
