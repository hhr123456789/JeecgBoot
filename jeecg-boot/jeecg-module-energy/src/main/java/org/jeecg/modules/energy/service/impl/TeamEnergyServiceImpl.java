package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.TeamDimensionRelation;
import org.jeecg.modules.energy.entity.TeamInfo;
import org.jeecg.modules.energy.mapper.TeamDimensionRelationMapper;
import org.jeecg.modules.energy.mapper.TeamInfoMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyMonthcountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyYearcountMapper;
import org.jeecg.modules.energy.service.ITeamEnergyService;
import org.jeecg.modules.energy.vo.teamenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 班组能源统计服务实现
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Service
@Slf4j
public class TeamEnergyServiceImpl implements ITeamEnergyService {

    @Autowired
    private TeamInfoMapper teamInfoMapper;

    @Autowired
    private TeamDimensionRelationMapper teamDimensionRelationMapper;

    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;

    @Autowired
    private TbEpEquEnergyMonthcountMapper monthcountMapper;

    @Autowired
    private TbEpEquEnergyYearcountMapper yearcountMapper;

    @Override
    public List<TeamInfoVO> getTeamListByDimension(String dimensionCode, Integer dimensionType) {
        log.info("获取班组列表 - dimensionCode: {}, dimensionType: {}", dimensionCode, dimensionType);

        // 查询该维度下的班组关联
        LambdaQueryWrapper<TeamDimensionRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamDimensionRelation::getDimensionCode, dimensionCode)
               .eq(TeamDimensionRelation::getDimensionType, dimensionType)
               .eq(TeamDimensionRelation::getStatus, 1);

        List<TeamDimensionRelation> relations = teamDimensionRelationMapper.selectList(wrapper);

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取班组编码列表
        List<String> teamCodes = relations.stream()
                .map(TeamDimensionRelation::getTeamCode)
                .collect(Collectors.toList());

        // 查询班组信息
        LambdaQueryWrapper<TeamInfo> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.in(TeamInfo::getTeamCode, teamCodes)
                   .eq(TeamInfo::getStatus, 1)
                   .orderByAsc(TeamInfo::getSortOrder);

        List<TeamInfo> teamInfos = teamInfoMapper.selectList(teamWrapper);

        // 转换为VO
        return teamInfos.stream().map(team -> {
            TeamInfoVO vo = new TeamInfoVO();
            vo.setCode(team.getTeamCode());
            vo.setName(team.getTeamName());
            vo.setShiftType(team.getShiftType());
            vo.setOrgCode(team.getOrgCode());
            vo.setOrgName(team.getOrgName());
            vo.setStatus(team.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TeamEnergyStatisticsVO getStatistics(TeamEnergyQueryRequest request) {
        log.info("获取统计数据 - request: {}", request);

        TeamEnergyStatisticsVO vo = new TeamEnergyStatisticsVO();

        // TODO: 实现实际的统计逻辑，从数据库查询能耗数据
        // 这里先返回模拟数据
        vo.setTotalConsumption("162.00");
        vo.setTotalCost("129.60");
        vo.setCarbonEmission("161.51");
        vo.setStandardCoal("19.92");
        vo.setEnergyUnit(getEnergyUnit(request.getEnergyType()));

        return vo;
    }

    @Override
    public TeamEnergyTrendVO getTrendData(TeamEnergyQueryRequest request) {
        log.info("获取趋势数据 - request: {}", request);

        TeamEnergyTrendVO vo = new TeamEnergyTrendVO();

        // 根据时间维度生成X轴数据
        List<String> xAxisData = generateXAxisData(request.getTimeUnit(), request.getQueryDate());
        vo.setXAxisData(xAxisData);

        // TODO: 实现实际的趋势数据查询逻辑
        // 这里先返回模拟数据
        List<TeamEnergyTrendVO.SeriesData> seriesDataList = new ArrayList<>();

        if ("all".equals(request.getTeamCode())) {
            // 显示所有班组
            seriesDataList.add(createSeriesData("A-1班", "bar", generateMockData(xAxisData.size()), "#4B7BE5"));
            seriesDataList.add(createSeriesData("A-2班", "bar", generateMockData(xAxisData.size()), "#23C343"));
            seriesDataList.add(createSeriesData("B-1班", "bar", generateMockData(xAxisData.size()), "#FF9F40"));
        } else {
            // 显示单个班组
            seriesDataList.add(createSeriesData(request.getTeamCode(), "bar", generateMockData(xAxisData.size()), "#4B7BE5"));
        }

        vo.setSeriesData(seriesDataList);

        return vo;
    }

    @Override
    public List<TeamEnergyRankingVO> getRankingData(TeamEnergyQueryRequest request) {
        log.info("获取排名数据 - request: {}", request);

        // TODO: 实现实际的排名数据查询逻辑
        // 这里先返回模拟数据
        List<TeamEnergyRankingVO> list = new ArrayList<>();

        TeamEnergyRankingVO rank1 = new TeamEnergyRankingVO();
        rank1.setName("B-1班");
        rank1.setValue(42.53);
        rank1.setUnit(getEnergyUnit(request.getEnergyType()));
        rank1.setRank(1);
        list.add(rank1);

        TeamEnergyRankingVO rank2 = new TeamEnergyRankingVO();
        rank2.setName("A-1班");
        rank2.setValue(41.65);
        rank2.setUnit(getEnergyUnit(request.getEnergyType()));
        rank2.setRank(2);
        list.add(rank2);

        TeamEnergyRankingVO rank3 = new TeamEnergyRankingVO();
        rank3.setName("A-2班");
        rank3.setValue(40.15);
        rank3.setUnit(getEnergyUnit(request.getEnergyType()));
        rank3.setRank(3);
        list.add(rank3);

        return list;
    }

    @Override
    public List<TeamEnergyTableVO> getTableData(TeamEnergyQueryRequest request) {
        log.info("获取表格数据 - request: {}", request);

        // TODO: 实现实际的表格数据查询逻辑
        // 这里先返回模拟数据
        List<TeamEnergyTableVO> list = new ArrayList<>();

        TeamEnergyTableVO row1 = new TeamEnergyTableVO();
        row1.setTeamName("A-1班");
        row1.setShiftType("早班");
        row1.setStatTime("2026-01-15");
        row1.setConsumption("84.00");
        row1.setCost("67.20");
        row1.setCarbon("83.75");
        row1.setCoal("10.33");
        row1.setPeak("20.00");
        row1.setFlat("40.00");
        row1.setValley("24.00");
        list.add(row1);

        TeamEnergyTableVO row2 = new TeamEnergyTableVO();
        row2.setTeamName("A-2班");
        row2.setShiftType("中班");
        row2.setStatTime("2026-01-15");
        row2.setConsumption("36.00");
        row2.setCost("28.80");
        row2.setCarbon("35.89");
        row2.setCoal("4.43");
        row2.setPeak("12.00");
        row2.setFlat("18.00");
        row2.setValley("6.00");
        list.add(row2);

        TeamEnergyTableVO row3 = new TeamEnergyTableVO();
        row3.setTeamName("B-1班");
        row3.setShiftType("晚班");
        row3.setStatTime("2026-01-15");
        row3.setConsumption("42.00");
        row3.setCost("33.60");
        row3.setCarbon("41.87");
        row3.setCoal("5.16");
        row3.setPeak("14.00");
        row3.setFlat("21.00");
        row3.setValley("7.00");
        list.add(row3);

        return list;
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据能源类型获取单位
     */
    private String getEnergyUnit(String energyType) {
        if (energyType == null) {
            return "kWh";
        }
        switch (energyType) {
            case "1":
                return "kWh";
            case "2":
            case "8":
            case "5":
                return "m³";
            case "all":
                return "tce";
            default:
                return "kWh";
        }
    }

    /**
     * 生成X轴数据
     */
    private List<String> generateXAxisData(String timeUnit, String queryDate) {
        List<String> xAxisData = new ArrayList<>();

        if ("day".equals(timeUnit)) {
            // 按小时统计
            for (int i = 0; i < 24; i++) {
                xAxisData.add(String.format("%02d:00", i));
            }
        } else if ("month".equals(timeUnit)) {
            // 按日统计
            for (int i = 1; i <= 30; i++) {
                xAxisData.add(i + "日");
            }
        } else if ("year".equals(timeUnit)) {
            // 按月统计
            for (int i = 1; i <= 12; i++) {
                xAxisData.add(i + "月");
            }
        }

        return xAxisData;
    }

    /**
     * 创建系列数据
     */
    private TeamEnergyTrendVO.SeriesData createSeriesData(String name, String type, List<Double> data, String color) {
        TeamEnergyTrendVO.SeriesData seriesData = new TeamEnergyTrendVO.SeriesData();
        seriesData.setName(name);
        seriesData.setType(type);
        seriesData.setData(data);
        seriesData.setColor(color);
        return seriesData;
    }

    /**
     * 生成模拟数据
     */
    private List<Double> generateMockData(int size) {
        List<Double> data = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            data.add(random.nextDouble() * 100);
        }
        return data;
    }
}
