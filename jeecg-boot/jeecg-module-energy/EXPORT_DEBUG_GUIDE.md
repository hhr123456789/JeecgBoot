# 实时数据导出Excel调试指南

## 🐛 当前问题

用户反馈导出Excel时遇到以下问题：
1. **数据只到13:30就结束了**（应该到21:51）
2. **所有数据都是空值（显示为"-"）**

## 🔍 问题排查步骤

### 1. 检查InfluxDB查询结果

在导出功能中已添加调试日志，查看后端日志：

```bash
# 查看导出相关日志
tail -f logs/jeecg-boot.log | grep "📊"
```

关键日志信息：
- `📊 InfluxDB查询结果数量：X` - 查看是否有查询到数据
- `📊 查询结果示例` - 查看查询到的数据格式
- `📊 从查询结果中提取到的tagname列表` - 查看tagname是否正确
- `📊 添加列` - 查看表头构建是否正确

### 2. 验证查询参数

确认以下参数：
- **仪表ID**: 1-4号注塑机的实际ID是什么？
- **参数编号**: A相电流对应的参数编号是1
- **时间范围**: 2025-07-23 00:00:00 至 2025-07-23 21:51:21
- **查询间隔**: 1（15分钟）

### 3. 检查仪表ID映射

```sql
-- 查询注塑机的实际仪表ID
SELECT module_id, module_name, energy_type, isaction 
FROM tb_module 
WHERE module_name LIKE '%注塑机%' 
AND isaction = 'Y';
```

### 4. 检查InfluxDB数据

```sql
-- 在InfluxDB中直接查询数据
SHOW DATABASES;
USE energy_data_202507;
SHOW MEASUREMENTS;

-- 查询特定仪表的数据
SELECT * FROM energy_data 
WHERE tagname =~ /^YJ0001_1202#.*/ 
AND time >= '2025-07-23T00:00:00Z' 
AND time <= '2025-07-23T13:51:21Z' 
LIMIT 10;
```

### 5. 对比查询接口和导出接口

使用相同参数分别调用：
1. `/energy/monitor/getRealTimeMonitorData` - 查询接口
2. `/energy/monitor/exportRealTimeData` - 导出接口

查看两个接口的查询结果是否一致。

## 🔧 可能的修复方案

### 方案1：仪表ID不匹配
如果仪表ID不正确，需要：
1. 确认正确的仪表ID
2. 更新前端传递的参数

### 方案2：tagname格式问题
如果tagname格式不匹配，需要：
1. 检查InfluxDB中实际的tagname格式
2. 调整tagname构建逻辑

### 方案3：时间范围问题
如果时间范围有问题，需要：
1. 检查时区转换是否正确
2. 确认InfluxDB中的数据时间范围

### 方案4：数据库连接问题
如果数据库连接有问题，需要：
1. 检查InfluxDB连接配置
2. 确认对应月份的数据库是否存在

## 📝 测试用例

### 测试请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204", "yj0001_1205"],
    "parameters": [1],
    "startTime": "2025-07-23 00:00:00",
    "endTime": "2025-07-23 21:51:21",
    "interval": 1,
    "displayMode": 1,
    "fileName": "注塑机A相电流数据导出"
}
```

### 预期结果
- Excel文件包含5列：时间 + 4个仪表的A相电流数据
- 数据时间范围：00:00 到 21:45（15分钟间隔）
- 每个时间点都应该有对应的电流值

## 🚀 下一步行动

1. **立即执行**：查看后端日志，确认查询结果
2. **数据验证**：确认仪表ID和InfluxDB中的数据
3. **对比测试**：使用查询接口验证数据是否存在
4. **修复问题**：根据日志信息定位并修复问题
