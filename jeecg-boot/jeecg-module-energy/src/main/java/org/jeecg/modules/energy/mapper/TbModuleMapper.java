package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.vo.analysis.ModuleVO;

import java.util.List;
import java.util.Map;


/**
 * @Description: 仪表基础信息Mapper
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Mapper
public interface TbModuleMapper extends BaseMapper<TbModule> {

    /**
     * 根据组织编码查询关联的仪表
     * @param orgCode 组织编码
     * @return 仪表列表
     */
    List<TbModule> selectModulesByOrgCode(@Param("orgCode") String orgCode);

    /**
     * 根据模块ID查询仪表信息
     * @param moduleId 模块ID
     * @return 仪表列表
     */
    /**
     * 批量查询模块的能源类型
     * @param moduleIds 仪表编号集合
     * @return 每条记录包含 module_id, energy_type, module_name
     */
    List<java.util.Map<String, Object>> selectEnergyTypeByModuleIds(@Param("moduleIds") java.util.List<String> moduleIds);

    List<TbModule> selectByModuleId(@Param("moduleId") String moduleId);

    /**
     * 根据维度ID列表获取仪表列表
     * @param dimensionIds 维度ID列表
     * @param energyType 能源类型
     * @return 仪表VO列表
     */
    List<ModuleVO> selectModulesByDimensionIds(@Param("dimensionIds") List<String> dimensionIds,
                                               @Param("energyType") Integer energyType);

    /**
     * 根据仪表编号获取仪表详细信息
     * @param moduleId 仪表编号
     * @return 仪表VO
     */
    ModuleVO selectModuleDetailByModuleId(@Param("moduleId") String moduleId);
}