<!-- 班次用能统计主视图 -->
<template>
  <div class="shift-energy-container flex min-h-screen bg-gray-50 p-4">
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
      <!-- 日期选择头部 -->
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
            <span class="text-sm text-gray-600">班次:</span>
            <a-select v-model:value="shiftType" class="w-32" size="middle">
              <a-select-option value="all">全部班次</a-select-option>
              <a-select-option value="morning">早班</a-select-option>
              <a-select-option value="middle">中班</a-select-option>
              <a-select-option value="night">晚班</a-select-option>
            </a-select>
          </div>

          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">能源类型:</span>
            <a-select v-model:value="energyType" class="w-36" size="middle" disabled>
              <a-select-option value="1">电</a-select-option>
              <a-select-option value="2">水</a-select-option>
              <a-select-option value="8">天然气</a-select-option>
              <a-select-option value="5">压缩空气</a-select-option>
            </a-select>
            <span class="text-xs text-gray-400">(由左侧维度决定)</span>
          </div>

          <a-button type="primary" size="middle" @click="handleQuery" :loading="loading">查询</a-button>
          <a-button type="default" size="middle" @click="handleExport" :disabled="!selectedOrgCode">导出数据</a-button>
        </div>

        <!-- 当前选中部门信息 -->
        <div v-if="selectedOrgName" class="mt-2 text-sm text-gray-600">
          当前部门: {{ selectedOrgName }}
        </div>
      </div>

      <!-- 班次能耗统计卡片 -->
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
              <div class="text-xs text-gray-500 mb-1">早班能耗</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.morningConsumption }}</div>
              <div class="text-xs text-gray-400 mt-1">{{ statsData.energyUnit }}</div>
            </div>
            <div class="stat-icon bg-green-500 rounded-lg p-3">
              <ThunderboltOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-gray-500 mb-1">中班能耗</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.middleConsumption }}</div>
              <div class="text-xs text-gray-400 mt-1">{{ statsData.energyUnit }}</div>
            </div>
            <div class="stat-icon bg-orange-500 rounded-lg p-3">
              <CloudOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-gray-500 mb-1">晚班能耗</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.nightConsumption }}</div>
              <div class="text-xs text-gray-400 mt-1">{{ statsData.energyUnit }}</div>
            </div>
            <div class="stat-icon bg-purple-500 rounded-lg p-3">
              <FireOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>
      </div>

      <!-- 第二行统计卡片 -->
      <div class="stats-cards grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
        <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-gray-500 mb-1">总费用</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.totalCost }}</div>
              <div class="text-xs text-gray-400 mt-1">元</div>
            </div>
            <div class="stat-icon bg-cyan-500 rounded-lg p-3">
              <DollarOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-gray-500 mb-1">碳排放</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.totalCarbon }}</div>
              <div class="text-xs text-gray-400 mt-1">kg</div>
            </div>
            <div class="stat-icon bg-teal-500 rounded-lg p-3">
              <CloudOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-gray-500 mb-1">标准煤</div>
              <div class="text-2xl font-bold text-gray-800">{{ statsData.totalCoal }}</div>
              <div class="text-xs text-gray-400 mt-1">tce</div>
            </div>
            <div class="stat-icon bg-amber-500 rounded-lg p-3">
              <FireOutlined style="font-size: 24px; color: #ffffff" />
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
        <!-- 班次能耗趋势图 -->
        <div class="chart-card bg-white rounded-lg shadow-sm p-4">
          <div class="chart-header flex items-center justify-between mb-4">
            <div class="flex items-center">
              <span class="text-base font-semibold text-gray-800">班次能耗趋势</span>
              <span class="text-xs text-gray-400 ml-2">({{ getTrendSubtitle() }})</span>
            </div>
            <span class="text-xs text-gray-400">单位: {{ statsData.energyUnit }}</span>
          </div>
          <ShiftEnergyTrend :chartData="trendChartData" />
        </div>

        <!-- 班次能耗占比图 -->
        <div class="chart-card bg-white rounded-lg shadow-sm p-4">
          <div class="chart-header flex items-center justify-between mb-4">
            <div class="flex items-center">
              <span class="text-base font-semibold text-gray-800">班次能耗占比</span>
            </div>
            <span class="text-xs text-gray-400">单位: %</span>
          </div>
          <ShiftEnergyPie :chartData="pieChartData" />
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="table-card bg-white rounded-lg shadow-sm p-4">
        <div class="table-header mb-4">
          <div class="text-base font-semibold text-gray-800 mb-1">班次能耗明细</div>
          <div class="text-xs text-gray-400">详细数据列表</div>
        </div>
        <a-table
          :columns="tableColumns"
          :data-source="tableData"
          :pagination="pagination"
          :loading="loading"
          size="small"
          :scroll="{ x: 1000 }"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, reactive, onMounted, nextTick, watch } from 'vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  ThunderboltOutlined,
  DollarOutlined,
  CloudOutlined,
  FireOutlined
} from '@ant-design/icons-vue';
import ShiftEnergyTrend from './components/ShiftEnergyTrend.vue';
import ShiftEnergyPie from './components/ShiftEnergyPie.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import {
  getShiftEnergyStatistics,
  getShiftEnergyTrendData,
  getShiftEnergyPieData,
  getShiftEnergyTableData
} from './shift-energy.api';

const { createMessage } = useMessage();

// ==================== 查询参数 ====================
const selectedOrgCode = ref<string>('');
const selectedOrgName = ref<string>('');
const timeUnit = ref<'day' | 'month' | 'year'>('day');
const selectedDate = ref<Dayjs>(dayjs());
const shiftType = ref<string>('all');
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

// ==================== 加载状态 ====================
const loading = ref(false);

// ==================== 计算属性 ====================
const pickerType = computed(() => {
  switch (timeUnit.value) {
    case 'month': return 'month';
    case 'year': return 'year';
    default: return 'date';
  }
});

const dateFormat = computed(() => {
  switch (timeUnit.value) {
    case 'day': return 'YYYY-MM-DD';
    case 'month': return 'YYYY-MM';
    case 'year': return 'YYYY';
    default: return 'YYYY-MM-DD';
  }
});

// ==================== 统计卡片数据 ====================
const statsData = reactive({
  totalConsumption: '0.00',
  morningConsumption: '0.00',
  middleConsumption: '0.00',
  nightConsumption: '0.00',
  totalCost: '0.00',
  totalCarbon: '0.00',
  totalCoal: '0.00',
  energyUnit: 'kWh'
});

// ==================== 趋势图数据 ====================
const trendChartData = ref<any>({
  xAxis: { type: 'category', data: [] },
  series: []
});

// ==================== 饼图数据 ====================
const pieChartData = ref<any>({
  series: [{
    name: '班次能耗占比',
    type: 'pie',
    radius: ['50%', '70%'],
    data: []
  }]
});

// ==================== 数据表 ====================
const tableColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120, fixed: 'left' },
  { title: '早班能耗', dataIndex: 'morningConsumption', key: 'morningConsumption', width: 120, align: 'right' },
  { title: '中班能耗', dataIndex: 'middleConsumption', key: 'middleConsumption', width: 120, align: 'right' },
  { title: '晚班能耗', dataIndex: 'nightConsumption', key: 'nightConsumption', width: 120, align: 'right' },
  { title: '总能耗', dataIndex: 'totalConsumption', key: 'totalConsumption', width: 120, align: 'right' },
  { title: '费用(元)', dataIndex: 'totalCost', key: 'totalCost', width: 100, align: 'right' },
  { title: '碳排放(kg)', dataIndex: 'carbon', key: 'carbon', width: 100, align: 'right' },
  { title: '标准煤(tce)', dataIndex: 'coal', key: 'coal', width: 100, align: 'right' }
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
function getTrendSubtitle() {
  if (timeUnit.value === 'day') return '按小时统计';
  if (timeUnit.value === 'month') return '按日统计';
  return '按月统计';
}

// ==================== 维度相关函数 ====================
const setTreeRef = (el, key) => {
  if (el) treeRefs.value[key] = el;
};

function loadDimensionDictData() {
  defHttp.get({ url: '/sys/dict/getDictItems/dimensionCode' })
    .then((res) => {
      if (res && Array.isArray(res)) {
        dimensionList.value = res.map((item, index) => ({
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(index + 1),
          value: Number(index + 1)
        }));
        if (dimensionList.value.length > 0) {
          activeTabKey.value = dimensionList.value[0].key;
          currentDimensionType.value = dimensionList.value[0].nowtype;
        }
      } else {
        setDefaultDimensionList();
      }
    })
    .catch(() => setDefaultDimensionList());
}

function setDefaultDimensionList() {
  dimensionList.value = [
    { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
    { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
    { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
    { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
    { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
  ];
}

function handleTabChange(key) {
  activeTabKey.value = key;
  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    currentDimensionType.value = selectedDimension.nowtype;
    // 根据维度类型自动设置能源类型
    updateEnergyTypeByDimension(selectedDimension.nowtype);
  }

  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    selectedOrgCode.value = savedNode.orgCode;
    selectedOrgName.value = savedNode.orgName;
    handleQuery();
  } else {
    // 切换到新Tab时，清空之前的选中状态和数据
    selectedOrgCode.value = '';
    selectedOrgName.value = '';
    resetData();
  }
}

// 根据维度类型自动设置能源类型
function updateEnergyTypeByDimension(dimensionType: number) {
  switch (dimensionType) {
    case 1: // 按部门（用电）
    case 2: // 按线路（用电）
      energyType.value = '1'; // 电
      break;
    case 3: // 天然气
      energyType.value = '8'; // 天然气
      break;
    case 4: // 压缩空气
      energyType.value = '5'; // 压缩空气
      break;
    case 5: // 企业用水
      energyType.value = '2'; // 水
      break;
    default:
      energyType.value = 'all';
  }
}

// 重置数据
function resetData() {
  Object.assign(statsData, {
    totalConsumption: '0.00',
    morningConsumption: '0.00',
    middleConsumption: '0.00',
    nightConsumption: '0.00',
    totalCost: '0.00',
    totalCarbon: '0.00',
    totalCoal: '0.00',
    energyUnit: 'kWh'
  });
  trendChartData.value = { xAxis: { type: 'category', data: [] }, series: [] };
  pieChartData.value = { series: [{ name: '班次能耗占比', type: 'pie', radius: ['50%', '70%'], data: [] }] };
  tableData.value = [];
}

function onDepartTreeSelect(data) {
  const orgCode = data.orgCode || data.id || data.key || data.value;

  // 确保 orgName 是字符串，避免循环引用问题
  let orgName = '';
  if (typeof data.orgName === 'string') {
    orgName = data.orgName;
  } else if (typeof data.title === 'string') {
    orgName = data.title;
  } else if (typeof data.label === 'string') {
    orgName = data.label;
  } else if (typeof data.name === 'string') {
    orgName = data.name;
  } else if (typeof data.depart_name === 'string') {
    orgName = data.depart_name;
  }

  if (!orgCode) {
    createMessage.warning('无法获取部门编码，请检查数据');
    return;
  }

  selectedOrgCode.value = orgCode;
  selectedOrgName.value = orgName || '未知部门';

  // 只保存必要的字段，避免循环引用
  selectedNodesMap.value[activeTabKey.value] = {
    orgCode,
    orgName: selectedOrgName.value
  };
  handleQuery();
}

// ==================== 数据加载函数 ====================
async function loadStatistics() {
  if (!selectedOrgCode.value) return;
  const params = {
    dimensionCode: selectedOrgCode.value,
    dimensionType: currentDimensionType.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    shiftType: shiftType.value === 'all' ? undefined : shiftType.value,
    energyType: energyType.value === 'all' ? undefined : energyType.value
  };

  try {
    const res = await getShiftEnergyStatistics(params);
    if (res) Object.assign(statsData, res);
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
}

async function loadTrendData() {
  if (!selectedOrgCode.value) return;
  const params = {
    dimensionCode: selectedOrgCode.value,
    dimensionType: currentDimensionType.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    shiftType: shiftType.value === 'all' ? undefined : shiftType.value,
    energyType: energyType.value === 'all' ? undefined : energyType.value
  };

  try {
    const res = await getShiftEnergyTrendData(params);
    if (res) {
      trendChartData.value = {
        xAxis: { type: 'category', data: res.xAxisData || [] },
        series: (res.seriesData || []).map(item => ({
          name: item.name,
          type: 'bar',
          stack: 'total',
          data: item.data,
          itemStyle: { color: item.color }
        }))
      };
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error);
  }
}

async function loadPieData() {
  if (!selectedOrgCode.value) return;
  const params = {
    dimensionCode: selectedOrgCode.value,
    dimensionType: currentDimensionType.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    shiftType: shiftType.value === 'all' ? undefined : shiftType.value,
    energyType: energyType.value === 'all' ? undefined : energyType.value
  };

  try {
    const res = await getShiftEnergyPieData(params);
    if (res && Array.isArray(res)) {
      pieChartData.value = {
        series: [{
          name: '班次能耗占比',
          type: 'pie',
          radius: ['50%', '70%'],
          data: res.map(item => ({
            value: item.value,
            name: item.name,
            itemStyle: { color: item.color }
          }))
        }]
      };
    }
  } catch (error) {
    console.error('加载饼图数据失败:', error);
  }
}

async function loadTableData() {
  if (!selectedOrgCode.value) return;
  const params = {
    dimensionCode: selectedOrgCode.value,
    dimensionType: currentDimensionType.value,
    timeUnit: timeUnit.value,
    queryDate: selectedDate.value.format(dateFormat.value),
    shiftType: shiftType.value === 'all' ? undefined : shiftType.value,
    energyType: energyType.value === 'all' ? undefined : energyType.value
  };

  try {
    const res = await getShiftEnergyTableData(params);
    if (res && Array.isArray(res)) {
      tableData.value = res.map((item, index) => ({ ...item, key: index.toString() }));
      pagination.total = res.length;
    }
  } catch (error) {
    console.error('加载表格数据失败:', error);
  }
}

// ==================== 事件处理 ====================
async function handleQuery() {
  if (!selectedOrgCode.value) {
    createMessage.warning('请先选择部门');
    return;
  }

  loading.value = true;
  try {
    await Promise.all([loadStatistics(), loadTrendData(), loadPieData(), loadTableData()]);
    createMessage.success('查询成功');
  } catch (error) {
    console.error('查询失败:', error);
    createMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
}

function handleExport() {
  createMessage.info('导出功能开发中');
}

// ==================== 自动刷新 ====================
watch([shiftType], () => {
  if (selectedOrgCode.value) handleQuery();
});

watch([timeUnit, selectedDate], () => {
  if (selectedOrgCode.value) handleQuery();
});

// ==================== 生命周期 ====================
onMounted(() => {
  loadDimensionDictData();
});
</script>

<style scoped>
.shift-energy-container {
  min-height: calc(100vh - 120px);
}

.left-panel {
  min-height: 600px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  overflow-x: hidden;
}

.left-panel::-webkit-scrollbar {
  width: 6px;
}

.left-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.query-header {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

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

.stat-icon {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card, .table-card {
  background: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #d1d5db;
}

:deep(.ant-tree) {
  background: transparent;
  color: #374151;
}

:deep(.ant-tree-node-content-wrapper:hover) {
  background-color: #f3f4f6 !important;
}

:deep(.ant-tree-node-selected) {
  background-color: #eff6ff !important;
}

:deep(.ant-table) {
  background: #ffffff !important;
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

:deep(.ant-radio-group-solid .ant-radio-button-wrapper-checked) {
  background: #3b82f6 !important;
  border-color: #3b82f6 !important;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
}

@media (max-width: 1200px) {
  .shift-energy-container {
    flex-direction: column;
  }
  .left-panel {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 1rem;
    max-height: 400px;
  }
}

@media (max-width: 768px) {
  .query-header {
    padding: 1rem !important;
  }
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .left-panel {
    max-height: 300px;
  }
}
</style>
