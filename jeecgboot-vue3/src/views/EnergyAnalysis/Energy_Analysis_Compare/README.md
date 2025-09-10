# 能源分析对比功能

## 📋 功能概述

能源分析对比功能用于分析单个仪表在指定时间范围内的能源消耗情况，支持当期与同比期的对比分析，支持日、月、年三种时间粒度的统计分析。

## 🚀 主要特性

- ✅ 支持单仪表能源消耗分析
- ✅ 支持当期与同比期对比（如：2025年7月 vs 2024年7月）
- ✅ 根据能源类型自动显示对应单位（电：kWh，其他：m³）
- ✅ 提供图表和表格两种展示方式
- ✅ 支持日、月、年三种统计粒度
- ✅ 计算同比增长率
- ✅ 支持数据导出Excel功能

## 🔧 技术实现

### 前端技术栈
- Vue 3 + TypeScript
- Ant Design Vue
- ECharts (图表组件)
- Dayjs (时间处理)

### API接口
- `getModulesByDimension` - 根据维度获取仪表列表
- `getCompareData` - 获取能源分析对比数据
- `getEnergyTypes` - 获取能源类型配置
- `exportCompareData` - 导出对比数据

## 📁 文件结构

```
Energy_Analysis_Compare/
├── index.vue              # 主页面组件
├── api.ts                 # API接口定义
├── components/
│   └── CompareChart.vue   # 图表组件
├── test-api.html          # API测试页面
└── README.md              # 说明文档
```

## 🎯 使用流程

### 1. 选择维度
- 左侧维度树支持多种能源类型（电、水、气等）
- 点击维度节点自动加载对应的仪表列表

### 2. 选择仪表
- 从下拉列表中选择要分析的仪表
- 仪表列表根据选择的维度动态加载

### 3. 设置时间范围
- 选择时间粒度：日/月/年
- 设置基准期时间范围
- 设置对比期时间范围

### 4. 查询数据
- 点击"查询"按钮获取对比数据
- 系统自动计算增长率和差值

### 5. 查看结果
- 顶部卡片显示汇总数据
- 中间图表显示趋势对比
- 底部表格显示详细数据

### 6. 导出数据
- 点击"导出数据"按钮下载Excel文件
- 文件包含完整的对比分析数据

## 🔌 API接口说明

### 1. 获取仪表列表
```typescript
GET /energy/analysis/getModulesByDimension
参数：
- orgCode: string (必填) - 维度编码
- energyType: number (可选) - 能源类型
- includeChildren: boolean (可选) - 是否包含子维度
```

### 2. 获取对比数据
```typescript
POST /energy/analysis/getCompareData
参数：
- moduleId: string (必填) - 仪表编号
- timeType: string (必填) - 时间类型 day/month/year
- startTime: string (必填) - 开始时间
- endTime: string (必填) - 结束时间
- compareType: string (可选) - 对比类型
```

### 3. 导出数据
```typescript
GET /energy/analysis/exportCompareData
参数：与获取对比数据相同，返回Excel文件
```

## 🧪 测试说明

### API测试
1. 打开 `test-api.html` 文件
2. 确保后端服务已启动
3. 点击测试按钮验证API接口

### 功能测试
1. 启动前端项目
2. 访问能源分析对比页面
3. 按照使用流程进行测试

## 📝 注意事项

### 数据关联关系
- 前端传递的是 `sys_depart.org_code`
- 后端需要先查询获取对应的 `sys_depart.id`
- `tb_module.sys_org_code` 字段保存的是逗号分隔的 `sys_depart.id` 列表

### 能源类型和单位
```javascript
const ENERGY_TYPE_MAP = {
  1: { name: '电', unit: 'kWh' },
  2: { name: '水', unit: 'm³' },
  3: { name: '气', unit: 'm³' }
};
```

### 时间格式规范
- 日统计: `YYYY-MM-DD` (如: 2025-07-26)
- 月统计: `YYYY-MM` (如: 2025-07)
- 年统计: `YYYY` (如: 2025)

## 🔄 更新日志

### v1.0.0 (2025-01-20)
- ✅ 完成基础功能开发
- ✅ 实现API接口调用
- ✅ 支持动态仪表选择
- ✅ 支持数据导出功能
- ✅ 完成UI界面优化

## 🚧 待开发功能

- [ ] 预警功能（设置能耗增长率阈值）
- [ ] 环比对比（与上期对比）
- [ ] 多仪表横向对比
- [ ] 数据缓存优化

## 📞 技术支持

如有问题请联系开发团队或查看相关文档：
- [JeecgBoot开发文档](http://help.jeecg.com/java/)
- [前端开发文档](https://help.jeecg.com/ui/)
