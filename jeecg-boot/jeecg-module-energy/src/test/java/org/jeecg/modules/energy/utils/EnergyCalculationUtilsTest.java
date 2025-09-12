package org.jeecg.modules.energy.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 能源计算工具类单元测试
 */
public class EnergyCalculationUtilsTest {

    /**
     * 测试计算负荷状态 - 停机情况
     */
    @Test
    public void testCalculateLoadStatusStopped() {
        // 三相电流接近0，表示设备停机
        BigDecimal IA = new BigDecimal("0.2");
        BigDecimal IB = new BigDecimal("0.3");
        BigDecimal IC = new BigDecimal("0.1");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("停机", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 轻载情况（改进后的算法）
     */
    @Test
    public void testCalculateLoadStatusLight() {
        // 使用改进后的算法，基于电流估算
        BigDecimal IA = new BigDecimal("3.00");
        BigDecimal IB = new BigDecimal("3.50");
        BigDecimal IC = new BigDecimal("3.20");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("轻载", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 正常情况（改进后的算法）
     */
    @Test
    public void testCalculateLoadStatusNormal() {
        // 使用改进后的算法，基于电流估算
        BigDecimal IA = new BigDecimal("15.00");
        BigDecimal IB = new BigDecimal("16.00");
        BigDecimal IC = new BigDecimal("14.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("正常", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 重载情况（改进后的算法）
     */
    @Test
    public void testCalculateLoadStatusHeavy() {
        // 使用改进后的算法，基于电流估算
        BigDecimal IA = new BigDecimal("35.00");
        BigDecimal IB = new BigDecimal("36.00");
        BigDecimal IC = new BigDecimal("34.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("重载", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 过载情况（改进后的算法）
     */
    @Test
    public void testCalculateLoadStatusOverload() {
        // 使用改进后的算法，基于电流估算
        BigDecimal IA = new BigDecimal("60.00");
        BigDecimal IB = new BigDecimal("62.00");
        BigDecimal IC = new BigDecimal("58.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("过载", loadStatus);
    }
    
    /**
     * 测试三相不平衡检测
     */
    @Test
    public void testThreePhaseImbalance() {
        // 三相电流差异较大，应该检测出不平衡
        BigDecimal IA = new BigDecimal("30.00");
        BigDecimal IB = new BigDecimal("10.00");  // B相电流明显偏小
        BigDecimal IC = new BigDecimal("25.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("三相不平衡", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 参数为null的情况
     */
    @Test
    public void testCalculateLoadStatusWithNull() {
        // 部分参数为null
        BigDecimal IA = new BigDecimal("40.00");
        BigDecimal IB = null;
        BigDecimal IC = new BigDecimal("38.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("未知", loadStatus);
    }
    
    /**
     * 测试计算负荷率
     */
    @Test
    public void testCalculateLoadRate() {
        // 当前功率为500，额定功率为1000
        BigDecimal currentPower = new BigDecimal("500.00");
        BigDecimal ratedPower = new BigDecimal("1000.00");
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        assertEquals(new BigDecimal("50.00"), loadRate);
    }
    
    /**
     * 测试计算负荷率 - 额定功率为0
     */
    @Test
    public void testCalculateLoadRateWithZeroRatedPower() {
        BigDecimal currentPower = new BigDecimal("500.00");
        BigDecimal ratedPower = BigDecimal.ZERO;
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        assertEquals(BigDecimal.ZERO, loadRate);
    }
    
    /**
     * 测试计算负荷率 - 额定功率为null
     */
    @Test
    public void testCalculateLoadRateWithNullRatedPower() {
        BigDecimal currentPower = new BigDecimal("500.00");
        BigDecimal ratedPower = null;
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        assertEquals(BigDecimal.ZERO, loadRate);
    }
    
    /**
     * 测试计算负荷率 - 当前功率为null
     */
    @Test
    public void testCalculateLoadRateWithNullCurrentPower() {
        BigDecimal currentPower = null;
        BigDecimal ratedPower = new BigDecimal("1000.00");
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        assertEquals(BigDecimal.ZERO, loadRate);
    }
} 