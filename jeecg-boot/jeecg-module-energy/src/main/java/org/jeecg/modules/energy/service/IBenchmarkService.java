package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.entity.benchmark.*;
import java.util.List;
import java.util.Map;

/**
 * @Description: 能效对标服务接口
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
public interface IBenchmarkService {

    /**
     * 获取对标配置列表
     */
    List<BenchmarkConfig> getConfigList(Integer benchmarkType, String energyType);

    /**
     * 获取对标对象列表
     */
    List<BenchmarkTarget> getTargetList(String configId);

    /**
     * 获取对标统计数据
     */
    Map<String, Object> getStatistics(String targetCode, String timeUnit, String startTime, String endTime, String energyType);

    /**
     * 导出对标数据
     */
    List<Map<String, Object>> exportData(String targetCode, String timeUnit, String startTime, String endTime, String energyType);
}
