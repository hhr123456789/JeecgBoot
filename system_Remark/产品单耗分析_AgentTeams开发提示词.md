# 能源管理系统 - 产品单耗分析功能开发任务

## 项目背景

基于 JeecgBoot 3.7.2 的能源管理系统,采用 Spring Boot 2.7.18 + Vue3 + InfluxDB 1.8 + MySQL 架构。

**当前任务**: 开发产品单耗分析功能
- 前端静态页面已完成: `jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Product_Consumption/`
- 需要设计数据库表、开发后端 API、前端对接
- 左侧维度列表需要与其他功能统一 (使用产品分类树)

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
├── jeecg-boot/jeecg-module-energy/     # 能源管理模块 (后端)
│   ├── controller/                      # 控制器
│   ├── service/                         # 业务逻辑
│   ├── mapper/                          # 数据访问
│   ├── entity/                          # 实体类
│   └── job/                             # 定时任务
├── jeecgboot-vue3/src/views/EnergyAnalysis/
│   ├── Energy_Product_Consumption/      # 产品单耗分析 (本次开发) ✅
│   │   ├── index.vue                    # 主页面 ✅
│   │   ├── README.md                    # 功能说明 ✅
│   │   └── components/                  # 图表组件 ✅
│   │       ├── ProductPie.vue           # 饼图
│   │       ├── ProductLine.vue          # 折线图
│   │       ├── ProductBar.vue           # 柱状图
│   │       └── ProductRanking.vue       # 排名图
│   └── Energy_Process_Consumption/      # 工序单耗分析 (参考)
└── system_Remark/                       # 开发文档
    ├── 能碳管理平台能管部份开发说明_extracted.txt
    └── 数据库结构与测试数据含jeecgboot本身的结构与数据.sql
```

## Agent Team 组织结构

### 1. Team Lead (团队负责人)
**角色**: 协调者
**职责**:
- 整体协调和任务分配
- 审核方案和代码
- 批准敏感操作 (DELETE、DROP、结构变更)
- 最终验收

### 2. System Architect (系统设计师)
**角色**: Explore agent
**职责**:
- 分析现有代码和数据库结构
- 设计产品单耗分析的数据模型
- 设计 API 接口规范
- 确保与现有功能的一致性
- 输出: 设计文档

### 3. Database Engineer (数据库工程师)
**角色**: general-purpose agent
**职责**:
- 设计数据库表结构
- 编写建表 SQL (需 Team Lead 批准)
- 编写测试数据 SQL
- 验证数据关联关系

### 4. Backend Engineer (后端工程师)
**角色**: general-purpose agent
**职责**:
- 开发 Entity、Mapper、Service、Controller
- 实现 API 接口
- 编写单元测试
- Git 提交代码

### 5. Frontend Engineer (前端工程师)
**角色**: general-purpose agent
**职责**:
- 修改前端页面,对接后端 API
- 统一左侧维度列表组件
- 实现数据展示和交互
- Git 提交代码

### 6. QA Engineer (测试工程师)
**角色**: general-purpose agent (+ Playwright MCP)
**职责**:
- 手动测试功能 (当前阶段)
- 验证数据正确性
- 执行端到端测试
- 生成测试报告

## 工作流程

### 阶段 1: 需求分析与设计 (System Architect)

**任务**:

1. **分析前端页面**:
   - 查看 `Energy_Product_Consumption/index.vue` 和 `README.md`
   - 理解页面功能和数据结构需求

2. **分析现有数据库**:
   - 查看现有表结构 (tb_module、tb_ep_equ_energy_daycount 等)
   - 查看班组用能统计、工序单耗分析的数据库设计

3. **设计数据模型**:
   需要设计以下表:
   
   **3.1 产品基础信息表** (`tb_product_info`)
   - 产品编码、产品名称、产品分类
   - 产品型号、规格、单位
   - 所属部门、状态等
   
   **3.2 产品分类表** (`tb_product_category`)
   - 分类编码、分类名称
   - 父级分类、层级
   - 排序、状态等
   
   **3.3 产品产量记录表** (`tb_product_production`)
   - 产品编码、生产日期
   - 计划产量、实际产量、合格产量
   - 不合格产量、合格率
   - 生产线、班组等
   
   **3.4 产品能耗统计表** (`tb_product_energy_consumption`)
   - 产品编码、统计日期、时间维度 (day/month/year)
   - 总能耗、单位产品能耗
   - 产量、合格产量、合格率
   - 折标煤、碳排放等
   
   **3.5 产品工序关联表** (`tb_product_process_rel`)
   - 产品编码、工序编码
   - 仪表编码列表 (关联 tb_module)
   - 能源类型、状态等

4. **设计 API 接口**:
   
   **4.1 统计数据接口**
   ```
   GET /api/energy/product/statistics
   参数: productCode, timeUnit, startDate, endDate
   返回: 总能耗、总产量、合格产量、合格率、单位产品能耗
   ```
   
   **4.2 能耗分布接口**
   ```
   GET /api/energy/product/distribution
   参数: productCategory, timeUnit, date
   返回: 各产品的能耗占比数据 (饼图)
   ```
   
   **4.3 单耗趋势接口**
   ```
   GET /api/energy/product/trend
   参数: productCodes[], timeUnit, startDate, endDate
   返回: 各产品的单耗趋势数据 (折线图)
   ```
   
   **4.4 产量能耗对比接口**
   ```
   GET /api/energy/product/comparison
   参数: productCategory, timeUnit, date
   返回: 各产品的产量和能耗数据 (双轴柱状图)
   ```
   
   **4.5 单耗排名接口**
   ```
   GET /api/energy/product/ranking
   参数: productCategory, timeUnit, date, order (asc/desc)
   返回: 产品单耗排名数据 (横向柱状图)
   ```
   
   **4.6 明细列表接口**
   ```
   GET /api/energy/product/detail-list
   参数: productCategory, timeUnit, startDate, endDate, pageNo, pageSize
   返回: 分页的产品单耗明细数据 (表格)
   ```
   
   **4.7 产品分类树接口**
   ```
   GET /api/energy/product/category-tree
   返回: 产品分类树形数据 (左侧树)
   ```

5. **输出设计文档,等待 Team Lead 审批**

### 阶段 2: 数据库开发 (Database Engineer)

**任务**:

1. **编写建表 SQL**:
   - 根据设计文档编写 5 张表的建表语句
   - 添加必要的索引和约束
   - 添加注释说明

2. **编写测试数据 SQL**:
   - 为每张表准备充足的测试数据
   - 确保数据关联关系正确
   - 覆盖不同的产品类型和时间范围

3. **提交 SQL 文件** (需 Team Lead 批准执行):
   - `001_create_product_tables.sql` - 建表语句
   - `002_insert_product_test_data.sql` - 测试数据

### 阶段 3: 后端开发 (Backend Engineer)

**任务**:

1. **创建 Entity 类**:
   - `ProductInfo.java` - 产品信息
   - `ProductCategory.java` - 产品分类
   - `ProductProduction.java` - 产品产量
   - `ProductEnergyConsumption.java` - 产品能耗统计
   - `ProductProcessRel.java` - 产品工序关联

2. **创建 Mapper 接口和 XML**:
   - `ProductInfoMapper.java` / `ProductInfoMapper.xml`
   - `ProductCategoryMapper.java` / `ProductCategoryMapper.xml`
   - `ProductProductionMapper.java` / `ProductProductionMapper.xml`
   - `ProductEnergyConsumptionMapper.java` / `ProductEnergyConsumptionMapper.xml`
   - `ProductProcessRelMapper.java` / `ProductProcessRelMapper.xml`

3. **创建 Service 接口和实现**:
   - `IProductEnergyService.java`
   - `ProductEnergyServiceImpl.java`
   
   实现业务逻辑:
   - 计算单位产品能耗 = 总能耗 ÷ 合格产量
   - 计算合格率 = 合格产量 ÷ 总产量 × 100%
   - 计算环比 = (本期单耗 - 上期单耗) ÷ 上期单耗 × 100%
   - 聚合统计数据
   - 构建树形数据

4. **创建 Controller**:
   - `ProductEnergyController.java`
   
   实现 7 个 API 接口:
   - `/api/energy/product/statistics` - 统计数据
   - `/api/energy/product/distribution` - 能耗分布
   - `/api/energy/product/trend` - 单耗趋势
   - `/api/energy/product/comparison` - 产量能耗对比
   - `/api/energy/product/ranking` - 单耗排名
   - `/api/energy/product/detail-list` - 明细列表
   - `/api/energy/product/category-tree` - 分类树

5. **编写单元测试**:
   - `ProductEnergyServiceTest.java`
   - 测试核心业务逻辑

6. **测试 API 接口**:
   - 使用 Postman 或浏览器测试每个接口
   - 验证数据正确性

7. **Git 提交**: `[产品单耗] 实现后端 API`

### 阶段 4: 前端开发 (Frontend Engineer)

**任务**:

1. **创建 API 调用服务**:
   
   创建 `src/api/energy/productEnergy.ts`:
   ```typescript
   import { defHttp } from '/@/utils/http/axios';
   
   // 获取统计数据
   export const getProductStatistics = (params) => 
     defHttp.get({ url: '/energy/product/statistics', params });
   
   // 获取能耗分布
   export const getProductDistribution = (params) => 
     defHttp.get({ url: '/energy/product/distribution', params });
   
   // 获取单耗趋势
   export const getProductTrend = (params) => 
     defHttp.get({ url: '/energy/product/trend', params });
   
   // 获取产量能耗对比
   export const getProductComparison = (params) => 
     defHttp.get({ url: '/energy/product/comparison', params });
   
   // 获取单耗排名
   export const getProductRanking = (params) => 
     defHttp.get({ url: '/energy/product/ranking', params });
   
   // 获取明细列表
   export const getProductDetailList = (params) => 
     defHttp.get({ url: '/energy/product/detail-list', params });
   
   // 获取分类树
   export const getProductCategoryTree = () => 
     defHttp.get({ url: '/energy/product/category-tree' });
   ```

2. **修改 `index.vue` 对接后端**:
   
   主要修改:
   - 导入 API 方法
   - 在 `onMounted` 中调用 `getProductCategoryTree()` 加载树数据
   - 实现 `handleQuery()` 方法调用统计接口
   - 实现 `handleSelect()` 方法更新图表数据
   - 实现 `handleExport()` 方法导出数据
   - 添加 loading 状态
   - 添加错误处理

3. **统一左侧维度列表组件**:
   
   参考其他功能的实现,确保:
   - 树形数据结构一致
   - 节点选择交互一致
   - 搜索功能一致
   - 样式风格一致

4. **优化用户体验**:
   - 添加数据加载提示
   - 添加空数据提示
   - 添加错误提示
   - 优化交互反馈

5. **Git 提交**: `[产品单耗] 前端对接后端 API`

### 阶段 5: 集成测试 (QA Engineer)

**当前阶段**: 手动测试

**测试步骤**:

1. **启动服务**:
   ```bash
   # 启动后端
   cd jeecg-boot
   npm run dev  # 或 start-dev.bat
   
   # 启动前端
   cd jeecgboot-vue3
   pnpm dev
   ```

2. **登录系统**:
   - 访问 http://127.0.0.1:3100
   - 用户名: xdadmin
   - 密码: xd@123456

3. **导航到产品单耗分析**:
   - 找到"能源分析"菜单
   - 点击"产品单耗分析"

4. **测试功能点**:
   
   **4.1 左侧树测试**:
   - [ ] 树形数据正确加载
   - [ ] 节点展开/收起正常
   - [ ] 节点选择后数据更新
   - [ ] 搜索功能正常

   **4.2 顶部筛选测试**:
   - [ ] 时间维度切换正常 (日/月/年)
   - [ ] 日期选择器正常
   - [ ] 查询按钮触发数据更新

   **4.3 数据卡片测试**:
   - [ ] 5 个指标卡片显示正确
   - [ ] 数值格式化正确
   - [ ] 数值与后端返回一致

   **4.4 图表测试**:
   - [ ] 饼图正确显示
   - [ ] 折线图正确显示
   - [ ] 柱状图正确显示
   - [ ] 排名图正确显示
   - [ ] 图表数据与后端返回一致
   - [ ] 图表交互正常 (悬停、点击)

   **4.5 表格测试**:
   - [ ] 表格数据正确显示
   - [ ] 分页功能正常
   - [ ] 环比数据显示正确 (红升绿降)
   - [ ] 导出按钮功能正常

   **4.6 异常情况测试**:
   - [ ] 无数据时的提示
   - [ ] 网络错误时的提示
   - [ ] 加载状态提示

5. **数据准确性验证**:
   
   手动计算验证:
   ```
   单位产品能耗 = 总能耗 ÷ 合格产量
   合格率 = 合格产量 ÷ 总产量 × 100%
   环比 = (本期单耗 - 上期单耗) ÷ 上期单耗 × 100%
   ```

6. **生成测试报告**:
   - 测试通过的功能点
   - 发现的问题列表
   - 问题截图
   - 修复建议

### 阶段 6: 问题修复循环

- 如果测试发现问题,QA 报告给相应工程师
- 工程师修复后重新测试
- 重复直到所有测试通过

### 阶段 7: 代码审查和提交

1. Team Lead 审查所有代码变更
2. 确认无误后合并到主分支
3. 创建最终 Git commit (需要 Team Lead 批准)
4. 更新开发文档

## 技术约束

### 后端开发规范
- 使用 Lombok 注解 (`@Data`, `@Slf4j`)
- RESTful API 设计
- MyBatis-Plus 进行数据库操作
- 参考 `TeamEnergyController` 的实现风格
- 添加详细日志记录
- 完善异常处理

### 前端开发规范
- 必须使用 Composition API
- 使用 `const` 定义方法
- 事件处理器以 "handle" 开头
- 优先使用 Tailwind CSS
- 添加 TypeScript 类型定义
- 参考 `Energy_Process_Consumption` 的组件结构

### Git 工作流
- 分支名称: `feature/product-energy-consumption`
- 提交信息格式: `[产品单耗] 简短描述`
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
- 工序单耗分析: `Energy_Process_Consumption/`
- 班组用能统计: `Team_Energy/`
- 维度树组件: 查看其他功能的左侧维度列表实现

### 数据库文档
- `system_Remark/能碳管理平台能管部份开发说明_extracted.txt`
- `system_Remark/数据库结构与测试数据含jeecgboot本身的结构与数据.sql`

### 现有表结构
- `tb_module` - 仪表信息
- `tb_ep_equ_energy_daycount` - 日能耗统计
- `tb_ep_equ_energy_monthcount` - 月能耗统计
- `tb_ep_equ_energy_yearcount` - 年能耗统计
- `tb_team_info` - 班组信息
- `sys_depart` - 组织机构

## 核心业务逻辑

### 数据流程

```
1. 生产过程
   ├─ 产品生产 → 记录产量数据 (tb_product_production)
   ├─ 仪表采集 → 能耗数据 (tb_equ_ele_data, tb_equ_energy_data)
   └─ 定时统计 → 日/月/年统计 (tb_ep_equ_energy_*count)

2. 产品单耗计算
   ├─ 关联产品工序 (tb_product_process_rel)
   ├─ 汇总工序能耗 (通过仪表 module_ids)
   ├─ 获取产品产量 (tb_product_production)
   └─ 计算单位产品能耗 = 总能耗 ÷ 合格产量

3. 数据展示
   ├─ 统计卡片: 汇总数据
   ├─ 饼图: 能耗分布
   ├─ 折线图: 单耗趋势
   ├─ 柱状图: 产量能耗对比
   ├─ 排名图: 单耗排名
   └─ 表格: 明细数据
```

### 关键计算公式

```sql
-- 单位产品能耗
SELECT 
  product_code,
  SUM(total_consumption) / SUM(qualified_production) AS unit_consumption
FROM tb_product_energy_consumption
WHERE stat_date BETWEEN ? AND ?
GROUP BY product_code;

-- 合格率
SELECT 
  product_code,
  (SUM(qualified_production) / SUM(total_production)) * 100 AS qualification_rate
FROM tb_product_production
WHERE production_date BETWEEN ? AND ?
GROUP BY product_code;

-- 环比计算 (需要两个时间段的数据)
WITH current_period AS (
  SELECT product_code, AVG(unit_consumption) AS current_value
  FROM tb_product_energy_consumption
  WHERE stat_date BETWEEN ? AND ?
  GROUP BY product_code
),
previous_period AS (
  SELECT product_code, AVG(unit_consumption) AS previous_value
  FROM tb_product_energy_consumption
  WHERE stat_date BETWEEN ? AND ?
  GROUP BY product_code
)
SELECT 
  c.product_code,
  ((c.current_value - p.previous_value) / p.previous_value) * 100 AS chain_ratio
FROM current_period c
JOIN previous_period p ON c.product_code = p.product_code;
```

## 成功标准

- [ ] 数据库表设计完成并审核通过
- [ ] 建表 SQL 和测试数据已执行
- [ ] 后端 7 个 API 接口开发完成
- [ ] 后端单元测试通过
- [ ] 前端对接完成
- [ ] 左侧产品分类树正常工作
- [ ] 5 个数据卡片正确显示
- [ ] 4 个图表正确显示
- [ ] 数据表格和分页正常
- [ ] 导出数据功能正常
- [ ] 所有手动测试通过
- [ ] 数据计算准确无误
- [ ] 代码已提交到 Git

## 启动命令

### 开发环境启动

```bash
# 后端启动
cd E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot
npm run dev  # 或 start-dev.bat (Windows)

# 前端启动
cd E:\workspace\EMSProject_jeecg\JeecgBoot\jeecgboot-vue3
pnpm dev
```

### 数据库操作

```bash
# 连接数据库
mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@

# 使用数据库
USE emsproject_jeecg;

# 执行 SQL 文件
SOURCE E:/path/to/001_create_product_tables.sql;
SOURCE E:/path/to/002_insert_product_test_data.sql;
```

## 浏览器测试配置 (Playwright MCP)

### 安装配置 Playwright

**注意**: 当前阶段使用手动测试,Playwright 配置为可选项。

如需配置 Playwright MCP:

1. **安装 Playwright**:
   ```bash
   npm install -D @playwright/test
   npx playwright install
   ```

2. **配置 MCP Server**:
   在 Claude Desktop 配置中添加 Playwright MCP
   
3. **编写测试脚本**:
   创建 `tests/product-energy.spec.ts`

4. **运行测试**:
   ```bash
   npx playwright test
   ```

详细配置步骤请参考: https://playwright.dev/docs/intro

## 快速启动提示词

复制以下内容到 OpenCode 中启动开发:

```
请帮我开发产品单耗分析功能。

项目信息:
- 前端页面已创建: jeecgboot-vue3/src/views/EnergyAnalysis/Energy_Product_Consumption/
- 参考实现: Energy_Process_Consumption 工序单耗分析
- 数据库: 127.0.0.1:3306/emsproject_jeecg (root/Abc123456@)
- 后端: http://127.0.0.1:8080/jeecg-boot
- 前端: http://127.0.0.1:3100
- 测试账号: xdadmin / xd@123456

请使用 Agent Teams 进行开发:
1. 创建团队: product-energy-dev
2. 分配角色: 系统设计师、数据库工程师、后端工程师、前端工程师、测试工程师
3. 按阶段执行: 设计 -> 数据库 -> 后端 -> 前端 -> 测试
4. 敏感操作 (DELETE/DROP/文件删除) 需要我批准
5. 使用手动测试验证功能
6. 完成后提交 Git

详细开发文档请参考: system_Remark/产品单耗分析_AgentTeams开发提示词.md
```

---

**文档版本**: 1.0
**创建时间**: 2026-02-16
**适用范围**: 产品单耗分析功能开发
**负责人**: AI Assistant
