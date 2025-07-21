package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.monitor.SeparatedDisplayResult;
import org.jeecg.modules.energy.vo.monitor.UnifiedDisplayResult;

import java.util.List;
import java.util.Map;

/**
 * @Description: 数据格式化服务接口
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
public interface IDataFormatService {

    /**
     * 格式化统一显示模式数据
     * @param influxResults InfluxDB查询结果
     * @param moduleNameMap 仪表名称映射
     * @param parameters 参数列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 查询间隔
     * @return 统一显示结果
     */
    UnifiedDisplayResult formatUnifiedDisplay(
            List<Map<String, Object>> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval);

    /**
     * 格式化分开显示模式数据
     * @param influxResults InfluxDB查询结果
     * @param moduleNameMap 仪表名称映射
     * @param parameters 参数列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 查询间隔
     * @return 分开显示结果
     */
    SeparatedDisplayResult formatSeparatedDisplay(
            List<Map<String, Object>> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval);
}
