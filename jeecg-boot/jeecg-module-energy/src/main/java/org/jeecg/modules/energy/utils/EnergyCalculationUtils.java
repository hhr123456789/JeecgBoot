package org.jeecg.modules.energy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 能源计算工具类
 * 用于封装负荷状态计算和负荷率计算等功能
 */
public class EnergyCalculationUtils {

    /**
     * 计算负荷状态
     * @param IA A相电流
     * @param IB B相电流
     * @param IC C相电流
     * @param UA A相电压
     * @param UB B相电压
     * @param UC C相电压
     * @return 负荷状态
     */
    public static String calculateLoadStatus(BigDecimal IA, BigDecimal IB, BigDecimal IC,
                                          BigDecimal UA, BigDecimal UB, BigDecimal UC) {
        // 1. 检查三相平衡度
        BigDecimal avgCurrent = IA.add(IB).add(IC)
                .divide(new BigDecimal(3), 2, RoundingMode.HALF_UP);
        
        BigDecimal maxCurrentDeviation = calculateMaxDeviation(IA, IB, IC, avgCurrent);
        
        // 电压正常范围检查 (标准电压220V，允许偏差±10%)
        boolean voltageNormal = UA.compareTo(new BigDecimal(198)) >= 0 
                && UA.compareTo(new BigDecimal(242)) <= 0
                && UB.compareTo(new BigDecimal(198)) >= 0 
                && UB.compareTo(new BigDecimal(242)) <= 0
                && UC.compareTo(new BigDecimal(198)) >= 0 
                && UC.compareTo(new BigDecimal(242)) <= 0;
        
        // 三相不平衡度超过20%或电压异常时，判定为"异常"
        if (maxCurrentDeviation.compareTo(new BigDecimal(0.2)) > 0 || !voltageNormal) {
            return "异常";
        }
        
        // 三相不平衡度在10%-20%之间，判定为"警告"
        if (maxCurrentDeviation.compareTo(new BigDecimal(0.1)) > 0) {
            return "警告";
        }
        
        return "正常";
    }

    /**
     * 计算最大偏差
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @param avg 平均值
     * @return 最大偏差
     */
    public static BigDecimal calculateMaxDeviation(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal avg) {
        BigDecimal devA = a.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
        BigDecimal devB = b.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
        BigDecimal devC = c.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
        
        return devA.max(devB).max(devC);
    }

    /**
     * 计算负荷率
     * @param pp 当前功率
     * @param rated_power 额定功率
     * @return 负荷率(%)
     */
    public static BigDecimal calculateLoadRate(BigDecimal pp, BigDecimal rated_power) {
        if (rated_power == null || rated_power.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return pp.multiply(new BigDecimal(100))
                .divide(rated_power, 2, RoundingMode.HALF_UP);
    }
} 