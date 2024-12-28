<template>
  <div class="real-data-monitor-container p-4">
    <!-- 顶部导航栏 -->
    <div class="mb-4">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="1" tab="生产线用电数据"></a-tab-pane>
        <a-tab-pane key="2" tab="生产线用水数据"></a-tab-pane>
        <a-tab-pane key="3" tab="生产线用气数据"></a-tab-pane>
        <a-tab-pane key="4" tab="生产线用热数据"></a-tab-pane>
      </a-tabs>
    </div>

    <!-- 主要内容区域 -->
    <div class="grid grid-cols-4 gap-4">
      <!-- 1#车间1#产线用电电流 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center mr-1">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">1#车间1#产线用电表</span>
        </div>
        <div class="space-y-0.5">
          <div class="flex items-center text-xs">
            <span class="text-gray-600 w-14">负荷状态</span>
            <span class="text-green-500">正常</span>
          </div>
          <div class="flex items-center text-xs relative">
            <span class="text-gray-600 w-14">负荷率</span>
            <div class="flex-1 mx-1">
              <div class="relative h-1.5 bg-gray-200 rounded">
                <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
              </div>
            </div>
            <span class="text-xs text-gray-600">63.10%</span>
          </div>
          <div class="grid grid-cols-2 gap-x-2 text-xs mt-0.5">
            <div class="flex justify-between">
              <span class="text-gray-600">Ia</span>
              <span>{{ workshop1Line1Current.l1.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ib</span>
              <span>{{ workshop1Line1Current.l2.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ic</span>
              <span>{{ workshop1Line1Current.l3.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">COS</span>
              <span>63.80</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 1#车间3#产线用电电流 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center mr-1">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">1#车间3#产线用电表</span>
        </div>
        <div class="space-y-0.5">
          <div class="flex items-center text-xs">
            <span class="text-gray-600 w-14">负荷状态</span>
            <span class="text-green-500">正常</span>
          </div>
          <div class="flex items-center text-xs relative">
            <span class="text-gray-600 w-14">负荷率</span>
            <div class="flex-1 mx-1">
              <div class="relative h-1.5 bg-gray-200 rounded">
                <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
              </div>
            </div>
            <span class="text-xs text-gray-600">63.10%</span>
          </div>
          <div class="grid grid-cols-2 gap-x-2 text-xs mt-0.5">
            <div class="flex justify-between">
              <span class="text-gray-600">Ia</span>
              <span>{{ workshop1Line3Current.l1.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ib</span>
              <span>{{ workshop1Line3Current.l2.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ic</span>
              <span>{{ workshop1Line3Current.l3.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">COS</span>
              <span>63.80</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 2#车间用电电流 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center mr-1">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">2#车间用电表</span>
        </div>
        <div class="space-y-0.5">
          <div class="flex items-center text-xs">
            <span class="text-gray-600 w-14">负荷状态</span>
            <span class="text-green-500">正常</span>
          </div>
          <div class="flex items-center text-xs relative">
            <span class="text-gray-600 w-14">负荷率</span>
            <div class="flex-1 mx-1">
              <div class="relative h-1.5 bg-gray-200 rounded">
                <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
              </div>
            </div>
            <span class="text-xs text-gray-600">63.10%</span>
          </div>
          <div class="grid grid-cols-2 gap-x-2 text-xs mt-0.5">
            <div class="flex justify-between">
              <span class="text-gray-600">Ia</span>
              <span>{{ workshop2Current.l1.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ib</span>
              <span>{{ workshop2Current.l2.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ic</span>
              <span>{{ workshop2Current.l3.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">COS</span>
              <span>63.80</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 3#车间用电电流 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-2">
          <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">3#车间用电表</span>
        </div>
        <div class="space-y-1">
          <div class="flex items-center text-sm">
            <span class="text-gray-600 w-16">负荷状态</span>
            <span class="text-green-500">正常</span>
          </div>
          <div class="flex items-center text-sm">
            <span class="text-gray-600 w-16">负荷率</span>
            <div class="flex-1 ml-2">
              <div class="relative h-2 bg-gray-200 rounded">
                <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
              </div>
              <span class="text-xs text-gray-600 absolute right-4">63.10%</span>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-x-4 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-600">Ia</span>
              <span>{{ workshop3Current.l1.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ib</span>
              <span>{{ workshop3Current.l2.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ic</span>
              <span>{{ workshop3Current.l3.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">COS</span>
              <span>63.80</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 4#车间用电电流 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-2">
          <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">4#车间用电表</span>
        </div>
        <div class="space-y-1">
          <div class="flex items-center text-sm">
            <span class="text-gray-600 w-16">负荷状态</span>
            <span class="text-green-500">正常</span>
          </div>
          <div class="flex items-center text-sm">
            <span class="text-gray-600 w-16">负荷率</span>
            <div class="flex-1 ml-2">
              <div class="relative h-2 bg-gray-200 rounded">
                <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
              </div>
              <span class="text-xs text-gray-600 absolute right-4">63.10%</span>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-x-4 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-600">Ia</span>
              <span>{{ workshop4Current.l1.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ib</span>
              <span>{{ workshop4Current.l2.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Ic</span>
              <span>{{ workshop4Current.l3.toFixed(2) }} A</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">COS</span>
              <span>63.80</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 电力监测数据展示 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-2">
          <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">电力监测数据展示</span>
        </div>
        <RealTimeChart :chartData="powerMonitorData" />
      </a-card>

      <!-- 电力监测历史数据 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-2">
          <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">电力监测历史数据</span>
        </div>
        <HistoryChart :chartData="powerHistoryData" />
      </a-card>

      <!-- 生产参数监测 -->
      <a-card class="monitor-card" :bordered="false">
        <div class="flex items-center mb-2">
          <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
            <span class="text-white text-xs">O</span>
          </div>
          <span class="text-sm">生产参数监测</span>
        </div>
        <ProductionParamsChart :chartData="productionParamsData" />
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import RealTimeChart from './components/RealTimeChart.vue';
import HistoryChart from './components/HistoryChart.vue';
import ProductionParamsChart from './components/ProductionParamsChart.vue';

interface CurrentData {
  total: number;
  l1: number;
  l2: number;
  l3: number;
}

// 当前激活的标签页
const activeTab = ref('1');

// 各车间电流数据
const workshop1Line1Current = ref<CurrentData>({
  total: 84.61,
  l1: 27.87,
  l2: 28.26,
  l3: 28.48
});

const workshop1Line3Current = ref<CurrentData>({
  total: 74.24,
  l1: 20.20,
  l2: 47.72,
  l3: 6.32
});

const workshop2Current = ref<CurrentData>({
  total: 31.88,
  l1: 1.57,
  l2: 75.19,
  l3: 0.245
});

const workshop3Current = ref<CurrentData>({
  total: 80.63,
  l1: 30.63,
  l2: 40.12,
  l3: 9.88
});

const workshop4Current = ref<CurrentData>({
  total: 99.28,
  l1: 30.63,
  l2: 44.12,
  l3: 24.53
});

// 模拟实时数据
const powerMonitorData = ref<{
  categories: string[];
  series: Array<{
    name: string;
    data: number[];
  }>;
}>({
  categories: [],
  series: [
    { name: '电流', data: [] },
    { name: '电压', data: [] },
    { name: '功率', data: [] }
  ]
});

// 模拟历史数据
const powerHistoryData = ref({
  categories: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
  series: [
    { name: '今日', data: [120, 132, 145, 160, 142, 138] },
    { name: '昨日', data: [110, 128, 140, 155, 138, 133] }
  ]
});

// 模拟生产参数数据
const productionParamsData = ref({
  categories: ['压力', '温度', '流量', '速度'],
  data: [85, 65, 92, 78]
});

// 模拟实时数据更新
let timer: number | null = null;

const updateRealTimeData = () => {
  const now = new Date();
  const timeStr = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
  
  const newCategories = [...powerMonitorData.value.categories, timeStr];
  const newSeries = powerMonitorData.value.series.map(series => ({
    name: series.name,
    data: [...series.data, Number((Math.random() * (series.name === '电压' ? 220 : 100)).toFixed(2))]
  }));

  // 保持最近30个数据点
  if (newCategories.length > 30) {
    newCategories.shift();
    newSeries.forEach(series => series.data.shift());
  }

  powerMonitorData.value = {
    categories: newCategories,
    series: newSeries
  };

  // 更新车间数据
  workshop1Line1Current.value = updateCurrentData(workshop1Line1Current.value);
  workshop1Line3Current.value = updateCurrentData(workshop1Line3Current.value);
  workshop2Current.value = updateCurrentData(workshop2Current.value);
  workshop3Current.value = updateCurrentData(workshop3Current.value);
  workshop4Current.value = updateCurrentData(workshop4Current.value);
};

// 模拟更新电流数据
const updateCurrentData = (current: CurrentData): CurrentData => {
  const variation = 0.1; // 10% 的变化范围
  const round = (num: number) => Number(num.toFixed(2));
  return {
    l1: round(current.l1 * (1 + (Math.random() - 0.5) * variation)),
    l2: round(current.l2 * (1 + (Math.random() - 0.5) * variation)),
    l3: round(current.l3 * (1 + (Math.random() - 0.5) * variation)),
    total: round(current.total * (1 + (Math.random() - 0.5) * variation))
  };
};

onMounted(() => {
  // 启动实时数据更新
  timer = window.setInterval(updateRealTimeData, 1000);
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
.real-data-monitor-container {
  @apply min-h-screen bg-gray-50;
}

.monitor-card {
  @apply bg-white rounded-lg shadow-sm;
}

/* 自定义标签页样式 */
:deep(.ant-tabs-nav) {
  @apply mb-4;
}

:deep(.ant-card-body) {
  @apply p-3;
}
</style> 