# 设备能源统计API测试说明

## API接口地址
- 查询：`POST /energy/consumption/statistics`
- 导出：`POST /energy/consumption/statistics/export`

## 测试用例

### 1. 统一显示模式测试
```http
POST /energy/consumption/statistics
Content-Type: application/json

{
  "moduleIds": ["yj0001_1202", "yj0001_12", "yj0001_13"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 1
}
```

**预期响应：**
```json
{
  "success": true,
  "result": {
    "displayMode": "unified",
    "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
    "series": [
      {
        "name": "1#空压机-能耗",
        "metric": "energy",
        "unit": "kWh",
        "moduleId": "yj0001_1202",
        "moduleName": "1#空压机",
        "data": [
          {"x": "2025-08-01", "y": 1234.5},
          {"x": "2025-08-02", "y": 1182.0}
        ]
      },
      {
        "name": "2#空压机-能耗",
        "metric": "energy",
        "unit": "kWh",
        "moduleId": "yj0001_12",
        "moduleName": "2#空压机",
        "data": [
          {"x": "2025-08-01", "y": 1010.0},
          {"x": "2025-08-02", "y": 985.0}
        ]
      }
    ]
  }
}
```

### 2. 分开显示模式测试
```http
POST /energy/consumption/statistics
Content-Type: application/json

{
  "moduleIds": ["yj0001_1202", "yj0001_12"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 2
}
```

**预期响应：**
```json
{
  "success": true,
  "result": {
    "displayMode": "separated",
    "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
    "charts": [
      {
        "moduleId": "yj0001_1202",
        "moduleName": "1#空压机",
        "title": "1#空压机",
        "series": [
          {
            "name": "能耗",
            "metric": "energy",
            "unit": "kWh",
            "moduleId": "yj0001_1202",
            "data": [
              {"x": "2025-08-01", "y": 1234.5},
              {"x": "2025-08-02", "y": 1182.0}
            ]
          }
        ]
      },
      {
        "moduleId": "yj0001_12",
        "moduleName": "2#空压机",
        "title": "2#空压机",
        "series": [
          {
            "name": "能耗",
            "metric": "energy",
            "unit": "kWh",
            "moduleId": "yj0001_12",
            "data": [
              {"x": "2025-08-01", "y": 1010.0},
              {"x": "2025-08-02", "y": 985.0}
            ]
          }
        ]
      }
    ]
  }
}
```

### 3. 月度统计测试
```http
POST /energy/consumption/statistics
Content-Type: application/json

{
  "moduleIds": ["yj0001_1202"],
  "startDate": "2025-01",
  "endDate": "2025-08",
  "timeType": "month",
  "displayMode": 1
}
```

### 4. Excel导出测试
```http
POST /energy/consumption/statistics/export
Content-Type: application/json

{
  "moduleIds": ["yj0001_1202", "yj0001_12"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 1
}
```

**预期结果：**
- 下载Excel文件：`设备能源统计_20250910_160000.xlsx`
- 包含时间列和各设备能耗列
- 数据格式正确，有表头和边框

## 数据库依赖
确保以下表有数据：
- `tb_module` - 设备信息
- `tb_ep_equ_energy_daycount` - 日统计数据
- `tb_ep_equ_energy_monthcount` - 月统计数据
- `tb_ep_equ_energy_yearcount` - 年统计数据
- `tb_energy_ratio_info` - 能源单位信息

## 错误处理测试

### 1. 参数验证错误
```http
POST /energy/consumption/statistics
Content-Type: application/json

{
  "moduleIds": [],
  "startDate": "",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 1
}
```

**预期响应：**
```json
{
  "success": false,
  "message": "moduleIds不能为空"
}
```

### 2. 时间范围错误
```http
POST /energy/consumption/statistics
Content-Type: application/json

{
  "moduleIds": ["yj0001_1202"],
  "startDate": "2025-08-24",
  "endDate": "2025-08-01",
  "timeType": "day",
  "displayMode": 1
}
```

**预期响应：**
```json
{
  "success": false,
  "message": "开始时间不能大于结束时间"
}
```

## 与趋势分析API的对比

| 特性 | 设备能源统计 | 趋势分析 |
|------|-------------|----------|
| 接口路径 | `/energy/consumption/statistics` | `/energy/analysis/trend` |
| 数据内容 | 原始能耗量 | 折标煤+碳排放 |
| 单位显示 | kWh, m³等原始单位 | kgce, kgCO2e |
| 应用场景 | 成本核算、用量管理 | 环保分析、节能评估 |
| 计算方式 | 直接取energy_count | 需要乘以转换系数 |