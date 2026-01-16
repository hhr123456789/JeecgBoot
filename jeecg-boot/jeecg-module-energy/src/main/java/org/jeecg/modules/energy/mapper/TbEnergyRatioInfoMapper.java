package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.energy.entity.TbEnergyRatioInfo;
import org.jeecg.modules.energy.vo.classification.EnergyTypeVO;

import java.util.List;

/**
 * 能源比例信息Mapper
 * @author jeecg
 * @date 2025-12-05
 */
@Mapper
public interface TbEnergyRatioInfoMapper extends BaseMapper<TbEnergyRatioInfo> {
    
    /**
     * 获取所有能源类型配置(用于前端下拉选择)
     * @return 能源类型列表
     */
    @Select("SELECT isenergy_type AS energyType, energy_name AS energyName, energy_unit AS energyUnit, " +
            "price_per_unit AS pricePerUnit, zbmxs_value AS coalFactor, tpfxs_value AS carbonFactor " +
            "FROM tb_energy_ratio_info " +
            "ORDER BY isenergy_type ASC")
    List<EnergyTypeVO> selectAllEnergyTypes();
    
    /**
     * 根据能源类型编码获取配置信息
     * @param energyType 能源类型编码
     * @return 能源类型配置
     */
    @Select("SELECT isenergy_type AS energyType, energy_name AS energyName, energy_unit AS energyUnit, " +
            "price_per_unit AS pricePerUnit, zbmxs_value AS coalFactor, tpfxs_value AS carbonFactor " +
            "FROM tb_energy_ratio_info " +
            "WHERE isenergy_type = #{energyType}")
    EnergyTypeVO selectByEnergyType(@Param("energyType") Integer energyType);
    
    /**
     * 根据orgCode查询该部门下设备的所有能源类型
     * @param orgCode 部门编码
     * @return 该部门下设备的能源类型列表
     */
    @Select("SELECT DISTINCT r.isenergy_type AS energyType, r.energy_name AS energyName, " +
            "r.energy_unit AS energyUnit, r.price_per_unit AS pricePerUnit, " +
            "r.zbmxs_value AS coalFactor, r.tpfxs_value AS carbonFactor " +
            "FROM tb_module m " +
            "INNER JOIN tb_energy_ratio_info r ON m.energy_type = r.isenergy_type " +
            "WHERE m.sys_org_code = #{orgCode} AND m.isaction = 'Y' " +
            "ORDER BY r.isenergy_type ASC")
    List<EnergyTypeVO> selectEnergyTypesByOrgCode(@Param("orgCode") String orgCode);
}
