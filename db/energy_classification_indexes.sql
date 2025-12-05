-- 企业分类分区统计功能数据库索引优化脚本
-- 创建时间: 2025-11-10
-- 目的: 提升企业分类分区统计查询性能

-- ==================== 1. 分类统计汇总表索引优化 ====================

-- 复合主键索引：按部门+能源类型+时间维度查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_org_energy_time` (`org_code`, `energy_type`, `time_dimension`, `stat_date`);

-- 部门编码索引：用于部门维度统计
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_org_code` (`org_code`);

-- 能源类型索引：用于能源类型维度统计
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_energy_type` (`energy_type`);

-- 统计日期索引：用于时间范围查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_stat_date` (`stat_date`);

-- 时间维度索引：用于日/月/年维度查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_time_dimension` (`time_dimension`);

-- 父级部门索引：用于部门树形查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_parent_org_code` (`parent_org_code`);

-- 统计月份索引：用于月度数据查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_stat_month` (`stat_month`);

-- 统计年份索引：用于年度数据查询
ALTER TABLE `tb_energy_classification_summary` 
ADD INDEX `idx_stat_year` (`stat_year`);

-- ==================== 2. 能源类型配置表索引优化 ====================

-- 能源类型编码唯一索引：确保能源类型唯一性
ALTER TABLE `tb_energy_type_config` 
ADD UNIQUE INDEX `uk_energy_type` (`energy_type`);

-- 状态索引：用于查询启用的能源类型
ALTER TABLE `tb_energy_type_config` 
ADD INDEX `idx_status` (`status`);

-- 排序索引：用于配置列表查询
ALTER TABLE `tb_energy_type_config` 
ADD INDEX `idx_sort_order` (`sort_order`);

-- ==================== 3. 统计明细表索引优化 ====================

-- 汇总表ID索引：用于关联查询
ALTER TABLE `tb_energy_classification_detail` 
ADD INDEX `idx_summary_id` (`summary_id`);

-- 部门编码索引：用于部门维度查询
ALTER TABLE `tb_energy_classification_detail` 
ADD INDEX `idx_org_code` (`org_code`);

-- 仪表编号索引：用于仪表维度查询
ALTER TABLE `tb_energy_classification_detail` 
ADD INDEX `idx_module_id` (`module_id`);

-- 统计日期索引：用于时间范围查询
ALTER TABLE `tb_energy_classification_detail` 
ADD INDEX `idx_stat_date` (`stat_date`);

-- 复合索引：部门+日期查询
ALTER TABLE `tb_energy_classification_detail` 
ADD INDEX `idx_org_date` (`org_code`, `stat_date`);

-- ==================== 4. 实时数据表索引优化 ====================

-- 仪表编号+统计日期索引：用于按仪表和时间查询
ALTER TABLE `tb_ep_equ_energy_daycount` 
ADD INDEX `idx_module_date` (`module_id`, `dt`);

-- 统计日期索引：用于时间范围查询
ALTER TABLE `tb_ep_equ_energy_daycount` 
ADD INDEX `idx_dt` (`dt`);

-- ==================== 5. 仪表表索引优化 ====================

-- 部门编码索引：用于部门维度查询
ALTER TABLE `tb_module` 
ADD INDEX `idx_sys_org_code` (`sys_org_code`);

-- 能源类型索引：用于能源类型查询
ALTER TABLE `tb_module` 
ADD INDEX `idx_energy_type` (`energy_type`);

-- 仪表编号索引：用于仪表查询
ALTER TABLE `tb_module` 
ADD INDEX `idx_module_id` (`module_id`);

-- 复合索引：部门+能源类型查询
ALTER TABLE `tb_module` 
ADD INDEX `idx_org_energy_type` (`sys_org_code`, `energy_type`);

-- ==================== 6. 数据量大的优化建议 ====================

-- 如果数据量很大，建议：
-- 1. 使用SSD硬盘提升IO性能
-- 2. 适当增加数据库连接池大小
-- 3. 考虑读写分离架构
-- 4. 定期执行 OPTIMIZE TABLE 命令
-- 5. 监控慢查询并优化SQL语句

-- ==================== 7. 统计表优化 ====================

-- 更新表统计信息，优化查询计划
ANALYZE TABLE `tb_energy_classification_summary`;
ANALYZE TABLE `tb_energy_type_config`;
ANALYZE TABLE `tb_energy_classification_detail`;
ANALYZE TABLE `tb_ep_equ_energy_daycount`;
ANALYZE TABLE `tb_module`;

-- 优化表结构
OPTIMIZE TABLE `tb_energy_classification_summary`;
OPTIMIZE TABLE `tb_energy_type_config`;
OPTIMIZE TABLE `tb_energy_classification_detail`;

-- ==================== 8. 性能监控索引使用情况 ====================

-- 查看索引使用情况
EXPLAIN SELECT 
    org_code,
    energy_type,
    stat_date,
    time_dimension,
    SUM(total_consumption) as total_consumption,
    SUM(total_cost) as total_cost
FROM tb_energy_classification_summary 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-12-31'
    AND time_dimension = 'day'
    AND org_code IN ('A01', 'A02')
    AND energy_type IN (1, 2, 3)
GROUP BY org_code, energy_type, stat_date
ORDER BY org_code, energy_type, stat_date;

-- ==================== 9. 定期维护脚本 ====================

-- 创建定期索引重建的存储过程（可选）
DELIMITER $$

CREATE PROCEDURE RebuildEnergyClassificationIndexes()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(64);
    DECLARE cur CURSOR FOR 
        SELECT DISTINCT TABLE_NAME 
        FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME IN (
            'tb_energy_classification_summary',
            'tb_energy_type_config', 
            'tb_energy_classification_detail',
            'tb_ep_equ_energy_daycount',
            'tb_module'
        );
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO table_name;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET @sql = CONCAT('ANALYZE TABLE ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

    END LOOP;

    CLOSE cur;
END$$

DELIMITER ;

-- 注意：此版本不包含定期数据清理功能
-- 如需清理历史数据，请手动执行DELETE语句：
-- DELETE FROM tb_energy_classification_summary WHERE stat_date < '2024-01-01';
-- DELETE FROM tb_energy_classification_detail WHERE stat_date < '2024-01-01';

-- ==================== 10. 查询性能测试 ====================

-- 测试1: 按部门+能源类型+时间查询
EXPLAIN 
SELECT s.org_code, s.energy_type, s.stat_date, s.total_consumption, s.total_cost
FROM tb_energy_classification_summary s
WHERE s.org_code = 'A01' 
  AND s.energy_type = 1 
  AND s.stat_date BETWEEN '2024-01-01' AND '2024-01-31'
  AND s.time_dimension = 'day'
ORDER BY s.stat_date;

-- 测试2: 按时间范围聚合查询
EXPLAIN
SELECT 
    DATE_FORMAT(stat_date, '%Y-%m') as month,
    org_code,
    energy_type,
    SUM(total_consumption) as total_consumption,
    SUM(total_cost) as total_cost
FROM tb_energy_classification_summary
WHERE stat_date BETWEEN '2024-01-01' AND '2024-12-31'
  AND time_dimension = 'day'
GROUP BY DATE_FORMAT(stat_date, '%Y-%m'), org_code, energy_type
ORDER BY month, org_code, energy_type;

-- 测试3: 仪表数据关联查询
EXPLAIN
SELECT 
    m.sys_org_code as org_code,
    m.energy_type,
    COUNT(DISTINCT m.module_id) as meter_count,
    SUM(d.energy_count) as total_consumption
FROM tb_module m
LEFT JOIN tb_ep_equ_energy_daycount d ON m.module_id = d.module_id
WHERE d.dt BETWEEN '2024-01-01' AND '2024-01-31'
GROUP BY m.sys_org_code, m.energy_type
ORDER BY org_code, energy_type;