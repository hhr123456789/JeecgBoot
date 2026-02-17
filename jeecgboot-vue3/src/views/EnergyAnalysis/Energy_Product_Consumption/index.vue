<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-64 bg-white p-2 mr-2 rounded overflow-auto">
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索产品类型"
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
          <a-select v-model:value="energyType" size="small" class="w-28" placeholder="能源类型">
            <a-select-option :value="null">全部能源</a-select-option>
            <a-select-option :value="1">电</a-select-option>
            <a-select-option :value="2">水</a-select-option>
            <a-select-option :value="3">天然气</a-select-option>
            <a-select-option :value="4">蒸汽</a-select-option>
            <a-select-option :value="5">压缩空气</a-select-option>
          </a-select>
          <a-button type="primary" size="small" @click="handleQuery">查询</a-button>
        </div>
      </div>

      <!-- 数据卡片区域 -->
      <div class="grid grid-cols-5 gap-4 mb-4">
        <!-- 总能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总能耗({{ energyUnit }})</div>
          <div class="bg-blue-50 rounded-lg py-2 px-3 text-base font-medium text-center text-blue-600">
            {{ statisticsData.totalConsumption }}
          </div>
        </div>
        <!-- 总产量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">总产量(件)</div>
          <div class="bg-green-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-600">
            {{ statisticsData.totalProduction }}
          </div>
        </div>
        <!-- 合格产量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">合格产量(件)</div>
          <div class="bg-green-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-600">
            {{ statisticsData.qualifiedProduction }}
          </div>
        </div>
        <!-- 合格率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">合格率(%)</div>
          <div class="bg-yellow-50 rounded-lg py-2 px-3 text-base font-medium text-center text-yellow-600">
            {{ statisticsData.qualificationRate }}
          </div>
        </div>
        <!-- 单位产品能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">单位产品能耗(kWh/件)</div>
          <div class="bg-red-50 rounded-lg py-2 px-3 text-base font-medium text-center text-red-600">
            {{ statisticsData.unitConsumption }}
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <!-- 产品能耗分布 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm font-medium mb-2">产品能耗分布</div>
          <div class="h-80">
            <ProductPie :chartData="pieChartData" />
          </div>
        </div>
        <!-- 产品单耗趋势 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm font-medium mb-2">产品单耗趋势</div>
          <div class="h-80">
            <ProductLine :chartData="lineChartData" />
          </div>
        </div>
      </div>

      <!-- 第二行图表 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <!-- 产品产量与能耗对比 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm font-medium mb-2">产量与能耗对比</div>
          <div class="h-80">
            <ProductBar :chartData="barChartData" />
          </div>
        </div>
        <!-- 产品单耗排名 -->
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm font-medium mb-2">产品单耗排名(kWh/件)</div>
          <div class="h-80">
            <ProductRanking :chartData="rankingChartData" />
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-3">
        <div class="flex items-center justify-between mb-3">
          <div class="text-gray-600 text-sm font-medium">产品单耗明细数据</div>
          <a-button type="primary" size="small" @click="handleExport">导出数据</a-button>
        </div>
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="pagination"
          :loading="loading"
          size="middle"
          @change="handleTableChange"
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
import ProductPie from './components/ProductPie.vue';
import ProductLine from './components/ProductLine.vue';
import ProductBar from './components/ProductBar.vue';
import ProductRanking from './components/ProductRanking.vue';
import {
  getProductStatistics,
  getProductDistribution,
  getProductTrend,
  getProductComparison,
  getProductRanking,
  getProductDetailList,
  getProductCategoryTree
} from '/@/api/energy/productEnergy';

// 搜索文本
const searchText = ref('');

// Loading状态
const loading = ref(false);

// 树形菜单展开和选中状态
const expandedKeys = ref<string[]>([]);
const selectedKeys = ref<string[]>([]);

// 树形菜单数据 - 从后端加载
const treeData = ref<TreeDataItem[]>([]);

// 时间单位选择
const timeUnit = ref('month');
const selectedDate = ref<Dayjs>(dayjs());

// 能源类型选择
const energyType = ref<number | null>(1); // 默认电

// 能源单位计算
const energyUnit = computed(() => {
  const unitMap: Record<number, string> = {
    1: 'kWh',
    2: 'm³',
    3: 'm³',
    4: 't',
    5: 'm³'
  };
  return energyType.value ? unitMap[energyType.value] || 'kWh' : 'kWh';
});

// 统计数据接口
interface StatisticsData {
  totalConsumption: number;       // 总能耗
  totalProduction: number;        // 总产量
  qualifiedProduction: number;    // 合格产量
  qualificationRate: number;      // 合格率
  unitConsumption: number;        // 单位产品能耗
}

// 静态统计数据 -> 改为动态数据
const statisticsData = ref<StatisticsData>({
  totalConsumption: 0,
  totalProduction: 0,
  qualifiedProduction: 0,
  qualificationRate: 0,
  unitConsumption: 0
});

// 饼图数据 - 产品能耗分布
const pieChartData = ref({
  series: [
    {
      name: '产品能耗分布',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      data: []
    }
  ]
});

// 折线图数据 - 产品单耗趋势
const lineChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [] as { name: string; type: 'line'; data: number[] }[]
});

// 柱状图数据 - 产量与能耗对比
const barChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [
    {
      name: '产量(件)',
      type: 'bar' as const,
      yAxisIndex: 0,
      data: [] as number[]
    },
    {
      name: '能耗(万kWh)',
      type: 'bar' as const,
      yAxisIndex: 1,
      data: [] as number[]
    }
  ]
});

// 排名柱状图数据 - 产品单耗排名
const rankingChartData = ref({
  yAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [
    {
      name: '单位产品能耗',
      type: 'bar' as const,
      data: [] as number[]
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '时间',
    dataIndex: 'time',
    width: 100,
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    width: 120,
  },
  {
    title: '产量(件)',
    dataIndex: 'production',
    width: 100,
    align: 'right',
  },
  {
    title: '合格量(件)',
    dataIndex: 'qualified',
    width: 100,
    align: 'right',
  },
  {
    title: '合格率(%)',
    dataIndex: 'qualificationRate',
    width: 100,
    align: 'right',
  },
  {
    title: '总能耗(kWh)',
    dataIndex: 'totalConsumption',
    width: 120,
    align: 'right',
  },
  {
    title: '单位产品能耗(kWh/件)',
    dataIndex: 'unitConsumption',
    width: 150,
    align: 'right',
  },
  {
    title: '环比',
    dataIndex: 'chainRatio',
    width: 100,
    align: 'right',
    customRender: ({ text }) => {
      const value = parseFloat(text);
      const color = value >= 0 ? 'text-red-500' : 'text-green-500';
      return `<span class="${color}">${value >= 0 ? '+' : ''}${text}%</span>`;
    }
  }
];

// 表格数据
const tableData = ref<any[]>([]);

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

// 获取查询参数
const getQueryParams = () => {
  let startDate = '';
  let endDate = '';
  const date = selectedDate.value;

  if (timeUnit.value === 'day') {
    startDate = date.format('YYYY-MM-DD');
    endDate = date.format('YYYY-MM-DD');
  } else if (timeUnit.value === 'month') {
    startDate = date.startOf('month').format('YYYY-MM-DD');
    endDate = date.endOf('month').format('YYYY-MM-DD');
  } else {
    startDate = date.startOf('year').format('YYYY-MM-DD');
    endDate = date.endOf('year').format('YYYY-MM-DD');
  }

  return {
    timeDimension: timeUnit.value,
    startDate,
    endDate,
    energyType: energyType.value,
    categoryId: selectedKeys.value.length > 0 ? selectedKeys.value[0] : null
  };
};

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const res = await getProductCategoryTree();
    if (res) {
      treeData.value = res;
      // 默认展开第一级
      if (res.length > 0) {
        expandedKeys.value = res.map((item: any) => item.key);
      }
    }
  } catch (error) {
    console.error('加载分类树失败:', error);
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const params = getQueryParams();
    const res = await getProductStatistics(params);
    if (res) {
      statisticsData.value = {
        totalConsumption: res.totalConsumption || 0,
        totalProduction: res.totalProduction || 0,
        qualifiedProduction: res.qualifiedProduction || 0,
        qualificationRate: res.qualificationRate || 0,
        unitConsumption: res.unitConsumption || 0
      };
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 加载饼图数据
const loadDistribution = async () => {
  try {
    const params = getQueryParams();
    const res = await getProductDistribution(params);
    if (res && res.series) {
      pieChartData.value = res;
    }
  } catch (error) {
    console.error('加载分布数据失败:', error);
  }
};

// 加载趋势数据
const loadTrend = async () => {
  try {
    const params = getQueryParams();
    const res = await getProductTrend(params);
    if (res) {
      lineChartData.value = {
        xAxis: res.xAxis || { type: 'category', data: [] },
        series: res.series || []
      };
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error);
  }
};

// 加载对比数据
const loadComparison = async () => {
  try {
    const params = getQueryParams();
    const res = await getProductComparison(params);
    if (res) {
      barChartData.value = {
        xAxis: res.xAxis || { type: 'category', data: [] },
        series: res.series || []
      };
    }
  } catch (error) {
    console.error('加载对比数据失败:', error);
  }
};

// 加载排名数据
const loadRanking = async () => {
  try {
    const params = getQueryParams();
    const res = await getProductRanking({ ...params, order: 'asc' });
    if (res) {
      rankingChartData.value = {
        yAxis: res.yAxis || { type: 'category', data: [] },
        series: res.series || []
      };
    }
  } catch (error) {
    console.error('加载排名数据失败:', error);
  }
};

// 加载表格数据
const loadDetailList = async () => {
  try {
    const params = {
      ...getQueryParams(),
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize
    };
    const res = await getProductDetailList(params);
    if (res) {
      tableData.value = res.records || [];
      pagination.value.total = res.total || 0;
    }
  } catch (error) {
    console.error('加载明细数据失败:', error);
  }
};

// 加载所有数据
const loadAllData = async () => {
  loading.value = true;
  try {
    await Promise.all([
      loadStatistics(),
      loadDistribution(),
      loadTrend(),
      loadComparison(),
      loadRanking(),
      loadDetailList()
    ]);
  } finally {
    loading.value = false;
  }
};

// 处理树节点选择
const handleSelect = (keys: string[], info: any) => {
  console.log('selected', keys, info);
  // 选中节点后重新加载数据
  loadAllData();
};

// 处理查询
const handleQuery = () => {
  pagination.value.current = 1;
  loadAllData();
};

// 处理表格分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadDetailList();
};

// 处理导出
const handleExport = () => {
  message.info('导出功能开发中...');
};

// 页面加载时初始化
onMounted(() => {
  loadCategoryTree();
  loadAllData();
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
