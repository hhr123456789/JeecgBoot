<template>
  <div class="team-energy-container flex">
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
            <a-select-option value="all">综合能耗</a-select-option>
            <a-select-option value="electric">电</a-select-option>
            <a-select-option value="water">水</a-select-option>
            <a-select-option value="gas">燃气</a-select-option>
            <a-select-option value="steam">蒸汽</a-select-option>
            <a-select-option value="compressed_air">压缩空气</a-select-option>
            <a-select-option value="cold">冷</a-select-option>
            <a-select-option value="hot">热</a-select-option>
            <a-select-option value="soft_water">软水</a-select-option>
            <a-select-option value="pure_water">纯水</a-select-option>
            <a-select-option value="industrial_water">工业水</a-select-option>
            <a-select-option value="nitrogen">氮气</a-select-option>
            <a-select-option value="oxygen">氧气</a-select-option>
            <a-select-option value="diesel">柴油</a-select-option>
          </a-select>
          <a-button type="primary">查询</a-button>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 gap-4">
        <!-- 班组能耗趋势图 -->
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium">班组能耗</span>
            <a-select v-model:value="shiftType" size="small" class="w-32">
              <a-select-option value="all">全部班组</a-select-option>
              <a-select-option value="a">A-1班</a-select-option>
              <a-select-option value="b">A-2班</a-select-option>
              <a-select-option value="c">B-1班</a-select-option>
            </a-select>
          </div>
          <TeamEnergyTrend :chartData="trendData" />
        </div>

        <!-- 班组能耗排名和占比 -->
        <div class="grid grid-cols-2 gap-4">
          <!-- 班组能耗排名 -->
          <div class="bg-white rounded p-4">
            <div class="text-base font-medium mb-4">班组用能排名</div>
            <TeamEnergyRanking :rankData="rankingData" />
          </div>

          <!-- 班组能耗占比 -->
          <div class="bg-white rounded p-4">
            <div class="text-base font-medium mb-4">班组��能占比</div>
            <TeamEnergyPie :chartData="pieData" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import TeamEnergyTrend from './components/TeamEnergyTrend.vue';
import TeamEnergyRanking from './components/TeamEnergyRanking.vue';
import TeamEnergyPie from './components/TeamEnergyPie.vue';

// 部门树相关
const searchText = ref<string>('');
const selectedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>(['1']);

// 部门树数据
const treeData = ref([
  {
    title: '车间',
    key: '1',
    children: [
      {
        title: '1#车间',
        key: '1-1',
      },
      {
        title: '2#车间',
        key: '1-2',
      },
      {
        title: '3#车间',
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
const energyType = ref<string>('all');
const shiftType = ref<string>('all');

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

// 班组能耗趋势数据
const trendData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', 
           '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', 
           '18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
  },
  series: [
    {
      name: 'A-1班',
      type: 'bar',
      stack: 'total',
      data: [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      itemStyle: {
        color: '#4B7BE5'
      }
    },
    {
      name: 'A-2班',
      type: 'bar',
      stack: 'total',
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0, 0],
      itemStyle: {
        color: '#23C343'
      }
    },
    {
      name: 'B-1班',
      type: 'bar',
      stack: 'total',
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 7, 7],
      itemStyle: {
        color: '#FF9F40'
      }
    }
  ],
  markLine: {
    data: [
      {
        type: 'average',
        name: '班组能耗',
        lineStyle: {
          color: '#FF9F40',
          type: 'dashed'
        }
      }
    ]
  }
});

// 班组能耗排名数据
const rankingData = ref([
  {
    name: 'B-1班',
    value: 42.53,
    unit: 'tce'
  },
  {
    name: 'A-1班',
    value: 41.65,
    unit: 'tce'
  },
  {
    name: 'A-2班',
    value: 40.15,
    unit: 'tce'
  }
]);

// 班组能耗占比数据
const pieData = ref({
  series: [
    {
      name: '班组用能占比',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 33.5, name: 'A-1班' },
        { value: 32.29, name: 'A-2班' },
        { value: 34.21, name: 'B-1班' }
      ]
    }
  ]
});
</script>

<style scoped>
.team-energy-container {
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