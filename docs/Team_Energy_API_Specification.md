# 班组用能管理前端接口规范文档

## 文档说明

本文档定义了班组用能管理模块(Team_Energy)前端所需的所有后端接口规范。所有接口遵循RESTful API设计规范，统一使用JSON格式进行数据交互。

**基础路径**: `/jeecg-boot/energy/team`

**通用响应格式**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {},
  "timestamp": 1642348800000
}
```

---

## 1. 基础数据接口

### 1.1 获取部门树形结构

**接口**: `GET /energy/team/getOrgTree`

**描述**: 获取企业部门树形结构数据，用于部门选择器

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "title": "企业总部",
      "value": "A01",
      "key": "A01",
      "children": [
        {
          "title": "1#车间",
          "value": "A01B03",
          "key": "A01B03",
          "children": [
            {
              "title": "线路1",
              "value": "A01B03C01",
              "key": "A01B03C01"
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 1.2 根据部门获取班组列表

**接口**: `GET /energy/team/listByOrgCode`

**描述**: 根据部门编码获取该部门下的所有班组列表

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| status | string | 否 | 班组状态(1:启用 0:禁用，默认查询启用) |

**示例**: `/energy/team/listByOrgCode?orgCode=A01B03`

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "id": "team_001",
      "teamCode": "A-1",
      "teamName": "A-1班",
      "teamType": "生产班组",
      "shiftType": "早班",
      "workStartTime": "00:00:00",
      "workEndTime": "12:00:00",
      "orgCode": "A01B03",
      "orgName": "1#车间",
      "leaderName": "张三",
      "memberCount": 15,
      "status": "1"
    },
    {
      "id": "team_002",
      "teamCode": "A-2",
      "teamName": "A-2班",
      "teamType": "生产班组",
      "shiftType": "中班",
      "workStartTime": "12:00:00",
      "workEndTime": "18:00:00",
      "orgCode": "A01B03",
      "orgName": "1#车间",
      "leaderName": "李四",
      "memberCount": 12,
      "status": "1"
    }
  ]
}
```

---

## 2. 统计数据接口

### 2.1 获取班组能耗统计汇总

**接口**: `POST /energy/team/getStats`

**描述**: 获取指定条件下的班组能耗统计汇总数据(总能耗、总费用、碳排放、标准煤)

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "teamCode": "all",
  "energyType": "1",
  "timeUnit": "day",
  "startDate": "2026-01-15",
  "endDate": "2026-01-15"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| teamCode | string | 是 | 班组编码(all表示全部班组) |
| energyType | string | 是 | 能源类型(all:综合 1:电 2:水 5:压缩空气 8:天然气) |
| timeUnit | string | 是 | 时间维度(day/month/year) |
| startDate | string | 是 | 开始日期(格式根据timeUnit不同而不同) |
| endDate | string | 是 | 结束日期 |

**响应示例**:
```json
{
  "success": true,
  "result": {
    "totalConsumption": "162.00",
    "totalCost": "129.60",
    "carbonEmission": "161.51",
    "standardCoal": "19.92",
    "peakConsumption": "46.00",
    "flatConsumption": "79.00",
    "valleyConsumption": "37.00",
    "unit": "kWh"
  }
}
```

---

### 2.2 获取班组用能趋势对比数据

**接口**: `POST /energy/team/getTrendData`

**描述**: 获取班组用能趋势对比图表数据，根据时间维度返回不同粒度的数据

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "teamCode": "all",
  "energyType": "1",
  "timeUnit": "day",
  "startDate": "2026-01-15",
  "endDate": "2026-01-15",
  "metric": "consumption"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| teamCode | string | 是 | 班组编码(all表示全部班组) |
| energyType | string | 是 | 能源类型 |
| timeUnit | string | 是 | 时间维度(day:按小时 month:按日 year:按月) |
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| metric | string | 是 | 指标类型(consumption:能耗 cost:费用 carbon:碳排放) |

**响应示例(日维度-按小时)**:
```json
{
  "success": true,
  "result": {
    "xAxis": {
      "type": "category",
      "data": ["00:00", "01:00", "02:00", ..., "23:00"]
    },
    "series": [
      {
        "name": "A-1班",
        "type": "bar",
        "data": [7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
        "color": "#4B7BE5"
      },
      {
        "name": "A-2班",
        "type": "bar",
        "data": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
        "color": "#23C343"
      },
      {
        "name": "B-1班",
        "type": "bar",
        "data": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0],
        "color": "#FF9F40"
      }
    ]
  }
}
```

**响应示例(月维度-按日)**:
```json
{
  "success": true,
  "result": {
    "xAxis": {
      "type": "category",
      "data": ["1日", "2日", "3日", ..., "30日"]
    },
    "series": [
      {
        "name": "A-1班",
        "type": "bar",
        "data": [84.0, 82.5, 86.3, ...],
        "color": "#4B7BE5"
      }
    ]
  }
}
```

---

### 2.3 获取班组用能排名数据

**接口**: `POST /energy/team/getRankingData`

**描述**: 获取班组用能排名数据(前N名)

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "energyType": "1",
  "timeUnit": "day",
  "startDate": "2026-01-15",
  "endDate": "2026-01-15",
  "topN": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| energyType | string | 是 | 能源类型 |
| timeUnit | string | 是 | 时间维度 |
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| topN | int | 否 | 返回前N名(默认10) |

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "teamCode": "B-1",
      "teamName": "B-1班",
      "value": 42.53,
      "unit": "kWh",
      "rank": 1
    },
    {
      "teamCode": "A-1",
      "teamName": "A-1班",
      "value": 41.65,
      "unit": "kWh",
      "rank": 2
    },
    {
      "teamCode": "A-2",
      "teamName": "A-2班",
      "value": 40.15,
      "unit": "kWh",
      "rank": 3
    }
  ]
}
```

---

### 2.4 获取班组用能占比数据

**接口**: `POST /energy/team/getPieData`

**描述**: 获取班组用能占比饼图数据

**请求参数**: 同2.3排名接口

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "teamCode": "A-1",
      "teamName": "A-1班",
      "value": 33.5,
      "percentage": "33.50%"
    },
    {
      "teamCode": "A-2",
      "teamName": "A-2班",
      "value": 32.29,
      "percentage": "32.29%"
    },
    {
      "teamCode": "B-1",
      "teamName": "B-1班",
      "value": 34.21,
      "percentage": "34.21%"
    }
  ]
}
```

---

### 2.5 获取班组用能明细表数据

**接口**: `POST /energy/team/getTableData`

**描述**: 获取班组用能明细表格数据(支持分页)

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "teamCode": "all",
  "energyType": "1",
  "timeUnit": "day",
  "startDate": "2026-01-15",
  "endDate": "2026-01-15",
  "pageNo": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| teamCode | string | 是 | 班组编码 |
| energyType | string | 是 | 能源类型 |
| timeUnit | string | 是 | 时间维度 |
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| pageNo | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |

**响应示例**:
```json
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "day_001",
        "teamCode": "A-1",
        "teamName": "A-1班",
        "shiftType": "早班",
        "statTime": "2026-01-15",
        "consumption": "84.00",
        "cost": "67.20",
        "carbonEmission": "83.75",
        "standardCoal": "10.33",
        "peakConsumption": "20.00",
        "flatConsumption": "40.00",
        "valleyConsumption": "24.00",
        "meterCount": 5
      }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

## 3. 班组管理接口

### 3.1 获取班组详情

**接口**: `GET /energy/team/getTeamDetail`

**描述**: 获取指定班组的详细信息

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| teamCode | string | 是 | 班组编码 |

**示例**: `/energy/team/getTeamDetail?teamCode=A-1`

**响应示例**:
```json
{
  "success": true,
  "result": {
    "id": "team_001",
    "teamCode": "A-1",
    "teamName": "A-1班",
    "teamType": "生产班组",
    "shiftType": "早班",
    "workStartTime": "00:00:00",
    "workEndTime": "12:00:00",
    "orgCode": "A01B03",
    "orgName": "1#车间",
    "leaderName": "张三",
    "memberCount": 15,
    "description": "主要负责夜间生产任务",
    "status": "1",
    "createTime": "2026-01-01 08:00:00",
    "updateTime": "2026-01-15 10:30:00"
  }
}
```

---

### 3.2 获取班组关联的仪表列表

**接口**: `GET /energy/team/getTeamModules`

**描述**: 获取指定班组关联的所有仪表信息

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| teamCode | string | 是 | 班组编码 |
| energyType | string | 否 | 能源类型(筛选特定能源类型的仪表) |

**示例**: `/energy/team/getTeamModules?teamCode=A-1&energyType=1`

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "id": "rel_001",
      "teamCode": "A-1",
      "moduleId": "M001",
      "moduleName": "车间配电柜-1",
      "energyType": 1,
      "energyTypeName": "电能",
      "allocationRatio": 100.0,
      "startDate": "2026-01-01",
      "endDate": null,
      "status": "1"
    }
  ]
}
```

---

## 4. 预警和分析接口

### 4.1 获取班组能耗预警列表

**接口**: `POST /energy/team/getAlarmList`

**描述**: 获取班组能耗预警记录列表

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "teamCode": "all",
  "alarmType": "all",
  "isHandled": "0",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "pageNo": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 否 | 部门编码 |
| teamCode | string | 否 | 班组编码 |
| alarmType | string | 否 | 预警类型(all/budget/abnormal/peak) |
| isHandled | string | 否 | 是否已处理(0:未处理 1:已处理) |
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| pageNo | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |

**响应示例**:
```json
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "alarm_001",
        "teamCode": "A-1",
        "teamName": "A-1班",
        "energyType": 1,
        "energyTypeName": "电能",
        "alarmType": "budget",
        "alarmLevel": "warning",
        "alarmTime": "2026-01-15 14:30:00",
        "statDate": "2026-01-15",
        "alarmValue": "90.00",
        "thresholdValue": "85.00",
        "alarmMessage": "班组能耗已超出预算阈值5.88%",
        "isHandled": "0"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 4.2 获取班组能耗预算对比

**接口**: `POST /energy/team/getBudgetComparison`

**描述**: 获取班组能耗预算与实际消耗对比数据

**请求参数**:
```json
{
  "orgCode": "A01B03",
  "teamCode": "all",
  "energyType": "1",
  "budgetYear": "2026",
  "budgetMonth": "2026-01"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | string | 是 | 部门编码 |
| teamCode | string | 是 | 班组编码 |
| energyType | string | 是 | 能源类型 |
| budgetYear | string | 是 | 预算年度 |
| budgetMonth | string | 否 | 预算月度(查询月度预算时必填) |

**响应示例**:
```json
{
  "success": true,
  "result": [
    {
      "teamCode": "A-1",
      "teamName": "A-1班",
      "budgetConsumption": "2500.00",
      "actualConsumption": "2645.00",
      "completionRate": "105.80",
      "overBudget": "1",
      "variance": "+145.00"
    }
  ]
}
```

---

## 5. 数据导出接口

### 5.1 导出班组用能报表

**接口**: `POST /energy/team/exportReport`

**描述**: 导出班组用能统计报表(Excel格式)

**请求参数**: 同2.5明细表接口参数

**响应**: 返回Excel文件流

**Headers**:
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=team_energy_report_20260115.xlsx
```

---

## 6. 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 重新登录获取token |
| 403 | 无权限 | 联系管理员开通权限 |
| 404 | 接口不存在 | 检查接口路径是否正确 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 10001 | 部门不存在 | 检查部门编码是否正确 |
| 10002 | 班组不存在 | 检查班组编码是否正确 |
| 10003 | 时间范围超出限制 | 缩小查询时间范围 |
| 10004 | 数据查询失败 | 检查数据库连接和SQL语句 |

---

## 7. 数据库表关联关系说明

### 7.1 主要数据来源表

1. **tb_team** - 班组基础信息表
2. **tb_team_module_rel** - 班组仪表关联表
3. **tb_team_energy_daycount** - 班组能源日统计表(包含hour_00~hour_23字段)
4. **tb_team_energy_monthcount** - 班组能源月统计表
5. **tb_team_energy_yearcount** - 班组能源年统计表
6. **tb_energy_ratio_info** - 能源类型配置表(获取能源单位、碳排放系数等)
7. **sys_depart** - 部门表(获取部门树形结构)

### 7.2 SQL查询示例

#### 查询班组日统计汇总(按小时)
```sql
SELECT 
  team_code,
  team_name,
  stat_date,
  total_consumption,
  total_cost,
  carbon_emission,
  standard_coal,
  hour_00, hour_01, hour_02, ..., hour_23
FROM tb_team_energy_daycount
WHERE team_code = 'A-1'
  AND energy_type = 1
  AND stat_date = '2026-01-15'
```

#### 查询班组月统计(按日汇总)
```sql
SELECT 
  DATE(stat_date) as date,
  SUM(total_consumption) as daily_consumption
FROM tb_team_energy_daycount
WHERE team_code IN ('A-1', 'A-2', 'B-1')
  AND energy_type = 1
  AND stat_month = '2026-01'
GROUP BY DATE(stat_date)
ORDER BY date
```

---

## 8. 注意事项

1. **时间维度处理**:
   - `day`: 返回24个小时的数据(hour_00~hour_23)
   - `month`: 返回当月每日的汇总数据
   - `year`: 返回当年每月的汇总数据

2. **能源类型编码**:
   - `1`: 电能(kWh)
   - `2`: 水能(m³)
   - `5`: 压缩空气(m³)
   - `8`: 天然气(m³)
   - `all`: 综合能耗(标准煤tce)

3. **班组筛选**:
   - `teamCode='all'`: 查询所有班组的汇总数据
   - `teamCode='A-1'`: 查询指定班组的数据

4. **数据缓存策略**:
   - 统计汇总数据建议缓存5分钟
   - 趋势图数据建议缓存10分钟
   - 排名数据建议缓存15分钟

5. **性能优化建议**:
   - 大时间范围查询建议使用异步导出
   - 趋势图数据建议使用预聚合表
   - 排名查询建议添加索引: `idx_stat_date`, `idx_team_code`, `idx_energy_type`

---

**文档版本**: v1.0  
**更新日期**: 2026-01-16  
**维护人**: 班组用能管理开发团队
