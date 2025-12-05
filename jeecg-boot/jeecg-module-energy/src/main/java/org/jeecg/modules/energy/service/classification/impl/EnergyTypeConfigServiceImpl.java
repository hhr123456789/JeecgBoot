package org.jeecg.modules.energy.service.classification.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.mapper.classification.TbEnergyTypeConfigMapper;
import org.jeecg.modules.energy.service.classification.IEnergyTypeConfigService;
import org.jeecg.modules.energy.vo.classification.EnergyTypeConfigVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 能源类型配置服务实现类
 * @author jeecg
 */
@Slf4j
@Service
public class EnergyTypeConfigServiceImpl extends ServiceImpl<TbEnergyTypeConfigMapper, TbEnergyTypeConfig> 
        implements IEnergyTypeConfigService {

    @Override
    public EnergyTypeConfigVO getConfigByEnergyType(Integer energyType) {
        try {
            log.debug("获取能源类型配置: energyType={}", energyType);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return null;
            }
            
            EnergyTypeConfigVO config = baseMapper.selectByEnergyType(energyType);
            
            if (config == null) {
                log.warn("未找到能源类型配置: energyType={}", energyType);
            } else {
                log.debug("成功获取能源类型配置: {}", config);
            }
            
            return config;
        } catch (Exception e) {
            log.error("获取能源类型配置失败: energyType={}", energyType, e);
            return null;
        }
    }

    @Override
    public List<EnergyTypeConfigVO> getAllEnabledConfigs() {
        try {
            log.debug("获取所有启用的能源类型配置");
            
            List<EnergyTypeConfigVO> configs = baseMapper.selectAllEnabled();
            
            log.info("成功获取 {} 个启用的能源类型配置", configs.size());
            
            return configs;
        } catch (Exception e) {
            log.error("获取启用的能源类型配置失败", e);
            return null;
        }
    }

    @Override
    public String getEnergyTypeName(Integer energyType) {
        try {
            log.debug("获取能源类型名称: energyType={}", energyType);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return null;
            }
            
            String energyName = baseMapper.selectEnergyTypeName(energyType);
            
            if (energyName == null) {
                log.warn("未找到能源类型名称: energyType={}", energyType);
            } else {
                log.debug("成功获取能源类型名称: {} -> {}", energyType, energyName);
            }
            
            return energyName;
        } catch (Exception e) {
            log.error("获取能源类型名称失败: energyType={}", energyType, e);
            return null;
        }
    }

    @Override
    public boolean existsByEnergyType(Integer energyType) {
        try {
            log.debug("检查能源类型是否存在: energyType={}", energyType);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return false;
            }
            
            int count = baseMapper.existsByEnergyType(energyType);
            
            boolean exists = count > 0;
            log.debug("能源类型存在性检查结果: energyType={}, exists={}", energyType, exists);
            
            return exists;
        } catch (Exception e) {
            log.error("检查能源类型存在性失败: energyType={}", energyType, e);
            return false;
        }
    }

    @Override
    public double calculateCost(Integer energyType, double consumption) {
        try {
            log.debug("计算能源费用: energyType={}, consumption={}", energyType, consumption);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return 0.0;
            }
            
            if (consumption < 0) {
                log.warn("消耗量不能为负数: consumption={}", consumption);
                return 0.0;
            }
            
            EnergyTypeConfigVO config = getConfigByEnergyType(energyType);
            
            if (config == null || config.getPricePerUnit() == null) {
                log.warn("未找到能源类型配置或价格信息: energyType={}", energyType);
                return 0.0;
            }
            
            double cost = consumption * config.getPricePerUnit().doubleValue();
            
            log.debug("计算能源费用结果: consumption={}, price={}, cost={}", 
                     consumption, config.getPricePerUnit(), cost);
            
            return cost;
        } catch (Exception e) {
            log.error("计算能源费用失败: energyType={}, consumption={}", energyType, consumption, e);
            return 0.0;
        }
    }

    @Override
    public double calculateCarbonEmission(Integer energyType, double consumption) {
        try {
            log.debug("计算碳排放量: energyType={}, consumption={}", energyType, consumption);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return 0.0;
            }
            
            if (consumption < 0) {
                log.warn("消耗量不能为负数: consumption={}", consumption);
                return 0.0;
            }
            
            EnergyTypeConfigVO config = getConfigByEnergyType(energyType);
            
            if (config == null || config.getCarbonFactor() == null) {
                log.warn("未找到能源类型配置或碳排放系数: energyType={}", energyType);
                return 0.0;
            }
            
            double carbonEmission = consumption * config.getCarbonFactor().doubleValue();
            
            log.debug("计算碳排放量结果: consumption={}, carbonFactor={}, carbonEmission={}", 
                     consumption, config.getCarbonFactor(), carbonEmission);
            
            return carbonEmission;
        } catch (Exception e) {
            log.error("计算碳排放量失败: energyType={}, consumption={}", energyType, consumption, e);
            return 0.0;
        }
    }

    @Override
    public double calculateStandardCoal(Integer energyType, double consumption) {
        try {
            log.debug("计算标准煤当量: energyType={}, consumption={}", energyType, consumption);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return 0.0;
            }
            
            if (consumption < 0) {
                log.warn("消耗量不能为负数: consumption={}", consumption);
                return 0.0;
            }
            
            EnergyTypeConfigVO config = getConfigByEnergyType(energyType);
            
            if (config == null || config.getCoalFactor() == null) {
                log.warn("未找到能源类型配置或标准煤系数: energyType={}", energyType);
                return 0.0;
            }
            
            double standardCoal = consumption * config.getCoalFactor().doubleValue();
            
            log.debug("计算标准煤当量结果: consumption={}, coalFactor={}, standardCoal={}", 
                     consumption, config.getCoalFactor(), standardCoal);
            
            return standardCoal;
        } catch (Exception e) {
            log.error("计算标准煤当量失败: energyType={}, consumption={}", energyType, consumption, e);
            return 0.0;
        }
    }

    @Override
    public boolean batchInsert(List<TbEnergyTypeConfig> configList) {
        try {
            log.info("批量插入能源类型配置: count={}", configList.size());
            
            if (configList == null || configList.isEmpty()) {
                log.warn("配置列表为空");
                return false;
            }
            
            int result = baseMapper.batchInsert(configList);
            
            boolean success = result > 0;
            log.info("批量插入能源类型配置结果: expected={}, actual={}, success={}", 
                    configList.size(), result, success);
            
            return success;
        } catch (Exception e) {
            log.error("批量插入能源类型配置失败", e);
            return false;
        }
    }

    @Override
    public boolean updateByEnergyType(Integer energyType, TbEnergyTypeConfig config) {
        try {
            log.debug("更新能源类型配置: energyType={}", energyType);
            
            if (energyType == null || config == null) {
                log.warn("参数不能为空: energyType={}, config={}", energyType, config);
                return false;
            }
            
            // 设置能源类型编码
            config.setEnergyType(energyType);
            
            int result = baseMapper.updateByEnergyType(config);
            
            boolean success = result > 0;
            log.debug("更新能源类型配置结果: energyType={}, result={}, success={}", 
                     energyType, result, success);
            
            return success;
        } catch (Exception e) {
            log.error("更新能源类型配置失败: energyType={}", energyType, e);
            return false;
        }
    }

    @Override
    public boolean deleteByEnergyType(Integer energyType) {
        try {
            log.debug("删除能源类型配置: energyType={}", energyType);
            
            if (energyType == null) {
                log.warn("能源类型编码不能为空");
                return false;
            }
            
            int result = baseMapper.deleteByEnergyType(energyType);
            
            boolean success = result > 0;
            log.debug("删除能源类型配置结果: energyType={}, result={}, success={}", 
                     energyType, result, success);
            
            return success;
        } catch (Exception e) {
            log.error("删除能源类型配置失败: energyType={}", energyType, e);
            return false;
        }
    }

    @Override
    public List<TbEnergyTypeConfig> getConfigListByPage(String status, Integer energyType, 
                                                       String energyName, Integer start, Integer end) {
        try {
            log.debug("获取能源类型配置列表（分页）: status={}, energyType={}, energyName={}, start={}, end={}", 
                     status, energyType, energyName, start, end);
            
            Map<String, Object> params = new HashMap<>();
            params.put("status", status);
            params.put("energyType", energyType);
            params.put("energyName", energyName);
            params.put("start", start);
            params.put("end", end);
            
            List<TbEnergyTypeConfig> configs = baseMapper.selectListByPage(params);
            
            log.debug("成功获取能源类型配置列表: count={}", configs.size());
            
            return configs;
        } catch (Exception e) {
            log.error("获取能源类型配置列表失败", e);
            return null;
        }
    }

    @Override
    public int getConfigCount(String status, Integer energyType, String energyName) {
        try {
            log.debug("获取能源类型配置总数: status={}, energyType={}, energyName={}", 
                     status, energyType, energyName);
            
            Map<String, Object> params = new HashMap<>();
            params.put("status", status);
            params.put("energyType", energyType);
            params.put("energyName", energyName);
            
            int count = baseMapper.selectCountByCondition(params);
            
            log.debug("成功获取能源类型配置总数: {}", count);
            
            return count;
        } catch (Exception e) {
            log.error("获取能源类型配置总数失败", e);
            return 0;
        }
    }
}