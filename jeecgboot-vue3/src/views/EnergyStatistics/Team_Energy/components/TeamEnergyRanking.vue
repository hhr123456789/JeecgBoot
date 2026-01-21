<template>
  <div class="team-energy-ranking">
    <div v-for="(item, index) in rankData" :key="index" class="ranking-item mb-4">
      <div class="flex items-center justify-between mb-2">
        <div class="flex items-center">
          <span class="rank-number mr-2" :class="getRankClass(index + 1)">{{ index + 1 }}</span>
          <span class="text-gray-700">{{ item.name }}</span>
        </div>
        <div class="text-gray-600">{{ item.value }} {{ item.unit }}</div>
      </div>
      <div class="progress-bar h-2 rounded-full bg-gray-100">
        <div 
          class="h-full rounded-full transition-all duration-500"
          :class="getProgressClass(index + 1)"
          :style="{ width: getProgressWidth(item.value) }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

// 定义props
const props = defineProps<{
  rankData: {
    name: string;
    value: number;
    unit: string;
  }[];
}>();

// 获取排名样式(科技蓝色系)
const getRankClass = (rank: number) => {
  switch (rank) {
    case 1:
      return 'bg-gradient-to-r from-blue-500 to-blue-600';
    case 2:
      return 'bg-gradient-to-r from-cyan-500 to-cyan-600';
    case 3:
      return 'bg-gradient-to-r from-purple-500 to-purple-600';
    default:
      return 'bg-gradient-to-r from-gray-500 to-gray-600';
  }
};

// 获取进度条样式(科技蓝色系，单色扁平化)
const getProgressClass = (rank: number) => {
  switch (rank) {
    case 1:
      return 'bg-blue-500';
    case 2:
      return 'bg-cyan-500';
    case 3:
      return 'bg-purple-500';
    default:
      return 'bg-gray-500';
  }
};

// 计算进度条宽度
const maxValue = computed(() => Math.max(...props.rankData.map(item => item.value)));
const getProgressWidth = (value: number) => {
  return `${(value / maxValue.value) * 100}%`;
};
</script>

<style scoped>
.team-energy-ranking {
  @apply p-2;
}

.ranking-item {
  @apply transition-all duration-300;
}

.ranking-item:hover {
  @apply transform translate-x-1;
}

.ranking-item .text-gray-700 {
  color: #374151 !important;
  font-weight: 500;
}

.ranking-item .text-gray-600 {
  color: #4b5563 !important;
  font-weight: 600;
}

.rank-number {
  @apply w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.progress-bar {
  @apply overflow-hidden;
  background: #f3f4f6 !important;
  border: 1px solid #e5e7eb;
}

.progress-bar > div {
  box-shadow: 0 0 10px currentColor;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
</style> 