package org.jeecg.modules.energy.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 日统计数据
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
public interface TbEpEquEnergyDaycountMapper extends BaseMapper<TbEpEquEnergyDaycount> {
    
    /**
     * 根据仪表ID和日期查询当天的能耗数据
     * @param moduleId 仪表ID
     * @param date 日期
     * @return 能耗数据
     */
    TbEpEquEnergyDaycount selectTodayDataByModuleId(@Param("moduleId") String moduleId, @Param("date") Date date);
} 