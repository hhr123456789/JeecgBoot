package org.jeecg.modules.energy.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 日统计数据
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Data
@TableName("tb_ep_equ_energy_daycount")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_ep_equ_energy_daycount对象", description="日统计数据")
public class TbEpEquEnergyDaycount implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;
    
    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    /**统计日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "统计日期")
    private Date dt;
    
    /**能耗值*/
    @ApiModelProperty(value = "能耗值")
    private BigDecimal energyCount;
    
    /**开始值*/
    @ApiModelProperty(value = "开始值")
    private BigDecimal stratCount;
    
    /**结束值*/
    @ApiModelProperty(value = "结束值")
    private BigDecimal endCount;
    
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