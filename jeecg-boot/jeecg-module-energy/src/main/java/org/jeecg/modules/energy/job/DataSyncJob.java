package org.jeecg.modules.energy.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.energy.config.InfluxDBConfig;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.jeecg.modules.energy.entity.TbEpEquEnergyMonthcount;
import org.jeecg.modules.energy.entity.TbEpEquEnergyYearcount;
import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyMonthcountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyYearcountMapper;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IInfluxDBService;
import org.jeecg.modules.energy.util.InfluxDBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据同步定时任务
 * 从InfluxDB同步数据到MySQL
 */
@Slf4j
@Component
public class DataSyncJob {
    
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
    
    /**
     * 每5分钟同步一次实时数据
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncRealTimeData() {
        log.info("开始同步实时数据...");
        try {
            // 获取当前时间
            Date now = new Date();
            String endTime = dateTimeFormat.format(now);
            
            // 获取5分钟前的时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, -5);
            Date fiveMinutesAgo = calendar.getTime();
            String startTime = dateTimeFormat.format(fiveMinutesAgo);
            
            // 获取当前月份的数据库名
            String currentMonthDB = influxDBConfig.getCurrentMonthDatabaseName();
            log.info("当前月份数据库名: {}", currentMonthDB);
            
            // 检查数据库是否存在
            if (!influxDB.databaseExists(currentMonthDB)) {
                log.warn("数据库 {} 不存在，跳过同步", currentMonthDB);
                return;
            }
            
            log.info("成功连接到数据库: {}", currentMonthDB);
            
            // 获取所有模块
            List<TbModule> moduleList = moduleMapper.selectList(null);
            log.info("获取到 {} 个模块", moduleList.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (TbModule module : moduleList) {
                String moduleId = module.getModuleId();
                Integer energyType = module.getEnergyType();
                
                log.info("处理模块: {}, 能源类型: {}", moduleId, energyType);
                
                try {
                    // 查询InfluxDB中的数据
                    QueryResult queryResult = influxDBService.queryByModuleIdAndTimeRange(moduleId, startTime, endTime);
                    List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
                    
                    // 处理查询结果
                    if (resultList.isEmpty()) {
                        log.info("模块 {} 没有数据", moduleId);
                        continue;
                    }
                    
                    log.info("模块 {} 获取到 {} 条数据", moduleId, resultList.size());
                    
                    // 根据能源类型处理数据
                    if (energyType != null && energyType == 1) {
                        // 电力能源
                        syncElectricData(module, resultList);
                        successCount++;
                    } else if (energyType != null) {
                        // 其他能源类型
                        syncOtherEnergyData(module, resultList);
                        successCount++;
                    } else {
                        log.warn("模块 {} 没有指定能源类型", moduleId);
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("处理模块 {} 数据失败", moduleId, e);
                    failCount++;
                }
            }
            
            // 同步完实时数据后，同步更新日、月、年统计数据
            syncDailyEnergyData();
            syncMonthlyEnergyData();
            syncYearlyEnergyData();
            
            log.info("实时数据同步完成，成功: {}，失败: {}", successCount, failCount);
        } catch (Exception e) {
            log.error("同步实时数据失败", e);
        }
    }
    
    /**
     * 同步电力数据
     * @param module 模块信息
     * @param resultList 查询结果
     */
    private void syncElectricData(TbModule module, List<Map<String, Object>> resultList) {
        String moduleId = module.getModuleId();
        log.info("同步电力数据: moduleId={}", moduleId);
        
        try {
            // 创建电力数据对象
            TbEquEleData eleData = new TbEquEleData();
            eleData.setModuleId(moduleId);
            eleData.setEquElectricDT(new Date());
            
            // 处理各个点位的数据
            Map<String, Object> pointValues = new HashMap<>();
            for (Map<String, Object> data : resultList) {
                String tagname = (String) data.get("tagname");
                if (tagname != null) {
                    String pointName = InfluxDBUtil.extractPointNameFromTagname(tagname);
                    Object value = data.get("value");
                    
                    if (value != null) {
                        pointValues.put(pointName, value);
                        log.debug("收集数据点: 模块ID={}, 点位={}, 值={}", moduleId, pointName, value);
                    }
                }
            }
            
            log.info("模块 {} 收集了 {} 个数据点", moduleId, pointValues.size());
            
            // 设置各个字段的值
            if (pointValues.containsKey("UA")) {
                eleData.setUA(toBigDecimal(pointValues.get("UA")));
            }
            if (pointValues.containsKey("UB")) {
                eleData.setUB(toBigDecimal(pointValues.get("UB")));
            }
            if (pointValues.containsKey("UC")) {
                eleData.setUC(toBigDecimal(pointValues.get("UC")));
            }
            if (pointValues.containsKey("IA")) {
                eleData.setIA(toBigDecimal(pointValues.get("IA")));
            }
            if (pointValues.containsKey("IB")) {
                eleData.setIB(toBigDecimal(pointValues.get("IB")));
            }
            if (pointValues.containsKey("IC")) {
                eleData.setIC(toBigDecimal(pointValues.get("IC")));
            }
            if (pointValues.containsKey("PP")) {
                eleData.setPp(toBigDecimal(pointValues.get("PP")));
            }
            if (pointValues.containsKey("QQ")) {
                eleData.setQq(toBigDecimal(pointValues.get("QQ")));
            }
            if (pointValues.containsKey("SS")) {
                eleData.setSs(toBigDecimal(pointValues.get("SS")));
            }
            if (pointValues.containsKey("PFS")) {
                eleData.setPFS(toBigDecimal(pointValues.get("PFS")));
            }
            if (pointValues.containsKey("HZ")) {
                eleData.setHZ(toBigDecimal(pointValues.get("HZ")));
            }
            if (pointValues.containsKey("KWH")) {
                eleData.setKWH(toBigDecimal(pointValues.get("KWH")));
            }
            if (pointValues.containsKey("KVARH")) {
                eleData.setKVARH(toBigDecimal(pointValues.get("KVARH")));
            }
            
            // 保存到数据库
            int result = equEleDataMapper.insert(eleData);
            log.info("保存电力数据结果: moduleId={}, result={}", moduleId, result);
        } catch (Exception e) {
            log.error("同步电力数据失败: moduleId={}", moduleId, e);
            throw e;
        }
    }
    
    /**
     * 同步其他能源数据
     * @param module 模块信息
     * @param resultList 查询结果
     */
    private void syncOtherEnergyData(TbModule module, List<Map<String, Object>> resultList) {
        String moduleId = module.getModuleId();
        log.info("同步其他能源数据: moduleId={}", moduleId);
        
        try {
            // 创建能源数据对象
            TbEquEnergyData energyData = new TbEquEnergyData();
            energyData.setModuleId(moduleId);
            energyData.setEquEnergyDt(new Date());
            
            // 处理各个点位的数据
            Map<String, Object> pointValues = new HashMap<>();
            for (Map<String, Object> data : resultList) {
                String tagname = (String) data.get("tagname");
                if (tagname != null) {
                    String pointName = InfluxDBUtil.extractPointNameFromTagname(tagname);
                    Object value = data.get("value");
                    
                    if (value != null) {
                        pointValues.put(pointName, value);
                        log.debug("收集数据点: 模块ID={}, 点位={}, 值={}", moduleId, pointName, value);
                    }
                }
            }
            
            log.info("模块 {} 收集了 {} 个数据点", moduleId, pointValues.size());
            
            // 设置各个字段的值
            if (pointValues.containsKey("TEMPERATURE")) {
                energyData.setEnergyTemperature(toBigDecimal(pointValues.get("TEMPERATURE")));
            }
            if (pointValues.containsKey("PRESSURE")) {
                energyData.setEnergyPressure(toBigDecimal(pointValues.get("PRESSURE")));
            }
            if (pointValues.containsKey("WINKVALUE")) {
                energyData.setEnergyWinkvalue(toBigDecimal(pointValues.get("WINKVALUE")));
            }
            if (pointValues.containsKey("ACCUMULATEVALUE")) {
                energyData.setEnergyAccumulatevalue(toBigDecimal(pointValues.get("ACCUMULATEVALUE")));
            }
            
            // 保存到数据库
            int result = equEnergyDataMapper.insert(energyData);
            log.info("保存能源数据结果: moduleId={}, result={}", moduleId, result);
        } catch (Exception e) {
            log.error("同步其他能源数据失败: moduleId={}", moduleId, e);
            throw e;
        }
    }
    
    /**
     * 统计前一天的日能耗
     * 原定时：每天凌晨1点执行
     * 现改为：每5分钟执行一次，与实时数据同步
     */
    //@Scheduled(cron = "0 0 1 * * ?")
    public void syncDailyEnergyData() {
        log.info("开始统计日能耗数据...");
        try {
            // 获取昨天的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar.getTime();
            String yesterdayStr = dateFormat.format(yesterday);
            
            // 获取所有模块
            List<TbModule> moduleList = moduleMapper.selectList(null);
            
            for (TbModule module : moduleList) {
                String moduleId = module.getModuleId();
                
                // 查询模块的日统计数据
                QueryResult queryResult = influxDBService.queryDailyStatsByDate(moduleId, yesterdayStr);
                List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
                
                // 处理查询结果
                if (resultList.isEmpty()) {
                    continue;
                }
                
                // 计算累计值
                BigDecimal sumValue = null;
                for (Map<String, Object> data : resultList) {
                    String tagname = (String) data.get("tagname");
                    if (tagname != null && tagname.endsWith("#KWH") || tagname.endsWith("#ACCUMULATEVALUE")) {
                        Object sum = data.get("sum_value");
                        if (sum != null) {
                            sumValue = toBigDecimal(sum);
                            break;
                        }
                    }
                }
                
                if (sumValue == null) {
                    continue;
                }
                
                // 查询昨天是否已有记录
                LocalDate localDate = LocalDate.parse(yesterdayStr);
                Date dtDate = java.sql.Date.valueOf(localDate);
                
                LambdaQueryWrapper<TbEpEquEnergyDaycount> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TbEpEquEnergyDaycount::getModuleId, moduleId)
                        .eq(TbEpEquEnergyDaycount::getDt, dtDate);
                
                TbEpEquEnergyDaycount daycount = daycountMapper.selectOne(queryWrapper);
                
                if (daycount == null) {
                    // 创建新记录
                    daycount = new TbEpEquEnergyDaycount();
                    daycount.setModuleId(moduleId);
                    daycount.setDt(dtDate);
                    daycount.setEnergyCount(sumValue);
                    daycount.setEndCount(sumValue);
                    
                    // 查询前一天的记录，获取开始值
                    Calendar prevDay = Calendar.getInstance();
                    prevDay.setTime(yesterday);
                    prevDay.add(Calendar.DAY_OF_MONTH, -1);
                    LocalDate prevLocalDate = LocalDate.parse(dateFormat.format(prevDay.getTime()));
                    Date prevDtDate = java.sql.Date.valueOf(prevLocalDate);
                    
                    LambdaQueryWrapper<TbEpEquEnergyDaycount> prevQueryWrapper = new LambdaQueryWrapper<>();
                    prevQueryWrapper.eq(TbEpEquEnergyDaycount::getModuleId, moduleId)
                            .eq(TbEpEquEnergyDaycount::getDt, prevDtDate);
                    
                    TbEpEquEnergyDaycount prevDaycount = daycountMapper.selectOne(prevQueryWrapper);
                    
                    if (prevDaycount != null) {
                        daycount.setStratCount(prevDaycount.getEndCount());
                    } else {
                        daycount.setStratCount(BigDecimal.ZERO);
                    }
                    
                    daycountMapper.insert(daycount);
                } else {
                    // 更新记录
                    daycount.setEnergyCount(sumValue);
                    daycount.setEndCount(sumValue);
                    daycountMapper.updateById(daycount);
                }
            }
            
            log.info("日能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计日能耗数据失败", e);
        }
    }
    
    /**
     * 统计上个月的月能耗
     * 原定时：每月1号凌晨2点执行
     * 现改为：每5分钟执行一次，与实时数据同步
     */
    //@Scheduled(cron = "0 0 2 1 * ?")
    public void syncMonthlyEnergyData() {
        log.info("开始统计月能耗数据...");
        try {
            // 获取上个月的年月
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            
            // 获取所有模块
            List<TbModule> moduleList = moduleMapper.selectList(null);
            
            for (TbModule module : moduleList) {
                String moduleId = module.getModuleId();
                
                // 查询模块的月统计数据
                QueryResult queryResult = influxDBService.queryMonthlyStatsByYearMonth(moduleId, year, month);
                List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
                
                // 处理查询结果
                if (resultList.isEmpty()) {
                    continue;
                }
                
                // 计算累计值
                BigDecimal sumValue = null;
                for (Map<String, Object> data : resultList) {
                    String tagname = (String) data.get("tagname");
                    if (tagname != null && tagname.endsWith("#KWH") || tagname.endsWith("#ACCUMULATEVALUE")) {
                        Object sum = data.get("sum_value");
                        if (sum != null) {
                            sumValue = toBigDecimal(sum);
                            break;
                        }
                    }
                }
                
                if (sumValue == null) {
                    continue;
                }
                
                // 查询上个月是否已有记录
                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDate firstDayOfMonth = yearMonth.atDay(1);
                Date dtDate = java.sql.Date.valueOf(firstDayOfMonth);
                
                LambdaQueryWrapper<TbEpEquEnergyMonthcount> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TbEpEquEnergyMonthcount::getModuleId, moduleId)
                        .eq(TbEpEquEnergyMonthcount::getDt, dtDate);
                
                TbEpEquEnergyMonthcount monthcount = monthcountMapper.selectOne(queryWrapper);
                
                if (monthcount == null) {
                    // 创建新记录
                    monthcount = new TbEpEquEnergyMonthcount();
                    monthcount.setModuleId(moduleId);
                    monthcount.setDt(dtDate);
                    monthcount.setEnergyCount(sumValue);
                    monthcount.setEndCount(sumValue);
                    
                    // 查询前一个月的记录，获取开始值
                    YearMonth prevYearMonth = yearMonth.minusMonths(1);
                    LocalDate firstDayOfPrevMonth = prevYearMonth.atDay(1);
                    Date prevDtDate = java.sql.Date.valueOf(firstDayOfPrevMonth);
                    
                    LambdaQueryWrapper<TbEpEquEnergyMonthcount> prevQueryWrapper = new LambdaQueryWrapper<>();
                    prevQueryWrapper.eq(TbEpEquEnergyMonthcount::getModuleId, moduleId)
                            .eq(TbEpEquEnergyMonthcount::getDt, prevDtDate);
                    
                    TbEpEquEnergyMonthcount prevMonthcount = monthcountMapper.selectOne(prevQueryWrapper);
                    
                    if (prevMonthcount != null) {
                        monthcount.setStratCount(prevMonthcount.getEndCount());
                    } else {
                        monthcount.setStratCount(BigDecimal.ZERO);
                    }
                    
                    monthcountMapper.insert(monthcount);
                } else {
                    // 更新记录
                    monthcount.setEnergyCount(sumValue);
                    monthcount.setEndCount(sumValue);
                    monthcountMapper.updateById(monthcount);
                }
            }
            
            log.info("月能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计月能耗数据失败", e);
        }
    }
    
    /**
     * 统计上一年的年能耗
     * 原定时：每年1月1日凌晨3点执行
     * 现改为：每5分钟执行一次，与实时数据同步
     */
    //@Scheduled(cron = "0 0 3 1 1 ?")
    public void syncYearlyEnergyData() {
        log.info("开始统计年能耗数据...");
        try {
            // 获取上一年的年份
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1);
            int year = calendar.get(Calendar.YEAR);
            
            // 获取所有模块
            List<TbModule> moduleList = moduleMapper.selectList(null);
            
            for (TbModule module : moduleList) {
                String moduleId = module.getModuleId();
                
                // 查询模块的年统计数据
                QueryResult queryResult = influxDBService.queryYearlyStatsByYear(moduleId, year);
                List<Map<String, Object>> resultList = InfluxDBUtil.parseQueryResult(queryResult);
                
                // 处理查询结果
                if (resultList.isEmpty()) {
                    continue;
                }
                
                // 计算累计值
                BigDecimal sumValue = null;
                for (Map<String, Object> data : resultList) {
                    String tagname = (String) data.get("tagname");
                    if (tagname != null && tagname.endsWith("#KWH") || tagname.endsWith("#ACCUMULATEVALUE")) {
                        Object sum = data.get("sum_value");
                        if (sum != null) {
                            sumValue = toBigDecimal(sum);
                            break;
                        }
                    }
                }
                
                if (sumValue == null) {
                    continue;
                }
                
                // 查询上一年是否已有记录
                LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
                Date dtDate = java.sql.Date.valueOf(firstDayOfYear);
                
                LambdaQueryWrapper<TbEpEquEnergyYearcount> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TbEpEquEnergyYearcount::getModuleId, moduleId)
                        .eq(TbEpEquEnergyYearcount::getDt, dtDate);
                
                TbEpEquEnergyYearcount yearcount = yearcountMapper.selectOne(queryWrapper);
                
                if (yearcount == null) {
                    // 创建新记录
                    yearcount = new TbEpEquEnergyYearcount();
                    yearcount.setModuleId(moduleId);
                    yearcount.setDt(dtDate);
                    yearcount.setEnergyCount(sumValue);
                    yearcount.setEndCount(sumValue);
                    
                    // 查询前一年的记录，获取开始值
                    LocalDate firstDayOfPrevYear = firstDayOfYear.minusYears(1);
                    Date prevDtDate = java.sql.Date.valueOf(firstDayOfPrevYear);
                    
                    LambdaQueryWrapper<TbEpEquEnergyYearcount> prevQueryWrapper = new LambdaQueryWrapper<>();
                    prevQueryWrapper.eq(TbEpEquEnergyYearcount::getModuleId, moduleId)
                            .eq(TbEpEquEnergyYearcount::getDt, prevDtDate);
                    
                    TbEpEquEnergyYearcount prevYearcount = yearcountMapper.selectOne(prevQueryWrapper);
                    
                    if (prevYearcount != null) {
                        yearcount.setStratCount(prevYearcount.getEndCount());
                    } else {
                        yearcount.setStratCount(BigDecimal.ZERO);
                    }
                    
                    yearcountMapper.insert(yearcount);
                } else {
                    // 更新记录
                    yearcount.setEnergyCount(sumValue);
                    yearcount.setEndCount(sumValue);
                    yearcountMapper.updateById(yearcount);
                }
            }
            
            log.info("年能耗数据统计完成");
        } catch (Exception e) {
            log.error("统计年能耗数据失败", e);
        }
    }
    
    /**
     * 将对象转换为BigDecimal
     * @param value 值对象
     * @return BigDecimal值
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将对象转换为Double
     * @param value 值对象
     * @return Double值
     */
    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
} 