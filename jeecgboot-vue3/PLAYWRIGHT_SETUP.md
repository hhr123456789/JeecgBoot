# Playwright 快速安装指南

## 一键安装

在 `jeecgboot-vue3` 目录下运行以下命令:

```bash
# 1. 安装 Playwright
pnpm add -D @playwright/test

# 2. 安装浏览器
pnpm test:install
```

## 验证安装

```bash
npx playwright --version
```

## 快速运行测试

**重要**: 确保前后端服务都在运行!

```bash
# 前端服务
pnpm dev

# 运行测试 (在新终端)
pnpm test:e2e:ui
```

## 查看详细文档

完整使用说明请查看: `tests/README.md`

---

如有问题,请联系开发团队。
