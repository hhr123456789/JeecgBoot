-- =============================================
-- 告警规则表结构补充脚本
-- 用途：为已存在环境补充维度树关联字段
-- =============================================

ALTER TABLE `tb_alarm_rule`
  ADD COLUMN `target_node_id` varchar(64) DEFAULT NULL COMMENT '监控节点ID（维度树选中的节点）' AFTER `target_scope`,
  ADD COLUMN `target_node_name` varchar(100) DEFAULT NULL COMMENT '监控节点名称' AFTER `target_node_id`,
  ADD COLUMN `target_node_ids` text DEFAULT NULL COMMENT '自定义选择的节点ID列表JSON' AFTER `target_node_name`,
  ADD COLUMN `dimension_type` int(11) DEFAULT NULL COMMENT '维度类型：1-按部门,2-按线路,3-天然气,4-压缩空气,5-企业用水' AFTER `target_node_ids`;

ALTER TABLE `tb_alarm_rule`
  ADD INDEX `idx_target_node_id` (`target_node_id`),
  ADD INDEX `idx_dimension_type` (`dimension_type`);

