package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.impl.EnergyMonitorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 能源监控服务单元测试
 */
public class EnergyMonitorServiceTest {

    @InjectMocks
    private EnergyMonitorServiceImpl energyMonitorService;

    @Mock
    private TbModuleMapper tbModuleMapper;

    @Mock
    private TbEquEleDataMapper tbEquEleDataMapper;

    @Mock
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;

    @Mock
    private TbEpEquEnergyDaycountMapper tbEpEquEnergyDaycountMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试获取电力类型的实时数据
     */
    @Test
    public void testGetRealTimeDataForElectric() {
        // 准备测试数据
        String orgCode = "A02A02A01";
        Integer nowtype = 1; // 按部门用电

        // 模拟仪表数据
        List<TbModule> modules = new ArrayList<>();
        TbModule module = new TbModule();
        module.setId("1");
        module.setModuleName("1号注塑机");
        module.setModuleId("yj0001_1202");
        module.setEnergyType(1); // 电力类型
        module.setRatedPower(1000.0);
        modules.add(module);

        // 模拟电力数据
        TbEquEleData eleData = new TbEquEleData();
        eleData.setEquElectricDT(new Date());
        eleData.setUA(new BigDecimal("220.50"));
        eleData.setUB(new BigDecimal("219.80"));
        eleData.setUC(new BigDecimal("221.20"));
        eleData.setIA(new BigDecimal("60.89"));
        eleData.setIB(new BigDecimal("12.93"));
        eleData.setIC(new BigDecimal("28.49"));
        eleData.setPFS(new BigDecimal("0.91"));
        eleData.setHZ(new BigDecimal("50.00"));
        eleData.setPp(new BigDecimal("28.49"));
        eleData.setPFa(new BigDecimal("0.90"));
        eleData.setPFb(new BigDecimal("0.90"));
        eleData.setPFc(new BigDecimal("0.93"));
        eleData.setPa(new BigDecimal("13.40"));
        eleData.setPb(new BigDecimal("2.84"));
        eleData.setPc(new BigDecimal("12.25"));
        eleData.setKWH(new BigDecimal("1256.78"));
        eleData.setKVARH(new BigDecimal("605.42"));

        // 模拟日统计数据
        TbEpEquEnergyDaycount dayCount = new TbEpEquEnergyDaycount();
        dayCount.setEnergyCount(new BigDecimal("125.67"));

        // 配置Mock行为
        when(tbModuleMapper.selectModulesByOrgCode(anyString())).thenReturn(modules);
        when(tbEquEleDataMapper.selectLatestDataByModuleId(anyString())).thenReturn(eleData);
        when(tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(anyString(), any(Date.class))).thenReturn(dayCount);

        // 执行测试
        List<Map<String, Object>> result = energyMonitorService.getRealTimeData(orgCode, nowtype);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> dataMap = result.get(0);
        assertEquals("1号注塑机", dataMap.get("module_name"));
        assertEquals("yj0001_1202", dataMap.get("module_id"));
        assertEquals(1000.0, dataMap.get("rated_power"));
        assertEquals(1, dataMap.get("energy_type"));
        assertEquals(new BigDecimal("125.67"), dataMap.get("dailyPower"));
        
        // 验证电力数据
        assertNotNull(dataMap.get("Equ_Electric_DT"));
        assertEquals("正常", dataMap.get("loadStatus")); // 根据模拟数据应该是"正常"
        assertEquals(new BigDecimal("2.85"), dataMap.get("loadRate")); // 28.49 / 1000 * 100 = 2.85
        assertEquals(new BigDecimal("0.91"), dataMap.get("PFS"));
        assertEquals(new BigDecimal("50.00"), dataMap.get("HZ"));
        assertEquals(new BigDecimal("28.49"), dataMap.get("pp"));
        assertEquals(new BigDecimal("220.50"), dataMap.get("UA"));
        assertEquals(new BigDecimal("219.80"), dataMap.get("UB"));
        assertEquals(new BigDecimal("221.20"), dataMap.get("UC"));
        assertEquals(new BigDecimal("60.89"), dataMap.get("IA"));
        assertEquals(new BigDecimal("12.93"), dataMap.get("IB"));
        assertEquals(new BigDecimal("28.49"), dataMap.get("IC"));
    }

    /**
     * 测试获取天然气类型的实时数据
     */
    @Test
    public void testGetRealTimeDataForGas() {
        // 准备测试数据
        String orgCode = "A02A04A01";
        Integer nowtype = 3; // 天然气

        // 模拟仪表数据
        List<TbModule> modules = new ArrayList<>();
        TbModule module = new TbModule();
        module.setId("2");
        module.setModuleName("天然气表1#");
        module.setModuleId("yj0004_1");
        module.setEnergyType(8); // 天然气类型
        module.setRatedPower(1.0);
        modules.add(module);

        // 模拟能源数据
        TbEquEnergyData energyData = new TbEquEnergyData();
        energyData.setEquEnergyDt(new Date());
        energyData.setEnergyTemperature(25.6);
        energyData.setEnergyPressure(0.8);
        energyData.setEnergyWinkvalue(new BigDecimal("2.345"));
        energyData.setEnergyAccumulatevalue(new BigDecimal("1256.78"));

        // 模拟日统计数据
        TbEpEquEnergyDaycount dayCount = new TbEpEquEnergyDaycount();
        dayCount.setEnergyCount(new BigDecimal("125.67"));

        // 配置Mock行为
        when(tbModuleMapper.selectModulesByOrgCode(anyString())).thenReturn(modules);
        when(tbEquEnergyDataMapper.selectLatestDataByModuleId(anyString())).thenReturn(energyData);
        when(tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(anyString(), any(Date.class))).thenReturn(dayCount);

        // 执行测试
        List<Map<String, Object>> result = energyMonitorService.getRealTimeData(orgCode, nowtype);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> dataMap = result.get(0);
        assertEquals("天然气表1#", dataMap.get("module_name"));
        assertEquals("yj0004_1", dataMap.get("module_id"));
        assertEquals(1.0, dataMap.get("rated_power"));
        assertEquals(8, dataMap.get("energy_type"));
        assertEquals(new BigDecimal("125.67"), dataMap.get("dailyPower"));
        
        // 验证能源数据
        assertNotNull(dataMap.get("equ_energy_dt"));
        assertEquals(25.6, dataMap.get("energy_temperature"));
        assertEquals(0.8, dataMap.get("energy_pressure"));
        assertEquals(new BigDecimal("2.345"), dataMap.get("energy_winkvalue"));
        assertEquals(new BigDecimal("1256.78"), dataMap.get("energy_accumulatevalue"));
    }
} 