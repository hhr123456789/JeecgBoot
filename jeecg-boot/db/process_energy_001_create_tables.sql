-- =====================================================
-- 工序能耗分析 - 数据库表创建脚本
-- 创建时间: 2026-02-17
-- 说明: 生产线/工序配置表，用于工序能耗分析
-- =====================================================

USE EMSProject_jeecg;

-- =====================================================
-- 表1: tb_production_line (生产线/工序配置表)
-- 说明: 树形结构，支持生产线-工序两级或多级结构
-- =====================================================
DROP TABLE IF EXISTS `tb_production_line`;
CREATE TABLE `tb_production_line` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级ID (根节点为空)',
  `line_code` varchar(50) NOT NULL COMMENT '编码 (唯一)',
  `line_name` varchar(100) NOT NULL COMMENT '名称',
  `line_type` varchar(20) DEFAULT 'line' COMMENT '类型 (line:生产线, process:工序)',
  `line_level` int(11) DEFAULT '1' COMMENT '层级 (1:生产线 2:工序)',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `module_ids` text COMMENT '关联的仪表ID列表 (逗号分隔)',
  `process_type` varchar(50) DEFAULT NULL COMMENT '工序类型 (main:主工艺过程, auxiliary:辅助工艺过程, utility:公用工程系统, subsidiary:附属生产系统)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_line_code` (`line_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_line_type` (`line_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产线/工序配置表';

-- =====================================================
-- 表2: tb_production_line_energy (生产线能源类型关联表)
-- 说明: 生产线/工序与能源类型、仪表的关联
-- =====================================================
DROP TABLE IF EXISTS `tb_production_line_energy`;
CREATE TABLE `tb_production_line_energy` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `line_id` varchar(32) NOT NULL COMMENT '生产线/工序ID',
  `energy_type` int(11) NOT NULL COMMENT '能源类型 (1:电 2:水 3:气 4:蒸汽)',
  `module_ids` text COMMENT '该能源类型关联的仪表ID列表 (逗号分隔)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_line_energy` (`line_id`, `energy_type`),
  KEY `idx_line_id` (`line_id`),
  KEY `idx_energy_type` (`energy_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产线能源类型关联表';

-- =====================================================
-- 插入测试数据
-- =====================================================

-- 生产线数据
INSERT INTO `tb_production_line` (`id`, `parent_id`, `line_code`, `line_name`, `line_type`, `line_level`, `sort_order`, `process_type`, `status`) VALUES
-- 熔炼生产线
('line_melting', NULL, 'melting', '熔炼生产线', 'line', 1, 1, NULL, 1),
('line_melting_jk', 'line_melting', 'melting-jk', 'JK熔炼生产线', 'line', 2, 1, NULL, 1),
('line_melting_mn', 'line_melting', 'melting-mn', 'MN熔炼生产线', 'line', 2, 2, NULL, 1),
-- 挤压生产线
('line_extrusion', NULL, 'extrusion', '挤压生产线', 'line', 1, 2, NULL, 1),
('line_extrusion_2800', 'line_extrusion', 'extrusion-2800', '2800T生产线', 'line', 2, 1, NULL, 1),
-- JK熔炼生产线下的工序
('process_jk_main', 'line_melting_jk', 'jk-main', '主工艺过程', 'process', 3, 1, 'main', 1),
('process_jk_aux', 'line_melting_jk', 'jk-auxiliary', '辅助工艺过程', 'process', 3, 2, 'auxiliary', 1),
('process_jk_utility', 'line_melting_jk', 'jk-utility', '公用工程系统', 'process', 3, 3, 'utility', 1),
('process_jk_sub', 'line_melting_jk', 'jk-subsidiary', '附属生产系统', 'process', 3, 4, 'subsidiary', 1);

-- 生产线能源类型关联数据 (需要根据实际仪表ID配置)
-- 这里先插入框架数据，module_ids 需要根据实际仪表配置
INSERT INTO `tb_production_line_energy` (`id`, `line_id`, `energy_type`, `module_ids`, `status`) VALUES
-- JK熔炼生产线 - 电
('ple_jk_elec', 'line_melting_jk', 1, NULL, 1),
-- JK熔炼生产线 - 水
('ple_jk_water', 'line_melting_jk', 2, NULL, 1),
-- JK熔炼生产线 - 气
('ple_jk_gas', 'line_melting_jk', 3, NULL, 1),
-- MN熔炼生产线 - 电
('ple_mn_elec', 'line_melting_mn', 1, NULL, 1),
-- 2800T生产线 - 电
('ple_2800_elec', 'line_extrusion_2800', 1, NULL, 1);

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT 'Production Line Tables Created Successfully!' as Status;
