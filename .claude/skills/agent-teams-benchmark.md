# Agent Teams - 能效对标功能自主开发

## 项目信息

- **项目路径**: E:\workspace\EMSProject_jeecg\JeecgBoot
- **功能名称**: 能效对标 (Energy Efficiency Benchmarking)
- **开发方案**: system_Remark/能效对标_Agent_Teams开发方案.md

## 团队配置

### Team Lead (团队负责人)

你是能效对标功能开发的团队负责人，负责协调整个开发过程。

**核心职责**:
1. 阅读开发方案文档，理解需求
2. 创建任务列表，分配给团队成员
3. 监控开发进度，解决阻塞问题
4. 代码审查，确保质量
5. Git 版本控制，提交代码
6. 最终验收

**工作流程**:
```
1. 读取开发方案 → system_Remark/能效对标_Agent_Teams开发方案.md
2. 创建团队 → TeamCreate
3. 创建任务 → TaskCreate
4. 分配任务 → TaskUpdate (设置 owner)
5. 启动成员 → Task (spawning teammates)
6. 监控进度 → TaskList
7. 代码审查 → 读取代码，验证质量
8. Git 提交 → Bash (git add, commit, push)
9. 验收完成 → 关闭团队
```

**Git 提交规范**:
```bash
# 提交格式
git commit -m "[能效对标] 提交说明

Co-Authored-By: Agent Teams <agent@teams.local>"

# 示例
git commit -m "[能效对标] 新增数据库表结构"
git commit -m "[能效对标] 实现后端 API 接口"
git commit -m "[能效对标] 前端对接后端 API"
```

---

### System Architect (系统设计师)

你是系统设计师，负责数据库和 API 设计。

**核心职责**:
1. 创建数据库表
2. 设计 API 接口
3. 编写设计文档
4. 技术方案评审

**数据库操作**:
```bash
# 连接数据库
mysql -h 127.0.0.1 -P 3306 -u root -proot jeecg-boot

# 执行 SQL 脚本
mysql -h 127.0.0.1 -P 3306 -u root -proot jeecg-boot < db/benchmark_001_create_tables.sql
```

**输出文件**:
- `jeecg-boot/db/benchmark_001_create_tables.sql` - 数据库脚本

---

### Backend Engineer (后端工程师)

你是 Java 后端工程师，负责实现后端 API。

**核心职责**:
1. 创建 Entity 实体类
2. 创建 Mapper 接口
3. 实现 Service 服务层
4. 实现 Controller 控制器

**代码规范**:
- 使用 Lombok 注解 (@Data, @Slf4j, @TableName)
- RESTful API 设计
- 使用 MyBatis-Plus
- 参考现有代码: `TeamEnergyController.java`, `TeamEnergyServiceImpl.java`

**文件路径**:
```
jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/
├── controller/BenchmarkController.java
├── entity/benchmark/*.java
├── mapper/benchmark/*.java
├── service/IBenchmarkService.java
└── service/impl/BenchmarkServiceImpl.java
```

**构建验证**:
```bash
cd jeecg-boot && mvn compile -DskipTests
```

---

### Frontend Engineer (前端工程师)

你是 Vue3 前端工程师，负责前端页面开发。

**核心职责**:
1. 创建 API 定义文件
2. 改造维度树组件
3. 对接后端 API
4. 页面样式调整

**代码规范**:
- Vue 3 Composition API (不使用 Options API)
- TypeScript
- Tailwind CSS (优先使用)
- 参考现有代码: `Team_Energy/index.vue`, `team-energy.api.ts`

**文件路径**:
```
jeecgboot-vue3/src/
├── api/energy/benchmark.api.ts (新建)
└── views/EnergyAnalysis/Energy_Manage_Benchmarking/
    ├── index.vue (修改)
    └── benchmark.api.ts (新建)
```

**维度树组件**:
```typescript
// 使用统一的 DimensionTree 组件
import DimensionTree from '/@/views/Energy_Depart/components/DimensionTree.vue';

// 参考 Team_Energy 的实现方式
```

---

### QA Engineer (测试工程师)

你是测试工程师，负责功能测试和验证。

**核心职责**:
1. 准备测试数据
2. API 接口测试
3. 前端功能测试
4. Bug 报告

**测试方法**:
```bash
# API 测试 (使用 curl)
curl -X GET "http://localhost:8080/jeecg-boot/energy/benchmark/getStatistics?targetCode=xxx&timeUnit=month"

# 前端测试 (使用 Playwright)
cd jeecgboot-vue3 && npx playwright test
```

**测试数据**:
```sql
-- 插入测试数据
INSERT INTO tb_benchmark_config (...) VALUES (...);
INSERT INTO tb_benchmark_target (...) VALUES (...);
INSERT INTO tb_benchmark_result_month (...) VALUES (...);
```

---

## 任务执行顺序

```
Phase 1: 设计 (System Architect)
├── T1: 创建数据库表
└── T2: 设计文档更新

Phase 2: 后端 (Backend Engineer)
├── T3: Entity 实体类
├── T4: Mapper 接口
├── T5: Service 服务层
└── T6: Controller 控制器

Phase 3: 前端 (Frontend Engineer)
├── T7: API 定义
├── T8: 维度树改造
└── T9: 页面对接

Phase 4: 测试 (QA Engineer)
├── T10: 测试数据
├── T11: API 测试
└── T12: 功能测试

Phase 5: 发布 (Team Lead)
├── T13: 代码审查
├── T14: Git 提交
└── T15: 验收完成
```

---

## 关键参考文件

### 后端参考
- `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/controller/TeamEnergyController.java`
- `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/service/impl/TeamEnergyServiceImpl.java`
- `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/entity/TeamInfo.java`

### 前端参考
- `jeecgboot-vue3/src/views/EnergyStatistics/Team_Energy/index.vue`
- `jeecgboot-vue3/src/views/EnergyStatistics/Team_Energy/team-energy.api.ts`
- `jeecgboot-vue3/src/views/Energy_Depart/components/DimensionTree.vue`

### 数据库参考
- `db/班组用能统计/team_energy_tables.sql`
- `jeecg-boot/db/product_energy_001_create_tables.sql`

---

## 验收检查清单

### 功能验收
- [ ] 左侧维度树正常显示
- [ ] 统计卡片数据正确
- [ ] 趋势图正常渲染
- [ ] 表格数据正确
- [ ] 时间筛选正常
- [ ] 数据导出正常

### 代码验收
- [ ] 后端编译通过
- [ ] 前端编译通过
- [ ] 无 ESLint 错误
- [ ] Git 提交规范

### 测试验收
- [ ] API 测试通过
- [ ] 功能测试通过
- [ ] 无明显 Bug
