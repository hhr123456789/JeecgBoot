-- =====================================================
-- 产品单耗分析 - 数据库表创建脚本
-- 创建时间: 2026-02-16
-- 说明: 包含5张产品相关表
-- =====================================================

USE EMSProject_jeecg;

-- =====================================================
-- 表1: tb_product_category (产品分类表)
-- 说明: 产品分类树形结构
-- =====================================================
DROP TABLE IF EXISTS `tb_product_category`;
CREATE TABLE `tb_product_category` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级分类ID (根节点为空)',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码 (唯一)',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_level` int(11) DEFAULT '1' COMMENT '分类层级 (1/2/3)',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `is_leaf` tinyint(1) DEFAULT '0' COMMENT '是否叶子节点 (0:否 1:是)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- =====================================================
-- 表2: tb_product_info (产品基础信息表)
-- 说明: 产品主数据表
-- =====================================================
DROP TABLE IF EXISTS `tb_product_info`;
CREATE TABLE `tb_product_info` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码 (唯一)',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `category_id` varchar(32) NOT NULL COMMENT '产品分类ID',
  `category_code` varchar(50) DEFAULT NULL COMMENT '产品分类编码',
  `product_model` varchar(100) DEFAULT NULL COMMENT '产品型号',
  `product_spec` varchar(200) DEFAULT NULL COMMENT '产品规格',
  `product_unit` varchar(20) DEFAULT '件' COMMENT '产品单位 (件/吨/米等)',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_org_code` (`sys_org_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品基础信息表';

-- =====================================================
-- 表3: tb_product_production (产品产量记录表)
-- 说明: 记录每日产品的生产产量数据
-- =====================================================
DROP TABLE IF EXISTS `tb_product_production`;
CREATE TABLE `tb_product_production` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `production_date` date NOT NULL COMMENT '生产日期',
  `production_line` varchar(100) DEFAULT NULL COMMENT '生产线',
  `team_code` varchar(50) DEFAULT NULL COMMENT '班组编码',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型',
  `plan_production` decimal(18,2) DEFAULT '0.00' COMMENT '计划产量',
  `actual_production` decimal(18,2) DEFAULT '0.00' COMMENT '实际产量',
  `qualified_production` decimal(18,2) DEFAULT '0.00' COMMENT '合格产量',
  `unqualified_production` decimal(18,2) DEFAULT '0.00' COMMENT '不合格产量',
  `qualification_rate` decimal(5,2) DEFAULT '0.00' COMMENT '合格率 (%)',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_date` (`product_code`,`production_date`),
  KEY `idx_production_date` (`production_date`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_org_code` (`sys_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品产量记录表';

-- =====================================================
-- 表4: tb_product_process_rel (产品工序关联表)
-- 说明: 建立产品与工序、仪表的关联关系
-- =====================================================
DROP TABLE IF EXISTS `tb_product_process_rel`;
CREATE TABLE `tb_product_process_rel` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `process_code` varchar(50) DEFAULT NULL COMMENT '工序编码',
  `process_name` varchar(100) DEFAULT NULL COMMENT '工序名称',
  `dimension_code` varchar(64) DEFAULT NULL COMMENT '维度编码 (部门编码)',
  `dimension_type` int(11) DEFAULT NULL COMMENT '维度类型',
  `energy_type` int(11) DEFAULT NULL COMMENT '能源类型',
  `module_ids` text COMMENT '关联的仪表ID列表 (逗号分隔)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_process_code` (`process_code`),
  KEY `idx_dimension_code` (`dimension_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品工序关联表';

-- =====================================================
-- 表5: tb_product_energy_consumption (产品能耗统计表)
-- 说明: 产品维度的能耗统计数据
-- =====================================================
DROP TABLE IF EXISTS `tb_product_energy_consumption`;
CREATE TABLE `tb_product_energy_consumption` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_month` varchar(7) DEFAULT NULL COMMENT '统计月份 (YYYY-MM)',
  `stat_year` varchar(4) DEFAULT NULL COMMENT '统计年份 (YYYY)',
  `time_dimension` varchar(10) NOT NULL COMMENT '时间维度 (day/month/year)',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总能耗 (kWh)',
  `total_production` decimal(18,2) DEFAULT '0.00' COMMENT '总产量',
  `qualified_production` decimal(18,2) DEFAULT '0.00' COMMENT '合格产量',
  `qualification_rate` decimal(5,2) DEFAULT '0.00' COMMENT '合格率 (%)',
  `unit_consumption` decimal(18,4) DEFAULT '0.0000' COMMENT '单位产品能耗 (kWh/件)',
  `energy_price` decimal(18,4) DEFAULT '0.0000' COMMENT '能源单价',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_date_dimension` (`product_code`,`stat_date`,`time_dimension`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_time_dimension` (`time_dimension`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_org_code` (`sys_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品能耗统计表';

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT 'Product Energy Tables Created Successfully!' as Status;
