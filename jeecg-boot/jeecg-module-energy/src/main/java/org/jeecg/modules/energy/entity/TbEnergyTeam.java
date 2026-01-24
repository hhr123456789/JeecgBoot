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
 * @Description: 能耗班组表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@Data
@TableName("tb_energy_team")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TbEnergyTeam implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;

    /**班组编码*/
    @Excel(name = "班组编码", width = 15)
    private java.lang.String teamCode;

    /**班组名称*/
    @Excel(name = "班组名称", width = 15)
    private java.lang.String teamName;

    /**班次类型*/
    @Excel(name = "班次类型", width = 15)
    private java.lang.String shiftType;

    /**创建人*/
    private java.lang.String createBy;

    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**更新人*/
    private java.lang.String updateBy;

    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}
