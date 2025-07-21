# 根据维度获取仪表列表接口参数更新总结

## 🔄 接口参数变更

根据用户需求，将接口参数修改为与 `getRealTimeData` 接口保持一致，并支持多选维度。

### 修改前的接口
```java
@GetMapping("/getModulesByOrgCode")
public Result<List<ModuleVO>> getModulesByOrgCode(
    @RequestParam String orgCode,                    // 单个维度编码
    @RequestParam Boolean includeChildren           // 是否包含子维度
)
```

### 修改后的接口
```java
@GetMapping("/getModulesByOrgCode")
public Result<List<ModuleVO>> getModulesByOrgCode(
    @RequestParam String orgCodes,                  // 维度编码列表（逗号分隔）
    @RequestParam Integer nowtype                   // 维度类型
)
```

## 📝 参数说明

### 1. orgCodes (维度编码列表)
- **类型**: String
- **必填**: 是
- **格式**: 逗号分隔的维度编码列表
- **示例**: 
  - 单个维度: `"A02A02"`
  - 多个维度: `"A02A02,A02A03,A02A04"`

### 2. nowtype (维度类型)
- **类型**: Integer
- **必填**: 是
- **取值范围**:
  - `1`: 按部门用电
  - `2`: 按线路用电
  - `3`: 天然气
  - `4`: 压缩空气
  - `5`: 企业用水

## 🔧 实现逻辑

### 1. 维度编码解析
```java
// 解析逗号分隔的维度编码
List<String> orgCodeList = Arrays.asList(orgCodes.split(","))
    .stream()
    .map(String::trim)
    .filter(StringUtils::hasText)
    .collect(Collectors.toList());
```

### 2. 部门ID查询
```java
// 根据维度编码列表查询部门ID
QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
queryWrapper.in("org_code", orgCodeList);
List<SysDepart> departs = sysDepartService.list(queryWrapper);
```

### 3. 仪表查询和过滤
```java
// 根据部门ID和维度类型查询仪表
for (String departId : departIds) {
    List<TbModule> moduleList = tbModuleMapper.selectModulesByOrgCode(departId);
    
    // 根据维度类型过滤
    List<TbModule> filteredModules = moduleList.stream()
        .filter(module -> nowtype.equals(module.getEnergyType()))
        .collect(Collectors.toList());
    
    modules.addAll(filteredModules);
}
```

### 4. 去重和排序
```java
// 去重（防止同一个仪表被多次添加）
modules = modules.stream()
    .collect(Collectors.toMap(TbModule::getModuleId, m -> m, (existing, replacement) -> existing))
    .values().stream()
    .sorted(Comparator.comparing(TbModule::getModuleName))
    .collect(Collectors.toList());
```

## 🧪 测试示例

### 1. 单个维度查询
```bash
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A02&nowtype=1
```

### 2. 多个维度查询
```bash
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A02,A02A03,A02A04&nowtype=1
```

### 3. 不同维度类型查询
```bash
# 查询天然气仪表
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A02&nowtype=3

# 查询压缩空气仪表
GET /energy/monitor/getModulesByOrgCode?orgCodes=A02A02&nowtype=4
```

## 📊 响应示例

```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_1202",
            "moduleName": "1号注塑机",
            "orgCode": "A02A02",
            "departName": "生产车间",
            "energyType": 1,
            "isAction": "Y"
        },
        {
            "moduleId": "yj0001_1203",
            "moduleName": "2号注塑机",
            "orgCode": "A02A03",
            "departName": "装配车间",
            "energyType": 1,
            "isAction": "Y"
        }
    ]
}
```

## 🔍 与现有接口的一致性

现在两个接口的参数保持一致：

### getRealTimeData 接口
```java
@GetMapping("/getRealTimeData")
public Result<List<Map<String, Object>>> getRealTimeData(
    @RequestParam String orgCode,      // 部门编码
    @RequestParam Integer nowtype      // 维度类型
)
```

### getModulesByOrgCode 接口
```java
@GetMapping("/getModulesByOrgCode")
public Result<List<ModuleVO>> getModulesByOrgCode(
    @RequestParam String orgCodes,     // 维度编码列表（支持多选）
    @RequestParam Integer nowtype      // 维度类型
)
```

## ⚠️ 注意事项

1. **多选支持**: `getModulesByOrgCode` 支持多选维度，而 `getRealTimeData` 只支持单个维度
2. **参数名称**: `orgCodes` (复数) vs `orgCode` (单数)，体现了多选的特性
3. **维度类型过滤**: 只返回指定维度类型的仪表
4. **去重处理**: 多个维度可能包含相同的仪表，系统会自动去重
5. **排序**: 结果按仪表名称排序

## ✅ 更新状态

- [x] 控制器接口参数修改
- [x] 服务接口参数修改
- [x] 服务实现逻辑更新
- [x] 支持多选维度解析
- [x] 维度类型过滤实现
- [x] 去重和排序逻辑
- [x] 测试用例更新
- [x] 编译测试通过
- [x] 文档更新完成

**更新完成时间**: 2025-07-17  
**主要改进**: 支持多选维度 + 维度类型过滤  
**向后兼容**: 否（参数结构变更）
