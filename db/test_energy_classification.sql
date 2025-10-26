-- 测试企业分类分区统计功能
-- 创建测试数据

-- 1. 测试能源类型配置表
SELECT * FROM tb_energy_type_config;

-- 2. 测试统计汇总表
SELECT 
    org_name as '部门名称',
    energy_type_name as '能源类型',
    stat_date as '统计日期',
    total_consumption as '总消耗量',
    total_cost as '总费用',
    carbon_emission as '碳排放',
    meter_count as '仪表数量'
FROM tb_energy_classification_summary 
ORDER BY org_code, energy_type, stat_date;

-- 3. 测试按部门汇总
SELECT 
    org_name as '部门名称',
    SUM(total_consumption) as '总消耗量',
    SUM(total_cost) as '总费用',
    SUM(carbon_emission) as '总碳排放'
FROM tb_energy_classification_summary 
GROUP BY org_code, org_name
ORDER BY total_consumption DESC;

-- 4. 测试按能源类型汇总
SELECT 
    energy_type_name as '能源类型',
    SUM(total_consumption) as '总消耗量',
    SUM(total_cost) as '总费用',
    SUM(carbon_emission) as '总碳排放'
FROM tb_energy_classification_summary 
GROUP BY energy_type, energy_type_name
ORDER BY total_consumption DESC;

-- 5. 测试时间维度查询
SELECT 
    stat_month as '统计月份',
    org_name as '部门名称',
    energy_type_name as '能源类型',
    total_consumption as '消耗量',
    total_cost as '费用'
FROM tb_energy_classification_summary 
WHERE time_dimension = 'month'
ORDER BY stat_month, org_code, energy_type;