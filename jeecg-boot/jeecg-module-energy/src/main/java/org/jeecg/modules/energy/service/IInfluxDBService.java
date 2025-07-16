package org.jeecg.modules.energy.service;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.List;

/**
 * InfluxDB服务接口
 */
public interface IInfluxDBService {
    
    /**
     * 在指定数据库中执行查询
     * @param command 查询命令
     * @param database 数据库名
     * @return 查询结果
     */
    QueryResult queryInDatabase(String command, String database);
    
    /**
     * 执行查询
     * @param command 查询命令
     * @return 查询结果
     */
    QueryResult query(String command);
    
    /**
     * 执行查询
     * @param query 查询对象
     * @return 查询结果
     */
    QueryResult query(Query query);
    
    /**
     * 获取所有数据库列表
     * @return 数据库名称列表
     */
    List<String> getDatabases();
    
    /**
     * 根据模块ID和时间范围查询数据
     * @param moduleId 模块ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询结果
     */
    QueryResult queryByModuleIdAndTimeRange(String moduleId, String startTime, String endTime);
    
    /**
     * 根据tagname查询最新数据
     * @param tagname 标签名称
     * @return 查询结果
     */
    QueryResult queryLatestByTagname(String tagname);
    
    /**
     * 查询指定模块ID的最新数据
     * @param moduleId 模块ID
     * @return 查询结果
     */
    QueryResult queryLatestByModuleId(String moduleId);
    
    /**
     * 查询指定日期的日统计数据
     * @param moduleId 模块ID
     * @param date 日期 (yyyy-MM-dd)
     * @return 查询结果
     */
    QueryResult queryDailyStatsByDate(String moduleId, String date);
    
    /**
     * 查询指定月份的月统计数据
     * @param moduleId 模块ID
     * @param year 年份
     * @param month 月份
     * @return 查询结果
     */
    QueryResult queryMonthlyStatsByYearMonth(String moduleId, int year, int month);
    
    /**
     * 查询指定年份的年统计数据
     * @param moduleId 模块ID
     * @param year 年份
     * @return 查询结果
     */
    QueryResult queryYearlyStatsByYear(String moduleId, int year);
} 