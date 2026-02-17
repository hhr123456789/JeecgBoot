package org.jeecg.modules.energy.mapper.process;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.process.TbProductionLineEnergy;

import java.util.List;

/**
 * @Description: 生产线能源类型关联表Mapper
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface TbProductionLineEnergyMapper extends BaseMapper<TbProductionLineEnergy> {

    /**
     * 根据生产线ID和能源类型查询
     */
    TbProductionLineEnergy selectByLineIdAndEnergyType(
            @Param("lineId") String lineId,
            @Param("energyType") Integer energyType);

    /**
     * 根据生产线ID查询所有能源类型配置
     */
    List<TbProductionLineEnergy> selectByLineId(@Param("lineId") String lineId);
}
