package org.jeecg.modules.energy.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 时间间隔配置类
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Component
public class IntervalConfig {

    private static final Map<Integer, String> INTERVAL_MAP = new HashMap<>();
    private static final Map<Integer, String> INTERVAL_DISPLAY_MAP = new HashMap<>();

    static {
        INTERVAL_MAP.put(1, "15m");   // 15分钟
        INTERVAL_MAP.put(2, "30m");   // 30分钟
        INTERVAL_MAP.put(3, "1h");    // 1小时
        INTERVAL_MAP.put(4, "2h");    // 2小时

        INTERVAL_DISPLAY_MAP.put(1, "15分钟");
        INTERVAL_DISPLAY_MAP.put(2, "30分钟");
        INTERVAL_DISPLAY_MAP.put(3, "60分钟");
        INTERVAL_DISPLAY_MAP.put(4, "120分钟");
    }

    /**
     * 获取InfluxDB查询间隔
     * @param intervalCode 间隔编码
     * @return InfluxDB间隔字符串
     */
    public static String getInfluxInterval(Integer intervalCode) {
        return INTERVAL_MAP.get(intervalCode);
    }

    /**
     * 获取间隔显示名称
     * @param intervalCode 间隔编码
     * @return 显示名称
     */
    public static String getIntervalDisplayName(Integer intervalCode) {
        return INTERVAL_DISPLAY_MAP.get(intervalCode);
    }

    /**
     * 获取所有间隔映射
     * @return 间隔映射
     */
    public static Map<Integer, String> getAllIntervals() {
        return new HashMap<>(INTERVAL_MAP);
    }
}
