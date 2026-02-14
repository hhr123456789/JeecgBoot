package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.teamenergy.*;

import java.util.List;

/**
 * @Description: 班组能源统计服务接口
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
public interface ITeamEnergyService {

    /**
     * 根据维度获取班组列表
     * @param dimensionCode 维度编码
     * @param dimensionType 维度类型
     * @return 班组列表
     */
    List<TeamInfoVO> getTeamListByDimension(String dimensionCode, Integer dimensionType);

    /**
     * 获取班组能源统计数据
     * @param request 查询请求参数
     * @return 统计数据
     */
    TeamEnergyStatisticsVO getStatistics(TeamEnergyQueryRequest request);

    /**
     * 获取班组能源趋势图数据
     * @param request 查询请求参数
     * @return 趋势图数据
     */
    TeamEnergyTrendVO getTrendData(TeamEnergyQueryRequest request);

    /**
     * 获取班组能源排名数据
     * @param request 查询请求参数
     * @return 排名数据
     */
    List<TeamEnergyRankingVO> getRankingData(TeamEnergyQueryRequest request);

    /**
     * 获取班组能源明细表数据
     * @param request 查询请求参数
     * @return 明细表数据
     */
    List<TeamEnergyTableVO> getTableData(TeamEnergyQueryRequest request);
}
