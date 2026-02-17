-- =============================================
-- 告警模板与告警规则功能 - 建表脚本
-- 创建时间: 2026-02-17
-- =============================================

-- 1. 告警模板表
DROP TABLE IF EXISTS `tb_alarm_template`;
CREATE TABLE `tb_alarm_template` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `type` varchar(20) NOT NULL COMMENT '模板类型：device-设备告警,energy-能源告警',
  `energy_type` varchar(10) NOT NULL COMMENT '能源类型：1-电,2-水,8-天然气,5-压缩空气',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型（设备告警用）：GFMT/CEC/ACOP/WMCT/METE/ELEV',
  `target_scope` varchar(50) DEFAULT NULL COMMENT '监控范围（能源告警用）：department/line/workshop/device',
  `conditions` text NOT NULL COMMENT '告警条件配置JSON数组',
  `level` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '默认告警级别：high/medium/low',
  `notify_methods` varchar(200) DEFAULT NULL COMMENT '默认通知方式JSON数组：system/email/sms/wechat',
  `silence_period` int(11) DEFAULT '60' COMMENT '默认静默期（分钟）',
  `description` varchar(500) DEFAULT NULL COMMENT '模板说明',
  `dept_id` varchar(64) DEFAULT NULL COMMENT '所属部门ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-启用,0-禁用',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_status` (`status`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警模板表';

-- 2. 告警规则表
DROP TABLE IF EXISTS `tb_alarm_rule`;
CREATE TABLE `tb_alarm_rule` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(20) NOT NULL COMMENT '告警类型：device-设备告警,energy-能源告警',
  `energy_type` varchar(10) NOT NULL COMMENT '能源类型：1-电,2-水,8-天然气,5-压缩空气',
  `target_type` varchar(50) DEFAULT NULL COMMENT '监控对象类型（设备告警用）',
  `target_scope` varchar(50) DEFAULT NULL COMMENT '监控范围（能源告警用）',
  `conditions` text NOT NULL COMMENT '告警条件配置JSON数组',
  `level` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '告警级别：high/medium/low',
  `notify_methods` varchar(200) DEFAULT NULL COMMENT '通知方式JSON数组',
  `notify_users` varchar(500) DEFAULT NULL COMMENT '通知人员JSON数组',
  `silence_period` int(11) DEFAULT '60' COMMENT '静默期（分钟）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `template_id` varchar(64) DEFAULT NULL COMMENT '关联模板ID',
  `dept_id` varchar(64) DEFAULT NULL COMMENT '所属部门ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-启用,0-禁用',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_energy_type` (`energy_type`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_status` (`status`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';
