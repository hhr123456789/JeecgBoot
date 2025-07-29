# 导出Excel问题调试指南

## 🐛 问题描述

用户反馈：
- **仪表**: 4个仪表（1-4号注塑机）
- **参数**: A相电流（参数编号1）
- **时间范围**: 2025-07-23 00:00:00 至 2025-07-23 21:51:21
- **查询间隔**: 1（15分钟）
- **显示模式**: 1（统一显示）

**问题现象**:
1. 导出结果只到13:30就结束了（应该到21:51）
2. 所有数据都是空值（显示为"-"）

## 🔍 排查步骤

### 1. 确认仪表ID

首先需要确认1-4号注塑机的实际仪表ID：

```sql
-- 查询注塑机的仪表ID
SELECT module_id, module_name, energy_type, isaction, sys_org_code
FROM tb_module 
WHERE module_name LIKE '%注塑机%' 
AND isaction = 'Y'
ORDER BY module_name;
```

**预期结果**：
```
module_id       | module_name | energy_type | isaction | sys_org_code
yj0001_1202     | 1号注塑机   | 1           | Y        | xxx
yj0001_1203     | 2号注塑机   | 1           | Y        | xxx
yj0001_1204     | 3号注塑机   | 1           | Y        | xxx
yj0001_1205     | 4号注塑机   | 1           | Y        | xxx
```

### 2. 检查InfluxDB数据

确认InfluxDB中是否有对应的数据：

```sql
-- 连接InfluxDB
USE energy_data_202507;

-- 查询A相电流数据（IA字段）
SELECT * FROM hist 
WHERE tagname =~ /^YJ0001_120[2-5]#IA$/ 
AND time >= '2025-07-23T00:00:00Z' 
AND time <= '2025-07-23T13:51:21Z' 
ORDER BY time DESC 
LIMIT 20;

-- 检查数据时间范围
SELECT tagname, MIN(time) as min_time, MAX(time) as max_time, COUNT(*) as count
FROM hist 
WHERE tagname =~ /^YJ0001_120[2-5]#IA$/ 
AND time >= '2025-07-23T00:00:00Z' 
AND time <= '2025-07-23T21:51:21Z' 
GROUP BY tagname;
```

### 3. 检查查询接口

使用相同参数调用查询接口，看是否能正常返回数据：

```bash
curl -X POST "http://localhost:8080/energy/monitor/getRealTimeMonitorData" \
-H "Content-Type: application/json" \
-d '{
    "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204", "yj0001_1205"],
    "parameters": [1],
    "startTime": "2025-07-23 00:00:00",
    "endTime": "2025-07-23 21:51:21",
    "interval": 1,
    "displayMode": 1
}'
```

### 4. 检查导出接口日志

查看后端日志中的关键信息：

```bash
# 查看导出相关日志
tail -f logs/jeecg-boot.log | grep -E "(📊|🔍|❌)"
```

**关键日志点**：
1. `📊 InfluxDB查询结果数量：X` - 是否查询到数据
2. `📊 查询结果示例` - 查询到的数据格式
3. `📊 从查询结果中提取到的tagname列表` - tagname是否正确
4. `📊 表头构建完成` - 表头是否正确构建
5. `📊 数据按时间分组完成` - 数据分组是否成功

## 🔧 可能的问题和解决方案

### 问题1：仪表ID不匹配

**症状**：查询结果为空
**原因**：前端传递的仪表ID与数据库中的不一致
**解决**：确认正确的仪表ID格式

### 问题2：tagname格式错误

**症状**：查询有结果但导出为空
**原因**：导出功能中的tagname构建与查询不一致
**解决**：统一tagname构建逻辑

### 问题3：时区转换问题

**症状**：时间范围不正确
**原因**：UTC时间转换有误
**解决**：检查时区转换逻辑

### 问题4：数据分组逻辑错误

**症状**：有数据但Excel中显示为空
**原因**：时间分组或列映射有问题
**解决**：修复数据处理逻辑

## 🚀 立即行动

1. **执行SQL查询**：确认仪表ID和InfluxDB数据
2. **查看后端日志**：分析导出过程中的问题
3. **对比查询接口**：确认数据源是否正常
4. **修复代码**：根据发现的问题进行修复

## 📝 调试检查清单

- [ ] 确认1-4号注塑机的实际仪表ID
- [ ] 检查InfluxDB中是否有对应时间范围的数据
- [ ] 验证查询接口是否正常返回数据
- [ ] 查看导出接口的详细日志
- [ ] 对比查询和导出的tagname构建逻辑
- [ ] 检查时间转换和数据分组逻辑
- [ ] 测试修复后的导出功能
