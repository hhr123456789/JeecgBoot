package org.jeecg.modules.energy.entity.process;

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
 * @Description: 生产线/工序配置表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Data
@TableName("tb_production_line")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_production_line对象", description="生产线/工序配置表")
public class TbProductionLine implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("parent_id")
    @ApiModelProperty(value = "父级ID")
    private String parentId;

    @TableField("line_code")
    @ApiModelProperty(value = "编码")
    private String lineCode;

    @TableField("line_name")
    @ApiModelProperty(value = "名称")
    private String lineName;

    @TableField("line_type")
    @ApiModelProperty(value = "类型 (line:生产线, process:工序)")
    private String lineType;

    @TableField("line_level")
    @ApiModelProperty(value = "层级")
    private Integer lineLevel;

    @TableField("sort_order")
    @ApiModelProperty(value = "排序号")
    private Integer sortOrder;

    @TableField("module_ids")
    @ApiModelProperty(value = "关联的仪表ID列表")
    private String moduleIds;

    @TableField("process_type")
    @ApiModelProperty(value = "工序类型")
    private String processType;

    @TableField("status")
    @ApiModelProperty(value = "状态")
    private Integer status;

    @TableField("description")
    @ApiModelProperty(value = "描述")
    private String description;

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
}
