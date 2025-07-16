package org.jeecg.modules.energy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.service.IInfluxDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * InfluxDB服务实现类
 */
@Slf4j
@Service
public class InfluxDBServiceImpl implements IInfluxDBService {
    
    @Autowired
    public InfluxDB influxDB;
    
    @Autowired
    private InfluxDBConfig influxDBConfig;
    
    /**
     * 在指定数据库中执行查询
     * @param command 查询命令
     * @param database 数据库名
     * @return 查询结果
     */
    @Override
    public QueryResult queryInDatabase(String command, String database) {
        return influxDB.query(new Query(command, database));
    }
    
    @Override
    public QueryResult query(String command) {
        // 获取当前月份的数据库名
        String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
        return influxDB.query(new Query(command, currentMonthDB));
    }
    
    @Override
    public QueryResult query(Query query) {
        return influxDB.query(query);
    }
    
    /**
     * 获取所有数据库列表
     * @return 数据库名称列表
     */
    @Override
    public List<String> getDatabases() {
        QueryResult result = influxDB.query(new Query("SHOW DATABASES"));
        List<String> databases = new ArrayList<>();
        
        if (result.getResults() != null && !result.getResults().isEmpty()) {
            QueryResult.Result queryResult = result.getResults().get(0);
            if (queryResult.getSeries() != null && !queryResult.getSeries().isEmpty()) {
                QueryResult.Series series = queryResult.getSeries().get(0);
                List<List<Object>> values = series.getValues();
                if (values != null) {
                    databases = values.stream()
                            .map(value -> value.get(0).toString())
                            .collect(Collectors.toList());
                }
            }
        }
        
        return databases;
    }
    
    @Override
    public QueryResult queryByModuleIdAndTimeRange(String moduleId, String startTime, String endTime) {
        // 解析开始时间和结束时间，确定需要查询哪些月份的数据库
        LocalDate startDate = LocalDate.parse(startTime.substring(0, 10));
        LocalDate endDate = LocalDate.parse(endTime.substring(0, 10));
        
        // 如果开始和结束时间在同一个月内
        if (startDate.getYear() == endDate.getYear() && startDate.getMonthValue() == endDate.getMonthValue()) {
            String dbName = influxDBConfig.getDatabaseName(startDate.getYear(), startDate.getMonthValue());
            String command = String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/ AND time >= '%s' AND time <= '%s'", 
                    influxDBConfig.getMeasurement(), moduleId, startTime, endTime);
            
            // 确保数据库存在
            if (!influxDB.databaseExists(dbName)) {
                log.warn("数据库 {} 不存在，跳过查询", dbName);
                return new QueryResult();
            }
            log.info("成功连接到数据库: {}", dbName);
            
            return queryInDatabase(command, dbName);
        } else {
            // 跨月查询，合并多个月份的结果
            List<QueryResult> results = new ArrayList<>();
            
            // 计算开始日期到结束日期之间的所有月份
            YearMonth startYearMonth = YearMonth.from(startDate);
            YearMonth endYearMonth = YearMonth.from(endDate);
            
            for (YearMonth yearMonth = startYearMonth; 
                 !yearMonth.isAfter(endYearMonth); 
                 yearMonth = yearMonth.plusMonths(1)) {
                
                String dbName = influxDBConfig.getDatabaseName(yearMonth.getYear(), yearMonth.getMonthValue());
                
                // 确保数据库存在
                if (!influxDB.databaseExists(dbName)) {
                    continue;
                }
                
                String command;
                if (yearMonth.equals(startYearMonth)) {
                    // 第一个月，使用起始时间
                    command = String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/ AND time >= '%s'", 
                            influxDBConfig.getMeasurement(), moduleId, startTime);
                } else if (yearMonth.equals(endYearMonth)) {
                    // 最后一个月，使用结束时间
                    command = String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/ AND time <= '%s'", 
                            influxDBConfig.getMeasurement(), moduleId, endTime);
                } else {
                    // 中间月份，查询整个月
                    command = String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/", 
                            influxDBConfig.getMeasurement(), moduleId);
                }
                
                results.add(queryInDatabase(command, dbName));
            }
            
            // 合并查询结果
            QueryResult mergedResult = new QueryResult();
            for (QueryResult result : results) {
                if (result.getResults() != null && !result.getResults().isEmpty()) {
                    if (mergedResult.getResults() == null) {
                        mergedResult.setResults(new ArrayList<>());
                    }
                    mergedResult.getResults().addAll(result.getResults());
                }
            }
            
            return mergedResult;
        }
    }
    
    @Override
    public QueryResult queryLatestByTagname(String tagname) {
        // 获取当前月份的数据库名
        String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
        
        // 确保数据库存在
        if (!influxDB.databaseExists(currentMonthDB)) {
            return new QueryResult();
        }
        
        String command = String.format("SELECT * FROM %s WHERE tagname = '%s' ORDER BY time DESC LIMIT 1", 
                influxDBConfig.getMeasurement(), tagname);
        return queryInDatabase(command, currentMonthDB);
    }
    
    /**
     * 查询指定模块ID的最新数据
     * @param moduleId 模块ID
     * @return 查询结果
     */
    @Override
    public QueryResult queryLatestByModuleId(String moduleId) {
        // 获取当前月份的数据库名
        String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
        
        // 确保数据库存在
        if (!influxDB.databaseExists(currentMonthDB)) {
            return new QueryResult();
        }
        
        String command = String.format("SELECT * FROM %s WHERE tagname =~ /^%s#.*/ GROUP BY tagname ORDER BY time DESC LIMIT 1", 
                influxDBConfig.getMeasurement(), moduleId);
        return queryInDatabase(command, currentMonthDB);
    }
    
    /**
     * 查询指定日期的日统计数据
     * @param moduleId 模块ID
     * @param date 日期 (yyyy-MM-dd)
     * @return 查询结果
     */
    @Override
    public QueryResult queryDailyStatsByDate(String moduleId, String date) {
        // 解析日期
        LocalDate localDate = LocalDate.parse(date);
        String dbName = influxDBConfig.getDatabaseName(localDate.getYear(), localDate.getMonthValue());
        
        // 确保数据库存在
        if (!influxDB.databaseExists(dbName)) {
            return new QueryResult();
        }
        
        String command = String.format(
                "SELECT mean(value) as mean_value, min(value) as min_value, max(value) as max_value, sum(value) as sum_value " +
                "FROM %s WHERE tagname =~ /^%s#.*/ AND time >= '%s 00:00:00' AND time <= '%s 23:59:59' GROUP BY tagname", 
                influxDBConfig.getMeasurement(), moduleId, date, date);
        
        return queryInDatabase(command, dbName);
    }
    
    /**
     * 查询指定月份的月统计数据
     * @param moduleId 模块ID
     * @param year 年份
     * @param month 月份
     * @return 查询结果
     */
    @Override
    public QueryResult queryMonthlyStatsByYearMonth(String moduleId, int year, int month) {
        String dbName = influxDBConfig.getDatabaseName(year, month);
        
        // 确保数据库存在
        if (!influxDB.databaseExists(dbName)) {
            return new QueryResult();
        }
        
        // 获取月份的第一天和最后一天
        YearMonth yearMonth = YearMonth.of(year, month);
        String firstDay = yearMonth.atDay(1).toString();
        String lastDay = yearMonth.atEndOfMonth().toString();
        
        String command = String.format(
                "SELECT mean(value) as mean_value, min(value) as min_value, max(value) as max_value, sum(value) as sum_value " +
                "FROM %s WHERE tagname =~ /^%s#.*/ AND time >= '%s 00:00:00' AND time <= '%s 23:59:59' GROUP BY tagname", 
                influxDBConfig.getMeasurement(), moduleId, firstDay, lastDay);
        
        return queryInDatabase(command, dbName);
    }
    
    /**
     * 查询指定年份的年统计数据
     * @param moduleId 模块ID
     * @param year 年份
     * @return 查询结果
     */
    @Override
    public QueryResult queryYearlyStatsByYear(String moduleId, int year) {
        List<QueryResult> results = new ArrayList<>();
        
        // 遍历年份的每个月
        for (int month = 1; month <= 12; month++) {
            String dbName = influxDBConfig.getDatabaseName(year, month);
            
            // 确保数据库存在
            if (!influxDB.databaseExists(dbName)) {
                continue;
            }
            
            // 获取月份的第一天和最后一天
            YearMonth yearMonth = YearMonth.of(year, month);
            String firstDay = yearMonth.atDay(1).toString();
            String lastDay = yearMonth.atEndOfMonth().toString();
            
            String command = String.format(
                    "SELECT mean(value) as mean_value, min(value) as min_value, max(value) as max_value, sum(value) as sum_value " +
                    "FROM %s WHERE tagname =~ /^%s#.*/ AND time >= '%s 00:00:00' AND time <= '%s 23:59:59' GROUP BY tagname", 
                    influxDBConfig.getMeasurement(), moduleId, firstDay, lastDay);
            
            results.add(queryInDatabase(command, dbName));
        }
        
        // 合并查询结果
        QueryResult mergedResult = new QueryResult();
        for (QueryResult result : results) {
            if (result.getResults() != null && !result.getResults().isEmpty()) {
                if (mergedResult.getResults() == null) {
                    mergedResult.setResults(new ArrayList<>());
                }
                mergedResult.getResults().addAll(result.getResults());
            }
        }
        
        return mergedResult;
    }
} 