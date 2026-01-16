# 企业分类分区统计页面无数据问题诊断

## 问题现象
页面显示的所有能耗数据都是 0.00，包括：
- 总能耗：0.00
- 电能消耗：0.00
- 水能消耗：0.00
- 燃气消耗：0.00

## 可能的原因

### 1. 数据库中没有统计数据
**检查方法：**
```sql
-- 检查 tb_energy_classification_summary 表是否有数据
SELECT COUNT(*) FROM tb_energy_classification_summary;

-- 检查表中的数据示例
SELECT * FROM tb_energy_classification_summary LIMIT 10;

-- 检查特定时间范围的数据
SELECT * FROM tb_energy_classification_summary
WHERE stat_date BETWEEN '2025-11-01' AND '2025-11-30'
LIMIT 10;

-- 检查特定部门的数据
SELECT * FROM tb_energy_classification_summary
WHERE org_code = 'A01-01'  -- 替换为实际的部门编码
LIMIT 10;
```

**解决方案：**
- 如果表中没有数据，需要运行定时任务同步数据
- 或者手动触发数据同步接口

### 2. 查询条件不匹配
**检查要点：**
- 选中的部门编码（orgCode）是否正确
- 查询的时间范围是否有数据
- 能源类型是否匹配

**前端查询参数：**
```javascript
{
  orgCode: '选中部门的编码',
  energyType: 'all',  // 或 '1', '2', '3'
  timeDimension: 'month',  // 'day', 'month', 'year'
  startDate: '2025-11-01',
  endDate: '2025-11-30',
  includeChildren: true
}
```

### 3. 维度树选中问题
**检查要点：**
- 维度树是否正确加载
- 选中节点后是否触发了数据查询
- orgCode 是否正确传递

**调试方法：**
打开浏览器控制台，查看：
1. 维度树加载的日志
2. 选中节点时的日志
3. 数据查询的请求和响应

## 修复步骤

### 步骤1：检查数据库是否有数据
```sql
-- 执行上面的SQL检查数据
SELECT COUNT(*) FROM tb_energy_classification_summary;
```

### 步骤2：如果没有数据，运行数据同步
有两种方式：

**方式1：通过后端接口手动触发**
```bash
# 使用 curl 或 Postman 调用同步接口
POST http://localhost:8080/jeecg-boot/energy/classification/triggerDataSync
{
  "startDate": "2025-11-01",
  "endDate": "2025-11-30"
}
```

**方式2：直接运行定时任务**
找到 `EnergyClassificationSyncJob` 类，手动执行同步方法

### 步骤3：检查前端查询逻辑
打开浏览器控制台，查看：
1. 页面初始化日志
2. 维度树选中日志
3. 数据查询请求和响应

### 步骤4：修改默认查询日期
如果数据库中没有 2025-11 的数据，修改默认日期：

在 `useEnergyClassification.ts` 中：
```typescript
// 修改默认日期为有数据的日期
const selectedDate = ref<Dayjs>(dayjs('2025-12-01')); // 改为当前月份
```

## 快速验证

### 1. 检查后端日志
查看后端控制台输出，应该能看到：
```
==== 开始从sys_depart表查询部门树形结构(只展示到二级) ====
查询到部门数量: X
========== 查询分类分区统计数据的SQL详细信息 ==========
```

### 2. 检查前端控制台
应该能看到：
```
=== 页面初始化开始 ===
维度列表初始化完成: [...]
能源类型加载完成: [...]
选中的部门节点: {...}
开始加载数据，查询参数: {...}
```

### 3. 检查网络请求
在浏览器开发者工具的 Network 标签中：
- 查看 `/energy/classification/getSummaryData` 请求
- 检查请求参数是否正确
- 检查响应数据是否为空

## 常见问题

### Q1: 维度树显示但点击没反应
**原因：** 事件绑定问题或 orgCode 为空
**解决：** 检查 ClassificationDimensionTree 组件的 @select 事件

### Q2: 数据查询返回空
**原因：** 数据库中没有对应条件的数据
**解决：** 调整查询条件或同步数据

### Q3: 页面一直显示 0.00
**原因：** 查询返回的数据为空或全为0
**解决：** 检查数据库和查询条件

## 下一步操作

1. 先执行 SQL 检查数据库是否有数据
2. 如果没有数据，运行数据同步
3. 如果有数据，检查查询条件是否匹配
4. 查看浏览器控制台和后端日志定位具体问题
