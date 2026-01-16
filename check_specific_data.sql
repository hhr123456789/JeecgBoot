-- 检查特定部门的数据
-- 根据日志中的部门ID进行检查

-- 1. 检查统计表是否有任何数据
SELECT '1. 统计表总记录数' AS step, COUNT(*) AS total_count
FROM tb_energy_classification_summary;

-- 2. 检查统计表中是否有这些部门ID的数据
SELECT '2. 检查特定部门ID的数据' AS step;
SELECT
    org_code,
    org_name,
    energy_type,
    time_dimension,
    stat_date,
    stat_month,
    total_consumption
FROM tb_energy_classification_summary
WHERE org_code IN (
    '1881235917079482369',
    '1940618841356288002',
    '1940618899342540802',
    '1940618949271535617'
)
LIMIT 20;

-- 3. 检查统计表中有哪些 org_code
SELECT '3. 统计表中的 org_code 列表（前20个）' AS step;
SELECT DISTINCT
    org_code,
    org_name,
    COUNT(*) AS record_count
FROM tb_energy_classification_summary
GROUP BY org_code, org_name
ORDER BY record_count DESC
LIMIT 20;

-- 4. 检查统计表中的时间范围
SELECT '4. 统计表中的时间范围' AS step;
SELECT
    MIN(stat_date) AS earliest_date,
    MAX(stat_date) AS latest_date,
    COUNT(DISTINCT stat_date) AS date_count
FROM tb_energy_classification_summary;

-- 5. 检查2025-11月的数据
SELECT '5. 2025-11月的数据' AS step;
SELECT
    org_code,
    org_name,
    energy_type,
    COUNT(*) AS record_count,
    SUM(total_consumption) AS total_consumption
FROM tb_energy_classification_summary
WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30'
GROUP BY org_code, org_name, energy_type
LIMIT 20;

-- 6. 检查能源类型为1（电能）的数据
SELECT '6. 能源类型为1的数据' AS step;
SELECT
    org_code,
    org_name,
    stat_date,
    total_consumption
FROM tb_energy_classification_summary
WHERE energy_type = 1
ORDER BY stat_date DESC
LIMIT 20;

-- 7. 检查时间维度为month的数据
SELECT '7. 时间维度为month的数据' AS step;
SELECT
    org_code,
    org_name,
    stat_month,
    energy_type,
    COUNT(*) AS record_count
FROM tb_energy_classification_summary
WHERE time_dimension = 'month'
GROUP BY org_code, org_name, stat_month, energy_type
ORDER BY stat_month DESC
LIMIT 20;

-- 8. 完整模拟查询（使用日志中的条件）
SELECT '8. 完整模拟查询' AS step;
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
WHERE org_code IN (
    '1881235917079482369',
    '1940618841356288002',
    '1940618899342540802',
    '1940618949271535617'
)
AND time_dimension = 'month'
AND stat_date BETWEEN '2025-11-01' AND '2025-11-30'
AND energy_type = 1;

-- 9. 检查是否数据同步有问题（org_code存储的是什么）
SELECT '9. org_code格式检查' AS step;
SELECT
    org_code,
    LENGTH(org_code) AS code_length,
    CASE
        WHEN org_code REGEXP '^[0-9]+$' THEN '纯数字ID'
        WHEN org_code REGEXP '^[A-Z]' THEN '组织编码'
        ELSE '其他格式'
    END AS code_type,
    COUNT(*) AS count
FROM tb_energy_classification_summary
GROUP BY org_code
LIMIT 10;

-- 10. 诊断建议
SELECT '10. 诊断结果' AS step;
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary) = 0 THEN
            '❌ 问题：统计表完全没有数据，需要运行数据同步任务'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30') = 0 THEN
            '⚠️ 问题：2025-11月没有数据，需要同步该月数据'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE org_code IN ('1881235917079482369','1940618841356288002','1940618899342540802','1940618949271535617')) = 0 THEN
            '❌ 问题：这些部门ID在统计表中没有数据，数据同步时可能使用了错误的org_code'
        ELSE
            '✓ 数据存在，但查询条件组合后没有匹配的记录'
    END AS diagnosis;
