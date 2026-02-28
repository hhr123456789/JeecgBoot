SELECT 'tb_alarm_rule_total' AS item, COUNT(*) AS total FROM tb_alarm_rule;
SELECT 'tb_alarm_template_total' AS item, COUNT(*) AS total FROM tb_alarm_template;

SELECT id, name, rule_type, energy_type, status, update_time
FROM tb_alarm_rule
WHERE id IS NULL
   OR TRIM(id) = ''
   OR name IS NULL
   OR TRIM(name) = ''
   OR rule_type IS NULL
   OR TRIM(rule_type) = ''
   OR energy_type IS NULL
   OR TRIM(energy_type) = '';

SELECT id, name, type, energy_type, status, update_time
FROM tb_alarm_template
WHERE id IS NULL
   OR TRIM(id) = ''
   OR name IS NULL
   OR TRIM(name) = ''
   OR type IS NULL
   OR TRIM(type) = ''
   OR energy_type IS NULL
   OR TRIM(energy_type) = '';

SELECT id, name, rule_type, energy_type, status
FROM tb_alarm_rule
WHERE rule_type NOT IN ('device', 'energy')
   OR energy_type NOT IN ('1', '2', '5', '8')
   OR level NOT IN ('high', 'medium', 'low')
   OR status NOT IN (0, 1);

SELECT id, name, type, energy_type, status
FROM tb_alarm_template
WHERE type NOT IN ('device', 'energy')
   OR energy_type NOT IN ('1', '2', '5', '8')
   OR level NOT IN ('high', 'medium', 'low')
   OR status NOT IN (0, 1);

SELECT id, name, rule_type, target_type, target_scope, target_node_id, target_node_name, status
FROM tb_alarm_rule
WHERE status = 1
  AND (
    target_node_id IS NULL OR TRIM(target_node_id) = ''
    OR target_node_name IS NULL OR TRIM(target_node_name) = ''
    OR (rule_type = 'device' AND (target_type IS NULL OR TRIM(target_type) = ''))
    OR (rule_type = 'energy' AND COALESCE(target_scope, '') <> 'device')
  );

SELECT id, name, conditions
FROM tb_alarm_rule
WHERE conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0;

SELECT id, name, notify_methods, notify_users, target_node_ids
FROM tb_alarm_rule
WHERE (notify_methods IS NOT NULL AND TRIM(notify_methods) <> '' AND JSON_VALID(notify_methods) = 0)
   OR (notify_users IS NOT NULL AND TRIM(notify_users) <> '' AND JSON_VALID(notify_users) = 0)
   OR (target_node_ids IS NOT NULL AND TRIM(target_node_ids) <> '' AND JSON_VALID(target_node_ids) = 0);

SELECT id, name, conditions
FROM tb_alarm_template
WHERE conditions IS NULL
   OR TRIM(conditions) = ''
   OR JSON_VALID(conditions) = 0;

SELECT id, name, notify_methods
FROM tb_alarm_template
WHERE notify_methods IS NOT NULL
  AND TRIM(notify_methods) <> ''
  AND JSON_VALID(notify_methods) = 0;

SELECT id, name, conditions
FROM tb_alarm_rule
WHERE JSON_VALID(conditions) = 1
  AND (
    JSON_EXTRACT(conditions, '$[0].metric') IS NULL
    OR JSON_EXTRACT(conditions, '$[0].operator') IS NULL
    OR JSON_EXTRACT(conditions, '$[0].threshold') IS NULL
  );

SELECT id, name, conditions
FROM tb_alarm_template
WHERE JSON_VALID(conditions) = 1
  AND (
    JSON_EXTRACT(conditions, '$[0].metric') IS NULL
    OR JSON_EXTRACT(conditions, '$[0].operator') IS NULL
    OR JSON_EXTRACT(conditions, '$[0].threshold') IS NULL
  );

SELECT r.id, r.name, r.template_id
FROM tb_alarm_rule r
LEFT JOIN tb_alarm_template t ON r.template_id = t.id
WHERE r.template_id IS NOT NULL
  AND TRIM(r.template_id) <> ''
  AND t.id IS NULL;

SELECT name, rule_type, energy_type, COUNT(*) AS duplicate_count
FROM tb_alarm_rule
GROUP BY name, rule_type, energy_type
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

SELECT name, type, energy_type, COUNT(*) AS duplicate_count
FROM tb_alarm_template
GROUP BY name, type, energy_type
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

