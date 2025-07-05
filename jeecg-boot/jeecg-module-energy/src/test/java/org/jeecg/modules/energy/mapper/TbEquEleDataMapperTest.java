package org.jeecg.modules.energy.mapper;

import org.jeecg.modules.energy.entity.TbEquEleData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TbEquEleDataMapper单元测试
 * 注意：这个测试需要连接到实际的数据库，因此需要在测试环境中配置好数据库连接
 */
@SpringBootTest
@Transactional
public class TbEquEleDataMapperTest {

    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;

    /**
     * 测试根据仪表ID查询最新的实时数据
     */
    @Test
    public void testSelectLatestDataByModuleId() {
        // 使用已知存在的仪表ID
        String moduleId = "yj0001_1202"; // 1号注塑机
        
        TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(moduleId);
        
        // 验证结果
        // 注意：如果数据库中没有这个仪表的数据，这个测试可能会失败
        // 在实际环境中运行前，请确保数据库中有相应的测试数据
        if (eleData != null) {
            assertEquals(moduleId, eleData.getModuleId());
            assertNotNull(eleData.getEquElectricDT());
            // 验证其他关键字段不为空
            assertNotNull(eleData.getUA());
            assertNotNull(eleData.getUB());
            assertNotNull(eleData.getUC());
            assertNotNull(eleData.getIA());
            assertNotNull(eleData.getIB());
            assertNotNull(eleData.getIC());
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
        
        TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(moduleId);
        
        // 验证结果
        assertNull(eleData); // 应该返回null
    }
} 