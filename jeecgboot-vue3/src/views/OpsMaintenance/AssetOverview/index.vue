<template>
  <div class="asset-overview-container">
    <!-- 第一栏目：统计概览 -->
    <div class="overview-section">
      <a-row :gutter="[16, 16]">
        <!-- 设备总数统计 -->
        <a-col :span="6">
          <div class="kpi-card">
            <div class="kpi-head">
              <Icon icon="ant-design:appstore-outlined" :size="16" />
              <span>设备总数</span>
            </div>
            <div class="kpi-body">
              <div>
                <div class="kpi-num">{{ totalDevices }}</div>
                <div class="kpi-sub">总设备数</div>
              </div>
              <div ref="totalDeviceChart" class="kpi-chart"></div>
            </div>
            <div class="type-list" style="min-height:100px">
              <div class="type-item">
                <span class="type-name">生产设备</span>
                <span class="type-value">{{ productionDeviceCount }} 台</span>
              </div>
              <div class="type-item">
                <span class="type-name">通用设备</span>
                <span class="type-value">{{ generalDeviceCount }} 台</span>
              </div>
              <div class="type-item">
                <span class="type-name">启用设备</span>
                <span class="type-value">{{ enabledDevices }} 台</span>
              </div>
              <div class="type-item">
                <span class="type-name">停用设备</span>
                <span class="type-value">{{ disabledDevices }} 台</span>
              </div>
            </div>
          </div>
        </a-col>

        <!-- 生产设备类型统计 -->
        <a-col :span="6">
          <div class="kpi-card">
            <div class="kpi-head">
              <Icon icon="ant-design:build-outlined" :size="16" />
              <span>生产设备类型统计</span>
            </div>
            <div class="kpi-body">
              <div>
                <div class="kpi-num">{{ productionDeviceCount }}</div>
                <div class="kpi-sub">类型数：{{ productionDeviceTypes }}种</div>
              </div>
              <div ref="productionDeviceChart" class="kpi-chart"></div>
            </div>
            <div class="type-list">
              <div class="type-item" v-for="item in productionTypeList" :key="item.name">
                <span class="type-name">{{ item.name }}</span>
                <span class="type-value">{{ item.value }} 台</span>
              </div>
            </div>
          </div>
        </a-col>

        <!-- 通用设备类型统计 -->
        <a-col :span="6">
          <div class="kpi-card" >
            <div class="kpi-head">
              <Icon icon="ant-design:setting-outlined" :size="16" />
              <span>通用设备类型统计</span>
            </div>
            <div class="kpi-body">
              <div>
                <div class="kpi-num">{{ generalDeviceCount }}</div>
                <div class="kpi-sub">类型数：{{ generalDeviceTypes }}种</div>
              </div>
              <div ref="generalDeviceChart" class="kpi-chart"></div>
            </div>
            <div class="type-list" style="min-height:100px">
              <div class="type-item" v-for="item in generalTypeList" :key="item.name">
                <span class="type-name">{{ item.name }}</span>
                <span class="type-value">{{ item.value }} 台</span>
              </div>
            </div>
          </div>
        </a-col>

        <!-- 设备使用状态统计 -->
        <a-col :span="6" >
          <div class="kpi-card" >
            <div class="kpi-head">
              <Icon icon="ant-design:pie-chart-outlined" :size="16" />
              <span>设备使用统计</span>
            </div>
            <div class="kpi-body">
              <div>
                <div class="kpi-num">{{ totalDevices }}</div>
                <div class="kpi-sub" style="width:250px">在用：{{ enabledDevices }}｜停用：{{ disabledDevices }}｜报废：{{ scrapDevices }}</div>
              </div>
              <div ref="statusChart" class="kpi-chart"></div>
            </div>
            <div class="type-list" style="min-height:100px">
              <div class="type-item">
                <span class="type-name">在用</span>
                <span class="type-value">{{ enabledDevices }} 台</span>
              </div>
              <div class="type-item">
                <span class="type-name">停用</span>
                <span class="type-value">{{ disabledDevices }} 台</span>
              </div>
              <div class="type-item">
                <span class="type-name">报废</span>
                <span class="type-value">{{ scrapDevices }} 台</span>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 第二栏目：查询条件和表格 -->
    <div class="table-section">
      <!-- 查询条件 -->
      <div class="search-form">
        <a-row :gutter="16">
          <a-col :span="4">
            <a-select
              v-model:value="searchForm.deviceType"
              placeholder="设备类型"
              allowClear
            >
              <a-select-option value="general">通用设备</a-select-option>
              <a-select-option value="production">生产设备</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="searchForm.department"
              placeholder="使用部门"
              allowClear
            >
              <a-select-option value="production">生产部门</a-select-option>
              <a-select-option value="maintenance">维护部门</a-select-option>
              <a-select-option value="quality">质检部门</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="searchForm.location"
              placeholder="安装位置"
              allowClear
            >
              <a-select-option value="workshop1">车间1</a-select-option>
              <a-select-option value="workshop2">车间2</a-select-option>
              <a-select-option value="office">办公区</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="searchForm.status"
              placeholder="使用状态"
              allowClear
            >
              <a-select-option value="enabled">启用</a-select-option>
              <a-select-option value="disabled">停用</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <Icon icon="ant-design:search-outlined" />
                查询
              </a-button>
              <a-button @click="handleExport">
                <Icon icon="ant-design:export-outlined" />
                导出
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 设备列表表格 -->
      <div class="table-container">
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="pagination"
          rowKey="id"
          size="middle"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'index'">
              {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
            </template>
            <template v-else-if="column.key === 'runStatus'">
              <a-tag :color="record.runStatus === '运行' ? 'green' : record.runStatus === '待机' ? 'orange' : 'blue'">
                {{ record.runStatus }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'deviceStatus'">
              <a-tag :color="record.deviceStatus === '正常' ? 'green' : record.deviceStatus === '良好' ? 'cyan' : 'red'">
                {{ record.deviceStatus }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" size="small" @click="handleEdit(record)">
                查看
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 第三栏目：设备统计图表 -->
    <div class="chart-section">
      <a-row :gutter="16">
        <!-- 左侧柱状图 -->
        <a-col :span="12">
          <div class="chart-container-wrapper">
            <div class="chart-header-bar">
              <span class="section-title">设备类型数量/功率排序</span>
              <div class="chart-switch">
                <a-radio-group v-model:value="chartMode" @change="handleChartModeChange">
                  <a-radio-button value="count">设备数量</a-radio-button>
                  <a-radio-button value="power">设备功率</a-radio-button>
                </a-radio-group>
              </div>
            </div>
            <div ref="barChart" class="bar-chart-container"></div>
          </div>
        </a-col>
        
        <!-- 右侧折线图 -->
        <a-col :span="12">
          <div class="chart-container-wrapper">
            <div class="chart-header-bar">
              <span class="section-title">区域设备活跃度（最近30天）</span>
            </div>
            <div ref="areaChart" class="area-chart-container"></div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 设备详情弹窗 -->
    <a-modal
      v-model:open="deviceModalVisible"
      :title="modalTitle"
      width="1000px"
      :footer="null"
      centered
    >
      <div class="device-modal-body">
        <div class="device-info-grid">
          <div class="device-image-section">
            <img :src="deviceImageUrl" :alt="currentDevice?.name" class="device-image">
          </div>
          <div class="device-details-section">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">设备编码：</span>
                <span class="detail-value">{{ currentDevice?.code }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">设备名称：</span>
                <span class="detail-value">{{ currentDevice?.name }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">设备类型：</span>
                <span class="detail-value">{{ currentDevice?.type }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">安装位置：</span>
                <span class="detail-value">{{ currentDevice?.location }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">生产厂家：</span>
                <span class="detail-value">{{ currentDevice?.manufacturer }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">购置日期：</span>
                <span class="detail-value">{{ currentDevice?.purchaseDate }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">资产编号：</span>
                <span class="detail-value">{{ currentDevice?.assetCode }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">供应商：</span>
                <span class="detail-value">{{ currentDevice?.supplier }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">运行状态：</span>
                <span class="detail-value">
                  <a-tag :color="getRunStatusColor(currentDevice?.runStatus)">{{ currentDevice?.runStatus }}</a-tag>
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">设备状态：</span>
                <span class="detail-value">
                  <a-tag :color="getDeviceStatusColor(currentDevice?.deviceStatus)">{{ currentDevice?.deviceStatus }}</a-tag>
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 维护保养记录 -->
        <div class="maintenance-section">
          <h4>维护保养记录</h4>
          <div class="maintenance-list">
            <div v-if="maintenanceRecords.length === 0" class="no-records">
              暂无维护保养记录
            </div>
            <div v-else>
              <div v-for="record in maintenanceRecords" :key="record.date" class="maintenance-item">
                <div class="maintenance-info">
                  <div class="maintenance-date">{{ record.date }}</div>
                  <div class="maintenance-desc">{{ record.desc }}</div>
                </div>
                <div class="maintenance-status" :class="getMaintenanceStatusClass(record.status)">
                  {{ getMaintenanceStatusText(record.status) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { Icon } from '/@/components/Icon'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

defineOptions({ name: 'AssetOverview' })
// 图片资源导入
import Image1 from '/@/assets/images/空压机.png'
import Image2 from '/@/assets/images/冷却塔.png'
import Image3 from '/@/assets/images/中央空调.png'
import Image4 from '/@/assets/images/变压器.png'



// 响应式数据
const totalDevices = ref(19)
const generalDeviceCount = ref(7)
const productionDeviceCount = ref(12)
const enabledDevices = ref(18)
const disabledDevices = ref(1)
const scrapDevices = ref(0)
const chartMode = ref('count')
const generalDeviceTypes = ref(4)
const productionDeviceTypes = ref(8)

// 通用设备类型列表
const generalTypeList = ref([
  { name: '冷却塔', value: 3 },
  { name: '中央空调', value: 2 },
  { name: '空压机', value: 2 },
  { name: '变压器', value: 2 }
])

// 生产设备类型列表  
const productionTypeList = ref([
  { name: '熔炼炉', value: 2 },
  { name: '管式过滤器', value: 1 },
  { name: '倾倒炉', value: 1 },
  { name: '铸造机', value: 1 },
  { name: '挤压机', value: 1 },
  { name: '均质炉', value: 1 },
  { name: '时效炉', value: 1 },
  { name: 'CNC', value: 1 }
])

// 图表实例
const totalDeviceChart = ref<HTMLElement>()
const generalDeviceChart = ref<HTMLElement>()
const productionDeviceChart = ref<HTMLElement>()
const statusChart = ref<HTMLElement>()
const barChart = ref<HTMLElement>()
const areaChart = ref<HTMLElement>()

let totalChart: ECharts | null = null
let generalChart: ECharts | null = null
let productionChart: ECharts | null = null
let statusChartInstance: ECharts | null = null
let barChartInstance: ECharts | null = null
let areaChartInstance: ECharts | null = null

// 设备详情弹窗相关
const deviceModalVisible = ref(false)
const currentDevice = ref<any>(null)
const modalTitle = ref('')
const deviceImageUrl = ref('')
const maintenanceRecords = ref<any[]>([])

// 设备图片映射（使用英文文件名避免中文编码问题）
const deviceImageMap: Record<string, string> = {
  '螺杆式空压机': Image1,
  '活塞式空压机': Image1,
  '冷却塔': Image2,
  '中央空调': Image3,
  '变压器': Image4,
  '熔炼炉': Image1,
  '管式过滤器': Image1,
  '均质炉': Image1,
  '挤压机': Image1,
  '时效炉': Image1,
  'CNC': Image1,
  '冷水机': Image1
}

// 模拟维护保养数据
const maintenanceData: Record<string, any[]> = {
  'TY-001': [
    { date: '2024-01-15', desc: '定期保养 - 更换滤芯', status: 'completed' },
    { date: '2024-02-20', desc: '检查压缩机运行状态', status: 'completed' },
    { date: '2024-03-10', desc: '润滑系统维护', status: 'scheduled' }
  ],
  'TY-002': [
    { date: '2024-01-10', desc: '活塞环更换', status: 'completed' },
    { date: '2024-02-15', desc: '冷却系统检查', status: 'completed' },
    { date: '2024-03-05', desc: '电机轴承润滑', status: 'pending' }
  ],
  'CT-001': [
    { date: '2024-01-20', desc: '冷却塔清洗', status: 'completed' },
    { date: '2024-02-25', desc: '风机叶片检查', status: 'completed' },
    { date: '2024-03-12', desc: '水质检测', status: 'scheduled' }
  ],
  'AC-001': [
    { date: '2024-01-12', desc: '制冷剂补充', status: 'completed' },
    { date: '2024-02-18', desc: '冷凝器清洗', status: 'completed' },
    { date: '2024-03-08', desc: '控制系统检查', status: 'pending' }
  ],
  'AC-002': [
    { date: '2024-01-08', desc: '压缩机维护', status: 'completed' },
    { date: '2024-02-22', desc: '蒸发器清洗', status: 'completed' },
    { date: '2024-03-15', desc: '电气系统检查', status: 'scheduled' }
  ]
}

// 搜索表单
const searchForm = reactive({
  deviceType: undefined,
  department: undefined,
  location: undefined,
  status: undefined
})

// 表格配置
const columns = [
  { title: '#', key: 'index', width: 60 },
  { title: '设备编码', dataIndex: 'code', key: 'code', width: 100 },
  { title: '设备名称', dataIndex: 'name', key: 'name', width: 120 },
  { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '安装位置', dataIndex: 'location', key: 'location', width: 100 },
  { title: '生产厂家', dataIndex: 'manufacturer', key: 'manufacturer', width: 100 },
  { title: '购置日期', dataIndex: 'purchaseDate', key: 'purchaseDate', width: 110 },
  { title: '资产编号', dataIndex: 'assetCode', key: 'assetCode', width: 100 },
  { title: '供应商', dataIndex: 'supplier', key: 'supplier', width: 100 },
  { title: '运行状态', dataIndex: 'runStatus', key: 'runStatus', width: 80 },
  { title: '设备状态', dataIndex: 'deviceStatus', key: 'deviceStatus', width: 80 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' }
]

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 10,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条数据`
})

// 模拟表格数据
const tableData = ref([
  {
    id: 1,
    code: 'TY-001',
    name: '空压机#1',
    type: '螺杆式空压机',
    location: '厂房A东侧',
    manufacturer: '阿特拉斯',
    purchaseDate: '2021-03-01',
    assetCode: 'E-100',
    supplier: '华科',
    runStatus: '运行',
    deviceStatus: '正常'
  },
  {
    id: 2,
    code: 'TY-002',
    name: '空压机#2',
    type: '活塞式空压机',
    location: '厂房B北侧',
    manufacturer: '英格索兰',
    purchaseDate: '2020-07-12',
    assetCode: 'C-200',
    supplier: '中机',
    runStatus: '运行',
    deviceStatus: '良好'
  },
  {
    id: 3,
    code: 'CT-001',
    name: '冷却塔#1',
    type: '冷却塔',
    location: '屋顶区',
    manufacturer: 'BAC',
    purchaseDate: '2019-10-05',
    assetCode: 'H-800',
    supplier: '冷工',
    runStatus: '待机',
    deviceStatus: '告警'
  },
  {
    id: 4,
    code: 'AC-001',
    name: '中区机组',
    type: '中央空调',
    location: '机房1',
    manufacturer: 'YORK',
    purchaseDate: '2022-01-15',
    assetCode: 'K-500',
    supplier: '华科',
    runStatus: '运行',
    deviceStatus: '正常'
  },
  {
    id: 5,
    code: 'AC-002',
    name: '西区机组',
    type: '中央空调',
    location: '机房2',
    manufacturer: 'TRANE',
    purchaseDate: '2021-11-02',
    assetCode: 'K-600',
    supplier: '云冷',
    runStatus: '维护',
    deviceStatus: '停机'
  }
])

// 通用设备数据
const generalDeviceData = [
  { name: '变压器', value: 28, color: '#FFD93D' },
  { name: '中央空调', value: 35, color: '#6BCF7F' },
  { name: '空压机', value: 20, color: '#4D96FF' },
  { name: '冷却塔', value: 17, color: '#FF6B9D' },
  { name: '其他', value: 25, color: '#C77DFF' }
]

// 生产设备数据
const productionDeviceData = [
  { name: '熔炼炉', value: 19, color: '#FF8A65' },
  { name: '管式过滤器', value: 15, color: '#81C784' },
  { name: '倾倒炉', value: 12, color: '#64B5F6' },
  { name: '铸造机', value: 14, color: '#FFB74D' },
  { name: '挤压机', value: 8, color: '#F06292' },
  { name: '均质炉', value: 10, color: '#BA68C8' },
  { name: '时效炉', value: 13, color: '#4DD0E1' },
  { name: 'CNC', value: 64, color: '#AED581' }
]

// 柱状图数据
const barChartData = ref({
  count: [
    { name: '变压器', value: 40 },
    { name: '中央空调', value: 35 },
    { name: '空压机', value: 30 },
    { name: '冷却塔', value: 28 },
    { name: '熔炼炉', value: 20 },
    { name: '管式过滤器', value: 18 },
    { name: '倾倒炉', value: 17 },
    { name: '铸造机', value: 15 },
    { name: '挤压机', value: 14 },
    { name: '均质炉', value: 12 },
    { name: '时效炉', value: 10 },
    { name: 'CNC', value: 8 }
  ],
  power: [
    { name: '变压器', value: 800 },
    { name: '中央空调', value: 650 },
    { name: '熔炼炉', value: 500 },
    { name: '空压机', value: 420 },
    { name: '冷却塔', value: 380 },
    { name: 'CNC', value: 350 },
    { name: '铸造机', value: 300 },
    { name: '挤压机', value: 280 },
    { name: '管式过滤器', value: 250 },
    { name: '倾倒炉', value: 220 },
    { name: '均质炉', value: 180 },
    { name: '时效炉', value: 150 }
  ]
})

// 区域活跃度数据
const areaActivityData = ref([
  { name: '厂房A', value: 40 },
  { name: '厂房B', value: 78 },
  { name: '生产区', value: 30 },
  { name: '办公区', value: 45 },
  { name: '仓储区', value: 100 }
])

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 总设备数饼图（生产/通用设备占比）
    if (totalDeviceChart.value) {
      totalChart = echarts.init(totalDeviceChart.value)
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}台 ({d}%)',
          backgroundColor: 'rgba(255,255,255,.95)',
          borderColor: '#e6ecf5',
          textStyle: { color: '#2b3a55' }
        },
        legend: { show: false },
        color: ['#1677ff', '#52c41a'],
        series: [{
          type: 'pie',
          radius: ['55%', '80%'],
          center: ['70%', '50%'],
          label: { show: false },
          labelLine: { show: false },
          data: [
            { value: productionDeviceCount.value, name: '生产设备' },
            { value: generalDeviceCount.value, name: '通用设备' }
          ]
        }]
      }
      totalChart.setOption(option)
    }

    // 通用设备饼图
    if (generalDeviceChart.value) {
      generalChart = echarts.init(generalDeviceChart.value)
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}台 ({d}%)',
          backgroundColor: 'rgba(255,255,255,.95)',
          borderColor: '#e6ecf5',
          textStyle: { color: '#2b3a55' }
        },
        legend: { show: false },
        color: ['#1677ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2'],
        series: [{
          type: 'pie',
          radius: ['55%', '80%'],
          center: ['70%', '50%'],
          label: { show: false },
          labelLine: { show: false },
          data: generalTypeList.value.map(item => ({ name: item.name, value: item.value }))
        }]
      }
      generalChart.setOption(option)
    }

    // 生产设备饼图
    if (productionDeviceChart.value) {
      productionChart = echarts.init(productionDeviceChart.value)
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}台 ({d}%)',
          backgroundColor: 'rgba(255,255,255,.95)',
          borderColor: '#e6ecf5',
          textStyle: { color: '#2b3a55' }
        },
        legend: { show: false },
        color: ['#FF8A65', '#81C784', '#64B5F6', '#FFB74D', '#F06292', '#BA68C8', '#4DD0E1', '#AED581'],
        series: [{
          type: 'pie',
          radius: ['55%', '80%'],
          center: ['70%', '50%'],
          label: { show: false },
          labelLine: { show: false },
          data: productionTypeList.value.map(item => ({ name: item.name, value: item.value }))
        }]
      }
      productionChart.setOption(option)
    }

    // 设备状态饼图
    if (statusChart.value) {
      statusChartInstance = echarts.init(statusChart.value)
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}台 ({d}%)',
          backgroundColor: 'rgba(255,255,255,.95)',
          borderColor: '#e6ecf5',
          textStyle: { color: '#2b3a55' }
        },
        legend: { show: false },
        color: ['#52c41a', '#faad14', '#f5222d'],
        series: [{
          type: 'pie',
          radius: ['55%', '80%'],
          center: ['70%', '50%'],
          label: { show: false },
          labelLine: { show: false },
          data: [
            { name: '在用', value: enabledDevices.value },
            { name: '停用', value: disabledDevices.value },
            { name: '报废', value: scrapDevices.value }
          ]
        }]
      }
      statusChartInstance.setOption(option)
    }

    // 柱状图
    initBarChart()
    
    // 区域活跃度图
    initAreaChart()
  })
}

// 初始化柱状图
const initBarChart = () => {
  if (barChart.value) {
    barChartInstance = echarts.init(barChart.value)
    updateBarChart()
  }
}

// 更新柱状图
const updateBarChart = () => {
  if (!barChartInstance) return
  
  const data = barChartData.value[chartMode.value]
  const option = {
    title: {
      text: chartMode.value === 'count' ? '设备数量排序(前10)' : '设备功率排序(前10)',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#262626'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const unit = chartMode.value === 'count' ? '台' : 'kW'
        return `${params[0].name}: ${params[0].value}${unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLabel: {
        rotate: 45,
        fontSize: 11,
        color: '#262626',
        margin: 10
      },
      axisLine: {
        lineStyle: {
          color: '#e8e8e8',
          width: 1
        }
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      name: chartMode.value === 'count' ? '数量(台)' : '功率(kW)',
      nameTextStyle: {
        color: '#262626',
        fontSize: 12
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: ['#e8e8e8'],
          width: 1,
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#262626',
        fontSize: 12
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      }
    },
    series: [{
      type: 'bar',
      data: data.map(item => item.value),
      itemStyle: {
        color: '#1890ff',
        borderRadius: 0
      },
      barWidth: '20%'
    }]
  }
  barChartInstance.setOption(option)
}

// 初始化区域活跃度图
const initAreaChart = () => {
  if (areaChart.value) {
    areaChartInstance = echarts.init(areaChart.value)
    const option = {
      title: {
        text: '活跃度 = 活跃设备数 / 区域总设备数 × 100%',
        left: 'center',
        bottom: 20,
        textStyle: {
          fontSize: 12,
          color: '#999999'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          return `${params[0].name}: ${params[0].value}%`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: areaActivityData.value.map(item => item.name),
        axisLabel: {
          fontSize: 11,
          color: '#262626'
        },
        axisLine: {
          lineStyle: {
            color: '#e8e8e8'
          }
        },
        axisTick: {
          show: false
        }
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          color: '#262626',
          fontSize: 12
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: ['#e8e8e8'],
            width: 1,
            type: 'dashed'
          }
        },
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        }
      },
      series: [{
        type: 'line',
        data: areaActivityData.value.map(item => item.value),
        smooth: true,
        lineStyle: {
          color: '#52c41a',
          width: 3
        },
        itemStyle: {
          color: '#52c41a',
          borderWidth: 3,
          borderColor: '#ffffff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
              { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
            ]
          }
        }
      }]
    }
    areaChartInstance.setOption(option)
  }
}

// 切换图表模式
const handleChartModeChange = () => {
  updateBarChart()
}

// 搜索处理
const handleSearch = () => {
  console.log('搜索条件:', searchForm)
  // TODO: 实现搜索逻辑
}

// 导出处理
const handleExport = () => {
  console.log('导出数据')
  // TODO: 实现导出逻辑
}

// 编辑处理
const handleEdit = (record: any) => {
  currentDevice.value = record
  modalTitle.value = `${record.name} - 设备详情`
  
  // 设置设备图片
  deviceImageUrl.value = deviceImageMap[record.type] || '/src/assets/images/placeholderImage.png'
  
  // 获取维护记录
  maintenanceRecords.value = maintenanceData[record.code] || []
  
  // 显示弹窗
  deviceModalVisible.value = true
}

// 获取运行状态颜色
const getRunStatusColor = (status: string) => {
  switch (status) {
    case '运行': return 'green'
    case '待机': return 'orange'
    case '维护': return 'blue'
    default: return 'default'
  }
}

// 获取设备状态颜色
const getDeviceStatusColor = (status: string) => {
  switch (status) {
    case '正常': return 'green'
    case '良好': return 'cyan'
    case '告警': return 'red'
    case '停机': return 'red'
    default: return 'default'
  }
}

// 获取维护状态样式类
const getMaintenanceStatusClass = (status: string) => {
  switch (status) {
    case 'completed': return 'status-completed'
    case 'pending': return 'status-pending'
    case 'scheduled': return 'status-scheduled'
    default: return ''
  }
}

// 获取维护状态文字
const getMaintenanceStatusText = (status: string) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'pending': return '待处理'
    case 'scheduled': return '已安排'
    default: return '未知'
  }
}

// 窗口大小变化处理
const handleResize = () => {
  totalChart?.resize()
  generalChart?.resize()
  productionChart?.resize()
  statusChartInstance?.resize()
  barChartInstance?.resize()
  areaChartInstance?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
  
  // 生成完整的模拟数据
  const additionalData = [
    { id: 6, code: 'CT-002', name: '冷却塔#2', type: '冷却塔', location: '屋顶区', manufacturer: 'BAC', purchaseDate: '2020-04-22', assetCode: 'H-1000', supplier: '冷工', runStatus: '运行', deviceStatus: '良好' },
    { id: 7, code: 'SC-001', name: '熔炼炉#1', type: '熔炼炉', location: '车间A东侧', manufacturer: '有研新材', purchaseDate: '2021-08-10', assetCode: 'F-500', supplier: '华科', runStatus: '运行', deviceStatus: '正常' },
    { id: 8, code: 'SC-002', name: '熔炼炉#2', type: '熔炼炉', location: '车间A西侧', manufacturer: '有研新材', purchaseDate: '2021-09-15', assetCode: 'F-501', supplier: '华科', runStatus: '待机', deviceStatus: '正常' }
  ]
  
  tableData.value.push(...additionalData)
})

// 清理事件监听
const cleanup = () => {
  window.removeEventListener('resize', handleResize)
  totalChart?.dispose()
  generalChart?.dispose()
  productionChart?.dispose()
  statusChartInstance?.dispose()
  barChartInstance?.dispose()
  areaChartInstance?.dispose()
}

// 组件卸载时清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  cleanup()
})
</script>

<style lang="less" scoped>
.asset-overview-container {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .overview-section {
    margin-bottom: 24px;

    .kpi-card {
      background: #ffffff;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      height: 380px;
      overflow: visible;
      
      .kpi-head {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 18px 20px;
        border-bottom: 1px solid #e8e9ea;
        background: linear-gradient(90deg, rgba(22,119,255,.10), rgba(26,198,255,.06));
        font-weight: 700;
        color: #1f3a72;
        font-size: 14px;
      }
      
      .kpi-body {
        display: grid;
        grid-template-columns: 140px 1fr;
        align-items: center;
        padding: 18px 16px;
        height: 150px;
        
        .kpi-num {
          font-size: 36px;
          font-weight: 800;
          color: #1677ff;
          line-height: 1;
        }
        
        .kpi-sub {
          color: #8c8c8c;
          font-size: 12px;
          margin-top: 10px;
          line-height: 1.5;
        }
        
        .kpi-chart {
          height: 130px;
          width: 100%;
        }
      }
      
      .type-list {
        display: grid;
        grid-template-columns: repeat(2, minmax(0,1fr));
        gap: 2px 14px;
        padding: 16px 18px 20px;
        border-top: 1px dashed #e8e9ea;
        min-height: 150px;
        
        .type-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 12px;
          color: #262626;
          padding: 1px 0;
          line-height: 1.1;
          
          .type-name {
            color: #8c8c8c;
          }
          
          .type-value {
            font-weight: 700;
            color: #1677ff;
          }
        }
      }
    }

    @media (max-width: 1200px) {
      .kpi-card {
        .kpi-body {
          grid-template-columns: 1fr;
          text-align: center;
        }
        
        .type-list {
          grid-template-columns: 1fr;
        }
      }
    }
  }

  .table-section {
    background: white;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 24px;

    .search-form {
      background: #f5f5f5;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 16px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
      
      .ant-select,
      .ant-input {
        border-radius: 6px;
        
        .ant-select-selector {
          color: #333333 !important;
          
          .ant-select-selection-placeholder {
            color: #333333 !important;
          }
          
          .ant-select-selection-item {
            color: #333333 !important;
          }
        }
        
        &.ant-select {
          .ant-select-selector {
            border-color: #d9d9d9;
            
            .ant-select-selection-placeholder {
              color: #333333 !important;
            }
            
            &:hover {
              border-color: #40a9ff;
            }
          }
          
          &.ant-select-focused .ant-select-selector {
            border-color: #40a9ff;
            box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
            
            .ant-select-selection-placeholder {
              color: #333333 !important;
            }
          }
        }
      }
      
      .ant-btn {
        border-radius: 6px;
        height: 36px;
        
        &[type="primary"] {
          background: #1890ff;
          border-color: #1890ff;
          box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
          
          &:hover {
            background: #40a9ff;
            border-color: #40a9ff;
            box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
          }
        }
      }
    }

    .table-container {
      .ant-table {
        font-size: 12px;
      }
    }
  }

  .chart-section {
    margin-bottom: 24px;

    .bar-chart-container {
      height: 400px;
      width: 100%;
    }
    
    .chart-container-wrapper {
      background: white;
      border-radius: 8px;
      padding: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      
      .chart-header-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .section-title {
          font-size: 16px;
          font-weight: 500;
          color: #333;
        }

        .chart-switch {
          .ant-radio-group {
            .ant-radio-button-wrapper {
              border-color: #d9d9d9;
              
              &.ant-radio-button-wrapper-checked {
                background: #1890ff;
                border-color: #1890ff;
                color: white;
              }
            }
          }
        }
      }
    }
    
    .area-chart-container {
      height: 400px;
      width: 100%;
    }
  }
}

// 强制覆盖Ant Design下拉框placeholder颜色
:deep(.ant-select-selection-placeholder) {
  color: #333333 !important;
}

:deep(.ant-select-selector .ant-select-selection-placeholder) {
  color: #333333 !important;
}

// 确保所有状态下的placeholder都是黑色
:deep(.ant-select:not(.ant-select-disabled) .ant-select-selector .ant-select-selection-placeholder) {
  color: #333333 !important;
}

// 设备详情弹窗样式
.device-modal-body {
  padding: 8px;
  
  .device-info-grid {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 32px;
    margin-bottom: 32px;
    padding: 0 8px;
  }
  
  .device-image-section {
    text-align: center;
    padding: 16px;
  }
  
  .device-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e8e9ea;
  }
  
  .device-details-section {
    padding: 16px 0;
    
    .detail-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px 24px;
    }
    
    .detail-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px dashed #f0f0f0;
      
      .detail-label {
        font-weight: 600;
        color: #595959;
        min-width: 100px;
        font-size: 14px;
        margin-right: 12px;
      }
      
      .detail-value {
        color: #262626;
        font-weight: 500;
        font-size: 14px;
      }
    }
  }
  
  .maintenance-section {
    margin: 32px 16px 16px 16px;
    padding: 24px;
    border-top: 2px solid #e8e9ea;
    border-radius: 8px;
    background: #fafbfc;
    
    h4 {
      margin: 0 0 20px 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f3a72;
    }
    
    .no-records {
      text-align: center;
      color: #999;
      padding: 32px;
      font-style: italic;
      background: #ffffff;
      border-radius: 6px;
      border: 1px dashed #e8e9ea;
    }
    
    .maintenance-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      margin-bottom: 12px;
      background: #ffffff;
      border-radius: 8px;
      border-left: 4px solid #1677ff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .maintenance-info {
        .maintenance-date {
          font-size: 12px;
          color: #8c8c8c;
          margin-bottom: 6px;
        }
        
        .maintenance-desc {
          font-size: 14px;
          color: #262626;
          font-weight: 500;
        }
      }
      
      .maintenance-status {
        padding: 6px 12px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 600;
        
        &.status-completed {
          background: #f6ffed;
          color: #52c41a;
          border: 1px solid #b7eb8f;
        }
        
        &.status-pending {
          background: #fff7e6;
          color: #faad14;
          border: 1px solid #ffd666;
        }
        
        &.status-scheduled {
          background: #e6f7ff;
          color: #1677ff;
          border: 1px solid #91d5ff;
        }
      }
    }
  }
}
</style>
