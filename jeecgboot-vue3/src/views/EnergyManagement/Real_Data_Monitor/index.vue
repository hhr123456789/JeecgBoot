<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs defaultActiveKey="info1" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
            <a-card :bordered="false" style="height: 100%">
              <DimensionTree 
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

          <!-- 查询按钮 -->
          <a-button type="primary" class="custom-button" @click="handleQuery">查询</a-button>
        </div>
      </div>

      <!-- 统一显示模式 - 所有数据在一个图表中显示 -->
      <div v-if="displayMode === 'unified' || displayMode === '1'" class="bg-white rounded p-3 mb-4">
        <div class="flex justify-between items-center mb-3">
          <div class="text-sm">所有数据统一显示</div>
          <a-button type="primary" size="small">导出数据</a-button>
        </div>
        <MonitorChart
          :chartData="getUnifiedChartData()"
          chartId="unified-chart"
          :activeIndex="activeIndex"
          :chartType="chartType"
          @mouseOnIndex="handleMouseOnIndex"
          @mouseOut="handleMouseOut"
        />
      </div>

      <!-- 分开显示模式 - 为每个参数和每个仪表生成独立图表 -->
      <template v-else>
        <div v-for="(param, paramIndex) in selectedParams" :key="`param-${param}`">
          <div v-for="(meterId, meterIndex) in selectedMeters" :key="`${param}-${meterId}`" class="bg-white rounded p-3 mb-4">
            <div class="flex justify-between items-center mb-3">
              <div class="text-sm">{{ getMeterLabel(meterId) }} - {{ getParamLabel(param) }}</div>
              <a-button type="primary" size="small">导出数据</a-button>
            </div>
            <MonitorChart
              :chartData="getChartDataForMeterAndParam(meterId, param)"
              :chartId="getChartId(param, meterId)"
              :activeIndex="activeIndex"
              :chartType="chartType"
              @mouseOnIndex="handleMouseOnIndex"
              @mouseOut="handleMouseOut"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import MonitorChart from './components/MonitorChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { initDictOptions } from '/@/utils/dict/index';
import dayjs from 'dayjs';

const { createMessage } = useMessage();

// 当前活动的数据点索引
const activeIndex = ref<number>(-1);

// 防抖标志
let debounceTimer: number | null = null;

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
const selectedMeters = ref(['meter1', 'meter2']);
const selectedParams = ref([]);
const queryInterval = ref(''); // 查询间隔从字典获取
const displayMode = ref(''); // 查询方式从字典获取
const chartType = ref('line'); // 图表类型：line(曲线图) 或 bar(柱状图)

// 仪表列表
const meters = ref([
  { label: '1号仪表', value: 'meter1' },
  { label: '2号仪表', value: 'meter2' },
  { label: '3号仪表', value: 'meter3' }
]);

// 获取选中的仪表名称
const selectedMeterLabels = computed(() => {
  return selectedMeters.value.map(meterId => {
    const meter = meters.value.find(m => m.value === meterId);
    return meter ? meter.label : '';
  });
});

// 获取字典数据
function loadDimensionDictData() {
  defHttp.get({
    url: '/sys/dict/getDictItems/dimensionCode'
  })
  .then((res) => {
    if (res && Array.isArray(res)) {
      // 将字典数据转换为维度列表
      dimensionList.value = res.map((item, index) => {
        return {
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(index + 1), // 使用索引+1作为nowtype值
          value: Number(index + 1)
        };
      });

      // 默认选中第一个标签页
      if (dimensionList.value.length > 0) {
        activeTabKey.value = dimensionList.value[0].key;
        currentNowtype.value = dimensionList.value[0].nowtype;
      }
    } else {
      // 如果获取字典失败，使用默认维度列表
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
        { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
        { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
        { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
      ];
    }
  })
  .catch(() => {
    // 如果API调用失败，使用默认维度列表
    dimensionList.value = [
      { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
      { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
      { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
      { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
      { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
    ];
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
      return 'parameter_gas';
    case 4: // 压缩空气
      return 'parameter_air';
    case 5: // 企业用水
      return 'parameter_water';
    default:
      return 'parameter'; // 默认为电能参数
  }
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
  switch (currentNowtype.value) {
    case 1: // 按部门（用电）
    case 2: // 按线路（用电）
      parameterOptions.value = [
        { text: 'A相电流', value: '1' },
        { text: 'B相电流', value: '2' },
        { text: 'C相电流', value: '3' },
        { text: 'A相电压', value: '4' },
        { text: 'B相电压', value: '5' },
        { text: 'C相电压', value: '6' },
        { text: '总功率因数', value: '7' },
        { text: 'A相功率因数', value: '8' },
        { text: 'B相功率因数', value: '9' },
        { text: 'C相功率因数', value: '10' }
      ];
      break;
    case 3: // 天然气
      parameterOptions.value = [
        { text: '瞬时流量', value: '1' },
        { text: '累计流量', value: '2' },
        { text: '温度', value: '3' },
        { text: '压力', value: '4' },
        { text: '密度', value: '5' }
      ];
      break;
    case 4: // 压缩空气
      parameterOptions.value = [
        { text: '瞬时流量', value: '1' },
        { text: '累计流量', value: '2' },
        { text: '压力', value: '3' },
        { text: '温度', value: '4' }
      ];
      break;
    case 5: // 企业用水
      parameterOptions.value = [
        { text: '瞬时流量', value: '1' },
        { text: '累计流量', value: '2' },
        { text: '压力', value: '3' },
        { text: '温度', value: '4' }
      ];
      break;
    default:
      parameterOptions.value = [
        { text: 'A相电流', value: '1' },
        { text: 'B相电流', value: '2' },
        { text: 'C相电流', value: '3' }
      ];
  }

  // 默认选中第一个参数
  selectedParams.value = parameterOptions.value.length > 0 ? [parameterOptions.value[0].value] : [];
}

// 加载查询间隔字典数据
async function loadQueryIntervalDictData() {
  try {
    const res = await initDictOptions('queryInterval');
    if (res && Array.isArray(res) && res.length > 0) {
      queryIntervalOptions.value = res;
      // 设置默认选中第一个间隔
      if (!queryInterval.value) {
        queryInterval.value = res[0].value;
      }
    } else {
      throw new Error('字典数据为空');
    }
  } catch (error) {
    console.error('加载查询间隔字典失败:', error);
    // 使用默认数据，使用数字值与字典保持一致
    queryIntervalOptions.value = [
      { text: '15分钟', value: '1' },
      { text: '30分钟', value: '2' },
      { text: '60分钟', value: '3' },
      { text: '120分钟', value: '4' }
    ];
    queryInterval.value = '4'; // 默认选中120分钟
  }
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

    // 如果能源类型发生变化，重新加载参数字典
    if (oldNowtype !== selectedDimension.nowtype) {
      console.log(`能源类型从 ${oldNowtype} 切换为 ${selectedDimension.nowtype}`);
      loadParameterDictData();
      // 清空图表数据
      chartData.value = [];
    }
  }

  // 如果该标签页之前已经选择过节点，则使用保存的节点信息
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    currentOrgCode.value = savedNode.orgCode;
  }

  // 等待树组件加载完成后，如果没有选中的节点，则触发树组件的默认选择
  nextTick(() => {
    const currentTreeRef = treeRefs.value[key];
    if (currentTreeRef && !savedNode) {
      // 树组件会自动选择默认节点并触发select事件
    }
  });
}

// 获取当前激活标签页对应的树组件引用
function getCurrentTreeRef() {
  return treeRefs.value[activeTabKey.value];
}

// 左侧树选择后触发
function onDepartTreeSelect(data) {
  if (Array.isArray(data) && data.length > 0) {
    const orgCodestr = data.map(item => item.orgCode).join(',');
    currentOrgCode.value = orgCodestr;
    
    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: orgCodestr,
      data: data
    };
    
  } else if (data && data.orgCode) {
    // 处理单个对象的情况
    currentOrgCode.value = data.orgCode;
    
    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: data.orgCode,
      data: data
    };
    
  } else {
    console.log("没有选中任何项目");
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

// 实时数据
const realTimeData = ref<RealTimeData>({
  // 电能数据
  activePower: 55.54,
  totalActivePower: 80.92,
  
  // 电流和温度数据
  powerFactor: 0.95,
  targetPowerFactor: 0.98,
  frequency: 80.92,
  currentA: 53.26,
  currentB: 61.49,
  currentC: 57.48,
  temperatureA: 25.81,
  temperatureB: 11.70,
  temperatureC: 40.80
});

// 图表数据接口定义
interface ChartDataSeries {
  name: string;
  data: number[];
  itemStyle?: {
    color: string;
  };
}

interface ChartData {
  categories: string[];
  series: ChartDataSeries[];
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

// 根据仪表ID和参数获取对应的图表数据
const getChartDataForMeterAndParam = (meterId: string, param: string | number): ChartData => {
  const categories = ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
                    '14:00', '16:00', '18:00', '20:00', '22:00'];

  // 基础颜色数组
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f759ab', '#722ed1', '#13c2c2'];

  // 获取仪表索引，用于生成不同的基础数据
  const meterIndex = meters.value.findIndex(m => m.value === meterId);

  // 将参数值转换为字符串进行比较，支持数字和字符串两种格式
  const paramStr = String(param);

  // 根据能源类型和字典值映射到对应的参数类型
  let paramType = '';

  // 根据当前能源类型确定参数映射
  switch (currentNowtype.value) {
    case 1: // 按部门（用电）
    case 2: // 按线路（用电）
      switch (paramStr) {
        case '1':
        case '2':
        case '3':
        case 'current':
          paramType = 'current';
          break;
        case '4':
        case '5':
        case '6':
        case 'voltage':
          paramType = 'voltage';
          break;
        case '7':
        case '8':
        case '9':
        case '10':
        case 'powerFactor':
          paramType = 'powerFactor';
          break;
        default:
          paramType = 'current';
      }
      break;

    case 3: // 天然气
      switch (paramStr) {
        case '1':
          paramType = 'instantFlow';
          break;
        case '2':
          paramType = 'totalFlow';
          break;
        case '3':
          paramType = 'temperature';
          break;
        case '4':
          paramType = 'pressure';
          break;
        case '5':
          paramType = 'density';
          break;
        default:
          paramType = 'instantFlow';
      }
      break;

    case 4: // 压缩空气
    case 5: // 企业用水
      switch (paramStr) {
        case '1':
          paramType = 'instantFlow';
          break;
        case '2':
          paramType = 'totalFlow';
          break;
        case '3':
          paramType = 'pressure';
          break;
        case '4':
          paramType = 'temperature';
          break;
        default:
          paramType = 'instantFlow';
      }
      break;

    default:
      paramType = 'current';
  }

  switch (paramType) {
    case 'current':
      // 为当前仪表生成A、B、C三相电流数据
      const currentSeries: ChartDataSeries[] = [];

      ['A', 'B', 'C'].forEach((phase, phaseIndex) => {
        // 生成基于仪表ID和相位的随机但一致的数据
        const baseValue = 50 + (meterIndex * 5) + (phaseIndex * 3);
        const data = categories.map((_, i) => {
          // 使用仪表ID、相位和时间点生成伪随机值
          const seed = (meterIndex * 100) + (phaseIndex * 10) + i;
          const variation = Math.sin(seed * 0.1) * 5;
          return Number((baseValue + variation).toFixed(2));
        });

        currentSeries.push({
          name: `${phase}相电流`,
          data,
          itemStyle: {
            color: colors[phaseIndex % colors.length]
          }
        });
      });

      return {
        categories,
        series: currentSeries
      };

    case 'power':
      return {
        categories,
        series: [
          {
            name: '总有功功率',
            data: categories.map((_, i) => {
              const baseValue = 80 + (meterIndex * 10);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 3;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[0]
            }
          },
          {
            name: 'A相有功功率',
            data: categories.map((_, i) => {
              const baseValue = 26 + (meterIndex * 3);
              const seed = (meterIndex * 100) + i + 1;
              const variation = Math.sin(seed * 0.1) * 2;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[1]
            }
          },
          {
            name: 'B相有功功率',
            data: categories.map((_, i) => {
              const baseValue = 28 + (meterIndex * 3);
              const seed = (meterIndex * 100) + i + 2;
              const variation = Math.sin(seed * 0.1) * 2;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[2]
            }
          },
          {
            name: 'C相有功功率',
            data: categories.map((_, i) => {
              const baseValue = 26 + (meterIndex * 3);
              const seed = (meterIndex * 100) + i + 3;
              const variation = Math.sin(seed * 0.1) * 1;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[3]
            }
          }
        ]
      };
      
    case 'reactivePower':
      return {
        categories,
        series: [
          {
            name: '无功功率',
            data: categories.map((_, i) => {
              const baseValue = 30 + (meterIndex * 5);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 2;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[0]
            }
          }
        ]
      };

    case 'powerFactor':
      return {
        categories,
        series: [
          {
            name: '功率因数',
            data: categories.map((_, i) => {
              const baseValue = 0.95 - (meterIndex * 0.01);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 0.01;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[0]
            }
          }
        ]
      };

    case 'voltage':
      // 电压数据（A、B、C三相）
      const voltageSeries: ChartDataSeries[] = [];

      ['A', 'B', 'C'].forEach((phase, phaseIndex) => {
        const baseValue = 220 + (meterIndex * 2) + (phaseIndex * 1);
        const data = categories.map((_, i) => {
          const seed = (meterIndex * 100) + (phaseIndex * 10) + i;
          const variation = Math.sin(seed * 0.1) * 2;
          return Number((baseValue + variation).toFixed(1));
        });

        voltageSeries.push({
          name: `${phase}相电压`,
          data,
          itemStyle: {
            color: colors[phaseIndex % colors.length]
          }
        });
      });

      return {
        categories,
        series: voltageSeries
      };

    case 'instantFlow':
      return {
        categories,
        series: [
          {
            name: '瞬时流量',
            data: categories.map((_, i) => {
              const baseValue = 100 + (meterIndex * 10);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 10;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[0]
            }
          }
        ]
      };

    case 'totalFlow':
      return {
        categories,
        series: [
          {
            name: '累计流量',
            data: categories.map((_, i) => {
              const baseValue = 1000 + (meterIndex * 100) + (i * 50);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 20;
              return Number((baseValue + variation).toFixed(2));
            }),
            itemStyle: {
              color: colors[1]
            }
          }
        ]
      };

    case 'temperature':
      return {
        categories,
        series: [
          {
            name: '温度',
            data: categories.map((_, i) => {
              const baseValue = 25 + (meterIndex * 2);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 3;
              return Number((baseValue + variation).toFixed(1));
            }),
            itemStyle: {
              color: colors[2]
            }
          }
        ]
      };

    case 'pressure':
      return {
        categories,
        series: [
          {
            name: '压力',
            data: categories.map((_, i) => {
              const baseValue = 0.5 + (meterIndex * 0.1);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 0.05;
              return Number((baseValue + variation).toFixed(3));
            }),
            itemStyle: {
              color: colors[3]
            }
          }
        ]
      };

    case 'density':
      return {
        categories,
        series: [
          {
            name: '密度',
            data: categories.map((_, i) => {
              const baseValue = 0.8 + (meterIndex * 0.01);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 0.01;
              return Number((baseValue + variation).toFixed(4));
            }),
            itemStyle: {
              color: colors[4]
            }
          }
        ]
      };

    case 'frequency':
      return {
        categories,
        series: [
          {
            name: '频率',
            data: categories.map((_, i) => {
              const baseValue = 50 + (meterIndex * 0.1);
              const seed = (meterIndex * 100) + i;
              const variation = Math.sin(seed * 0.1) * 0.1;
              return Number((baseValue + variation).toFixed(1));
            }),
            itemStyle: {
              color: colors[0]
            }
          }
        ]
      };
      
    default:
      return {
        categories: [],
        series: []
      };
  }
};

// 获取统一显示的图表数据
const getUnifiedChartData = (): ChartData => {
  const categories = ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
                    '14:00', '16:00', '18:00', '20:00', '22:00'];

  // 基础颜色数组
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f759ab', '#722ed1', '#13c2c2',
                  '#eb2f96', '#fa8c16', '#a0d911', '#1890ff', '#722ed1', '#fa541c'];

  // 合并所有选中的仪表和参数的数据系列
  const allSeries: ChartDataSeries[] = [];
  let colorIndex = 0;

  // 如果没有选中参数，返回空数据
  if (selectedParams.value.length === 0) {
    console.log('No parameters selected, returning empty chart data');
    return {
      categories,
      series: []
    };
  }

  selectedParams.value.forEach(param => {
    selectedMeters.value.forEach(meterId => {
      const meterLabel = getMeterLabel(meterId);
      const paramLabel = getParamLabel(param);

      // 获取该仪表和参数的图表数据
      const chartData = getChartDataForMeterAndParam(meterId, param);

      // 将每个数据系列添加到统一图表中，并添加仪表和参数标识
      chartData.series.forEach(series => {
        allSeries.push({
          name: `${meterLabel}-${paramLabel}-${series.name}`,
          data: series.data,
          itemStyle: {
            color: colors[colorIndex % colors.length]
          }
        });
        colorIndex++;
      });
    });
  });

  console.log('Generated unified chart data:', { categories, series: allSeries });
  return {
    categories,
    series: allSeries
  };
};

// 处理查询按钮点击
const handleQuery = () => {
  console.log('查询条件:', {
    dateRange: dateRange.value,
    selectedMeters: selectedMeters.value,
    selectedParams: selectedParams.value,
    queryInterval: queryInterval.value,
    displayMode: displayMode.value,
    currentOrgCode: currentOrgCode.value,
    currentNowtype: currentNowtype.value
  });
};

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