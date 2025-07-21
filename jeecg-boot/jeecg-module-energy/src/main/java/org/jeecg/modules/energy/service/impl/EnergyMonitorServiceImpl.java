package org.jeecg.modules.energy.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IDataFormatService;
import org.jeecg.modules.energy.service.IEnergyMonitorService;
import org.jeecg.modules.energy.service.IInfluxDBQueryService;
import org.jeecg.modules.energy.utils.EnergyCalculationUtils;
import org.jeecg.modules.energy.vo.monitor.*;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * @Description: 能源监控服务实现类
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class EnergyMonitorServiceImpl implements IEnergyMonitorService {

    @Autowired
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;
    
    @Autowired
    private TbEpEquEnergyDaycountMapper tbEpEquEnergyDaycountMapper;
    
    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IInfluxDBQueryService influxDBQueryService;

    @Autowired
    private IDataFormatService dataFormatService;
    
    @Override
    public List<Map<String, Object>> getRealTimeData(String orgCode, Integer nowtype) {
        // 1. 将部门编码转换为部门ID
        String departId = getDepartIdByOrgCode(orgCode);
        log.info("部门编码 {} 对应的部门ID为: {}", orgCode, departId);
        
        if(departId == null) {
            // 如果找不到对应的部门ID，直接使用orgCode作为查询条件
            log.warn("未找到部门编码 {} 对应的部门ID，将直接使用部门编码查询", orgCode);
            departId = orgCode;
        }
        
        // 2. 根据部门ID查询关联的仪表列表
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(departId);
        log.info("根据部门ID/编码 {} 查询到 {} 个仪表", departId, modules.size());
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TbModule module : modules) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("module_name", module.getModuleName());
            dataMap.put("module_id", module.getModuleId());
            dataMap.put("rated_power", module.getRatedPower());
            dataMap.put("energy_type", module.getEnergyType());
            
            // 根据能源类型获取不同的实时数据和采集日期
            Date collectionDate = null;
            
            if (nowtype == 1 || nowtype == 2) {
                // 电力数据
                TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(module.getModuleId());
                if (eleData == null) {
                    log.warn("未找到仪表 {} 的电力数据，但仍将保留基本信息和日用量", module.getModuleId());
                } else {
                    // 获取电力数据的采集日期
                    collectionDate = eleData.getEquElectricDT();
                    dataMap.put("Equ_Electric_DT", eleData.getEquElectricDT());
                    
                    // 使用工具类计算负荷状态
                    String loadStatus = EnergyCalculationUtils.calculateLoadStatus(
                        eleData.getIA(), eleData.getIB(), eleData.getIC(),
                        eleData.getUA(), eleData.getUB(), eleData.getUC()
                    );
                    dataMap.put("loadStatus", loadStatus);
                    
                    // 使用工具类计算负荷率
                    BigDecimal loadRate = EnergyCalculationUtils.calculateLoadRate(
                        eleData.getPp(), 
                        module.getRatedPower() != null ? new BigDecimal(module.getRatedPower()) : BigDecimal.ZERO
                    );
                    dataMap.put("loadRate", loadRate);
                    
                    // 设置其他电力数据
                    dataMap.put("PFS", eleData.getPFS());
                    dataMap.put("HZ", eleData.getHZ());
                    dataMap.put("pp", eleData.getPp());
                    dataMap.put("UA", eleData.getUA());
                    dataMap.put("UB", eleData.getUB());
                    dataMap.put("UC", eleData.getUC());
                    dataMap.put("IA", eleData.getIA());
                    dataMap.put("IB", eleData.getIB());
                    dataMap.put("IC", eleData.getIC());
                    dataMap.put("PFa", eleData.getPFa());
                    dataMap.put("PFb", eleData.getPFb());
                    dataMap.put("PFc", eleData.getPFc());
                    dataMap.put("Pa", eleData.getPa());
                    dataMap.put("Pb", eleData.getPb());
                    dataMap.put("Pc", eleData.getPc());
                    dataMap.put("KWH", eleData.getKWH());
                    dataMap.put("KVARH", eleData.getKVARH());
                }
            } else {
                // 天然气/压缩空气/用水数据
                TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(module.getModuleId());
                if (energyData == null) {
                    log.warn("未找到仪表 {} 的能源数据，但仍将保留基本信息和日用量", module.getModuleId());
                } else {
                    // 获取能源数据的采集日期
                    collectionDate = energyData.getEquEnergyDt();
                    dataMap.put("equ_energy_dt", energyData.getEquEnergyDt());
                    dataMap.put("energy_temperature", energyData.getEnergyTemperature());
                    dataMap.put("energy_pressure", energyData.getEnergyPressure());
                    dataMap.put("energy_winkvalue", energyData.getEnergyWinkvalue());
                    dataMap.put("energy_accumulatevalue", energyData.getEnergyAccumulatevalue());
                }
            }
            
            // 使用采集日期查询仪表的日用电量，如果没有采集日期则使用当前日期
            Date queryDate = collectionDate != null ? DateUtil.beginOfDay(collectionDate) : DateUtil.beginOfDay(new Date());
            TbEpEquEnergyDaycount dayCount = tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(
                module.getModuleId(), queryDate);
            
            // 设置日用量，如果没有数据则默认为0
            if (dayCount != null && dayCount.getEnergyCount() != null) {
                dataMap.put("dailyPower", dayCount.getEnergyCount());
                log.info("仪表 {} 使用日期 {} 设置日用量: {}", module.getModuleId(), DateUtil.formatDate(queryDate), dayCount.getEnergyCount());
            } else {
                dataMap.put("dailyPower", BigDecimal.ZERO);
                log.info("仪表 {} 使用日期 {} 未找到能耗数据，设置默认值0", module.getModuleId(), DateUtil.formatDate(queryDate));
            }
            
            // 最后检查确保dailyPower字段存在
            if (!dataMap.containsKey("dailyPower")) {
                dataMap.put("dailyPower", BigDecimal.ZERO);
                log.warn("最终检查发现仪表 {} 缺少dailyPower字段，已添加默认值0", module.getModuleId());
            }
            
            result.add(dataMap);
        }
        
        return result;
    }
    
    @Override
    public List<ModuleVO> getModulesByOrgCode(String orgCodes, Integer nowtype) {
        log.info("根据维度获取仪表列表，维度编码：{}，维度类型：{}", orgCodes, nowtype);

        List<ModuleVO> result = new ArrayList<>();

        try {
            // 1. 解析维度编码列表（支持逗号分隔的多个维度）
            List<String> orgCodeList = Arrays.asList(orgCodes.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            log.info("解析到维度编码列表：{}", orgCodeList);

            if (orgCodeList.isEmpty()) {
                log.warn("维度编码列表为空");
                return result;
            }

            // 2. 根据维度编码列表查询对应的部门ID列表
            List<String> departIds = getDepartIdsByOrgCodes(orgCodeList);
            log.info("根据维度编码列表查询到部门ID列表：{}", departIds);

            if (departIds.isEmpty()) {
                log.warn("❌ 未找到维度编码列表 {} 对应的部门ID，请检查 sys_depart 表中是否存在这些维度编码", orgCodeList);
                // 添加详细的调试信息
                for (String orgCode : orgCodeList) {
                    QueryWrapper<SysDepart> debugQuery = new QueryWrapper<>();
                    debugQuery.eq("org_code", orgCode);
                    List<SysDepart> debugDeparts = sysDepartService.list(debugQuery);
                    log.warn("🔍 调试信息 - 维度编码 {} 在 sys_depart 表中的查询结果：{}", orgCode,
                        debugDeparts.isEmpty() ? "不存在" : debugDeparts.size() + "条记录");
                }
                return result;
            }

            // 3. 根据部门ID列表和维度类型查询仪表
            List<TbModule> modules = getModulesByDepartIdsAndType(departIds, nowtype);
            log.info("🔍 根据部门ID列表 {} 和维度类型 {} 查询到仪表数量：{}", departIds, nowtype, modules.size());

            if (modules.isEmpty()) {
                Integer expectedEnergyType = getEnergyTypeByNowtype(nowtype);
                log.warn("❌ 未找到仪表数据，可能原因：");
                log.warn("   1. 部门ID {} 下没有仪表", departIds);
                log.warn("   2. 没有能源类型为 {} 的仪表（维度类型 {} 对应能源类型 {}）", expectedEnergyType, nowtype, expectedEnergyType);
                log.warn("   3. 仪表的 isaction 字段不是 'Y'");

                // 查询该部门下的所有仪表进行调试
                for (String departId : departIds) {
                    List<TbModule> allModules = tbModuleMapper.selectModulesByOrgCode(departId);
                    log.warn("🔍 调试信息 - 部门ID {} 下的所有仪表数量：{}", departId, allModules.size());

                    if (!allModules.isEmpty()) {
                        Map<Integer, Long> energyTypeCount = allModules.stream()
                            .collect(Collectors.groupingBy(TbModule::getEnergyType, Collectors.counting()));
                        log.warn("🔍 调试信息 - 按能源类型统计：{}", energyTypeCount);
                        log.warn("🔍 调试信息 - 维度类型 {} 需要的能源类型：{}", nowtype, expectedEnergyType);

                        long activeCount = allModules.stream()
                            .filter(m -> "Y".equals(m.getIsaction()))
                            .count();
                        log.warn("🔍 调试信息 - 启用状态的仪表数量：{}/{}", activeCount, allModules.size());

                        long targetTypeCount = allModules.stream()
                            .filter(m -> expectedEnergyType.equals(m.getEnergyType()))
                            .count();
                        log.warn("🔍 调试信息 - 能源类型为 {} 的仪表数量：{}", expectedEnergyType, targetTypeCount);
                    }
                }
            }

            // 4. 获取维度名称映射 (部门ID -> 部门名称)
            Map<String, String> departNameMap = getDepartNameMapByIds(departIds);

            // 5. 获取维度编码映射 (部门ID -> 部门编码)
            Map<String, String> departOrgCodeMap = getDepartOrgCodeMapByIds(departIds);

            // 6. 转换为VO
            for (TbModule module : modules) {
                ModuleVO vo = new ModuleVO();
                vo.setModuleId(module.getModuleId());
                vo.setModuleName(module.getModuleName());
                vo.setOrgCode(departOrgCodeMap.get(module.getSysOrgCode())); // 返回维度编码而不是部门ID
                vo.setDepartName(departNameMap.get(module.getSysOrgCode()));
                vo.setEnergyType(module.getEnergyType());
                vo.setIsAction(module.getIsaction());
                result.add(vo);
            }

        } catch (Exception e) {
            log.error("根据维度获取仪表列表失败", e);
            throw new RuntimeException("查询仪表列表失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Object getRealTimeMonitorData(RealTimeDataRequest request) {
        log.info("🔍 查询实时数据开始，请求参数：{}", request);

        try {
            // 1. 参数验证
            log.info("🔍 步骤1 - 参数验证");
            validateRequest(request);

            // 2. 查询InfluxDB数据
            log.info("🔍 步骤2 - 查询InfluxDB数据，仪表：{}，参数：{}，时间：{} ~ {}",
                request.getModuleIds(), request.getParameters(), request.getStartTime(), request.getEndTime());
            List<Map<String, Object>> influxResults = influxDBQueryService.queryRealTimeData(
                    request.getModuleIds(),
                    request.getParameters(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getInterval()
            );
            log.info("🔍 步骤2 - InfluxDB查询完成，返回数据条数：{}", influxResults != null ? influxResults.size() : 0);

            // 3. 获取仪表名称映射
            log.info("🔍 步骤3 - 获取仪表名称映射");
            Map<String, String> moduleNameMap = getModuleNameMap(request.getModuleIds());
            log.info("🔍 步骤3 - 仪表名称映射：{}", moduleNameMap);

            // 4. 根据显示模式格式化数据
            log.info("🔍 步骤4 - 数据格式化，显示模式：{}", request.getDisplayMode() == 1 ? "统一显示" : "分开显示");
            Object result;
            if (request.getDisplayMode() == 1) {
                // 统一显示
                result = dataFormatService.formatUnifiedDisplay(
                        influxResults,
                        moduleNameMap,
                        request.getParameters(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getInterval()
                );
            } else {
                // 分开显示
                result = dataFormatService.formatSeparatedDisplay(
                        influxResults,
                        moduleNameMap,
                        request.getParameters(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getInterval()
                );
            }

            log.info("✅ 查询实时数据完成，返回结果类型：{}", result != null ? result.getClass().getSimpleName() : "null");
            return result;

        } catch (Exception e) {
            log.error("查询实时数据失败", e);
            throw new RuntimeException("查询实时数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据部门编码获取部门ID
     * @param orgCode 部门编码
     * @return 部门ID
     */
    private String getDepartIdByOrgCode(String orgCode) {
        try {
            // 直接根据部门编码查询部门信息
            QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("org_code", orgCode);
            SysDepart depart = sysDepartService.getOne(queryWrapper);

            log.info("通过org_code={}直接查询部门结果: {}", orgCode, depart);
            if(depart != null) {
                return depart.getId();
            }

            // 如果直接查询不到，尝试其他方法
            JSONObject departInfo = sysDepartService.queryAllParentIdByOrgCode(orgCode);
            log.info("通过queryAllParentIdByOrgCode查询结果: {}", departInfo);
            if(departInfo != null && departInfo.containsKey("departId")) {
                return departInfo.getString("departId");
            }

            return null;
        } catch (Exception e) {
            log.error("获取部门ID失败", e);
            return null;
        }
    }

    /**
     * 根据维度编码列表获取部门ID列表
     * @param orgCodeList 维度编码列表
     * @return 部门ID列表
     */
    private List<String> getDepartIdsByOrgCodes(List<String> orgCodeList) {
        List<String> departIds = new ArrayList<>();

        try {
            QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("org_code", orgCodeList);
            List<SysDepart> departs = sysDepartService.list(queryWrapper);

            departIds = departs.stream()
                    .map(SysDepart::getId)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            log.info("根据维度编码列表 {} 查询到部门ID列表：{}", orgCodeList, departIds);

        } catch (Exception e) {
            log.error("根据维度编码列表查询部门ID失败: {}", orgCodeList, e);
        }

        return departIds;
    }

    /**
     * 根据维度编码获取部门ID列表（已废弃，保留用于兼容）
     * @param orgCode 维度编码
     * @param includeChildren 是否包含子维度
     * @return 部门ID列表
     */
    @Deprecated
    private List<String> getDepartIdsByOrgCode(String orgCode, Boolean includeChildren) {
        List<String> departIds = new ArrayList<>();

        try {
            if (includeChildren != null && includeChildren) {
                // 包含子维度：先查询当前维度，再查询其直接子维度

                // 1. 查询当前维度
                QueryWrapper<SysDepart> currentWrapper = new QueryWrapper<>();
                currentWrapper.eq("org_code", orgCode);
                SysDepart currentDepart = sysDepartService.getOne(currentWrapper);

                if (currentDepart != null) {
                    departIds.add(currentDepart.getId());

                    // 2. 查询直接子维度（parent_id = 当前维度的ID）
                    QueryWrapper<SysDepart> childWrapper = new QueryWrapper<>();
                    childWrapper.eq("parent_id", currentDepart.getId());
                    List<SysDepart> childDeparts = sysDepartService.list(childWrapper);

                    for (SysDepart child : childDeparts) {
                        if (StringUtils.hasText(child.getId())) {
                            departIds.add(child.getId());
                        }
                    }

                    log.info("维度编码 {} 包含子维度查询：当前维度ID={}, 子维度数量={}",
                            orgCode, currentDepart.getId(), childDeparts.size());
                } else {
                    log.warn("未找到维度编码 {} 对应的部门", orgCode);
                }
            } else {
                // 不包含子维度：只查询当前维度
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("org_code", orgCode);
                SysDepart depart = sysDepartService.getOne(queryWrapper);

                if (depart != null && StringUtils.hasText(depart.getId())) {
                    departIds.add(depart.getId());
                    log.info("维度编码 {} 精确查询：部门ID={}", orgCode, depart.getId());
                } else {
                    log.warn("未找到维度编码 {} 对应的部门", orgCode);
                }
            }

        } catch (Exception e) {
            log.error("根据维度编码查询部门ID失败: {}", orgCode, e);
        }

        return departIds;
    }

    /**
     * 根据部门ID列表和维度类型查询仪表
     * @param departIds 部门ID列表
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 仪表列表
     */
    private List<TbModule> getModulesByDepartIdsAndType(List<String> departIds, Integer nowtype) {
        List<TbModule> modules = new ArrayList<>();

        try {
            // 根据维度类型映射到对应的能源类型
            Integer energyType = getEnergyTypeByNowtype(nowtype);
            log.info("🔍 维度类型 {} 映射到能源类型 {}", nowtype, energyType);

            for (String departId : departIds) {
                // 使用现有的 Mapper 方法查询仪表（支持 FIND_IN_SET）
                List<TbModule> moduleList = tbModuleMapper.selectModulesByOrgCode(departId);

                // 根据能源类型过滤仪表
                List<TbModule> filteredModules = moduleList.stream()
                        .filter(module -> energyType.equals(module.getEnergyType()))
                        .collect(Collectors.toList());

                modules.addAll(filteredModules);
            }

            // 去重（防止同一个仪表被多次添加）
            modules = modules.stream()
                    .collect(Collectors.toMap(TbModule::getModuleId, m -> m, (existing, replacement) -> existing))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(TbModule::getModuleName))
                    .collect(Collectors.toList());

            log.info("根据部门ID列表 {} 和维度类型 {} 查询到仪表数量：{}", departIds, nowtype, modules.size());

        } catch (Exception e) {
            log.error("根据部门ID列表和维度类型查询仪表失败: departIds={}, nowtype={}", departIds, nowtype, e);
        }

        return modules;
    }

    /**
     * 根据维度类型获取对应的能源类型
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 能源类型
     */
    private Integer getEnergyTypeByNowtype(Integer nowtype) {
        switch (nowtype) {
            case 1: // 按部门用电
            case 2: // 按线路用电
                return 1; // 电
            case 3: // 天然气
                return 8; // 天然气
            case 4: // 压缩空气
                return 5; // 压缩空气
            case 5: // 企业用水
                return 2; // 水
            default:
                log.warn("未知的维度类型: {}, 默认返回能源类型1(电)", nowtype);
                return 1; // 默认为电
        }
    }

    /**
     * 根据部门ID列表获取部门名称映射
     * @param departIds 部门ID列表
     * @return 部门ID到名称的映射
     */
    private Map<String, String> getDepartNameMapByIds(List<String> departIds) {
        Map<String, String> departNameMap = new HashMap<>();

        if (departIds != null && !departIds.isEmpty()) {
            try {
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("id", departIds);
                List<SysDepart> departs = sysDepartService.list(queryWrapper);

                for (SysDepart depart : departs) {
                    departNameMap.put(depart.getId(), depart.getDepartName());
                }
            } catch (Exception e) {
                log.error("查询部门名称失败", e);
            }
        }

        return departNameMap;
    }

    /**
     * 根据部门ID列表获取部门编码映射
     * @param departIds 部门ID列表
     * @return 部门ID到编码的映射
     */
    private Map<String, String> getDepartOrgCodeMapByIds(List<String> departIds) {
        Map<String, String> departOrgCodeMap = new HashMap<>();

        if (departIds != null && !departIds.isEmpty()) {
            try {
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("id", departIds);
                List<SysDepart> departs = sysDepartService.list(queryWrapper);

                for (SysDepart depart : departs) {
                    departOrgCodeMap.put(depart.getId(), depart.getOrgCode());
                }
            } catch (Exception e) {
                log.error("查询部门编码失败", e);
            }
        }

        return departOrgCodeMap;
    }

    /**
     * 获取维度名称映射 (已废弃，保留用于兼容)
     * @param modules 仪表列表
     * @return 维度编码到名称的映射
     */
    @Deprecated
    private Map<String, String> getDepartNameMap(List<TbModule> modules) {
        Map<String, String> departNameMap = new HashMap<>();

        // 收集所有唯一的维度编码
        List<String> orgCodes = modules.stream()
                .map(TbModule::getSysOrgCode)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!orgCodes.isEmpty()) {
            try {
                // 批量查询维度信息
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("org_code", orgCodes);
                List<SysDepart> departs = sysDepartService.list(queryWrapper);

                // 构建映射
                for (SysDepart depart : departs) {
                    departNameMap.put(depart.getOrgCode(), depart.getDepartName());
                }
            } catch (Exception e) {
                log.error("查询维度名称失败", e);
            }
        }

        return departNameMap;
    }

    /**
     * 获取仪表名称映射
     * @param moduleIds 仪表ID列表
     * @return 仪表ID到名称的映射
     */
    private Map<String, String> getModuleNameMap(List<String> moduleIds) {
        Map<String, String> moduleNameMap = new HashMap<>();

        if (moduleIds != null && !moduleIds.isEmpty()) {
            try {
                LambdaQueryWrapper<TbModule> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(TbModule::getModuleId, moduleIds);
                queryWrapper.select(TbModule::getModuleId, TbModule::getModuleName);

                List<TbModule> modules = tbModuleMapper.selectList(queryWrapper);
                for (TbModule module : modules) {
                    moduleNameMap.put(module.getModuleId(), module.getModuleName());
                }
            } catch (Exception e) {
                log.error("查询仪表名称失败", e);
            }
        }

        return moduleNameMap;
    }

    /**
     * 验证请求参数
     * @param request 请求参数
     */
    private void validateRequest(RealTimeDataRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }

        if (request.getModuleIds() == null || request.getModuleIds().isEmpty()) {
            throw new IllegalArgumentException("仪表编号列表不能为空");
        }

        if (request.getParameters() == null || request.getParameters().isEmpty()) {
            throw new IllegalArgumentException("参数编号列表不能为空");
        }

        if (!StringUtils.hasText(request.getStartTime())) {
            throw new IllegalArgumentException("开始时间不能为空");
        }

        if (!StringUtils.hasText(request.getEndTime())) {
            throw new IllegalArgumentException("结束时间不能为空");
        }

        if (request.getInterval() == null) {
            throw new IllegalArgumentException("查询间隔不能为空");
        }

        if (request.getDisplayMode() == null) {
            throw new IllegalArgumentException("显示方式不能为空");
        }

        // 验证显示方式的有效性
        if (request.getDisplayMode() != 1 && request.getDisplayMode() != 2) {
            throw new IllegalArgumentException("显示方式必须为1(统一显示)或2(分开显示)");
        }

        // 验证查询间隔的有效性
        if (request.getInterval() < 1 || request.getInterval() > 4) {
            throw new IllegalArgumentException("查询间隔必须为1-4之间的整数");
        }
    }


}