# 设备负荷监控页面问题修复说明

## 修复的问题

### 1. 左边维度选择与右上仪表选择不同步
**问题描述**: 选择左侧维度树后，右上角的仪表下拉框没有正确加载对应的仪表数据。

**修复方案**:
- 优化了 `onDepartTreeSelect` 函数，支持单选和多选两种情况
- 在维度选择变化时，先清空之前的仪表数据，再加载新的仪表数据
- 仪表加载完成后，如果有选择的日期，自动触发查询

**关键代码**:
```javascript
function onDepartTreeSelect(data: any) {
  // 先清空之前的仪表选择和图表数据
  meters.value = [];
  allModules.value = [];
  selectedMeters.value = [];
  clearChartData();

  if (Array.isArray(data) && data.length > 0) {
    // 处理多选情况
    selectedDevices.value = data;
    const dimensionCodes = data.map(item => item.orgCode);
    loadModulesByDimensionCodes(dimensionCodes);
  } else if (data && data.orgCode) {
    // 处理单选情况
    selectedDevices.value = [data];
    loadModulesByDimensionCodes([data.orgCode]);
  }
}
```

### 2. 去掉"监控参数：有功功率 & 负荷率"显示
**问题描述**: 界面上显示了不需要的参数说明文字。

**修复方案**:
- 直接删除了该显示元素

**修改位置**: 查询条件区域中的参数说明文字已被移除。

### 3. 去掉模拟数据，没数据时显示无数据提示
**问题描述**: 页面使用模拟数据，需要改为真实API调用，没数据时显示友好提示。

**修复方案**:
- 删除了所有模拟数据生成函数：
  - `generateTimeLabels()`
  - `generateMockPowerData()`
  - `generateMockLoadRateData()`
  - `generateMockStatisticsData()`
- 修改了 `updateLoadChartData` 函数，只处理真实API数据
- API调用失败时，清空图表数据并显示错误信息
- 优化了无数据时的友好提示界面

**关键代码**:
```javascript
function updateLoadChartData(data: any) {
  if (!data) {
    clearChartData();
    return;
  }

  // 只处理真实API数据，不生成模拟数据
  if (data.powerChartData && data.powerChartData.series && data.powerChartData.series.length > 0) {
    // 更新功率图表
  } else {
    // 清空功率图表
    activePowerChartData.value = { xAxis: { type: 'category', data: [] }, series: [] };
  }
}
```

### 4. 修复负荷统计数据接口调用
**问题描述**: 负荷统计表格仍在使用模拟数据，需要调用真实API。

**修复方案**:
- 添加了专门的 `loadLoadStatisticsData()` 函数
- 调用 `getLoadTableData` API 获取统计数据
- 正确处理API返回的数据格式（支持中文和英文字段名）
- 在主查询完成后自动调用统计数据查询

**关键代码**:
```javascript
async function loadLoadStatisticsData() {
  const requestData = {
    moduleIds: selectedMeters.value,
    timeType: timeRange.value,
    startTime: startTime,
    endTime: endTime,
    pageNum: 1,
    pageSize: 100
  };

  const response = await getLoadTableData(requestData);
  if (response && response.success) {
    // 处理统计数据，支持中英文字段名
    statisticsData.value = response.result.tableData.map((item, index) => ({
      id: index + 1,
      deviceName: item['设备名称'] || item.deviceName,
      maxLoad: item['最大功率'] || item.maxLoad || 0,
      // ... 其他字段映射
    }));
  }
}
```

## 优化的功能

### 1. 自动查询机制
- 仪表选择变化时自动查询
- 日期选择变化时自动查询
- 仪表加载完成后自动查询

### 2. 错误处理优化
- API调用失败时显示具体错误信息
- 网络异常时提供友好提示
- 数据为空时显示无数据提示

### 3. 数据同步优化
- 维度切换时正确清空相关数据
- 确保界面状态与数据状态一致
- 优化了数据加载的时序

## 测试建议

1. **维度切换测试**: 切换不同的维度标签页，检查仪表下拉框是否正确更新
2. **数据查询测试**: 选择仪表和日期后，检查图表和统计表格是否正确显示
3. **无数据测试**: 选择没有数据的时间范围，检查是否显示友好的无数据提示
4. **错误处理测试**: 断网或API异常时，检查错误提示是否友好

## API接口依赖

确保以下API接口已正确实现：
1. `/energy/realtime/getModulesByDimension` - 获取仪表列表
2. `/energy/realtime/getLoadTimeSeriesData` - 获取负荷时序数据
3. `/energy/realtime/getLoadTableData` - 获取负荷统计表格数据

## 注意事项

1. 所有模拟数据已被移除，页面完全依赖真实API数据
2. 参数编码固定为7（总有功功率），符合负荷监控的业务需求
3. 支持日/月/年三种时间粒度的数据查询
4. 统计表格数据支持中英文字段名，提高兼容性