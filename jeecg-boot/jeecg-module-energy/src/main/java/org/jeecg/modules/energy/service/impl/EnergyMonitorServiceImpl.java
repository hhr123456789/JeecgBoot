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
import org.jeecg.modules.energy.service.IEnergyMonitorService;
import org.jeecg.modules.energy.utils.EnergyCalculationUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
} 