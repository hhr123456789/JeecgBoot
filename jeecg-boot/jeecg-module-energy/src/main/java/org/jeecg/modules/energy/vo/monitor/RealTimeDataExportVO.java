package org.jeecg.modules.energy.vo.monitor;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;

/**
 * 实时数据导出VO
 * 用于Excel导出的数据结构
 */
@Data
public class RealTimeDataExportVO {

    /**
     * 时间
     */
    @Excel(name = "时间", width = 20, orderNum = "1")
    private String time;

    /**
     * 动态参数列 - 这些字段会根据实际选择的参数动态设置
     * 格式：{仪表名称}/{参数名称}({单位})
     */
    
    // 预定义一些常用的参数字段，实际使用时会动态设置Excel注解
    private BigDecimal value1;
    private BigDecimal value2;
    private BigDecimal value3;
    private BigDecimal value4;
    private BigDecimal value5;
    private BigDecimal value6;
    private BigDecimal value7;
    private BigDecimal value8;
    private BigDecimal value9;
    private BigDecimal value10;
    private BigDecimal value11;
    private BigDecimal value12;
    private BigDecimal value13;
    private BigDecimal value14;
    private BigDecimal value15;
    private BigDecimal value16;
    private BigDecimal value17;
    private BigDecimal value18;
    private BigDecimal value19;
    private BigDecimal value20;

    // 可以根据需要扩展更多字段
}
