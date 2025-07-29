package org.jeecg.modules.energy.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 参数配置类
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Component
public class ParameterConfig {

    // 参数编号到InfluxDB字段的映射
    private static final Map<Integer, ParameterInfo> PARAMETER_MAP = new HashMap<>();

    static {
        // 根据字典表正确映射
        PARAMETER_MAP.put(1, new ParameterInfo("IA", "A相电流", "A"));
        PARAMETER_MAP.put(2, new ParameterInfo("IB", "B相电流", "A"));
        PARAMETER_MAP.put(3, new ParameterInfo("IC", "C相电流", "A"));
        PARAMETER_MAP.put(4, new ParameterInfo("UA", "A相电压", "V"));
        PARAMETER_MAP.put(5, new ParameterInfo("UB", "B相电压", "V"));
        PARAMETER_MAP.put(6, new ParameterInfo("UC", "C相电压", "V"));
        PARAMETER_MAP.put(7, new ParameterInfo("PFS", "总功率因素", ""));
        PARAMETER_MAP.put(8, new ParameterInfo("PFSA", "A相功率因素", ""));
        PARAMETER_MAP.put(9, new ParameterInfo("PFSB", "B相功率因素", ""));
        PARAMETER_MAP.put(10, new ParameterInfo("PFSC", "C相功率因素", ""));
        PARAMETER_MAP.put(11, new ParameterInfo("HZ", "频率", "Hz"));
        PARAMETER_MAP.put(12, new ParameterInfo("P", "总有功功率", "kW"));
        PARAMETER_MAP.put(13, new ParameterInfo("PA", "A相有功功率", "kW"));
        PARAMETER_MAP.put(14, new ParameterInfo("PB", "B相有功功率", "kW"));
        PARAMETER_MAP.put(15, new ParameterInfo("PC", "C相有功功率", "kW"));
        PARAMETER_MAP.put(16, new ParameterInfo("KWH", "有功电量", "kWh"));
        PARAMETER_MAP.put(17, new ParameterInfo("KVARH", "无功电量", "kVarh"));

        // 其他能源类型参数（天然气、压缩空气、用水）- 按字典表正确映射
        PARAMETER_MAP.put(101, new ParameterInfo("TEMP", "温度", "℃"));
        PARAMETER_MAP.put(102, new ParameterInfo("PRE", "压力", "MPa"));
        PARAMETER_MAP.put(103, new ParameterInfo("PV", "瞬时流量", "m³/h"));
        PARAMETER_MAP.put(104, new ParameterInfo("SV", "累计量", "m³"));
        // 可以根据需要添加更多参数映射
    }

    /**
     * 根据参数编号获取参数信息
     * @param paramCode 参数编号
     * @return 参数信息
     */
    public static ParameterInfo getParameterInfo(Integer paramCode) {
        return PARAMETER_MAP.get(paramCode);
    }

    /**
     * 根据字段名查找参数信息
     * @param fieldName 字段名
     * @return 参数信息
     */
    public static ParameterInfo findParameterByField(String fieldName) {
        return PARAMETER_MAP.values().stream()
                .filter(param -> param.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有参数映射
     * @return 参数映射
     */
    public static Map<Integer, ParameterInfo> getAllParameters() {
        return new HashMap<>(PARAMETER_MAP);
    }

    /**
     * 参数信息类
     */
    @Data
    @AllArgsConstructor
    public static class ParameterInfo {
        private String fieldName;    // InfluxDB字段名
        private String displayName;  // 显示名称
        private String unit;         // 单位
    }
}
