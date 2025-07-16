# 运行状态接口设计

## 1. 获取设备运行状态接口

### 接口信息
- **URL**: `/energy/monitor/getRunStatus`
- **Method**: GET
- **功能**: 获取设备运行状态数据

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|------|------|
| orgCode | String | 是 | 部门编码 |
| deviceStatus | Integer | 否 | 设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障) |

### 响应结果

```json
{
  "code": 200,
  "message": "操作成功",
  "result": [
    {
      "device_name": "1号生产线注塑机",
      "所属车间": "1车间",
      "电流(A)": 49.97,
      "电压(V)": 388.25,
      "功率因素": 1.06,
      "有功功率(kW)": 141.11,
      "运行状态": "运行中",
      "status_code": 1,
      "energy_type": 1,
      "last_collection_time": "2023-08-17 10:30:45"
    },
    {
      "device_name": "2号生产线注塑机",
      "所属车间": "1车间",
      "电流(A)": 0.00,
      "电压(V)": 380.00,
      "功率因素": 0.00,
      "有功功率(kW)": 150.20,
      "运行状态": "已停止",
      "status_code": 2,
      "energy_type": 1,
      "last_collection_time": "2023-08-17 10:28:32"
    },
    {
      "device_name": "3号生产线注塑机",
      "所属车间": "2车间",
      "电流(A)": 0.00,
      "电压(V)": 0.00,
      "功率因素": 0.00,
      "有功功率(kW)": 0.00,
      "运行状态": "通讯故障",
      "status_code": 3,
      "energy_type": 1,
      "last_collection_time": "2023-08-16 08:15:20"
    },
    {
      "device_name": "1号车间水表",
      "所属车间": "1车间",
      "瞬时流量(m³/h)": 2.35,
      "累计用量(m³)": 1256.78,
      "运行状态": "运行中",
      "status_code": 1,
      "energy_type": 2,
      "last_collection_time": "2023-08-17 10:35:12"
    },
    {
      "device_name": "2号车间空压机",
      "所属车间": "2车间",
      "压力(MPa)": 0.8,
      "瞬时流量(m³/h)": 0.05,
      "累计用量(m³)": 5689.32,
      "运行状态": "已停止",
      "status_code": 2,
      "energy_type": 5,
      "last_collection_time": "2023-08-17 10:22:18"
    },
    {
      "device_name": "锅炉天然气表",
      "所属车间": "动力车间",
      "温度(℃)": 25.6,
      "压力(MPa)": 0.3,
      "瞬时流量(m³/h)": 3.42,
      "累计用量(m³)": 8965.45,
      "运行状态": "运行中",
      "status_code": 1,
      "energy_type": 8,
      "last_collection_time": "2023-08-17 10:32:56"
    }
  ]
}
```

## 2. 接口实现逻辑

### 处理流程
1. 根据部门编码(orgCode)查询对应的部门ID
2. 使用部门ID查询关联的仪表列表
3. 根据仪表的能源类型分别获取对应的实时数据
   - 电力类型(1): 从tb_equ_ele_data表获取电力数据
   - 水类型(2): 从tb_equ_energy_data表获取水表数据
   - 压缩空气类型(5): 从tb_equ_energy_data表获取压缩空气数据
   - 天然气类型(8): 从tb_equ_energy_data表获取天然气数据
4. 根据最后采集时间判断是否为通讯故障（超过1小时）
5. 根据不同能源类型的特性判断设备运行状态
   - 电力: 根据三相电流判断
   - 其他能源: 根据瞬时流量判断
6. 根据筛选条件过滤设备
7. 返回结果

### 部门编码转换为部门ID
```java
/**
 * 根据部门编码获取部门ID
 * @param orgCode 部门编码
 * @return 部门ID
 */
private String getDepartIdByOrgCode(String orgCode) {
    try {
        // 直接根据部门编码查询部门信息
        QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_code", orgCode);
        SysDepart depart = sysDepartService.getOne(queryWrapper);
        
        log.info("通过org_code={}直接查询部门结果: {}", orgCode, depart);
        if(depart != null) {
            return depart.getId();
        }
        
        // 如果直接查询不到，尝试其他方法
        JSONObject departInfo = sysDepartService.queryAllParentIdByOrgCode(orgCode);
        log.info("通过queryAllParentIdByOrgCode查询结果: {}", departInfo);
        if(departInfo != null && departInfo.containsKey("departId")) {
            return departInfo.getString("departId");
        }
        
        return null;
    } catch (Exception e) {
        log.error("获取部门ID失败", e);
        return null;
    }
}
```

### 设备运行状态判断
```java
/**
 * 判断电力设备运行状态
 * @param eleData 电力数据
 * @param lastCollectionTime 最后采集时间
 * @param currentTime 当前系统时间
 * @return 运行状态
 */
private String determineElectricRunStatus(TbEquEleData eleData, Date lastCollectionTime, Date currentTime) {
    if (eleData == null) {
        return "未知";
    }
    
    // 检查是否为通讯故障（最后采集时间超过1小时）
    if (lastCollectionTime != null && currentTime != null) {
        long timeDiff = currentTime.getTime() - lastCollectionTime.getTime();
        if (timeDiff > COMMUNICATION_FAULT_THRESHOLD) {
            return "通讯故障";
        }
    }
    
    // 根据电流值判断设备是否在运行
    // 如果三相电流都接近0，则认为设备已停止
    BigDecimal threshold = new BigDecimal("0.5");
    if (eleData.getIA() != null && eleData.getIB() != null && eleData.getIC() != null &&
            eleData.getIA().compareTo(threshold) <= 0 && 
            eleData.getIB().compareTo(threshold) <= 0 && 
            eleData.getIC().compareTo(threshold) <= 0) {
        return "已停止";
    }
    
    return "运行中";
}

/**
 * 判断其他能源设备运行状态
 * @param energyData 能源数据
 * @param lastCollectionTime 最后采集时间
 * @param currentTime 当前系统时间
 * @return 运行状态文本
 */
private String determineEnergyRunStatus(TbEquEnergyData energyData, Date lastCollectionTime, Date currentTime) {
    if (energyData == null) {
        return "未知";
    }
    
    // 检查是否为通讯故障（最后采集时间超过1小时）
    if (lastCollectionTime != null && currentTime != null) {
        long timeDiff = currentTime.getTime() - lastCollectionTime.getTime();
        if (timeDiff > COMMUNICATION_FAULT_THRESHOLD) {
            return "通讯故障";
        }
    }
    
    // 根据瞬时流量判断设备是否在运行
    BigDecimal threshold = new BigDecimal("0.1");
    if (energyData.getEnergyWinkvalue() != null && 
            energyData.getEnergyWinkvalue().compareTo(threshold) <= 0) {
        return "已停止";
    }
    
    return "运行中";
}
```

## 3. 实现代码示例

### Controller层
```java
@RestController
@RequestMapping("/energy/monitor")
@Api(tags = "设备运行状态")
@Slf4j
public class RunStatusController {

    @Autowired
    private IRunStatusService runStatusService;
    
    /**
     * 获取设备运行状态
     *
     * @param orgCode 部门编码
     * @param deviceStatus 设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障)
     * @return 设备运行状态数据
     */
    @ApiOperation(value = "获取设备运行状态", notes = "获取设备运行状态数据")
    @GetMapping("/getRunStatus")
    public Result<List<Map<String, Object>>> getRunStatus(
            @ApiParam(value = "部门编码", required = true) @RequestParam String orgCode,
            @ApiParam(value = "设备状态筛选(0:全部, 1:运行中, 2:已停止, 3:通讯故障)") @RequestParam(required = false, defaultValue = "0") Integer deviceStatus) {
        log.info("获取设备运行状态，部门编码：{}，设备状态：{}", orgCode, deviceStatus);
        List<Map<String, Object>> result = runStatusService.getRunStatus(orgCode, deviceStatus);
        log.info("查询结果条数：{}", result.size());
        return Result.OK(result);
    }
}
```

### Service层
```java
@Service
@Slf4j
public class RunStatusServiceImpl implements IRunStatusService {

    @Autowired
    private TbModuleMapper tbModuleMapper;
    
    @Autowired
    private TbEquEleDataMapper tbEquEleDataMapper;
    
    @Autowired
    private TbEquEnergyDataMapper tbEquEnergyDataMapper;
    
    @Autowired
    private ISysDepartService sysDepartService;
    
    // 通讯故障时间阈值，单位：毫秒（1小时）
    private static final long COMMUNICATION_FAULT_THRESHOLD = 60 * 60 * 1000;
    
    // 能源类型常量
    private static final int ENERGY_TYPE_ELECTRIC = 1; // 电力
    private static final int ENERGY_TYPE_WATER = 2;    // 水
    private static final int ENERGY_TYPE_AIR = 5;      // 压缩空气
    private static final int ENERGY_TYPE_GAS = 8;      // 天然气
    
    @Override
    public List<Map<String, Object>> getRunStatus(String orgCode, Integer deviceStatus) {
        // 1. 将部门编码转换为部门ID
        String departId = getDepartIdByOrgCode(orgCode);
        log.info("部门编码 {} 对应的部门ID为: {}", orgCode, departId);
        
        if(departId == null) {
            // 如果找不到对应的部门ID，直接使用orgCode作为查询条件
            log.warn("未找到部门编码 {} 对应的部门ID，将直接使用部门编码查询", orgCode);
            departId = orgCode;
        }
        
        // 2. 根据部门ID查询关联的仪表列表
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(departId);
        log.info("根据部门ID/编码 {} 查询到 {} 个仪表", departId, modules.size());
        
        List<Map<String, Object>> result = new ArrayList<>();
        Date currentTime = new Date(); // 当前系统时间
        
        for (TbModule module : modules) {
            // 检查仪表是否有能源类型
            if (module.getEnergyType() == null) {
                log.warn("仪表 {} 未设置能源类型，跳过处理", module.getModuleId());
                continue;
            }
            
            Map<String, Object> dataMap = new HashMap<>();
            String runStatusText;
            int runStatusCode;
            Date lastCollectionTime = null;
            
            // 根据不同能源类型处理数据
            switch (module.getEnergyType()) {
                case ENERGY_TYPE_ELECTRIC:
                    // 处理电力类型
                    TbEquEleData eleData = tbEquEleDataMapper.selectLatestDataByModuleId(module.getModuleId());
                    if (eleData == null) {
                        log.warn("未找到仪表 {} 的电力数据", module.getModuleId());
                        continue;
                    }
                    
                    // 获取最后采集时间
                    lastCollectionTime = eleData.getEquElectricDT();
                    
                    // 判断设备运行状态
                    runStatusText = determineElectricRunStatus(eleData, lastCollectionTime, currentTime);
                    runStatusCode = getRunStatusCode(runStatusText);
                    
                    // 添加电力特有数据
                    dataMap.put("电流(A)", eleData.getIA());
                    dataMap.put("电压(V)", eleData.getUA());
                    dataMap.put("功率因素", eleData.getPFS());
                    dataMap.put("有功功率(kW)", eleData.getPp());
                    break;
                    
                case ENERGY_TYPE_WATER:
                case ENERGY_TYPE_AIR:
                case ENERGY_TYPE_GAS:
                    // 处理水、压缩空气、天然气类型
                    TbEquEnergyData energyData = tbEquEnergyDataMapper.selectLatestDataByModuleId(module.getModuleId());
                    if (energyData == null) {
                        log.warn("未找到仪表 {} 的能源数据", module.getModuleId());
                        continue;
                    }
                    
                    // 获取最后采集时间
                    lastCollectionTime = energyData.getEquEnergyDt();
                    
                    // 判断设备运行状态
                    runStatusText = determineEnergyRunStatus(energyData, lastCollectionTime, currentTime);
                    runStatusCode = getRunStatusCode(runStatusText);
                    
                    // 添加能源特有数据
                    if (module.getEnergyType() == ENERGY_TYPE_WATER) {
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    } else if (module.getEnergyType() == ENERGY_TYPE_AIR) {
                        dataMap.put("压力(MPa)", energyData.getEnergyPressure());
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    } else { // 天然气
                        dataMap.put("温度(℃)", energyData.getEnergyTemperature());
                        dataMap.put("压力(MPa)", energyData.getEnergyPressure());
                        dataMap.put("瞬时流量(m³/h)", energyData.getEnergyWinkvalue());
                        dataMap.put("累计用量(m³)", energyData.getEnergyAccumulatevalue());
                    }
                    break;
                    
                default:
                    log.warn("未知能源类型: {}, 仪表ID: {}", module.getEnergyType(), module.getModuleId());
                    continue;
            }
            
            // 根据筛选条件过滤
            if (deviceStatus != null && deviceStatus != 0) {
                if (deviceStatus != runStatusCode) {
                    continue;
                }
            }
            
            // 获取所属车间信息
            String workshopName = getWorkshopName(module.getSysOrgCode());
            
            // 添加通用数据
            dataMap.put("device_name", module.getModuleName());
            dataMap.put("所属车间", workshopName);
            dataMap.put("运行状态", runStatusText);
            dataMap.put("status_code", runStatusCode);
            dataMap.put("energy_type", module.getEnergyType());
            dataMap.put("last_collection_time", lastCollectionTime);
            
            result.add(dataMap);
        }
        
        return result;
    }
}
```

### Mapper层
```java
@Mapper
public interface TbModuleMapper extends BaseMapper<TbModule> {
    
    /**
     * 根据组织编码查询关联的仪表
     * @param orgCode 组织编码
     * @return 仪表列表
     */
    List<TbModule> selectModulesByOrgCode(@Param("orgCode") String orgCode);
}

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
     * 根据仪表ID查询最新的能源实时数据
     * @param moduleId 仪表ID
     * @return 实时数据
     */
    TbEquEnergyData selectLatestDataByModuleId(@Param("moduleId") String moduleId);
}
```

## 4. SQL实现示例

```xml
<!-- TbModuleMapper.xml -->
<select id="selectModulesByOrgCode" resultType="org.jeecg.modules.energy.entity.TbModule">
    SELECT * FROM tb_module 
    WHERE FIND_IN_SET(#{orgCode}, sys_org_code)
    AND isaction = 'Y'
</select>

<!-- TbEquEleDataMapper.xml -->
<select id="selectLatestDataByModuleId" resultType="org.jeecg.modules.energy.entity.TbEquEleData">
    SELECT * FROM tb_equ_ele_data 
    WHERE Module_ID = #{moduleId} 
    ORDER BY Equ_Electric_DT DESC 
    LIMIT 1
</select>

<!-- TbEquEnergyDataMapper.xml -->
<select id="selectLatestDataByModuleId" resultType="org.jeecg.modules.energy.entity.TbEquEnergyData">
    SELECT * FROM tb_equ_energy_data 
    WHERE Module_ID = #{moduleId} 
    ORDER BY Equ_Energy_DT DESC 
    LIMIT 1
</select>
```

## 5. 数据表说明

### 仪表基础信息表(tb_module)
存储所有仪表的基本信息，包括电表、天然气表、压缩空气表、水表等。
- `id`: 主键ID
- `module_name`: 仪表名称
- `module_id`: 仪表编号，唯一标识一个仪表
- `energy_type`: 能源类型(1:电力, 2:水, 5:压缩空气, 8:天然气)
- `rated_power`: 额定功率
- `sys_org_code`: 维度，记录仪表所属部门ID
- `isaction`: 是否启用(Y/N)

### 电力实时数据表(tb_equ_ele_data)
存储电表的实时数据。
- `id`: 主键ID
- `Module_ID`: 仪表编号
- `Equ_Electric_DT`: 采集时间
- `UA`, `UB`, `UC`: 三相电压
- `IA`, `IB`, `IC`: 三相电流
- `pp`: 总有功功率
- `PFS`: 总功率因素
- `KWH`: 正向有功总电能

### 能源实时数据表(tb_equ_energy_data)
存储水、压缩空气、天然气等非电力能源的实时数据。
- `id`: 主键ID
- `Module_ID`: 仪表编号
- `Equ_Energy_DT`: 采集时间
- `energy_temperature`: 能源温度
- `energy_pressure`: 能源压力
- `energy_winkvalue`: 瞬时值
- `energy_accumulatevalue`: 累计值

### 系统部门表(sys_depart)
存储系统部门信息。
- `id`: 部门ID
- `parent_id`: 父部门ID
- `depart_name`: 部门名称
- `org_code`: 部门编码
- `org_category`: 机构类别(1:公司, 2:组织机构, 3:岗位)
- `org_type`: 机构类型(1:一级部门, 2:子部门)

## 6. 前端界面说明

前端界面主要展示设备的运行状态信息，包括：

1. 左侧维度树：展示组织机构树，用户可以选择不同的部门查看设备运行状态
2. 顶部筛选条件：可以按设备状态(0:全部, 1:运行中, 2:已停止, 3:通讯故障)进行筛选
3. 表格展示：
   - 设备名称
   - 所属车间
   - 能源类型(1:电力, 2:水, 5:压缩空气, 8:天然气)
   - 根据能源类型显示不同的数据列:
     - 电力: 电流(A)、电压(V)、功率因素、有功功率(kW)
     - 水: 瞬时流量(m³/h)、累计用量(m³)
     - 压缩空气: 压力(MPa)、瞬时流量(m³/h)、累计用量(m³)
     - 天然气: 温度(℃)、压力(MPa)、瞬时流量(m³/h)、累计用量(m³)
   - 运行状态
   - 最后采集时间

界面根据设备的运行状态显示不同的状态标识：
- 运行中: 绿色状态标识
- 已停止: 灰色状态标识
- 通讯故障: 黄色状态标识
