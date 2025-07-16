package org.jeecg.modules.energy.service;

import java.util.List;
import java.util.Map;

/**
 * @Description: 设备运行状态服务接口
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
public interface IRunStatusService {

    /**
     * 获取设备运行状态
     * @param orgCode 部门编码
     * @param deviceStatus 设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障)
     * @return 设备运行状态数据
     */
    List<Map<String, Object>> getRunStatus(String orgCode, Integer deviceStatus);
} 