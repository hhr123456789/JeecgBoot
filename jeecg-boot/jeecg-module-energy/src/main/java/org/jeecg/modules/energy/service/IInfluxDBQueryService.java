package org.jeecg.modules.energy.service;

import java.util.List;
import java.util.Map;

/**
 * @Description: InfluxDB查询服务接口
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
public interface IInfluxDBQueryService {

    /**
     * 构建实时数据查询语句
     * @param moduleIds 仪表ID列表
     * @param parameters 参数列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 查询间隔
     * @return 查询语句
     */
    String buildRealTimeDataQuery(List<String> moduleIds,
                                  List<Integer> parameters,
                                  String startTime,
                                  String endTime,
                                  Integer interval);

    /**
     * 执行实时数据查询
     * @param moduleIds 仪表ID列表
     * @param parameters 参数列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 查询间隔
     * @return 查询结果
     */
    List<Map<String, Object>> queryRealTimeData(List<String> moduleIds,
                                                 List<Integer> parameters,
                                                 String startTime,
                                                 String endTime,
                                                 Integer interval);

    /**
     * 本地时间转UTC时间
     * @param localTime 本地时间
     * @return UTC时间
     */
    String convertToUTC(String localTime);
}
