-- 测试精确维度查询的SQL脚本
-- 验证修复后的查询逻辑

-- 1. 查看 A02A02 维度的基本信息
SELECT 
    id,
    org_code,
    depart_name,
    parent_id,
    '当前维度' as 类型
FROM sys_depart 
WHERE org_code = 'A02A02';

-- 2. 查看 A02A02 的直接子维度（不是所有以A02A02开头的）
SELECT 
    child.id,
    child.org_code,
    child.depart_name,
    child.parent_id,
    parent.org_code as parent_org_code,
    '直接子维度' as 类型
FROM sys_depart child
JOIN sys_depart parent ON child.parent_id = parent.id
WHERE parent.org_code = 'A02A02';

-- 3. 对比：所有以A02A02开头的维度（旧的模糊查询方式）
SELECT 
    id,
    org_code,
    depart_name,
    parent_id,
    '模糊查询结果' as 类型
FROM sys_depart 
WHERE org_code LIKE 'A02A02%'
ORDER BY org_code;

-- 4. 测试不包含子维度的查询（includeChildren=false）
-- 应该只返回 A02A02 这一个维度
SELECT 
    '不包含子维度查询' as 查询类型,
    d.id as 部门ID,
    d.org_code as 维度编码,
    d.depart_name as 部门名称,
    COUNT(m.module_id) as 仪表数量
FROM sys_depart d
LEFT JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code) AND m.isaction = 'Y'
WHERE d.org_code = 'A02A02'
GROUP BY d.id, d.org_code, d.depart_name;

-- 5. 测试包含子维度的查询（includeChildren=true）
-- 应该返回 A02A02 + 其直接子维度
WITH target_departs AS (
    -- 当前维度
    SELECT id, org_code, depart_name FROM sys_depart WHERE org_code = 'A02A02'
    UNION ALL
    -- 直接子维度
    SELECT child.id, child.org_code, child.depart_name
    FROM sys_depart child
    JOIN sys_depart parent ON child.parent_id = parent.id
    WHERE parent.org_code = 'A02A02'
)
SELECT 
    '包含子维度查询' as 查询类型,
    d.id as 部门ID,
    d.org_code as 维度编码,
    d.depart_name as 部门名称,
    COUNT(m.module_id) as 仪表数量
FROM target_departs d
LEFT JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code) AND m.isaction = 'Y'
GROUP BY d.id, d.org_code, d.depart_name
ORDER BY d.org_code;

-- 6. 验证数据结构：检查维度层级关系
SELECT 
    level1.org_code as 一级维度,
    level1.depart_name as 一级名称,
    level2.org_code as 二级维度,
    level2.depart_name as 二级名称,
    level3.org_code as 三级维度,
    level3.depart_name as 三级名称
FROM sys_depart level1
LEFT JOIN sys_depart level2 ON level2.parent_id = level1.id
LEFT JOIN sys_depart level3 ON level3.parent_id = level2.id
WHERE level1.org_code = 'A02A02'
ORDER BY level2.org_code, level3.org_code;

-- 7. 检查可能的问题：是否存在重复的维度编码
SELECT 
    org_code,
    COUNT(*) as 重复数量,
    GROUP_CONCAT(id) as 部门ID列表,
    GROUP_CONCAT(depart_name) as 部门名称列表
FROM sys_depart 
WHERE org_code LIKE 'A02A02%'
GROUP BY org_code
HAVING COUNT(*) > 1;

-- 8. 完整的测试查询：模拟修复后的接口逻辑
-- 不包含子维度的完整查询
SELECT 
    '精确查询-不含子维度' as 测试场景,
    d.org_code as 维度编码,
    d.depart_name as 维度名称,
    m.module_id as 仪表编号,
    m.module_name as 仪表名称
FROM sys_depart d
JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE d.org_code = 'A02A02'
AND m.isaction = 'Y'
ORDER BY m.module_name;

-- 包含子维度的完整查询
SELECT 
    '精确查询-含子维度' as 测试场景,
    d.org_code as 维度编码,
    d.depart_name as 维度名称,
    m.module_id as 仪表编号,
    m.module_name as 仪表名称
FROM (
    -- 当前维度
    SELECT id, org_code, depart_name FROM sys_depart WHERE org_code = 'A02A02'
    UNION ALL
    -- 直接子维度
    SELECT child.id, child.org_code, child.depart_name
    FROM sys_depart child
    JOIN sys_depart parent ON child.parent_id = parent.id
    WHERE parent.org_code = 'A02A02'
) d
JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE m.isaction = 'Y'
ORDER BY d.org_code, m.module_name;
