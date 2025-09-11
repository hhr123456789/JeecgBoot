# 能耗趋势分析接口（折标煤/碳排放）

本接口用于根据"左侧维度树选中仪表集合 + 统计粒度（日/月/年）+ 起止日期"输出趋势图数据。

新版需求明确：
- 统一显示（displayMode=1）：一个图中包含"所选设备 × 所选指标"的多条曲线。例如选择4个设备、2个指标（折标煤/碳排），则应有8条曲线。
- 分开显示（displayMode=2）：按设备分图。选择4个设备则产生4个图，每个图里包含"所选指标"多条曲线（如折标煤与碳排两条）。
- 支持导出（Excel）。

---

## 1. 接口定义
- Method: POST
- URL:
  - 查询：/energy/analysis/trend
  - 导出：/energy/analysis/trend/export
- Content-Type: application/json

### 1.1 请求参数 TrendRequest
```json
{
  "moduleIds": ["yj0001_1202", "yj0001_12"],   // 必填，可多选
  "startDate": "2025-08-01",                     // day=yyyy-MM-dd, month=yyyy-MM, year=yyyy
  "endDate": "2025-08-24",                       // 同上；需满足 startDate <= endDate
  "timeType": "day",                              // day | month | year
  "displayMode": 1,                                // 1=统一显示; 2=分开显示
  "metrics": ["standardCoal", "carbon"]          // 可选：energy | standardCoal | carbon；不传默认[standardCoal,carbon]
}
```

### 1.2 响应参数（目标结构）
- displayMode=1（统一显示：一个图，多条曲线=设备×指标）
```json
{
  "displayMode": "unified",
  "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
  "series": [
    {"name":"1#空压机-折标煤","metric":"standardCoal","unit":"kgce","moduleId":"yj0001_1202","moduleName":"1#空压机","data":[{"x":"2025-08-01","y":123.45},{"x":"2025-08-02","y":118.20}]},
    {"name":"1#空压机-碳排放","metric":"carbon","unit":"kgCO2e","moduleId":"yj0001_1202","moduleName":"1#空压机","data":[{"x":"2025-08-01","y":26.77}]},
    {"name":"2#空压机-折标煤","metric":"standardCoal","unit":"kgce","moduleId":"yj0001_12","moduleName":"2#空压机","data":[{"x":"2025-08-01","y":101.00}]}
  ]
}
```
- displayMode=2（分开显示：按设备分图，每图含所选指标多曲线）
```json
{
  "displayMode": "separated",
  "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
  "charts": [
    {
      "moduleId":"yj0001_1202","moduleName":"1#空压机","title":"1#空压机",
      "series":[
        {"name":"折标煤","metric":"standardCoal","unit":"kgce","data":[{"x":"2025-08-01","y":123.45}]},
        {"name":"碳排放","metric":"carbon","unit":"kgCO2e","data":[{"x":"2025-08-01","y":26.77}]}
      ]
    },
    {
      "moduleId":"yj0001_12","moduleName":"2#空压机","title":"2#空压机",
      "series":[
        {"name":"折标煤","metric":"standardCoal","unit":"kgce","data":[{"x":"2025-08-01","y":101.00}]},
        {"name":"碳排放","metric":"carbon","unit":"kgCO2e","data":[{"x":"2025-08-01","y":22.10}]}
      ]
    }
  ]
}
```
说明：
- series[].unit 为该曲线单位；前端可用单轴或双轴展示。
- data 点位采用对象形式：{"x":label, "y":value}。

---

## 2. 统计口径与数据来源
- 仪表信息：tb_module（module_id, module_name, energy_type）
- 统计表：
  - 日：tb_ep_equ_energy_daycount
  - 月：tb_ep_equ_energy_monthcount
  - 年：tb_ep_equ_energy_yearcount
- 关联系数表：tb_energy_ratio_info
  - energy_type：1=电，2=水，5=压缩空气，8=天然气
  - 字段：energy_unit, zbmxs_value(折标煤系数), tpfxs_value(碳排放系数)
- 计算公式：
  - 能耗 energy = energy_count
  - 折标煤 standardCoal = energy × zbmxs_value
  - 碳排 carbon = energy × tpfxs_value
- 时间字段：dt（已标准化为 00:00:00）；闭区间 [startDate, endDate]

---

## 3. SQL 聚合口径（需按"时间标签 + 设备"聚合）
- 表映射：{day: tb_ep_equ_energy_daycount, month: tb_ep_equ_energy_monthcount, year: tb_ep_equ_energy_yearcount}
- 标签：
  - day：DATE_FORMAT(dt, '%Y-%m-%d')
  - month：DATE_FORMAT(dt, '%Y-%m')
  - year：DATE_FORMAT(dt, '%Y')

示例（伪）：
```sql
SELECT {labelExpr} AS label, module_id, IFNULL(SUM(energy_count),0) AS energy
FROM {table}
WHERE module_id IN (:moduleIds)
  AND dt BETWEEN :startDt AND :endDt
GROUP BY {labelExpr}, module_id
ORDER BY MIN(dt), module_id;
```
后端再按 module_id 关联 energy_type，套用系数，生成"设备×指标"的曲线。

---

## 4. 导出说明（Excel）
导出格式为Excel文件（.xlsx），文件名包含时间戳，如：`趋势分析导出_20250910_160000.xlsx`

### 4.1 统一显示模式导出格式
- 单个工作表，包含所有设备和指标的数据
- 表格结构：
  ```
  时间          | 设备1-指标1(单位) | 设备1-指标2(单位) | 设备2-指标1(单位) | ...
  2025-08-01   | 123.45           | 26.77            | 101.00           | ...
  2025-08-02   | 118.20           | 25.60            | 98.50            | ...
  ```
- 列标题格式：`设备名称-指标名称(单位)`
- 数据按时间顺序排列，空值显示为"-"

### 4.2 分开显示模式导出格式
- 单个工作表，按设备分区块显示
- 每个设备区块包含：
  - 设备标题行：`设备: 设备名称`
  - 该设备的所有指标数据
  - 空行分隔不同设备
- 表格结构：
  ```
  设备: 1#空压机
  时间          | 折标煤(kgce) | 碳排放(kgCO2e)
  2025-08-01   | 123.45       | 26.77
  2025-08-02   | 118.20       | 25.60
  
  设备: 2#空压机
  时间          | 折标煤(kgce) | 碳排放(kgCO2e)
  2025-08-01   | 101.00       | 22.10
  ```

### 4.3 Excel样式说明
- 表头：灰色背景，加粗字体，居中对齐
- 数据：居中对齐，数值保留2位小数
- 边框：所有单元格添加细边框
- 列宽：时间列20字符，数据列18字符

---

## 5. 注意事项
- startDate 必须小于等于 endDate；建议后端做校验/自动纠正。
- 必须按 module_id 维度聚合并各自套系数，避免混合能源类型时的误差。
- 建议在三张统计表建立索引：idx_module_dt(module_id, dt)。
- 时区：若源数据来自 InfluxDB 落库，请确保 dt 已转为业务时区。

---

## 6. 示例请求
```http
POST /energy/analysis/trend
Content-Type: application/json
{
  "moduleIds": ["yj0001_1202","yj0001_12","yj0001_13","yj0001_14"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 1,
  "metrics": ["standardCoal","carbon"]
}
```

---

## 7. 前端对接要点
- 统一显示：直接渲染 series（长度=设备数×指标数）。
- 分开显示：遍历 charts；每个 chart.series 为当前设备的多指标曲线。
- legend 建议展示 name；tooltip 中展示 moduleName + metric + value + unit。