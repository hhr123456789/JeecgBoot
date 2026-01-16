-- ========================================
-- 企业分类分区统计数据检查SQL脚本
-- ========================================

-- 1. 检查统计汇总表是否有数据
SELECT '=== 1. 检查 tb_energy_classification_summary 表数据量 ===' AS info;
SELECT COUNT(*) AS total_records FROM tb_energy_classification_summary;

-- 2. 查看最近的统计数据
SELECT '=== 2. 最近10条统计数据 ===' AS info;
SELECT
    id,
    org_code,
    org_name,
    energy_type,
    energy_type_name,
    time_dimension,
    stat_date,
    stat_month,
    stat_year,
    total_consumption,
    total_cost,
    carbon_emission
FROM tb_energy_classification_summary
ORDER BY stat_date DESC
LIMIT 10;

-- 3. 按时间维度统计数据量
SELECT '=== 3. 按时间维度统计数据量 ===' AS info;
SELECT
    time_dimension,
    COUNT(*) AS record_count,
    MIN(stat_date) AS earliest_date,
    MAX(stat_date) AS latest_date
FROM tb_energy_classification_summary
GROUP BY time_dimension;

-- 4. 按部门统计数据量
SELECT '=== 4. 按部门统计数据量（前10个） ===' AS info;
SELECT
    org_code,
    org_name,
    COUNT(*) AS record_count,
    SUM(total_consumption) AS total_consumption
FROM tb_energy_classification_summary
GROUP BY org_code, org_name
ORDER BY record_count DESC
LIMIT 10;

-- 5. 按能源类型统计数据量
SELECT '=== 5. 按能源类型统计数据量 ===' AS info;
SELECT
    energy_type,
    energy_type_name,
    COUNT(*) AS record_count,
    SUM(total_consumption) AS total_consumption,
    SUM(total_cost) AS total_cost
FROM tb_energy_classification_summary
GROUP BY energy_type, energy_type_name
ORDER BY energy_type;

-- 6. 检查2025年11月的数据（页面默认查询月份）
SELECT '=== 6. 检查2025年11月的数据 ===' AS info;
SELECT
    org_code,
    org_name,
    energy_type_name,
    time_dimension,
    stat_date,
    total_consumption,
    total_cost
FROM tb_energy_classification_summary
WHERE stat_date BETWEEN '2025-11-01' AND '2025-11-30'
ORDER BY stat_date DESC
LIMIT 20;

-- 7. 检查2025年12月的数据（当前月份）
SELECT '=== 7. 检查2025年12月的数据 ===' AS info;
SELECT
    org_code,
    org_name,
    energy_type_name,
    time_dimension,
    stat_date,
    total_consumption,
    total_cost
FROM tb_energy_classification_summary
WHERE stat_date BETWEEN '2025-12-01' AND '2025-12-31'
ORDER BY stat_date DESC
LIMIT 20;

-- 8. 检查部门表数据
SELECT '=== 8. 检查 sys_depart 部门表数据 ===' AS info;
SELECT
    id,
    parent_id,
    depart_name,
    org_code,
    org_type,
    status,
    del_flag
FROM sys_depart
WHERE del_flag = '0' AND status = '1'
ORDER BY org_code
LIMIT 20;

-- 9. 检查设备表数据
SELECT '=== 9. 检查 tb_module 设备表数据 ===' AS info;
SELECT
    module_id,
    module_name,
    sys_org_code,
    energy_type,
    isaction
FROM tb_module
WHERE isaction = 1
LIMIT 20;

-- 10. 检查能源比例信息表
SELECT '=== 10. 检查 tb_energy_ratio_info 能源配置表 ===' AS info;
SELECT
    id,
    isenergy_type,
    energy_name,
    energy_unit,
    zbmxs_value,
    tpfxs_value,
    price_per_unit
FROM tb_energy_ratio_info
ORDER BY isenergy_type;

-- 11. 检查日统计表数据（用于同步）
SELECT '=== 11. 检查 tb_ep_equ_energy_daycount 日统计表数据 ===' AS info;
SELECT
    module_id,
    dt,
    energy_count,
    COUNT(*) AS record_count
FROM tb_ep_equ_energy_daycount
WHERE dt >= '2025-11-01'
GROUP BY module_id, dt
ORDER BY dt DESC
LIMIT 20;

-- 12. 诊断建议
SELECT '=== 12. 诊断建议 ===' AS info;
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary) = 0 THEN
            '❌ 统计汇总表没有数据，需要运行数据同步任务'
        WHEN (SELECT COUNT(*) FROM tb_energy_classification_summary WHERE stat_date BETWEEN '2025-11-01' AND '2025-11-30') = 0 THEN
            '⚠️ 2025年11月没有数据，请修改页面默认查询日期或同步该月数据'
        ELSE
            '✓ 数据正常，请检查前端查询条件是否正确'
    END AS diagnosis;
