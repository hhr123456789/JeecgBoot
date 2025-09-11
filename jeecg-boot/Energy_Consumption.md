# 设备能源统计接口

本接口用于根据"左侧维度树选中仪表集合 + 统计粒度（日/月/年）+ 起止日期"输出设备原始能耗统计数据。

功能说明：
- 统一显示（displayMode=1）：一个图中包含"所选设备"的多条曲线，每条曲线显示该设备的原始能耗量。
- 分开显示（displayMode=2）：按设备分图。选择4个设备则产生4个图，每个图显示该设备的能耗趋势。
- 支持导出（Excel）。

---

## 1. 接口定义
- Method: POST
- URL:
  - 查询：/energy/consumption/statistics
  - 导出：/energy/consumption/statistics/export
- Content-Type: application/json

### 1.1 请求参数 ConsumptionRequest
```json
{
  "moduleIds": ["yj0001_1202", "yj0001_12"],   // 必填，可多选
  "startDate": "2025-08-01",                     // day=yyyy-MM-dd, month=yyyy-MM, year=yyyy
  "endDate": "2025-08-24",                       // 同上；需满足 startDate <= endDate
  "timeType": "day",                              // day | month | year
  "displayMode": 1                                // 1=统一显示; 2=分开显示
}
```

### 1.2 响应参数（目标结构）
- displayMode=1（统一显示：一个图，多条曲线=各设备能耗）
```json
{
  "displayMode": "unified",
  "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
  "series": [
    {"name":"1#空压机-能耗","metric":"energy","unit":"kWh","moduleId":"yj0001_1202","moduleName":"1#空压机","data":[{"x":"2025-08-01","y":1234.5},{"x":"2025-08-02","y":1182.0}]},
    {"name":"2#空压机-能耗","metric":"energy","unit":"kWh","moduleId":"yj0001_12","moduleName":"2#空压机","data":[{"x":"2025-08-01","y":1010.0},{"x":"2025-08-02","y":985.0}]},
    {"name":"冷却水泵-能耗","metric":"energy","unit":"m³","moduleId":"yj0001_13","moduleName":"冷却水泵","data":[{"x":"2025-08-01","y":45.2}]}
  ]
}
```
- displayMode=2（分开显示：按设备分图，每图显示该设备能耗趋势）
```json
{
  "displayMode": "separated",
  "timeRange": "2025-08-01 ~ 2025-08-24 (day)",
  "charts": [
    {
      "moduleId":"yj0001_1202","moduleName":"1#空压机","title":"1#空压机",
      "series":[
        {"name":"能耗","metric":"energy","unit":"kWh","data":[{"x":"2025-08-01","y":1234.5},{"x":"2025-08-02","y":1182.0}]}
      ]
    },
    {
      "moduleId":"yj0001_12","moduleName":"2#空压机","title":"2#空压机",
      "series":[
        {"name":"能耗","metric":"energy","unit":"kWh","data":[{"x":"2025-08-01","y":1010.0},{"x":"2025-08-02","y":985.0}]}
      ]
    }
  ]
}
```
说明：
- series[].unit 为该设备的原始能耗单位（电=kWh，水=m³，压缩空气=m³，天然气=m³）。
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
  - 字段：energy_unit（原始能耗单位）
- 计算公式：
  - 能耗 energy = energy_count（直接取原始值，不进行系数转换）
- 单位对应关系：
  - 电（energy_type=1）：kWh（千瓦时）
  - 水（energy_type=2）：m³（立方米）
  - 压缩空气（energy_type=5）：m³（立方米）
  - 天然气（energy_type=8）：m³（立方米）
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
后端再按 module_id 关联 energy_type，获取对应的原始单位，生成各设备的能耗曲线。

---

## 4. 导出说明（Excel）
导出格式为Excel文件（.xlsx），文件名包含时间戳，如：`设备能源统计_20250910_160000.xlsx`

### 4.1 统一显示模式导出格式
- 单个工作表，包含所有设备的能耗数据
- 表格结构：
  ```
  时间          | 1#空压机-能耗(kWh) | 2#空压机-能耗(kWh) | 冷却水泵-能耗(m³) | ...
  2025-08-01   | 1234.5            | 1010.0            | 45.2             | ...
  2025-08-02   | 1182.0            | 985.0             | 43.8             | ...
  ```
- 列标题格式：`设备名称-能耗(单位)`
- 数据按时间顺序排列，空值显示为"-"

### 4.2 分开显示模式导出格式
- 单个工作表，按设备分区块显示
- 每个设备区块包含：
  - 设备标题行：`设备: 设备名称`
  - 该设备的能耗数据
  - 空行分隔不同设备
- 表格结构：
  ```
  设备: 1#空压机
  时间          | 能耗(kWh)
  2025-08-01   | 1234.5
  2025-08-02   | 1182.0
  
  设备: 冷却水泵
  时间          | 能耗(m³)
  2025-08-01   | 45.2
  ```

### 4.3 Excel样式说明
- 表头：灰色背景，加粗字体，居中对齐
- 数据：居中对齐，数值保留2位小数
- 边框：所有单元格添加细边框
- 列宽：时间列20字符，数据列18字符

---

## 5. 注意事项
- startDate 必须小于等于 endDate；建议后端做校验/自动纠正。
- 必须按 module_id 维度聚合，保持各设备数据的独立性。
- 不同能源类型的设备单位不同，需要根据energy_type正确显示单位。
- 建议在三张统计表建立索引：idx_module_dt(module_id, dt)。
- 时区：若源数据来自 InfluxDB 落库，请确保 dt 已转为业务时区。

---

## 6. 示例请求
```http
POST /energy/consumption/statistics
Content-Type: application/json
{
  "moduleIds": ["yj0001_1202","yj0001_12","yj0001_13","yj0001_14"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-24",
  "timeType": "day",
  "displayMode": 1
}
```

---

## 7. 前端对接要点
- 统一显示：直接渲染 series（长度=设备数）。
- 分开显示：遍历 charts；每个 chart.series 为当前设备的能耗曲线。
- legend 建议展示 name；tooltip 中展示 moduleName + value + unit。
- 注意不同设备的单位可能不同，建议按单位分组显示或使用多Y轴。

---

## 8. 与趋势分析接口的区别
- **数据内容**：只统计原始能耗量，不计算折标煤和碳排放
- **单位显示**：显示各能源类型的原始单位（kWh、m³等）
- **应用场景**：用于查看设备实际消耗的能源数量，便于成本核算和用量管理