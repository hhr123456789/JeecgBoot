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

    <!-- 右侧内容区域：能源消耗统计 -->
    <div class="flex-1" style="margin-top: 16px;" >
      <!-- 查询卡片（白底） -->
      <div class="bg-white rounded p-4">
        <!-- 条件查询 -->
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
            <a-select v-model:value="displayMode" style="width: 120px">
              <a-select-option v-for="opt in queryMethodOptions" :key="opt.value" :value="String(opt.value)">{{ opt.text }}</a-select-option>
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
      <div v-show="isUnifiedView" class="bg-white rounded p-4">
        <div ref="chartRef" style="width: 100%; height: 420px;"></div>
      </div>
      <div v-show="!isUnifiedView">
        <div v-for="(chart, idx) in separateChartsData" :key="`sep-${idx}`" class="bg-white rounded p-4 mb-3">
          <div class="text-sm mb-2">{{ chart.title }}（单位：{{ chart.unit }}）</div>
          <MonitorChart :chartData="chart" :chartId="`sep-${idx}`" :activeIndex="-1" :chartType="chartType" />
        </div>
      </div>

    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, computed, watch, type Ref } from 'vue';
import MonitorChart from '../EnergyManagement/Real_Data_Monitor/components/MonitorChart.vue';
import { getConsumptionData, exportConsumptionData, type ConsumptionRequest, type ConsumptionResponse } from './api';
import { initDictOptions } from '/@/utils/dict';
import { downloadByData } from '/@/utils/file/download';

// 调试开关：需要时可以改成 false 屏蔽日志
const DBG = false;
function dbg(...args:any[]){ if (DBG) console.log('[Consumption]', ...args); }

import { useECharts } from '/@/hooks/web/useECharts';
import MultiSelectDimensionTree from '../EnergyManagement/Real_Data_Monitor/components/MultiSelectDimensionTree.vue';
import { getModulesByOrgCode } from '../EnergyManagement/Real_Data_Monitor/api';
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
    await nextTick();
    handleQuery();
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
// 避免并发响应覆盖：每次查询生成一个请求id，只处理最新的
const lastReqIdRef = ref(0);

// 查询方式字典（统一/分开）
const queryMethodOptions = ref<Array<{ text: string; value: string | number }>>([]);
const displayMode = ref<'1' | '2' | 'unified' | 'separated'>('1');

// 统一视图开关：当 UI 选统一且 unifiedChart 有数据时展示；否则展示分开
const isUnifiedView = computed(() => {
  const uiUnified = displayMode.value === '1' || displayMode.value === 'unified';
  const hasUnifiedData = (unifiedChart.value.series || []).length > 0;
  return uiUnified && hasUnifiedData;
});

async function loadQueryMethodDict() {
  try {
    const res = await initDictOptions('queryMethod');
    queryMethodOptions.value = Array.isArray(res) && res.length ? res : [
      { text: '统一显示', value: '1' },
      { text: '分开显示', value: '2' }
    ];
    if (!displayMode.value) displayMode.value = String(queryMethodOptions.value[0].value) as any;
  } catch (e) {
    queryMethodOptions.value = [
      { text: '统一显示', value: '1' },
      { text: '分开显示', value: '2' }
    ];
    displayMode.value = '1';
  }
}

// 通用解析：支持 [label,value] 或 {label/value} 或 {x/y}
function parsePoint(pt: any): { label: string; value: number } {
  if (Array.isArray(pt)) return { label: String(pt[0]), value: Number(pt[1]) };
  const label = pt?.label ?? pt?.date ?? pt?.time ?? pt?.x ?? pt?.dt ?? '';
  const value = pt?.value ?? pt?.y ?? pt?.energy ?? 0;
  return { label: String(label), value: Number(value) };
}

// 能耗查询
const unifiedChart = ref<{ categories: string[]; series: any[] }>({ categories: [], series: [] });
const separateChartsData = ref<any[]>([]);

async function handleQuery() {
  dbg('handleQuery:start', { mode: displayMode.value, selected: selectedMeterKeys.value.length, isUnifiedView: isUnifiedView.value });
  if (!selectedMeterKeys.value.length) { 
    unifiedChart.value = { categories: [], series: [] }; 
    separateChartsData.value = []; 
    renderChart(); 
    return; 
  }
  
  const [start, end] = dateRange.value as any;
  const req: ConsumptionRequest = {
    moduleIds: selectedMeterKeys.value,
    startDate: periodType.value === 'day' ? start.format('YYYY-MM-DD') : periodType.value === 'month' ? start.format('YYYY-MM') : start.format('YYYY'),
    endDate: periodType.value === 'day' ? end.format('YYYY-MM-DD') : periodType.value === 'month' ? end.format('YYYY-MM') : end.format('YYYY'),
    timeType: periodType.value,
    displayMode: Number(displayMode.value as any)
  };
  
  dbg('handleQuery:req', req);
  const reqId = (lastReqIdRef.value += 1);
  
  try {
    const raw = await getConsumptionData(req);
    // 只处理最后一次查询的响应
    if (reqId !== lastReqIdRef.value) return;

    const res: any = (raw && (raw as any).result) ? (raw as any).result : raw;
    const dm: any = res?.displayMode;
    const mode = dm === 1 || dm === '1' || dm === 'unified' ? 'unified' : 'separated';
    dbg('handleQuery:respMode', { dm, mode, uiMode: displayMode.value });

    if (mode === 'unified') {
      const categories = Array.isArray(res.series) && res.series.length ? 
        (res.series[0].data || []).map((pt: any) => parsePoint(pt).label) : [];

      unifiedChart.value = {
        categories,
        series: (res.series || []).map((s: any) => ({ 
          name: `${s.moduleName ? s.moduleName + '-' : ''}${s.name || '能耗'}(${s.unit})`, 
          unit: s.unit, 
          data: (s.data || []).map((pt: any) => parsePoint(pt).value) 
        }))
      };
      separateChartsData.value = [];
    } else {
      // 分开显示：每个设备一张图，每张图包含能耗曲线
      separateChartsData.value = (res.charts || []).map((c: any) => {
        const categories = (c.series?.[0]?.data || []).map((pt: any) => parsePoint(pt).label);
        const series = (c.series || []).map((s: any) => ({ 
          name: s.name || '能耗', 
          unit: s.unit || c.unit, 
          data: (s.data || []).map((pt: any) => parsePoint(pt).value) 
        }));
        return { title: c.title, unit: c.unit, categories, series };
      });
      unifiedChart.value = { categories: [], series: [] };

      // 若UI当前为统一显示但后端返回分开结构，临时合并为统一结构避免空白
      if (displayMode.value === '1' || displayMode.value === 'unified') {
        const charts: any[] = res.charts || [];
        dbg('merge separated->unified begin', { chartsLen: charts.length });
        if (charts.length) {
          const categories = (charts[0]?.series?.[0]?.data || []).map((pt: any) => parsePoint(pt).label);
          const mergedSeries: any[] = [];
          charts.forEach((c: any) => {
            (c.series || []).forEach((s: any) => {
              mergedSeries.push({
                name: `${c.title}-${s.name || '能耗'}(${s.unit || c.unit || ''})`,
                unit: s.unit || c.unit || '',
                data: (s.data || []).map((pt: any) => parsePoint(pt).value),
              });
            });
          });
          dbg('merge separated->unified set', { catLen: categories.length, seriesLen: mergedSeries.length });
          unifiedChart.value = { categories, series: mergedSeries };
          separateChartsData.value = [];
        } else {
          dbg('merge separated->unified skip: charts empty');
        }
      }
    }
  } catch (e) {
    console.error('Consumption API error:', e);
    unifiedChart.value = { categories: [], series: [] };
    separateChartsData.value = [];
  }
  await nextTick();
  renderChart();
}

async function handleExport() {
  const [start, end] = dateRange.value as any;
  const req: ConsumptionRequest = {
    moduleIds: selectedMeterKeys.value,
    startDate: periodType.value === 'day' ? start.format('YYYY-MM-DD') : periodType.value === 'month' ? start.format('YYYY-MM') : start.format('YYYY'),
    endDate: periodType.value === 'day' ? end.format('YYYY-MM-DD') : periodType.value === 'month' ? end.format('YYYY-MM') : end.format('YYYY'),
    timeType: periodType.value,
    displayMode: Number(displayMode.value as any)
  };
  
  try {
    const data: any = await exportConsumptionData(req);
    if (data) {
      const now = dayjs().format('YYYYMMDD_HHmmss');
      downloadByData(data, `设备能源统计_${now}.xlsx`, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    }
  } catch (e) {
    console.error('Export error:', e);
  }
}

// 右侧：图表
const chartRef = ref<HTMLDivElement | null>(null);
const chartType = ref<'line' | 'bar'>('line');

// 右侧"仪表选择"与左侧下拉联动（多选）
function handleMeterSelectChange(vals: string[]) {
  selectedMeterKeys.value = vals;
}

const { setOptions, getInstance } = useECharts(chartRef as Ref<HTMLDivElement>);

function renderChart() {
  if (displayMode.value === '2' || displayMode.value === 'separated') {
    dbg('renderChart:skip separated');
    return;
  }

  const el = chartRef.value as HTMLDivElement | null;
  dbg('renderChart:enter', { 
    isUnifiedView: isUnifiedView.value, 
    elExists: !!el, 
    elH: el?.offsetHeight, 
    hasInstance: !!getInstance(), 
    seriesLen: unifiedChart.value.series?.length, 
    catLen: unifiedChart.value.categories?.length 
  });

  // 切回统一显示时，确保 ECharts 容器已初始化
  if (!getInstance()) {
    // 触发一次初始化
    setOptions({});
  }

  const categories = unifiedChart.value.categories || [];
  const series = (unifiedChart.value.series || []) as Array<{name:string; data:number[]; unit?:string}>;
  
  // 能耗量统计的颜色主题（绿色系为主）
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'];

  // 根据不同能源类型的单位进行分轴处理
  const units = Array.from(new Set(series.map((s:any)=>s.unit||'').filter(Boolean)));
  
  let yAxis: any;
  if (units.length <= 1) {
    // 单一单位或无单位
    yAxis = [{ type:'value', name: units[0] || '能耗量' }];
  } else if (units.length === 2) {
    // 两种单位：左右双轴
    yAxis = [
      { type:'value', name: units[0], position:'left' }, 
      { type:'value', name: units[1], position:'right' }
    ];
  } else {
    // 多种单位：只显示左轴，在tooltip中显示具体单位
    yAxis = [{ type:'value', name: '能耗量（多单位）' }];
  }

  dbg('renderChart:setOptions', { 
    units, 
    yAxisCount: yAxis.length,
    names: series.map((s:any)=>s.name) 
  });
  
  setOptions({
    tooltip: { 
      trigger: 'axis',
      formatter: function(params: any) {
        let content = `${params[0].axisValue}<br/>`;
        params.forEach((param: any, index: number) => {
          const seriesItem = series[index];
          const unit = seriesItem?.unit || '';
          content += `${param.marker}${param.seriesName}: ${param.value}${unit ? ' ' + unit : ''}<br/>`;
        });
        return content;
      }
    },
    legend: { data: series.map((s:any)=>s.name) },
    grid: { left: '3%', right: '3%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: categories },
    yAxis,
    series: series.map((s: any, index: number) => ({
      name: s.name,
      type: chartType.value,
      smooth: true,
      yAxisIndex: units.length === 2 && s.unit === units[1] ? 1 : 0,
      data: s.data,
      color: colors[index % colors.length],
    })),
  });
}

// 自动查询：以下变更触发查询
watch(displayMode, () => handleQuery());
watch(periodType, () => { onPeriodChange(); handleQuery(); });
watch(selectedMeterKeys, (v) => { if ((v || []).length) handleQuery(); });
watch(dateRange, () => handleQuery(), { deep: true });
watch(meters, (list) => { 
  if ((list || []).length && !selectedMeterKeys.value.length) {
    selectedMeterKeys.value = list.map((m:any)=>m.value);
    handleQuery();
  }
}, { deep: true });

// 图表类型变化时重新渲染图表
watch(chartType, () => {
  nextTick(() => {
    dbg('watch chartType changed, renderChart');
    renderChart();
  });
});

// 统一视图刚显示时，主动渲染一次（防止没有新请求但已有数据时不渲染）
watch(isUnifiedView, (v) => { 
  if (v) nextTick(() => { 
    dbg('watch isUnifiedView -> true, renderChart'); 
    renderChart(); 
  }); 
});

onMounted(() => {
  loadDimensionDictData();
  loadQueryMethodDict();
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