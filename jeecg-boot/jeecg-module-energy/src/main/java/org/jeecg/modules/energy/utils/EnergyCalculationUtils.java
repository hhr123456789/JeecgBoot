package org.jeecg.modules.energy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 能源计算工具类
 * @Author: CodeBuddy
 * @Date: 2025/09/12
 * @Version: V2.0 - 支持高压三相三线制设备
 */
public class EnergyCalculationUtils {

    // 负荷率阈值常量
    private static final BigDecimal LIGHT_LOAD_THRESHOLD = new BigDecimal("30");      // 轻载阈值 30%
    private static final BigDecimal NORMAL_LOAD_THRESHOLD = new BigDecimal("80");     // 正常负载阈值 80%
    private static final BigDecimal HEAVY_LOAD_THRESHOLD = new BigDecimal("100");     // 重载阈值 100%
    private static final BigDecimal CURRENT_THRESHOLD = new BigDecimal("0.5");        // 停机电流阈值 0.5A
    private static final BigDecimal IMBALANCE_THRESHOLD = new BigDecimal("10");       // 三相不平衡阈值 10%
    private static final BigDecimal HIGH_VOLTAGE_IMBALANCE_THRESHOLD = new BigDecimal("15"); // 高压设备不平衡阈值 15%
    
    // 设备类型常量
    private static final Integer MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE = 4;             // 高压三相三线制

    /**
     * 计算负荷状态（推荐使用，基于负荷率百分比）
     * @param currentPower 当前功率 (kW)
     * @param ratedPower 额定功率 (kW)
     * @param ia A相电流
     * @param ib B相电流
     * @param ic C相电流
     * @param moduleType 设备类型 (4=高压三相三线制)
     * @return 负荷状态
     */
    public static String calculateLoadStatus(BigDecimal currentPower, BigDecimal ratedPower, 
                                           BigDecimal ia, BigDecimal ib, BigDecimal ic, Integer moduleType) {
        // 参数验证
        if (ia == null || ib == null || ic == null) {
            return "未知";
        }

        // 检查设备停机状态
        if (isDeviceStopped(ia, ib, ic)) {
            return "停机";
        }

        // 检查三相不平衡（考虑设备类型）
        String imbalanceStatus = checkThreePhaseImbalance(ia, ib, ic, moduleType);
        if (!"正常".equals(imbalanceStatus)) {
            return imbalanceStatus;
        }

        // 优先使用基于功率的计算
        if (currentPower != null && ratedPower != null && ratedPower.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal loadRate = calculateLoadRate(currentPower, ratedPower);
            return getLoadStatusByRate(loadRate);
        }

        // 降级方案：基于电流估算（需要额定电流，这里使用经验值）
        return calculateLoadStatusByCurrentEstimate(ia, ib, ic, moduleType);
    }

    /**
     * 计算负荷状态（兼容原有方法签名，基于电流估算）
     * @param ia A相电流
     * @param ib B相电流
     * @param ic C相电流
     * @param ua A相电压
     * @param ub B相电压
     * @param uc C相电压
     * @param moduleType 设备类型 (4=高压三相三线制)
     * @return 负荷状态
     */
    public static String calculateLoadStatus(BigDecimal ia, BigDecimal ib, BigDecimal ic, 
                                          BigDecimal ua, BigDecimal ub, BigDecimal uc, Integer moduleType) {
        // 参数验证
        if (ia == null || ib == null || ic == null) {
            return "未知";
        }

        // 检查设备停机状态
        if (isDeviceStopped(ia, ib, ic)) {
            return "停机";
        }

        // 检查三相不平衡（考虑设备类型）
        String imbalanceStatus = checkThreePhaseImbalance(ia, ib, ic, moduleType);
        if (!"正常".equals(imbalanceStatus)) {
            return imbalanceStatus;
        }

        // 如果有电压信息，计算视在功率进行更准确的判断
        if (ua != null && ub != null && uc != null) {
            return calculateLoadStatusByPower(ia, ib, ic, ua, ub, uc, moduleType);
        }

        // 降级到电流估算
        return calculateLoadStatusByCurrentEstimate(ia, ib, ic, moduleType);
    }

    /**
     * 计算负荷状态（兼容原有方法签名）
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
        try {
            // 检查设备停机状态
            if (isDeviceStopped(ia, ib, ic)) {
                return "停机";
            }

            // 检查三相不平衡（不考虑设备类型）
            String imbalanceStatus = checkThreePhaseImbalance(ia, ib, ic, null);
            if (!"正常".equals(imbalanceStatus)) {
                return imbalanceStatus;
            }

            // 如果有电压信息，计算视在功率
            if (ua != null && ub != null && uc != null) {
                return calculateLoadStatusByPower(ia, ib, ic, ua, ub, uc, null);
            }

            // 降级到电流估算（需要额定电流，这里使用经验值）
            return calculateLoadStatusByCurrentEstimate(ia, ib, ic, null);
        } catch (Exception e) {
            // 异常时降级到电流估算
            return calculateLoadStatusByCurrentEstimate(ia, ib, ic, null);
        }
    }

    /**
     * 检查设备是否停机
     */
    private static boolean isDeviceStopped(BigDecimal ia, BigDecimal ib, BigDecimal ic) {
        return ia.compareTo(CURRENT_THRESHOLD) <= 0 && 
               ib.compareTo(CURRENT_THRESHOLD) <= 0 && 
               ic.compareTo(CURRENT_THRESHOLD) <= 0;
    }

    /**
     * 检查三相不平衡（考虑设备类型）
     */
    private static String checkThreePhaseImbalance(BigDecimal ia, BigDecimal ib, BigDecimal ic, Integer moduleType) {
        // 高压三相三线制设备特殊处理
        if (moduleType != null && moduleType.equals(MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE)) {
            // 高压三相三线制：B相应该为0
            if (ib.compareTo(new BigDecimal("0.5")) > 0) {
                return "接线异常"; // B相不应该有电流
            }
            
            // 只检查A、C两相的平衡度
            BigDecimal avgCurrent = ia.add(ic).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
            if (avgCurrent.compareTo(BigDecimal.ZERO) == 0) {
                return "正常";
            }
            
            BigDecimal imbalanceRate = ia.subtract(ic).abs()
                                        .multiply(new BigDecimal("100"))
                                        .divide(avgCurrent, 2, RoundingMode.HALF_UP);
            
            if (imbalanceRate.compareTo(HIGH_VOLTAGE_IMBALANCE_THRESHOLD) > 0) {
                return "两相不平衡";
            }
        } else {
            // 普通三相设备：检查三相平衡
            BigDecimal avgCurrent = ia.add(ib).add(ic).divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP);
            if (avgCurrent.compareTo(BigDecimal.ZERO) == 0) {
                return "正常";
            }
            
            BigDecimal maxCurrent = ia.max(ib).max(ic);
            BigDecimal minCurrent = ia.min(ib).min(ic);
            BigDecimal imbalanceRate = maxCurrent.subtract(minCurrent)
                                                .multiply(new BigDecimal("100"))
                                                .divide(avgCurrent, 2, RoundingMode.HALF_UP);
            
            if (imbalanceRate.compareTo(IMBALANCE_THRESHOLD) > 0) {
                return "三相不平衡";
            }
        }
        
        return "正常";
    }

    /**
     * 基于功率和电压计算负荷状态
     */
    private static String calculateLoadStatusByPower(BigDecimal ia, BigDecimal ib, BigDecimal ic,
                                                   BigDecimal ua, BigDecimal ub, BigDecimal uc, Integer moduleType) {
        try {
            // 根据设备类型计算视在功率
            BigDecimal apparentPower;
            
            if (moduleType != null && moduleType.equals(MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE)) {
                // 高压三相三线制：只使用A、C相
                BigDecimal avgVoltage = ua.add(uc).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                BigDecimal avgCurrent = ia.add(ic).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                
                // 视在功率 (kVA) = 2 * 相电压 * 相电流 / 1000（两相）
                apparentPower = new BigDecimal("2")
                        .multiply(avgVoltage)
                        .multiply(avgCurrent)
                        .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
            } else {
                // 普通三相计算
                BigDecimal avgVoltage = ua.add(ub).add(uc).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
                BigDecimal avgCurrent = ia.add(ib).add(ic).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
                
                // 视在功率 (kVA) = 3 * 相电压 * 相电流 / 1000
                apparentPower = new BigDecimal("3")
                        .multiply(avgVoltage)
                        .multiply(avgCurrent)
                        .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
            }

            // 根据设备类型调整阈值
            if (moduleType != null && moduleType.equals(MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE)) {
                // 高压设备使用更高的功率阈值
                if (apparentPower.compareTo(new BigDecimal("500")) <= 0) {
                    return "轻载";
                } else if (apparentPower.compareTo(new BigDecimal("2000")) <= 0) {
                    return "正常";
                } else if (apparentPower.compareTo(new BigDecimal("5000")) <= 0) {
                    return "重载";
                } else {
                    return "过载";
                }
            } else {
                // 普通设备使用较低阈值
                if (apparentPower.compareTo(new BigDecimal("10")) <= 0) {
                    return "轻载";
                } else if (apparentPower.compareTo(new BigDecimal("50")) <= 0) {
                    return "正常";
                } else if (apparentPower.compareTo(new BigDecimal("100")) <= 0) {
                    return "重载";
                } else {
                    return "过载";
                }
            }
        } catch (Exception e) {
            // 计算异常时降级到电流估算
            return calculateLoadStatusByCurrentEstimate(ia, ib, ic, moduleType);
        }
    }

    /**
     * 基于电流估算负荷状态（降级方案）
     */
    private static String calculateLoadStatusByCurrentEstimate(BigDecimal ia, BigDecimal ib, BigDecimal ic, Integer moduleType) {
        // 根据设备类型计算平均电流
        BigDecimal avgCurrent;
        if (moduleType != null && moduleType.equals(MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE)) {
            // 高压三相三线制：只考虑A、C相
            avgCurrent = ia.add(ic).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        } else {
            // 普通三相：考虑所有三相
            avgCurrent = ia.add(ib).add(ic).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        }
        
        // 根据设备类型调整阈值
        if (moduleType != null && moduleType.equals(MODULE_TYPE_HIGH_VOLTAGE_THREE_WIRE)) {
            // 高压设备通常有更高的电流额定值
            if (avgCurrent.compareTo(new BigDecimal("20")) <= 0) {
                return "轻载";
            } else if (avgCurrent.compareTo(new BigDecimal("100")) <= 0) {
                return "正常";
            } else if (avgCurrent.compareTo(new BigDecimal("200")) <= 0) {
                return "重载";
            } else {
                return "过载";
            }
        } else {
            // 普通设备使用较低阈值
            if (avgCurrent.compareTo(new BigDecimal("5")) <= 0) {
                return "轻载";
            } else if (avgCurrent.compareTo(new BigDecimal("20")) <= 0) {
                return "正常";
            } else if (avgCurrent.compareTo(new BigDecimal("50")) <= 0) {
                return "重载";
            } else {
                return "过载";
            }
        }
    }

    /**
     * 根据负荷率百分比获取负荷状态
     */
    private static String getLoadStatusByRate(BigDecimal loadRate) {
        if (loadRate.compareTo(LIGHT_LOAD_THRESHOLD) < 0) {
            return "轻载";
        } else if (loadRate.compareTo(NORMAL_LOAD_THRESHOLD) <= 0) {
            return "正常";
        } else if (loadRate.compareTo(HEAVY_LOAD_THRESHOLD) <= 0) {
            return "重载";
        } else {
            return "过载";
        }
    }
    
    /**
     * 计算负荷率
     * @param currentPower 当前功率
     * @param ratedPower 额定功率
     * @return 负荷率
     */
    public static BigDecimal calculateLoadRate(BigDecimal currentPower, BigDecimal ratedPower) {
        // 如果当前功率或额定功率为空，或额定功率为0，返回0
        if (currentPower == null || ratedPower == null || ratedPower.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // 计算负荷率 = 当前功率 / 额定功率 * 100%
        return currentPower.divide(ratedPower, 4, RoundingMode.HALF_UP)
                          .multiply(new BigDecimal("100"));
    }
}