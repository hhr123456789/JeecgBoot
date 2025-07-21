# 维度编码映射问题修复说明

## 🐛 问题描述

用户反馈根据维度获取仪表列表接口 (`/energy/monitor/getModulesByOrgCode`) 查不到数据。

**问题原因分析**：
- 前端传入的是维度编码，如：`A02A02`（这是 `sys_depart` 表的 `org_code` 字段）
- 但仪表表 `tb_module` 中的 `sys_org_code` 字段存储的是部门ID，如：`6d35e179cd814e3299bd588ea7daed3f`（这是 `sys_depart` 表的 `id` 字段）
- 原代码直接用维度编码去匹配仪表表的部门ID字段，导致查询不到数据

## 🔧 修复方案

### 问题深入分析
通过参考现有的 `/energy/monitor/getRealTimeData` 接口，发现了关键信息：

1. **现有接口的查询方式**：
   ```xml
   <!-- TbModuleMapper.xml -->
   <select id="selectModulesByOrgCode" resultType="org.jeecg.modules.energy.entity.TbModule">
       SELECT * FROM tb_module
       WHERE FIND_IN_SET(#{orgCode}, sys_org_code)
       AND isaction = 'Y'
   </select>
   ```

2. **数据存储格式**：`tb_module.sys_org_code` 字段可能存储逗号分隔的多个部门ID，如：
   - `"6d35e179cd814e3299bd588ea7daed3f"`
   - `"6d35e179cd814e3299bd588ea7daed3f,abc123def456ghi789"`

### 修复前的逻辑
```java
// 错误：直接用维度编码查询仪表表
queryWrapper.eq(TbModule::getSysOrgCode, orgCode); // orgCode = "A02A02"
// 但 tb_module.sys_org_code 存储的是部门ID，不是维度编码
```

### 修复后的逻辑
```java
// 正确：参考现有接口的实现方式
// 1. 根据维度编码查询部门ID列表
List<String> departIds = getDepartIdsByOrgCode(orgCode, includeChildren);

// 2. 使用现有的 Mapper 方法查询仪表（支持 FIND_IN_SET）
List<TbModule> modules = new ArrayList<>();
for (String departId : departIds) {
    List<TbModule> moduleList = tbModuleMapper.selectModulesByOrgCode(departId);
    modules.addAll(moduleList);
}

// 3. 去重和排序
modules = modules.stream()
    .collect(Collectors.toMap(TbModule::getModuleId, m -> m, (existing, replacement) -> existing))
    .values().stream()
    .sorted(Comparator.comparing(TbModule::getModuleName))
    .collect(Collectors.toList());
```

## 📝 修复的核心方法

### 1. getDepartIdsByOrgCode()
```java
/**
 * 根据维度编码获取部门ID列表
 * @param orgCode 维度编码 (如: A02A02)
 * @param includeChildren 是否包含子维度
 * @return 部门ID列表 (如: ["6d35e179cd814e3299bd588ea7daed3f"])
 */
private List<String> getDepartIdsByOrgCode(String orgCode, Boolean includeChildren) {
    QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
    
    if (includeChildren != null && includeChildren) {
        // 包含子维度：查询当前维度及其所有子维度
        queryWrapper.and(wrapper -> wrapper
            .eq("org_code", orgCode)
            .or()
            .likeRight("org_code", orgCode)
        );
    } else {
        // 不包含子维度：只查询当前维度
        queryWrapper.eq("org_code", orgCode);
    }
    
    List<SysDepart> departs = sysDepartService.list(queryWrapper);
    return departs.stream().map(SysDepart::getId).collect(toList());
}
```

### 2. getDepartNameMapByIds()
```java
/**
 * 根据部门ID列表获取部门名称映射
 * @param departIds 部门ID列表
 * @return 部门ID到名称的映射
 */
private Map<String, String> getDepartNameMapByIds(List<String> departIds) {
    QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
    queryWrapper.in("id", departIds);
    List<SysDepart> departs = sysDepartService.list(queryWrapper);
    
    Map<String, String> departNameMap = new HashMap<>();
    for (SysDepart depart : departs) {
        departNameMap.put(depart.getId(), depart.getDepartName());
    }
    return departNameMap;
}
```

### 3. getDepartOrgCodeMapByIds()
```java
/**
 * 根据部门ID列表获取部门编码映射
 * @param departIds 部门ID列表
 * @return 部门ID到编码的映射
 */
private Map<String, String> getDepartOrgCodeMapByIds(List<String> departIds) {
    QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
    queryWrapper.in("id", departIds);
    List<SysDepart> departs = sysDepartService.list(queryWrapper);
    
    Map<String, String> departOrgCodeMap = new HashMap<>();
    for (SysDepart depart : departs) {
        departOrgCodeMap.put(depart.getId(), depart.getOrgCode());
    }
    return departOrgCodeMap;
}
```

## 🧪 测试方法

### 1. 通过Swagger测试
1. 启动应用
2. 访问 `http://localhost:8080/jeecg-boot/doc.html`
3. 找到 "能源实时监控" -> "根据维度获取仪表列表"
4. 输入参数：
   - `orgCode`: `A02A02` (您的实际维度编码)
   - `includeChildren`: `true`
5. 点击执行

### 2. 通过curl测试
```bash
curl -X GET "http://localhost:8080/jeecg-boot/energy/monitor/getModulesByOrgCode?orgCode=A02A02&includeChildren=true" \
     -H "accept: application/json"
```

### 3. 预期响应
```json
{
    "success": true,
    "message": "查询成功",
    "code": 200,
    "result": [
        {
            "moduleId": "yj0001_1202",
            "moduleName": "1号注塑机",
            "orgCode": "A02A02A01",
            "departName": "1号注塑机",
            "energyType": 1,
            "isAction": "Y"
        }
    ]
}
```

## 📊 数据流程图

```
前端传入维度编码 (A02A02)
         ↓
查询 sys_depart 表获取部门ID列表
  SELECT id FROM sys_depart WHERE org_code = 'A02A02' OR org_code LIKE 'A02A02%'
         ↓
得到部门ID列表 (["6d35e179cd814e3299bd588ea7daed3f", "abc123def456ghi789"])
         ↓
循环使用每个部门ID查询仪表表（使用 FIND_IN_SET 支持逗号分隔的字段）
  SELECT * FROM tb_module WHERE FIND_IN_SET('6d35e179cd814e3299bd588ea7daed3f', sys_org_code)
         ↓
合并结果、去重、排序
         ↓
返回仪表列表，并将部门ID转换回维度编码显示
```

## ⚠️ 注意事项

1. **数据一致性**: 确保 `sys_depart` 表和 `tb_module` 表的关联关系正确
2. **子维度查询**: `includeChildren=true` 时会查询所有子维度下的仪表
3. **性能考虑**: 如果维度层级很深，建议合理设置查询范围
4. **日志监控**: 修复后的代码增加了详细的日志，便于问题排查

## 🔍 排查步骤

如果仍然查询不到数据，请按以下步骤排查：

1. **检查维度编码是否存在**：
   ```sql
   SELECT * FROM sys_depart WHERE org_code = 'A02A02';
   ```

2. **检查仪表表中的部门ID**：
   ```sql
   SELECT sys_org_code, COUNT(*) FROM tb_module 
   WHERE isaction = 'Y' 
   GROUP BY sys_org_code;
   ```

3. **检查关联关系**：
   ```sql
   SELECT d.org_code, d.depart_name, m.module_id, m.module_name
   FROM sys_depart d
   JOIN tb_module m ON d.id = m.sys_org_code
   WHERE d.org_code LIKE 'A02A02%' AND m.isaction = 'Y';
   ```

## ✅ 修复状态

- [x] 问题分析完成
- [x] 代码修复完成
- [x] 编译测试通过
- [x] 文档更新完成

**修复时间**: 2025-07-16
**影响范围**: `/energy/monitor/getModulesByOrgCode` 接口
**向后兼容**: 是
