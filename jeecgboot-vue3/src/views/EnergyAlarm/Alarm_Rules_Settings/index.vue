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
        :fieldNames="{ title: 'name', key: 'id' }"
        @select="onSelect"
      />
    </div>

    <!-- 右侧内容区 -->
    <div class="flex-1 p-4 bg-gray-50">
      <!-- 顶部搜索和操作区 -->
      <div class="bg-white p-4 rounded-lg mb-4">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center space-x-4">
            <a-input-search
              v-model:value="ruleName"
              placeholder="请输入规则名称"
              style="width: 200px"
            />
            <a-select
              v-model:value="ruleType"
              style="width: 150px"
              placeholder="告警类型"
            >
              <a-select-option value="all">全部类型</a-select-option>
              <a-select-option value="device">设备告警</a-select-option>
              <a-select-option value="energy">能源告警</a-select-option>
            </a-select>
            <a-select
              v-model:value="energyType"
              style="width: 150px"
              placeholder="能源类型"
            >
              <a-select-option value="all">全部能源</a-select-option>
              <a-select-option value="1">电力</a-select-option>
              <a-select-option value="2">水</a-select-option>
              <a-select-option value="8">天然气</a-select-option>
              <a-select-option value="5">压缩空气</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </div>
          <div class="space-x-2">
            <a-button type="primary" @click="handleAddRule">
              新增告警规则
            </a-button>
            <a-button @click="handleAddFromTemplate">
              基于模板创建
            </a-button>
            <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length">
              批量删除
            </a-button>
          </div>
        </div>
      </div>

      <!-- 规则列表 -->
      <div class="bg-white rounded-lg">
        <a-table
          :columns="columns"
          :data-source="ruleList"
          :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
          :pagination="pagination"
          :loading="loading"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'ruleType'">
              <a-tag :color="getRuleTypeColor(record.ruleType)">
                {{ getRuleTypeText(record.ruleType) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'energyType'">
              <a-tag color="blue">{{ getEnergyTypeText(record.energyType) }}</a-tag>
            </template>
            <template v-if="column.key === 'level'">
              <a-tag :color="getLevelColor(record.level)">
                {{ getLevelText(record.level) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'templateName'">
              <span v-if="record.templateName" class="text-blue-500">
                {{ record.templateName }}
              </span>
              <span v-else class="text-gray-400">自定义</span>
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
                <a-popconfirm
                  title="确定要删除此规则吗？"
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

    <!-- 新增/编辑规则弹窗 -->
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
        
        <a-form-item label="规则名称" name="name">
          <a-input 
            v-model:value="formState.name" 
            placeholder="请输入规则名称，如：车间用电量超限告警" 
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="告警类型" name="ruleType">
          <a-radio-group v-model:value="formState.ruleType" @change="handleRuleTypeChange">
            <a-radio value="device">设备告警</a-radio>
            <a-radio value="energy">能源告警</a-radio>
          </a-radio-group>
          <div class="text-gray-400 text-xs mt-1">
            设备告警：监控设备运行状态（如故障、超负荷）；能源告警：监控能源消耗（如超限、异常）
          </div>
        </a-form-item>

        <a-form-item label="能源类型" name="energyType">
          <a-select 
            v-model:value="formState.energyType" 
            placeholder="请选择能源类型"
            @change="handleEnergyTypeChange"
          >
            <a-select-option value="1">电力</a-select-option>
            <a-select-option value="2">水</a-select-option>
            <a-select-option value="8">天然气</a-select-option>
            <a-select-option value="5">压缩空气</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item 
          label="监控对象类型" 
          name="targetType"
          v-if="formState.ruleType === 'device'"
        >
          <a-select 
            v-model:value="formState.targetType" 
            placeholder="请选择监控对象类型"
          >
            <a-select-option value="GFMT">仪表类/GFMT</a-select-option>
            <a-select-option value="CEC">虚拟类/CEC</a-select-option>
            <a-select-option value="ACOP">生产辅助设备/ACOP</a-select-option>
            <a-select-option value="WMCT">供冷热辅机设备/WMCT</a-select-option>
            <a-select-option value="METE">仪表类/METE</a-select-option>
            <a-select-option value="ELEV">通用辅助设备/ELEV</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item 
          label="监控对象范围" 
          name="targetScope"
          v-if="formState.ruleType === 'energy'"
        >
          <a-select 
            v-model:value="formState.targetScope" 
            placeholder="请选择监控对象范围"
          >
            <a-select-option value="department">按部门</a-select-option>
            <a-select-option value="line">按线路</a-select-option>
            <a-select-option value="workshop">按车间</a-select-option>
            <a-select-option value="device">按设备</a-select-option>
          </a-select>
        </a-form-item>

        <a-divider orientation="left">告警条件配置（可添加多个条件，满足任一即告警）</a-divider>
        
        <div class="condition-config-area bg-gray-50 p-4 rounded mb-4">
          <div 
            v-for="(condition, index) in formState.conditions" 
            :key="index"
            class="condition-item bg-white p-3 mb-3 rounded border"
          >
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium">条件 {{ index + 1 }}</span>
              <a-button 
                type="link" 
                danger 
                size="small"
                @click="removeCondition(index)"
                v-if="formState.conditions.length > 1"
              >
                删除
              </a-button>
            </div>
            
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item 
                  :label-col="{ span: 0 }" 
                  :wrapper-col="{ span: 24 }"
                  class="mb-2"
                >
                  <span class="text-xs text-gray-500">监控指标</span>
                  <a-select 
                    v-model:value="condition.metric" 
                    placeholder="选择监控指标"
                    @change="handleMetricChange(index)"
                  >
                    <a-select-opt-group label="设备运行指标" v-if="formState.ruleType === 'device'">
                      <a-select-option value="device_status">设备状态</a-select-option>
                      <a-select-option value="device_current">运行电流</a-select-option>
                      <a-select-option value="device_voltage">运行电压</a-select-option>
                      <a-select-option value="device_power">运行功率</a-select-option>
                      <a-select-option value="device_temperature">设备温度</a-select-option>
                      <a-select-option value="device_pressure">设备压力</a-select-option>
                    </a-select-opt-group>
                    <a-select-opt-group label="能源消耗指标" v-if="formState.ruleType === 'energy'">
                      <a-select-option value="hour_consumption">小时用量</a-select-option>
                      <a-select-option value="day_consumption">日用量</a-select-option>
                      <a-select-option value="month_consumption">月用量</a-select-option>
                      <a-select-option value="consumption_rate">用量变化率</a-select-option>
                      <a-select-option value="unit_consumption">单位产品能耗</a-select-option>
                      <a-select-option value="cost">能源费用</a-select-option>
                    </a-select-opt-group>
                  </a-select>
                </a-form-item>
              </a-col>
              
              <a-col :span="6">
                <a-form-item 
                  :label-col="{ span: 0 }" 
                  :wrapper-col="{ span: 24 }"
                  class="mb-2"
                >
                  <span class="text-xs text-gray-500">比较方式</span>
                  <a-select 
                    v-model:value="condition.operator" 
                    placeholder="选择比较方式"
                  >
                    <a-select-option value="gt">大于 (&gt;)</a-select-option>
                    <a-select-option value="gte">大于等于 (&gt;=)</a-select-option>
                    <a-select-option value="lt">小于 (&lt;)</a-select-option>
                    <a-select-option value="lte">小于等于 (&lt;=)</a-select-option>
                    <a-select-option value="eq">等于 (=)</a-select-option>
                    <a-select-option value="ne">不等于 (!=)</a-select-option>
                    <a-select-option value="between">区间内</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              
              <a-col :span="10">
                <a-form-item 
                  :label-col="{ span: 0 }" 
                  :wrapper-col="{ span: 24 }"
                  class="mb-2"
                >
                  <span class="text-xs text-gray-500">
                    阈值 {{ condition.unit ? `(${condition.unit})` : '' }}
                  </span>
                  <div v-if="condition.operator === 'between'" class="flex space-x-2">
                    <a-input-number 
                      v-model:value="condition.threshold" 
                      placeholder="最小值"
                      :precision="2"
                      style="width: 50%"
                    />
                    <a-input-number 
                      v-model:value="condition.thresholdMax" 
                      placeholder="最大值"
                      :precision="2"
                      style="width: 50%"
                    />
                  </div>
                  <a-input-number 
                    v-else
                    v-model:value="condition.threshold" 
                    placeholder="输入阈值"
                    :precision="2"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item 
                  :label-col="{ span: 0 }" 
                  :wrapper-col="{ span: 24 }"
                  class="mb-0"
                >
                  <span class="text-xs text-gray-500">持续时长（触发条件需持续的时间）</span>
                  <a-input-number 
                    v-model:value="condition.duration" 
                    placeholder="如：5"
                    :min="1"
                    addon-after="分钟"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item 
                  :label-col="{ span: 0 }" 
                  :wrapper-col="{ span: 24 }"
                  class="mb-0"
                >
                  <span class="text-xs text-gray-500">检查频率（多久检查一次）</span>
                  <a-input-number 
                    v-model:value="condition.checkInterval" 
                    placeholder="如：10"
                    :min="1"
                    addon-after="分钟"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <a-button 
            type="dashed" 
            block 
            @click="addCondition"
            class="mt-2"
          >
            <template #icon><PlusOutlined /></template>
            添加告警条件
          </a-button>
        </div>

        <a-divider orientation="left">告警设置</a-divider>

        <a-form-item label="告警级别" name="level">
          <a-select v-model:value="formState.level" placeholder="请选择告警级别">
            <a-select-option value="high">
              <a-tag color="red">高</a-tag>
              严重影响生产或安全，需立即处理
            </a-select-option>
            <a-select-option value="medium">
              <a-tag color="orange">中</a-tag>
              影响运行效率，需尽快处理
            </a-select-option>
            <a-select-option value="low">
              <a-tag color="blue">低</a-tag>
              提醒关注，可稍后处理
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="告警方式" name="notifyMethods">
          <a-checkbox-group v-model:value="formState.notifyMethods">
            <a-checkbox value="system">系统通知</a-checkbox>
            <a-checkbox value="email">邮件通知</a-checkbox>
            <a-checkbox value="sms">短信通知</a-checkbox>
            <a-checkbox value="wechat">微信通知</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="通知人员" name="notifyUsers">
          <a-select
            v-model:value="formState.notifyUsers"
            mode="multiple"
            placeholder="请选择需要通知的人员"
            :options="userOptions"
          />
        </a-form-item>

        <a-form-item label="静默期" name="silencePeriod">
          <a-input-number 
            v-model:value="formState.silencePeriod" 
            placeholder="避免重复告警"
            :min="0"
            addon-after="分钟"
            style="width: 200px"
          />
          <div class="text-gray-400 text-xs mt-1">
            在此时间内不会重复发送相同的告警信息，0表示不设置静默期
          </div>
        </a-form-item>

        <a-form-item label="备注说明" name="remark">
          <a-textarea
            v-model:value="formState.remark"
            :rows="3"
            placeholder="请输入备注说明，如规则用途、特殊注意事项等"
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
            禁用后规则不会生效，不会产生告警
          </span>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 选择模板弹窗 -->
    <a-modal
      v-model:open="templateModalVisible"
      title="选择告警模板"
      width="900px"
      @ok="handleTemplateSelect"
      @cancel="templateModalVisible = false"
    >
      <div class="mb-4">
        <a-alert
          message="选择一个模板后，将自动填充模板中的预设配置，您可以在此基础上进行修改"
          type="info"
          show-icon
        />
      </div>
      <a-table
        :columns="templateColumns"
        :data-source="availableTemplates"
        :row-selection="{
          type: 'radio',
          selectedRowKeys: selectedTemplateKeys,
          onChange: onTemplateSelectChange
        }"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.type === 'device' ? 'blue' : 'green'">
              {{ record.type === 'device' ? '设备告警' : '能源告警' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'energyType'">
            <a-tag color="blue">{{ getEnergyTypeText(record.energyType) }}</a-tag>
          </template>
          <template v-if="column.key === 'level'">
            <a-tag :color="getLevelColor(record.level)">
              {{ getLevelText(record.level) }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import {
  getRuleList,
  addRule,
  editRule,
  deleteRule,
  deleteRuleBatch,
  changeRuleStatus,
  getTemplateOptions
} from '/@/api/energy/alarm';

const route = useRoute();

// 类型定义
interface TreeNode {
  id: string;
  name: string;
  children?: TreeNode[];
}

interface RuleCondition {
  metric: string;          // 监控指标
  operator: string;        // 比较运算符
  threshold: number;       // 阈值
  thresholdMax?: number;   // 最大阈值（用于区间）
  unit: string;            // 单位
  duration: number;        // 持续时长（分钟）
  checkInterval: number;   // 检查频率（分钟）
}

interface RuleRecord {
  id: string;
  name: string;
  ruleType: 'device' | 'energy';  // 告警类型
  energyType: string;              // 能源类型：1-电,2-水,8-天然气,5-压缩空气
  targetType?: string;             // 设备类型（设备告警用）
  targetScope?: string;            // 监控范围（能源告警用）
  conditions: RuleCondition[];     // 告警条件数组
  level: 'high' | 'medium' | 'low';
  notifyMethods: string[];         // 通知方式
  notifyUsers: string[];           // 通知人员
  silencePeriod: number;           // 静默期（分钟）
  remark?: string;
  status: boolean;
  templateId?: string;             // 关联的模板ID
  templateName?: string;           // 关联的模板名称
  updater: string;
  updateTime: string;
}

interface FormState {
  name: string;
  ruleType: 'device' | 'energy';
  energyType: string;
  targetType?: string;
  targetScope?: string;
  conditions: RuleCondition[];
  level: 'high' | 'medium' | 'low';
  notifyMethods: string[];
  notifyUsers: string[];
  silencePeriod: number;
  remark: string;
  status: boolean;
  templateId?: string; // 关联的模板ID
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

// 搜索相关
const ruleName = ref('');
const ruleType = ref('all');
const energyType = ref('all');

// 表格相关
const selectedRowKeys = ref<string[]>([]);
const columns: TableColumnsType = [
  {
    title: '规则名称',
    dataIndex: 'name',
    key: 'name',
    width: 180,
  },
  {
    title: '告警类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 100,
  },
  {
    title: '能源类型',
    dataIndex: 'energyType',
    key: 'energyType',
    width: 100,
  },
  {
    title: '监控指标',
    dataIndex: 'metricDesc',
    key: 'metricDesc',
    width: 180,
  },
  {
    title: '告警级别',
    dataIndex: 'level',
    key: 'level',
    width: 80,
  },
  {
    title: '模板来源',
    dataIndex: 'templateName',
    key: 'templateName',
    width: 150,
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
    width: 100,
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
    width: 180,
    fixed: 'right',
  },
];

// 规则列表数据
const ruleList = ref<RuleRecord[]>([]);
const loading = ref(false);

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 模板选择相关
const templateModalVisible = ref(false);
const selectedTemplateKeys = ref<string[]>([]);
const templateColumns: TableColumnsType = [
  { title: '模板名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '模板类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '能源类型', dataIndex: 'energyType', key: 'energyType', width: 100 },
  { title: '告警条件', dataIndex: 'conditionDesc', key: 'conditionDesc', width: 200 },
  { title: '告警级别', dataIndex: 'level', key: 'level', width: 80 },
  { title: '说明', dataIndex: 'description', key: 'description', ellipsis: true },
];

// 可用模板列表（从后端获取）
const availableTemplates = ref<any[]>([]);

// 用户选项（示例数据，实际应从后端获取）
const userOptions = ref([
  { label: '张三', value: 'user1' },
  { label: '李四', value: 'user2' },
  { label: '王五', value: 'user3' },
]);

// 弹窗相关
const modalVisible = ref(false);
const modalTitle = ref('新增规则');
const formRef = ref();
const formState = reactive<FormState>({
  name: '',
  ruleType: 'device',
  energyType: '1',
  targetType: '',
  targetScope: '',
  conditions: [
    {
      metric: '',
      operator: 'gt',
      threshold: 0,
      unit: '',
      duration: 5,
      checkInterval: 10,
    }
  ],
  level: 'medium',
  notifyMethods: ['system'],
  notifyUsers: [],
  silencePeriod: 60,
  remark: '',
  status: true,
});

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择告警类型', trigger: 'change' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择告警级别', trigger: 'change' }],
  notifyMethods: [{ required: true, type: 'array', min: 1, message: '请至少选择一种告警方式', trigger: 'change' }],
};

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
  pagination.current = 1;
  loadRuleList();
};

const handleReset = () => {
  ruleName.value = '';
  ruleType.value = 'all';
  energyType.value = 'all';
  pagination.current = 1;
  loadRuleList();
};

// 加载规则列表
const loadRuleList = async () => {
  loading.value = true;
  try {
    const params = {
      name: ruleName.value || undefined,
      ruleType: ruleType.value !== 'all' ? ruleType.value : undefined,
      energyType: energyType.value !== 'all' ? energyType.value : undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await getRuleList(params);
    if (res) {
      ruleList.value = (res.records || []).map((item: any) => {
        // 解析 conditions JSON
        let conditions = [];
        let metricDesc = '';
        try {
          conditions = typeof item.conditions === 'string' ? JSON.parse(item.conditions) : item.conditions || [];
          if (conditions.length > 0) {
            const cond = conditions[0];
            metricDesc = `${getMetricName(cond.metric)} ${getOperatorSymbol(cond.operator)} ${cond.threshold} ${cond.unit || ''}`;
          }
        } catch (e) {
          console.error('解析conditions失败:', e);
        }
        // 解析 notifyMethods 和 notifyUsers JSON
        let notifyMethods = [];
        let notifyUsers = [];
        try {
          notifyMethods = typeof item.notifyMethods === 'string' ? JSON.parse(item.notifyMethods) : item.notifyMethods || [];
          notifyUsers = typeof item.notifyUsers === 'string' ? JSON.parse(item.notifyUsers) : item.notifyUsers || [];
        } catch (e) {
          // ignore
        }
        return {
          ...item,
          conditions,
          metricDesc,
          notifyMethods,
          notifyUsers,
          status: item.status === 1 || item.status === true,
          updater: item.updateBy || item.createBy,
        };
      });
      pagination.total = res.total || 0;
    }
  } catch (error) {
    console.error('加载规则列表失败:', error);
    message.error('加载规则列表失败');
  } finally {
    loading.value = false;
  }
};

// 加载模板选项
const loadTemplateOptions = async () => {
  try {
    const res = await getTemplateOptions();
    if (res) {
      availableTemplates.value = (res || []).map((item: any) => {
        let conditions = [];
        let conditionDesc = '';
        try {
          conditions = typeof item.conditions === 'string' ? JSON.parse(item.conditions) : item.conditions || [];
          if (conditions.length > 0) {
            const cond = conditions[0];
            conditionDesc = `${getMetricName(cond.metric)} ${getOperatorSymbol(cond.operator)} ${cond.threshold} ${cond.unit || ''}`;
          }
        } catch (e) {
          // ignore
        }
        let notifyMethods = [];
        try {
          notifyMethods = typeof item.notifyMethods === 'string' ? JSON.parse(item.notifyMethods) : item.notifyMethods || [];
        } catch (e) {
          // ignore
        }
        return {
          ...item,
          conditions,
          conditionDesc,
          notifyMethods,
        };
      });
    }
  } catch (error) {
    console.error('加载模板选项失败:', error);
  }
};

// 获取指标名称
const getMetricName = (metric: string): string => {
  const metricMap: Record<string, string> = {
    'device_status': '设备状态',
    'device_current': '运行电流',
    'device_voltage': '运行电压',
    'device_power': '运行功率',
    'device_temperature': '设备温度',
    'device_pressure': '设备压力',
    'hour_consumption': '小时用量',
    'day_consumption': '日用量',
    'month_consumption': '月用量',
    'consumption_rate': '用量变化率',
    'unit_consumption': '单位产品能耗',
    'cost': '能源费用',
  };
  return metricMap[metric] || metric;
};

// 获取运算符符号
const getOperatorSymbol = (operator: string): string => {
  const operatorMap: Record<string, string> = {
    'gt': '>',
    'gte': '>=',
    'lt': '<',
    'lte': '<=',
    'eq': '=',
    'ne': '!=',
    'between': '区间',
  };
  return operatorMap[operator] || operator;
};

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadRuleList();
};

const handleAddRule = () => {
  modalTitle.value = '新增告警规则';
  resetForm();
  modalVisible.value = true;
};

// 基于模板创建规则
const handleAddFromTemplate = () => {
  selectedTemplateKeys.value = [];
  templateModalVisible.value = true;
};

// 模板选择变化
const onTemplateSelectChange = (keys: string[]) => {
  selectedTemplateKeys.value = keys;
};

// 确认选择模板
const handleTemplateSelect = () => {
  if (selectedTemplateKeys.value.length === 0) {
    message.warning('请选择一个模板');
    return;
  }

  const selectedTemplate = availableTemplates.value.find(
    t => t.id === selectedTemplateKeys.value[0]
  );

  if (selectedTemplate) {
    applyTemplate(selectedTemplate);
    templateModalVisible.value = false;
    modalTitle.value = '基于模板创建规则';
    modalVisible.value = true;
    message.success('已加载模板配置，请根据需要修改后保存');
  }
};

// 应用模板到表单
const applyTemplate = (template: any) => {
  resetForm();
  Object.assign(formState, {
    name: '', // 规则名称需要用户自己填写
    ruleType: template.type,
    energyType: template.energyType,
    targetType: template.deviceType || '',
    targetScope: template.targetScope || '',
    conditions: JSON.parse(JSON.stringify(template.conditions)),
    level: template.level,
    notifyMethods: [...template.notifyMethods],
    silencePeriod: template.silencePeriod,
    remark: `基于模板"${template.name}"创建`,
    status: true,
    templateId: template.id, // 记录模板ID
  });
};

const handleView = (record: RuleRecord) => {
  console.log('view rule:', record);
  // TODO: 显示规则详情（只读模式）
};

const handleEdit = (record: RuleRecord) => {
  modalTitle.value = '编辑规则';
  Object.assign(formState, {
    ...record,
    conditions: JSON.parse(JSON.stringify(record.conditions)) // 深拷贝
  });
  modalVisible.value = true;
};

const handleDelete = async (record: RuleRecord) => {
  try {
    await deleteRule({ id: record.id });
    message.success('删除成功');
    loadRuleList();
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }
  try {
    await deleteRuleBatch({ ids: selectedRowKeys.value.join(',') });
    message.success(`已删除 ${selectedRowKeys.value.length} 条规则`);
    selectedRowKeys.value = [];
    loadRuleList();
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

const handleStatusChange = async (record: RuleRecord, checked: boolean) => {
  try {
    await changeRuleStatus({ id: record.id, status: checked ? 1 : 0 });
    message.success(checked ? '规则已启用' : '规则已禁用');
    record.status = checked;
  } catch (error: any) {
    message.error(error.message || '操作失败');
  }
};

const handleModalSubmit = async () => {
  formRef.value?.validate().then(async () => {
    // 验证条件配置
    const hasInvalidCondition = formState.conditions.some(
      cond => !cond.metric || !cond.operator || cond.threshold === undefined
    );

    if (hasInvalidCondition) {
      message.error('请完善告警条件配置');
      return;
    }

    try {
      // 构建提交数据
      const submitData = {
        ...formState,
        conditions: JSON.stringify(formState.conditions),
        notifyMethods: JSON.stringify(formState.notifyMethods),
        notifyUsers: JSON.stringify(formState.notifyUsers),
        status: formState.status ? 1 : 0,
      };

      if (modalTitle.value.includes('编辑')) {
        await editRule(submitData);
      } else {
        await addRule(submitData);
      }

      message.success('保存成功');
      modalVisible.value = false;
      loadRuleList();
    } catch (error: any) {
      message.error(error.message || '保存失败');
    }
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
    ruleType: 'device',
    energyType: '1',
    targetType: '',
    targetScope: '',
    conditions: [
      {
        metric: '',
        operator: 'gt',
        threshold: 0,
        unit: '',
        duration: 5,
        checkInterval: 10,
      }
    ],
    level: 'medium',
    notifyMethods: ['system'],
    notifyUsers: [],
    silencePeriod: 60,
    remark: '',
    status: true,
  });
  formRef.value?.clearValidate();
};

// 添加条件
const addCondition = () => {
  formState.conditions.push({
    metric: '',
    operator: 'gt',
    threshold: 0,
    unit: '',
    duration: 5,
    checkInterval: 10,
  });
};

// 删除条件
const removeCondition = (index: number) => {
  formState.conditions.splice(index, 1);
};

// 告警类型改变
const handleRuleTypeChange = () => {
  // 清空条件，重新配置
  formState.conditions = [{
    metric: '',
    operator: 'gt',
    threshold: 0,
    unit: '',
    duration: 5,
    checkInterval: 10,
  }];
};

// 能源类型改变
const handleEnergyTypeChange = (value: string) => {
  // 根据能源类型更新默认单位
  formState.conditions.forEach(cond => {
    if (!cond.metric) return;
    cond.unit = getDefaultUnit(value, cond.metric);
  });
};

// 监控指标改变
const handleMetricChange = (index: number) => {
  const condition = formState.conditions[index];
  condition.unit = getDefaultUnit(formState.energyType, condition.metric);
};

// 获取默认单位
const getDefaultUnit = (energyType: string, metric: string): string => {
  // 根据能源类型和指标返回默认单位
  const unitMap: Record<string, Record<string, string>> = {
    '1': {  // 电力
      'hour_consumption': 'kWh',
      'day_consumption': 'kWh',
      'month_consumption': 'kWh',
      'consumption_rate': '%',
      'unit_consumption': 'kWh/件',
      'cost': '元',
      'device_current': 'A',
      'device_voltage': 'V',
      'device_power': 'kW',
    },
    '2': {  // 水
      'hour_consumption': 'm³',
      'day_consumption': 'm³',
      'month_consumption': 'm³',
      'consumption_rate': '%',
      'unit_consumption': 'm³/件',
      'cost': '元',
    },
    '8': {  // 天然气
      'hour_consumption': 'm³',
      'day_consumption': 'm³',
      'month_consumption': 'm³',
      'consumption_rate': '%',
      'unit_consumption': 'm³/件',
      'cost': '元',
      'device_pressure': 'MPa',
    },
    '5': {  // 压缩空气
      'hour_consumption': 'm³',
      'day_consumption': 'm³',
      'month_consumption': 'm³',
      'consumption_rate': '%',
      'unit_consumption': 'm³/件',
      'cost': '元',
      'device_pressure': 'MPa',
    },
  };
  
  return unitMap[energyType]?.[metric] || '';
};

// 辅助方法：获取规则类型颜色
const getRuleTypeColor = (type: string) => {
  return type === 'device' ? 'blue' : 'green';
};

// 辅助方法：获取规则类型文本
const getRuleTypeText = (type: string) => {
  return type === 'device' ? '设备告警' : '能源告警';
};

// 辅助方法：获取能源类型文本
const getEnergyTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    '1': '电力',
    '2': '水',
    '8': '天然气',
    '5': '压缩空气',
  };
  return typeMap[type] || '未知';
};

// 辅助方法：获取告警级别颜色
const getLevelColor = (level: string) => {
  const colors = {
    high: 'red',
    medium: 'orange',
    low: 'blue',
  };
  return colors[level as keyof typeof colors];
};

// 辅助方法：获取告警级别文本
const getLevelText = (level: string) => {
  const texts = {
    high: '高',
    medium: '中',
    low: '低',
  };
  return texts[level as keyof typeof texts];
};

// 生命周期钩子
onMounted(() => {
  // 加载规则列表
  loadRuleList();
  // 加载模板选项
  loadTemplateOptions();

  // 检查是否从模板页面跳转过来
  const templateId = route.query.templateId as string;
  if (templateId) {
    // 从 sessionStorage 获取模板数据
    const templateDataStr = sessionStorage.getItem('alarmTemplateData');
    if (templateDataStr) {
      try {
        const templateData = JSON.parse(templateDataStr);
        // 解析 conditions
        if (typeof templateData.conditions === 'string') {
          templateData.conditions = JSON.parse(templateData.conditions);
        }
        if (typeof templateData.notifyMethods === 'string') {
          templateData.notifyMethods = JSON.parse(templateData.notifyMethods);
        }
        // 应用模板
        applyTemplate(templateData);
        modalTitle.value = '基于模板创建规则';
        modalVisible.value = true;
        message.info('已加载模板配置，请填写规则名称并根据需要修改后保存');
        // 清除 sessionStorage
        sessionStorage.removeItem('alarmTemplateData');
      } catch (e) {
        console.error('解析模板数据失败:', e);
      }
    }
  }
});
</script>

<style scoped>
.condition-config-area {
  max-height: 400px;
  overflow-y: auto;
}

.condition-item {
  transition: all 0.3s;
}

.condition-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-divider-horizontal.ant-divider-with-text) {
  margin: 16px 0;
  font-weight: 500;
  color: #1890ff;
}
</style>