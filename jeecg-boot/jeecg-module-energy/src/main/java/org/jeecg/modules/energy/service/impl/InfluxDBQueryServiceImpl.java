package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.config.IntervalConfig;
import org.jeecg.modules.energy.config.ParameterConfig;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IInfluxDBQueryService;
import org.jeecg.modules.energy.service.IInfluxDBService;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: InfluxDB查询服务实现类
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Slf4j
@Service
public class InfluxDBQueryServiceImpl implements IInfluxDBQueryService {

    @Autowired
    private IInfluxDBService influxDBService;

    @Autowired
    private InfluxDBConfig influxDBConfig;

    @Autowired
    private InfluxDB influxDB;

    @Autowired
    private TbModuleMapper tbModuleMapper;

    @Override
    public String buildRealTimeDataQuery(List<String> moduleIds,
                                         List<Integer> parameters,
                                         String startTime,
                                         String endTime,
                                         Integer interval) {

        // 1. 构建tagname条件 (module_id#parameter格式)
        List<String> tagConditions = new ArrayList<>();
        for (String moduleId : moduleIds) {
            for (Integer param : parameters) {
                // 根据仪表ID判断能源类型，选择对应的参数字段名
                String paramFieldName = getParameterFieldByModuleAndParam(moduleId, param);
                if (paramFieldName != null) {
                    // 构建InfluxDB的tagname格式：大写模块ID#参数名
                    String tagname = moduleId.toUpperCase() + "#" + paramFieldName;
                    tagConditions.add("tagname = '" + tagname + "'");
                    log.info("🔍 构建tagname: {} (仪表:{}, 参数:{}, 字段:{})",
                        tagname, moduleId, param, paramFieldName);
                }
            }
        }

        if (tagConditions.isEmpty()) {
            throw new IllegalArgumentException("没有有效的参数配置");
        }

        // 2. 构建时间聚合间隔
        String influxInterval = IntervalConfig.getInfluxInterval(interval);
        if (influxInterval == null) {
            throw new IllegalArgumentException("无效的时间间隔: " + interval);
        }

        // 3. 构建tagname条件字符串 (使用OR连接，因为InfluxDB 1.8不支持IN操作符)
        String tagnameCondition = "(" + String.join(" OR ", tagConditions) + ")";

        // 4. 构建完整查询语句
        String query = String.format(
                "SELECT mean(value) as value " +
                        "FROM %s " +
                        "WHERE time >= '%s' AND time <= '%s' " +
                        "AND %s " +
                        "AND status = 1 " +
                        "GROUP BY time(%s), tagname " +
                        "ORDER BY time ASC",
                influxDBConfig.getMeasurement(),
                convertToUTC(startTime),    // 转换为UTC时间
                convertToUTC(endTime),      // 转换为UTC时间
                tagnameCondition,           // 使用OR条件替代IN
                influxInterval
        );

        log.info("构建的InfluxDB查询语句: {}", query);

        // 调试：同时查询原始数据看看value字段的情况
        String debugQuery = String.format(
            "SELECT * FROM %s " +
            "WHERE time >= '%s' AND time <= '%s' " +
            "AND %s " +
            "LIMIT 5",
            influxDBConfig.getMeasurement(),
            convertToUTC(startTime),
            convertToUTC(endTime),
            tagnameCondition
        );

        log.info("🔍 调试查询语句（查看原始数据）: {}", debugQuery);

        return query;
    }

    /**
     * 根据仪表ID和参数编号获取对应的InfluxDB字段名
     * @param moduleId 仪表ID
     * @param param 参数编号
     * @return InfluxDB字段名
     */
    private String getParameterFieldByModuleAndParam(String moduleId, Integer param) {
        // 从数据库查询仪表的能源类型
        Integer energyType = getEnergyTypeFromDatabase(moduleId);

        if (energyType == null) {
            log.warn("未找到仪表 {} 的能源类型，默认使用电力参数", moduleId);
            energyType = 1; // 默认为电力
        }

        switch (energyType) {
            case 1: // 电力数据
                ParameterConfig.ParameterInfo paramInfo = ParameterConfig.getParameterInfo(param);
                return paramInfo != null ? paramInfo.getFieldName() : null;

            case 8: // 天然气数据
                return getGasParameterField(param);

            case 5: // 压缩空气数据
                return getAirParameterField(param);

            case 2: // 用水数据
                return getWaterParameterField(param);

            default:
                log.warn("未知的能源类型: {}, 仪表ID: {}, 默认使用电力参数", energyType, moduleId);
                ParameterConfig.ParameterInfo defaultParamInfo = ParameterConfig.getParameterInfo(param);
                return defaultParamInfo != null ? defaultParamInfo.getFieldName() : null;
        }
    }

    /**
     * 从数据库查询仪表的能源类型
     * @param moduleId 仪表ID
     * @return 能源类型 (1:电力, 2:水, 5:压缩空气, 8:天然气)
     */
    private Integer getEnergyTypeFromDatabase(String moduleId) {
        try {
            List<TbModule> modules = tbModuleMapper.selectByModuleId(moduleId);
            if (modules != null && !modules.isEmpty()) {
                TbModule module = modules.get(0); // 取第一个匹配的仪表
                log.info("🔍 查询到仪表 {} 的能源类型: {}", moduleId, module.getEnergyType());
                return module.getEnergyType();
            } else {
                log.warn("⚠️ 未找到仪表: {}", moduleId);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ 查询仪表能源类型失败: {}", moduleId, e);
            return null;
        }
    }

    /**
     * 获取天然气参数字段名
     */
    private String getGasParameterField(Integer param) {
        switch (param) {
            case 1: return "PV";   // 瞬时流量
            case 2: return "SV";   // 累计值
            case 3: return "TEMP"; // 温度
            case 4: return "PRE";  // 压力
            default:
                log.warn("未知的天然气参数: {}", param);
                return null;
        }
    }

    /**
     * 获取压缩空气参数字段名
     */
    private String getAirParameterField(Integer param) {
        switch (param) {
            case 1: return "PV";   // 瞬时流量
            case 2: return "SV";   // 累计值
            case 3: return "TEMP"; // 温度
            case 4: return "PRE";  // 压力
            default:
                log.warn("未知的压缩空气参数: {}", param);
                return null;
        }
    }

    /**
     * 获取用水参数字段名
     */
    private String getWaterParameterField(Integer param) {
        switch (param) {
            case 1: return "PV";   // 瞬时流量
            case 2: return "SV";   // 累计值
            case 3: return "TEMP"; // 温度
            case 4: return "PRE";  // 压力
            default:
                log.warn("未知的用水参数: {}", param);
                return null;
        }
    }

    @Override
    public List<Map<String, Object>> queryRealTimeData(List<String> moduleIds,
                                                        List<Integer> parameters,
                                                        String startTime,
                                                        String endTime,
                                                        Integer interval) {
        try {
            // 构建查询语句
            String queryStr = buildRealTimeDataQuery(moduleIds, parameters, startTime, endTime, interval);

            // 获取当前月份的数据库名
            String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();

            // 确保数据库存在
            if (!influxDB.databaseExists(currentMonthDB)) {
                log.warn("数据库不存在: {}", currentMonthDB);
                return new ArrayList<>();
            }

            // 执行查询
            QueryResult queryResult = influxDBService.queryInDatabase(queryStr, currentMonthDB);

            // 解析查询结果
            log.info("🔍 准备调用 InfluxDBUtil.parseQueryResult 解析查询结果");
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
            log.info("🔍 InfluxDBUtil.parseQueryResult 调用完成");

            log.info("InfluxDB查询完成，返回数据条数: {}", resultList.size());

            // 输出前几条数据用于调试
            if (!resultList.isEmpty()) {
                log.info("🔍 前3条数据示例：");
                for (int i = 0; i < Math.min(3, resultList.size()); i++) {
                    log.info("  数据[{}]: {}", i, resultList.get(i));
                }

                // 统计不同tagname的数据量
                Map<String, Long> tagnameStats = resultList.stream()
                    .filter(r -> r.get("tagname") != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        r -> (String) r.get("tagname"),
                        java.util.stream.Collectors.counting()
                    ));
                log.info("🔍 按tagname统计数据量: {}", tagnameStats);

                // 统计有效数据（value不为null）的数量
                long validDataCount = resultList.stream()
                    .filter(r -> r.get("value") != null)
                    .count();
                log.info("🔍 有效数据量（value不为null）: {}/{}", validDataCount, resultList.size());
            }

            return resultList;

        } catch (Exception e) {
            log.error("InfluxDB查询失败", e);
            throw new RuntimeException("查询实时数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToUTC(String localTime) {
        try {
            // 东八区转UTC：减8小时
            LocalDateTime local = LocalDateTime.parse(localTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZonedDateTime utc = local.atZone(ZoneId.of("Asia/Shanghai"))
                    .withZoneSameInstant(ZoneOffset.UTC);
            return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        } catch (Exception e) {
            log.error("时间转换失败: {}", localTime, e);
            throw new IllegalArgumentException("时间格式错误: " + localTime, e);
        }
    }
}
