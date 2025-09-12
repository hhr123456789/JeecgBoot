package org.jeecg.modules.energy.utils;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * High Voltage Three-Phase Three-Wire Device Test
 */
public class EnergyCalculationUtilsHighVoltageTest {

    /**
     * Test high voltage three-phase three-wire device with B-phase = 0 (normal case)
     */
    @Test
    public void testHighVoltageThreeWireNormal() {
        BigDecimal ia = new BigDecimal("100.0");
        BigDecimal ib = new BigDecimal("0.0");    // B-phase is 0, which is normal for high voltage three-wire
        BigDecimal ic = new BigDecimal("95.0");
        BigDecimal ua = new BigDecimal("10000.0");
        BigDecimal ub = new BigDecimal("10000.0");
        BigDecimal uc = new BigDecimal("10000.0");
        Integer moduleType = 4; // High voltage three-phase three-wire
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        // Should return NORMAL, not imbalance
        assertEquals("NORMAL", result);
        System.out.println("High voltage device (B=0): " + result);
    }

    /**
     * Test high voltage device with B-phase having current (wiring error)
     */
    @Test
    public void testHighVoltageThreeWireWiringError() {
        BigDecimal ia = new BigDecimal("80.0");
        BigDecimal ib = new BigDecimal("15.0");   // B-phase should be 0, having current indicates wiring error
        BigDecimal ic = new BigDecimal("75.0");
        BigDecimal ua = new BigDecimal("10000.0");
        BigDecimal ub = new BigDecimal("10000.0");
        BigDecimal uc = new BigDecimal("10000.0");
        Integer moduleType = 4; // High voltage three-phase three-wire
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        assertEquals("WIRING_ERROR", result);
        System.out.println("High voltage device (B>0): " + result);
    }

    /**
     * Test high voltage device with A and C phase imbalance
     */
    @Test
    public void testHighVoltageThreeWireTwoPhaseImbalance() {
        BigDecimal ia = new BigDecimal("100.0");
        BigDecimal ib = new BigDecimal("0.0");    // B-phase is 0 (normal)
        BigDecimal ic = new BigDecimal("60.0");   // C-phase much lower, causing imbalance
        BigDecimal ua = new BigDecimal("10000.0");
        BigDecimal ub = new BigDecimal("10000.0");
        BigDecimal uc = new BigDecimal("10000.0");
        Integer moduleType = 4; // High voltage three-phase three-wire
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        // Imbalance rate = |100-60| / ((100+60)/2) * 100% = 40/80 * 100% = 50% > 15%
        assertEquals("TWO_PHASE_IMBALANCE", result);
        System.out.println("High voltage device (A-C imbalance): " + result);
    }

    /**
     * Test normal three-phase device (not high voltage)
     */
    @Test
    public void testNormalThreePhaseDevice() {
        BigDecimal ia = new BigDecimal("100.0");
        BigDecimal ib = new BigDecimal("0.0");    // B-phase is 0
        BigDecimal ic = new BigDecimal("95.0");
        BigDecimal ua = new BigDecimal("380.0");
        BigDecimal ub = new BigDecimal("380.0");
        BigDecimal uc = new BigDecimal("380.0");
        Integer moduleType = 1; // Normal device (not high voltage three-wire)
        
        String result = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, moduleType);
        
        // For normal device, B-phase being 0 should be detected as imbalance
        assertEquals("THREE_PHASE_IMBALANCE", result);
        System.out.println("Normal device (B=0): " + result);
    }

    /**
     * Test power-based calculation with high voltage device
     */
    @Test
    public void testHighVoltagePowerBasedCalculation() {
        BigDecimal currentPower = new BigDecimal("800.0");
        BigDecimal ratedPower = new BigDecimal("1000.0");
        BigDecimal ia = new BigDecimal("100.0");
        BigDecimal ib = new BigDecimal("0.0");    // B-phase is 0 (normal for high voltage)
        BigDecimal ic = new BigDecimal("95.0");
        Integer moduleType = 4; // High voltage three-phase three-wire
        
        String result = EnergyCalculationUtils.calculateLoadStatus(currentPower, ratedPower, ia, ib, ic, moduleType);
        
        // Load rate = 800/1000 * 100% = 80%, should be NORMAL (30%-80%)
        assertEquals("NORMAL", result);
        System.out.println("High voltage device (power-based): " + result);
    }

    /**
     * Test comparison between old and new algorithm
     */
    @Test
    public void testAlgorithmComparison() {
        System.out.println("\n=== Algorithm Comparison ===");
        
        // High voltage device: A=100A, B=0A, C=95A
        BigDecimal ia = new BigDecimal("100.0");
        BigDecimal ib = new BigDecimal("0.0");
        BigDecimal ic = new BigDecimal("95.0");
        BigDecimal ua = new BigDecimal("10000.0");
        BigDecimal ub = new BigDecimal("10000.0");
        BigDecimal uc = new BigDecimal("10000.0");
        
        // Old algorithm (treat as normal device)
        String oldResult = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, 1);
        System.out.println("Old algorithm (normal device): " + oldResult);
        
        // New algorithm (high voltage device)
        String newResult = EnergyCalculationUtils.calculateLoadStatus(ia, ib, ic, ua, ub, uc, 4);
        System.out.println("New algorithm (high voltage): " + newResult);
        
        // Verify the improvement
        assertEquals("THREE_PHASE_IMBALANCE", oldResult); // Old: incorrect judgment
        assertEquals("NORMAL", newResult);                // New: correct judgment
        
        System.out.println("Algorithm improvement verified!");
    }

    /**
     * Test load rate calculation
     */
    @Test
    public void testLoadRateCalculation() {
        BigDecimal currentPower = new BigDecimal("750.0");
        BigDecimal ratedPower = new BigDecimal("1000.0");
        
        BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(currentPower, ratedPower);
        
        assertEquals(new BigDecimal("75.00"), loadRate);
        System.out.println("Load rate: " + loadRate + "%");
    }
}