package org.jeecg.modules.energy.entity;

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
 * @Description: 能源数据实体
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Data
@TableName("tb_equ_energy_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_equ_energy_data对象", description="能源数据实体")
public class TbEquEnergyData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;
    
    /**仪表编号*/
    @TableField("module_id")
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    /**采集时间*/
    @TableField("equ_energy_dt")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date equEnergyDt;
    
    /**能源温度*/
    @TableField("energy_temperature")
    @ApiModelProperty(value = "能源温度")
    private BigDecimal energyTemperature;
    
    /**能源压力*/
    @TableField("energy_pressure")
    @ApiModelProperty(value = "能源压力")
    private BigDecimal energyPressure;
    
    /**瞬时值*/
    @TableField("energy_winkvalue")
    @ApiModelProperty(value = "瞬时值")
    private BigDecimal energyWinkvalue;
    
    /**累计值*/
    @TableField("energy_accumulatevalue")
    @ApiModelProperty(value = "累计值")
    private BigDecimal energyAccumulatevalue;
    
    /**备用字段1*/
    @TableField("standby1")
    @ApiModelProperty(value = "备用字段1")
    private String standby1;
    
    /**备用字段2*/
    @TableField("standby2")
    @ApiModelProperty(value = "备用字段2")
    private String standby2;
    
    /**备用字段3*/  
    @TableField("standby3")
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
} 