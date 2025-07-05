package org.jeecg.modules.energy.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 能源计算工具类单元测试
 */
public class EnergyCalculationUtilsTest {

    /**
     * 测试计算负荷状态 - 正常情况
     */
    @Test
    public void testCalculateLoadStatusNormal() {
        // 三相电流平衡，电压正常
        BigDecimal IA = new BigDecimal("60.00");
        BigDecimal IB = new BigDecimal("61.00");
        BigDecimal IC = new BigDecimal("59.00");
        BigDecimal UA = new BigDecimal("220.00");
        BigDecimal UB = new BigDecimal("220.00");
        BigDecimal UC = new BigDecimal("220.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("正常", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 警告情况
     */
    @Test
    public void testCalculateLoadStatusWarning() {
        // 三相电流不平衡度在10%-20%之间
        BigDecimal IA = new BigDecimal("60.00");
        BigDecimal IB = new BigDecimal("70.00"); // 偏高约16.7%
        BigDecimal IC = new BigDecimal("59.00");
        BigDecimal UA = new BigDecimal("220.00");
        BigDecimal UB = new BigDecimal("220.00");
        BigDecimal UC = new BigDecimal("220.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("警告", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 异常情况(电流不平衡)
     */
    @Test
    public void testCalculateLoadStatusAbnormalCurrent() {
        // 三相电流不平衡度超过20%
        BigDecimal IA = new BigDecimal("60.00");
        BigDecimal IB = new BigDecimal("80.00"); // 偏高约33.3%
        BigDecimal IC = new BigDecimal("59.00");
        BigDecimal UA = new BigDecimal("220.00");
        BigDecimal UB = new BigDecimal("220.00");
        BigDecimal UC = new BigDecimal("220.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("异常", loadStatus);
    }
    
    /**
     * 测试计算负荷状态 - 异常情况(电压异常)
     */
    @Test
    public void testCalculateLoadStatusAbnormalVoltage() {
        // 电压异常
        BigDecimal IA = new BigDecimal("60.00");
        BigDecimal IB = new BigDecimal("61.00");
        BigDecimal IC = new BigDecimal("59.00");
        BigDecimal UA = new BigDecimal("220.00");
        BigDecimal UB = new BigDecimal("250.00"); // 电压过高
        BigDecimal UC = new BigDecimal("220.00");
        
        String loadStatus = EnergyCalculationUtils.calculateLoadStatus(IA, IB, IC, UA, UB, UC);
        
        assertEquals("异常", loadStatus);
    }
    
    /**
     * 测试计算负荷率
     */
    @Test
    public void testCalculateLoadRate() {
        // 当前功率为500，额定功率为1000
        BigDecimal pp = new BigDecimal("500.00");
        BigDecimal ratedPower = new BigDecimal("1000.00");
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(pp, ratedPower);
        
        assertEquals(new BigDecimal("50.00"), loadRate);
    }
    
    /**
     * 测试计算负荷率 - 额定功率为0
     */
    @Test
    public void testCalculateLoadRateWithZeroRatedPower() {
        BigDecimal pp = new BigDecimal("500.00");
        BigDecimal ratedPower = BigDecimal.ZERO;
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(pp, ratedPower);
        
        assertEquals(BigDecimal.ZERO, loadRate);
    }
    
    /**
     * 测试计算负荷率 - 额定功率为null
     */
    @Test
    public void testCalculateLoadRateWithNullRatedPower() {
        BigDecimal pp = new BigDecimal("500.00");
        BigDecimal ratedPower = null;
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(pp, ratedPower);
        
        assertEquals(BigDecimal.ZERO, loadRate);
    }
    
    /**
     * 测试计算最大偏差
     */
    @Test
    public void testCalculateMaxDeviation() {
        BigDecimal a = new BigDecimal("60.00");
        BigDecimal b = new BigDecimal("70.00");
        BigDecimal c = new BigDecimal("59.00");
        BigDecimal avg = new BigDecimal("63.00");
        
        BigDecimal maxDeviation = EnergyCalculationUtils.calculateMaxDeviation(a, b, c, avg);
        
        // 最大偏差应该是 (70-63)/63 = 0.111...
        assertEquals(new BigDecimal("0.11"), maxDeviation);
    }
} 