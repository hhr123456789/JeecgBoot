-- ============================================
-- 快速插入测试数据脚本（自动关联有数据的仪表）
-- 创建时间: 2026-02-15
-- 说明: 自动从能耗统计表中获取有数据的仪表ID
-- ============================================

-- 步骤1: 查看最近有数据的仪表ID
SELECT
    module_id,
    COUNT(*) as record_count,
    SUM(energy_count) as total_energy,
    MAX(dt) as last_date
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY module_id
ORDER BY total_energy DESC
LIMIT 20;

-- 步骤2: 插入班组基础信息
INSERT INTO tb_team_info (id, team_code, team_name, shift_type, org_code, org_name, team_leader, team_members, status, sort_order, create_by, create_time)
VALUES
('team_001', 'TEAM_A', 'A班组', '早班', 'A02A02A01', '生产部门', '张三', 15, 1, 1, 'admin', NOW()),
('team_002', 'TEAM_B', 'B班组', '中班', 'A02A02A01', '生产部门', '李四', 12, 1, 2, 'admin', NOW()),
('team_003', 'TEAM_C', 'C班组', '晚班', 'A02A02A01', '生产部门', '王五', 14, 1, 3, 'admin', NOW())
ON DUPLICATE KEY UPDATE team_name = VALUES(team_name);

-- 步骤3: 自动插入班组维度关联（自动获取前5个有数据的仪表ID）
-- 为 A02A02A01 维度下的 TEAM_A 班组
INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, energy_type, module_ids, status, create_by, create_time)
SELECT
    'team_rel_001',
    'TEAM_A',
    'A02A02A01',
    1,
    1,
    GROUP_CONCAT(DISTINCT module_id ORDER BY module_id SEPARATOR ','),
    1,
    'admin',
    NOW()
FROM (
    SELECT DISTINCT module_id
    FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 5
) t
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- 为 A02A02A01 维度下的 TEAM_B 班组
INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, energy_type, module_ids, status, create_by, create_time)
SELECT
    'team_rel_002',
    'TEAM_B',
    'A02A02A01',
    1,
    1,
    GROUP_CONCAT(DISTINCT module_id ORDER BY module_id SEPARATOR ','),
    1,
    'admin',
    NOW()
FROM (
    SELECT DISTINCT module_id
    FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 5, 5  -- 跳过前5个，取接下来的5个
) t
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- 为 A02A02A01 维度下的 TEAM_C 班组
INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, energy_type, module_ids, status, create_by, create_time)
SELECT
    'team_rel_003',
    'TEAM_C',
    'A02A02A01',
    1,
    1,
    GROUP_CONCAT(DISTINCT module_id ORDER BY module_id SEPARATOR ','),
    1,
    'admin',
    NOW()
FROM (
    SELECT DISTINCT module_id
    FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 10, 5  -- 跳过前10个，取接下来的5个
) t
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- 如果你还有其他维度编码（例如 A02A02A01A02），也可以插入
INSERT INTO tb_team_info (id, team_code, team_name, shift_type, org_code, org_name, team_leader, team_members, status, sort_order, create_by, create_time)
VALUES
('team_004', 'TEAM_D', 'D班组', '早班', 'A02A02A01A02', '生产子部门', '赵六', 10, 1, 4, 'admin', NOW()),
('team_005', 'TEAM_E', 'E班组', '中班', 'A02A02A01A02', '生产子部门', '孙七', 8, 1, 5, 'admin', NOW())
ON DUPLICATE KEY UPDATE team_name = VALUES(team_name);

INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, energy_type, module_ids, status, create_by, create_time)
SELECT
    'team_rel_004',
    'TEAM_D',
    'A02A02A01A02',
    1,
    1,
    GROUP_CONCAT(DISTINCT module_id ORDER BY module_id SEPARATOR ','),
    1,
    'admin',
    NOW()
FROM (
    SELECT DISTINCT module_id
    FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 3
) t
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, energy_type, module_ids, status, create_by, create_time)
SELECT
    'team_rel_005',
    'TEAM_E',
    'A02A02A01A02',
    1,
    1,
    GROUP_CONCAT(DISTINCT module_id ORDER BY module_id SEPARATOR ','),
    1,
    'admin',
    NOW()
FROM (
    SELECT DISTINCT module_id
    FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 3, 3
) t
ON DUPLICATE KEY UPDATE module_ids = VALUES(module_ids);

-- 步骤4: 验证插入结果
SELECT '========== 班组信息 ==========' as '';
SELECT team_code, team_name, shift_type, org_code, team_leader, status
FROM tb_team_info
WHERE org_code IN ('A02A02A01', 'A02A02A01A02')
ORDER BY sort_order;

SELECT '========== 班组维度关联 ==========' as '';
SELECT team_code, dimension_code, dimension_type,
       SUBSTRING(module_ids, 1, 50) as module_ids_preview,
       CHAR_LENGTH(module_ids) as module_ids_length,
       status
FROM tb_team_dimension_relation
WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02')
ORDER BY dimension_code, team_code;

SELECT '========== 统计信息 ==========' as '';
SELECT
    tdr.dimension_code,
    COUNT(DISTINCT tdr.team_code) as team_count,
    GROUP_CONCAT(DISTINCT ti.team_name ORDER BY ti.sort_order) as team_names
FROM tb_team_dimension_relation tdr
LEFT JOIN tb_team_info ti ON tdr.team_code = ti.team_code
WHERE tdr.dimension_code IN ('A02A02A01', 'A02A02A01A02')
GROUP BY tdr.dimension_code;

-- 步骤5: 测试查询（模拟后端查询逻辑）
SELECT '========== 测试查询: 获取 A02A02A01 维度下的班组 ==========' as '';
SELECT
    ti.team_code,
    ti.team_name,
    ti.shift_type,
    tdr.module_ids
FROM tb_team_dimension_relation tdr
INNER JOIN tb_team_info ti ON tdr.team_code = ti.team_code
WHERE tdr.dimension_code = 'A02A02A01'
  AND tdr.status = 1
  AND ti.status = 1
ORDER BY ti.sort_order;
