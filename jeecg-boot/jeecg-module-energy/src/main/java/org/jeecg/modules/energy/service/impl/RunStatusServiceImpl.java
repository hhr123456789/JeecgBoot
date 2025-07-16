package org.jeecg.modules.energy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.TbEquEleData;
import org.jeecg.modules.energy.entity.TbEquEnergyData;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEquEleDataMapper;
import org.jeecg.modules.energy.mapper.TbEquEnergyDataMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IRunStatusService;
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
 * @Description: 设备运行状态服务实现类
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class RunStatusServiceImpl implements IRunStatusService {

    @Autowired
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;
    
    @Autowired
    private ISysDepartService sysDepartService;
    
    // 通讯故障时间阈值，单位：毫秒（1小时）
    private static final long COMMUNICATION_FAULT_THRESHOLD = 60 * 60 * 1000;
    
    // 能源类型常量
    private static final int ENERGY_TYPE_ELECTRIC = 1; // 电力
    private static final int ENERGY_TYPE_WATER = 2;    // 水
    private static final int ENERGY_TYPE_AIR = 5;      // 压缩空气
    private static final int ENERGY_TYPE_GAS = 8;      // 天然气
    
    @Override
    public List<Map<String, Object>> getRunStatus(String orgCode, Integer deviceStatus) {
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
        Date currentTime = new Date(); // 当前系统时间
        
        for (TbModule module : modules) {
            // 检查仪表是否有能源类型
            if (module.getEnergyType() == null) {
                log.warn("仪表 {} 未设置能源类型，跳过处理", module.getModuleId());
                continue;
            }
            
            Map<String, Object> dataMap = new HashMap<>();
            String runStatusText;
            int runStatusCode;
            Date lastCollectionTime = null;
            
            // 根据不同能源类型处理数据
            switch (module.getEnergyType()) {
                case ENERGY_TYPE_ELECTRIC:
                    // 处理电力类型
                    TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(module.getModuleId());
                    if (eleData == null) {
                        log.warn("未找到仪表 {} 的电力数据", module.getModuleId());
                        continue;
                    }
                    
                    // 获取最后采集时间
                    lastCollectionTime = eleData.getEquElectricDT();
                    
                    // 判断设备运行状态
                    runStatusText = determineElectricRunStatus(eleData, lastCollectionTime, currentTime);
                    runStatusCode = getRunStatusCode(runStatusText);
                    
                    // 添加电力特有数据
                    dataMap.put("电流(A)", eleData.getIA());
                    dataMap.put("电压(V)", eleData.getUA());
                    dataMap.put("功率因素", eleData.getPFS());
                    dataMap.put("有功功率(kW)", eleData.getPp());
                    break;
                    
                case ENERGY_TYPE_WATER:
                case ENERGY_TYPE_AIR:
                case ENERGY_TYPE_GAS:
                    // 处理水、压缩空气、天然气类型
                    TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(module.getModuleId());
                    if (energyData == null) {
                        log.warn("未找到仪表 {} 的能源数据", module.getModuleId());
                        continue;
                    }
                    
                    // 获取最后采集时间
                    lastCollectionTime = energyData.getEquEnergyDt();
                    
                    // 判断设备运行状态
                    runStatusText = determineEnergyRunStatus(energyData, lastCollectionTime, currentTime);
                    runStatusCode = getRunStatusCode(runStatusText);
                    
                    // 添加能源特有数据
                    if (module.getEnergyType() == ENERGY_TYPE_WATER) {
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    } else if (module.getEnergyType() == ENERGY_TYPE_AIR) {
                        dataMap.put("压力(MPa)", energyData.getEnergyPressure());
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    } else { // 天然气
                        dataMap.put("温度(℃)", energyData.getEnergyTemperature());
                        dataMap.put("压力(MPa)", energyData.getEnergyPressure());
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    }
                    break;
                    
                default:
                    log.warn("未知能源类型: {}, 仪表ID: {}", module.getEnergyType(), module.getModuleId());
                    continue;
            }
            
            // 根据筛选条件过滤
            if (deviceStatus != null && deviceStatus != 0) {
                if (deviceStatus != runStatusCode) {
                    continue;
                }
            }
            
            // 获取所属车间信息
            String workshopName = getWorkshopName(module.getSysOrgCode());
            
            // 添加通用数据
            dataMap.put("device_name", module.getModuleName());
            dataMap.put("所属车间", workshopName);
            dataMap.put("运行状态", runStatusText);
            dataMap.put("status_code", runStatusCode);
            dataMap.put("energy_type", module.getEnergyType());
            dataMap.put("last_collection_time", lastCollectionTime);
            
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
    
    /**
     * 判断电力设备运行状态
     * @param eleData 电力数据
     * @param lastCollectionTime 最后采集时间
     * @param currentTime 当前系统时间
     * @return 运行状态文本
     */
    private String determineElectricRunStatus(TbEquEleData eleData, Date lastCollectionTime, Date currentTime) {
        if (eleData == null) {
            return "未知";
        }
        
        // 检查是否为通讯故障（最后采集时间超过1小时）
        if (lastCollectionTime != null && currentTime != null) {
            long timeDiff = currentTime.getTime() - lastCollectionTime.getTime();
            if (timeDiff > COMMUNICATION_FAULT_THRESHOLD) {
                return "通讯故障";
            }
        }
        
        // 移除故障状态判断，因为实际中没有isrunState=2的状态
        // 根据电流值判断设备是否在运行
        // 如果三相电流都接近0，则认为设备已停止
        BigDecimal threshold = new BigDecimal("0.5");
        if (eleData.getIA() != null && eleData.getIB() != null && eleData.getIC() != null &&
                eleData.getIA().compareTo(threshold) <= 0 && 
                eleData.getIB().compareTo(threshold) <= 0 && 
                eleData.getIC().compareTo(threshold) <= 0) {
            return "已停止";
        }
        
        return "运行中";
    }
    
    /**
     * 判断其他能源设备运行状态
     * @param energyData 能源数据
     * @param lastCollectionTime 最后采集时间
     * @param currentTime 当前系统时间
     * @return 运行状态文本
     */
    private String determineEnergyRunStatus(TbEquEnergyData energyData, Date lastCollectionTime, Date currentTime) {
        if (energyData == null) {
            return "未知";
        }
        
        // 检查是否为通讯故障（最后采集时间超过1小时）
        if (lastCollectionTime != null && currentTime != null) {
            long timeDiff = currentTime.getTime() - lastCollectionTime.getTime();
            if (timeDiff > COMMUNICATION_FAULT_THRESHOLD) {
                return "通讯故障";
            }
        }
        
        // 根据瞬时流量判断设备是否在运行
        BigDecimal threshold = new BigDecimal("0.1");
        if (energyData.getEnergyWinkvalue() != null && 
                energyData.getEnergyWinkvalue().compareTo(threshold) <= 0) {
            return "已停止";
        }
        
        return "运行中";
    }
    
    /**
     * 获取运行状态代码
     * @param statusText 运行状态文本
     * @return 状态代码(0:全部, 1:运行中, 2:已停止, 3:通讯故障)
     */
    private int getRunStatusCode(String statusText) {
        switch (statusText) {
            case "运行中":
                return 1;
            case "已停止":
                return 2;
            case "通讯故障":
                return 3;
            default:
                return 0;
        }
    }
    
    /**
     * 获取所属车间名称
     * @param sysOrgCode 组织编码
     * @return 车间名称
     */
    private String getWorkshopName(String sysOrgCode) {
        if (sysOrgCode == null) {
            return "";
        }
        
        try {
            // 从组织编码中提取车间信息
            String[] orgCodes = sysOrgCode.split(",");
            if (orgCodes.length > 0) {
                String workshopCode = orgCodes[0];
                QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("id", workshopCode);
                SysDepart depart = sysDepartService.getOne(queryWrapper);
                if (depart != null) {
                    return depart.getDepartName();
                }
            }
        } catch (Exception e) {
            log.error("获取车间名称失败", e);
        }
        
        return "";
    }
} 