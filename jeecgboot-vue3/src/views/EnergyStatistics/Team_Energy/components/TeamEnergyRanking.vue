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

// 获取排名样式
const getRankClass = (rank: number) => {
  switch (rank) {
    case 1:
      return 'bg-amber-500';
    case 2:
      return 'bg-gray-400';
    case 3:
      return 'bg-amber-600';
    default:
      return 'bg-gray-300';
  }
};

// 获取进度条样式
const getProgressClass = (rank: number) => {
  switch (rank) {
    case 1:
      return 'bg-amber-500';
    case 2:
      return 'bg-gray-400';
    case 3:
      return 'bg-amber-600';
    default:
      return 'bg-gray-300';
  }
};

// 计算进度条宽度
const maxValue = computed(() => Math.max(...props.rankData.map(item => item.value)));
const getProgressWidth = (value: number) => {
  return `${(value / maxValue.value) * 100}%`;
};
</script>

<style scoped>
.rank-number {
  @apply w-5 h-5 rounded-full text-white flex items-center justify-center text-xs font-medium;
}

.progress-bar {
  @apply overflow-hidden;
}
</style> 