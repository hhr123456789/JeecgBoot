<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs defaultActiveKey="info1" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
            <a-card :bordered="false" style="height: 100%">
              <MultiSelectDimensionTree
                :ref="(el) => setTreeRef(el, item.key)"
                @select="onDepartTreeSelect"
                :nowtype="item.nowtype"
                :select-level="2"
                style="margin-top:-20px ;"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1" style="margin-top: 10px;">
      <!-- 条件查询栏 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 时间范围 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">时间范围：</span>
            <a-range-picker 
              v-model:value="dateRange" 
              :show-time="{ format: 'HH:mm' }" 
              format="YYYY-MM-DD HH:mm"
              class="custom-picker"
            />
          </div>
          
          <!-- 仪表下拉选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">仪表选择：</span>
            <a-select
              v-model:value="selectedMeters"
              mode="multiple"
              style="width: 180px"
              class="custom-select"
              placeholder="请选择仪表"
              :maxTagCount="1"
              :maxTagTextLength="10"
            >
              <a-select-option v-for="meter in meters" :key="meter.value" :value="meter.value">
                {{ meter.label }}
              </a-select-option>
            </a-select>
          </div>
          
          <!-- 仪表参数多选 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">参数选择：</span>
            <a-select
              v-model:value="selectedParams"
              mode="multiple"
              style="width: 180px"
              class="custom-select"
              placeholder="请选择参数"
              :maxTagCount="1"
              :maxTagTextLength="10"
            >
              <a-select-option v-for="param in parameterOptions" :key="param.value" :value="param.value">
                {{ param.text }}
              </a-select-option>
            </a-select>
          </div>
          
          <!-- 查询间隔下拉选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">查询间隔：</span>
            <a-select
              v-model:value="queryInterval"
              style="width: 120px"
              class="custom-select"
              placeholder="请选择间隔"
            >
              <a-select-option v-for="interval in queryIntervalOptions" :key="interval.value" :value="interval.value">
                {{ interval.text }}
              </a-select-option>
            </a-select>
          </div>
          
          <!-- 查询方式下拉选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">查询方式：</span>
            <a-select
              v-model:value="displayMode"
              @change="handleQuery"
              style="width: 120px"
              class="custom-select"
              placeholder="请选择显示方式"
            >
              <a-select-option v-for="method in queryMethodOptions" :key="method.value" :value="method.value">
                {{ method.text }}
              </a-select-option>
            </a-select>
          </div>
          
          <!-- 图表类型切换 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">图表类型：</span>
            <a-radio-group v-model:value="chartType" size="small">
              <a-radio-button value="line">曲线图</a-radio-button>
              <a-radio-button value="bar">柱状图</a-radio-button>
            </a-radio-group>
          </div>

          <!-- 查询和导出按钮 -->
          <div class="flex gap-2">
            <a-button type="primary" class="custom-button" @click="handleQuery">查询</a-button>
            <a-button type="default" class="custom-button" @click="handleExportExcel" :loading="exportLoading">
              <template #icon>
                <DownloadOutlined />
              </template>
              导出数据
            </a-button>
          </div>
        </div>
      </div>

      <!-- 统一显示模式 - 所有数据在一个图表中显示 -->
      <div v-if="displayMode === 'unified' || displayMode === '1'" class="bg-white rounded p-3 mb-4">
        <div class="mb-3">
          <div class="text-sm">所有数据统一显示</div>
        </div>

        <!-- 检查是否有数据 -->
        <template v-if="hasChartData()">
          <MonitorChart
            :chartData="getUnifiedChartData()"
            chartId="unified-chart"
            :activeIndex="activeIndex"
            :chartType="chartType"
            :enableMultiYAxis="true"
            @mouseOnIndex="handleMouseOnIndex"
            @mouseOut="handleMouseOut"
          />
        </template>

        <!-- 无数据时的友好提示 -->
        <template v-else>
          <div class="flex flex-col items-center justify-center py-16 text-gray-500">
            <div class="text-6xl mb-4">📊</div>
            <div class="text-lg font-medium mb-2">暂无监控数据</div>
            <div class="text-sm text-center max-w-md">
              <p class="mb-2">当前条件下没有找到监控数据，可能的原因：</p>
              <ul class="text-left space-y-1">
                <li>• 该能源类型的数据采集尚未配置</li>
                <li>• 选择的时间范围内没有数据记录</li>
                <li>• 仪表设备离线或数据传输异常</li>
              </ul>
              <p class="mt-3 text-xs text-gray-400">
                请联系系统管理员检查数据采集配置，或尝试选择其他时间范围
              </p>
            </div>
          </div>
        </template>
      </div>

      <!-- 分开显示模式 - 为每个参数和每个仪表生成独立图表 -->
      <template v-else>
        <!-- 如果有API数据，使用API数据 -->
        <template v-if="hasChartData()">
          <div v-for="(chartData, index) in separateChartsData" :key="`api-chart-${index}`" class="bg-white rounded p-3 mb-4">
            <div class="mb-3">
              <div class="text-sm">{{ chartData.moduleName }} - {{ chartData.parameter }}</div>
            </div>
            <MonitorChart
              :chartData="chartData"
              :chartId="`api-chart-${index}`"
              :activeIndex="activeIndex"
              :chartType="chartType"
              @mouseOnIndex="handleMouseOnIndex"
              @mouseOut="handleMouseOut"
            />
          </div>
        </template>

        <!-- 无数据时的友好提示 -->
        <template v-else>
          <div class="bg-white rounded p-3 mb-4">
            <div class="flex flex-col items-center justify-center py-16 text-gray-500">
              <div class="text-6xl mb-4">📊</div>
              <div class="text-lg font-medium mb-2">暂无监控数据</div>
              <div class="text-sm text-center max-w-md">
                <p class="mb-2">当前条件下没有找到监控数据，可能的原因：</p>
                <ul class="text-left space-y-1">
                  <li>• 该能源类型的数据采集尚未配置</li>
                  <li>• 选择的时间范围内没有数据记录</li>
                  <li>• 仪表设备离线或数据传输异常</li>
                </ul>
                <p class="mt-3 text-xs text-gray-400">
                  请联系系统管理员检查数据采集配置，或尝试选择其他时间范围
                </p>
              </div>
            </div>
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import MonitorChart from './components/MonitorChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import MultiSelectDimensionTree from './components/MultiSelectDimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { initDictOptions } from '/@/utils/dict/index';
import { getModulesByOrgCode, getRealTimeMonitorData, getRealTimeData, exportRealTimeData, type ModuleInfo, type RealTimeMonitorRequest } from './api';
import dayjs from 'dayjs';
import { DownloadOutlined } from '@ant-design/icons-vue';

const { createMessage } = useMessage();

// 当前活动的数据点索引
const activeIndex = ref<number>(-1);

// 防抖标志
let debounceTimer: number | null = null;
let queryDebounceTimer: number | null = null;

// 导出状态
const exportLoading = ref(false);

// 处理鼠标在图表上的移动 - 添加防抖
const handleMouseOnIndex = (index: number) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }

  debounceTimer = window.setTimeout(() => {
    if (activeIndex.value !== index) {
      activeIndex.value = index;
    }
  }, 50); // 50ms 防抖
};

// 处理鼠标离开图表 - 添加防抖
const handleMouseOut = () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  debounceTimer = window.setTimeout(() => {
    console.log('Setting active index to -1');
    activeIndex.value = -1;
  }, 100); // 100ms 防抖
};

// 生成图表唯一ID
const getChartId = (param: string, meterId: string): string => {
  return `chart-${param}-${meterId}`;
};

// 加载状态
const loading = ref(false);

// 当前选中的能源类型
const currentNowtype = ref(1);

// 当前选中的部门编码
const currentOrgCode = ref('');

// 当前激活的标签页
const activeTabKey = ref('info1');

// 维度列表
const dimensionList = ref<any[]>([]);

// 树组件引用
const treeRefs = ref<Record<string, any>>({});

// 设置树组件引用
const setTreeRef = (el, key) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 存储每个标签页选中的节点信息
const selectedNodesMap = ref({
  info1: null,
  info2: null,
  info3: null,
  info4: null,
  info5: null
});

// 字典数据
const parameterOptions = ref([]); // 参数选择字典数据
const queryIntervalOptions = ref([]); // 查询间隔字典数据
const queryMethodOptions = ref([]); // 查询方式字典数据

// 查询条件 - 默认选择两个仪表以便测试联动效果
const dateRange = ref([dayjs().startOf('day'), dayjs()]);
const selectedMeters = ref<string[]>([]);
const selectedParams = ref<string[]>([]);
const queryInterval = ref(''); // 查询间隔从字典获取
const displayMode = ref(''); // 查询方式从字典获取
const chartType = ref('line'); // 图表类型：line(曲线图) 或 bar(柱状图)

// 仪表列表 - 从API获取
const meters = ref<Array<{ label: string; value: string }>>([]);
const allModules = ref<ModuleInfo[]>([]); // 存储完整的仪表信息

// 获取选中的仪表名称
const selectedMeterLabels = computed(() => {
  return selectedMeters.value.map(meterId => {
    const meter = meters.value.find(m => m.value === meterId);
    return meter ? meter.label : '';
  });
});

// 从系列名称推断参数类型
const getParameterTypeFromName = (seriesName: string): string => {
  const name = seriesName.toLowerCase();
  console.log('hht='+name);
  // 优先匹配更具体的关键词
  if (name.includes('有功电量') || name.includes('无功电量') || name.includes('电量')) {
    return 'powerCount';
  } else if (name.includes('有功功率') || name.includes('无功功率') || name.includes('功率') ) {
    return 'power';
  } else if (name.includes('功率因数') || name.includes('factor')) {
    return 'powerFactor';
  } else if (name.includes('频率') || name.includes('frequency')) {
    return 'frequency';
  } else if (name.includes('电压') || name.includes('voltage')) {
    return 'voltage';
  } else if (name.includes('电流') || name.includes('current')) {
    return 'current';
  } else if (name.includes('瞬时流量') || name.includes('instant')) {
    return 'instantFlow';
  } else if (name.includes('累计流量') || name.includes('total')) {
    return 'totalFlow';
  } else if (name.includes('温度') || name.includes('temperature')) {
    return 'temperature';
  } else if (name.includes('压力') || name.includes('pressure')) {
    return 'pressure';
  } else if (name.includes('密度') || name.includes('density')) {
    return 'density';
  }
  
  return 'current'; // 默认类型
};

// 获取参数类型对应的颜色
const getParamTypeColor = (paramType: string): string => {
  const colorMap = {
    current: '#1890ff',
    voltage: '#722ed1',
    power: '#13c2c2',
    powerCount: '#531dab',
    powerFactor: '#f759ab',
    frequency: '#fa541c',
    instantFlow: '#096dd9',
    totalFlow: '#389e0d',
    temperature: '#d4380d',
    pressure: '#7cb305',
    density: '#531dab'
  };
  return colorMap[paramType] || '#666';
};

// 格式化Y轴标签值
const getFormattedValue = (value: number, paramType: string): string => {
  switch (paramType) {
    case 'current':
      return `${value}A`;
    case 'voltage':
      return `${value}V`;
    case 'power':
      return `${value}kW`;
    case 'powerCount':
      return `${value}kWH`;
    case 'powerFactor':
      return value.toFixed(2);
    case 'frequency':
      return `${value}Hz`;
    case 'instantFlow':
      return `${value}m³/h`;
    case 'totalFlow':
      return `${value}m³`;
    case 'temperature':
      return `${value}°C`;
    case 'pressure':
      return `${value}MPa`;
    case 'density':
      return `${value}kg/m³`;
    default:
      return String(value);
  }
};

// 为特定参数类型创建Y轴配置
const createYAxisForParamType = (paramType: string, typeIndex: number, totalTypes: number): any => {
  const baseConfig = {
    type: 'value',
    position: typeIndex % 2 === 0 ? 'left' : 'right',
    axisLine: {
      show: true,
      lineStyle: { color: getParamTypeColor(paramType) }
    },
    axisLabel: {
      color: getParamTypeColor(paramType),
      formatter: (value: number) => getFormattedValue(value, paramType)
    },
    splitLine: {
      show: typeIndex === 0, // 只有第一个Y轴显示网格线
      lineStyle: {
        color: '#f0f0f0',
        type: 'dashed'
      }
    }
  };

  // 根据参数类型设置特定配置
  const getYAxisConfig = () => {
    switch (paramType) {
      case 'current':
        return {
          ...baseConfig,
          name: '电流 (A)',
          min: 0,
          max: (value: any) => Math.ceil(value.max * 1.1)
        };
      
      case 'voltage':
        return {
          ...baseConfig,
          name: '电压 (V)',
          min: 200,
          max: 250
        };
      
      case 'power':
        return {
          ...baseConfig,
          name: '功率 (kW)',
          min: 0
        };

      case 'powerCount':
        return {
          ...baseConfig,
          name: '电量 (kWH)',
          min: 0
        };
      
      case 'powerFactor':
        return {
          ...baseConfig,
          name: '功率因数',
          min: 0.8,
          max: 1.0
        };
      
      case 'frequency':
        return {
          ...baseConfig,
          name: '频率 (Hz)',
          min: 49,
          max: 51
        };
      
      case 'instantFlow':
        return {
          ...baseConfig,
          name: '瞬时流量 (m³/h)',
          min: 0
        };
      
      case 'totalFlow':
        return {
          ...baseConfig,
          name: '累计流量 (m³)',
          min: 0
        };
      
      case 'temperature':
        return {
          ...baseConfig,
          name: '温度 (°C)',
          min: 0,
          max: 100
        };
      
      case 'pressure':
        return {
          ...baseConfig,
          name: '压力 (MPa)',
          min: 0
        };
      
      case 'density':
        return {
          ...baseConfig,
          name: '密度 (kg/m³)',
          min: 0
        };
      
      default:
        return baseConfig;
    }
  };

  const yAxis = getYAxisConfig();
  
  // 如果有多个Y轴，需要设置偏移避免重叠
  if (totalTypes > 2) {
    const offset = Math.floor(typeIndex / 2) * 60; // 每两个轴偏移60像素
    yAxis.offset = offset;
  }

  return yAxis;
};

// 根据参数类型对数据系列进行分组和Y轴映射
const groupSeriesByParamType = (series: any[]) => {
  const paramTypeGroups: Record<string, any[]> = {};
  const yAxisConfig: any[] = [];
  const colorsByType = {
    current: ['#1890ff', '#52c41a', '#faad14'],      // 电流类 - 蓝绿橙
    voltage: ['#722ed1', '#eb2f96', '#fa8c16'],      // 电压类 - 紫红橙
    power: ['#13c2c2', '#52c41a', '#faad14'],        // 功率类 - 青绿橙
    powerCount: ['#096dd9', '#531dab'],            // 电 电量类
    powerFactor: ['#f759ab'],                        // 功率因数 - 粉
    frequency: ['#fa541c'],                          // 频率 - 红橙
    instantFlow: ['#096dd9'],                        // 瞬时流量 - 深蓝
    totalFlow: ['#389e0d'],                          // 累计流量 - 深绿
    temperature: ['#d4380d'],                        // 温度 - 深红
    pressure: ['#7cb305'],                           // 压力 - 深绿
    density: ['#531dab']                             // 密度 - 深紫
  };

  // 按参数类型分组
  series.forEach((seriesItem, index) => {
    const paramType = seriesItem.paramType || getParameterTypeFromName(seriesItem.name);
    
    if (!paramTypeGroups[paramType]) {
      paramTypeGroups[paramType] = [];
    }
    
    paramTypeGroups[paramType].push({
      ...seriesItem,
      paramType
    });
  });

  console.log('📊 参数类型分组结果:', paramTypeGroups);

  // 为每个参数类型创建Y轴配置
  const paramTypeKeys = Object.keys(paramTypeGroups);
  const processedSeries: any[] = [];

  paramTypeKeys.forEach((paramType, typeIndex) => {
    const group = paramTypeGroups[paramType];
    const colors = colorsByType[paramType] || ['#666'];
    
    // 创建Y轴配置
    const yAxisConfigItem = createYAxisForParamType(paramType, typeIndex, paramTypeKeys.length);
    yAxisConfig.push(yAxisConfigItem);

    // 处理该组的系列数据
    group.forEach((seriesItem, seriesIndex) => {
      processedSeries.push({
        ...seriesItem,
        yAxisIndex: typeIndex, // 使用参数类型索引作为Y轴索引
        itemStyle: {
          color: colors[seriesIndex % colors.length]
        }
      });
    });
  });

  return {
    series: processedSeries,
    yAxisConfig: yAxisConfig
  };
};

// 获取字典数据
function loadDimensionDictData() {
  console.log('🔄 开始加载维度字典数据...');
  defHttp.get({
    url: '/sys/dict/getDictItems/dimensionCode'
  })
  .then((res) => {
    if (res && Array.isArray(res)) {
      console.log('📋 维度字典原始数据:', res);

      // 将字典数据转换为维度列表
      dimensionList.value = res.map((item, index) => {
        return {
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(item.value), // 使用字典中的value作为nowtype值
          value: Number(item.value)
        };
      });

      console.log('🏷️ 转换后的维度列表:', dimensionList.value);

      // 默认选中第一个标签页
      if (dimensionList.value.length > 0) {
        activeTabKey.value = dimensionList.value[0].key;
        currentNowtype.value = dimensionList.value[0].nowtype;
        console.log('🎯 默认选中维度类型:', currentNowtype.value);
      }
    } else {
      console.log('⚠️ 维度字典数据为空或格式不正确，使用默认配置');
      // 如果获取字典失败，使用默认维度列表
      // 根据实际测试，第一个标签页应该使用nowtype=1才有数据
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 }, // 使用1（有数据）
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 }, // 使用2（可能没数据）
        { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
        { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
        { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
      ];
      activeTabKey.value = 'info1';
      currentNowtype.value = 1; // 默认选中nowtype=1（有数据）
    }
  })
  .catch((error) => {
    console.error('❌ 获取维度字典失败:', error);
    // 如果API调用失败，使用默认维度列表
    dimensionList.value = [
      { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 }, // 使用1（有数据）
      { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 }, // 使用2（可能没数据）
      { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
      { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
      { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
    ];
    activeTabKey.value = 'info1';
    currentNowtype.value = 1; // 默认选中nowtype=1（有数据）
  });
}

// 根据能源类型获取参数字典编码
function getParameterDictCode() {
  // 根据当前选中的能源类型(nowtype)确定字典编码
  switch (currentNowtype.value) {
    case 1: // 按部门（用电）
    case 2: // 按线路（用电）
      return 'parameter';
    case 3: // 天然气
      return 'parameter_energy';
    case 4: // 压缩空气
      return 'parameter_energy';
    case 5: // 企业用水
      return 'parameter_energy';
    default:
      return 'parameter'; // 默认为电能参数
  }
}

// 根据能源类型获取查询间隔字典编码
function getQueryIntervalDictCode() {
  // 所有能源类型都使用统一的查询间隔字典
  return 'queryInterval';
}

// 加载参数选择字典数据
async function loadParameterDictData() {
  try {
    const dictCode = getParameterDictCode();
    console.log(`Loading parameter dict with code: ${dictCode} for energy type: ${currentNowtype.value}`);

    const res = await initDictOptions(dictCode);
    if (res && Array.isArray(res) && res.length > 0) {
      parameterOptions.value = res;
      // 清空之前的选择，设置默认选中第一个参数
      selectedParams.value = [res[0].value];
    } else {
      // 如果字典为空或不存在，使用默认数据
      throw new Error('字典数据为空');
    }
  } catch (error) {
    console.error('加载参数字典失败:', error);
    // 根据能源类型使用不同的默认数据
    getDefaultParameterOptions();
  }
}

// 获取默认参数选项（根据能源类型）
function getDefaultParameterOptions() {
  const dictCode = getParameterDictCode();
  const energyTypeName = getEnergyTypeName(currentNowtype.value);

  console.warn(`⚠️ 字典数据加载失败，请检查字典配置: ${dictCode}`);
  console.warn(`⚠️ 当前能源类型: ${energyTypeName} (nowtype=${currentNowtype.value})`);

  // 清空参数选项，避免使用可能不准确的写死数据
  parameterOptions.value = [];
  selectedParams.value = [];

  // 提示用户检查字典配置
  createMessage.error(`参数字典 "${dictCode}" 加载失败，请联系管理员检查字典配置`);
}

// 获取能源类型名称
function getEnergyTypeName(nowtype: number): string {
  switch (nowtype) {
    case 1: return '按部门（用电）';
    case 2: return '按线路（用电）';
    case 3: return '天然气';
    case 4: return '压缩空气';
    case 5: return '企业用水';
    default: return '未知类型';
  }
}

// 根据参数值获取参数类型（用于多Y轴显示）
function getParameterType(paramValue: string | number): string {
  const paramStr = String(paramValue);
  console.log('🔍 参数类型判断 - 输入参数:', paramStr);

  // 根据当前能源类型确定参数映射
  switch (currentNowtype.value) {
    case 1: // 按部门（用电）
    case 2: // 按线路（用电）
      // 首先尝试匹配parameterCode（如IA, IB, IC等）
      switch (paramStr.toUpperCase()) {
        case 'IA': case 'IB': case 'IC': return 'current'; // A、B、C相电流
        case 'UA': case 'UB': case 'UC': return 'voltage'; // A、B、C相电压
        case 'PA': case 'PB': case 'PC': return 'power'; // A、B、C相功率
        case 'PFS': case 'PFSA': case 'PFSB': case 'PFSC': return 'powerFactor'; // 功率因数
        case 'HZ':  return 'frequency'; // 频率
        case 'PP': case 'Q': return 'power'; // 有功功率、无功功率
        case 'EP': case 'EQ': return 'power'; // 有功电量、无功电量
        case 'KWH': case 'KVARH': return 'powerCount'; // 有功电量、无功电量（千瓦时）
        case 'W': case 'KW': case 'MW': return 'power'; // 功率单位
        case 'VAR': case 'KVAR': case 'MVAR': return 'power'; // 无功功率单位
        
      }
    case 3: // 天然气
      switch (paramStr.toUpperCase()) {
        case 'PV': return 'instantFlow'; // 瞬时流量
        case 'SV': return 'totalFlow'; // 累计流量
        case 'TEMP': return 'temperature'; // 温度
        case 'PRE': return 'pressure'; // 压力
        default: return 'instantFlow';
      }
    case 4: // 压缩空气
    case 5: // 企业用水
      switch (paramStr.toUpperCase()) {
        case 'PV': return 'instantFlow'; // 瞬时流量
        case 'SV': return 'totalFlow'; // 累计流量
        case 'TEMP': return 'temperature'; // 温度
        case 'PRE': return 'pressure'; // 压力
        default: return 'instantFlow';
      }
    default:
      return 'current';
  }
}

// 加载查询间隔字典数据
async function loadQueryIntervalDictData() {
  try {
    const dictCode = getQueryIntervalDictCode();
    console.log(`Loading query interval dict with code: ${dictCode} for energy type: ${currentNowtype.value}`);

    const res = await initDictOptions(dictCode);
    if (res && Array.isArray(res) && res.length > 0) {
      queryIntervalOptions.value = res;
      // 根据能源类型设置不同的默认值
      if (!queryInterval.value) {
        const defaultValue = getDefaultQueryInterval();
        queryInterval.value = defaultValue;
        console.log(`设置默认查询间隔: ${defaultValue} for energy type: ${currentNowtype.value}`);
      }
    } else {
      throw new Error('字典数据为空');
    }
  } catch (error) {
    console.error('加载查询间隔字典失败:', error);
    // 根据能源类型使用不同的默认数据
    getDefaultQueryIntervalOptions();
  }
}

// 根据能源类型获取默认查询间隔
function getDefaultQueryInterval() {
  // 所有能源类型都默认使用15分钟
  return '1'; // 15分钟
}

// 根据能源类型获取默认查询间隔选项
function getDefaultQueryIntervalOptions() {
  // 使用默认数据，使用数字值与字典保持一致
  queryIntervalOptions.value = [
    { text: '5分钟', value: '1' },
    { text: '15分钟', value: '2' },
    { text: '30分钟', value: '3' },
    { text: '60分钟', value: '4' },
    { text: '120分钟', value: '5' }
  ];

  const defaultValue = getDefaultQueryInterval();
  queryInterval.value = defaultValue;
  console.log(`使用默认查询间隔选项，设置默认值: ${defaultValue} for energy type: ${currentNowtype.value}`);
}

// 加载查询方式字典数据
async function loadQueryMethodDictData() {
  try {
    const res = await initDictOptions('queryMethod');
    if (res && Array.isArray(res) && res.length > 0) {
      queryMethodOptions.value = res;
      // 设置默认选中第一个方式
      if (!displayMode.value) {
        displayMode.value = res[0].value;
      }
    } else {
      throw new Error('字典数据为空');
    }
  } catch (error) {
    console.error('加载查询方式字典失败:', error);
    // 使用默认数据，使用数字值与字典保持一致
    queryMethodOptions.value = [
      { text: '统一显示', value: '1' },
      { text: '分开显示', value: '2' }
    ];
    displayMode.value = '1'; // 默认选中统一显示
  }
}

// 处理标签页切换
function handleTabChange(key) {
  activeTabKey.value = key;

  // 根据选中的标签页设置当前能源类型
  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    const oldNowtype = currentNowtype.value;
    currentNowtype.value = selectedDimension.nowtype;

    // 如果能源类型发生变化，重新加载参数字典和查询间隔字典
    if (oldNowtype !== selectedDimension.nowtype) {
      console.log(`能源类型从 ${oldNowtype} 切换为 ${selectedDimension.nowtype}`);
      loadParameterDictData();
      loadQueryIntervalDictData(); // 🔥 重新加载查询间隔字典
      // 清空图表数据
      unifiedChartData.value = { categories: [], series: [] };
      separateChartsData.value = [];
    }
  }

  // 如果该标签页之前已经选择过节点，则使用保存的节点信息
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    currentOrgCode.value = savedNode.orgCode;
    // 重新加载该标签页对应的仪表列表
    if (savedNode.data) {
      const orgCodes = Array.isArray(savedNode.data)
        ? savedNode.data.map(item => item.orgCode)
        : [savedNode.data.orgCode];
      loadModulesByOrgCodes(orgCodes);
    }
  } else {
    // 清空仪表列表
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
  }

  // 等待树组件加载完成后，如果没有选中的节点，则触发树组件的默认选择
  nextTick(() => {
    const currentTreeRef = treeRefs.value[key];
    if (currentTreeRef && !savedNode) {
      // 手动触发树组件的默认选择
      console.log('手动触发树组件的默认选择...');
      // 等待一段时间确保树组件完全加载
      setTimeout(() => {
        if (currentTreeRef.autoExpandToTargetLevelNode) {
          currentTreeRef.autoExpandToTargetLevelNode(2);
        }
      }, 100);
    }
  });
}

// 获取当前激活标签页对应的树组件引用
function getCurrentTreeRef() {
  return treeRefs.value[activeTabKey.value];
}

// 根据维度编码获取仪表列表
async function loadModulesByOrgCodes(orgCodes: string[]) {
  console.log('loadModulesByOrgCodes called with:', orgCodes);

  if (!orgCodes || orgCodes.length === 0) {
    console.log('No orgCodes provided, clearing meters');
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
    return;
  }

  try {
    loading.value = true;

    // 将多个维度编码合并为逗号分隔的字符串
    const orgCodesStr = orgCodes.join(',');
    console.log('API request params:', {
      orgCodes: orgCodesStr,
      nowtype: String(currentNowtype.value || 1),
      includeChildren: true
    });

    try {
      const response = await getModulesByOrgCode({
        orgCodes: orgCodesStr,  // 使用orgCodes参数名（复数）
        nowtype: String(currentNowtype.value || 1),  // 传递维度类型，确保是字符串
        includeChildren: true
      });

      console.log('API response:', response);
      console.log('Response type:', typeof response);
      console.log('Is array:', Array.isArray(response));
      console.log('Response.success:', response?.success);
      console.log('Response.result:', response?.result);

      // 检查响应是否直接是数组（没有包装在success/result结构中）
      if (Array.isArray(response)) {
        allModules.value = response;
        console.log('Successfully loaded modules (direct array):', response.length);
        if (response.length === 0) {
          console.warn(`⚠️ 维度类型 ${currentNowtype.value} 下的维度编码 ${orgCodesStr} 没有找到仪表数据`);
          console.warn('可能的原因：1. 该维度下确实没有仪表 2. 维度编码不正确 3. 后端数据问题');
        }
      } else if (response && response.success && Array.isArray(response.result)) {
        allModules.value = response.result;
        console.log('Successfully loaded modules (wrapped):', response.result.length);
      } else {
        console.warn('获取仪表列表失败:', response?.message);
        console.warn('Unexpected response structure:', response);
        allModules.value = [];
      }
    } catch (error) {
      console.error(`获取维度 ${orgCodesStr} 的仪表列表失败:`, error);
      allModules.value = [];
    }

    // 转换为下拉框选项格式
    meters.value = allModules.value.map(module => ({
      label: module.moduleName,
      value: module.moduleId
    }));

    // 默认选择所有仪表（如果有的话）
    if (meters.value.length > 0) {
      selectedMeters.value = meters.value.map(m => m.value);
    } else {
      selectedMeters.value = [];
    }

    console.log(`加载了 ${allModules.value.length} 个仪表，默认选中 ${selectedMeters.value.length} 个`);
    console.log('仪表详情:', meters.value);

    // 如果没有仪表数据，显示友好提示
    if (allModules.value.length === 0) {
      console.log('💡 提示：当前维度下暂无仪表数据，请尝试切换其他维度或联系管理员配置仪表');
    } else {
      // 🔥 自动触发查询 - 仪表加载完成后自动查询数据
      console.log('🚀 仪表加载完成，自动触发查询...');
      await nextTick(); // 确保DOM更新完成
      handleQuery();
    }

    // 🔄 加载实时数据
    loadRealTimeData();

  } catch (error) {
    console.error('获取仪表列表失败:', error);
    createMessage.error('获取仪表列表失败');
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
  } finally {
    loading.value = false;
  }
}

// 左侧树选择后触发 - 支持多选
function onDepartTreeSelect(data) {
  console.log('onDepartTreeSelect received:', data);
  console.log('Current nowtype:', currentNowtype.value);
  console.log('Current tab:', activeTabKey.value);

  if (Array.isArray(data) && data.length > 0) {
    // 直接使用所有选中的节点，不过滤
    // 因为树组件已经处理了父子关系，选中的都是有效节点
    const orgCodes = data.map(item => item.orgCode).filter(code => code); // 过滤掉空值
    const orgCodestr = orgCodes.join(',');
    currentOrgCode.value = orgCodestr;

    console.log('Selected nodes count:', data.length);
    console.log('Selected orgCodes:', orgCodestr);
    console.log('Selected nodes details:', data.map(item => ({
      orgCode: item.orgCode,
      departName: item.departName,
      id: item.id
    })));

    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: orgCodestr,
      data: data
    };

    // 根据选中的维度获取仪表列表
    console.log('Calling loadModulesByOrgCodes with:', orgCodes);
    loadModulesByOrgCodes(orgCodes);

  } else if (data && data.orgCode) {
    // 处理单个对象的情况
    currentOrgCode.value = data.orgCode;

    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: data.orgCode,
      data: data
    };

    // 根据选中的维度获取仪表列表
    loadModulesByOrgCodes([data.orgCode]);

  } else {
    console.log("没有选中任何项目");
    // 清空仪表列表
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
  }
}

// 实时数据
interface RealTimeData {
  // 电能数据
  activePower: number;
  totalActivePower: number;
  
  // 电流和温度数据
  powerFactor: number;
  targetPowerFactor: number;
  frequency: number;
  currentA: number;
  currentB: number;
  currentC: number;
  temperatureA: number;
  temperatureB: number;
  temperatureC: number;
}

// 实时数据 - 初始化为空，从API获取
const realTimeData = ref<RealTimeData>({
  // 电能数据
  activePower: 0,
  totalActivePower: 0,

  // 电流和温度数据
  powerFactor: 0,
  targetPowerFactor: 0,
  frequency: 0,
  currentA: 0,
  currentB: 0,
  currentC: 0,
  temperatureA: 0,
  temperatureB: 0,
  temperatureC: 0
});

// 加载实时数据
async function loadRealTimeData() {
  if (!currentOrgCode.value || !currentNowtype.value) {
    console.log('缺少必要参数，跳过实时数据加载');
    return;
  }

  try {
    console.log('🔄 开始加载实时数据...', {
      orgCode: currentOrgCode.value,
      nowtype: currentNowtype.value
    });

    const response = await getRealTimeData({
      orgCode: currentOrgCode.value,
      nowtype: currentNowtype.value
    });

    console.log('📊 实时数据API响应:', response);

    if (response && response.success && response.result) {
      const data = response.result;

      // 根据能源类型处理不同的数据结构
      if (currentNowtype.value === 1 || currentNowtype.value === 2) {
        // 电力数据
        realTimeData.value = {
          activePower: data.pp || 0,
          totalActivePower: data.KWH || 0,
          powerFactor: data.PFS || 0,
          targetPowerFactor: 0.98, // 目标功率因数通常是固定值
          frequency: data.HZ || 0,
          currentA: data.IA || 0,
          currentB: data.IB || 0,
          currentC: data.IC || 0,
          temperatureA: data.temperatureA || 0,
          temperatureB: data.temperatureB || 0,
          temperatureC: data.temperatureC || 0
        };
      } else {
        // 其他能源类型的数据处理
        realTimeData.value = {
          activePower: data.instantFlow || 0,
          totalActivePower: data.totalFlow || 0,
          powerFactor: 0,
          targetPowerFactor: 0,
          frequency: 0,
          currentA: data.pressure || 0,
          currentB: data.temperature || 0,
          currentC: 0,
          temperatureA: data.temperature || 0,
          temperatureB: 0,
          temperatureC: 0
        };
      }

      console.log('✅ 实时数据更新成功:', realTimeData.value);
    } else {
      console.warn('⚠️ 实时数据API返回格式异常:', response);
    }
  } catch (error) {
    console.error('❌ 加载实时数据失败:', error);
  }
}

// 图表数据接口定义
interface ChartDataSeries {
  name: string;
  data: number[];
  paramType?: string;
  yAxisIndex?: number;
  itemStyle?: {
    color: string;
  };
}

interface ChartData {
  categories: string[];
  series: ChartDataSeries[];
  yAxisConfig?: any[];
  enableMultiYAxis?: boolean;
}

// 定时更新数据
let timer: number | null = null;

// 根据参数值获取参数标签
const getParamLabel = (paramValue: string | number): string => {
  const param = parameterOptions.value.find(p => p.value === String(paramValue));
  return param ? param.text : String(paramValue);
};

// 获取指定仪表的标签
const getMeterLabel = (meterId: string): string => {
  const meter = meters.value.find(m => m.value === meterId);
  return meter ? meter.label : '';
};

// 根据查询间隔生成时间分类
const generateTimeCategories = (): string[] => {
  // 获取查询间隔对应的分钟数
  const getIntervalMinutes = (intervalValue: string): number => {
    switch (intervalValue) {
      case '1': return 15;  // 15分钟
      case '2': return 30;  // 30分钟
      case '3': return 60;  // 60分钟
      case '4': return 120; // 120分钟
      default: return 15;   // 默认15分钟
    }
  };

  const intervalMinutes = getIntervalMinutes(queryInterval.value);
  const categories: string[] = [];

  // 从00:00开始，按间隔生成时间点，直到24:00
  for (let minutes = 0; minutes < 24 * 60; minutes += intervalMinutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const timeStr = `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`;
    categories.push(timeStr);
  }

  console.log(`生成时间轴 (间隔${intervalMinutes}分钟):`, categories);
  return categories;
};

// 检查是否有图表数据
const hasChartData = (): boolean => {

  // 统一显示模式：检查是否有API数据
  if (displayMode.value === 'unified' || displayMode.value === '1') {
    return unifiedChartData.value.categories.length > 0 && unifiedChartData.value.series.length > 0;
  }

  // 分开显示模式：检查是否有分开显示的数据
  console.log('检查是否有分开显示的数据:', separateChartsData.value.length);
  return separateChartsData.value.length > 0;
};

// 获取统一显示的图表数据
const getUnifiedChartData = (): ChartData => {
  // 如果有API数据，优先使用API数据
  if (unifiedChartData.value.categories.length > 0) {
    return {
      ...unifiedChartData.value,
      enableMultiYAxis: true, // 确保启用多Y轴
      yAxisConfig: unifiedChartData.value.yAxisConfig // 传递Y轴配置
    };
  }

  // 如果没有API数据，返回空数据（不显示模拟数据）
  return {
    categories: [],
    series: [],
    enableMultiYAxis: false
  };
};

// 处理查询按钮点击 - 添加防抖
const handleQuery = () => {
  if (queryDebounceTimer) {
    clearTimeout(queryDebounceTimer);
  }

  queryDebounceTimer = window.setTimeout(async () => {
    await executeQuery();
  }, 300); // 300ms防抖
};

// 处理导出Excel
const handleExportExcel = async () => {
  // 验证查询条件
  const validationResult = validateQueryParams();
  if (!validationResult.isValid) {
    createMessage.warning(validationResult.message);
    return;
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    createMessage.warning('请选择时间范围');
    return;
  }

  // 获取选中的仪表名称用于显示
  const selectedMeterNames = selectedMeters.value.map(meterId => {
    const meter = meters.value.find(m => m.value === meterId);
    return meter ? meter.label : meterId;
  });

  // 获取选中的参数名称用于显示
  const selectedParamNames = selectedParams.value.map(paramId => {
    const param = parameterOptions.value.find(p => p.value === paramId);
    return param ? param.label : paramId;
  });

  // 显示导出确认信息
  console.log(`📊 准备导出数据:`);
  console.log(`   仪表: ${selectedMeterNames.join(', ')}`);
  console.log(`   参数: ${selectedParamNames.join(', ')}`);
  console.log(`   时间: ${dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm')} 至 ${dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm')}`);

  try {
    exportLoading.value = true;

    // 显示开始导出的提示
    createMessage.loading('正在准备导出数据，请稍候...', 2);

    // 构建导出参数（与查询接口保持一致）
    const parameters = selectedParams.value.map(p => {
      const numParam = Number(p);
      if (isNaN(numParam)) {
        console.warn(`⚠️ 导出参数值 "${p}" 无法转换为数字，请检查字典配置`);
        return 0; // 使用0作为默认值
      }
      return numParam;
    });

    console.log('🔍 导出参数转换详情:', {
      原始参数: selectedParams.value,
      转换后参数: parameters,
      参数选项: parameterOptions.value
    });

    const exportData = {
      moduleIds: selectedMeters.value,
      parameters: parameters,
      startTime: dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
      interval: Number(queryInterval.value),
      displayMode: Number(displayMode.value),
      fileName: `实时数据导出_${dayjs().format('YYYYMMDD_HHmmss')}`
    };

    console.log('📊 导出Excel完整参数:', exportData);
    console.log(`📊 导出范围: ${selectedMeters.value.length}个仪表, ${parameters.length}个参数`);
    console.log(`📊 时间范围: ${exportData.startTime} 至 ${exportData.endTime}`);
    console.log(`📊 查询间隔: ${exportData.interval}, 显示模式: ${exportData.displayMode}`);
    console.log('📊 选中的仪表:', selectedMeters.value);
    console.log('📊 选中的参数:', parameters);

    // 调用导出API
    const response = await exportRealTimeData(exportData);

    if (!response) {
      createMessage.error('导出失败：未收到响应数据');
      return;
    }

    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${exportData.fileName}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    createMessage.success('导出成功');
  } catch (error: any) {
    console.error('❌ 导出Excel失败:', error);

    // 根据错误类型提供更具体的错误信息
    let errorMessage = '导出失败，请稍后重试';
    if (error?.response?.status === 404) {
      errorMessage = '导出接口不存在，请检查后端服务';
    } else if (error?.response?.status === 500) {
      errorMessage = '服务器内部错误，请联系管理员';
    } else if (error?.code === 'NETWORK_ERROR') {
      errorMessage = '网络连接失败，请检查网络';
    } else if (error?.message) {
      errorMessage = `导出失败: ${error.message}`;
    }

    createMessage.error(errorMessage);
  } finally {
    exportLoading.value = false;
  }
};

// 验证查询参数
const validateQueryParams = () => {
  if (!selectedMeters.value || selectedMeters.value.length === 0) {
    return { isValid: false, message: '请选择至少一个仪表' };
  }

  if (!selectedParams.value || selectedParams.value.length === 0) {
    return { isValid: false, message: '请选择至少一个参数' };
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    return { isValid: false, message: '请选择时间范围' };
  }

  if (!queryInterval.value) {
    return { isValid: false, message: '请选择查询间隔' };
  }

  if (!displayMode.value) {
    return { isValid: false, message: '请选择查询方式' };
  }

  return { isValid: true, message: '' };
};

// 执行实际查询
const executeQuery = async () => {
  console.log('查询条件:', {
    dateRange: dateRange.value,
    selectedMeters: selectedMeters.value,
    selectedParams: selectedParams.value,
    queryInterval: queryInterval.value,
    displayMode: displayMode.value,
    currentOrgCode: currentOrgCode.value,
    currentNowtype: currentNowtype.value
  });

  // 验证查询条件
  const validationResult = validateQueryParams();
  if (!validationResult.isValid) {
    createMessage.warning(validationResult.message);
    return;
  }

  try {
    loading.value = true;

    // 清空当前图表数据，避免显示旧数据
    unifiedChartData.value = {
      categories: [],
      series: [],
      yAxisConfig: []
    };
    separateChartsData.value = [];

    // 构建请求参数
    const parameters = selectedParams.value.map(p => {
      const numParam = Number(p);
      if (isNaN(numParam)) {
        console.warn(`⚠️ 参数值 "${p}" 无法转换为数字，请检查字典配置`);
        return 0; // 使用0作为默认值
      }
      return numParam;
    });

    console.log('🔍 参数转换详情:', {
      原始参数: selectedParams.value,
      转换后参数: parameters,
      参数选项: parameterOptions.value
    });

    const requestData: RealTimeMonitorRequest = {
      moduleIds: selectedMeters.value,
      parameters: parameters,
      startTime: dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
      interval: Number(queryInterval.value),
      displayMode: Number(displayMode.value)
    };

    console.log('API请求参数:', requestData);

    // 调用API获取数据
    console.log('🚀 开始调用API...');
    const response = await getRealTimeMonitorData(requestData);
    console.log('📡 API原始响应:', response);
    console.log('📡 响应类型:', typeof response);
    console.log('📡 响应成功标志:', response?.success);

    // 检查响应格式：可能是包装格式或直接数据格式
    if (response && response.success) {
      // 标准包装格式：{success: true, result: {...}}
      console.log('✅ API响应数据(包装格式):', response.result);
      updateChartDataFromAPI(response.result);
      createMessage.success('数据查询成功');
    } else if (response && response.series && Array.isArray(response.series)) {
      // 统一显示格式：{displayMode: 'unified', series: [...]}
      console.log('✅ API响应数据(统一显示格式):', response);
      console.log('✅ 数据系列数量:', response.series.length);
      console.log('✅ 显示模式:', response.displayMode);

      if (response.series.length === 0) {
        console.warn('⚠️ API返回成功但数据为空，可能原因：');
        console.warn('1. 数据库中没有该能源类型的监控数据');
        console.warn('2. 选择的时间范围内没有数据');
        console.warn('3. 仪表配置或数据采集问题');
        createMessage.warning('当前条件下暂无监控数据，请检查数据采集配置或选择其他时间范围。');
      } else {
        createMessage.success('数据查询成功');
      }

      updateChartDataFromAPI(response);
    } else if (response && response.charts && Array.isArray(response.charts)) {
      // 分开显示格式：{displayMode: 'separated', charts: [...]}
      console.log('✅ API响应数据(分开显示格式):', response);
      console.log('✅ 图表数量:', response.charts.length);
      console.log('✅ 显示模式:', response.displayMode);

      if (response.charts.length === 0) {
        console.warn('⚠️ API返回成功但数据为空，可能原因：');
        console.warn('1. 数据库中没有该能源类型的监控数据');
        console.warn('2. 选择的时间范围内没有数据');
        console.warn('3. 仪表配置或数据采集问题');
        createMessage.warning('当前条件下暂无监控数据，请检查数据采集配置或选择其他时间范围。');
      } else {
        createMessage.success('数据查询成功');
      }

      updateChartDataFromAPI(response);
    } else {
      console.error('❌ API调用失败或数据格式错误');
      console.error('❌ 响应消息:', response?.message);
      console.error('❌ 完整响应:', response);
      createMessage.error(response?.message || '数据查询失败');
    }

  } catch (error) {
    console.error('查询数据失败:', error);
    console.error('错误详情:', {
      message: error?.message,
      stack: error?.stack,
      response: error?.response
    });

    // 根据错误类型提供更具体的错误信息
    let errorMessage = '查询数据失败';
    if (error?.response?.status === 404) {
      errorMessage = '接口不存在，请检查后端服务';
    } else if (error?.response?.status === 500) {
      errorMessage = '服务器内部错误，请联系管理员';
    } else if (error?.code === 'NETWORK_ERROR') {
      errorMessage = '网络连接失败，请检查网络';
    } else if (error?.message) {
      errorMessage = `查询失败: ${error.message}`;
    }

    createMessage.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

// 处理API响应数据，更新图表
const updateChartDataFromAPI = (apiData: any) => {
  console.log('更新图表数据:', apiData);

  if (!apiData) {
    console.warn('API数据为空');
    return;
  }

  // 保存原始API数据，用于显示模式切换
  originalApiData.value = apiData;

  // 同时处理两种显示模式的数据，让界面根据当前模式选择显示
console.log('series='+JSON.stringify(apiData.series, null, 2));
  // 处理统一显示数据
  if (apiData.series && Array.isArray(apiData.series)) {
      // 转换API数据格式为图表组件需要的格式
      const categories = [];
      const seriesData = [];

      // 提取时间轴数据
      if (apiData.series.length > 0 && apiData.series[0].data) {
        categories.push(...apiData.series[0].data.map(item => item[0]));
      }

      // 转换系列数据
      apiData.series.forEach((series, index) => {
        // 从系列名称中提取参数信息，用于确定参数类型
        let paramType = 'current'; // 默认类型

        // 尝试从API数据中获取参数信息
        if (series.parameterCode !== undefined) {
          paramType = getParameterType(series.parameterCode);
        } else if (series.parameter !== undefined) {
          paramType = getParameterType(series.parameter);
        } else {
          // 如果没有参数信息，尝试从系列名称推断
          paramType = getParameterTypeFromName(series.name);
        }

        seriesData.push({
          name: series.name,
          data: series.data.map(item => item[1]),
          paramType: paramType, // 添加参数类型信息
        });

        console.log(`📊 系列数据: ${series.name}, 参数类型: ${paramType}, 参数值: ${series.parameter}, 参数代码: ${series.parameterCode}`);
      });

      // 按参数类型分组并生成多Y轴配置
      const groupedData = groupSeriesByParamType(seriesData);
      
      // 更新统一图表数据
      unifiedChartData.value = {
        categories,
        series: groupedData.series,
        yAxisConfig: groupedData.yAxisConfig // 添加Y轴配置
      };
    }

        // 处理分开显示数据
      if (apiData.charts && Array.isArray(apiData.charts)) {
          // 处理分开显示的数据
          console.log('📊 分开显示原始数据:', apiData.charts);
          apiData.charts.forEach((chart, index) => {
            console.log(`📊 图表${index}:`, chart);
            console.log(`📊 图表${index}的data属性:`, chart.data);
            console.log(`📊 图表${index}的series属性:`, chart.series);
          });

          // 分开显示：为每个仪表的每个系列创建独立图表
          const separateCharts = [];
          const colors = ['#1890ff', '#52c41a', '#faad14', '#fa8c16', '#722ed1', '#13c2c2'];
          let colorIndex = 0;

          apiData.charts.forEach((chart, chartIndex) => {
            console.log(`📊 处理图表${chartIndex}:`, chart);

            // 检查数据结构：分开显示模式使用 series 字段
            if (!chart.series || !Array.isArray(chart.series) || chart.series.length === 0) {
              console.warn(`⚠️ 图表${chartIndex}的series属性无效:`, chart.series);
              return;
            }

            // 为每个系列创建独立的图表
            chart.series.forEach((series, seriesIndex) => {
              const categories = series.data ? series.data.map(item => item[0]) : [];
              const data = series.data ? series.data.map(item => item[1]) : [];

              separateCharts.push({
                moduleId: chart.moduleId,
                moduleName: series.name || chart.moduleName || chart.title,
                parameter: chart.parameter,
                categories,
                series: [{
                  name: series.name || `${chart.parameter}-${seriesIndex}`,
                  data,
                  itemStyle: {
                    color: colors[colorIndex % colors.length]
                  }
                }]
              });

              colorIndex++;
              console.log(`📊 创建独立图表: ${series.name}`, {
                categories: categories.length,
                dataPoints: data.length
              });
            });
          });

          separateChartsData.value = separateCharts;
          console.log('📊 分开显示图表总数:', separateCharts.length);
        }
};

// 统一显示的图表数据
const unifiedChartData = ref<ChartData>({
  categories: [],
  series: []
});

// 分开显示的图表数据
const separateChartsData = ref<any[]>([]);

// 存储原始API数据，用于显示模式切换
const originalApiData = ref<any>(null);

// 更新数据的方法
const updateData = () => {
  // 模拟数据更新
  realTimeData.value = {
    ...realTimeData.value,
    currentA: Number((realTimeData.value.currentA * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    currentB: Number((realTimeData.value.currentB * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    currentC: Number((realTimeData.value.currentC * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    powerFactor: Number((realTimeData.value.powerFactor * (1 + (Math.random() - 0.5) * 0.001)).toFixed(2))
  };
};


onMounted(() => {
  // 加载维度字典数据
  loadDimensionDictData();

  // 加载下拉框字典数据
  loadParameterDictData();
  loadQueryIntervalDictData();
  loadQueryMethodDictData();

  // 等待树组件加载完成后触发默认选择
  nextTick(() => {
    const currentTreeRef = getCurrentTreeRef();
    if (currentTreeRef) {
      // 树组件会自动选择默认节点并触发select事件
      // 等待一下让树组件完全初始化，然后自动执行查询
      setTimeout(() => {
        handleQuery();
      }, 500);
    }
  });

  // 启动定时更新
  timer = window.setInterval(updateData, 5000);
});

onUnmounted(() => {
  // 清理定时器
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.h-full {
  min-height: calc(100vh - 100px);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  @apply w-1;
}

::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded;
}

/* 数据单元格样式 */
.data-cell {
  @apply p-3;
}

/* 数据值样式 */
.data-value {
  @apply text-base font-medium bg-gray-100 rounded mt-1 p-2 text-center;
}

/* 标签样式 */
.text-gray-600 {
  @apply text-sm font-normal;
}

/* 卡片基础样式 */
.bg-white {
  background-color: white;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

/* 树形菜单样式 */
:deep(.ant-tree) {
  font-size: 13px;
}

/* 按钮组样式 */
:deep(.ant-radio-group) {
  font-size: 13px;
}

/* 搜索框样式 */
:deep(.ant-input-search) {
  font-size: 13px;
}

/* 更新数据值样式 */
.bg-gray-50 {
  background-color: #f9fafb;
}

/* 圆角大小 */
.rounded-lg {
  border-radius: 0.5rem;
}

/* 自定义选择器和按钮样式 */
:deep(.custom-picker) {
  height: 36px;
}

:deep(.custom-picker .ant-picker-input) {
  height: 36px;
  display: flex;
  align-items: center;
}

:deep(.custom-select) {
  height: 36px;
}

:deep(.custom-select .ant-select-selector) {
  height: 36px !important;
  padding-top: 3px !important;
}

.custom-button {
  height: 36px;
  padding: 0 20px;
  font-size: 14px;
  border-radius: 4px;
}

/* 自定义标签页样式 */
:deep(.ant-tabs-nav) {
  @apply mb-4;
}

:deep(.ant-card-body) {
  @apply p-3;
}

/* 图表类型切换按钮样式 */
:deep(.ant-radio-group) {
  border-radius: 4px;
}

:deep(.ant-radio-button-wrapper) {
  height: 32px;
  line-height: 30px;
  font-size: 13px;
}
</style>