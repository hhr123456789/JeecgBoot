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
 * @Description: 电力数据实体
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Data
@TableName("tb_equ_ele_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_equ_ele_data对象", description="电力数据实体")
public class TbEquEleData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;
    
    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    /**采集时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date equElectricDT;
    
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
    
    /**uaw*/
    @ApiModelProperty(value = "uaw")
    private BigDecimal uaw;
    
    /**ubw*/
    @ApiModelProperty(value = "ubw")
    private BigDecimal ubw;
    
    /**ucw*/
    @ApiModelProperty(value = "ucw")
    private BigDecimal ucw;
    
    /**iaw*/
    @ApiModelProperty(value = "iaw")
    private BigDecimal iaw;
    
    /**ibw*/
    @ApiModelProperty(value = "ibw")
    private BigDecimal ibw;
    
    /**icw*/
    @ApiModelProperty(value = "icw")
    private BigDecimal icw;
    
    /**总有功功率*/
    @ApiModelProperty(value = "总有功功率")
    private BigDecimal pp;
    
    /**总无在功率*/
    @ApiModelProperty(value = "总无在功率")
    private BigDecimal qq;
    
    /**总视在功率*/
    @ApiModelProperty(value = "总视在功率")
    private BigDecimal ss;
    
    /**总功率因素*/
    @ApiModelProperty(value = "总功率因素")
    private BigDecimal PFS;
    
    /**频率*/
    @ApiModelProperty(value = "频率")
    private BigDecimal HZ;
    
    /**f*/
    @ApiModelProperty(value = "f")
    private BigDecimal ff;
    
    /**A相功率*/
    @ApiModelProperty(value = "A相功率")
    private BigDecimal Pa;
    
    /**B相功率*/
    @ApiModelProperty(value = "B相功率")
    private BigDecimal Pb;
    
    /**C相功率*/
    @ApiModelProperty(value = "C相功率")
    private BigDecimal Pc;
    
    /**A相无功功率*/
    @ApiModelProperty(value = "A相无功功率")
    private BigDecimal Qa;
    
    /**B相无功功率*/
    @ApiModelProperty(value = "B相无功功率")
    private BigDecimal Qb;
    
    /**C相无功功率*/
    @ApiModelProperty(value = "C相无功功率")
    private BigDecimal Qc;
    
    /**A相视在功率*/
    @ApiModelProperty(value = "A相视在功率")
    private BigDecimal Sa;
    
    /**B相视在功率*/
    @ApiModelProperty(value = "B相视在功率")
    private BigDecimal Sb;
    
    /**C相视在功率*/
    @ApiModelProperty(value = "C相视在功率")
    private BigDecimal Sc;
    
    /**A相功率因素*/
    @ApiModelProperty(value = "A相功率因素")
    private BigDecimal PFa;
    
    /**B相功率因素*/
    @ApiModelProperty(value = "B相功率因素")
    private BigDecimal PFb;
    
    /**C相功率因素*/
    @ApiModelProperty(value = "C相功率因素")
    private BigDecimal PFc;
    
    /**正向有功总电能*/
    @ApiModelProperty(value = "正向有功总电能")
    private BigDecimal KWH;
    
    /**正向无功总电能*/
    @ApiModelProperty(value = "正向无功总电能")
    private BigDecimal KVARH;
    
    /**uUnbl*/
    @ApiModelProperty(value = "uUnbl")
    private BigDecimal uuUnbl;
    
    /**iUnbl*/
    @ApiModelProperty(value = "iUnbl")
    private BigDecimal iiUnbl;
    
    /**尖时正向有功总电能*/
    @ApiModelProperty(value = "尖时正向有功总电能")
    private BigDecimal cuspValue;
    
    /**峰时正向有功总电能*/
    @ApiModelProperty(value = "峰时正向有功总电能")
    private BigDecimal peakValue;
    
    /**平时正向有功总电能*/
    @ApiModelProperty(value = "平时正向有功总电能")
    private BigDecimal levelValue;
    
    /**谷时正向有功总电能*/
    @ApiModelProperty(value = "谷时正向有功总电能")
    private BigDecimal valleyValue;
    
    /**isrunState*/
    @ApiModelProperty(value = "isrunState")
    private Integer isrunState;
    
    /**townRunHour*/
    @ApiModelProperty(value = "townRunHour")
    private BigDecimal townRunHour;
    
    /**economizeRunHour*/
    @ApiModelProperty(value = "economizeRunHour")
    private BigDecimal economizeRunHour;
    
    /**stopRunHour*/
    @ApiModelProperty(value = "stopRunHour")
    private BigDecimal stopRunHour;
    
    /**备用字段1*/
    @ApiModelProperty(value = "备用字段1")
    private String standby1;
    
    /**备用字段2*/
    @ApiModelProperty(value = "备用字段2")
    private String standby2;
    
    /**备用字段3*/
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
    
    /**need*/
    @ApiModelProperty(value = "need")
    private BigDecimal need;
    
    /**needtm*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "needtm")
    private Date needtm;
    
    /**prevkwh*/
    @ApiModelProperty(value = "prevkwh")
    private BigDecimal prevkwh;
} 