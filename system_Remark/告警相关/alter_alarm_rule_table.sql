-- 告警规则表新增字段 - 支持维度树选择
-- 执行时间: 2026-02-18
-- 说明: 为告警规则表添加维度树关联字段，支持从维度树选择监控对象

-- 添加监控节点ID字段
ALTER TABLE `tb_alarm_rule`
ADD COLUMN `target_node_id` varchar(64) DEFAULT NULL COMMENT '监控节点ID（维度树选中的节点）' AFTER `target_scope`;

-- 添加监控节点名称字段
ALTER TABLE `tb_alarm_rule`
ADD COLUMN `target_node_name` varchar(200) DEFAULT NULL COMMENT '监控节点名称' AFTER `target_node_id`;

-- 添加自定义选择的节点ID列表字段
ALTER TABLE `tb_alarm_rule`
ADD COLUMN `target_node_ids` text DEFAULT NULL COMMENT '自定义选择的节点ID列表JSON' AFTER `target_node_name`;

-- 添加维度类型字段
ALTER TABLE `tb_alarm_rule`
ADD COLUMN `dimension_type` int(11) DEFAULT NULL COMMENT '维度类型：1-按部门(用电),2-按线路(用电),3-天然气,4-压缩空气,5-企业用水' AFTER `target_node_ids`;

-- 添加索引
ALTER TABLE `tb_alarm_rule` ADD INDEX `idx_target_node_id` (`target_node_id`);
ALTER TABLE `tb_alarm_rule` ADD INDEX `idx_dimension_type` (`dimension_type`);

-- 更新 target_scope 字段注释
ALTER TABLE `tb_alarm_rule`
MODIFY COLUMN `target_scope` varchar(50) DEFAULT NULL COMMENT '监控范围：selected-仅选中节点,children-包含子节点,custom-自定义选择';
