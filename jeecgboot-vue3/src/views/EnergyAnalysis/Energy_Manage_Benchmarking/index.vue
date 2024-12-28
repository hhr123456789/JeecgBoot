<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-64 bg-white p-2 mr-2 rounded overflow-auto">
      <!-- 搜索框 -->
      <a-input-search
        v-model:value="searchText"
        placeholder="请输入搜索内容"
        class="mb-2"
      />
      <!-- 部门树形选择 -->
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
        <div class="text-lg font-medium mb-2">能耗对标分析</div>
        <div class="grid grid-cols-3 gap-4">
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">平均能耗强度指标（kgce/㎡）</div>
            <div class="text-2xl font-bold">3</div>
          </div>
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">最优能耗强度指标（kgce/㎡）</div>
            <div class="text-2xl font-bold">2</div>
          </div>
          <div class="bg-gray-50 rounded p-3">
            <div class="text-sm text-gray-600 mb-2">方差系数标准差除以均值(kWh/㎡)</div>
            <div class="text-2xl font-bold">4.4541</div>
          </div>
        </div>
      </div>

      <!-- 能耗对标趋势图 -->
      <div class="bg-white rounded p-4 mb-4">
        <div class="flex items-center justify-between mb-4">
          <div class="text-base font-medium">能耗对标趋势</div>
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
        <div class="h-80">
          <BenchmarkTrend :chartData="trendChartData" />
        </div>
      </div>

      <!-- 能耗对标分布 -->
      <div class="bg-white rounded p-4">
        <div class="flex items-center justify-between mb-4">
          <div class="text-base font-medium">能耗对标分布</div>
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
import { ref, onMounted, onUnmounted } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import BenchmarkTrend from './components/BenchmarkTrend.vue';

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
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  yAxis: {
    type: 'value',
    name: 'kgce/㎡'
  },
  series: [
    {
      name: '本部门',
      type: 'line',
      data: [4.5, 4.2, 4.8, 4.3, 4.6, 4.4],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '对标部门',
      type: 'line',
      data: [3.8, 3.9, 4.1, 3.7, 3.8, 3.9],
      itemStyle: {
        color: '#52c41a'
      }
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '部门',
    dataIndex: 'department',
    width: 150,
  },
  {
    title: '能耗强度(kgce/㎡)',
    dataIndex: 'intensity',
    width: 150,
    align: 'right',
  },
  {
    title: '能耗总量(tce)',
    dataIndex: 'total',
    width: 150,
    align: 'right',
  },
  {
    title: '建筑面积(㎡)',
    dataIndex: 'area',
    width: 150,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    department: '分厂1-1号生产线',
    intensity: 4.5,
    total: 450,
    area: 10000
  },
  {
    key: '2',
    department: '分厂1-2号生产线',
    intensity: 3.8,
    total: 380,
    area: 10000
  },
  {
    key: '3',
    department: '分厂2-1号生产线',
    intensity: 4.1,
    total: 410,
    area: 10000
  }
]);

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选择的部门更新数据
};

// 生命周期钩子
onMounted(() => {
  // TODO: 初始化数据加载
});

onUnmounted(() => {
  // TODO: 清理工作
});
</script> 