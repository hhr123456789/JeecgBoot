package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.energy.entity.*;
import org.jeecg.modules.energy.mapper.*;
import org.jeecg.modules.energy.service.ITeamEnergyService;
import org.jeecg.modules.energy.vo.teamenergy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
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

    @Autowired
    private TbEnergyRatioInfoMapper energyRatioInfoMapper;

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
        log.info("========================================");
        log.info("开始获取统计数据");
        log.info("请求参数: teamCode={}, orgCode={}, timeUnit={}, queryDate={}, energyType={}, dimensionType={}",
                request.getTeamCode(), request.getOrgCode(), request.getTimeUnit(),
                request.getQueryDate(), request.getEnergyType(), request.getDimensionType());

        // 1. 获取班组关联的仪表ID列表
        List<String> moduleIds = getModuleIdsByTeamCode(request.getTeamCode(), request.getOrgCode(), request.getDimensionType());
        if (moduleIds.isEmpty()) {
            log.warn("未找到关联的仪表ID, teamCode={}, orgCode={}, dimensionType={}", request.getTeamCode(), request.getOrgCode(), request.getDimensionType());
            log.info("========================================");
            return createEmptyStatistics(request.getEnergyType());
        }

        // 2. 解析日期范围
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            log.error("日期范围解析失败");
            log.info("========================================");
            return createEmptyStatistics(request.getEnergyType());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("解析后的日期范围: {} 到 {}", sdf.format(dateRange[0]), sdf.format(dateRange[1]));

        // 3. 查询能耗数据（根据时间维度选择不同统计表）
        BigDecimal totalConsumption = queryTotalEnergy(moduleIds, dateRange[0], dateRange[1], request.getTimeUnit());

        // 4. 获取能源系数，计算费用、碳排放、折标煤
        TbEnergyRatioInfo ratioInfo = getEnergyRatioInfo(request.getEnergyType());
        BigDecimal pricePerUnit = ratioInfo != null && ratioInfo.getPricePerUnit() != null ? ratioInfo.getPricePerUnit() : new BigDecimal("0.80");
        BigDecimal carbonFactor = ratioInfo != null && ratioInfo.getTpfxsValue() != null ? ratioInfo.getTpfxsValue() : new BigDecimal("0.997");
        BigDecimal coalFactor = ratioInfo != null && ratioInfo.getZbmxsValue() != null ? ratioInfo.getZbmxsValue() : new BigDecimal("0.1229");

        log.info("能源系数: pricePerUnit={}, carbonFactor={}, coalFactor={}", pricePerUnit, carbonFactor, coalFactor);

        BigDecimal totalCost = totalConsumption.multiply(pricePerUnit);
        BigDecimal carbonEmission = totalConsumption.multiply(carbonFactor);
        BigDecimal standardCoal = totalConsumption.multiply(coalFactor);

        log.info("计算结果: totalConsumption={}, totalCost={}, carbonEmission={}, standardCoal={}",
                totalConsumption, totalCost, carbonEmission, standardCoal);

        // 5. 构建返回对象
        TeamEnergyStatisticsVO vo = new TeamEnergyStatisticsVO();
        vo.setTotalConsumption(totalConsumption.setScale(2, RoundingMode.HALF_UP).toString());
        vo.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP).toString());
        vo.setCarbonEmission(carbonEmission.setScale(2, RoundingMode.HALF_UP).toString());
        vo.setStandardCoal(standardCoal.setScale(2, RoundingMode.HALF_UP).toString());
        vo.setEnergyUnit(getEnergyUnit(request.getEnergyType()));

        log.info("返回数据: totalConsumption={}, totalCost={}, carbonEmission={}, standardCoal={}, energyUnit={}",
                vo.getTotalConsumption(), vo.getTotalCost(), vo.getCarbonEmission(), vo.getStandardCoal(), vo.getEnergyUnit());
        log.info("统计数据查询完成");
        log.info("========================================");
        return vo;
    }

    @Override
    public TeamEnergyTrendVO getTrendData(TeamEnergyQueryRequest request) {
        log.info("获取趋势数据 - teamCode={}, orgCode={}, timeUnit={}, queryDate={}, dimensionType={}",
                request.getTeamCode(), request.getOrgCode(), request.getTimeUnit(), request.getQueryDate(), request.getDimensionType());

        TeamEnergyTrendVO vo = new TeamEnergyTrendVO();

        // 1. 解析日期范围并生成X轴数据
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            vo.setXAxisData(new ArrayList<>());
            vo.setSeriesData(new ArrayList<>());
            return vo;
        }
        List<String> xAxisData = generateXAxisData(request.getTimeUnit(), dateRange[0], dateRange[1]);
        vo.setXAxisData(xAxisData);

        // 2. 查询班组列表
        List<TeamInfo> teams = getTeamList(request.getTeamCode(), request.getOrgCode(), request.getDimensionType());

        // 3. 为每个班组查询趋势数据
        List<TeamEnergyTrendVO.SeriesData> seriesList = new ArrayList<>();
        for (TeamInfo team : teams) {
            List<String> moduleIds = getModuleIdsByTeamCode(team.getTeamCode(), request.getOrgCode(), request.getDimensionType());
            if (moduleIds.isEmpty()) {
                continue;
            }

            List<Double> dataValues = new ArrayList<>();
            for (String dateLabel : xAxisData) {
                BigDecimal value = queryEnergyByDateLabel(moduleIds, dateLabel, request.getTimeUnit(), request.getQueryDate());
                dataValues.add(value.setScale(2, RoundingMode.HALF_UP).doubleValue());
            }

            TeamEnergyTrendVO.SeriesData series = new TeamEnergyTrendVO.SeriesData();
            series.setName(team.getTeamName());
            series.setType("bar");
            series.setData(dataValues);
            series.setColor(getColorByIndex(seriesList.size()));
            seriesList.add(series);
        }

        vo.setSeriesData(seriesList);
        return vo;
    }

    @Override
    public List<TeamEnergyRankingVO> getRankingData(TeamEnergyQueryRequest request) {
        log.info("获取排名数据 - teamCode={}, orgCode={}, timeUnit={}, queryDate={}, dimensionType={}",
                request.getTeamCode(), request.getOrgCode(), request.getTimeUnit(), request.getQueryDate(), request.getDimensionType());

        // 1. 解析日期范围
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            return new ArrayList<>();
        }

        // 2. 查询所有班组（排名始终显示全部班组）
        List<TeamInfo> teams = getTeamList("all", request.getOrgCode(), request.getDimensionType());

        // 3. 为每个班组计算能耗
        List<TeamEnergyRankingVO> rankings = new ArrayList<>();
        for (TeamInfo team : teams) {
            List<String> moduleIds = getModuleIdsByTeamCode(team.getTeamCode(), request.getOrgCode(), request.getDimensionType());
            if (moduleIds.isEmpty()) {
                continue;
            }

            BigDecimal totalEnergy = queryTotalEnergy(moduleIds, dateRange[0], dateRange[1], request.getTimeUnit());

            TeamEnergyRankingVO vo = new TeamEnergyRankingVO();
            vo.setName(team.getTeamName());
            vo.setValue(totalEnergy.setScale(2, RoundingMode.HALF_UP).doubleValue());
            vo.setUnit(getEnergyUnit(request.getEnergyType()));
            rankings.add(vo);
        }

        // 4. 按能耗降序排序并设置排名
        rankings.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    @Override
    public List<TeamEnergyTableVO> getTableData(TeamEnergyQueryRequest request) {
        log.info("获取表格数据 - teamCode={}, orgCode={}, timeUnit={}, queryDate={}, dimensionType={}",
                request.getTeamCode(), request.getOrgCode(), request.getTimeUnit(), request.getQueryDate(), request.getDimensionType());

        List<TeamEnergyTableVO> tableData = new ArrayList<>();

        // 1. 解析日期范围
        Date[] dateRange = parseDateRange(request.getTimeUnit(), request.getQueryDate());
        if (dateRange == null) {
            return tableData;
        }

        // 2. 获取能源系数
        TbEnergyRatioInfo ratioInfo = getEnergyRatioInfo(request.getEnergyType());
        BigDecimal pricePerUnit = ratioInfo != null && ratioInfo.getPricePerUnit() != null ? ratioInfo.getPricePerUnit() : new BigDecimal("0.80");
        BigDecimal carbonFactor = ratioInfo != null && ratioInfo.getTpfxsValue() != null ? ratioInfo.getTpfxsValue() : new BigDecimal("0.997");
        BigDecimal coalFactor = ratioInfo != null && ratioInfo.getZbmxsValue() != null ? ratioInfo.getZbmxsValue() : new BigDecimal("0.1229");

        // 3. 查询班组列表
        List<TeamInfo> teams = getTeamList(request.getTeamCode(), request.getOrgCode(), request.getDimensionType());

        // 4. 为每个班组查询明细数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (TeamInfo team : teams) {
            log.info("========================================");
            log.info("查询班组表格数据: {}", team.getTeamName());
            List<String> moduleIds = getModuleIdsByTeamCode(team.getTeamCode(), request.getOrgCode(), request.getDimensionType());
            if (moduleIds.isEmpty()) {
                log.warn("班组 {} 没有关联的仪表ID", team.getTeamName());
                continue;
            }

            // 查询日统计数据
            String moduleIdsStr = moduleIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
            String sql = String.format("SELECT * FROM tb_ep_equ_energy_daycount WHERE module_id IN (%s) AND dt BETWEEN '%s' AND '%s'",
                    moduleIdsStr, sdf.format(dateRange[0]), sdf.format(dateRange[1]));
            log.info("执行SQL: {}", sql);

            LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyDaycount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyDaycount::getDt, dateRange[0], dateRange[1]);

            List<TbEpEquEnergyDaycount> dataList = daycountMapper.selectList(wrapper);
            log.info("查询结果: 找到 {} 条日统计记录", dataList.size());
            log.info("========================================");

            // 按日期分组汇总
            Map<String, List<TbEpEquEnergyDaycount>> groupedData = dataList.stream()
                    .collect(Collectors.groupingBy(d -> new SimpleDateFormat("yyyy-MM-dd").format(d.getDt())));

            for (Map.Entry<String, List<TbEpEquEnergyDaycount>> entry : groupedData.entrySet()) {
                List<TbEpEquEnergyDaycount> dayData = entry.getValue();
                BigDecimal consumption = sumBigDecimal(dayData, TbEpEquEnergyDaycount::getEnergyCount);

                TeamEnergyTableVO vo = new TeamEnergyTableVO();
                vo.setTeamName(team.getTeamName());
                vo.setShiftType(team.getShiftType() != null ? team.getShiftType() : "-");
                vo.setStatTime(entry.getKey());
                vo.setConsumption(consumption.setScale(2, RoundingMode.HALF_UP).toString());
                vo.setCost(consumption.multiply(pricePerUnit).setScale(2, RoundingMode.HALF_UP).toString());
                vo.setCarbon(consumption.multiply(carbonFactor).setScale(2, RoundingMode.HALF_UP).toString());
                vo.setCoal(consumption.multiply(coalFactor).setScale(2, RoundingMode.HALF_UP).toString());
                vo.setPeak(sumBigDecimal(dayData, TbEpEquEnergyDaycount::getPeakCount).setScale(2, RoundingMode.HALF_UP).toString());
                vo.setFlat(sumBigDecimal(dayData, TbEpEquEnergyDaycount::getLevelCount).setScale(2, RoundingMode.HALF_UP).toString());
                vo.setValley(sumBigDecimal(dayData, TbEpEquEnergyDaycount::getValleyCount).setScale(2, RoundingMode.HALF_UP).toString());
                tableData.add(vo);
            }
        }

        // 5. 按时间排序
        tableData.sort(Comparator.comparing(TeamEnergyTableVO::getStatTime));
        return tableData;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取班组关联的仪表ID列表
     */
    private List<String> getModuleIdsByTeamCode(String teamCode, String orgCode, Integer dimensionType) {
        log.info("========================================");
        log.info("查询仪表ID列表");
        log.info("输入参数: teamCode={}, orgCode={}, dimensionType={}", teamCode, orgCode, dimensionType);

        LambdaQueryWrapper<TeamDimensionRelation> wrapper = new LambdaQueryWrapper<>();
        if (!"all".equals(teamCode)) {
            wrapper.eq(TeamDimensionRelation::getTeamCode, teamCode);
        }
        if (StringUtils.isNotBlank(orgCode)) {
            wrapper.eq(TeamDimensionRelation::getDimensionCode, orgCode);
        }
        if (dimensionType != null) {
            wrapper.eq(TeamDimensionRelation::getDimensionType, dimensionType);
        }
        wrapper.eq(TeamDimensionRelation::getStatus, 1);

        // 构建SQL日志
        StringBuilder sqlLog = new StringBuilder("SELECT * FROM tb_team_dimension_relation WHERE ");
        if (!"all".equals(teamCode)) {
            sqlLog.append("team_code = '").append(teamCode).append("' AND ");
        }
        if (StringUtils.isNotBlank(orgCode)) {
            sqlLog.append("dimension_code = '").append(orgCode).append("' AND ");
        }
        if (dimensionType != null) {
            sqlLog.append("dimension_type = ").append(dimensionType).append(" AND ");
        }
        sqlLog.append("status = 1");
        log.info("执行SQL: {}", sqlLog);

        List<TeamDimensionRelation> relations = teamDimensionRelationMapper.selectList(wrapper);
        log.info("查询结果: 找到 {} 条班组维度关联记录", relations.size());

        if (!relations.isEmpty()) {
            log.info("关联记录详情:");
            relations.forEach(r -> log.info("  teamCode={}, dimensionCode={}, moduleIds={}",
                    r.getTeamCode(), r.getDimensionCode(), r.getModuleIds()));
        }

        List<String> moduleIds = relations.stream()
                .map(TeamDimensionRelation::getModuleIds)
                .filter(StringUtils::isNotBlank)
                .flatMap(ids -> Arrays.stream(ids.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        log.info("解析后的仪表ID列表 (共{}个): {}", moduleIds.size(), moduleIds);
        log.info("========================================");
        return moduleIds;
    }

    /**
     * 获取班组列表
     */
    private List<TeamInfo> getTeamList(String teamCode, String orgCode, Integer dimensionType) {
        if (!"all".equals(teamCode)) {
            LambdaQueryWrapper<TeamInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeamInfo::getTeamCode, teamCode)
                   .eq(TeamInfo::getStatus, 1);
            return teamInfoMapper.selectList(wrapper);
        }

        // teamCode=all: 通过维度关联表查找该orgCode下的所有班组
        LambdaQueryWrapper<TeamDimensionRelation> relWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(orgCode)) {
            relWrapper.eq(TeamDimensionRelation::getDimensionCode, orgCode);
        }
        if (dimensionType != null) {
            relWrapper.eq(TeamDimensionRelation::getDimensionType, dimensionType);
        }
        relWrapper.eq(TeamDimensionRelation::getStatus, 1);
        List<TeamDimensionRelation> relations = teamDimensionRelationMapper.selectList(relWrapper);

        List<String> teamCodes = relations.stream()
                .map(TeamDimensionRelation::getTeamCode)
                .distinct()
                .collect(Collectors.toList());

        if (teamCodes.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<TeamInfo> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.in(TeamInfo::getTeamCode, teamCodes)
                   .eq(TeamInfo::getStatus, 1)
                   .orderByAsc(TeamInfo::getSortOrder);
        return teamInfoMapper.selectList(teamWrapper);
    }

    /**
     * 解析日期范围: 根据 timeUnit 和 queryDate 返回 [startDate, endDate]
     */
    private Date[] parseDateRange(String timeUnit, String queryDate) {
        try {
            Calendar cal = Calendar.getInstance();
            if ("day".equals(timeUnit)) {
                // queryDate = "yyyy-MM-dd", 范围就是这一天
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            } else if ("month".equals(timeUnit)) {
                // queryDate = "yyyy-MM", 范围是整个月
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            } else if ("year".equals(timeUnit)) {
                // queryDate = "yyyy", 范围是整年
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                Date date = sdf.parse(queryDate);
                cal.setTime(date);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 31);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return new Date[]{start, cal.getTime()};
            }
        } catch (ParseException e) {
            log.error("日期解析失败: timeUnit={}, queryDate={}", timeUnit, queryDate, e);
        }
        return null;
    }

    /**
     * 生成X轴日期数据
     */
    private List<String> generateXAxisData(String timeUnit, Date startDate, Date endDate) {
        List<String> xAxisData = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        if ("day".equals(timeUnit)) {
            // 按天查询时，X轴显示24小时
            for (int i = 0; i < 24; i++) {
                xAxisData.add(String.format("%02d:00", i));
            }
        } else if ("month".equals(timeUnit)) {
            // 按月查询时，X轴显示每一天
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            while (!cal.getTime().after(endDate)) {
                xAxisData.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if ("year".equals(timeUnit)) {
            // 按年查询时，X轴显示12个月
            for (int i = 1; i <= 12; i++) {
                xAxisData.add(i + "月");
            }
        }
        return xAxisData;
    }

    /**
     * 查询指定仪表在时间范围内的总能耗
     */
    private BigDecimal queryTotalEnergy(List<String> moduleIds, Date startDate, Date endDate, String timeUnit) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("========================================");
        log.info("查询总能耗");
        log.info("仪表ID列表: {}", moduleIds);
        log.info("时间范围: {} 到 {}", sdf.format(startDate), sdf.format(endDate));
        log.info("时间维度: {}", timeUnit);

        if (moduleIds.isEmpty()) {
            log.warn("仪表ID列表为空，返回0");
            log.info("========================================");
            return BigDecimal.ZERO;
        }

        String moduleIdsStr = moduleIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));

        if ("day".equals(timeUnit)) {
            log.info("使用日统计表: tb_ep_equ_energy_daycount");
            String sql = String.format("SELECT * FROM tb_ep_equ_energy_daycount WHERE module_id IN (%s) AND dt BETWEEN '%s' AND '%s'",
                    moduleIdsStr, sdf.format(startDate), sdf.format(endDate));
            log.info("执行SQL: {}", sql);

            LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyDaycount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyDaycount::getDt, startDate, endDate);

            List<TbEpEquEnergyDaycount> list = daycountMapper.selectList(wrapper);
            log.info("查询结果: 找到 {} 条日统计记录", list.size());

            if (!list.isEmpty()) {
                log.info("记录详情:");
                SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
                list.forEach(d -> log.info("  moduleId={}, dt={}, energyCount={}",
                        d.getModuleId(), dateSdf.format(d.getDt()), d.getEnergyCount()));
            }

            BigDecimal total = sumBigDecimal(list, TbEpEquEnergyDaycount::getEnergyCount);
            log.info("总能耗: {}", total);
            log.info("========================================");
            return total;
        } else if ("month".equals(timeUnit)) {
            log.info("使用月统计表: tb_ep_equ_energy_monthcount");
            String sql = String.format("SELECT * FROM tb_ep_equ_energy_monthcount WHERE module_id IN (%s) AND dt BETWEEN '%s' AND '%s'",
                    moduleIdsStr, sdf.format(startDate), sdf.format(endDate));
            log.info("执行SQL: {}", sql);

            LambdaQueryWrapper<TbEpEquEnergyMonthcount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyMonthcount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyMonthcount::getDt, startDate, endDate);

            List<TbEpEquEnergyMonthcount> list = monthcountMapper.selectList(wrapper);
            log.info("查询结果: 找到 {} 条月统计记录", list.size());

            if (!list.isEmpty()) {
                log.info("记录详情:");
                SimpleDateFormat monthSdf = new SimpleDateFormat("yyyy-MM");
                list.forEach(d -> log.info("  moduleId={}, dt={}, energyCount={}",
                        d.getModuleId(), monthSdf.format(d.getDt()), d.getEnergyCount()));
            }

            BigDecimal total = list.stream()
                    .map(TbEpEquEnergyMonthcount::getEnergyCount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("总能耗: {}", total);
            log.info("========================================");
            return total;
        } else if ("year".equals(timeUnit)) {
            log.info("使用年统计表: tb_ep_equ_energy_yearcount");
            String sql = String.format("SELECT * FROM tb_ep_equ_energy_yearcount WHERE module_id IN (%s) AND dt BETWEEN '%s' AND '%s'",
                    moduleIdsStr, sdf.format(startDate), sdf.format(endDate));
            log.info("执行SQL: {}", sql);

            LambdaQueryWrapper<TbEpEquEnergyYearcount> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TbEpEquEnergyYearcount::getModuleId, moduleIds);
            wrapper.between(TbEpEquEnergyYearcount::getDt, startDate, endDate);

            List<TbEpEquEnergyYearcount> list = yearcountMapper.selectList(wrapper);
            log.info("查询结果: 找到 {} 条年统计记录", list.size());

            if (!list.isEmpty()) {
                log.info("记录详情:");
                SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
                list.forEach(d -> log.info("  moduleId={}, dt={}, energyCount={}",
                        d.getModuleId(), yearSdf.format(d.getDt()), d.getEnergyCount()));
            }

            BigDecimal total = list.stream()
                    .map(TbEpEquEnergyYearcount::getEnergyCount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("总能耗: {}", total);
            log.info("========================================");
            return total;
        }
        log.warn("未知的时间维度: {}", timeUnit);
        log.info("========================================");
        return BigDecimal.ZERO;
    }

    /**
     * 根据X轴标签查询对应日期的能耗（用于趋势图）
     */
    private BigDecimal queryEnergyByDateLabel(List<String> moduleIds, String dateLabel, String timeUnit, String queryDate) {
        try {
            if ("day".equals(timeUnit)) {
                // dateLabel = "08:00", 日维度暂不支持小时级别统计，返回日统计均分
                // 由于日统计表是按天汇总的，小时级别数据需要从原始数据获取
                // 这里简化处理：查询当天总量后均分到24小时
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(queryDate);
                BigDecimal dayTotal = queryTotalEnergy(moduleIds, date, date, "day");
                if (dayTotal.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO;
                }
                return dayTotal.divide(new BigDecimal(24), 2, RoundingMode.HALF_UP);
            } else if ("month".equals(timeUnit)) {
                // dateLabel = "01-15", queryDate = "2026-01"
                String fullDate = queryDate + "-" + dateLabel.split("-")[1];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(fullDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return queryTotalEnergy(moduleIds, start, cal.getTime(), "day");
            } else if ("year".equals(timeUnit)) {
                // dateLabel = "1月", queryDate = "2026"
                String monthStr = dateLabel.replace("月", "");
                String yearMonth = queryDate + "-" + String.format("%02d", Integer.parseInt(monthStr));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date date = sdf.parse(yearMonth);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return queryTotalEnergy(moduleIds, start, cal.getTime(), "month");
            }
        } catch (ParseException e) {
            log.error("趋势数据日期解析失败: dateLabel={}, timeUnit={}, queryDate={}", dateLabel, timeUnit, queryDate, e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取能源系数配置
     */
    private TbEnergyRatioInfo getEnergyRatioInfo(String energyType) {
        if (StringUtils.isBlank(energyType) || "all".equals(energyType)) {
            energyType = "1"; // 默认电力
        }
        try {
            LambdaQueryWrapper<TbEnergyRatioInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbEnergyRatioInfo::getIsenergyType, Integer.parseInt(energyType));
            return energyRatioInfoMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("获取能源系数失败, energyType={}", energyType, e);
            return null;
        }
    }

    /**
     * 创建空的统计数据
     */
    private TeamEnergyStatisticsVO createEmptyStatistics(String energyType) {
        TeamEnergyStatisticsVO vo = new TeamEnergyStatisticsVO();
        vo.setTotalConsumption("0.00");
        vo.setTotalCost("0.00");
        vo.setCarbonEmission("0.00");
        vo.setStandardCoal("0.00");
        vo.setEnergyUnit(getEnergyUnit(energyType));
        return vo;
    }

    /**
     * 汇总 BigDecimal 字段
     */
    private BigDecimal sumBigDecimal(List<TbEpEquEnergyDaycount> dataList, Function<TbEpEquEnergyDaycount, BigDecimal> getter) {
        return dataList.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

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
     * 根据索引获取颜色（用于趋势图系列）
     */
    private String getColorByIndex(int index) {
        String[] colors = {"#4B7BE5", "#23C343", "#FF9F40", "#F56C6C", "#909399", "#E6A23C"};
        return colors[index % colors.length];
    }
}
