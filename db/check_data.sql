-- ============================================
-- 企业分类分区统计定时任务数据诊断SQL
-- 用于排查定时任务为什么没有生成数据
-- ============================================

-- ========== 第一部分：检查源数据表 ==========

-- 1. 检查实时统计表数据量
SELECT '1. 实时统计表(tb_ep_equ_energy_daycount)数据量' AS check_item;
SELECT
    COUNT(*) AS total_records,
    MIN(dt) AS earliest_date,
    MAX(dt) AS latest_date,
    COUNT(DISTINCT module_id) AS unique_modules
FROM tb_ep_equ_energy_daycount;

-- 2. 检查最近7天的实时数据
SELECT '2. 最近7天的实时数据' AS check_item;
SELECT
    DATE(dt) AS stat_date,
    COUNT(*) AS record_count,
    COUNT(DISTINCT module_id) AS module_count,
    SUM(energy_count) AS total_energy
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(dt)
ORDER BY DATE(dt) DESC;

-- 3. 检查仪表表数据
SELECT '3. 仪表表(tb_module)数据' AS check_item;
SELECT
    COUNT(*) AS total_modules,
    COUNT(CASE WHEN isaction = 'Y' THEN 1 END) AS active_modules,
    COUNT(CASE WHEN sys_org_code IS NOT NULL AND sys_org_code != '' THEN 1 END) AS modules_with_org_code,
    COUNT(CASE WHEN energy_type IS NOT NULL THEN 1 END) AS modules_with_energy_type
FROM tb_module;

-- 4. 检查仪表按部门和能源类型分布
SELECT '4. 仪表按部门和能源类型分布' AS check_item;
SELECT
    sys_org_code,
    energy_type,
    COUNT(*) AS module_count,
    SUM(CASE WHEN isaction = 'Y' THEN 1 ELSE 0 END) AS active_count
FROM tb_module
WHERE sys_org_code IS NOT NULL AND sys_org_code != ''
GROUP BY sys_org_code, energy_type
ORDER BY sys_org_code, energy_type;

-- 5. 检查实时数据与仪表的关联情况（最近7天）
SELECT '5. 实时数据与仪表的关联情况(最近7天)' AS check_item;
SELECT
    m.sys_org_code,
    m.energy_type,
    DATE(d.dt) AS stat_date,
    COUNT(*) AS record_count,
    COUNT(DISTINCT d.module_id) AS module_count,
    SUM(d.energy_count) AS total_consumption
FROM tb_ep_equ_energy_daycount d
INNER JOIN tb_module m ON d.module_id = m.module_id
WHERE m.isaction = 'Y'
  AND d.dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY m.sys_org_code, m.energy_type, DATE(d.dt)
ORDER BY DATE(d.dt) DESC, m.sys_org_code, m.energy_type;

-- 6. 检查能源类型配置表
SELECT '6. 能源类型配置表(tb_energy_ratio_info)' AS check_item;
SELECT
    isenergy_type AS energy_type,
    energy_name,
    energy_unit,
    price_per_unit,
    tpfxs_value AS carbon_factor,
    zbmxs_value AS coal_factor
FROM tb_energy_ratio_info
ORDER BY isenergy_type;

-- ========== 第二部分：检查目标数据表 ==========

-- 7. 检查分类统计汇总表数据
SELECT '7. 分类统计汇总表(tb_energy_classification_summary)数据量' AS check_item;
SELECT
    COUNT(*) AS total_records,
    MIN(stat_date) AS earliest_date,
    MAX(stat_date) AS latest_date,
    COUNT(DISTINCT org_code) AS unique_org_codes,
    COUNT(DISTINCT energy_type) AS unique_energy_types
FROM tb_energy_classification_summary;

-- 8. 检查汇总表按时间维度分布
SELECT '8. 汇总表按时间维度分布' AS check_item;
SELECT
    time_dimension,
    COUNT(*) AS record_count,
    MIN(stat_date) AS earliest_date,
    MAX(stat_date) AS latest_date
FROM tb_energy_classification_summary
GROUP BY time_dimension;

-- 9. 检查汇总表最近的数据
SELECT '9. 汇总表最近的数据' AS check_item;
SELECT
    stat_date,
    org_code,
    energy_type,
    energy_type_name,
    time_dimension,
    total_consumption,
    total_cost,
    meter_count
FROM tb_energy_classification_summary
ORDER BY stat_date DESC, org_code, energy_type
LIMIT 20;

-- ========== 第三部分：诊断问题 ==========

-- 10. 检查是否有未同步的数据（实时表有数据但汇总表没有）
SELECT '10. 未同步的日期（实时表有数据但汇总表没有）' AS check_item;
SELECT DISTINCT DATE(d.dt) AS unsynced_date
FROM tb_ep_equ_energy_daycount d
INNER JOIN tb_module m ON d.module_id = m.module_id
WHERE m.isaction = 'Y'
  AND m.sys_org_code IS NOT NULL
  AND m.sys_org_code != ''
  AND DATE(d.dt) NOT IN (
    SELECT DISTINCT stat_date
    FROM tb_energy_classification_summary
  )
ORDER BY unsynced_date DESC
LIMIT 10;

-- 11. 检查有问题的仪表（缺少关键信息）
SELECT '11. 有问题的仪表（缺少关键信息）' AS check_item;
SELECT
    module_id,
    module_name,
    sys_org_code,
    energy_type,
    isaction,
    CASE
        WHEN sys_org_code IS NULL OR sys_org_code = '' THEN '缺少部门编码'
        WHEN energy_type IS NULL THEN '缺少能源类型'
        WHEN isaction != 'Y' THEN '未启用'
        ELSE '正常'
    END AS issue
FROM tb_module
WHERE (sys_org_code IS NULL OR sys_org_code = '' OR energy_type IS NULL OR isaction != 'Y')
LIMIT 20;

-- 12. 模拟定时任务的SQL查询（检查是否能查到数据）
SELECT '12. 模拟定时任务SQL查询（昨天的数据）' AS check_item;
SELECT
    m.sys_org_code as org_code,
    m.energy_type,
    DATE(d.dt) as stat_date,
    DATE_FORMAT(d.dt, '%Y-%m') as stat_month,
    DATE_FORMAT(d.dt, '%Y') as stat_year,
    COALESCE(SUM(d.energy_count), 0) as total_consumption,
    COALESCE(SUM(d.peak_count), 0) as peak_consumption,
    COALESCE(SUM(d.level_count), 0) as flat_consumption,
    COALESCE(SUM(d.valley_count), 0) as valley_consumption,
    COALESCE(SUM(d.cusp_count), 0) as cusp_consumption,
    COUNT(DISTINCT d.module_id) as meter_count
FROM tb_ep_equ_energy_daycount d
INNER JOIN tb_module m ON d.module_id = m.module_id
WHERE m.isaction = 'Y'
AND d.dt >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)
AND d.dt < CURDATE()
GROUP BY m.sys_org_code, m.energy_type, DATE(d.dt)
ORDER BY m.sys_org_code, m.energy_type, DATE(d.dt);

-- ========== 第四部分：2025年数据检查 ==========

-- 13. 检查2025年数据是否存在
SELECT '13. 检查2025年数据' AS check_item;
SELECT stat_year, COUNT(*) as count
FROM tb_energy_classification_summary
WHERE stat_year = '2025'
GROUP BY stat_year;

-- 14. 检查2025年日数据
SELECT '14. 检查2025年日数据' AS check_item;
SELECT time_dimension, COUNT(*) as count
FROM tb_energy_classification_summary
WHERE time_dimension = 'day' AND stat_year = '2025'
GROUP BY time_dimension;

-- 15. 检查2025年月数据
SELECT '15. 检查2025年月数据' AS check_item;
SELECT time_dimension, COUNT(*) as count
FROM tb_energy_classification_summary
WHERE time_dimension = 'month' AND stat_year = '2025'
GROUP BY time_dimension;

-- ========== 第五部分：快速诊断汇总 ==========

-- 16. 快速诊断汇总（一次性检查所有关键指标）
SELECT '16. 快速诊断汇总' AS check_item;
SELECT
    (SELECT COUNT(*) FROM tb_ep_equ_energy_daycount) AS '实时表总记录数',
    (SELECT COUNT(*) FROM tb_ep_equ_energy_daycount WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)) AS '最近7天实时数据',
    (SELECT COUNT(*) FROM tb_module) AS '仪表总数',
    (SELECT COUNT(*) FROM tb_module WHERE isaction = 'Y') AS '已启用仪表数',
    (SELECT COUNT(*) FROM tb_module WHERE sys_org_code IS NOT NULL AND sys_org_code != '') AS '有部门编码的仪表数',
    (SELECT COUNT(*) FROM tb_module WHERE energy_type IS NOT NULL) AS '有能源类型的仪表数',
    (SELECT COUNT(*) FROM tb_module WHERE isaction = 'Y' AND sys_org_code IS NOT NULL AND sys_org_code != '' AND energy_type IS NOT NULL) AS '配置完整的仪表数',
    (SELECT COUNT(*) FROM tb_energy_classification_summary) AS '汇总表总记录数',
    (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)) AS '最近7天汇总数据';

-- 17. 诊断结果判断
SELECT '17. 问题诊断结果' AS check_item;
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM tb_ep_equ_energy_daycount WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)) = 0
        THEN '问题1: 实时表最近7天没有数据，请检查数据采集系统'
        WHEN (SELECT COUNT(*) FROM tb_module WHERE isaction = 'Y') = 0
        THEN '问题2: 没有启用的仪表，请执行: UPDATE tb_module SET isaction = ''Y'' WHERE module_id IN (...)'
        WHEN (SELECT COUNT(*) FROM tb_module WHERE isaction = 'Y' AND (sys_org_code IS NULL OR sys_org_code = '')) > 0
        THEN '问题3: 存在启用但缺少部门编码的仪表，请执行第19项SQL修复'
        WHEN (SELECT COUNT(*) FROM tb_module WHERE isaction = 'Y' AND energy_type IS NULL) > 0
        THEN '问题4: 存在启用但缺少能源类型的仪表，请执行第20项SQL修复'
        WHEN (SELECT COUNT(*) FROM tb_ep_equ_energy_daycount d
              INNER JOIN tb_module m ON d.module_id = m.module_id
              WHERE m.isaction = 'Y' AND d.dt >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)) = 0
        THEN '问题5: 实时表和仪表表无法关联，请检查module_id是否匹配'
        ELSE '数据配置正常，请检查定时任务是否正常运行'
    END AS diagnosis_result;

-- 18. 检查module_id关联情况
SELECT '18. 检查module_id关联失败的记录' AS check_item;
SELECT
    d.module_id AS unmatched_module_id,
    COUNT(*) AS record_count,
    MIN(d.dt) AS earliest_date,
    MAX(d.dt) AS latest_date
FROM tb_ep_equ_energy_daycount d
LEFT JOIN tb_module m ON d.module_id = m.module_id
WHERE m.module_id IS NULL
GROUP BY d.module_id
ORDER BY record_count DESC
LIMIT 10;

-- ========== 第六部分：修复SQL模板 ==========

-- 19. 修复缺少部门编码的仪表（示例，需要根据实际情况修改）
SELECT '19. 修复缺少部门编码的仪表 - 请根据实际情况修改' AS fix_template;
-- 查看需要修复的仪表
SELECT module_id, module_name, sys_org_code, energy_type, isaction
FROM tb_module
WHERE isaction = 'Y' AND (sys_org_code IS NULL OR sys_org_code = '');

-- 修复SQL示例（取消注释并修改后执行）：
-- UPDATE tb_module SET sys_org_code = '部门ID' WHERE module_id = 'xxx';

-- 20. 修复缺少能源类型的仪表（示例）
SELECT '20. 修复缺少能源类型的仪表 - 请根据实际情况修改' AS fix_template;
-- 查看需要修复的仪表
SELECT module_id, module_name, sys_org_code, energy_type, isaction
FROM tb_module
WHERE isaction = 'Y' AND energy_type IS NULL;

-- 修复SQL示例（1=电能, 2=水能, 3=燃气）：
-- UPDATE tb_module SET energy_type = 1 WHERE module_id = 'xxx';

-- 21. 批量启用仪表（示例）
SELECT '21. 批量启用仪表 - 请根据实际情况修改' AS fix_template;
-- 查看未启用的仪表
SELECT module_id, module_name, isaction
FROM tb_module
WHERE isaction != 'Y' OR isaction IS NULL
LIMIT 20;

-- 启用SQL示例：
-- UPDATE tb_module SET isaction = 'Y' WHERE module_id IN ('id1', 'id2', 'id3');

-- ========== 第七部分：手动触发同步 ==========

-- 22. 手动同步说明
SELECT '22. 手动触发同步的方法' AS manual_sync_guide;
SELECT '方法1: 调用REST API' AS method,
       'GET /jeecg-boot/energy/classification/syncByDate?targetDate=2025-01-13' AS url,
       '手动触发指定日期的数据同步' AS description
UNION ALL
SELECT '方法2: 调用REST API',
       'GET /jeecg-boot/energy/classification/triggerSync?startDate=2025-01-01&endDate=2025-01-13',
       '手动触发日期范围内的数据同步'
UNION ALL
SELECT '方法3: 等待定时任务',
       '定时任务每5分钟自动执行一次',
       '同步前一日和当日的数据';

-- 23. 验证同步结果
SELECT '23. 验证同步结果' AS check_item;
SELECT
    stat_date,
    org_code,
    energy_type,
    energy_type_name,
    total_consumption,
    total_cost,
    meter_count,
    create_time
FROM tb_energy_classification_summary
WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 3 DAY)
ORDER BY create_time DESC
LIMIT 20;