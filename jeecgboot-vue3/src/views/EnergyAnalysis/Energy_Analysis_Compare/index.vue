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
            <!-- 仪表选择（单选，API数据） -->
            <div class="flex items-center">
              <span class="text-gray-600 text-sm mr-2 whitespace-nowrap">仪表:</span>
              <a-select
                v-model:value="selectedInstrument"
                :options="instrumentOptions"
                :loading="instrumentLoading"
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
            <a-button type="primary" class="custom-button" :loading="queryLoading" @click="onQuery">查询</a-button>
            <a-button type="default" class="custom-button" :loading="exportLoading" @click="onExport">导出数据</a-button>
          </div>
          </div>

        </div>
      </div>

      <!-- 数据对比卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <!-- 基准期用量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">基准期用量 {{ currentUnit }}</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ typeof compareData.baseConsumption === 'number' ? compareData.baseConsumption.toLocaleString() : compareData.baseConsumption }}
          </div>
        </div>
        <!-- 对比期用量 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">对比期用量 {{ currentUnit }}</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center">
            {{ typeof compareData.compareConsumption === 'number' ? compareData.compareConsumption.toLocaleString() : compareData.compareConsumption }}
          </div>
        </div>
        <!-- 差值 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">差值 {{ currentUnit }}</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center"
               :class="compareData.energySaving >= 0 ? 'text-green-500' : 'text-red-500'">
            {{ typeof compareData.energySaving === 'number' ? compareData.energySaving.toLocaleString() : compareData.energySaving }}
          </div>
        </div>
        <!-- 增长率 -->
        <div class="bg-white rounded-lg p-3 shadow-sm">
          <div class="text-gray-600 text-sm mb-2">增长率 %</div>
          <div class="bg-gray-50 rounded-lg py-2 px-3 text-base font-medium text-center"
               :class="compareData.savingRate >= 0 ? 'text-red-500' : 'text-green-500'">
            {{ typeof compareData.savingRate === 'number' ? compareData.savingRate.toFixed(2) : compareData.savingRate }}
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
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import CompareChart from './components/CompareChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import {
  getModulesByDimension,
  getCompareData,
  getEnergyTypes,
  exportCompareData,
  type ModuleVO,
  type CompareDataRequest,
  type CompareDataVO
} from './api';

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
      // 树组件会自动默认选择并触发 select 事件
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

async function refreshDataBasedOnSelection() {
  // 加载仪表列表
  await loadInstruments(currentOrgCode.value, currentNowtype.value);

  // 如果有默认选中的仪表和时间范围，自动执行查询
  if (selectedInstrument.value && baseDateRange.value.length === 2) {
    console.log('🚀 仪表加载完成，自动执行查询');
    await onQuery();
  }
}

// 查询对比数据
const queryLoading = ref(false);
const exportLoading = ref(false);

async function onQuery() {
  if (!selectedInstrument.value) {
    message.warning('请先选择仪表');
    return;
  }

  if (!baseDateRange.value || !compareDateRange.value) {
    message.warning('请选择基准期和对比期时间范围');
    return;
  }

  // 当时间类型为日时，验证基准期与对比期的天数必须相同
  if (timeRange.value === 'day') {
    const baselineDays = baseDateRange.value[1].diff(baseDateRange.value[0], 'day') + 1;
    const compareDays = compareDateRange.value[1].diff(compareDateRange.value[0], 'day') + 1;

    if (baselineDays !== compareDays) {
      message.error(`基准期和对比期的天数必须相同！基准期：${baselineDays}天，对比期：${compareDays}天`);
      return;
    }
  }

  try {
    queryLoading.value = true;

    // 格式化时间
    const baseStart = baseDateRange.value[0].format(dateFormat.value);
    const baseEnd = baseDateRange.value[1].format(dateFormat.value);
    const compareStart = compareDateRange.value[0].format(dateFormat.value);
    const compareEnd = compareDateRange.value[1].format(dateFormat.value);

    const request: CompareDataRequest = {
      moduleId: selectedInstrument.value,
      timeType: timeRange.value,
      baselineStartTime: baseStart,
      baselineEndTime: baseEnd,
      compareStartTime: compareStart,
      compareEndTime: compareEnd
    };

    console.log('🚀 发送API请求:', request);

    let result;
    try {
      result = await getCompareData(request);
    } catch (error) {
      console.warn('⚠️ API调用失败，使用模拟数据:', error);
      // 使用模拟数据进行测试
      result = {
        summary: {
          baselineTotal: 1500.5,
          compareTotal: 1200.3,
          savingTotal: 300.2,
          unit: 'kWh'
        },
        chartData: {
          baselineDates: ['08-01', '08-02', '08-03', '08-04', '08-05', '08-06', '08-07'],
          compareDates: ['08-01', '08-02', '08-03', '08-04', '08-05', '08-06', '08-07'],
          series: [
            {
              name: '基准期',
              type: 'line',
              data: [200, 220, 180, 250, 300, 180, 170],
              unit: 'kWh'
            },
            {
              name: '对比期',
              type: 'line',
              data: [150, 180, 160, 200, 220, 150, 140],
              unit: 'kWh'
            },
            {
              name: '节能情况',
              type: 'bar',
              data: [50, 40, 20, 50, 80, 30, 30],
              unit: 'kWh'
            }
          ]
        },
        tableData: [
          {
            baselineDate: '2025-08-01',
            baselineValue: 200,
            compareDate: '2024-08-01',
            compareValue: 150,
            saving: '节约 50 kWh'
          },
          {
            baselineDate: '2025-08-02',
            baselineValue: 220,
            compareDate: '2024-08-02',
            compareValue: 180,
            saving: '节约 40 kWh'
          },
          {
            baselineDate: '2025-08-03',
            baselineValue: 180,
            compareDate: '2024-08-03',
            compareValue: 160,
            saving: '节约 20 kWh'
          }
        ]
      };
    }

    console.log('📥 API响应数据:', result);
    console.log('📊 图表数据:', result?.chartData);
    console.log('📋 表格数据:', result?.tableData);
    console.log('📈 汇总数据:', result?.summary);

    if (result && result.summary) {
      // 更新汇总数据 - 使用正确的字段映射
      const baselineTotal = result.summary.baselineTotal || 0;
      const compareTotal = result.summary.compareTotal || 0;
      const savingTotal = result.summary.savingTotal || 0;

      // 计算增长率：(对比期 - 基准期) / 基准期 * 100
      const growthRate = baselineTotal > 0 ? ((compareTotal - baselineTotal) / baselineTotal * 100) : 0;

      compareData.value = {
        baseConsumption: baselineTotal,
        compareConsumption: compareTotal,
        energySaving: savingTotal,
        savingRate: growthRate
      };

      console.log('✅ 汇总数据更新完成:', compareData.value);
      console.log('📊 字段映射详情:', {
        baselineTotal,
        compareTotal,
        savingTotal,
        growthRate: growthRate.toFixed(2) + '%'
      });

      // 更新图表数据
      console.log('🔄 开始更新图表数据...');
      console.log('📊 原始图表数据:', result.chartData);

      if (result.chartData) {
        // 兼容新旧两种数据格式
        let xAxisData, seriesData;

        if (result.chartData.baselineDates && result.chartData.series) {
          // 新格式：{baselineDates, compareDates, series}
          console.log('📅 使用新格式数据');
          console.log('📅 baselineDates:', result.chartData.baselineDates);
          console.log('📅 compareDates:', result.chartData.compareDates);
          console.log('📈 series:', result.chartData.series);

          xAxisData = result.chartData.baselineDates;
          seriesData = result.chartData.series.map(s => ({
            name: s.name,
            type: s.type || 'line',
            data: s.data,
            itemStyle: {
              color: s.name.includes('基准') ? '#1890ff' :
                     s.name.includes('对比') ? '#52c41a' :
                     '#ff7f0e'
            }
          }));
        } else if (result.chartData.categories && result.chartData.series) {
          // 旧格式：{categories, series}
          console.log('📅 使用旧格式数据');
          console.log('📅 categories:', result.chartData.categories);
          console.log('📈 series:', result.chartData.series);

          xAxisData = result.chartData.categories;
          seriesData = result.chartData.series.map(s => ({
            name: s.name,
            type: 'line',
            data: s.data,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: {
              color: s.name.includes('基准') || s.name.includes('本期') ? '#1890ff' : '#52c41a'
            },
            lineStyle: {
              color: s.name.includes('基准') || s.name.includes('本期') ? '#1890ff' : '#52c41a'
            },
            areaStyle: {
              opacity: 0.1,
              color: s.name.includes('基准') || s.name.includes('本期') ? '#1890ff' : '#52c41a'
            }
          }));
        } else {
          console.warn('⚠️ 图表数据格式不正确:', result.chartData);
          return;
        }

        chartData.value = {
          xAxis: {
            type: 'category',
            data: xAxisData
          },
          series: seriesData
        };

        console.log('✅ 图表数据更新完成:', chartData.value);
      } else {
        console.warn('⚠️ 图表数据为空:', result.chartData);
      }

      // 更新表格数据
      console.log('📋 原始表格数据:', result.tableData);
      if (result.tableData && Array.isArray(result.tableData)) {
        // 先查看第一条数据的结构
        if (result.tableData.length > 0) {
          console.log('📋 第一条数据结构:', result.tableData[0]);
        }

        tableData.value = result.tableData.map((item, index) => ({
          key: (index + 1).toString(),
          baselineDate: item.baselineDate || item.date || item.time || '--',
          baselineValue: item.baselineValue || item.currentConsumption || 0,
          compareDate: item.compareDate || item.date || item.time || '--',
          compareValue: item.compareValue || item.previousConsumption || 0,
          saving: item.savingText || item.saving || '--'  // 优先使用 savingText 字段
        }));
        console.log('📋 处理后表格数据:', tableData.value);
      } else {
        console.warn('⚠️ 表格数据为空或格式不正确:', result.tableData);
        tableData.value = [];
      }

      // 更新表格列标题中的单位
      const unit = result.summary?.unit || result.moduleInfo?.unit || 'kWh';
      updateTableColumns(unit);
    }
  } catch (error) {
    console.error('查询对比数据失败:', error);
  } finally {
    queryLoading.value = false;
  }
}


onMounted(async () => {
  loadDimensionDictData();

  // 等待DOM更新完成
  await nextTick();

  // 等待树组件初始化并自动选择默认节点
  // 给树组件一些时间来完成初始化和默认选择
  setTimeout(async () => {
    // 如果还没有选中的仪表，尝试手动触发数据加载
    if (!selectedInstrument.value && currentOrgCode.value && currentNowtype.value) {
      console.log('🔄 手动触发仪表数据加载');
      await loadInstruments(currentOrgCode.value, currentNowtype.value);
    }

    // 检查是否有默认选中的仪表和时间范围，自动执行查询
    if (selectedInstrument.value && baseDateRange.value.length === 2) {
      console.log('🚀 页面加载完成，自动执行查询');
      await onQuery();
    }
  }, 500); // 给树组件500ms的初始化时间
});

// 时间范围和日期选择
const timeRange = ref('day');
// 基准期：前7天（2025-07-31 到 2025-08-06）
const baseDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(14, 'day'), dayjs().subtract(8, 'day')]);
// 对比期：后7天（2025-08-07 到 2025-08-13）
const compareDateRange = ref<[Dayjs, Dayjs]>([dayjs().subtract(7, 'day'), dayjs().subtract(1, 'day')]);

// 监听时间类型切换，自动设置默认时间范围
watch(timeRange, (newTimeType) => {
  console.log('🕐 时间类型切换:', newTimeType);

  const now = dayjs();

  if (newTimeType === 'day') {
    // 日对比：基准期为前7天，对比期为后7天
    baseDateRange.value = [now.subtract(14, 'day'), now.subtract(8, 'day')];
    compareDateRange.value = [now.subtract(7, 'day'), now.subtract(1, 'day')];
  } else if (newTimeType === 'month') {
    // 月对比：基准期比对比期少1个月
    const currentMonth = now.format('YYYY-MM');
    const previousMonth = now.subtract(1, 'month').format('YYYY-MM');

    // 对比期：当前月
    compareDateRange.value = [dayjs(currentMonth), dayjs(currentMonth)];
    // 基准期：上个月
    baseDateRange.value = [dayjs(previousMonth), dayjs(previousMonth)];
  } else if (newTimeType === 'year') {
    // 年对比：基准期为去年，对比期为今年
    const currentYear = now.format('YYYY');
    const previousYear = now.subtract(1, 'year').format('YYYY');

    // 对比期：今年
    compareDateRange.value = [dayjs(currentYear), dayjs(currentYear)];
    // 基准期：去年
    baseDateRange.value = [dayjs(previousYear), dayjs(previousYear)];
  }

  console.log('📅 基准期设置为:', baseDateRange.value.map(d => d.format(dateFormat.value)));
  console.log('📅 对比期设置为:', compareDateRange.value.map(d => d.format(dateFormat.value)));
});

// 仪表选择（单选，使用API数据）
const instrumentList = ref<ModuleVO[]>([]);
const selectedInstrument = ref<string | null>(null);
const instrumentLoading = ref(false);
const instrumentOptions = computed(() =>
  instrumentList.value.map((i) => ({ label: i.moduleName, value: i.moduleId }))
);

// 根据维度获取仪表列表
async function loadInstruments(orgCode?: string, nowtype?: number) {
  if (!orgCode) return;

  try {
    instrumentLoading.value = true;
    const energyType = nowtype ?? currentNowtype.value;

    const result = await getModulesByDimension({
      orgCode: orgCode,
      energyType: energyType,
      includeChildren: false
    });

    instrumentList.value = result || [];
    selectedInstrument.value = instrumentList.value[0]?.moduleId || null;
  } catch (error) {
    console.error('获取仪表列表失败:', error);
    instrumentList.value = [];
    selectedInstrument.value = null;
  } finally {
    instrumentLoading.value = false;
  }
}

// 根据“日”粒度生成模拟数据：以当月天数为横坐标


// 监听时间粒度切换为“日”时，刷新模拟数据


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

// 对比数据（动态加载）
const compareData = ref<CompareData>({
  baseConsumption: 0,
  compareConsumption: 0,
  energySaving: 0,
  savingRate: 0
});

// 图表数据（动态加载）
const chartData = ref<{
  xAxis: {
    type: string;
    data: string[];
  };
  series: Array<{
    name: string;
    type: string;
    data: number[];
    itemStyle: {
      color: string;
    };
  }>;
}>({
  xAxis: {
    type: 'category',
    data: []
  },
  series: []
});

// 表格列定义（动态单位）
const currentUnit = ref('kWh');
const columns = computed<TableColumnsType>(() => [
  {
    title: '基准时间',
    dataIndex: 'baselineDate',
    width: '20%',
    align: 'center'
  },
  {
    title: `基准能耗(${currentUnit.value})`,
    dataIndex: 'baselineValue',
    width: '20%',
    align: 'center',
    customRender: ({ text }) => {
      return typeof text === 'number' ? text.toLocaleString() : '--';
    }
  },
  {
    title: '对比时间',
    dataIndex: 'compareDate',
    width: '20%',
    align: 'center'
  },
  {
    title: `对比能耗(${currentUnit.value})`,
    dataIndex: 'compareValue',
    width: '20%',
    align: 'center',
    customRender: ({ text }) => {
      return typeof text === 'number' ? text.toLocaleString() : '--';
    }
  },
  {
    title: '节能情况',
    dataIndex: 'saving',
    width: '20%',
    align: 'center',
    customRender: ({ text, record }) => {
      // 如果接口已经返回格式化的字符串，直接使用
      if (typeof text === 'string') {
        return text;
      }

      // 否则根据数值计算
      const baselineValue = record.baselineValue || 0;
      const compareValue = record.compareValue || 0;
      const delta = baselineValue - compareValue;
      const tag = delta >= 0 ? '节约' : '超出';
      return `${tag} ${Math.abs(delta).toLocaleString()} ${currentUnit.value}`;
    },
    customCell: (record) => {
      // 根据节能情况判断颜色
      const baselineValue = record.baselineValue || 0;
      const compareValue = record.compareValue || 0;
      const delta = baselineValue - compareValue;

      return {
        style: {
          color: delta >= 0 ? '#52c41a' : '#ff4d4f' // 节约绿色，超出红色
        }
      };
    }
  }
]);

// 更新表格列标题中的单位
function updateTableColumns(unit: string) {
  currentUnit.value = unit;
}

// 导出数据
async function onExport() {
  if (!selectedInstrument.value) {
    message.warning('请先选择仪表');
    return;
  }

  if (!baseDateRange.value || !compareDateRange.value) {
    message.warning('请选择基准期和对比期时间范围');
    return;
  }

  // 当时间类型为日时，验证基准期与对比期的天数必须相同
  if (timeRange.value === 'day') {
    const baselineDays = baseDateRange.value[1].diff(baseDateRange.value[0], 'day') + 1;
    const compareDays = compareDateRange.value[1].diff(compareDateRange.value[0], 'day') + 1;

    if (baselineDays !== compareDays) {
      message.error(`基准期和对比期的天数必须相同！基准期：${baselineDays}天，对比期：${compareDays}天`);
      return;
    }
  }

  try {
    exportLoading.value = true;

    // 格式化时间
    const baseStart = baseDateRange.value[0].format(dateFormat.value);
    const baseEnd = baseDateRange.value[1].format(dateFormat.value);
    const compareStart = compareDateRange.value[0].format(dateFormat.value);
    const compareEnd = compareDateRange.value[1].format(dateFormat.value);

    const params = {
      moduleId: selectedInstrument.value,
      timeType: timeRange.value,
      baselineStartTime: baseStart,
      baselineEndTime: baseEnd,
      compareStartTime: compareStart,
      compareEndTime: compareEnd,
      orgCode: currentOrgCode.value
    };

    const response = await exportCompareData(params);

    // 处理文件下载
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;

    // 生成文件名
    const selectedModule = instrumentList.value.find(m => m.moduleId === selectedInstrument.value);
    const moduleName = selectedModule?.moduleName || '仪表';
    const timeTypeName = timeRange.value === 'day' ? '日' : timeRange.value === 'month' ? '月' : '年';
    const timestamp = new Date().toISOString().slice(0, 16).replace(/[-:T]/g, '');

    link.download = `能源对比_${moduleName}_${timeTypeName}_${baseStart}至${baseEnd}_${timestamp}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('导出数据失败:', error);
  } finally {
    exportLoading.value = false;
  }
}

// 表格数据（动态加载）
const tableData = ref<Array<{
  key: string;
  baselineDate: string;
  baselineValue: number;
  compareDate: string;
  compareValue: number;
  saving: string;
}>>([]);

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