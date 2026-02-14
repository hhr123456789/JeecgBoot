package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TeamInfo;

import java.util.List;

/**
 * @Description: 班组基础信息Mapper
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Mapper
public interface TeamInfoMapper extends BaseMapper<TeamInfo> {

    /**
     * 根据组织编码查询班组列表
     * @param orgCode 组织编码
     * @return 班组列表
     */
    List<TeamInfo> selectByOrgCode(@Param("orgCode") String orgCode);

    /**
     * 根据班组编码查询班组信息
     * @param teamCode 班组编码
     * @return 班组信息
     */
    TeamInfo selectByTeamCode(@Param("teamCode") String teamCode);
}
