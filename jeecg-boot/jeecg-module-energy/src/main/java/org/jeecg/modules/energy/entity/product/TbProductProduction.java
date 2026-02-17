package org.jeecg.modules.energy.entity.product;

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
 * @Description: 产品产量记录表
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
@Data
@TableName("tb_product_production")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_product_production对象", description="产品产量记录表")
public class TbProductProduction implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("product_code")
    @ApiModelProperty(value = "产品编码")
    private String productCode;

    @TableField("production_date")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "生产日期")
    private Date productionDate;

    @TableField("production_line")
    @ApiModelProperty(value = "生产线")
    private String productionLine;

    @TableField("team_code")
    @ApiModelProperty(value = "班组编码")
    private String teamCode;

    @TableField("shift_type")
    @ApiModelProperty(value = "班次类型")
    private String shiftType;

    @TableField("plan_production")
    @ApiModelProperty(value = "计划产量")
    private BigDecimal planProduction;

    @TableField("actual_production")
    @ApiModelProperty(value = "实际产量")
    private BigDecimal actualProduction;

    @TableField("qualified_production")
    @ApiModelProperty(value = "合格产量")
    private BigDecimal qualifiedProduction;

    @TableField("unqualified_production")
    @ApiModelProperty(value = "不合格产量")
    private BigDecimal unqualifiedProduction;

    @TableField("qualification_rate")
    @ApiModelProperty(value = "合格率")
    private BigDecimal qualificationRate;

    @TableField("sys_org_code")
    @ApiModelProperty(value = "所属部门编码")
    private String sysOrgCode;

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
