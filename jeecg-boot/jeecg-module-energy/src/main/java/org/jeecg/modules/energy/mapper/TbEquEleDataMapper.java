package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEquEleData;

/**
 * @Description: 电力数据Mapper
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@Mapper
public interface TbEquEleDataMapper extends BaseMapper<TbEquEleData> {
    
    /**
     * 根据仪表ID查询最新的电力实时数据
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEleData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
} 