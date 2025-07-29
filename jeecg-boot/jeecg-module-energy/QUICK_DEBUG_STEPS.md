# 🚀 快速调试导出Excel问题

## 立即执行的调试步骤

### 1. 使用调试接口检查仪表数据

我已经添加了一个调试接口，请立即调用：

```bash
# 调用调试接口（请根据实际的仪表ID调整）
GET http://localhost:8080/energy/monitor/debugModuleData?moduleIds=yj0001_1202,yj0001_1203,yj0001_1204,yj0001_1205
```

**或者使用curl**：
```bash
curl "http://localhost:8080/energy/monitor/debugModuleData?moduleIds=yj0001_1202,yj0001_1203,yj0001_1204,yj0001_1205"
```

### 2. 分析调试结果

调试接口会返回以下关键信息：

```json
{
    "success": true,
    "result": {
        "moduleNameMap": {
            "yj0001_1202": "1号注塑机",
            "yj0001_1203": "2号注塑机"
        },
        "foundModules": 2,
        "requestedModules": 4,
        "influxDataCount": 120,
        "tagnameDataCount": {
            "YJ0001_1202#IA": 60,
            "YJ0001_1203#IA": 60
        },
        "expectedTagnames": [
            "YJ0001_1202#IA",
            "YJ0001_1203#IA",
            "YJ0001_1204#IA",
            "YJ0001_1205#IA"
        ],
        "sampleData": [...]
    }
}
```

### 3. 根据调试结果判断问题

#### 情况A：foundModules < requestedModules
**问题**：仪表ID不正确
**解决**：使用正确的仪表ID

#### 情况B：influxDataCount = 0
**问题**：InfluxDB中没有数据
**解决**：检查数据同步或时间范围

#### 情况C：tagnameDataCount为空或不匹配
**问题**：tagname格式不正确
**解决**：检查tagname构建逻辑

### 4. 查看后端日志

同时查看后端日志中的详细信息：

```bash
tail -f logs/jeecg-boot.log | grep -E "(📊|🔍|❌)"
```

### 5. 确认正确的仪表ID

如果调试接口显示仪表ID不正确，请执行以下SQL查询：

```sql
-- 查询所有注塑机的仪表ID
SELECT module_id, module_name, energy_type, isaction, sys_org_code
FROM tb_module 
WHERE module_name LIKE '%注塑机%' 
AND isaction = 'Y'
ORDER BY module_name;
```

### 6. 测试修复后的导出功能

使用正确的仪表ID重新测试导出：

```json
{
    "moduleIds": ["正确的仪表ID1", "正确的仪表ID2", "正确的仪表ID3", "正确的仪表ID4"],
    "parameters": [1],
    "startTime": "2025-07-23 00:00:00",
    "endTime": "2025-07-23 21:51:21",
    "interval": 1,
    "displayMode": 1,
    "fileName": "注塑机A相电流数据导出"
}
```

## 🔧 已修复的问题

1. **增强了调试日志**：导出过程中会显示详细的调试信息
2. **添加了调试接口**：可以快速检查仪表ID和数据状态
3. **统一了tagname构建**：确保导出和查询使用相同的逻辑
4. **改进了错误处理**：提供更详细的错误信息

## 📞 如果问题仍然存在

请提供以下信息：

1. **调试接口的返回结果**
2. **后端日志中的关键信息**
3. **数据库中实际的仪表ID**
4. **InfluxDB中是否有对应的数据**

这样我可以进一步定位和修复问题。

## 🎯 预期结果

修复后，导出的Excel应该：
- 包含完整的时间范围（00:00 到 21:45）
- 显示4个仪表的A相电流数据
- 每个时间点都有对应的数值（不是"-"）
