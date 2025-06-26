<template>
  <div class="real-time-monitor grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
    <div
      v-for="(item, idx) in monitorData.list"
      :key="idx"
      class="bg-white rounded-lg shadow-sm p-4 flex flex-col"
      tabindex="0"
      :aria-label="item.deviceName + '监控卡片'"
    >
      <!-- 设备名称和状态 -->
      <div class="flex items-center mb-2">
        <span class="w-6 h-6 flex items-center justify-center rounded-full bg-blue-100 text-blue-600 mr-2 text-base font-bold">{{ idx + 1 }}</span>
        <span class="font-medium text-gray-800 flex-1 truncate">{{ item.deviceName }}</span>
      </div>
      <div class="flex items-center mb-2">
        <span class="text-xs text-gray-500 mr-2">负荷状态</span>
        <span class="text-xs text-green-600 font-medium">正常</span>
      </div>
      <!-- 负荷率及进度条 -->
      <div class="flex items-center mb-1">
        <span class="text-xs text-gray-500 mr-2">负荷率</span>
        <span class="text-xs text-blue-600 font-medium">{{ item.loadRate ?? '35.44' }}%</span>
      </div>
      <div class="w-full h-2 bg-gray-200 rounded overflow-hidden mb-2">
        <div class="h-2 bg-blue-500" :style="{ width: (item.loadRate ?? 35.44) + '%' }"></div>
      </div>
      <!-- 数据两行两列紧凑排列，字段名+冒号+数值+单位一行 -->
      <div class="flex text-xs text-gray-500 mb-1">
        <span class="mr-8">Ia：<span class="font-semibold text-base text-gray-800">{{ item.Ia ?? '-' }}</span> A</span>
        <span>Ib：<span class="font-semibold text-base text-gray-800">{{ item.Ib ?? '-' }}</span> A</span>
      </div>
      <div class="flex text-xs text-gray-500">
        <span class="mr-8">Ic：<span class="font-semibold text-base text-gray-800">{{ item.Ic ?? '-' }}</span> A</span>
        <span>COS：<span class="font-semibold text-base text-gray-800">{{ item.COS ?? '-' }}</span></span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// 生产线用电表监控卡片式布局，两行两列数据展示，字段名+冒号+数值+单位一行
const props = defineProps<{
  monitorData: {
    list: Array<{
      deviceName: string;
      Ia?: number;
      Ib?: number;
      Ic?: number;
      COS?: number;
      loadRate?: number;
    }>;
  };
}>();
</script>

<style scoped>
.real-time-monitor {
  /* 响应式卡片布局 */
}
</style> 