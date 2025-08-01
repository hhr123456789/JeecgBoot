# 设备负荷监控接口开发文档

## 📋 功能概述

设备负荷监控模块专门用于监控电力设备的负荷情况，提供有功功率和负荷率的时序数据查询和可视化功能，支持日/月/年不同时间粒度的数据展示。

### 核心功能
- 🌳 **维度树选择**：根据选择的维度节点获取对应的电力仪表列表
- 📊 **多仪表选择**：支持多选仪表，同时显示多个设备的负荷曲线
- 📈 **负荷曲线图表**：显示有功功率(kW)的时序变化曲线
- 📉 **负荷率曲线图表**：显示负荷率(%)的时序变化曲线（功率/额定功率×100%）
- 📋 **数据表格**：显示各仪表在不同时间点的功率和负荷率详细数据

### 时间粒度说明
- **日查询**：以每小时为间隔，查询24小时的负荷数据
- **月查询**：以每天为间隔，查询整月的负荷数据
- **年查询**：以每月为间隔，查询整年的负荷数据

### 监控指标说明
- **有功功率(kW)**：设备当前的实际功率消耗
- **负荷率(%)**：当前功率与设备额定功率的比值，反映设备利用率

## 🔧 接口设计

### 1. 根据维度获取电力仪表列表接口

#### 接口信息
- **URL**: `/energy/realtime/getModulesByDimension`
- **Method**: `GET`
- **功能**: 根据维度编码获取该维度下的所有启用电力仪表，支持多选

#### 请求参数（URL参数）
- `dimensionCode`: 维度编码 (必填，例如: "A02A02A01")
- `energyType`: 能源类型 (必填，1:电力,2:天然气,3:压缩空气,4:企业用水)
- `includeChildren`: 是否包含子维度 (可选，默认true)

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_1202",
            "moduleName": "1号注塑机",
            "energyType": 1,
            "dimensionCode": "A02A02A01A01",
            "dimensionName": "1号注塑机",
            "ratedPower": 1000.00,
            "currentPower": 850.5,
            "loadRate": 85.05,
            "isOnline": true,
            "isAction": "Y",
            "updateTime": "2025-07-25 14:30:00"
        },
        {
            "moduleId": "yj0001_1203",
            "moduleName": "2号注塑机",
            "energyType": 1,
            "dimensionCode": "A02A02A01A02",
            "dimensionName": "2号注塑机",
            "ratedPower": 1200.00,
            "currentPower": 920.8,
            "loadRate": 76.73,
            "isOnline": true,
            "isAction": "Y",
            "updateTime": "2025-07-25 14:29:00"
        }
    ]
}
```

### 2. 获取参数配置接口

#### 接口信息
- **URL**: `/energy/realtime/getParameterConfig`
- **Method**: `GET`
- **功能**: 根据能源类型获取可选的参数配置列表

#### 请求参数（URL参数）
- `energyType`: 能源类型 (必填，1:电力,2:天然气,3:压缩空气,4:企业用水)

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "paramCode": 1,
            "paramName": "A相电流",
            "fieldName": "IA",
            "unit": "A"
        },
        {
            "paramCode": 7,
            "paramName": "总有功功率",
            "fieldName": "P",
            "unit": "kW"
        }
    ]
}
```

#### 监控参数说明
- **有功功率 (kW)**: 设备实际消耗的功率，对应InfluxDB中的P字段
- **负荷率 (%)**: 有功功率/额定功率×100%，反映设备利用率

### 3. 负荷时序数据查询接口（核心接口）

#### 接口信息
- **URL**: `/energy/realtime/getLoadTimeSeriesData`
- **Method**: `POST`
- **功能**: 根据时间粒度查询多仪表的负荷时序数据，用于负荷曲线和负荷率曲线展示

#### 请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204"],  // 仪表ID列表 (必填)
    "timeGranularity": "day",                                   // 时间粒度 (必填: day/month/year)
    "queryDate": "2025-07-25",                                  // 查询日期 (必填)
    "startTime": "2025-07-25 00:00:00",                        // 开始时间 (可选，用于精确时间范围)
    "endTime": "2025-07-25 23:59:59"                           // 结束时间 (可选，用于精确时间范围)
}
```

#### 时间粒度说明
- **day**: 查询指定日期的24小时负荷数据，每小时一个数据点
- **month**: 查询指定月份的负荷数据，每天一个数据点
- **year**: 查询指定年份的负荷数据，每月一个数据点

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": {
        "powerChartData": {
            "title": "有功功率",
            "timeLabels": [
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            ],
            "series": [
                {
                    "moduleId": "yj0001_1202",
                    "moduleName": "1号注塑机",
                    "unit": "kW",
                    "data": [850.5, 860.2, 845.8, 855.3, 862.1, 858.9, 868.4, 872.6, 881.2, 875.8, 869.7, 878.5, 885.2, 879.6, 867.4, 858.2, 864.7, 871.3, 866.8, 859.1, 853.6, 847.9, 851.4, 849.2],
                    "color": "#1890ff"
                },
                {
                    "moduleId": "yj0001_1203",
                    "moduleName": "2号注塑机",
                    "unit": "kW",
                    "data": [920.8, 930.5, 915.2, 925.7, 932.4, 928.1, 938.6, 942.8, 951.4, 945.9, 939.8, 948.6, 955.3, 949.7, 937.5, 928.3, 934.8, 941.4, 936.9, 929.2, 923.7, 918.0, 921.5, 919.3],
                    "color": "#52c41a"
                }
            ]
        },
        "loadRateChartData": {
            "title": "负荷率",
            "timeLabels": [
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            ],
            "series": [
                {
                    "moduleId": "yj0001_1202",
                    "moduleName": "1号注塑机",
                    "unit": "%",
                    "data": [85.05, 86.02, 84.58, 85.53, 86.21, 85.89, 86.84, 87.26, 88.12, 87.58, 86.97, 87.85, 88.52, 87.96, 86.74, 85.82, 86.47, 87.13, 86.68, 85.91, 85.36, 84.79, 85.14, 84.92],
                    "color": "#1890ff"
                },
                {
                    "moduleId": "yj0001_1203",
                    "moduleName": "2号注塑机",
                    "unit": "%",
                    "data": [76.73, 77.54, 76.27, 77.14, 77.70, 77.34, 78.22, 78.57, 79.28, 78.83, 78.32, 79.05, 79.61, 79.14, 78.13, 77.36, 77.90, 78.45, 78.08, 77.43, 76.98, 76.50, 76.79, 76.61],
                    "color": "#52c41a"
                }
            ]
        },
        "tableData": [
            {
                "time": "00:00",
                "timeLabel": "2025-07-25 00:00",
                "modules": [
                    {
                        "moduleId": "yj0001_1202",
                        "moduleName": "1号注塑机",
                        "ratedPower": 1000.00,
                        "currentPower": 850.5,
                        "loadRate": 85.05,
                        "powerUnit": "kW",
                        "loadRateUnit": "%"
                    },
                    {
                        "moduleId": "yj0001_1203",
                        "moduleName": "2号注塑机",
                        "ratedPower": 1200.00,
                        "currentPower": 920.8,
                        "loadRate": 76.73,
                        "powerUnit": "kW",
                        "loadRateUnit": "%"
                    }
                ]
            },
            {
                "time": "01:00",
                "timeLabel": "2025-07-25 01:00",
                "modules": [
                    {
                        "moduleId": "yj0001_1202",
                        "moduleName": "1号注塑机",
                        "ratedPower": 1000.00,
                        "currentPower": 860.2,
                        "loadRate": 86.02,
                        "powerUnit": "kW",
                        "loadRateUnit": "%"
                    },
                    {
                        "moduleId": "yj0001_1203",
                        "moduleName": "2号注塑机",
                        "ratedPower": 1200.00,
                        "currentPower": 930.5,
                        "loadRate": 77.54,
                        "powerUnit": "kW",
                        "loadRateUnit": "%"
                    }
                ]
            }
        ],
        "summary": {
            "totalDataPoints": 48,
            "timeRange": "2025-07-25 00:00:00 ~ 2025-07-25 23:00:00",
            "granularity": "每小时",
            "moduleCount": 2,
            "dataType": "负荷监控数据"
        }
    }
}
```

### 4. 获取实时负荷状态接口

#### 接口信息
- **URL**: `/energy/realtime/getCurrentLoadStatus`
- **Method**: `POST`
- **功能**: 获取选中仪表的当前实时负荷状态和最新功率数值

#### 请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203"]  // 仪表ID列表 (必填)
}
```

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_1202",
            "moduleName": "1号注塑机",
            "isOnline": true,
            "lastUpdateTime": "2025-07-25 14:30:15",
            "ratedPower": 1000.00,
            "currentPower": 868.4,
            "loadRate": 86.84,
            "powerUnit": "kW",
            "loadRateUnit": "%",
            "status": "normal",
            "loadLevel": "高负荷"
        },
        {
            "moduleId": "yj0001_1203",
            "moduleName": "2号注塑机",
            "isOnline": true,
            "lastUpdateTime": "2025-07-25 14:29:45",
            "ratedPower": 1200.00,
            "currentPower": 938.6,
            "loadRate": 78.22,
            "powerUnit": "kW",
            "loadRateUnit": "%",
            "status": "normal",
            "loadLevel": "中等负荷"
        }
    ]
}
```

### 5. 通用时序数据查询接口

#### 接口信息
- **URL**: `/energy/realtime/getTimeSeriesData`
- **Method**: `POST`
- **功能**: 根据时间粒度查询多仪表、多参数的时序数据，用于通用图表和表格展示

#### 请求参数
```json
{
    "moduleIds": ["yj0001_13", "yj0001_14"],     // 仪表ID列表 (必填)
    "parameters": [1, 2, 7],                    // 参数编码列表 (必填)
    "timeGranularity": "day",                   // 时间粒度 (必填: day/month/year)
    "queryDate": "2025-07-25",                 // 查询日期 (必填)
    "startTime": "2025-07-25 00:00:00",        // 开始时间 (可选)
    "endTime": "2025-07-25 23:59:59"           // 结束时间 (可选)
}
```

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": {
        "chartData": {
            "timeLabels": ["00:00", "01:00", "02:00", "..."],
            "series": [
                {
                    "moduleId": "yj0001_13",
                    "moduleName": "1号设备",
                    "paramCode": 1,
                    "paramName": "A相电流",
                    "unit": "A",
                    "data": [10.5, 11.2, 10.8, "..."],
                    "color": "#1890ff"
                }
            ]
        },
        "tableData": [
            {
                "time": "00:00",
                "timeLabel": "2025-07-25 00:00",
                "data": {
                    "yj0001_13_IA": 10.5,
                    "yj0001_13_IB": 10.3,
                    "yj0001_13_PP": 850.5
                }
            }
        ]
    }
}
```

### 6. 获取实时状态接口

#### 接口信息
- **URL**: `/energy/realtime/getCurrentStatus`
- **Method**: `POST`
- **功能**: 获取选中仪表的当前实时状态和最新数值

#### 请求参数
```json
{
    "moduleIds": ["yj0001_13", "yj0001_14"],    // 仪表ID列表 (必填)
    "parameters": [1, 2, 7]                    // 参数编码列表 (必填)
}
```

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_13",
            "moduleName": "1号设备",
            "isOnline": true,
            "lastUpdateTime": "2025-07-25 14:30:15",
            "parameters": [
                {
                    "paramCode": 1,
                    "paramName": "A相电流",
                    "currentValue": 10.5,
                    "unit": "A",
                    "status": "normal"
                },
                {
                    "paramCode": 7,
                    "paramName": "总有功功率",
                    "currentValue": 850.5,
                    "unit": "kW",
                    "status": "normal"
                }
            ]
        }
    ]
}
```

### 7. 负荷数据表格查询接口

#### 接口信息
- **URL**: `/energy/realtime/getLoadTableData`
- **Method**: `POST`
- **功能**: 获取负荷数据表格，显示各仪表在指定时间范围内的详细功率和负荷率统计数据

#### 请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204"],  // 仪表ID数组 (必填)
    "timeType": "day",                                          // 时间类型 (必填: day/month/year)
    "startTime": "2025-07-25 00:00:00",                        // 开始时间 (必填)
    "endTime": "2025-07-25 23:59:59",                          // 结束时间 (必填)
    "pageNum": 1,                                              // 页码 (可选，默认1)
    "pageSize": 100                                            // 每页条数 (可选，默认100)
}
```

#### 响应数据
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": {
        "tableData": [
            {
                "序号": 1,
                "设备名称": "1号设备",
                "最大功率": 90.25,
                "最大功率率": 90.3,
                "最大功率发生时间": "14:30",
                "最小功率": 65.12,
                "最小功率率": 65.1,
                "最小功率发生时间": "02:30",
                "平均功率": 78.45,
                "平均功率率": 78.5
            },
            {
                "序号": 2,
                "设备名称": "2号设备",
                "最大功率": 82.4,
                "最大功率率": 82.4,
                "最大功率发生时间": "15:45",
                "最小功率": 62.33,
                "最小功率率": 62.1,
                "最小功率发生时间": "03:30",
                "平均功率": 73.69,
                "平均功率率": 73.5
            },
            {
                "序号": 3,
                "设备名称": "3号设备",
                "最大功率": 95.12,
                "最大功率率": 95.2,
                "最大功率发生时间": "16:20",
                "最小功率": 59.67,
                "最小功率率": 61.5,
                "最小功率发生时间": "04:15",
                "平均功率": 82.34,
                "平均功率率": 85.7
            }
        ],
        "pagination": {
            "total": 3,
            "pageNum": 1,
            "pageSize": 100,
            "pages": 1
        },
        "summary": {
            "totalModules": 3,
            "timeRange": "2025-07-25 00:00:00 ~ 2025-07-25 23:59:59",
            "dataType": "负荷统计数据"
        }
    }
}
```

## 🗄️ 数据库设计

### InfluxDB负荷数据查询逻辑

#### 日查询（每小时间隔）
```sql
SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value
FROM hist
WHERE (tagname = 'YJ0001_1202#P' OR tagname = 'YJ0001_1203#P')
  AND time >= '2025-07-25T00:00:00Z'
  AND time < '2025-07-26T00:00:00Z'
GROUP BY time(1h), tagname
ORDER BY time ASC
```

#### 月查询（每天间隔）
```sql
SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value
FROM hist
WHERE (tagname = 'YJ0001_1202#P' OR tagname = 'YJ0001_1203#P')
  AND time >= '2025-07-01T00:00:00Z'
  AND time < '2025-08-01T00:00:00Z'
GROUP BY time(1d), tagname
ORDER BY time ASC
```

#### 年查询（每月间隔）
```sql
SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value
FROM hist
WHERE (tagname = 'YJ0001_1202#P' OR tagname = 'YJ0001_1203#P')
  AND time >= '2025-01-01T00:00:00Z'
  AND time < '2026-01-01T00:00:00Z'
GROUP BY time(30d), tagname
ORDER BY time ASC
```

#### 负荷率计算说明
- **有功功率**: 直接从InfluxDB的P字段获取（总有功功率）
- **负荷率**: 有功功率 ÷ 额定功率 × 100%
- **额定功率**: 从MySQL的tb_module表的rated_power字段获取

### MySQL关联查询

#### 获取电力仪表信息（含额定功率）
```sql
SELECT
    m.module_id,
    m.module_name,
    m.energy_type,
    m.dimension_code,
    d.depart_name as dimension_name,
    m.rated_power,
    m.isaction,
    m.update_time,
    COALESCE(e.P, 0) as current_power,
    CASE
        WHEN m.rated_power > 0 THEN ROUND((COALESCE(e.P, 0) / m.rated_power * 100), 2)
        ELSE 0
    END as load_rate
FROM tb_module m
LEFT JOIN sys_depart d ON m.dimension_code = d.org_code
LEFT JOIN tb_equ_ele_data e ON m.module_id = e.module_id
WHERE m.dimension_code LIKE 'A02A02A01%'
  AND m.isaction = 'Y'
  AND m.energy_type = 1
  AND m.rated_power IS NOT NULL
  AND m.rated_power > 0
ORDER BY m.module_name
```

## 🎯 前端交互流程

### 1. 页面初始化
```javascript
// 1. 加载维度树（已有接口）
// 2. 设置默认时间和粒度
const today = new Date();
setQueryDate(today);
setTimeGranularity('day');

// 3. 初始化图表配置
initPowerChart(); // 有功功率图表
initLoadRateChart(); // 负荷率图表
```

### 2. 维度选择事件
```javascript
onDimensionSelect(dimensionCode) {
    // 1. 清空仪表选择
    clearModuleSelection();

    // 2. 加载该维度下的电力仪表
    loadModulesByDimension(dimensionCode, 1); // energyType=1 电力

    // 3. 清空图表和表格
    clearChartAndTable();
}
```

### 3. 仪表选择事件
```javascript
onModuleSelect(selectedModuleIds) {
    if (selectedModuleIds.length > 0) {
        // 1. 获取实时负荷状态
        getCurrentLoadStatus(selectedModuleIds);

        // 2. 查询负荷时序数据
        loadLoadTimeSeriesData();
    } else {
        clearChartAndTable();
    }
}
```

### 4. 时间粒度切换事件
```javascript
onTimeGranularityChange(granularity) {
    setTimeGranularity(granularity);

    // 根据粒度调整时间选择器
    if (granularity === 'day') {
        showDatePicker(); // 显示日期选择
    } else if (granularity === 'month') {
        showMonthPicker(); // 显示月份选择
    } else if (granularity === 'year') {
        showYearPicker(); // 显示年份选择
    }

    // 重新查询负荷数据
    if (selectedModuleIds.length > 0) {
        loadLoadTimeSeriesData();
    }
}
```

### 5. 数据查询核心方法
```javascript
async function loadLoadTimeSeriesData() {
    const params = {
        moduleIds: selectedModuleIds,
        timeGranularity: timeGranularity,
        queryDate: queryDate,
        startTime: startTime,
        endTime: endTime
    };

    try {
        const response = await api.post('/energy/realtime/getLoadTimeSeriesData', params);
        if (response.success) {
            // 更新有功功率图表
            updatePowerChart(response.result.powerChartData);
            // 更新负荷率图表
            updateLoadRateChart(response.result.loadRateChartData);
            // 更新表格数据
            updateTableData(response.result.tableData);

            // 同时加载统计表格数据
            loadLoadTableData();
        }
    } catch (error) {
        console.error('查询负荷数据失败:', error);
    }
}

### 6. 负荷统计表格数据查询方法
```javascript
async function loadLoadTableData() {
    const params = {
        moduleIds: selectedModuleIds,
        timeType: timeGranularity,
        startTime: startTime,
        endTime: endTime,
        pageNum: 1,
        pageSize: 100
    };

    try {
        const response = await api.post('/energy/realtime/getLoadTableData', params);
        if (response.success) {
            // 更新统计表格
            updateLoadStatisticsTable(response.result.tableData);
            // 更新分页信息
            updatePagination(response.result.pagination);
        }
    } catch (error) {
        console.error('查询负荷统计表格失败:', error);
    }
}
```

## 📊 图表配置

### 有功功率图表配置
```javascript
const powerChartOption = {
    title: {
        text: '有功功率',
        left: 'center'
    },
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'cross'
        },
        formatter: function(params) {
            let html = params[0].axisValue + '<br/>';
            params.forEach(param => {
                html += `${param.marker}${param.seriesName}: ${param.value} kW<br/>`;
            });
            return html;
        }
    },
    legend: {
        data: [], // 动态设置仪表名称
        top: 30
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis: {
        type: 'category',
        boundaryGap: false,
        data: [] // 时间标签
    },
    yAxis: {
        type: 'value',
        name: '功率 (kW)',
        axisLabel: {
            formatter: '{value} kW'
        }
    },
    series: [] // 动态设置
};
```

### 负荷率图表配置
```javascript
const loadRateChartOption = {
    title: {
        text: '负荷率',
        left: 'center'
    },
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'cross'
        },
        formatter: function(params) {
            let html = params[0].axisValue + '<br/>';
            params.forEach(param => {
                html += `${param.marker}${param.seriesName}: ${param.value}%<br/>`;
            });
            return html;
        }
    },
    legend: {
        data: [], // 动态设置仪表名称
        top: 30
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis: {
        type: 'category',
        boundaryGap: false,
        data: [] // 时间标签
    },
    yAxis: {
        type: 'value',
        name: '负荷率 (%)',
        min: 0,
        max: 100,
        axisLabel: {
            formatter: '{value}%'
        }
    },
    series: [] // 动态设置数据系列
};
```

### 负荷数据表格配置
```javascript
const loadTableColumns = [
    {
        title: '时间',
        dataIndex: 'time',
        key: 'time',
        width: 120,
        fixed: 'left'
    },
    // 动态生成仪表负荷列
    ...generateLoadColumns(selectedModules)
];

function generateLoadColumns(modules) {
    const columns = [];
    modules.forEach(module => {
        // 有功功率列
        columns.push({
            title: `${module.moduleName}/有功功率(kW)`,
            dataIndex: `${module.moduleId}_power`,
            key: `${module.moduleId}_power`,
            width: 150,
            render: (value) => value !== null ? Number(value).toFixed(2) : '-'
        });

        // 负荷率列
        columns.push({
            title: `${module.moduleName}/负荷率(%)`,
            dataIndex: `${module.moduleId}_loadRate`,
            key: `${module.moduleId}_loadRate`,
            width: 150,
            render: (value) => value !== null ? Number(value).toFixed(2) + '%' : '-'
        });
    });
    return columns;
}
```

### 负荷统计表格配置
```javascript
const loadStatisticsColumns = [
    {
        title: '序号',
        dataIndex: '序号',
        key: 'index',
        width: 80,
        align: 'center'
    },
    {
        title: '设备名称',
        dataIndex: '设备名称',
        key: 'deviceName',
        width: 120,
        fixed: 'left'
    },
    {
        title: '最大功率(kW)',
        dataIndex: '最大功率(kW)',
        key: 'maxPower',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(2)
    },
    {
        title: '最大功率率(%)',
        dataIndex: '最大功率率(%)',
        key: 'maxPowerRate',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(1) + '%'
    },
    {
        title: '最大功率发生时间',
        dataIndex: '最大功率发生时间',
        key: 'maxPowerTime',
        width: 140,
        align: 'center'
    },
    {
        title: '最小功率(kW)',
        dataIndex: '最小功率(kW)',
        key: 'minPower',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(2)
    },
    {
        title: '最小功率率(%)',
        dataIndex: '最小功率率(%)',
        key: 'minPowerRate',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(1) + '%'
    },
    {
        title: '最小功率发生时间',
        dataIndex: '最小功率发生时间',
        key: 'minPowerTime',
        width: 140,
        align: 'center'
    },
    {
        title: '平均功率(kW)',
        dataIndex: '平均功率(kW)',
        key: 'avgPower',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(2)
    },
    {
        title: '平均功率率(%)',
        dataIndex: '平均功率率(%)',
        key: 'avgPowerRate',
        width: 120,
        align: 'right',
        render: (value) => Number(value).toFixed(1) + '%'
    }
];
```

## 🔧 后端实现要点

### 1. Service层核心方法

```java
@Service
public class LoadMonitorService {

    /**
     * 根据维度获取电力仪表列表（含额定功率）
     */
    public List<ModuleInfo> getModulesByDimension(String dimensionCode, Integer energyType, Boolean includeChildren) {
        // 1. 构建维度查询条件
        // 2. 查询tb_module表，过滤电力类型且有额定功率的仪表
        // 3. 关联sys_depart获取维度名称
        // 4. 关联tb_equ_ele_data获取当前功率值
        // 5. 计算负荷率
        // 6. 返回仪表列表
    }

    /**
     * 查询负荷时序数据（核心方法）
     */
    public LoadTimeSeriesResult getLoadTimeSeriesData(LoadTimeSeriesQuery query) {
        // 1. 参数验证
        // 2. 从MySQL获取仪表的额定功率信息
        // 3. 构建InfluxDB查询语句（只查询PP字段）
        // 4. 根据时间粒度设置GROUP BY间隔
        // 5. 使用OR语法构建tagname条件
        // 6. 执行查询并处理结果
        // 7. 时区转换（UTC -> 北京时间）
        // 8. 计算负荷率（功率/额定功率×100%）
        // 9. 组装有功功率图表数据和负荷率图表数据
        // 10. 生成表格数据
        // 11. 返回完整的负荷监控数据
    }

    /**
     * 获取实时负荷状态
     */
    public List<ModuleLoadStatus> getCurrentLoadStatus(List<String> moduleIds) {
        // 1. 查询最新的功率数据（从tb_equ_ele_data的P字段）
        // 2. 获取仪表的额定功率（从tb_module的rated_power字段）
        // 3. 计算当前负荷率
        // 4. 判断设备在线状态
        // 5. 确定负荷等级（低负荷<50%, 中等负荷50-80%, 高负荷>80%）
        // 6. 返回当前负荷状态信息
    }

    /**
     * 获取负荷数据表格（统计数据）
     */
    public LoadTableResult getLoadTableData(LoadTableQuery query) {
        // 1. 参数验证
        // 2. 从MySQL获取仪表基本信息和额定功率
        // 3. 构建InfluxDB查询语句，查询指定时间范围内的P字段数据
        // 4. 使用OR语法构建tagname条件
        // 5. 执行查询并处理结果
        // 6. 对每个仪表计算统计数据：
        //    - 最大功率及发生时间
        //    - 最小功率及发生时间
        //    - 平均功率
        //    - 最大负荷率及发生时间
        //    - 最小负荷率及发生时间
        //    - 平均负荷率
        // 7. 时区转换（UTC -> 北京时间）
        // 8. 组装表格数据，按序号排列
        // 9. 支持分页处理
        // 10. 返回表格统计结果
    }
}
```

### 2. InfluxDB负荷数据查询工具类

```java
@Component
public class InfluxDBQueryBuilder {

    /**
     * 构建时序数据查询语句
     */
    public String buildTimeSeriesQuery(List<String> moduleIds, List<Integer> parameters,
                                     String timeGranularity, String startTime, String endTime) {

        // 1. 构建tagname列表
        List<String> tagnames = buildTagnames(moduleIds, parameters);

        // 2. 根据时间粒度设置GROUP BY间隔
        String interval = getTimeInterval(timeGranularity);

        // 3. 转换时间格式
        String utcStartTime = convertToUTC(startTime);
        String utcEndTime = convertToUTC(endTime);

        // 4. 构建查询语句 - 使用OR语法（与InfluxDBQueryServiceImpl保持一致）
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MEAN(value) as avg_value, MAX(value) as max_value, MIN(value) as min_value ");
        sql.append("FROM hist ");
        sql.append("WHERE (").append(buildOrCondition(tagnames)).append(") ");
        sql.append("AND time >= '").append(utcStartTime).append("' ");
        sql.append("AND time < '").append(utcEndTime).append("' ");
        sql.append("GROUP BY time(").append(interval).append("), tagname ");
        sql.append("ORDER BY time ASC");

        return sql.toString();
    }

    private String getTimeInterval(String granularity) {
        switch (granularity) {
            case "day": return "1h";    // 每小时
            case "month": return "1d";  // 每天
            case "year": return "30d";  // 每月
            default: return "1h";
        }
    }

    /**
     * 构建tagname列表
     */
    private List<String> buildTagnames(List<String> moduleIds, List<Integer> parameters) {
        List<String> tagnames = new ArrayList<>();
        for (String moduleId : moduleIds) {
            for (Integer param : parameters) {
                String fieldName = getFieldNameByParam(param);
                String tagname = moduleId.trim().toUpperCase() + "#" + fieldName;
                tagnames.add(tagname);
            }
        }
        return tagnames;
    }

    /**
     * 根据参数编码获取字段名称
     */
    private String getFieldNameByParam(Integer paramCode) {
        switch (paramCode) {
            case 1: return "IA";        // A相电流
            case 2: return "IB";        // B相电流
            case 3: return "IC";        // C相电流
            case 4: return "UA";        // A相电压
            case 5: return "UB";        // B相电压
            case 6: return "UC";        // C相电压
            case 7: return "P";         // 总有功功率 - 修复：从PP改为P
            case 8: return "Q";        // 总无功功率
            case 9: return "S";        // 总视在功率
            case 10: return "PFS";      // 总功率因数
            case 11: return "HZ";       // 频率
            case 12: return "KWH";      // 正向有功总电能
            case 13: return "KVARH";    // 正向无功总电能
            // 其他能源参数
            case 20: return "TEMP";     // 温度
            case 21: return "PRE";    // 压力
            case 22: return "PV";     // 瞬时流量
            case 23: return "SV";    // 累计值
            default: return "VALUE";    // 默认值
        }
    }

    /**
     * 构建OR条件语句
     * 将多个tagname转换为OR条件，例如：tagname = 'YJ0001_13#IA' OR tagname = 'YJ0001_13#PP'
     */
    private String buildOrCondition(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "tagname = 'EMPTY'";
        }
        
        List<String> conditions = values.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(value -> "tagname = '" + value.trim() + "'")
                .collect(Collectors.toList());
        
        return String.join(" OR ", conditions);
    }
}
```

### 3. 时区处理工具类

```java
@Component
public class TimeZoneUtil {

    /**
     * UTC时间转北京时间
     */
    public String convertUTCToBeijing(String utcTimeStr) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime utcTime = LocalDateTime.parse(utcTimeStr, inputFormatter);
            LocalDateTime beijingTime = utcTime.plusHours(8); // UTC+8

            return beijingTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("时间转换失败: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }

    /**
     * 北京时间转UTC时间
     */
    public String convertBeijingToUTC(String beijingTimeStr) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            LocalDateTime beijingTime = LocalDateTime.parse(beijingTimeStr, inputFormatter);
            LocalDateTime utcTime = beijingTime.minusHours(8); // UTC-8

            return utcTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("时间转换失败: {}", beijingTimeStr, e);
            return beijingTimeStr;
        }
    }
}
```

## 🚀 负荷监控开发优先级和步骤

### 第一阶段：基础功能
1. ✅ **维度树接口**（已完成）
2. 🔧 **电力仪表列表接口** - `getModulesByDimension`（含额定功率）
3. 🔧 **负荷时序查询** - `getLoadTimeSeriesData`（日粒度，只查询P字段）
4. 🔧 **负荷率计算逻辑**

### 第二阶段：完善功能
1. 🔧 **月/年粒度负荷查询**
2. 🔧 **实时负荷状态接口** - `getCurrentLoadStatus`
3. 🔧 **时区处理优化**
4. 🔧 **负荷等级判断逻辑**

### 第三阶段：优化功能
1. 🔧 **性能优化**（缓存、分页）
2. 🔧 **异常处理和日志**
3. 🔧 **负荷数据导出功能**
   - 支持导出Excel格式的负荷数据
   - 支持按日/月/年粒度导出
   - 包含功率和负荷率数据
4. 🔧 **负荷预警功能**

## 📝 注意事项

### 1. 时区处理
- ⚠️ **关键**：InfluxDB存储UTC时间，前端显示需要转换为北京时间
- 🕐 查询时：北京时间 → UTC时间
- 🕐 显示时：UTC时间 → 北京时间

### 2. 负荷率计算
- 📊 **公式**：负荷率 = 有功功率 ÷ 额定功率 × 100%
- 🚀 **数据来源**：有功功率来自InfluxDB的P字段，额定功率来自MySQL的rated_power字段
- 📈 **精度控制**：负荷率保留2位小数

### 3. 性能优化
- 📊 **数据量控制**：日查询24个点，月查询30个点，年查询12个点
- 🚀 **缓存策略**：实时负荷数据缓存5分钟，历史数据缓存1小时
- 📈 **分页处理**：表格数据超过1000行时分页显示

### 4. 错误处理
- 🔍 **数据验证**：仪表ID有效性、额定功率不为空、时间格式正确
- 🚨 **异常捕获**：InfluxDB连接异常、数据解析异常、除零异常
- 📝 **日志记录**：查询参数、执行时间、异常信息

### 5. 前端适配
- 📱 **双图表显示**：有功功率图表和负荷率图表分别显示
- 🎨 **图表自适应**：根据仪表数量调整颜色和图例
- ⚡ **加载状态**：查询过程中显示加载动画

### 8. 负荷数据导出接口

#### 接口信息
- **URL**: `/energy/realtime/exportLoadData`
- **Method**: `POST`
- **功能**: 导出负荷数据为Excel文件，支持日/月/年不同时间粒度

#### 请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_1203"],  // 仪表ID列表 (必填)
    "timeGranularity": "day",                    // 时间粒度 (必填: day/month/year)
    "queryDate": "2025-07-25",                   // 查询日期 (必填)
    "fileName": "负荷数据_2025-07-25"             // 导出文件名 (可选，默认为"负荷数据_日期")
}
```

#### 响应数据
- 直接返回Excel文件流，Content-Type为application/vnd.ms-excel
- 文件名格式：负荷数据_2025-07-25.xlsx

#### Excel文件内容
- **Sheet1**: 有功功率数据
  - 第一行：时间点
  - 第一列：仪表名称
  - 数据单位：kW
- **Sheet2**: 负荷率数据
  - 第一行：时间点
  - 第一列：仪表名称
  - 数据单位：%

## 🧪 测试用例

### 1. 接口测试
```bash
# 1. 获取电力仪表列表
curl -X GET "http://localhost:8080/energy/realtime/getModulesByDimension?dimensionCode=A02A02A01&energyType=1&includeChildren=true"

# 2. 获取参数配置
curl -X GET "http://localhost:8080/energy/realtime/getParameterConfig?energyType=1"

# 3. 查询通用时序数据
curl -X POST "http://localhost:8080/energy/realtime/getTimeSeriesData" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_13", "yj0001_14"],
    "parameters": [1, 2, 7],
    "timeGranularity": "day",
    "queryDate": "2025-07-25",
    "startTime": "2025-07-25 00:00:00",
    "endTime": "2025-07-25 23:59:59"
  }'

# 4. 获取实时状态
curl -X POST "http://localhost:8080/energy/realtime/getCurrentStatus" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_13", "yj0001_14"],
    "parameters": [1, 2, 7]
  }'

# 5. 查询负荷时序数据
curl -X POST "http://localhost:8080/energy/realtime/getLoadTimeSeriesData" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_1202", "yj0001_1203"],
    "timeGranularity": "day",
    "queryDate": "2025-07-25"
  }'

# 6. 获取实时负荷状态
curl -X POST "http://localhost:8080/energy/realtime/getCurrentLoadStatus" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_1202", "yj0001_1203"]
  }'

# 7. 获取负荷统计表格数据
curl -X POST "http://localhost:8080/energy/realtime/getLoadTableData" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_1202", "yj0001_1203", "yj0001_1204"],
    "timeType": "day",
    "startTime": "2025-07-25 00:00:00",
    "endTime": "2025-07-25 23:59:59",
    "pageNum": 1,
    "pageSize": 100
  }'

# 8. 导出负荷数据
curl -X POST "http://localhost:8080/energy/realtime/exportLoadData" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleIds": ["yj0001_1202", "yj0001_1203"],
    "timeGranularity": "day",
    "queryDate": "2025-07-25",
    "fileName": "负荷数据_2025-07-25"
  }' \
  --output "负荷数据_2025-07-25.xlsx"
```

### 2. 数据验证
- ✅ **时间正确性**：确保显示的是北京时间
- ✅ **负荷率计算正确性**：验证负荷率 = 功率/额定功率×100%
- ✅ **多仪表支持**：同时显示多个仪表的负荷曲线
- ✅ **双图表显示**：有功功率图表和负荷率图表都正确显示
- ✅ **数据完整性**：包含所有时间点，缺失数据显示为null
- ✅ **统计表格正确性**：验证最大/最小/平均功率及负荷率计算准确
- ✅ **时间点准确性**：最大/最小功率发生时间显示正确
- ✅ **分页功能**：表格数据支持分页显示

## 📋 总结

这个实时数据监控接口文档涵盖了完整的能源监控功能，主要特点：

1. **多能源类型支持**：支持电力、天然气、压缩空气、企业用水等多种能源类型
2. **通用时序查询**：支持多仪表、多参数的时序数据查询和可视化
3. **专业负荷监控**：专门针对电力设备的负荷情况监控，包含有功功率PP和负荷率
4. **双图表展示**：分别显示有功功率曲线和负荷率曲线
5. **多仪表支持**：支持同时监控多个设备的实时状态和历史数据
6. **负荷率计算**：自动计算并显示设备利用率
7. **完整的时间粒度**：支持日/月/年不同时间维度的数据分析
8. **统一的查询语法**：使用OR语法构建InfluxDB查询，保证语法兼容性

### 接口分类
- **基础接口**：仪表列表、参数配置
- **通用监控**：时序数据查询、实时状态获取
- **负荷监控**：负荷时序数据、负荷状态、负荷统计表格

您可以直接使用这个文档来指导开发具体的实时数据监控API接口代码。