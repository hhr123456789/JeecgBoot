# 班组能源统计功能实现总结

## 项目概述
本项目实现了班组能源统计功能的动态化改造，将原有的静态数据展示改造为基于维度动态切换的能源统计系统。

## 实现日期
2026-01-24

## 一、数据库设计

### 1.1 数据表结构

#### tb_team_info - 班组基础信息表
- **主键**: id (VARCHAR(32))
- **核心字段**:
  - team_code: 班组编码（唯一索引）
  - team_name: 班组名称
  - shift_type: 班次类型（早班/中班/晚班/夜班）
  - org_code: 所属组织编码
  - status: 状态（0-停用，1-启用）
- **索引**: uk_team_code, idx_org_code, idx_status

#### tb_team_dimension_relation - 班组维度关联表
- **主键**: id (VARCHAR(32))
- **核心字段**:
  - team_code: 班组编码
  - dimension_code: 维度编码（如A01B03）
  - dimension_type: 维度类型（1-按部门用电，2-按线路用电，3-天然气，4-压缩空气，5-企业用水）
  - energy_type: 能源类型（1-电，2-水，8-天然气，5-压缩空气）
  - module_ids: 关联的仪表ID列表
- **索引**: uk_team_dimension（联合唯一索引）, idx_dimension_code, idx_dimension_type

#### tb_team_shift_schedule - 班组排班表
- **主键**: id (VARCHAR(32))
- **核心字段**:
  - team_code: 班组编码
  - shift_date: 排班日期
  - shift_type: 班次类型
  - start_time: 开始时间
  - end_time: 结束时间
  - work_hours: 工作时长
- **索引**: uk_team_shift_date（联合唯一索引）, idx_shift_date

### 1.2 SQL脚本位置
`jeecg-boot/jeecg-module-energy/src/main/resources/sql/team_energy_schema.sql`

## 二、后端实现

### 2.1 实体类（Entity）
位置: `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/entity/`

1. **TeamInfo.java** - 班组基础信息实体
2. **TeamDimensionRelation.java** - 班组维度关联实体
3. **TeamShiftSchedule.java** - 班组排班实体

### 2.2 Mapper接口
位置: `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/mapper/`

1. **TeamInfoMapper.java** - 班组信息数据访问
2. **TeamDimensionRelationMapper.java** - 班组维度关联数据访问
3. **TeamShiftScheduleMapper.java** - 班组排班数据访问

### 2.3 VO类（Value Object）
位置: `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/vo/teamenergy/`

1. **TeamInfoVO.java** - 班组信息响应对象
2. **TeamEnergyStatisticsVO.java** - 统计数据响应对象
3. **TeamEnergyTrendVO.java** - 趋势图数据响应对象
4. **TeamEnergyRankingVO.java** - 排名数据响应对象
5. **TeamEnergyTableVO.java** - 明细表数据响应对象
6. **TeamEnergyQueryRequest.java** - 查询请求参数对象

### 2.4 服务层（Service）
位置: `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/service/`

#### ITeamEnergyService.java - 服务接口
定义了以下核心方法：
- `getTeamListByDimension()` - 根据维度获取班组列表
- `getStatistics()` - 获取统计数据
- `getTrendData()` - 获取趋势图数据
- `getRankingData()` - 获取排名数据
- `getTableData()` - 获取明细表数据

#### TeamEnergyServiceImpl.java - 服务实现
实现了所有业务逻辑，包括：
- 维度与班组的动态关联查询
- 能源数据的统计计算
- 图表数据的格式化处理

### 2.5 控制器（Controller）
位置: `jeecg-boot/jeecg-module-energy/src/main/java/org/jeecg/modules/energy/controller/`

#### TeamEnergyController.java
提供以下REST API端点：

| 端点 | 方法 | 说明 |
|------|------|------|
| /energy/teamEnergy/getTeamList | GET | 根据维度获取班组列表 |
| /energy/teamEnergy/getStatistics | GET | 获取统计数据 |
| /energy/teamEnergy/getTrendData | GET | 获取趋势图数据 |
| /energy/teamEnergy/getRankingData | GET | 获取排名数据 |
| /energy/teamEnergy/getTableData | GET | 获取明细表数据 |

## 三、前端实现

### 3.1 API服务
位置: `jeecgboot-vue3/src/views/EnergyStatistics/Team_Energy/team-energy.api.ts`

封装了所有后端API调用：
- `getTeamListByDimension()` - 获取班组列表
- `getTeamEnergyStatistics()` - 获取统计数据
- `getTeamEnergyTrendData()` - 获取趋势数据
- `getTeamEnergyRankingData()` - 获取排名数据
- `getTeamEnergyTableData()` - 获取表格数据

### 3.2 组件重构
位置: `jeecgboot-vue3/src/views/EnergyStatistics/Team_Energy/`

#### 主要改动：

1. **左侧维度树重构**
   - 从静态部门树改为动态维度树
   - 使用 `DimensionTree` 组件
   - 支持多维度标签页切换
   - 维度类型从字典动态加载

2. **班组列表动态化**
   - 根据选中的维度动态加载班组列表
   - 支持"全部班组"和单个班组切换
   - 班组数据从后端API获取

3. **数据展示动态化**
   - 统计卡片数据从API获取
   - 趋势图数据动态加载
   - 排名数据实时更新
   - 明细表数据分页展示

4. **维度切换联动**
   - 切换维度时自动更新班组列表
   - 保存每个维度的选中状态
   - 支持维度间快速切换

### 3.3 核心功能实现

#### 维度管理
```typescript
// 维度列表从字典加载
loadDimensionDictData()

// 维度切换处理
handleTabChange(key)

// 树节点选择处理
onDepartTreeSelect(data)
```

#### 数据加载
```typescript
// 加载班组列表
loadTeamList(dimensionCode, dimensionType)

// 加载统计数据
loadStatistics()

// 加载趋势数据
loadTrendData()

// 加载排名数据
loadRankingData()

// 加载表格数据
loadTableData()
```

## 四、核心特性

### 4.1 维度动态配置
- 支持多种维度类型（部门、线路、能源类型等）
- 维度配置可通过字典管理
- 维度与班组的多对多关系

### 4.2 数据实时查询
- 所有数据从后端API实时获取
- 支持按时间维度（日/月/年）查询
- 支持按能源类型筛选

### 4.3 图表动态展示
- 趋势图支持柱状图/折线图切换
- 支持多指标切换（能耗/费用/碳排放）
- 排名和占比图表自动更新

### 4.4 用户体验优化
- 维度切换状态保持
- 加载状态提示
- 错误处理和提示
- 响应式布局适配

## 五、技术栈

### 后端
- **框架**: JeecgBoot (Spring Boot)
- **ORM**: MyBatis-Plus
- **数据库**: MySQL
- **API文档**: Swagger

### 前端
- **框架**: Vue 3 (Composition API)
- **UI组件**: Ant Design Vue
- **状态管理**: Vue Reactive
- **HTTP客户端**: Axios
- **日期处理**: Day.js

## 六、文件清单

### 后端文件
```
jeecg-boot/jeecg-module-energy/
├── src/main/resources/sql/
│   └── team_energy_schema.sql
├── src/main/java/org/jeecg/modules/energy/
│   ├── entity/
│   │   ├── TeamInfo.java
│   │   ├── TeamDimensionRelation.java
│   │   └── TeamShiftSchedule.java
│   ├── mapper/
│   │   ├── TeamInfoMapper.java
│   │   ├── TeamDimensionRelationMapper.java
│   │   └── TeamShiftScheduleMapper.java
│   ├── vo/teamenergy/
│   │   ├── TeamInfoVO.java
│   │   ├── TeamEnergyStatisticsVO.java
│   │   ├── TeamEnergyTrendVO.java
│   │   ├── TeamEnergyRankingVO.java
│   │   ├── TeamEnergyTableVO.java
│   │   └── TeamEnergyQueryRequest.java
│   ├── service/
│   │   ├── ITeamEnergyService.java
│   │   └── impl/
│   │       └── TeamEnergyServiceImpl.java
│   └── controller/
│       └── TeamEnergyController.java
```

### 前端文件
```
jeecgboot-vue3/src/views/EnergyStatistics/Team_Energy/
├── team-energy.api.ts
├── index_new.vue (模板部分)
├── index_new_script.vue (脚本部分)
└── components/
    ├── TeamEnergyTrend.vue
    ├── TeamEnergyRanking.vue
    └── TeamEnergyPie.vue
```

## 七、部署步骤

### 7.1 数据库初始化
```sql
-- 执行SQL脚本
source jeecg-boot/jeecg-module-energy/src/main/resources/sql/team_energy_schema.sql
```

### 7.2 后端部署
1. 确保所有Java文件已编译
2. 重启后端服务
3. 验证API端点可访问

### 7.3 前端部署
1. 合并 `index_new.vue` 和 `index_new_script.vue` 的内容
2. 替换原有的 `index.vue` 文件
3. 重新编译前端项目
4. 部署到服务器

## 八、测试要点

### 8.1 功能测试
- [ ] 维度切换功能正常
- [ ] 班组列表动态加载
- [ ] 统计数据正确显示
- [ ] 趋势图数据准确
- [ ] 排名数据正确排序
- [ ] 明细表分页正常

### 8.2 性能测试
- [ ] 大数据量下的查询性能
- [ ] 维度切换响应速度
- [ ] 图表渲染性能

### 8.3 兼容性测试
- [ ] 不同浏览器兼容性
- [ ] 响应式布局适配
- [ ] 移动端显示效果

## 九、后续优化建议

1. **数据缓存**: 实现Redis缓存提升查询性能
2. **实时数据**: 集成WebSocket实现数据实时推送
3. **导出功能**: 完善Excel导出功能
4. **权限控制**: 添加细粒度的数据权限控制
5. **数据分析**: 增加更多维度的数据分析功能

## 十、注意事项

1. **数据一致性**: 确保班组与维度的关联关系正确配置
2. **性能优化**: 大数据量查询时注意添加适当的索引
3. **错误处理**: 完善前后端的错误处理和用户提示
4. **安全性**: 注意SQL注入和XSS攻击防护

## 十一、联系方式

如有问题，请联系开发团队。

---
文档生成时间: 2026-01-24
