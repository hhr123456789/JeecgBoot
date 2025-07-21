# InfluxDB 查询调试指南

## 🐛 问题分析

### 问题1: InfluxDB IN 操作符错误 (已修复)
**错误**: `error parsing query: found IN, expected ; at line 1, char 123`
**原因**: InfluxDB 1.8不支持 `IN` 操作符
**修复**: 使用 `OR` 条件替代

### 问题2: tagname 为 null 导致分组错误 (已修复)
**错误**: `element cannot be mapped to a null key`
**原因**: InfluxDB 查询结果中 `tagname` 字段为 null，导致 `Collectors.groupingBy()` 失败
**修复**: 
1. 修改 `InfluxDBUtil.parseQueryResult()` 正确解析 tag 信息
2. 在数据分组前过滤掉 `tagname` 为 null 的记录

## 🔧 修复详情

### 1. InfluxDB 查询语句修复

**修复前**:
```sql
SELECT mean(value) as value 
FROM measurement 
WHERE time >= '2025-07-15T00:00:00Z' AND time <= '2025-07-15T08:00:00Z' 
AND tagname IN ('YJ0001_1202#IA', 'YJ0001_1202#UA') 
AND status = 1 
GROUP BY time(15m), tagname 
ORDER BY time ASC
```

**修复后**:
```sql
SELECT mean(value) as value 
FROM measurement 
WHERE time >= '2025-07-15T00:00:00Z' AND time <= '2025-07-15T08:00:00Z' 
AND (tagname = 'YJ0001_1202#IA' OR tagname = 'YJ0001_1202#UA') 
AND status = 1 
GROUP BY time(15m), tagname 
ORDER BY time ASC
```

### 2. InfluxDB 结果解析修复

**修复前**:
```java
// 只解析 columns 和 values，忽略了 tags
for (List<Object> valueRow : values) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < columns.size(); i++) {
        map.put(columns.get(i), valueRow.get(i));
    }
    resultList.add(map);
}
```

**修复后**:
```java
// 同时解析 columns、values 和 tags
for (List<Object> valueRow : values) {
    Map<String, Object> map = new HashMap<>();
    
    // 添加列数据
    for (int i = 0; i < columns.size(); i++) {
        if (i < valueRow.size()) {
            map.put(columns.get(i), valueRow.get(i));
        }
    }
    
    // 添加tag数据 (包括tagname)
    if (tags != null) {
        map.putAll(tags);
    }
    
    resultList.add(map);
}
```

### 3. 数据分组空值检查

**修复前**:
```java
// 直接分组，可能遇到 null key
Map<String, List<Map<String, Object>>> groupedData = influxResults.stream()
    .collect(Collectors.groupingBy(r -> (String) r.get("tagname")));
```

**修复后**:
```java
// 先过滤掉 tagname 为 null 的记录
Map<String, List<Map<String, Object>>> groupedData = influxResults.stream()
    .filter(r -> r.get("tagname") != null)
    .collect(Collectors.groupingBy(r -> (String) r.get("tagname")));
```

## 🧪 测试验证

### 测试用例
```json
{
    "moduleIds": ["yj0001_1202"],
    "parameters": [1, 4, 7],
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00",
    "interval": 1,
    "displayMode": 1
}
```

### 预期结果
1. InfluxDB 查询语句正确生成（使用 OR 条件）
2. 查询结果正确解析（包含 tagname 信息）
3. 数据分组成功（无 null key 错误）
4. 返回格式化的监控数据

## 📋 调试检查清单

如果仍有问题，请按以下步骤检查：

### 1. InfluxDB 连接检查
- [ ] InfluxDB 服务是否正常运行
- [ ] 数据库连接配置是否正确
- [ ] 当前月份的数据库是否存在

### 2. 数据存在性检查
- [ ] InfluxDB 中是否有对应的 measurement
- [ ] 是否有对应的 tagname 数据
- [ ] 时间范围内是否有数据

### 3. 配置检查
- [ ] `ParameterConfig` 中参数映射是否正确
- [ ] `IntervalConfig` 中时间间隔配置是否正确
- [ ] MySQL 中仪表信息是否存在

### 4. 日志检查
查看详细的查询语句和结果：
```bash
# 查看 InfluxDB 查询语句
tail -f logs/jeecg-boot.log | grep "构建的InfluxDB查询语句"

# 查看查询结果
tail -f logs/jeecg-boot.log | grep "InfluxDB查询完成"

# 查看错误信息
tail -f logs/jeecg-boot.log | grep -i error
```

## 🔍 InfluxDB 手动验证

可以直接在 InfluxDB 中执行查询来验证：

```bash
# 连接到 InfluxDB
influx -host localhost -port 8086

# 切换到对应数据库
USE your_database_name

# 执行查询
SELECT mean(value) as value 
FROM your_measurement 
WHERE time >= '2025-07-15T00:00:00Z' AND time <= '2025-07-15T08:00:00Z' 
AND (tagname = 'YJ0001_1202#IA' OR tagname = 'YJ0001_1202#UA') 
AND status = 1 
GROUP BY time(15m), tagname 
ORDER BY time ASC

# 检查 tagname 数据
SHOW TAG VALUES FROM your_measurement WITH KEY = "tagname"
```

## 📝 修复状态

- [x] InfluxDB IN 操作符错误 - 已修复 (2025-07-17)
- [x] tagname null 值分组错误 - 已修复 (2025-07-17)
- [x] InfluxDB 结果解析 tag 信息 - 已修复 (2025-07-17)

修复完成后，接口应该可以正常工作。如果还有其他问题，请提供具体的错误信息。
