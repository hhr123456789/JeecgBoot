<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs defaultActiveKey="info1" @change="handleTabChange" style="height: 100%;width:300px;">
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

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <!-- 查询条件区域 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 时间范围选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">时间范围:</span>
            <a-radio-group v-model:value="timeRange" button-style="solid" class="custom-radio-group" @change="handleTimeRangeChange">
              <a-radio-button value="day">日</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
          </div>

          <!-- 日期选择器 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">选择日期:</span>
            <a-date-picker
              v-if="timeRange === 'day'"
              v-model:value="selectedDate"
              placeholder="选择日期"
              class="custom-picker"
              style="width: 140px"
            />
            <a-date-picker
              v-else-if="timeRange === 'month'"
              v-model:value="selectedMonth"
              picker="month"
              placeholder="选择月份"
              class="custom-picker"
              style="width: 140px"
            />
            <a-date-picker
              v-else-if="timeRange === 'year'"
              v-model:value="selectedYear"
              picker="year"
              placeholder="选择年份"
              class="custom-picker"
              style="width: 140px"
            />
          </div>

          <!-- 仪表下拉选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">仪表选择：</span>
            <a-select
              v-model:value="selectedMeters"
              mode="multiple"
              style="width: 180px"
              class="custom-select"
              placeholder="请选择仪表"
              :maxTagCount="1"
              :maxTagTextLength="10"
            >
              <a-select-option v-for="meter in meters" :key="meter.value" :value="meter.value">
                {{ meter.label }}
              </a-select-option>
            </a-select>
          </div>

          <!-- 参数选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">参数选择：</span>
            <a-select
              v-model:value="selectedParameters"
              mode="multiple"
              style="width: 200px"
              class="custom-select"
              placeholder="请选择参数"
              :maxTagCount="2"
              :maxTagTextLength="8"
            >
              <a-select-option v-for="param in parameterConfigs" :key="param.paramCode" :value="param.paramCode">
                {{ param.paramName }}({{ param.unit }})
              </a-select-option>
            </a-select>
          </div>

          <!-- 查询和导出按钮 -->
          <div class="flex gap-2">
            <a-button type="primary" class="custom-button" @click="handleQuery" :loading="loading">查询</a-button>
            <a-button type="default" class="custom-button">导出数据</a-button>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <!-- 有功功率图表 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="text-gray-600 text-sm mb-3 flex items-center">
          <span class="mr-2">有功功率趋势</span>
          <span class="text-xs text-gray-400">(kW)</span>
        </div>
        <LineChart :chartData="activePowerChartData" />
      </div>

      <!-- 负荷率图表 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="text-gray-600 text-sm mb-3 flex items-center">
          <span class="mr-2">负荷率趋势</span>
          <span class="text-xs text-gray-400">(%)</span>
        </div>
        <LineChart :chartData="loadRateChartData" />
      </div>

      <!-- 数据统计表格 -->
      <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-gray-50">
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">序号</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">设备名称</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷 (kw)</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷率 (%)</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷发生时间</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最小负荷 (kw)</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最小负荷率 (%)</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最小负荷发生时间</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">平均负荷 (kW)</th>
              <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">平均负荷率 (%)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in statisticsData" :key="index" class="hover:bg-gray-50">
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.id }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.deviceName }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.maxLoad }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.maxLoadRate }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.maxLoadTime }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.minLoad }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.minLoadRate }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.minLoadTime }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.avgLoad }}</td>
              <td class="border border-gray-200 px-4 py-2 text-sm text-gray-900 text-center">{{ item.avgLoadRate }}</td>
            </tr>
          </tbody>
        </table>
      </div>


    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import type { Dayjs } from 'dayjs';
import LineChart from './components/LineChart.vue';
import MultiSelectDimensionTree from '../Real_Data_Monitor/components/MultiSelectDimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import {
  getModulesByDimension,
  getParameterConfig,
  getTimeSeriesData,
  getCurrentStatus,
  type ModuleInfo,
  type ParameterConfig,
  type TimeSeriesData,
  type ModuleStatus
} from './api';
import dayjs from 'dayjs';

// 消息提示
const { createMessage } = useMessage();

// 当前激活的标签页
const activeTabKey = ref('info1');

// 维度列表
const dimensionList = ref<any[]>([]);

// 树组件引用
const treeRefs = ref<Record<string, any>>({});

// 当前能源类型
const currentNowtype = ref(1);

// 选中的设备节点
const selectedDevices = ref<any[]>([]);

// 仪表相关变量
const meters = ref<Array<{ label: string; value: string }>>([]);
const allModules = ref<ModuleInfo[]>([]);
const selectedMeters = ref<string[]>([]);

// 参数配置
const parameterConfigs = ref<ParameterConfig[]>([]);
const selectedParameters = ref<number[]>([]);

// 加载状态
const loading = ref(false);

// 设置树组件引用
const setTreeRef = (el: any, key: string) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 处理标签页切换
function handleTabChange(key: string) {
  activeTabKey.value = key;

  // 根据选中的标签页设置当前能源类型
  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    currentNowtype.value = selectedDimension.nowtype;
    console.log('🎯 切换到维度类型:', currentNowtype.value);

    // 清空仪表选择，等待用户重新选择设备
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];

    // 加载对应能源类型的参数配置
    loadParameterConfig(currentNowtype.value);
  }
}

// 左侧树选择后触发 - 支持多选
function onDepartTreeSelect(data: any) {
  console.log('onDepartTreeSelect received:', data);
  console.log('Current nowtype:', currentNowtype.value);
  console.log('Current tab:', activeTabKey.value);

  if (Array.isArray(data) && data.length > 0) {
    selectedDevices.value = data;
    console.log('✅ 选中的设备:', selectedDevices.value);

    // 根据选中的设备加载仪表列表
    const dimensionCodes = data.map(item => item.orgCode);
    loadModulesByDimensionCodes(dimensionCodes);
  } else {
    selectedDevices.value = [];
    console.log('❌ 未选中任何设备');
    // 清空仪表列表
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
    // 清空图表数据
    clearChartData();
  }
}

// 根据维度编码加载仪表列表
async function loadModulesByDimensionCodes(dimensionCodes: string[]) {
  console.log('loadModulesByDimensionCodes called with:', dimensionCodes);

  if (!dimensionCodes || dimensionCodes.length === 0) {
    console.log('No dimensionCodes provided, clearing meters');
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
    return;
  }

  try {
    loading.value = true;

    // 使用第一个维度编码进行查询（如果需要支持多个维度，可以循环查询）
    const dimensionCode = dimensionCodes[0];
    console.log('请求参数:', {
      dimensionCode: dimensionCode,
      energyType: currentNowtype.value || 1,
      includeChildren: true
    });

    const response = await getModulesByDimension({
      dimensionCode: dimensionCode,
      energyType: currentNowtype.value || 1,
      includeChildren: true
    });

    console.log('API响应:', response);

    if (response && response.success && Array.isArray(response.result)) {
      allModules.value = response.result;
      console.log(`成功获取 ${response.result.length} 个仪表`);
    } else {
      console.log('响应格式不正确或无数据:', response);
      allModules.value = [];
    }

    // 转换为下拉框选项格式
    meters.value = allModules.value.map(module => ({
      label: module.moduleName,
      value: module.moduleId
    }));

    // 默认选择所有仪表（如果有的话）
    if (meters.value.length > 0) {
      selectedMeters.value = meters.value.map(m => m.value);
    } else {
      selectedMeters.value = [];
    }

    console.log(`加载了 ${allModules.value.length} 个仪表，默认选中 ${selectedMeters.value.length} 个`);
    console.log('仪表详情:', meters.value);

    // 如果没有仪表数据，显示友好提示
    if (allModules.value.length === 0) {
      console.log('💡 提示：当前维度下暂无仪表数据，请尝试切换其他维度或联系管理员配置仪表');
      createMessage.warning('当前维度下暂无仪表数据');
    }

  } catch (error) {
    console.error('获取仪表列表失败:', error);
    createMessage.error('获取仪表列表失败');
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
  } finally {
    loading.value = false;
  }
}

// 加载参数配置
async function loadParameterConfig(energyType: number) {
  try {
    const response = await getParameterConfig({ energyType });
    if (response && response.success && Array.isArray(response.result)) {
      parameterConfigs.value = response.result;
      // 默认选择有功功率和负荷率相关参数
      selectedParameters.value = parameterConfigs.value
        .filter(param => param.isDefault || param.paramName.includes('有功') || param.paramName.includes('负荷'))
        .map(param => param.paramCode);
      console.log('加载参数配置成功:', parameterConfigs.value);
    }
  } catch (error) {
    console.error('加载参数配置失败:', error);
    // 使用默认参数
    selectedParameters.value = [1]; // 默认选择第一个参数
  }
}

// 清空图表数据
function clearChartData() {
  activePowerChartData.value = {
    xAxis: { type: 'category', data: [] },
    series: []
  };
  loadRateChartData.value = {
    xAxis: { type: 'category', data: [] },
    series: []
  };
  statisticsData.value = [];
}

// 实时数据接口定义（简化版，只保留必要数据）
interface RealTimeData {
  activePower: number;      // 有功功率
  powerFactor: number;      // 功率因数
  loadRate: number;         // 负荷率
}

// 实时数据（静态数据）
const realTimeData = ref<RealTimeData>({
  activePower: 75.54,
  powerFactor: 0.95,
  loadRate: 85.6
});

// 时间范围选择
const timeRange = ref('day');

// 日期选择
const selectedDate = ref<Dayjs | null>(null);
const selectedMonth = ref<Dayjs | null>(null);
const selectedYear = ref<Dayjs | null>(null);



// 有功功率图表数据
const activePowerChartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '有功功率',
      type: 'line',
      data: [75.54, 78.23, 80.67, 79.45, 78.92, 80.34, 81.78,
             79.89, 78.45, 77.89, 79.23, 78.67],
      itemStyle: {
        color: '#1890ff'
      },
      unit: 'kW',
      deviceName: '1号设备'
    }
  ]
});

// 负荷率图表数据
const loadRateChartData = ref({
  xAxis: {
    type: 'category',
    data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00',
           '14:00', '16:00', '18:00', '20:00', '22:00']
  },
  series: [
    {
      name: '负荷率',
      type: 'line',
      data: [85.6, 87.2, 89.1, 88.3, 87.8, 89.5, 90.2,
             88.9, 87.4, 86.8, 88.1, 87.6],
      itemStyle: {
        color: '#52c41a'
      },
      unit: '%',
      deviceName: '1号设备'
    }
  ]
});

// 统计数据表格
const statisticsData = ref([
  {
    id: 1,
    deviceName: '1号设备',
    maxLoad: 90.25,
    maxLoadRate: 95.8,
    maxLoadTime: '14:30',
    minLoad: 65.12,
    minLoadRate: 68.9,
    minLoadTime: '03:15',
    avgLoad: 78.45,
    avgLoadRate: 83.2
  },
  {
    id: 2,
    deviceName: '2号设备',
    maxLoad: 88.76,
    maxLoadRate: 92.4,
    maxLoadTime: '15:45',
    minLoad: 62.34,
    minLoadRate: 65.1,
    minLoadTime: '02:30',
    avgLoad: 75.89,
    avgLoadRate: 79.8
  },
  {
    id: 3,
    deviceName: '3号设备',
    maxLoad: 95.12,
    maxLoadRate: 98.2,
    maxLoadTime: '16:20',
    minLoad: 58.67,
    minLoadRate: 61.5,
    minLoadTime: '04:45',
    avgLoad: 82.34,
    avgLoadRate: 86.7
  }
]);



// 定时更新数据
let timer: number | null = null;

// 时间范围变化处理
const handleTimeRangeChange = () => {
  // 清空日期选择
  selectedDate.value = null;
  selectedMonth.value = null;
  selectedYear.value = null;
};

// 查询处理
const handleQuery = async () => {
  console.log('开始查询数据...');

  // 验证查询参数
  if (!selectedMeters.value || selectedMeters.value.length === 0) {
    createMessage.warning('请选择至少一个仪表');
    return;
  }

  if (!selectedParameters.value || selectedParameters.value.length === 0) {
    createMessage.warning('请选择至少一个参数');
    return;
  }

  try {
    loading.value = true;

    // 构建查询日期
    let queryDate: string;
    let timeGranularity: string;

    if (timeRange.value === 'day' && selectedDate.value) {
      queryDate = dayjs(selectedDate.value).format('YYYY-MM-DD');
      timeGranularity = 'day';
    } else if (timeRange.value === 'month' && selectedMonth.value) {
      queryDate = dayjs(selectedMonth.value).format('YYYY-MM');
      timeGranularity = 'month';
    } else if (timeRange.value === 'year' && selectedYear.value) {
      queryDate = dayjs(selectedYear.value).format('YYYY');
      timeGranularity = 'year';
    } else {
      createMessage.warning('请选择查询时间范围');
      return;
    }

    // 构建请求参数
    const requestData = {
      moduleIds: selectedMeters.value,
      parameters: selectedParameters.value,
      timeGranularity: timeGranularity,
      queryDate: queryDate
    };

    console.log('📊 查询参数:', requestData);

    // 调用查询API
    const response = await getTimeSeriesData(requestData);
    console.log('📊 查询响应:', response);

    if (response && response.success) {
      // 更新图表数据
      updateChartData(response.result);
      createMessage.success('查询成功');
    } else {
      console.error('查询失败:', response);
      createMessage.error('查询失败：' + (response?.message || '未知错误'));
    }

  } catch (error) {
    console.error('查询数据失败:', error);
    createMessage.error('查询数据失败');
  } finally {
    loading.value = false;
  }
};

// 更新图表数据
function updateChartData(data: TimeSeriesData) {
  console.log('更新图表数据:', data);

  if (!data || !data.chartData) {
    console.warn('无效的图表数据');
    return;
  }

  const { chartData } = data;

  // 更新有功功率图表数据
  const activePowerSeries = chartData.series.filter(series =>
    series.paramName.includes('有功') || series.paramName.includes('功率')
  );

  if (activePowerSeries.length > 0) {
    activePowerChartData.value = {
      xAxis: {
        type: 'category',
        data: chartData.timeLabels
      },
      series: activePowerSeries.map(series => ({
        name: `${series.moduleName}-${series.paramName}`,
        type: 'line',
        data: series.data,
        itemStyle: {
          color: series.color
        },
        unit: series.unit,
        deviceName: series.moduleName
      }))
    };
  }

  // 更新负荷率图表数据（如果有负荷率参数）
  const loadRateSeries = chartData.series.filter(series =>
    series.paramName.includes('负荷') || series.paramName.includes('率')
  );

  if (loadRateSeries.length > 0) {
    loadRateChartData.value = {
      xAxis: {
        type: 'category',
        data: chartData.timeLabels
      },
      series: loadRateSeries.map(series => ({
        name: `${series.moduleName}-${series.paramName}`,
        type: 'line',
        data: series.data,
        itemStyle: {
          color: series.color
        },
        unit: series.unit,
        deviceName: series.moduleName
      }))
    };
  }

  // 更新统计数据表格
  updateStatisticsData(chartData.series);
}

// 更新统计数据表格
function updateStatisticsData(series: any[]) {
  const stats: any[] = [];

  // 按设备分组统计
  const deviceGroups = new Map();
  series.forEach(s => {
    if (!deviceGroups.has(s.moduleId)) {
      deviceGroups.set(s.moduleId, {
        moduleId: s.moduleId,
        moduleName: s.moduleName,
        series: []
      });
    }
    deviceGroups.get(s.moduleId).series.push(s);
  });

  let index = 1;
  deviceGroups.forEach(group => {
    // 计算该设备的统计数据
    const allData = group.series.flatMap((s: any) => s.data.filter((v: number) => v !== null && v !== undefined));

    if (allData.length > 0) {
      const maxLoad = Math.max(...allData);
      const minLoad = Math.min(...allData);
      const avgLoad = allData.reduce((sum: number, val: number) => sum + val, 0) / allData.length;

      // 假设额定功率为100kW，计算负荷率
      const ratedPower = 100;
      const maxLoadRate = (maxLoad / ratedPower) * 100;
      const minLoadRate = (minLoad / ratedPower) * 100;
      const avgLoadRate = (avgLoad / ratedPower) * 100;

      stats.push({
        id: index++,
        deviceName: group.moduleName,
        maxLoad: Number(maxLoad.toFixed(2)),
        maxLoadRate: Number(maxLoadRate.toFixed(1)),
        maxLoadTime: '14:30', // 这里需要根据实际数据计算
        minLoad: Number(minLoad.toFixed(2)),
        minLoadRate: Number(minLoadRate.toFixed(1)),
        minLoadTime: '03:15', // 这里需要根据实际数据计算
        avgLoad: Number(avgLoad.toFixed(2)),
        avgLoadRate: Number(avgLoadRate.toFixed(1))
      });
    }
  });

  statisticsData.value = stats;
}

// 更新数据的方法
const updateData = () => {
  // 模拟数据更新
  realTimeData.value = {
    ...realTimeData.value,
    activePower: Number((realTimeData.value.activePower * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2)),
    powerFactor: Number((realTimeData.value.powerFactor * (1 + (Math.random() - 0.5) * 0.001)).toFixed(2)),
    loadRate: Number((realTimeData.value.loadRate * (1 + (Math.random() - 0.5) * 0.01)).toFixed(2))
  };
};

// 获取字典数据
function loadDimensionDictData() {
  console.log('🔄 开始加载维度字典数据...');

  // 先设置默认配置，确保界面能正常显示
  useDefaultDimensions();

  // 使用与 Real_Data_Monitor 相同的 API 调用
  defHttp.get({
    url: '/sys/dict/getDictItems/dimensionCode'
  })
  .then((res) => {
    if (res && Array.isArray(res) && res.length > 0) {
      console.log('📋 维度字典原始数据:', res);

      // 将字典数据转换为维度列表
      dimensionList.value = res.map((item, index) => {
        return {
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(item.value), // 使用字典中的value作为nowtype值
          value: Number(item.value)
        };
      });

      console.log('🏷️ 转换后的维度列表:', dimensionList.value);

      // 默认选中第一个标签页
      if (dimensionList.value.length > 0) {
        activeTabKey.value = dimensionList.value[0].key;
        currentNowtype.value = dimensionList.value[0].nowtype;
        console.log('🎯 使用字典数据，默认选中维度类型:', currentNowtype.value);
      }
    } else {
      console.log('⚠️ 维度字典数据为空或格式不正确，继续使用默认配置');
    }
  })
  .catch((error) => {
    console.error('❌ 获取维度字典失败:', error);
    console.log('🔄 继续使用默认维度配置');
  });
}

// 使用默认维度配置
function useDefaultDimensions() {
  dimensionList.value = [
    { key: 'info1', title: '按部门（用电）', nowtype: 1, value: 1 },
    { key: 'info2', title: '按线路（用电）', nowtype: 2, value: 2 },
    { key: 'info3', title: '天然气', nowtype: 3, value: 3 },
    { key: 'info4', title: '压缩空气', nowtype: 4, value: 4 },
    { key: 'info5', title: '企业用水', nowtype: 5, value: 5 }
  ];
  activeTabKey.value = 'info1';
  currentNowtype.value = 1;
  console.log('✅ 已设置默认维度列表:', dimensionList.value);
}



// 监听仪表选择变化，自动查询数据
watch([selectedMeters, selectedParameters], ([newMeters, newParams]) => {
  if (newMeters && newMeters.length > 0 && newParams && newParams.length > 0) {
    // 如果有选择的日期，自动查询
    if (selectedDate.value || selectedMonth.value || selectedYear.value) {
      handleQuery();
    }
  }
}, { deep: true });

onMounted(() => {
  // 加载维度字典数据
  loadDimensionDictData();

  // 加载默认参数配置（电力类型）
  loadParameterConfig(1);

  // 设置默认日期为今天
  selectedDate.value = dayjs();

  // 启动定时更新
  timer = window.setInterval(updateData, 5000);
});

onUnmounted(() => {
  // 清理定时器
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.h-full {
  min-height: calc(100vh - 100px);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-track {
  background-color: #f5f5f5;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 4px;
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

/* 更新数据值样式 */
.bg-gray-50 {
  background-color: #f9fafb;
}

/* 圆角大小 */
.rounded-lg {
  border-radius: 0.5rem;
}

/* 自定义选择器和按钮样式 */
:deep(.custom-picker) {
  height: 36px;
}

:deep(.custom-picker .ant-picker-input) {
  height: 36px;
  display: flex;
  align-items: center;
}

:deep(.custom-select) {
  height: 36px;
}

:deep(.custom-select .ant-select-selector) {
  height: 36px !important;
  padding-top: 3px !important;
}

.custom-button {
  height: 36px;
  display: flex;
  align-items: center;
  padding: 0 16px;
}

:deep(.custom-radio-group) {
  height: 36px;
  display: inline-flex;
}

:deep(.custom-radio-group .ant-radio-button-wrapper) {
  height: 36px;
  line-height: 34px;
  display: inline-flex;
  align-items: center;
}
</style> 