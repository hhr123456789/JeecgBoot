package org.jeecg.modules.energy.service.classification.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.jeecg.modules.energy.entity.classification.TbEnergyClassificationSummary;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.classification.TbEnergyClassificationSummaryMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.classification.IEnergyClassificationSyncService;
import org.jeecg.modules.energy.service.classification.IEnergyTypeConfigService;
import org.jeecg.modules.energy.vo.classification.ClassificationStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 企业分类分区统计同步服务实现类
 * @author jeecg
 */
@Slf4j
@Service
public class EnergyClassificationSyncServiceImpl implements IEnergyClassificationSyncService {

    @Autowired
    private TbEnergyClassificationSummaryMapper summaryMapper;

    @Autowired
    private TbModuleMapper moduleMapper;

    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;

    @Autowired
    private IEnergyTypeConfigService energyTypeConfigService;

    

    @Override
    public List<Map<String, Object>> aggregateStatisticsByOrgAndEnergyType(Date startDate, Date endDate) {
        try {
            log.info("开始按部门+能源类型+时间维度聚合统计数据: startDate={}, endDate={}", 
                     startDate, endDate);

            // 获取指定时间范围内的统计数据
            List<ClassificationStatisticsVO> statisticsList = 
                summaryMapper.selectStatisticsGroupByOrgAndEnergyType(startDate, endDate);

            List<Map<String, Object>> result = new ArrayList<>();

            for (ClassificationStatisticsVO stat : statisticsList) {
                Map<String, Object> item = new HashMap<>();
                item.put("orgCode", stat.getOrgCode());
                item.put("energyType", stat.getEnergyType());
                item.put("statDate", stat.getStatDate());
                item.put("totalConsumption", stat.getTotalConsumption());
                item.put("peakConsumption", stat.getPeakConsumption());
                item.put("flatConsumption", stat.getFlatConsumption());
                item.put("valleyConsumption", stat.getValleyConsumption());
                item.put("meterCount", stat.getMeterCount());

                // 计算费用和碳排放量
                if (stat.getTotalConsumption() != null) {
                    double totalCost = energyTypeConfigService.calculateCost(
                        stat.getEnergyType(), stat.getTotalConsumption().doubleValue());
                    double carbonEmission = energyTypeConfigService.calculateCarbonEmission(
                        stat.getEnergyType(), stat.getTotalConsumption().doubleValue());
                    double standardCoal = energyTypeConfigService.calculateStandardCoal(
                        stat.getEnergyType(), stat.getTotalConsumption().doubleValue());

                    item.put("totalCost", BigDecimal.valueOf(totalCost));
                    item.put("carbonEmission", BigDecimal.valueOf(carbonEmission));
                    item.put("standardCoal", BigDecimal.valueOf(standardCoal));
                }

                result.add(item);
            }

            log.info("成功聚合统计数据: 记录数={}", result.size());
            return result;

        } catch (Exception e) {
            log.error("聚合统计数据失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncClassificationData(Date startDate, Date endDate) {
        try {
            log.info("========== 开始同步企业分类分区统计数据 ==========");
            log.info("时间范围: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> syncResult = new HashMap<>();
            syncResult.put("startTime", new Date());
            syncResult.put("startDate", startDate);
            syncResult.put("endDate", endDate);

            // 1. 先查询是否有数据可以同步
            log.info("步骤1: 查询实时表数据...");
            log.info("执行SQL: selectStatisticsGroupByOrgAndEnergyType");
            log.info("查询条件: startDate={}, endDate={}", startDate, endDate);

            List<ClassificationStatisticsVO> dailyStats =
                summaryMapper.selectStatisticsGroupByOrgAndEnergyType(startDate, endDate);

            log.info("查询到 {} 条待同步的统计数据", dailyStats.size());

            if (dailyStats.isEmpty()) {
                log.warn("========================================");
                log.warn("警告: 未查询到任何待同步的数据！");
                log.warn("========================================");
                log.warn("请检查以下几点:");
                log.warn("1. tb_ep_equ_energy_daycount 表是否有数据");
                log.warn("   SQL: SELECT COUNT(*) FROM tb_ep_equ_energy_daycount WHERE dt >= '{}' AND dt <= '{}'", startDate, endDate);
                log.warn("2. tb_module 表中仪表是否启用 (isaction='Y')");
                log.warn("   SQL: SELECT COUNT(*) FROM tb_module WHERE isaction='Y'");
                log.warn("3. tb_module 表中仪表是否有 sys_org_code 和 energy_type");
                log.warn("   SQL: SELECT COUNT(*) FROM tb_module WHERE sys_org_code IS NOT NULL AND energy_type IS NOT NULL");
                log.warn("4. 实时表和仪表表是否能关联");
                log.warn("   SQL: SELECT COUNT(*) FROM tb_ep_equ_energy_daycount d INNER JOIN tb_module m ON d.module_id = m.module_id WHERE m.isaction='Y'");
                log.warn("========================================");
                log.warn("建议: 执行 db/check_data.sql 进行完整诊断");
                log.warn("========================================");

                syncResult.put("successCount", 0);
                syncResult.put("failCount", 0);
                syncResult.put("totalRecords", 0);
                syncResult.put("status", "NO_DATA");
                syncResult.put("message", "未查询到待同步的数据");
                syncResult.put("endTime", new Date());
                return syncResult;
            }

            // 打印前3条数据样例
            log.info("数据样例 (前3条):");
            for (int i = 0; i < Math.min(3, dailyStats.size()); i++) {
                ClassificationStatisticsVO stat = dailyStats.get(i);
                log.info("  [{}] orgCode={}, energyType={}, statDate={}, totalConsumption={}, meterCount={}",
                         i + 1, stat.getOrgCode(), stat.getEnergyType(), stat.getStatDate(),
                         stat.getTotalConsumption(), stat.getMeterCount());
            }

            // 2. 清理指定时间范围内的旧数据
            log.info("步骤2: 清理旧数据...");
            Map<String, Object> cleanupResult = cleanupOldData(startDate, endDate);
            Integer deletedCount = (Integer) cleanupResult.get("deletedCount");
            log.info("清理完成: 删除了 {} 条旧记录", deletedCount);
            syncResult.put("cleanupResult", cleanupResult);

            // 3. 按日期聚合并保存数据
            log.info("步骤3: 保存统计数据...");
            int successCount = 0;
            int failCount = 0;
            int totalRecords = 0;

            for (ClassificationStatisticsVO stat : dailyStats) {
                try {
                    log.debug("处理统计记录 [{}/{}]: orgCode={}, energyType={}, statDate={}, totalConsumption={}",
                             totalRecords + 1, dailyStats.size(),
                             stat.getOrgCode(), stat.getEnergyType(), stat.getStatDate(), stat.getTotalConsumption());

                    // 保存统计数据
                    boolean saved = saveStatisticsRecord(stat, "day");
                    if (saved) {
                        successCount++;
                        log.debug("  ✓ 保存成功");
                    } else {
                        failCount++;
                        log.warn("  ✗ 保存失败");
                    }
                    totalRecords++;
                } catch (Exception e) {
                    log.error("  ✗ 保存统计记录失败: orgCode={}, energyType={}",
                             stat.getOrgCode(), stat.getEnergyType(), e);
                    failCount++;
                    totalRecords++;
                }
            }

            syncResult.put("successCount", successCount);
            syncResult.put("failCount", failCount);
            syncResult.put("totalRecords", totalRecords);
            syncResult.put("endTime", new Date());
            syncResult.put("status", "SUCCESS");

            log.info("========== 同步企业分类分区统计数据完成 ==========");
            log.info("成功: {}, 失败: {}, 总数: {}", successCount, failCount, totalRecords);
            log.info("========================================");

            return syncResult;

        } catch (Exception e) {
            log.error("========== 同步企业分类分区统计数据失败 ==========");
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常信息: {}", e.getMessage());
            log.error("异常堆栈:", e);
            log.error("========================================");

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("startTime", startDate);
            errorResult.put("endDate", endDate);
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            errorResult.put("endTime", new Date());

            return errorResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncByDate(Date targetDate) {
        try {
            log.info("按日期同步企业分类分区统计数据: targetDate={}", targetDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(targetDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startDate = calendar.getTime();
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();

            Map<String, Object> result = syncClassificationData(startDate, endDate);
            result.put("targetDate", targetDate);
            result.put("type", "DATE");

            return result;

        } catch (Exception e) {
            log.error("按日期同步数据失败: targetDate={}", targetDate, e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("targetDate", targetDate);
            errorResult.put("type", "DATE");
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncByMonth(Integer year, Integer month) {
        try {
            log.info("按月份同步企业分类分区统计数据: year={}, month={}", year, month);

            // 计算月份的开始和结束日期
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            
            Date startDate = java.sql.Date.valueOf(startOfMonth);
            Date endDate = java.sql.Date.valueOf(endOfMonth.plusDays(1));

            Map<String, Object> result = syncClassificationData(startDate, endDate);
            result.put("year", year);
            result.put("month", month);
            result.put("type", "MONTH");

            return result;

        } catch (Exception e) {
            log.error("按月份同步数据失败: year={}, month={}", year, month, e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("year", year);
            errorResult.put("month", month);
            errorResult.put("type", "MONTH");
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncByYear(Integer year) {
        try {
            log.info("按年份同步企业分类分区统计数据: year={}", year);

            // 计算年份的开始和结束日期
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);
            
            Date startDate = java.sql.Date.valueOf(startOfYear);
            Date endDate = java.sql.Date.valueOf(endOfYear.plusDays(1));

            Map<String, Object> result = syncClassificationData(startDate, endDate);
            result.put("year", year);
            result.put("type", "YEAR");

            return result;

        } catch (Exception e) {
            log.error("按年份同步数据失败: year={}", year, e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("year", year);
            errorResult.put("type", "YEAR");
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cleanupOldData(Date startDate, Date endDate) {
        try {
            log.info("清理历史数据: startDate={}, endDate={}", startDate, endDate);

            int deletedCount = summaryMapper.deleteByDateRange(startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("status", "SUCCESS");

            log.info("清理历史数据完成: 删除记录数={}", deletedCount);
            return result;

        } catch (Exception e) {
            log.error("清理历史数据失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("startDate", startDate);
            errorResult.put("endDate", endDate);
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    public Date getLatestStatisticsDate() {
        try {
            log.debug("获取最新统计数据的日期");
            
            Date latestDate = summaryMapper.getLatestStatisticsDate();
            
            if (latestDate != null) {
                log.debug("最新统计数据日期: {}", latestDate);
            } else {
                log.debug("暂无统计数据");
            }
            
            return latestDate;

        } catch (Exception e) {
            log.error("获取最新统计日期失败", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> validateDataCompleteness(Date startDate, Date endDate) {
        try {
            log.info("验证数据完整性: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("startDate", startDate);
            validationResult.put("endDate", endDate);

            // 统计实时表数据量
            long realTimeRecordCount = 0;
            try {
                LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
                wrapper.between(TbEpEquEnergyDaycount::getDt, startDate, endDate);
                realTimeRecordCount = daycountMapper.selectCount(wrapper);
            } catch (Exception e) {
                log.error("统计实时表数据量失败", e);
            }

            // 统计汇总表数据量
            long summaryRecordCount = 0;
            try {
                List<ClassificationStatisticsVO> summaryData = 
                    summaryMapper.selectStatisticsGroupByOrgAndEnergyType(startDate, endDate);
                summaryRecordCount = summaryData.size();
            } catch (Exception e) {
                log.error("统计汇总表数据量失败", e);
            }

            // 统计仪表数量
            long moduleCount = moduleMapper.selectCount(null);

            validationResult.put("realTimeRecordCount", realTimeRecordCount);
            validationResult.put("summaryRecordCount", summaryRecordCount);
            validationResult.put("moduleCount", moduleCount);
            validationResult.put("isComplete", summaryRecordCount > 0);
            validationResult.put("validationTime", new Date());

            log.info("数据完整性验证完成: 实时数据={}, 汇总数据={}, 仪表数={}", 
                     realTimeRecordCount, summaryRecordCount, moduleCount);

            return validationResult;

        } catch (Exception e) {
            log.error("验证数据完整性失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("startDate", startDate);
            errorResult.put("endDate", endDate);
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> recalculateStatistics(Date startDate, Date endDate) {
        try {
            log.info("重新计算统计数据: startDate={}, endDate={}", startDate, endDate);

            // 先清理再重新计算
            Map<String, Object> cleanupResult = cleanupOldData(startDate, endDate);
            Map<String, Object> syncResult = syncClassificationData(startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("cleanupResult", cleanupResult);
            result.put("syncResult", syncResult);
            result.put("recalculateTime", new Date());

            log.info("重新计算统计数据完成");
            return result;

        } catch (Exception e) {
            log.error("重新计算统计数据失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("startDate", startDate);
            errorResult.put("endDate", endDate);
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> getRealTimeDataStatistics(Date startDate, Date endDate) {
        try {
            log.debug("获取实时表数据统计信息: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> statistics = new HashMap<>();

            // 统计实时表数据量
            long totalRecords = 0;
            Map<Integer, Long> energyTypeCount = new HashMap<>();
            Map<String, Long> orgCodeCount = new HashMap<>();

            try {
                LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
                wrapper.between(TbEpEquEnergyDaycount::getDt, startDate, endDate);
                totalRecords = daycountMapper.selectCount(wrapper);

                // 获取所有符合条件的记录进行详细统计
                List<TbEpEquEnergyDaycount> records = daycountMapper.selectList(wrapper);
                
                for (TbEpEquEnergyDaycount record : records) {
                    // 通过moduleId（仪表编号）获取module信息
                    // 注意：tb_ep_equ_energy_daycount.module_id 存储的是仪表编号，不是主键
                    List<TbModule> modules = moduleMapper.selectByModuleId(record.getModuleId());
                    TbModule module = (modules != null && !modules.isEmpty()) ? modules.get(0) : null;
                    if (module != null) {
                        // 统计能源类型
                        Integer energyType = module.getEnergyType();
                        energyTypeCount.put(energyType, energyTypeCount.getOrDefault(energyType, 0L) + 1);
                        
                        // 统计部门编码
                        String orgCode = module.getSysOrgCode();
                        if (orgCode != null) {
                            orgCodeCount.put(orgCode, orgCodeCount.getOrDefault(orgCode, 0L) + 1);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("获取实时表数据统计信息失败", e);
            }

            statistics.put("totalRecords", totalRecords);
            statistics.put("energyTypeCount", energyTypeCount);
            statistics.put("orgCodeCount", orgCodeCount);
            statistics.put("statisticsTime", new Date());

            return statistics;

        } catch (Exception e) {
            log.error("获取实时表数据统计信息失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean hasUnsyncedData(Date targetDate) {
        try {
            log.debug("检查是否存在未同步的数据: targetDate={}", targetDate);

            // 检查目标日期的实时数据是否已同步到汇总表
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(targetDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startDate = calendar.getTime();
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();

            // 1. 先检查实时表是否有数据
            LambdaQueryWrapper<TbEpEquEnergyDaycount> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(TbEpEquEnergyDaycount::getDt, startDate, endDate);
            long realTimeCount = daycountMapper.selectCount(wrapper);

            if (realTimeCount > 0) {
                // 2. 检查汇总表是否已有对应日期的数据（使用汇总表计数）
                long summaryCount = summaryMapper.countByDateRange(startDate, endDate);
                
                boolean hasSynced = summaryCount > 0;
                
                log.debug("未同步数据检查: targetDate={}, realTimeCount={}, summaryCount={}, hasSynced={}", 
                         targetDate, realTimeCount, summaryCount, hasSynced);
                
                return !hasSynced;
            }

            log.debug("实时表无数据，无需同步: targetDate={}", targetDate);
            return false;

        } catch (Exception e) {
            log.error("检查未同步数据失败: targetDate={}", targetDate, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> incrementalSync(Date startDate, Date endDate) {
        try {
            log.info("增量同步数据: startDate={}, endDate={}", startDate, endDate);

            // 找出需要增量同步的日期
            List<Date> datesToSync = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (!calendar.getTime().after(endDate)) {
                Date currentDate = calendar.getTime();
                if (hasUnsyncedData(currentDate)) {
                    datesToSync.add(currentDate);
                }
                calendar.add(Calendar.DATE, 1);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("datesToSync", datesToSync);
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            int successCount = 0;
            int failCount = 0;

            for (Date date : datesToSync) {
                try {
                    Map<String, Object> syncResult = syncByDate(date);
                    String status = (String) syncResult.get("status");
                    
                    if ("SUCCESS".equals(status)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("增量同步日期失败: date={}", date, e);
                    failCount++;
                }
            }

            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalDates", datesToSync.size());
            result.put("syncTime", new Date());
            result.put("status", "COMPLETED");

            log.info("增量同步完成: 成功={}, 失败={}, 总数={}", successCount, failCount, datesToSync.size());

            return result;

        } catch (Exception e) {
            log.error("增量同步失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("startDate", startDate);
            errorResult.put("endDate", endDate);
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> syncAllUnsyncedData() {
        try {
            log.info("同步所有未同步的数据");

            // 获取最新统计日期
            Date latestStatisticsDate = getLatestStatisticsDate();
            
            // 如果没有统计数据，从昨天开始同步
            Date startDate;
            if (latestStatisticsDate == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1); // 从昨天开始
                startDate = calendar.getTime();
            } else {
                // 从最新统计日期的下一日开始
                Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(latestStatisticsDate);
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
            startDate = startCalendar.getTime();
            }

            Date endDate = new Date();

            // 如果没有需要同步的数据，直接返回
            if (startDate.after(endDate)) {
                log.info("没有需要同步的数据");
                
                Map<String, Object> result = new HashMap<>();
                result.put("status", "NO_DATA_TO_SYNC");
                result.put("message", "没有需要同步的数据");
                result.put("startDate", startDate);
                result.put("endDate", endDate);
                
                return result;
            }

            Map<String, Object> result = incrementalSync(startDate, endDate);
            result.put("type", "ALL_UNSYNCED_DATA");

            return result;

        } catch (Exception e) {
            log.error("同步所有未同步数据失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("type", "ALL_UNSYNCED_DATA");
            errorResult.put("status", "FAILED");
            errorResult.put("errorMessage", e.getMessage());
            
            return errorResult;
        }
    }

    /**
     * 保存统计记录
     * @param stat 统计数据
     * @param timeDimension 时间维度
     * @return 保存结果
     */
    private boolean saveStatisticsRecord(ClassificationStatisticsVO stat, String timeDimension) {
        try {
            // 构建汇总记录
            TbEnergyClassificationSummary summary = new TbEnergyClassificationSummary();
            summary.setId(UUID.randomUUID().toString());
            summary.setOrgCode(stat.getOrgCode());
            summary.setEnergyType(stat.getEnergyType());
            summary.setStatDate(stat.getStatDate());
            summary.setTimeDimension(timeDimension);

            // 获取能源类型名称
            String energyTypeName = energyTypeConfigService.getEnergyTypeName(stat.getEnergyType());
            summary.setEnergyTypeName(energyTypeName);

            // 设置统计数据
            if (stat.getTotalConsumption() != null) {
                summary.setTotalConsumption(stat.getTotalConsumption());
                
                // 计算费用
                double cost = energyTypeConfigService.calculateCost(
                    stat.getEnergyType(), stat.getTotalConsumption().doubleValue());
                summary.setTotalCost(BigDecimal.valueOf(cost));
                
                // 计算碳排放量
                double carbonEmission = energyTypeConfigService.calculateCarbonEmission(
                    stat.getEnergyType(), stat.getTotalConsumption().doubleValue());
                summary.setCarbonEmission(BigDecimal.valueOf(carbonEmission));
                
                // 计算标准煤当量
                double standardCoal = energyTypeConfigService.calculateStandardCoal(
                    stat.getEnergyType(), stat.getTotalConsumption().doubleValue());
                summary.setStandardCoal(BigDecimal.valueOf(standardCoal));
            }

            // 设置分时段数据
            if (stat.getPeakConsumption() != null) {
                summary.setPeakConsumption(stat.getPeakConsumption());
                summary.setPeakCost(BigDecimal.valueOf(
                    energyTypeConfigService.calculateCost(
                        stat.getEnergyType(), stat.getPeakConsumption().doubleValue())));
            }

            if (stat.getFlatConsumption() != null) {
                summary.setFlatConsumption(stat.getFlatConsumption());
                summary.setFlatCost(BigDecimal.valueOf(
                    energyTypeConfigService.calculateCost(
                        stat.getEnergyType(), stat.getFlatConsumption().doubleValue())));
            }

            if (stat.getValleyConsumption() != null) {
                summary.setValleyConsumption(stat.getValleyConsumption());
                summary.setValleyCost(BigDecimal.valueOf(
                    energyTypeConfigService.calculateCost(
                        stat.getEnergyType(), stat.getValleyConsumption().doubleValue())));
            }

            summary.setMeterCount(stat.getMeterCount());

            // 设置统计月份和年份
            if (stat.getStatDate() != null) {
                LocalDate localDate = stat.getStatDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                summary.setStatMonth(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                summary.setStatYear(String.valueOf(localDate.getYear()));
            }

            // 保存到数据库
            int result = summaryMapper.insert(summary);
            
            return result > 0;

        } catch (Exception e) {
            log.error("保存统计记录失败", e);
            return false;
        }
    }
}