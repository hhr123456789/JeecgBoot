# 企业分类分区统计功能重构 - 实施进度

## 已完成的修改 (2025-12-05)

### 1. 创建 tb_energy_ratio_info 相关文件
- ✅ `TbEnergyRatioInfo.java` - Entity实体类
- ✅ `TbEnergyRatioInfoMapper.java` - Mapper接口(含3个查询方法)
- ✅ `ComparisonDataVO.java` - 横向对比数据VO
- ✅ `ComparisonSeriesVO.java` - 对比数据系列VO
- ✅ `ComparisonItemVO.java` - 对比项VO

### 2. 扩展接口方法
- ✅ `IEnergyClassificationService.java` - 新增2个方法:
  - `getEnergyTypesByOrgCode(String orgCode)` - 根据部门查询能源类型
  - `getComparisonData(ClassificationQueryParam param)` - 获取横向对比数据

### 3. Service实现类修改 (已完成)
**文件**: `EnergyClassificationServiceImpl.java`

- ✅ `getOrgTree()` - 从sys_depart表动态查询,参考EmsDimensionService实现,只展示二级
- ✅ `getEnergyTypes()` - 改为从tb_energy_ratio_info表查询
- ✅ `getEnergyTypesByOrgCode(String orgCode)` - 根据部门编码查询该部门下设备的能源类型
- ✅ `getComparisonData(ClassificationQueryParam param)` - 实现横向对比数据查询逻辑
  - ✅ 支持一级部门对比(展示所有二级子部门对比)
  - ✅ 支持二级部门对比(展示该部门下所有设备对比)
  - ⚠️ 设备级统计数据查询逻辑待完善(当前返回基础结构,实际数据需从日统计表查询)

### 4. Controller接口新增 (已完成)
**文件**: `EnergyClassificationController.java`

- ✅ `GET /energy/classification/getEnergyTypesByOrgCode` - 根据orgCode获取能源类型
- ✅ `POST /energy/classification/getComparisonData` - 获取横向对比数据

## 待完成的修改

### 1. 设备级统计数据查询完善 (中优先级)
**相关方法**: `EnergyClassificationServiceImpl.getDeviceComparison()`

当前状态:
- 设备对比的基础结构已完成
- 设备列表查询已完成
- ⚠️ 实际统计数据查询逻辑需要完善,当前返回值为0

需要实现:
1. 从 `tb_ep_equ_energy_daycount` 等日统计表查询设备实际能耗数据
2. 根据时间范围参数汇总设备统计数据
3. 计算设备的总消耗、总费用、碳排放、标煤等指标

### 2. 定时任务调整 (中优先级)
**文件**: `EnergyClassificationSyncJob.java` 和相关Service

需要调整:
1. 同步逻辑增加设备级别统计(当前只统计部门级别)
2. 评估是否需要修改 `tb_energy_classification_summary` 表结构,增加 `module_id` 字段

### 4. 前端适配 (高优先级)
**目录**: `jeecgboot-vue3/src/views/EnergyStatistics/Energy_Classification/`

需要修改的文件:
1. `index.vue` - 替换左侧树为DimensionTree组件,增加横向对比图表区域
2. `api/index.ts` - 新增2个API方法
3. `api/types.ts` - 新增ComparisonData相关类型定义
4. `hooks/useEnergyClassification.ts` - 修改能源类型动态加载逻辑
5. 新增 `components/ComparisonChart.vue` - 横向对比图表组件

### 5. 查找和修改sys_depart查询Service (中优先级)
**参考文件**: `EmsDimensionService.java`

需要:
1. 了解现有的维度树查询方法 `searchByDepartType()`
2. 在 `EnergyClassificationService` 中复用或参考该逻辑

##下一步执行计划

1. **立即执行**: 修改 `EnergyClassificationServiceImpl.java`,实现4个方法
2. **立即执行**: 修改 `EnergyClassificationController.java`,新增2个接口
3. **中期执行**: 前端组件修改和适配
4. **后期优化**: 定时任务调整(需先评估表结构)
