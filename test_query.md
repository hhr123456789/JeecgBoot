# 数据查询问题诊断结果

## 问题原因

**数据库中有数据，但查询的部门编码不匹配！**

### 数据库中的部门编码格式
```
A01          - 生产部门
A01-01       - 一号车间
A01-02       - 二号车间
A01-03       - 三号车间
A02          - 辅助部门
```

### 前端查询的部门编码格式
```
A02A06A01    - 1#水表
A02A03A01    - 10KV高压天水线
A02A04A01    - 天然气1#
A02A05A01    - 压缩空气1#
A02A02A01    - 挤压车间
```

**这两种格式不匹配，导致查询不到数据！**

## 解决方案

### 方案1：修改数据同步任务（推荐）

修改 `EnergyClassificationSyncJob` 或相关的数据同步服务，使其：
1. 从 `sys_depart` 表读取实际的 `org_code`
2. 使用这些 `org_code` 来生成统计数据
3. 确保统计表中的 `org_code` 与 `sys_depart` 表一致

### 方案2：测试已有数据（快速验证）

使用 Postman 或浏览器直接测试已有数据：

```bash
POST http://localhost:8080/jeecg-boot/energy/classification/getSummaryData
Content-Type: application/json

{
  "orgCode": "A01-01",
  "energyType": "1",
  "timeDimension": "month",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "includeChildren": true
}
```

应该能看到数据：
- 总能耗: 152345.67 kWh
- 电能消耗: 152345.67 kWh
- 总费用: 121876.54 元

### 方案3：修改维度树查询逻辑

如果 `sys_depart` 表中的 `org_code` 确实是 `A02A06A01` 这种格式，那么需要：
1. 检查数据同步任务为什么使用了不同的 `org_code` 格式
2. 重新运行数据同步任务，使用正确的 `org_code`

## 验证步骤

### 1. 检查 sys_depart 表中的实际数据

```sql
SELECT id, org_code, depart_name, parent_id
FROM sys_depart
WHERE del_flag = '0' AND status = '1'
ORDER BY org_code
LIMIT 20;
```

### 2. 检查统计表中的 org_code

```sql
SELECT DISTINCT org_code, org_name
FROM tb_energy_classification_summary
ORDER BY org_code;
```

### 3. 对比两个表的 org_code 格式

如果格式不一致，需要：
- 要么修改数据同步任务使用正确的 org_code
- 要么重新生成统计数据

## 临时解决方案

如果您想快速看到效果，可以手动插入一条测试数据：

```sql
INSERT INTO `tb_energy_classification_summary` VALUES
('TEST_A02A02A01_11_E', 'A02A02A01', '挤压车间', 'A02', '1', '电能',
'2025-11-01', '2025-11', '2025', 'month',
'152345.67', '121876.54', '151.89', '18.69',
'40000.00', '32000.00', '65000.00', '52000.00', '47345.67', '37876.54',
'5', NOW(), NOW());
```

然后刷新页面，选择"挤压车间"，应该就能看到数据了。

## 下一步操作

1. **立即执行**：检查 `sys_depart` 表和统计表的 `org_code` 格式
2. **确认格式**：确定哪个是正确的格式
3. **修复数据**：
   - 如果统计表格式错误，重新运行数据同步
   - 如果维度树格式错误，修改维度树查询逻辑
