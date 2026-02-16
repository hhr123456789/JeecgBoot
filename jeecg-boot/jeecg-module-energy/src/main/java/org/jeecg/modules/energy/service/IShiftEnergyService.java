package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.shiftenergy.*;

import java.util.List;

/**
 * @Description: 班次用能统计服务接口
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
public interface IShiftEnergyService {

    /**
     * 获取班次能源统计数据
     */
    ShiftEnergyStatisticsVO getStatistics(ShiftEnergyQueryRequest request);

    /**
     * 获取班次能源趋势数据
     */
    ShiftEnergyTrendVO getTrendData(ShiftEnergyQueryRequest request);

    /**
     * 获取班次能源占比数据
     */
    List<ShiftEnergyPieVO> getPieData(ShiftEnergyQueryRequest request);

    /**
     * 获取班次能源表格数据
     */
    List<ShiftEnergyTableVO> getTableData(ShiftEnergyQueryRequest request);
}
