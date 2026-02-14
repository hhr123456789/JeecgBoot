-- ============================================
-- Team Energy Statistics Database Schema
-- Author: System
-- Date: 2026-01-24
-- Description: Database tables for team energy statistics with dynamic dimension support
-- ============================================

-- ============================================
-- Table 1: tb_team_info - Team Basic Information
-- ============================================
CREATE TABLE IF NOT EXISTS `tb_team_info` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `team_code` VARCHAR(50) NOT NULL COMMENT '班组编码',
  `team_name` VARCHAR(100) NOT NULL COMMENT '班组名称',
  `shift_type` VARCHAR(20) DEFAULT NULL COMMENT '班次类型(早班/中班/晚班/夜班)',
  `org_code` VARCHAR(64) DEFAULT NULL COMMENT '所属组织编码',
  `org_name` VARCHAR(200) DEFAULT NULL COMMENT '所属组织名称',
  `team_leader` VARCHAR(50) DEFAULT NULL COMMENT '班组长',
  `team_members` INT DEFAULT 0 COMMENT '班组人数',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` VARCHAR(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` VARCHAR(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` VARCHAR(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组基础信息表';

-- ============================================
-- Table 2: tb_team_dimension_relation - Team-Dimension Relationship
-- ============================================
CREATE TABLE IF NOT EXISTS `tb_team_dimension_relation` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `team_code` VARCHAR(50) NOT NULL COMMENT '班组编码',
  `dimension_code` VARCHAR(64) NOT NULL COMMENT '维度编码(如A01B03)',
  `dimension_type` INT NOT NULL COMMENT '维度类型(1-按部门用电,2-按线路用电,3-天然气,4-压缩空气,5-企业用水)',
  `energy_type` INT DEFAULT NULL COMMENT '能源类型(1-电,2-水,8-天然气,5-压缩空气)',
  `module_ids` TEXT DEFAULT NULL COMMENT '关联的仪表ID列表(逗号分隔)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` VARCHAR(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` VARCHAR(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` VARCHAR(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_dimension` (`team_code`, `dimension_code`, `dimension_type`),
  KEY `idx_dimension_code` (`dimension_code`),
  KEY `idx_dimension_type` (`dimension_type`),
  KEY `idx_team_code` (`team_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组维度关联表';

-- ============================================
-- Table 3: tb_team_shift_schedule - Team Shift Schedule
-- ============================================
CREATE TABLE IF NOT EXISTS `tb_team_shift_schedule` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `team_code` VARCHAR(50) NOT NULL COMMENT '班组编码',
  `shift_date` DATE NOT NULL COMMENT '排班日期',
  `shift_type` VARCHAR(20) NOT NULL COMMENT '班次类型(早班/中班/晚班/夜班)',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `work_hours` DECIMAL(5,2) DEFAULT NULL COMMENT '工作时长(小时)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0-取消,1-正常)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` VARCHAR(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` VARCHAR(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` VARCHAR(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_shift_date` (`team_code`, `shift_date`),
  KEY `idx_shift_date` (`shift_date`),
  KEY `idx_team_code` (`team_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组排班表';

-- ============================================
-- Insert Sample Data
-- ============================================

-- Sample team data
INSERT INTO `tb_team_info` (`id`, `team_code`, `team_name`, `shift_type`, `org_code`, `org_name`, `status`, `sort_order`) VALUES
('1', 'A-1', 'A-1班', '早班', 'A01B03', '1#车间', 1, 1),
('2', 'A-2', 'A-2班', '中班', 'A01B03', '1#车间', 1, 2),
('3', 'B-1', 'B-1班', '晚班', 'A01B03', '1#车间', 1, 3),
('4', '1-A', '1号班', '早班', 'A01B03C01', '线路1', 1, 4),
('5', '2-A', '2号班', '中班', 'A01B03C01', '线路1', 1, 5),
('6', '3-A', '3号班', '晚班', 'A01B03C01', '线路1', 1, 6);

-- Sample team-dimension relations (dimension_type: 1=按部门用电, 2=按线路用电)
INSERT INTO `tb_team_dimension_relation` (`id`, `team_code`, `dimension_code`, `dimension_type`, `energy_type`, `status`) VALUES
('1', 'A-1', 'A01B03', 1, 1, 1),
('2', 'A-2', 'A01B03', 1, 1, 1),
('3', 'B-1', 'A01B03', 1, 1, 1),
('4', '1-A', 'A01B03C01', 2, 1, 1),
('5', '2-A', 'A01B03C01', 2, 1, 1),
('6', '3-A', 'A01B03C01', 2, 1, 1);

-- Sample shift schedules for today
INSERT INTO `tb_team_shift_schedule` (`id`, `team_code`, `shift_date`, `shift_type`, `start_time`, `end_time`, `work_hours`, `status`) VALUES
('1', 'A-1', CURDATE(), '早班', '00:00:00', '12:00:00', 12.00, 1),
('2', 'A-2', CURDATE(), '中班', '12:00:00', '18:00:00', 6.00, 1),
('3', 'B-1', CURDATE(), '晚班', '18:00:00', '23:59:59', 6.00, 1);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================
-- Additional composite indexes for common queries
CREATE INDEX idx_team_dimension_type_status ON tb_team_dimension_relation(dimension_type, status);
CREATE INDEX idx_team_org_status ON tb_team_info(org_code, status);
