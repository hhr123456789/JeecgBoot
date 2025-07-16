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

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;
    
    @TableField("Module_ID")
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    @TableField("Equ_Electric_DT")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private Date equElectricDT;
    
    @TableField("UA")
    @ApiModelProperty(value = "A相电压")
    private BigDecimal UA;
    
    @TableField("UB")
    @ApiModelProperty(value = "B相电压")
    private BigDecimal UB;
    
    @TableField("UC")
    @ApiModelProperty(value = "C相电压")
    private BigDecimal UC;
    
    @TableField("IA")
    @ApiModelProperty(value = "A相电流")
    private BigDecimal IA;
    
    @TableField("IB")
    @ApiModelProperty(value = "B相电流")
    private BigDecimal IB;
    
    @TableField("IC")
    @ApiModelProperty(value = "C相电流")
    private BigDecimal IC;
    
    @TableField("uaw")
    @ApiModelProperty(value = "uaw")
    private BigDecimal uaw;
    
    @TableField("ubw")
    @ApiModelProperty(value = "ubw")
    private BigDecimal ubw;
    
    @TableField("ucw")
    @ApiModelProperty(value = "ucw")
    private BigDecimal ucw;
    
    @TableField("iaw")
    @ApiModelProperty(value = "iaw")
    private BigDecimal iaw;
    
    @TableField("ibw")
    @ApiModelProperty(value = "ibw")
    private BigDecimal ibw;
    
    @TableField("icw")
    @ApiModelProperty(value = "icw")
    private BigDecimal icw;
    
    @TableField("pp")
    @ApiModelProperty(value = "总有功功率")
    private BigDecimal pp;
    
    @TableField("qq")
    @ApiModelProperty(value = "总无在功率")
    private BigDecimal qq;
    
    @TableField("ss")
    @ApiModelProperty(value = "总视在功率")
    private BigDecimal ss;
    
    @TableField("PFS")
    @ApiModelProperty(value = "总功率因素")
    private BigDecimal PFS;
    
    @TableField("HZ")
    @ApiModelProperty(value = "频率")
    private BigDecimal HZ;
    
    @TableField("ff")
    @ApiModelProperty(value = "f")
    private BigDecimal ff;
    
    @TableField("Pa")
    @ApiModelProperty(value = "A相功率")
    private BigDecimal Pa;
    
    @TableField("Pb")
    @ApiModelProperty(value = "B相功率")
    private BigDecimal Pb;
    
    @TableField("Pc")
    @ApiModelProperty(value = "C相功率")
    private BigDecimal Pc;
    
    @TableField("Qa")
    @ApiModelProperty(value = "A相无功功率")
    private BigDecimal Qa;
    
    @TableField("Qb")
    @ApiModelProperty(value = "B相无功功率")
    private BigDecimal Qb;
    
    @TableField("Qc")
    @ApiModelProperty(value = "C相无功功率")
    private BigDecimal Qc;
    
    @TableField("Sa")
    @ApiModelProperty(value = "A相视在功率")
    private BigDecimal Sa;
    
    @TableField("Sb")
    @ApiModelProperty(value = "B相视在功率")
    private BigDecimal Sb;
    
    @TableField("Sc")
    @ApiModelProperty(value = "C相视在功率")
    private BigDecimal Sc;
    
    @TableField("PFa")
    @ApiModelProperty(value = "A相功率因素")
    private BigDecimal PFa;
    
    @TableField("PFb")
    @ApiModelProperty(value = "B相功率因素")
    private BigDecimal PFb;
    
    @TableField("PFc")
    @ApiModelProperty(value = "C相功率因素")
    private BigDecimal PFc;
    
    @TableField("KWH")
    @ApiModelProperty(value = "正向有功总电能")
    private BigDecimal KWH;
    
    @TableField("KVARH")
    @ApiModelProperty(value = "正向无功总电能")
    private BigDecimal KVARH;
    
    @TableField("uu_unbl")
    @ApiModelProperty(value = "uUnbl")
    private BigDecimal uuUnbl;
    
    @TableField("ii_unbl")
    @ApiModelProperty(value = "iUnbl")
    private BigDecimal iiUnbl;
    
    @TableField("CuspValue")
    @ApiModelProperty(value = "尖时正向有功总电能")
    private BigDecimal cuspValue;
    
    @TableField("PeakValue")
    @ApiModelProperty(value = "峰时正向有功总电能")
    private BigDecimal peakValue;
    
    @TableField("LevelValue")
    @ApiModelProperty(value = "平时正向有功总电能")
    private BigDecimal levelValue;
    
    @TableField("ValleyValue")
    @ApiModelProperty(value = "谷时正向有功总电能")
    private BigDecimal valleyValue;
    
    @TableField("isrun_state")
    @ApiModelProperty(value = "isrunState")
    private Integer isrunState;
    
    @TableField("town_run_hour")
    @ApiModelProperty(value = "townRunHour")
    private BigDecimal townRunHour;
    
    @TableField("economize_run_hour")
    @ApiModelProperty(value = "economizeRunHour")
    private BigDecimal economizeRunHour;
    
    @TableField("stop_run_hour")
    @ApiModelProperty(value = "stopRunHour")
    private BigDecimal stopRunHour;
    
    @TableField("Standby1")
    @ApiModelProperty(value = "备用字段1")
    private String standby1;
    
    @TableField("Standby2")
    @ApiModelProperty(value = "备用字段2")
    private String standby2;
    
    @TableField("Standby3")
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
    
    @TableField("need")
    @ApiModelProperty(value = "need")
    private BigDecimal need;
    
    @TableField("needtm")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "needtm")
    private Date needtm;
    
    @TableField("prevkwh")
    @ApiModelProperty(value = "prevkwh")
    private BigDecimal prevkwh;
}