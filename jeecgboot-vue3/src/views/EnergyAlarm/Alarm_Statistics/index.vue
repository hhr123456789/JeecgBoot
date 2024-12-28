<template>
  <div class="alarm-statistics-container flex">
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
          <a-select v-model:value="alarmType" class="w-40">
            <a-select-option value="all">全部告警</a-select-option>
            <a-select-option value="overload">超负荷</a-select-option>
            <a-select-option value="abnormal">异常运行</a-select-option>
            <a-select-option value="fault">故障</a-select-option>
          </a-select>
          <a-button type="primary">查询</a-button>
        </div>
      </div>

      <!-- 告警统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">总告警数</div>
          <div class="text-2xl font-bold">{{ statisticsData.totalAlarms }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: 次</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">超负荷告警</div>
          <div class="text-2xl font-bold text-orange-500">{{ statisticsData.overloadAlarms }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: 次</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">异常运行告警</div>
          <div class="text-2xl font-bold text-yellow-500">{{ statisticsData.abnormalAlarms }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: 次</div>
        </div>
        <div class="bg-white rounded-lg p-4">
          <div class="text-gray-600 mb-2">故障告警</div>
          <div class="text-2xl font-bold text-red-500">{{ statisticsData.faultAlarms }}</div>
          <div class="text-sm text-gray-500 mt-1">单位: 次</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <!-- 告警趋势图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">告警趋势</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: 次</span>
            </div>
          </div>
          <AlarmTrend :chartData="trendData" />
        </div>

        <!-- 告警类型分布图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">告警类型分布</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: %</span>
            </div>
          </div>
          <AlarmDistribution :chartData="distributionData" />
        </div>
      </div>

      <!-- 告警排名 TOP10 -->
      <div class="bg-white rounded p-4">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium">告警设备排名 TOP10</span>
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
import AlarmTrend from './components/AlarmTrend.vue';
import AlarmDistribution from './components/AlarmDistribution.vue';

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
const alarmType = ref<string>('all');

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
  totalAlarms: number;
  overloadAlarms: number;
  abnormalAlarms: number;
  faultAlarms: number;
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  totalAlarms: 156,
  overloadAlarms: 68,
  abnormalAlarms: 45,
  faultAlarms: 43
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
      name: '超负荷',
      type: 'line' as const,
      data: [5, 8, 6, 7, 9, 8, 6, 4, 5, 3, 4, 3],
      itemStyle: {
        color: '#FF9F40'
      }
    },
    {
      name: '异常运行',
      type: 'line' as const,
      data: [3, 4, 5, 4, 3, 5, 4, 3, 4, 5, 3, 2],
      itemStyle: {
        color: '#FACC14'
      }
    },
    {
      name: '故障',
      type: 'line' as const,
      data: [2, 3, 4, 3, 4, 5, 4, 3, 4, 3, 4, 4],
      itemStyle: {
        color: '#FF4D4F'
      }
    }
  ]
});

// 分布图数据
const distributionData = ref({
  series: [
    {
      name: '告警类型分布',
      type: 'pie' as const,
      radius: ['50%', '70%'],
      data: [
        { value: 43.59, name: '超负荷' },
        { value: 28.85, name: '异常运行' },
        { value: 27.56, name: '故障' }
      ]
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '排名',
    dataIndex: 'rank',
    width: 80,
    align: 'center',
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    width: 200,
  },
  {
    title: '所属部门',
    dataIndex: 'department',
    width: 150,
  },
  {
    title: '告警次数',
    dataIndex: 'alarmCount',
    width: 100,
    align: 'right',
  },
  {
    title: '超负荷',
    dataIndex: 'overload',
    width: 100,
    align: 'right',
  },
  {
    title: '异常运行',
    dataIndex: 'abnormal',
    width: 100,
    align: 'right',
  },
  {
    title: '故障',
    dataIndex: 'fault',
    width: 100,
    align: 'right',
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    rank: 1,
    deviceName: '1#空压机',
    department: '一号车间',
    alarmCount: 25,
    overload: 12,
    abnormal: 8,
    fault: 5
  },
  {
    key: '2',
    rank: 2,
    deviceName: '2#空压机',
    department: '一号车间',
    alarmCount: 22,
    overload: 10,
    abnormal: 7,
    fault: 5
  },
  {
    key: '3',
    rank: 3,
    deviceName: '1#冷水机组',
    department: '二号车间',
    alarmCount: 18,
    overload: 8,
    abnormal: 6,
    fault: 4
  }
]);
</script>

<style scoped>
.alarm-statistics-container {
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