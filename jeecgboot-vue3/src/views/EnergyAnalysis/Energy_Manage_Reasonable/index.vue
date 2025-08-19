<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧动态维度树（复用 Compare 实现） -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs :defaultActiveKey="activeTabKey" @change="handleTabChange" style="height: 100%;width:300px;">
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
    <div class="flex-1">
      <!-- 顶部查询区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex items-center gap-4 flex-nowrap">
          <!-- 时间粒度：按钮组（与 Compare 保持一致） -->
          <a-radio-group v-model:value="queryType" button-style="solid" class="custom-radio-group" @change="handleQueryTypeChange">
            <a-radio-button value="day">日</a-radio-button>
            <a-radio-button value="month">月</a-radio-button>
            <a-radio-button value="year">年</a-radio-button>
          </a-radio-group>

          <!-- 时间范围选择（与 Compare 一致） -->
          <div class="flex items-center gap-4 flex-nowrap">
            <a-range-picker
              v-model:value="dateRange"
              :format="dateFormat"
              :picker="rangePickerType"
              class="w-64 custom-picker"
            />
          </div>

          <!-- 仪表选择（仅用电维度显示） -->
          <div class="flex items-center" v-if="isElectric">
            <span class="text-gray-600 text-sm mr-2 whitespace-nowrap">仪表：</span>
            <a-select
              v-model:value="selectedMeter"
              :options="instrumentOptions"
              :loading="instrumentLoading"
              style="width: 220px"
              class="custom-select"
              placeholder="请选择仪表"
              :dropdownMatchSelectWidth="false"
              @change="handleMeterChange"
            />
          </div>
          <!-- 查询按钮 -->
          <a-button type="primary" class="custom-button" @click="handleQuery">查询</a-button>
        </div>
      </div>

      <!-- 顶部统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 改为尖时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">尖时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.cuspConsumption }}
          </div>
        </div>
        <!-- 峰时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">峰时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.peakConsumption }}
          </div>
        </div>
        <!-- 改为平时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">平时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.leveConsumption }}
          </div>
        </div>
        <!-- 改为谷时能耗 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">谷时能耗(kWh)</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ statisticsData.valleyConsumption }}
          </div>
        </div>
      </div>

      <!-- 饼图和柱状图区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">尖峰平谷</div>
          <ConsumptionPie :chartData="pieChartData" />
        </div>
        <div class="bg-white rounded p-3">
          <div class="text-gray-600 text-sm mb-2">能耗趋势</div>
          <ConsumptionBar :chartData="barChartData" />
        </div>
      </div>

      <!-- 折线图区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="text-gray-600 text-sm mb-2">能耗分析</div>
        <ConsumptionLine :chartData="lineChartData" />
      </div>


    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import ConsumptionPie from './components/ConsumptionPie.vue';
import ConsumptionBar from './components/ConsumptionBar.vue';
import ConsumptionLine from './components/ConsumptionLine.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { getModulesByDimension, type ModuleVO } from '../Energy_Analysis_Compare/api';
import { defHttp } from '/@/utils/http/axios';
import { Dayjs } from 'dayjs';

// 左侧维度（动态 Tabs + DimensionTree）
const activeTabKey = ref('info1');
const currentNowtype = ref(1);
const currentOrgCode = ref('');

const dimensionList = ref<any[]>([]);
const treeRefs = ref<Record<string, any>>({});
const setTreeRef = (el, key) => { if (el) treeRefs.value[key] = el; };

// 存储每个标签页选中的节点信息
const selectedNodesMap = ref<Record<string, any>>({
  info1: null,
  info2: null,
  info3: null,
  info4: null,
  info5: null,
});

// 查询类型
const queryType = ref('month');

// 仪表选择（动态加载，仅用电维度显示）
const instrumentList = ref<ModuleVO[]>([]);
const selectedMeter = ref<string | null>(null);
const instrumentLoading = ref(false);
const instrumentOptions = computed(() => instrumentList.value.map(i => ({ label: i.moduleName, value: i.moduleId })));
const isElectric = computed(() => [1, 2].includes(currentNowtype.value));

// 时间范围（与 Compare 一致）
const dateRange = ref<[Dayjs, Dayjs] | null>(null);
const dateFormat = computed(() => {
  switch (queryType.value) {
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
const rangePickerType = computed(() => (queryType.value === 'year' ? 'year' : queryType.value));

// 统计数据
interface StatisticsData {
  cuspConsumption: number;    // 尖时能耗
  peakConsumption: number;     // 峰时能耗
  leveConsumption: number;     // 平时能耗
  valleyConsumption: number;   // 谷时能耗
}

// 静态统计数据
const statisticsData = ref<StatisticsData>({
  cuspConsumption: 8668433.80,
  peakConsumption: 5424683.40,
  leveConsumption: 24540.23,
  valleyConsumption: 3243750.40
});

// 饼图数据
const pieChartData = ref({
  series: [
    {
      name: '能耗分布',
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 1424683.40, name: '尖峰能耗' },
        { value: 2067865.00, name: '峰时能耗' },
        { value: 3161614.60, name: '平时能耗' },
        { value: 2014444.80, name: '谷时能耗' }
      ]
    }
  ]
});

// 柱状图数据
const barChartData = ref({
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  series: [
    {
      name: '能耗量',
      type: 'bar',
      data: [320, 280, 250, 340, 360, 320]
    }
  ]
});

// 折线图数据
const lineChartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '尖峰',
      type: 'line',
      data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330]
    },
    {
      name: '峰时',
      type: 'line',
      data: [220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149]
    },
    {
      name: '平时',
      type: 'line',
      data: [150, 232, 201, 154, 190, 330, 410, 182, 191, 234, 290, 330]
    },
    {
      name: '谷时',
      type: 'line',
      data: [320, 332, 301, 334, 390, 330, 320, 132, 142, 244, 190, 130]
    }
  ]
});

// 加载维度字典数据（与 Compare 保持一致）
function loadDimensionDictData() {
  defHttp
    .get({ url: '/sys/dict/getDictItems/dimensionCode' })
    .then((res) => {
      if (res && Array.isArray(res)) {
        dimensionList.value = res
        .filter((_, index) => index === 0 || index === 1) 
        .map((item, index) => ({
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
          //{ key: 'info3', title: '天然气', nowtype: 3, value: 3 },
          //{ key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
          //{ key: 'info5', title: '企业用水', nowtype: 5, value: 5 },
        ];
      }
    })
    .catch(() => {
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
        //{ key: 'info3', title: '天然气', nowtype: 3, value: 3 },
        //{ key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
        //{ key: 'info5', title: '企业用水', nowtype: 5, value: 5 },
      ];
    });
}

// 标签页切换
async function handleTabChange(key) {
  activeTabKey.value = key;
  const selectedDimension = dimensionList.value.find((item) => item.key === key);
  if (selectedDimension) {
    currentNowtype.value = selectedDimension.nowtype;
  }
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    currentOrgCode.value = savedNode.orgCode;
    await refreshDataBasedOnSelection();
  }
  nextTick(() => {
    const current = treeRefs.value[key];
    if (current && !savedNode) {
      // 由树组件完成默认选择
    }
  });
}

// 左侧树选择
async function onDepartTreeSelect(data) {
  if (Array.isArray(data) && data.length > 0) {
    const orgCodestr = data.map((item) => item.orgCode).join(',');
    currentOrgCode.value = orgCodestr;
    selectedNodesMap.value[activeTabKey.value] = { orgCode: orgCodestr, data };
  } else if (data && data.orgCode) {
    currentOrgCode.value = data.orgCode;
    selectedNodesMap.value[activeTabKey.value] = { orgCode: data.orgCode, data };
  }
  await refreshDataBasedOnSelection();
}

// 根据选择刷新：仅用电维度加载仪表，其他维度隐藏
async function refreshDataBasedOnSelection() {
  if (!currentOrgCode.value) return;
  if ([1, 2].includes(currentNowtype.value)) {
    await loadInstruments(currentOrgCode.value, currentNowtype.value);
  } else {
    instrumentList.value = [];
    selectedMeter.value = null;
  }
}

// 根据维度获取仪表列表（复用 Compare 的API）
async function loadInstruments(orgCode?: string, nowtype?: number) {
  if (!orgCode) return;
  try {
    instrumentLoading.value = true;
    const energyType = nowtype ?? currentNowtype.value;
    const result = await getModulesByDimension({ orgCode, energyType, includeChildren: false });
    instrumentList.value = result || [];
    selectedMeter.value = instrumentList.value[0]?.moduleId || null;
  } catch (error) {
    instrumentList.value = [];
    selectedMeter.value = null;
  } finally {
    instrumentLoading.value = false;
  }
}

onMounted(async () => {
  loadDimensionDictData();
  await nextTick();
  setTimeout(async () => {
    if (!selectedMeter.value && currentOrgCode.value && currentNowtype.value) {
      await loadInstruments(currentOrgCode.value, currentNowtype.value);
    }
  }, 500);
});





// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // TODO: 根据选中节点更新数据
};

// 处理查询类型变化
const handleQueryTypeChange = (value: string) => {
  // 切换时间粒度时，清空当前范围
  dateRange.value = null;
};

// 处理仪表选择变化
const handleMeterChange = (value: string) => {
  console.log('选择的仪表：', value);
  // TODO: 根据选择的仪表更新数据
};

// 处理查询
const handleQuery = () => {
  if (!dateRange || !dateRange.value || dateRange.value.length !== 2) {
    console.warn('请选择时间范围');
    return;
  }

  const start = dateRange.value[0].format(dateFormat.value);
  const end = dateRange.value[1].format(dateFormat.value);

  const queryParams = {
    type: queryType.value,
    startTime: start,
    endTime: end,
    orgCode: currentOrgCode.value || undefined,
    nowtype: currentNowtype.value,
    moduleId: isElectric.value ? selectedMeter.value : undefined,
  };

  console.log('查询参数：', queryParams);
  // TODO: 根据查询参数更新数据
};
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

/* 顶部控件统一高度样式（与 Compare 一致） */
.custom-button { height: 36px; display: flex; align-items: center; padding: 0 16px; }
:deep(.custom-picker) { height: 36px; }
:deep(.custom-picker .ant-picker-input) { height: 36px; display: flex; align-items: center; }
:deep(.custom-select) { height: 36px; }
:deep(.custom-select .ant-select-selector) { height: 36px !important; padding-top: 3px !important; }
:deep(.custom-radio-group) { height: 36px; display: inline-flex; }
:deep(.custom-radio-group .ant-radio-button-wrapper) { height: 36px; line-height: 34px; display: inline-flex; align-items: center; }

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