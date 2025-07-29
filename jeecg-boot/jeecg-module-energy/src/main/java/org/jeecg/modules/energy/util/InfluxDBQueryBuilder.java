package org.jeecg.modules.energy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        
        // 3. 构建查询语句
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value ");
        sql.append("FROM hist ");
        sql.append("WHERE tagname IN (").append(buildInClause(tagnames)).append(") ");
        sql.append("AND time >= '").append(convertToUTC(startTime)).append("' ");
        sql.append("AND time < '").append(convertToUTC(endTime)).append("' ");
        sql.append("GROUP BY time(").append(interval).append("), tagname ");
        sql.append("ORDER BY time ASC");
        
        log.info("构建的InfluxDB查询语句: {}", sql.toString());
        return sql.toString();
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
        for (String moduleId : moduleIds) {
            for (Integer param : parameters) {
                String fieldName = getFieldNameByParam(param); // IA, IB, UC等
                tagnames.add(moduleId.toUpperCase() + "#" + fieldName);
            }
        }
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
            case 7: return "PP";        // 总有功功率
            case 8: return "QQ";        // 总无功功率
            case 9: return "SS";        // 总视在功率
            case 10: return "PFS";      // 总功率因数
            case 11: return "HZ";       // 频率
            case 12: return "KWH";      // 正向有功总电能
            case 13: return "KVARH";    // 正向无功总电能
            // 其他能源参数
            case 20: return "TEMP";     // 温度
            case 21: return "PRESS";    // 压力
            case 22: return "FLOW";     // 瞬时流量
            case 23: return "ACCUM";    // 累计值
            default: return "VALUE";    // 默认值
        }
    }
    
    /**
     * 构建IN子句
     */
    private String buildInClause(List<String> values) {
        return values.stream()
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(", "));
    }
    
    /**
     * 北京时间转UTC时间（简化版）
     */
    private String convertToUTC(String beijingTimeStr) {
        // 这里简化处理，实际应该使用TimeZoneUtil
        try {
            // 假设输入格式为 "2025-07-25 00:00:00"
            String[] parts = beijingTimeStr.split(" ");
            String datePart = parts[0];
            String timePart = parts.length > 1 ? parts[1] : "00:00:00";
            
            // 转换为UTC格式
            return datePart + "T" + timePart + "Z";
        } catch (Exception e) {
            log.error("时间转换失败: {}", beijingTimeStr, e);
            return beijingTimeStr;
        }
    }
}
