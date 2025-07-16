package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;

import java.util.Date;

/**
 * @Description: 日统计表Mapper
 * @Author: jeecg-boot
 * @Date: 2025-01-20
 * @Version: V1.0
 */
@Mapper
public interface TbEpEquEnergyDaycountMapper extends BaseMapper<TbEpEquEnergyDaycount> {

    /**
     * 查询指定模块指定日期的统计记录
     * @param moduleId 模块ID
     * @param date 日期
     * @return 统计记录
     */
    TbEpEquEnergyDaycount selectByModuleIdAndDate(@Param("moduleId") String moduleId, @Param("date") Date date);

    /**
     * 根据仪表ID和日期查询当天的能耗数据（兼容旧方法名）
     * @param moduleId 仪表ID
     * @param date 日期
     * @return 能耗数据
     */
    TbEpEquEnergyDaycount selectTodayDataByModuleId(@Param("moduleId") String moduleId, @Param("date") Date date);
}