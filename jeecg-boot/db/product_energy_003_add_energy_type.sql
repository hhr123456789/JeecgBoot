-- =====================================================
-- 产品单耗分析 - 添加能源类型支持
-- 创建时间: 2026-02-17
-- 说明: 修改表结构支持多能源类型的单耗统计
-- =====================================================

USE EMSProject_jeecg;

-- =====================================================
-- 1. 修改 tb_product_energy_consumption 表，添加能源类型字段
-- =====================================================
ALTER TABLE `tb_product_energy_consumption`
ADD COLUMN `energy_type` int(11) DEFAULT 1 COMMENT '能源类型 (1:电 2:水 3:天然气 4:蒸汽 5:压缩空气)' AFTER `product_code`,
ADD COLUMN `energy_type_name` varchar(50) DEFAULT '电' COMMENT '能源类型名称' AFTER `energy_type`,
ADD COLUMN `energy_unit` varchar(20) DEFAULT 'kWh' COMMENT '能源单位' AFTER `energy_type_name`;

-- 修改唯一索引，加入能源类型
ALTER TABLE `tb_product_energy_consumption`
DROP INDEX `uk_product_date_dimension`,
ADD UNIQUE KEY `uk_product_energy_date_dimension` (`product_code`, `energy_type`, `stat_date`, `time_dimension`);

-- 添加能源类型索引
ALTER TABLE `tb_product_energy_consumption`
ADD INDEX `idx_energy_type` (`energy_type`);

-- =====================================================
-- 2. 更新现有数据的能源类型（默认为电）
-- =====================================================
UPDATE `tb_product_energy_consumption` SET
  `energy_type` = 1,
  `energy_type_name` = '电',
  `energy_unit` = 'kWh'
WHERE `energy_type` IS NULL OR `energy_type` = 0;

-- =====================================================
-- 3. 插入其他能源类型的测试数据（水、天然气）
-- =====================================================

-- 水的能耗数据 (2026-01 月统计)
INSERT INTO `tb_product_energy_consumption` (`id`, `product_code`, `energy_type`, `energy_type_name`, `energy_unit`, `stat_date`, `stat_month`, `stat_year`, `time_dimension`, `total_consumption`, `total_production`, `qualified_production`, `qualification_rate`, `unit_consumption`, `energy_price`, `total_cost`, `carbon_emission`, `standard_coal`, `sys_org_code`, `create_by`) VALUES
('PEC_W001', 'PROD_MC001', 2, '水', 'm³', '2026-01-01', '2026-01', '2026', 'month', 1250.50, 5200, 5096, 98.00, 0.25, 4.50, 5627.25, 0, 0, 'A01B03', 'admin'),
('PEC_W002', 'PROD_MQ001', 2, '水', 'm³', '2026-01-01', '2026-01', '2026', 'month', 980.30, 3100, 3007, 97.00, 0.33, 4.50, 4411.35, 0, 0, 'A01B03', 'admin'),
('PEC_W003', 'PROD_SR001', 2, '水', 'm³', '2026-01-01', '2026-01', '2026', 'month', 720.00, 2850, 2793, 98.00, 0.26, 4.50, 3240.00, 0, 0, 'A01B04', 'admin'),
('PEC_W004', 'PROD_QC001', 2, '水', 'm³', '2026-01-01', '2026-01', '2026', 'month', 580.00, 1430, 1360, 95.10, 0.43, 4.50, 2610.00, 0, 0, 'A01B04', 'admin');

-- 水的能耗数据 (2026-02 月统计)
INSERT INTO `tb_product_energy_consumption` (`id`, `product_code`, `energy_type`, `energy_type_name`, `energy_unit`, `stat_date`, `stat_month`, `stat_year`, `time_dimension`, `total_consumption`, `total_production`, `qualified_production`, `qualification_rate`, `unit_consumption`, `energy_price`, `total_cost`, `carbon_emission`, `standard_coal`, `sys_org_code`, `create_by`) VALUES
('PEC_W005', 'PROD_MC001', 2, '水', 'm³', '2026-02-01', '2026-02', '2026', 'month', 1320.00, 5300, 5194, 98.00, 0.25, 4.50, 5940.00, 0, 0, 'A01B03', 'admin'),
('PEC_W006', 'PROD_MQ001', 2, '水', 'm³', '2026-02-01', '2026-02', '2026', 'month', 1050.00, 3200, 3104, 97.00, 0.34, 4.50, 4725.00, 0, 0, 'A01B03', 'admin'),
('PEC_W007', 'PROD_SR001', 2, '水', 'm³', '2026-02-01', '2026-02', '2026', 'month', 780.00, 2950, 2891, 98.00, 0.27, 4.50, 3510.00, 0, 0, 'A01B04', 'admin'),
('PEC_W008', 'PROD_QC001', 2, '水', 'm³', '2026-02-01', '2026-02', '2026', 'month', 620.00, 1480, 1407, 95.07, 0.44, 4.50, 2790.00, 0, 0, 'A01B04', 'admin');

-- 天然气的能耗数据 (2026-01 月统计)
INSERT INTO `tb_product_energy_consumption` (`id`, `product_code`, `energy_type`, `energy_type_name`, `energy_unit`, `stat_date`, `stat_month`, `stat_year`, `time_dimension`, `total_consumption`, `total_production`, `qualified_production`, `qualification_rate`, `unit_consumption`, `energy_price`, `total_cost`, `carbon_emission`, `standard_coal`, `sys_org_code`, `create_by`) VALUES
('PEC_G001', 'PROD_MC001', 3, '天然气', 'm³', '2026-01-01', '2026-01', '2026', 'month', 8500.00, 5200, 5096, 98.00, 1.67, 3.20, 27200.00, 16150.00, 9690.00, 'A01B03', 'admin'),
('PEC_G002', 'PROD_MQ001', 3, '天然气', 'm³', '2026-01-01', '2026-01', '2026', 'month', 6200.00, 3100, 3007, 97.00, 2.06, 3.20, 19840.00, 11780.00, 7068.00, 'A01B03', 'admin'),
('PEC_G003', 'PROD_SR001', 3, '天然气', 'm³', '2026-01-01', '2026-01', '2026', 'month', 4800.00, 2850, 2793, 98.00, 1.72, 3.20, 15360.00, 9120.00, 5472.00, 'A01B04', 'admin'),
('PEC_G004', 'PROD_QC001', 3, '天然气', 'm³', '2026-01-01', '2026-01', '2026', 'month', 3500.00, 1430, 1360, 95.10, 2.57, 3.20, 11200.00, 6650.00, 3990.00, 'A01B04', 'admin');

-- 天然气的能耗数据 (2026-02 月统计)
INSERT INTO `tb_product_energy_consumption` (`id`, `product_code`, `energy_type`, `energy_type_name`, `energy_unit`, `stat_date`, `stat_month`, `stat_year`, `time_dimension`, `total_consumption`, `total_production`, `qualified_production`, `qualification_rate`, `unit_consumption`, `energy_price`, `total_cost`, `carbon_emission`, `standard_coal`, `sys_org_code`, `create_by`) VALUES
('PEC_G005', 'PROD_MC001', 3, '天然气', 'm³', '2026-02-01', '2026-02', '2026', 'month', 8800.00, 5300, 5194, 98.00, 1.69, 3.20, 28160.00, 16720.00, 10032.00, 'A01B03', 'admin'),
('PEC_G006', 'PROD_MQ001', 3, '天然气', 'm³', '2026-02-01', '2026-02', '2026', 'month', 6500.00, 3200, 3104, 97.00, 2.09, 3.20, 20800.00, 12350.00, 7410.00, 'A01B03', 'admin'),
('PEC_G007', 'PROD_SR001', 3, '天然气', 'm³', '2026-02-01', '2026-02', '2026', 'month', 5100.00, 2950, 2891, 98.00, 1.76, 3.20, 16320.00, 9690.00, 5814.00, 'A01B04', 'admin'),
('PEC_G008', 'PROD_QC001', 3, '天然气', 'm³', '2026-02-01', '2026-02', '2026', 'month', 3700.00, 1480, 1407, 95.07, 2.63, 3.20, 11840.00, 7030.00, 4218.00, 'A01B04', 'admin');

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT 'Energy Type Support Added Successfully!' as Status;
SELECT energy_type, energy_type_name, COUNT(*) as count FROM tb_product_energy_consumption GROUP BY energy_type, energy_type_name;
