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
        <div class="flex items-center space-x-4">
          <a-radio-group v-model:value="timeUnit" button-style="solid" size="small">
            <a-radio-button value="day">日</a-radio-button>
            <a-radio-button value="month">月</a-radio-button>
            <a-radio-button value="year">年</a-radio-button>
          </a-radio-group>
          <a-date-picker
            v-model:value="selectedDate"
            :picker="timeUnit === 'year' ? 'year' : timeUnit === 'month' ? 'month' : 'date'"
            size="small"
            class="w-32"
          />
          <a-button type="primary" size="small">查询</a-button>
        </div>
      </div>

      <!-- 数据卡片区域 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 总能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.totalConsumption }}
          </div>
        </div>
        <!-- 生产用能 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">生产用能(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.productionConsumption }}
          </div>
        </div>
        <!-- 辅助用能 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">辅助用能(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.auxiliaryConsumption }}
          </div>
        </div>
        <!-- 单位产品能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">单位产品能耗(kWh/件)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.unitConsumption }}
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <!-- 过程能耗分布 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">过程能耗分布</div>
          <div class="h-80">
            <ProcessPie :chartData="pieChartData" />
          </div>
        </div>
        <!-- 过程能耗趋势 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">过程能耗趋势</div>
          <div class="h-80">
            <ProcessLine :chartData="lineChartData" />
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-3">
        <div class="flex items-center justify-between mb-3">
          <div class="text-gray-600 text-sm">过程能耗数据</div>
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
import dayjs, { Dayjs } from 'dayjs';
import ProcessPie from './components/ProcessPie.vue';
import ProcessLine from './components/ProcessLine.vue';

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

// 统计数据
interface StatisticsData {
  totalConsumption: number;       // 总能耗
  productionConsumption: number;  // 生产用能
  auxiliaryConsumption: number;   // 辅助用能
  unitConsumption: number;        // 单位产品能耗
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 8668433.80,
  productionConsumption: 5424683.40,
  auxiliaryConsumption: 3243750.40,
  unitConsumption: 0.85
});

// 饼图数据
const pieChartData = ref({
  series: [
    {
      name: '过程能耗分布',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      data: [
        { value: 3424683.40, name: '主工艺过程' },
        { value: 2067865.00, name: '辅助工艺过程' },
        { value: 1961614.60, name: '公用工程系统' },
        { value: 1214444.80, name: '附属生产系统' }
      ]
    }
  ]
});

// 折线图数据
const lineChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  series: [
    {
      name: '主工艺过程',
      type: 'line' as const,
      data: [320, 332, 301, 334, 390, 330]
    },
    {
      name: '辅助工艺过程',
      type: 'line' as const,
      data: [220, 182, 191, 234, 290, 330]
    },
    {
      name: '公用工程系统',
      type: 'line' as const,
      data: [150, 232, 201, 154, 190, 330]
    },
    {
      name: '附属生产系统',
      type: 'line' as const,
      data: [98, 77, 101, 99, 140, 120]
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
    title: '主工艺过程(kWh)',
    dataIndex: 'mainProcess',
    width: 150,
    align: 'right',
  },
  {
    title: '辅助工艺过程(kWh)',
    dataIndex: 'auxiliaryProcess',
    width: 150,
    align: 'right',
  },
  {
    title: '公用工程系统(kWh)',
    dataIndex: 'utilitySystem',
    width: 150,
    align: 'right',
  },
  {
    title: '附属生产系统(kWh)',
    dataIndex: 'subsidiarySystem',
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
    time: '2024-01',
    mainProcess: 3424683.40,
    auxiliaryProcess: 2067865.00,
    utilitySystem: 1961614.60,
    subsidiarySystem: 1214444.80,
    total: 8668433.80
  },
  {
    key: '2',
    time: '2024-02',
    mainProcess: 3324683.40,
    auxiliaryProcess: 1967865.00,
    utilitySystem: 1861614.60,
    subsidiarySystem: 1114444.80,
    total: 8268433.80
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