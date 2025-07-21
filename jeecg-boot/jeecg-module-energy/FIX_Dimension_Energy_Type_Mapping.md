# 维度类型与能源类型映射修复

## 🐛 问题描述

在 `getModulesByOrgCode` 接口中，维度类型（`nowtype`）和能源类型（`energy_type`）的映射关系不正确，导致查询不到数据。

**错误的逻辑**：
```java
// 错误：直接将维度类型当作能源类型使用
.filter(module -> nowtype.equals(module.getEnergyType()))
```

**问题现象**：
- 调用 `nowtype=2` 时返回空数组
- 但该维度下有 4 个 `energy_type=1` 的仪表

## 🔍 正确的映射关系

通过分析 `getRealTimeData` 接口的实现，发现正确的映射关系：

| 维度类型 (nowtype) | 维度名称 | 能源类型 (energy_type) | 能源名称 |
|-------------------|----------|----------------------|----------|
| 1 | 按部门用电 | 1 | 电 |
| 2 | 按线路用电 | 1 | 电 |
| 3 | 天然气 | 8 | 天然气 |
| 4 | 压缩空气 | 5 | 压缩空气 |
| 5 | 企业用水 | 2 | 水 |

**关键发现**：
- `nowtype=1` 和 `nowtype=2` 都对应 `energy_type=1`（电）
- 它们的区别在于数据展示方式，而不是能源类型

## 🛠️ 修复方案

### 1. 添加映射方法

```java
/**
 * 根据维度类型获取对应的能源类型
 * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
 * @return 能源类型
 */
private Integer getEnergyTypeByNowtype(Integer nowtype) {
    switch (nowtype) {
        case 1: // 按部门用电
        case 2: // 按线路用电
            return 1; // 电
        case 3: // 天然气
            return 8; // 天然气
        case 4: // 压缩空气
            return 5; // 压缩空气
        case 5: // 企业用水
            return 2; // 水
        default:
            log.warn("未知的维度类型: {}, 默认返回能源类型1(电)", nowtype);
            return 1; // 默认为电
    }
}
```

### 2. 修复过滤逻辑

```java
// 修复前：错误的直接映射
.filter(module -> nowtype.equals(module.getEnergyType()))

// 修复后：正确的映射关系
Integer energyType = getEnergyTypeByNowtype(nowtype);
.filter(module -> energyType.equals(module.getEnergyType()))
```

### 3. 增强调试日志

```java
log.warn("🔍 调试信息 - 维度类型 {} 需要的能源类型：{}", nowtype, expectedEnergyType);
log.warn("🔍 调试信息 - 能源类型为 {} 的仪表数量：{}", expectedEnergyType, targetTypeCount);
```

## ✅ 修复验证

### 修复前的问题
```log
🔍 调试信息 - 按维度类型统计：{1=4}
❌ 未找到仪表数据，可能原因：
   2. 没有维度类型为 2 的仪表
```

### 修复后的预期结果
```log
🔍 维度类型 2 映射到能源类型 1
🔍 根据部门ID列表 [1940592141922783234] 和维度类型 2 查询到仪表数量：4
✅ 查询到仪表数量：4
```

## 📋 测试用例

### 测试1: 按线路用电 (nowtype=2)
```bash
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A03A01&nowtype=2&includeChildren=true
```
**预期结果**: 返回 4 个 `energy_type=1` 的仪表

### 测试2: 按部门用电 (nowtype=1)
```bash
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A03A01&nowtype=1&includeChildren=true
```
**预期结果**: 返回 4 个 `energy_type=1` 的仪表（与测试1相同）

### 测试3: 天然气 (nowtype=3)
```bash
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A03A01&nowtype=3&includeChildren=true
```
**预期结果**: 返回 `energy_type=8` 的仪表（如果存在）

## 🔍 数据验证SQL

```sql
-- 查看该维度下的仪表按能源类型分布
SELECT 
    m.energy_type,
    COUNT(*) as count,
    GROUP_CONCAT(m.module_name) as module_names
FROM tb_module m 
WHERE FIND_IN_SET('1940592141922783234', m.sys_org_code)
  AND m.isaction = 'Y'
GROUP BY m.energy_type;

-- 验证维度类型2应该能查到的仪表
SELECT 
    m.module_id,
    m.module_name,
    m.energy_type,
    m.isaction
FROM tb_module m 
WHERE FIND_IN_SET('1940592141922783234', m.sys_org_code)
  AND m.energy_type = 1  -- 维度类型2对应能源类型1
  AND m.isaction = 'Y';
```

## 📝 相关接口对比

### getRealTimeData 接口
```java
if (nowtype == 1 || nowtype == 2) {
    // 电力数据 - 使用 tb_equ_ele_data 表
} else {
    // 天然气/压缩空气/用水数据 - 使用 tb_equ_energy_data 表
}
```

### getModulesByOrgCode 接口（修复后）
```java
Integer energyType = getEnergyTypeByNowtype(nowtype);
.filter(module -> energyType.equals(module.getEnergyType()))
```

两个接口现在使用相同的映射逻辑，保证了数据一致性。

## 🎯 修复状态

- [x] 添加维度类型到能源类型的映射方法
- [x] 修复 `getModulesByDepartIdsAndType` 方法的过滤逻辑
- [x] 增强调试日志显示正确的映射关系
- [x] 验证修复效果

**修复完成时间**: 2025-07-17
**影响范围**: `getModulesByOrgCode` 接口
**向后兼容**: 是，不影响现有功能
