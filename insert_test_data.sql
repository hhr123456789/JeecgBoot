-- 检查表是否存在
SHOW TABLES LIKE 'tb_energy_%';

-- 如果表存在但没有数据，插入一些测试数据
INSERT IGNORE INTO tb_energy_type_config (id, energy_type, energy_name, energy_unit, price_per_unit, carbon_factor, coal_factor, status, sort_order, create_by) VALUES
('1', 1, '电能', 'kWh', 0.8000, 0.99700000, 0.12290000, '1', 1, 'admin'),
('2', 2, '水能', 'm³', 0.6000, 0.00000000, 0.00000000, '1', 2, 'admin'),
('3', 3, '燃气', 'm³', 2.5000, 2.16500000, 1.33000000, '1', 3, 'admin');

-- 插入分类分区统计汇总数据
INSERT IGNORE INTO tb_energy_classification_summary (id, org_code, org_name, parent_org_code, energy_type, energy_type_name, stat_date, stat_month, stat_year, time_dimension, total_consumption, total_cost, carbon_emission, standard_coal, meter_count) VALUES
('summary_001', 'A01', '生产部门', 'A', 1, '电能', '2024-01-01', '2024-01', '2024', 'month', 456789.23, 365431.38, 455.43, 56.14, 15),
('summary_002', 'A01', '生产部门', 'A', 2, '水能', '2024-01-01', '2024-01', '2024', 'month', 123456.78, 74074.07, 0.00, 0.00, 8),
('summary_003', 'A01', '生产部门', 'A', 3, '燃气', '2024-01-01', '2024-01', '2024', 'month', 87654.32, 219135.80, 189.77, 116.58, 5),
('summary_004', 'A02', '辅助部门', 'A', 1, '电能', '2024-01-01', '2024-01', '2024', 'month', 234567.89, 187654.31, 233.91, 28.83, 8),
('summary_005', 'A02', '辅助部门', 'A', 2, '水能', '2024-01-01', '2024-01', '2024', 'month', 65432.10, 39259.26, 0.00, 0.00, 4),
('summary_006', 'A02', '辅助部门', 'A', 3, '燃气', '2024-01-01', '2024-01', '2024', 'month', 43210.98, 108027.45, 93.51, 57.47, 3),
('summary_007', 'A01-01', '一号车间', 'A01', 1, '电能', '2024-01-01', '2024-01', '2024', 'month', 156789.23, 125431.38, 156.23, 19.25, 5),
('summary_008', 'A01-01', '一号车间', 'A01', 2, '水能', '2024-01-01', '2024-01', '2024', 'month', 43456.78, 26074.07, 0.00, 0.00, 3),
('summary_009', 'A01-01', '一号车间', 'A01', 3, '燃气', '2024-01-01', '2024-01', '2024', 'month', 27654.32, 69135.80, 59.86, 36.78, 2);

-- 查询数据确认
SELECT '能源类型配置表' as table_name, COUNT(*) as record_count FROM tb_energy_type_config
UNION ALL
SELECT '分类分区统计汇总表', COUNT(*) FROM tb_energy_classification_summary;

-- 按部门分组查看数据
SELECT org_code, org_name, energy_type, energy_type_name, SUM(total_consumption) as total_consumption, SUM(total_cost) as total_cost
FROM tb_energy_classification_summary
GROUP BY org_code, org_name, energy_type, energy_type_name
ORDER BY org_code, energy_type;