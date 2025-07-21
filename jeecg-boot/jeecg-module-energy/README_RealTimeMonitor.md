# 实时数据监控接口使用说明

## 📋 概述

本文档介绍了能源管理系统中实时数据监控模块的接口使用方法。该模块提供了根据维度树选择仪表、查看多参数时序数据图表的功能。

## 🔧 接口列表

### 1. 根据维度获取仪表列表

**接口地址：** `GET /energy/monitor/getModulesByOrgCode`

**功能描述：** 根据维度编码获取该维度下的所有启用仪表

**请求参数：**
- `orgCode` (String, 必填): 维度编码，如 "A02A02A01"
- `includeChildren` (Boolean, 可选): 是否包含子维度，默认为 true

**请求示例：**
```bash
GET /energy/monitor/getModulesByOrgCode?orgCode=A02A02A01&includeChildren=true
```

**响应示例：**
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_1202",
            "moduleName": "1号注塑机",
            "orgCode": "A02A02A01A01",
            "departName": "1号注塑机",
            "energyType": 1,
            "isAction": "Y"
        }
    ]
}
```

### 2. 查询实时数据

**接口地址：** `POST /energy/monitor/getRealTimeMonitorData`

**功能描述：** 查询指定仪表、参数、时间范围的实时数据

**请求参数：**
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_12"],
    "parameters": [1, 4, 7],
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00",
    "interval": 1,
    "displayMode": 1
}
```

**参数说明：**
- `moduleIds`: 仪表编号列表
- `parameters`: 参数编号列表 (1=A相电流, 2=B相电流, 3=C相电流, 4=A相电压, 5=B相电压, 6=C相电压, 7=总有功功率, 8=总无功功率, 9=总视在功率, 10=功率因数, 11=频率, 12=正向有功总电能)
- `startTime`: 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
- `endTime`: 结束时间 (格式: yyyy-MM-dd HH:mm:ss)
- `interval`: 查询间隔 (1=15分钟, 2=30分钟, 3=60分钟, 4=120分钟)
- `displayMode`: 显示方式 (1=统一显示, 2=分开显示)

## 📊 响应格式

### 统一显示模式 (displayMode=1)

```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": {
        "displayMode": "unified",
        "timeRange": {
            "startTime": "2025-07-15 08:00:00",
            "endTime": "2025-07-15 16:00:00",
            "interval": "15分钟"
        },
        "series": [
            {
                "name": "1号注塑机-A相电流",
                "moduleId": "yj0001_1202",
                "moduleName": "1号注塑机",
                "parameter": "A相电流",
                "parameterCode": "IA",
                "unit": "A",
                "data": [
                    ["2025-07-15 08:00:00", 12.5],
                    ["2025-07-15 08:15:00", 12.8]
                ]
            }
        ]
    }
}
```

### 分开显示模式 (displayMode=2)

```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": {
        "displayMode": "separated",
        "timeRange": {
            "startTime": "2025-07-15 08:00:00",
            "endTime": "2025-07-15 16:00:00",
            "interval": "15分钟"
        },
        "charts": [
            {
                "title": "A相电流",
                "parameter": "A相电流",
                "parameterCode": "IA",
                "unit": "A",
                "series": [
                    {
                        "name": "1号注塑机",
                        "moduleId": "yj0001_1202",
                        "data": [
                            ["2025-07-15 08:00:00", 12.5],
                            ["2025-07-15 08:15:00", 12.8]
                        ]
                    }
                ]
            }
        ]
    }
}
```

## 🔍 使用示例

### 前端JavaScript调用示例

```javascript
// 1. 获取仪表列表
async function getModules(orgCode) {
    const response = await fetch(`/energy/monitor/getModulesByOrgCode?orgCode=${orgCode}&includeChildren=true`);
    const result = await response.json();
    return result.result;
}

// 2. 查询实时数据
async function getRealTimeData(request) {
    const response = await fetch('/energy/monitor/getRealTimeMonitorData', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });
    const result = await response.json();
    return result.result;
}

// 使用示例
const modules = await getModules('A02A02A01');
console.log('仪表列表:', modules);

const realTimeData = await getRealTimeData({
    moduleIds: ['yj0001_1202', 'yj0001_12'],
    parameters: [1, 4, 7],
    startTime: '2025-07-15 08:00:00',
    endTime: '2025-07-15 16:00:00',
    interval: 1,
    displayMode: 1
});
console.log('实时数据:', realTimeData);
```

## ⚠️ 注意事项

1. **时区处理**: 接口自动处理时区转换，前端传入本地时间即可
2. **数据聚合**: 根据查询间隔自动聚合数据，使用平均值计算
3. **参数映射**: 参数编号与InfluxDB字段名的映射已在后端配置
4. **错误处理**: 接口包含完整的参数验证和异常处理
5. **性能优化**: 建议合理设置时间范围，避免查询过大数据量

## 🧪 测试

项目包含完整的单元测试，可以运行以下命令进行测试：

```bash
mvn test -Dtest=EnergyMonitorControllerTest
```

## 📝 更新日志

- 2025-07-16: 初始版本，实现基本的实时数据监控功能
- 支持统一显示和分开显示两种模式
- 支持多仪表多参数查询
- 支持时间间隔聚合
- 完整的参数验证和异常处理
