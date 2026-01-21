-- ============================================
-- 班组用能管理相关表创建脚本
-- 创建时间: 2026-01-16
-- 功能: 支持班组维度的能源数据统计和管理
-- ============================================

-- 1. 班组基础信息表
CREATE TABLE `tb_team` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码(唯一标识)',
  `team_name` varchar(100) NOT NULL COMMENT '班组名称',
  `team_type` varchar(20) DEFAULT NULL COMMENT '班组类型(生产班组/维修班组/辅助班组等)',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型(早班/中班/晚班/夜班)',
  `work_start_time` time DEFAULT NULL COMMENT '工作开始时间',
  `work_end_time` time DEFAULT NULL COMMENT '工作结束时间',
  `org_code` varchar(64) NOT NULL COMMENT '所属部门编码(关联sys_depart.org_code)',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `leader_id` varchar(50) DEFAULT NULL COMMENT '班组长用户ID',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '班组长姓名',
  `member_count` int(5) DEFAULT '0' COMMENT '班组人数',
  `description` varchar(500) DEFAULT NULL COMMENT '班组描述',
  `status` varchar(1) DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `sort_order` int(5) DEFAULT '0' COMMENT '排序',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_team_type` (`team_type`),
  KEY `idx_shift_type` (`shift_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组基础信息表';

-- 2. 班组仪表关联表
CREATE TABLE `tb_team_module_rel` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID(关联tb_team.id)',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `module_id` varchar(50) NOT NULL COMMENT '仪表编号(关联tb_module.module_id)',
  `module_name` varchar(100) DEFAULT NULL COMMENT '仪表名称',
  `energy_type` int(10) DEFAULT NULL COMMENT '能源类型(1:电 2:水 3:燃气 5:压缩空气 8:天然气)',
  `allocation_ratio` decimal(5,2) DEFAULT '100.00' COMMENT '分配比例(%,支持多班组共用仪表)',
  `start_date` date DEFAULT NULL COMMENT '关联开始日期',
  `end_date` date DEFAULT NULL COMMENT '关联结束日期(NULL表示持续有效)',
  `status` varchar(1) DEFAULT '1' COMMENT '状态(1:有效 0:无效)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_team_code` (`team_code`),
  KEY `idx_module_id` (`module_id`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组仪表关联表';

-- 3. 班组能源日统计表
CREATE TABLE `tb_team_energy_daycount` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `org_code` varchar(64) NOT NULL COMMENT '所属部门编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `energy_type_name` varchar(50) DEFAULT NULL COMMENT '能源类型名称',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总消耗量',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量(kg)',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量(tce)',
  `peak_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段消耗',
  `peak_cost` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段费用',
  `flat_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '平时段消耗',
  `flat_cost` decimal(18,2) DEFAULT '0.00' COMMENT '平时段费用',
  `valley_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段消耗',
  `valley_cost` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段费用',
  `meter_count` int(10) DEFAULT '0' COMMENT '关联仪表数量',
  `hour_00` decimal(18,2) DEFAULT '0.00' COMMENT '00:00-01:00消耗',
  `hour_01` decimal(18,2) DEFAULT '0.00' COMMENT '01:00-02:00消耗',
  `hour_02` decimal(18,2) DEFAULT '0.00' COMMENT '02:00-03:00消耗',
  `hour_03` decimal(18,2) DEFAULT '0.00' COMMENT '03:00-04:00消耗',
  `hour_04` decimal(18,2) DEFAULT '0.00' COMMENT '04:00-05:00消耗',
  `hour_05` decimal(18,2) DEFAULT '0.00' COMMENT '05:00-06:00消耗',
  `hour_06` decimal(18,2) DEFAULT '0.00' COMMENT '06:00-07:00消耗',
  `hour_07` decimal(18,2) DEFAULT '0.00' COMMENT '07:00-08:00消耗',
  `hour_08` decimal(18,2) DEFAULT '0.00' COMMENT '08:00-09:00消耗',
  `hour_09` decimal(18,2) DEFAULT '0.00' COMMENT '09:00-10:00消耗',
  `hour_10` decimal(18,2) DEFAULT '0.00' COMMENT '10:00-11:00消耗',
  `hour_11` decimal(18,2) DEFAULT '0.00' COMMENT '11:00-12:00消耗',
  `hour_12` decimal(18,2) DEFAULT '0.00' COMMENT '12:00-13:00消耗',
  `hour_13` decimal(18,2) DEFAULT '0.00' COMMENT '13:00-14:00消耗',
  `hour_14` decimal(18,2) DEFAULT '0.00' COMMENT '14:00-15:00消耗',
  `hour_15` decimal(18,2) DEFAULT '0.00' COMMENT '15:00-16:00消耗',
  `hour_16` decimal(18,2) DEFAULT '0.00' COMMENT '16:00-17:00消耗',
  `hour_17` decimal(18,2) DEFAULT '0.00' COMMENT '17:00-18:00消耗',
  `hour_18` decimal(18,2) DEFAULT '0.00' COMMENT '18:00-19:00消耗',
  `hour_19` decimal(18,2) DEFAULT '0.00' COMMENT '19:00-20:00消耗',
  `hour_20` decimal(18,2) DEFAULT '0.00' COMMENT '20:00-21:00消耗',
  `hour_21` decimal(18,2) DEFAULT '0.00' COMMENT '21:00-22:00消耗',
  `hour_22` decimal(18,2) DEFAULT '0.00' COMMENT '22:00-23:00消耗',
  `hour_23` decimal(18,2) DEFAULT '0.00' COMMENT '23:00-00:00消耗',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_energy_day` (`team_code`,`energy_type`,`stat_date`,`shift_type`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_shift_type` (`shift_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组能源日统计表(支持按小时统计)';

-- 4. 班组能源月统计表
CREATE TABLE `tb_team_energy_monthcount` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `org_code` varchar(64) NOT NULL COMMENT '所属部门编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `energy_type_name` varchar(50) DEFAULT NULL COMMENT '能源类型名称',
  `stat_month` varchar(7) NOT NULL COMMENT '统计月份(YYYY-MM)',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总消耗量',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量(kg)',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量(tce)',
  `peak_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段消耗',
  `peak_cost` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段费用',
  `flat_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '平时段消耗',
  `flat_cost` decimal(18,2) DEFAULT '0.00' COMMENT '平时段费用',
  `valley_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段消耗',
  `valley_cost` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段费用',
  `meter_count` int(10) DEFAULT '0' COMMENT '关联仪表数量',
  `avg_daily_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '日均消耗',
  `max_daily_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '日最大消耗',
  `min_daily_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '日最小消耗',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_energy_month` (`team_code`,`energy_type`,`stat_month`,`shift_type`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_stat_month` (`stat_month`),
  KEY `idx_shift_type` (`shift_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组能源月统计表';

-- 5. 班组能源年统计表
CREATE TABLE `tb_team_energy_yearcount` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `org_code` varchar(64) NOT NULL COMMENT '所属部门编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `energy_type_name` varchar(50) DEFAULT NULL COMMENT '能源类型名称',
  `stat_year` varchar(4) NOT NULL COMMENT '统计年份(YYYY)',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总消耗量',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量(kg)',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量(tce)',
  `peak_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段消耗',
  `peak_cost` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段费用',
  `flat_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '平时段消耗',
  `flat_cost` decimal(18,2) DEFAULT '0.00' COMMENT '平时段费用',
  `valley_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段消耗',
  `valley_cost` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段费用',
  `meter_count` int(10) DEFAULT '0' COMMENT '关联仪表数量',
  `avg_monthly_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '月均消耗',
  `max_monthly_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '月最大消耗',
  `min_monthly_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '月最小消耗',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_energy_year` (`team_code`,`energy_type`,`stat_year`,`shift_type`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_stat_year` (`stat_year`),
  KEY `idx_shift_type` (`shift_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组能源年统计表';

-- 6. 班组能耗预算表
CREATE TABLE `tb_team_energy_budget` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `budget_year` varchar(4) NOT NULL COMMENT '预算年度',
  `budget_month` varchar(7) DEFAULT NULL COMMENT '预算月度(YYYY-MM,NULL表示年度预算)',
  `budget_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '预算消耗量',
  `budget_cost` decimal(18,2) DEFAULT '0.00' COMMENT '预算费用',
  `actual_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '实际消耗量',
  `actual_cost` decimal(18,2) DEFAULT '0.00' COMMENT '实际费用',
  `completion_rate` decimal(5,2) DEFAULT '0.00' COMMENT '完成率(%)',
  `over_budget` varchar(1) DEFAULT '0' COMMENT '是否超预算(1:是 0:否)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_team_code` (`team_code`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_budget_year` (`budget_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组能耗预算表';

-- 7. 班组能耗预警表
CREATE TABLE `tb_team_energy_alarm` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `alarm_type` varchar(20) NOT NULL COMMENT '预警类型(budget:预算超标 abnormal:异常消耗 peak:峰值告警)',
  `alarm_level` varchar(10) DEFAULT 'warning' COMMENT '预警级别(info:提示 warning:警告 error:严重)',
  `alarm_time` datetime NOT NULL COMMENT '预警时间',
  `stat_date` date DEFAULT NULL COMMENT '统计日期',
  `alarm_value` decimal(18,2) DEFAULT '0.00' COMMENT '预警数值',
  `threshold_value` decimal(18,2) DEFAULT '0.00' COMMENT '阈值',
  `alarm_message` varchar(500) DEFAULT NULL COMMENT '预警信息',
  `is_handled` varchar(1) DEFAULT '0' COMMENT '是否已处理(1:已处理 0:未处理)',
  `handle_by` varchar(50) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_team_code` (`team_code`),
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_alarm_time` (`alarm_time`),
  KEY `idx_is_handled` (`is_handled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='班组能耗预警表';

-- ============================================
-- 插入模拟数据
-- ============================================

-- 插入班组基础数据
INSERT INTO `tb_team` (`id`, `team_code`, `team_name`, `team_type`, `shift_type`, `work_start_time`, `work_end_time`, `org_code`, `org_name`, `leader_name`, `member_count`, `status`, `sort_order`, `create_by`) VALUES
('team_001', 'A-1', 'A-1班', '生产班组', '早班', '00:00:00', '12:00:00', 'A01B03', '1#车间', '张三', 15, '1', 1, 'admin'),
('team_002', 'A-2', 'A-2班', '生产班组', '中班', '12:00:00', '18:00:00', 'A01B03', '1#车间', '李四', 12, '1', 2, 'admin'),
('team_003', 'B-1', 'B-1班', '生产班组', '晚班', '18:00:00', '24:00:00', 'A01B03', '1#车间', '王五', 14, '1', 3, 'admin'),
('team_004', '1-A', '1号班', '生产班组', '早班', '00:00:00', '12:00:00', 'A01B03C01', '线路1', '赵六', 10, '1', 4, 'admin'),
('team_005', '2-A', '2号班', '生产班组', '中班', '12:00:00', '18:00:00', 'A01B03C01', '线路1', '孙七', 8, '1', 5, 'admin'),
('team_006', '3-A', '3号班', '生产班组', '晚班', '18:00:00', '24:00:00', 'A01B03C01', '线路1', '周八', 9, '1', 6, 'admin');

-- 插入班组能源日统计模拟数据
INSERT INTO `tb_team_energy_daycount` (`id`, `team_id`, `team_code`, `team_name`, `org_code`, `org_name`, `energy_type`, `energy_type_name`, `stat_date`, `shift_type`, `total_consumption`, `total_cost`, `carbon_emission`, `standard_coal`, `meter_count`,
  `hour_00`, `hour_01`, `hour_02`, `hour_03`, `hour_04`, `hour_05`, `hour_06`, `hour_07`, `hour_08`, `hour_09`, `hour_10`, `hour_11`, 
  `hour_12`, `hour_13`, `hour_14`, `hour_15`, `hour_16`, `hour_17`, `hour_18`, `hour_19`, `hour_20`, `hour_21`, `hour_22`, `hour_23`) VALUES
('day_001', 'team_001', 'A-1', 'A-1班', 'A01B03', '1#车间', 1, '电能', '2026-01-15', '早班', 84.00, 67.20, 83.75, 10.33, 5,
  7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0,
  0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('day_002', 'team_002', 'A-2', 'A-2班', 'A01B03', '1#车间', 1, '电能', '2026-01-15', '中班', 36.00, 28.80, 35.89, 4.43, 5,
  0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
  6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('day_003', 'team_003', 'B-1', 'B-1班', 'A01B03', '1#车间', 1, '电能', '2026-01-15', '晚班', 42.00, 33.60, 41.87, 5.16, 5,
  0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
  0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0);

-- 插入班组能源月统计模拟数据
INSERT INTO `tb_team_energy_monthcount` (`id`, `team_id`, `team_code`, `team_name`, `org_code`, `org_name`, `energy_type`, `energy_type_name`, `stat_month`, `shift_type`, `total_consumption`, `total_cost`, `carbon_emission`, `standard_coal`, `meter_count`, `avg_daily_consumption`, `max_daily_consumption`, `min_daily_consumption`) VALUES
('month_001', 'team_001', 'A-1', 'A-1班', 'A01B03', '1#车间', 1, '电能', '2026-01', '早班', 41.65, 33.32, 41.52, 5.12, 5, 83.30, 90.00, 75.00),
('month_002', 'team_002', 'A-2', 'A-2班', 'A01B03', '1#车间', 1, '电能', '2026-01', '中班', 40.15, 32.12, 40.03, 4.93, 5, 80.30, 88.00, 70.00),
('month_003', 'team_003', 'B-1', 'B-1班', 'A01B03', '1#车间', 1, '电能', '2026-01', '晚班', 42.53, 34.02, 42.40, 5.23, 5, 85.06, 95.00, 78.00);
