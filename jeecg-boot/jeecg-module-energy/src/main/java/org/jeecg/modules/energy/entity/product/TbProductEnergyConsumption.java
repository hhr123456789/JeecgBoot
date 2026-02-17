package org.jeecg.modules.energy.entity.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 产品能耗统计表
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@TableName("tb_product_energy_consumption")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_product_energy_consumption对象", description="产品能耗统计表")
public class TbProductEnergyConsumption implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("product_code")
    @ApiModelProperty(value = "产品编码")
    private String productCode;

    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型 (1:电 2:水 3:天然气 4:蒸汽 5:压缩空气)")
    private Integer energyType;

    @TableField("energy_type_name")
    @ApiModelProperty(value = "能源类型名称")
    private String energyTypeName;

    @TableField("energy_unit")
    @ApiModelProperty(value = "能源单位")
    private String energyUnit;

    @TableField("stat_date")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "统计日期")
    private Date statDate;

    @TableField("stat_month")
    @ApiModelProperty(value = "统计月份")
    private String statMonth;

    @TableField("stat_year")
    @ApiModelProperty(value = "统计年份")
    private String statYear;

    @TableField("time_dimension")
    @ApiModelProperty(value = "时间维度")
    private String timeDimension;

    @TableField("total_consumption")
    @ApiModelProperty(value = "总能耗")
    private BigDecimal totalConsumption;

    @TableField("total_production")
    @ApiModelProperty(value = "总产量")
    private BigDecimal totalProduction;

    @TableField("qualified_production")
    @ApiModelProperty(value = "合格产量")
    private BigDecimal qualifiedProduction;

    @TableField("qualification_rate")
    @ApiModelProperty(value = "合格率")
    private BigDecimal qualificationRate;

    @TableField("unit_consumption")
    @ApiModelProperty(value = "单位产品能耗")
    private BigDecimal unitConsumption;

    @TableField("energy_price")
    @ApiModelProperty(value = "能源单价")
    private BigDecimal energyPrice;

    @TableField("total_cost")
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalCost;

    @TableField("carbon_emission")
    @ApiModelProperty(value = "碳排放量")
    private BigDecimal carbonEmission;

    @TableField("standard_coal")
    @ApiModelProperty(value = "标准煤当量")
    private BigDecimal standardCoal;

    @TableField("sys_org_code")
    @ApiModelProperty(value = "所属部门编码")
    private String sysOrgCode;

    @TableField("create_by")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    @TableField("create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField("update_by")
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @TableField("update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
