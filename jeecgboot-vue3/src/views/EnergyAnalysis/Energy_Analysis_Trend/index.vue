<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧维度树（动态，多选），复用实时监测的组件 -->
    <div class="bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs :defaultActiveKey="activeTabKey" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
            <a-card :bordered="false" style="height: 100%">
              <MultiSelectDimensionTree
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

    <!-- 右侧内容区域：碳排放趋势分析 -->
    <div class="flex-1" style="margin-top: 16px;" >
      <!-- 查询卡片（白底） -->
      <div class="bg-white rounded p-4">
        <!-- 条件查询（去除：参数选择、查询间隔） -->
        <div class="flex flex-wrap items-center gap-3">
          <div class="flex items-center">
            <span class="text-gray-600 mr-2">时间范围：</span>
            <a-radio-group v-model:value="periodType" button-style="solid" class="mr-2" @change="onPeriodChange">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
            <a-range-picker v-model:value="dateRange" :picker="pickerType" :format="dateFormat" style="width: 360px" />
          </div>
          <div class="flex items-center">
            <span class="text-gray-600 mr-2">仪表选择：</span>
            <a-select
              v-model:value="selectedMeterKeys"
              mode="multiple"
              style="min-width: 180px"
              @change="handleMeterSelectChange"
              :max-tag-count="2"
            >
              <a-select-option v-for="meter in meters" :key="meter.value" :value="meter.value">
                {{ meter.label }}
              </a-select-option>
            </a-select>
          </div>
          <div class="flex items-center">
            <span class="text-gray-600 mr-2">查询方式：</span>
            <a-select v-model:value="queryMode" style="width: 120px">
              <a-select-option value="merge">统一显示</a-select-option>
            </a-select>
          </div>
          <div class="flex items-center">
            <span class="text-gray-600 mr-2">图表类型：</span>
            <a-radio-group v-model:value="chartType" size="small">
              <a-radio-button value="line">曲线图</a-radio-button>
              <a-radio-button value="bar">柱状图</a-radio-button>
            </a-radio-group>
          </div>
          <br>
          <a-button type="primary" @click="handleQuery">查询</a-button>
          <a-button @click="handleExport">导出数据</a-button>
        </div>
      </div>

      <!-- 间隔区域，显示页面灰色背景 -->
      <div style="height: 10px;"></div>

      <!-- 图表卡片（白底） -->
      <div class="bg-white rounded p-4">
        <div ref="chartRef" style="width: 100%; height: 420px;"></div>
      </div>

    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, type Ref } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import MultiSelectDimensionTree from '../../EnergyManagement/Real_Data_Monitor/components/MultiSelectDimensionTree.vue';
import { getModulesByOrgCode } from '../../EnergyManagement/Real_Data_Monitor/api';
import { defHttp } from '/@/utils/http/axios';
// 维度树 tabs & 选择
const dimensionList = ref<any[]>([]);
const activeTabKey = ref('info1');
const currentNowtype = ref(1);
const treeRefs = ref<Record<string, any>>({});
const setTreeRef = (el, key) => { if (el) treeRefs.value[key] = el; };

// 仪表（由维度选择动态加载），用于右侧下拉和图表聚合
const meters = ref<Array<{ label: string; value: string }>>([]);
const allModules = ref<any[]>([]);

// 维度字典 -> tabs
function loadDimensionDictData() {
  defHttp.get({ url: '/sys/dict/getDictItems/dimensionCode' })
    .then((res: any) => {
      if (Array.isArray(res)) {
        dimensionList.value = res.map((item, index) => ({ key: `info${index + 1}`, title: item.text, nowtype: Number(item.value), value: Number(item.value) }));
        if (dimensionList.value.length) {
          activeTabKey.value = dimensionList.value[0].key;
          currentNowtype.value = dimensionList.value[0].nowtype;
        }
      } else {
        // 兜底
        dimensionList.value = [
          { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
          { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
        ];
        activeTabKey.value = 'info1';
        currentNowtype.value = 1;
      }
    })
    .catch(() => {
      dimensionList.value = [
        { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
        { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
      ];
      activeTabKey.value = 'info1';
      currentNowtype.value = 1;
    });
}

// 树选择（支持多选）
function onDepartTreeSelect(data: any[]) {
  const orgCodes = Array.isArray(data) ? data.map((n) => n.orgCode).filter(Boolean) : [];
  if (orgCodes.length) {
    loadModulesByOrgCodes(orgCodes);
  } else {
    meters.value = [];
    allModules.value = [];
    selectedMeterKeys.value = [];
    checkedKeys.value = [];
    renderChart();
  }
}

async function loadModulesByOrgCodes(orgCodes: string[]) {
  try {
    const res: any = await getModulesByOrgCode({ orgCodes: orgCodes.join(','), nowtype: String(currentNowtype.value || 1), includeChildren: true });
    const list = Array.isArray(res) ? res : res?.result || [];
    allModules.value = list;
    meters.value = list.map((m: any) => ({ label: m.moduleName, value: m.moduleId }));
    selectedMeterKeys.value = meters.value.map((m) => m.value);
    checkedKeys.value = [...selectedMeterKeys.value];
    renderChart();
  } catch (e) {
    meters.value = [];
    allModules.value = [];
    selectedMeterKeys.value = [];
    checkedKeys.value = [];
    renderChart();
  }
}

function handleTabChange(key) {
  activeTabKey.value = key;
  const dim = dimensionList.value.find((d) => d.key === key);
  if (dim) currentNowtype.value = dim.nowtype;
  // 等树渲染后默认展开第2级并触发一次select
  nextTick(() => {
    const tree = treeRefs.value[key];
    if (tree && tree.autoExpandToTargetLevelNode) {
      setTimeout(() => tree.autoExpandToTargetLevelNode(2), 100);
    }
  });
}


// 日/月/年 切换
import dayjs, { Dayjs } from 'dayjs';
const periodType = ref<'day' | 'month' | 'year'>('day');
const pickerType = computed(() => (periodType.value === 'day' ? 'date' : periodType.value));
const dateFormat = computed(() => (periodType.value === 'year' ? 'YYYY' : periodType.value === 'month' ? 'YYYY-MM' : 'YYYY-MM-DD'));

// 时间范围
const dateRange = ref<[Dayjs, Dayjs]>([dayjs().startOf('month'), dayjs()]);

function onPeriodChange() {
  if (periodType.value === 'day') {
    dateRange.value = [dayjs().startOf('month'), dayjs()];
  } else if (periodType.value === 'month') {
    dateRange.value = [dayjs().subtract(1, 'month').startOf('month'), dayjs().endOf('month')];
  } else if (periodType.value === 'year') {
    dateRange.value = [dayjs().subtract(1, 'year').startOf('year'), dayjs().endOf('year')];
  }
}

// 选择的仪表keys（与左侧树和右侧下拉联动）
const checkedKeys = ref<string[]>([]);
const selectedMeterKeys = ref<string[]>([]);

// 查询与导出（简单占位）
function handleQuery() {
  renderChart();
}
function handleExport() {
  // TODO: 接入导出接口
}

// 右侧：趋势图
const chartRef = ref<HTMLDivElement | null>(null);
// 条件查询控件（periodType/dateRange 已在上方定义）
const queryMode = ref<'merge'>('merge');
const chartType = ref<'line' | 'bar'>('line');

// 右侧“仪表选择”与左侧树联动（多选）
function handleMeterSelectChange(vals: string[]) {
  selectedMeterKeys.value = vals;
  checkedKeys.value = vals;
  renderChart();
}

const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

// 横轴（静态 1~23日）
const xLabels = Array.from({ length: 23 }, (_, i) => `${i + 1}日`);

type SeriesPair = { carbon: number[]; coal: number[] };
function gen(seed: number): SeriesPair {
  const carbon = xLabels.map((_, i) => Math.round(220000 + Math.sin((i + seed) / 3) * 60000));
  const coal = xLabels.map((_, i) => Math.round(52000 + Math.cos((i + seed) / 4) * 12000));
  return { carbon, coal };
}

// 每个仪表的静态数据容器（动态按模块ID生成）
const meterData: Record<string, SeriesPair> = {};
function seedFrom(id: string): number {
  return id.split('').reduce((s, c) => s + c.charCodeAt(0), 0) % 9;
}
function getSeriesPair(id: string): SeriesPair {
  if (!meterData[id]) meterData[id] = gen(seedFrom(id));
  return meterData[id];
}

function aggregate(ids: string[]): SeriesPair {
  const total: SeriesPair = { carbon: xLabels.map(() => 0), coal: xLabels.map(() => 0) };
  ids.forEach((id) => {
    const d = getSeriesPair(id);
    d.carbon.forEach((v, i) => (total.carbon[i] += v));
    d.coal.forEach((v, i) => (total.coal[i] += v));
  });
  return total;
}

function renderChart() {
  const ids = Array.isArray(checkedKeys.value) ? (checkedKeys.value as string[]) : [];
  const agg = aggregate(ids);
  setOptions({
    tooltip: { trigger: 'axis' },
    legend: { data: ['折标煤(kgce)', '碳排放(kgCO₂e)'] },
    grid: { left: '3%', right: '3%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: xLabels },
    yAxis: [
      { type: 'value', name: '折标煤(kgce)' },
      { type: 'value', name: '碳排放(kgCO₂e)', position: 'right' },
    ],
    series: [
      { name: '折标煤(kgce)', type: chartType.value, smooth: true, yAxisIndex: 0, data: agg.coal, color: '#2ecc71' },
      { name: '碳排放(kgCO₂e)', type: chartType.value, smooth: true, yAxisIndex: 1, data: agg.carbon, color: '#ff9f40' },
    ],
  });
}

// 树选择（多选）
function handleCheck(checked: any) {
  checkedKeys.value = Array.isArray(checked) ? checked : checked.checked;
  selectedMeterKeys.value = [...checkedKeys.value];
  renderChart();
}

onMounted(() => {
  // 加载维度字典并等待树触发默认选择
  loadDimensionDictData();
  renderChart();
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