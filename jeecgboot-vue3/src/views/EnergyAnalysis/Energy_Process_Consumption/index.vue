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
          <a-select
            v-model:value="selectedEnergyType"
            :options="energyTypeOptions"
            size="small"
            class="w-24"
            placeholder="能源类型"
          />
          <a-button type="primary" size="small" :loading="loading" @click="handleQuery">查询</a-button>
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
import { ref, onMounted, computed } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { message } from 'ant-design-vue';
import ProcessPie from './components/ProcessPie.vue';
import ProcessLine from './components/ProcessLine.vue';
import {
  getProductionLineTree,
  getProcessEnergyStatistics,
  getProcessEnergyPieData,
  getProcessEnergyTrendData,
  getProcessEnergyTableData,
} from './process-energy.api';

// 加载状态
const loading = ref(false);

// 搜索文本
const searchText = ref('');

// 树形菜单展开和选中状态
const expandedKeys = ref<string[]>([]);
const selectedKeys = ref<string[]>([]);

// 当前选中的生产线ID
const selectedLineId = ref<string>('');

// 树形菜单数据
const treeData = ref<TreeDataItem[]>([]);

// 时间单位选择
const timeUnit = ref('month');
const selectedDate = ref<Dayjs>(dayjs());

// 能源类型选项
const energyTypeOptions = ref([
  { label: '电', value: 'electricity' },
  { label: '水', value: 'water' },
  { label: '气', value: 'gas' },
  { label: '蒸汽', value: 'steam' },
]);
const selectedEnergyType = ref('electricity');

// 统计数据
interface StatisticsData {
  totalConsumption: number;       // 总能耗
  productionConsumption: number;  // 生产用能
  auxiliaryConsumption: number;   // 辅助用能
  unitConsumption: number;        // 单位产品能耗
}

// 统计数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 0,
  productionConsumption: 0,
  auxiliaryConsumption: 0,
  unitConsumption: 0
});

// 饼图数据
const pieChartData = ref({
  series: [
    {
      name: '过程能耗分布',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      data: [] as { value: number; name: string }[]
    }
  ]
});

// 折线图数据
const lineChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [] as { name: string; type: 'line'; data: number[] }[]
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
const tableData = ref<any[]>([]);

// 获取查询参数
const getQueryParams = () => {
  let dateStr = '';
  if (timeUnit.value === 'year') {
    dateStr = selectedDate.value.format('YYYY');
  } else if (timeUnit.value === 'month') {
    dateStr = selectedDate.value.format('YYYY-MM');
  } else {
    dateStr = selectedDate.value.format('YYYY-MM-DD');
  }
  return {
    lineId: selectedLineId.value,
    timeUnit: timeUnit.value,
    date: dateStr,
    energyType: selectedEnergyType.value,
  };
};

// 加载生产线树数据
const loadTreeData = async () => {
  try {
    const res = await getProductionLineTree();
    if (res.success && res.result) {
      treeData.value = res.result;
      // 默认选中第一个叶子节点
      if (res.result.length > 0) {
        const firstNode = res.result[0];
        expandedKeys.value = [firstNode.key];
        if (firstNode.children && firstNode.children.length > 0) {
          selectedKeys.value = [firstNode.children[0].key];
          selectedLineId.value = firstNode.children[0].key;
        } else {
          selectedKeys.value = [firstNode.key];
          selectedLineId.value = firstNode.key;
        }
      }
    }
  } catch (error) {
    console.error('加载生产线数据失败:', error);
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const params = getQueryParams();
    const res = await getProcessEnergyStatistics(params);
    if (res.success && res.result) {
      statisticsData.value = res.result;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 加载饼图数据
const loadPieData = async () => {
  try {
    const params = getQueryParams();
    const res = await getProcessEnergyPieData(params);
    if (res.success && res.result) {
      pieChartData.value = {
        series: [
          {
            name: '过程能耗分布',
            type: 'pie' as const,
            radius: ['50%', '70%'],
            data: res.result
          }
        ]
      };
    }
  } catch (error) {
    console.error('加载饼图数据失败:', error);
  }
};

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const params = getQueryParams();
    const res = await getProcessEnergyTrendData(params);
    if (res.success && res.result) {
      lineChartData.value = {
        xAxis: {
          type: 'category' as const,
          data: res.result.xAxisData || []
        },
        series: res.result.series || []
      };
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error);
  }
};

// 加载表格数据
const loadTableData = async () => {
  try {
    const params = getQueryParams();
    const res = await getProcessEnergyTableData(params);
    if (res.success && res.result) {
      tableData.value = res.result;
    }
  } catch (error) {
    console.error('加载表格数据失败:', error);
  }
};

// 查询数据
const handleQuery = async () => {
  if (!selectedLineId.value) {
    message.warning('请先选择生产线');
    return;
  }
  loading.value = true;
  try {
    await Promise.all([
      loadStatistics(),
      loadPieData(),
      loadTrendData(),
      loadTableData(),
    ]);
  } finally {
    loading.value = false;
  }
};

// 处理树节点选择
const handleSelect = (keys: string[], info: any) => {
  if (keys.length > 0) {
    selectedLineId.value = keys[0];
    handleQuery();
  }
};

// 页面初始化
onMounted(async () => {
  await loadTreeData();
  if (selectedLineId.value) {
    handleQuery();
  }
});
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