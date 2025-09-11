package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.consumption.ConsumptionRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * 设备能源统计服务接口
 */
public interface IConsumptionStatisticsService {

    /**
     * 获取设备能源统计数据
     * @param request 查询请求
     * @return 统计结果（根据displayMode返回不同类型）
     */
    Object getConsumptionStatistics(ConsumptionRequest request);

    /**
     * 导出设备能源统计数据到Excel
     * @param request 查询请求
     * @param response HTTP响应
     */
    void exportConsumptionStatistics(ConsumptionRequest request, HttpServletResponse response);
}