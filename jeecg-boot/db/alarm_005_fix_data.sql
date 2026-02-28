SET sql_safe_updates = 0;

-- =====================================================
-- 告警规则/模板数据修复脚本
-- 说明：
-- 1) 先备份“可能被修复”的数据到 *_bak_fix_20260219 表
-- 2) 再执行修复
-- 3) 最后输出修复后校验结果
-- =====================================================

-- ==================== 1. 备份 ====================
CREATE TABLE IF NOT EXISTS tb_alarm_rule_bak_fix_20260219 LIKE tb_alarm_rule;
INSERT IGNORE INTO tb_alarm_rule_bak_fix_20260219
SELECT *
FROM tb_alarm_rule
WHERE id IS NULL
   OR TRIM(id) = ''
   OR name IS NULL
   OR TRIM(name) = ''
   OR rule_type NOT IN ('device', 'energy')
   OR energy_type NOT IN ('1', '2', '5', '8')
   OR level NOT IN ('high', 'medium', 'low')
   OR status NOT IN (0, 1)
   OR conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0
   OR (notify_methods IS NOT NULL AND TRIM(notify_methods) <> '' AND JSON_VALID(notify_methods) = 0)
   OR (notify_users IS NOT NULL AND TRIM(notify_users) <> '' AND JSON_VALID(notify_users) = 0)
   OR (target_node_ids IS NOT NULL AND TRIM(target_node_ids) <> '' AND JSON_VALID(target_node_ids) = 0)
   OR (rule_type = 'energy' AND COALESCE(target_scope, '') <> 'device')
   OR (rule_type = 'device' AND (target_scope IS NULL OR TRIM(target_scope) = '' OR target_scope NOT IN ('selected', 'children', 'custom')))
   OR (status = 1 AND (target_node_id IS NULL OR TRIM(target_node_id) = '' OR target_node_name IS NULL OR TRIM(target_node_name) = ''));

CREATE TABLE IF NOT EXISTS tb_alarm_template_bak_fix_20260219 LIKE tb_alarm_template;
INSERT IGNORE INTO tb_alarm_template_bak_fix_20260219
SELECT *
FROM tb_alarm_template
WHERE id IS NULL
   OR TRIM(id) = ''
   OR name IS NULL
   OR TRIM(name) = ''
   OR type NOT IN ('device', 'energy')
   OR energy_type NOT IN ('1', '2', '5', '8')
   OR level NOT IN ('high', 'medium', 'low')
   OR status NOT IN (0, 1)
   OR conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0
   OR (notify_methods IS NOT NULL AND TRIM(notify_methods) <> '' AND JSON_VALID(notify_methods) = 0)
   OR (type = 'energy' AND COALESCE(target_scope, '') <> 'device');

START TRANSACTION;

-- ==================== 2. 规则修复 ====================
UPDATE tb_alarm_rule
SET rule_type = 'device',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE rule_type NOT IN ('device', 'energy') OR rule_type IS NULL OR TRIM(rule_type) = '';

UPDATE tb_alarm_rule
SET energy_type = '1',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE energy_type NOT IN ('1', '2', '5', '8') OR energy_type IS NULL OR TRIM(energy_type) = '';

UPDATE tb_alarm_rule
SET level = 'medium',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE level NOT IN ('high', 'medium', 'low') OR level IS NULL OR TRIM(level) = '';

UPDATE tb_alarm_rule
SET status = 1,
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE status NOT IN (0, 1) OR status IS NULL;

UPDATE tb_alarm_rule
SET conditions = '[]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE conditions IS NULL OR TRIM(conditions) = '' OR JSON_VALID(conditions) = 0;

UPDATE tb_alarm_rule
SET notify_methods = '["system"]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE notify_methods IS NULL OR TRIM(notify_methods) = '' OR JSON_VALID(notify_methods) = 0;

UPDATE tb_alarm_rule
SET notify_users = '[]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE notify_users IS NULL OR TRIM(notify_users) = '' OR JSON_VALID(notify_users) = 0;

UPDATE tb_alarm_rule
SET target_node_ids = '[]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE target_node_ids IS NULL OR TRIM(target_node_ids) = '' OR JSON_VALID(target_node_ids) = 0;

UPDATE tb_alarm_rule
SET target_scope = 'device',
    target_type = '',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE rule_type = 'energy'
  AND (COALESCE(target_scope, '') <> 'device' OR COALESCE(target_type, '') <> '');

UPDATE tb_alarm_rule
SET target_scope = 'selected',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE rule_type = 'device'
  AND (target_scope IS NULL OR TRIM(target_scope) = '' OR target_scope NOT IN ('selected', 'children', 'custom'));

UPDATE tb_alarm_rule r
LEFT JOIN tb_alarm_template t ON r.template_id = t.id
SET r.template_id = NULL,
    r.update_by = 'db_fix_script',
    r.update_time = NOW()
WHERE r.template_id IS NOT NULL
  AND TRIM(r.template_id) <> ''
  AND t.id IS NULL;

UPDATE tb_alarm_rule
SET status = 0,
    remark = CONCAT(IFNULL(remark, ''), IF(IFNULL(remark, '') = '', '', ' '), '[auto-disabled-by-alarm_005_fix_data]'),
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE status = 1
  AND (
    target_node_id IS NULL OR TRIM(target_node_id) = ''
    OR target_node_name IS NULL OR TRIM(target_node_name) = ''
    OR (rule_type = 'device' AND (target_type IS NULL OR TRIM(target_type) = ''))
  );

-- ==================== 3. 模板修复 ====================
UPDATE tb_alarm_template
SET type = 'device',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE type NOT IN ('device', 'energy') OR type IS NULL OR TRIM(type) = '';

UPDATE tb_alarm_template
SET energy_type = '1',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE energy_type NOT IN ('1', '2', '5', '8') OR energy_type IS NULL OR TRIM(energy_type) = '';

UPDATE tb_alarm_template
SET level = 'medium',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE level NOT IN ('high', 'medium', 'low') OR level IS NULL OR TRIM(level) = '';

UPDATE tb_alarm_template
SET status = 1,
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE status NOT IN (0, 1) OR status IS NULL;

UPDATE tb_alarm_template
SET conditions = '[]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE conditions IS NULL OR TRIM(conditions) = '' OR JSON_VALID(conditions) = 0;

UPDATE tb_alarm_template
SET notify_methods = '["system"]',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE notify_methods IS NULL OR TRIM(notify_methods) = '' OR JSON_VALID(notify_methods) = 0;

UPDATE tb_alarm_template
SET target_scope = 'device',
    device_type = '',
    update_by = 'db_fix_script',
    update_time = NOW()
WHERE type = 'energy'
  AND (COALESCE(target_scope, '') <> 'device' OR COALESCE(device_type, '') <> '');

COMMIT;

-- ==================== 4. 修复后校验 ====================
SELECT 'rule_invalid_json_after_fix' AS item, COUNT(*) AS total
FROM tb_alarm_rule
WHERE conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0
   OR notify_methods IS NULL
   OR TRIM(notify_methods) = ''
   OR JSON_VALID(notify_methods) = 0
   OR notify_users IS NULL
   OR TRIM(notify_users) = ''
   OR JSON_VALID(notify_users) = 0
   OR target_node_ids IS NULL
   OR TRIM(target_node_ids) = ''
   OR JSON_VALID(target_node_ids) = 0;

SELECT 'template_invalid_json_after_fix' AS item, COUNT(*) AS total
FROM tb_alarm_template
WHERE conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0
   OR notify_methods IS NULL
   OR TRIM(notify_methods) = ''
   OR JSON_VALID(notify_methods) = 0;

SELECT 'enabled_rule_missing_target_after_fix' AS item, COUNT(*) AS total
FROM tb_alarm_rule
WHERE status = 1
  AND (
    target_node_id IS NULL OR TRIM(target_node_id) = ''
    OR target_node_name IS NULL OR TRIM(target_node_name) = ''
    OR (rule_type = 'device' AND (target_type IS NULL OR TRIM(target_type) = ''))
  );

