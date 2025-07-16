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
     * 测试计算负荷状态 - 低负荷情况
     */
    @Test
    public void testCalculateLoadStatusLow() {
        // 三相电流平均值小于10A，表示低负荷
        BigDecimal IA = new BigDecimal("8.00");
        BigDecimal IB = new BigDecimal("7.00");
        BigDecimal IC = new BigDecimal("9.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("低负荷", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 中负荷情况
     */
    @Test
    public void testCalculateLoadStatusMedium() {
        // 三相电流平均值在10A-30A之间，表示中负荷
        BigDecimal IA = new BigDecimal("20.00");
        BigDecimal IB = new BigDecimal("22.00");
        BigDecimal IC = new BigDecimal("18.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("中负荷", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 高负荷情况
     */
    @Test
    public void testCalculateLoadStatusHigh() {
        // 三相电流平均值大于30A，表示高负荷
        BigDecimal IA = new BigDecimal("40.00");
        BigDecimal IB = new BigDecimal("42.00");
        BigDecimal IC = new BigDecimal("38.00");
        BigDecimal UA = new BigDecimal("380.00");
        BigDecimal UB = new BigDecimal("380.00");
        BigDecimal UC = new BigDecimal("380.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("高负荷", loadStatus);
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