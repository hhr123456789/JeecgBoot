# Playwright 自动化测试配置与使用指南

## 📋 目录

- [概述](#概述)
- [安装配置](#安装配置)
- [运行测试](#运行测试)
- [测试用例说明](#测试用例说明)
- [常见问题](#常见问题)
- [高级用法](#高级用法)

---

## 📖 概述

本项目已集成 Playwright 端到端测试框架,用于自动化测试能源管理系统的前端功能。

### 已完成配置

✅ Playwright 配置文件 (`playwright.config.ts`)  
✅ 登录助手类 (`tests/helpers/LoginHelper.ts`)  
✅ 产品单耗分析页面对象 (`tests/pages/ProductEnergyPage.ts`)  
✅ 产品单耗分析测试用例 (`tests/e2e/product-energy.spec.ts`)  
✅ NPM 测试脚本命令

### 测试覆盖范围

当前已编写 **12 个测试用例**,覆盖产品单耗分析的所有功能:

1. 页面加载测试
2. 左侧树测试
3. 搜索功能测试
4. 时间维度切换测试
5. 查询功能测试
6. 数据卡片测试
7. 图表显示测试
8. 表格功能测试
9. 导出功能测试
10. 完整流程测试
11. 数据准确性验证
12. 响应式测试

---

## 🚀 安装配置

### 步骤 1: 安装 Playwright

在 `jeecgboot-vue3` 目录下运行:

```bash
# 使用 pnpm 安装 Playwright
pnpm add -D @playwright/test

# 安装浏览器 (Chromium, Firefox, WebKit)
pnpm test:install
```

或者手动安装:

```bash
npx playwright install
```

### 步骤 2: 验证安装

检查 Playwright 是否安装成功:

```bash
npx playwright --version
```

应该显示类似:
```
Version 1.42.0
```

### 步骤 3: 目录结构

安装完成后,项目结构如下:

```
jeecgboot-vue3/
├── playwright.config.ts           # Playwright 配置文件 ✅
├── tests/                          # 测试目录 ✅
│   ├── e2e/                        # E2E 测试用例
│   │   └── product-energy.spec.ts # 产品单耗分析测试 ✅
│   ├── helpers/                    # 测试辅助工具
│   │   └── LoginHelper.ts         # 登录助手 ✅
│   └── pages/                      # 页面对象模型
│       └── ProductEnergyPage.ts   # 产品单耗分析页面对象 ✅
├── playwright-report/              # 测试报告 (自动生成)
└── test-results/                   # 测试结果 (自动生成)
    └── screenshots/                # 测试截图
```

---

## ▶️ 运行测试

### 前提条件

**重要**: 测试前必须确保前后端服务都在运行!

1. **启动后端服务**:
   ```bash
   cd E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot
   npm run dev  # 或 start-dev.bat (Windows)
   ```

2. **启动前端服务**:
   ```bash
   cd E:\workspace\EMSProject_jeecg\JeecgBoot\jeecgboot-vue3
   pnpm dev
   ```

3. **验证服务运行**:
   - 前端: http://127.0.0.1:3100
   - 后端: http://127.0.0.1:8080/jeecg-boot

### 运行所有测试

```bash
cd jeecgboot-vue3

# 运行所有测试 (无头模式)
pnpm test:e2e
```

### 运行特定测试

```bash
# 只运行产品单耗分析测试
pnpm test:e2e:product

# 或者指定完整文件名
npx playwright test product-energy.spec.ts
```

### 以有头模式运行 (可见浏览器)

```bash
# 运行时显示浏览器窗口
pnpm test:e2e:headed

# 或
npx playwright test --headed
```

### 使用 UI 模式运行 (推荐)

```bash
# 打开 Playwright UI 界面
pnpm test:e2e:ui

# 或
npx playwright test --ui
```

**UI 模式优点**:
- 可视化测试执行过程
- 可以逐步调试测试
- 查看每一步的截图和DOM快照
- 时间线回放功能

### 运行单个测试用例

```bash
# 运行特定测试 (通过测试名称匹配)
npx playwright test --grep "页面加载测试"

# 运行特定测试文件的特定用例
npx playwright test product-energy.spec.ts --grep "数据卡片测试"
```

---

## 📊 查看测试报告

### HTML 报告

测试完成后,会自动生成 HTML 报告:

```bash
# 查看测试报告
npx playwright show-report
```

浏览器会自动打开,显示详细的测试报告,包括:
- 测试执行时间
- 通过/失败的测试用例
- 失败测试的截图
- 测试步骤的详细日志
- 视频录制 (如果启用)

### JSON 报告

测试结果也会以 JSON 格式保存在 `test-results/results.json`,方便集成到 CI/CD 流程。

---

## 🔍 测试用例说明

### 1. 页面加载测试

**目的**: 验证页面所有元素是否正确加载

**检查项**:
- 左侧树形菜单
- 搜索框
- 时间筛选按钮 (日/月/年)
- 查询按钮
- 5 个数据卡片
- 4 个图表
- 数据表格
- 导出按钮

**预期结果**: 所有元素可见

---

### 2. 左侧树测试

**目的**: 验证产品分类树的选择功能

**操作步骤**:
1. 点击树节点 "门窗型材"
2. 等待数据更新

**预期结果**: 数据卡片数值更新

---

### 3. 搜索功能测试

**目的**: 验证产品搜索功能

**操作步骤**:
1. 在搜索框输入 "型材"
2. 等待搜索结果

**预期结果**: 搜索框显示输入的关键词

---

### 4. 时间维度切换测试

**目的**: 验证时间维度切换功能 (日/月/年)

**操作步骤**:
1. 点击 "日" 按钮
2. 点击 "月" 按钮
3. 点击 "年" 按钮

**预期结果**: 对应按钮显示选中状态

---

### 5. 查询功能测试

**目的**: 验证查询按钮功能

**操作步骤**:
1. 选择月维度
2. 点击查询按钮
3. 等待数据加载

**预期结果**: 数据卡片显示数值

---

### 6. 数据卡片测试

**目的**: 验证 5 个核心指标卡片

**检查项**:
- 总能耗 (kWh)
- 总产量 (件)
- 合格产量 (件)
- 合格率 (%)
- 单位产品能耗 (kWh/件)

**预期结果**: 所有卡片都有数值

---

### 7. 图表显示测试

**目的**: 验证 4 个图表是否正确渲染

**检查项**:
- 产品能耗分布饼图
- 产品单耗趋势折线图
- 产量与能耗对比柱状图
- 产品单耗排名横向柱状图

**预期结果**: 所有图表的 Canvas 元素可见

---

### 8. 表格功能测试

**目的**: 验证数据表格显示

**检查项**:
- 表格可见
- 表格有数据行
- 表格有列标题

**预期结果**: 表格显示多行数据

---

### 9. 导出功能测试

**目的**: 验证导出按钮功能

**操作步骤**:
1. 点击 "导出数据" 按钮
2. 监听下载事件

**预期结果**: 触发下载 (需要后端支持)

**注意**: 如果后端未实现,此测试会提示需要后端支持

---

### 10. 完整流程测试

**目的**: 验证完整的用户操作流程

**操作步骤**:
1. 选择产品分类 "铝型材产品"
2. 切换到月维度
3. 点击查询
4. 验证数据显示
5. 验证图表显示
6. 验证表格显示
7. 保存截图

**预期结果**: 所有步骤成功执行

---

### 11. 数据准确性验证

**目的**: 验证单位产品能耗计算公式

**计算公式**:
```
单位产品能耗 = 总能耗 ÷ 合格产量
```

**操作步骤**:
1. 获取总能耗、合格产量、单位产品能耗
2. 手动计算单位产品能耗
3. 对比计算值与显示值

**预期结果**: 误差小于 0.01

---

### 12. 响应式测试

**目的**: 验证不同屏幕尺寸下的显示

**测试尺寸**:
- 桌面: 1920x1080
- 笔记本: 1366x768

**预期结果**: 所有图表在不同尺寸下正常显示

---

## ❓ 常见问题

### Q1: 测试失败提示 "Cannot find element"

**原因**: 页面元素定位器可能需要调整

**解决方法**:
1. 使用 UI 模式运行测试,查看元素定位
2. 检查页面是否正常加载
3. 调整 `ProductEnergyPage.ts` 中的定位器

### Q2: 测试超时

**原因**: 
- 服务器响应慢
- 网络延迟
- 页面加载慢

**解决方法**:
```typescript
// 增加超时时间 (在 playwright.config.ts 中)
timeout: 60 * 1000,  // 60秒
```

### Q3: 浏览器无法启动

**原因**: Playwright 浏览器未安装

**解决方法**:
```bash
pnpm test:install
# 或
npx playwright install
```

### Q4: 登录失败

**原因**: 
- 登录页面元素变化
- 用户名密码错误
- 后端服务未启动

**解决方法**:
1. 检查后端服务: http://127.0.0.1:8080/jeecg-boot
2. 验证账号: xdadmin / xd@123456
3. 检查 `LoginHelper.ts` 中的元素定位器

### Q5: 如何调试测试

**方法 1**: 使用 UI 模式
```bash
pnpm test:e2e:ui
```

**方法 2**: 使用 `page.pause()`
```typescript
await page.pause();  // 暂停测试,打开 Playwright Inspector
```

**方法 3**: 使用 `--debug` 模式
```bash
npx playwright test --debug
```

---

## 🔧 高级用法

### 1. 生成测试代码

Playwright 可以录制用户操作并自动生成测试代码:

```bash
npx playwright codegen http://127.0.0.1:3100
```

这会打开浏览器和 Playwright Inspector,录制您的操作并生成代码。

### 2. 配置多个测试环境

在 `playwright.config.ts` 中添加:

```typescript
export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL || 'http://127.0.0.1:3100',
  },
});
```

运行时指定环境:

```bash
BASE_URL=http://staging.example.com pnpm test:e2e
```

### 3. 并行运行测试

```bash
# 使用 4 个 worker 并行运行
npx playwright test --workers=4
```

### 4. 只运行失败的测试

```bash
# 重新运行上次失败的测试
npx playwright test --last-failed
```

### 5. 生成测试报告

```bash
# 生成 Allure 报告
npx playwright test --reporter=allure-playwright

# 生成 JUnit XML 报告
npx playwright test --reporter=junit
```

### 6. 使用自定义 Fixture

创建可复用的测试固件:

```typescript
// tests/fixtures/auth.fixture.ts
import { test as base } from '@playwright/test';
import { LoginHelper } from '../helpers/LoginHelper';

export const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    const loginHelper = new LoginHelper(page);
    await loginHelper.login();
    await use(page);
  },
});
```

使用:

```typescript
import { test } from '../fixtures/auth.fixture';

test('需要登录的测试', async ({ authenticatedPage }) => {
  // authenticatedPage 已经登录
});
```

### 7. 跨浏览器测试

同时在多个浏览器中测试:

```bash
npx playwright test --project=chromium --project=firefox --project=webkit
```

---

## 📝 最佳实践

### 1. 使用页面对象模型 (POM)

✅ **推荐**:
```typescript
// 封装在 Page Object 中
await productEnergyPage.selectTreeNode('门窗型材');
```

❌ **不推荐**:
```typescript
// 直接在测试中操作 DOM
await page.click('.ant-tree >> text=门窗型材');
```

### 2. 避免硬编码等待时间

✅ **推荐**:
```typescript
await page.waitForSelector('.data-card', { state: 'visible' });
```

❌ **不推荐**:
```typescript
await page.waitForTimeout(3000);
```

### 3. 使用有意义的断言

✅ **推荐**:
```typescript
const value = await productEnergyPage.getCardValue('unitConsumption');
expect(value).toMatch(/\d+(\.\d+)?/);  // 验证是数字格式
```

❌ **不推荐**:
```typescript
expect(value).not.toBe('');  // 太宽松
```

### 4. 清理测试数据

每个测试应该独立,不依赖其他测试的数据:

```typescript
test.afterEach(async () => {
  // 清理测试数据
});
```

### 5. 合理组织测试用例

使用 `test.describe` 分组:

```typescript
test.describe('产品单耗分析', () => {
  test.describe('基础功能', () => {
    test('页面加载', async () => {});
    test('查询功能', async () => {});
  });
  
  test.describe('高级功能', () => {
    test('数据导出', async () => {});
  });
});
```

---

## 🎯 下一步

### 待添加的测试

- [ ] 班组用能统计测试
- [ ] 工序单耗分析测试
- [ ] 班次用能统计测试
- [ ] 企业总览测试
- [ ] 企业报表测试

### CI/CD 集成

将测试集成到 GitHub Actions / GitLab CI:

```yaml
# .github/workflows/test.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      - run: pnpm install
      - run: pnpm test:install
      - run: pnpm test:e2e
```

---

## 📚 参考资料

- [Playwright 官方文档](https://playwright.dev/)
- [Playwright API 参考](https://playwright.dev/docs/api/class-playwright)
- [最佳实践指南](https://playwright.dev/docs/best-practices)
- [CI/CD 集成](https://playwright.dev/docs/ci)

---

**文档创建时间**: 2026-02-16  
**适用版本**: Playwright 1.42+  
**维护人**: AI Assistant
