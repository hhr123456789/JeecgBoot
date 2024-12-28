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
      <!-- 顶部搜索和操作区 -->
      <div class="bg-white p-4 rounded-lg mb-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <a-input-search
              v-model:value="templateName"
              placeholder="请输入模板名称"
              style="width: 200px"
            />
            <a-select
              v-model:value="templateType"
              style="width: 200px"
              placeholder="请选择模板类型"
            >
              <a-select-option value="all">全部</a-select-option>
              <a-select-option value="device">设备告警</a-select-option>
              <a-select-option value="energy">能源告警</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </div>
          <div class="space-x-2">
            <a-button type="primary" @click="handleAddDeviceTemplate">
              新增设备告警模板
            </a-button>
            <a-button type="primary" @click="handleAddEnergyTemplate">
              新增用电告警模板
            </a-button>
          </div>
        </div>
      </div>

      <!-- 模板列表 -->
      <div class="bg-white rounded-lg">
        <a-table
          :columns="columns"
          :data-source="templateList"
          :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
          :pagination="pagination"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <a-tag :color="getTemplateTypeColor(record.type)">
                {{ getTemplateTypeText(record.type) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="handleView(record)">查看</a>
                <a @click="handleEdit(record)">编辑</a>
                <a-popconfirm
                  title="确定要删除此模板吗？"
                  @confirm="handleDelete(record)"
                >
                  <a class="text-red-500">删除</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 新增/编辑模板弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleModalSubmit"
    >
      <a-form
        :model="formState"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="模板名称" required>
          <a-input v-model:value="formState.name" placeholder="请输入模板���称" />
        </a-form-item>
        <a-form-item label="设备类型" required>
          <a-select v-model:value="formState.deviceType" placeholder="请选择设备类型">
            <a-select-option value="GFMT">仪表类/GFMT</a-select-option>
            <a-select-option value="CEC">虚拟类/CEC</a-select-option>
            <a-select-option value="ACOP">生产辅助设备/ACOP</a-select-option>
            <a-select-option value="WMCT">供冷热辅机设备/WMCT</a-select-option>
            <a-select-option value="METE">仪表类/METE</a-select-option>
            <a-select-option value="ELEV">通用辅助设备/ELEV</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模板类型" required>
          <a-select v-model:value="formState.type" placeholder="请选择模板类型">
            <a-select-option value="device">设备告警</a-select-option>
            <a-select-option value="energy">能源告警</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="告警规则" required>
          <a-textarea
            v-model:value="formState.rules"
            :rows="4"
            placeholder="请输入告警规则"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
            v-model:value="formState.remark"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import dayjs from 'dayjs';

// 类型定义
interface TreeNode {
  id: string;
  name: string;
  children?: TreeNode[];
}

interface TemplateRecord {
  id: string;
  name: string;
  deviceType: string;
  type: 'device' | 'energy';
  rules: string;
  remark?: string;
  updater: string;
  updateTime: string;
}

interface FormState {
  name: string;
  deviceType: string;
  type: 'device' | 'energy';
  rules: string;
  remark: string;
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

// 搜索相��
const templateName = ref('');
const templateType = ref('all');

// 表格相关
const selectedRowKeys = ref<string[]>([]);
const columns: TableColumnsType = [
  {
    title: '模板名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
  },
  {
    title: '模板类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '更新人员',
    dataIndex: 'updater',
    key: 'updater',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
  },
];

// 静态数据
const templateList = ref<TemplateRecord[]>([
  {
    id: '1',
    name: '燃气表设备告警',
    deviceType: '仪表类/GFMT',
    type: 'device',
    rules: '当设备运行电流超过额定值时触发告警',
    updater: '张三',
    updateTime: '2023-12-06 20:35:09',
  },
  {
    id: '2',
    name: '工段1用电量超限',
    deviceType: '虚拟类/CEC',
    type: 'energy',
    rules: '当日用电量超过设定阈值时触发告警',
    updater: '李晓明',
    updateTime: '2023-11-29 11:29:48',
  },
]);

const pagination = reactive({
  total: 100,
  current: 1,
  pageSize: 10,
});

// 弹窗相关
const modalVisible = ref(false);
const modalTitle = ref('新增模板');
const formState = reactive<FormState>({
  name: '',
  deviceType: '',
  type: 'device',
  rules: '',
  remark: '',
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
    templateName: templateName.value,
    templateType: templateType.value,
  });
};

const handleReset = () => {
  templateName.value = '';
  templateType.value = 'all';
};

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

const handleAddDeviceTemplate = () => {
  modalTitle.value = '新增设备告警模板';
  formState.type = 'device';
  modalVisible.value = true;
};

const handleAddEnergyTemplate = () => {
  modalTitle.value = '新增用电告警模板';
  formState.type = 'energy';
  modalVisible.value = true;
};

const handleView = (record: TemplateRecord) => {
  console.log('view template:', record);
};

const handleEdit = (record: TemplateRecord) => {
  modalTitle.value = '编辑模板';
  Object.assign(formState, record);
  modalVisible.value = true;
};

const handleDelete = (record: TemplateRecord) => {
  console.log('delete template:', record);
};

const handleModalSubmit = () => {
  console.log('submit form:', formState);
  modalVisible.value = false;
};

const getTemplateTypeColor = (type: string) => {
  return type === 'device' ? 'blue' : 'green';
};

const getTemplateTypeText = (type: string) => {
  return type === 'device' ? '设备告警' : '能源告警';
};

// 生命周期钩子
onMounted(() => {
  // 初始化数据
});
</script> 