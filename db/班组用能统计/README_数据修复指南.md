# 班组用能统计数据修复指南

## 问题分析

根据日志分析，发现两个问题：

### 问题1: 没有数据显示
**原因**: `tb_team_dimension_relation` 表中没有对应维度编码的数据
- 日志显示查询 `dimension_code = 'A02A02A01'` 返回 0 条记录
- 需要插入班组信息和班组维度关联数据

### 问题2: 班组下拉只有"全部班组"
**原因**: 前端调用 `getTeamListByDimension` 接口时，如果 `tb_team_dimension_relation` 表为空，就无法查询到班组列表

## 解决方案

### 步骤1: 查询现有仪表ID

执行 `query_module_ids.sql` 中的SQL，找到有能耗数据的仪表ID：

```sql
-- 查询最近7天有数据的仪表ID
SELECT DISTINCT module_id
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
LIMIT 20;
```

记录下查询结果，例如: `1001, 1002, 1003, 1004, 1005`

### 步骤2: 修改并执行插入脚本

1. 打开 `insert_complete_test_data.sql`
2. 将所有 `REPLACE_WITH_REAL_MODULE_IDS` 替换为步骤1查询到的仪表ID（逗号分隔）
3. 执行SQL脚本

**示例**:
```sql
-- 修改前
('team_rel_001', 'TEAM_A01', 'A02A02A01', 1, 1, 'REPLACE_WITH_REAL_MODULE_IDS', 1, 'admin', NOW())

-- 修改后（假设查询到的仪表ID是 1001,1002,1003）
('team_rel_001', 'TEAM_A01', 'A02A02A01', 1, 1, '1001,1002,1003', 1, 'admin', NOW())
```

### 步骤3: 验证数据

执行以下SQL验证数据是否插入成功：

```sql
-- 查看班组信息
SELECT * FROM tb_team_info WHERE org_code IN ('A02A02A01', 'A02A02A01A02');

-- 查看班组维度关联
SELECT * FROM tb_team_dimension_relation WHERE dimension_code IN ('A02A02A01', 'A02A02A01A02');
```

### 步骤4: 重启后端并测试

1. 重启后端服务
2. 刷新前端页面
3. 查看班组下拉框是否显示班组列表
4. 查看能耗统计数据是否正常显示

## 数据表关系说明

```
tb_team_info (班组基础信息)
    ├── team_code (班组编码，唯一)
    ├── team_name (班组名称)
    ├── org_code (所属组织编码)
    └── status (状态: 1-启用, 0-停用)

tb_team_dimension_relation (班组维度关联)
    ├── team_code (关联 tb_team_info.team_code)
    ├── dimension_code (维度编码，对应左侧树的编码)
    ├── dimension_type (维度类型: 1-按部门用电)
    ├── module_ids (仪表ID列表，逗号分隔)
    └── status (状态: 1-启用, 0-停用)

tb_ep_equ_energy_daycount (能耗日统计)
    ├── module_id (仪表ID)
    ├── dt (统计日期)
    └── energy_count (能耗值)
```

## 数据流程

1. 用户在前端选择左侧维度树节点（例如: A02A02A01）
2. 前端调用 `getTeamListByDimension(dimensionCode, dimensionType)` 获取班组列表
3. 后端查询 `tb_team_dimension_relation` 表，找到该维度下的所有班组
4. 后端再查询 `tb_team_info` 表，获取班组详细信息
5. 前端显示班组下拉列表（包含"全部班组"和各个班组）
6. 用户选择班组后，后端根据 `module_ids` 查询能耗数据

## 注意事项

1. **module_ids 必须是真实存在且有数据的仪表ID**，否则查询不到能耗数据
2. **dimension_code 必须与左侧维度树的编码一致**
3. 每个班组可以关联多个仪表，用逗号分隔
4. 如果多个班组共用同一个仪表，可以在多个班组中都配置该仪表ID

## 快速测试数据

如果只是想快速测试功能，可以使用以下简化脚本：

```sql
-- 插入一个测试班组
INSERT INTO tb_team_info (id, team_code, team_name, org_code, status, sort_order, create_time)
VALUES ('test_001', 'TEST_TEAM', '测试班组', 'A02A02A01', 1, 1, NOW());

-- 插入班组维度关联（使用任意有数据的仪表ID）
INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, module_ids, status, create_time)
SELECT 'test_rel_001', 'TEST_TEAM', 'A02A02A01', 1, GROUP_CONCAT(DISTINCT module_id), 1, NOW()
FROM (
    SELECT module_id FROM tb_ep_equ_energy_daycount
    WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    LIMIT 5
) t;

-- 或者手动指定仪表ID（推荐）
-- 先查询有数据的仪表
SELECT DISTINCT module_id, COUNT(*) as count
FROM tb_ep_equ_energy_daycount
WHERE dt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY module_id
ORDER BY count DESC
LIMIT 10;

-- 然后手动插入（替换下面的仪表ID）
INSERT INTO tb_team_dimension_relation (id, team_code, dimension_code, dimension_type, module_ids, status, create_time)
VALUES ('test_rel_001', 'TEST_TEAM', 'A02A02A01', 1, '你的仪表ID1,你的仪表ID2,你的仪表ID3', 1, NOW());
```

**注意**: tb_module 表中表示启用状态的字段是 `isaction = 'Y'`，不是 `status`

## 日志查看

修复后，可以查看日志确认数据查询情况：

```bash
# 查看班组能源SQL日志
tail -f E:/workspace/EMSProject_jeecg/JeecgBoot/jeecg-boot/jeecg-module-system/logs/team-energy-sql.log
```

日志会显示：
- 查询到的班组维度关联记录数
- 解析后的仪表ID列表
- 查询到的能耗统计记录数
- 每条记录的详细信息
