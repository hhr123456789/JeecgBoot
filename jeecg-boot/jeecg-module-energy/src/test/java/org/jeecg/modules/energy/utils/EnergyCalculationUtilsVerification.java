package org.jeecg.modules.energy.utils;

import java.math.BigDecimal;

/**
 * 能源计算工具类验证程序
 * 测试高压三相三线制设备支持
 */
public class EnergyCalculationUtilsVerification {
    
    public static void main(String[] args) {
        System.out.println("=== 能源计算工具类验证程序 ===");
        
        // 测试1: 高压三相三线制系统 (module_type = 4)
        testHighVoltageThreeWire();
        
        // 测试2: 普通三相系统 (module_type = 1)
        testNormalThreePhase();
        
        // 测试3: 基于功率的计算
        testPowerBasedCalculation();
        
        System.out.println("=== 所有测试完成 ===");
    }
    
    /**
     * 测试高压三相三线制系统（B相为0是正常的）
     */
    private static void testHighVoltageThreeWire() {
        System.out.println("\n--- 测试1: 高压三相三线制系统 ---");
        
        BigDecimal ia = new BigDecimal("50");   // A相电流（调整到正常范围）
        BigDecimal ib = new BigDecimal("0");    // B相电流（应该为0）
        BigDecimal ic = new BigDecimal("48");   // C相电流
        BigDecimal ua = new BigDecimal("10000"); // 高压 10kV
        BigDecimal ub = new BigDecimal("10000");
        BigDecimal uc = new BigDecimal("10000");
        Integer moduleType = 4; // 高压三相三线制
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        System.out.println("输入: IA=" + ia + "A, IB=" + ib + "A, IC=" + ic + "A, UA=" + ua + "V, 设备类型=" + moduleType);
        System.out.println("结果: " + result);
        System.out.println("预期: 正常 (B相为0对高压三相三线制是正常的)");
        
        if ("正常".equals(result)) {
            System.out.println("✅ 通过: 高压三相三线制测试通过！");
        } else {
            System.out.println("❌ 失败: 预期'正常'，实际得到'" + result + "'");
        }
    }
    
    /**
     * 测试普通三相系统
     */
    private static void testNormalThreePhase() {
        System.out.println("\n--- 测试2: 普通三相系统 ---");
        
        BigDecimal ia = new BigDecimal("100");
        BigDecimal ib = new BigDecimal("0");    // B相为0对普通系统是异常的
        BigDecimal ic = new BigDecimal("95");
        BigDecimal ua = new BigDecimal("380");
        BigDecimal ub = new BigDecimal("380");
        BigDecimal uc = new BigDecimal("380");
        Integer moduleType = 1; // 普通三相
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        System.out.println("输入: IA=" + ia + "A, IB=" + ib + "A, IC=" + ic + "A, 设备类型=" + moduleType);
        System.out.println("结果: " + result);
        System.out.println("预期: 三相不平衡 (B相为0对普通系统是异常的)");
        
        if ("三相不平衡".equals(result)) {
            System.out.println("✅ 通过: 普通三相系统测试通过！");
        } else {
            System.out.println("❌ 失败: 预期'三相不平衡'，实际得到'" + result + "'");
        }
    }
    
    /**
     * 测试基于功率的计算
     */
    private static void testPowerBasedCalculation() {
        System.out.println("\n--- 测试3: 基于功率的计算 ---");
        
        BigDecimal currentPower = new BigDecimal("750");  // 750kW
        BigDecimal ratedPower = new BigDecimal("1000");   // 1000kW额定
        BigDecimal ia = new BigDecimal("100");
        BigDecimal ib = new BigDecimal("0");
        BigDecimal ic = new BigDecimal("95");
        Integer moduleType = 4; // 高压三相三线制
        
        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, moduleType);
        
        System.out.println("输入: 当前功率=" + currentPower + "kW, 额定功率=" + ratedPower + "kW");
        System.out.println("负荷率: " + currentPower.divide(ratedPower).multiply(new BigDecimal("100")) + "%");
        System.out.println("结果: " + result);
        System.out.println("预期: 正常 (75%负荷率是正常的)");
        
        if ("正常".equals(result)) {
            System.out.println("✅ 通过: 基于功率的计算测试通过！");
        } else {
            System.out.println("❌ 失败: 预期'正常'，实际得到'" + result + "'");
        }
    }
}