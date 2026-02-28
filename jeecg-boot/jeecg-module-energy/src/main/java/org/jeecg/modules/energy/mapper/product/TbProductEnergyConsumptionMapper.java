package org.jeecg.modules.energy.mapper.product;

import org.jeecg.modules.energy.entity.product.TbProductEnergyConsumption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Description: 产品能耗统计Mapper接口
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
public interface TbProductEnergyConsumptionMapper extends BaseMapper<TbProductEnergyConsumption> {

    /**
     * 获取产品能耗统计数据
     */
    @Select("<script>" +
            "SELECT " +
            "SUM(e.total_consumption) as totalConsumption, " +
            "SUM(e.total_production) as totalProduction, " +
            "SUM(e.qualified_production) as qualifiedProduction, " +
            "ROUND(SUM(e.qualified_production) * 100.0 / NULLIF(SUM(e.total_production), 0), 2) as qualificationRate, " +
            "ROUND(SUM(e.total_consumption) / NULLIF(SUM(e.qualified_production), 0), 2) as unitConsumption, " +
            "MAX(e.energy_unit) as energyUnit " +
            "FROM tb_product_energy_consumption e " +
            "<if test='categoryId != null and categoryId != \"\"'>" +
            "LEFT JOIN tb_product_info p ON e.product_code = p.product_code " +
            "</if>" +
            "WHERE e.time_dimension = #{timeDimension} " +
            "AND e.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "<if test='energyType != null'> AND e.energy_type = #{energyType} </if>" +
            "<if test='categoryId != null and categoryId != \"\"'> AND p.category_id = #{categoryId} </if>" +
            "</script>")
    Map<String, Object> getStatistics(@Param("timeDimension") String timeDimension,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("energyType") Integer energyType,
                                     @Param("categoryId") String categoryId);

    /**
     * 获取产品能耗分布数据
     */
    @Select("<script>" +
            "SELECT " +
            "p.product_name as name, " +
            "SUM(e.total_consumption) as value " +
            "FROM tb_product_energy_consumption e " +
            "LEFT JOIN tb_product_info p ON e.product_code = p.product_code " +
            "WHERE e.time_dimension = #{timeDimension} " +
            "AND e.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "<if test='energyType != null'> AND e.energy_type = #{energyType} </if>" +
            "<if test='categoryId != null and categoryId != \"\"'> AND p.category_id = #{categoryId} </if>" +
            "GROUP BY e.product_code, p.product_name " +
            "ORDER BY value DESC" +
            "</script>")
    List<Map<String, Object>> getDistribution(@Param("timeDimension") String timeDimension,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("energyType") Integer energyType,
                                              @Param("categoryId") String categoryId);

    /**
     * 获取产品单耗排名
     */
    @Select("<script>" +
            "SELECT " +
            "p.product_name as name, " +
            "ROUND(SUM(e.total_consumption) / NULLIF(SUM(e.qualified_production), 0), 2) as value " +
            "FROM tb_product_energy_consumption e " +
            "LEFT JOIN tb_product_info p ON e.product_code = p.product_code " +
            "WHERE e.time_dimension = #{timeDimension} " +
            "AND e.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "<if test='energyType != null'> AND e.energy_type = #{energyType} </if>" +
            "<if test='categoryId != null and categoryId != \"\"'> AND p.category_id = #{categoryId} </if>" +
            "GROUP BY e.product_code, p.product_name " +
            "ORDER BY value ASC" +
            "</script>")
    List<Map<String, Object>> getRanking(@Param("timeDimension") String timeDimension,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("energyType") Integer energyType,
                                        @Param("categoryId") String categoryId);

    /**
     * 获取产量与能耗对比数据
     */
    @Select("<script>" +
            "SELECT " +
            "p.product_name as name, " +
            "SUM(e.total_production) as production, " +
            "SUM(e.total_consumption) as consumption " +
            "FROM tb_product_energy_consumption e " +
            "LEFT JOIN tb_product_info p ON e.product_code = p.product_code " +
            "WHERE e.time_dimension = #{timeDimension} " +
            "AND e.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "<if test='energyType != null'> AND e.energy_type = #{energyType} </if>" +
            "<if test='categoryId != null and categoryId != \"\"'> AND p.category_id = #{categoryId} </if>" +
            "GROUP BY e.product_code, p.product_name " +
            "ORDER BY consumption DESC" +
            "</script>")
    List<Map<String, Object>> getComparison(@Param("timeDimension") String timeDimension,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("energyType") Integer energyType,
                                           @Param("categoryId") String categoryId);

    /**
     * 获取明细列表数据（带产品名称）
     */
    @Select("<script>" +
            "SELECT " +
            "e.id, " +
            "CASE WHEN e.time_dimension = 'month' THEN e.stat_month " +
            "     WHEN e.time_dimension = 'year' THEN e.stat_year " +
            "     ELSE DATE_FORMAT(e.stat_date, '%Y-%m-%d') END as time, " +
            "p.product_name as productName, " +
            "e.energy_type_name as energyTypeName, " +
            "e.total_production as production, " +
            "e.qualified_production as qualified, " +
            "e.qualification_rate as qualificationRate, " +
            "e.total_consumption as totalConsumption, " +
            "e.unit_consumption as unitConsumption, " +
            "e.energy_unit as energyUnit " +
            "FROM tb_product_energy_consumption e " +
            "LEFT JOIN tb_product_info p ON e.product_code = p.product_code " +
            "WHERE e.time_dimension = #{timeDimension} " +
            "AND e.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "<if test='energyType != null'> AND e.energy_type = #{energyType} </if>" +
            "<if test='categoryId != null and categoryId != \"\"'> AND p.category_id = #{categoryId} </if>" +
            "ORDER BY e.stat_date DESC, e.product_code" +
            "</script>")
    List<Map<String, Object>> getDetailList(@Param("timeDimension") String timeDimension,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("energyType") Integer energyType,
                                           @Param("categoryId") String categoryId);

    /**
     * 根据分类ID获取产品编码列表
     */
    @Select("<script>" +
            "SELECT product_code FROM tb_product_info WHERE 1=1 " +
            "<if test='categoryId != null and categoryId != \"\"'> AND category_id = #{categoryId} </if>" +
            "</script>")
    List<String> getProductCodesByCategory(@Param("categoryId") String categoryId);

    /**
     * 根据产品编码获取产品名称
     */
    @Select("<script>" +
            "SELECT product_code as code, product_name as name " +
            "FROM tb_product_info " +
            "WHERE product_code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> getProductNamesByCodes(@Param("codes") List<String> codes);

    /**
     * 获取可用的能源类型列表
     */
    @Select("SELECT DISTINCT energy_type as value, energy_type_name as label, energy_unit as unit " +
            "FROM tb_product_energy_consumption " +
            "WHERE energy_type IS NOT NULL " +
            "ORDER BY energy_type")
    List<Map<String, Object>> getEnergyTypes();
}
