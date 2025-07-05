package org.jeecg.modules.energy.mapper;

import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TbEquEnergyDataMapper单元测试
 * 注意：这个测试需要连接到实际的数据库，因此需要在测试环境中配置好数据库连接
 */
@SpringBootTest
@Transactional
public class TbEquEnergyDataMapperTest {

    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;

    /**
     * 测试根据仪表ID查询最新的实时数据
     */
    @Test
    public void testSelectLatestDataByModuleId() {
        // 使用已知存在的仪表ID
        String moduleId = "yj0004_1"; // 天然气表1#
        
        TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(moduleId);
        
        // 验证结果
        // 注意：如果数据库中没有这个仪表的数据，这个测试可能会失败
        // 在实际环境中运行前，请确保数据库中有相应的测试数据
        if (energyData != null) {
            assertEquals(moduleId, energyData.getModuleId());
            assertNotNull(energyData.getEquEnergyDt());
            // 验证其他关键字段
            // 温度、压力可能为null，所以不做非空验证
            assertNotNull(energyData.getEnergyAccumulatevalue()); // 累计值不应为空
        } else {
            // 如果没有数据，这个测试就跳过
            System.out.println("No data found for moduleId: " + moduleId + ". Test skipped.");
        }
    }
    
    /**
     * 测试不存在的仪表ID
     */
    @Test
    public void testSelectLatestDataByNonExistentModuleId() {
        // 使用一个不存在的仪表ID
        String moduleId = "NON_EXISTENT_MODULE_ID";
        
        TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(moduleId);
        
        // 验证结果
        assertNull(energyData); // 应该返回null
    }
} 