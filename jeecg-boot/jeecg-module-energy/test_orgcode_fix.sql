-- 测试维度编码映射修复的SQL脚本
-- 用于验证数据结构和查询逻辑

-- 1. 检查维度表中的数据结构
SELECT 
    id,
    org_code,
    depart_name,
    parent_id
FROM sys_depart 
WHERE org_code LIKE 'A02A02%'
ORDER BY org_code;

-- 2. 检查仪表表中的 sys_org_code 字段格式
SELECT 
    module_id,
    module_name,
    sys_org_code,
    isaction,
    LENGTH(sys_org_code) as org_code_length,
    CASE 
        WHEN sys_org_code LIKE '%,%' THEN '多个部门ID'
        ELSE '单个部门ID'
    END as org_code_type
FROM tb_module 
WHERE isaction = 'Y'
LIMIT 10;

-- 3. 测试 FIND_IN_SET 查询（模拟现有接口的查询方式）
-- 假设部门ID为 '6d35e179cd814e3299bd588ea7daed3f'
SELECT 
    module_id,
    module_name,
    sys_org_code
FROM tb_module 
WHERE FIND_IN_SET('6d35e179cd814e3299bd588ea7daed3f', sys_org_code)
AND isaction = 'Y';

-- 4. 验证维度编码到部门ID的映射
SELECT 
    d.org_code as 维度编码,
    d.id as 部门ID,
    d.depart_name as 部门名称,
    COUNT(m.module_id) as 仪表数量
FROM sys_depart d
LEFT JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code) AND m.isaction = 'Y'
WHERE d.org_code LIKE 'A02A02%'
GROUP BY d.org_code, d.id, d.depart_name
ORDER BY d.org_code;

-- 5. 检查是否存在子维度
SELECT 
    parent.org_code as 父维度编码,
    parent.depart_name as 父维度名称,
    child.org_code as 子维度编码,
    child.depart_name as 子维度名称
FROM sys_depart parent
LEFT JOIN sys_depart child ON child.parent_id = parent.id
WHERE parent.org_code = 'A02A02'
ORDER BY child.org_code;

-- 6. 完整的关联查询（模拟修复后的逻辑）
SELECT 
    d.org_code as 维度编码,
    d.depart_name as 维度名称,
    m.module_id as 仪表编号,
    m.module_name as 仪表名称,
    m.energy_type as 能源类型
FROM sys_depart d
JOIN tb_module m ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE (d.org_code = 'A02A02' OR d.org_code LIKE 'A02A02%')
AND m.isaction = 'Y'
ORDER BY d.org_code, m.module_name;

-- 7. 检查数据一致性
-- 查找可能存在的数据问题
SELECT 
    '孤立的仪表记录' as 问题类型,
    m.module_id,
    m.module_name,
    m.sys_org_code
FROM tb_module m
LEFT JOIN sys_depart d ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE d.id IS NULL 
AND m.isaction = 'Y'
AND m.sys_org_code IS NOT NULL
AND m.sys_org_code != ''

UNION ALL

SELECT 
    '空的维度编码' as 问题类型,
    m.module_id,
    m.module_name,
    m.sys_org_code
FROM tb_module m
WHERE m.isaction = 'Y'
AND (m.sys_org_code IS NULL OR m.sys_org_code = '');
