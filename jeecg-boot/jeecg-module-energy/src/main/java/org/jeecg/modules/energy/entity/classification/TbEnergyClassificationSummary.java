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
 * 企业分类分区统计汇总表
 * @author jeecg
 */
@Data
@TableName("tb_energy_classification_summary")
@ApiModel(value="企业分类分区统计汇总表", description="企业分类分区统计汇总表")
public class TbEnergyClassificationSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**部门编码*/
    @Excel(name = "部门编码", width = 15)
    @ApiModelProperty(value = "部门编码")
    private String orgCode;
    
    /**部门名称*/
    @Excel(name = "部门名称", width = 15)
    @ApiModelProperty(value = "部门名称")
    private String orgName;
    
    /**父级部门编码*/
    @Excel(name = "父级部门编码", width = 15)
    @ApiModelProperty(value = "父级部门编码")
    private String parentOrgCode;
    
    /**能源类型(1:电能 2:水能 3:燃气)*/
    @Excel(name = "能源类型", width = 15)
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;
    
    /**能源类型名称*/
    @Excel(name = "能源类型名称", width = 15)
    @ApiModelProperty(value = "能源类型名称")
    private String energyTypeName;
    
    /**统计日期*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "统计日期")
    private Date statDate;
    
    /**统计月份(YYYY-MM)*/
    @Excel(name = "统计月份", width = 15)
    @ApiModelProperty(value = "统计月份")
    private String statMonth;
    
    /**统计年份(YYYY)*/
    @Excel(name = "统计年份", width = 15)
    @ApiModelProperty(value = "统计年份")
    private String statYear;
    
    /**时间维度(day/month/year)*/
    @Excel(name = "时间维度", width = 15)
    @ApiModelProperty(value = "时间维度")
    private String timeDimension;
    
    /**总消耗量*/
    @Excel(name = "总消耗量", width = 15)
    @ApiModelProperty(value = "总消耗量")
    private BigDecimal totalConsumption;
    
    /**总费用*/
    @Excel(name = "总费用", width = 15)
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalCost;
    
    /**碳排放量*/
    @Excel(name = "碳排放量", width = 15)
    @ApiModelProperty(value = "碳排放量")
    private BigDecimal carbonEmission;
    
    /**标准煤当量*/
    @Excel(name = "标准煤当量", width = 15)
    @ApiModelProperty(value = "标准煤当量")
    private BigDecimal standardCoal;
    
    /**峰时段消耗*/
    @Excel(name = "峰时段消耗", width = 15)
    @ApiModelProperty(value = "峰时段消耗")
    private BigDecimal peakConsumption;
    
    /**峰时段费用*/
    @Excel(name = "峰时段费用", width = 15)
    @ApiModelProperty(value = "峰时段费用")
    private BigDecimal peakCost;
    
    /**平时段消耗*/
    @Excel(name = "平时段消耗", width = 15)
    @ApiModelProperty(value = "平时段消耗")
    private BigDecimal flatConsumption;
    
    /**平时段费用*/
    @Excel(name = "平时段费用", width = 15)
    @ApiModelProperty(value = "平时段费用")
    private BigDecimal flatCost;
    
    /**谷时段消耗*/
    @Excel(name = "谷时段消耗", width = 15)
    @ApiModelProperty(value = "谷时段消耗")
    private BigDecimal valleyConsumption;
    
    /**谷时段费用*/
    @Excel(name = "谷时段费用", width = 15)
    @ApiModelProperty(value = "谷时段费用")
    private BigDecimal valleyCost;
    
    /**仪表数量*/
    @Excel(name = "仪表数量", width = 15)
    @ApiModelProperty(value = "仪表数量")
    private Integer meterCount;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}