# 实时数据监控接口设计

## 1. 获取维度树接口

### 接口信息
- **URL**: `/ems/monitor/getDimensionTree`
- **Method**: GET
- **功能**: 获取左侧维度树数据

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|------|------|
| nowtype | Integer | 是 | 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水) |

### 响应结果
```json
{
  "code": 200,
  "message": "操作成功",
  "result": [
    {
      "id": "6d35e179cd814e3299bd588ea7daed3f",
      "key": "6d35e179cd814e3299bd588ea7daed3f",
      "title": "深圳市远景易云科技有限公司",
      "value": "6d35e179cd814e3299bd588ea7daed3f",
      "orgCode": "A02",
      "children": [
        {
          "id": "5159cde220114246b045e574adceafe9",
          "key": "5159cde220114246b045e574adceafe9",
          "title": "按部门（电）",
          "value": "5159cde220114246b045e574adceafe9",
          "orgCode": "A02A02",
          "children": [...]
        }
      ]
    }
  ]
}
```

## 2. 获取实时监控数据接口

### 接口信息
- **URL**: `/ems/monitor/getRealTimeData`
- **Method**: GET
- **功能**: 获取右侧实时监控数据

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|------|------|
| orgCode | String | 是 | 部门编码 |
| nowtype | Integer | 是 | 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水) |

### 响应结果
```json
{
  "code": 200,
  "message": "操作成功",
  "result": [
    {
      "module_name": "1号注塑机",
      "module_id": "yj0001_1202",
      "Equ_Electric_DT": "2025-07-03 16:30:45",
      "loadStatus": "正常",
      "loadRate": 63.10,
      "PFS": 0.91,
      "HZ": 50.00,
      "pp": 28.49,
      "UA": 220.50,
      "UB": 219.80,
      "UC": 221.20,
      "IA": 60.89,
      "IB": 12.93,
      "IC": 28.49,
      "PFa": 0.90,
      "PFb": 0.90,
      "PFc": 0.93,
      "Pa": 13.40,
      "Pb": 2.84,
      "Pc": 12.25,
      "KWH": 1256.78,
      "KVARH": 605.42,
      "dailyPower": 125.67,
      "rated_power": 1000.00
    }
  ]
}
```

## 3. 接口实现逻辑

### 负荷状态计算
```java
/**
 * 计算负荷状态
 * @param phaseACurrent A相电流
 * @param phaseBCurrent B相电流
 * @param phaseCCurrent C相电流
 * @param phaseAVoltage A相电压
 * @param phaseBVoltage B相电压
 * @param phaseCVoltage C相电压
 * @return 负荷状态
 */
private String calculateLoadStatus(BigDecimal IA, BigDecimal IB, BigDecimal IC,
                                  BigDecimal UA, BigDecimal UB, BigDecimal UC) {
    // 1. 检查三相平衡度
    BigDecimal avgCurrent = IA.add(IB).add(IC)
            .divide(new BigDecimal(3), 2, RoundingMode.HALF_UP);
    
    BigDecimal maxCurrentDeviation = calculateMaxDeviation(IA, IB, IC, avgCurrent);
    
    // 电压正常范围检查 (标准电压220V，允许偏差±10%)
    boolean voltageNormal = UA.compareTo(new BigDecimal(198)) >= 0 
            && UA.compareTo(new BigDecimal(242)) <= 0
            && UB.compareTo(new BigDecimal(198)) >= 0 
            && UB.compareTo(new BigDecimal(242)) <= 0
            && UC.compareTo(new BigDecimal(198)) >= 0 
            && UC.compareTo(new BigDecimal(242)) <= 0;
    
    // 三相不平衡度超过20%或电压异常时，判定为"异常"
    if (maxCurrentDeviation.compareTo(new BigDecimal(0.2)) > 0 || !voltageNormal) {
        return "异常";
    }
    
    // 三相不平衡度在10%-20%之间，判定为"警告"
    if (maxCurrentDeviation.compareTo(new BigDecimal(0.1)) > 0) {
        return "警告";
    }
    
    return "正常";
}

private BigDecimal calculateMaxDeviation(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal avg) {
    BigDecimal devA = a.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
    BigDecimal devB = b.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
    BigDecimal devC = c.subtract(avg).abs().divide(avg, 2, RoundingMode.HALF_UP);
    
    return devA.max(devB).max(devC);
}
```

### 负荷率计算
```java
/**
 * 计算负荷率
 * @param currentPower 当前功率
 * @param ratedPower 额定功率
 * @return 负荷率(%)
 */
private BigDecimal calculateLoadRate(BigDecimal pp, BigDecimal rated_power) {
    if (rated_power == null || rated_power.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
    }
    
    return pp.multiply(new BigDecimal(100))
            .divide(rated_power, 2, RoundingMode.HALF_UP);
}
```

## 4. 实现代码示例

### Controller层
```java
@RestController
@RequestMapping("/ems/monitor")
@Api(tags = "能源实时监控")
@Slf4j
public class EnergyRealMonitorController {

    @Autowired
    private EnergyRealMonitorService energyRealMonitorService;
    
    @GetMapping("/getDimensionTree")
    @ApiOperation("获取维度树")
    public Result<List<DimensionTreeVO>> getDimensionTree(@RequestParam Integer nowtype) {
        List<DimensionTreeVO> result = energyRealMonitorService.getDimensionTree(nowtype);
        return Result.OK(result);
    }
    
    @GetMapping("/getRealTimeData")
    @ApiOperation("获取实时监控数据")
    public Result<List<Map<String, Object>>> getRealTimeData(@RequestParam String orgCode, @RequestParam Integer nowtype) {
        List<Map<String, Object>> result = energyRealMonitorService.getRealTimeData(orgCode, nowtype);
        return Result.OK(result);
    }
}
```

### Service层
```java
@Service
@Slf4j
public class EnergyRealMonitorServiceImpl implements EnergyRealMonitorService {

    @Autowired
    private SysDepartMapper sysDepartMapper;
    
    @Autowired
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEpEquEnergyDaycountMapper tbEpEquEnergyDaycountMapper;
    
    @Override
    public List<DimensionTreeVO> getDimensionTree(Integer nowtype) {
        // 根据nowtype获取不同类型的维度树
        List<SysDepart> departments = sysDepartMapper.selectDepartsByType(nowtype);
        return buildDimensionTree(departments);
    }
    
    @Override
    public List<Map<String, Object>> getRealTimeData(String orgCode, Integer nowtype) {
        // 1. 根据orgCode查询关联的仪表列表
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(orgCode);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TbModule module : modules) {
            // 2. 查询仪表的实时数据
            TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(module.getModuleId());
            if (eleData == null) {
                continue;
            }
            
            // 3. 查询仪表的日用电量
            TbEpEquEnergyDaycount dayCount = tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(
                module.getModuleId(), DateUtil.beginOfDay(new Date()));
            
            // 4. 组装数据
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("module_name", module.getModuleName());
            dataMap.put("module_id", module.getModuleId());
            dataMap.put("Equ_Electric_DT", eleData.getEquElectricDT());
            
            // 5. 计算负荷状态
            String loadStatus = calculateLoadStatus(
                eleData.getIA(), eleData.getIB(), eleData.getIC(),
                eleData.getUA(), eleData.getUB(), eleData.getUC()
            );
            dataMap.put("loadStatus", loadStatus);
            
            // 6. 计算负荷率
            BigDecimal loadRate = calculateLoadRate(
                eleData.getPp(), 
                module.getRatedPower() != null ? new BigDecimal(module.getRatedPower()) : BigDecimal.ZERO
            );
            dataMap.put("loadRate", loadRate);
            
            // 7. 设置其他数据 - 使用原始字段名
            dataMap.put("PFS", eleData.getPFS());
            dataMap.put("HZ", eleData.getHZ());
            dataMap.put("pp", eleData.getPp());
            
            // 电压电流
            dataMap.put("UA", eleData.getUA());
            dataMap.put("UB", eleData.getUB());
            dataMap.put("UC", eleData.getUC());
            dataMap.put("IA", eleData.getIA());
            dataMap.put("IB", eleData.getIB());
            dataMap.put("IC", eleData.getIC());
            
            // 功率因数
            dataMap.put("PFa", eleData.getPFa());
            dataMap.put("PFb", eleData.getPFb());
            dataMap.put("PFc", eleData.getPFc());
            
            // 有功功率
            dataMap.put("Pa", eleData.getPa());
            dataMap.put("Pb", eleData.getPb());
            dataMap.put("Pc", eleData.getPc());
            
            // 电能
            dataMap.put("KWH", eleData.getKWH());
            dataMap.put("KVARH", eleData.getKVARH());
            
            // 额定功率
            dataMap.put("rated_power", module.getRatedPower());
            
            // 9. 设置日用电量
            if (dayCount != null) {
                dataMap.put("dailyPower", dayCount.getEnergyCount());
            }
            
            result.add(dataMap);
        }
        
        return result;
    }
    
    // 负荷状态计算和负荷率计算方法实现
    // ...
}
```

### Mapper层
```java
@Mapper
public interface TbEquEleDataMapper {
    
    /**
     * 根据仪表ID查询最新的实时数据
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEleData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
}

@Mapper
public interface TbEpEquEnergyDaycountMapper {
    
    /**
     * 根据仪表ID和日期查询当天的能耗数据
     * @param moduleId 仪表ID
     * @param date 日期
     * @return 能耗数据
     */
    TbEpEquEnergyDaycount selectTodayDataByModuleId(@Param("moduleId") String moduleId, @Param("date") Date date);
}

@Mapper
public interface TbModuleMapper {
    
    /**
     * 根据组织编码查询关联的仪表
     * @param orgCode 组织编码
     * @return 仪表列表
     */
    List<TbModule> selectModulesByOrgCode(@Param("orgCode") String orgCode);
}
```

## 5. SQL实现示例

```xml
<!-- TbEquEleDataMapper.xml -->
<select id="selectLatestDataByModuleId" resultType="com.jeecg.ems.entity.TbEquEleData">
    SELECT * FROM tb_equ_ele_data 
    WHERE Module_ID = #{moduleId} 
    ORDER BY Equ_Electric_DT DESC 
    LIMIT 1
</select>

<!-- TbEpEquEnergyDaycountMapper.xml -->
<select id="selectTodayDataByModuleId" resultType="com.jeecg.ems.entity.TbEpEquEnergyDaycount">
    SELECT * FROM tb_ep_equ_energy_daycount 
    WHERE module_id = #{moduleId} 
    AND DATE(dt) = DATE(#{date})
    LIMIT 1
</select>

<!-- TbModuleMapper.xml -->
<select id="selectModulesByOrgCode" resultType="com.jeecg.ems.entity.TbModule">
    SELECT * FROM tb_module 
    WHERE FIND_IN_SET(#{orgCode}, sys_org_code)
    AND isaction = 'Y'
</select>
```
