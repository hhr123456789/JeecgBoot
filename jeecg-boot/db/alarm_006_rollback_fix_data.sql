SET sql_safe_updates = 0;

-- =====================================================
-- 告警规则/模板修复回滚脚本
-- 对应备份：
--   tb_alarm_rule_bak_fix_20260219
--   tb_alarm_template_bak_fix_20260219
--
-- 使用说明：
-- 1) 仅回滚“被 alarm_005_fix_data.sql 备份过的数据”
-- 2) 不处理备份范围之外的数据
-- =====================================================

START TRANSACTION;

-- ==================== 1. 回滚规则表 ====================
-- 删除当前表中对应备份ID记录
DELETE r
FROM tb_alarm_rule r
INNER JOIN tb_alarm_rule_bak_fix_20260219 b ON r.id = b.id;

-- 重新写回备份记录
INSERT INTO tb_alarm_rule (
  id,
  name,
  rule_type,
  energy_type,
  target_type,
  target_scope,
  target_node_id,
  target_node_name,
  target_node_ids,
  dimension_type,
  conditions,
  level,
  notify_methods,
  notify_users,
  silence_period,
  remark,
  template_id,
  dept_id,
  status,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  id,
  name,
  rule_type,
  energy_type,
  target_type,
  target_scope,
  target_node_id,
  target_node_name,
  target_node_ids,
  dimension_type,
  conditions,
  level,
  notify_methods,
  notify_users,
  silence_period,
  remark,
  template_id,
  dept_id,
  status,
  create_by,
  create_time,
  update_by,
  update_time
FROM tb_alarm_rule_bak_fix_20260219;

-- ==================== 2. 回滚模板表 ====================
DELETE t
FROM tb_alarm_template t
INNER JOIN tb_alarm_template_bak_fix_20260219 b ON t.id = b.id;

INSERT INTO tb_alarm_template (
  id,
  name,
  type,
  energy_type,
  device_type,
  target_scope,
  conditions,
  level,
  notify_methods,
  silence_period,
  description,
  dept_id,
  status,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  id,
  name,
  type,
  energy_type,
  device_type,
  target_scope,
  conditions,
  level,
  notify_methods,
  silence_period,
  description,
  dept_id,
  status,
  create_by,
  create_time,
  update_by,
  update_time
FROM tb_alarm_template_bak_fix_20260219;

COMMIT;

-- ==================== 3. 回滚结果校验 ====================
SELECT 'rollback_rule_restored' AS item, COUNT(*) AS total
FROM tb_alarm_rule r
INNER JOIN tb_alarm_rule_bak_fix_20260219 b ON r.id = b.id;

SELECT 'rollback_template_restored' AS item, COUNT(*) AS total
FROM tb_alarm_template t
INNER JOIN tb_alarm_template_bak_fix_20260219 b ON t.id = b.id;

