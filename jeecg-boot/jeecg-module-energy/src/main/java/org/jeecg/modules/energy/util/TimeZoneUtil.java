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
     * UTC时间转北京时间（支持多种格式）
     */
    public String convertUTCToBeijing(String utcTimeStr) {
        if (utcTimeStr == null || utcTimeStr.trim().isEmpty()) {
            return utcTimeStr;
        }

        try {
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime utcTime;

            // 尝试不同的输入格式
            if (utcTimeStr.contains(".")) {
                // 包含毫秒的格式：2025-07-12T10:50:21.212Z
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                utcTime = LocalDateTime.parse(utcTimeStr, inputFormatter);
                log.debug("✅ 解析带毫秒的UTC时间: {}", utcTimeStr);
            } else {
                // 标准格式：2025-07-12T10:50:21Z
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                utcTime = LocalDateTime.parse(utcTimeStr, inputFormatter);
                log.debug("✅ 解析标准UTC时间: {}", utcTimeStr);
            }

            LocalDateTime beijingTime = utcTime.plusHours(8); // UTC+8
            String result = beijingTime.format(outputFormatter);

            log.debug("🕐 时间转换成功: {} -> {}", utcTimeStr, result);
            return result;

        } catch (Exception e) {
            log.error("❌ 时间转换失败: {}", utcTimeStr, e);
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
