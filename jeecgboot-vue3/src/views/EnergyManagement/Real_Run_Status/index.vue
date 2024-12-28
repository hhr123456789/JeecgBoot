<template>
  <div class="real-run-status-container p-4">
    <!-- 顶部筛选区域 -->
    <div class="mb-4 bg-white rounded-lg shadow-sm">
      <a-form layout="inline" class="px-4 py-3">
        <div class="flex items-center">
          <span class="mr-2 w-16 text-right">车间:</span>
          <a-select v-model:value="filterForm.workshop" class="!w-32">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="1">1#车间</a-select-option>
            <a-select-option value="2">2#车间</a-select-option>
            <a-select-option value="3">3#车间</a-select-option>
          </a-select>
          <span class="ml-6 mr-2 w-20 text-right">设备状态:</span>
          <a-select v-model:value="filterForm.status" class="!w-32">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="running">运行中</a-select-option>
            <a-select-option value="stopped">已停止</a-select-option>
            <a-select-option value="fault">故障</a-select-option>
          </a-select>
          <a-space class="ml-6">
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </div>
      </a-form>
    </div>

    <!-- 数据列表区域 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-4">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
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

        <!-- 电流/电压/功率因素 -->
        <template #current="{ text }">
          <span>{{ text.toFixed(2) }} A</span>
        </template>
        <template #voltage="{ text }">
          <span>{{ text.toFixed(2) }} V</span>
        </template>
        <template #powerFactor="{ text }">
          <span>{{ text.toFixed(2) }}</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a @click="handleView(record)">查看</a>
          <a-divider type="vertical" />
          <a @click="handleHistory(record)">历史</a>
        </template>
      </a-table>
    </div>

    <!-- 图表展示区域 -->
    <div class="grid grid-cols-2 gap-4">
      <a-card title="运行状态分布" :bordered="false">
        <StatusChart :data="statusChartData" />
      </a-card>
      <a-card title="参数趋势" :bordered="false">
        <StatusChart :data="trendChartData" />
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import StatusChart from './components/StatusChart.vue';

// 筛选表单数据
const filterForm = ref({
  workshop: '',
  status: ''
});

// 表格加载状态
const loading = ref(false);

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
    title: '所属车间',
    dataIndex: 'workshop',
    width: 120,
    align: 'center'
  },
  {
    title: '运行状态',
    dataIndex: 'status',
    width: 100,
    align: 'center',
    slots: { customRender: 'status' }
  },
  {
    title: '电流(A)',
    dataIndex: 'current',
    width: 100,
    align: 'right',
    slots: { customRender: 'current' }
  },
  {
    title: '电压(V)',
    dataIndex: 'voltage',
    width: 100,
    align: 'right',
    slots: { customRender: 'voltage' }
  },
  {
    title: '功率因素',
    dataIndex: 'powerFactor',
    width: 100,
    align: 'right',
    slots: { customRender: 'powerFactor' }
  },
  {
    title: '运行时长(h)',
    dataIndex: 'runningHours',
    width: 120,
    align: 'right'
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    align: 'center',
    slots: { customRender: 'action' }
  }
];

// 模拟表格数据
const tableData = ref([
  {
    key: '1',
    deviceName: '1#生产线主机',
    workshop: '1#车间',
    status: 'running',
    current: 75.6,
    voltage: 380.5,
    powerFactor: 0.92,
    runningHours: 168.5
  },
  {
    key: '2',
    deviceName: '2#生产线主机',
    workshop: '1#车间',
    status: 'stopped',
    current: 0,
    voltage: 380.0,
    powerFactor: 0,
    runningHours: 150.2
  },
  // ... 更多数据
]);

// 状态图表数据
const statusChartData = ref({
  running: 15,
  stopped: 3,
  fault: 2
});

// 趋势图表数据
const trendChartData = ref({
  categories: ['08:00', '09:00', '10:00', '11:00', '12:00'],
  series: [
    {
      name: '平均电流',
      data: [68.5, 72.3, 75.8, 71.2, 73.6]
    },
    {
      name: '平均电压',
      data: [380.2, 379.8, 380.5, 380.1, 379.9]
    }
  ]
});

// 定时更新数据
let timer: number | null = null;

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

// 处理查询
const handleSearch = () => {
  console.log('查询条件：', filterForm.value);
  // TODO: 实现查询逻辑
};

// 处理重置
const handleReset = () => {
  filterForm.value = {
    workshop: '',
    status: ''
  };
};

// 查看详情
const handleView = (record: any) => {
  console.log('查看详情：', record);
  // TODO: 实现查看详情逻辑
};

// 查看历史
const handleHistory = (record: any) => {
  console.log('查看历史：', record);
  // TODO: 实现查看历史逻辑
};

// 更新数据的方法
const updateData = () => {
  // 模拟数据更新
  tableData.value = tableData.value.map(item => {
    if (item.status === 'running') {
      return {
        ...item,
        current: Number((item.current * (1 + (Math.random() - 0.5) * 0.1)).toFixed(2)),
        voltage: Number((item.voltage * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
        powerFactor: Number((item.powerFactor * (1 + (Math.random() - 0.5) * 0.05)).toFixed(2))
      };
    }
    return item;
  });
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
.real-run-status-container {
  @apply min-h-screen bg-gray-50;
}

/* 表格样式调整 */
:deep(.ant-table-thead > tr > th) {
  @apply text-center bg-gray-50;
  padding: 8px 16px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 8px 16px;
}

/* 数值类型的单元格样式 */
:deep(.ant-table-tbody > tr > td.ant-table-cell-right) {
  @apply font-mono;
}
</style> 