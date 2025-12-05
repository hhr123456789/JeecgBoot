package org.jeecg.modules.energy.mapper.classification;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.classification.TbEnergyTypeConfig;
import org.jeecg.modules.energy.vo.classification.EnergyTypeConfigVO;

import java.util.List;
import java.util.Map;

/**
 * 能源类型配置表Mapper接口
 * @author jeecg
 */
@Mapper
public interface TbEnergyTypeConfigMapper extends BaseMapper<TbEnergyTypeConfig> {

    /**
     * 根据能源类型编码获取配置信息
     */
    EnergyTypeConfigVO selectByEnergyType(@Param("energyType") Integer energyType);

    /**
     * 获取所有启用的能源类型配置
     */
    List<EnergyTypeConfigVO> selectAllEnabled();

    /**
     * 获取能源类型名称
     */
    String selectEnergyTypeName(@Param("energyType") Integer energyType);

    /**
     * 检查能源类型是否存在
     */
    int existsByEnergyType(@Param("energyType") Integer energyType);

    /**
     * 批量插入能源类型配置
     */
    int batchInsert(List<TbEnergyTypeConfig> list);

    /**
     * 根据能源类型更新配置
     */
    int updateByEnergyType(TbEnergyTypeConfig config);

    /**
     * 根据能源类型删除配置
     */
    int deleteByEnergyType(@Param("energyType") Integer energyType);

    /**
     * 分页查询能源类型配置
     */
    List<TbEnergyTypeConfig> selectListByPage(Map<String, Object> params);

    /**
     * 根据条件统计数量
     */
    int selectCountByCondition(Map<String, Object> params);
}