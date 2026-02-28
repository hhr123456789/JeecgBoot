import { test, expect, Page } from '@playwright/test';
import { LoginHelper } from '../helpers/LoginHelper';

const ALARM_RULES_URL = '/EnergyAlarm/Alarm_Rules_Settings';

async function gotoAlarmRulesPage(page: Page) {
  await page.goto(ALARM_RULES_URL);
  await page.waitForLoadState('networkidle');
  await expect(page.locator('text=新增告警规则')).toBeVisible();
}

test.describe('告警规则设置页回归', () => {
  test.beforeEach(async ({ page }) => {
    const loginHelper = new LoginHelper(page);
    await loginHelper.login('xdadmin', 'xd@123456');
    await gotoAlarmRulesPage(page);
  });

  test('新增按钮可打开弹窗', async ({ page }) => {
    const runtimeErrors: string[] = [];
    page.on('pageerror', (error) => {
      runtimeErrors.push(error.message || String(error));
    });

    const addButton = page.locator('button:has-text("新增告警规则")').first();
    await addButton.click();

    const hasDimensionTip = await page.locator('text=请先在左侧维度树中选择监控对象').isVisible();
    const hasModal = await page.locator('.ant-modal:has-text("新增告警规则")').isVisible();

    expect(hasDimensionTip || hasModal).toBeTruthy();
    expect(
      runtimeErrors.some(
        (message) =>
          message.includes('Converting circular structure to JSON') ||
          message.includes("Cannot read properties of null (reading 'parentNode')"),
      ),
    ).toBeFalsy();
  });

  test('查看与编辑操作不触发关键异常', async ({ page }) => {
    const runtimeErrors: string[] = [];
    page.on('pageerror', (error) => {
      runtimeErrors.push(error.message || String(error));
    });

    const viewLink = page.locator('a:has-text("查看")').first();
    const editLink = page.locator('a:has-text("编辑")').first();

    const hasView = await viewLink.count();
    const hasEdit = await editLink.count();
    expect(hasView).toBeGreaterThan(0);
    expect(hasEdit).toBeGreaterThan(0);

    await viewLink.click();
    await expect(page.locator('.ant-modal:has-text("查看规则")')).toBeVisible();
    await page.locator('.ant-modal-close').first().click();
    await page.waitForTimeout(300);

    await editLink.click();
    await expect(page.locator('.ant-modal:has-text("编辑规则")')).toBeVisible();
    await page.locator('.ant-modal-close').first().click();
    await page.waitForTimeout(300);

    expect(
      runtimeErrors.some(
        (message) =>
          message.includes('Converting circular structure to JSON') ||
          message.includes("Cannot read properties of null (reading 'parentNode')"),
      ),
    ).toBeFalsy();
  });
});

