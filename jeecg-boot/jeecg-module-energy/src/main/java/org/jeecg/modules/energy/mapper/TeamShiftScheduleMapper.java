package org.jeecg.modules.energy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.TeamShiftSchedule;

import java.util.Date;
import java.util.List;

/**
 * @Description: 班组排班Mapper
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Mapper
public interface TeamShiftScheduleMapper extends BaseMapper<TeamShiftSchedule> {

    /**
     * 根据班组编码和日期查询排班信息
     * @param teamCode 班组编码
     * @param shiftDate 排班日期
     * @return 排班信息
     */
    TeamShiftSchedule selectByTeamCodeAndDate(
            @Param("teamCode") String teamCode,
            @Param("shiftDate") Date shiftDate
    );

    /**
     * 根据日期范围查询排班信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<TeamShiftSchedule> selectByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
