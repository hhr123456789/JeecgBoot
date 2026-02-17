import { Page, expect } from '@playwright/test';

/**
 * 登录助手类
 * 提供登录相关的辅助方法
 */
export class LoginHelper {
  constructor(private page: Page) {}

  /**
   * 执行登录操作
   * @param username 用户名 (默认: xdadmin)
   * @param password 密码 (默认: xd@123456)
   */
  async login(username: string = 'xdadmin', password: string = 'xd@123456') {
    // 访问登录页
    await this.page.goto('/');
    
    // 等待登录页面加载
    await this.page.waitForSelector('input[placeholder*="用户名"]', { timeout: 10000 });
    
    // 输入用户名
    await this.page.fill('input[placeholder*="用户名"]', username);
    
    // 输入密码
    await this.page.fill('input[type="password"]', password);
    
    // 点击登录按钮
    await this.page.click('button:has-text("登录")');
    
    // 等待登录成功 (等待首页元素出现)
    await this.page.waitForLoadState('networkidle');
    await this.page.waitForSelector('.ant-layout', { timeout: 15000 });
    
    // 验证登录成功
    const currentUrl = this.page.url();
    expect(currentUrl).not.toContain('/user/login');
  }

  /**
   * 退出登录
   */
  async logout() {
    // 点击用户头像下拉菜单
    await this.page.click('.ant-dropdown-trigger');
    
    // 点击退出登录
    await this.page.click('text=退出登录');
    
    // 等待跳转到登录页
    await this.page.waitForURL('**/user/login');
  }

  /**
   * 检查是否已登录
   */
  async isLoggedIn(): Promise<boolean> {
    const currentUrl = this.page.url();
    return !currentUrl.includes('/user/login');
  }
}
