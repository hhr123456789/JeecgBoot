/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80000
 Source Host           : localhost:3306
 Source Schema         : jeecg-boot

 Target Server Type    : MySQL
 Target Server Version : 80000
 File Encoding         : 65001

 Date: 23/01/2026 10:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_energy_team
-- ----------------------------
DROP TABLE IF EXISTS `tb_energy_team`;
CREATE TABLE `tb_energy_team` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) NOT NULL COMMENT '班组名称',
  `shift_type` varchar(50) DEFAULT NULL COMMENT '班次类型(早班/中班/晚班)',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能耗班组表';

-- ----------------------------
-- Table structure for tb_energy_team_rel
-- ----------------------------
DROP TABLE IF EXISTS `tb_energy_team_rel`;
CREATE TABLE `tb_energy_team_rel` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `team_id` varchar(36) NOT NULL COMMENT '班组ID',
  `dimension_code` varchar(100) NOT NULL COMMENT '维度节点编码(如部门ID: A01)',
  `dimension_type` varchar(50) NOT NULL COMMENT '维度类型(1:组织架构, 2:时间维度, 3:设备类型)',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_dim_code` (`dimension_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组维度关联表';

-- ----------------------------
-- Table structure for tb_energy_dimension_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_energy_dimension_config`;
CREATE TABLE `tb_energy_dimension_config` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `dimension_code` varchar(50) NOT NULL COMMENT '维度编码(1/2/3)',
  `dimension_name` varchar(100) NOT NULL COMMENT '维度名称',
  `sort_order` int(11) DEFAULT 0,
  `is_enable` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度定义配置表';

-- ----------------------------
-- Records of tb_energy_dimension_config
-- ----------------------------
BEGIN;
INSERT INTO `tb_energy_dimension_config` (`id`, `dimension_code`, `dimension_name`, `sort_order`, `is_enable`) VALUES
('1', '1', '组织架构', 1, 1),
('2', '2', '时间维度', 2, 1),
('3', '3', '设备类型', 3, 1);
COMMIT;

-- ----------------------------
-- Records of tb_energy_team (Test Data)
-- ----------------------------
BEGIN;
INSERT INTO `tb_energy_team` (`id`, `team_code`, `team_name`, `shift_type`, `create_time`) VALUES
('t1', 'A-1', 'A-1班', '早班', NOW()),
('t2', 'A-2', 'A-2班', '中班', NOW()),
('t3', 'B-1', 'B-1班', '晚班', NOW());
COMMIT;

-- ----------------------------
-- Records of tb_energy_team_rel (Test Data)
-- ----------------------------
BEGIN;
-- 关联到 组织架构(1) -> 1#车间(A01B03) -> 线路1(A01B03C01)
INSERT INTO `tb_energy_team_rel` (`id`, `team_id`, `dimension_code`, `dimension_type`, `create_by`, `create_time`) VALUES
('r1', 't1', 'A01B03C01', '1', 'system', NOW()),
('r2', 't2', 'A01B03C01', '1', 'system', NOW()),
('r3', 't3', 'A01B03C02', '1', 'system', NOW());
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
