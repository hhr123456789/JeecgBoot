<template>
  <div class="real-time-monitor">
    <a-table
      :columns="columns"
      :data-source="monitorData.list"
      :pagination="false"
      size="middle"
    >
      <!-- 设备名称 -->
      <template #deviceName="{ text }">
        <span>{{ text }}</span>
      </template>
      
      <!-- 运行状态 -->
      <template #status="{ text }">
        <a-tag :color="getStatusColor(text)">
          {{ getStatusText(text) }}
        </a-tag>
      </template>

      <!-- 功率/能耗/运行时长 -->
      <template #power="{ text }">
        <span>{{ text.toFixed(1) }} kW</span>
      </template>
      <template #energy="{ text }">
        <span>{{ text.toFixed(1) }} kWh</span>
      </template>
      <template #runtime="{ text }">
        <span>{{ text.toFixed(1) }} h</span>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';

// 定义props
const props = defineProps<{
  monitorData: {
    list: {
      deviceName: string;
      status: string;
      power: number;
      energy: number;
      runtime: number;
    }[];
  };
}>();

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    width: 180,
    align: 'left',
    slots: { customRender: 'deviceName' }
  },
  {
    title: '运行状态',
    dataIndex: 'status',
    width: 120,
    align: 'center',
    slots: { customRender: 'status' }
  },
  {
    title: '实时功率(kW)',
    dataIndex: 'power',
    width: 150,
    align: 'right',
    slots: { customRender: 'power' }
  },
  {
    title: '累计能耗(kWh)',
    dataIndex: 'energy',
    width: 150,
    align: 'right',
    slots: { customRender: 'energy' }
  },
  {
    title: '运行时长(h)',
    dataIndex: 'runtime',
    width: 150,
    align: 'right',
    slots: { customRender: 'runtime' }
  }
];

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    running: 'success',
    stopped: 'default',
    fault: 'error'
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    running: '运行中',
    stopped: '已停止',
    fault: '故障'
  };
  return textMap[status] || status;
};
</script>

<style scoped>
/* ��格样式调整 */
:deep(.ant-table-thead > tr > th) {
  @apply text-center bg-gray-50;
  padding: 12px 16px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 12px 16px;
}

/* 数值类型的单元格样式 */
:deep(.ant-table-tbody > tr > td.ant-table-cell-right) {
  @apply font-mono;
}
</style> 