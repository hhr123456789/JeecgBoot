<template>
  <div class="flex h-full">
    <!-- 左侧部门树 -->
    <div class="w-64 bg-white p-4 border-r">
      <div class="mb-4">
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索部门"
          @search="onSearch"
          class="mb-2"
        />
        <div class="flex justify-between items-center mb-2">
          <span class="text-gray-600">部门列表</span>
          <a-button type="link" @click="expandAll">
            {{ isExpanded ? '收起' : '展开' }}
          </a-button>
        </div>
      </div>
      <a-tree
        v-model:expandedKeys="expandedKeys"
        v-model:selectedKeys="selectedKeys"
        :tree-data="treeData"
        :replaceFields="{ title: 'name', key: 'id' }"
        @select="onSelect"
      />
    </div>

    <!-- 右侧内容区 -->
    <div class="flex-1 p-4 bg-gray-50">
      <!-- 筛选区域 -->
      <div class="bg-white p-4 rounded-lg mb-4">
        <div class="flex items-center space-x-4">
          <a-range-picker
            v-model:value="dateRange"
          />
          <a-select
            v-model:value="alarmType"
            style="width: 120px"
            placeholder="告警类型"
          >
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="overload">超负荷</a-select-option>
            <a-select-option value="abnormal">异常运行</a-select-option>
            <a-select-option value="fault">故障</a-select-option>
          </a-select>
          <a-select
            v-model:value="alarmStatus"
            style="width: 120px"
            placeholder="告警状态"
          >
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="unhandled">未处理</a-select-option>
            <a-select-option value="handling">处理中</a-select-option>
            <a-select-option value="handled">已处理</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-4">
        <div class="bg-white p-4 rounded-lg">
          <div class="text-gray-500 mb-2">总告警数</div>
          <div class="text-2xl font-bold">{{ statistics.total }}</div>
        </div>
        <div class="bg-white p-4 rounded-lg">
          <div class="text-gray-500 mb-2">未处理告警</div>
          <div class="text-2xl font-bold text-red-500">
            {{ statistics.unhandled }}
          </div>
        </div>
        <div class="bg-white p-4 rounded-lg">
          <div class="text-gray-500 mb-2">处理中告警</div>
          <div class="text-2xl font-bold text-orange-500">
            {{ statistics.handling }}
          </div>
        </div>
        <div class="bg-white p-4 rounded-lg">
          <div class="text-gray-500 mb-2">已处理告警</div>
          <div class="text-2xl font-bold text-green-500">
            {{ statistics.handled }}
          </div>
        </div>
      </div>

      <!-- 告警列表 -->
      <div class="bg-white rounded-lg">
        <div class="p-4 border-b flex justify-between items-center">
          <div class="text-lg font-medium">告警列表</div>
          <div class="space-x-2">
            <a-button
              type="primary"
              :disabled="!selectedRowKeys.length"
              @click="handleBatchProcess"
            >
              批量处理
            </a-button>
            <a-button @click="handleExport">导出</a-button>
          </div>
        </div>
        <a-table
          :columns="columns"
          :data-source="alarmList"
          :row-selection="rowSelection"
          :pagination="pagination"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'level'">
              <a-tag :color="getLevelColor(record.level)">
                {{ getLevelText(record.level) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="handleProcess(record)">处理</a>
                <a @click="handleViewDetail(record)">详情</a>
                <a @click="handleViewHistory(record)">历史</a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 处理弹窗 -->
    <a-modal
      v-model:visible="processModalVisible"
      title="告警处理"
      @ok="handleProcessSubmit"
    >
      <a-form :model="processForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="处理方式">
          <a-select v-model:value="processForm.method">
            <a-select-option value="reset">重置</a-select-option>
            <a-select-option value="repair">维修</a-select-option>
            <a-select-option value="ignore">忽略</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理说明">
          <a-textarea
            v-model:value="processForm.description"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';

interface TreeNode {
  id: string;
  name: string;
  children?: TreeNode[];
}

interface AlarmRecord {
  id: string;
  time: string;
  level: 'high' | 'medium' | 'low';
  type: string;
  content: string;
  device: string;
  department: string;
  status: 'unhandled' | 'handling' | 'handled';
}

interface ProcessForm {
  method: 'reset' | 'repair' | 'ignore';
  description: string;
}

// 部门树相关
const searchText = ref('');
const isExpanded = ref(false);
const expandedKeys = ref<string[]>([]);
const selectedKeys = ref<string[]>([]);
const treeData = ref<TreeNode[]>([
  {
    id: '1',
    name: '生产部',
    children: [
      { id: '1-1', name: '一号车间' },
      { id: '1-2', name: '二号车间' },
    ],
  },
  {
    id: '2',
    name: '设备部',
    children: [
      { id: '2-1', name: '设备维护组' },
      { id: '2-2', name: '设备管理组' },
    ],
  },
]);

// 筛选相关
const dateRange = ref<[Dayjs, Dayjs]>([dayjs(), dayjs()]);
const alarmType = ref('all');
const alarmStatus = ref('all');

// 统计数据
const statistics = reactive({
  total: 100,
  unhandled: 30,
  handling: 20,
  handled: 50,
});

// 表格相关
const selectedRowKeys = ref<string[]>([]);
const columns: TableColumnsType = [
  {
    title: '告警时间',
    dataIndex: 'time',
    key: 'time',
    width: 180,
  },
  {
    title: '告警等级',
    dataIndex: 'level',
    key: 'level',
    width: 100,
  },
  {
    title: '告警类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '告警内容',
    dataIndex: 'content',
    key: 'content',
  },
  {
    title: '设备名称',
    dataIndex: 'device',
    key: 'device',
    width: 150,
  },
  {
    title: '所属部门',
    dataIndex: 'department',
    key: 'department',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
  },
];

const alarmList = ref<AlarmRecord[]>([
  {
    id: '1',
    time: '2024-01-20 10:00:00',
    level: 'high',
    type: '超负荷',
    content: '设备运行电流超过额定值',
    device: '空压机-01',
    department: '一号车间',
    status: 'unhandled',
  },
  {
    id: '2',
    time: '2024-01-20 09:30:00',
    level: 'medium',
    type: '异常运行',
    content: '设备温度异常',
    device: '注塑机-02',
    department: '二号车间',
    status: 'handling',
  },
]);

const pagination = reactive({
  total: 100,
  current: 1,
  pageSize: 10,
});

const rowSelection = {
  selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys;
  },
};

// 处理弹窗相关
const processModalVisible = ref(false);
const processForm = reactive<ProcessForm>({
  method: 'reset',
  description: '',
});

// 方法定义
const onSearch = (value: string) => {
  console.log('search:', value);
};

const expandAll = () => {
  isExpanded.value = !isExpanded.value;
  expandedKeys.value = isExpanded.value ? treeData.value.map(node => node.id) : [];
};

const onSelect = (selectedKeys: string[]) => {
  console.log('selected:', selectedKeys);
};

const handleSearch = () => {
  console.log('search with:', {
    dateRange: dateRange.value,
    alarmType: alarmType.value,
    alarmStatus: alarmStatus.value,
  });
};

const handleReset = () => {
  dateRange.value = [dayjs(), dayjs()];
  alarmType.value = 'all';
  alarmStatus.value = 'all';
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

const handleProcess = (record: AlarmRecord) => {
  processModalVisible.value = true;
};

const handleBatchProcess = () => {
  processModalVisible.value = true;
};

const handleProcessSubmit = () => {
  console.log('process form:', processForm);
  processModalVisible.value = false;
};

const handleViewDetail = (record: AlarmRecord) => {
  console.log('view detail:', record);
};

const handleViewHistory = (record: AlarmRecord) => {
  console.log('view history:', record);
};

const handleExport = () => {
  console.log('export data');
};

const getLevelColor = (level: string) => {
  const colors = {
    high: 'red',
    medium: 'orange',
    low: 'blue',
  };
  return colors[level as keyof typeof colors];
};

const getLevelText = (level: string) => {
  const texts = {
    high: '高',
    medium: '中',
    low: '低',
  };
  return texts[level as keyof typeof texts];
};

const getStatusColor = (status: string) => {
  const colors = {
    unhandled: 'red',
    handling: 'orange',
    handled: 'green',
  };
  return colors[status as keyof typeof colors];
};

const getStatusText = (status: string) => {
  const texts = {
    unhandled: '未处理',
    handling: '处理中',
    handled: '已处理',
  };
  return texts[status as keyof typeof texts];
};

// 生命周期钩子
onMounted(() => {
  // 初始化数据
});
</script>