package org.jeecg.modules.energy.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: tb_equipment_depreciation
 * @Author: jeecg-boot
 * @Date:   2025-10-02
 * @Version: V1.0
 */
@Data
@TableName("tb_equipment_depreciation")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tb_equipment_depreciation对象", description="tb_equipment_depreciation")
public class TbEquipmentDepreciation implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private java.lang.String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**关联设备ID*/
	@Excel(name = "关联设备ID", width = 15)
    @ApiModelProperty(value = "关联设备ID")
    private java.lang.String equId;
	/**设备编号*/
	@Excel(name = "设备编号", width = 15)
    @ApiModelProperty(value = "设备编号")
    private java.lang.String equNo;
	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @ApiModelProperty(value = "设备名称")
    private java.lang.String equName;
	/**折旧年月 */
	@Excel(name = "折旧年月 ", width = 15)
    @ApiModelProperty(value = "折旧年月 ")
    private java.lang.String depMonth;
	/**折旧方法*/
	@Excel(name = "折旧方法", width = 15)
    @ApiModelProperty(value = "折旧方法")
    private java.lang.String method;
	/**购置金额*/
	@Excel(name = "购置金额", width = 15)
    @ApiModelProperty(value = "购置金额")
    private java.math.BigDecimal purchaseAmount;
	/**使用寿命(月)*/
	@Excel(name = "使用寿命(月)", width = 15)
    @ApiModelProperty(value = "使用寿命(月)")
    private java.lang.Integer usefulLifeMonths;
	/**净残率%*/
	@Excel(name = "净残率%", width = 15)
    @ApiModelProperty(value = "净残率%")
    private java.math.BigDecimal salvageRate;
	/**初期净值*/
	@Excel(name = "初期净值", width = 15)
    @ApiModelProperty(value = "初期净值")
    private java.math.BigDecimal initNetValue;
	/**本月折旧*/
	@Excel(name = "本月折旧", width = 15)
    @ApiModelProperty(value = "本月折旧")
    private java.math.BigDecimal monthlyDep;
	/**累计折旧*/
	@Excel(name = "累计折旧", width = 15)
    @ApiModelProperty(value = "累计折旧")
    private java.math.BigDecimal accumulatedDep;
	/**净值*/
	@Excel(name = "净值", width = 15)
    @ApiModelProperty(value = "净值")
    private java.math.BigDecimal netValue;
}
