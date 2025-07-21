# Real_Data_Monitor 页面改造总结

## 改造目标
将 Real_Data_Monitor 页面从静态数据改为调用API获取动态数据，并支持维度树的多选功能。

## 主要改动

### 1. 创建支持多选的维度树组件
**文件**: `components/MultiSelectDimensionTree.vue`

**主要特性**:
- 基于原有的 DimensionTree 组件改造
- 支持多选功能 (`checkable: true`)
- 使用 `checkedKeys` 替代 `selectedKeys`
- 在 `onCheck` 事件中处理多选逻辑
- 向父组件传递选中的节点数组

**关键代码**:
```vue
<BasicTree
  checkable
  :checkStrictly="false"
  :checkedKeys="checkedKeys"
  @check="onCheck"
/>
```

### 2. 创建API接口文件
**文件**: `api.ts`

**包含接口**:
- `getModulesByOrgCode`: 根据维度编码获取仪表列表
- `getRealTimeMonitorData`: 查询实时监控数据

**类型定义**:
- `ModuleInfo`: 仪表信息接口
- `RealTimeMonitorRequest`: 实时监控数据请求参数
- `RealTimeMonitorResponse`: 实时监控数据响应

### 3. 修改主页面支持API调用
**文件**: `index.vue`

**主要改动**:

#### 3.1 组件替换
- 将 `DimensionTree` 替换为 `MultiSelectDimensionTree`
- 支持维度树的多选功能

#### 3.2 数据结构调整
```typescript
// 仪表列表改为从API获取
const meters = ref<Array<{ label: string; value: string }>>([]);
const allModules = ref<ModuleInfo[]>([]);

// 添加API数据存储
const unifiedChartData = ref<ChartData>({ categories: [], series: [] });
const separateChartsData = ref<any[]>([]);
```

#### 3.3 维度选择联动
```typescript
function onDepartTreeSelect(data) {
  // 支持多选数据处理
  if (Array.isArray(data) && data.length > 0) {
    const orgCodestr = data.map(item => item.orgCode).join(',');
    // 根据选中的维度获取仪表列表
    loadModulesByOrgCodes(data.map(item => item.orgCode));
  }
}
```

#### 3.4 仪表列表动态加载
```typescript
async function loadModulesByOrgCodes(orgCodes: string[]) {
  // 为每个维度编码获取仪表列表
  for (const orgCode of orgCodes) {
    const response = await getModulesByOrgCode({
      orgCode: orgCode,
      includeChildren: true
    });
    // 处理响应数据...
  }
  // 去重并更新仪表选择列表
}
```

#### 3.5 查询功能改造
```typescript
const handleQuery = async () => {
  // 构建API请求参数
  const requestData: RealTimeMonitorRequest = {
    moduleIds: selectedMeters.value,
    parameters: selectedParams.value.map(p => Number(p)),
    startTime: dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
    interval: Number(queryInterval.value),
    displayMode: Number(displayMode.value)
  };

  // 调用API获取数据
  const response = await getRealTimeMonitorData(requestData);
  
  // 更新图表数据
  updateChartDataFromAPI(response.result);
};
```

#### 3.6 图表数据处理
```typescript
const updateChartDataFromAPI = (apiData: any) => {
  if (apiData.displayMode === 'unified') {
    // 统一显示模式
    unifiedChartData.value = {
      categories: apiData.series[0].data.map(item => item[0]),
      series: apiData.series.map(series => ({
        name: series.name,
        data: series.data.map(item => item[1]),
        itemStyle: { color: colors[index % colors.length] }
      }))
    };
  } else {
    // 分开显示模式
    separateChartsData.value = apiData.charts.map(chart => ({
      moduleId: chart.moduleId,
      moduleName: chart.moduleName,
      parameter: chart.parameter,
      categories: chart.data.map(item => item[0]),
      series: [{
        name: `${chart.moduleName}-${chart.parameter}`,
        data: chart.data.map(item => item[1])
      }]
    }));
  }
};
```

#### 3.7 模板更新
支持API数据和模拟数据的切换显示：

```vue
<!-- 分开显示模式 -->
<template v-else>
  <!-- 如果有API数据，使用API数据 -->
  <template v-if="separateChartsData.length > 0">
    <div v-for="(chartData, index) in separateChartsData" :key="`api-chart-${index}`">
      <MonitorChart :chartData="chartData" />
    </div>
  </template>
  <!-- 否则使用模拟数据 -->
  <template v-else>
    <!-- 原有的模拟数据逻辑 -->
  </template>
</template>
```

## 功能特性

### 1. 维度树多选
- 支持同时选择多个维度节点
- 自动合并多个维度下的仪表列表
- 去重处理，避免重复仪表

### 2. 仪表选择联动
- 根据维度选择动态更新仪表列表
- 默认选择前两个仪表
- 支持标签页切换时保持选择状态

### 3. API数据集成
- 完整的API调用流程
- 错误处理和用户提示
- 数据格式转换适配图表组件

### 4. 兼容性保持
- 保持原有的模拟数据作为后备
- API数据优先，无API数据时使用模拟数据
- 保持原有的图表交互功能

## 接口依赖

### 后端接口
1. `/energy/monitor/getModulesByOrgCode` - 获取仪表列表
   - **参数**:
     - `orgCodes` (string, 必填): 维度编码列表，多个用逗号分隔
     - `nowtype` (string, 必填): 维度类型
     - `includeChildren` (boolean, 可选): 是否包含子级
2. `/energy/monitor/getRealTimeMonitorData` - 获取实时监控数据

### 字典接口
- `parameter` / `parameter_energy` - 参数选择字典
- `queryInterval` - 查询间隔字典
- `queryMethod` - 查询方式字典

## 问题修复记录

### 2025-07-17 修复API参数问题

**问题**: 后端报错 `Required request parameter 'orgCodes' for method parameter type String is not present`

**原因**:
1. 前端传递的参数名是 `orgCode`（单数），但后端期望的是 `orgCodes`（复数）
2. 缺少必填参数 `nowtype`（维度类型）

**修复方案**:
1. 修改API接口定义，将参数名从 `orgCode` 改为 `orgCodes`
2. 添加必填参数 `nowtype`
3. 修改前端调用逻辑，将多个维度编码合并为逗号分隔的字符串
4. 确保传递正确的维度类型参数

**修改文件**:
- `api.ts`: 更新接口参数定义
- `index.vue`: 修改API调用逻辑

## 使用说明

1. **维度选择**: 在左侧维度树中可以多选维度节点
2. **仪表选择**: 根据维度选择自动更新，也可手动调整
3. **参数选择**: 根据能源类型动态加载对应参数
4. **查询数据**: 点击查询按钮调用API获取实时数据
5. **图表显示**: 支持统一显示和分开显示两种模式

## 注意事项

1. 需要确保后端API接口已实现并可访问
2. 字典数据需要正确配置
3. 时间格式需要与后端保持一致
4. 错误处理已添加，但需要根据实际情况调整提示信息

## 测试建议

1. 测试维度树多选功能
2. 测试仪表列表联动更新
3. 测试API调用和数据显示
4. 测试错误情况的处理
5. 测试不同能源类型的参数切换
