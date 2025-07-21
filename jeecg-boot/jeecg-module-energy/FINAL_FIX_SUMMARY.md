# 维度编码映射问题最终修复总结

## 🎯 问题根本原因

通过分析现有的 `/energy/monitor/getRealTimeData` 接口，发现了数据存储的真实结构：

### 数据存储格式
1. **维度表 (sys_depart)**:
   - `id`: 部门ID (如: `6d35e179cd814e3299bd588ea7daed3f`)
   - `org_code`: 维度编码 (如: `A02A02`)

2. **仪表表 (tb_module)**:
   - `sys_org_code`: 存储部门ID，可能是逗号分隔的多个ID
   - 格式示例: `"6d35e179cd814e3299bd588ea7daed3f"` 或 `"id1,id2,id3"`

### 现有接口的查询方式
```xml
<!-- 现有接口使用 FIND_IN_SET 函数 -->
<select id="selectModulesByOrgCode">
    SELECT * FROM tb_module 
    WHERE FIND_IN_SET(#{orgCode}, sys_org_code)
    AND isaction = 'Y'
</select>
```

## 🔧 最终修复方案

### 核心修改
1. **复用现有的查询逻辑**: 使用 `tbModuleMapper.selectModulesByOrgCode()` 方法
2. **支持 FIND_IN_SET**: 处理逗号分隔的部门ID字段
3. **正确的数据映射**: 维度编码 → 部门ID → 仪表列表

### 修复后的代码逻辑
```java
@Override
public List<ModuleVO> getModulesByOrgCode(String orgCode, Boolean includeChildren) {
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
    
    // 4. 转换为VO并返回
    return convertToModuleVOs(modules, departIds);
}
```

## 📊 数据流程

```
用户输入: orgCode = "A02A02"
         ↓
查询维度表: SELECT id FROM sys_depart WHERE org_code = 'A02A02' OR org_code LIKE 'A02A02%'
         ↓
得到部门ID列表: ["6d35e179cd814e3299bd588ea7daed3f", "abc123def456ghi789"]
         ↓
循环查询仪表: SELECT * FROM tb_module WHERE FIND_IN_SET('部门ID', sys_org_code) AND isaction = 'Y'
         ↓
合并结果、去重、排序
         ↓
转换为VO: 部门ID → 维度编码, 添加部门名称等信息
         ↓
返回仪表列表
```

## 🧪 测试验证

### 1. 数据库验证
运行 `test_orgcode_fix.sql` 脚本检查：
- 维度编码是否存在
- 仪表表的数据格式
- 关联关系是否正确

### 2. 接口测试
```bash
# 测试接口
GET /energy/monitor/getModulesByOrgCode?orgCode=A02A02&includeChildren=true

# 预期响应
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

## 🔍 关键改进点

1. **参考现有接口**: 复用了 `getRealTimeData` 接口的查询逻辑
2. **支持复杂数据格式**: 处理逗号分隔的部门ID字段
3. **完整的数据映射**: 正确处理维度编码、部门ID、部门名称的转换
4. **去重处理**: 防止同一仪表被多次返回
5. **子维度支持**: 支持查询子维度下的仪表

## ⚠️ 注意事项

1. **数据一致性**: 确保 `sys_depart.id` 与 `tb_module.sys_org_code` 的关联正确
2. **性能考虑**: 如果部门层级很深，建议限制查询范围
3. **数据格式**: `tb_module.sys_org_code` 可能包含多个逗号分隔的部门ID
4. **错误处理**: 增加了详细的日志记录，便于问题排查

## 🔄 二次修复：精确查询逻辑

### 问题反馈
用户反馈接口返回了过多数据，原因是使用了模糊查询 `LIKE 'A02A02%'`，导致所有以 `A02A02` 开头的维度都被查询出来。

### 修复方案
改为精确的层级查询：

1. **不包含子维度** (`includeChildren=false`)：
   ```sql
   SELECT * FROM sys_depart WHERE org_code = 'A02A02'
   ```

2. **包含子维度** (`includeChildren=true`)：
   ```sql
   -- 先查询当前维度
   SELECT * FROM sys_depart WHERE org_code = 'A02A02'
   -- 再查询直接子维度
   SELECT child.* FROM sys_depart child
   JOIN sys_depart parent ON child.parent_id = parent.id
   WHERE parent.org_code = 'A02A02'
   ```

### 修复后的逻辑
```java
if (includeChildren != null && includeChildren) {
    // 1. 查询当前维度
    SysDepart currentDepart = sysDepartService.getOne(
        new QueryWrapper<SysDepart>().eq("org_code", orgCode)
    );

    if (currentDepart != null) {
        departIds.add(currentDepart.getId());

        // 2. 查询直接子维度（parent_id = 当前维度的ID）
        List<SysDepart> childDeparts = sysDepartService.list(
            new QueryWrapper<SysDepart>().eq("parent_id", currentDepart.getId())
        );

        for (SysDepart child : childDeparts) {
            departIds.add(child.getId());
        }
    }
} else {
    // 精确查询当前维度
    SysDepart depart = sysDepartService.getOne(
        new QueryWrapper<SysDepart>().eq("org_code", orgCode)
    );
    if (depart != null) {
        departIds.add(depart.getId());
    }
}
```

## ✅ 修复状态

- [x] 问题分析完成
- [x] 参考现有接口实现
- [x] 代码修复完成
- [x] 编译测试通过
- [x] 支持 FIND_IN_SET 查询
- [x] 数据去重和排序
- [x] 完整的数据映射
- [x] **精确查询逻辑修复**
- [x] **避免模糊查询过多数据**
- [x] 测试脚本提供
- [x] 文档更新完成

**修复完成时间**: 2025-07-17
**修复方法**: 参考现有 `getRealTimeData` 接口 + 精确层级查询
**关键改进**: 使用 `parent_id` 关系查询直接子维度，避免模糊查询
**向后兼容**: 是
