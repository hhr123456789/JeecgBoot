package org.jeecg.modules.energy.entity;

import java.io.Serializable;
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
 * @Description: 仪表基础信息
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Data
@TableName("tb_module")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_module对象", description="仪表基础信息")
public class TbModule implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    
    /**所属企业*/
    @ApiModelProperty(value = "所属企业")
    private Integer enterpriseId;
    
    /**采集器名称*/
    @ApiModelProperty(value = "采集器名称")
    private String gatewayCode;
    
    /**仪表id*/
    @ApiModelProperty(value = "仪表id")
    private String meterId;
    
    /**仪表名称*/
    @ApiModelProperty(value = "仪表名称")
    private String moduleName;
    
    /**仪表编号*/
    @ApiModelProperty(value = "仪表编号")
    private String moduleId;
    
    /**来源方式*/
    @ApiModelProperty(value = "来源方式")
    private Integer datasource;
    
    /**仪表类型*/
    @ApiModelProperty(value = "仪表类型")
    private Integer moduleType;
    
    /**仪表等级*/
    @ApiModelProperty(value = "仪表等级")
    private Integer level;
    
    /**是否启用*/
    @ApiModelProperty(value = "是否启用")
    private String isaction;
    
    /**备用字段1*/
    @ApiModelProperty(value = "备用字段1")
    private String standby1;
    
    /**备用字段2*/
    @ApiModelProperty(value = "备用字段2")
    private String standby2;
    
    /**备用字段3*/
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
    
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    
    /**维度*/
    @ApiModelProperty(value = "维度")
    private String sysOrgCode;
    
    /**电压倍率*/
    @ApiModelProperty(value = "电压倍率")
    private Integer pt;
    
    /**计量精度*/
    @ApiModelProperty(value = "计量精度")
    private String accuracy;
    
    /**仪表型号*/
    @ApiModelProperty(value = "仪表型号")
    private String deviceType;
    
    /**电流倍率*/
    @ApiModelProperty(value = "电流倍率")
    private Integer ct;
    
    /**远程IP*/
    @ApiModelProperty(value = "远程IP")
    private String remoteip;
    
    /**通讯协议*/
    @ApiModelProperty(value = "通讯协议")
    private String protocol;
    
    /**数据端口号*/
    @ApiModelProperty(value = "数据端口号")
    private Integer remotecom;
    
    /**本地端口*/
    @ApiModelProperty(value = "本地端口")
    private Integer localcom;
    
    /**能源类型*/
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;
    
    /**额定功率*/
    @ApiModelProperty(value = "额定功率")
    private Double ratedPower;
} 