package org.jeecg.modules.energy.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 班组维度关联表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@Data
@TableName("tb_energy_team_rel")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TbEnergyTeamRel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;

    /**班组ID*/
    @Excel(name = "班组ID", width = 15)
    private java.lang.String teamId;

    /**维度编码*/
    @Excel(name = "维度编码", width = 15)
    private java.lang.String dimensionCode;

    /**维度类型*/
    @Excel(name = "维度类型", width = 15)
    private java.lang.String dimensionType;

    /**创建人*/
    private java.lang.String createBy;

    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
}
