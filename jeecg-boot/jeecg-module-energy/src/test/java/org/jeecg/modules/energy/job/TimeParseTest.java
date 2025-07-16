package org.jeecg.modules.energy.job;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间解析测试
 */
public class TimeParseTest {

    @Test
    public void testInfluxTimeFormats() {
        // 测试各种InfluxDB时间格式
        String[] timeFormats = {
            "2025-07-15T09:22:12.075Z",  // 带毫秒的UTC时间
            "2025-07-15T09:22:12Z",      // 不带毫秒的UTC时间
            "2025-07-15T09:22:12.075",   // 带毫秒的本地时间
            "2025-07-15T09:22:12",       // 不带毫秒的本地时间
            "2025-07-15 09:22:12"        // 普通格式
        };

        for (String timeStr : timeFormats) {
            System.out.println("测试时间格式: " + timeStr);
            Date parsedDate = parseInfluxTime(timeStr);
            if (parsedDate != null) {
                System.out.println("解析成功: " + parsedDate);
                System.out.println("格式化输出: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(parsedDate));
            } else {
                System.out.println("解析失败");
            }
            System.out.println("---");
        }
    }

    /**
     * 模拟InfluxDBSyncJob中的时间解析方法
     */
    private Date parseInfluxTime(Object timeObj) {
        if (timeObj == null) {
            return null;
        }

        try {
            if (timeObj instanceof String) {
                String timeStr = (String) timeObj;
                // InfluxDB时间格式通常是 "2025-07-15T09:22:12.075Z" 或 "2025-07-11T09:06:06Z" 或 "2025-07-11 09:06:06"
                if (timeStr.contains("T") && timeStr.endsWith("Z")) {
                    // ISO 8601格式，支持毫秒和不带毫秒的格式
                    SimpleDateFormat isoFormat;
                    if (timeStr.contains(".")) {
                        // 包含毫秒的格式：2025-07-15T09:22:12.075Z
                        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    } else {
                        // 不包含毫秒的格式：2025-07-15T09:22:12Z
                        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    }
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date utcDate = isoFormat.parse(timeStr);
                    // 转换为本地时间（东八区）
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(utcDate);
                    calendar.add(Calendar.HOUR, 8); // TIME_ZONE_OFFSET_HOURS
                    return calendar.getTime();
                } else if (timeStr.contains("T")) {
                    // ISO格式但不以Z结尾，可能是本地时间格式
                    SimpleDateFormat isoFormat;
                    if (timeStr.contains(".")) {
                        // 包含毫秒：2025-07-15T09:22:12.075
                        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    } else {
                        // 不包含毫秒：2025-07-15T09:22:12
                        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    }
                    return isoFormat.parse(timeStr);
                } else {
                    // 普通格式：2025-07-11 09:06:06
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return format.parse(timeStr);
                }
            } else if (timeObj instanceof Date) {
                // 如果已经是Date对象，直接返回
                return (Date) timeObj;
            } else if (timeObj instanceof Long) {
                // 如果是时间戳（毫秒）
                return new Date((Long) timeObj);
            }
            return null;
        } catch (Exception e) {
            System.err.println("解析时间失败: " + timeObj + ", 错误: " + e.getMessage());
            return null;
        }
    }

    @Test
    public void testSpecificFailingTime() {
        // 测试具体失败的时间格式
        String failingTime = "2025-07-15T09:22:12.075Z";
        System.out.println("测试失败的时间: " + failingTime);
        
        Date result = parseInfluxTime(failingTime);
        if (result != null) {
            System.out.println("解析成功!");
            System.out.println("UTC时间: " + failingTime);
            System.out.println("本地时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(result));
            
            // 验证时区转换是否正确
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date utcDate = utcFormat.parse(failingTime);
                System.out.println("原始UTC时间: " + utcDate);
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(utcDate);
                cal.add(Calendar.HOUR, 8);
                System.out.println("转换后本地时间: " + cal.getTime());
                
                System.out.println("时区转换正确: " + result.equals(cal.getTime()));
            } catch (Exception e) {
                System.err.println("验证失败: " + e.getMessage());
            }
        } else {
            System.out.println("解析失败!");
        }
    }
}
