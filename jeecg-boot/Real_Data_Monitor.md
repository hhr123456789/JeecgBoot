# 实时数据监控接口开发文档

## 📋 项目概述

### 功能描述
实时数据监控模块用于展示能源管理系统中各仪表的实时数据，支持按维度树选择仪表，查看多参数的时序数据图表。

### 核心功能
1. **维度关联仪表查询**: 根据选择的维度树节点，获取对应的仪表列表
2. **实时数据查询**: 从InfluxDB获取指定仪表、参数、时间范围的时序数据
3. **数据关联**: 结合MySQL中的仪表信息和维度信息

### 技术架构
- **数据源**: InfluxDB (时序数据) + MySQL (基础信息)
- **查询方式**: 按时间间隔聚合查询
- **返回格式**: 适配前端图表组件的JSON格式

## 🏗️ 数据模型设计

### 前端交互参数

#### 查询参数映射
```javascript
// 参数选择对应值 (来自字典表)
const PARAMETER_MAP = {
    1: "A相电流",     // IA
    2: "B相电流",     // IB
    3: "C相电流",     // IC
    4: "A相电压",     // UA
    5: "B相电压",     // UB
    6: "C相电压",     // UC
    7: "总有功功率",   // PP
    8: "总无功功率",   // QQ
    9: "总视在功率",   // SS
    10: "功率因数",    // PFS
    11: "频率",       // HZ
    12: "正向有功总电能", // KWH
    // ... 更多参数
};

// 查询间隔对应值 (来自字典表)
const INTERVAL_MAP = {
    1: "15分钟",      // 15min
    2: "30分钟",      // 30min
    3: "60分钟",      // 1h
    4: "120分钟"      // 2h
};

// 查询方式对应值 (来自字典表)
const DISPLAY_MODE = {
    1: "统一显示",    // unified
    2: "分开显示"     // separated
};
```

### InfluxDB数据结构
```sql
-- measurement: hist
-- 数据格式示例
time                    tagname                 value    status
2025-07-15T10:00:00Z   yj0001_1202#IA         12.5     1
2025-07-15T10:00:00Z   yj0001_1202#UA         220.0    1
2025-07-15T10:00:00Z   yj0001_12#IA           8.3      1
```

### MySQL关联表结构
```sql
-- 仪表基础信息
tb_module: module_id, module_name, sys_org_code, isaction
-- 部门维度信息
sys_depart: org_code, depart_name, parent_id
```

## 🔧 接口设计

### 1. 根据维度获取仪表列表接口

#### 接口信息
- **URL**: `/energy/monitor/getModulesByOrgCode`
- **Method**: `GET`
- **功能**: 根据维度编码获取该维度下的所有启用仪表

#### 请求参数
```json
{
    "orgCode": "A02A02A01",  // 维度编码 (必填)
    "includeChildren": true   // 是否包含子维度 (可选，默认true)
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
            "orgCode": "A02A02A01A01",
            "departName": "1号注塑机",
            "energyType": 1,
            "isAction": "Y"
        },
        {
            "moduleId": "yj0001_12",
            "moduleName": "2号注塑机",
            "orgCode": "A02A02A01A02",
            "departName": "2号注塑机",
            "energyType": 1,
            "isAction": "Y"
        }
    ]
}
```

#### 业务逻辑
```java
@GetMapping("/getModulesByOrgCode")
public Result<List<ModuleVO>> getModulesByOrgCode(
    @RequestParam String orgCode,
    @RequestParam(defaultValue = "true") Boolean includeChildren) {

    // 1. 根据orgCode查询维度信息
    // 2. 如果includeChildren=true，查询所有子维度
    // 3. 查询这些维度下的所有启用仪表
    // 4. 关联查询仪表基础信息和维度名称
    // 5. 返回仪表列表
}
```

### 2. 实时数据查询接口

#### 接口信息
- **URL**: `/energy/monitor/getRealTimeData`
- **Method**: `POST`
- **功能**: 查询指定仪表、参数、时间范围的实时数据

#### 请求参数
```json
{
    "moduleIds": ["yj0001_1202", "yj0001_12"],  // 仪表编号列表 (必填)
    "parameters": [1, 4, 7],                    // 参数编号列表 (必填) 1=A相电流,4=A相电压,7=总有功功率
    "startTime": "2025-07-15 08:00:00",         // 开始时间 (必填)
    "endTime": "2025-07-15 16:00:00",           // 结束时间 (必填)
    "interval": 1,                              // 查询间隔 (必填) 1=15分钟,2=30分钟,3=60分钟,4=120分钟
    "displayMode": 1                            // 显示方式 (必填) 1=统一显示,2=分开显示
}
```

#### 响应数据结构

##### 统一显示模式 (displayMode=1)
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
                    ["2025-07-15 08:15:00", 12.8],
                    ["2025-07-15 08:30:00", 13.1],
                    // ... 更多数据点
                ]
            },
            {
                "name": "1号注塑机-A相电压",
                "moduleId": "yj0001_1202",
                "moduleName": "1号注塑机",
                "parameter": "A相电压",
                "parameterCode": "UA",
                "unit": "V",
                "data": [
                    ["2025-07-15 08:00:00", 220.0],
                    ["2025-07-15 08:15:00", 221.5],
                    ["2025-07-15 08:30:00", 219.8],
                    // ... 更多数据点
                ]
            }
            // ... 更多系列
        ]
    }
}
```

##### 分开显示模式 (displayMode=2)
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
                            ["2025-07-15 08:15:00", 12.8],
                            // ... 更多数据点
                        ]
                    },
                    {
                        "name": "2号注塑机",
                        "moduleId": "yj0001_12",
                        "data": [
                            ["2025-07-15 08:00:00", 8.3],
                            ["2025-07-15 08:15:00", 8.7],
                            // ... 更多数据点
                        ]
                    }
                ]
            },
            {
                "title": "A相电压",
                "parameter": "A相电压",
                "parameterCode": "UA",
                "unit": "V",
                "series": [
                    // ... 类似结构
                ]
            }
            // ... 更多图表
        ]
    }
}
```

## 💻 核心实现逻辑

### 1. 参数映射配置

#### 参数字典配置
```java
@Component
public class ParameterConfig {

    // 参数编号到InfluxDB字段的映射
    private static final Map<Integer, ParameterInfo> PARAMETER_MAP = new HashMap<>();

    static {
        PARAMETER_MAP.put(1, new ParameterInfo("IA", "A相电流", "A"));
        PARAMETER_MAP.put(2, new ParameterInfo("IB", "B相电流", "A"));
        PARAMETER_MAP.put(3, new ParameterInfo("IC", "C相电流", "A"));
        PARAMETER_MAP.put(4, new ParameterInfo("UA", "A相电压", "V"));
        PARAMETER_MAP.put(5, new ParameterInfo("UB", "B相电压", "V"));
        PARAMETER_MAP.put(6, new ParameterInfo("UC", "C相电压", "V"));
        PARAMETER_MAP.put(7, new ParameterInfo("PP", "总有功功率", "kW"));
        PARAMETER_MAP.put(8, new ParameterInfo("QQ", "总无功功率", "kVar"));
        PARAMETER_MAP.put(9, new ParameterInfo("SS", "总视在功率", "kVA"));
        PARAMETER_MAP.put(10, new ParameterInfo("PFS", "功率因数", ""));
        PARAMETER_MAP.put(11, new ParameterInfo("HZ", "频率", "Hz"));
        PARAMETER_MAP.put(12, new ParameterInfo("KWH", "正向有功总电能", "kWh"));
        // ... 更多参数映射
    }

    public static ParameterInfo getParameterInfo(Integer paramCode) {
        return PARAMETER_MAP.get(paramCode);
    }

    @Data
    @AllArgsConstructor
    public static class ParameterInfo {
        private String fieldName;    // InfluxDB字段名
        private String displayName;  // 显示名称
        private String unit;         // 单位
    }
}
```

#### 时间间隔配置
```java
@Component
public class IntervalConfig {

    private static final Map<Integer, String> INTERVAL_MAP = new HashMap<>();

    static {
        INTERVAL_MAP.put(1, "15m");   // 15分钟
        INTERVAL_MAP.put(2, "30m");   // 30分钟
        INTERVAL_MAP.put(3, "1h");    // 1小时
        INTERVAL_MAP.put(4, "2h");    // 2小时
    }

    public static String getInfluxInterval(Integer intervalCode) {
        return INTERVAL_MAP.get(intervalCode);
    }
}
```

### 2. InfluxDB查询逻辑

#### InfluxDB查询语句构建
```java
@Service
public class InfluxDBQueryService {

    /**
     * 构建实时数据查询语句
     */
    public String buildRealTimeDataQuery(List<String> moduleIds,
                                       List<Integer> parameters,
                                       String startTime,
                                       String endTime,
                                       Integer interval) {

        // 1. 构建tagname条件 (module_id#parameter格式)
        List<String> tagConditions = new ArrayList<>();
        for (String moduleId : moduleIds) {
            for (Integer param : parameters) {
                ParameterConfig.ParameterInfo paramInfo = ParameterConfig.getParameterInfo(param);
                if (paramInfo != null) {
                    tagConditions.add(String.format("'%s#%s'", moduleId, paramInfo.getFieldName()));
                }
            }
        }

        // 2. 构建时间聚合间隔
        String influxInterval = IntervalConfig.getInfluxInterval(interval);

        // 3. 构建完整查询语句
        String query = String.format(
            "SELECT mean(value) as value " +
            "FROM hist " +
            "WHERE time >= '%s' AND time <= '%s' " +
            "AND tagname IN (%s) " +
            "AND status = 1 " +
            "GROUP BY time(%s), tagname " +
            "ORDER BY time ASC",
            convertToUTC(startTime),    // 转换为UTC时间
            convertToUTC(endTime),      // 转换为UTC时间
            String.join(",", tagConditions),
            influxInterval
        );

        return query;
    }

    /**
     * 时间转换：本地时间 -> UTC时间
     */
    private String convertToUTC(String localTime) {
        // 东八区转UTC：减8小时
        LocalDateTime local = LocalDateTime.parse(localTime,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ZonedDateTime utc = local.atZone(ZoneId.of("Asia/Shanghai"))
                                .withZoneSameInstant(ZoneOffset.UTC);
        return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
```

### 3. 数据处理与格式化

#### 数据转换服务
```java
@Service
public class DataFormatService {

    /**
     * 格式化统一显示模式数据
     */
    public UnifiedDisplayResult formatUnifiedDisplay(
            List<InfluxDBResult> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval) {

        UnifiedDisplayResult result = new UnifiedDisplayResult();
        result.setDisplayMode("unified");
        result.setTimeRange(buildTimeRange(startTime, endTime, interval));

        List<SeriesData> seriesList = new ArrayList<>();

        // 按module_id和parameter分组处理数据
        Map<String, List<InfluxDBResult>> groupedData = influxResults.stream()
            .collect(Collectors.groupingBy(r -> r.getTagname()));

        for (Map.Entry<String, List<InfluxDBResult>> entry : groupedData.entrySet()) {
            String tagname = entry.getKey();  // 格式: yj0001_1202#IA
            String[] parts = tagname.split("#");
            String moduleId = parts[0];
            String paramField = parts[1];

            // 查找参数信息
            ParameterConfig.ParameterInfo paramInfo = findParameterByField(paramField);
            if (paramInfo == null) continue;

            SeriesData series = new SeriesData();
            series.setName(moduleNameMap.get(moduleId) + "-" + paramInfo.getDisplayName());
            series.setModuleId(moduleId);
            series.setModuleName(moduleNameMap.get(moduleId));
            series.setParameter(paramInfo.getDisplayName());
            series.setParameterCode(paramInfo.getFieldName());
            series.setUnit(paramInfo.getUnit());

            // 转换数据点 (UTC时间转本地时间)
            List<Object[]> dataPoints = entry.getValue().stream()
                .map(r -> new Object[]{
                    convertToLocalTime(r.getTime()),
                    r.getValue()
                })
                .collect(Collectors.toList());
            series.setData(dataPoints);

            seriesList.add(series);
        }

        result.setSeries(seriesList);
        return result;
    }

    /**
     * 格式化分开显示模式数据
     */
    public SeparatedDisplayResult formatSeparatedDisplay(
            List<InfluxDBResult> influxResults,
            Map<String, String> moduleNameMap,
            List<Integer> parameters,
            String startTime, String endTime, Integer interval) {

        SeparatedDisplayResult result = new SeparatedDisplayResult();
        result.setDisplayMode("separated");
        result.setTimeRange(buildTimeRange(startTime, endTime, interval));

        List<ChartData> chartsList = new ArrayList<>();

        // 按参数分组
        Map<String, List<InfluxDBResult>> paramGroupedData = influxResults.stream()
            .collect(Collectors.groupingBy(r -> r.getTagname().split("#")[1]));

        for (String paramField : paramGroupedData.keySet()) {
            ParameterConfig.ParameterInfo paramInfo = findParameterByField(paramField);
            if (paramInfo == null) continue;

            ChartData chart = new ChartData();
            chart.setTitle(paramInfo.getDisplayName());
            chart.setParameter(paramInfo.getDisplayName());
            chart.setParameterCode(paramInfo.getFieldName());
            chart.setUnit(paramInfo.getUnit());

            // 按仪表分组该参数的数据
            List<SeriesData> seriesList = new ArrayList<>();
            Map<String, List<InfluxDBResult>> moduleGroupedData =
                paramGroupedData.get(paramField).stream()
                    .collect(Collectors.groupingBy(r -> r.getTagname().split("#")[0]));

            for (Map.Entry<String, List<InfluxDBResult>> moduleEntry : moduleGroupedData.entrySet()) {
                String moduleId = moduleEntry.getKey();

                SeriesData series = new SeriesData();
                series.setName(moduleNameMap.get(moduleId));
                series.setModuleId(moduleId);

                List<Object[]> dataPoints = moduleEntry.getValue().stream()
                    .map(r -> new Object[]{
                        convertToLocalTime(r.getTime()),
                        r.getValue()
                    })
                    .collect(Collectors.toList());
                series.setData(dataPoints);

                seriesList.add(series);
            }

            chart.setSeries(seriesList);
            chartsList.add(chart);
        }

        result.setCharts(chartsList);
        return result;
    }

    /**
     * UTC时间转本地时间
     */
    private String convertToLocalTime(String utcTime) {
        Instant instant = Instant.parse(utcTime);
        ZonedDateTime local = instant.atZone(ZoneId.of("Asia/Shanghai"));
        return local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
```

## 🗄️ 数据库查询SQL

### 1. 根据维度查询仪表SQL
```sql
-- 查询指定维度及其子维度下的所有启用仪表
SELECT
    m.module_id,
    m.module_name,
    m.sys_org_code as org_code,
    d.depart_name,
    m.energy_type,
    m.isaction
FROM tb_module m
LEFT JOIN sys_depart d ON m.sys_org_code = d.org_code
WHERE m.isaction = 'Y'
  AND (
    m.sys_org_code = #{orgCode}
    OR m.sys_org_code LIKE CONCAT(#{orgCode}, '%')
  )
ORDER BY d.depart_order, m.module_name
```

### 2. 批量查询仪表名称SQL
```sql
-- 根据仪表ID列表查询仪表名称
SELECT
    module_id,
    module_name
FROM tb_module
WHERE module_id IN
<foreach collection="moduleIds" item="moduleId" open="(" close=")" separator=",">
    #{moduleId}
</foreach>
```

## 🎯 关键技术要点

### 1. 时区处理
- **InfluxDB存储**: UTC时间
- **MySQL存储**: 本地时间 (东八区)
- **前端显示**: 本地时间
- **转换规则**: 查询时本地时间转UTC，返回时UTC转本地时间

### 2. 数据聚合策略
- **15分钟间隔**: `GROUP BY time(15m)`
- **30分钟间隔**: `GROUP BY time(30m)`
- **1小时间隔**: `GROUP BY time(1h)`
- **2小时间隔**: `GROUP BY time(2h)`
- **聚合函数**: 使用 `mean(value)` 计算平均值

### 3. 性能优化
- **索引优化**: InfluxDB按时间和tagname建立索引
- **查询限制**: 限制时间范围，避免大数据量查询
- **缓存策略**: 仪表名称等基础信息可缓存
- **分页处理**: 大量数据点时考虑分页返回

### 4. 异常处理
- **数据缺失**: 当某时间点无数据时，返回null值
- **仪表离线**: status字段过滤，只返回status=1的数据
- **参数不存在**: 跳过不存在的参数，不影响其他参数查询
- **时间格式**: 严格校验时间格式，防止SQL注入

## 📝 接口调用示例

### 示例1: 查询注塑部门下的仪表
```bash
GET /energy/monitor/getModulesByOrgCode?orgCode=A02A02A01&includeChildren=true
```

### 示例2: 查询实时数据 - 统一显示
```bash
POST /energy/monitor/getRealTimeData
Content-Type: application/json

{
    "moduleIds": ["yj0001_1202", "yj0001_12"],
    "parameters": [1, 4, 7],
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00",
    "interval": 1,
    "displayMode": 1
}
```

### 示例3: 查询实时数据 - 分开显示
```bash
POST /energy/monitor/getRealTimeData
Content-Type: application/json

{
    "moduleIds": ["yj0001_1202", "yj0001_12", "yj0001_13"],
    "parameters": [1, 2, 3],
    "startTime": "2025-07-15 08:00:00",
    "endTime": "2025-07-15 16:00:00",
    "interval": 2,
    "displayMode": 2
}
```

## 🔍 测试用例

### 1. 功能测试
- ✅ 维度树选择后正确显示对应仪表
- ✅ 多仪表多参数数据查询正常
- ✅ 统一显示和分开显示模式切换正常
- ✅ 不同时间间隔聚合数据正确
- ✅ 时区转换准确

### 2. 边界测试
- ✅ 空维度查询返回空列表
- ✅ 不存在的仪表ID处理
- ✅ 无效的参数编号处理
- ✅ 超大时间范围查询限制
- ✅ 时间格式错误处理

### 3. 性能测试
- ✅ 大量仪表查询响应时间 < 3秒
- ✅ 长时间范围数据查询优化
- ✅ 并发查询稳定性测试

---

## 📋 开发检查清单

- [ ] 创建Controller类和方法
- [ ] 实现Service业务逻辑
- [ ] 配置InfluxDB连接和查询
- [ ] 实现MySQL关联查询
- [ ] 添加参数映射配置
- [ ] 实现时区转换逻辑
- [ ] 添加数据格式化处理
- [ ] 实现异常处理机制
- [ ] 编写单元测试
- [ ] 进行接口测试
- [ ] 性能优化调整
- [ ] 文档更新完善

**注意事项**:
1. 确保InfluxDB连接配置正确
2. 注意时区转换的准确性
3. 参数映射要与字典表保持一致
4. 数据格式要适配前端图表组件
5. 添加适当的日志记录和监控