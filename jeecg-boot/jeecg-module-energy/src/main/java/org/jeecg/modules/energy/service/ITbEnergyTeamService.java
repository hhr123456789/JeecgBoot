package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.entity.TbEnergyTeam;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 能耗班组表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
public interface ITbEnergyTeamService extends IService<TbEnergyTeam> {

    /**
     * 根据维度查询班组
     * @param dimensionCode
     * @param dimensionType
     * @return
     */
    List<TbEnergyTeam> queryTeamsByDimension(String dimensionCode, String dimensionType);

}
