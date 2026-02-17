-- =============================================
-- 告警模板与告警规则功能 - 测试数据
-- 创建时间: 2026-02-17
-- =============================================

-- 1. 插入告警模板测试数据
INSERT INTO `tb_alarm_template` (`id`, `name`, `type`, `energy_type`, `device_type`, `target_scope`, `conditions`, `level`, `notify_methods`, `silence_period`, `description`, `dept_id`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
('TPL001', '燃气表设备告警模板', 'device', '8', 'GFMT', NULL,
 '[{"metric":"device_pressure","operator":"gt","threshold":0.8,"unit":"MPa","duration":5,"checkInterval":10}]',
 'high', '["system","sms"]', 30, '用于监控燃气表设备压力异常', NULL, 1,
 'admin', NOW(), 'admin', NOW()),

('TPL002', '工段用电量超限模板', 'energy', '1', NULL, 'department',
 '[{"metric":"day_consumption","operator":"gt","threshold":10000,"unit":"kWh","duration":5,"checkInterval":10}]',
 'high', '["system","email"]', 60, '用于监控各工段日用电量是否超限', NULL, 1,
 'admin', NOW(), 'admin', NOW()),

('TPL003', '空压机电流超限模板', 'device', '1', 'ACOP', NULL,
 '[{"metric":"device_current","operator":"gt","threshold":150,"unit":"A","duration":3,"checkInterval":5}]',
 'high', '["system","sms"]', 30, '用于监控空压机运行电流是否超限', NULL, 1,
 'admin', NOW(), 'admin', NOW()),

('TPL004', '水用量异常模板', 'energy', '2', NULL, 'department',
 '[{"metric":"day_consumption","operator":"gt","threshold":500,"unit":"m³","duration":10,"checkInterval":30}]',
 'medium', '["system","email"]', 120, '用于监控各部门日用水量是否异常', NULL, 1,
 'admin', NOW(), 'admin', NOW()),

('TPL005', '压缩空气用量监控模板', 'energy', '5', NULL, 'workshop',
 '[{"metric":"hour_consumption","operator":"gt","threshold":100,"unit":"m³","duration":15,"checkInterval":10}]',
 'medium', '["system"]', 60, '用于监控车间压缩空气小时用量', NULL, 1,
 'admin', NOW(), 'admin', NOW()),

('TPL006', '设备功率超限模板', 'device', '1', 'ACOP', NULL,
 '[{"metric":"device_power","operator":"gt","threshold":500,"unit":"kW","duration":5,"checkInterval":5}]',
 'high', '["system","sms","email"]', 30, '用于监控生产设备运行功率是否超限', NULL, 1,
 'admin', NOW(), 'admin', NOW());

-- 2. 插入告警规则测试数据
INSERT INTO `tb_alarm_rule` (`id`, `name`, `rule_type`, `energy_type`, `target_type`, `target_scope`, `conditions`, `level`, `notify_methods`, `notify_users`, `silence_period`, `remark`, `template_id`, `dept_id`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
('RULE001', '车间用电量超限告警', 'energy', '1', NULL, 'department',
 '[{"metric":"day_consumption","operator":"gt","threshold":10000,"unit":"kWh","duration":5,"checkInterval":10}]',
 'high', '["system","email"]', '["user1","user2"]', 60, '监控一号车间日用电量',
 'TPL002', NULL, 1, 'admin', NOW(), 'admin', NOW()),

('RULE002', '空压机运行电流超限', 'device', '1', 'ACOP', NULL,
 '[{"metric":"device_current","operator":"gt","threshold":150,"unit":"A","duration":3,"checkInterval":5}]',
 'high', '["system","sms"]', '["user1"]', 30, '监控空压机运行电流',
 'TPL003', NULL, 1, 'admin', NOW(), 'admin', NOW()),

('RULE003', '天然气日消耗量异常', 'energy', '8', NULL, 'department',
 '[{"metric":"day_consumption","operator":"gt","threshold":5000,"unit":"m³","duration":10,"checkInterval":30}]',
 'medium', '["system","email"]', '["user3"]', 120, '监控天然气日消耗量',
 NULL, NULL, 1, 'admin', NOW(), 'admin', NOW()),

('RULE004', '二号车间用水量告警', 'energy', '2', NULL, 'workshop',
 '[{"metric":"day_consumption","operator":"gt","threshold":300,"unit":"m³","duration":10,"checkInterval":30}]',
 'medium', '["system"]', '["user2","user3"]', 60, '监控二号车间日用水量',
 'TPL004', NULL, 1, 'admin', NOW(), 'admin', NOW()),

('RULE005', '生产线压缩空气用量监控', 'energy', '5', NULL, 'line',
 '[{"metric":"hour_consumption","operator":"gt","threshold":80,"unit":"m³","duration":15,"checkInterval":10}]',
 'low', '["system"]', '["user1"]', 120, '监控生产线压缩空气小时用量',
 'TPL005', NULL, 1, 'admin', NOW(), 'admin', NOW()),

('RULE006', '注塑机功率超限告警', 'device', '1', 'ACOP', NULL,
 '[{"metric":"device_power","operator":"gt","threshold":450,"unit":"kW","duration":5,"checkInterval":5},{"metric":"device_current","operator":"gt","threshold":200,"unit":"A","duration":3,"checkInterval":5}]',
 'high', '["system","sms","email"]', '["user1","user2","user3"]', 30, '监控注塑机运行功率和电流，多条件告警',
 'TPL006', NULL, 1, 'admin', NOW(), 'admin', NOW());
