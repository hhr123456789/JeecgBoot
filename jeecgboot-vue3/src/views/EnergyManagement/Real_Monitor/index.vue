<template>
  <div class="flex h-full">
    <!-- 左侧树形菜单 -->
    <div class="w-64 bg-white p-2 mr-2 rounded overflow-auto">
      <a-input-search
        v-model:value="searchText"
        placeholder="请输入搜索内容"
        class="mb-2"
      />
      <a-tree
        v-model:expandedKeys="expandedKeys"
        v-model:selectedKeys="selectedKeys"
        :tree-data="treeData"
        @select="handleSelect"
      />
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <div class="real-data-monitor-container p-4">
        <!-- 顶部导航栏 -->
        <div class="mb-4">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="1" tab="全厂用电"></a-tab-pane>
            <a-tab-pane key="2" tab="全厂用水"></a-tab-pane>
            <a-tab-pane key="3" tab="全厂用天然气"></a-tab-pane>
            <a-tab-pane key="4" tab="全厂用缩空气"></a-tab-pane>
          </a-tabs>
        </div>

        <!-- 主要内容区域 -->
        <div class="grid grid-cols-2 gap-2">
          <!-- 1#产线1#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线1#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线2#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线2#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线3#电表-->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线3#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线4#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线4#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线5#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线5#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import RealTimeChart from './components/RealTimeChart.vue';
import HistoryChart from './components/HistoryChart.vue';
import ProductionParamsChart from './components/ProductionParamsChart.vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';

interface CurrentData {
  total: number;
  l1: number;
  l2: number;
  l3: number;
}

// 搜索文本
const searchText = ref('');

// 树形菜单展开和选中状态
const expandedKeys = ref<string[]>(['1']);
const selectedKeys = ref<string[]>(['1-1']);

// 树形菜单数据
const treeData = ref<TreeDataItem[]>([
  {
    title: '1#车间',
    key: '1',
    children: [
      {
        title: '1#车间1#生产线',
        key: '1-1',
      },
      {
        title: '1#车间3#生产线',
        key: '1-3',
      }
    ]
  },
  {
    title: '2#车间',
    key: '2',
    children: [
      {
        title: '2#车间用电表',
        key: '2-1',
      }
    ]
  },
  {
    title: '3#车间',
    key: '3',
    children: [
      {
        title: '3#车间用电表',
        key: '3-1',
      }
    ]
  },
  {
    title: '4#车间',
    key: '4',
    children: [
      {
        title: '4#车间用电表',
        key: '4-1',
      }
    ]
  }
]);

// 当前激活的标签页
const activeTab = ref('1');

// 各车间电流数据
const workshop1 = ref<CurrentData>({
  total: 84.61,
  l1: 27.87,
  l2: 28.26,
  l3: 28.48
});

const workshop2 = ref<CurrentData>({
  total: 74.24,
  l1: 20.20,
  l2: 47.72,
  l3: 6.32
});

const workshop3 = ref<CurrentData>({
  total: 31.88,
  l1: 1.57,
  l2: 75.19,
  l3: 0.245
});

const workshop4 = ref<CurrentData>({
  total: 80.63,
  l1: 30.63,
  l2: 40.12,
  l3: 9.88
});

const workshop5 = ref<CurrentData>({
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
  workshop1.value = updateCurrentData(workshop1.value);
  workshop2.value = updateCurrentData(workshop2.value);
  workshop3.value = updateCurrentData(workshop3.value);
  workshop4.value = updateCurrentData(workshop4.value);
  workshop5.value = updateCurrentData(workshop5.value);
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

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // 根据选中节点更新数据
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
.h-full {
  /*min-height: calc(100vh - 100px);*/
}

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

/* 树形菜单样式 */
:deep(.ant-tree) {
  font-size: 13px;
}

/* 搜索框样式 */
:deep(.ant-input-search) {
  font-size: 13px;
}

/* 进度条样式 */
.progress-bar {
  width: 100px;
  max-width: 100px;
}

.bg-blue-500 {
  max-width: 100px;
}

/* 数据表格样式 */
.data-table {
  margin-top: 8px;
  padding: 8px;
  background-color: rgba(249, 250, 251, 0.5);
  border-radius: 6px;
}

/* 数据行样式 */
.data-row {
  padding: 6px 0;
  border-bottom: 1px solid;
  border-image: linear-gradient(to right, rgba(229, 231, 235, 0.1), rgba(209, 213, 219, 0.8), rgba(229, 231, 235, 0.1));
  border-image-slice: 1;
}

.data-row:last-child {
  border-bottom: none;
}

.data-row span {
  color: #374151;
}
</style> 