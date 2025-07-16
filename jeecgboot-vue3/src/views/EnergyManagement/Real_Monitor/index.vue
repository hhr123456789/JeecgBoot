<template>
  <div class="flex h-full">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
      <a-tabs defaultActiveKey="info1" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title" :forceRender="item.key === 'info1'">
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
      <div class="real-data-monitor-container p-4">
        <!-- 主要内容区域 -->
        <a-spin :spinning="loading">
          <div class="grid grid-cols-2 gap-2">
            <!-- 动态生成仪表卡片 -->
            <template v-if="realTimeData.length > 0">
              <a-card 
                v-for="(item, index) in realTimeData" 
                :key="index" 
                class="monitor-card" 
                :bordered="false"
              >
                <div class="flex items-center mb-2">
                  <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                    <span class="text-white text-xs">O</span>
                  </div>
                  <span class="text-base font-bold">{{ item.module_name }}</span>
                </div>
                
                <!-- 电力仪表数据 (nowtype=1或2) -->
                <div v-if="currentNowtype === 1 || currentNowtype === 2" class="space-y-1 data-table">
                  <div class="flex items-center text-sm">
                    <span class="text-gray-600 w-16">负荷状态</span>
                    <span :class="getLoadStatusClass(item.loadStatus)">{{ item.loadStatus }}</span>
                  </div>
                  <div class="flex items-center text-sm">
                    <span class="text-gray-600 w-16">负荷率</span>
                    <div class="flex-1 mx-1">
                      <div class="relative h-1.5 bg-gray-200 rounded">
                        <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: item.loadRate + '%' }"></div>
                      </div>
                    </div>
                    <span class="text-xs text-gray-600">{{ item.loadRate }}%</span>
                  </div>
                  <div class="flex items-center text-sm">
                    <span class="text-gray-600 w-16">采集时间</span>
                    <span class="text-green-500">{{ item.Equ_Electric_DT }}</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                    <span>总功率因素：{{ item.PFS }}</span>
                    <span>频率：{{ item.HZ }} Hz</span>
                    <span>总有功功率：{{ item.pp }} KW</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                    <span>A相电流：{{ item.IA }} A</span>
                    <span>B相电流：{{ item.IB }} A</span>
                    <span>C相电流：{{ item.IC }} A</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                    <span>A相电压：{{ item.UA }} V</span>
                    <span>B相电压：{{ item.UB }} V</span>
                    <span>C相电压：{{ item.UC }} V</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                    <span>A相功率因素：{{ item.PFa }}</span>
                    <span>B相功率因素：{{ item.PFb }}</span>
                    <span>C相功率因素：{{ item.PFc }}</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                    <span>A相有功功率：{{ item.Pa }} kW</span>
                    <span>B相有功功率：{{ item.Pb }} kW</span>
                    <span>C相有功功率：{{ item.Pc }} kW</span>
                  </div>
                  <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                    <span>有功电量：{{ item.KWH }} kW·h</span>
                    <span>无功电量：{{ item.KVARH }} kvar·h</span>
                    <span>日用电量：{{ item.dailyPower }} kWh</span>
                  </div>
                </div>
                
                <!-- 其他能源仪表数据 (nowtype=3/4/5) -->
                <div v-else class="space-y-1 data-table">
                  <div class="flex items-center text-sm">
                    <span class="text-gray-600 w-16">采集时间</span>
                    <span class="text-green-500">{{ item.equ_energy_dt }}</span>
                  </div>
                  <div class="grid grid-cols-2 gap-4 text-sm mt-0.5 data-row">
                    <span>温度：{{ item.energy_temperature }} ℃</span>
                    <span>压力：{{ item.energy_pressure }} MPa</span>
                  </div>
                  <div class="grid grid-cols-2 gap-4 text-sm mt-0.5 data-row">
                    <span>瞬时流量：{{ item.energy_winkvalue }}m³/h</span>
                    <span>累计值：{{ item.energy_accumulatevalue }}m³</span>
                  </div>
                  <div class="grid grid-cols-2 gap-4 text-sm mt-0.5 data-row">
                    <span>日用量：{{ item.dailyPower }}m³</span>
                    
                  </div>
                </div>
              </a-card>
            </template>
            <div v-else class="col-span-2 flex justify-center items-center h-64">
              <a-empty description="请选择左侧维度查看实时数据" />
            </div>
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {provide, ref, onMounted, onUnmounted, nextTick} from 'vue';
import RealTimeChart from './components/RealTimeChart.vue';
import HistoryChart from './components/HistoryChart.vue';
import ProductionParamsChart from './components/ProductionParamsChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

// 实时数据
const realTimeData = ref<any[]>([]);

// 当前选中的能源类型
const currentNowtype = ref(1);

// 当前选中的部门编码
const currentOrgCode = ref('');

// 当前激活的标签页
const activeTabKey = ref('info1');

// 维度列表
const dimensionList = ref<any[]>([]);

// 树组件引用
const treeRefs = ref<Record<string, any>>({});

// 设置树组件引用
const setTreeRef = (el, key) => {
  if (el) {
    treeRefs.value[key] = el;
  }
};

// 存储每个标签页选中的节点信息
const selectedNodesMap = ref({
  info1: null,
  info2: null,
  info3: null,
  info4: null,
  info5: null
});

// 定时刷新数据的定时器
let timer: number | null = null;

// 获取字典数据
function loadDimensionDictData() {
  defHttp.get({
    url: '/sys/dict/getDictItems/dimensionCode'
  })
  .then((res) => {
    if (res && Array.isArray(res)) {
      // 将字典数据转换为维度列表
      dimensionList.value = res.map((item, index) => {
        return {
          key: `info${index + 1}`,
          title: item.text,
          nowtype: Number(index + 1), // 使用索引+1作为nowtype值
          value: Number(index + 1)
        };
      });
      
      // 默认选中第一个标签页
      if (dimensionList.value.length > 0) {
        activeTabKey.value = dimensionList.value[0].key;
        currentNowtype.value = dimensionList.value[0].nowtype;
      }
    } else {
      // 如果获取字典失败，使用默认维度列表
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
    // 如果API调用失败，使用默认维度列表
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
  
  // 根据选中的标签页设置当前能源类型
  const selectedDimension = dimensionList.value.find(item => item.key === key);
  if (selectedDimension) {
    currentNowtype.value = selectedDimension.nowtype;
  }
  
  // 如果该标签页之前已经选择过节点，则使用保存的节点信息
  const savedNode = selectedNodesMap.value[key];
  if (savedNode) {
    currentOrgCode.value = savedNode.orgCode;
    loadRealTimeData(savedNode.orgCode, currentNowtype.value);
  }
  
  // 等待树组件加载完成后，如果没有选中的节点，则触发树组件的默认选择
  nextTick(() => {
    const currentTreeRef = treeRefs.value[key];
    if (currentTreeRef && !savedNode) {
      // 树组件会自动选择默认节点并触发select事件
    }
  });
}

// 获取当前激活标签页对应的树组件引用
function getCurrentTreeRef() {
  return treeRefs.value[activeTabKey.value];
}

// 左侧树选择后触发
function onDepartTreeSelect(data) {
  if (Array.isArray(data) && data.length > 0) {
    const orgCodestr = data.map(item => item.orgCode).join(',');
    currentOrgCode.value = orgCodestr;
    
    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: orgCodestr,
      data: data
    };
    
    loadRealTimeData(orgCodestr, currentNowtype.value);
  } else if (data && data.orgCode) {
    // 处理单个对象的情况
    currentOrgCode.value = data.orgCode;
    
    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: data.orgCode,
      data: data
    };
    
    loadRealTimeData(data.orgCode, currentNowtype.value);
  } else {
    console.log("没有选中任何项目");
  }
}

// 获取负荷状态对应的样式类
function getLoadStatusClass(status) {
  if (!status) return 'text-gray-500';
  
  switch (status) {
    case '正常':
      return 'text-green-500';
    case '警告':
      return 'text-yellow-500';
    case '异常':
      return 'text-red-500';
    default:
      return 'text-gray-500';
  }
}

// 加载实时监控数据
function loadRealTimeData(orgCode, nowtype) {
  if (!orgCode) return;
  
  loading.value = true;
  realTimeData.value = [];
  
  defHttp.get({
    url: '/energy/monitor/getRealTimeData',
    params: {
      orgCode: orgCode,
      nowtype: nowtype
    }
  })
  .then((res) => {
    if (res && Array.isArray(res)) {
      realTimeData.value = res;
    } else {
      createMessage.warning('获取实时数据失败');
    }
  })
  .catch((err) => {
    console.error('获取实时数据出错:', err);
    createMessage.error('获取实时数据出错');
  })
  .finally(() => {
    loading.value = false;
  });
}

// 初始化时设置默认标签页
onMounted(() => {
  // 加载维度字典数据
  loadDimensionDictData();
  
  // 等待树组件加载完成后触发默认选择
  nextTick(() => {
    const currentTreeRef = getCurrentTreeRef();
    if (currentTreeRef) {
      // 树组件会自动选择默认节点并触发select事件
    }
  });
  
  // 每30秒自动刷新数据
  timer = window.setInterval(() => {
    if (currentOrgCode.value) {
      // 使用当前激活标签页对应的nowtype值
      const nowtype = currentNowtype.value;
      loadRealTimeData(currentOrgCode.value, nowtype);
    }
  }, 30000);
});

// 组件卸载时清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.h-full {
  /*min-height: calc(100vh - 100px);*/
}

.real-data-monitor-container {
  @apply min-h-screen bg-gray-50;
}

.monitor-card {
  @apply bg-white rounded-lg shadow-sm;
}

/* 自定义标签页样式 */
:deep(.ant-tabs-nav) {
  @apply mb-4;
}

:deep(.ant-card-body) {
  @apply p-3;
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

/* 搜索框样式 */
:deep(.ant-input-search) {
  font-size: 13px;
}

/* 进度条样式 */
.progress-bar {
  width: 100px;
  max-width: 100px;
}

.bg-blue-500 {
  max-width: 100px;
}

/* 数据表格样式 */
.data-table {
  margin-top: 8px;
  padding: 8px;
  background-color: rgba(249, 250, 251, 0.5);
  border-radius: 6px;
}

/* 数据行样式 */
.data-row {
  padding: 6px 0;
  border-bottom: 1px solid;
  border-image: linear-gradient(to right, rgba(229, 231, 235, 0.1), rgba(209, 213, 219, 0.8), rgba(229, 231, 235, 0.1));
  border-image-slice: 1;
}

.data-row:last-child {
  border-bottom: none;
}

.data-row span {
  color: #374151;
}
</style>

