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
      <div class="real-run-status-container p-4">
        <!-- 设备状态筛选区域 -->
        <div class="mb-4 bg-white rounded-lg shadow-sm">
          <a-form layout="inline" class="px-4 py-3">
            <div class="flex items-center">
              <span class="mr-2 w-20 text-right">设备状态:</span>
              <a-select v-model:value="filterForm.deviceStatus" class="!w-32">
                <a-select-option value="0">全部</a-select-option>
                <a-select-option value="1">运行中</a-select-option>
                <a-select-option value="2">已停止</a-select-option>
                
                <a-select-option value="3">通讯故障</a-select-option>
              </a-select>
              <a-space class="ml-6">
                <a-button type="primary" @click="handleSearch">查询</a-button>
                <a-button @click="handleReset">重置</a-button>
              </a-space>
            </div>
          </a-form>
        </div>

        <!-- 数据列表区域 -->
        <a-spin :spinning="loading">
          <div class="bg-white p-4 rounded-lg shadow-sm mb-4">
            <!-- 电力设备表格 (nowtype=1或2) -->
            <a-table
              v-if="currentNowtype === 1 || currentNowtype === 2"
              :columns="electricColumns"
              :data-source="tableData"
              :pagination="false"
              size="middle"
              rowKey="device_name"
            >
              <!-- 设备名称 -->
              <template #deviceName="{ text }">
                <span>{{ text }}</span>
              </template>
              
              <!-- 电流/电压/功率因素 -->
              <template #current="{ text }">
                <span>{{ formatNumber(text, 2) }} A</span>
              </template>
              <template #voltage="{ text }">
                <span>{{ formatNumber(text, 2) }} V</span>
              </template>
              <template #powerFactor="{ text }">
                <span>{{ formatNumber(text, 2) }}</span>
              </template>
              <template #realPower="{ text }">
                <span>{{ formatNumber(text, 2) }} kW</span>
              </template>
              <!-- 运行状态 -->
              <template #status="{ text, record }">
                <a-tag :color="getStatusColor(record.status_code)" 
                  :class="{
                    'text-red-500 font-bold': record.status_code === 3 || record.status_code === 4
                  }">
                  {{ text }}
                </a-tag>
              </template>
              <!-- 最后采集时间 -->
              <template #lastCollectionTime="{ text }">
                <span>{{ text }}</span>
              </template>
            </a-table>

            <!-- 其他能源设备表格 (nowtype=3/4/5) -->
            <a-table
              v-else
              :columns="energyColumns"
              :data-source="tableData"
              :pagination="false"
              size="middle"
              rowKey="device_name"
            >
              <!-- 设备名称 -->
              <template #deviceName="{ text }">
                <span>{{ text }}</span>
              </template>
              
              <!-- 温度/压力/流量 -->
              <template #temperature="{ text }">
                <span>{{ formatNumber(text, 2) }} ℃</span>
              </template>
              <template #pressure="{ text }">
                <span>{{ formatNumber(text, 2) }} MPa</span>
              </template>
              <template #flowRate="{ text }">
                <span>{{ formatNumber(text, 3) }} m³/h</span>
              </template>
              <template #accumulate="{ text }">
                <span>{{ formatNumber(text, 2) }} m³</span>
              </template>
              <!-- 运行状态 -->
              <template #status="{ text, record }">
                <a-tag :color="getStatusColor(record.status_code)" 
                  :class="{
                    'text-red-500 font-bold': record.status_code === 3 || record.status_code === 4
                  }">
                  {{ text }}
                </a-tag>
              </template>
              <!-- 最后采集时间 -->
              <template #lastCollectionTime="{ text }">
                <span>{{ text }}</span>
              </template>
            </a-table>

            <div v-if="tableData.length === 0" class="flex justify-center items-center h-64">
              <a-empty description="请选择左侧维度查看运行状态数据" />
            </div>
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

// 筛选表单数据
const filterForm = ref({
  deviceStatus: '0'
});

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

// 表格数据
const tableData = ref<any[]>([]);

// 电力设备表格列定义
const electricColumns: TableColumnsType = [
  {
    title: '设备名称',
    dataIndex: 'device_name',
    width: 180,
    align: 'left',
    slots: { customRender: 'deviceName' }
  },
  {
    title: '所属车间',
    dataIndex: '所属车间',
    width: 120,
    align: 'center'
  },
  {
    title: '电流(A)',
    dataIndex: '电流(A)',
    width: 100,
    align: 'right',
    slots: { customRender: 'current' }
  },
  {
    title: '电压(V)',
    dataIndex: '电压(V)',
    width: 100,
    align: 'right',
    slots: { customRender: 'voltage' }
  },
  {
    title: '功率因素',
    dataIndex: '功率因素',
    width: 100,
    align: 'right',
    slots: { customRender: 'powerFactor' }
  },
  {
    title: '有功功率(kW)',
    dataIndex: '有功功率(kW)',
    width: 120,
    align: 'right',
    slots: { customRender: 'realPower' }
  },
  {
    title: '运行状态',
    dataIndex: '运行状态',
    width: 100,
    align: 'center',
    slots: { customRender: 'status' }
  },
  {
    title: '最后采集时间',
    dataIndex: 'last_collection_time',
    width: 160,
    align: 'center',
    slots: { customRender: 'lastCollectionTime' }
  }
];

// 其他能源设备表格列定义
const energyColumns: TableColumnsType = [
  {
    title: '设备名称',
    dataIndex: 'device_name',
    width: 180,
    align: 'left',
    slots: { customRender: 'deviceName' }
  },
  {
    title: '所属车间',
    dataIndex: '所属车间',
    width: 120,
    align: 'center'
  },
  {
    title: '温度(℃)',
    dataIndex: '温度(℃)',
    width: 100,
    align: 'right',
    slots: { customRender: 'temperature' }
  },
  {
    title: '压力(MPa)',
    dataIndex: '压力(MPa)',
    width: 100,
    align: 'right',
    slots: { customRender: 'pressure' }
  },
  {
    title: '瞬时流量(m³/h)',
    dataIndex: '瞬时流量(m³/h)',
    width: 130,
    align: 'right',
    slots: { customRender: 'flowRate' }
  },
  {
    title: '累计值(m³)',
    dataIndex: '累计值(m³)',
    width: 120,
    align: 'right',
    slots: { customRender: 'accumulate' }
  },
  {
    title: '运行状态',
    dataIndex: '运行状态',
    width: 100,
    align: 'center',
    slots: { customRender: 'status' }
  },
  {
    title: '最后采集时间',
    dataIndex: 'last_collection_time',
    width: 160,
    align: 'center',
    slots: { customRender: 'lastCollectionTime' }
  }
];

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
    loadRunStatusData(savedNode.orgCode, currentNowtype.value);
  } else {
    // 清空表格数据
    tableData.value = [];
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
    
    loadRunStatusData(orgCodestr, currentNowtype.value);
  } else if (data && data.orgCode) {
    // 处理单个对象的情况
    currentOrgCode.value = data.orgCode;
    
    // 保存当前标签页选中的节点信息
    selectedNodesMap.value[activeTabKey.value] = {
      orgCode: data.orgCode,
      data: data
    };
    
    loadRunStatusData(data.orgCode, currentNowtype.value);
  } else {
    console.log("没有选中任何项目");
    tableData.value = [];
  }
}

// 获取状态颜色
const getStatusColor = (statusCode: number) => {
  const colorMap: Record<number, string> = {
    1: 'success',  // 运行中
    2: 'default',  // 已停止
    3: 'error'     // 通讯故障，改为与故障相同的颜色
  };
  return colorMap[statusCode] || 'default';
};

// 格式化数字
const formatNumber = (value: any, decimals = 2) => {
  if (value === null || value === undefined) return '0.00';
  if (typeof value === 'string') value = parseFloat(value);
  return value.toFixed(decimals);
};

// 处理查询
const handleSearch = () => {
  if (currentOrgCode.value) {
    loadRunStatusData(currentOrgCode.value, currentNowtype.value);
  } else {
    createMessage.warning('请先选择左侧维度');
  }
};

// 处理重置
const handleReset = () => {
  filterForm.value = {
    deviceStatus: '0'
  };
  
  if (currentOrgCode.value) {
    loadRunStatusData(currentOrgCode.value, currentNowtype.value);
  }
};

// 加载运行状态数据
function loadRunStatusData(orgCode, nowtype) {
  if (!orgCode) return;
  
  loading.value = true;
  tableData.value = [];
  
  // 调用后端API获取运行状态数据
  defHttp.get({
    url: '/energy/monitor/getRunStatus',
    params: {
      orgCode: orgCode,
      deviceStatus: filterForm.value.deviceStatus,
      nowtype: nowtype // 添加能源类型参数
    }
  })
  .then((res) => {
    console.log(`获取能源类型 ${nowtype} 的数据:`, res);
    if (res && Array.isArray(res)) {
      tableData.value = res;
    } else {
      createMessage.warning('获取运行状态数据失败');
    }
  })
  .catch((err) => {
    console.error('获取运行状态数据出错:', err);
    createMessage.error('获取运行状态数据出错');
  })
  .finally(() => {
    loading.value = false;
  });
}

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
      loadRunStatusData(currentOrgCode.value, currentNowtype.value);
    }
  }, 30000);
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
  /*min-height: calc(100vh - 100px);*/
}

.real-run-status-container {
  @apply min-h-screen bg-gray-50;
}

/* 表格样式调整 */
:deep(.ant-table-thead > tr > th) {
  @apply text-center bg-gray-50;
  padding: 8px 16px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 8px 16px;
}

/* 数值类型的单元格样式 */
:deep(.ant-table-tbody > tr > td.ant-table-cell-right) {
  @apply font-mono;
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

/* 故障状态文字样式 */
:deep(.text-red-500) {
  color: #f5222d !important;
}

/* 删除通讯故障状态文字样式 */
/* :deep(.text-yellow-500) {
  color: #faad14 !important;
} */

:deep(.font-bold) {
  font-weight: bold;
}
</style> 