package org.jeecg.modules.energy.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 维度定义配置表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@Data
@TableName("tb_energy_dimension_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TbEnergyDimensionConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;

    /**维度编码*/
    @Excel(name = "维度编码", width = 15)
    private java.lang.String dimensionCode;

    /**维度名称*/
    @Excel(name = "维度名称", width = 15)
    private java.lang.String dimensionName;

    /**排序*/
    @Excel(name = "排序", width = 15)
    private java.lang.Integer sortOrder;

    /**是否启用*/
    @Excel(name = "是否启用", width = 15)
    private java.lang.Integer isEnable;
}
