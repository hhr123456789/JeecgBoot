<template>
  <div class="team-energy-container flex min-h-screen bg-gray-50 p-4">
    <!-- 左侧部门树 -->
    <div class="left-panel bg-white p-4 mr-4 rounded-lg shadow-sm" style="width: 310px; flex-shrink: 0;">
      <div class="panel-header mb-4">
        <h3 class="text-base font-semibold text-gray-800">部门结构</h3>
      </div>
      <a-spin :spinning="treeLoading">
        <a-tree
          v-if="orgTreeData.length > 0"
          :tree-data="orgTreeData"
          :expanded-keys="expandedKeys"
          :selected-keys="selectedKeys"
          :auto-expand-parent="autoExpandParent"
          show-line
          @select="onDepartTreeSelect"
          @expand="onTreeExpand"
        >
          <template #title="{ title }">
            <span>{{ title }}</span>
          </template>
        </a-tree>
        <a-empty v-else description="暂无部门数据" :image="simpleImage" />
      </a-spin>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <!-- 顶部查询控制栏 -->
      <div class="query-header bg-white rounded-lg shadow-sm p-6 mb-4">
        <div class="flex items-center gap-4 flex-wrap min-h-[56px]">
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">时间维度:</span>
            <a-radio-group v-model:value="timeUnit" button-style="solid" size="middle">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
          </div>
          
          <a-date-picker 
            v-model:value="selectedDate"
            :picker="pickerType"
            :format="dateFormat"
            class="w-40"
            size="middle"
          />
          
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">班组:</span>
            <a-select
              v-model:value="selectedTeamCode"
              placeholder="请选择班组"
              class="w-40"
              size="middle"
              @change="handleTeamChange"
            >
              <a-select-option value="all">全部班组</a-select-option>
              <a-select-option v-for="team in teamList" :key="team.code" :value="team.code">
                {{ team.name }}
              </a-select-option>
            </a-select>
          </div>
          
          <a-select 
            v-model:value="energyType" 
            class="w-36" 
            size="middle"
          >
            <a-select-option value="all">综合能耗</a-select-option>
            <a-select-option value="1">电</a-select-option>
            <a-select-option value="2">水</a-select-option>
            <a-select-option value="8">天然气</a-select-option>
            <a-select-option value="5">压缩空气</a-select-option>
          </a-select>
          
          <a-button 
            type="primary" 
            size="middle"
            @click="handleQuery"
            :loading="loading"
          >
            查询
          </a-button>
          
          <a-button 
            type="default" 
            size="middle"
            @click="handleExport"
            :disabled="!selectedOrgCode"
          >
            导出数据
          </a-button>
        </div>
        
        <!-- 当前选中部门信息 -->
        <div v-if="selectedOrgName" class="mt-2 text-sm text-gray-600">
          当前部门: {{ selectedOrgName }}
        </div>
      </div>

      <!-- 统计卡片区域 -->
      <div class="stats-cards grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-xs text-gray-500 mb-1">总能耗</div>
            <div class="text-2xl font-bold text-gray-800">{{ statsData.totalConsumption }}</div>
            <div class="text-xs text-gray-400 mt-1">{{ getEnergyUnit() }}</div>
          </div>
          <div class="stat-icon bg-blue-500 rounded-lg p-3">
            <ThunderboltOutlined style="font-size: 24px; color: #ffffff" />
          </div>
        </div>
      </div>

      <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-xs text-gray-500 mb-1">总费用</div>
            <div class="text-2xl font-bold text-gray-800">{{ statsData.totalCost }}</div>
            <div class="text-xs text-gray-400 mt-1">元</div>
          </div>
          <div class="stat-icon bg-green-500 rounded-lg p-3">
            <DollarOutlined style="font-size: 24px; color: #ffffff" />
          </div>
        </div>
      </div>

      <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-xs text-gray-500 mb-1">碳排放</div>
            <div class="text-2xl font-bold text-gray-800">{{ statsData.carbonEmission }}</div>
            <div class="text-xs text-gray-400 mt-1">kg</div>
          </div>
          <div class="stat-icon bg-orange-500 rounded-lg p-3">
            <CloudOutlined style="font-size: 24px; color: #ffffff" />
          </div>
        </div>
      </div>

      <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-xs text-gray-500 mb-1">标准煤</div>
            <div class="text-2xl font-bold text-gray-800">{{ statsData.standardCoal }}</div>
            <div class="text-xs text-gray-400 mt-1">tce</div>
          </div>
          <div class="stat-icon bg-purple-500 rounded-lg p-3">
            <FireOutlined style="font-size: 24px; color: #ffffff" />
          </div>
        </div>
      </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-area grid grid-cols-1 gap-4">
      <!-- 班组能耗趋势对比图 -->
      <div class="chart-card bg-white rounded-lg shadow-sm p-4">
        <div class="chart-header flex items-center justify-between mb-4 flex-wrap gap-2">
          <div class="flex items-center">
            <span class="text-base font-semibold text-gray-800">班组用能趋势对比</span>
            <span class="text-xs text-gray-400 ml-2">({{ getTrendSubtitle() }})</span>
          </div>
          <a-space>
            <a-radio-group v-model:value="trendChartType" size="small" button-style="solid">
              <a-radio-button value="bar">柱状图</a-radio-button>
              <a-radio-button value="line">折线图</a-radio-button>
            </a-radio-group>
            <a-select v-model:value="trendMetric" size="small" class="w-28">
              <a-select-option value="consumption">能耗</a-select-option>
              <a-select-option value="cost">费用</a-select-option>
              <a-select-option value="carbon">碳排放</a-select-option>
            </a-select>
          </a-space>
        </div>
        <TeamEnergyTrend :chartData="trendChartData" :chartType="trendChartType" />
      </div>

      <!-- 班组能耗排名和占比 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <!-- 班组能耗排名 -->
        <div class="chart-card bg-white rounded-lg shadow-sm p-4">
          <div class="chart-header mb-4">
            <div class="text-base font-semibold text-gray-800 mb-1">班组用能排名</div>
            <div class="text-xs text-gray-400">当前周期各班组能耗对比</div>
          </div>
          <TeamEnergyRanking :rankData="rankingData" />
        </div>

        <!-- 班组能耗占比 -->
        <div class="chart-card bg-white rounded-lg shadow-sm p-4">
          <div class="chart-header mb-4">
            <div class="text-base font-semibold text-gray-800 mb-1">班组用能占比</div>
            <div class="text-xs text-gray-400">各班组能耗占比分布</div>
          </div>
          <TeamEnergyPie :chartData="pieChartData" />
        </div>
        </div>
      </div>

      <!-- 班组用能数据表 -->
      <div class="table-card bg-white rounded-lg shadow-sm p-4">
        <div class="table-header mb-4">
          <div class="text-base font-semibold text-gray-800 mb-1">班组用能明细</div>
          <div class="text-xs text-gray-400">详细数据列表</div>
        </div>
        <a-table
          :columns="tableColumns"
          :data-source="tableData"
          :pagination="pagination"
          :loading="loading"
          size="small"
          :scroll="{ x: 1200 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'teamName'">
              <a-tag color="blue">{{ record.teamName }}</a-tag>
            </template>
            <template v-if="column.key === 'shiftType'">
              <a-tag :color="getShiftColor(record.shiftType)">{{ record.shiftType }}</a-tag>
            </template>
          </template>
        </a-table>
      </div>
      </div>
    </div>
  </template>

<script lang="ts" setup>
import { ref, computed, reactive, onMounted } from 'vue';
import dayjs, { Dayjs } from 'dayjs';
import { Empty } from 'ant-design-vue';
import { 
  SearchOutlined, 
  ThunderboltOutlined, 
  DollarOutlined, 
  CloudOutlined, 
  FireOutlined 
} from '@ant-design/icons-vue';
import TeamEnergyTrend from './components/TeamEnergyTrend.vue';
import TeamEnergyRanking from './components/TeamEnergyRanking.vue';
import TeamEnergyPie from './components/TeamEnergyPie.vue';

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

// ==================== 查询参数 ====================
const selectedOrgCode = ref<string>('A01B03');
const selectedOrgName = ref<string>('1#车间');
const selectedTeamCode = ref<string>('all');
const timeUnit = ref<'day' | 'month' | 'year'>('day');
const selectedDate = ref<Dayjs>(dayjs());
const energyType = ref<string>('all');

// ==================== 部门树状态 ====================
const treeLoading = ref<boolean>(false);
const expandedKeys = ref<string[]>(['A01']);
const selectedKeys = ref<string[]>(['A01B03']);
const autoExpandParent = ref<boolean>(true);

// ==================== 部门树数据 ====================
const orgTreeData = ref([
  {
    title: '企业总部',
    value: 'A01',
    key: 'A01',
    children: [
      {
        title: '1#车间',
        value: 'A01B03',
        key: 'A01B03',
        children: [
          {
            title: '线路1',
            value: 'A01B03C01',
            key: 'A01B03C01'
          },
          {
            title: '线路2',
            value: 'A01B03C02',
            key: 'A01B03C02'
          }
        ]
      },
      {
        title: '2#车间',
        value: 'A01B04',
        key: 'A01B04'
      }
    ]
  }
]);

// ==================== 班组数据 ====================
const teamList = ref<{ code: string; name: string }[]>([
  { code: 'A-1', name: 'A-1班' },
  { code: 'A-2', name: 'A-2班' },
  { code: 'B-1', name: 'B-1班' },
  { code: '1-A', name: '1号班' },
  { code: '2-A', name: '2号班' },
  { code: '3-A', name: '3号班' }
]);

// ==================== 计算属性 ====================
const pickerType = computed(() => {
  switch (timeUnit.value) {
    case 'month':
      return 'month';
    case 'year':
      return 'year';
    default:
      return 'date';
  }
});

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

// ==================== 统计卡片数据 ====================
const statsData = reactive({
  totalConsumption: '162.00',
  totalCost: '129.60',
  carbonEmission: '161.51',
  standardCoal: '19.92'
});

// ==================== 趋势图数据 ====================
const trendChartType = ref<'bar' | 'line'>('bar');
const trendMetric = ref<string>('consumption');

const trendChartData = computed(() => {
  if (timeUnit.value === 'day') {
    // 日维度：按小时统计
    return {
      xAxis: {
        type: 'category',
        data: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', 
               '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', 
               '18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
      },
      series: selectedTeamCode.value === 'all' ? [
        {
          name: 'A-1班',
          type: trendChartType.value,
          stack: trendChartType.value === 'bar' ? 'total' : undefined,
          data: [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          itemStyle: {
            color: '#4B7BE5'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'A-2班',
          type: trendChartType.value,
          stack: trendChartType.value === 'bar' ? 'total' : undefined,
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0, 0],
          itemStyle: {
            color: '#23C343'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'B-1班',
          type: trendChartType.value,
          stack: trendChartType.value === 'bar' ? 'total' : undefined,
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 7, 7],
          itemStyle: {
            color: '#FF9F40'
          },
          smooth: trendChartType.value === 'line'
        }
      ] : [
        {
          name: selectedTeamCode.value,
          type: trendChartType.value,
          data: [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          itemStyle: {
            color: '#4B7BE5'
          },
          smooth: trendChartType.value === 'line'
        }
      ]
    };
  } else if (timeUnit.value === 'month') {
    // 月维度：按日统计
    const days = Array.from({ length: 30 }, (_, i) => `${i + 1}日`);
    return {
      xAxis: {
        type: 'category',
        data: days
      },
      series: [
        {
          name: 'A-1班',
          type: trendChartType.value,
          data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 70)),
          itemStyle: {
            color: '#4B7BE5'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'A-2班',
          type: trendChartType.value,
          data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 60)),
          itemStyle: {
            color: '#23C343'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'B-1班',
          type: trendChartType.value,
          data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 75)),
          itemStyle: {
            color: '#FF9F40'
          },
          smooth: trendChartType.value === 'line'
        }
      ]
    };
  } else {
    // 年维度：按月统计
    const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
    return {
      xAxis: {
        type: 'category',
        data: months
      },
      series: [
        {
          name: 'A-1班',
          type: trendChartType.value,
          data: [2100, 2200, 2150, 2300, 2250, 2400, 2350, 2450, 2380, 2500, 2420, 2550],
          itemStyle: {
            color: '#4B7BE5'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'A-2班',
          type: trendChartType.value,
          data: [1900, 2000, 1950, 2100, 2050, 2200, 2150, 2250, 2180, 2300, 2220, 2350],
          itemStyle: {
            color: '#23C343'
          },
          smooth: trendChartType.value === 'line'
        },
        {
          name: 'B-1班',
          type: trendChartType.value,
          data: [2200, 2300, 2250, 2400, 2350, 2500, 2450, 2550, 2480, 2600, 2520, 2650],
          itemStyle: {
            color: '#FF9F40'
          },
          smooth: trendChartType.value === 'line'
        }
      ]
    };
  }
});

// ==================== 排名数据 ====================
const rankingData = ref([
  {
    name: 'B-1班',
    value: 42.53,
    unit: energyType.value === 'all' ? 'tce' : getEnergyUnit()
  },
  {
    name: 'A-1班',
    value: 41.65,
    unit: energyType.value === 'all' ? 'tce' : getEnergyUnit()
  },
  {
    name: 'A-2班',
    value: 40.15,
    unit: energyType.value === 'all' ? 'tce' : getEnergyUnit()
  }
]);

// ==================== 饼图数据 ====================
const pieChartData = computed(() => ({
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
}));

// ==================== 数据表 ====================
const loading = ref(false);
const tableColumns = [
  {
    title: '班组名称',
    dataIndex: 'teamName',
    key: 'teamName',
    width: 120,
    fixed: 'left'
  },
  {
    title: '班次',
    dataIndex: 'shiftType',
    key: 'shiftType',
    width: 80
  },
  {
    title: '统计时间',
    dataIndex: 'statTime',
    key: 'statTime',
    width: 150
  },
  {
    title: `能耗(${getEnergyUnit()})`,
    dataIndex: 'consumption',
    key: 'consumption',
    width: 120,
    align: 'right'
  },
  {
    title: '费用(元)',
    dataIndex: 'cost',
    key: 'cost',
    width: 120,
    align: 'right'
  },
  {
    title: '碳排放(kg)',
    dataIndex: 'carbon',
    key: 'carbon',
    width: 120,
    align: 'right'
  },
  {
    title: '标准煤(tce)',
    dataIndex: 'coal',
    key: 'coal',
    width: 120,
    align: 'right'
  },
  {
    title: '峰时段',
    dataIndex: 'peak',
    key: 'peak',
    width: 100,
    align: 'right'
  },
  {
    title: '平时段',
    dataIndex: 'flat',
    key: 'flat',
    width: 100,
    align: 'right'
  },
  {
    title: '谷时段',
    dataIndex: 'valley',
    key: 'valley',
    width: 100,
    align: 'right'
  }
];

const tableData = ref([
  {
    key: '1',
    teamName: 'A-1班',
    shiftType: '早班',
    statTime: '2026-01-15',
    consumption: '84.00',
    cost: '67.20',
    carbon: '83.75',
    coal: '10.33',
    peak: '20.00',
    flat: '40.00',
    valley: '24.00'
  },
  {
    key: '2',
    teamName: 'A-2班',
    shiftType: '中班',
    statTime: '2026-01-15',
    consumption: '36.00',
    cost: '28.80',
    carbon: '35.89',
    coal: '4.43',
    peak: '12.00',
    flat: '18.00',
    valley: '6.00'
  },
  {
    key: '3',
    teamName: 'B-1班',
    shiftType: '晚班',
    statTime: '2026-01-15',
    consumption: '42.00',
    cost: '33.60',
    carbon: '41.87',
    coal: '5.16',
    peak: '14.00',
    flat: '21.00',
    valley: '7.00'
  }
]);

const pagination = {
  current: 1,
  pageSize: 10,
  total: 3,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
};

// ==================== 工具函数 ====================
function getEnergyUnit() {
  switch (energyType.value) {
    case '1':
      return 'kWh';
    case '2':
      return 'm³';
    case '8':
      return 'm³';
    case '5':
      return 'm³';
    case 'all':
      return 'tce';
    default:
      return 'kWh';
  }
}

function getTrendSubtitle() {
  if (timeUnit.value === 'day') {
    return '按小时统计';
  } else if (timeUnit.value === 'month') {
    return '按日统计';
  } else {
    return '按月统计';
  }
}

function getShiftColor(shiftType: string) {
  const colorMap: Record<string, string> = {
    '早班': 'blue',
    '中班': 'green',
    '晚班': 'orange',
    '夜班': 'purple'
  };
  return colorMap[shiftType] || 'default';
}

// ==================== 事件处理 ====================
// 部门树选择事件
function onDepartTreeSelect(selectedKeysValue: string[], info: any) {
  if (selectedKeysValue.length > 0) {
    const selectedKey = selectedKeysValue[0];
    selectedKeys.value = [selectedKey];
    selectedOrgCode.value = selectedKey;
    
    // 更新选中的部门名称
    if (info.selectedNodes && info.selectedNodes.length > 0) {
      selectedOrgName.value = info.selectedNodes[0].title;
    }
    
    console.log('部门选择:', selectedKey, selectedOrgName.value);
    // TODO: 根据部门加载班组列表和数据
  }
}

// 部门树展开事件
function onTreeExpand(expandedKeysValue: string[]) {
  expandedKeys.value = expandedKeysValue;
  autoExpandParent.value = false;
}

function handleTeamChange(value: string) {
  console.log('班组切换:', value);
}

function handleQuery() {
  console.log('查询参数:', {
    orgCode: selectedOrgCode.value,
    orgName: selectedOrgName.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    date: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value
  });
  // TODO: 调用后端接口查询数据
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
  }, 1000);
}

function handleExport() {
  console.log('导出数据:', {
    orgCode: selectedOrgCode.value,
    orgName: selectedOrgName.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    date: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value
  });
  // TODO: 调用后端接口导出数据
}

// ==================== 生命周期 ====================
onMounted(() => {
  // 初始化加载数据
  handleQuery();
});
</script>

<style scoped>
/* 整体容器 - Flex布局 */
.team-energy-container {
  min-height: calc(100vh - 120px);
}

/* 左侧部门树面板 */
.left-panel {
  min-height: 600px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  overflow-x: hidden;
  
  /* 自定义滚动条 */
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

.panel-header h3 {
  color: #1f2937;
  margin: 0;
}

/* 部门树样式 */
:deep(.ant-tree) {
  background: transparent;
  color: #374151;
}

:deep(.ant-tree-node-content-wrapper) {
  transition: all 0.3s;
}

:deep(.ant-tree-node-content-wrapper:hover) {
  background-color: #f3f4f6 !important;
}

:deep(.ant-tree-node-selected) {
  background-color: #eff6ff !important;
}

:deep(.ant-tree-title) {
  color: #374151;
}

/* 顶部查询控制栏 - 白色卡片 */
.query-header {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 统计卡片 - 白色背景 */
.stat-card {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card .text-xs.text-gray-500 {
  color: #6b7280 !important;
  font-weight: 500;
}

.stat-card .text-2xl.text-gray-800 {
  color: #1f2937 !important;
  font-weight: 700;
}

.stat-card .text-xs.text-gray-400 {
  color: #9ca3af !important;
}

.stat-icon {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 图表卡片 - 白色背景 */
.chart-card {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #d1d5db;
}

.chart-header .text-base {
  color: #1f2937 !important;
  font-weight: 600;
}

.chart-header .text-xs {
  color: #6b7280 !important;
}

/* 表格卡片 - 白色背景 */
.table-card {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.table-header .text-base {
  color: #1f2937 !important;
  font-weight: 600;
}

.table-header .text-xs {
  color: #6b7280 !important;
}

/* Ant Design表格样式 */
:deep(.ant-table) {
  background: #ffffff !important;
  color: #374151 !important;
}

:deep(.ant-table-thead > tr > th) {
  background: #f9fafb !important;
  color: #111827 !important;
  border-bottom: 2px solid #e5e7eb !important;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr > td) {
  background: #ffffff !important;
  color: #374151 !important;
  border-bottom: 1px solid #f3f4f6 !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f9fafb !important;
}

:deep(.ant-pagination) {
  color: #374151 !important;
}

:deep(.ant-pagination-item) {
  background: #ffffff !important;
  border: 1px solid #e5e7eb !important;
}

:deep(.ant-pagination-item a) {
  color: #374151 !important;
}

:deep(.ant-pagination-item-active) {
  background: #eff6ff !important;
  border-color: #3b82f6 !important;
}

:deep(.ant-pagination-item-active a) {
  color: #3b82f6 !important;
}

/* Ant Design选择器和按钮样式 */
:deep(.ant-select-selector),
:deep(.ant-picker),
:deep(.ant-tree-select-selector) {
  background: #ffffff !important;
  border: 1px solid #d1d5db !important;
  color: #374151 !important;
}

:deep(.ant-select-selector:hover),
:deep(.ant-picker:hover),
:deep(.ant-tree-select-selector:hover) {
  border-color: #3b82f6 !important;
}

:deep(.ant-select-selection-item),
:deep(.ant-picker-input > input) {
  color: #374151 !important;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4) !important;
}

/* Ant Design单选按钮组样式 */
:deep(.ant-radio-group-solid .ant-radio-button-wrapper) {
  background: #ffffff !important;
  border-color: #d1d5db !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked)) {
  color: #374151 !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper:hover) {
  color: #3b82f6 !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked) {
  background: #3b82f6 !important;
  border-color: #3b82f6 !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
  background: #3b82f6 !important;
  color: #ffffff !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled):hover) {
  background: #2563eb !important;
  border-color: #2563eb !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled):first-child) {
  border-color: #3b82f6 !important;
}

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked span) {
  color: #ffffff !important;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .team-energy-container {
    flex-direction: column;
  }
  
  .left-panel {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 1rem;
    max-height: 400px;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .query-header {
    padding: 1rem !important;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .charts-area .grid {
    grid-template-columns: 1fr;
  }
  
  .left-panel {
    max-height: 300px;
  }
}
</style>
