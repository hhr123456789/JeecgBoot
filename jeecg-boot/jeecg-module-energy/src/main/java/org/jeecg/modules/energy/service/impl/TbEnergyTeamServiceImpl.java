package org.jeecg.modules.energy.service.impl;

import org.jeecg.modules.energy.entity.TbEnergyTeam;
import org.jeecg.modules.energy.mapper.TbEnergyTeamMapper;
import org.jeecg.modules.energy.service.ITbEnergyTeamService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * @Description: 能耗班组表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@Service
public class TbEnergyTeamServiceImpl extends ServiceImpl<TbEnergyTeamMapper, TbEnergyTeam> implements ITbEnergyTeamService {

    @Autowired
    private TbEnergyTeamMapper tbEnergyTeamMapper;

    @Override
    public List<TbEnergyTeam> queryTeamsByDimension(String dimensionCode, String dimensionType) {
        return tbEnergyTeamMapper.selectByDimension(dimensionCode, dimensionType);
    }
}
