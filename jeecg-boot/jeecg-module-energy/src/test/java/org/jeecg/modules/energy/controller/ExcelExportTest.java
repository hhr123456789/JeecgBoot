package org.jeecg.modules.energy.controller;

import org.jeecg.modules.energy.vo.monitor.RealTimeDataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * Excel导出功能测试
 */
@SpringBootTest
public class ExcelExportTest {

    /**
     * 测试导出请求参数构建
     */
    @Test
    public void testExportRequestBuilder() {
        RealTimeDataRequest request = new RealTimeDataRequest();
        request.setModuleIds(Arrays.asList("yj0001_1202", "yj0001_12"));
        request.setParameters(Arrays.asList(1, 4, 7)); // A相电流, A相电压, 总有功功率
        request.setStartTime("2025-07-23 00:00:00");
        request.setEndTime("2025-07-23 15:55:00");
        request.setInterval(1); // 15分钟
        request.setDisplayMode(1); // 统一显示（导出时不影响）
        request.setFileName("中电电气实时数据导出");

        System.out.println("导出请求参数：" + request);
        
        // 验证参数
        assert request.getModuleIds() != null && !request.getModuleIds().isEmpty();
        assert request.getParameters() != null && !request.getParameters().isEmpty();
        assert request.getStartTime() != null;
        assert request.getEndTime() != null;
        assert request.getInterval() != null;
        assert request.getDisplayMode() != null;
        assert request.getFileName() != null;
        
        System.out.println("✅ 导出请求参数验证通过");
    }
}
