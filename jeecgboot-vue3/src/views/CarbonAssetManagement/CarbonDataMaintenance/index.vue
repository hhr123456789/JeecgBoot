<template>
  <div class="carbon-data-maintenance p-4">
    <!-- KPI 概览 -->
    <div class="kpi-grid mb-4">
      <a-card class="kpi-card">
        <div class="kpi-label">范围一排放 · 本月累计 (tCO₂e)</div>
        <div class="kpi-value">{{ kpiData.scope1Total }}</div>
        <div class="kpi-sub">
          化石燃料 + 工业过程
          <a-tag class="kpi-chip" :color="kpiData.scope1Change >= 0 ? 'green' : 'red'">
            {{ kpiData.scope1Change > 0 ? '+' : '' }}{{ kpiData.scope1Change }}%
          </a-tag>
        </div>
      </a-card>
      
      <a-card class="kpi-card">
        <div class="kpi-label">范围二排放 · 本月 (tCO₂e)</div>
        <div class="kpi-value">{{ kpiData.scope2Total }}</div>
        <div class="kpi-sub">
          购电 / 购蒸汽 / 购热
          <a-tag class="kpi-chip" :color="kpiData.scope2Change >= 0 ? 'green' : 'red'">
            {{ kpiData.scope2Change > 0 ? '+' : '' }}{{ kpiData.scope2Change }}%
          </a-tag>
        </div>
      </a-card>
      
      <a-card class="kpi-card">
        <div class="kpi-label">范围三排放 · 本月 (tCO₂e)</div>
        <div class="kpi-value">{{ kpiData.scope3Total }}</div>
        <div class="kpi-sub">
          供应链 / 物流 / 废弃物
          <a-tag class="kpi-chip" :color="kpiData.scope3Change >= 0 ? 'green' : 'red'">
            {{ kpiData.scope3Change > 0 ? '+' : '' }}{{ kpiData.scope3Change }}%
          </a-tag>
        </div>
      </a-card>
      
      <a-card class="kpi-card">
        <div class="kpi-label">排放因子维护完成度</div>
        <div class="kpi-value">{{ kpiData.factorRate }}</div>
        <div class="kpi-sub">本月更新 {{ kpiData.updatedCount }} 项，待校验 {{ kpiData.pendingCount }} 项</div>
      </a-card>
    </div>

    <!-- 车间范围排放数据维护 -->
    <a-card title="车间范围排放数据维护" class="mb-4">
      <div class="toolbar">
        <a-select v-model:value="filters.scope" style="width: 130px" @change="handleFilter">
          <a-select-option value="all">全部范围</a-select-option>
          <a-select-option value="scope1">范围一</a-select-option>
          <a-select-option value="scope2">范围二</a-select-option>
          <a-select-option value="scope3">范围三</a-select-option>
        </a-select>
        
        <a-select v-model:value="filters.workshop" style="width: 130px" @change="handleFilter">
          <a-select-option value="all">全部车间</a-select-option>
          <a-select-option value="电解车间">电解车间</a-select-option>
          <a-select-option value="铸造车间">铸造车间</a-select-option>
          <a-select-option value="轧制车间">轧制车间</a-select-option>
          <a-select-option value="动力站">动力站</a-select-option>
          <a-select-option value="精加工车间">精加工车间</a-select-option>
        </a-select>
        
        <a-date-picker v-model:value="filters.month" picker="month" style="width: 130px" />
        
        <a-button type="primary" @click="handleBatchImport">批量导入</a-button>
        <a-button @click="handleExportTemplate">导出模板</a-button>
      </div>
      
      <a-table
        :dataSource="filteredSummaryData"
        :columns="summaryColumns"
        :pagination="false"
        rowKey="workshop"
        :scroll="{ x: 'max-content' }"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'edit'">
            <a-button size="small" type="primary" @click="openEditDialog(record.workshop)">
              编辑
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详细数据录入 -->
    <a-row :gutter="16">
      <a-col :span="14">
        <div class="section-title">范围一 · 数据条目</div>
        
        <!-- 化石燃料燃烧排放 -->
        <a-card class="sub-card mb-3">
          <template #title>化石燃料燃烧排放</template>
          <template #extra>
            <small>记录各车间锅炉、炉窑、动力设备的燃料消耗、排放因子与计算结果。</small>
          </template>
          
          <a-table
            :dataSource="fossilData"
            :columns="fossilColumns"
            :pagination="false"
            size="small"
            rowKey="id"
            :scroll="{ x: 'max-content' }"
          />
          
          <div class="action-row">
            <a-button size="small" @click="copyLastMonthData">复制上月数据</a-button>
            <a-button size="small" type="primary" @click="addFossilEntry">新增燃料条目</a-button>
          </div>
        </a-card>

        <!-- 工业生产过程排放 -->
        <a-card class="sub-card">
          <template #title>工业生产过程排放</template>
          <template #extra>
            <small>涵盖石灰煅烧、电解质分解、溶剂挥发等过程排放。</small>
          </template>
          
          <a-table
            :dataSource="processData"
            :columns="processColumns"
            :pagination="false"
            size="small"
            rowKey="id"
            :scroll="{ x: 'max-content' }"
          />
          
          <div class="action-row">
            <a-button size="small" @click="importDetectionReport">导入检测报告</a-button>
            <a-button size="small" type="primary" @click="addProcessEntry">新增过程条目</a-button>
          </div>
        </a-card>
      </a-col>

      <a-col :span="10">
        <!-- 范围二 -->
        <div class="section-title">范围二 · 购电 / 购热</div>
        <a-card class="sub-card mb-3">
          <template #title>购电 / 购蒸汽录入</template>
          
          <a-table
            :dataSource="scope2Data"
            :columns="scope2Columns"
            :pagination="false"
            size="small"
            rowKey="id"
            :scroll="{ x: 'max-content' }"
          />
          
          <div class="action-row">
            <a-button size="small" @click="syncEnergyPlatform">同步能管平台</a-button>
            <a-button size="small" type="primary" @click="addScope2Record">新增购能记录</a-button>
          </div>
        </a-card>

        <!-- 范围三 -->
        <div class="section-title">范围三 · 供应链与物流</div>
        <a-card class="sub-card">
          <template #title>供应链与物流</template>
          <template #extra>
            <small>暂无直接计量数据？可录入业务量与排放因子进行估算。</small>
          </template>
          
          <a-table
            :dataSource="scope3Data"
            :columns="scope3Columns"
            :pagination="false"
            size="small"
            rowKey="id"
            :scroll="{ x: 'max-content' }"
          />
          
          <div class="action-row">
            <a-button size="small" @click="importSupplierList">导入供应商清单</a-button>
            <a-button size="small" type="primary" @click="addScope3Project">新增范围三项目</a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 排放因子维护 -->
    <a-card title="排放因子维护" class="mt-4">
      <a-table
        :dataSource="factorData"
        :columns="factorColumns"
        :pagination="false"
        rowKey="project"
        size="small"
        :scroll="{ x: 'max-content' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'scope'">
            <a-tag :class="getScopeTagClass(record.scope)">{{ record.scope }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'operation'">
            <a-button size="small" type="primary" @click="editFactor(record.project)">
              调整
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { Card, Row, Col, Tag, Button, Table, Select, DatePicker, message } from 'ant-design-vue';
import type { TableColumnsType } from 'ant-design-vue';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const ATag = Tag;
const AButton = Button;
const ATable = Table;
const ASelect = Select;
const ASelectOption = Select.Option;
const ADatePicker = DatePicker;

// 筛选器
const filters = ref({
  scope: 'all',
  workshop: 'all',
  month: null
});

// KPI 数据
const kpiData = ref({
  scope1Total: '788.7',
  scope2Total: '448.5', 
  scope3Total: '160.4',
  factorRate: '86%',
  updatedCount: 6,
  pendingCount: 2,
  scope1Change: -3.1,
  scope2Change: 1.2,
  scope3Change: -0.6
});

// 车间汇总数据
const summaryData = ref([
  { workshop: '电解车间', scope1: 186.4, scope2: 128.2, scope3: 52.1 },
  { workshop: '铸造车间', scope1: 162.3, scope2: 95.6, scope3: 33.5 },
  { workshop: '轧制车间', scope1: 142.7, scope2: 88.1, scope3: 40.3 },
  { workshop: '动力站', scope1: 201.9, scope2: 64.2, scope3: 18.7 },
  { workshop: '精加工车间', scope1: 95.4, scope2: 72.6, scope3: 15.8 }
]);

// 过滤后的数据
const filteredSummaryData = computed(() => {
  let data = summaryData.value;
  
  if (filters.value.workshop !== 'all') {
    data = data.filter(item => item.workshop === filters.value.workshop);
  }
  
  return data;
});

// 化石燃料数据
const fossilData = ref([
  { id: 1, workshop: '动力站锅炉#1', fuel: '天然气', usage: '185,000 Nm³', factor: 2.162, emission: 86.3 },
  { id: 2, workshop: '铸造熔炉', fuel: '焦炉煤气', usage: '95,000 Nm³', factor: 2.57, emission: 44.5 },
  { id: 3, workshop: '电解备用锅炉', fuel: '柴油', usage: '18,000 L', factor: 2.68, emission: 48.2 }
]);

// 工业过程数据
const processData = ref([
  { id: 1, workshop: '石灰窑', activity: '2,150 t 生石灰', factor: '0.785 tCO₂/t', recycle: '碳酸钙回收 4.5%', emission: 68.4 },
  { id: 2, workshop: '阳极烘烤', activity: '980 t 阳极砖', factor: '0.235 tCO₂/t', recycle: '焦粉回收 1.2%', emission: 22.5 },
  { id: 3, workshop: '电解过程', activity: '电流效率 92.1%', factor: '0.92 tCO₂/t铝', recycle: '无', emission: 75.3 }
]);

// 范围二数据
const scope2Data = ref([
  { id: 1, workshop: '电解车间', power: 3200, steam: 820, factor: '0.804 kg/kWh', emission: 126.9 },
  { id: 2, workshop: '轧制车间', power: 2100, steam: 260, factor: '0.788 kg/kWh', emission: 79.4 },
  { id: 3, workshop: '精加工车间', power: 1280, steam: 0, factor: '0.788 kg/kWh', emission: 52.1 }
]);

// 范围三数据
const scope3Data = ref([
  { id: 1, type: '外协物流 (公路)', amount: '1,250 万吨·公里', factor: '0.082 kg/t·km', emission: 102.5 },
  { id: 2, type: '供应商来料 (钢材)', amount: '3,600 t', factor: '1.95 tCO₂/t', emission: 62.0 },
  { id: 3, type: '固废委外处理', amount: '420 t', factor: '0.35 tCO₂/t', emission: 14.7 }
]);

// 排放因子数据
const factorData = ref([
  { project: '天然气低位发热值', scope: '范围一', source: '国家温室气体清单 (2024)', value: '2.162 kgCO₂/Nm³', owner: '刘明', time: '2025-10-02' },
  { project: '电网基准排放因子', scope: '范围二', source: '省级能管平台', value: '0.804 kgCO₂/kWh', owner: '能源中心', time: '2025-09-28' },
  { project: '货运公路排放系数', scope: '范围三', source: 'IPCC AR6 缺省', value: '0.082 kgCO₂/t·km', owner: '供应链', time: '2025-10-05' },
  { project: '液化石油气 EF', scope: '范围一', source: '企业实验室', value: '3.000 kgCO₂/kg', owner: '动力站', time: '2025-09-18' },
  { project: '蒸汽折算系数', scope: '范围二', source: '自备锅炉监测', value: '0.118 tCO₂/t 蒸汽', owner: '动力站', time: '2025-10-03' }
]);

// 表格列定义
const summaryColumns = [
  { title: '车间', dataIndex: 'workshop', key: 'workshop', width: 120 },
  { title: '范围一 (t)', dataIndex: 'scope1', key: 'scope1', width: 120, customRender: ({ text }: any) => text.toFixed(1) },
  { title: '范围二 (t)', dataIndex: 'scope2', key: 'scope2', width: 120, customRender: ({ text }: any) => text.toFixed(1) },
  { title: '范围三 (t)', dataIndex: 'scope3', key: 'scope3', width: 120, customRender: ({ text }: any) => text.toFixed(1) },
  { title: '编辑', dataIndex: 'edit', key: 'edit', width: 80 }
] as TableColumnsType;

const fossilColumns = [
  { title: '车间/设备', dataIndex: 'workshop', key: 'workshop', width: 150 },
  { title: '燃料种类', dataIndex: 'fuel', key: 'fuel', width: 100 },
  { title: '当月用量', dataIndex: 'usage', key: 'usage', width: 120 },
  { title: '排放因子 (kgCO₂e/单位)', dataIndex: 'factor', key: 'factor', width: 180 },
  { title: '排放量 (t)', dataIndex: 'emission', key: 'emission', width: 100, customRender: ({ text }: any) => text.toFixed(1) }
] as TableColumnsType;

const processColumns = [
  { title: '车间/工序', dataIndex: 'workshop', key: 'workshop', width: 120 },
  { title: '活动数据 (t / m³)', dataIndex: 'activity', key: 'activity', width: 150 },
  { title: '排放系数', dataIndex: 'factor', key: 'factor', width: 120 },
  { title: '副产物回收', dataIndex: 'recycle', key: 'recycle', width: 140 },
  { title: '排放量 (t)', dataIndex: 'emission', key: 'emission', width: 100, customRender: ({ text }: any) => text.toFixed(1) }
] as TableColumnsType;

const scope2Columns = [
  { title: '车间', dataIndex: 'workshop', key: 'workshop', width: 120 },
  { title: '用电量 (MWh)', dataIndex: 'power', key: 'power', width: 120, customRender: ({ text }: any) => text.toLocaleString() },
  { title: '用蒸汽 (t)', dataIndex: 'steam', key: 'steam', width: 100, customRender: ({ text }: any) => text.toLocaleString() },
  { title: '当地电网 EF', dataIndex: 'factor', key: 'factor', width: 120 },
  { title: '排放量 (t)', dataIndex: 'emission', key: 'emission', width: 100, customRender: ({ text }: any) => text.toFixed(1) }
] as TableColumnsType;

const scope3Columns = [
  { title: '类型', dataIndex: 'type', key: 'type', width: 150 },
  { title: '业务量', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '排放因子', dataIndex: 'factor', key: 'factor', width: 120 },
  { title: '排放量 (t)', dataIndex: 'emission', key: 'emission', width: 100, customRender: ({ text }: any) => text.toFixed(1) }
] as TableColumnsType;

const factorColumns = [
  { title: '项目', dataIndex: 'project', key: 'project', width: 160 },
  { title: '适用范围', dataIndex: 'scope', key: 'scope', width: 100 },
  { title: '数据来源', dataIndex: 'source', key: 'source', width: 180 },
  { title: '当前值', dataIndex: 'value', key: 'value', width: 150 },
  { title: '更新人', dataIndex: 'owner', key: 'owner', width: 100 },
  { title: '更新时间', dataIndex: 'time', key: 'time', width: 120 },
  { title: '操作', dataIndex: 'operation', key: 'operation', width: 80 }
] as TableColumnsType;

// 方法
const handleFilter = () => {
  console.log('筛选条件改变:', filters.value);
};

const handleBatchImport = () => {
  message.info('批量导入功能开发中');
};

const handleExportTemplate = () => {
  message.info('导出模板功能开发中');
};

const openEditDialog = (workshop: string) => {
  message.info(`打开 ${workshop} 的数据维护弹窗（开发中）`);
};

const copyLastMonthData = () => {
  message.success('已复制上月数据');
};

const addFossilEntry = () => {
  message.info('新增燃料条目功能开发中');
};

const importDetectionReport = () => {
  message.info('导入检测报告功能开发中');
};

const addProcessEntry = () => {
  message.info('新增过程条目功能开发中');
};

const syncEnergyPlatform = () => {
  message.success('已同步能管平台数据');
};

const addScope2Record = () => {
  message.info('新增购能记录功能开发中');
};

const importSupplierList = () => {
  message.info('导入供应商清单功能开发中');
};

const addScope3Project = () => {
  message.info('新增范围三项目功能开发中');
};

const editFactor = (project: string) => {
  message.info(`编辑排放因子：${project}（开发中）`);
};

const getScopeTagClass = (scope: string) => {
  const map = {
    '范围一': 'tag-scope1',
    '范围二': 'tag-scope2',
    '范围三': 'tag-scope3'
  };
  return map[scope as keyof typeof map] || '';
};
</script>

<style lang="less" scoped>
.carbon-data-maintenance {
  .kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;

    .kpi-card {
      border-radius: 16px;

      :deep(.ant-card-body) {
        padding: 18px;
      }

      .kpi-label {
        font-size: 13px;
        color: #6b778c;
      }

      .kpi-value {
        font-size: 30px;
        font-weight: 800;
        margin: 12px 0 6px;
        color: #1677ff;
      }

      .kpi-sub {
        font-size: 12px;
        color: #6b778c;

        .kpi-chip {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 2px 8px;
          border-radius: 999px;
          font-size: 12px;
          background: rgba(22, 119, 255, 0.08);
          color: #1677ff;
          margin-left: 6px;
          border: none;
        }
      }
    }
  }

  .toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px dashed #e6ecf5;

    > * {
      margin-bottom: 4px;
    }
  }

  .section-title {
    font-weight: 700;
    font-size: 16px;
    margin: 16px 0 12px;
    color: #1f3a72;
  }

  .sub-card {
    background: #f8fafc;
    border: 1px solid #e6ecf5;
    border-radius: 12px;

    :deep(.ant-card-head-title) {
      font-size: 15px;
      color: #2b3a55;
    }

    :deep(.ant-card-extra) {
      small {
        color: #6b778c;
        display: block;
        margin-bottom: 12px;
      }
    }
  }

  .action-row {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 10px;
    margin-top: 12px;
    padding: 0 16px 16px;

    .ant-btn {
      height: 32px;
      padding: 0 12px;
      border-radius: 6px;
      border: 1px solid #e6ecf5;
      background: #fff;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      line-height: 1.2;

      &.ant-btn-primary {
        border-color: #1677ff;
        color: #1677ff;
      }
    }
  }

  // 排放因子标签样式
  .tag-scope1 {
    background: rgba(255, 149, 0, 0.12);
    color: #fa8c16;
    border: none;
  }

  .tag-scope2 {
    background: rgba(45, 201, 151, 0.12);
    color: #2fc59e;
    border: none;
  }

  .tag-scope3 {
    background: rgba(114, 46, 209, 0.12);
    color: #722ed1;
    border: none;
  }

  @media (max-width: 1200px) {
    .kpi-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .kpi-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
