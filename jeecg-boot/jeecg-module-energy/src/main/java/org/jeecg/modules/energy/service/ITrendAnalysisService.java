package org.jeecg.modules.energy.service;

import javax.servlet.http.HttpServletResponse;

import org.jeecg.modules.energy.vo.trend.TrendRequest;

public interface ITrendAnalysisService {
    /**
     * 趋势查询：根据显示模式返回统一或分开结构
     */
    Object getTrend(TrendRequest request);

    /**
     * 导出趋势数据（Excel）
     */
    void exportTrend(TrendRequest request, HttpServletResponse response);
}

