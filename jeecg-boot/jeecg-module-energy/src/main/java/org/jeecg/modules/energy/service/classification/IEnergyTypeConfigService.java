package org.jeecg.modules.energy.service.classification;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.vo.classification.EnergyTypeConfigVO;

import java.util.List;

/**
 * 能源类型配置服务接口
 * @author jeecg
 */
public interface IEnergyTypeConfigService extends IService<TbEnergyTypeConfig> {

    /**
     * 根据能源类型编码获取配置信息
     * @param energyType 能源类型编码
     * @return 能源类型配置信息
     */
    EnergyTypeConfigVO getConfigByEnergyType(Integer energyType);

    /**
     * 获取所有启用的能源类型配置
     * @return 能源类型配置列表
     */
    List<EnergyTypeConfigVO> getAllEnabledConfigs();

    /**
     * 获取能源类型名称
     * @param energyType 能源类型编码
     * @return 能源类型名称
     */
    String getEnergyTypeName(Integer energyType);

    /**
     * 检查能源类型是否存在
     * @param energyType 能源类型编码
     * @return true:存在 false:不存在
     */
    boolean existsByEnergyType(Integer energyType);

    /**
     * 根据价格计算费用
     * @param energyType 能源类型编码
     * @param consumption 消耗量
     * @return 计算结果
     */
    double calculateCost(Integer energyType, double consumption);

    /**
     * 根据碳排放系数计算碳排放量
     * @param energyType 能源类型编码
     * @param consumption 消耗量
     * @return 碳排放量
     */
    double calculateCarbonEmission(Integer energyType, double consumption);

    /**
     * 根据标准煤系数计算标准煤当量
     * @param energyType 能源类型编码
     * @param consumption 消耗量
     * @return 标准煤当量
     */
    double calculateStandardCoal(Integer energyType, double consumption);

    /**
     * 批量插入能源类型配置
     * @param configList 配置列表
     * @return 插入结果
     */
    boolean batchInsert(List<TbEnergyTypeConfig> configList);

    /**
     * 更新能源类型配置
     * @param energyType 能源类型编码
     * @param config 配置信息
     * @return 更新结果
     */
    boolean updateByEnergyType(Integer energyType, TbEnergyTypeConfig config);

    /**
     * 删除能源类型配置
     * @param energyType 能源类型编码
     * @return 删除结果
     */
    boolean deleteByEnergyType(Integer energyType);

    /**
     * 获取能源类型配置列表（分页）
     * @param status 状态
     * @param energyType 能源类型编码
     * @param energyName 能源类型名称
     * @param start 开始位置
     * @param end 结束位置
     * @return 配置列表
     */
    List<TbEnergyTypeConfig> getConfigListByPage(String status, Integer energyType, 
                                               String energyName, Integer start, Integer end);

    /**
     * 获取能源类型配置总数
     * @param status 状态
     * @param energyType 能源类型编码
     * @param energyName 能源类型名称
     * @return 总数
     */
    int getConfigCount(String status, Integer energyType, String energyName);
}