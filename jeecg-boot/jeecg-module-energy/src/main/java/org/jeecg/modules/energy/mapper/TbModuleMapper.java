package org.jeecg.modules.energy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbModule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 仪表基础信息
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
public interface TbModuleMapper extends BaseMapper<TbModule> {
    
    /**
     * 根据组织编码查询关联的仪表
     * @param orgCode 组织编码
     * @return 仪表列表
     */
    List<TbModule> selectModulesByOrgCode(@Param("orgCode") String orgCode);
} 