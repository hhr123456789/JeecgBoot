package org.jeecg.modules.energy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: InfluxDB查询构建器
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Component
@Slf4j
public class InfluxDBQueryBuilder {
    
    /**
     * 构建时序数据查询语句
     */
    public String buildTimeSeriesQuery(List<String> moduleIds, List<Integer> parameters, 
                                     String timeGranularity, String startTime, String endTime) {
        
        // 1. 构建tagname列表
        List<String> tagnames = buildTagnames(moduleIds, parameters);
        
        // 2. 根据时间粒度设置GROUP BY间隔
        String interval = getTimeInterval(timeGranularity);
        
        // 3. 转换时间格式
        String utcStartTime = convertToUTC(startTime);
        String utcEndTime = convertToUTC(endTime);
        
        // 4. 构建查询语句 - 使用OR语法（与InfluxDBQueryServiceImpl保持一致）
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value ");
        sql.append("FROM hist ");
        sql.append("WHERE (").append(buildOrCondition(tagnames)).append(") ");
        sql.append("AND time >= '").append(utcStartTime).append("' ");
        sql.append("AND time < '").append(utcEndTime).append("' ");
        sql.append("GROUP BY time(").append(interval).append("), tagname ");
        sql.append("ORDER BY time ASC");
        
        log.info("构建的InfluxDB查询语句: {}", sql.toString());
        log.info("原始时间: {} -> {}, UTC时间: {} -> {}", startTime, endTime, utcStartTime, utcEndTime);
        return sql.toString();
    }

    /**
     * 构建跨月查询的数据库列表和查询语句
     * 当时间粒度为年时，需要查询多个月份的数据库
     */
    public List<DatabaseQuery> buildCrossMonthQueries(List<String> moduleIds, List<Integer> parameters, 
                                                     String timeGranularity, String startTime, String endTime) {
        
        List<DatabaseQuery> queries = new ArrayList<>();
        
        // 1. 构建tagname列表
        List<String> tagnames = buildTagnames(moduleIds, parameters);
        
        // 2. 根据时间粒度设置GROUP BY间隔
        String interval = getTimeInterval(timeGranularity);
        
        // 3. 转换时间格式
        String utcStartTime = convertToUTC(startTime);
        String utcEndTime = convertToUTC(endTime);
        
        // 4. 解析开始和结束时间，确定需要查询的月份
        List<String> monthDatabases = getMonthDatabasesBetween(startTime, endTime);
        
        // 5. 为每个月份数据库构建查询语句
        for (String dbName : monthDatabases) {
            String monthStartTime = getMonthStartTime(dbName, utcStartTime);
            String monthEndTime = getMonthEndTime(dbName, utcEndTime);
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value ");
            sql.append("FROM hist ");
            sql.append("WHERE (").append(buildOrCondition(tagnames)).append(") ");
            sql.append("AND time >= '").append(monthStartTime).append("' ");
            sql.append("AND time < '").append(monthEndTime).append("' ");
            sql.append("GROUP BY time(").append(interval).append("), tagname ");
            sql.append("ORDER BY time ASC");
            
            queries.add(new DatabaseQuery(dbName, sql.toString()));
            log.info("构建数据库 {} 的查询语句: {}", dbName, sql.toString());
        }
        
        log.info("跨月查询构建完成，共需查询 {} 个数据库", queries.size());
        return queries;
    }

    /**
     * 获取时间范围内需要查询的月份数据库列表
     */
    private List<String> getMonthDatabasesBetween(String startTime, String endTime) {
        List<String> databases = new ArrayList<>();
        
        try {
            // 解析开始和结束时间
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 获取开始月份和结束月份
            LocalDateTime current = startDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = endDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            
            while (!current.isAfter(end)) {
                String dbName = "hist" + current.format(DateTimeFormatter.ofPattern("yyyyMM"));
                databases.add(dbName);
                log.debug("添加数据库: {} (对应月份: {})", dbName, current.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                current = current.plusMonths(1);
            }
            
        } catch (Exception e) {
            log.error("解析时间范围失败: {} -> {}", startTime, endTime, e);
            // 如果解析失败，返回当前月份的数据库
            String currentMonthDb = "hist" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            databases.add(currentMonthDb);
        }
        
        log.info("需要查询的数据库列表: {}", databases);
        return databases;
    }

    /**
     * 获取指定数据库对应月份的开始时间
     */
    private String getMonthStartTime(String dbName, String globalStartTime) {
        try {
            // 从数据库名解析年月 (例如: hist202507 -> 2025-07)
            String yearMonth = dbName.substring(4); // 去掉"hist"前缀
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(4, 6);
            
            String monthStart = year + "-" + month + "-01T00:00:00Z";
            
            // 返回全局开始时间和月份开始时间中较晚的那个
            if (globalStartTime.compareTo(monthStart) > 0) {
                return globalStartTime;
            } else {
                return monthStart;
            }
            
        } catch (Exception e) {
            log.error("解析数据库名失败: {}", dbName, e);
            return globalStartTime;
        }
    }

    /**
     * 获取指定数据库对应月份的结束时间
     */
    private String getMonthEndTime(String dbName, String globalEndTime) {
        try {
            // 从数据库名解析年月 (例如: hist202507 -> 2025-07)
            String yearMonth = dbName.substring(4); // 去掉"hist"前缀
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(4, 6);
            
            // 计算月份的最后一天
            LocalDateTime monthEnd = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, 0, 0, 0)
                    .plusMonths(1).minusSeconds(1);
            String monthEndStr = monthEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            
            // 返回全局结束时间和月份结束时间中较早的那个
            if (globalEndTime.compareTo(monthEndStr) < 0) {
                return globalEndTime;
            } else {
                return monthEndStr;
            }
            
        } catch (Exception e) {
            log.error("解析数据库名失败: {}", dbName, e);
            return globalEndTime;
        }
    }

    /**
     * 数据库查询对象
     */
    public static class DatabaseQuery {
        private String databaseName;
        private String queryString;
        
        public DatabaseQuery(String databaseName, String queryString) {
            this.databaseName = databaseName;
            this.queryString = queryString;
        }
        
        public String getDatabaseName() {
            return databaseName;
        }
        
        public String getQueryString() {
            return queryString;
        }
        
        @Override
        public String toString() {
            return "DatabaseQuery{" +
                    "databaseName='" + databaseName + '\'' +
                    ", queryString='" + queryString + '\'' +
                    '}';
        }
    }
    
    /**
     * 根据时间粒度获取间隔
     */
    private String getTimeInterval(String granularity) {
        switch (granularity) {
            case "day": return "1h";    // 每小时
            case "month": return "1d";  // 每天
            case "year": return "30d";  // 每月
            default: return "1h";
        }
    }
    
    /**
     * 构建tagname列表
     */
    private List<String> buildTagnames(List<String> moduleIds, List<Integer> parameters) {
        List<String> tagnames = new ArrayList<>();
        
        if (moduleIds == null || moduleIds.isEmpty()) {
            log.error("构建tagname时，仪表ID列表为空");
            return tagnames;
        }
        
        if (parameters == null || parameters.isEmpty()) {
            log.error("构建tagname时，参数列表为空");
            return tagnames;
        }
        
        for (String moduleId : moduleIds) {
            if (moduleId == null || moduleId.trim().isEmpty()) {
                log.warn("跳过空的仪表ID");
                continue;
            }
            
            for (Integer param : parameters) {
                if (param == null) {
                    log.warn("跳过空的参数编码");
                    continue;
                }
                
                String fieldName = getFieldNameByParam(param);
                String tagname = moduleId.trim().toUpperCase() + "#" + fieldName;
                tagnames.add(tagname);
                log.debug("添加tagname: {}", tagname);
            }
        }
        
        log.info("构建tagname列表完成，共{}个: {}", tagnames.size(), tagnames);
        return tagnames;
    }
    
    /**
     * 根据参数编码获取字段名称
     */
    private String getFieldNameByParam(Integer paramCode) {
        switch (paramCode) {
            case 1: return "IA";        // A相电流
            case 2: return "IB";        // B相电流
            case 3: return "IC";        // C相电流
            case 4: return "UA";        // A相电压
            case 5: return "UB";        // B相电压
            case 6: return "UC";        // C相电压
            case 7: return "P";         // 总有功功率 - 修复：从PP改为P
            case 8: return "Q";        // 总无功功率
            case 9: return "S";        // 总视在功率
            case 10: return "PFS";      // 总功率因数
            case 11: return "HZ";       // 频率
            case 12: return "KWH";      // 正向有功总电能
            case 13: return "KVARH";    // 正向无功总电能
            // 其他能源参数
            case 20: return "TEMP";     // 温度
            case 21: return "PRE";    // 压力
            case 22: return "PV";     // 瞬时流量
            case 23: return "SV";    // 累计值
            default: return "VALUE";    // 默认值
        }
    }
    
    /**
     * 构建OR条件语句
     * 将多个tagname转换为OR条件，例如：tagname = 'YJ0001_13#IA' OR tagname = 'YJ0001_13#IB' OR tagname = 'YJ0001_13#PP'
     * 与InfluxDBQueryServiceImpl保持一致的语法
     */
    private String buildOrCondition(List<String> values) {
        if (values == null || values.isEmpty()) {
            log.error("构建OR条件时，值列表为空");
            return "tagname = 'EMPTY'";
        }
        
        List<String> conditions = values.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(value -> "tagname = '" + value.trim() + "'")
                .collect(Collectors.toList());
        
        if (conditions.isEmpty()) {
            log.error("构建OR条件时，过滤后的值列表为空");
            return "tagname = 'EMPTY'";
        }
        
        String result = String.join(" OR ", conditions);
        log.debug("构建OR条件: {}", result);
        return result;
    }
    
    /**
     * 北京时间转UTC时间
     */
    private String convertToUTC(String beijingTimeStr) {
        try {
            // 输入格式为 "2025-07-25 00:00:00"
            if (beijingTimeStr == null || beijingTimeStr.trim().isEmpty()) {
                log.error("输入时间字符串为空");
                return "2025-01-01T00:00:00Z";
            }
            
            // 使用Java 8的时间API进行转换
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            
            LocalDateTime beijingTime = LocalDateTime.parse(beijingTimeStr.trim(), inputFormatter);
            LocalDateTime utcTime = beijingTime.minusHours(8); // 北京时间减8小时得到UTC时间
            
            String result = utcTime.format(outputFormatter);
            log.debug("时间转换: {} -> {}", beijingTimeStr, result);
            return result;
            
        } catch (Exception e) {
            log.error("时间转换失败: {}", beijingTimeStr, e);
            // 返回一个默认的UTC时间
            return "2025-01-01T00:00:00Z";
        }
    }
}
