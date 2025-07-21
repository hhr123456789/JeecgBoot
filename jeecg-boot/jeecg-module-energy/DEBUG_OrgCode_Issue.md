# 维度编码数据问题调试指南

## 🐛 问题描述

前端调用 `/energy/monitor/getModulesByOrgCode` 接口时：
- 参数：`orgCodes=A02A03A01`, `nowtype=2`
- 返回：空数组 `[]`
- 错误：维度类型 2 下的维度编码 A02A03A01 没有找到仪表数据

## 🔍 调试步骤

### 1. 使用调试接口排查问题

我已经创建了一个专门的调试接口来帮助排查这个问题：

**接口地址**: `GET /energy/monitor/debugOrgCodeInfo`

**参数**:
- `orgCode`: 维度编码 (如: A02A03A01)
- `nowtype`: 维度类型 (如: 2)

**调用示例**:
```bash
GET http://localhost:8080/jeecg-boot/energy/monitor/debugOrgCodeInfo?orgCode=A02A03A01&nowtype=2
```

### 2. 调试接口返回信息说明

调试接口会返回详细的排查信息：

```json
{
  "success": true,
  "result": {
    "orgCode": "A02A03A01",
    "nowtype": 2,
    "step1_depart_check": {
      "found": true/false,
      "count": 1,
      "departs": [
        {
          "id": "部门ID",
          "departName": "部门名称",
          "orgCode": "A02A03A01",
          "parentId": "父部门ID"
        }
      ]
    },
    "step2_depart_ids": ["部门ID列表"],
    "step3_all_modules": {
      "total_count": 5,
      "modules": [
        {
          "moduleId": "仪表ID",
          "moduleName": "仪表名称",
          "energyType": 1,
          "isaction": "Y",
          "sysOrgCode": "部门ID"
        }
      ]
    },
    "step4_energy_type_stats": {
      "1": 3,
      "2": 0,
      "3": 2
    },
    "step5_filtered_modules": {
      "target_energy_type": 2,
      "filtered_count": 0,
      "modules": []
    },
    "step6_active_modules": {
      "active_count": 0,
      "modules": []
    },
    "conclusion": "具体的问题结论"
  }
}
```

### 3. 根据调试结果分析问题

#### 情况1: 维度编码不存在
如果 `step1_depart_check.found = false`：
- **问题**: 维度编码 `A02A03A01` 在 `sys_depart` 表中不存在
- **解决方案**: 检查维度编码是否正确，或在系统中添加该维度

#### 情况2: 该维度下没有对应类型的仪表
如果 `step5_filtered_modules.filtered_count = 0`：
- **问题**: 该维度下没有 `energy_type = 2` 的仪表
- **解决方案**: 
  1. 检查 `step4_energy_type_stats` 查看可用的维度类型
  2. 在该维度下添加对应类型的仪表
  3. 或者修改仪表的 `energy_type` 字段

#### 情况3: 仪表都处于禁用状态
如果 `step5_filtered_modules.filtered_count > 0` 但 `step6_active_modules.active_count = 0`：
- **问题**: 有对应类型的仪表，但都处于禁用状态 (`isaction != 'Y'`)
- **解决方案**: 将仪表的 `isaction` 字段设置为 'Y'

## 🛠️ 数据修复建议

### 1. 检查维度数据
```sql
-- 查看维度编码是否存在
SELECT * FROM sys_depart WHERE org_code = 'A02A03A01';

-- 查看该维度的层级结构
SELECT * FROM sys_depart WHERE org_code LIKE 'A02A03A01%';
```

### 2. 检查仪表数据
```sql
-- 查看该维度下的所有仪表
SELECT m.*, d.depart_name 
FROM tb_module m 
LEFT JOIN sys_depart d ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE d.org_code = 'A02A03A01';

-- 按维度类型统计仪表数量
SELECT energy_type, COUNT(*) as count
FROM tb_module m 
LEFT JOIN sys_depart d ON FIND_IN_SET(d.id, m.sys_org_code)
WHERE d.org_code = 'A02A03A01'
GROUP BY energy_type;
```

### 3. 数据修复SQL示例
```sql
-- 如果需要添加维度类型为2的仪表，可以修改现有仪表的类型
UPDATE tb_module 
SET energy_type = 2 
WHERE module_id IN ('需要修改的仪表ID列表');

-- 如果需要启用仪表
UPDATE tb_module 
SET isaction = 'Y' 
WHERE module_id IN ('需要启用的仪表ID列表');
```

## 📋 维度类型说明

根据系统设计，维度类型 `nowtype` 的含义：
- `1`: 按部门用电
- `2`: 按线路用电  ← 当前查询的类型
- `3`: 天然气
- `4`: 压缩空气
- `5`: 企业用水

## 🔧 使用调试接口的步骤

1. **调用调试接口**:
   ```bash
   GET /energy/monitor/debugOrgCodeInfo?orgCode=A02A03A01&nowtype=2
   ```

2. **分析返回结果**:
   - 查看 `conclusion` 字段了解问题原因
   - 查看各个步骤的详细信息

3. **根据结果修复数据**:
   - 如果是维度不存在，添加维度数据
   - 如果是仪表类型不匹配，修改仪表的 `energy_type`
   - 如果是仪表禁用，修改仪表的 `isaction` 状态

4. **重新测试**:
   - 修复数据后，重新调用原接口验证

## 📞 联系支持

如果通过调试接口仍无法解决问题，请提供：
1. 调试接口的完整返回结果
2. 相关的数据库表结构和数据
3. 具体的业务需求说明

这样可以更快速地定位和解决问题。
