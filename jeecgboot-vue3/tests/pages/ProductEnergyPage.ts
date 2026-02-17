import { Page, Locator, expect } from '@playwright/test';

/**
 * 产品单耗分析页面对象
 * 封装页面元素和操作方法
 */
export class ProductEnergyPage {
  readonly page: Page;
  
  // 左侧树形菜单
  readonly treeContainer: Locator;
  readonly searchInput: Locator;
  
  // 顶部筛选
  readonly dayButton: Locator;
  readonly monthButton: Locator;
  readonly yearButton: Locator;
  readonly datePicker: Locator;
  readonly queryButton: Locator;
  
  // 数据卡片
  readonly totalConsumptionCard: Locator;
  readonly totalProductionCard: Locator;
  readonly qualifiedProductionCard: Locator;
  readonly qualificationRateCard: Locator;
  readonly unitConsumptionCard: Locator;
  
  // 图表
  readonly pieChart: Locator;
  readonly lineChart: Locator;
  readonly barChart: Locator;
  readonly rankingChart: Locator;
  
  // 表格
  readonly dataTable: Locator;
  readonly exportButton: Locator;

  constructor(page: Page) {
    this.page = page;
    
    // 左侧树
    this.treeContainer = page.locator('.ant-tree');
    this.searchInput = page.locator('input[placeholder*="搜索"]');
    
    // 顶部筛选
    this.dayButton = page.locator('.ant-radio-button-wrapper:has-text("日")');
    this.monthButton = page.locator('.ant-radio-button-wrapper:has-text("月")');
    this.yearButton = page.locator('.ant-radio-button-wrapper:has-text("年")');
    this.datePicker = page.locator('.ant-picker');
    this.queryButton = page.locator('button:has-text("查询")');
    
    // 数据卡片
    this.totalConsumptionCard = page.locator('text=总能耗').locator('xpath=ancestor::div[contains(@class, "bg-white")]');
    this.totalProductionCard = page.locator('text=总产量').locator('xpath=ancestor::div[contains(@class, "bg-white")]');
    this.qualifiedProductionCard = page.locator('text=合格产量').locator('xpath=ancestor::div[contains(@class, "bg-white")]');
    this.qualificationRateCard = page.locator('text=合格率').locator('xpath=ancestor::div[contains(@class, "bg-white")]');
    this.unitConsumptionCard = page.locator('text=单位产品能耗').locator('xpath=ancestor::div[contains(@class, "bg-white")]');
    
    // 图表
    this.pieChart = page.locator('text=产品能耗分布').locator('xpath=following-sibling::div');
    this.lineChart = page.locator('text=产品单耗趋势').locator('xpath=following-sibling::div');
    this.barChart = page.locator('text=产量与能耗对比').locator('xpath=following-sibling::div');
    this.rankingChart = page.locator('text=产品单耗排名').locator('xpath=following-sibling::div');
    
    // 表格
    this.dataTable = page.locator('.ant-table');
    this.exportButton = page.locator('button:has-text("导出数据")');
  }

  /**
   * 访问产品单耗分析页面
   */
  async goto() {
    await this.page.goto('/#/energy-analysis/product-consumption');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 选择树节点
   * @param nodeText 节点文本
   */
  async selectTreeNode(nodeText: string) {
    await this.treeContainer.locator(`text=${nodeText}`).click();
    await this.page.waitForTimeout(500); // 等待数据更新
  }

  /**
   * 搜索产品
   * @param keyword 搜索关键词
   */
  async searchProduct(keyword: string) {
    await this.searchInput.fill(keyword);
    await this.page.waitForTimeout(500);
  }

  /**
   * 选择时间维度
   * @param unit 时间单位: 'day' | 'month' | 'year'
   */
  async selectTimeUnit(unit: 'day' | 'month' | 'year') {
    switch (unit) {
      case 'day':
        await this.dayButton.click();
        break;
      case 'month':
        await this.monthButton.click();
        break;
      case 'year':
        await this.yearButton.click();
        break;
    }
    await this.page.waitForTimeout(300);
  }

  /**
   * 点击查询按钮
   */
  async clickQuery() {
    await this.queryButton.click();
    await this.page.waitForTimeout(1000); // 等待数据加载
  }

  /**
   * 获取卡片数值
   * @param cardType 卡片类型
   */
  async getCardValue(cardType: 'totalConsumption' | 'totalProduction' | 'qualifiedProduction' | 'qualificationRate' | 'unitConsumption'): Promise<string> {
    let card: Locator;
    switch (cardType) {
      case 'totalConsumption':
        card = this.totalConsumptionCard;
        break;
      case 'totalProduction':
        card = this.totalProductionCard;
        break;
      case 'qualifiedProduction':
        card = this.qualifiedProductionCard;
        break;
      case 'qualificationRate':
        card = this.qualificationRateCard;
        break;
      case 'unitConsumption':
        card = this.unitConsumptionCard;
        break;
    }
    
    const valueElement = card.locator('div[class*="bg-"]').last();
    return await valueElement.innerText();
  }

  /**
   * 验证图表是否显示
   */
  async verifyChartsVisible() {
    await expect(this.pieChart).toBeVisible();
    await expect(this.lineChart).toBeVisible();
    await expect(this.barChart).toBeVisible();
    await expect(this.rankingChart).toBeVisible();
  }

  /**
   * 验证表格是否显示
   */
  async verifyTableVisible() {
    await expect(this.dataTable).toBeVisible();
  }

  /**
   * 获取表格行数
   */
  async getTableRowCount(): Promise<number> {
    const rows = await this.dataTable.locator('tbody tr').count();
    return rows;
  }

  /**
   * 点击导出按钮
   */
  async clickExport() {
    await this.exportButton.click();
  }

  /**
   * 截图
   * @param name 截图文件名
   */
  async screenshot(name: string) {
    await this.page.screenshot({ 
      path: `test-results/screenshots/${name}.png`,
      fullPage: true 
    });
  }
}
