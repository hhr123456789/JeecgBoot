## 能耗对比分析接口设计说明（能源管理-合理用能）

本接口用于根据“左侧维度树选中仪表集合 + 统计粒度（日/月/年）+ 起止日期”计算右侧所有分析图表需要的数据：
- 尖峰平谷能耗汇总
- 尖峰平谷能耗占比（环图）
- 总能耗趋势（柱状图）
- 尖峰平谷能耗趋势（折线图）

说明：本接口只负责分析数据聚合；维度树与联动仪表下拉的接口已完成。文档遵循 JeecgBoot 后端接口规范（参考 http://help.jeecg.com/java/）。

---

### 一、数据来源与关联
- 仪表信息：tb_module（业务关联字段：module_id）
- 统计表：
  - 日：tb_ep_equ_energy_daycount
  - 月：tb_ep_equ_energy_monthcount
  - 年：tb_ep_equ_energy_yearcount
- 关联字段：module_id
- 时间字段：dt（统计时间，已标准化为：日=当天00:00:00，月=1日00:00:00，年=1月1日00:00:00）
- 能耗字段：
  - cusp_count（尖）、peak_count（峰）、level_count（平）、Valley_count（谷）
  - energy_count（总能耗）
- 注意：模拟数据下 KWH1..4 之和可能不等于 KWH；总能耗以 energy_count 字段为准，尖/峰/平/谷用于分项占比和趋势。

---

### 二、接口定义（建议聚合为单个接口返回所有板块所需数据）
- Method: POST
- URL: /energy/analysis/reasonable
- Content-Type: application/json
- Request Body：
{
  "moduleIds": ["yj0001_1202", "yj0001_12"],  // 必填，可多选
  "startDate": "2025-08-01",                   // 必填，闭区间起点（配合 timeType）
  "endDate":   "2025-08-19",                   // 必填，闭区间终点（配合 timeType）
  "timeType": "day"                            // 必填：day | month | year
}

- Response（统一封装到 Jeecg 的 Result 成功返回体 data 字段）：
{
  "summary": {                                 // 01 汇总
    "cuspCount": 0.0,
    "peakCount": 0.0,
    "levelCount": 0.0,
    "valleyCount": 0.0,
    "totalCount": 0.0
  },
  "ratio": [                                   // 02 占比
    {"name": "尖", "value": 0.0, "percent": 0.0},
    {"name": "峰", "value": 0.0, "percent": 0.0},
    {"name": "平", "value": 0.0, "percent": 0.0},
    {"name": "谷", "value": 0.0, "percent": 0.0}
  ],
  "totalTrend": [                              // 03 总能耗趋势（柱状）
    {"date": "2025-08-01", "energyCount": 0.0}
  ],
  "touTrend": {                                // 04 尖峰平谷趋势（折线）
    "cusp":  [{"x": "1日/1月/2025", "y": 0.0}],
    "peak":  [{"x": "1日/1月/2025", "y": 0.0}],
    "level": [{"x": "1日/1月/2025", "y": 0.0}],
    "valley":[{"x": "1日/1月/2025", "y": 0.0}]
  }
}

---

### 三、参数与时间粒度说明
- timeType=day：
  - 查询表：tb_ep_equ_energy_daycount
  - startDate/endDate：Date（yyyy-MM-dd）。建议后端转为对应 00:00:00 的 dt 闭区间
- timeType=month：
  - 查询表：tb_ep_equ_energy_monthcount
  - startDate/endDate：Date（yyyy-MM），后端转为每月 1 日 00:00:00 的 dt 闭区间
- timeType=year：
  - 查询表：tb_ep_equ_energy_yearcount
  - startDate/endDate：Date（yyyy），后端转为每年 1 月 1 日 00:00:00 的 dt 闭区间
- dt 为闭区间 [startDate, endDate]，并与 module_id in (:moduleIds) 共同过滤。

---

### 四、SQL 统计模板（MySQL）
- 表名按粒度映射：{day: tb_ep_equ_energy_daycount, month: tb_ep_equ_energy_monthcount, year: tb_ep_equ_energy_yearcount}

1) 汇总（01）
SELECT
  IFNULL(SUM(cusp_count),0)   AS cuspCount,
  IFNULL(SUM(peak_count),0)   AS peakCount,
  IFNULL(SUM(level_count),0)  AS levelCount,
  IFNULL(SUM(Valley_count),0) AS valleyCount,
  IFNULL(SUM(energy_count),0) AS totalCount
FROM {table}
WHERE module_id IN (:moduleIds)
  AND dt BETWEEN :startDt AND :endDt;

2) 总能耗趋势（03）
- day：label = DATE_FORMAT(dt, '%Y-%m-%d')
- month：label = DATE_FORMAT(dt, '%Y-%m')
- year：label = DATE_FORMAT(dt, '%Y')

SELECT
  {labelExpr} AS label,
  IFNULL(SUM(energy_count),0) AS energyCount
FROM {table}
WHERE module_id IN (:moduleIds)
  AND dt BETWEEN :startDt AND :endDt
GROUP BY {labelExpr}
ORDER BY MIN(dt);

3) 尖峰平谷趋势（04）
同上，仅将聚合字段替换为 cusp_count / peak_count / level_count / Valley_count；
也可一次性查出四列后在后端拆分为四条序列以减少数据库往返：

SELECT
  {labelExpr} AS label,
  IFNULL(SUM(cusp_count),0)   AS cusp,
  IFNULL(SUM(peak_count),0)   AS peak,
  IFNULL(SUM(level_count),0)  AS level,
  IFNULL(SUM(Valley_count),0) AS valley
FROM {table}
WHERE module_id IN (:moduleIds)
  AND dt BETWEEN :startDt AND :endDt
GROUP BY {labelExpr}
ORDER BY MIN(dt);

4) 占比（02）
- 在后端由汇总结果计算：percent = value / totalCount * 100（totalCount<=0 时 percent=0）。

---

### 五、返回值格式说明（ECharts 直出）
- ratio 与 touTrend 的 name/x 字段已满足 ECharts 饼图/折线图默认读取；
- totalTrend 的 date 建议用于柱状图 X 轴；
- 若前端需要中/英文或单位显示，可在网关层追加国际化与单位转换。

---

### 六、性能与索引
- 强烈建议在三张统计表建立联合索引：idx_module_dt(module_id, dt)
- 使用 SUM 聚合+GROUP BY 一次查询完成；
- 多仪表时 IN 列表长度较大，可考虑：
  - 将 moduleIds 临时写入内存表/临时表后 JOIN；
  - 或按部门维度先换算为 module_id 范围再查询。

---

### 七、边界与异常处理
- 起止日期必填且 startDate <= endDate；
- totalCount=0 或为 NULL 时，环图 percent=0；
- 允许某些日期无数据，趋势序列只返回有数据的日期；是否补零由前端控制（可选在后端补全时间轴空档）；
- 所有 SUM 使用 IFNULL 包装防止 NULL；
- dt 已标准化（00:00:00），确保统计口径一致；
- 时区：与 InfluxDB 对齐，所有 dt 使用数据库本地时区并与统计表标准化时间一致。

---

### 八、示例
请求：
{
  "moduleIds": ["yj0001_1202","yj0001_12"],
  "startDate": "2025-08-01",
  "endDate": "2025-08-19",
  "timeType": "day"
}

响应（节选）：
{
  "summary": {"cuspCount": 3210.0, "peakCount": 4120.0, "levelCount": 2980.0, "valleyCount": 2750.0, "totalCount": 13060.0},
  "ratio": [{"name":"尖","value":3210.0,"percent":24.57}, {"name":"峰","value":4120.0,"percent":31.55}, {"name":"平","value":2980.0,"percent":22.82}, {"name":"谷","value":2750.0,"percent":21.05}],
  "totalTrend": [{"date":"2025-08-01","energyCount":650.0}, {"date":"2025-08-02","energyCount":710.0}],
  "touTrend": {"cusp":[{"x":"1日","y":120.0}], "peak":[{"x":"1日","y":210.0}], "level":[{"x":"1日","y":180.0}], "valley":[{"x":"1日","y":140.0}]}
}

---

### 九、实现要点（与 Jeecg 规范对齐）
- Controller：@PostMapping("/energy/analysis/reasonable")，入参 DTO 进行校验；返回 Result.ok(data)
- Service：按 timeType 选择表名与 labelExpr，一次查询汇总、一次查询趋势（或合并为一次查询并拆分）
- Mapper：编写动态 SQL（MyBatis-Plus/Xml），使用 foreach 处理 moduleIds IN 列表
- 单元测试：构造多仪表+不同粒度的数据快照，校验汇总与趋势正确性；空数据与 total=0 占比为 0 的场景也需覆盖。
