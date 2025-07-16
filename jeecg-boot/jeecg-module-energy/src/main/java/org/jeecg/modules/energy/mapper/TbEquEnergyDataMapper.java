package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEquEnergyData;

/**
 * @Description: 能源数据Mapper
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Mapper
public interface TbEquEnergyDataMapper extends BaseMapper<TbEquEnergyData> {
    
    /**
     * 根据仪表ID查询最新的能源实时数据
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEnergyData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
} 