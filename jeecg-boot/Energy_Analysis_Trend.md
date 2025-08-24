# 能耗趋势分析接口（折标煤/碳排放）

本接口用于根据“左侧维度树选中仪表集合 + 统计粒度（日/月/年）+ 起止日期”计算右侧趋势图所需的数据：
- 指标：能耗、折标煤、碳排放（可选）
- 支持两种显示模式：统一显示（多指标同图）/ 分开显示（每指标一张图）
- 支持导出（CSV，后续可升级为Excel）

左侧维度树与仪表下拉已实现，本接口仅负责右侧趋势区域的数据查询与导出。

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
  "moduleIds": ["yj0001_1202", "yj0001_12"],  // 必填，可多选
  "startDate": "2025-07",                        // day=yyyy-MM-dd, month=yyyy-MM, year=yyyy
  "endDate": "2025-09",                          // 同上
  "timeType": "month",                           // day | month | year
  "displayMode": 1,                               // 1=统一显示; 2=分开显示
  "metrics": ["standardCoal", "carbon"]         // 可选：energy | standardCoal | carbon
}
```

### 1.2 响应参数
- displayMode=1（统一显示）
```json
{
  "displayMode": "unified",
  "timeRange": "2025-07 ~ 2025-09 (month)",
  "series": [
    {"name":"折标煤","metric":"standardCoal","unit":"kgce","data":[["2025-07",801.548],["2025-08",760.213]]},
    {"name":"碳排放","metric":"carbon","unit":"kgCO2e","data":[["2025-07",167.876],["2025-08",155.112]]}
  ]
}
```
- displayMode=2（分开显示）
```json
{
  "displayMode": "separated",
  "timeRange": "2025-07 ~ 2025-09 (month)",
  "charts": [
    {
      "title":"折标煤","metric":"standardCoal","unit":"kgce",
      "series":[{"name":"合计","moduleId":"-","data":[["2025-07",801.548]]}]
    },
    {
      "title":"碳排放","metric":"carbon","unit":"kgCO2e",
      "series":[{"name":"合计","moduleId":"-","data":[["2025-07",167.876]]}]
    }
  ]
}
```

---

## 2. 统计口径与数据来源
- 仪表信息：tb_module（关联字段：module_id）
- 统计表：
  - 日：tb_ep_equ_energy_daycount
  - 月：tb_ep_equ_energy_monthcount
  - 年：tb_ep_equ_energy_yearcount
- 关联系数表：tb_energy_ratio_info
  - energy_type 字典：1=电，2=水，5=压缩空气，8=天然气
  - 字段：energy_unit, zbmxs_value(折标煤系数), tpfxs_value(碳排放系数)
- 计算公式：
  - 能耗：energy_count
  - 折标煤：energy_count × zbmxs_value
  - 碳排放：energy_count × tpfxs_value
- 时间字段：dt（已标准化为 00:00:00）；闭区间 [startDate, endDate]

---

## 3. SQL 聚合模板（MyBatis XML 已内置）
- 表映射：{day: tb_ep_equ_energy_daycount, month: tb_ep_equ_energy_monthcount, year: tb_ep_equ_energy_yearcount}
- 标签：
  - day：DATE_FORMAT(dt, '%Y-%m-%d')
  - month：DATE_FORMAT(dt, '%Y-%m')
  - year：DATE_FORMAT(dt, '%Y')

示例：
```sql
SELECT {labelExpr} AS label, IFNULL(SUM(energy_count),0) AS energy
FROM {table}
WHERE module_id IN (:moduleIds)
  AND dt BETWEEN :startDt AND :endDt
GROUP BY {labelExpr}
ORDER BY MIN(dt);
```
后端将 energy 转换为标准煤/碳排放。

---

## 4. 导出说明（CSV）
- 请求体与查询一致；返回CSV文件，列包含 label 与各系列值
- 如需xlsx，可替换实现为 EasyPOI/Apache POI

---

## 5. 注意事项
- 左侧维度树与仪表下拉接口已实现，此处仅关心右侧趋势
- energy_type→系数的映射当前通过 tb_energy_ratio_info；默认以“第一只模块的能源类型”推断（可在后续改为根据 module_id 查询 energy_type 或由前端显式传入 energyType）
- 建议在三张统计表建立索引：idx_module_dt(module_id, dt)
- 当 metrics 未传时默认返回 [standardCoal, carbon]

---

## 6. 示例请求
```http
POST /energy/analysis/trend
Content-Type: application/json
{
  "moduleIds": ["yj0001_1202","yj0001_12"],
  "startDate": "2025-07",
  "endDate": "2025-09",
  "timeType": "month",
  "displayMode": 1,
  "metrics": ["standardCoal","carbon"]
}
```

---

## 7. 对接说明（与前端）
- 统一显示：直接将 series 渲染为多折线；Y 轴单位按每条 series 的 unit 显示
- 分开显示：遍历 charts，按 chart.unit 渲染。每个 chart 的 series 可扩展为“每个仪表一条线”

如需将“分开显示”扩展为“每个参数/每个仪表一条曲线”，请告知，我会把后端按 moduleId 维度拆分返回。

