-- 检查能源类型配置表数据
SELECT * FROM tb_energy_type_config;

-- 检查分类分区统计汇总表数据
SELECT * FROM tb_energy_classification_summary;

-- 按部门分组查看数据
SELECT org_code, org_name, energy_type, energy_type_name, SUM(total_consumption) as total_consumption, SUM(total_cost) as total_cost
FROM tb_energy_classification_summary
GROUP BY org_code, org_name, energy_type, energy_type_name
ORDER BY org_code, energy_type;