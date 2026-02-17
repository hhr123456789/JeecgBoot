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
 * @Description: 产品分类表
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@TableName("tb_product_category")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_product_category对象", description="产品分类表")
public class TbProductCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("parent_id")
    @ApiModelProperty(value = "父级分类ID")
    private String parentId;

    @TableField("category_code")
    @ApiModelProperty(value = "分类编码")
    private String categoryCode;

    @TableField("category_name")
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @TableField("category_level")
    @ApiModelProperty(value = "分类层级")
    private Integer categoryLevel;

    @TableField("sort_order")
    @ApiModelProperty(value = "排序号")
    private Integer sortOrder;

    @TableField("is_leaf")
    @ApiModelProperty(value = "是否叶子节点")
    private Integer isLeaf;

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
}
