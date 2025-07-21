# getRealTimeMonitorData 接口调用问题排查指南

## 🔍 接口基本信息

### 接口地址
```
POST /energy/monitor/getRealTimeMonitorData
```

### 请求方式
- **Method**: POST
- **Content-Type**: application/json
- **Body**: JSON格式的请求参数

## 📝 正确的请求参数格式

### 完整的JSON请求体示例
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203"],
    "parameters": [1, 4, 7],
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00",
    "interval": 1,
    "displayMode": 1
}
```

### 参数详细说明

#### 1. moduleIds (仪表编号列表)
- **类型**: Array<String>
- **必填**: 是
- **说明**: 要查询的仪表编号列表
- **示例**: `["yj0001_1202", "yj0001_1203"]`

#### 2. parameters (参数编号列表)
- **类型**: Array<Integer>
- **必填**: 是
- **取值范围**:
  - `1`: A相电流
  - `2`: B相电流
  - `3`: C相电流
  - `4`: A相电压
  - `5`: B相电压
  - `6`: C相电压
  - `7`: 总有功功率
  - `8`: 总无功功率
  - `9`: 总视在功率
  - `10`: 功率因数
  - `11`: 频率
  - `12`: 正向有功总电能
- **示例**: `[1, 4, 7]` (查询A相电流、A相电压、总有功功率)

#### 3. startTime (开始时间)
- **类型**: String
- **必填**: 是
- **格式**: `yyyy-MM-dd HH:mm:ss`
- **示例**: `"2025-07-15 08:00:00"`

#### 4. endTime (结束时间)
- **类型**: String
- **必填**: 是
- **格式**: `yyyy-MM-dd HH:mm:ss`
- **示例**: `"2025-07-15 16:00:00"`

#### 5. interval (查询间隔)
- **类型**: Integer
- **必填**: 是
- **取值范围**:
  - `1`: 15分钟
  - `2`: 30分钟
  - `3`: 60分钟
  - `4`: 120分钟
- **示例**: `1`

#### 6. displayMode (显示方式)
- **类型**: Integer
- **必填**: 是
- **取值范围**:
  - `1`: 统一显示
  - `2`: 分开显示
- **示例**: `1`

## ❌ 常见错误及解决方案

### 1. 参数验证错误

#### 错误信息: "仪表编号列表不能为空"
**原因**: `moduleIds` 参数为空或未传递
**解决方案**:
```json
// ❌ 错误
{
    "moduleIds": [],
    // 或者缺少 moduleIds 字段
}

// ✅ 正确
{
    "moduleIds": ["yj0001_1202"]
}
```

#### 错误信息: "参数编号列表不能为空"
**原因**: `parameters` 参数为空或未传递
**解决方案**:
```json
// ❌ 错误
{
    "parameters": []
}

// ✅ 正确
{
    "parameters": [1, 4, 7]
}
```

#### 错误信息: "显示方式必须为1(统一显示)或2(分开显示)"
**原因**: `displayMode` 值不在有效范围内
**解决方案**:
```json
// ❌ 错误
{
    "displayMode": 0  // 或 3, 4 等无效值
}

// ✅ 正确
{
    "displayMode": 1  // 或 2
}
```

#### 错误信息: "查询间隔必须为1-4之间的整数"
**原因**: `interval` 值不在有效范围内
**解决方案**:
```json
// ❌ 错误
{
    "interval": 0  // 或 5, 6 等无效值
}

// ✅ 正确
{
    "interval": 1  // 1-4之间的整数
}
```

### 2. 时间格式错误

#### 错误信息: 时间解析相关错误
**原因**: 时间格式不正确
**解决方案**:
```json
// ❌ 错误
{
    "startTime": "2025-07-15",           // 缺少时间部分
    "endTime": "2025/07/15 16:00:00"     // 日期分隔符错误
}

// ✅ 正确
{
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00"
}
```

### 3. 数据类型错误

#### 错误信息: JSON解析错误
**原因**: 参数类型不匹配
**解决方案**:
```json
// ❌ 错误
{
    "moduleIds": "yj0001_1202",          // 应该是数组
    "parameters": "1,4,7",               // 应该是数组
    "interval": "1",                     // 应该是数字
    "displayMode": "1"                   // 应该是数字
}

// ✅ 正确
{
    "moduleIds": ["yj0001_1202"],
    "parameters": [1, 4, 7],
    "interval": 1,
    "displayMode": 1
}
```

## 🧪 测试用例

### 1. 基本测试用例
```bash
curl -X POST "http://localhost:8080/jeecg-boot/energy/monitor/getRealTimeMonitorData" \
     -H "Content-Type: application/json" \
     -d '{
         "moduleIds": ["yj0001_1202"],
         "parameters": [1, 4, 7],
         "startTime": "2025-07-15 08:00:00",
         "endTime": "2025-07-15 16:00:00",
         "interval": 1,
         "displayMode": 1
     }'
```

### 2. 多仪表多参数测试
```bash
curl -X POST "http://localhost:8080/jeecg-boot/energy/monitor/getRealTimeMonitorData" \
     -H "Content-Type: application/json" \
     -d '{
         "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204"],
         "parameters": [1, 2, 3, 4, 5, 6, 7],
         "startTime": "2025-07-15 00:00:00",
         "endTime": "2025-07-15 23:59:59",
         "interval": 2,
         "displayMode": 2
     }'
```

### 3. Swagger测试
1. 访问 `http://localhost:8080/jeecg-boot/doc.html`
2. 找到 "能源实时监控" -> "查询实时数据"
3. 点击 "Try it out"
4. 填入测试参数
5. 点击 "Execute"

## 🔧 调试建议

### 1. 检查请求格式
- 确保使用 POST 方法
- 确保 Content-Type 为 application/json
- 确保请求体是有效的JSON格式

### 2. 检查参数完整性
- 所有必填参数都已提供
- 参数类型正确（数组、字符串、数字）
- 参数值在有效范围内

### 3. 检查数据存在性
- 确保 `moduleIds` 中的仪表编号在数据库中存在
- 确保仪表的 `isaction` 字段为 'Y'
- 确保时间范围内有数据

### 4. InfluxDB相关问题
- 确保InfluxDB服务正常运行
- 确保数据库连接配置正确
- 确保当前月份的数据库存在
- 检查InfluxDB中是否有对应的数据

### 5. 查看日志
检查应用日志中的详细错误信息：
```bash
tail -f logs/jeecg-boot.log | grep -i error
```

## 🐛 已知问题及修复

### InfluxDB查询语法错误 (已修复)
**错误信息**: `error parsing query: found IN, expected ; at line 1, char 123`

**原因**: InfluxDB 1.8不支持 `IN` 操作符

**修复方案**: 将 `tagname IN (...)` 改为 `(tagname = '...' OR tagname = '...')`

**修复状态**: ✅ 已修复 (2025-07-17)

## 📋 检查清单

在调用接口前，请确认：

- [ ] 请求方法为 POST
- [ ] Content-Type 为 application/json
- [ ] moduleIds 是字符串数组且不为空
- [ ] parameters 是整数数组且不为空，值在1-12范围内
- [ ] startTime 格式为 "yyyy-MM-dd HH:mm:ss"
- [ ] endTime 格式为 "yyyy-MM-dd HH:mm:ss"
- [ ] interval 为1-4之间的整数
- [ ] displayMode 为1或2
- [ ] 仪表编号在数据库中存在且启用
- [ ] 时间范围合理（结束时间大于开始时间）

如果按照以上检查清单仍有问题，请提供具体的错误信息和请求参数，我可以进一步帮您分析。
