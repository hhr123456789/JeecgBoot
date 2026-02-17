<template>
  <div class="flex h-full">
    <!-- 左侧模板分类 -->
    <div class="w-64 bg-white p-4 border-r">
      <div class="mb-4">
        <div class="text-base font-medium mb-3">告警模板分类</div>
        <a-menu v-model:selectedKeys="selectedCategory" mode="inline">
          <a-menu-item key="all">
            <template #icon><AppstoreOutlined /></template>
            全部模板
          </a-menu-item>
          <a-sub-menu key="energy">
            <template #icon><ThunderboltOutlined /></template>
            <template #title>能源告警模板</template>
            <a-menu-item key="energy_electric">电力监控</a-menu-item>
            <a-menu-item key="energy_water">水监控</a-menu-item>
            <a-menu-item key="energy_gas">天然气监控</a-menu-item>
            <a-menu-item key="energy_air">压缩空气监控</a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="device">
            <template #icon><ToolOutlined /></template>
            <template #title>设备告警模板</template>
            <a-menu-item key="device_meter">仪表设备</a-menu-item>
            <a-menu-item key="device_production">生产设备</a-menu-item>
            <a-menu-item key="device_auxiliary">辅助设备</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </div>

      <a-divider />

      <!-- 快速操作 -->
      <div class="mt-4">
        <div class="text-sm text-gray-500 mb-2">快速操作</div>
        <a-button type="dashed" block size="small" @click="handleImportTemplate" class="mb-2">
          <template #icon><ImportOutlined /></template>
          导入模板
        </a-button>
        <a-button type="dashed" block size="small" @click="handleExportTemplate">
          <template #icon><ExportOutlined /></template>
          导出模板
        </a-button>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="flex-1 p-4 bg-gray-50">
      <!-- 顶部操作栏 -->
      <div class="bg-white p-4 rounded-lg mb-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索模板名称或标签"
              style="width: 250px"
              @search="handleSearch"
            />
            <a-select
              v-model:value="filterStatus"
              style="width: 120px"
              placeholder="使用状态"
            >
              <a-select-option value="all">全部状态</a-select-option>
              <a-select-option value="enabled">已启用</a-select-option>
              <a-select-option value="disabled">已禁用</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </div>
          <div class="space-x-2">
            <a-button type="primary" @click="handleAddTemplate">
              <template #icon><PlusOutlined /></template>
              新增模板
            </a-button>
            <a-button 
              danger 
              :disabled="!selectedRowKeys.length"
              @click="handleBatchDelete"
            >
              批量删除
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
            <template v-if="column.key === 'category'">
              <a-tag :color="getCategoryColor(record.category)">
                {{ getCategoryText(record.category) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'energyType'">
              <a-tag color="blue">{{ getEnergyTypeText(record.energyType) }}</a-tag>
            </template>
            <template v-if="column.key === 'tags'">
              <a-tag v-for="tag in record.tags" :key="tag" class="mb-1">
                {{ tag }}
              </a-tag>
            </template>
            <template v-if="column.key === 'usageCount'">
              <span class="text-blue-600 cursor-pointer" @click="handleViewUsage(record)">
                {{ record.usageCount }}
              </span>
            </template>
            <template v-if="column.key === 'status'">
              <a-switch
                v-model:checked="record.status"
                :checked-children="'启用'"
                :un-checked-children="'禁用'"
                @change="(checked) => handleStatusChange(record, checked)"
              />
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="handleView(record)">查看</a>
                <a @click="handleEdit(record)">编辑</a>
                <a @click="handleCopy(record)">复制</a>
                <a-popconfirm
                  title="确定要删除此模板吗？删除后将影响使用该模板的规则。"
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
      v-model:open="modalVisible"
      :title="modalTitle"
      width="900px"
      @ok="handleModalSubmit"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
        :rules="formRules"
      >
        <a-divider orientation="left">基本信息</a-divider>
        
        <a-form-item label="模板名称" name="name">
          <a-input 
            v-model:value="formState.name" 
            placeholder="请输入模板名称，如：空压机运行监控模板" 
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="模板分类" name="category">
          <a-select 
            v-model:value="formState.category" 
            placeholder="请选择模板分类"
            @change="handleCategoryChange"
          >
            <a-select-opt-group label="能源告警模板">
              <a-select-option value="energy_electric">电力监控</a-select-option>
              <a-select-option value="energy_water">水监控</a-select-option>
              <a-select-option value="energy_gas">天然气监控</a-select-option>
              <a-select-option value="energy_air">压缩空气监控</a-select-option>
            </a-select-opt-group>
            <a-select-opt-group label="设备告警模板">
              <a-select-option value="device_meter">仪表设备</a-select-option>
              <a-select-option value="device_production">生产设备</a-select-option>
              <a-select-option value="device_auxiliary">辅助设备</a-select-option>
            </a-select-opt-group>
          </a-select>
        </a-form-item>

        <a-form-item label="能源类型" name="energyType">
          <a-select 
            v-model:value="formState.energyType" 
            placeholder="请选择能源类型"
          >
            <a-select-option value="1">电力</a-select-option>
            <a-select-option value="2">水</a-select-option>
            <a-select-option value="8">天然气</a-select-option>
            <a-select-option value="5">压缩空气</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="模板标签" name="tags">
          <a-select
            v-model:value="formState.tags"
            mode="tags"
            placeholder="添加标签，便于分类和搜索"
            :max-tag-count="5"
          />
          <div class="text-gray-400 text-xs mt-1">
            建议标签：高耗能、关键设备、24小时监控、生产线等
          </div>
        </a-form-item>

        <a-divider orientation="left">模板配置（预设告警条件）</a-divider>
        
        <div class="template-config-area bg-gray-50 p-4 rounded mb-4">
          <div 
            v-for="(config, index) in formState.ruleConfigs" 
            :key="index"
            class="config-item bg-white p-3 mb-3 rounded border"
          >
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">预设条件 {{ index + 1 }}</span>
              <a-button 
                type="link" 
                danger 
                size="small"
                @click="removeConfig(index)"
                v-if="formState.ruleConfigs.length > 1"
              >
                删除
              </a-button>
            </div>
            
            <a-row :gutter="16">
              <a-col :span="8">
                <span class="text-xs text-gray-500">监控指标</span>
                <a-select 
                  v-model:value="config.metric" 
                  placeholder="选择监控指标"
                  style="width: 100%"
                  class="mt-1"
                >
                  <a-select-opt-group label="设备运行指标">
                    <a-select-option value="device_status">设备状态</a-select-option>
                    <a-select-option value="device_current">运行电流</a-select-option>
                    <a-select-option value="device_voltage">运行电压</a-select-option>
                    <a-select-option value="device_power">运行功率</a-select-option>
                    <a-select-option value="device_temperature">设备温度</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="能源消耗指标">
                    <a-select-option value="hour_consumption">小时用量</a-select-option>
                    <a-select-option value="day_consumption">日用量</a-select-option>
                    <a-select-option value="month_consumption">月用量</a-select-option>
                  </a-select-opt-group>
                </a-select>
              </a-col>
              
              <a-col :span="6">
                <span class="text-xs text-gray-500">比较方式</span>
                <a-select 
                  v-model:value="config.operator" 
                  placeholder="比较"
                  style="width: 100%"
                  class="mt-1"
                >
                  <a-select-option value="gt">大于</a-select-option>
                  <a-select-option value="gte">大于等于</a-select-option>
                  <a-select-option value="lt">小于</a-select-option>
                  <a-select-option value="lte">小于等于</a-select-option>
                </a-select>
              </a-col>
              
              <a-col :span="6">
                <span class="text-xs text-gray-500">默认阈值</span>
                <a-input-number 
                  v-model:value="config.defaultThreshold" 
                  placeholder="默认值"
                  :precision="2"
                  style="width: 100%"
                  class="mt-1"
                />
              </a-col>

              <a-col :span="4">
                <span class="text-xs text-gray-500">单位</span>
                <a-input 
                  v-model:value="config.unit" 
                  placeholder="单位"
                  style="width: 100%"
                  class="mt-1"
                />
              </a-col>
            </a-row>

            <a-row :gutter="16" class="mt-2">
              <a-col :span="12">
                <span class="text-xs text-gray-500">默认告警级别</span>
                <a-select 
                  v-model:value="config.defaultLevel" 
                  placeholder="告警级别"
                  style="width: 100%"
                  class="mt-1"
                >
                  <a-select-option value="high">高</a-select-option>
                  <a-select-option value="medium">中</a-select-option>
                  <a-select-option value="low">低</a-select-option>
                </a-select>
              </a-col>
              
              <a-col :span="12">
                <span class="text-xs text-gray-500">建议描述</span>
                <a-input 
                  v-model:value="config.description" 
                  placeholder="如：超过额定电流，需立即检查"
                  style="width: 100%"
                  class="mt-1"
                />
              </a-col>
            </a-row>
          </div>

          <a-button 
            type="dashed" 
            block 
            @click="addConfig"
            class="mt-2"
          >
            <template #icon><PlusOutlined /></template>
            添加预设条件
          </a-button>
        </div>

        <a-divider orientation="left">使用说明</a-divider>

        <a-form-item label="适用场景" name="applicableScene">
          <a-textarea
            v-model:value="formState.applicableScene"
            :rows="2"
            placeholder="如：适用于空压机、冷水机组等大型生产辅助设备的运行监控"
            :maxlength="200"
            show-count
          />
        </a-form-item>

        <a-form-item label="使用说明" name="usageGuide">
          <a-textarea
            v-model:value="formState.usageGuide"
            :rows="3"
            placeholder="如：1. 根据设备铭牌参数调整阈值&#10;2. 建议设置静默期避免频繁告警&#10;3. 高级别告警需配置短信通知"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formState.remark"
            :rows="2"
            placeholder="其他备注信息"
            :maxlength="200"
            show-count
          />
        </a-form-item>

        <a-form-item label="启用状态" name="status">
          <a-switch
            v-model:checked="formState.status"
            :checked-children="'启用'"
            :un-checked-children="'禁用'"
          />
          <span class="ml-2 text-gray-400 text-xs">
            禁用后无法在创建规则时选择此模板
          </span>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import { 
  PlusOutlined, 
  AppstoreOutlined, 
  ThunderboltOutlined, 
  ToolOutlined,
  ImportOutlined,
  ExportOutlined 
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

// 类型定义
interface RuleConfig {
  metric: string;
  operator: string;
  defaultThreshold: number;
  unit: string;
  defaultLevel: 'high' | 'medium' | 'low';
  description: string;
}

interface TemplateRecord {
  id: string;
  name: string;
  category: string;
  energyType: string;
  tags: string[];
  ruleConfigs: RuleConfig[];
  applicableScene: string;
  usageGuide: string;
  remark?: string;
  usageCount: number;
  status: boolean;
  creator: string;
  createTime: string;
  updater: string;
  updateTime: string;
}

interface FormState {
  name: string;
  category: string;
  energyType: string;
  tags: string[];
  ruleConfigs: RuleConfig[];
  applicableScene: string;
  usageGuide: string;
  remark: string;
  status: boolean;
}

// 状态变量
const selectedCategory = ref<string[]>(['all']);
const searchKeyword = ref('');
const filterStatus = ref('all');
const selectedRowKeys = ref<string[]>([]);

// 表格配置
const columns: TableColumnsType = [
  {
    title: '模板名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: 120,
  },
  {
    title: '能源类型',
    dataIndex: 'energyType',
    key: 'energyType',
    width: 100,
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags',
    width: 200,
  },
  {
    title: '使用次数',
    dataIndex: 'usageCount',
    key: 'usageCount',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '更新人员',
    dataIndex: 'updater',
    key: 'updater',
    width: 120,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
];

// 示例数据
const templateList = ref<TemplateRecord[]>([
  {
    id: '1',
    name: '空压机运行监控模板',
    category: 'device_production',
    energyType: '1',
    tags: ['生产设备', '高耗能', '24小时监控'],
    ruleConfigs: [
      {
        metric: 'device_current',
        operator: 'gt',
        defaultThreshold: 150,
        unit: 'A',
        defaultLevel: 'high',
        description: '电流超限，设备可能过载'
      },
      {
        metric: 'device_temperature',
        operator: 'gt',
        defaultThreshold: 80,
        unit: '℃',
        defaultLevel: 'medium',
        description: '温度过高，需检查散热'
      }
    ],
    applicableScene: '适用于空压机等大型生产辅助设备的运行监控',
    usageGuide: '1. 根据设备铭牌参数调整阈值\n2. 建议设置静默期避免频繁告警',
    usageCount: 15,
    status: true,
    creator: '张三',
    createTime: '2024-01-01 10:00:00',
    updater: '张三',
    updateTime: '2024-01-06 20:35:09',
  },
  {
    id: '2',
    name: '车间用电量监控模板',
    category: 'energy_electric',
    energyType: '1',
    tags: ['能源监控', '用电管理'],
    ruleConfigs: [
      {
        metric: 'day_consumption',
        operator: 'gt',
        defaultThreshold: 10000,
        unit: 'kWh',
        defaultLevel: 'medium',
        description: '日用电量超限'
      }
    ],
    applicableScene: '适用于车间、部门级别的用电量监控',
    usageGuide: '1. 根据历史用电量设置合理阈值\n2. 可配置按时段监控',
    usageCount: 28,
    status: true,
    creator: '李四',
    createTime: '2023-12-15 14:20:00',
    updater: '李四',
    updateTime: '2024-01-05 11:29:48',
  },
  {
    id: '3',
    name: '天然气消耗监控模板',
    category: 'energy_gas',
    energyType: '8',
    tags: ['天然气', '能源监控'],
    ruleConfigs: [
      {
        metric: 'day_consumption',
        operator: 'gt',
        defaultThreshold: 5000,
        unit: 'm³',
        defaultLevel: 'high',
        description: '日用气量超限'
      },
      {
        metric: 'consumption_rate',
        operator: 'gt',
        defaultThreshold: 20,
        unit: '%',
        defaultLevel: 'medium',
        description: '用气量增长异常'
      }
    ],
    applicableScene: '适用于使用天然气的生产线或车间',
    usageGuide: '1. 结合生产计划设置阈值\n2. 注意季节性用气变化',
    usageCount: 8,
    status: true,
    creator: '王五',
    createTime: '2023-11-20 09:30:00',
    updater: '王五',
    updateTime: '2024-01-04 15:20:33',
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
const formRef = ref();
const formState = reactive<FormState>({
  name: '',
  category: '',
  energyType: '1',
  tags: [],
  ruleConfigs: [
    {
      metric: '',
      operator: 'gt',
      defaultThreshold: 0,
      unit: '',
      defaultLevel: 'medium',
      description: '',
    }
  ],
  applicableScene: '',
  usageGuide: '',
  remark: '',
  status: true,
});

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择模板分类', trigger: 'change' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  applicableScene: [{ required: true, message: '请输入适用场景', trigger: 'blur' }],
};

// 方法定义
const handleSearch = () => {
  console.log('search with:', {
    keyword: searchKeyword.value,
    category: selectedCategory.value,
    status: filterStatus.value,
  });
  // TODO: 调用后端API查询
};

const handleReset = () => {
  searchKeyword.value = '';
  filterStatus.value = 'all';
  selectedCategory.value = ['all'];
};

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

const handleAddTemplate = () => {
  modalTitle.value = '新增告警模板';
  resetForm();
  modalVisible.value = true;
};

const handleView = (record: TemplateRecord) => {
  console.log('view template:', record);
  // TODO: 显示模板详情
};

const handleEdit = (record: TemplateRecord) => {
  modalTitle.value = '编辑告警模板';
  Object.assign(formState, {
    ...record,
    ruleConfigs: JSON.parse(JSON.stringify(record.ruleConfigs))
  });
  modalVisible.value = true;
};

const handleCopy = (record: TemplateRecord) => {
  modalTitle.value = '复制告警模板';
  Object.assign(formState, {
    ...record,
    name: record.name + ' - 副本',
    ruleConfigs: JSON.parse(JSON.stringify(record.ruleConfigs))
  });
  modalVisible.value = true;
};

const handleDelete = (record: TemplateRecord) => {
  console.log('delete template:', record);
  message.success('删除成功');
  // TODO: 调用后端API删除
};

const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }
  message.success(`已删除 ${selectedRowKeys.value.length} 个模板`);
  // TODO: 调用后端API批量删除
};

const handleViewUsage = (record: TemplateRecord) => {
  console.log('view usage:', record);
  // TODO: 显示使用该模板的规则列表
  message.info(`该模板被 ${record.usageCount} 个规则使用`);
};

const handleStatusChange = (record: TemplateRecord, checked: boolean) => {
  console.log('status change:', record, checked);
  message.success(checked ? '模板已启用' : '模板已禁用');
  // TODO: 调用后端API更新状态
};

const handleImportTemplate = () => {
  message.info('导入模板功能开发中');
  // TODO: 实现模板导入功能
};

const handleExportTemplate = () => {
  message.info('导出模板功能开发中');
  // TODO: 实现模板导出功能
};

const handleModalSubmit = () => {
  formRef.value?.validate().then(() => {
    console.log('submit form:', formState);
    message.success('保存成功');
    modalVisible.value = false;
    // TODO: 调用后端API保存
  }).catch((error: any) => {
    console.error('Validation failed:', error);
  });
};

const handleModalCancel = () => {
  modalVisible.value = false;
};

const resetForm = () => {
  Object.assign(formState, {
    name: '',
    category: '',
    energyType: '1',
    tags: [],
    ruleConfigs: [
      {
        metric: '',
        operator: 'gt',
        defaultThreshold: 0,
        unit: '',
        defaultLevel: 'medium',
        description: '',
      }
    ],
    applicableScene: '',
    usageGuide: '',
    remark: '',
    status: true,
  });
  formRef.value?.clearValidate();
};

const addConfig = () => {
  formState.ruleConfigs.push({
    metric: '',
    operator: 'gt',
    defaultThreshold: 0,
    unit: '',
    defaultLevel: 'medium',
    description: '',
  });
};

const removeConfig = (index: number) => {
  formState.ruleConfigs.splice(index, 1);
};

const handleCategoryChange = (value: string) => {
  // 根据分类自动设置能源类型
  const categoryEnergyMap: Record<string, string> = {
    'energy_electric': '1',
    'energy_water': '2',
    'energy_gas': '8',
    'energy_air': '5',
    'device_meter': '1',
    'device_production': '1',
    'device_auxiliary': '1',
  };
  formState.energyType = categoryEnergyMap[value] || '1';
};

// 辅助方法
const getCategoryColor = (category: string) => {
  if (category.startsWith('energy_')) return 'green';
  if (category.startsWith('device_')) return 'blue';
  return 'default';
};

const getCategoryText = (category: string) => {
  const categoryMap: Record<string, string> = {
    'energy_electric': '电力监控',
    'energy_water': '水监控',
    'energy_gas': '天然气监控',
    'energy_air': '压缩空气监控',
    'device_meter': '仪表设备',
    'device_production': '生产设备',
    'device_auxiliary': '辅助设备',
  };
  return categoryMap[category] || '未知';
};

const getEnergyTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    '1': '电力',
    '2': '水',
    '8': '天然气',
    '5': '压缩空气',
  };
  return typeMap[type] || '未知';
};

onMounted(() => {
  // 初始化数据
  // TODO: 从后端加载数据
});
</script>

<style scoped>
.template-config-area {
  max-height: 400px;
  overflow-y: auto;
}

.config-item {
  transition: all 0.3s;
}

.config-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.ant-menu-inline) {
  border-right: none;
}

:deep(.ant-divider-horizontal.ant-divider-with-text) {
  margin: 16px 0;
  font-weight: 500;
  color: #1890ff;
}
</style>
