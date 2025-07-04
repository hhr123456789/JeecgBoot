# 实时数据监控接口设计

## 1. 获取实时监控数据接口

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

#### 电力数据响应格式 (nowtype=1或2)
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
      "rated_power": 1000.00,
      "energy_type": 1
    }
  ]
}
```

#### 天然气/压缩空气/用水数据响应格式 (nowtype=3/4/5)
```json
{
  "code": 200,
  "message": "操作成功",
  "result": [
    {
      "module_name": "天然气表1#",
      "module_id": "yj0004_1",
      "equ_energy_dt": "2025-07-04 16:30:45",
      "energy_temperature": 25.6,
      "energy_pressure": 0.8,
      "energy_winkvalue": 2.345,
      "energy_accumulatevalue": 1256.78,
      "dailyPower": 125.67,
      "rated_power": 1.00,
      "energy_type": 8
    }
  ]
}
```

## 2. 接口实现逻辑

### 负荷状态计算（仅电力类型）
```java
/**
 * 计算负荷状态
 * @param IA A相电流
 * @param IB B相电流
 * @param IC C相电流
 * @param UA A相电压
 * @param UB B相电压
 * @param UC C相电压
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

### 负荷率计算（仅电力类型）
```java
/**
 * 计算负荷率
 * @param pp 当前功率
 * @param rated_power 额定功率
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

## 3. 实现代码示例

### Controller层
```java
@RestController
@RequestMapping("/ems/monitor")
@Api(tags = "能源实时监控")
@Slf4j
public class EnergyRealMonitorController {

    @Autowired
    private EnergyRealMonitorService energyRealMonitorService;
    
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
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;
    
    @Autowired
    private TbEpEquEnergyDaycountMapper tbEpEquEnergyDaycountMapper;
    
    @Override
    public List<Map<String, Object>> getRealTimeData(String orgCode, Integer nowtype) {
        // 1. 根据orgCode查询关联的仪表列表
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(orgCode);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TbModule module : modules) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("module_name", module.getModuleName());
            dataMap.put("module_id", module.getModuleId());
            dataMap.put("rated_power", module.getRatedPower());
            dataMap.put("energy_type", module.getEnergyType());
            
            // 查询仪表的日用电量
            TbEpEquEnergyDaycount dayCount = tbEpEquEnergyDaycountMapper.selectTodayDataByModuleId(
                module.getModuleId(), DateUtil.beginOfDay(new Date()));
                
            // 设置日用量
            if (dayCount != null) {
                dataMap.put("dailyPower", dayCount.getEnergyCount());
            }
            
            // 根据能源类型获取不同的实时数据
            if (nowtype == 1 || nowtype == 2) {
                // 电力数据
                TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(module.getModuleId());
                if (eleData == null) {
                    continue;
                }
                
                dataMap.put("Equ_Electric_DT", eleData.getEquElectricDT());
                
                // 计算负荷状态
                String loadStatus = calculateLoadStatus(
                    eleData.getIA(), eleData.getIB(), eleData.getIC(),
                    eleData.getUA(), eleData.getUB(), eleData.getUC()
                );
                dataMap.put("loadStatus", loadStatus);
                
                // 计算负荷率
                BigDecimal loadRate = calculateLoadRate(
                    eleData.getPp(), 
                    module.getRatedPower() != null ? new BigDecimal(module.getRatedPower()) : BigDecimal.ZERO
                );
                dataMap.put("loadRate", loadRate);
                
                // 设置其他电力数据
                dataMap.put("PFS", eleData.getPFS());
                dataMap.put("HZ", eleData.getHZ());
                dataMap.put("pp", eleData.getPp());
                dataMap.put("UA", eleData.getUA());
                dataMap.put("UB", eleData.getUB());
                dataMap.put("UC", eleData.getUC());
                dataMap.put("IA", eleData.getIA());
                dataMap.put("IB", eleData.getIB());
                dataMap.put("IC", eleData.getIC());
                dataMap.put("PFa", eleData.getPFa());
                dataMap.put("PFb", eleData.getPFb());
                dataMap.put("PFc", eleData.getPFc());
                dataMap.put("Pa", eleData.getPa());
                dataMap.put("Pb", eleData.getPb());
                dataMap.put("Pc", eleData.getPc());
                dataMap.put("KWH", eleData.getKWH());
                dataMap.put("KVARH", eleData.getKVARH());
            } else {
                // 天然气/压缩空气/用水数据
                TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(module.getModuleId());
                if (energyData == null) {
                    continue;
                }
                
                dataMap.put("equ_energy_dt", energyData.getEquEnergyDt());
                dataMap.put("energy_temperature", energyData.getEnergyTemperature());
                dataMap.put("energy_pressure", energyData.getEnergyPressure());
                dataMap.put("energy_winkvalue", energyData.getEnergyWinkvalue());
                dataMap.put("energy_accumulatevalue", energyData.getEnergyAccumulatevalue());
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
     * 根据仪表ID查询最新的电力实时数据
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEleData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
}

@Mapper
public interface TbEquEnergyDataMapper {
    
    /**
     * 根据仪表ID查询最新的能源实时数据（天然气/压缩空气/用水）
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEnergyData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
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

## 4. SQL实现示例

```xml
<!-- TbEquEleDataMapper.xml -->
<select id="selectLatestDataByModuleId" resultType="com.jeecg.ems.entity.TbEquEleData">
    SELECT * FROM tb_equ_ele_data 
    WHERE Module_ID = #{moduleId} 
    ORDER BY Equ_Electric_DT DESC 
    LIMIT 1
</select>

<!-- TbEquEnergyDataMapper.xml -->
<select id="selectLatestDataByModuleId" resultType="com.jeecg.ems.entity.TbEquEnergyData">
    SELECT * FROM tb_equ_energy_data 
    WHERE module_id = #{moduleId} 
    ORDER BY equ_energy_dt DESC 
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

## 5. 数据表说明

### 仪表基础信息表(tb_module)
存储所有仪表的基本信息，包括电表、天然气表、压缩空气表、水表等。
- `module_id`: 仪表编号，唯一标识一个仪表
- `module_name`: 仪表名称
- `energy_type`: 能源类型(1:电力, 2:水, 5:压缩空气, 8:天然气)
- `rated_power`: 额定功率
- `sys_org_code`: 维度，记录仪表所属部门，多个部门用逗号分隔

### 电力实时数据表(tb_equ_ele_data)
存储电表的实时数据。
- `Module_ID`: 仪表编号
- `Equ_Electric_DT`: 采集时间
- `UA`, `UB`, `UC`: 三相电压
- `IA`, `IB`, `IC`: 三相电流
- `pp`: 总有功功率
- `PFS`: 总功率因素
- `KWH`: 正向有功总电能

### 能源实时数据表(tb_equ_energy_data)
存储天然气、压缩空气、水表等非电力仪表的实时数据。
- `module_id`: 仪表编号
- `equ_energy_dt`: 采集时间
- `energy_temperature`: 温度
- `energy_pressure`: 压力
- `energy_winkvalue`: 瞬时流量
- `energy_accumulatevalue`: 累计值

### 日能耗统计表(tb_ep_equ_energy_daycount)
存储各类仪表的日能耗统计数据。
- `module_id`: 仪表编号
- `dt`: 统计日期
- `energy_count`: 能耗值
- `strat_count`: 开始值
- `end_count`: 结束值
