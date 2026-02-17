-- ============================================
-- 班组用能统计测试数据插入脚本
-- 创建时间: 2026-02-15
-- 说明: 根据现有维度编码插入测试数据
-- ============================================

-- 1. 插入班组基础信息到 tb_team_info 表
-- 假设你的维度编码是 A02A02A01 (从日志中看到的)
INSERT INTO `tb_team_info` (`id`, `team_code`, `team_name`, `shift_type`, `org_code`, `org_name`, `status`, `sort_order`, `create_time`) VALUES
('1001', 'TEAM_A01', 'A班组', '早班', 'A02A02A01', '测试部门', 1, 1, NOW()),
('1002', 'TEAM_B01', 'B班组', '中班', 'A02A02A01', '测试部门', 1, 2, NOW()),
('1003', 'TEAM_C01', 'C班组', '晚班', 'A02A02A01', '测试部门', 1, 3, NOW()),
('1004', 'TEAM_D01', 'D班组', '夜班', 'A02A02A01', '测试部门', 1, 4, NOW())
ON DUPLICATE KEY UPDATE team_name = VALUES(team_name);

-- 2. 插入班组维度关联到 tb_team_dimension_relation 表
-- 注意: module_ids 需要替换成你实际的仪表ID，多个用逗号分隔
-- 例如: 'MOD001,MOD002,MOD003'
INSERT INTO `tb_team_dimension_relation` (`id`, `team_code`, `dimension_code`, `dimension_type`, `energy_type`, `module_ids`, `status`, `create_time`) VALUES
('2001', 'TEAM_A01', 'A02A02A01', 1, 1, 'YOUR_MODULE_ID_1,YOUR_MODULE_ID_2', 1, NOW()),
('2002', 'TEAM_B01', 'A02A02A01', 1, 1, 'YOUR_MODULE_ID_3,YOUR_MODULE_ID_4', 1, NOW()),
('2003', 'TEAM_C01', 'A02A02A01', 1, 1, 'YOUR_MODULE_ID_5,YOUR_MODULE_ID_6', 1, NOW()),
('2004', 'TEAM_D01', 'A02A02A01', 1, 1, 'YOUR_MODULE_ID_7,YOUR_MODULE_ID_8', 1, NOW())
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- 3. 如果你有其他维度编码 (从日志中看到 A02A02A01A02)
INSERT INTO `tb_team_info` (`id`, `team_code`, `team_name`, `shift_type`, `org_code`, `org_name`, `status`, `sort_order`, `create_time`) VALUES
('1005', 'TEAM_E01', 'E班组', '早班', 'A02A02A01A02', '测试子部门', 1, 5, NOW()),
('1006', 'TEAM_F01', 'F班组', '中班', 'A02A02A01A02', '测试子部门', 1, 6, NOW())
ON DUPLICATE KEY UPDATE team_name = VALUES(team_name);

INSERT INTO `tb_team_dimension_relation` (`id`, `team_code`, `dimension_code`, `dimension_type`, `energy_type`, `module_ids`, `status`, `create_time`) VALUES
('2005', 'TEAM_E01', 'A02A02A01A02', 1, 1, 'YOUR_MODULE_ID_9,YOUR_MODULE_ID_10', 1, NOW()),
('2006', 'TEAM_F01', 'A02A02A01A02', 1, 1, 'YOUR_MODULE_ID_11,YOUR_MODULE_ID_12', 1, NOW())
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- ============================================
-- 查询现有仪表ID的SQL (执行后替换上面的 YOUR_MODULE_ID_X)
-- ============================================
-- SELECT module_id, module_name FROM tb_module WHERE status = 1 LIMIT 20;

-- 或者查询能耗统计表中有数据的仪表ID
-- SELECT DISTINCT module_id FROM tb_ep_equ_energy_daycount WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) LIMIT 20;

-- ============================================
-- 验证数据是否插入成功
-- ============================================
-- SELECT * FROM tb_team_info WHERE org_code IN ('A02A02A01', 'A02A02A01A02');
-- SELECT * FROM tb_team_dimension_relation WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02');
