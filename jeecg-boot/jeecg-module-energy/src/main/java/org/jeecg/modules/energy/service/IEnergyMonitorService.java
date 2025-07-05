package org.jeecg.modules.energy.service;

import java.util.List;
import java.util.Map;

/**
 * @Description: 能源监控服务接口
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
public interface IEnergyMonitorService {
    
    /**
     * 获取实时监控数据
     * @param orgCode 部门编码
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 实时监控数据
     */
    List<Map<String, Object>> getRealTimeData(String orgCode, Integer nowtype);
} 