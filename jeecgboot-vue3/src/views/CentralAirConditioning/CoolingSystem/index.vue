<template>
  <div class="cooling-system-container">
    

    <!-- 中央空调系统概览 -->
    <div class="system-overview">
      <a-card class="overview-card" :bordered="false">
        <template #title>
          <div class="section-title">
            <Icon icon="ant-design:appstore-outlined" class="title-icon" />
            中央空调系统
          </div>
        </template>
        
        <div class="overview-grid">
          <div class="overview-item">
            <div class="item-title">整体能耗</div>
            <div class="item-value">{{ systemOverview.totalEnergy }} <span class="unit">kWh</span></div>
            <div class="item-subtitle">昨日 {{ systemOverview.yesterdayEnergy }} kWh</div>
            <div class="item-label">能源中心</div>
          </div>
          
          <div class="overview-item">
            <div class="item-title">制冷总量</div>
            <div class="item-value">{{ systemOverview.totalCooling }} <span class="unit">GJ</span></div>
            <div class="item-subtitle">昨日 {{ systemOverview.yesterdayCooling }} GJ</div>
            <div class="item-label">能源中心</div>
          </div>
          
          <div class="overview-item">
            <div class="item-title">系统COP</div>
            <div class="item-value">{{ systemOverview.systemCOP }}</div>
            <div class="item-subtitle">昨日 {{ systemOverview.yesterdayCOP }}</div>
            <div class="item-label">综合指标</div>
          </div>
          
          <div class="overview-item">
            <div class="item-title">运行状态</div>
            <div class="item-value status-value">
              <a-tag :color="systemOverview.status === '正常运行' ? 'green' : 'red'">{{ systemOverview.status }}</a-tag>
            </div>
            <div class="item-subtitle">连续运行 {{ systemOverview.runningHours }} 小时</div>
            <div class="item-label">设备运行</div>
          </div>
          
          <div class="overview-item">
            <div class="item-title">数据采集时间</div>
            <div class="item-value time-value">{{ systemOverview.lastUpdateTime }}</div>
            <div class="item-subtitle">{{ systemOverview.updateSource }}</div>
            <div class="item-label">实时更新</div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 冷水机运行参数 -->
    <div class="chiller-section">
      <div class="section-header">
        <div class="section-title">
          <Icon icon="ant-design:setting-outlined" class="title-icon" />
          冷水机运行参数
        </div>
        <div class="chiller-tabs">
          <a-tabs v-model:activeKey="activeChillerTab" @change="onChillerTabChange">
            <a-tab-pane key="1" tab="1#冷水机" />
            <a-tab-pane key="2" tab="2#冷水机" />
            <a-tab-pane key="3" tab="3#冷水机" />
          </a-tabs>
        </div>
      </div>
      
      <div class="chiller-content">
        <div class="chiller-left">
          <div class="chiller-image">
            <img 
              :src="getCurrentChillerImage()" 
              :alt="`${activeChillerTab}#冷水机图片`"
              class="chiller-3d-image"
            />
            <div class="image-overlay">
              <Icon icon="ant-design:eye-outlined" @click="viewEquipment" class="view-icon" />
              <Icon icon="ant-design:fullscreen-outlined" @click="fullscreenView" class="fullscreen-icon" />
            </div>
          </div>
          <div class="equipment-status">
            <a-tag :color="getChillerStatusColor(currentChiller.status)">{{ currentChiller.status }}</a-tag>
          </div>
        </div>
        
        <div class="chiller-right">
          <div class="chiller-params">
            <div class="param-title">{{ activeChillerTab }}#冷水机运行参数</div>
            
            <div class="params-grid">
              <div class="param-row">
                <div class="param-item">
                  <span class="param-label">实时冷量</span>
                  <span class="param-value">{{ currentChiller.realTimeCooling }} <span class="param-unit">kW</span></span>
                </div>
                <div class="param-item">
                  <span class="param-label">实时功率</span>
                  <span class="param-value">{{ currentChiller.realTimePower }} <span class="param-unit">kW</span></span>
                </div>
              </div>
              
              <div class="param-row">
                <div class="param-item">
                  <span class="param-label">累计冷量</span>
                  <span class="param-value">{{ currentChiller.accumulatedCooling }} <span class="param-unit">GJ</span></span>
                </div>
                <div class="param-item">
                  <span class="param-label">累计电量</span>
                  <span class="param-value">{{ currentChiller.accumulatedPower }} <span class="param-unit">kWh</span></span>
                </div>
              </div>
              
              <div class="param-row">
                <div class="param-item">
                  <span class="param-label">COP</span>
                  <span class="param-value">{{ currentChiller.cop }}</span>
                </div>
                <div class="param-item">
                  <span class="param-label">负载率</span>
                  <span class="param-value">{{ currentChiller.loadRate }}<span class="param-unit">%</span></span>
                </div>
              </div>
              
              <div class="param-row">
                <div class="param-item">
                  <span class="param-label">运行时间</span>
                  <span class="param-value">{{ currentChiller.runningTime }} <span class="param-unit">小时</span></span>
                </div>
              </div>
            </div>
            
            <div class="equipment-components">
              <div class="component-title">主要部件状态</div>
              <div class="components-grid">
                <div class="component-item" v-for="component in currentChiller.components" :key="component.name">
                  <span class="component-name">{{ component.name }}</span>
                  <a-tag :color="component.status === '正常' ? 'green' : (component.status === '警告' ? 'orange' : 'red')">
                    {{ component.status }}
                  </a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 水泵系统参数 -->
    <div class="pump-section">
      <div class="section-title">
        <Icon icon="ant-design:cloud-outlined" class="title-icon" />
        水泵系统参数
      </div>
      
      <!-- 冷却泵参数 -->
      <div class="pump-subsection">
        <div class="subsection-title">
          <Icon icon="ant-design:thunderbolt-outlined" class="subsection-icon" />
          冷却泵参数
        </div>
        <div class="pumps-grid">
          <div class="pump-card" v-for="pump in coolingPumps" :key="pump.id">
            <div class="pump-header">
              <span class="pump-name">{{ pump.name }}</span>
              <a-tag :color="getPumpStatusColor(pump.status)">{{ pump.status }}</a-tag>
            </div>
            <div class="pump-params">
              <div class="pump-param">
                <span class="pump-param-label">流量</span>
                <span class="pump-param-value">{{ pump.flowRate }} <span class="pump-param-unit">m³/h</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">转速</span>
                <span class="pump-param-value">{{ pump.rotationSpeed }} <span class="pump-param-unit">rpm</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">电流</span>
                <span class="pump-param-value">{{ pump.current }} <span class="pump-param-unit">A</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">功率</span>
                <span class="pump-param-value">{{ pump.power }} <span class="pump-param-unit">kW</span></span>
              </div>
            </div>
            <div class="pump-temps">
              <div class="temp-item">
                <Icon icon="ant-design:fire-outlined" class="temp-icon supply" />
                <span class="temp-label">供水温度</span>
                <span class="temp-value">{{ pump.supplyTemp }}°C</span>
              </div>
              <div class="temp-item">
                <Icon icon="ant-design:fire-outlined" class="temp-icon return" />
                <span class="temp-label">回水温度</span>
                <span class="temp-value">{{ pump.returnTemp }}°C</span>
              </div>
              <div class="pressure-item">
                <Icon icon="ant-design:dashboard-outlined" class="pressure-icon supply" />
                <span class="pressure-label">供水压力</span>
                <span class="pressure-value">{{ pump.supplyPressure }} MPa</span>
              </div>
              <div class="pressure-item">
                <Icon icon="ant-design:dashboard-outlined" class="pressure-icon return" />
                <span class="pressure-label">回水压力</span>
                <span class="pressure-value">{{ pump.returnPressure }} MPa</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 冷冻泵参数 -->
      <div class="pump-subsection">
        <div class="subsection-title">
          <Icon icon="ant-design:loading-3-quarters-outlined" class="subsection-icon" />
          冷冻泵参数
        </div>
        <div class="pumps-grid">
          <div class="pump-card" v-for="pump in freezingPumps" :key="pump.id">
            <div class="pump-header">
              <span class="pump-name">{{ pump.name }}</span>
              <a-tag :color="getPumpStatusColor(pump.status)">{{ pump.status }}</a-tag>
            </div>
            <div class="pump-params">
              <div class="pump-param">
                <span class="pump-param-label">流量</span>
                <span class="pump-param-value">{{ pump.flowRate }} <span class="pump-param-unit">m³/h</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">转速</span>
                <span class="pump-param-value">{{ pump.rotationSpeed }} <span class="pump-param-unit">rpm</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">电流</span>
                <span class="pump-param-value">{{ pump.current }} <span class="pump-param-unit">A</span></span>
              </div>
              <div class="pump-param">
                <span class="pump-param-label">功率</span>
                <span class="pump-param-value">{{ pump.power }} <span class="pump-param-unit">kW</span></span>
              </div>
            </div>
            <div class="pump-temps">
              <div class="temp-item">
                <Icon icon="ant-design:fire-outlined" class="temp-icon supply" />
                <span class="temp-label">供水温度</span>
                <span class="temp-value">{{ pump.supplyTemp }}°C</span>
              </div>
              <div class="temp-item">
                <Icon icon="ant-design:fire-outlined" class="temp-icon return" />
                <span class="temp-label">回水温度</span>
                <span class="temp-value">{{ pump.returnTemp }}°C</span>
              </div>
              <div class="pressure-item">
                <Icon icon="ant-design:dashboard-outlined" class="pressure-icon supply" />
                <span class="pressure-label">供水压力</span>
                <span class="pressure-value">{{ pump.supplyPressure }} MPa</span>
              </div>
              <div class="pressure-item">
                <Icon icon="ant-design:dashboard-outlined" class="pressure-icon return" />
                <span class="pressure-label">回水压力</span>
                <span class="pressure-value">{{ pump.returnPressure }} MPa</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="update-time">
        数据更新时间：{{ dataUpdateTime }}
      </div>
    </div>
    
    <!-- 系统能耗与COP趋势分析图表 -->
    <div class="bottom-chart-section">
      <a-card class="bottom-chart-card" :bordered="false">
        <template #title>
          <div class="chart-title">
            <Icon icon="ant-design:line-chart-outlined" class="title-icon" />
            系统能耗与COP趋势分析
          </div>
        </template>
        <template #extra>
          <div class="chart-controls">
            <a-radio-group v-model:value="bottomChartTimeRange" size="small" @change="onBottomChartTimeRangeChange">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="week">周</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
            <a-button type="link" size="small" @click="exportBottomChart">
              <Icon icon="ant-design:download-outlined" /> 下载
            </a-button>
          </div>
        </template>
        <div class="bottom-chart-container">
          <EnergyAndCOPChart 
            :chartData="bottomChartData" 
            :height="'450px'" 
            :option="{}"
          />
        </div>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { Icon } from '/@/components/Icon';
import BarAndLine from '/@/components/chart/BarAndLine.vue';
import EnergyAndCOPChart from './components/EnergyAndCOPChart.vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import dayjs from 'dayjs';

// 图片引入
import chiller1Image from '/@/assets/images/冷水机1.png';
import chiller2Image from '/@/assets/images/冷水机2.png';
import chiller3Image from '/@/assets/images/冷水机3.png';

const { createMessage } = useMessage();

// 图表数据类型定义
interface ChartDataItem {
  name: string;
  value: number;
  type: string;
  seriesType: string;
  yAxisIndex?: number;
}

// 数据定义
const chartTimeRange = ref('day');
const bottomChartTimeRange = ref('day');
const activeChillerTab = ref('1');
const loading = ref(false);
const dataUpdateTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));

// 系统概览数据
const systemOverview = reactive({
  totalEnergy: '1256.89',
  yesterdayEnergy: '1189.56',
  totalCooling: '8975.32',
  yesterdayCooling: '8234.15',
  systemCOP: '3.85',
  yesterdayCOP: '3.62',
  status: '正常运行',
  runningHours: '128',
  lastUpdateTime: '2025-09-13 18:30:45',
  updateSource: '实时更新 (自动采集)'
});

// 冷水机数据
const chillersData = reactive({
  '1': {
    id: '1',
    name: '1#冷水机',
    status: '运行',
    realTimeCooling: '1256.8',
    realTimePower: '325.4',
    accumulatedCooling: '25689.3',
    accumulatedPower: '6895.2',
    cop: '3.86',
    loadRate: '78',
    runningTime: '128',
    components: [
      { name: '压缩机', status: '正常' },
      { name: '冷冻器', status: '正常' },
      { name: '蒸发器', status: '正常' }
    ]
  },
  '2': {
    id: '2',
    name: '2#冷水机',
    status: '运行',
    realTimeCooling: '1245.6',
    realTimePower: '318.9',
    accumulatedCooling: '24856.7',
    accumulatedPower: '6654.8',
    cop: '3.91',
    loadRate: '75',
    runningTime: '125',
    components: [
      { name: '压缩机', status: '正常' },
      { name: '冷冻器', status: '正常' },
      { name: '蒸发器', status: '正常' }
    ]
  },
  '3': {
    id: '3',
    name: '3#冷水机',
    status: '停机',
    realTimeCooling: '0',
    realTimePower: '0',
    accumulatedCooling: '18456.2',
    accumulatedPower: '4852.6',
    cop: '0',
    loadRate: '0',
    runningTime: '0',
    components: [
      { name: '压缩机', status: '停机' },
      { name: '冷冻器', status: '停机' },
      { name: '蒸发器', status: '停机' }
    ]
  }
});

// 冷却泵数据
const coolingPumps = reactive([
  {
    id: 'cp1',
    name: '1#冷却泵',
    status: '运行',
    flowRate: '125',
    rotationSpeed: '1480',
    current: '28.6',
    power: '15.2',
    supplyTemp: '32.5',
    returnTemp: '28.3',
    supplyPressure: '0.45',
    returnPressure: '0.38'
  },
  {
    id: 'cp2',
    name: '2#冷却泵',
    status: '运行',
    flowRate: '122',
    rotationSpeed: '1475',
    current: '27.8',
    power: '14.9',
    supplyTemp: '32.1',
    returnTemp: '28.5',
    supplyPressure: '0.43',
    returnPressure: '0.36'
  },
  {
    id: 'cp3',
    name: '3#冷却泵',
    status: '停机',
    flowRate: '0',
    rotationSpeed: '0',
    current: '0',
    power: '0',
    supplyTemp: '25.8',
    returnTemp: '25.2',
    supplyPressure: '0.00',
    returnPressure: '0.00'
  },
  {
    id: 'cp4',
    name: '4#冷却泵',
    status: '备机',
    flowRate: '0',
    rotationSpeed: '0',
    current: '0',
    power: '0',
    supplyTemp: '25.6',
    returnTemp: '25.1',
    supplyPressure: '0.00',
    returnPressure: '0.00'
  }
]);

// 冷冻泵数据
const freezingPumps = reactive([
  {
    id: 'fp1',
    name: '1#冷冻泵',
    status: '运行',
    flowRate: '98',
    rotationSpeed: '1450',
    current: '22.3',
    power: '11.8',
    supplyTemp: '7.2',
    returnTemp: '12.5',
    supplyPressure: '0.52',
    returnPressure: '0.46'
  },
  {
    id: 'fp2',
    name: '2#冷冻泵',
    status: '运行',
    flowRate: '96',
    rotationSpeed: '1445',
    current: '21.8',
    power: '11.5',
    supplyTemp: '7.4',
    returnTemp: '12.3',
    supplyPressure: '0.51',
    returnPressure: '0.45'
  },
  {
    id: 'fp3',
    name: '3#冷冻泵',
    status: '停机',
    flowRate: '0',
    rotationSpeed: '0',
    current: '0',
    power: '0',
    supplyTemp: '15.2',
    returnTemp: '15.8',
    supplyPressure: '0.00',
    returnPressure: '0.00'
  },
  {
    id: 'fp4',
    name: '4#冷冻泵',
    status: '检修中',
    flowRate: '0',
    rotationSpeed: '0',
    current: '0',
    power: '0',
    supplyTemp: '15.5',
    returnTemp: '16.1',
    supplyPressure: '0.00',
    returnPressure: '0.00'
  }
]);

// 图表数据
const chartData = ref<ChartDataItem[]>([]);
const bottomChartData = ref<ChartDataItem[]>([]);

// 图表配置
const chartOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    formatter: function(params) {
      let result = params[0].name + '<br/>';
      params.forEach(function(item) {
        if (item.seriesName === '能耗') {
          result += '● ' + item.seriesName + ': ' + item.value + ' kWh<br/>';
        } else if (item.seriesName === 'COP') {
          result += '● ' + item.seriesName + ': ' + item.value + '<br/>';
        }
      });
      return result;
    }
  },
  legend: {
    data: ['能耗', 'COP'],
    bottom: 10
  },
  xAxis: {
    type: 'category',
    data: ['10/20', '10/21', '10/22', '10/23', '10/24', '10/25', '10/26']
  },
  yAxis: [
    {
      type: 'value',
      name: '能耗 (kWh)',
      position: 'left',
      axisLabel: {
        formatter: '{value}'
      }
    },
    {
      type: 'value',
      name: 'COP',
      position: 'right',
      min: 3.0,
      max: 4.2,
      axisLabel: {
        formatter: '{value}'
      }
    }
  ],
  series: [
    {
      name: '能耗',
      type: 'bar',
      yAxisIndex: 0,
      data: []
    },
    {
      name: 'COP',
      type: 'line',
      yAxisIndex: 1,
      data: []
    }
  ]
});

const bottomChartOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    formatter: function(params) {
      let result = params[0].name + '<br/>';
      params.forEach(function(item) {
        if (item.seriesName === '能耗') {
          result += '● ' + item.seriesName + ': ' + item.value + ' kWh<br/>';
        } else if (item.seriesName === 'COP') {
          result += '● ' + item.seriesName + ': ' + item.value + '<br/>';
        }
      });
      return result;
    }
  },
  legend: {
    data: ['能耗', 'COP'],
    bottom: 10
  },
  xAxis: {
    type: 'category',
    data: ['10/20', '10/21', '10/22', '10/23', '10/24', '10/25', '10/26']
  },
  yAxis: [
    {
      type: 'value',
      name: '能耗 (kWh)',
      position: 'left',
      axisLabel: {
        formatter: '{value}'
      }
    },
    {
      type: 'value',
      name: 'COP',
      position: 'right',
      min: 3.0,
      max: 4.2,
      axisLabel: {
        formatter: '{value}'
      }
    }
  ],
  series: [
    {
      name: '能耗',
      type: 'bar',
      yAxisIndex: 0,
      data: []
    },
    {
      name: 'COP',
      type: 'line',
      yAxisIndex: 1,
      data: []
    }
  ]
});

// 计算属性
const currentChiller = computed(() => {
  return chillersData[activeChillerTab.value];
});

// 方法
const onTimeRangeChange = (e) => {
  console.log('时间范围切换:', e.target.value);
  loadChartData(e.target.value);
};

const onBottomChartTimeRangeChange = (e) => {
  console.log('底部图表时间范围切换:', e.target.value);
  loadBottomChartData(e.target.value);
};

const onChillerTabChange = (activeKey) => {
  console.log('冷水机切换:', activeKey);
  loadChillerData(activeKey);
};

const exportChart = () => {
  createMessage.success('图表导出功能待实现');
};

const exportBottomChart = () => {
  createMessage.success('图表导出功能待实现');
};

const getCurrentChillerImage = () => {
  const imageMap = {
    '1': chiller1Image,
    '2': chiller2Image,
    '3': chiller3Image
  };
  return imageMap[activeChillerTab.value] || imageMap['1'];
};

const viewEquipment = () => {
  createMessage.info('设备查看功能待实现');
};

const fullscreenView = () => {
  createMessage.info('全屏查看功能待实现');
};

const getChillerStatusColor = (status) => {
  switch (status) {
    case '运行':
      return 'green';
    case '停机':
      return 'gray';
    case '故障':
      return 'red';
    case '维修':
      return 'orange';
    default:
      return 'default';
  }
};

const getPumpStatusColor = (status) => {
  switch (status) {
    case '运行':
      return 'green';
    case '停机':
      return 'gray';
    case '备机':
      return 'blue';
    case '故障':
      return 'red';
    case '检修中':
      return 'orange';
    default:
      return 'default';
  }
};

// 数据加载方法
const loadChartData = async (timeRange) => {
  try {
    loading.value = true;
    generateMockChartData(timeRange);
    createMessage.success('数据加载成功');
  } catch (error) {
    createMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

const loadBottomChartData = async (timeRange) => {
  try {
    generateMockBottomChartData(timeRange);
  } catch (error) {
    createMessage.error('底部图表数据加载失败');
  }
};

const loadChillerData = async (chillerId) => {
  try {
    console.log('加载冷水机数据:', chillerId);
  } catch (error) {
    createMessage.error('冷水机数据加载失败');
  }
};

const loadSystemData = async () => {
  try {
    console.log('加载系统数据');
    dataUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    createMessage.error('系统数据加载失败');
  }
};

// 模拟数据生成
const generateMockChartData = (timeRange) => {
  const mockData: ChartDataItem[] = [];
  const categories = ['10/20', '10/21', '10/22', '10/23', '10/24', '10/25', '10/26'];
  const energyData = [1256, 1324, 1189, 1456, 1386, 1298, 1256];
  const copData = [3.78, 3.82, 3.65, 3.95, 3.91, 3.87, 3.85];
  
  categories.forEach((category, index) => {
    mockData.push({
      name: category,
      value: energyData[index],
      type: '能耗',
      seriesType: 'bar',
      yAxisIndex: 0
    });
  });
  
  categories.forEach((category, index) => {
    mockData.push({
      name: category,
      value: copData[index],
      type: 'COP',
      seriesType: 'line',
      yAxisIndex: 1
    });
  });
  
  chartData.value = mockData;
};

const generateMockBottomChartData = (timeRange) => {
  const mockData: ChartDataItem[] = [];
  const categories = ['10/20', '10/21', '10/22', '10/23', '10/24', '10/25', '10/26'];
  const energyData = [1256, 1324, 1189, 1456, 1386, 1298, 1256];
  const copData = [3.78, 3.82, 3.65, 3.95, 3.91, 3.87, 3.85];
  
  categories.forEach((category, index) => {
    mockData.push({
      name: category,
      value: energyData[index],
      type: '能耗',
      seriesType: 'bar',
      yAxisIndex: 0
    });
  });
  
  categories.forEach((category, index) => {
    mockData.push({
      name: category,
      value: copData[index],
      type: 'COP',
      seriesType: 'line',
      yAxisIndex: 1
    });
  });
  
  bottomChartData.value = mockData;
};

// 生命周期钩子
onMounted(() => {
  loadSystemData();
  loadChartData(chartTimeRange.value);
  loadBottomChartData(bottomChartTimeRange.value);
  
  const timer = setInterval(() => {
    loadSystemData();
  }, 30000);
  
  onUnmounted(() => {
    clearInterval(timer);
  });
});
</script>

<style lang="less" scoped>
.cooling-system-container {
  padding: 16px;
  background-color: #f5f7fa;
  min-height: 100vh;
  
  .chart-card {
    margin-bottom: 16px;
    
    .chart-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #1890ff;
      
      .title-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    
    .time-filter {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .chart-container {
      margin-top: 16px;
    }
  }
  
  .system-overview {
    margin-bottom: 16px;
    
    .overview-card {
      .section-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #1890ff;
        
        .title-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }
      
      .overview-grid {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        gap: 16px;
        margin-top: 16px;
        
        .overview-item {
          text-align: center;
          padding: 16px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 8px;
          color: white;
          position: relative;
          overflow: hidden;
          
          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255, 255, 255, 0.1);
            transform: translateX(-100%);
            transition: transform 0.3s ease;
          }
          
          &:hover::before {
            transform: translateX(0);
          }
          
          .item-title {
            font-size: 14px;
            margin-bottom: 8px;
            opacity: 0.9;
          }
          
          .item-value {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 4px;
            
            .unit {
              font-size: 14px;
              font-weight: normal;
              margin-left: 4px;
            }
            
            &.status-value {
              font-size: 16px;
            }
            
            &.time-value {
              font-size: 14px;
              font-weight: 500;
            }
          }
          
          .item-subtitle {
            font-size: 12px;
            opacity: 0.8;
            margin-bottom: 8px;
          }
          
          .item-label {
            font-size: 12px;
            opacity: 0.7;
            background: rgba(255, 255, 255, 0.2);
            padding: 2px 8px;
            border-radius: 12px;
            display: inline-block;
          }
        }
        
        @media (max-width: 1400px) {
          grid-template-columns: repeat(3, 1fr);
        }
        
        @media (max-width: 768px) {
          grid-template-columns: repeat(2, 1fr);
        }
        
        @media (max-width: 480px) {
          grid-template-columns: 1fr;
        }
      }
    }
  }
  
  .chiller-section {
    margin-bottom: 16px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .section-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #1890ff;
        
        .title-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }
      
      .chiller-tabs {
        :deep(.ant-tabs-nav) {
          margin-bottom: 0;
        }
      }
    }
    
    .chiller-content {
      display: flex;
      gap: 16px;
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .chiller-left {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: stretch;
        min-height: 500px;
        
        .chiller-image {
          position: relative;
          width: 100%;
          height: 400px;
          border: 2px dashed #d9d9d9;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #fafafa;
          flex: 1;
          overflow: hidden;
          
          .chiller-3d-image {
            width: 100%;
            height: 100%;
            object-fit: contain;
            transition: transform 0.3s ease;
            
            &:hover {
              transform: scale(1.05);
            }
          }
          
          .image-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #ccc;
            font-size: 16px;
            
            .anticon {
              font-size: 64px;
              margin-bottom: 12px;
            }
          }
          
          .image-overlay {
            position: absolute;
            top: 8px;
            right: 8px;
            display: flex;
            gap: 8px;
            
            .view-icon,
            .fullscreen-icon {
              padding: 4px;
              background: rgba(0, 0, 0, 0.6);
              color: white;
              border-radius: 4px;
              cursor: pointer;
              transition: background-color 0.3s;
              
              &:hover {
                background: rgba(0, 0, 0, 0.8);
              }
            }
          }
        }
        
        .equipment-status {
          margin-top: 16px;
          text-align: center;
          padding: 8px 0;
        }
      }
      
      .chiller-right {
        flex: 2;
        
        .chiller-params {
          .param-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #1890ff;
            border-bottom: 2px solid #e8f4fd;
            padding-bottom: 8px;
          }
          
          .params-grid {
            .param-row {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 24px;
              margin-bottom: 16px;
              
              .param-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 12px 16px;
                background: #f8fafc;
                border-radius: 6px;
                border-left: 4px solid #1890ff;
                
                .param-label {
                  font-size: 14px;
                  color: #666;
                  font-weight: 500;
                }
                
                .param-value {
                  font-size: 16px;
                  font-weight: 600;
                  color: #1890ff;
                  
                  .param-unit {
                    font-size: 12px;
                    font-weight: normal;
                    color: #999;
                    margin-left: 4px;
                  }
                }
              }
            }
          }
          
          .equipment-components {
            margin-top: 20px;
            
            .component-title {
              font-size: 16px;
              font-weight: 600;
              margin-bottom: 12px;
              color: #333;
            }
            
            .components-grid {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 12px;
              
              .component-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 8px 12px;
                background: white;
                border: 1px solid #e8e8e8;
                border-radius: 4px;
                
                .component-name {
                  font-size: 14px;
                  color: #666;
                }
              }
            }
          }
        }
      }
    }
  }
  
  .pump-section {
    margin-bottom: 16px;
    
    .section-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #1890ff;
      margin-bottom: 16px;
      
      .title-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    
    .pump-subsection {
      margin-bottom: 20px;
      
      .subsection-title {
        display: flex;
        align-items: center;
        font-size: 14px;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
        
        .subsection-icon {
          margin-right: 6px;
          font-size: 16px;
          color: #1890ff;
        }
      }
      
      .pumps-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        
        .pump-card {
          background: white;
          border-radius: 8px;
          padding: 16px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          border: 1px solid #f0f0f0;
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
            transform: translateY(-2px);
          }
          
          .pump-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .pump-name {
              font-size: 14px;
              font-weight: 600;
              color: #333;
            }
          }
          
          .pump-params {
            margin-bottom: 12px;
            
            .pump-param {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 6px;
              
              .pump-param-label {
                font-size: 12px;
                color: #666;
              }
              
              .pump-param-value {
                font-size: 14px;
                font-weight: 600;
                color: #1890ff;
                
                .pump-param-unit {
                  font-size: 11px;
                  font-weight: normal;
                  color: #999;
                  margin-left: 2px;
                }
              }
            }
          }
          
          .pump-temps {
            .temp-item,
            .pressure-item {
              display: flex;
              align-items: center;
              margin-bottom: 4px;
              font-size: 12px;
              
              .temp-icon {
                margin-right: 4px;
                font-size: 12px;
                
                &.supply {
                  color: #ff7875;
                }
                
                &.return {
                  color: #40a9ff;
                }
              }
              
              .pressure-icon {
                margin-right: 4px;
                font-size: 12px;
                
                &.supply {
                  color: #73d13d;
                }
                
                &.return {
                  color: #ffc53d;
                }
              }
              
              .temp-label,
              .pressure-label {
                color: #666;
                margin-right: 4px;
              }
              
              .temp-value,
              .pressure-value {
                color: #333;
                font-weight: 500;
                margin-left: auto;
              }
            }
          }
        }
        
        @media (max-width: 1200px) {
          grid-template-columns: repeat(2, 1fr);
        }
        
        @media (max-width: 768px) {
          grid-template-columns: 1fr;
        }
      }
    }
    
    .update-time {
      text-align: right;
      font-size: 12px;
      color: #999;
      margin-top: 12px;
      padding: 8px 16px;
      background: white;
      border-radius: 4px;
    }
  }
  
  .bottom-chart-section {
    .bottom-chart-card {
      .chart-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #1890ff;
        
        .title-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }
      
      .chart-controls {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .bottom-chart-container {
        margin-top: 16px;
        padding: 0 8px; // 减少左右内边距
        
        // 确保图表容器充分利用空间
        & > div {
          width: 100% !important;
          height: 450px !important;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .cooling-system-container {
    .chiller-content {
      flex-direction: column;
      
      .chiller-left {
        .chiller-image {
          width: 100%;
          max-width: 600px;
          height: 300px;
          margin: 0 auto;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .cooling-system-container {
    padding: 8px;
    
    .chiller-section {
      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
    }
    
    .chiller-content {
      .chiller-right {
        .chiller-params {
          .params-grid {
            .param-row {
              grid-template-columns: 1fr;
            }
          }
          
          .equipment-components {
            .components-grid {
              grid-template-columns: 1fr;
            }
          }
        }
      }
    }
  }
}
</style>