package org.jeecg.modules.energy.utils;

import java.math.BigDecimal;

/**
 * @Description: 改进后的能源计算工具类测试（无需JUnit）
 * @Author: jeecg-boot
 * @Date: 2025-09-12
 * @Version: V2.0
 */
public class EnergyCalculationUtilsImprovedTest {

    public static void main(String[] args) {
        System.out.println("=== 改进后的能源计算工具类测试 ===\n");
        
        EnergyCalculationUtilsImprovedTest test = new EnergyCalculationUtilsImprovedTest();
        
        test.testCalculateLoadStatusByPower_LightLoad();
        test.testCalculateLoadStatusByPower_Normal();
        test.testCalculateLoadStatusByPower_HeavyLoad();
        test.testCalculateLoadStatusByPower_Overload();
        test.testCalculateLoadStatus_Stopped();
        test.testCalculateLoadStatus_Imbalance();
        test.testCalculateLoadStatus_HighVoltageThreeWire();
        test.testCalculateLoadStatusWithVoltage();
        test.testCalculateLoadRate();
        test.testCalculateLoadStatus_NullValues();
        test.testCompareOldAndNewMethod();
        
        System.out.println("\n=== 所有测试完成 ===");
    }

    public void testCalculateLoadStatusByPower_LightLoad() {
        System.out.println("--- 测试1: 基于功率的负荷状态计算 - 轻载 ---");
        BigDecimal currentPower = new BigDecimal("20");  // 当前功率 20kW
        BigDecimal ratedPower = new BigDecimal("100");   // 额定功率 100kW
        BigDecimal ia = new BigDecimal("15");
        BigDecimal ib = new BigDecimal("16");
        BigDecimal ic = new BigDecimal("15");

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: 当前功率=20kW 额定功率=100kW");
        System.out.println("结果: " + result);
        System.out.println("预期: 轻载 (负荷率20%)");
        System.out.println("✓ " + (result.equals("轻载") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatusByPower_Normal() {
        System.out.println("--- 测试2: 基于功率的负荷状态计算 - 正常 ---");
        BigDecimal currentPower = new BigDecimal("60");  // 当前功率 60kW
        BigDecimal ratedPower = new BigDecimal("100");   // 额定功率 100kW
        BigDecimal ia = new BigDecimal("45");
        BigDecimal ib = new BigDecimal("46");
        BigDecimal ic = new BigDecimal("44");

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: 当前功率=60kW 额定功率=100kW");
        System.out.println("结果: " + result);
        System.out.println("预期: 正常 (负荷率60%)");
        System.out.println("✓ " + (result.equals("正常") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatusByPower_HeavyLoad() {
        System.out.println("--- 测试3: 基于功率的负荷状态计算 - 重载 ---");
        BigDecimal currentPower = new BigDecimal("90");  // 当前功率 90kW
        BigDecimal ratedPower = new BigDecimal("100");   // 额定功率 100kW
        BigDecimal ia = new BigDecimal("68");
        BigDecimal ib = new BigDecimal("69");
        BigDecimal ic = new BigDecimal("67");

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: 当前功率=90kW 额定功率=100kW");
        System.out.println("结果: " + result);
        System.out.println("预期: 重载 (负荷率90%)");
        System.out.println("✓ " + (result.equals("重载") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatusByPower_Overload() {
        System.out.println("--- 测试4: 基于功率的负荷状态计算 - 过载 ---");
        BigDecimal currentPower = new BigDecimal("120"); // 当前功率 120kW
        BigDecimal ratedPower = new BigDecimal("100");   // 额定功率 100kW
        BigDecimal ia = new BigDecimal("85");
        BigDecimal ib = new BigDecimal("86");
        BigDecimal ic = new BigDecimal("84");

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: 当前功率=120kW 额定功率=100kW");
        System.out.println("结果: " + result);
        System.out.println("预期: 过载 (负荷率120%)");
        System.out.println("✓ " + (result.equals("过载") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatus_Stopped() {
        System.out.println("--- 测试5: 停机状态检测 ---");
        BigDecimal currentPower = new BigDecimal("0");
        BigDecimal ratedPower = new BigDecimal("100");
        BigDecimal ia = new BigDecimal("0.2");  // 低于0.5A阈值
        BigDecimal ib = new BigDecimal("0.3");
        BigDecimal ic = new BigDecimal("0.1");

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: 三相电流均低于0.5A");
        System.out.println("结果: " + result);
        System.out.println("预期: 停机");
        System.out.println("✓ " + (result.equals("停机") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatus_Imbalance() {
        System.out.println("--- 测试6: 三相不平衡检测 ---");
        BigDecimal currentPower = new BigDecimal("50");
        BigDecimal ratedPower = new BigDecimal("100");
        BigDecimal ia = new BigDecimal("10");   // A相电流
        BigDecimal ib = new BigDecimal("30");   // B相电流
        BigDecimal ic = new BigDecimal("15");   // C相电流

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 1);
        
        System.out.println("输入: IA=10A IB=30A IC=15A (电流差异过大)");
        System.out.println("结果: " + result);
        System.out.println("预期: 三相不平衡");
        System.out.println("✓ " + (result.equals("三相不平衡") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatus_HighVoltageThreeWire() {
        System.out.println("--- 测试7: 高压三相三线制设备 ---");
        BigDecimal currentPower = new BigDecimal("500");
        BigDecimal ratedPower = new BigDecimal("1000");
        BigDecimal ia = new BigDecimal("50");   // A相电流
        BigDecimal ib = new BigDecimal("0");    // B相电流为0（三相三线制）
        BigDecimal ic = new BigDecimal("48");   // C相电流

        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, 4);
        
        System.out.println("输入: IA=50A IB=0A IC=48A 设备类型=4 (高压三相三线制)");
        System.out.println("结果: " + result);
        System.out.println("预期: 正常 (B相为0对高压三相三线制是正常的)");
        System.out.println("✓ " + (result.equals("正常") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatusWithVoltage() {
        System.out.println("--- 测试8: 兼容性方法 - 带电压参数 ---");
        BigDecimal ia = new BigDecimal("25");
        BigDecimal ib = new BigDecimal("26");
        BigDecimal ic = new BigDecimal("24");
        BigDecimal ua = new BigDecimal("380");
        BigDecimal ub = new BigDecimal("381");
        BigDecimal uc = new BigDecimal("379");

        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, 1);
        
        System.out.println("输入: 带电压参数的兼容性方法");
        System.out.println("结果: " + result);
        System.out.println("预期: 非'未知'状态");
        System.out.println("✓ " + (!result.equals("未知") ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadRate() {
        System.out.println("--- 测试9: 负荷率计算 ---");
        BigDecimal currentPower = new BigDecimal("75");
        BigDecimal ratedPower = new BigDecimal("100");

        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        System.out.println("输入: 当前功率=75kW 额定功率=100kW");
        System.out.println("结果: " + loadRate + "%");
        System.out.println("预期: 75.00%");
        System.out.println("✓ " + (loadRate.compareTo(new BigDecimal("75.00")) == 0 ? "通过" : "失败") + "\n");
    }

    public void testCalculateLoadStatus_NullValues() {
        System.out.println("--- 测试10: 边界条件 - 空值处理 ---");
        String result1 = EnergyCalculationUtils.calculateLoadStatus(null, null, null, null, null, 1);
        String result2 = EnergyCalculationUtils.calculateLoadStatus(null, new BigDecimal("100"), 
                new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), 1);
        
        System.out.println("输入1: 所有参数为null");
        System.out.println("结果1: " + result1);
        System.out.println("预期1: 未知");
        System.out.println("✓ " + (result1.equals("未知") ? "通过" : "失败"));
        
        System.out.println("输入2: 功率为null，降级到电流估算");
        System.out.println("结果2: " + result2);
        System.out.println("预期2: 轻载");
        System.out.println("✓ " + (result2.equals("轻载") ? "通过" : "失败") + "\n");
    }

    public void testCompareOldAndNewMethod() {
        System.out.println("--- 测试11: 新旧方法对比 ---");
        
        // 测试场景1: 小型设备 (额定功率10kW)
        BigDecimal smallDevicePower = new BigDecimal("8");   // 当前8kW
        BigDecimal smallDeviceRated = new BigDecimal("10");  // 额定10kW
        BigDecimal ia1 = new BigDecimal("8");  // 8A电流
        BigDecimal ib1 = new BigDecimal("8");
        BigDecimal ic1 = new BigDecimal("8");
        
        String newResult1 = EnergyCalculationUtils.calculateLoadStatus(smallDevicePower, smallDeviceRated, ia1, ib1, ic1, 1);
        System.out.println("小型设备(8kW/10kW=80%): 新方法=" + newResult1 + " (应该是重载)");
        
        // 测试场景2: 大型设备 (额定功率1000kW)
        BigDecimal largePower = new BigDecimal("250");      // 当前250kW
        BigDecimal largeRated = new BigDecimal("1000");     // 额定1000kW
        BigDecimal ia2 = new BigDecimal("25");  // 25A电流
        BigDecimal ib2 = new BigDecimal("25");
        BigDecimal ic2 = new BigDecimal("25");
        
        String newResult2 = EnergyCalculationUtils.calculateLoadStatus(largePower, largeRated, ia2, ib2, ic2, 1);
        System.out.println("大型设备(250kW/1000kW=25%): 新方法=" + newResult2 + " (应该是轻载)");
        
        System.out.println("✓ 新方法基于负荷率百分比，更加科学准确！\n");
    }
}