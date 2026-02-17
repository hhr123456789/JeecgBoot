-- =====================================================
-- 产品单耗分析 - 数据检查脚本
-- 执行此脚本检查表是否存在及数据情况
-- =====================================================

USE EMSProject_jeecg;

-- 1. 检查表是否存在
SELECT '=== 检查表是否存在 ===' as '检查项';

SELECT
    TABLE_NAME as '表名',
    TABLE_ROWS as '预估行数',
    CREATE_TIME as '创建时间'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'EMSProject_jeecg'
AND TABLE_NAME IN (
    'tb_product_category',
    'tb_product_info',
    'tb_product_production',
    'tb_product_process_rel',
    'tb_product_energy_consumption'
);

-- 2. 检查各表数据量
SELECT '=== 检查各表数据量 ===' as '检查项';

SELECT 'tb_product_category' as '表名', COUNT(*) as '记录数' FROM tb_product_category
UNION ALL
SELECT 'tb_product_info', COUNT(*) FROM tb_product_info
UNION ALL
SELECT 'tb_product_production', COUNT(*) FROM tb_product_production
UNION ALL
SELECT 'tb_product_process_rel', COUNT(*) FROM tb_product_process_rel
UNION ALL
SELECT 'tb_product_energy_consumption', COUNT(*) FROM tb_product_energy_consumption;

-- 3. 检查能耗统计表的数据范围
SELECT '=== 检查能耗统计表数据范围 ===' as '检查项';

SELECT
    time_dimension as '时间维度',
    MIN(stat_date) as '最早日期',
    MAX(stat_date) as '最晚日期',
    COUNT(*) as '记录数'
FROM tb_product_energy_consumption
GROUP BY time_dimension;

-- 4. 检查是否有 energy_type 字段（新增的）
SELECT '=== 检查 energy_type 字段 ===' as '检查项';

SELECT
    COLUMN_NAME as '字段名',
    COLUMN_TYPE as '字段类型',
    IS_NULLABLE as '可空',
    COLUMN_DEFAULT as '默认值'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'EMSProject_jeecg'
AND TABLE_NAME = 'tb_product_energy_consumption'
AND COLUMN_NAME IN ('energy_type', 'energy_type_name', 'energy_unit');

-- 5. 检查各能源类型的数据
SELECT '=== 检查各能源类型数据 ===' as '检查项';

SELECT
    IFNULL(energy_type, '未设置') as '能源类型',
    IFNULL(energy_type_name, '未设置') as '能源名称',
    COUNT(*) as '记录数'
FROM tb_product_energy_consumption
GROUP BY energy_type, energy_type_name;

-- 6. 查看产品能耗统计表的示例数据
SELECT '=== 产品能耗统计表示例数据 ===' as '检查项';

SELECT
    product_code as '产品编码',
    energy_type as '能源类型',
    stat_date as '统计日期',
    time_dimension as '时间维度',
    total_consumption as '总能耗',
    total_production as '总产量',
    unit_consumption as '单耗'
FROM tb_product_energy_consumption
ORDER BY stat_date DESC, product_code
LIMIT 10;

SELECT '=== 检查完成 ===' as '状态';
