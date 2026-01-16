-- ========================================
-- 诊断企业分类分区统计查询问题
-- ========================================

-- 1. 检查统计表中的 org_code 字段存储的是什么
SELECT '=== 1. 检查统计表中 org_code 的格式 ===' AS info;
SELECT DISTINCT
    org_code,
    org_name,
    LENGTH(org_code) AS org_code_length,
    CASE
        WHEN org_code REGEXP '^[0-9]+$' THEN '数字ID'
        WHEN org_code REGEXP '^[A-Z][0-9]+' THEN '组织编码'
        ELSE '其他格式'
    END AS org_code_type
FROM tb_energy_classification_summary
LIMIT 10;

-- 2. 检查部门表中的数据
SELECT '=== 2. 检查部门表中的 id 和 org_code ===' AS info;
SELECT
    id,
    parent_id,
    depart_name,
    org_code,
    LENGTH(id) AS id_length,
    LENGTH(org_code) AS org_code_length
FROM sys_depart
WHERE del_flag = '0' AND status = '1'
ORDER BY parent_id, depart_order
LIMIT 10;

-- 3. 尝试关联查询：使用 sys_depart.id 关联 tb_energy_classification_summary.org_code
SELECT '=== 3. 使用部门ID关联统计表（正确方式） ===' AS info;
SELECT
    d.id AS depart_id,
    d.depart_name,
    d.org_code AS depart_org_code,
    s.org_code AS summary_org_code,
    s.org_name AS summary_org_name,
    s.energy_type_name,
    s.total_consumption,
    s.stat_date
FROM sys_depart d
LEFT JOIN tb_energy_classification_summary s ON d.id = s.org_code
WHERE d.del_flag = '0' AND d.status = '1'
  AND s.time_dimension = 'month'
  AND s.stat_date BETWEEN '2025-11-01' AND '2025-11-30'
LIMIT 10;

-- 4. 尝试关联查询：使用 sys_depart.org_code 关联（错误方式，看是否有数据）
SELECT '=== 4. 使用组织编码关联统计表（错误方式） ===' AS info;
SELECT
    d.id AS depart_id,
    d.depart_name,
    d.org_code AS depart_org_code,
    s.org_code AS summary_org_code,
    s.org_name AS summary_org_name,
    s.energy_type_name,
    s.total_consumption,
    s.stat_date
FROM sys_depart d
LEFT JOIN tb_energy_classification_summary s ON d.org_code = s.org_code
WHERE d.del_flag = '0' AND d.status = '1'
  AND s.time_dimension = 'month'
  AND s.stat_date BETWEEN '2025-11-01' AND '2025-11-30'
LIMIT 10;

-- 5. 检查统计表中有哪些月份的数据
SELECT '=== 5. 统计表中的月份数据分布 ===' AS info;
SELECT
    stat_month,
    time_dimension,
    COUNT(*) AS record_count,
    SUM(total_consumption) AS total_consumption
FROM tb_energy_classification_summary
GROUP BY stat_month, time_dimension
ORDER BY stat_month DESC
LIMIT 10;

-- 6. 检查是否有2025-11的数据
SELECT '=== 6. 检查2025-11月的数据详情 ===' AS info;
SELECT
    org_code,
    org_name,
    energy_type,
    energy_type_name,
    time_dimension,
    stat_date,
    stat_month,
    total_consumption,
    total_cost
FROM tb_energy_classification_summary
WHERE stat_month = '2025-11' OR (stat_date >= '2025-11-01' AND stat_date <= '2025-11-30')
ORDER BY org_code, energy_type
LIMIT 20;

-- 7. 模拟前端查询：假设选中了第一个部门
SELECT '=== 7. 模拟查询：选中第一个部门 ===' AS info;
SET @selected_org_code = (SELECT org_code FROM sys_depart WHERE del_flag = '0' AND status = '1' ORDER BY depart_order LIMIT 1);
SET @selected_depart_id = (SELECT id FROM sys_depart WHERE org_code = @selected_org_code);

SELECT
    CONCAT('选中的部门 org_code: ', @selected_org_code, ', id: ', @selected_depart_id) AS selected_info;

-- 使用部门ID查询统计数据
SELECT
    org_code,
    org_name,
    energy_type_name,
    time_dimension,
    stat_date,
    total_consumption,
    total_cost
FROM tb_energy_classification_summary
WHERE org_code = @selected_depart_id
  AND time_dimension = 'month'
  AND stat_date BETWEEN '2025-11-01' AND '2025-11-30'
LIMIT 10;

-- 8. 检查设备表的 sys_org_code 字段
SELECT '=== 8. 检查设备表的 sys_org_code 格式 ===' AS info;
SELECT DISTINCT
    sys_org_code,
    LENGTH(sys_org_code) AS code_length,
    CASE
        WHEN sys_org_code REGEXP '^[0-9]+$' THEN '数字ID'
        WHEN sys_org_code REGEXP '^[A-Z][0-9]+' THEN '组织编码'
        ELSE '其他格式'
    END AS code_type,
    COUNT(*) AS device_count
FROM tb_module
WHERE isaction = 1
GROUP BY sys_org_code
LIMIT 10;

-- 9. 验证部门ID和设备的关联
SELECT '=== 9. 验证部门ID和设备的关联 ===' AS info;
SELECT
    d.id AS depart_id,
    d.depart_name,
    d.org_code AS depart_org_code,
    COUNT(m.id) AS device_count
FROM sys_depart d
LEFT JOIN tb_module m ON d.id = m.sys_org_code AND m.isaction = 1
WHERE d.del_flag = '0' AND d.status = '1'
GROUP BY d.id, d.depart_name, d.org_code
ORDER BY device_count DESC
LIMIT 10;

-- 10. 最终诊断
SELECT '=== 10. 诊断结果 ===' AS info;
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary) = 0 THEN
            '❌ 问题1: tb_energy_classification_summary 表完全没有数据，需要运行数据同步'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30') = 0 THEN
            '⚠️ 问题2: 2025年11月没有数据，需要同步该月数据或修改前端默认日期'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE org_code REGEXP '^[A-Z]') > 0 THEN
            '❌ 问题3: org_code字段存储的是组织编码而不是部门ID，数据同步逻辑有问题'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE org_code REGEXP '^[0-9]+$') > 0 THEN
            '✓ org_code字段存储的是部门ID（正确），请检查查询逻辑'
        ELSE
            '❓ 无法判断，请手动检查数据'
    END AS diagnosis;
