<template>
  <div class="h-full p-4 bg-gray-50">
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
        :loading="loading"
        @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <a-tag :color="getTemplateTypeColor(record.type)">
                {{ getTemplateTypeText(record.type) }}
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
            <template v-if="column.key === 'conditionDesc'">
              <a-tooltip :title="record.conditionDesc">
                <span class="truncate-text">{{ record.conditionDesc }}</span>
              </a-tooltip>
            </template>
            <template v-if="column.key === 'usageCount'">
              <a-button type="link" size="small" @click="handleViewUsage(record)">
                {{ record.usageCount }} 条规则
              </a-button>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="handleView(record)">查看</a>
                <a @click="handleEdit(record)">编辑</a>
                <a @click="handleCreateRule(record)" class="text-green-500">创建规则</a>
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
            placeholder="请输入模板名称，如：车间用电量超限告警模板"
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="模板类型" name="type">
          <a-radio-group v-model:value="formState.type" @change="handleTypeChange">
            <a-radio value="device">设备告警</a-radio>
            <a-radio value="energy">能源告警</a-radio>
          </a-radio-group>
          <div class="text-gray-400 text-xs mt-1">
            设备告警：监控设备运行状态；能源告警：监控能源消耗
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
          label="设备类型"
          name="deviceType"
          v-if="formState.type === 'device'"
        >
          <a-select
            v-model:value="formState.deviceType"
            placeholder="请选择设备类型"
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
          label="监控范围"
          name="targetScope"
          v-if="formState.type === 'energy'"
        >
          <a-select
            v-model:value="formState.targetScope"
            placeholder="请选择监控范围"
          >
            <a-select-option value="department">按部门</a-select-option>
            <a-select-option value="line">按线路</a-select-option>
            <a-select-option value="workshop">按车间</a-select-option>
            <a-select-option value="device">按设备</a-select-option>
          </a-select>
        </a-form-item>

        <a-divider orientation="left">告警条件配置（模板预设条件）</a-divider>

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
                    <a-select-opt-group label="设备运行指标" v-if="formState.type === 'device'">
                      <a-select-option value="device_status">设备状态</a-select-option>
                      <a-select-option value="device_current">运行电流</a-select-option>
                      <a-select-option value="device_voltage">运行电压</a-select-option>
                      <a-select-option value="device_power">运行功率</a-select-option>
                      <a-select-option value="device_temperature">设备温度</a-select-option>
                      <a-select-option value="device_pressure">设备压力</a-select-option>
                    </a-select-opt-group>
                    <a-select-opt-group label="能源消耗指标" v-if="formState.type === 'energy'">
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
                    默认阈值 {{ condition.unit ? `(${condition.unit})` : '' }}
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
                    placeholder="输入默认阈值"
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
                  <span class="text-xs text-gray-500">默认持续时长</span>
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
                  <span class="text-xs text-gray-500">默认检查频率</span>
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

        <a-divider orientation="left">默认告警设置</a-divider>

        <a-form-item label="默认告警级别" name="level">
          <a-select v-model:value="formState.level" placeholder="请选择默认告警级别">
            <a-select-option value="high">
              <a-tag color="red">高</a-tag>
              严重影响生产或安全
            </a-select-option>
            <a-select-option value="medium">
              <a-tag color="orange">中</a-tag>
              影响运行效率
            </a-select-option>
            <a-select-option value="low">
              <a-tag color="blue">低</a-tag>
              提醒关注
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="默认告警方式" name="notifyMethods">
          <a-checkbox-group v-model:value="formState.notifyMethods">
            <a-checkbox value="system">系统通知</a-checkbox>
            <a-checkbox value="email">邮件通知</a-checkbox>
            <a-checkbox value="sms">短信通知</a-checkbox>
            <a-checkbox value="wechat">微信通知</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="默认静默期" name="silencePeriod">
          <a-input-number
            v-model:value="formState.silencePeriod"
            placeholder="避免重复告警"
            :min="0"
            addon-after="分钟"
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="模板说明" name="description">
          <a-textarea
            v-model:value="formState.description"
            :rows="3"
            placeholder="请输入模板说明，描述此模板的适用场景"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看模板使用情况弹窗 -->
    <a-modal
      v-model:open="usageModalVisible"
      title="模板使用情况"
      width="800px"
      :footer="null"
    >
      <a-table
        :columns="usageColumns"
        :data-source="usageList"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status ? 'green' : 'default'">
              {{ record.status ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a @click="goToRule(record)">查看规则</a>
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
import { useRouter } from 'vue-router';
import {
  getTemplateList,
  addTemplate,
  editTemplate,
  deleteTemplate,
  getTemplateUsage
} from '/@/api/energy/alarm';

const router = useRouter();

// 类型定义
interface TemplateCondition {
  metric: string;
  operator: string;
  threshold: number;
  thresholdMax?: number;
  unit: string;
  duration: number;
  checkInterval: number;
}

interface TemplateRecord {
  id: string;
  name: string;
  type: 'device' | 'energy';
  energyType: string;
  deviceType?: string;
  targetScope?: string;
  conditions: TemplateCondition[];
  conditionDesc: string;
  level: 'high' | 'medium' | 'low';
  notifyMethods: string[];
  silencePeriod: number;
  description?: string;
  usageCount: number;
  updater: string;
  updateTime: string;
}

interface FormState {
  name: string;
  type: 'device' | 'energy';
  energyType: string;
  deviceType?: string;
  targetScope?: string;
  conditions: TemplateCondition[];
  level: 'high' | 'medium' | 'low';
  notifyMethods: string[];
  silencePeriod: number;
  description: string;
}

// 搜索相关
const templateName = ref('');
const templateType = ref('all');

// 表格相关
const selectedRowKeys = ref<string[]>([]);
const columns: TableColumnsType = [
  {
    title: '模板名称',
    dataIndex: 'name',
    key: 'name',
    width: 180,
  },
  {
    title: '模板类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
  },
  {
    title: '能源类型',
    dataIndex: 'energyType',
    key: 'energyType',
    width: 100,
  },
  {
    title: '告警条件',
    dataIndex: 'conditionDesc',
    key: 'conditionDesc',
    width: 200,
    ellipsis: true,
  },
  {
    title: '告警级别',
    dataIndex: 'level',
    key: 'level',
    width: 80,
  },
  {
    title: '使用情况',
    dataIndex: 'usageCount',
    key: 'usageCount',
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
    width: 200,
    fixed: 'right',
  },
];

// 模板列表数据
const templateList = ref<TemplateRecord[]>([]);
const loading = ref(false);

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 弹窗相关
const modalVisible = ref(false);
const modalTitle = ref('新增模板');
const formRef = ref();
const formState = reactive<FormState>({
  name: '',
  type: 'device',
  energyType: '1',
  deviceType: '',
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
  silencePeriod: 60,
  description: '',
});

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择告警级别', trigger: 'change' }],
};

// 使用情况弹窗
const usageModalVisible = ref(false);
const usageList = ref<any[]>([]);
const usageColumns: TableColumnsType = [
  { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
  { title: '应用部门', dataIndex: 'department', key: 'department' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 100 },
];

// 方法定义

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true;
  try {
    const params = {
      name: templateName.value || undefined,
      type: templateType.value !== 'all' ? templateType.value : undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await getTemplateList(params);
    if (res) {
      // 处理返回数据
      templateList.value = (res.records || []).map((item: any) => {
        // 解析 conditions JSON
        let conditions = [];
        let conditionDesc = '';
        try {
          conditions = typeof item.conditions === 'string' ? JSON.parse(item.conditions) : item.conditions || [];
          if (conditions.length > 0) {
            const cond = conditions[0];
            conditionDesc = `${getMetricName(cond.metric)} ${getOperatorSymbol(cond.operator)} ${cond.threshold} ${cond.unit || ''}`;
          }
        } catch (e) {
          console.error('解析conditions失败:', e);
        }
        // 解析 notifyMethods JSON
        let notifyMethods = [];
        try {
          notifyMethods = typeof item.notifyMethods === 'string' ? JSON.parse(item.notifyMethods) : item.notifyMethods || [];
        } catch (e) {
          notifyMethods = [];
        }
        return {
          ...item,
          conditions,
          conditionDesc,
          notifyMethods,
          updater: item.updateBy || item.createBy,
        };
      });
      pagination.total = res.total || 0;
    }
  } catch (error) {
    console.error('加载模板列表失败:', error);
    message.error('加载模板列表失败');
  } finally {
    loading.value = false;
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

const handleSearch = () => {
  pagination.current = 1;
  loadTemplateList();
};

const handleReset = () => {
  templateName.value = '';
  templateType.value = 'all';
  pagination.current = 1;
  loadTemplateList();
};

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys;
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadTemplateList();
};

const handleAddDeviceTemplate = () => {
  modalTitle.value = '新增设备告警模板';
  resetForm();
  formState.type = 'device';
  modalVisible.value = true;
};

const handleAddEnergyTemplate = () => {
  modalTitle.value = '新增用电告警模板';
  resetForm();
  formState.type = 'energy';
  formState.targetScope = 'device';
  modalVisible.value = true;
};

const handleView = (record: TemplateRecord) => {
  modalTitle.value = '查看模板';
  Object.assign(formState, {
    ...record,
    conditions: JSON.parse(JSON.stringify(record.conditions))
  });
  modalVisible.value = true;
};

const handleEdit = (record: TemplateRecord) => {
  modalTitle.value = '编辑模板';
  Object.assign(formState, {
    ...record,
    conditions: JSON.parse(JSON.stringify(record.conditions))
  });
  modalVisible.value = true;
};

const handleDelete = async (record: TemplateRecord) => {
  try {
    const res = await deleteTemplate({ id: record.id });
    if (res) {
      message.success('删除成功');
      loadTemplateList();
    }
  } catch (error: any) {
    message.error(error.message || '删除失败');
  }
};

// 基于模板创建规则 - 跳转到规则设置页面
const handleCreateRule = (record: TemplateRecord) => {
  // 将模板数据存储到 sessionStorage，供规则设置页面使用
  sessionStorage.setItem('alarmTemplateData', JSON.stringify(record));
  router.push({
    path: '/EnergyAlarm/Alarm_Rules_Settings',
    query: { templateId: record.id }
  });
  message.info('正在跳转到规则设置页面，将基于模板创建规则');
};

// 查看模板使用情况
const handleViewUsage = async (record: TemplateRecord) => {
  try {
    const res = await getTemplateUsage({ id: record.id });
    if (res) {
      usageList.value = res.rules || [];
      usageModalVisible.value = true;
    }
  } catch (error) {
    console.error('查询模板使用情况失败:', error);
    message.error('查询失败');
  }
};

const goToRule = (record: any) => {
  router.push({
    path: '/EnergyAlarm/Alarm_Rules_Settings',
    query: { ruleId: record.id }
  });
};

const handleModalSubmit = async () => {
  formRef.value?.validate().then(async () => {
    const hasInvalidCondition = formState.conditions.some(
      cond => !cond.metric || !cond.operator || cond.threshold === undefined
    );

    if (hasInvalidCondition) {
      message.error('请完善告警条件配置');
      return;
    }

    if (formState.type === 'device' && !formState.deviceType) {
      message.error('Please select device type for device template');
      return;
    }

    try {
      // 构建提交数据
      const submitData = {
        ...formState,
        deviceType: formState.type === 'energy' ? '' : formState.deviceType,
        targetScope: formState.type === 'energy' ? 'device' : formState.targetScope,
        conditions: JSON.stringify(formState.conditions),
        notifyMethods: JSON.stringify(formState.notifyMethods),
      };

      if (modalTitle.value.includes('编辑') || modalTitle.value.includes('查看')) {
        await editTemplate(submitData);
      } else {
        await addTemplate(submitData);
      }

      message.success('保存成功');
      modalVisible.value = false;
      loadTemplateList();
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
    type: 'device',
    energyType: '1',
    deviceType: '',
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
    silencePeriod: 60,
    description: '',
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

// 模板类型改变
const handleTypeChange = () => {
  formState.conditions = [{
    metric: '',
    operator: 'gt',
    threshold: 0,
    unit: '',
    duration: 5,
    checkInterval: 10,
  }];

  if (formState.type === 'energy') {
    formState.deviceType = '';
    formState.targetScope = 'device';
  } else {
    formState.targetScope = '';
  }
};

// 能源类型改变
const handleEnergyTypeChange = (value: string) => {
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
  const unitMap: Record<string, Record<string, string>> = {
    '1': {
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
    '2': {
      'hour_consumption': 'm³',
      'day_consumption': 'm³',
      'month_consumption': 'm³',
      'consumption_rate': '%',
      'unit_consumption': 'm³/件',
      'cost': '元',
    },
    '8': {
      'hour_consumption': 'm³',
      'day_consumption': 'm³',
      'month_consumption': 'm³',
      'consumption_rate': '%',
      'unit_consumption': 'm³/件',
      'cost': '元',
      'device_pressure': 'MPa',
    },
    '5': {
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

// 辅助方法
const getTemplateTypeColor = (type: string) => {
  return type === 'device' ? 'blue' : 'green';
};

const getTemplateTypeText = (type: string) => {
  return type === 'device' ? '设备告警' : '能源告警';
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

const getLevelColor = (level: string) => {
  const colors = { high: 'red', medium: 'orange', low: 'blue' };
  return colors[level as keyof typeof colors];
};

const getLevelText = (level: string) => {
  const texts = { high: '高', medium: '中', low: '低' };
  return texts[level as keyof typeof texts];
};

// 生命周期钩子
onMounted(() => {
  // 加载模板列表
  loadTemplateList();
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

.truncate-text {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
