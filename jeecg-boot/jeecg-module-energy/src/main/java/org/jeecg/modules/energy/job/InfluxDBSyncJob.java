package org.jeecg.modules.energy.job;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.entity.*;
import org.jeecg.modules.energy.mapper.*;
import org.jeecg.modules.energy.service.IInfluxDBService;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * InfluxDB数据同步定时任务
 */
@Slf4j
@Component
public class InfluxDBSyncJob {

    // 任务执行状态标志，防止重叠执行
    private volatile boolean isRunning = false;
    
    @Autowired
    private IInfluxDBService influxDBService;
    
    @Autowired
    private InfluxDBConfig influxDBConfig;
    
    @Autowired
    private InfluxDB influxDB;
    
    @Autowired
    private TbModuleMapper moduleMapper;
    
    @Autowired
    private TbEquEleDataMapper equEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper equEnergyDataMapper;
    
    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;
    
    @Autowired
    private TbEpEquEnergyMonthcountMapper monthcountMapper;
    
    @Autowired
    private TbEpEquEnergyYearcountMapper yearcountMapper;
    
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // InfluxDB使用UTC时间，系统使用东八区时间，需要8小时时差调整
    private static final int TIME_ZONE_OFFSET_HOURS = 8;

    /**
     * 每5分钟同步一次实时数据和统计数据
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncRealTimeData() {
        // 检查是否已有任务在执行
        if (isRunning) {
            log.warn("上一次同步任务尚未完成，跳过本次执行");
            return;
        }

        isRunning = true;
        log.info("开始同步实时数据和统计数据...");
        long startTime = System.currentTimeMillis();
        try {
            // 1. 同步实时数据
            syncRealTimeDataFromInfluxDB();

            // 2. 实时更新统计表
            updateStatisticsRealTime();

            long duration = System.currentTimeMillis() - startTime;
            log.info("实时数据和统计数据同步完成，耗时: {}ms", duration);
        } catch (Exception e) {
            log.error("同步实时数据和统计数据失败", e);
        } finally {
            isRunning = false;
        }
    }

    /**
     * 从InfluxDB同步实时数据到MySQL实时表
     */
    @SuppressWarnings("deprecation")
    private void syncRealTimeDataFromInfluxDB() {
        log.info("开始同步实时数据...");
        try {
            // 获取当前月份的数据库名
            String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();

            log.info("准备查询数据库: {}", currentMonthDB);

            // 确保数据库存在
            if (!influxDB.databaseExists(currentMonthDB)) {
                log.warn("数据库 {} 不存在，跳过同步", currentMonthDB);
                return;
            }

            // 获取所有启用的模块，用于验证模块ID
            LambdaQueryWrapper<TbModule> moduleQueryWrapper = new LambdaQueryWrapper<>();
            moduleQueryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> activeModules = moduleMapper.selectList(moduleQueryWrapper);

            // 构建模块ID映射表，支持大小写不敏感匹配
            Map<String, TbModule> moduleMap = new HashMap<>();
            for (TbModule module : activeModules) {
                // 使用小写作为key，确保匹配
                moduleMap.put(module.getModuleId().toLowerCase(), module);
            }

            log.info("成功连接到数据库: {}，活跃模块数: {}", currentMonthDB, moduleMap.size());

            // 构建查询命令 - 查询最近5分钟的数据（匹配定时任务频率）
            String command = String.format("SELECT * FROM %s WHERE time > now() - 5m",
                    influxDBConfig.getMeasurement());

            // 减少日志输出
            // log.info("执行查询: {}", command);

            QueryResult queryResult = influxDBService.queryInDatabase(command, currentMonthDB);

            // 解析查询结果
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);

            log.info("查询结果数量: {}", resultList.size());

            if (resultList.isEmpty()) {
                log.info("没有找到数据，跳过同步");
                return;
            }

            // 按模块ID和时间分组处理数据，取每个模块最新的数据
            Map<String, Map<String, Object>> moduleLatestData = new HashMap<>();
            Map<String, Date> moduleLatestTime = new HashMap<>();

            // 处理查询结果
            for (Map<String, Object> data : resultList) {
                String tagname = (String) data.get("tagname");
                Object timeObj = data.get("time");

                if (tagname != null && timeObj != null) {
                    String moduleId = InfluxDBUtil.extractModuleIdFromTagname(tagname);
                    String pointName = InfluxDBUtil.extractPointNameFromTagname(tagname);
                    Object value = data.get("value");

                    if (moduleId == null || pointName == null || value == null) {
                        log.warn("无效的数据点: tagname={}, value={}", tagname, value);
                        continue;
                    }

                    // 检查模块是否存在且启用
                    if (!moduleMap.containsKey(moduleId)) {
                        log.debug("模块不存在或未启用: moduleId={}", moduleId);
                        continue;
                    }

                    // 解析时间
                    Date dataTime = parseInfluxTime(timeObj);
                    if (dataTime == null) {
                        log.warn("无法解析时间: {}", timeObj);
                        continue;
                    }

                    // 检查是否是该模块的最新数据
                    Date latestTime = moduleLatestTime.get(moduleId);
                    if (latestTime == null || dataTime.after(latestTime)) {
                        // 更新最新时间
                        moduleLatestTime.put(moduleId, dataTime);
                        // 重置该模块的数据
                        moduleLatestData.put(moduleId, new HashMap<>());
                    } else if (dataTime.before(latestTime)) {
                        // 跳过较旧的数据
                        continue;
                    }

                    // 收集最新数据的点位值
                    Map<String, Object> pointValues = moduleLatestData.get(moduleId);
                    pointValues.put(pointName, value);

                    // 减少日志输出，只在需要时记录
                    // log.debug("收集数据点: 模块ID={}, 点位={}, 值={}, 时间={}", moduleId, pointName, value, dataTime);
                }
            }

            log.info("共收集了 {} 个模块的最新数据", moduleLatestData.size());

            // 处理每个模块的数据
            for (Map.Entry<String, Map<String, Object>> entry : moduleLatestData.entrySet()) {
                String moduleId = entry.getKey();
                Map<String, Object> pointValues = entry.getValue();

                // 从映射表中获取模块信息
                TbModule module = moduleMap.get(moduleId);
                if (module == null) {
                    log.warn("找不到模块信息: moduleId={}", moduleId);
                    continue;
                }

                // 根据能源类型处理数据
                Integer energyType = module.getEnergyType();
                if (energyType != null && energyType == 1) {
                    // 电力能源
                    syncElectricData(module, pointValues);
                } else if (energyType != null) {
                    // 其他能源类型
                    syncOtherEnergyData(module, pointValues);
                } else {
                    log.warn("模块 {} 没有指定能源类型", moduleId);
                }
            }

            log.info("实时数据同步完成，共处理 {} 个模块的数据", moduleLatestData.size());
        } catch (Exception e) {
            log.error("同步实时数据失败", e);
        }
    }

    /**
     * 实时更新统计表（日、月、年）
     */
    private void updateStatisticsRealTime() {
        log.info("开始实时更新统计表...");
        try {
            Date now = new Date();

            // 更新当日统计
            updateDailyStatisticsRealTime(now);

            // 更新当月统计
            updateMonthlyStatisticsRealTime(now);

            // 更新当年统计
            updateYearlyStatisticsRealTime(now);

            log.info("实时统计表更新完成");
        } catch (Exception e) {
            log.error("实时更新统计表失败", e);
        }
    }

    /**
     * 实时更新当日统计
     */
    private void updateDailyStatisticsRealTime(Date currentDate) {
        try {
            // 获取当天的开始时间
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(currentDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date dayStart = startCal.getTime();

            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);

            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();

                    if (energyType == null) {
                        continue;
                    }

                    // 获取当天开始时的累积值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, dayStart);
                    // 获取当前时间的累积值
                    BigDecimal currentValue = getAccumulateValue(moduleId, energyType, currentDate);

                    // 计算当日能耗
                    BigDecimal dailyConsumption = null;
                    if (startValue != null && currentValue != null) {
                        dailyConsumption = currentValue.subtract(startValue);
                        if (dailyConsumption.compareTo(BigDecimal.ZERO) < 0) {
                            dailyConsumption = currentValue; // 处理重置情况
                        }
                    } else if (currentValue != null) {
                        dailyConsumption = currentValue;
                    }

                    if (dailyConsumption != null) {
                        // 查询或创建当日统计记录
                        TbEpEquEnergyDaycount dayRecord = daycountMapper.selectByModuleIdAndDate(moduleId, currentDate);
                        if (dayRecord != null) {
                            // 更新现有记录
                            dayRecord.setEnergyCount(dailyConsumption);
                            dayRecord.setStratCount(startValue);
                            dayRecord.setEndCount(currentValue);
                            daycountMapper.updateById(dayRecord);
                        } else {
                            // 创建新记录
                            dayRecord = new TbEpEquEnergyDaycount();
                            dayRecord.setModuleId(moduleId);
                            dayRecord.setDt(currentDate);
                            dayRecord.setEnergyCount(dailyConsumption);
                            dayRecord.setStratCount(startValue);
                            dayRecord.setEndCount(currentValue);
                            daycountMapper.insert(dayRecord);
                        }

                        // 减少日志输出
                        // log.debug("更新日统计: moduleId={}, consumption={}", moduleId, dailyConsumption);
                    }
                } catch (Exception e) {
                    log.error("更新模块 {} 的日统计失败", module.getModuleId(), e);
                }
            }
        } catch (Exception e) {
            log.error("实时更新日统计失败", e);
        }
    }

    /**
     * 实时更新当月统计
     */
    private void updateMonthlyStatisticsRealTime(Date currentDate) {
        try {
            // 获取当月的开始时间
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(currentDate);
            startCal.set(Calendar.DAY_OF_MONTH, 1);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date monthStart = startCal.getTime();

            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);

            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();

                    if (energyType == null) {
                        continue;
                    }

                    // 获取当月开始时的累积值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, monthStart);
                    // 获取当前时间的累积值
                    BigDecimal currentValue = getAccumulateValue(moduleId, energyType, currentDate);

                    // 计算当月能耗
                    BigDecimal monthlyConsumption = null;
                    if (startValue != null && currentValue != null) {
                        monthlyConsumption = currentValue.subtract(startValue);
                        if (monthlyConsumption.compareTo(BigDecimal.ZERO) < 0) {
                            monthlyConsumption = currentValue; // 处理重置情况
                        }
                    } else if (currentValue != null) {
                        monthlyConsumption = currentValue;
                    }

                    if (monthlyConsumption != null) {
                        // 查询或创建当月统计记录
                        TbEpEquEnergyMonthcount monthRecord = monthcountMapper.selectByModuleIdAndMonth(moduleId, currentDate);
                        if (monthRecord != null) {
                            // 更新现有记录
                            monthRecord.setEnergyCount(monthlyConsumption);
                            monthRecord.setStratCount(startValue);
                            monthRecord.setEndCount(currentValue);
                            monthcountMapper.updateById(monthRecord);
                        } else {
                            // 创建新记录
                            monthRecord = new TbEpEquEnergyMonthcount();
                            monthRecord.setModuleId(moduleId);
                            monthRecord.setDt(currentDate);
                            monthRecord.setEnergyCount(monthlyConsumption);
                            monthRecord.setStratCount(startValue);
                            monthRecord.setEndCount(currentValue);
                            monthcountMapper.insert(monthRecord);
                        }

                        // 减少日志输出
                        // log.debug("更新月统计: moduleId={}, consumption={}", moduleId, monthlyConsumption);
                    }
                } catch (Exception e) {
                    log.error("更新模块 {} 的月统计失败", module.getModuleId(), e);
                }
            }
        } catch (Exception e) {
            log.error("实时更新月统计失败", e);
        }
    }

    /**
     * 实时更新当年统计
     */
    private void updateYearlyStatisticsRealTime(Date currentDate) {
        try {
            // 获取当年的开始时间
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(currentDate);
            startCal.set(Calendar.MONTH, Calendar.JANUARY);
            startCal.set(Calendar.DAY_OF_MONTH, 1);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date yearStart = startCal.getTime();

            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);

            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();

                    if (energyType == null) {
                        continue;
                    }

                    // 获取当年开始时的累积值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, yearStart);
                    // 获取当前时间的累积值
                    BigDecimal currentValue = getAccumulateValue(moduleId, energyType, currentDate);

                    // 计算当年能耗
                    BigDecimal yearlyConsumption = null;
                    if (startValue != null && currentValue != null) {
                        yearlyConsumption = currentValue.subtract(startValue);
                        if (yearlyConsumption.compareTo(BigDecimal.ZERO) < 0) {
                            yearlyConsumption = currentValue; // 处理重置情况
                        }
                    } else if (currentValue != null) {
                        yearlyConsumption = currentValue;
                    }

                    if (yearlyConsumption != null) {
                        // 查询或创建当年统计记录
                        TbEpEquEnergyYearcount yearRecord = yearcountMapper.selectByModuleIdAndYear(moduleId, currentDate);
                        if (yearRecord != null) {
                            // 更新现有记录
                            yearRecord.setEnergyCount(yearlyConsumption);
                            yearRecord.setStratCount(startValue);
                            yearRecord.setEndCount(currentValue);
                            yearcountMapper.updateById(yearRecord);
                        } else {
                            // 创建新记录
                            yearRecord = new TbEpEquEnergyYearcount();
                            yearRecord.setModuleId(moduleId);
                            yearRecord.setDt(currentDate);
                            yearRecord.setEnergyCount(yearlyConsumption);
                            yearRecord.setStratCount(startValue);
                            yearRecord.setEndCount(currentValue);
                            yearcountMapper.insert(yearRecord);
                        }

                        // 减少日志输出
                        // log.debug("更新年统计: moduleId={}, consumption={}", moduleId, yearlyConsumption);
                    }
                } catch (Exception e) {
                    log.error("更新模块 {} 的年统计失败", module.getModuleId(), e);
                }
            }
        } catch (Exception e) {
            log.error("实时更新年统计失败", e);
        }
    }
    /**
     * 解析InfluxDB时间格式并调整时区
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
                    calendar.add(Calendar.HOUR, TIME_ZONE_OFFSET_HOURS);
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
            log.error("解析时间失败: {}, 尝试使用备用解析方法", timeObj, e);
            // 备用解析方法
            return parseTimeWithFallback(timeObj);
        }
    }

    /**
     * 备用时间解析方法
     */
    private Date parseTimeWithFallback(Object timeObj) {
        if (timeObj == null) {
            return null;
        }

        try {
            String timeStr = timeObj.toString();

            // 尝试多种常见的时间格式
            String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss"
            };

            for (String pattern : patterns) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    if (pattern.endsWith("'Z'")) {
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date utcDate = format.parse(timeStr);
                        // 转换为本地时间
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(utcDate);
                        calendar.add(Calendar.HOUR, TIME_ZONE_OFFSET_HOURS);
                        return calendar.getTime();
                    } else {
                        return format.parse(timeStr);
                    }
                } catch (ParseException ignored) {
                    // 继续尝试下一个格式
                }
            }

            log.warn("所有时间格式解析都失败，时间字符串: {}", timeStr);
            return null;
        } catch (Exception e) {
            log.error("备用时间解析也失败: {}", timeObj, e);
            return null;
        }
    }

    /**
     * 将本地时间转换为UTC时间（用于查询InfluxDB）
     */
    private Date convertToUTC(Date localTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(localTime);
        calendar.add(Calendar.HOUR, -TIME_ZONE_OFFSET_HOURS);
        return calendar.getTime();
    }

    /**
     * 将UTC时间转换为本地时间
     * 注意：此方法保留供将来使用
     */
    @SuppressWarnings("unused")
    private Date convertToLocal(Date utcTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utcTime);
        calendar.add(Calendar.HOUR, TIME_ZONE_OFFSET_HOURS);
        return calendar.getTime();
    }

    /**
     * 同步电力数据 - 实时表更新操作（每个module_ID只有一条数据）
     */
    private void syncElectricData(TbModule module, Map<String, Object> pointValues) {
        String moduleId = module.getModuleId();
        log.info("同步电力数据: moduleId={}, 点位数量={}", moduleId, pointValues.size());
        log.info("点位数据: {}", pointValues);

        try {
            // 先查询是否已存在该模块的数据
            TbEquEleData existingData = equEleDataMapper.selectLatestDataByModuleId(moduleId);

            TbEquEleData eleData;
            boolean isUpdate = false;

            if (existingData != null) {
                // 存在数据，进行更新操作
                eleData = existingData;
                isUpdate = true;
                log.info("找到现有电力数据记录，将进行更新操作: moduleId={}, id={}", moduleId, existingData.getId());
            } else {
                // 不存在数据，创建新记录
                eleData = new TbEquEleData();
                eleData.setModuleId(moduleId);
                log.info("未找到现有电力数据记录，将进行插入操作: moduleId={}", moduleId);
            }

            // 更新采集时间
            eleData.setEquElectricDT(new Date());

            // 根据实际数据设置字段值
            for (Map.Entry<String, Object> entry : pointValues.entrySet()) {
                String pointName = entry.getKey();
                Object value = entry.getValue();
                BigDecimal decimalValue = toBigDecimal(value);

                if (decimalValue == null) {
                    continue;
                }

                // 根据点位名称设置对应字段
                switch (pointName.toUpperCase()) {
                    case "UA":
                        eleData.setUA(decimalValue);
                        break;
                    case "UB":
                        eleData.setUB(decimalValue);
                        break;
                    case "UC":
                        eleData.setUC(decimalValue);
                        break;
                    case "IA":
                        eleData.setIA(decimalValue);
                        break;
                    case "IB":
                        eleData.setIB(decimalValue);
                        break;
                    case "IC":
                        eleData.setIC(decimalValue);
                        break;
                    case "PP":
                    case "P":
                        eleData.setPp(decimalValue);
                        break;
                    case "QQ":
                    case "Q":
                        eleData.setQq(decimalValue);
                        break;
                    case "SS":
                    case "S":
                        eleData.setSs(decimalValue);
                        break;
                    case "PFS":
                    case "PFSC":
                        eleData.setPFS(decimalValue);
                        break;
                    case "HZ":
                        eleData.setHZ(decimalValue);
                        break;
                    case "KWH":
                    case "KWH3":
                        eleData.setKWH(decimalValue);
                        break;
                    case "KVARH":
                        eleData.setKVARH(decimalValue);
                        break;
                    case "PA":
                        eleData.setPa(decimalValue);
                        break;
                    case "PB":
                        eleData.setPb(decimalValue);
                        break;
                    case "PC":
                        eleData.setPc(decimalValue);
                        break;
                    case "PFA":
                        eleData.setPFa(decimalValue);
                        break;
                    case "PFB":
                        eleData.setPFb(decimalValue);
                        break;
                    case "PFC":
                        eleData.setPFc(decimalValue);
                        break;
                    default:
                        log.debug("未识别的电力点位: {}", pointName);
                        break;
                }
            }

            // 检查是否有足够的数据
            if (eleData.getUA() == null && eleData.getUB() == null && eleData.getUC() == null &&
                    eleData.getIA() == null && eleData.getIB() == null && eleData.getIC() == null &&
                    eleData.getPp() == null && eleData.getQq() == null && eleData.getSs() == null &&
                    eleData.getPFS() == null && eleData.getHZ() == null && eleData.getKWH() == null &&
                    eleData.getKVARH() == null) {
                log.warn("电力数据字段为空，跳过保存: moduleId={}", moduleId);
                return;
            }

            // 执行插入或更新操作
            int result;
            if (isUpdate) {
                result = equEleDataMapper.updateById(eleData);
                log.info("更新电力数据结果: moduleId={}, result={}, 有效字段数={}", moduleId, result, countNonNullFields(eleData));
            } else {
                result = equEleDataMapper.insert(eleData);
                log.info("插入电力数据结果: moduleId={}, result={}, 有效字段数={}", moduleId, result, countNonNullFields(eleData));
            }

        } catch (Exception e) {
            log.error("同步电力数据失败: moduleId={}", moduleId, e);
        }
    }

    /**
     * 统计非空字段数量
     */
    private int countNonNullFields(TbEquEleData eleData) {
        int count = 0;
        if (eleData.getUA() != null) count++;
        if (eleData.getUB() != null) count++;
        if (eleData.getUC() != null) count++;
        if (eleData.getIA() != null) count++;
        if (eleData.getIB() != null) count++;
        if (eleData.getIC() != null) count++;
        if (eleData.getPp() != null) count++;
        if (eleData.getQq() != null) count++;
        if (eleData.getSs() != null) count++;
        if (eleData.getPFS() != null) count++;
        if (eleData.getHZ() != null) count++;
        if (eleData.getKWH() != null) count++;
        if (eleData.getKVARH() != null) count++;
        return count;
    }

    /**
     * 同步其他能源数据 - 实时表更新操作（每个module_ID只有一条数据）
     * @param module 模块信息
     * @param pointValues 点位数据
     */
    private void syncOtherEnergyData(TbModule module, Map<String, Object> pointValues) {
        String moduleId = module.getModuleId();
        log.info("同步其他能源数据: moduleId={}, 点位数量={}", moduleId, pointValues.size());
        log.info("点位数据: {}", pointValues);

        try {
            // 先查询是否已存在该模块的数据
            TbEquEnergyData existingData = equEnergyDataMapper.selectLatestDataByModuleId(moduleId);

            TbEquEnergyData energyData;
            boolean isUpdate = false;

            if (existingData != null) {
                // 存在数据，进行更新操作
                energyData = existingData;
                isUpdate = true;
                log.info("找到现有能源数据记录，将进行更新操作: moduleId={}, id={}", moduleId, existingData.getId());
            } else {
                // 不存在数据，创建新记录
                energyData = new TbEquEnergyData();
                energyData.setModuleId(moduleId);
                log.info("未找到现有能源数据记录，将进行插入操作: moduleId={}", moduleId);
            }

            // 更新采集时间
            energyData.setEquEnergyDt(new Date());

            // 设置各个字段的值 - 尝试不同的大小写组合
            // TEMPERATURE字段
            if (pointValues.containsKey("TEMPERATURE")) {
                energyData.setEnergyTemperature(toBigDecimal(pointValues.get("TEMPERATURE")));
                log.info("设置TEMPERATURE={}", pointValues.get("TEMPERATURE"));
            } else if (pointValues.containsKey("temperature")) {
                energyData.setEnergyTemperature(toBigDecimal(pointValues.get("temperature")));
                log.info("设置TEMPERATURE={}", pointValues.get("temperature"));
            } else if (pointValues.containsKey("TEMP")) {
                energyData.setEnergyTemperature(toBigDecimal(pointValues.get("TEMP")));
                log.info("设置TEMPERATURE={}", pointValues.get("TEMP"));
            } else if (pointValues.containsKey("temp")) {
                energyData.setEnergyTemperature(toBigDecimal(pointValues.get("temp")));
                log.info("设置TEMPERATURE={}", pointValues.get("temp"));
            }

            // PRESSURE字段
            if (pointValues.containsKey("PRESSURE")) {
                energyData.setEnergyPressure(toBigDecimal(pointValues.get("PRESSURE")));
                log.info("设置PRESSURE={}", pointValues.get("PRESSURE"));
            } else if (pointValues.containsKey("pressure")) {
                energyData.setEnergyPressure(toBigDecimal(pointValues.get("pressure")));
                log.info("设置PRESSURE={}", pointValues.get("pressure"));
            }

            // WINKVALUE字段
            if (pointValues.containsKey("WINKVALUE")) {
                energyData.setEnergyWinkvalue(toBigDecimal(pointValues.get("WINKVALUE")));
                log.info("设置WINKVALUE={}", pointValues.get("WINKVALUE"));
            } else if (pointValues.containsKey("winkvalue")) {
                energyData.setEnergyWinkvalue(toBigDecimal(pointValues.get("winkvalue")));
                log.info("设置WINKVALUE={}", pointValues.get("winkvalue"));
            }

            // ACCUMULATEVALUE字段
            if (pointValues.containsKey("ACCUMULATEVALUE")) {
                energyData.setEnergyAccumulatevalue(toBigDecimal(pointValues.get("ACCUMULATEVALUE")));
                log.info("设置ACCUMULATEVALUE={}", pointValues.get("ACCUMULATEVALUE"));
            } else if (pointValues.containsKey("accumulatevalue")) {
                energyData.setEnergyAccumulatevalue(toBigDecimal(pointValues.get("accumulatevalue")));
                log.info("设置ACCUMULATEVALUE={}", pointValues.get("accumulatevalue"));
            }

            // 检查是否有足够的数据
            if (energyData.getEnergyTemperature() == null && energyData.getEnergyPressure() == null &&
                energyData.getEnergyWinkvalue() == null && energyData.getEnergyAccumulatevalue() == null) {
                log.warn("能源数据字段为空，跳过保存: moduleId={}", moduleId);
                return;
            }

            // 执行插入或更新操作
            int result;
            if (isUpdate) {
                result = equEnergyDataMapper.updateById(energyData);
                log.info("更新能源数据结果: moduleId={}, result={}", moduleId, result);
            } else {
                result = equEnergyDataMapper.insert(energyData);
                log.info("插入能源数据结果: moduleId={}, result={}", moduleId, result);
            }

        } catch (Exception e) {
            log.error("同步其他能源数据失败: moduleId={}", moduleId, e);
        }
    }
    
    /**
     * 将对象转换为BigDecimal
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法将字符串转换为BigDecimal: {}", value);
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 每天凌晨1点执行，统计前一天的日能耗
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncDailyEnergyData() {
        log.info("开始统计日能耗数据...");
        try {
            // 获取昨天的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar.getTime();
            // 用于日志记录
            log.info("开始统计 {} 的日能耗数据", dateFormat.format(yesterday));
            
            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);
            
            log.info("开始处理 {} 个模块的日统计", modules.size());
            
            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();
                    
                    // 根据能源类型获取累积值字段
                    String accumulateField = getAccumulateField(energyType);
                    if (accumulateField == null) {
                        log.warn("模块 {} 未配置累积值字段，跳过统计", moduleId);
                        continue;
                    }
                    
                    // 获取昨天开始时间和结束时间的累积值
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(yesterday);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(yesterday);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    
                    // 查询开始值和结束值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, startCal.getTime());
                    BigDecimal endValue = getAccumulateValue(moduleId, energyType, endCal.getTime());
                    
                    log.info("模块 {} 日统计: 开始值={}, 结束值={}", moduleId, startValue, endValue);
                    
                    // 计算能耗值
                    BigDecimal energyCount = null;
                    if (startValue != null && endValue != null) {
                        energyCount = endValue.subtract(startValue);
                        // 如果结果为负数，说明可能有重置，使用结束值作为能耗值
                        if (energyCount.compareTo(BigDecimal.ZERO) < 0) {
                            energyCount = endValue;
                        }
                    } else if (endValue != null) {
                        energyCount = endValue;
                    }
                    
                    // 检查是否已存在记录
                    TbEpEquEnergyDaycount existingRecord = daycountMapper.selectByModuleIdAndDate(moduleId, yesterday);
                    
                    if (existingRecord != null) {
                        // 更新现有记录
                        existingRecord.setEnergyCount(energyCount);
                        existingRecord.setStratCount(startValue);
                        existingRecord.setEndCount(endValue);
                        daycountMapper.updateById(existingRecord);
                        log.info("更新日统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    } else {
                        // 创建新记录
                        TbEpEquEnergyDaycount daycount = new TbEpEquEnergyDaycount();
                        daycount.setModuleId(moduleId);
                        daycount.setDt(yesterday);
                        daycount.setEnergyCount(energyCount);
                        daycount.setStratCount(startValue);
                        daycount.setEndCount(endValue);
                        daycountMapper.insert(daycount);
                        log.info("创建日统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    }
                    
                } catch (Exception e) {
                    log.error("处理模块 {} 日统计失败", module.getModuleId(), e);
                }
            }
            
            log.info("日能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计日能耗数据失败", e);
        }
    }
    
    /**
     * 每月1号凌晨2点执行，统计上个月的月能耗
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void syncMonthlyEnergyData() {
        log.info("开始统计月能耗数据...");
        try {
            // 获取上个月的第一天和最后一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date firstDayOfLastMonth = calendar.getTime();
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date lastDayOfLastMonth = calendar.getTime();
            
            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);
            
            log.info("开始处理 {} 个模块的月统计", modules.size());
            
            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();
                    
                    // 根据能源类型获取累积值字段
                    String accumulateField = getAccumulateField(energyType);
                    if (accumulateField == null) {
                        log.warn("模块 {} 未配置累积值字段，跳过统计", moduleId);
                        continue;
                    }
                    
                    // 获取月初和月末的累积值
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(firstDayOfLastMonth);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(lastDayOfLastMonth);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    
                    // 查询开始值和结束值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, startCal.getTime());
                    BigDecimal endValue = getAccumulateValue(moduleId, energyType, endCal.getTime());
                    
                    log.info("模块 {} 月统计: 开始值={}, 结束值={}", moduleId, startValue, endValue);
                    
                    // 计算能耗值
                    BigDecimal energyCount = null;
                    if (startValue != null && endValue != null) {
                        energyCount = endValue.subtract(startValue);
                        // 如果结果为负数，说明可能有重置，使用结束值作为能耗值
                        if (energyCount.compareTo(BigDecimal.ZERO) < 0) {
                            energyCount = endValue;
                        }
                    } else if (endValue != null) {
                        energyCount = endValue;
                    }
                    
                    // 检查是否已存在记录
                    TbEpEquEnergyMonthcount existingRecord = monthcountMapper.selectByModuleIdAndMonth(moduleId, firstDayOfLastMonth);
                    
                    if (existingRecord != null) {
                        // 更新现有记录
                        existingRecord.setEnergyCount(energyCount);
                        existingRecord.setStratCount(startValue);
                        existingRecord.setEndCount(endValue);
                        monthcountMapper.updateById(existingRecord);
                        log.info("更新月统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    } else {
                        // 创建新记录
                        TbEpEquEnergyMonthcount monthcount = new TbEpEquEnergyMonthcount();
                        monthcount.setModuleId(moduleId);
                        monthcount.setDt(firstDayOfLastMonth);
                        monthcount.setEnergyCount(energyCount);
                        monthcount.setStratCount(startValue);
                        monthcount.setEndCount(endValue);
                        monthcountMapper.insert(monthcount);
                        log.info("创建月统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    }
                    
                } catch (Exception e) {
                    log.error("处理模块 {} 月统计失败", module.getModuleId(), e);
                }
            }
            
            log.info("月能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计月能耗数据失败", e);
        }
    }
    
    /**
     * 每年1月1日凌晨3点执行，统计上一年的年能耗
     */
    @Scheduled(cron = "0 0 3 1 1 ?")
    public void syncYearlyEnergyData() {
        log.info("开始统计年能耗数据...");
        try {
            // 获取去年的第一天和最后一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date firstDayOfLastYear = calendar.getTime();
            
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            Date lastDayOfLastYear = calendar.getTime();
            
            // 获取所有启用的模块
            LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbModule::getIsaction, "Y");
            List<TbModule> modules = moduleMapper.selectList(queryWrapper);
            
            log.info("开始处理 {} 个模块的年统计", modules.size());
            
            for (TbModule module : modules) {
                try {
                    String moduleId = module.getModuleId();
                    Integer energyType = module.getEnergyType();
                    
                    // 根据能源类型获取累积值字段
                    String accumulateField = getAccumulateField(energyType);
                    if (accumulateField == null) {
                        log.warn("模块 {} 未配置累积值字段，跳过统计", moduleId);
                        continue;
                    }
                    
                    // 获取年初和年末的累积值
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(firstDayOfLastYear);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(lastDayOfLastYear);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    
                    // 查询开始值和结束值
                    BigDecimal startValue = getAccumulateValue(moduleId, energyType, startCal.getTime());
                    BigDecimal endValue = getAccumulateValue(moduleId, energyType, endCal.getTime());
                    
                    log.info("模块 {} 年统计: 开始值={}, 结束值={}", moduleId, startValue, endValue);
                    
                    // 计算能耗值
                    BigDecimal energyCount = null;
                    if (startValue != null && endValue != null) {
                        energyCount = endValue.subtract(startValue);
                        // 如果结果为负数，说明可能有重置，使用结束值作为能耗值
                        if (energyCount.compareTo(BigDecimal.ZERO) < 0) {
                            energyCount = endValue;
                        }
                    } else if (endValue != null) {
                        energyCount = endValue;
                    }
                    
                    // 检查是否已存在记录
                    TbEpEquEnergyYearcount existingRecord = yearcountMapper.selectByModuleIdAndYear(moduleId, firstDayOfLastYear);
                    
                    if (existingRecord != null) {
                        // 更新现有记录
                        existingRecord.setEnergyCount(energyCount);
                        existingRecord.setStratCount(startValue);
                        existingRecord.setEndCount(endValue);
                        yearcountMapper.updateById(existingRecord);
                        log.info("更新年统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    } else {
                        // 创建新记录
                        TbEpEquEnergyYearcount yearcount = new TbEpEquEnergyYearcount();
                        yearcount.setModuleId(moduleId);
                        yearcount.setDt(firstDayOfLastYear);
                        yearcount.setEnergyCount(energyCount);
                        yearcount.setStratCount(startValue);
                        yearcount.setEndCount(endValue);
                        yearcountMapper.insert(yearcount);
                        log.info("创建年统计记录: moduleId={}, energyCount={}", moduleId, energyCount);
                    }
                    
                } catch (Exception e) {
                    log.error("处理模块 {} 年统计失败", module.getModuleId(), e);
                }
            }
            
            log.info("年能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计年能耗数据失败", e);
        }
    }
    
    /**
     * 根据能源类型获取累积值字段名
     * @param energyType 能源类型
     * @return 累积值字段名
     */
    private String getAccumulateField(Integer energyType) {
        if (energyType == null) {
            return null;
        }
        
        switch (energyType) {
            case 1: // 电力
                return "KWH"; // 电能累积值
            case 2: // 天然气
            case 3: // 压缩空气
            case 4: // 企业用水
            default:
                return "energy_accumulatevalue"; // 其他能源累积值
        }
    }

    /**
     * 获取指定时间的累积值 - 支持跨月查询
     */
    private BigDecimal getAccumulateValue(String moduleId, Integer energyType, Date time) {
        try {
            if (energyType == null) {
                return null;
            }

            if (energyType == 1) {
                // 电力数据 - 先从MySQL查询
                LambdaQueryWrapper<TbEquEleData> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TbEquEleData::getModuleId, moduleId)
                        .le(TbEquEleData::getEquElectricDT, time)
                        .orderByDesc(TbEquEleData::getEquElectricDT)
                        .last("LIMIT 1");

                TbEquEleData eleData = equEleDataMapper.selectOne(queryWrapper);
                if (eleData != null && eleData.getKWH() != null) {
                    return eleData.getKWH();
                }

                // 如果MySQL中没有数据，从InfluxDB查询
                return getAccumulateValueFromInfluxDB(moduleId, "KWH", time);
            } else {
                // 其他能源数据
                LambdaQueryWrapper<TbEquEnergyData> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TbEquEnergyData::getModuleId, moduleId)
                        .le(TbEquEnergyData::getEquEnergyDt, time)
                        .orderByDesc(TbEquEnergyData::getEquEnergyDt)
                        .last("LIMIT 1");

                TbEquEnergyData energyData = equEnergyDataMapper.selectOne(queryWrapper);
                if (energyData != null && energyData.getEnergyAccumulatevalue() != null) {
                    return energyData.getEnergyAccumulatevalue();
                }

                // 如果MySQL中没有数据，从InfluxDB查询
                return getAccumulateValueFromInfluxDB(moduleId, "ACCUMULATEVALUE", time);
            }
        } catch (Exception e) {
            log.error("获取模块 {} 在时间 {} 的累积值失败", moduleId, time, e);
            return null;
        }
    }
    /**
     * 从InfluxDB获取累积值 - 支持大小写不敏感查询
     */
    @SuppressWarnings("deprecation")
    private BigDecimal getAccumulateValueFromInfluxDB(String moduleId, String pointName, Date time) {
        try {
            // 确定时间所在的月份数据库
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            String dbName = influxDBConfig.getDatabaseName(year, month);

            if (!influxDB.databaseExists(dbName)) {
                log.warn("数据库 {} 不存在", dbName);
                return null;
            }

            // 构建tagname - 使用工具方法确保格式正确
            String tagnamePattern = InfluxDBUtil.buildInfluxTagname(moduleId, pointName);

            // 调整时间到UTC（InfluxDB使用UTC时间）
            Date utcTime = convertToUTC(time);
            String timeStr = dateTimeFormat.format(utcTime);

            String command = String.format(
                    "SELECT * FROM %s WHERE tagname = '%s' AND time <= '%s' ORDER BY time DESC LIMIT 1",
                    influxDBConfig.getMeasurement(), tagnamePattern, timeStr);

            log.debug("查询InfluxDB累积值: {}", command);

            QueryResult queryResult = influxDBService.queryInDatabase(command, dbName);
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);

            if (!resultList.isEmpty()) {
                Object value = resultList.get(0).get("value");
                BigDecimal result = toBigDecimal(value);
                log.debug("从InfluxDB获取累积值成功: moduleId={}, pointName={}, value={}", moduleId, pointName, result);
                return result;
            }

            log.debug("从InfluxDB未找到累积值: moduleId={}, pointName={}, time={}", moduleId, pointName, time);
            return null;
        } catch (Exception e) {
            log.error("从InfluxDB获取累积值失败: moduleId={}, pointName={}, time={}", moduleId, pointName, time, e);
            return null;
        }
    }

    /**
     * 查询指定模块在指定时间范围内的数据 - 支持大小写不敏感
     */
    @SuppressWarnings("deprecation")
    public QueryResult queryModuleDataInTimeRange(String moduleId, String startTime, String endTime) {
        try {
            // 解析时间范围，确定需要查询的数据库
            LocalDate startDate = LocalDate.parse(startTime.substring(0, 10));
            LocalDate endDate = LocalDate.parse(endTime.substring(0, 10));

            List<QueryResult> results = new ArrayList<>();

            // 遍历时间范围内的每个月
            LocalDate currentDate = startDate.withDayOfMonth(1);
            while (!currentDate.isAfter(endDate)) {
                String dbName = influxDBConfig.getDatabaseName(currentDate.getYear(), currentDate.getMonthValue());

                if (!influxDB.databaseExists(dbName)) {
                    log.debug("数据库 {} 不存在，跳过", dbName);
                    currentDate = currentDate.plusMonths(1);
                    continue;
                }

                // 构建查询模式
                String modulePattern = InfluxDBUtil.buildInfluxModulePattern(moduleId);

                String command = String.format(
                        "SELECT * FROM %s WHERE tagname =~ /^%s/ AND time >= '%s' AND time <= '%s' ORDER BY time ASC",
                        influxDBConfig.getMeasurement(),
                        modulePattern.replace(".*", ".*"),
                        startTime, endTime);

                QueryResult result = influxDBService.queryInDatabase(command, dbName);
                if (result != null && result.getResults() != null && !result.getResults().isEmpty()) {
                    results.add(result);
                }

                currentDate = currentDate.plusMonths(1);
            }

            // 合并结果
            return mergeQueryResults(results);

        } catch (Exception e) {
            log.error("查询模块数据失败: moduleId={}, startTime={}, endTime={}", moduleId, startTime, endTime, e);
            return new QueryResult();
        }
    }

    /**
     * 合并多个QueryResult
     */
    private QueryResult mergeQueryResults(List<QueryResult> results) {
        QueryResult mergedResult = new QueryResult();
        List<QueryResult.Result> mergedResults = new ArrayList<>();

        for (QueryResult result : results) {
            if (result.getResults() != null) {
                mergedResults.addAll(result.getResults());
            }
        }

        mergedResult.setResults(mergedResults);
        return mergedResult;
    }

    /**
     * 验证并标准化模块ID
     * @param moduleId 输入的模块ID
     * @return 标准化后的模块ID（小写）
     */
    public String normalizeModuleId(String moduleId) {
        if (moduleId == null || moduleId.trim().isEmpty()) {
            return null;
        }
        return moduleId.trim().toLowerCase();
    }

    /**
     * 检查模块ID是否匹配（大小写不敏感）
     * @param moduleId1 模块ID1
     * @param moduleId2 模块ID2
     * @return 是否匹配
     */
    public boolean isModuleIdMatch(String moduleId1, String moduleId2) {
        if (moduleId1 == null || moduleId2 == null) {
            return false;
        }
        return moduleId1.toLowerCase().equals(moduleId2.toLowerCase());
    }

    /**
     * 获取模块的InfluxDB标签名模式
     * @param moduleId MySQL中的模块ID
     * @return InfluxDB标签名的正则表达式模式
     */
    public String getInfluxTagPattern(String moduleId) {
        if (moduleId == null) {
            return null;
        }
        // 转换为大写并创建正则表达式模式
        return "^" + moduleId.toUpperCase() + "#.*";
    }

    /**
     * 每小时执行一次，统计小时数据
     */
    @Scheduled(cron = "0 1 * * * ?")
    @SuppressWarnings("deprecation")
    public void syncHourlyData() {
        log.info("开始统计小时数据...");
        try {
            // 获取上一个小时的开始和结束时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            
            // 设置为小时开始时间
            Calendar hourStartCal = (Calendar) calendar.clone();
            hourStartCal.set(Calendar.MINUTE, 0);
            hourStartCal.set(Calendar.SECOND, 0);
            hourStartCal.set(Calendar.MILLISECOND, 0);
            Date hourStart = hourStartCal.getTime();
            
            // 设置为小时结束时间
            Calendar hourEndCal = (Calendar) calendar.clone();
            hourEndCal.set(Calendar.MINUTE, 59);
            hourEndCal.set(Calendar.SECOND, 59);
            hourEndCal.set(Calendar.MILLISECOND, 999);
            Date hourEnd = hourEndCal.getTime();

            String hourStartStr = dateTimeFormat.format(hourStart);
            // 记录时间范围用于日志
            log.debug("小时统计时间范围: {} - {}", hourStartStr, dateTimeFormat.format(hourEnd));
            
            // 获取当前小时所在月份的数据库名
            LocalDate hourDate = LocalDate.parse(hourStartStr.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dbName = influxDBConfig.getDatabaseName(hourDate.getYear(), hourDate.getMonthValue());
            
            log.info("准备查询数据库: {}", dbName);
            
            // 确保数据库存在
            if (!influxDB.databaseExists(dbName)) {
                log.warn("数据库 {} 不存在，跳过统计", dbName);
                return;
            }
            
            log.info("成功连接到数据库: {}", dbName);
            
            // 调整时区差异（InfluxDB使用UTC时间，系统使用东八区时间）
            Date utcStartTime = convertToUTC(hourStart);
            Date utcEndTime = convertToUTC(hourEnd);
            String adjustedStartTime = dateTimeFormat.format(utcStartTime);
            String adjustedEndTime = dateTimeFormat.format(utcEndTime);

            log.info("查询时间范围（本地）: {} - {}", dateTimeFormat.format(hourStart), dateTimeFormat.format(hourEnd));
            log.info("查询时间范围（UTC）: {} - {}", adjustedStartTime, adjustedEndTime);
            
            // 查询InfluxDB中的数据，按模块ID分组计算平均值
            String command = String.format(
                    "SELECT mean(value) FROM %s WHERE time >= '%s' AND time <= '%s' GROUP BY tagname",
                    influxDBConfig.getMeasurement(), adjustedStartTime, adjustedEndTime);
            
            log.info("执行查询: {}", command);
            
            QueryResult queryResult = influxDBService.queryInDatabase(command, dbName);
            
            // 解析查询结果
            List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
            
            // 处理查询结果
            for (Map<String, Object> data : resultList) {
                String tagname = (String) data.get("tagname");
                if (tagname != null) {
                    String moduleId = InfluxDBUtil.extractModuleIdFromTagname(tagname);
                    Object meanValue = data.get("mean");
                    
                    // 这里可以更新MySQL中的小时统计表（如果有的话）
                    log.info("模块ID: {}, 小时平均值: {}", moduleId, meanValue);
                    
                    // 注意：如果需要小时统计表，可以在这里实现相关逻辑
                }
            }
            
            log.info("小时数据统计完成，共处理 {} 条数据", resultList.size());
        } catch (Exception e) {
            log.error("统计小时数据失败", e);
        }
    }
}