package org.jeecg.modules.energy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 能源计算工具类
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
public class EnergyCalculationUtils {

    /**
     * 计算负荷状态
     * @param ia A相电流
     * @param ib B相电流
     * @param ic C相电流
     * @param ua A相电压
     * @param ub B相电压
     * @param uc C相电压
     * @return 负荷状态
     */
    public static String calculateLoadStatus(BigDecimal ia, BigDecimal ib, BigDecimal ic, 
                                          BigDecimal ua, BigDecimal ub, BigDecimal uc) {
        // 如果电流或电压为空，返回未知状态
        if (ia == null || ib == null || ic == null || ua == null || ub == null || uc == null) {
            return "未知";
        }
        
        // 电流阈值，小于此值认为是停机状态
        BigDecimal currentThreshold = new BigDecimal("0.5");
        
        // 如果三相电流都接近0，则认为设备已停止
        if (ia.compareTo(currentThreshold) <= 0 && 
            ib.compareTo(currentThreshold) <= 0 && 
            ic.compareTo(currentThreshold) <= 0) {
            return "停机";
        }
        
        // 计算三相电流平均值
        BigDecimal avgCurrent = ia.add(ib).add(ic).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        
        // 根据平均电流值判断负荷状态
        if (avgCurrent.compareTo(new BigDecimal("10")) <= 0) {
            return "低负荷";
        } else if (avgCurrent.compareTo(new BigDecimal("30")) <= 0) {
            return "中负荷";
        } else {
            return "高负荷";
        }
    }
    
    /**
     * 计算负荷率
     * @param currentPower 当前功率
     * @param ratedPower 额定功率
     * @return 负荷率
     */
    public static BigDecimal calculateLoadRate(BigDecimal currentPower, BigDecimal ratedPower) {
        // 如果当前功率或额定功率为空，或者额定功率为0，返回0
        if (currentPower == null || ratedPower == null || ratedPower.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // 计算负荷率 = 当前功率 / 额定功率 * 100%
        return currentPower.multiply(new BigDecimal("100"))
                .divide(ratedPower, 2, RoundingMode.HALF_UP);
    }
} 