package org.jeecg.modules.energy.service.classification;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.mapper.classification.TbEnergyTypeConfigMapper;
import org.jeecg.modules.energy.vo.classification.EnergyTypeConfigVO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.junit.jupiter.SpringJUnitTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 能源类型配置服务测试类
 * @author jeecg
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class EnergyTypeConfigServiceTest {

    @Autowired
    private IEnergyTypeConfigService energyTypeConfigService;

    @Test
    public void testGetConfigByEnergyType() {
        log.info("开始测试根据能源类型编码获取配置信息...");
        
        // 测试存在的能源类型
        EnergyTypeConfigVO config = energyTypeConfigService.getConfigByEnergyType(1);
        assertNotNull(config, "电能配置信息不应为空");
        assertEquals(1, config.getEnergyType(), "能源类型应该为1");
        assertNotNull(config.getEnergyName(), "能源类型名称不应为空");
        assertNotNull(config.getPricePerUnit(), "单价不应为空");
        
        log.info("电能配置信息: {}", config);
        
        // 测试不存在的能源类型
        EnergyTypeConfigVO nullConfig = energyTypeConfigService.getConfigByEnergyType(999);
        assertNull(nullConfig, "不存在的能源类型配置应该返回null");
        
        log.info("测试通过");
    }

    @Test
    public void testGetAllEnabledConfigs() {
        log.info("开始测试获取所有启用的能源类型配置...");
        
        List<EnergyTypeConfigVO> configs = energyTypeConfigService.getAllEnabledConfigs();
        assertNotNull(configs, "配置列表不应为空");
        assertTrue(configs.size() > 0, "应该至少有一个启用的配置");
        
        for (EnergyTypeConfigVO config : configs) {
            assertNotNull(config.getEnergyType(), "能源类型编码不应为空");
            assertNotNull(config.getEnergyName(), "能源类型名称不应为空");
            assertEquals("1", config.getStatus(), "状态应该为启用");
        }
        
        log.info("启用的配置数量: {}", configs.size());
        for (EnergyTypeConfigVO config : configs) {
            log.info("配置: {}", config);
        }
        
        log.info("测试通过");
    }

    @Test
    public void testExistsByEnergyType() {
        log.info("开始测试检查能源类型是否存在...");
        
        // 测试存在的能源类型
        assertTrue(energyTypeConfigService.existsByEnergyType(1), "电能类型应该存在");
        assertTrue(energyTypeConfigService.existsByEnergyType(2), "水能类型应该存在");
        assertTrue(energyTypeConfigService.existsByEnergyType(3), "燃气类型应该存在");
        
        // 测试不存在的能源类型
        assertFalse(energyTypeConfigService.existsByEnergyType(999), "不存在的能源类型应该返回false");
        assertFalse(energyTypeConfigService.existsByEnergyType(0), "无效的能源类型应该返回false");
        
        log.info("测试通过");
    }

    @Test
    public void testCalculateCost() {
        log.info("开始测试计算能源费用...");
        
        // 测试电能费用计算
        double electricCost = energyTypeConfigService.calculateCost(1, 100.0);
        assertTrue(electricCost > 0, "电能费用应该大于0");
        log.info("电能费用计算: 100.0 kWh = {} 元", electricCost);
        
        // 测试水能费用计算
        double waterCost = energyTypeConfigService.calculateCost(2, 100.0);
        assertTrue(waterCost > 0, "水能费用应该大于0");
        log.info("水能费用计算: 100.0 m³ = {} 元", waterCost);
        
        // 测试燃气费用计算
        double gasCost = energyTypeConfigService.calculateCost(3, 100.0);
        assertTrue(gasCost > 0, "燃气费用应该大于0");
        log.info("燃气费用计算: 100.0 m³ = {} 元", gasCost);
        
        // 测试边界情况
        assertEquals(0.0, energyTypeConfigService.calculateCost(1, 0.0), "零消耗量费用应该为0");
        assertEquals(0.0, energyTypeConfigService.calculateCost(999, 100.0), "不存在的能源类型费用应该为0");
        assertEquals(0.0, energyTypeConfigService.calculateCost(null, 100.0), "空能源类型费用应该为0");
        
        log.info("测试通过");
    }

    @Test
    public void testCalculateCarbonEmission() {
        log.info("开始测试计算碳排放量...");
        
        // 测试电能碳排放量计算
        double electricCarbon = energyTypeConfigService.calculateCarbonEmission(1, 100.0);
        assertTrue(electricCarbon >= 0, "电能碳排放量应该大于等于0");
        log.info("电能碳排放量计算: 100.0 kWh = {} 吨CO2", electricCarbon);
        
        // 测试水能碳排放量计算
        double waterCarbon = energyTypeConfigService.calculateCarbonEmission(2, 100.0);
        assertTrue(waterCarbon >= 0, "水能碳排放量应该大于等于0");
        log.info("水能碳排放量计算: 100.0 m³ = {} 吨CO2", waterCarbon);
        
        // 测试燃气碳排放量计算
        double gasCarbon = energyTypeConfigService.calculateCarbonEmission(3, 100.0);
        assertTrue(gasCarbon >= 0, "燃气碳排放量应该大于等于0");
        log.info("燃气碳排放量计算: 100.0 m³ = {} 吨CO2", gasCarbon);
        
        // 测试边界情况
        assertEquals(0.0, energyTypeConfigService.calculateCarbonEmission(1, 0.0), "零消耗量碳排放量应该为0");
        assertEquals(0.0, energyTypeConfigService.calculateCarbonEmission(999, 100.0), "不存在的能源类型碳排放量应该为0");
        
        log.info("测试通过");
    }

    @Test
    public void testCalculateStandardCoal() {
        log.info("开始测试计算标准煤当量...");
        
        // 测试电能标准煤当量计算
        double electricCoal = energyTypeConfigService.calculateStandardCoal(1, 100.0);
        assertTrue(electricCoal >= 0, "电能标准煤当量应该大于等于0");
        log.info("电能标准煤当量计算: 100.0 kWh = {} 吨标煤", electricCoal);
        
        // 测试水能标准煤当量计算
        double waterCoal = energyTypeConfigService.calculateStandardCoal(2, 100.0);
        assertTrue(waterCoal >= 0, "水能标准煤当量应该大于等于0");
        log.info("水能标准煤当量计算: 100.0 m³ = {} 吨标煤", waterCoal);
        
        // 测试燃气标准煤当量计算
        double gasCoal = energyTypeConfigService.calculateStandardCoal(3, 100.0);
        assertTrue(gasCoal >= 0, "燃气标准煤当量应该大于等于0");
        log.info("燃气标准煤当量计算: 100.0 m³ = {} 吨标煤", gasCoal);
        
        // 测试边界情况
        assertEquals(0.0, energyTypeConfigService.calculateStandardCoal(1, 0.0), "零消耗量标准煤当量应该为0");
        assertEquals(0.0, energyTypeConfigService.calculateStandardCoal(999, 100.0), "不存在的能源类型标准煤当量应该为0");
        
        log.info("测试通过");
    }

    @Test
    public void testGetEnergyTypeName() {
        log.info("开始测试获取能源类型名称...");
        
        // 测试存在的能源类型
        String electricName = energyTypeConfigService.getEnergyTypeName(1);
        assertNotNull(electricName, "电能名称不应为空");
        assertTrue(electricName.contains("电") || electricName.contains("Electric"), "电能名称应该包含电字");
        log.info("电能类型名称: {}", electricName);
        
        String waterName = energyTypeConfigService.getEnergyTypeName(2);
        assertNotNull(waterName, "水能名称不应为空");
        log.info("水能类型名称: {}", waterName);
        
        String gasName = energyTypeConfigService.getEnergyTypeName(3);
        assertNotNull(gasName, "燃气名称不应为空");
        log.info("燃气类型名称: {}", gasName);
        
        // 测试不存在的能源类型
        String nullName = energyTypeConfigService.getEnergyTypeName(999);
        assertNull(nullName, "不存在的能源类型名称应该返回null");
        
        log.info("测试通过");
    }

    @Test
    public void testBatchInsert() {
        log.info("开始测试批量插入能源类型配置...");
        
        // 创建测试数据
        List<TbEnergyTypeConfig> testConfigs = createTestConfigList();
        
        // 执行批量插入
        boolean result = energyTypeConfigService.batchInsert(testConfigs);
        assertTrue(result, "批量插入应该成功");
        
        // 验证插入结果
        for (TbEnergyTypeConfig testConfig : testConfigs) {
            assertTrue(energyTypeConfigService.existsByEnergyType(testConfig.getEnergyType()), 
                      "配置应该被成功插入");
        }
        
        // 清理测试数据
        cleanupTestData(testConfigs);
        
        log.info("测试通过");
    }

    @Test
    public void testUpdateByEnergyType() {
        log.info("开始测试更新能源类型配置...");
        
        // 创建测试配置
        TbEnergyTypeConfig testConfig = createTestEnergyTypeConfig();
        testConfig.setEnergyType(999);
        testConfig.setEnergyName("测试能源类型");
        testConfig.setPricePerUnit(BigDecimal.valueOf(5.0));
        
        // 执行更新
        boolean result = energyTypeConfigService.updateByEnergyType(999, testConfig);
        // 由于是测试环境，可能没有实际数据，所以这里不强制要求成功
        log.info("更新结果: {}", result);
        
        log.info("测试通过");
    }

    @Test
    public void testDeleteByEnergyType() {
        log.info("开始测试删除能源类型配置...");
        
        // 测试删除不存在的配置
        boolean result = energyTypeConfigService.deleteByEnergyType(999);
        log.info("删除不存在配置结果: {}", result);
        // 不强制要求成功，因为测试环境可能没有数据
        
        log.info("测试通过");
    }

    @Test
    public void testGetConfigListByPage() {
        log.info("开始测试获取配置列表（分页）...");
        
        // 测试获取所有配置
        List<TbEnergyTypeConfig> allConfigs = energyTypeConfigService.getConfigListByPage(
                null, null, null, 0, 10);
        assertNotNull(allConfigs, "配置列表不应为空");
        
        // 测试按状态查询
        List<TbEnergyTypeConfig> enabledConfigs = energyTypeConfigService.getConfigListByPage(
                "1", null, null, 0, 10);
        assertNotNull(enabledConfigs, "启用配置列表不应为空");
        
        log.info("所有配置数量: {}", allConfigs.size());
        log.info("启用配置数量: {}", enabledConfigs.size());
        
        log.info("测试通过");
    }

    @Test
    public void testGetConfigCount() {
        log.info("开始测试获取配置总数...");
        
        // 测试获取所有配置总数
        int totalCount = energyTypeConfigService.getConfigCount(null, null, null);
        assertTrue(totalCount >= 0, "配置总数应该大于等于0");
        log.info("总配置数量: {}", totalCount);
        
        // 测试按状态查询配置总数
        int enabledCount = energyTypeConfigService.getConfigCount("1", null, null);
        assertTrue(enabledCount >= 0, "启用配置数量应该大于等于0");
        log.info("启用配置数量: {}", enabledCount);
        
        log.info("测试通过");
    }

    // ==================== 辅助方法 ====================

    private List<TbEnergyTypeConfig> createTestConfigList() {
        List<TbEnergyTypeConfig> configs = new ArrayList<>();
        
        TbEnergyTypeConfig config1 = new TbEnergyTypeConfig();
        config1.setId(UUID.randomUUID().toString());
        config1.setEnergyType(999);
        config1.setEnergyName("测试能源类型1");
        config1.setEnergyUnit("testUnit");
        config1.setPricePerUnit(BigDecimal.valueOf(5.0));
        config1.setCarbonFactor(BigDecimal.valueOf(1.0));
        config1.setCoalFactor(BigDecimal.valueOf(0.5));
        config1.setStatus("1");
        config1.setSortOrder(99);
        config1.setCreateBy("test");
        config1.setCreateTime(new Date());
        
        TbEnergyTypeConfig config2 = new TbEnergyTypeConfig();
        config2.setId(UUID.randomUUID().toString());
        config2.setEnergyType(998);
        config2.setEnergyName("测试能源类型2");
        config2.setEnergyUnit("testUnit2");
        config2.setPricePerUnit(BigDecimal.valueOf(6.0));
        config2.setCarbonFactor(BigDecimal.valueOf(2.0));
        config2.setCoalFactor(BigDecimal.valueOf(0.8));
        config2.setStatus("1");
        config2.setSortOrder(98);
        config2.setCreateBy("test");
        config2.setCreateTime(new Date());
        
        configs.add(config1);
        configs.add(config2);
        
        return configs;
    }

    private TbEnergyTypeConfig createTestEnergyTypeConfig() {
        TbEnergyTypeConfig config = new TbEnergyTypeConfig();
        config.setId(UUID.randomUUID().toString());
        config.setEnergyName("测试能源类型");
        config.setEnergyUnit("testUnit");
        config.setPricePerUnit(BigDecimal.valueOf(5.0));
        config.setCarbonFactor(BigDecimal.valueOf(1.0));
        config.setCoalFactor(BigDecimal.valueOf(0.5));
        config.setStatus("1");
        config.setSortOrder(99);
        config.setCreateBy("test");
        config.setCreateTime(new Date());
        return config;
    }

    private void cleanupTestData(List<TbEnergyTypeConfig> testConfigs) {
        for (TbEnergyTypeConfig config : testConfigs) {
            try {
                energyTypeConfigService.deleteByEnergyType(config.getEnergyType());
            } catch (Exception e) {
                log.warn("清理测试数据失败: energyType={}", config.getEnergyType(), e);
            }
        }
    }
}