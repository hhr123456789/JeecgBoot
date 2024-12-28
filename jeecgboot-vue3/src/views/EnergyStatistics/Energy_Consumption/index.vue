<template>
  <div class="energy-consumption-container flex">
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
            <a-radio-button value="custom">自定义</a-radio-button>
          </a-radio-group>
          <a-date-picker 
            v-model:value="selectedDate"
            :format="dateFormat"
            class="w-40"
          />
          <a-select v-model:value="energyType" class="w-40">
            <a-select-option value="cumulative">累计用电量</a-select-option>
            <a-select-option value="performance">性能系数</a-select-option>
          </a-select>
          <a-button type="primary">查询</a-button>
          <a-button>对比设置</a-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="bg-white rounded p-4 mb-4">
        <div class="text-lg font-medium mb-4">本期累计用电量</div>
        <div class="text-3xl font-bold text-blue-600">3361.46</div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 gap-4">
        <!-- 用电量趋势图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">用电量趋势</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: kWh</span>
            </div>
          </div>
          <ConsumptionTrend :chartData="trendData" />
        </div>

        <!-- 性能系数趋势图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">性能系数趋势</span>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">单位: -</span>
            </div>
          </div>
          <PerformanceTrend :chartData="performanceData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import ConsumptionTrend from './components/ConsumptionTrend.vue';
import PerformanceTrend from './components/PerformanceTrend.vue';

// 部门树相关
const searchText = ref<string>('');
const selectedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>(['1']);

// 部门树数据
const treeData = ref([
  {
    title: '制冷系统',
    key: '1',
    children: [
      {
        title: '1#水冷冷水机',
        key: '1-1',
      },
      {
        title: '2#水冷冷水机',
        key: '1-2',
      },
      {
        title: '3#水冷冷水机',
        key: '1-3',
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
const timeUnit = ref<'day' | 'month' | 'year' | 'custom'>('day');
const selectedDate = ref<Dayjs>(dayjs());
const energyType = ref<string>('cumulative');

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

// 用电量趋势数据
const trendData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', 
           '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', 
           '18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
  },
  series: [
    {
      name: '制冷系统',
      type: 'bar',
      data: [30, 35, 32, 40, 180, 270, 250, 320, 170, 280, 110, 270, 160, 230, 210, 
             90, 350, 210, 0, 0, 0, 0, 0, 0],
      itemStyle: {
        color: '#4B7BE5'
      }
    }
  ],
  markLine: {
    data: [
      {
        type: 'average',
        name: '历史均值',
        lineStyle: {
          color: '#87CEEB',
          type: 'dashed'
        }
      }
    ]
  }
});

// 性能系数趋势数据
const performanceData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', 
           '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', 
           '18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
  },
  series: [
    {
      name: '制冷系统',
      type: 'line',
      data: [3.5, 3.6, 3.5, 3.7, 3.5, 3.6, 3.5, 3.6, 3.7, 3.5, 3.6, 3.5, 3.6, 3.5, 3.6,
             3.7, 3.5, 3.6, 3.5, 3.6, 3.5, 3.6, 3.5, 3.6],
      lineStyle: {
        color: '#4B7BE5'
      },
      itemStyle: {
        color: '#4B7BE5'
      }
    },
    {
      name: '额定COP',
      type: 'line',
      data: Array(24).fill(5.0),
      lineStyle: {
        type: 'dashed',
        color: '#87CEEB'
      }
    },
    {
      name: '建议COP',
      type: 'line',
      data: Array(24).fill(4.5),
      lineStyle: {
        type: 'dashed',
        color: '#FFA500'
      }
    }
  ]
});
</script>

<style scoped>
.energy-consumption-container {
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
</style> 