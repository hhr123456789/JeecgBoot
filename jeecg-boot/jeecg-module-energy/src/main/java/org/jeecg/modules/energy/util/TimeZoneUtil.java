package org.jeecg.modules.energy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description: 时区处理工具类
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Component
@Slf4j
public class TimeZoneUtil {
    
    /**
     * UTC时间转北京时间
     */
    public String convertUTCToBeijing(String utcTimeStr) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            LocalDateTime utcTime = LocalDateTime.parse(utcTimeStr, inputFormatter);
            LocalDateTime beijingTime = utcTime.plusHours(8); // UTC+8
            
            return beijingTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("时间转换失败: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }
    
    /**
     * 北京时间转UTC时间
     */
    public String convertBeijingToUTC(String beijingTimeStr) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            
            LocalDateTime beijingTime = LocalDateTime.parse(beijingTimeStr, inputFormatter);
            LocalDateTime utcTime = beijingTime.minusHours(8); // UTC-8
            
            return utcTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("时间转换失败: {}", beijingTimeStr, e);
            return beijingTimeStr;
        }
    }
    
    /**
     * UTC时间转本地时间（静态方法）
     */
    public static String convertUTCToLocal(String utcTimeStr) {
        try {
            // 处理InfluxDB返回的时间格式
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime utcTime = LocalDateTime.parse(utcTimeStr, inputFormatter);
            LocalDateTime localTime = utcTime.plusHours(8); // UTC+8

            return localTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("UTC时间转本地时间失败: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }

    /**
     * 本地时间转UTC时间（静态方法）
     */
    public static String convertToUTC(String localTimeStr) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            LocalDateTime localTime = LocalDateTime.parse(localTimeStr, inputFormatter);
            LocalDateTime utcTime = localTime.minusHours(8); // UTC-8

            return utcTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("本地时间转UTC时间失败: {}", localTimeStr, e);
            return localTimeStr;
        }
    }

    /**
     * 格式化时间标签（根据粒度）
     */
    public String formatTimeLabel(String timeStr, String granularity) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            switch (granularity) {
                case "day":
                    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                case "month":
                    return dateTime.format(DateTimeFormatter.ofPattern("MM-dd"));
                case "year":
                    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                default:
                    return timeStr;
            }
        } catch (Exception e) {
            log.error("时间标签格式化失败: {}", timeStr, e);
            return timeStr;
        }
    }
}
