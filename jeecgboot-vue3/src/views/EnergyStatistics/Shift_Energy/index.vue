<!-- 主视图文件 -->
<template>
  <div class="shift-energy-container flex">
    <!-- 左侧部门树 -->
    <div class="w-64 bg-white p-4 mr-4 rounded">
      <div class="flex items-center justify-between mb-4">
        <span class="text-base font-medium">部门列表</span>
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索部门"
          style="width: 130px"
          size="small"
        />
      </div>
      <a-tree
        v-model:selectedKeys="selectedKeys"
        v-model:expandedKeys="expandedKeys"
        :tree-data="treeData"
        :field-names="{ title: 'title', key: 'key', children: 'children' }"
        @select="handleSelect"
      />
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <!-- 日期选择头部 -->
      <div class="mb-4 bg-white rounded p-4">
        <div class="flex items-center gap-4">
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
          <a-select v-model:value="energyType" class="w-40">
            <a-select-option value="all">全部班次</a-select-option>
            <a-select-option value="morning">早班</a-select-option>
            <a-select-option value="middle">中班</a-select-option>
            <a-select-option value="night">晚班</a-select-option>
          </a-select>
          <a-button type="primary">查询</a-button>
        </div>
      </div>

      <!-- 班次能耗统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">总能耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.totalConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">早班能耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.morningConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">中班能耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.middleConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">晚班能耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.nightConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <!-- 班次能耗趋势图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">班次能耗趋势</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: kWh</span>
            </div>
          </div>
          <ShiftEnergyTrend :chartData="trendData" />
        </div>

        <!-- 班次能耗占比图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">班次能耗占比</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: %</span>
            </div>
          </div>
          <ShiftEnergyPie :chartData="pieData" />
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-4">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium">班次能耗明细</span>
          <a-button type="primary" size="small">导出数据</a-button>
        </div>
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="false"
          size="middle"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import ShiftEnergyTrend from './components/ShiftEnergyTrend.vue';
import ShiftEnergyPie from './components/ShiftEnergyPie.vue';

// 部门树相关
const searchText = ref<string>('');
const selectedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>(['1']);

// 部门树数据
const treeData = ref([
  {
    title: '总公司',
    key: '1',
    children: [
      {
        title: '生产部门',
        key: '1-1',
        children: [
          {
            title: '一号车间',
            key: '1-1-1',
          },
          {
            title: '二号车间',
            key: '1-1-2',
          }
        ]
      }
    ]
  }
]);

// 处理部门选择
const handleSelect: TreeProps['onSelect'] = (selectedKeys, info) => {
  console.log('selected', selectedKeys, info);
  // 这里可以根据选择的部门重新加载数据
};

// 时间单位选择
const timeUnit = ref<'day' | 'month' | 'year'>('day');
const selectedDate = ref<Dayjs>(dayjs());
const energyType = ref<string>('all');

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

// 统计数据
interface StatisticsData {
  totalConsumption: number;
  morningConsumption: number;
  middleConsumption: number;
  nightConsumption: number;
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 29368.72,
  morningConsumption: 10031.75,
  middleConsumption: 9948.16,
  nightConsumption: 9388.81
});

// 趋势图数据
const trendData = ref({
  xAxis: {
    type: 'category' as const,
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '早班',
      type: 'bar' as const,
      stack: 'total',
      data: [0, 0, 0, 0, 0, 0, 1800, 1500, 1700, 1800, 1650, 1750],
      itemStyle: {
        color: '#4B7BE5'
      }
    },
    {
      name: '中班',
      type: 'bar' as const,
      stack: 'total',
      data: [1850, 1800, 1700, 1400, 1800, 1900, 0, 0, 0, 0, 0, 0],
      itemStyle: {
        color: '#23C343'
      }
    },
    {
      name: '晚班',
      type: 'bar' as const,
      stack: 'total',
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 1700, 1650, 1550],
      itemStyle: {
        color: '#FF9F40'
      }
    }
  ]
});

// 饼图数据
const pieData = ref({
  series: [
    {
      name: '班次能耗占比',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      data: [
        { value: 34.16, name: '早班' },
        { value: 33.87, name: '中班' },
        { value: 31.97, name: '晚班' }
      ]
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '时间',
    dataIndex: 'time',
    width: 120,
  },
  {
    title: '早班能耗(kWh)',
    dataIndex: 'morning',
    width: 150,
    align: 'right',
  },
  {
    title: '中班能耗(kWh)',
    dataIndex: 'middle',
    width: 150,
    align: 'right',
  },
  {
    title: '晚班能耗(kWh)',
    dataIndex: 'night',
    width: 150,
    align: 'right',
  },
  {
    title: '总能耗(kWh)',
    dataIndex: 'total',
    width: 150,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-02-01',
    morning: 10031.75,
    middle: 9948.16,
    night: 9388.81,
    total: 29368.72
  },
  {
    key: '2',
    time: '2024-02-02',
    morning: 9856.32,
    middle: 9745.18,
    night: 9234.56,
    total: 28836.06
  }
]);
</script>

<style scoped>
.shift-energy-container {
  @apply min-h-screen bg-gray-50 p-4;
}

/* 树形控件样式 */
:deep(.ant-tree) {
  font-size: 14px;
}

:deep(.ant-tree-node-content-wrapper) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 按钮组样式 */
:deep(.ant-radio-group) {
  @apply text-sm;
}

/* 下拉框样式 */
:deep(.ant-select) {
  @apply text-sm;
}

/* 表格样式 */
:deep(.ant-table) {
  @apply text-sm;
}

:deep(.ant-table-thead > tr > th) {
  @apply bg-gray-50 font-medium;
}
</style> 