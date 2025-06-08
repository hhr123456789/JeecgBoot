<template>
  <div class="grid grid-cols-4 gap-4">
    <!-- 总能耗 -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <div class="text-gray-600 text-sm mb-2">总能耗</div>
      <div class="flex items-baseline">
        <span class="text-2xl font-bold mr-2">{{ statistics.totalEnergy }}</span>
        <span class="text-gray-500 text-sm">kWh</span>
      </div>
      <div class="flex items-center mt-2">
        <span class="text-green-500 text-sm mr-2">
          <up-outlined v-if="statistics.energyTrend > 0" />
          <down-outlined v-else />
          {{ Math.abs(statistics.energyTrend) }}%
        </span>
        <span class="text-gray-400 text-sm">较上期</span>
      </div>
    </div>

    <!-- 总用水量 -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <div class="text-gray-600 text-sm mb-2">总用水量</div>
      <div class="flex items-baseline">
        <span class="text-2xl font-bold mr-2">{{ statistics.totalWater }}</span>
        <span class="text-gray-500 text-sm">m³</span>
      </div>
      <div class="flex items-center mt-2">
        <span class="text-red-500 text-sm mr-2">
          <up-outlined v-if="statistics.waterTrend > 0" />
          <down-outlined v-else />
          {{ Math.abs(statistics.waterTrend) }}%
        </span>
        <span class="text-gray-400 text-sm">较上期</span>
      </div>
    </div>

    <!-- 总用气量 -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <div class="text-gray-600 text-sm mb-2">总用气量</div>
      <div class="flex items-baseline">
        <span class="text-2xl font-bold mr-2">{{ statistics.totalGas }}</span>
        <span class="text-gray-500 text-sm">m³</span>
      </div>
      <div class="flex items-center mt-2">
        <span class="text-green-500 text-sm mr-2">
          <up-outlined v-if="statistics.gasTrend > 0" />
          <down-outlined v-else />
          {{ Math.abs(statistics.gasTrend) }}%
        </span>
        <span class="text-gray-400 text-sm">较上期</span>
      </div>
    </div>

    <!-- 综合能耗 -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <div class="text-gray-600 text-sm mb-2">综合能耗</div>
      <div class="flex items-baseline">
        <span class="text-2xl font-bold mr-2">{{ statistics.totalConsumption }}</span>
        <span class="text-gray-500 text-sm">tce</span>
      </div>
      <div class="flex items-center mt-2">
        <span class="text-green-500 text-sm mr-2">
          <up-outlined v-if="statistics.consumptionTrend > 0" />
          <down-outlined v-else />
          {{ Math.abs(statistics.consumptionTrend) }}%
        </span>
        <span class="text-gray-400 text-sm">较上期</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { UpOutlined, DownOutlined } from '@ant-design/icons-vue';

// 统计数据
const statistics = ref({
  totalEnergy: '256,789',
  energyTrend: 12.5,
  totalWater: '45,678',
  waterTrend: -8.2,
  totalGas: '89,012',
  gasTrend: 5.7,
  totalConsumption: '15,678',
  consumptionTrend: 3.4
});
</script> 