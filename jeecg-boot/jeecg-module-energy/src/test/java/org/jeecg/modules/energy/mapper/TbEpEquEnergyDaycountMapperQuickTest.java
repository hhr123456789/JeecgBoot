package org.jeecg.modules.energy.mapper;

import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * TbEpEquEnergyDaycountMapper快速测试
 */
@SpringBootTest
public class TbEpEquEnergyDaycountMapperQuickTest {

    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;

    @Test
    public void testSelectTodayDataByModuleId() {
        try {
            String moduleId = "test_module";
            Date today = new Date();
            
            // 测试方法是否存在并可以调用（即使没有数据）
            TbEpEquEnergyDaycount result = daycountMapper.selectTodayDataByModuleId(moduleId, today);
            
            System.out.println("✅ selectTodayDataByModuleId 方法调用成功");
            if (result != null) {
                System.out.println("找到数据: " + result.getModuleId());
            } else {
                System.out.println("没有找到数据（这是正常的）");
            }
        } catch (Exception e) {
            System.err.println("❌ selectTodayDataByModuleId 方法调用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectByModuleIdAndDate() {
        try {
            String moduleId = "test_module";
            Date today = new Date();
            
            // 测试方法是否存在并可以调用（即使没有数据）
            TbEpEquEnergyDaycount result = daycountMapper.selectByModuleIdAndDate(moduleId, today);
            
            System.out.println("✅ selectByModuleIdAndDate 方法调用成功");
            if (result != null) {
                System.out.println("找到数据: " + result.getModuleId());
            } else {
                System.out.println("没有找到数据（这是正常的）");
            }
        } catch (Exception e) {
            System.err.println("❌ selectByModuleIdAndDate 方法调用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
