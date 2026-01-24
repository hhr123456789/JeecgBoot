package org.jeecg.modules.energy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TbEnergyTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 能耗班组表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
public interface TbEnergyTeamMapper extends BaseMapper<TbEnergyTeam> {

    /**
     * 根据维度查询班组
     * @param dimensionCode
     * @param dimensionType
     * @return
     */
    List<TbEnergyTeam> selectByDimension(@Param("dimensionCode") String dimensionCode, @Param("dimensionType") String dimensionType);
}
