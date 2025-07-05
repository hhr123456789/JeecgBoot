package org.jeecg.modules.energy.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 实时监控数据VO
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Data
@ApiModel(value="实时监控数据VO", description="实时监控数据VO")
public class RealTimeDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**仪表名称*/
    @ApiModelProperty(value = "仪表名称")
    private String module_name;
    
    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String module_id;
    
    /**额定功率*/
    @ApiModelProperty(value = "额定功率")
    private Double rated_power;
    
    /**能源类型*/
    @ApiModelProperty(value = "能源类型")
    private Integer energy_type;
    
    /**日用量*/
    @ApiModelProperty(value = "日用量")
    private BigDecimal dailyPower;
    
    // 电力数据字段
    
    /**采集时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date Equ_Electric_DT;
    
    /**负荷状态*/
    @ApiModelProperty(value = "负荷状态")
    private String loadStatus;
    
    /**负荷率*/
    @ApiModelProperty(value = "负荷率")
    private BigDecimal loadRate;
    
    /**总功率因素*/
    @ApiModelProperty(value = "总功率因素")
    private BigDecimal PFS;
    
    /**频率*/
    @ApiModelProperty(value = "频率")
    private BigDecimal HZ;
    
    /**总有功功率*/
    @ApiModelProperty(value = "总有功功率")
    private BigDecimal pp;
    
    /**A相电压*/
    @ApiModelProperty(value = "A相电压")
    private BigDecimal UA;
    
    /**B相电压*/
    @ApiModelProperty(value = "B相电压")
    private BigDecimal UB;
    
    /**C相电压*/
    @ApiModelProperty(value = "C相电压")
    private BigDecimal UC;
    
    /**A相电流*/
    @ApiModelProperty(value = "A相电流")
    private BigDecimal IA;
    
    /**B相电流*/
    @ApiModelProperty(value = "B相电流")
    private BigDecimal IB;
    
    /**C相电流*/
    @ApiModelProperty(value = "C相电流")
    private BigDecimal IC;
    
    /**A相功率因素*/
    @ApiModelProperty(value = "A相功率因素")
    private BigDecimal PFa;
    
    /**B相功率因素*/
    @ApiModelProperty(value = "B相功率因素")
    private BigDecimal PFb;
    
    /**C相功率因素*/
    @ApiModelProperty(value = "C相功率因素")
    private BigDecimal PFc;
    
    /**A相功率*/
    @ApiModelProperty(value = "A相功率")
    private BigDecimal Pa;
    
    /**B相功率*/
    @ApiModelProperty(value = "B相功率")
    private BigDecimal Pb;
    
    /**C相功率*/
    @ApiModelProperty(value = "C相功率")
    private BigDecimal Pc;
    
    /**正向有功总电能*/
    @ApiModelProperty(value = "正向有功总电能")
    private BigDecimal KWH;
    
    /**正向无功总电能*/
    @ApiModelProperty(value = "正向无功总电能")
    private BigDecimal KVARH;
    
    // 能源数据字段
    
    /**采集时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date equ_energy_dt;
    
    /**温度*/
    @ApiModelProperty(value = "温度")
    private Double energy_temperature;
    
    /**压力*/
    @ApiModelProperty(value = "压力")
    private Double energy_pressure;
    
    /**瞬时流量*/
    @ApiModelProperty(value = "瞬时流量")
    private BigDecimal energy_winkvalue;
    
    /**累计值*/
    @ApiModelProperty(value = "累计值")
    private BigDecimal energy_accumulatevalue;
} 