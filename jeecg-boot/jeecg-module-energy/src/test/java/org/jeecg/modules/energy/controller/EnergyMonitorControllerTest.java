package org.jeecg.modules.energy.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IEnergyMonitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 能源监控控制器单元测试
 */
public class EnergyMonitorControllerTest {

    @InjectMocks
    private EnergyMonitorController energyMonitorController;

    @Mock
    private IEnergyMonitorService energyMonitorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试获取电力类型的实时数据接口
     */
    @Test
    public void testGetRealTimeDataForElectric() {
        // 准备测试数据
        String orgCode = "A02A02A01";
        Integer nowtype = 1; // 按部门用电

        // 模拟服务层返回数据
        List<Map<String, Object>> serviceResult = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("module_name", "1号注塑机");
        dataMap.put("module_id", "yj0001_1202");
        dataMap.put("rated_power", 1000.0);
        dataMap.put("energy_type", 1);
        dataMap.put("dailyPower", new BigDecimal("125.67"));
        dataMap.put("Equ_Electric_DT", new Date());
        dataMap.put("loadStatus", "正常");
        dataMap.put("loadRate", new BigDecimal("63.10"));
        dataMap.put("PFS", new BigDecimal("0.91"));
        dataMap.put("HZ", new BigDecimal("50.00"));
        dataMap.put("pp", new BigDecimal("28.49"));
        dataMap.put("UA", new BigDecimal("220.50"));
        dataMap.put("UB", new BigDecimal("219.80"));
        dataMap.put("UC", new BigDecimal("221.20"));
        dataMap.put("IA", new BigDecimal("60.89"));
        dataMap.put("IB", new BigDecimal("12.93"));
        dataMap.put("IC", new BigDecimal("28.49"));
        dataMap.put("PFa", new BigDecimal("0.90"));
        dataMap.put("PFb", new BigDecimal("0.90"));
        dataMap.put("PFc", new BigDecimal("0.93"));
        dataMap.put("Pa", new BigDecimal("13.40"));
        dataMap.put("Pb", new BigDecimal("2.84"));
        dataMap.put("Pc", new BigDecimal("12.25"));
        dataMap.put("KWH", new BigDecimal("1256.78"));
        dataMap.put("KVARH", new BigDecimal("605.42"));
        serviceResult.add(dataMap);

        // 配置Mock行为
        when(energyMonitorService.getRealTimeData(anyString(), anyInt())).thenReturn(serviceResult);

        // 执行测试
        Result<List<Map<String, Object>>> result = energyMonitorController.getRealTimeData(orgCode, nowtype);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        
        List<Map<String, Object>> resultData = result.getResult();
        assertNotNull(resultData);
        assertEquals(1, resultData.size());
        
        Map<String, Object> resultMap = resultData.get(0);
        assertEquals("1号注塑机", resultMap.get("module_name"));
        assertEquals("yj0001_1202", resultMap.get("module_id"));
        assertEquals(1000.0, resultMap.get("rated_power"));
        assertEquals(1, resultMap.get("energy_type"));
        assertEquals(new BigDecimal("125.67"), resultMap.get("dailyPower"));
        assertEquals("正常", resultMap.get("loadStatus"));
        assertEquals(new BigDecimal("63.10"), resultMap.get("loadRate"));
    }

    /**
     * 测试获取天然气类型的实时数据接口
     */
    @Test
    public void testGetRealTimeDataForGas() {
        // 准备测试数据
        String orgCode = "A02A04A01";
        Integer nowtype = 3; // 天然气

        // 模拟服务层返回数据
        List<Map<String, Object>> serviceResult = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("module_name", "天然气表1#");
        dataMap.put("module_id", "yj0004_1");
        dataMap.put("rated_power", 1.0);
        dataMap.put("energy_type", 8);
        dataMap.put("dailyPower", new BigDecimal("125.67"));
        dataMap.put("equ_energy_dt", new Date());
        dataMap.put("energy_temperature", 25.6);
        dataMap.put("energy_pressure", 0.8);
        dataMap.put("energy_winkvalue", new BigDecimal("2.345"));
        dataMap.put("energy_accumulatevalue", new BigDecimal("1256.78"));
        serviceResult.add(dataMap);

        // 配置Mock行为
        when(energyMonitorService.getRealTimeData(anyString(), anyInt())).thenReturn(serviceResult);

        // 执行测试
        Result<List<Map<String, Object>>> result = energyMonitorController.getRealTimeData(orgCode, nowtype);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        
        List<Map<String, Object>> resultData = result.getResult();
        assertNotNull(resultData);
        assertEquals(1, resultData.size());
        
        Map<String, Object> resultMap = resultData.get(0);
        assertEquals("天然气表1#", resultMap.get("module_name"));
        assertEquals("yj0004_1", resultMap.get("module_id"));
        assertEquals(1.0, resultMap.get("rated_power"));
        assertEquals(8, resultMap.get("energy_type"));
        assertEquals(new BigDecimal("125.67"), resultMap.get("dailyPower"));
        assertEquals(25.6, resultMap.get("energy_temperature"));
        assertEquals(0.8, resultMap.get("energy_pressure"));
        assertEquals(new BigDecimal("2.345"), resultMap.get("energy_winkvalue"));
        assertEquals(new BigDecimal("1256.78"), resultMap.get("energy_accumulatevalue"));
    }
} 