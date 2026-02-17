-- ============================================
-- 能效对标功能 - 数据库表创建脚本
-- 创建时间: 2026-02-17
-- ============================================

-- 1. 对标配置表
CREATE TABLE IF NOT EXISTS `tb_benchmark_config` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_code` varchar(50) NOT NULL COMMENT '配置编码',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `benchmark_type` int(2) DEFAULT 1 COMMENT '对标类型(1-生产线对标,2-部门对标,3-班组对标,4-行业对标)',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型(1-电,2-水,8-天然气,5-压缩空气,all-全部)',
  `indicator_type` varchar(20) DEFAULT 'intensity' COMMENT '指标类型(intensity-能耗强度,total-能耗总量,cost-费用)',
  `unit` varchar(20) DEFAULT 'kgce/t' COMMENT '单位',
  `baseline_value` decimal(18,4) DEFAULT NULL COMMENT '基准值',
  `target_value` decimal(18,4) DEFAULT NULL COMMENT '目标值',
  `warning_threshold` decimal(18,4) DEFAULT NULL COMMENT '预警阈值',
  `status` int(1) DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_code` (`config_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标配置表';

-- 2. 对标对象表
CREATE TABLE IF NOT EXISTS `tb_benchmark_target` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_id` varchar(36) NOT NULL COMMENT '配置ID',
  `target_code` varchar(50) NOT NULL COMMENT '对象编码(部门/班组/生产线编码)',
  `target_name` varchar(100) NOT NULL COMMENT '对象名称',
  `target_type` int(2) DEFAULT 1 COMMENT '对象类型(1-生产线,2-部门,3-班组)',
  `parent_code` varchar(50) DEFAULT NULL COMMENT '父级编码',
  `dimension_id` varchar(36) DEFAULT NULL COMMENT '维度ID(关联tb_team_dimension_relation)',
  `sort_order` int(4) DEFAULT 0 COMMENT '排序',
  `status` int(1) DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_target_code` (`target_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标对象表';

-- 3. 对标结果日表
CREATE TABLE IF NOT EXISTS `tb_benchmark_result_day` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `target_code` varchar(50) NOT NULL COMMENT '对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对象名称',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量',
  `energy_cost` decimal(18,4) DEFAULT 0 COMMENT '能耗费用',
  `production_output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `energy_intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度(单耗)',
  `benchmark_value` decimal(18,6) DEFAULT NULL COMMENT '对标值(行业/目标值)',
  `deviation_rate` decimal(10,4) DEFAULT NULL COMMENT '偏差率(%)',
  `ranking` int(4) DEFAULT NULL COMMENT '排名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_date_energy` (`target_code`, `stat_date`, `energy_type`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果日表';

-- 4. 对标结果月表
CREATE TABLE IF NOT EXISTS `tb_benchmark_result_month` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `target_code` varchar(50) NOT NULL COMMENT '对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对象名称',
  `stat_year` int(4) NOT NULL COMMENT '统计年份',
  `stat_month` int(2) NOT NULL COMMENT '统计月份',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量',
  `energy_cost` decimal(18,4) DEFAULT 0 COMMENT '能耗费用',
  `production_output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `energy_intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度(单耗)',
  `benchmark_value` decimal(18,6) DEFAULT NULL COMMENT '对标值',
  `deviation_rate` decimal(10,4) DEFAULT NULL COMMENT '偏差率(%)',
  `ranking` int(4) DEFAULT NULL COMMENT '排名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_month_energy` (`target_code`, `stat_year`, `stat_month`, `energy_type`),
  KEY `idx_stat_year_month` (`stat_year`, `stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果月表';

-- 5. 对标结果年表
CREATE TABLE IF NOT EXISTS `tb_benchmark_result_year` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `target_code` varchar(50) NOT NULL COMMENT '对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对象名称',
  `stat_year` int(4) NOT NULL COMMENT '统计年份',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量',
  `energy_cost` decimal(18,4) DEFAULT 0 COMMENT '能耗费用',
  `production_output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `energy_intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度(单耗)',
  `benchmark_value` decimal(18,6) DEFAULT NULL COMMENT '对标值',
  `deviation_rate` decimal(10,4) DEFAULT NULL COMMENT '偏差率(%)',
  `ranking` int(4) DEFAULT NULL COMMENT '排名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_year_energy` (`target_code`, `stat_year`, `energy_type`),
  KEY `idx_stat_year` (`stat_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果年表';

-- ============================================
-- 插入初始配置数据
-- ============================================
INSERT INTO `tb_benchmark_config` (`id`, `config_code`, `config_name`, `benchmark_type`, `energy_type`, `indicator_type`, `unit`, `status`, `create_time`) VALUES
(UUID(), 'BENCHMARK_DEPT_ELEC', '部门电力对标', 2, '1', 'intensity', 'kWh/t', 1, NOW()),
(UUID(), 'BENCHMARK_DEPT_ALL', '部门综合能耗对标', 2, 'all', 'intensity', 'kgce/t', 1, NOW()),
(UUID(), 'BENCHMARK_LINE_ELEC', '生产线电力对标', 1, '1', 'intensity', 'kWh/t', 1, NOW());
