package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEpEquEnergyYearcount;

import java.util.Date;

/**
 * @Description: 年统计表Mapper
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Mapper
public interface TbEpEquEnergyYearcountMapper extends BaseMapper<TbEpEquEnergyYearcount> {
    
    /**
     * 查询指定模块指定年份的统计记录
     * @param moduleId 模块ID
     * @param date 日期
     * @return 统计记录
     */
    TbEpEquEnergyYearcount selectByModuleIdAndYear(@Param("moduleId") String moduleId, @Param("date") Date date);
}