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
 * @Description: 产品工序关联表
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@TableName("tb_product_process_rel")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_product_process_rel对象", description="产品工序关联表")
public class TbProductProcessRel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("product_code")
    @ApiModelProperty(value = "产品编码")
    private String productCode;

    @TableField("process_code")
    @ApiModelProperty(value = "工序编码")
    private String processCode;

    @TableField("process_name")
    @ApiModelProperty(value = "工序名称")
    private String processName;

    @TableField("dimension_code")
    @ApiModelProperty(value = "维度编码")
    private String dimensionCode;

    @TableField("dimension_type")
    @ApiModelProperty(value = "维度类型")
    private Integer dimensionType;

    @TableField("energy_type")
    @ApiModelProperty(value = "能源类型")
    private Integer energyType;

    @TableField("module_ids")
    @ApiModelProperty(value = "关联的仪表ID列表")
    private String moduleIds;

    @TableField("status")
    @ApiModelProperty(value = "状态")
    private Integer status;

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
