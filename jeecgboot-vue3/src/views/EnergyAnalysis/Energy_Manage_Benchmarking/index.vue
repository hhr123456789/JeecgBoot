<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧维度树 -->
    <div class="w-64 flex-shrink-0">
      <a-tabs v-model:activeKey="activeTabKey" @change="handleTabChange" size="small">
        <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title">
          <DimensionTree
            :ref="(el) => setTreeRef(el, item.key)"
            @select="handleDepartTreeSelect"
            :nowtype="item.nowtype"
            :select-level="2"
          />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1 ml-2 overflow-auto">
      <!-- 顶部统计卡片 -->
      <a-spin :spinning="statsLoading">
        <div class="bg-white rounded p-4 mb-4">
          <div class="text-lg font-medium mb-2">能耗对标分析</div>
          <div class="grid grid-cols-3 gap-4">
            <div class="bg-gray-50 rounded p-3">
              <div class="text-sm text-gray-600 mb-2">平均能耗强度指标（kgce/㎡）</div>
              <div class="text-2xl font-bold">{{ statsData.avgIntensity ?? '--' }}</div>
            </div>
            <div class="bg-gray-50 rounded p-3">
              <div class="text-sm text-gray-600 mb-2">最优能耗强度指标（kgce/㎡）</div>
              <div class="text-2xl font-bold">{{ statsData.bestIntensity ?? '--' }}</div>
            </div>
            <div class="bg-gray-50 rounded p-3">
              <div class="text-sm text-gray-600 mb-2">方差系数标准差除以均值(kWh/t)</div>
              <div class="text-2xl font-bold">{{ statsData.varianceCoefficient ?? '--' }}</div>
            </div>
          </div>
        </div>
      </a-spin>

      <!-- 能耗对标趋势图 -->
      <a-spin :spinning="trendLoading">
        <div class="bg-white rounded p-4 mb-4">
          <div class="flex items-center justify-between mb-4">
            <div class="text-base font-medium">能耗对标趋势</div>
            <div class="flex items-center">
              <a-radio-group v-model:value="timeUnit" button-style="solid" size="small">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
                <a-radio-button value="year">年</a-radio-button>
              </a-radio-group>
              <a-date-picker
                v-model:value="selectedDate"
                :picker="timeUnit === 'year' ? 'year' : timeUnit === 'month' ? 'month' : 'date'"
                size="small"
                class="ml-2"
                style="width: 120px"
              />
            </div>
          </div>
          <div class="h-80">
            <BenchmarkTrend :chartData="trendChartData" />
          </div>
        </div>
      </a-spin>

      <!-- 能耗对标分布 -->
      <a-spin :spinning="tableLoading">
        <div class="bg-white rounded p-4">
          <div class="flex items-center justify-between mb-4">
            <div class="text-base font-medium">能耗对标分布</div>
            <a-button type="primary" size="small" :loading="exportLoading" @click="handleExport">导出数据</a-button>
          </div>
          <a-table
            :columns="columns"
            :data-source="tableData"
            :pagination="false"
            size="middle"
          />
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, nextTick } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import BenchmarkTrend from './components/BenchmarkTrend.vue';
import { getBenchmarkStatistics, exportBenchmarkData } from './benchmark.api';

const { createMessage } = useMessage();

// ==================== 维度树相关 ====================
const activeTabKey = ref('');
const dimensionList = ref<{ key: string; title: string; nowtype: string; value: string }[]>([]);
const treeRefs = ref<Record<string, any>>({});
const selectedNodesMap = ref<Record<string, any>>({});
const selectedTargetCode = ref('');
const selectedTargetName = ref('');

const setTreeRef = (el: any, key: string) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 加载维度字典
const loadDimensionDict = async () => {
  try {
    const res = await defHttp.get({ url: '/sys/dict/getDictItems/dimensionCode' });
    if (res && res.length > 0) {
      dimensionList.value = res.map((item: any, index: number) => ({
        key: 'dim_' + index,
        title: item.text,
        nowtype: item.value,
        value: item.value,
      }));
      activeTabKey.value = dimensionList.value[0].key;
    }
  } catch (e) {
    console.error('加载维度字典失败', e);
  }
};

// 树节点选择
const handleDepartTreeSelect = (data: any) => {
  const code = data.orgCode || data.id || data.key || data.value;
  const name = data.departName || data.orgName || data.title || data.label || '';
  selectedNodesMap.value[activeTabKey.value] = data;
  selectedTargetCode.value = code;
  selectedTargetName.value = name;
  handleQuery();
};

// Tab切换
const handleTabChange = (key: string) => {
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    const code = savedNode.orgCode || savedNode.id || savedNode.key || savedNode.value;
    const name = savedNode.orgName || savedNode.title || savedNode.label || '';
    selectedTargetCode.value = code;
    selectedTargetName.value = name;
    handleQuery();
  }
};

// ==================== 时间与筛选 ====================
const timeUnit = ref('month');
const selectedDate = ref<Dayjs>(dayjs());

// ==================== 数据状态 ====================
const statsLoading = ref(false);
const trendLoading = ref(false);
const tableLoading = ref(false);
const exportLoading = ref(false);

// 统计卡片数据
const statsData = ref<{
  avgIntensity: number | null;
  bestIntensity: number | null;
  varianceCoefficient: number | null;
}>({
  avgIntensity: null,
  bestIntensity: null,
  varianceCoefficient: null,
});

// 趋势图数据
const trendChartData = ref({
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value', name: 'kgce/㎡' },
  series: [] as any[],
});

// 表格列定义
const columns: TableColumnsType = [
  { title: '对标对象', dataIndex: 'targetName', width: 150 },
  { title: '能耗强度(kgce/㎡)', dataIndex: 'energyIntensity', width: 150, align: 'right' },
  { title: '能耗总量(tce)', dataIndex: 'energyConsumption', width: 150, align: 'right' },
  { title: '产量(吨)', dataIndex: 'productionOutput', width: 150, align: 'right' },
  { title: '排名', dataIndex: 'ranking', width: 100, align: 'center' },
];

// 表格数据
const tableData = ref<any[]>([]);

// ==================== 数据加载 ====================
const buildParams = () => ({
  targetCode: selectedTargetCode.value,
  timeUnit: timeUnit.value,
  startTime: selectedDate.value?.format(
    timeUnit.value === 'year' ? 'YYYY' : timeUnit.value === 'month' ? 'YYYY-MM' : 'YYYY-MM-DD'
  ),
});

const handleQuery = async () => {
  if (!selectedTargetCode.value) return;

  const params = buildParams();
  statsLoading.value = true;
  trendLoading.value = true;
  tableLoading.value = true;

  try {
    const res = await getBenchmarkStatistics(params);
    if (res.success && res.result) {
      const { cards, trendData, tableData: tData } = res.result;

      // 统计卡片 - 映射后端字段名
      if (cards) {
        statsData.value = {
          avgIntensity: cards.avgIntensity ?? null,
          bestIntensity: cards.minIntensity ?? null,
          varianceCoefficient: cards.varianceCoeff ?? null,
        };
      }

      // 趋势图 - 将扁平数组转换为ECharts格式
      if (trendData && trendData.length > 0) {
        const grouped = new Map<string, { time: string; value: number }[]>();
        for (const item of trendData) {
          const name = item.targetName || '默认';
          if (!grouped.has(name)) grouped.set(name, []);
          grouped.get(name)!.push({ time: item.time, value: item.value });
        }
        const xAxisData = [...new Set(trendData.map((d: any) => d.time))];
        const series = [...grouped.entries()].map(([name, data]) => ({
          name,
          type: 'line',
          data: xAxisData.map((t) => {
            const found = data.find((d) => d.time === t);
            return found ? found.value : null;
          }),
        }));
        trendChartData.value = {
          xAxis: { type: 'category', data: xAxisData },
          yAxis: { type: 'value', name: 'kgce/㎡' },
          series,
        };
      } else {
        trendChartData.value = {
          xAxis: { type: 'category', data: [] },
          yAxis: { type: 'value', name: 'kgce/㎡' },
          series: [],
        };
      }

      // 表格数据
      tableData.value = tData || [];
    }
  } catch (e) {
    console.error('加载对标数据失败', e);
  } finally {
    statsLoading.value = false;
    trendLoading.value = false;
    tableLoading.value = false;
  }
};

// 导出
const handleExport = async () => {
  if (!selectedTargetCode.value) {
    createMessage.warning('请先选择对标目标');
    return;
  }
  exportLoading.value = true;
  try {
    const params = buildParams();
    const data = await exportBenchmarkData(params);
    if (data) {
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `能耗对标数据_${dayjs().format('YYYYMMDDHHmmss')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  } catch (e) {
    console.error('导出失败', e);
    createMessage.error('导出失败');
  } finally {
    exportLoading.value = false;
  }
};

// ==================== 监听器 ====================
watch([timeUnit, selectedDate], () => {
  if (selectedTargetCode.value) {
    handleQuery();
  }
});

// ==================== 初始化 ====================
onMounted(async () => {
  await loadDimensionDict();
});
</script>
