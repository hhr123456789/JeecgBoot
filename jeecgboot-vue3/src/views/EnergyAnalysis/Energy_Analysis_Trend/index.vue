<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-64 bg-white p-2 mr-2 rounded overflow-auto">
      <a-input-search
        v-model:value="searchText"
        placeholder="请输入搜索内容"
        class="mb-2"
      />
      <a-tree
        v-model:expandedKeys="expandedKeys"
        v-model:selectedKeys="selectedKeys"
        :tree-data="treeData"
        @select="handleSelect"
      />
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <!-- 顶部统计卡片 -->
      <div class="bg-white rounded p-4 mb-4">
        <div class="text-lg font-medium mb-2">总能耗(tCO₂e)</div>
        <div class="flex items-end">
          <div class="text-2xl font-bold mr-4">19,981.39</div>
          <div class="text-green-500 text-sm flex items-center">
            <span class="mr-1">同比:</span>
            <span>36%</span>
            <UpOutlined class="ml-1" />
          </div>
        </div>
      </div>

      <!-- 分项能耗占比 -->
      <div class="bg-white rounded p-4 mb-4">
        <div class="flex items-center justify-between mb-4">
          <div class="text-base font-medium">分项能耗占比</div>
          <div class="flex items-center">
            <a-radio-group v-model:value="timeUnit" button-style="solid" size="small">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
            <a-date-picker
              v-model:value="selectedDate"
              :picker="timeUnit === 'year' ? 'year' : timeUnit === 'month' ? 'month' : 'date'"
              size="small"
              class="ml-2"
              style="width: 120px"
            />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <!-- 能耗趋势图 -->
          <div class="h-80">
            <ConsumptionTrend :chartData="trendChartData" />
          </div>
          <!-- 能耗占比图 -->
          <div class="h-80">
            <ConsumptionDistribution :chartData="distributionChartData" />
          </div>
        </div>
      </div>

      <!-- 分项能耗明细 -->
      <div class="bg-white rounded p-4">
        <div class="text-base font-medium mb-4">分项能耗明细</div>
        <div class="grid grid-cols-4 gap-4">
          <!-- 清洁能源 -->
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">清洁能源</div>
            <div class="text-lg font-medium">359.16</div>
            <div class="text-xs text-gray-500">占比: 1.80%</div>
          </div>
          <!-- 常规能源 -->
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">常规能源</div>
            <div class="text-lg font-medium">19,622.23</div>
            <div class="text-xs text-gray-500">占比: 98.20%</div>
          </div>
          <!-- 其他能源 -->
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">其他能源</div>
            <div class="text-lg font-medium">0</div>
            <div class="text-xs text-gray-500">占比: 0.00%</div>
          </div>
        </div>
      </div>

      <!-- 能耗趋势分析表格 -->
      <div class="bg-white rounded p-4 mt-4">
        <div class="flex items-center justify-between mb-4">
          <div class="text-base font-medium">能耗趋势分析</div>
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
import { ref } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { TableColumnsType } from 'ant-design-vue';
import { UpOutlined } from '@ant-design/icons-vue';
import dayjs, { Dayjs } from 'dayjs';
import ConsumptionTrend from './components/ConsumptionTrend.vue';
import ConsumptionDistribution from './components/ConsumptionDistribution.vue';

// 搜索文本
const searchText = ref('');

// 树形菜单展开和选中状态
const expandedKeys = ref<string[]>(['1']);
const selectedKeys = ref<string[]>(['1-1']);

// 树形菜单数据
const treeData = ref<TreeDataItem[]>([
  {
    title: '分厂1',
    key: '1',
    children: [
      {
        title: '1号生产线',
        key: '1-1',
      },
      {
        title: '2号生产线',
        key: '1-2',
      }
    ]
  },
  {
    title: '分厂2',
    key: '2',
    children: [
      {
        title: '1号生产线',
        key: '2-1',
      }
    ]
  }
]);

// 时间单位选择
const timeUnit = ref('month');
const selectedDate = ref<Dayjs>(dayjs());

// 趋势图数据
const trendChartData = ref({
  xAxis: {
    type: 'category',
    data: ['2024-01', '2024-02']
  },
  series: [
    {
      name: '第一季度',
      type: 'line',
      data: [10000, 8000],
      areaStyle: {}
    },
    {
      name: '第二季度',
      type: 'line',
      data: [9000, 7500],
      areaStyle: {}
    }
  ]
});

// 分布图���据
const distributionChartData = ref({
  series: [
    {
      name: '能耗分布',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 4517.92, name: '清洁能源', percentage: 52.9 },
        { value: 3569.25, name: '常规能源', percentage: 45.3 },
        { value: 152.34, name: '其他能源', percentage: 1.8 }
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
    title: '清洁能源(tCO₂e)',
    dataIndex: 'cleanEnergy',
    width: 150,
    align: 'right',
  },
  {
    title: '常规能源(tCO₂e)',
    dataIndex: 'conventionalEnergy',
    width: 150,
    align: 'right',
  },
  {
    title: '其他能源(tCO₂e)',
    dataIndex: 'otherEnergy',
    width: 150,
    align: 'right',
  },
  {
    title: '合计(tCO₂e)',
    dataIndex: 'total',
    width: 150,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-02',
    cleanEnergy: 359.16,
    conventionalEnergy: 19622.23,
    otherEnergy: 0,
    total: 19981.39
  },
  {
    key: '2',
    time: '2024-01',
    cleanEnergy: 352.14,
    conventionalEnergy: 19245.78,
    otherEnergy: 0,
    total: 19597.92
  }
]);

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选中节点更新数据
};
</script>

<style scoped>
.h-full {
  min-height: calc(100vh - 100px);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  @apply w-1;
}

::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded;
}

/* 树形菜单样式 */
:deep(.ant-tree) {
  font-size: 13px;
}

/* 按钮组样式 */
:deep(.ant-radio-group) {
  font-size: 13px;
}

/* 搜索框样式 */
:deep(.ant-input-search) {
  font-size: 13px;
}

/* 表格样式 */
:deep(.ant-table) {
  font-size: 13px;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-weight: 500;
}
</style> 