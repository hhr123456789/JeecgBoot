-- ============================================
-- 查询现有仪表ID和能耗数据
-- 用于填充 tb_team_dimension_relation 表的 module_ids 字段
-- ============================================

-- 1. 查询所有启用的仪表
SELECT
    module_id,
    module_name,
    energy_type,
    isaction
FROM tb_module
WHERE isaction = 'Y'
ORDER BY module_id
LIMIT 50;

-- 2. 查询最近7天有能耗数据的仪表ID (日统计表)
SELECT DISTINCT
    module_id,
    COUNT(*) as record_count,
    SUM(energy_count) as total_energy,
    MIN(dt) as first_date,
    MAX(dt) as last_date
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY module_id
ORDER BY total_energy DESC
LIMIT 50;

-- 3. 查询最近一个月有能耗数据的仪表ID (月统计表)
SELECT DISTINCT
    module_id,
    COUNT(*) as record_count,
    SUM(energy_count) as total_energy
FROM tb_ep_equ_energy_monthcount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY module_id
ORDER BY total_energy DESC
LIMIT 50;

-- 4. 查看当前维度编码
SELECT DISTINCT
    dimension_code,
    dimension_type,
    COUNT(*) as relation_count
FROM tb_team_dimension_relation
GROUP BY dimension_code, dimension_type;

-- 5. 查看现有的班组信息
SELECT
    team_code,
    team_name,
    org_code,
    org_name,
    status
FROM tb_team_info
ORDER BY sort_order;

-- 6. 查看现有的班组维度关联
SELECT
    team_code,
    dimension_code,
    dimension_type,
    module_ids,
    status
FROM tb_team_dimension_relation
ORDER BY dimension_code, team_code;
