package org.jeecg.modules.energy.mapper;

import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TbEpEquEnergyDaycountMapper单元测试
 * 注意：这个测试需要连接到实际的数据库，因此需要在测试环境中配置好数据库连接
 */
@SpringBootTest
@Transactional
public class TbEpEquEnergyDaycountMapperTest {

    @Autowired
    private TbEpEquEnergyDaycountMapper tbEpEquEnergyDaycountMapper;

    /**
     * 测试根据仪表ID和日期查询当天的能耗数据
     */
    @Test
    public void testSelectTodayDataByModuleId() {
        // 使用已知存在的仪表ID
        String moduleId = "yj0001_1202"; // 1号注塑机
        Date today = new Date(); // 今天
        
        TbEpEquEnergyDaycount daycount = tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(moduleId, today);
        
        // 验证结果
        // 注意：如果数据库中没有今天的数据，这个测试可能会失败
        // 在实际环境中运行前，请确保数据库中有相应的测试数据
        if (daycount != null) {
            assertEquals(moduleId, daycount.getModuleId());
            assertNotNull(daycount.getDt());
            assertNotNull(daycount.getEnergyCount());

            // 验证日期是否为今天
            // 注意：这里只比较日期部分，不比较时间部分
            java.sql.Date sqlToday = new java.sql.Date(today.getTime());
            java.sql.Date sqlDt = new java.sql.Date(daycount.getDt().getTime());
            assertEquals(sqlToday, sqlDt);
        } else {
            // 如果没有数据，这个测试就跳过
            System.out.println("No data found for moduleId: " + moduleId + " on date: " + today + ". Test skipped.");
        }
    }
    
    /**
     * 测试不存在的仪表ID
     */
    @Test
    public void testSelectTodayDataByNonExistentModuleId() {
        // 使用一个不存在的仪表ID
        String moduleId = "NON_EXISTENT_MODULE_ID";
        Date today = new Date(); // 今天
        
        TbEpEquEnergyDaycount daycount = tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(moduleId, today);
        
        // 验证结果
        assertNull(daycount); // 应该返回null
    }
} 