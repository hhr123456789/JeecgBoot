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
      <!-- 顶部查询区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex items-center space-x-4">
          <!-- 查询区间选择 -->
          <div class="flex items-center">
            <span class="text-gray-600 text-sm mr-2">查询区间：</span>
            <a-select
              v-model:value="queryType"
              style="width: 160px"
              size="small"
              @change="handleQueryTypeChange"
            >
              <a-select-option value="month">按月查询</a-select-option>
              <a-select-option value="monthRange">按月区间查询</a-select-option>
              <a-select-option value="year">按年查询</a-select-option>
              <a-select-option value="yearRange">按年区间查询</a-select-option>
            </a-select>
          </div>

          <!-- 时间选择区域 -->
          <div class="flex items-center space-x-4">
            <!-- 单月选择 -->
            <a-month-picker
              v-if="queryType === 'month'"
              v-model:value="singleMonth"
              format="YYYY-MM"
              size="small"
              :placeholder="'请选择月份'"
              style="width: 120px"
            />

            <!-- 月区间选择 -->
            <template v-if="queryType === 'monthRange'">
              <a-month-picker
                v-model:value="monthRange[0]"
                format="YYYY-MM"
                size="small"
                :placeholder="'起始月份'"
                style="width: 120px"
              />
              <span class="text-gray-400">至</span>
              <a-month-picker
                v-model:value="monthRange[1]"
                format="YYYY-MM"
                size="small"
                :placeholder="'结束月份'"
                style="width: 120px"
              />
            </template>

            <!-- 单年选择 -->
            <a-date-picker
              v-if="queryType === 'year'"
              v-model:value="singleYear"
              picker="year"
              size="small"
              :placeholder="'请选择年份'"
              style="width: 120px"
            />

            <!-- 年区间选择 -->
            <template v-if="queryType === 'yearRange'">
              <a-date-picker
                v-model:value="yearRange[0]"
                picker="year"
                size="small"
                :placeholder="'起始年份'"
                style="width: 120px"
              />
              <span class="text-gray-400">至</span>
              <a-date-picker
                v-model:value="yearRange[1]"
                picker="year"
                size="small"
                :placeholder="'结束年份'"
                style="width: 120px"
              />
            </template>
          </div>

          <!-- 查询按钮 -->
          <a-button type="primary" size="small" @click="handleQuery">查询</a-button>
        </div>
      </div>

      <!-- 顶部统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 总能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.totalConsumption }}
          </div>
        </div>
        <!-- 峰时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">峰时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.peakConsumption }}
          </div>
        </div>
        <!-- 谷时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">谷时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.valleyConsumption }}
          </div>
        </div>
        <!-- 峰谷差率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">峰谷差率(%)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.peakValleyRatio }}
          </div>
        </div>
      </div>

      <!-- 饼图和柱状图区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">能耗分布</div>
          <ConsumptionPie :chartData="pieChartData" />
        </div>
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">能耗趋势</div>
          <ConsumptionBar :chartData="barChartData" />
        </div>
      </div>

      <!-- 折线图区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex items-center justify-between mb-2">
          <div class="text-gray-600 text-sm">能耗分析</div>
          <a-radio-group v-model:value="timeRange" button-style="solid" size="small">
            <a-radio-button value="day">日</a-radio-button>
            <a-radio-button value="month">月</a-radio-button>
            <a-radio-button value="year">年</a-radio-button>
          </a-radio-group>
        </div>
        <ConsumptionLine :chartData="lineChartData" />
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-3">
        <div class="flex items-center justify-between mb-3">
          <div class="text-gray-600 text-sm">能耗数据明细</div>
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
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { TableColumnsType } from 'ant-design-vue';
import ConsumptionPie from './components/ConsumptionPie.vue';
import ConsumptionBar from './components/ConsumptionBar.vue';
import ConsumptionLine from './components/ConsumptionLine.vue';
import { Dayjs } from 'dayjs';

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

// 时间范围选择
const timeRange = ref('day');

// 查询类型
const queryType = ref('month');

// 时间选择值
const singleMonth = ref<Dayjs | null>(null);
const monthRange = ref<[Dayjs | null, Dayjs | null]>([null, null]);
const singleYear = ref<Dayjs | null>(null);
const yearRange = ref<[Dayjs | null, Dayjs | null]>([null, null]);

// 统计数据
interface StatisticsData {
  totalConsumption: number;    // 总能耗
  peakConsumption: number;     // 峰时能耗
  valleyConsumption: number;   // 谷时能耗
  peakValleyRatio: number;     // 峰谷差率
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 8668433.80,
  peakConsumption: 5424683.40,
  valleyConsumption: 3243750.40,
  peakValleyRatio: 40.23
});

// 饼图数据
const pieChartData = ref({
  series: [
    {
      name: '能耗分布',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 1424683.40, name: '尖峰能耗' },
        { value: 2067865.00, name: '峰时能耗' },
        { value: 3161614.60, name: '平时能耗' },
        { value: 2014444.80, name: '谷时能耗' }
      ]
    }
  ]
});

// 柱状图数据
const barChartData = ref({
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  series: [
    {
      name: '能耗量',
      type: 'bar',
      data: [320, 280, 250, 340, 360, 320]
    }
  ]
});

// 折线图数据
const lineChartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', 
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '尖峰',
      type: 'line',
      data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330]
    },
    {
      name: '峰时',
      type: 'line',
      data: [220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149]
    },
    {
      name: '平时',
      type: 'line',
      data: [150, 232, 201, 154, 190, 330, 410, 182, 191, 234, 290, 330]
    },
    {
      name: '谷时',
      type: 'line',
      data: [320, 332, 301, 334, 390, 330, 320, 132, 142, 244, 190, 130]
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
    title: '总能耗(kWh)',
    dataIndex: 'totalConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '尖峰能耗(kWh)',
    dataIndex: 'peakConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '峰时能耗(kWh)',
    dataIndex: 'highConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '平时能耗(kWh)',
    dataIndex: 'normalConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '谷时能耗(kWh)',
    dataIndex: 'valleyConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '峰谷差率(%)',
    dataIndex: 'peakValleyRatio',
    width: 120,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-01-16',
    totalConsumption: 326061.00,
    peakConsumption: 98234.50,
    highConsumption: 89234.20,
    normalConsumption: 78234.80,
    valleyConsumption: 60357.50,
    peakValleyRatio: 38.56
  },
  {
    key: '2',
    time: '2024-01-17',
    totalConsumption: 313371.00,
    peakConsumption: 95234.30,
    highConsumption: 85234.40,
    normalConsumption: 75234.80,
    valleyConsumption: 57667.50,
    peakValleyRatio: 39.45
  }
]);

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选中节点更新数据
};

// 处理查询类型变化
const handleQueryTypeChange = (value: string) => {
  // 重置所有时间选择
  singleMonth.value = null;
  monthRange.value = [null, null];
  singleYear.value = null;
  yearRange.value = [null, null];
};

// 处理查询
const handleQuery = () => {
  let queryParams = {
    type: queryType.value,
    time: null as any
  };

  switch (queryType.value) {
    case 'month':
      queryParams.time = singleMonth.value?.format('YYYY-MM');
      break;
    case 'monthRange':
      queryParams.time = monthRange.value.map(date => date?.format('YYYY-MM'));
      break;
    case 'year':
      queryParams.time = singleYear.value?.format('YYYY');
      break;
    case 'yearRange':
      queryParams.time = yearRange.value.map(date => date?.format('YYYY'));
      break;
  }

  console.log('查询参数：', queryParams);
  // TODO: 根据查询参数更新数据
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