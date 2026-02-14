package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TeamDimensionRelation;

import java.util.List;

/**
 * @Description: 班组维度关联Mapper
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Mapper
public interface TeamDimensionRelationMapper extends BaseMapper<TeamDimensionRelation> {

    /**
     * 根据维度编码和维度类型查询班组列表
     * @param dimensionCode 维度编码
     * @param dimensionType 维度类型
     * @return 班组维度关联列表
     */
    List<TeamDimensionRelation> selectByDimensionCodeAndType(
            @Param("dimensionCode") String dimensionCode,
            @Param("dimensionType") Integer dimensionType
    );

    /**
     * 根据班组编码查询维度关联
     * @param teamCode 班组编码
     * @return 维度关联列表
     */
    List<TeamDimensionRelation> selectByTeamCode(@Param("teamCode") String teamCode);
}
