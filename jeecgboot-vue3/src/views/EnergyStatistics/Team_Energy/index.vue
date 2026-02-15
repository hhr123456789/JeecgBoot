<template>
  <div class="team-energy-container flex min-h-screen bg-gray-50 p-4">
    <!-- 左侧维度树 -->
    <div class="left-panel bg-white p-4 mr-4 rounded-lg shadow-sm" style="width: 310px; flex-shrink: 0;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs v-model:activeKey="activeTabKey" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
            <a-card :bordered="false" style="height: 100%">
              <DimensionTree
                :ref="(el) => setTreeRef(el, item.key)"
                @select="onDepartTreeSelect"
                :nowtype="item.nowtype"
                :select-level="2"
                style="margin-top:-20px;"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
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
            <div class="text-xs text-gray-400 mt-1">{{ statsData.energyUnit }}</div>
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
import { ref, computed, reactive, onMounted, nextTick, watch } from 'vue';
import dayjs, { Dayjs } from 'dayjs';
import { Empty } from 'ant-design-vue';
import {
  ThunderboltOutlined,
  DollarOutlined,
  CloudOutlined,
  FireOutlined
} from '@ant-design/icons-vue';
import TeamEnergyTrend from './components/TeamEnergyTrend.vue';
import TeamEnergyRanking from './components/TeamEnergyRanking.vue';
import TeamEnergyPie from './components/TeamEnergyPie.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import {
  getTeamListByDimension,
  getTeamEnergyStatistics,
  getTeamEnergyTrendData,
  getTeamEnergyRankingData,
  getTeamEnergyTableData
} from './team-energy.api';

const { createMessage } = useMessage();
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

// ==================== 查询参数 ====================
const selectedOrgCode = ref<string>('');
const selectedOrgName = ref<string>('');
const selectedTeamCode = ref<string>('all');
const timeUnit = ref<'day' | 'month' | 'year'>('day');
const selectedDate = ref<Dayjs>(dayjs());
const energyType = ref<string>('all');

// ==================== 维度相关 ====================
const activeTabKey = ref('info1');
const currentDimensionType = ref(1);
const dimensionList = ref<any[]>([]);
const treeRefs = ref<Record<string, any>>({});

// 存储每个标签页选中的节点信息
const selectedNodesMap = ref({
  info1: null,
  info2: null,
  info3: null,
  info4: null,
  info5: null
});

// ==================== 班组数据 ====================
const teamList = ref<{ code: string; name: string }[]>([]);

// ==================== 加载状态 ====================
const loading = ref(false);

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
  totalConsumption: '0.00',
  totalCost: '0.00',
  carbonEmission: '0.00',
  standardCoal: '0.00',
  energyUnit: 'kWh'
});

// ==================== 趋势图数据 ====================
const trendChartType = ref<'bar' | 'line'>('bar');
const trendMetric = ref<string>('consumption');
const trendChartData = ref<any>({
  xAxis: { type: 'category', data: [] },
  series: []
});

// ==================== 排名数据 ====================
const rankingData = ref<any[]>([]);

// ==================== 饼图数据 ====================
const pieChartData = computed(() => ({
  series: [
    {
      name: '班组用能占比',
      type: 'pie',
      radius: ['50%', '70%'],
      data: rankingData.value.map(item => ({
        value: item.value,
        name: item.name
      }))
    }
  ]
}));

// ==================== 数据表 ====================
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
    title: `能耗`,
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

const tableData = ref<any[]>([]);

const pagination = {
  current: 1,
  pageSize: 10,
  total: 0,
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

// ==================== 维度相关函数 ====================
// 设置树组件引用
const setTreeRef = (el, key) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 获取维度字典数据
function loadDimensionDictData() {
  defHttp.get({
    url: '/sys/dict/getDictItems/dimensionCode'
  })
  .then((res) => {
    if (res && Array.isArray(res)) {
      dimensionList.value = res.map((item, index) => {
        return {
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(index + 1),
          value: Number(index + 1)
        };
      });

      if (dimensionList.value.length > 0) {
        activeTabKey.value = dimensionList.value[0].key;
        currentDimensionType.value = dimensionList.value[0].nowtype;
      }
    } else {
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
        { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
        { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
        { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
      ];
    }
  })
  .catch(() => {
    dimensionList.value = [
      { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
      { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
      { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
      { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
      { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
    ];
  });
}

// 处理标签页切换
function handleTabChange(key) {
  activeTabKey.value = key;

  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    currentDimensionType.value = selectedDimension.nowtype;
  }

  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    selectedOrgCode.value = savedNode.orgCode;
    selectedOrgName.value = savedNode.orgName;
    loadTeamList(savedNode.orgCode, currentDimensionType.value);
    // 切换标签页后自动加载数据
    handleQuery();
  }

  nextTick(() => {
    const currentTreeRef = treeRefs.value[key];
    if (currentTreeRef && !savedNode) {
      // 树组件会自动选择默认节点并触发select事件
    }
  });
}

// 左侧树选择后触发
function onDepartTreeSelect(data) {
  console.log('DimensionTree 选中节点原始数据:', data);

  // 兼容多种字段名
  const orgCode = data.orgCode || data.id || data.key || data.value;
  const orgName = data.orgName || data.title || data.label || data.name || data.depart_name;

  if (!orgCode) {
    console.warn('树节点数据中没有找到有效的编码字段', data);
    createMessage.warning('无法获取部门编码，请检查数据');
    return;
  }

  selectedOrgCode.value = orgCode;
  selectedOrgName.value = orgName || '未知部门';

  console.log('选中部门:', { orgCode, orgName });

  // 保存当前维度的选中节点
  selectedNodesMap.value[activeTabKey.value] = {
    orgCode,
    orgName: selectedOrgName.value,
    data: data
  };

  // 加载班组列表
  loadTeamList(orgCode, currentDimensionType.value);

  // 自动查询
  handleQuery();
}

// ==================== 数据加载函数 ====================
// 加载班组列表
async function loadTeamList(dimensionCode: string, dimensionType: number) {
  if (!dimensionCode) return;

  try {
    const res = await getTeamListByDimension({ dimensionCode, dimensionType });
    if (res.success && res.result) {
      teamList.value = res.result;
      selectedTeamCode.value = 'all';
    } else {
      teamList.value = [];
      createMessage.warning('获取班组列表失败');
    }
  } catch (error) {
    console.error('加载班组列表失败:', error);
    teamList.value = [];
  }
}

// 加载统计数据
async function loadStatistics() {
  if (!selectedOrgCode.value) return;

  const params = {
    orgCode: selectedOrgCode.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value,
    dimensionType: currentDimensionType.value,
    metricType: trendMetric.value
  };

  try {
    const res = await getTeamEnergyStatistics(params);
    if (res.success && res.result) {
      Object.assign(statsData, res.result);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
    createMessage.error('加载统计数据失败，请稍后重试');
  }
}

// 加载趋势数据
async function loadTrendData() {
  if (!selectedOrgCode.value) return;

  const params = {
    orgCode: selectedOrgCode.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value,
    dimensionType: currentDimensionType.value,
    metricType: trendMetric.value
  };

  try {
    const res = await getTeamEnergyTrendData(params);
    if (res.success && res.result) {
      trendChartData.value = {
        xAxis: {
          type: 'category',
          data: res.result.xAxisData
        },
        series: res.result.seriesData.map(item => ({
          name: item.name,
          type: trendChartType.value,
          stack: trendChartType.value === 'bar' ? 'total' : undefined,
          data: item.data,
          itemStyle: {
            color: item.color
          },
          smooth: trendChartType.value === 'line'
        }))
      };
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error);
    createMessage.error('加载趋势数据失败，请稍后重试');
  }
}

// 加载排名数据
async function loadRankingData() {
  if (!selectedOrgCode.value) return;

  const params = {
    orgCode: selectedOrgCode.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value,
    dimensionType: currentDimensionType.value,
    metricType: trendMetric.value
  };

  try {
    const res = await getTeamEnergyRankingData(params);
    if (res.success && res.result) {
      rankingData.value = res.result;
    }
  } catch (error) {
    console.error('加载排名数据失败:', error);
    createMessage.error('加载排名数据失败，请稍后重试');
  }
}

// 加载表格数据
async function loadTableData() {
  if (!selectedOrgCode.value) return;

  const params = {
    orgCode: selectedOrgCode.value,
    teamCode: selectedTeamCode.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    energyType: energyType.value,
    dimensionType: currentDimensionType.value,
    metricType: trendMetric.value
  };

  try {
    const res = await getTeamEnergyTableData(params);
    if (res.success && res.result) {
      tableData.value = res.result.map((item, index) => ({
        ...item,
        key: index.toString()
      }));
      pagination.total = res.result.length;
    }
  } catch (error) {
    console.error('加载表格数据失败:', error);
    createMessage.error('加载表格数据失败，请稍后重试');
  }
}

// ==================== 事件处理 ====================
function handleTeamChange(value: string) {
  console.log('班组切换:', value);
}

async function handleQuery() {
  if (!selectedOrgCode.value) {
    createMessage.warning('请先选择部门');
    return;
  }

  loading.value = true;
  try {
    await Promise.all([
      loadStatistics(),
      loadTrendData(),
      loadRankingData(),
      loadTableData()
    ]);
    createMessage.success('查询成功');
  } catch (error) {
    console.error('查询失败:', error);
    createMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
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
  // TODO: 实现导出功能
  createMessage.info('导出功能开发中');
}

// ==================== 自动刷新 ====================
// 监听关键参数变化，自动刷新数据
watch([selectedTeamCode, trendMetric, energyType], () => {
  if (selectedOrgCode.value) {
    console.log('参数变化，自动刷新数据');
    handleQuery();
  }
});

// 监听时间维度和日期变化
watch([timeUnit, selectedDate], () => {
  if (selectedOrgCode.value) {
    console.log('时间参数变化，自动刷新数据');
    handleQuery();
  }
});

// ==================== 生命周期 ====================
onMounted(() => {
  loadDimensionDictData();

  nextTick(() => {
    // 等待树组件加载完成
  });
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
