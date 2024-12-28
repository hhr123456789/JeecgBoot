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
      <!-- 顶部筛选区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <!-- 时间范围选择 -->
            <a-radio-group v-model:value="timeRange" button-style="solid" size="small">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
            <!-- 基准期时间选择 -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2">基准期：</span>
              <a-range-picker 
                v-model:value="baseDateRange"
                :format="dateFormat"
                class="w-64"
              />
            </div>
            <!-- 对比期时间选择 -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2">对比期：</span>
              <a-range-picker 
                v-model:value="compareDateRange"
                :format="dateFormat"
                class="w-64"
              />
            </div>
          </div>
          <a-button type="primary" size="small">导出数据</a-button>
        </div>
      </div>

      <!-- 数据对比卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 基准期用电量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">基准期用电量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ compareData.baseConsumption }}
          </div>
        </div>
        <!-- 对比期用电量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">对比期用电量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ compareData.compareConsumption }}
          </div>
        </div>
        <!-- 节能量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">节能量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-500">
            {{ compareData.energySaving }}
          </div>
        </div>
        <!-- 节能率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">节能率 %</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-500">
            {{ compareData.savingRate }}
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <CompareChart :chartData="chartData" />
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-3">
        <div class="text-gray-600 text-sm mb-3">对比数据明细</div>
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
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import CompareChart from './components/CompareChart.vue';

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

// 时间范围和日期选择
const timeRange = ref('day');
const baseDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(14, 'day'), dayjs().subtract(7, 'day')]);
const compareDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(7, 'day'), dayjs()]);

// 日期格式
const dateFormat = computed(() => {
  switch (timeRange.value) {
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

// 对比数据
interface CompareData {
  baseConsumption: number;    // 基准期用电量
  compareConsumption: number; // 对比期用电量
  energySaving: number;       // 节能量
  savingRate: number;         // 节能率
}

// 静态对比数据
const compareData = ref<CompareData>({
  baseConsumption: 296.37,
  compareConsumption: 201.74,
  energySaving: 94.63,
  savingRate: 31.93
});

// 图表数据
const chartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '基准期',
      type: 'line',
      data: [320, 280, 250, 340, 360, 320, 380, 
             340, 320, 300, 340, 360],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '对比期',
      type: 'line',
      data: [280, 200, 220, 300, 340, 300, 340,
             300, 280, 260, 300, 320],
      itemStyle: {
        color: '#52c41a'
      }
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '时间',
    dataIndex: 'time',
    width: 180,
  },
  {
    title: '基准期用电量(kWh)',
    dataIndex: 'baseConsumption',
    width: 160,
    align: 'right',
  },
  {
    title: '对比期用电量(kWh)',
    dataIndex: 'compareConsumption',
    width: 160,
    align: 'right',
  },
  {
    title: '节能量(kWh)',
    dataIndex: 'energySaving',
    width: 140,
    align: 'right',
  },
  {
    title: '节能率(%)',
    dataIndex: 'savingRate',
    width: 120,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-01-16',
    baseConsumption: 326061.00,
    compareConsumption: 297241.00,
    energySaving: 28820.00,
    savingRate: 8.84
  },
  {
    key: '2',
    time: '2024-01-17',
    baseConsumption: 213371.00,
    compareConsumption: 279242.60,
    energySaving: -65871.60,
    savingRate: -30.87
  },
  {
    key: '3',
    time: '2024-01-18',
    baseConsumption: 288831.00,
    compareConsumption: 286104.20,
    energySaving: 2726.80,
    savingRate: 0.94
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

/* 更新数据值样式 */
.bg-gray-50 {
  background-color: #f9fafb;
}

/* 圆角大小 */
.rounded-lg {
  border-radius: 0.5rem;
}
</style> 