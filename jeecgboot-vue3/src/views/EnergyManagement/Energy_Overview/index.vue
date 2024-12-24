<template>
  <div class="energy-overview-container p-4">
    <!-- 日期选择头部 -->
    <div class="mb-4 flex items-center gap-4">
      <a-radio-group v-model:value="timeUnit" button-style="solid">
        <a-radio-button value="day">日</a-radio-button>
        <a-radio-button value="month">月</a-radio-button>
        <a-radio-button value="year">年</a-radio-button>
      </a-radio-group>
      <a-date-picker 
        v-model:value="selectedDate"
        :format="dateFormat"
        class="w-40"
      />
    </div>

    <!-- 主要内容网格 -->
    <div class="grid grid-cols-2 gap-4">
      <!-- 综合能源查询 -->
      <a-card title="综合能源查询" :bordered="false">
        <template #extra>
          <a-space>
            <a-button type="primary">综合能耗</a-button>
            <a-button>成本</a-button>
          </a-space>
        </template>
        <EnergyTimeChart :chartData="energyTimeData" />
      </a-card>

      <!-- 能源分布 -->
      <a-card title="综合能源占比" :bordered="false">
        <template #extra>
          <a-space>
            <a-button type="primary">综合能耗</a-button>
            <a-button>成本</a-button>
          </a-space>
        </template>
        <EnergyDistributionPie :chartData="energyDistributionData" />
      </a-card>

      <!-- 区域能源分布 -->
      <a-card title="各区域能源占比" :bordered="false">
        <template #extra>
          <a-space>
            <a-button type="primary">综合能耗</a-button>
            <a-button>成本</a-button>
          </a-space>
        </template>
        <AreaEnergyPie :chartData="areaEnergyData" />
      </a-card>

      <!-- 能源转换 -->
      <a-card title="能源转换" :bordered="false">
        <template #extra>
          <a-space>
            <a-select v-model:value="selectedArea" class="w-32">
              <a-select-option value="all">全部电站</a-select-option>
              <a-select-option value="station1">电站1</a-select-option>
            </a-select>
            <a-select v-model:value="selectedMetric" class="w-40">
              <a-select-option value="efficiency">转换效率</a-select-option>
              <a-select-option value="consumption">能耗</a-select-option>
            </a-select>
          </a-space>
        </template>
        <EnergyConsumptionLine :chartData="energyConversionData" />
      </a-card>

      <!-- 能源趋势 -->
      <a-card title="综合能源累计查询" :bordered="false">
        <EnergyTrendChart :chartData="energyTrendData" />
      </a-card>

      <!-- 能源统计 -->
      <a-card title="耗损查询" :bordered="false">
        <template #extra>
          <a-select v-model:value="selectedEnergyType" class="w-32">
            <a-select-option value="electric">电能</a-select-option>
            <a-select-option value="water">水能</a-select-option>
          </a-select>
        </template>
        <EnergyStatistics :data="energyStatsData" />
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import EnergyTimeChart from './components/EnergyTimeChart.vue';
import EnergyDistributionPie from './components/EnergyDistributionPie.vue';
import AreaEnergyPie from './components/AreaEnergyPie.vue';
import EnergyConsumptionLine from './components/EnergyConsumptionLine.vue';
import EnergyTrendChart from './components/EnergyTrendChart.vue';
import EnergyStatistics from './components/EnergyStatistics.vue';

// 状态管理
const timeUnit = ref<'day' | 'month' | 'year'>('day');
const selectedDate = ref<Dayjs>();
const selectedArea = ref<string>('all');
const selectedMetric = ref<string>('efficiency');
const selectedEnergyType = ref<string>('electric');

// 根据时间单位的日期选择器格式
const dateFormat = computed(() => {
  switch (timeUnit.value) {
    case 'day':
      return 'YYYY-MM-DD';
    case 'month':
      return 'YYYY-MM';
    case 'year':
      return 'YYYY';
    default:
      return 'YYYY-MM-DD';
  }
});

// 模拟数据 - 实际实现中这些数据会来自API调用
const energyTimeData = ref({
  categories: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
  series: [
    { name: '电能', data: [30, 40, 35, 50, 49, 60, 70, 91, 125, 100, 80, 65] },
    { name: '燃气', data: [20, 25, 30, 35, 40, 45, 50, 55, 60, 55, 50, 45] },
    { name: '水', data: [15, 20, 25, 30, 35, 40, 45, 50, 55, 50, 45, 40] },
  ]
});

const energyDistributionData = ref([
  { name: '电能', value: 83.6, percentage: 7.92 },
  { name: '燃气', value: 651.42, percentage: 61.69 },
  { name: '水', value: 265.92, percentage: 25.18 },
]);

const areaEnergyData = ref([
  { name: '1号车间', value: 96.64, percentage: 37.73 },
  { name: '2号车间', value: 25.04, percentage: 9.78 },
  { name: '3号车间', value: 42.77, percentage: 16.71 },
  { name: '4号车间', value: 0.45, percentage: 0.18 },
  { name: '5号车间', value: 0.4, percentage: 0.16 },
]);

const energyConversionData = ref({
  categories: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
  data: [45, 52, 65, 115, 95, 90, 85, 95, 90, 85, 80, 85]
});

const energyTrendData = ref({
  categories: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
  data: [1500, 1600, 1700, 1800, 1650, 1750, 1850, 1800, 1700, 1400, 1800, 1900]
});

const energyStatsData = ref({
  consumption: 51.86,
  generation: 3991.8,
  efficiency: 0.08
});

// 生命周期钩子
onMounted(() => {
  // 初始化当前日期
  selectedDate.value = dayjs();
});
</script>

<style scoped>
.energy-overview-container {
  @apply min-h-screen bg-gray-50;
}
</style> 