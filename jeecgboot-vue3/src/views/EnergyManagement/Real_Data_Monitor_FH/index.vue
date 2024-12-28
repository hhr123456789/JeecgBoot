<template>
  <div class="flex h-full bg-gray-100">
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
      <!-- 图表区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex justify-between items-center mb-3">
          <div class="flex space-x-2">
            <a-radio-group v-model:value="timeRange" button-style="solid" size="small">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
          </div>
          <a-button type="primary" size="small">导出数据</a-button>
        </div>
        <LineChart :chartData="chartData" />
      </div>

      <!-- 数据卡片区域 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 有功功率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">有功功率 kW</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.activePower }}
          </div>
        </div>
        <!-- 无功功率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">无功功率 kVar</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.reactivePower }}
          </div>
        </div>
        <!-- 功率因数 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">功率因数</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.powerFactor }}
          </div>
        </div>
        <!-- 负荷率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">负荷率 %</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.loadRate }}
          </div>
        </div>
      </div>

      <!-- 用量查询 -->
      <div class="bg-white rounded p-3">
        <div class="text-gray-600 text-sm mb-3">用量查询</div>
        <div class="grid grid-cols-4 gap-4">
          <div class="bg-white rounded-lg p-3 shadow-sm">
            <div class="text-gray-600 text-sm mb-2">日累计用电量 kWh</div>
            <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-blue-600">
              {{ realTimeData.dailyConsumption }}
            </div>
          </div>
          <div class="bg-white rounded-lg p-3 shadow-sm">
            <div class="text-gray-600 text-sm mb-2">月累计用电量 kWh</div>
            <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-blue-600">
              {{ realTimeData.monthlyConsumption }}
            </div>
          </div>
          <div class="bg-white rounded-lg p-3 shadow-sm">
            <div class="text-gray-600 text-sm mb-2">年累计用电量 kWh</div>
            <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-blue-600">
              {{ realTimeData.yearlyConsumption }}
            </div>
          </div>
          <div class="bg-white rounded-lg p-3 shadow-sm">
            <div class="text-gray-600 text-sm mb-2">小时累计用电量 kWh</div>
            <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-blue-600">
              {{ realTimeData.hourlyConsumption }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import LineChart from './components/LineChart.vue';

// 搜索文本
const searchText = ref('');

// 树形菜单展开和选中状态
const expandedKeys = ref<string[]>(['1']);
const selectedKeys = ref<string[]>(['1-1']);

// 树形菜单数据
const treeData = ref<TreeDataItem[]>([
  {
    title: '分厂1',
    key: '1',
    children: [
      {
        title: '1号生产线',
        key: '1-1',
      },
      {
        title: '2号生产线',
        key: '1-2',
      }
    ]
  },
  {
    title: '分厂2',
    key: '2',
    children: [
      {
        title: '1号生产线',
        key: '2-1',
      }
    ]
  }
]);

// 实时数据接口定义
interface RealTimeData {
  activePower: number;      // 有功功率
  reactivePower: number;    // 无功功率
  powerFactor: number;      // 功率因数
  loadRate: number;         // 负荷率
  hourlyConsumption: number;// 小时用电量
  dailyConsumption: number; // 日用电量
  monthlyConsumption: number;// 月用电量
  yearlyConsumption: number;// 年用电量
}

// 实时数据（静态数据）
const realTimeData = ref<RealTimeData>({
  activePower: 75.54,
  reactivePower: 25.32,
  powerFactor: 0.95,
  loadRate: 85.6,
  hourlyConsumption: 145.53,
  dailyConsumption: 3538.87,
  monthlyConsumption: 104037.17,
  yearlyConsumption: 252525.07
});

// 时间范围选择
const timeRange = ref('day');

// 图表数据（静态数据）
const chartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '有功功率',
      type: 'line',
      data: [75.54, 78.23, 80.67, 79.45, 78.92, 80.34, 81.78, 
             79.89, 78.45, 77.89, 79.23, 78.67],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '无功功率',
      type: 'line',
      data: [25.32, 26.45, 24.89, 25.67, 26.12, 25.78, 26.34,
             25.89, 26.45, 25.67, 26.12, 25.89],
      itemStyle: {
        color: '#52c41a'
      }
    }
  ]
});

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选中节点更新数据
};

// 定时更新数据
let timer: number | null = null;

// 更新数据的方法
const updateData = () => {
  // 模拟数据更新
  realTimeData.value = {
    ...realTimeData.value,
    activePower: Number((realTimeData.value.activePower * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    reactivePower: Number((realTimeData.value.reactivePower * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    powerFactor: Number((realTimeData.value.powerFactor * (1 + (Math.random() - 0.5) * 0.001)).toFixed(2)),
    loadRate: Number((realTimeData.value.loadRate * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2))
  };
};

onMounted(() => {
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
</style> 