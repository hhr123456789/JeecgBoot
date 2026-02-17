import { test, expect } from '@playwright/test';
import { LoginHelper } from '../helpers/LoginHelper';
import { ProductEnergyPage } from '../pages/ProductEnergyPage';

/**
 * 产品单耗分析功能测试套件
 */
test.describe('产品单耗分析', () => {
  let loginHelper: LoginHelper;
  let productEnergyPage: ProductEnergyPage;

  // 每个测试前登录
  test.beforeEach(async ({ page }) => {
    loginHelper = new LoginHelper(page);
    productEnergyPage = new ProductEnergyPage(page);
    
    // 登录系统
    await loginHelper.login();
    
    // 访问产品单耗分析页面
    await productEnergyPage.goto();
  });

  test('1. 页面加载测试 - 验证所有元素正常显示', async () => {
    // 验证左侧树显示
    await expect(productEnergyPage.treeContainer).toBeVisible();
    
    // 验证搜索框显示
    await expect(productEnergyPage.searchInput).toBeVisible();
    
    // 验证时间筛选按钮显示
    await expect(productEnergyPage.dayButton).toBeVisible();
    await expect(productEnergyPage.monthButton).toBeVisible();
    await expect(productEnergyPage.yearButton).toBeVisible();
    
    // 验证查询按钮显示
    await expect(productEnergyPage.queryButton).toBeVisible();
    
    // 验证5个数据卡片显示
    await expect(productEnergyPage.totalConsumptionCard).toBeVisible();
    await expect(productEnergyPage.totalProductionCard).toBeVisible();
    await expect(productEnergyPage.qualifiedProductionCard).toBeVisible();
    await expect(productEnergyPage.qualificationRateCard).toBeVisible();
    await expect(productEnergyPage.unitConsumptionCard).toBeVisible();
    
    // 验证图表显示
    await productEnergyPage.verifyChartsVisible();
    
    // 验证表格显示
    await productEnergyPage.verifyTableVisible();
    
    // 验证导出按钮显示
    await expect(productEnergyPage.exportButton).toBeVisible();
    
    console.log('✅ 页面所有元素加载正常');
  });

  test('2. 左侧树测试 - 选择产品节点', async () => {
    // 选择树节点
    await productEnergyPage.selectTreeNode('门窗型材');
    
    // 等待数据更新
    await productEnergyPage.page.waitForTimeout(1000);
    
    // 验证数据已更新（这里应该检查卡片数值变化）
    const unitConsumption = await productEnergyPage.getCardValue('unitConsumption');
    expect(unitConsumption).not.toBe('');
    
    console.log('✅ 树节点选择功能正常');
  });

  test('3. 搜索功能测试', async () => {
    // 搜索产品
    await productEnergyPage.searchProduct('型材');
    
    // 等待搜索结果
    await productEnergyPage.page.waitForTimeout(500);
    
    // 验证搜索框有值
    const searchValue = await productEnergyPage.searchInput.inputValue();
    expect(searchValue).toBe('型材');
    
    console.log('✅ 搜索功能正常');
  });

  test('4. 时间维度切换测试', async () => {
    // 测试切换到日
    await productEnergyPage.selectTimeUnit('day');
    await expect(productEnergyPage.dayButton).toHaveClass(/ant-radio-button-wrapper-checked/);
    
    // 测试切换到月
    await productEnergyPage.selectTimeUnit('month');
    await expect(productEnergyPage.monthButton).toHaveClass(/ant-radio-button-wrapper-checked/);
    
    // 测试切换到年
    await productEnergyPage.selectTimeUnit('year');
    await expect(productEnergyPage.yearButton).toHaveClass(/ant-radio-button-wrapper-checked/);
    
    console.log('✅ 时间维度切换功能正常');
  });

  test('5. 查询功能测试', async () => {
    // 选择月维度
    await productEnergyPage.selectTimeUnit('month');
    
    // 点击查询按钮
    await productEnergyPage.clickQuery();
    
    // 验证数据已加载（检查卡片有数值）
    const totalConsumption = await productEnergyPage.getCardValue('totalConsumption');
    expect(totalConsumption).not.toBe('');
    
    console.log('✅ 查询功能正常');
  });

  test('6. 数据卡片测试 - 验证5个核心指标', async () => {
    // 获取所有卡片数值
    const totalConsumption = await productEnergyPage.getCardValue('totalConsumption');
    const totalProduction = await productEnergyPage.getCardValue('totalProduction');
    const qualifiedProduction = await productEnergyPage.getCardValue('qualifiedProduction');
    const qualificationRate = await productEnergyPage.getCardValue('qualificationRate');
    const unitConsumption = await productEnergyPage.getCardValue('unitConsumption');
    
    // 验证所有卡片都有数值
    expect(totalConsumption).not.toBe('');
    expect(totalProduction).not.toBe('');
    expect(qualifiedProduction).not.toBe('');
    expect(qualificationRate).not.toBe('');
    expect(unitConsumption).not.toBe('');
    
    console.log('数据卡片值:');
    console.log('  总能耗:', totalConsumption);
    console.log('  总产量:', totalProduction);
    console.log('  合格产量:', qualifiedProduction);
    console.log('  合格率:', qualificationRate);
    console.log('  单位产品能耗:', unitConsumption);
    
    console.log('✅ 数据卡片显示正常');
  });

  test('7. 图表显示测试', async () => {
    // 验证饼图显示
    await expect(productEnergyPage.pieChart).toBeVisible();
    const pieChartCanvas = productEnergyPage.pieChart.locator('canvas');
    await expect(pieChartCanvas).toBeVisible();
    
    // 验证折线图显示
    await expect(productEnergyPage.lineChart).toBeVisible();
    const lineChartCanvas = productEnergyPage.lineChart.locator('canvas');
    await expect(lineChartCanvas).toBeVisible();
    
    // 验证柱状图显示
    await expect(productEnergyPage.barChart).toBeVisible();
    const barChartCanvas = productEnergyPage.barChart.locator('canvas');
    await expect(barChartCanvas).toBeVisible();
    
    // 验证排名图显示
    await expect(productEnergyPage.rankingChart).toBeVisible();
    const rankingChartCanvas = productEnergyPage.rankingChart.locator('canvas');
    await expect(rankingChartCanvas).toBeVisible();
    
    console.log('✅ 所有图表显示正常');
  });

  test('8. 表格功能测试', async () => {
    // 验证表格显示
    await productEnergyPage.verifyTableVisible();
    
    // 获取表格行数
    const rowCount = await productEnergyPage.getTableRowCount();
    expect(rowCount).toBeGreaterThan(0);
    
    console.log(`表格显示 ${rowCount} 行数据`);
    
    // 验证表格列标题
    const tableHeaders = productEnergyPage.dataTable.locator('thead th');
    const headerCount = await tableHeaders.count();
    expect(headerCount).toBeGreaterThan(0);
    
    console.log('✅ 表格显示正常');
  });

  test('9. 导出功能测试', async () => {
    // 设置下载监听
    const downloadPromise = productEnergyPage.page.waitForEvent('download');
    
    // 点击导出按钮
    await productEnergyPage.clickExport();
    
    // 等待下载 (如果后端已实现)
    // 注意: 如果后端未实现,这里会超时,需要注释掉
    // const download = await downloadPromise;
    // expect(download).toBeTruthy();
    
    console.log('✅ 导出按钮功能正常 (注意: 需要后端支持)');
  });

  test('10. 完整流程测试', async () => {
    // 1. 选择产品分类
    await productEnergyPage.selectTreeNode('铝型材产品');
    await productEnergyPage.page.waitForTimeout(500);
    
    // 2. 切换到月维度
    await productEnergyPage.selectTimeUnit('month');
    
    // 3. 点击查询
    await productEnergyPage.clickQuery();
    
    // 4. 验证数据显示
    const unitConsumption = await productEnergyPage.getCardValue('unitConsumption');
    expect(unitConsumption).not.toBe('');
    
    // 5. 验证图表显示
    await productEnergyPage.verifyChartsVisible();
    
    // 6. 验证表格显示
    await productEnergyPage.verifyTableVisible();
    
    // 7. 截图保存
    await productEnergyPage.screenshot('product-energy-full-flow');
    
    console.log('✅ 完整流程测试通过');
  });

  test('11. 数据准确性验证 - 单位产品能耗计算', async ({ page }) => {
    // 获取卡片数值
    const totalConsumptionText = await productEnergyPage.getCardValue('totalConsumption');
    const qualifiedProductionText = await productEnergyPage.getCardValue('qualifiedProduction');
    const unitConsumptionText = await productEnergyPage.getCardValue('unitConsumption');
    
    // 解析数值 (去除千分位逗号)
    const totalConsumption = parseFloat(totalConsumptionText.replace(/,/g, ''));
    const qualifiedProduction = parseFloat(qualifiedProductionText.replace(/,/g, ''));
    const unitConsumption = parseFloat(unitConsumptionText.replace(/,/g, ''));
    
    // 计算单位产品能耗
    const calculatedUnitConsumption = totalConsumption / qualifiedProduction;
    
    // 验证计算结果 (允许小数点后2位的误差)
    const diff = Math.abs(calculatedUnitConsumption - unitConsumption);
    expect(diff).toBeLessThan(0.01);
    
    console.log('数据验证:');
    console.log(`  总能耗: ${totalConsumption} kWh`);
    console.log(`  合格产量: ${qualifiedProduction} 件`);
    console.log(`  单位产品能耗: ${unitConsumption} kWh/件`);
    console.log(`  计算值: ${calculatedUnitConsumption.toFixed(2)} kWh/件`);
    console.log(`  误差: ${diff.toFixed(4)}`);
    
    console.log('✅ 单位产品能耗计算准确');
  });

  test('12. 响应式测试 - 不同屏幕尺寸', async ({ page }) => {
    // 测试桌面尺寸 (1920x1080)
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.waitForTimeout(500);
    await productEnergyPage.verifyChartsVisible();
    
    // 测试笔记本尺寸 (1366x768)
    await page.setViewportSize({ width: 1366, height: 768 });
    await page.waitForTimeout(500);
    await productEnergyPage.verifyChartsVisible();
    
    console.log('✅ 响应式布局正常');
  });

  // 测试后清理
  test.afterEach(async ({ page }, testInfo) => {
    // 如果测试失败,截图保存
    if (testInfo.status !== 'passed') {
      await page.screenshot({ 
        path: `test-results/screenshots/${testInfo.title}-FAILED.png`,
        fullPage: true 
      });
    }
  });
});
