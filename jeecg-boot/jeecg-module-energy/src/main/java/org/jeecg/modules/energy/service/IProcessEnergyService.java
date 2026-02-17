package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.processenergy.*;

import java.util.List;

/**
 * 工序能耗分析服务接口
 */
public interface IProcessEnergyService {

    /**
     * 获取生产线树形数据
     */
    List<ProductionLineTreeVO> getProductionLineTree();

    /**
     * 获取统计数据
     */
    ProcessEnergyStatisticsVO getStatistics(ProcessEnergyQueryRequest request);

    /**
     * 获取饼图数据
     */
    List<ProcessEnergyPieVO> getPieData(ProcessEnergyQueryRequest request);

    /**
     * 获取趋势数据
     */
    ProcessEnergyTrendVO getTrendData(ProcessEnergyQueryRequest request);

    /**
     * 获取表格数据
     */
    List<ProcessEnergyTableVO> getTableData(ProcessEnergyQueryRequest request);
}
