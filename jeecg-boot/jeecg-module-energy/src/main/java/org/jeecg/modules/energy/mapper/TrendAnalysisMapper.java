package org.jeecg.modules.energy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TrendAnalysisMapper {

    /**
     * 趋势：按时间标签聚合能耗
     */
    List<Map<String,Object>> selectEnergyTrend(@Param("table") String table,
                                               @Param("labelExpr") String labelExpr,
                                               @Param("moduleIds") List<String> moduleIds,
                                               @Param("startDt") String startDt,
                                               @Param("endDt") String endDt);

    /**
     * 汇总能耗
     */
    Map<String,Object> selectEnergySummary(@Param("table") String table,
                                           @Param("moduleIds") List<String> moduleIds,
                                           @Param("startDt") String startDt,
                                           @Param("endDt") String endDt);

    /**
     * 查询能源系数
     */
    List<Map<String,Object>> selectEnergyRatio();
}

