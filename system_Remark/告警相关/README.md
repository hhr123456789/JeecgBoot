# 告警系统改进 - 快速参考

## 改进文件清单

### ✅ 已完成改进

1. **告警规则设置** 
   - 文件: `jeecgboot-vue3/src/views/EnergyAlarm/Alarm_Rules_Settings/index.vue`
   - 改进: 支持多能源类型 + 可视化规则配置

2. **告警模板设置（改进版）**
   - 文件: `jeecgboot-vue3/src/views/EnergyAlarm/Alarm_Settings/index_improved.vue`
   - 改进: 模板分类 + 预设配置 + 标签管理

3. **详细文档**
   - 文件: `system_Remark/告警相关/告警系统改进说明.md`
   - 包含: 完整设计说明 + 后端需求 + API文档

---

## 核心改进点

### 1. 支持所有能源类型
```typescript
能源类型枚举:
- "1" : 电力
- "2" : 水  
- "8" : 天然气
- "5" : 压缩空气
```

### 2. 可视化规则配置
**从文本输入** → **结构化配置**

```typescript
// 原来: 用户手写文本
condition: "当设备运行电流超过额定值时触发告警"

// 现在: 可视化配置
conditions: [{
  metric: "device_current",     // 下拉选择
  operator: "gt",               // 下拉选择
  threshold: 150,               // 数字输入
  unit: "A",                    // 自动匹配
  duration: 5,                  // 持续时长
  checkInterval: 10             // 检查频率
}]
```

### 3. 丰富的监控指标

**设备指标**: 状态、电流、电压、功率、温度、压力  
**能源指标**: 小时/日/月用量、变化率、单耗、费用

### 4. 灵活告警设置
- 多条件组合（OR逻辑）
- 多种通知方式
- 静默期防止骚扰
- 持续时长判断

---

## 后端开发要点

### 数据表
- `tb_alarm_rule` - 告警规则表
- `tb_alarm_template` - 告警模板表

### 核心API
```
POST /energy/alarm/rule/add          - 新增规则
PUT  /energy/alarm/rule/edit         - 编辑规则
GET  /energy/alarm/rule/list         - 查询规则
POST /energy/alarm/template/add      - 新增模板
```

### 规则引擎
```java
@Service
public class AlarmRuleEngine {
    // 解析条件 → 获取当前值 → 判断阈值 → 检查持续时长 → 检查静默期 → 触发告警
    public void executeRuleCheck(AlarmRule rule) { ... }
}
```

### 定时任务
```java
@Scheduled(cron = "0 * * * * ?")  // 每分钟检查
public void checkAlarmRules() { ... }
```

---

## 前端使用示例

### 创建告警规则
```typescript
const formState = {
  name: "车间用电量超限",
  ruleType: "energy",           // 设备告警 or 能源告警
  energyType: "1",               // 电力
  targetScope: "department",     // 按部门监控
  conditions: [                  // 可多个条件
    {
      metric: "day_consumption",
      operator: "gt",
      threshold: 10000,
      unit: "kWh",
      duration: 5,
      checkInterval: 10
    }
  ],
  level: "high",
  notifyMethods: ["system", "email"],
  notifyUsers: ["user1", "user2"],
  silencePeriod: 60
}
```

### 创建告警模板
```typescript
const templateState = {
  name: "空压机监控模板",
  category: "device_production",
  energyType: "1",
  tags: ["生产设备", "高耗能"],
  ruleConfigs: [                 // 预设配置
    {
      metric: "device_current",
      operator: "gt",
      defaultThreshold: 150,
      unit: "A",
      defaultLevel: "high",
      description: "电流超限告警"
    }
  ]
}
```

---

## 与原系统对比

| 功能 | 原系统 | 改进后 |
|------|--------|--------|
| 能源类型 | 仅电力 | 电/水/气/压缩空气 |
| 规则配置 | 文本输入 | 可视化配置 |
| 后端解析 | 困难 | 结构化JSON直接解析 |
| 条件类型 | 单一 | 多条件+多运算符 |
| 通用性 | 差 | 高 |
| 扩展性 | 差 | 优秀 |

---

## 测试建议

### 前端测试
- [ ] 新增各类型规则（设备/能源 × 4种能源）
- [ ] 多条件配置
- [ ] 区间条件
- [ ] 模板创建和复制
- [ ] 表单验证

### 后端测试
- [ ] 规则引擎执行
- [ ] 各类监控指标数据获取
- [ ] 持续时长判断
- [ ] 静默期判断
- [ ] 通知发送

---

## 快速定位

- **详细文档**: `system_Remark/告警相关/告警系统改进说明.md`
- **规则设置**: `jeecgboot-vue3/src/views/EnergyAlarm/Alarm_Rules_Settings/index.vue`
- **模板设置**: `jeecgboot-vue3/src/views/EnergyAlarm/Alarm_Settings/index_improved.vue`

---

**更新时间**: 2024-01-06  
**改进状态**: ✅ 前端完成，等待后端开发
