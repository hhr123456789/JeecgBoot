package org.jeecg.modules.energy.entity.classification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 企业能源类型配置表
 * @author jeecg
 */
@Data
@TableName("tb_energy_type_config")
@ApiModel(value="企业能源类型配置表", description="企业能源类型配置表")
public class TbEnergyTypeConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**能源类型编码*/
    @Excel(name = "能源类型编码", width = 15)
    @ApiModelProperty(value = "能源类型编码")
    private Integer energyType;
    
    /**能源类型名称*/
    @Excel(name = "能源类型名称", width = 15)
    @ApiModelProperty(value = "能源类型名称")
    private String energyName;
    
    /**计量单位(kWh/m³等)*/
    @Excel(name = "计量单位", width = 15)
    @ApiModelProperty(value = "计量单位")
    private String energyUnit;
    
    /**单价*/
    @Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
    private BigDecimal pricePerUnit;
    
    /**碳排放系数*/
    @Excel(name = "碳排放系数", width = 15)
    @ApiModelProperty(value = "碳排放系数")
    private BigDecimal carbonFactor;
    
    /**标准煤系数*/
    @Excel(name = "标准煤系数", width = 15)
    @ApiModelProperty(value = "标准煤系数")
    private BigDecimal coalFactor;
    
    /**状态(1:启用 0:禁用)*/
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    private String status;
    
    /**排序*/
    @Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private Integer sortOrder;
    
    /**创建人*/
    @Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    /**更新人*/
    @Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    
    /**备注*/
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;
}