package org.jeecg.modules.energy.entity.product;

import java.io.Serializable;
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
 * @Description: 产品基础信息表
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@TableName("tb_product_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_product_info对象", description="产品基础信息表")
public class TbProductInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("product_code")
    @ApiModelProperty(value = "产品编码")
    private String productCode;

    @TableField("product_name")
    @ApiModelProperty(value = "产品名称")
    private String productName;

    @TableField("category_id")
    @ApiModelProperty(value = "产品分类ID")
    private String categoryId;

    @TableField("category_code")
    @ApiModelProperty(value = "产品分类编码")
    private String categoryCode;

    @TableField("product_model")
    @ApiModelProperty(value = "产品型号")
    private String productModel;

    @TableField("product_spec")
    @ApiModelProperty(value = "产品规格")
    private String productSpec;

    @TableField("product_unit")
    @ApiModelProperty(value = "产品单位")
    private String productUnit;

    @TableField("sys_org_code")
    @ApiModelProperty(value = "所属部门编码")
    private String sysOrgCode;

    @TableField("status")
    @ApiModelProperty(value = "状态")
    private Integer status;

    @TableField("description")
    @ApiModelProperty(value = "描述")
    private String description;

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

    @TableField("standby1")
    @ApiModelProperty(value = "备用字段1")
    private String standby1;

    @TableField("standby2")
    @ApiModelProperty(value = "备用字段2")
    private String standby2;

    @TableField("standby3")
    @ApiModelProperty(value = "备用字段3")
    private String standby3;
}
