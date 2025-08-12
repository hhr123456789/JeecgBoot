<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧动态维度（复用 Real_Monitor 的实现） -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs defaultActiveKey="info1" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title">
            <a-card :bordered="false" style="height: 100%">
              <DimensionTree
                :ref="(el) => setTreeRef(el, item.key)"
                @select="onDepartTreeSelect"
                :nowtype="item.nowtype"
                :select-level="2"
                style="margin-top:-20px ;"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1 mt-4">
      <!-- 顶部筛选区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div>
          <div class="flex items-center gap-4 flex-nowrap">
            <!-- 时间范围选择 -->


            <a-radio-group v-model:value="timeRange" button-style="solid" class="custom-radio-group">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
            <!-- 仪表选择（单选，模拟数据） -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2 whitespace-nowrap">仪表:</span>
              <a-select
                v-model:value="selectedInstrument"
                :options="instrumentOptions"
                placeholder="请选择仪表"
                style="width:220px"
                class="custom-select"
                :dropdownMatchSelectWidth="false"
              />
            </div>
            <!-- 基准期时间选择 -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2 whitespace-nowrap">基准期:</span>
              <a-range-picker
                v-model:value="baseDateRange"
                :format="dateFormat"
                class="w-64 custom-picker"
              />
            </div>
            <!-- 对比期时间选择 -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2 whitespace-nowrap">对比期:</span>
              <a-range-picker
                v-model:value="compareDateRange"
                :format="dateFormat"
                class="w-64 custom-picker"
              />
            </div>
            <div class="flex gap-2">
            <a-button type="primary" class="custom-button" @click="onQuery">查询</a-button>
            <a-button type="default" class="custom-button">导出数据</a-button>
          </div>
          </div>

        </div>
      </div>

      <!-- 数据对比卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 基准期用电量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">基准期用电量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ compareData.baseConsumption }}
          </div>
        </div>
        <!-- 对比期用电量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">对比期用电量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ compareData.compareConsumption }}
          </div>
        </div>
        <!-- 节能量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">节能量 kWh</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-500">
            {{ compareData.energySaving }}
          </div>
        </div>
        <!-- 节能率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">节能率 %</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center text-green-500">
            {{ compareData.savingRate }}
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <CompareChart :chartData="chartData" />
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded p-3">
        <div class="text-gray-600 text-sm mb-3">对比数据明细</div>
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="false"
          size="middle"
          table-layout="fixed"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import CompareChart from './components/CompareChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';

// 左侧维度（动态 Tabs + DimensionTree）
const activeTabKey = ref('info1');
const currentNowtype = ref(1);
const currentOrgCode = ref('');

const dimensionList = ref<any[]>([]);
const treeRefs = ref<Record<string, any>>({});
const setTreeRef = (el, key) => {
  if (el) treeRefs.value[key] = el;
};

// 存储每个标签页选中的节点信息
const selectedNodesMap = ref<Record<string, any>>({
  info1: null,
  info2: null,
  info3: null,
  info4: null,
  info5: null,
});

// 加载维度字典数据（与 Real_Monitor 保持一致）
function loadDimensionDictData() {
  defHttp
    .get({ url: '/sys/dict/getDictItems/dimensionCode' })
    .then((res) => {
      if (res && Array.isArray(res)) {
        dimensionList.value = res.map((item, index) => ({
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(index + 1),
          value: Number(index + 1),
        }));
        if (dimensionList.value.length > 0) {
          activeTabKey.value = dimensionList.value[0].key;
          currentNowtype.value = dimensionList.value[0].nowtype;
        }
      } else {
        dimensionList.value = [
          { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
          { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
          { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
          { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
          { key: 'info5', title: '企业用水', nowtype: 5, value: 5 },
        ];
      }
    })
    .catch(() => {
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
        { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
        { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
        { key: 'info5', title: '企业用水', nowtype: 5, value: 5 },
      ];
    });
}

// 标签页切换
function handleTabChange(key) {
  activeTabKey.value = key;
  const selectedDimension = dimensionList.value.find((item) => item.key === key);
  if (selectedDimension) {
    currentNowtype.value = selectedDimension.nowtype;
  }
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    currentOrgCode.value = savedNode.orgCode;
    refreshDataBasedOnSelection();
  }
  nextTick(() => {
    const current = treeRefs.value[key];
    if (current && !savedNode) {
      // 树组件会自动默认选择并触发 select 事件
    }
  });
}

// 左侧树选择
function onDepartTreeSelect(data) {
  if (Array.isArray(data) && data.length > 0) {
    const orgCodestr = data.map((item) => item.orgCode).join(',');
    currentOrgCode.value = orgCodestr;
    selectedNodesMap.value[activeTabKey.value] = { orgCode: orgCodestr, data };
  } else if (data && data.orgCode) {
    currentOrgCode.value = data.orgCode;
    selectedNodesMap.value[activeTabKey.value] = { orgCode: data.orgCode, data };
  }
  refreshDataBasedOnSelection();
}

function refreshDataBasedOnSelection() {
  // 先加载模拟的仪表列表（单选）
  loadMockInstruments(currentOrgCode.value, currentNowtype.value);
  // 更新模拟图表（按日）
  if (timeRange.value === 'day') {
    updateMockChartDataForDay();
  }
  // TODO: 后续在此根据 currentOrgCode、currentNowtype、selectedInstrument 等请求后端，刷新对比数据
}

function onQuery() {
  // 根据当前筛选条件刷新数据
  refreshDataBasedOnSelection();
}


onMounted(() => {
  loadDimensionDictData();
  if (timeRange.value === 'day') {
    updateMockChartDataForDay();
  }
});

// 时间范围和日期选择
const timeRange = ref('day');
const baseDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(14, 'day'), dayjs().subtract(7, 'day')]);
const compareDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(7, 'day'), dayjs()]);

// 仪表选择（单选，使用模拟数据）
const instrumentList = ref<Array<{ id: string; name: string }>>([]);
const selectedInstrument = ref<string | null>(null);
const instrumentOptions = computed(() =>
  instrumentList.value.map((i) => ({ label: i.name, value: i.id }))
);

function loadMockInstruments(orgCode?: string, nowtype?: number) {
  const type = nowtype ?? currentNowtype.value;
  const org = orgCode ?? (currentOrgCode.value || '默认');
  instrumentList.value = Array.from({ length: 5 }).map((_, idx) => ({
    id: `${type}-${org}-M${idx + 1}`,
    name: `仪表${idx + 1}`,
  }));
  selectedInstrument.value = instrumentList.value[0]?.id || null;
}

// 根据“日”粒度生成模拟数据：以当月天数为横坐标
function updateMockChartDataForDay() {
  // 取基准期起止，若无则以当前月
  const start = baseDateRange.value?.[0] || dayjs().startOf('month');
  const end = baseDateRange.value?.[1] || dayjs().endOf('month');
  const days = end.diff(start, 'day') + 1;
  const x = Array.from({ length: Math.max(days, 7) }).map((_, i) => start.add(i, 'day').format('MM-DD'));

  // 构造两组模拟曲线（基准期、对比期）
  const base = x.map((_, i) => 200 + Math.round(60 * Math.sin((i / x.length) * Math.PI * 2) + 40 * Math.random()));
  const cmp = base.map((v) => Math.max(120, v - 20 + Math.round(20 * (Math.random() - 0.5))));

  chartData.value = {
    xAxis: { type: 'category', data: x },
    series: [
      { name: '基准期', type: 'line', data: base, itemStyle: { color: '#1890ff' } },
      { name: '对比期', type: 'line', data: cmp, itemStyle: { color: '#52c41a' } },
    ],
  } as any;
}

// 监听时间粒度切换为“日”时，刷新模拟数据
watch(() => timeRange.value, (val) => {
  if (val === 'day') updateMockChartDataForDay();
});

// 日期格式
const dateFormat = computed(() => {
  switch (timeRange.value) {
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

// 对比数据
interface CompareData {
  baseConsumption: number;    // 基准期用电量
  compareConsumption: number; // 对比期用电量
  energySaving: number;       // 节能量
  savingRate: number;         // 节能率
}

// 静态对比数据
const compareData = ref<CompareData>({
  baseConsumption: 296.37,
  compareConsumption: 201.74,
  energySaving: 94.63,
  savingRate: 31.93
});

// 图表数据
const chartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '基准期',
      type: 'line',
      data: [320, 280, 250, 340, 360, 320, 380,
             340, 320, 300, 340, 360],
      itemStyle: {
        color: '#1890ff'
      }
    },
    {
      name: '对比期',
      type: 'line',
      data: [280, 200, 220, 300, 340, 300, 340,
             300, 280, 260, 300, 320],
      itemStyle: {
        color: '#52c41a'
      }
    }
  ]
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '时间',
    dataIndex: 'time',
    width: '20%',
    align: 'center'
  },
  {
    title: '基准期用电量(kWh)',
    dataIndex: 'baseConsumption',
    width: '20%',
    align: 'center'
  },
  {
    title: '对比期用电量(kWh)',
    dataIndex: 'compareConsumption',
    width: '20%',
    align: 'center'
  },
  {
    title: '节能量(kWh)',
    dataIndex: 'energySaving',
    width: '20%',
    align: 'center'
  },
  {
    title: '节能率(%)',
    dataIndex: 'savingRate',
    width: '20%',
    align: 'center'
  }
];

// 表格数据
const tableData = ref([
  {
    key: '1',
    time: '2024-01-16',
    baseConsumption: 326061.00,
    compareConsumption: 297241.00,
    energySaving: 28820.00,
    savingRate: 8.84
  },
  {
    key: '2',
    time: '2024-01-17',
    baseConsumption: 213371.00,
    compareConsumption: 279242.60,
    energySaving: -65871.60,
    savingRate: -30.87
  },
  {
    key: '3',
    time: '2024-01-18',
    baseConsumption: 288831.00,
    compareConsumption: 286104.20,
    energySaving: 2726.80,
    savingRate: 0.94
  }
]);

</script>

<style scoped>
.h-full {
  min-height: calc(100vh - 100px);
}

/* 顶部控件统一高度样式（参考 Real_Data_Monitor_FH） */
.custom-button { height: 36px; display: flex; align-items: center; padding: 0 16px; }
:deep(.custom-picker) { height: 36px; }
:deep(.custom-picker .ant-picker-input) { height: 36px; display: flex; align-items: center; }
:deep(.custom-select) { height: 36px; }
:deep(.custom-select .ant-select-selector) { height: 36px !important; padding-top: 3px !important; }
:deep(.custom-radio-group) { height: 36px; display: inline-flex; }
:deep(.custom-radio-group .ant-radio-button-wrapper) { height: 36px; line-height: 34px; display: inline-flex; align-items: center; }

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