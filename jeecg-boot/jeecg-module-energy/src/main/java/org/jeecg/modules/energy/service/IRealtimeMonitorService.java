package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.realtime.*;

import java.util.List;

/**
 * @Description: 实时数据监控Service接口
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
public interface IRealtimeMonitorService {
    
    /**
     * 根据维度获取仪表列表
     * @param dimensionCode 维度编码
     * @param energyType 能源类型
     * @param includeChildren 是否包含子维度
     * @return 仪表列表
     */
    List<ModuleInfoVO> getModulesByDimension(String dimensionCode, Integer energyType, Boolean includeChildren);
    
    /**
     * 获取参数配置
     * @param energyType 能源类型
     * @return 参数配置列表
     */
    List<ParameterConfigVO> getParameterConfig(Integer energyType);
    
    /**
     * 查询时序数据
     * @param query 查询参数
     * @return 时序数据结果
     */
    TimeSeriesResultVO getTimeSeriesData(TimeSeriesQueryVO query);
    
    /**
     * 获取实时状态
     * @param moduleIds 仪表ID列表
     * @param parameters 参数编码列表
     * @return 实时状态列表
     */
    List<ModuleStatusVO> getCurrentStatus(List<String> moduleIds, List<Integer> parameters);

    /**
     * 查询负荷时序数据
     * @param query 负荷查询参数
     * @return 负荷时序数据结果
     */
    LoadTimeSeriesResultVO getLoadTimeSeriesData(LoadTimeSeriesQueryVO query);

    /**
     * 获取实时负荷状态
     * @param moduleIds 仪表ID列表
     * @return 实时负荷状态列表
     */
    List<ModuleLoadStatusVO> getCurrentLoadStatus(List<String> moduleIds);

    /**
     * 获取负荷数据表格
     * @param query 负荷表格查询参数
     * @return 负荷表格结果
     */
    LoadTableResultVO getLoadTableData(LoadTableQueryVO query);
    
    /**
     * 导出负荷数据
     * @param exportVO 导出参数
     * @return Excel字节数组
     */
    byte[] exportLoadData(LoadDataExportVO exportVO) throws Exception;
}
