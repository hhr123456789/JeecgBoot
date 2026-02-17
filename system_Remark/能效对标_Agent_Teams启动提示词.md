# 能效对标 - Agent Teams 自主开发启动提示词

## 启动命令

在 Claude Code 中执行以下提示词，即可启动 Agent Teams 自主开发：

---

## 提示词 (复制使用)

```
请使用 Agent Teams 自主完成能效对标功能的开发。

## 项目信息
- 项目路径: E:\workspace\EMSProject_jeecg\JeecgBoot
- 开发方案: system_Remark/能效对标_Agent_Teams开发方案.md
- 技能文档: .claude/skills/agent-teams-benchmark.md

## 团队配置

创建一个名为 "benchmark-dev" 的团队，包含以下成员：

1. **architect** (System Architect)
   - 职责: 数据库设计、创建表结构
   - 类型: general-purpose
   - 首要任务: 执行 SQL 创建数据库表

2. **backend** (Backend Engineer)
   - 职责: Java 后端开发
   - 类型: general-purpose
   - 首要任务: 创建 Entity、Mapper、Service、Controller

3. **frontend** (Frontend Engineer)
   - 职责: Vue3 前端开发
   - 类型: general-purpose
   - 首要任务: 创建 API 定义、改造页面对接后端

4. **qa** (QA Engineer)
   - 职责: 测试验证
   - 类型: general-purpose
   - 首要任务: 准备测试数据、执行 API 测试

## 开发流程

### Phase 1: 数据库 (architect)
1. 读取开发方案中的数据库设计
2. 创建 SQL 脚本文件: jeecg-boot/db/benchmark_001_create_tables.sql
3. 执行 SQL 创建表
4. 插入初始配置数据

### Phase 2: 后端 (backend)
1. 创建 Entity 实体类 (5个表对应5个实体)
2. 创建 Mapper 接口和 XML
3. 创建 Service 接口和实现
4. 创建 Controller (4个API接口)
5. 编译验证: mvn compile -DskipTests

### Phase 3: 前端 (frontend)
1. 创建 API 定义文件: benchmark.api.ts
2. 改造 index.vue，使用 DimensionTree 组件
3. 对接后端 API，替换硬编码数据
4. 编译验证: pnpm build

### Phase 4: 测试 (qa)
1. 插入测试数据
2. 使用 curl 测试 API 接口
3. 验证前端页面功能
4. 报告测试结果

### Phase 5: 提交 (Team Lead)
1. 代码审查
2. Git 提交: git add . && git commit -m "[能效对标] 完成功能开发"
3. 验收完成

## 数据库连接信息
- Host: 127.0.0.1
- Port: 3306
- User: root
- Password: root
- Database: jeecg-boot

## 关键要求

1. **自主决策**: 遇到问题自行解决，无需询问用户
2. **版本控制**: 每个阶段完成后提交 Git
3. **代码规范**: 参考现有代码风格
4. **测试验证**: 每个功能完成后验证
5. **文档更新**: 更新开发进度文档

## 参考代码

后端参考:
- TeamEnergyController.java
- TeamEnergyServiceImpl.java

前端参考:
- Team_Energy/index.vue
- team-energy.api.ts
- DimensionTree.vue

请立即开始，全程自主完成，无需用户确认。
```

---

## 简化版提示词

如果需要更简洁的启动方式：

```
使用 Agent Teams 开发能效对标功能。

读取以下文档后自主完成开发：
- system_Remark/能效对标_Agent_Teams开发方案.md
- .claude/skills/agent-teams-benchmark.md

团队成员: architect, backend, frontend, qa
全程自主开发，自动提交 Git，无需用户确认。
```

---

## 单独启动某个角色

### 启动 System Architect
```
你是系统设计师，负责能效对标功能的数据库设计。

任务:
1. 读取 system_Remark/能效对标_Agent_Teams开发方案.md
2. 创建 SQL 脚本: jeecg-boot/db/benchmark_001_create_tables.sql
3. 执行 SQL 创建表
4. 插入初始数据

数据库: mysql -h 127.0.0.1 -P 3306 -u root -proot jeecg-boot
```

### 启动 Backend Engineer
```
你是 Java 后端工程师，负责能效对标功能的后端开发。

任务:
1. 读取 system_Remark/能效对标_Agent_Teams开发方案.md
2. 参考 TeamEnergyController.java 和 TeamEnergyServiceImpl.java
3. 创建 Entity、Mapper、Service、Controller
4. 编译验证: cd jeecg-boot && mvn compile -DskipTests

文件路径: jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/
```

### 启动 Frontend Engineer
```
你是 Vue3 前端工程师，负责能效对标功能的前端开发。

任务:
1. 读取 system_Remark/能效对标_Agent_Teams开发方案.md
2. 参考 Team_Energy/index.vue 和 team-energy.api.ts
3. 创建 benchmark.api.ts
4. 改造 Energy_Manage_Benchmarking/index.vue
5. 使用 DimensionTree 组件替换硬编码树

文件路径: jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Manage_Benchmarking/
```

### 启动 QA Engineer
```
你是测试工程师，负责能效对标功能的测试验证。

任务:
1. 插入测试数据到数据库
2. 测试后端 API 接口
3. 验证前端页面功能
4. 报告测试结果

API 地址: http://localhost:8080/jeecg-boot/energy/benchmark/
```

---

## 注意事项

1. **确保服务运行**: 后端服务需要运行在 8080 端口
2. **数据库可用**: MySQL 需要可连接
3. **依赖安装**: 前端需要先执行 pnpm install
4. **Git 配置**: 确保 Git 已配置用户信息

## 预期输出

开发完成后，应该有以下文件变更：

```
新增文件:
├── jeecg-boot/db/benchmark_001_create_tables.sql
├── jeecg-boot/db/benchmark_002_test_data.sql
├── jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/
│   ├── controller/BenchmarkController.java
│   ├── entity/benchmark/BenchmarkConfig.java
│   ├── entity/benchmark/BenchmarkTarget.java
│   ├── entity/benchmark/BenchmarkResultDay.java
│   ├── entity/benchmark/BenchmarkResultMonth.java
│   ├── entity/benchmark/BenchmarkResultYear.java
│   ├── mapper/benchmark/BenchmarkConfigMapper.java
│   ├── mapper/benchmark/BenchmarkTargetMapper.java
│   ├── mapper/benchmark/BenchmarkResultDayMapper.java
│   ├── mapper/benchmark/BenchmarkResultMonthMapper.java
│   ├── mapper/benchmark/BenchmarkResultYearMapper.java
│   ├── service/IBenchmarkService.java
│   └── service/impl/BenchmarkServiceImpl.java
├── jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Manage_Benchmarking/
│   └── benchmark.api.ts

修改文件:
└── jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Manage_Benchmarking/index.vue
```

Git 提交记录:
```
[能效对标] 新增数据库表结构
[能效对标] 实现后端 Entity 和 Mapper
[能效对标] 实现后端 Service 和 Controller
[能效对标] 前端对接后端 API
[能效对标] 完成功能开发和测试
```
