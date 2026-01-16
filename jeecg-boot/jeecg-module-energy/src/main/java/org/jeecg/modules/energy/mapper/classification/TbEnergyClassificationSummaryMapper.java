package org.jeecg.modules.energy.mapper.classification;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.classification.TbEnergyClassificationSummary;
import org.jeecg.modules.energy.vo.classification.ClassificationStatisticsVO;

import java.util.Date;
import java.util.List;

/**
 * 企业分类分区统计汇总表Mapper接口
 * @author jeecg
 */
@Mapper
public interface TbEnergyClassificationSummaryMapper extends BaseMapper<TbEnergyClassificationSummary> {

    /**
     * 根据条件查询分类统计汇总数据
     */
    TbEnergyClassificationSummary selectByCondition(
            @Param("orgCode") String orgCode,
            @Param("energyType") Integer energyType,
            @Param("timeDimension") String timeDimension,
            @Param("statDate") Date statDate
    );

    /**
     * 按部门编码、能源类型、时间维度分组统计日数据
     */
    List<ClassificationStatisticsVO> selectStatisticsGroupByOrgAndEnergyType(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 按部门编码、能源类型、时间维度分组统计月数据
     */
    List<ClassificationStatisticsVO> selectMonthlyStatisticsGroupByOrgAndEnergyType(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 按部门编码、能源类型、时间维度分组统计数据
     */
    List<ClassificationStatisticsVO> selectYearlyStatisticsGroupByOrgAndEnergyType(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 删除指定时间范围的数据
     */
    int deleteByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 获取最新统计日期
     */
    Date getLatestStatisticsDate();

    /**
     * 检查汇总表中指定日期范围是否有数据
     * 用于判断数据是否已同步
     */
    long countByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}