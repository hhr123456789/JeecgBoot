package org.jeecg.modules.energy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 合理用能 - 统计查询Mapper
 */
@Mapper
public interface ReasonableAnalysisMapper {

    /**
     * 汇总：尖/峰/平/谷及总能耗
     */
    Map<String, Object> selectSummary(@Param("table") String table,
                                      @Param("moduleIds") List<String> moduleIds,
                                      @Param("startDt") String startDt,
                                      @Param("endDt") String endDt);

    /**
     * 总能耗趋势
     */
    List<Map<String, Object>> selectTotalTrend(@Param("table") String table,
                                               @Param("labelExpr") String labelExpr,
                                               @Param("moduleIds") List<String> moduleIds,
                                               @Param("startDt") String startDt,
                                               @Param("endDt") String endDt);

    /**
     * 尖峰平谷趋势（一次查出四列）
     */
    List<Map<String, Object>> selectTouTrend(@Param("table") String table,
                                             @Param("labelExpr") String labelExpr,
                                             @Param("moduleIds") List<String> moduleIds,
                                             @Param("startDt") String startDt,
                                             @Param("endDt") String endDt);
}

