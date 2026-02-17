# 能源管理系统 - 工序能耗分析功能开发任务

## 项目背景

基于 JeecgBoot 3.7.2 的能源管理系统，采用 Spring Boot 2.7.18 + Vue3 + InfluxDB 1.8 + MySQL 架构。

**当前任务**：开发工序能耗分析功能
- 前端静态页面已存在：`jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Process_Consumption/`
- 需要设计数据库表、开发后端 API、前端对接
- 左侧需要改为维度树组件，与其他功能统一（参考 Energy_Classification 和 Team_Energy）

## 系统信息

### 环境配置
- **数据库**: 127.0.0.1:3306, 数据库名: emsproject_jeecg
  - 用户名: root
  - 密码: Abc123456@
- **InfluxDB**: localhost:8086, 数据库前缀: EMS_
- **后端服务**: http://127.0.0.1:8080/jeecg-boot
- **前端服务**: http://127.0.0.1:3100
- **测试账号**: xdadmin / xd@123456

### 项目结构
```
JeecgBoot/
├── jeecg-boot/jeecg-module-energy/     # 能源管理模块（后端）
│   ├── controller/                      # 控制器
│   ├── service/                         # 业务逻辑
│   ├── mapper/                          # 数据访问
│   ├── entity/                          # 实体类
│   └── job/                             # 定时任务
├── jeecgboot-vue3/src/views/
│   ├── EnergyAnalysis/
│   │   └── Energy_Process_Consumption/  # 工序能耗分析（待开发后端）
│   └── EnergyStatistics/
│       ├── Energy_Classification/       # 能源分类统计（参考）
│       └── Team_Energy/                 # 班组用能统计（参考）
└── system_Remark/                       # 开发文档
```

## Agent Team 组织结构

### 1. Team Lead (团队负责人)
**角色**: 协调者
**职责**:
- 整体协调和任务分配
- 审核方案和代码
- 批准敏感操作（DELETE、DROP、结构变更、Git push）
- 最终验收
- 监控团队进度，确保按计划推进

### 2. Project Analyst (项目分析师)
**角色**: Explore agent
**职责**:
- 深入分析现有代码和数据库结构
- 研究前端页面 Energy_Consumption 的实现细节
- 分析参考功能（Energy_Classification、Team_Energy）的实现模式
- 理解维度树组件的统一使用方式
- 分析现有数据表结构和关联关系
- 输出：项目分析报告（包含现状、需求、技术方案建议）

### 3. System Architect (系统设计师)
**角色**: Plan agent
**职责**:
- 基于项目分析报告设计工序能耗分析的数据模型
- 设计数据库表结构（工序信息表、工序能耗统计表等）
- 设计 API 接口规范
- 确保与现有功能的一致性（维度树、查询参数、返回格式）
- 设计前后端数据交互协议
- 输出：详细设计文档（数据库设计、API 设计、技术架构）

### 4. Database Engineer (数据库工程师)
**角色**: general-purpose agent
**职责**:
- 根据设计文档创建数据库表（需 Team Lead 批准）
- 设计表索引和约束
- 编写测试数据脚本
- 验证数据关联和完整性
- 输出：数据库脚本和测试数据

### 5. Backend Engineer (后端工程师)
**角色**: general-purpose agent
**职责**:
- 开发 Entity、Mapper、Service、Controller
- 实现 API 接口（参考 TeamEnergyController 的风格）
- 实现业务逻辑（工序能耗统计、趋势分析、数据导出）
- 编写单元测试
- Git 提交代码
- 输出：完整的后端代码

### 6. Frontend Engineer (前端工程师)
**角色**: general-purpose agent
**职责**:
- 修改前端页面，统一左侧维度列表组件
- 创建 API 调用服务
- 对接后端 API
- 实现数据展示和交互
- 优化用户体验
- Git 提交代码
- 输出：完整的前端代码

### 7. QA Engineer (测试工程师)
**角色**: general-purpose agent
**职责**:
- 使用浏览器自动化测试（Playwright MCP）
- 验证数据正确性
- 执行端到端测试
- 测试各种查询条件和边界情况
- 生成测试报告
- 输出：测试报告和问题清单

## 工作流程

### 阶段 1: 项目分析（Project Analyst）

**任务**：
1. **分析前端页面**：
   - 查看 `EnergyAnalysis/Energy_Process_Consumption/index.vue` 的实现
   - 分析页面结构：左侧树形菜单（需改为维度树）、右侧数据展示
   - 分析数据展示内容：
     - 统计卡片：总能耗、生产用能、辅助用能、单位产品能耗
     - 饼图：过程能耗分布（主工艺过程、辅助工艺过程、公用工程系统、附属生产系统）
     - 折线图：过程能耗趋势
     - 数据表格：详细数据
   - 理解前端期望的 API 接口格式

2. **分析参考功能**：
   - 研究 `EnergyStatistics/Energy_Classification/index.vue` 的维度树实现
   - 研究 `EnergyStatistics/Team_Energy/index.vue` 的维度树实现
   - 找出维度树组件的统一使用模式（ClassificationDimensionTree 或 DimensionTree）
   - 分析 API 调用方式和数据处理逻辑

3. **分析数据库结构**：
   - 查看现有表结构（tb_module、tb_ep_equ_energy_*）
   - 理解能耗统计的数据流
   - 分析工序能耗分析需要哪些新表
   - 理解"工序"的业务含义：主工艺过程、辅助工艺过程、公用工程系统、附属生产系统

4. **输出项目分析报告**：
   - 现状分析（前端页面结构、数据展示需求）
   - 需求分析（工序分类、能耗统计维度）
   - 技术方案建议（数据库设计、API 设计）
   - 风险评估

**输出**: 项目分析报告，提交给 Team Lead 审核

### 阶段 2: 系统设计（System Architect）

**任务**：
1. **设计数据模型**：
   - 工序信息表（tb_process_info）：工序编码、工序名称、所属部门、工序类型等
   - 工序维度关联表（tb_process_dimension_relation）：工序与维度的关联
   - 工序能耗统计表（tb_process_energy_statistics）：工序能耗统计数据
   - 考虑是否需要工序排班表、工序设备关联表等

2. **设计 API 接口**：
   - GET /energy/process/statistics - 工序能耗统计数据
   - GET /energy/process/trend - 工序能耗趋势数据
   - GET /energy/process/table - 工序能耗表格数据
   - GET /energy/process/dimension/tree - 维度树数据
   - POST /energy/process/export - 导出数据
   - 参考 TeamEnergyController 的接口设计风格

3. **设计数据交互协议**：
   - 请求参数格式
   - 响应数据格式
   - 错误处理机制

4. **输出设计文档**：
   - 数据库表设计（DDL 语句）
   - API 接口设计（接口文档）
   - 数据流程图
   - 技术架构图

**输出**: 详细设计文档，等待 Team Lead 批准

### 阶段 3: 数据库开发（Database Engineer）

**任务**：
1. 根据设计文档创建数据库表（需 Team Lead 批准）
2. 创建索引和约束
3. 编写测试数据脚本
4. 验证数据关联和完整性
5. 输出数据库脚本文件

**输出**: 数据库脚本和测试数据

### 阶段 4: 后端开发（Backend Engineer）

**任务**：
1. 创建 Entity 类（参考 TeamInfo、TeamDimensionRelation）
2. 创建 Mapper 接口和 XML
3. 创建 Service 接口和实现
4. 创建 Controller（参考 TeamEnergyController）
5. 实现业务逻辑：
   - 工序能耗统计
   - 工序能耗趋势分析
   - 工序能耗表格数据
   - 数据导出
6. 测试 API 接口
7. Git 提交：`[工序统计] 实现后端 API`

**输出**: 完整的后端代码

### 阶段 5: 前端开发（Frontend Engineer）

**任务**：
1. **替换左侧树形菜单为维度树组件**：
   - 移除现有的简单树形菜单（a-tree）
   - 参考 Energy_Classification 和 Team_Energy 的实现
   - 使用统一的维度树组件（ClassificationDimensionTree 或 DimensionTree）
   - 添加维度切换 Tabs（按部门用电、按线路用电、天然气、压缩空气、企业用水）
   - 确保维度切换逻辑一致

2. **创建 API 调用服务**：
   - 创建 `process-energy.api.ts`
   - 定义 API 接口函数：
     - getProcessEnergyStatistics - 获取统计数据
     - getProcessEnergyDistribution - 获取能耗分布（饼图）
     - getProcessEnergyTrend - 获取能耗趋势（折线图）
     - getProcessEnergyTable - 获取表格数据
     - exportProcessEnergyData - 导出数据

3. **修改 index.vue 对接后端**：
   - 修改查询逻辑，调用后端 API
   - 处理返回数据，更新统计卡片、图表和表格
   - 实现数据导出功能
   - 添加维度选择和切换逻辑

4. **优化用户体验**：
   - 添加加载状态
   - 添加错误提示
   - 优化界面布局
   - 确保与其他功能页面风格一致

5. Git 提交：`[工序统计] 前端对接后端 API`

**输出**: 完整的前端代码

### 阶段 6: 集成测试（QA Engineer）

**任务**：
1. 确保后端和前端服务正在运行
2. 使用 Playwright MCP 访问系统
3. 登录系统（xdadmin / xd@123456）
4. 导航到"工序能耗分析"页面
5. 测试各种查询条件：
   - 选择不同维度
   - 选择不同时间范围
   - 选择不同仪表
   - 切换显示方式（统一/分开）
   - 切换图表类型（曲线/柱状）
6. 验证数据正确性
7. 测试边界情况（无数据、大量数据等）
8. 截图保存测试结果
9. 生成测试报告

**输出**: 测试报告和问题清单

### 阶段 7: 问题修复循环

- 如果测试发现问题，QA 报告给相应工程师
- 工程师修复后重新测试
- 重复直到所有测试通过

### 阶段 8: 代码审查和提交

1. Team Lead 审查所有代码变更
2. 确认无误后合并到主分支
3. 创建最终 Git commit（需要 Team Lead 批准）
4. 更新开发文档

## 技术约束

### 后端开发规范
- 使用 Lombok 注解（@Data, @Slf4j）
- RESTful API 设计
- MyBatis-Plus 进行数据库操作
- 参考 TeamEnergyController 的实现风格
- 添加详细日志记录
- 完善异常处理

### 前端开发规范
- 必须使用 Composition API
- 使用 `const` 定义方法
- 事件处理器以 "handle" 开头
- 优先使用 Tailwind CSS
- 添加 TypeScript 类型定义
- 参考 Energy_Classification 和 Team_Energy 的组件结构

### Git 工作流
- 分支名称: `feature/process-energy-analysis`
- 提交信息格式: `[工序统计] 简短描述`
- 敏感操作需要 Team Lead 批准

### 权限控制
| 操作类型 | 权限 |
|----------|------|
| SELECT 查询 | 自动执行 |
| INSERT/UPDATE | 记录日志后执行 |
| DELETE/DROP/TRUNCATE | 必须等待 Team Lead 批准 |
| 文件删除 | 必须等待 Team Lead 批准 |
| Git push | 必须等待 Team Lead 批准 |

## 参考资料

### 已有功能参考
- 能源分类统计：`EnergyStatistics/Energy_Classification/`
  - 维度树组件：`ClassificationDimensionTree.vue`
  - API 调用：`api/index.ts`
  - 数据处理：`hooks/useEnergyClassification.ts`
- 班组用能统计：`EnergyStatistics/Team_Energy/`
  - 维度树组件：`DimensionTree.vue`（来自 Energy_Depart）
  - API 调用：`team-energy.api.ts`
- 工序能耗分析：`EnergyAnalysis/Energy_Process_Consumption/`（当前页面）
  - 现状：使用简单的 a-tree 组件
  - 需要改造：替换为统一的维度树组件

### 数据库文档
- `system_Remark/能碳管理平台能管部份开发说明_extracted.txt`
- `system_Remark/数据库结构与测试数据含jeecgboot本身的结构与数据.sql`

### 现有表结构
- `tb_module` - 仪表信息
- `tb_ep_equ_energy_daycount` - 日能耗统计
- `tb_ep_equ_energy_monthcount` - 月能耗统计
- `tb_ep_equ_energy_yearcount` - 年能耗统计
- `sys_depart` - 部门信息

## 业务逻辑说明

### 工序能耗分析的数据流

```
InfluxDB (实时点位数据)
    ↓ (InfluxDBSyncJob 每5分钟同步)
MySQL 实时表 (tb_equ_ele_data, tb_equ_energy_data)
    ↓ (InfluxDBSyncJob 实时更新统计)
MySQL 统计表 (tb_ep_equ_energy_daycount/monthcount/yearcount)
    ↓ (工序统计服务)
工序能耗统计 (按工序+时间维度汇总)
```

### 核心业务需求

根据前端页面分析，工序能耗分析的业务需求如下：

1. **工序分类**（4大类）：
   - **主工艺过程**：核心生产工序（如熔炼、挤压等）
   - **辅助工艺过程**：辅助生产的工序（如预热、冷却等）
   - **公用工程系统**：公共设施（如供电、供水、空压等）
   - **附属生产系统**：附属设施（如仓储、运输等）

2. **工序定义**：
   - 工序是生产过程中的一个环节
   - 每个工序属于上述4大类之一
   - 每个工序可能使用多个设备/仪表
   - 工序可能属于不同的部门或生产线

3. **工序能耗统计**：
   - 按工序分类汇总能耗数据
   - 支持按日/月/年统计
   - 统计指标：
     - 总能耗
     - 生产用能（主工艺过程 + 辅助工艺过程）
     - 辅助用能（公用工程系统 + 附属生产系统）
     - 单位产品能耗

4. **维度分析**：
   - 按部门维度：统计各部门下的工序能耗
   - 按线路维度：统计各线路下的工序能耗（如熔炼生产线、挤压生产线）
   - 按能源类型维度：统计不同能源的工序能耗

5. **数据展示**：
   - 统计卡片：总能耗、生产用能、辅助用能、单位产品能耗
   - 饼图：过程能耗分布（4大类工序的占比）
   - 折线图：过程能耗趋势（4大类工序随时间的变化）
   - 数据表格：详细的工序能耗数据
   - 数据导出：支持导出 Excel

## 数据库设计建议

### 1. tb_process_info（工序基础信息表）

```sql
CREATE TABLE `tb_process_info` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `process_code` varchar(50) NOT NULL COMMENT '工序编码（唯一）',
  `process_name` varchar(100) NOT NULL COMMENT '工序名称',
  `process_category` int(11) NOT NULL COMMENT '工序分类（1:主工艺过程, 2:辅助工艺过程, 3:公用工程系统, 4:附属生产系统）',
  `process_type` varchar(20) DEFAULT NULL COMMENT '工序类型（熔炼/挤压/切割/焊接等）',
  `org_code` varchar(64) DEFAULT NULL COMMENT '所属组织编码',
  `org_name` varchar(200) DEFAULT NULL COMMENT '所属组织名称',
  `production_line` varchar(100) DEFAULT NULL COMMENT '所属生产线',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态（0-停用，1-启用）',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_process_category` (`process_category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序基础信息表';
```

### 2. tb_process_dimension_relation（工序维度关联表）

```sql
CREATE TABLE `tb_process_dimension_relation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `process_code` varchar(50) NOT NULL COMMENT '工序编码',
  `dimension_code` varchar(64) NOT NULL COMMENT '维度编码（如A01B03）',
  `dimension_type` int(11) NOT NULL COMMENT '维度类型（1:按部门用电, 2:按线路用电, 3:天然气, 4:压缩空气, 5:企业用水）',
  `energy_type` int(11) DEFAULT NULL COMMENT '能源类型（1:电, 2:水, 5:压缩空气, 8:天然气）',
  `module_ids` text COMMENT '关联的仪表ID列表（逗号分隔）',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态（0-停用，1-启用）',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `standby1` varchar(200) DEFAULT NULL COMMENT '备用字段1',
  `standby2` varchar(200) DEFAULT NULL COMMENT '备用字段2',
  `standby3` varchar(200) DEFAULT NULL COMMENT '备用字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_dimension` (`process_code`,`dimension_code`,`dimension_type`),
  KEY `idx_dimension_code` (`dimension_code`),
  KEY `idx_dimension_type` (`dimension_type`),
  KEY `idx_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序维度关联表';
```

### 3. tb_process_energy_statistics（工序能耗统计表）

```sql
CREATE TABLE `tb_process_energy_statistics` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `process_code` varchar(50) NOT NULL COMMENT '工序编码',
  `process_name` varchar(100) DEFAULT NULL COMMENT '工序名称',
  `process_category` int(11) NOT NULL COMMENT '工序分类（1:主工艺过程, 2:辅助工艺过程, 3:公用工程系统, 4:附属生产系统）',
  `org_code` varchar(64) DEFAULT NULL COMMENT '所属组织编码',
  `dimension_type` int(11) DEFAULT NULL COMMENT '维度类型',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_month` varchar(7) DEFAULT NULL COMMENT '统计月份（YYYY-MM）',
  `stat_year` varchar(4) DEFAULT NULL COMMENT '统计年份（YYYY）',
  `time_dimension` varchar(10) NOT NULL COMMENT '时间维度（day/month/year）',
  `energy_type` int(10) NOT NULL COMMENT '能源类型',
  `energy_type_name` varchar(50) DEFAULT NULL COMMENT '能源类型名称',
  `total_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '总消耗量',
  `total_cost` decimal(18,2) DEFAULT '0.00' COMMENT '总费用',
  `carbon_emission` decimal(18,2) DEFAULT '0.00' COMMENT '碳排放量',
  `standard_coal` decimal(18,2) DEFAULT '0.00' COMMENT '标准煤当量',
  `peak_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段消耗',
  `peak_cost` decimal(18,2) DEFAULT '0.00' COMMENT '峰时段费用',
  `flat_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '平时段消耗',
  `flat_cost` decimal(18,2) DEFAULT '0.00' COMMENT '平时段费用',
  `valley_consumption` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段消耗',
  `valley_cost` decimal(18,2) DEFAULT '0.00' COMMENT '谷时段费用',
  `meter_count` int(10) DEFAULT '0' COMMENT '仪表数量',
  `production_output` decimal(18,2) DEFAULT '0.00' COMMENT '产量',
  `unit_consumption` decimal(18,4) DEFAULT '0.0000' COMMENT '单位产品能耗',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_code` (`process_code`),
  KEY `idx_process_category` (`process_category`),
  KEY `idx_org_code` (`org_code`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_time_dimension` (`time_dimension`),
  KEY `idx_energy_type` (`energy_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序能耗统计表';
```

## MCP 工具使用

### MySQL MCP
用于数据库操作，已配置连接信息：
- 查询数据：直接使用 SQL
- 创建表：需要 Team Lead 批准

### Playwright MCP
用于浏览器自动化测试：
- 访问前端页面
- 模拟用户操作
- 截图验证

## 成功标准

- [ ] 项目分析报告完成
- [ ] 系统设计文档完成
- [ ] 数据库表设计完成
- [ ] 后端 API 开发完成
- [ ] 前端对接完成
- [ ] 左侧维度列表与其他功能统一
- [ ] 所有测试通过
- [ ] 代码已提交到 Git
- [ ] 文档已更新

## 启动命令

```bash
# 创建 Agent Team
claude --team "process-energy-dev" --description "工序能耗分析功能开发"
```

---

## 快速启动提示词

复制以下内容到 Claude Code 中启动开发：

```
请帮我开发工序能耗分析功能。

项目信息：
- 前端页面已存在：jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Process_Consumption/
- 参考实现：EnergyStatistics/Energy_Classification 能源分类统计、EnergyStatistics/Team_Energy 班组用能统计
- 数据库：127.0.0.1:3306/emsproject_jeecg (root/Abc123456@)
- 后端：http://127.0.0.1:8080/jeecg-boot
- 前端：http://127.0.0.1:3100
- 测试账号：xdadmin / xd@123456

请使用 Agent Teams 进行开发：
1. 创建团队：process-energy-dev
2. 分配角色：
   - Project Analyst（项目分析师）- 分析现有代码和需求
   - System Architect（系统设计师）- 设计数据库和 API
   - Database Engineer（数据库工程师）- 创建数据库表
   - Backend Engineer（后端工程师）- 开发后端 API
   - Frontend Engineer（前端工程师）- 前端对接（重点：替换左侧树为维度树组件）
   - QA Engineer（测试工程师）- 测试验证
3. 按阶段执行：分析 -> 设计 -> 数据库 -> 后端 -> 前端 -> 测试
4. 敏感操作（DELETE/DROP/文件删除/Git push）需要我批准
5. 使用 Playwright MCP 进行浏览器测试
6. 使用 MySQL MCP 进行数据库操作
7. 完成后提交 Git

详细开发文档请参考：system_Remark/工序能耗分析_AgentTeams开发提示词.md
```

## 重要提醒

1. **先分析后设计**: 必须先完成项目分析，理解现有代码和需求后再设计
2. **统一维度树**: 重点关注左侧维度列表的统一实现方式
3. **参考现有功能**: 充分参考 Energy_Classification 和 Team_Energy 的实现
4. **方案先行**: 每个阶段开始前，先输出方案等待审批
5. **敏感操作**: 删除、数据库结构变更、Git push 需要等待批准
6. **测试驱动**: 修改后必须通过测试验证
7. **文档同步**: 重要变更要更新文档
8. **自主开发**: Agent Teams 应该自主完成大部分工作，只在关键决策点等待批准
9. **浏览器测试**: 使用 Playwright MCP 自己查看运行效果，有问题再修改
10. **版本控制**: 做好 Git 提交，每个阶段完成后提交一次

## Agent Teams 自主权限

### 可以自主执行的操作
- SELECT 查询数据库
- 读取文件
- 分析代码
- 设计方案
- 编写代码
- INSERT/UPDATE 数据（会记录日志）
- 运行测试
- 使用浏览器自动化测试
- 创建 Git commit（本地）

### 需要 Team Lead 批准的操作
- DELETE/DROP/TRUNCATE 数据库操作
- 创建或修改数据库表结构
- 删除文件
- Git push 到远程仓库
- 重大架构变更

## 沟通机制

### Agent 之间的沟通
- 使用 SendMessage 工具进行沟通
- 重要决策需要抄送 Team Lead
- 发现问题及时通知相关 Agent

### 向 Team Lead 汇报
- 每个阶段完成后汇报进度
- 遇到问题及时汇报
- 需要批准的操作提前申请
- 定期汇报整体进度

## 质量标准

### 代码质量
- 遵循项目编码规范
- 添加必要的注释
- 完善的异常处理
- 详细的日志记录

### 测试质量
- 功能测试覆盖所有场景
- 边界情况测试
- 性能测试（大数据量）
- 用户体验测试

### 文档质量
- 清晰的设计文档
- 完整的 API 文档
- 详细的测试报告
- 更新的开发文档

---

**祝开发顺利！**
