package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbModule;

import java.util.List;

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
    List<TbModule> selectByModuleId(@Param("moduleId") String moduleId);
} 