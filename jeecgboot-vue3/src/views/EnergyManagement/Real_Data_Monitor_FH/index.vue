<template>
  <div class="flex h-full bg-gray-100">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs v-model:activeKey="activeTabKey" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
            <a-card :bordered="false" style="height: 100%">
              <MultiSelectDimensionTree
                :ref="(el) => setTreeRef(el, item.key)"
                @select="onDepartTreeSelect"
                :nowtype="item.nowtype"
                :select-level="2"
                style="margin-top:-20px ;"
                :key="`tree-${item.key}-${item.nowtype}`"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1" style="margin-top: 10px;">
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
              @change="handleDateChange"
            />
            <a-date-picker
              v-else-if="timeRange === 'month'"
              v-model:value="selectedMonth"
              picker="month"
              placeholder="选择月份"
              class="custom-picker"
              style="width: 140px"
              @change="handleDateChange"
            />
            <a-date-picker
              v-else-if="timeRange === 'year'"
              v-model:value="selectedYear"
              picker="year"
              placeholder="选择年份"
              class="custom-picker"
              style="width: 140px"
              @change="handleDateChange"
            />
          </div>

          <!-- 仪表下拉选择 -->
          <div class="flex items-center">
            <span class="text-sm mr-2">仪表选择：</span>
            <a-select
              v-model:value="selectedMeters"
              mode="multiple"
              style="width: 200px"
              class="custom-select"
              placeholder="请选择仪表"
              :maxTagCount="1"
              :maxTagTextLength="10"
              @change="handleMeterChange"
              :loading="meterLoading"
            >
              <a-select-option v-for="meter in meters" :key="meter.value" :value="meter.value">
                {{ meter.label }}
              </a-select-option>
            </a-select>
          </div>

          <!-- 查询和导出按钮 -->
          <div class="flex gap-2">
            <a-button type="primary" class="custom-button" @click="handleQuery" :loading="loading">查询</a-button>
            <a-button type="default" class="custom-button" @click="handleExport" :loading="exportLoading">导出数据</a-button>
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
        <template v-if="hasChartData()">
          <LineChart :chartData="activePowerChartData" chartId="power-chart" />
        </template>
        <template v-else>
          <div class="flex flex-col items-center justify-center py-16 text-gray-500">
            <div class="text-6xl mb-4">📊</div>
            <div class="text-lg font-medium mb-2">暂无功率数据</div>
            <div class="text-sm text-center max-w-md">
              <p class="mb-2">当前条件下没有找到功率数据，可能的原因：</p>
              <ul class="text-left space-y-1">
                <li>• 请先选择维度和仪表</li>
                <li>• 选择的时间范围内没有数据记录</li>
                <li>• 仪表设备离线或数据传输异常</li>
              </ul>
            </div>
          </div>
        </template>
      </div>

      <!-- 负荷率图表 -->
      <div class="bg-white rounded p-3 mb-4">
        <div class="text-gray-600 text-sm mb-3 flex items-center">
          <span class="mr-2">负荷率趋势</span>
          <span class="text-xs text-gray-400">(%)</span>
        </div>
        <template v-if="hasLoadRateChartData()">
          <LineChart :chartData="loadRateChartData" chartId="loadrate-chart" />
        </template>
        <template v-else>
          <div class="flex flex-col items-center justify-center py-16 text-gray-500">
            <div class="text-6xl mb-4">📈</div>
            <div class="text-lg font-medium mb-2">暂无负荷率数据</div>
            <div class="text-sm text-center max-w-md">
              <p class="mb-2">当前条件下没有找到负荷率数据，可能的原因：</p>
              <ul class="text-left space-y-1">
                <li>• 请先选择维度和仪表</li>
                <li>• 选择的时间范围内没有数据记录</li>
                <li>• 仪表设备离线或数据传输异常</li>
              </ul>
            </div>
          </div>
        </template>
      </div>

      <!-- 数据统计表格 -->
      <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
        <div class="text-gray-600 text-sm mb-3 flex items-center">
          <span>负荷统计数据</span>
          <span class="ml-2 text-xs text-gray-400">
            ({{ timeRange === 'day' && selectedDate ? dayjs(selectedDate).format('YYYY-MM-DD') : 
                timeRange === 'month' && selectedMonth ? dayjs(selectedMonth).format('YYYY-MM') : 
                timeRange === 'year' && selectedYear ? dayjs(selectedYear).format('YYYY') : '未选择日期' }})
          </span>
        </div>
        <template v-if="statisticsData.length > 0">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-gray-50">
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">序号</th>
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">设备名称</th>
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷 (kW)</th>
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷率 (%)</th>
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最大负荷发生时间</th>
                <th class="border border-gray-200 px-4 py-2 text-center text-sm font-medium text-gray-700">最小负荷 (kW)</th>
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
        </template>
        <template v-else>
          <div class="flex flex-col items-center justify-center py-8 text-gray-500">
            <div class="text-4xl mb-2">📋</div>
            <div class="text-sm">暂无统计数据</div>
          </div>
        </template>
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
  getLoadTimeSeriesData, 
  getCurrentLoadStatus,
  type ModuleInfo,
  type LoadTimeSeriesRequest,
  type LoadTimeSeriesData
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
const allModules = ref<any[]>([]);
const selectedMeters = ref<string[]>([]);
const meterLoading = ref(false);

// 负荷监控固定参数（有功功率，参数编码7）
const LOAD_PARAMETER_CODE = 7;

// 加载状态
const loading = ref(false);
const exportLoading = ref(false);

// 设置树组件引用
const setTreeRef = (el: any, key: string) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 处理标签页切换
function handleTabChange(key: string) {
  console.log('🔄 标签页切换:', key);
  activeTabKey.value = key;

  // 根据选中的标签页设置当前能源类型
  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    const oldNowtype = currentNowtype.value;
    currentNowtype.value = selectedDimension.nowtype;
    console.log('🎯 切换到维度类型:', currentNowtype.value);

    // 如果维度类型发生变化，清空所有相关数据
    if (oldNowtype !== selectedDimension.nowtype) {
      console.log('🧹 维度类型变化，清空所有数据');
      
      // 清空设备选择
      selectedDevices.value = [];
      
      // 清空仪表选择
      meters.value = [];
      allModules.value = [];
      selectedMeters.value = [];

      // 清空图表数据
      clearChartData();
      
      console.log('✅ 数据清空完成，等待用户重新选择设备');
    }
  }
}

// 左侧树选择后触发 - 支持多选
function onDepartTreeSelect(data: any) {
  console.log('🌳 树选择事件触发:', data);
  console.log('当前维度类型:', currentNowtype.value);
  console.log('当前标签页:', activeTabKey.value);

  // 先清空之前的仪表选择
  console.log('🧹 清空之前的仪表选择');
  meters.value = [];
  allModules.value = [];
  selectedMeters.value = [];
  clearChartData();

  if (Array.isArray(data) && data.length > 0) {
    selectedDevices.value = data;
    console.log('✅ 选中的设备:', selectedDevices.value);

    // 根据选中的设备加载仪表列表
    const dimensionCodes = data.map(item => item.orgCode);
    console.log('📡 准备加载仪表，维度编码:', dimensionCodes);
    loadModulesByDimensionCodes(dimensionCodes);
  } else {
    selectedDevices.value = [];
    console.log('❌ 未选中任何设备，保持清空状态');
  }
}

// 根据维度编码加载仪表列表 - 修改为支持多个维度编码
async function loadModulesByDimensionCodes(dimensionCodes: string[]) {
  console.log('🔍 loadModulesByDimensionCodes called with:', dimensionCodes);

  if (!dimensionCodes || dimensionCodes.length === 0) {
    console.log('❌ No dimensionCodes provided, clearing meters');
    meters.value = [];
    allModules.value = [];
    selectedMeters.value = [];
    return;
  }

  try {
    meterLoading.value = true;

    // 获取所有维度编码的仪表数据
    console.log('📡 批量请求仪表数据，维度编码数量:', dimensionCodes.length);
    
    // 创建所有API请求的Promise数组
    const apiPromises = dimensionCodes.map(dimensionCode => {
      console.log('📡 请求参数:', {
        dimensionCode: dimensionCode,
        energyType: currentNowtype.value || 1,
        includeChildren: true
      });

      return getModulesByDimension({
        dimensionCode: dimensionCode,
        energyType: currentNowtype.value || 1,
        includeChildren: true
      }).catch(error => {
        console.error(`获取维度${dimensionCode}的仪表失败:`, error);
        return []; // 返回空数组，避免Promise.all失败
      });
    });

    // 并行执行所有API请求
    const responses = await Promise.all(apiPromises);
    console.log('📡 批量API响应:', responses);

    // 合并所有响应的仪表数据
    let allModuleList: any[] = [];
    
    responses.forEach((response, index) => {
      console.log(`处理维度${dimensionCodes[index]}的响应:`, response);
      
      let moduleList: any[] = [];
      
      if (response && typeof response === 'object') {
        if ('success' in response && response.success && Array.isArray(response.result)) {
          moduleList = response.result;
        } else if (Array.isArray(response)) {
          moduleList = response;
        } else if ('data' in response && Array.isArray(response.data)) {
          moduleList = response.data;
        }
      }

      if (moduleList.length > 0) {
        console.log(`维度${dimensionCodes[index]}获取到${moduleList.length}个仪表`);
        allModuleList = allModuleList.concat(moduleList);
      }
    });

    // 去重处理，避免重复的仪表
    const uniqueModules: any[] = [];
    const moduleIdSet = new Set();
    
    allModuleList.forEach(module => {
      if (!moduleIdSet.has(module.moduleId)) {
        moduleIdSet.add(module.moduleId);
        uniqueModules.push(module);
      }
    });

    allModules.value = uniqueModules;
    console.log(`✅ 合并后获取 ${uniqueModules.length} 个仪表（去重后）`);

    // 转换为下拉框选项格式
    meters.value = allModules.value.map(module => ({
      label: module.moduleName || `仪表${module.moduleId}`,
      value: module.moduleId
    }));

    // 默认选择所有仪表
    selectedMeters.value = meters.value.map(m => m.value);

    console.log(`🏷️ 转换后的仪表选项:`, meters.value);
    console.log(`🎯 默认选中所有仪表:`, selectedMeters.value);

    // 如果没有仪表数据，显示友好提示
    if (allModules.value.length === 0) {
      console.log('💡 提示：当前维度下暂无仪表数据');
      createMessage.warning('当前维度下暂无仪表数据，请检查维度配置或联系管理员');
    } else {
      createMessage.success(`成功加载 ${allModules.value.length} 个仪表，已默认全选`);
      
      // 如果有默认日期，自动执行查询
      if (selectedDate.value) {
        console.log('🚀 有默认日期，自动执行查询');
        // 延迟一下让界面更新完成
        setTimeout(() => {
          handleQuery();
        }, 500);
      }
    }

  } catch (error) {
    console.error('❌ 获取仪表列表失败:', error);
    
    // 清空仪表数据
    allModules.value = [];
    meters.value = [];
    selectedMeters.value = [];
    
    createMessage.error('获取仪表列表失败，请检查网络连接或联系管理员');
  } finally {
    meterLoading.value = false;
  }
}

// 清空图表数据
function clearChartData() {
  console.log('🧹 清空图表数据');
  activePowerChartData.value = {
    xAxis: { type: 'category' as const, data: [] as string[] },
    series: [] as any[]
  };
  loadRateChartData.value = {
    xAxis: { type: 'category' as const, data: [] as string[] },
    series: [] as any[]
  };
  statisticsData.value = [];
}

// 时间范围选择
const timeRange = ref('day');

// 日期选择
const selectedDate = ref<Dayjs | null>(null);
const selectedMonth = ref<Dayjs | null>(null);
const selectedYear = ref<Dayjs | null>(null);

// 有功功率图表数据
const activePowerChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [] as any[]
});

// 负荷率图表数据
const loadRateChartData = ref({
  xAxis: {
    type: 'category' as const,
    data: [] as string[]
  },
  series: [] as any[]
});

// 统计数据表格
const statisticsData = ref<any[]>([]);

// 检查是否有图表数据
const hasChartData = (): boolean => {
  return activePowerChartData.value.series.length > 0 && 
         activePowerChartData.value.xAxis.data.length > 0;
};

// 检查是否有负荷率图表数据
const hasLoadRateChartData = (): boolean => {
  return loadRateChartData.value.series.length > 0 && 
         loadRateChartData.value.xAxis.data.length > 0;
};

// 时间范围变化处理
const handleTimeRangeChange = () => {
  console.log('📅 时间范围变化:', timeRange.value);
  
  // 清空所有日期选择
  selectedDate.value = null;
  selectedMonth.value = null;
  selectedYear.value = null;
  
  // 设置默认日期
  if (timeRange.value === 'day') {
    selectedDate.value = dayjs();
    console.log('📅 设置默认日期:', dayjs().format('YYYY-MM-DD'));
  } else if (timeRange.value === 'month') {
    selectedMonth.value = dayjs();
    console.log('📅 设置默认月份:', dayjs().format('YYYY-MM'));
  } else if (timeRange.value === 'year') {
    selectedYear.value = dayjs();
    console.log('📅 设置默认年份:', dayjs().format('YYYY'));
  }
  
  // 如果有选中的仪表，自动触发查询
  if (selectedMeters.value && selectedMeters.value.length > 0) {
    console.log('🚀 时间范围变化后自动查询');
    // 延迟一点时间确保日期设置完成
    setTimeout(() => {
      handleQuery();
    }, 100);
  }
};


// 日期变化处理 - 也需要修改
const handleDateChange = () => {
  console.log('📅 日期变化事件触发');
  console.log('当前时间范围:', timeRange.value);
  console.log('选中的日期:', {
    day: selectedDate.value ? dayjs(selectedDate.value).format('YYYY-MM-DD') : null,
    month: selectedMonth.value ? dayjs(selectedMonth.value).format('YYYY-MM') : null,
    year: selectedYear.value ? dayjs(selectedYear.value).format('YYYY') : null
  });
  
  // 如果有选中的仪表，自动查询
  if (selectedMeters.value && selectedMeters.value.length > 0) {
    console.log('🚀 日期变化后自动查询');
    handleQuery();
  }
};


// 仪表选择变化处理
const handleMeterChange = () => {
  // 如果有选择的日期，自动查询
  if (selectedDate.value || selectedMonth.value || selectedYear.value) {
    handleQuery();
  }
};

// 查询处理 - 修改日期格式处理部分
const handleQuery = async () => {
  console.log('🔍 开始查询负荷数据...');

  // 验证查询参数
  if (!selectedMeters.value || selectedMeters.value.length === 0) {
    createMessage.warning('请选择至少一个仪表');
    return;
  }

  try {
    loading.value = true;

    // 构建查询日期 - 修改这部分逻辑
    let queryDate: string;
    let timeGranularity: string;

    if (timeRange.value === 'day') {
      if (!selectedDate.value) {
        createMessage.warning('请选择查询日期');
        return;
      }
      queryDate = dayjs(selectedDate.value).format('YYYY-MM-DD');
      timeGranularity = 'day';
    } else if (timeRange.value === 'month') {
      if (!selectedMonth.value) {
        createMessage.warning('请选择查询月份');
        return;
      }
      queryDate = dayjs(selectedMonth.value).format('YYYY-MM');
      timeGranularity = 'month';
    } else if (timeRange.value === 'year') {
      if (!selectedYear.value) {
        createMessage.warning('请选择查询年份');
        return;
      }
      queryDate = dayjs(selectedYear.value).format('YYYY');
      timeGranularity = 'year';
    } else {
      createMessage.warning('请选择查询时间范围');
      return;
    }

    console.log('📊 查询参数构建完成:', {
      timeRange: timeRange.value,
      queryDate: queryDate,
      timeGranularity: timeGranularity,
      selectedMeters: selectedMeters.value
    });

    // 构建负荷监控请求参数
    const requestData: LoadTimeSeriesRequest = {
      moduleIds: selectedMeters.value,
      timeGranularity: timeGranularity,
      queryDate: queryDate
    };

    console.log('📊 负荷查询参数:', requestData);

    // 调用负荷时序数据查询API
    const response = await getLoadTimeSeriesData(requestData);

    console.log('📊 负荷查询响应:', response);

    // 处理响应数据
    if (response && typeof response === 'object') {
      let loadData: LoadTimeSeriesData | null = null;
      
      if ('success' in response && response.success && response.result) {
        loadData = response.result;
      } else if ('powerChartData' in response || 'loadRateChartData' in response) {
        loadData = response as LoadTimeSeriesData;
      }

      if (loadData) {
        // 更新负荷图表数据
        updateLoadChartData(loadData);
        createMessage.success(`${timeRange.value === 'day' ? '日' : timeRange.value === 'month' ? '月' : '年'}负荷数据查询成功`);
      } else {
        console.error('负荷查询失败: 无有效数据', response);
        // 清空图表数据
        clearChartData();
        createMessage.warning(`查询成功，但${queryDate}期间暂无数据`);
      }
    } else {
      console.error('负荷查询失败: 响应格式错误', response);
      // 清空图表数据
      clearChartData();
      createMessage.error('查询失败，响应数据格式错误');
    }

  } catch (error) {
    console.error('❌ 查询负荷数据失败:', error);
    // 清空图表数据
    clearChartData();
    createMessage.error('查询失败，请检查网络连接或联系管理员');
  } finally {
    loading.value = false;
  }
};

// 更新负荷图表数据
function updateLoadChartData(data: LoadTimeSeriesData) {
  console.log('📊 更新负荷图表数据:', data);

  if (!data) {
    console.warn('❌ 无效的负荷数据');
    clearChartData();
    return;
  }

  // 更新有功功率图表数据
  if (data.powerChartData && data.powerChartData.series && data.powerChartData.series.length > 0) {
    activePowerChartData.value = {
      xAxis: {
        type: 'category',
        data: data.powerChartData.timeLabels || []
      },
      series: data.powerChartData.series.map((series: any) => ({
        name: series.moduleName,
        type: 'line',
        data: series.data || [],
        itemStyle: {
          color: series.color || '#1890ff'
        },
        unit: series.unit || 'kW',
        deviceName: series.moduleName
      }))
    };
    console.log('✅ 有功功率图表数据更新完成');
  } else {
    console.log('⚠️ 无有功功率数据');
    activePowerChartData.value = {
      xAxis: { type: 'category' as const, data: [] as string[] },
      series: [] as any[]
    };
  }

  // 更新负荷率图表数据
  if (data.loadRateChartData && data.loadRateChartData.series && data.loadRateChartData.series.length > 0) {
    loadRateChartData.value = {
      xAxis: {
        type: 'category',
        data: data.loadRateChartData.timeLabels || []
      },
      series: data.loadRateChartData.series.map((series: any) => ({
        name: series.moduleName,
        type: 'line',
        data: series.data || [],
        itemStyle: {
          color: series.color || '#52c41a'
        },
        unit: series.unit || '%',
        deviceName: series.moduleName
      }))
    };
    console.log('✅ 负荷率图表数据更新完成');
  } else {
    console.log('⚠️ 无负荷率数据');
    loadRateChartData.value = {
      xAxis: { type: 'category' as const, data: [] as string[] },
      series: [] as any[]
    };
  }

  // 更新统计数据表格
  if (data.tableData && Array.isArray(data.tableData) && data.tableData.length > 0) {
    updateLoadStatisticsData(data.tableData);
    console.log('✅ 统计数据更新完成');
  } else {



    // 更新统计数据表格 - 继续
    console.log('⚠️ 无统计数据');
    statisticsData.value = [];
  }
}

// 更新负荷统计数据表格
function updateLoadStatisticsData(tableData: any[]) {
  console.log('📊 更新负荷统计数据:', tableData);
  
  const stats: any[] = [];

  // 处理表格数据，计算每个设备的统计信息
  const deviceStats = new Map();

  tableData.forEach((timePoint: any) => {
    if (timePoint.modules && Array.isArray(timePoint.modules)) {
      timePoint.modules.forEach((module: any) => {
        const moduleId = module.moduleId;
        if (!deviceStats.has(moduleId)) {
          deviceStats.set(moduleId, {
            moduleName: module.moduleName,
            powerData: [],
            loadRateData: [],
            timePoints: []
          });
        }

        const stats = deviceStats.get(moduleId);
        if (module.currentPower !== null && module.currentPower !== undefined) {
          stats.powerData.push(module.currentPower);
        }
        if (module.loadRate !== null && module.loadRate !== undefined) {
          stats.loadRateData.push(module.loadRate);
        }
        stats.timePoints.push({
          time: timePoint.timeLabel || timePoint.time,
          power: module.currentPower,
          loadRate: module.loadRate
        });
      });
    }
  });

  let index = 1;
  deviceStats.forEach((deviceData: any, moduleId: string) => {
    const powerData = deviceData.powerData.filter((p: any) => p !== null && p !== undefined && !isNaN(p));
    const loadRateData = deviceData.loadRateData.filter((r: any) => r !== null && r !== undefined && !isNaN(r));

    if (powerData.length > 0) {
      // 计算功率统计
      const maxPower = Math.max(...powerData);
      const minPower = Math.min(...powerData);
      const avgPower = powerData.reduce((sum: number, val: number) => sum + val, 0) / powerData.length;

      // 计算负荷率统计
      const maxLoadRate = loadRateData.length > 0 ? Math.max(...loadRateData) : 0;
      const minLoadRate = loadRateData.length > 0 ? Math.min(...loadRateData) : 0;
      const avgLoadRate = loadRateData.length > 0 ? loadRateData.reduce((sum: number, val: number) => sum + val, 0) / loadRateData.length : 0;

      // 找到最大最小功率发生的时间
      const maxPowerPoint = deviceData.timePoints.find((p: any) => p.power === maxPower);
      const minPowerPoint = deviceData.timePoints.find((p: any) => p.power === minPower);

      stats.push({
        id: index++,
        deviceName: deviceData.moduleName,
        maxLoad: Number(maxPower.toFixed(2)),
        maxLoadRate: Number(maxLoadRate.toFixed(1)),
        maxLoadTime: maxPowerPoint ? maxPowerPoint.time : '--',
        minLoad: Number(minPower.toFixed(2)),
        minLoadRate: Number(minLoadRate.toFixed(1)),
        minLoadTime: minPowerPoint ? minPowerPoint.time : '--',
        avgLoad: Number(avgPower.toFixed(2)),
        avgLoadRate: Number(avgLoadRate.toFixed(1))
      });
    }
  });

  statisticsData.value = stats;
  console.log('✅ 统计数据生成完成:', stats);
}

// 导出数据
const handleExport = async () => {
  if (!selectedMeters.value || selectedMeters.value.length === 0) {
    createMessage.warning('请选择至少一个仪表');
    return;
  }

  try {
    exportLoading.value = true;
    createMessage.loading('正在导出数据，请稍候...', 2);
    
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

    console.log('📤 开始导出数据:', {
      moduleIds: selectedMeters.value,
      timeGranularity: timeGranularity,
      queryDate: queryDate,
      fileName: `负荷数据_${queryDate}`,
      statisticsDataCount: statisticsData.value ? statisticsData.value.length : 0
    });
    
    console.log('📤 当前统计数据:', statisticsData.value);

    // 使用defHttp但配置为不转换响应
    const response = await defHttp.post(
      {
        url: '/energy/realtime/exportLoadData',
        data: {
          moduleIds: selectedMeters.value,
          timeGranularity: timeGranularity,
          queryDate: queryDate,
          fileName: `负荷数据_${queryDate}`,
          // 添加统计数据，如果前端有的话
          statisticsData: statisticsData.value && statisticsData.value.length > 0 ? statisticsData.value : null
        },
        responseType: 'blob',
        timeout: 60000,
      },
      {
        isTransformResponse: false,
        isReturnNativeResponse: true,
      }
    );
    
    console.log('📤 导出API响应:', response);
    
    // 处理响应
    let blob: Blob;
    
    if (response.data instanceof Blob) {
      blob = response.data;
    } else if (response instanceof Blob) {
      blob = response;
    } else {
      // 如果不是blob，尝试从response中获取
      const responseData = response.data || response;
      if (responseData instanceof ArrayBuffer) {
        blob = new Blob([responseData], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
      } else {
        throw new Error('响应数据格式不正确');
      }
    }

    console.log('📤 文件大小:', blob.size, '字节');

    if (blob.size === 0) {
      createMessage.error('导出的文件为空，请检查数据');
      return;
    }

    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `负荷数据_${queryDate}.xlsx`;
    link.style.display = 'none';
    
    // 添加到DOM并触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }, 100);
    
    createMessage.success('导出成功');

  } catch (error: any) {
    console.error('导出失败:', error);
    
    // 检查是否是认证错误
    if (error.response && error.response.status === 401) {
      createMessage.error('导出失败：用户未登录或登录已过期，请重新登录');
    } else if (error.response && error.response.status) {
      createMessage.error(`导出失败：服务器错误 (${error.response.status})`);
    } else if (error.message) {
      createMessage.error(`导出失败: ${error.message}`);
    } else {
      createMessage.error('导出失败：未知错误');
    }
  } finally {
    exportLoading.value = false;
  }
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

// 监听维度类型变化，重新渲染树组件
watch(() => currentNowtype.value, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    console.log('🔄 维度类型变化，从', oldValue, '到', newValue);
    // 维度类型变化时，需要重新渲染对应的树组件
    nextTick(() => {
      const currentTreeRef = treeRefs.value[activeTabKey.value];
      if (currentTreeRef && typeof currentTreeRef.refresh === 'function') {
        currentTreeRef.refresh();
      }
    });
  }
}, { immediate: false });

onMounted(() => {
  // 加载维度字典数据
  loadDimensionDictData();

  // 设置默认日期为今天
  selectedDate.value = dayjs();
  
  // 等待DOM渲染完成后，触发默认选择
  nextTick(() => {
    setTimeout(() => {
      // 如果有维度数据，尝试触发默认选择
      if (dimensionList.value.length > 0) {
        console.log('📋 页面加载完成，准备触发默认选择');
      }
    }, 1000);
  });
});

onUnmounted(() => {
  // 清理资源
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