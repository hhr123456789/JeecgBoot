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
    <div class="flex-1" style="margin-top: 10px;">
      <!-- 数据卡片区域 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 有功电度正向 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">有功电度正向 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.activePower }}
          </div>
        </div>
        <!-- 总有功功率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总有功功率 kW</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.totalActivePower }}
          </div>
        </div>
        <!-- 总功率因数 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总功率因数</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.powerFactor }}
          </div>
        </div>
        <!-- 频率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">频率 Hz</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.frequency }}
          </div>
        </div>
        <!-- A相电流 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">A相电流 A</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.currentA }}
          </div>
        </div>
        <!-- B相电流 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">B相电流 A</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.currentB }}
          </div>
        </div>
        <!-- C相电流 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">C相电流 A</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.currentC }}
          </div>
        </div>
        <!-- A相温度 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">A相温度 ℃</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.temperatureA }}
          </div>
        </div>
        <!-- B相温度 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">B相温度 ℃</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.temperatureB }}
          </div>
        </div>
        <!-- C相温度 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">C相温度 ℃</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ realTimeData.temperatureC }}
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="bg-white rounded p-3 mb-2">
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
        <MonitorChart :chartData="chartData" />
      </div>

      <!-- 用电量统计 -->
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
import MonitorChart from './components/MonitorChart.vue';

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
        title: '1#车间2#生产线',
        key: '1-2',
      }
    ]
  },
  {
    title: '2#车间',
    key: '2',
    children: [
      {
        title: '2#车间1#生产线',
        key: '2-1',
      }
    ]
  }
]);

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
  
  // 用量数据
  hourlyConsumption: number;
  dailyConsumption: number;
  monthlyConsumption: number;
  yearlyConsumption: number;
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
  currentA: 55.54,
  currentB: 60.67,
  currentC: 58.92,
  temperatureA: 25.81,
  temperatureB: 11.70,
  temperatureC: 40.80,
  
  // 用量数据
  hourlyConsumption: 145.53,
  dailyConsumption: 3538.87,
  monthlyConsumption: 104037.17,
  yearlyConsumption: 252525.07
});

// 时间范围和数据类型选择
const timeRange = ref('day');
const dataType = ref('power');

// 图表数据
const chartData = ref({
  categories: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
               '14:00', '16:00', '18:00', '20:00', '22:00'],
  series: [
    {
      name: '功率',
      data: [55.54, 58.23, 60.67, 59.45, 58.92, 60.34, 61.78, 
             59.89, 58.45, 57.89, 59.23, 58.67]
    }
  ]
});

// 定时更新数据
let timer: number | null = null;

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选中节点更新数据
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
</style> 