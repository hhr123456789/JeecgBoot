package org.jeecg.modules.energy.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * InfluxDB配置类
 */
@Configuration
public class InfluxDBConfig {
    
    @Value("${influxdb.url}")
    private String influxDBUrl;
    
    @Value("${influxdb.username}")
    private String username;
    
    @Value("${influxdb.password}")
    private String password;
    
    @Value("${influxdb.database-prefix:hist}")
    private String databasePrefix;
    
    @Value("${influxdb.measurement:hist}")
    private String measurement;
    
    @Value("${influxdb.retention-policy:autogen}")
    private String retentionPolicy;
    
    @Value("${influxdb.connect-timeout:10}")
    private int connectTimeout;
    
    @Value("${influxdb.read-timeout:30}")
    private int readTimeout;
    
    @Value("${influxdb.write-timeout:10}")
    private int writeTimeout;
    
    @Bean
    public InfluxDB influxDB() {
        InfluxDB influxDB;
        
        // 根据是否配置用户名密码决定连接方式
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            // 有用户名密码时使用认证连接
            influxDB = InfluxDBFactory.connect(influxDBUrl, username, password);
        } else {
            // 无用户名密码时使用匿名连接
            influxDB = InfluxDBFactory.connect(influxDBUrl);
        }
        
        try {
            // 设置批量操作，提高写入效率
            influxDB.enableBatch();
            
            // 设置日志级别
            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
            
            return influxDB;
        } catch (Exception e) {
            influxDB.close();
            throw e;
        }
    }
    
    /**
     * 获取当前月份的数据库名
     * 格式：hist + 年份(4位) + 月份(2位)，例如：hist202507
     * @return 当前月份的数据库名
     */
    public String getCurrentMonthDatabaseName() {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        return databasePrefix + yearMonth;
    }
    
    /**
     * 获取指定年月的数据库名
     * @param year 年份
     * @param month 月份
     * @return 指定年月的数据库名
     */
    public String getDatabaseName(int year, int month) {
        return String.format("%s%04d%02d", databasePrefix, year, month);
    }
    
    /**
     * 获取数据库前缀
     * @return 数据库前缀
     */
    public String getDatabasePrefix() {
        return databasePrefix;
    }
    
    /**
     * 获取measurement名称
     * @return measurement名称
     */
    public String getMeasurement() {
        return measurement;
    }
    
    /**
     * 获取保留策略
     * @return 保留策略
     */
    public String getRetentionPolicy() {
        return retentionPolicy;
    }
} 