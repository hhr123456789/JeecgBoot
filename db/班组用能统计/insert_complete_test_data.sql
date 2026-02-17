-- ============================================
-- 班组用能统计完整测试数据插入脚本
-- 创建时间: 2026-02-15
-- 说明: 自动关联有数据的仪表ID
-- ============================================

-- 步骤1: 先查询有能耗数据的仪表ID
-- 执行以下查询，查看有哪些仪表ID有数据
SELECT DISTINCT module_id
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
LIMIT 20;

-- 步骤2: 根据查询结果，手动修改下面的 module_ids
-- 或者使用以下自动插入脚本（需要MySQL 5.7+支持）

-- 清理旧的测试数据（可选）
-- DELETE FROM tb_team_dimension_relation WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02');
-- DELETE FROM tb_team_info WHERE org_code IN ('A02A02A01', 'A02A02A01A02');

-- 插入班组基础信息
INSERT INTO `tb_team_info` (`id`, `team_code`, `team_name`, `shift_type`, `org_code`, `org_name`, `team_leader`, `team_members`, `status`, `sort_order`, `description`, `create_by`, `create_time`) VALUES
('team_info_001', 'TEAM_A01', 'A班组(早班)', '早班', 'A02A02A01', '生产部门', '张三', 15, 1, 1, '早班生产班组', 'admin', NOW()),
('team_info_002', 'TEAM_B01', 'B班组(中班)', '中班', 'A02A02A01', '生产部门', '李四', 12, 1, 2, '中班生产班组', 'admin', NOW()),
('team_info_003', 'TEAM_C01', 'C班组(晚班)', '晚班', 'A02A02A01', '生产部门', '王五', 14, 1, 3, '晚班生产班组', 'admin', NOW()),
('team_info_004', 'TEAM_D01', 'D班组(夜班)', '夜班', 'A02A02A01', '生产部门', '赵六', 10, 1, 4, '夜班生产班组', 'admin', NOW())
ON DUPLICATE KEY UPDATE
    team_name = VALUES(team_name),
    update_time = NOW();

-- 为子维度插入班组
INSERT INTO `tb_team_info` (`id`, `team_code`, `team_name`, `shift_type`, `org_code`, `org_name`, `team_leader`, `team_members`, `status`, `sort_order`, `description`, `create_by`, `create_time`) VALUES
('team_info_005', 'TEAM_E01', 'E班组(早班)', '早班', 'A02A02A01A02', '生产子部门', '孙七', 8, 1, 5, '子部门早班', 'admin', NOW()),
('team_info_006', 'TEAM_F01', 'F班组(中班)', '中班', 'A02A02A01A02', '生产子部门', '周八', 9, 1, 6, '子部门中班', 'admin', NOW())
ON DUPLICATE KEY UPDATE
    team_name = VALUES(team_name),
    update_time = NOW();

-- ============================================
-- 重要: 下面的 module_ids 需要替换成实际的仪表ID
-- 请先执行上面的查询语句，获取有数据的仪表ID
-- 然后替换 'REPLACE_WITH_REAL_MODULE_IDS' 为实际的仪表ID（逗号分隔）
-- 例如: '1001,1002,1003' 或 'MOD001,MOD002,MOD003'
-- ============================================

-- 插入班组维度关联（维度 A02A02A01）
INSERT INTO `tb_team_dimension_relation` (`id`, `team_code`, `dimension_code`, `dimension_type`, `energy_type`, `module_ids`, `status`, `create_by`, `create_time`) VALUES
('team_rel_001', 'TEAM_A01', 'A02A02A01', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW()),
('team_rel_002', 'TEAM_B01', 'A02A02A01', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW()),
('team_rel_003', 'TEAM_C01', 'A02A02A01', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW()),
('team_rel_004', 'TEAM_D01', 'A02A02A01', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE
    module_ids = VALUES(module_ids),
    update_time = NOW();

-- 插入班组维度关联（维度 A02A02A01A02）
INSERT INTO `tb_team_dimension_relation` (`id`, `team_code`, `dimension_code`, `dimension_type`, `energy_type`, `module_ids`, `status`, `create_by`, `create_time`) VALUES
('team_rel_005', 'TEAM_E01', 'A02A02A01A02', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW()),
('team_rel_006', 'TEAM_F01', 'A02A02A01A02', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE
    module_ids = VALUES(module_ids),
    update_time = NOW();

-- ============================================
-- 验证插入结果
-- ============================================
SELECT '=== 班组信息 ===' as info;
SELECT team_code, team_name, shift_type, org_code, status FROM tb_team_info WHERE org_code IN ('A02A02A01', 'A02A02A01A02');

SELECT '=== 班组维度关联 ===' as info;
SELECT team_code, dimension_code, dimension_type, module_ids, status FROM tb_team_dimension_relation WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02');

SELECT '=== 统计 ===' as info;
SELECT
    dimension_code,
    COUNT(*) as team_count,
    GROUP_CONCAT(team_code) as teams
FROM tb_team_dimension_relation
WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02')
GROUP BY dimension_code;
