# 能源概览仪表板实现

## 变更内容

### 新建文件:
1. `src/views/Energy_Overview/index.vue` - 主仪表板视图
2. `src/views/Energy_Overview/components/` - 组件目录，包含:
   - EnergyTimeChart.vue (综合能源查询图表)
   - EnergyDistributionPie.vue (综合能源占比饼图)
   - AreaEnergyPie.vue (各区域能源占比饼图)
   - EnergyConsumptionLine.vue (能源转换折线图)
   - EnergyTrendChart.vue (综合能源累计查询图表)
   - EnergyStatistics.vue (耗损查询统计)

### 功能实现:
1. 能源消耗时间序列图表
2. 能源分布饼图
3. 区域能源消耗分布
4. 能源转换效率指标
5. 能源趋势分析
6. 能源统计数据展示

### 数据结构:
- 静态数据实现用于初始设置
- 为未来API集成做好准备
- 遵循 Jeecg-Boot Vue3 开发规范

### 组件结构:
- 使用 Vue 3 组合式 API
- 实现响应式设计
- 使用 Ant Design Vue 组件
- 使用 TailwindCSS 进行样式设计

### 主要功能说明:
1. 时间选择器：支持日、月、年切换
2. 综合能源查询：展示不同能源类型的消耗趋势
3. 能源分布：展示各类能源的占比情况
4. 区域能源分布：展示各个车间的能源消耗情况
5. 能源转换：展示能源转换效率和消耗数据
6. 累计查询：展示能源消耗的累计数据
7. 统计信息：展示关键能源指标
