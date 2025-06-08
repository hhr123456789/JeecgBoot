<template>
  <div class="analysis-container p-4">
    <!-- 顶部统计卡片 -->
    <div class="grid grid-cols-4 gap-4 mb-4">
      <TotalStatistics />
    </div>

    <!-- 中部图表区域 -->
    <div class="grid grid-cols-2 gap-4 mb-4">
      <!-- 能源趋势图 -->
      <div class="bg-white rounded-lg p-4">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium">能源趋势</span>
          <a-radio-group v-model:value="timeRange" button-style="solid" size="small">
            <a-radio-button value="day">日</a-radio-button>
            <a-radio-button value="month">月</a-radio-button>
            <a-radio-button value="year">年</a-radio-button>
          </a-radio-group>
        </div>
        <EnergyTrend :chartData="trendData" />
      </div>

      <!-- 能源分布图 -->
      <div class="bg-white rounded-lg p-4">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium">能源分布</span>
          <a-select v-model:value="energyType" style="width: 120px" size="small">
            <a-select-option value="all">全部能源</a-select-option>
            <a-select-option value="electric">电能</a-select-option>
            <a-select-option value="water">水能</a-select-option>
            <a-select-option value="gas">燃气</a-select-option>
          </a-select>
        </div>
        <EnergyDistribution :chartData="distributionData" />
      </div>
    </div>

    <!-- 底部实时监控 -->
    <div class="bg-white rounded-lg p-4">
      <div class="flex items-center justify-between mb-4">
        <span class="text-base font-medium">实时监控</span>
        <a-button type="primary" size="small">导出数据</a-button>
      </div>
      <RealTimeMonitor :monitorData="monitorData" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import TotalStatistics from './components/TotalStatistics.vue';
import EnergyTrend from './components/EnergyTrend.vue';
import EnergyDistribution from './components/EnergyDistribution.vue';
import RealTimeMonitor from './components/RealTimeMonitor.vue';

// 时间范围选择
const timeRange = ref<'day' | 'month' | 'year'>('day');
// 能源类型选择
const energyType = ref<string>('all');

// 能源趋势数据
const trendData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '用电量',
      type: 'line',
      data: [320, 332, 301, 334, 390, 330, 320, 332, 301, 334, 390, 330],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '用水量',
      type: 'line',
      data: [220, 182, 191, 234, 290, 330, 310, 182, 191, 234, 290, 330],
      itemStyle: {
        color: '#52c41a'
      }
    }
  ]
});

// 能源分布数据
const distributionData = ref({
  series: [
    {
      name: '能源分布',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 44.25, name: '电能' },
        { value: 33.25, name: '水能' },
        { value: 22.50, name: '燃气' }
      ]
    }
  ]
});

// 实时监控数据
const monitorData = ref({
  list: [
    {
      deviceName: '1#空压机',
      status: 'running',
      power: 75.6,
      energy: 1234.5,
      runtime: 168.5
    },
    {
      deviceName: '2#空压机',
      status: 'stopped',
      power: 0,
      energy: 986.3,
      runtime: 150.2
    },
    {
      deviceName: '3#空压机',
      status: 'fault',
      power: 0,
      energy: 1567.8,
      runtime: 245.7
    }
  ]
});
</script>

<style scoped>
.analysis-container {
  @apply min-h-screen bg-gray-50;
}
</style> 