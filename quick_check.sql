-- 快速检查脚本
-- 1. 检查统计表是否有数据
SELECT '1. 统计表总记录数' AS step, COUNT(*) AS count FROM tb_energy_classification_summary;

-- 2. 检查统计表中 org_code 的格式（前5条）
SELECT '2. org_code格式示例' AS step, org_code, org_name, LENGTH(org_code) AS len
FROM tb_energy_classification_summary
LIMIT 5;

-- 3. 检查部门表数据（前5条）
SELECT '3. 部门表示例' AS step, id, depart_name, org_code, LENGTH(id) AS id_len, LENGTH(org_code) AS code_len
FROM sys_depart
WHERE del_flag = '0' AND status = '1'
LIMIT 5;

-- 4. 检查是否有2025-11的数据
SELECT '4. 2025-11月数据' AS step, COUNT(*) AS count
FROM tb_energy_classification_summary
WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30';

-- 5. 检查最新的数据日期
SELECT '5. 最新数据日期' AS step, MAX(stat_date) AS latest_date
FROM tb_energy_classification_summary;

-- 6. 尝试关联查询（使用部门ID）
SELECT '6. 关联查询测试' AS step,
       d.id AS dept_id,
       d.depart_name,
       COUNT(s.id) AS summary_count
FROM sys_depart d
LEFT JOIN tb_energy_classification_summary s ON d.id = s.org_code
WHERE d.del_flag = '0' AND d.status = '1'
GROUP BY d.id, d.depart_name
LIMIT 5;
