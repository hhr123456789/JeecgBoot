package org.jeecg.modules.energy.job;

import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * InfluxDBSyncJob单元测试
 */
public class InfluxDBSyncJobTest {

    @InjectMocks
    private InfluxDBSyncJob influxDBSyncJob;

    @Mock
    private TbModuleMapper moduleMapper;

    @Mock
    private TbEquEleDataMapper equEleDataMapper;

    @Mock
    private TbEquEnergyDataMapper equEnergyDataMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试电力数据同步 - 更新操作
     */
    @Test
    public void testSyncElectricDataUpdate() {
        // 准备测试数据
        TbModule module = new TbModule();
        module.setModuleId("test_module_001");
        module.setEnergyType(1); // 电力

        // 模拟现有数据
        TbEquEleData existingData = new TbEquEleData();
        existingData.setId(1); // 使用Integer类型的ID
        existingData.setModuleId("test_module_001");
        existingData.setEquElectricDT(new Date());

        // 模拟点位数据
        Map<String, Object> pointValues = new HashMap<>();
        pointValues.put("UA", 220.5);
        pointValues.put("UB", 221.0);
        pointValues.put("UC", 219.8);
        pointValues.put("IA", 10.2);
        pointValues.put("IB", 10.5);
        pointValues.put("IC", 10.1);
        pointValues.put("KWH", 1500.25);

        // 设置Mock行为
        when(equEleDataMapper.selectLatestDataByModuleId("test_module_001")).thenReturn(existingData);
        when(equEleDataMapper.updateById(any(TbEquEleData.class))).thenReturn(1);

        // 执行测试 - 使用反射调用私有方法
        try {
            java.lang.reflect.Method method = InfluxDBSyncJob.class.getDeclaredMethod("syncElectricData", TbModule.class, Map.class);
            method.setAccessible(true);
            method.invoke(influxDBSyncJob, module, pointValues);

            // 验证调用
            verify(equEleDataMapper, times(1)).selectLatestDataByModuleId("test_module_001");
            verify(equEleDataMapper, times(1)).updateById(any(TbEquEleData.class));
            verify(equEleDataMapper, never()).insert(any(TbEquEleData.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试电力数据同步 - 插入操作
     */
    @Test
    public void testSyncElectricDataInsert() {
        // 准备测试数据
        TbModule module = new TbModule();
        module.setModuleId("test_module_002");
        module.setEnergyType(1); // 电力

        // 模拟点位数据
        Map<String, Object> pointValues = new HashMap<>();
        pointValues.put("UA", 220.5);
        pointValues.put("UB", 221.0);
        pointValues.put("UC", 219.8);
        pointValues.put("KWH", 1500.25);

        // 设置Mock行为 - 没有现有数据
        when(equEleDataMapper.selectLatestDataByModuleId("test_module_002")).thenReturn(null);
        when(equEleDataMapper.insert(any(TbEquEleData.class))).thenReturn(1);

        // 执行测试
        try {
            java.lang.reflect.Method method = InfluxDBSyncJob.class.getDeclaredMethod("syncElectricData", TbModule.class, Map.class);
            method.setAccessible(true);
            method.invoke(influxDBSyncJob, module, pointValues);

            // 验证调用
            verify(equEleDataMapper, times(1)).selectLatestDataByModuleId("test_module_002");
            verify(equEleDataMapper, times(1)).insert(any(TbEquEleData.class));
            verify(equEleDataMapper, never()).updateById(any(TbEquEleData.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试其他能源数据同步 - 更新操作
     */
    @Test
    public void testSyncOtherEnergyDataUpdate() {
        // 准备测试数据
        TbModule module = new TbModule();
        module.setModuleId("test_module_003");
        module.setEnergyType(2); // 天然气

        // 模拟现有数据
        TbEquEnergyData existingData = new TbEquEnergyData();
        existingData.setId(1); // 使用Integer类型的ID
        existingData.setModuleId("test_module_003");
        existingData.setEquEnergyDt(new Date());

        // 模拟点位数据
        Map<String, Object> pointValues = new HashMap<>();
        pointValues.put("TEMPERATURE", 25.5);
        pointValues.put("PRESSURE", 1.2);
        pointValues.put("ACCUMULATEVALUE", 2500.75);

        // 设置Mock行为
        when(equEnergyDataMapper.selectLatestDataByModuleId("test_module_003")).thenReturn(existingData);
        when(equEnergyDataMapper.updateById(any(TbEquEnergyData.class))).thenReturn(1);

        // 执行测试
        try {
            java.lang.reflect.Method method = InfluxDBSyncJob.class.getDeclaredMethod("syncOtherEnergyData", TbModule.class, Map.class);
            method.setAccessible(true);
            method.invoke(influxDBSyncJob, module, pointValues);

            // 验证调用
            verify(equEnergyDataMapper, times(1)).selectLatestDataByModuleId("test_module_003");
            verify(equEnergyDataMapper, times(1)).updateById(any(TbEquEnergyData.class));
            verify(equEnergyDataMapper, never()).insert(any(TbEquEnergyData.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试其他能源数据同步 - 插入操作
     */
    @Test
    public void testSyncOtherEnergyDataInsert() {
        // 准备测试数据
        TbModule module = new TbModule();
        module.setModuleId("test_module_004");
        module.setEnergyType(3); // 压缩空气

        // 模拟点位数据
        Map<String, Object> pointValues = new HashMap<>();
        pointValues.put("TEMPERATURE", 25.5);
        pointValues.put("PRESSURE", 1.2);
        pointValues.put("ACCUMULATEVALUE", 2500.75);

        // 设置Mock行为 - 没有现有数据
        when(equEnergyDataMapper.selectLatestDataByModuleId("test_module_004")).thenReturn(null);
        when(equEnergyDataMapper.insert(any(TbEquEnergyData.class))).thenReturn(1);

        // 执行测试
        try {
            java.lang.reflect.Method method = InfluxDBSyncJob.class.getDeclaredMethod("syncOtherEnergyData", TbModule.class, Map.class);
            method.setAccessible(true);
            method.invoke(influxDBSyncJob, module, pointValues);

            // 验证调用
            verify(equEnergyDataMapper, times(1)).selectLatestDataByModuleId("test_module_004");
            verify(equEnergyDataMapper, times(1)).insert(any(TbEquEnergyData.class));
            verify(equEnergyDataMapper, never()).updateById(any(TbEquEnergyData.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
