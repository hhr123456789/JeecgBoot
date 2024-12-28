<template>
  <div class="energy-classification-container flex">
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
            <a-select-option value="all">全部能源</a-select-option>
            <a-select-option value="electric">电能</a-select-option>
            <a-select-option value="water">水能</a-select-option>
            <a-select-option value="gas">燃气</a-select-option>
          </a-select>
          <a-button type="primary">查询</a-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">总能耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.totalConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">电能消耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.electricConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: kWh</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">水能消耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.waterConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: m³</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">燃气消耗</div>
          <div class="text-2xl font-bold">{{ statisticsData.gasConsumption }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: m³</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">能源分类占比</span>
            <a-radio-group v-model:value="chartType" button-style="solid" size="small">
              <a-radio-button value="consumption">能耗量</a-radio-button>
              <a-radio-button value="cost">成本</a-radio-button>
            </a-radio-group>
          </div>
          <EnergyDistributionPie :chartData="pieChartData" />
        </div>

        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">能源趋势对比</span>
            <a-select v-model:value="trendType" size="small" class="w-32">
              <a-select-option value="day">日趋势</a-select-option>
              <a-select-option value="month">月趋势</a-select-option>
              <a-select-option value="year">年趋势</a-select-option>
            </a-select>
          </div>
          <EnergyTrendLine :chartData="lineChartData" />
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-4">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium">能源分类数据</span>
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
import type { TableColumnsType } from 'ant-design-vue';
import type { TreeProps } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import EnergyDistributionPie from './components/EnergyDistributionPie.vue';
import EnergyTrendLine from './components/EnergyTrendLine.vue';

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
          },
          {
            title: '三号车间',
            key: '1-1-3',
          }
        ]
      },
      {
        title: '辅助部门',
        key: '1-2',
        children: [
          {
            title: '动力车间',
            key: '1-2-1',
          },
          {
            title: '维修车间',
            key: '1-2-2',
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
const timeUnit = ref<'day' | 'month' | 'year'>('month');
const selectedDate = ref<Dayjs>(dayjs());
const energyType = ref<string>('all');
const chartType = ref<'consumption' | 'cost'>('consumption');
const trendType = ref<string>('month');

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
  electricConsumption: number;
  waterConsumption: number;
  gasConsumption: number;
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 1256789.45,
  electricConsumption: 856432.12,
  waterConsumption: 234567.89,
  gasConsumption: 165789.44
});

// 饼图数据
const pieChartData = ref({
  series: [
    {
      name: '能源分类占比',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 856432.12, name: '电能', percentage: 68.15 },
        { value: 234567.89, name: '水能', percentage: 18.66 },
        { value: 165789.44, name: '燃气', percentage: 13.19 }
      ]
    }
  ]
});

// 折线图数据
const lineChartData = ref({
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  series: [
    {
      name: '电能',
      type: 'line',
      data: [150000, 160000, 145000, 155000, 165000, 170000]
    },
    {
      name: '水能',
      type: 'line',
      data: [35000, 38000, 36000, 40000, 42000, 43000]
    },
    {
      name: '燃气',
      type: 'line',
      data: [25000, 28000, 26000, 29000, 30000, 31000]
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
    title: '电能消耗(kWh)',
    dataIndex: 'electric',
    width: 150,
    align: 'right',
  },
  {
    title: '水能消耗(m³)',
    dataIndex: 'water',
    width: 150,
    align: 'right',
  },
  {
    title: '燃气消耗(m³)',
    dataIndex: 'gas',
    width: 150,
    align: 'right',
  },
  {
    title: '电能成本(元)',
    dataIndex: 'electricCost',
    width: 150,
    align: 'right',
  },
  {
    title: '水能成本(元)',
    dataIndex: 'waterCost',
    width: 150,
    align: 'right',
  },
  {
    title: '燃气成本(元)',
    dataIndex: 'gasCost',
    width: 150,
    align: 'right',
  },
  {
    title: '总成本(元)',
    dataIndex: 'totalCost',
    width: 150,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-01',
    electric: 856432.12,
    water: 234567.89,
    gas: 165789.44,
    electricCost: 685145.70,
    waterCost: 140740.73,
    gasCost: 82894.72,
    totalCost: 908781.15
  },
  {
    key: '2',
    time: '2024-02',
    electric: 845678.34,
    water: 225678.90,
    gas: 158976.23,
    electricCost: 676542.67,
    waterCost: 135407.34,
    gasCost: 79488.12,
    totalCost: 891438.13
  }
]);
</script>

<style scoped>
.energy-classification-container {
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

/* 表格样式 */
:deep(.ant-table) {
  @apply text-sm;
}

:deep(.ant-table-thead > tr > th) {
  @apply bg-gray-50 font-medium;
}

/* 按钮组样式 */
:deep(.ant-radio-group) {
  @apply text-sm;
}

/* 下拉框样式 */
:deep(.ant-select) {
  @apply text-sm;
}
</style> 