# 数据同步操作指南

## 问题说明

数据同步逻辑是**正确的**！SQL查询使用的是 `m.sys_org_code`，会自动从 `tb_module` 表读取正确的部门编码。

数据库中那些 `A01`, `A01-01` 格式的数据是**手动插入的测试数据**，格式不正确。

## 解决方案

重新运行数据同步任务，它会自动生成正确格式的数据。

### 方法1：通过后端接口触发（推荐）

使用 Postman 或 curl 调用同步接口：

```bash
# 同步2025年11月的数据
POST http://localhost:8080/jeecg-boot/energy/classification/syncByMonth
Content-Type: application/json

{
  "year": 2025,
  "month": 11
}
```

或者同步指定日期范围：

```bash
POST http://localhost:8080/jeecg-boot/energy/classification/triggerDataSync
Content-Type: application/json

{
  "startDate": "2025-11-01",
  "endDate": "2025-11-30"
}
```

### 方法2：通过定时任务触发

如果配置了定时任务，等待定时任务自动执行即可。

### 方法3：清理旧数据后重新同步

如果想清理那些手动插入的测试数据：

```sql
-- 1. 备份现有数据（可选）
CREATE TABLE tb_energy_classification_summary_backup AS
SELECT * FROM tb_energy_classification_summary;

-- 2. 删除2025年11月的旧数据
DELETE FROM tb_energy_classification_summary
WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30';

-- 3. 然后通过接口重新同步
```

## 验证步骤

### 1. 检查 tb_module 表中的 sys_org_code

```sql
SELECT
    module_id,
    module_name,
    sys_org_code,
    energy_type,
    isaction
FROM tb_module
WHERE isaction = 'Y'
LIMIT 20;
```

应该能看到类似 `A02A06A01`, `A02A03A01` 这样的 `sys_org_code`。

### 2. 检查 tb_ep_equ_energy_daycount 表是否有数据

```sql
SELECT COUNT(*) as total_records
FROM tb_ep_equ_energy_daycount
WHERE dt >= '2025-11-01' AND dt <= '2025-11-30';
```

如果返回 0，说明没有实时数据可以同步。

### 3. 测试SQL查询

```sql
-- 这是数据同步任务实际执行的SQL
SELECT
    m.sys_org_code as org_code,
    m.energy_type,
    DATE(d.dt) as stat_date,
    DATE_FORMAT(d.dt, '%Y-%m') as stat_month,
    DATE_FORMAT(d.dt, '%Y') as stat_year,
    COALESCE(SUM(d.energy_count), 0) as total_consumption,
    COALESCE(SUM(d.peak_count), 0) as peak_consumption,
    COALESCE(SUM(d.level_count), 0) as flat_consumption,
    COALESCE(SUM(d.valley_count), 0) as valley_consumption,
    COALESCE(SUM(d.cusp_count), 0) as cusp_consumption,
    COUNT(DISTINCT d.module_id) as meter_count
FROM tb_ep_equ_energy_daycount d
INNER JOIN tb_module m ON d.module_id = m.module_id
WHERE m.isaction = 'Y'
AND d.dt >= '2025-11-01'
AND d.dt <= '2025-11-30'
GROUP BY m.sys_org_code, m.energy_type, DATE(d.dt)
ORDER BY m.sys_org_code, m.energy_type, DATE(d.dt)
LIMIT 10;
```

这个查询应该返回使用正确 `org_code` 格式的数据。

## 常见问题

### Q1: 同步后还是没有数据？

**原因：** `tb_ep_equ_energy_daycount` 表中没有实时数据。

**解决：**
1. 检查实时数据采集是否正常运行
2. 检查设备是否在线并上报数据
3. 检查 `tb_module` 表中设备的 `isaction` 字段是否为 'Y'

### Q2: 同步接口返回 "NO_DATA"？

**原因：** 查询条件不匹配，没有找到可同步的数据。

**解决：**
1. 检查 `tb_module.isaction` 是否为 'Y'
2. 检查 `tb_module.sys_org_code` 是否为 NULL
3. 检查 `tb_module.energy_type` 是否为 NULL
4. 检查实时表和仪表表是否能关联

### Q3: 如何确认同步成功？

执行以下SQL：

```sql
-- 查看同步后的数据
SELECT
    org_code,
    energy_type,
    stat_date,
    total_consumption,
    total_cost
FROM tb_energy_classification_summary
WHERE stat_date >= '2025-11-01' AND stat_date <= '2025-11-30'
ORDER BY stat_date DESC, org_code
LIMIT 20;
```

应该能看到 `org_code` 为 `A02A06A01`, `A02A03A01` 等格式的数据。

## 使用 Postman 测试

### 1. 创建请求

- Method: POST
- URL: `http://localhost:8080/jeecg-boot/energy/classification/syncByMonth`
- Headers:
  - Content-Type: application/json
  - X-Access-Token: (您的登录token)
- Body (raw JSON):
```json
{
  "year": 2025,
  "month": 11
}
```

### 2. 查看响应

成功响应示例：
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "startTime": "2025-12-06T15:00:00",
    "endTime": "2025-12-06T15:00:05",
    "successCount": 150,
    "failCount": 0,
    "totalRecords": 150,
    "status": "SUCCESS",
    "year": 2025,
    "month": 11,
    "type": "MONTH"
  }
}
```

失败响应示例：
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "successCount": 0,
    "failCount": 0,
    "totalRecords": 0,
    "status": "NO_DATA",
    "message": "未查询到待同步的数据"
  }
}
```

## 总结

1. **数据同步逻辑是正确的** - 无需修改代码
2. **只需重新运行同步任务** - 会自动使用正确的 `org_code`
3. **删除旧的测试数据** - 那些 `A01`, `A01-01` 格式的数据是错误的
4. **确保实时表有数据** - 没有实时数据就无法同步

执行同步后，刷新前端页面，选择维度树中的部门，应该就能看到数据了！
