# 能源管理系统 - 班次用能统计功能开发任务

## 项目背景

基于 JeecgBoot 3.7.2 的能源管理系统，采用 Spring Boot 2.7.18 + Vue3 + InfluxDB 1.8 + MySQL 架构。

**当前任务**：开发班次用能统计功能
- 前端静态页面已存在：`jeecgboot-vue3/src/views/EnergyStatistics/Shift_Energy/`
- 需要设计数据库表、开发后端 API、前端对接
- 左侧维度列表需要与其他功能统一

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
├── jeecgboot-vue3/src/views/EnergyStatistics/
│   ├── Shift_Energy/                    # 班次用能统计（待开发）
│   └── Team_Energy/                     # 班组用能统计（参考）
└── system_Remark/                       # 开发文档
```

## Agent Team 组织结构

### 1. Team Lead (团队负责人)
**角色**: 协调者
**职责**:
- 整体协调和任务分配
- 审核方案和代码
- 批准敏感操作（DELETE、DROP、结构变更）
- 最终验收

### 2. System Architect (系统设计师)
**角色**: Plan agent
**职责**:
- 分析现有代码和数据库结构
- 设计班次用能统计的数据模型
- 设计 API 接口规范
- 确保与现有功能的一致性
- 输出：设计文档

### 3. Backend Engineer (后端工程师)
**角色**: general-purpose agent
**职责**:
- 创建数据库表（需 Team Lead 批准）
- 开发 Entity、Mapper、Service、Controller
- 实现 API 接口
- 编写单元测试
- Git 提交代码

### 4. Frontend Engineer (前端工程师)
**角色**: general-purpose agent
**职责**:
- 修改前端页面，对接后端 API
- 统一左侧维度列表组件
- 实现数据展示和交互
- Git 提交代码

### 5. QA Engineer (测试工程师)
**角色**: general-purpose agent
**职责**:
- 使用浏览器自动化测试（Playwright MCP）
- 验证数据正确性
- 执行端到端测试
- 生成测试报告

## 工作流程

### 阶段 1: 需求分析与设计（System Architect）

1. **分析现有代码**：
   - 查看 `Shift_Energy/index.vue` 前端页面结构
   - 查看 `Team_Energy` 班组用能统计的实现作为参考
   - 查看现有数据库表结构

2. **设计数据模型**：
   - 班次信息表设计
   - 班次与仪表关联
   - 班次能耗统计逻辑

3. **设计 API 接口**：
   - GET /energy/shift/statistics - 统计数据
   - GET /energy/shift/trend - 趋势数据
   - GET /energy/shift/pie - 占比数据
   - GET /energy/shift/table - 表格数据
   - GET /energy/shift/dimension/tree - 维度树

4. **输出设计文档，等待 Team Lead 审批**

### 阶段 2: 数据库开发（Backend Engineer）

1. 创建数据库表（需 Team Lead 批准）
2. 插入测试数据
3. 验证数据关联

### 阶段 3: 后端开发（Backend Engineer）

1. 创建 Entity 类
2. 创建 Mapper 接口和 XML
3. 创建 Service 接口和实现
4. 创建 Controller
5. 测试 API 接口
6. Git 提交：`[班次统计] 实现后端 API`

### 阶段 4: 前端开发（Frontend Engineer）

1. 统一左侧维度列表组件（参考其他功能）
2. 创建 API 调用服务
3. 修改 index.vue 对接后端
4. 实现数据展示
5. Git 提交：`[班次统计] 前端对接后端 API`

### 阶段 5: 集成测试（QA Engineer）

1. 启动后端和前端服务
2. 使用 Playwright MCP 访问系统
3. 测试班次用能统计功能
4. 验证数据正确性
5. 生成测试报告

### 阶段 6: 问题修复循环

- 如果测试发现问题，QA 报告给相应工程师
- 工程师修复后重新测试
- 重复直到所有测试通过

### 阶段 7: 代码审查和提交

1. Team Lead 审查所有代码变更
2. 合并到主分支
3. 创建最终 Git commit

## 技术约束

### 后端开发规范
- 使用 Lombok 注解（@Data, @Slf4j）
- RESTful API 设计
- MyBatis-Plus 进行数据库操作
- 参考 TeamEnergyController 的实现风格

### 前端开发规范
- 必须使用 Composition API
- 使用 TypeScript
- 优先使用 Tailwind CSS
- 参考 Team_Energy 的组件结构

### Git 工作流
- 分支名称: `feature/shift-energy-statistics`
- 提交信息格式: `[班次统计] 简短描述`
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
- 班组用能统计：`Team_Energy/`
- 维度树组件：查看其他功能的左侧维度列表实现

### 数据库文档
- `system_Remark/能碳管理平台能管部份开发说明_extracted.txt`
- `system_Remark/班组用能统计修复方案.md`

### 现有表结构
- `tb_team_info` - 班组信息
- `tb_team_shift_schedule` - 班组排班
- `tb_ep_equ_energy_daycount` - 日能耗统计
- `tb_module` - 仪表信息

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

- [ ] 数据库表设计完成
- [ ] 后端 API 开发完成
- [ ] 前端对接完成
- [ ] 左侧维度列表与其他功能统一
- [ ] 所有测试通过
- [ ] 代码已提交到 Git

## 启动命令

```bash
# 创建 Agent Team
claude --team "shift-energy-dev" --description "班次用能统计功能开发"

# 或者使用以下提示词启动
```

---

## 快速启动提示词

复制以下内容到 Claude Code 中启动开发：

```
请帮我开发班次用能统计功能。

项目信息：
- 前端页面已存在：jeecgboot-vue3/src/views/EnergyStatistics/Shift_Energy/
- 参考实现：Team_Energy 班组用能统计
- 数据库：127.0.0.1:3306/emsproject_jeecg (root/Abc123456@)
- 后端：http://127.0.0.1:8080/jeecg-boot
- 前端：http://127.0.0.1:3100
- 测试账号：xdadmin / xd@123456

请使用 Agent Teams 进行开发：
1. 创建团队：shift-energy-dev
2. 分配角色：系统设计师、后端工程师、前端工程师、测试工程师
3. 按阶段执行：设计 -> 数据库 -> 后端 -> 前端 -> 测试
4. 敏感操作（DELETE/DROP/文件删除）需要我批准
5. 使用 Playwright MCP 进行浏览器测试
6. 使用 MySQL MCP 进行数据库操作
7. 完成后提交 Git

详细开发文档请参考：system_Remark/班次用能统计_AgentTeams开发提示词.md
```
