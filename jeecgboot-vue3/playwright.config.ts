import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 配置文件
 * 用于能源管理系统的端到端测试
 */
export default defineConfig({
  // 测试文件目录
  testDir: './tests/e2e',
  
  // 测试超时时间 (30秒)
  timeout: 30 * 1000,
  
  // 期望超时时间 (5秒)
  expect: {
    timeout: 5000
  },
  
  // 失败时重试次数
  retries: process.env.CI ? 2 : 0,
  
  // 并行运行的 worker 数量
  workers: process.env.CI ? 1 : undefined,
  
  // 测试报告配置
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['list']
  ],
  
  // 测试结果目录
  outputDir: 'test-results/',
  
  // 全局配置
  use: {
    // 基础 URL
    baseURL: 'http://127.0.0.1:3100',
    
    // 测试追踪 (失败时保留)
    trace: 'retain-on-failure',
    
    // 截图 (仅失败时)
    screenshot: 'only-on-failure',
    
    // 视频录制 (仅失败时)
    video: 'retain-on-failure',
    
    // 浏览器上下文配置
    viewport: { width: 1920, height: 1080 },
    
    // 忽略 HTTPS 错误
    ignoreHTTPSErrors: true,
    
    // 导航超时
    navigationTimeout: 15000,
    
    // 操作超时
    actionTimeout: 10000,
  },

  // 测试项目配置
  projects: [
    // Chromium 浏览器
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // Firefox 浏览器 (可选)
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },

    // WebKit 浏览器 (可选)
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    // 移动端模拟 (可选)
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
  ],

  // Web Server 配置 (自动启动前端服务)
  webServer: {
    command: 'pnpm dev',
    url: 'http://127.0.0.1:3100',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
