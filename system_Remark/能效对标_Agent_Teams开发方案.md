# 能效对标功能 - Agent Teams 自主开发方案

## 一、项目背景

### 1.1 功能概述
能效对标（Energy Efficiency Benchmarking）是能源管理系统的核心分析功能，用于对比不同生产线、部门、班组的能效指标，找出最优实践和改进空间。

### 1.2 当前状态
- **前端**：`EnergyAnalysis/Energy_Manage_Benchmarking/index.vue` 已有基础框架，数据为硬编码
- **后端**：尚未开发
- **数据库**：需要新建表

### 1.3 参考实现
- 班组用能统计：`EnergyStatistics/Team_Energy/`
- 产品单耗分析：`EnergyAnalysis/Energy_Product_Consumption/`

---

## 二、需求分析

### 2.1 功能需求

| 功能点 | 说明 |
|--------|------|
| 左侧维度树 | 按生产线/部门/班组等维度选择对标对象，需与其他功能统一 |
| 统计卡片 | 显示平均能耗强度、最优能耗强度、方差系数 |
| 趋势图 | 展示本部门与对标部门的能耗趋势对比 |
| 分布表格 | 显示各部门能耗强度、总量、产量明细 |
| 时间筛选 | 支持日/月/年三种时间粒度 |
| 数据导出 | 支持导出对标分析数据 |

### 2.2 对标指标

| 指标 | 计算公式 | 单位 |
|------|----------|------|
| 能耗强度 | 能耗总量 / 产量 | kgce/t 或 kWh/t |
| 平均能耗强度 | Σ能耗强度 / 对象数量 | kgce/t |
| 最优能耗强度 | MIN(能耗强度) | kgce/t |
| 方差系数 | 标准差 / 均值 | 无量纲 |

### 2.3 数据来源
- 能耗数据：`tb_ep_equ_energy_daycount/monthcount/yearcount`
- 产量数据：`tb_product_output`
- 维度数据：`tb_team_dimension_relation`

---

## 三、数据库设计

### 3.1 新建表结构

#### 表1：tb_benchmark_config（对标配置表）
```sql
CREATE TABLE `tb_benchmark_config` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_code` varchar(50) NOT NULL COMMENT '配置编码',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `benchmark_type` int(2) DEFAULT 1 COMMENT '对标类型(1-生产线对标,2-部门对标,3-班组对标,4-行业对标)',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型(1-电,2-水,8-天然气,5-压缩空气,all-全部)',
  `indicator_type` varchar(20) DEFAULT 'intensity' COMMENT '指标类型(intensity-能耗强度,total-能耗总量,cost-费用)',
  `unit` varchar(20) DEFAULT 'kgce/t' COMMENT '单位',
  `baseline_value` decimal(18,4) DEFAULT NULL COMMENT '基准值',
  `target_value` decimal(18,4) DEFAULT NULL COMMENT '目标值',
  `warning_threshold` decimal(18,4) DEFAULT NULL COMMENT '预警阈值',
  `status` int(1) DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_code` (`config_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标配置表';
```

#### 表2：tb_benchmark_target（对标对象表）
```sql
CREATE TABLE `tb_benchmark_target` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_id` varchar(36) NOT NULL COMMENT '配置ID',
  `target_code` varchar(50) NOT NULL COMMENT '对标对象编码(部门/生产线/班组编码)',
  `target_name` varchar(100) NOT NULL COMMENT '对标对象名称',
  `target_type` int(2) DEFAULT 1 COMMENT '对象类型(1-生产线,2-部门,3-班组)',
  `parent_code` varchar(50) DEFAULT NULL COMMENT '父级编码',
  `dimension_type` int(2) DEFAULT 1 COMMENT '维度类型',
  `module_ids` text DEFAULT NULL COMMENT '关联仪表ID列表',
  `sort_order` int(4) DEFAULT 0 COMMENT '排序号',
  `status` int(1) DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_target_code` (`target_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标对象表';
```

#### 表3：tb_benchmark_result_day（对标结果日统计表）
```sql
CREATE TABLE `tb_benchmark_result_day` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_id` varchar(36) NOT NULL COMMENT '配置ID',
  `target_code` varchar(50) NOT NULL COMMENT '对标对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对标对象名称',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(kWh或m³)',
  `energy_consumption_tce` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(tce)',
  `output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度',
  `cost` decimal(18,2) DEFAULT 0 COMMENT '费用',
  `carbon_emission` decimal(18,4) DEFAULT 0 COMMENT '碳排放量',
  `rank_num` int(4) DEFAULT 0 COMMENT '排名',
  `vs_avg_percent` decimal(10,2) DEFAULT 0 COMMENT '与平均值对比百分比',
  `vs_best_percent` decimal(10,2) DEFAULT 0 COMMENT '与最优值对比百分比',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_date` (`target_code`, `stat_date`, `energy_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果日统计表';
```

#### 表4：tb_benchmark_result_month（对标结果月统计表）
```sql
CREATE TABLE `tb_benchmark_result_month` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_id` varchar(36) NOT NULL COMMENT '配置ID',
  `target_code` varchar(50) NOT NULL COMMENT '对标对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对标对象名称',
  `stat_year` int(4) NOT NULL COMMENT '统计年份',
  `stat_month` int(2) NOT NULL COMMENT '统计月份',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(kWh或m³)',
  `energy_consumption_tce` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(tce)',
  `output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度',
  `cost` decimal(18,2) DEFAULT 0 COMMENT '费用',
  `carbon_emission` decimal(18,4) DEFAULT 0 COMMENT '碳排放量',
  `rank_num` int(4) DEFAULT 0 COMMENT '排名',
  `vs_avg_percent` decimal(10,2) DEFAULT 0 COMMENT '与平均值对比百分比',
  `vs_best_percent` decimal(10,2) DEFAULT 0 COMMENT '与最优值对比百分比',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_month` (`target_code`, `stat_year`, `stat_month`, `energy_type`),
  KEY `idx_stat_year_month` (`stat_year`, `stat_month`),
  KEY `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果月统计表';
```

#### 表5：tb_benchmark_result_year（对标结果年统计表）
```sql
CREATE TABLE `tb_benchmark_result_year` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_id` varchar(36) NOT NULL COMMENT '配置ID',
  `target_code` varchar(50) NOT NULL COMMENT '对标对象编码',
  `target_name` varchar(100) DEFAULT NULL COMMENT '对标对象名称',
  `stat_year` int(4) NOT NULL COMMENT '统计年份',
  `energy_type` varchar(20) DEFAULT NULL COMMENT '能源类型',
  `energy_consumption` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(kWh或m³)',
  `energy_consumption_tce` decimal(18,4) DEFAULT 0 COMMENT '能耗总量(tce)',
  `output` decimal(18,4) DEFAULT 0 COMMENT '产量',
  `intensity` decimal(18,6) DEFAULT 0 COMMENT '能耗强度',
  `cost` decimal(18,2) DEFAULT 0 COMMENT '费用',
  `carbon_emission` decimal(18,4) DEFAULT 0 COMMENT '碳排放量',
  `rank_num` int(4) DEFAULT 0 COMMENT '排名',
  `vs_avg_percent` decimal(10,2) DEFAULT 0 COMMENT '与平均值对比百分比',
  `vs_best_percent` decimal(10,2) DEFAULT 0 COMMENT '与最优值对比百分比',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_year` (`target_code`, `stat_year`, `energy_type`),
  KEY `idx_stat_year` (`stat_year`),
  KEY `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标结果年统计表';
```

---

## 四、API 接口设计

### 4.1 接口列表

| 接口 | 方法 | 说明 |
|------|------|------|
| `/energy/benchmark/getDimensionTree` | GET | 获取维度树数据 |
| `/energy/benchmark/getStatistics` | GET | 获取统计卡片数据 |
| `/energy/benchmark/getTrendData` | GET | 获取趋势图数据 |
| `/energy/benchmark/getTableData` | GET | 获取表格数据 |
| `/energy/benchmark/exportData` | GET | 导出数据 |

### 4.2 请求参数

```typescript
interface BenchmarkQueryRequest {
  targetCode: string;       // 对标对象编码
  targetType: number;       // 对象类型(1-生产线,2-部门,3-班组)
  timeUnit: string;         // 时间维度(day/month/year)
  queryDate: string;        // 查询日期
  energyType: string;       // 能源类型
  dimensionType: number;    // 维度类型
}
```

### 4.3 响应数据结构

```typescript
// 统计卡片响应
interface StatisticsResponse {
  avgIntensity: number;     // 平均能耗强度
  bestIntensity: number;    // 最优能耗强度
  coefficient: number;      // 方差系数
  unit: string;             // 单位
}

// 趋势数据响应
interface TrendDataResponse {
  xAxisData: string[];      // X轴数据
  currentData: number[];    // 当前对象数据
  benchmarkData: number[];  // 对标对象数据
  avgData: number[];        // 平均值数据
}

// 表格数据响应
interface TableDataResponse {
  records: BenchmarkRecord[];
  total: number;
}

interface BenchmarkRecord {
  targetCode: string;
  targetName: string;
  intensity: number;
  total: number;
  output: number;
  rank: number;
  vsAvgPercent: number;
}
```

---

## 五、Agent Teams 配置

### 5.1 团队结构

```
┌─────────────────────────────────────────────────────────────┐
│                      Team Lead (协调者)                      │
│  职责：任务分配、进度跟踪、代码审查、Git提交、冲突解决        │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│System Architect│   │Backend Engineer│   │Frontend Engineer│
│  系统设计师    │   │   后端工程师   │   │   前端工程师   │
└───────────────┘   └───────────────┘   └───────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                              ▼
                    ┌───────────────┐
                    │  QA Engineer  │
                    │   测试工程师   │
                    └───────────────┘
```

### 5.2 角色定义

#### Team Lead（团队负责人）
```yaml
name: team-lead
role: 项目协调与版本控制
responsibilities:
  - 分解任务并分配给团队成员
  - 监控开发进度
  - 代码审查与合并
  - Git 版本控制（commit, push, branch）
  - 解决团队冲突
  - 最终验收
tools:
  - 全部工具
```

#### System Architect（系统设计师）
```yaml
name: system-architect
role: 系统设计与架构
responsibilities:
  - 数据库表设计
  - API 接口设计
  - 系统架构设计
  - 技术方案评审
  - 编写设计文档
tools:
  - Read, Glob, Grep
  - Write (仅限文档和SQL)
  - Task (Explore)
```

#### Backend Engineer（后端工程师）
```yaml
name: backend-engineer
role: Java 后端开发
responsibilities:
  - Entity 实体类开发
  - Mapper 接口开发
  - Service 服务层开发
  - Controller 控制器开发
  - 数据库操作
  - 单元测试
tools:
  - Read, Edit, Write
  - Bash (mvn, mysql)
  - Glob, Grep
```

#### Frontend Engineer（前端工程师）
```yaml
name: frontend-engineer
role: Vue3 前端开发
responsibilities:
  - Vue 组件开发
  - API 接口对接
  - 页面样式调整
  - 状态管理
  - 前端测试
tools:
  - Read, Edit, Write
  - Glob, Grep
```

#### QA Engineer（测试工程师）
```yaml
name: qa-engineer
role: 测试与验证
responsibilities:
  - 功能测试
  - API 测试
  - 浏览器自动化测试
  - Bug 报告
  - 回归测试
tools:
  - Read, Bash
  - WebFetch
  - Glob, Grep
```

---

## 六、开发任务分解

### 6.1 任务列表

| ID | 任务 | 负责人 | 依赖 | 优先级 |
|----|------|--------|------|--------|
| T1 | 数据库表设计与创建 | System Architect | - | P0 |
| T2 | 后端 Entity 实体类 | Backend Engineer | T1 | P0 |
| T3 | 后端 Mapper 接口 | Backend Engineer | T2 | P0 |
| T4 | 后端 Service 服务层 | Backend Engineer | T3 | P1 |
| T5 | 后端 Controller 控制器 | Backend Engineer | T4 | P1 |
| T6 | 前端 API 定义 | Frontend Engineer | T5 | P1 |
| T7 | 前端维度树改造 | Frontend Engineer | T6 | P1 |
| T8 | 前端页面对接 | Frontend Engineer | T7 | P2 |
| T9 | 测试数据准备 | QA Engineer | T1 | P1 |
| T10 | API 接口测试 | QA Engineer | T5 | P2 |
| T11 | 前端功能测试 | QA Engineer | T8 | P2 |
| T12 | 集成测试 | QA Engineer | T11 | P3 |

### 6.2 开发流程

```
Phase 1: 设计阶段
├── T1: 数据库表设计与创建
└── 设计评审

Phase 2: 后端开发
├── T2: Entity 实体类
├── T3: Mapper 接口
├── T4: Service 服务层
├── T5: Controller 控制器
└── 后端代码审查

Phase 3: 前端开发
├── T6: API 定义
├── T7: 维度树改造
├── T8: 页面对接
└── 前端代码审查

Phase 4: 测试阶段
├── T9: 测试数据准备
├── T10: API 接口测试
├── T11: 前端功能测试
├── T12: 集成测试
└── Bug 修复

Phase 5: 发布
├── 代码合并
├── Git 提交
└── 版本发布
```

---

## 七、Git 版本控制规范

### 7.1 分支策略
```
master (主分支)
  └── feature/benchmark (功能分支)
        ├── feature/benchmark-db (数据库)
        ├── feature/benchmark-backend (后端)
        └── feature/benchmark-frontend (前端)
```

### 7.2 提交规范
```
[模块] 提交说明

示例：
[能效对标] 新增数据库表结构
[能效对标] 实现后端 Entity 实体类
[能效对标] 实现后端 API 接口
[能效对标] 前端对接后端 API
[能效对标] 修复 xxx 问题
```

### 7.3 代码审查检查点
- [ ] 代码符合项目规范
- [ ] 无明显 Bug
- [ ] 有必要的注释
- [ ] 测试通过
- [ ] 无安全漏洞

---

## 八、数据库连接信息

```yaml
# MySQL 连接信息（从 application-dev.yml 获取）
host: 127.0.0.1
port: 3306
database: jeecg-boot
username: root
password: root

# InfluxDB 连接信息
host: 127.0.0.1
port: 8086
database: energy_data
```

---

## 九、文件路径参考

### 9.1 后端文件
```
jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/
├── controller/
│   └── BenchmarkController.java (新建)
├── entity/
│   └── benchmark/
│       ├── BenchmarkConfig.java (新建)
│       ├── BenchmarkTarget.java (新建)
│       ├── BenchmarkResultDay.java (新建)
│       ├── BenchmarkResultMonth.java (新建)
│       └── BenchmarkResultYear.java (新建)
├── mapper/
│   └── benchmark/
│       ├── BenchmarkConfigMapper.java (新建)
│       ├── BenchmarkTargetMapper.java (新建)
│       └── BenchmarkResultMapper.java (新建)
├── service/
│   ├── IBenchmarkService.java (新建)
│   └── impl/
│       └── BenchmarkServiceImpl.java (新建)
```

### 9.2 前端文件
```
jeecgboot-vue3/src/
├── api/energy/
│   └── benchmark.api.ts (新建)
├── views/EnergyAnalysis/Energy_Manage_Benchmarking/
│   ├── index.vue (修改)
│   ├── benchmark.api.ts (新建)
│   └── components/
│       └── BenchmarkTrend.vue (已有)
```

### 9.3 数据库脚本
```
jeecg-boot/db/
└── benchmark_001_create_tables.sql (新建)
```

---

## 十、验收标准

### 10.1 功能验收
- [ ] 左侧维度树正常显示，与其他功能统一
- [ ] 统计卡片显示正确数据
- [ ] 趋势图正常渲染
- [ ] 表格数据正确显示
- [ ] 时间筛选功能正常
- [ ] 数据导出功能正常

### 10.2 性能验收
- [ ] 页面加载时间 < 3秒
- [ ] API 响应时间 < 1秒
- [ ] 无明显卡顿

### 10.3 代码验收
- [ ] 代码符合项目规范
- [ ] 无 ESLint/编译错误
- [ ] Git 提交规范
- [ ] 有必要的注释

---

## 十一、风险与注意事项

1. **数据一致性**：确保对标数据与其他模块数据一致
2. **性能优化**：大数据量时需要考虑分页和缓存
3. **维度树统一**：需要与班组用能统计的 DimensionTree 组件保持一致
4. **测试数据**：需要准备足够的测试数据验证功能

---

*文档版本：v1.0*
*创建时间：2026-02-17*
*创建者：System Architect Agent*
