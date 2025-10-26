-- 企业分类分区统计功能数据表创建脚本
-- 创建时间: 2025-10-09
-- 功能: 支持企业按部门、能源类型进行分类分区统计

-- 1. 企业分类分区统计汇总表（按部门+能源类型+时间维度聚合）
CREATE TABLE `tb_energy_classification_summary` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `org_code` varchar(64) NOT NULL COMMENT '部门编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
  `parent_org_code` varchar(64) DEFAULT NULL COMMENT '父级部门编码',
  `energy_type` int(10) NOT NULL COMMENT '能源类型(1:电能 2:水能 3:燃气)',
  `energy_type_name` varchar(50) DEFAULT NULL COMMENT '能源类型名称',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_month` varchar(7) DEFAULT NULL COMMENT '统计月份(YYYY-MM)',
  `stat_year` varchar(4) DEFAULT NULL COMMENT '统计年份(YYYY)',
  `time_dimension` varchar(10) NOT NULL COMMENT '时间维度(day/month/year)',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总消耗量',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量',
  `peak_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段消耗',
  `peak_cost` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段费用',
  `flat_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '平时段消耗',
  `flat_cost` decimal(18,2) DEFAULT '0.00' COMMENT '平时段费用',
  `valley_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段消耗',
  `valley_cost` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段费用',
  `meter_count` int(10) DEFAULT '0' COMMENT '仪表数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_time_dimension` (`time_dimension`),
  KEY `idx_parent_org_code` (`parent_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='企业分类分区统计汇总表';

-- 2. 企业能源类型配置表
CREATE TABLE `tb_energy_type_config` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `energy_type` int(10) NOT NULL COMMENT '能源类型编码',
  `energy_name` varchar(50) NOT NULL COMMENT '能源类型名称',
  `energy_unit` varchar(20) DEFAULT NULL COMMENT '计量单位(kWh/m³等)',
  `price_per_unit` decimal(10,4) DEFAULT '0.0000' COMMENT '单价',
  `carbon_factor` decimal(18,8) DEFAULT '0.00000000' COMMENT '碳排放系数',
  `coal_factor` decimal(18,8) DEFAULT '0.00000000' COMMENT '标准煤系数',
  `status` varchar(1) DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `sort_order` int(5) DEFAULT '0' COMMENT '排序',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_energy_type` (`energy_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='企业能源类型配置表';

-- 3. 企业部门能源统计明细表
CREATE TABLE `tb_energy_classification_detail` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `summary_id` varchar(36) NOT NULL COMMENT '汇总表ID',
  `org_code` varchar(64) NOT NULL COMMENT '部门编码',
  `module_id` varchar(50) NOT NULL COMMENT '仪表编号',
  `module_name` varchar(100) DEFAULT NULL COMMENT '仪表名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `consumption` decimal(18,2) DEFAULT '0.00' COMMENT '消耗量',
  `cost` decimal(18,2) DEFAULT '0.00' COMMENT '费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_summary_id` (`summary_id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_module_id` (`module_id`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='企业部门能源统计明细表';

-- 4. 企业部门能源预算表
CREATE TABLE `tb_energy_budget` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `org_code` varchar(64) NOT NULL COMMENT '部门编码',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `budget_year` varchar(4) NOT NULL COMMENT '预算年度',
  `budget_month` varchar(7) DEFAULT NULL COMMENT '预算月度',
  `budget_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '预算消耗量',
  `budget_cost` decimal(18,2) DEFAULT '0.00' COMMENT '预算费用',
  `actual_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '实际消耗量',
  `actual_cost` decimal(18,2) DEFAULT '0.00' COMMENT '实际费用',
  `completion_rate` decimal(5,2) DEFAULT '0.00' COMMENT '完成率(%)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_budget_year` (`budget_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='企业部门能源预算表';

-- 插入能源类型配置数据
INSERT INTO `tb_energy_type_config` (`id`, `energy_type`, `energy_name`, `energy_unit`, `price_per_unit`, `carbon_factor`, `coal_factor`, `status`, `sort_order`, `create_by`, `remark`) VALUES
('1', 1, '电能', 'kWh', 0.8000, 0.99700000, 0.12290000, '1', 1, 'admin', '电力能源'),
('2', 2, '水能', 'm³', 0.6000, 0.00000000, 0.00000000, '1', 2, 'admin', '水资源'),
('3', 3, '燃气', 'm³', 2.5000, 2.16500000, 1.33000000, '1', 3, 'admin', '天然气');

-- 插入模拟的统计汇总数据
INSERT INTO `tb_energy_classification_summary` (`id`, `org_code`, `org_name`, `parent_org_code`, `energy_type`, `energy_type_name`, `stat_date`, `stat_month`, `stat_year`, `time_dimension`, `total_consumption`, `total_cost`, `carbon_emission`, `standard_coal`, `peak_consumption`, `peak_cost`, `flat_consumption`, `flat_cost`, `valley_consumption`, `valley_cost`, `meter_count`) VALUES
('summary_001', 'A01', '生产部门', 'A', 1, '电能', '2024-01-01', '2024-01', '2024', 'month', 456789.23, 365431.38, 455.43, 56.14, 120000.00, 96000.00, 200000.00, 160000.00, 136789.23, 109431.38, 15),
('summary_002', 'A01', '生产部门', 'A', 2, '水能', '2024-01-01', '2024-01', '2024', 'month', 123456.78, 74074.07, 0.00, 0.00, 30000.00, 18000.00, 60000.00, 36000.00, 33456.78, 20074.07, 8),
('summary_003', 'A01', '生产部门', 'A', 3, '燃气', '2024-01-01', '2024-01', '2024', 'month', 87654.32, 219135.80, 189.77, 116.58, 20000.00, 50000.00, 45000.00, 112500.00, 22654.32, 56635.80, 5),
('summary_004', 'A02', '辅助部门', 'A', 1, '电能', '2024-01-01', '2024-01', '2024', 'month', 234567.89, 187654.31, 233.91, 28.83, 60000.00, 48000.00, 120000.00, 96000.00, 54567.89, 43654.31, 8),
('summary_005', 'A02', '辅助部门', 'A', 2, '水能', '2024-01-01', '2024-01', '2024', 'month', 65432.10, 39259.26, 0.00, 0.00, 15000.00, 9000.00, 35000.00, 21000.00, 15432.10, 9259.26, 4),
('summary_006', 'A02', '辅助部门', 'A', 3, '燃气', '2024-01-01', '2024-01', '2024', 'month', 43210.98, 108027.45, 93.51, 57.47, 10000.00, 25000.00, 22000.00, 55000.00, 11210.98, 28027.45, 3);