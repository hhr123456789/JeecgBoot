<template>
  <div class="grid grid-cols-3 gap-4">
    <div class="stat-card">
      <div class="stat-title">耗能用量 (t)</div>
      <div class="stat-value">{{ data.consumption }}</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">电生产量 (kWh)</div>
      <div class="stat-value">{{ data.generation }}</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">转换效率</div>
      <div class="stat-value">{{ data.efficiency }} kWh/t</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  data: {
    consumption: number;
    generation: number;
    efficiency: number;
  };
}>();
</script>

<style scoped>
.stat-card {
  @apply bg-white rounded-lg p-4 shadow-sm border border-gray-100;
}

.stat-title {
  @apply text-gray-500 text-sm mb-2;
}

.stat-value {
  @apply text-2xl font-semibold text-gray-800;
}
</style> 