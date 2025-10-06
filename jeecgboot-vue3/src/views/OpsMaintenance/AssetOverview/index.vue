<template>
  <div class="asset-overview-container">
    <!-- 第一栏目：统计概览 -->
    <div class="overview-section">
      <a-row :gutter="[16, 16]">
        <!-- 设备总数统计 -->
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <div class="stat-icon">
                <Icon icon="ant-design:appstore-outlined" :size="20" />
              </div>
              <span class="stat-title">设备总数</span>
            </div>
            <div class="total-count">{{ totalDevices }}</div>
            <div class="sub-stats">
              <div class="sub-stat">
                <span>通用设备数量：{{ generalDeviceCount }}</span>
              </div>
              <div class="sub-stat">
                <span>生产设备数量：{{ productionDeviceCount }}</span>
              </div>
            </div>
          </div>
        </a-col>

        <!-- 通用设备分布饼图 -->
        <a-col :span="6">
          <div class="chart-card">
            <div class="chart-header">
              <span class="chart-title">通用设备类型统计</span>
            </div>
            <div ref="generalDeviceChart" class="chart-container"></div>
          </div>
        </a-col>

        <!-- 生产设备分布饼图 -->
        <a-col :span="6">
          <div class="chart-card">
            <div class="chart-header">
              <span class="chart-title">生产设备类型统计</span>
            </div>
            <div ref="productionDeviceChart" class="chart-container"></div>
          </div>
        </a-col>

        <!-- 设备使用状态统计 -->
        <a-col :span="6">
          <div class="chart-card">
            <div class="chart-header">
              <span class="chart-title">设备使用状态统计</span>
            </div>
            <div ref="statusChart" class="chart-container"></div>
            <div class="status-info">
              <div class="status-item">
                <span class="status-dot enabled"></span>
                <span>启用：{{ enabledDevices }} 台</span>
              </div>
              <div class="status-item">
                <span class="status-dot disabled"></span>
                <span>停用：{{ disabledDevices }} 台</span>
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
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'enabled' ? 'green' : 'red'">
                {{ record.status === 'enabled' ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" size="small" @click="handleEdit(record)">
                <Icon icon="ant-design:edit-outlined" />
                编辑
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 第三栏目：设备统计柱状图 -->
    <div class="chart-section">
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
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { Icon } from '/@/components/Icon'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

defineOptions({ name: 'AssetOverview' })

// 响应式数据
const totalDevices = ref(280)
const generalDeviceCount = ref(125)
const productionDeviceCount = ref(155)
const enabledDevices = ref(215)
const disabledDevices = ref(65)
const chartMode = ref('count')

// 图表实例
const generalDeviceChart = ref<HTMLElement>()
const productionDeviceChart = ref<HTMLElement>()
const statusChart = ref<HTMLElement>()
const barChart = ref<HTMLElement>()

let generalChart: ECharts | null = null
let productionChart: ECharts | null = null
let statusChartInstance: ECharts | null = null
let barChartInstance: ECharts | null = null

// 搜索表单
const searchForm = reactive({
  deviceType: undefined,
  department: undefined,
  location: undefined,
  status: undefined
})

// 表格配置
const columns = [
  { title: '序号', key: 'index', width: 80 },
  { title: '设备编码', dataIndex: 'code', key: 'code', width: 120 },
  { title: '设备名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '安装位置', dataIndex: 'location', key: 'location', width: 120 },
  { title: '生产厂家', dataIndex: 'manufacturer', key: 'manufacturer', width: 120 },
  { title: '规格型号', dataIndex: 'model', key: 'model', width: 120 },
  { title: '资产负责人', dataIndex: 'manager', key: 'manager', width: 100 },
  { title: '资产负责人电话', dataIndex: 'managerPhone', key: 'managerPhone', width: 140 },
  { title: '供应商', dataIndex: 'supplier', key: 'supplier', width: 120 },
  { title: '供应商联系人', dataIndex: 'supplierContact', key: 'supplierContact', width: 120 },
  { title: '使用状态', dataIndex: 'status', key: 'status', width: 100 },
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
    name: '变压器#1',
    type: '通用设备-变压器',
    location: '测试中心1F',
    manufacturer: 'A厂商',
    model: 'B-100',
    manager: '张三',
    managerPhone: '13800138000',
    supplier: 'A供应商',
    supplierContact: '李四',
    status: 'enabled'
  },
  {
    id: 2,
    code: 'TY-002',
    name: '中央空调#1',
    type: '通用设备-中央空调',
    location: '研发大楼2F',
    manufacturer: 'B厂商',
    model: 'C-200',
    manager: '王五',
    managerPhone: '13900139000',
    supplier: 'B供应商',
    supplierContact: '赵六',
    status: 'enabled'
  }
  // ... 其他18条数据将在后面添加
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

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 通用设备饼图
    if (generalDeviceChart.value) {
      generalChart = echarts.init(generalDeviceChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#ffffff',
          borderWidth: 1,
          textStyle: {
            color: '#ffffff'
          }
        },
        series: [{
          name: '通用设备',
          type: 'pie',
          radius: ['30%', '70%'],
          center: ['50%', '55%'],
          data: generalDeviceData,
          emphasis: {
            itemStyle: {
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            },
            scaleSize: 5
          },
          label: {
            fontSize: 12,
            color: '#262626',
            fontWeight: 'bold',
            textShadowColor: 'rgba(255, 255, 255, 0.8)',
            textShadowBlur: 3,
            textShadowOffsetX: 1,
            textShadowOffsetY: 1
          },
          labelLine: {
            lineStyle: {
              color: '#666666',
              width: 2
            }
          },
          itemStyle: {
            borderColor: '#ffffff',
            borderWidth: 2,
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        }]
      }
      generalChart.setOption(option)
    }

    // 生产设备饼图
    if (productionDeviceChart.value) {
      productionChart = echarts.init(productionDeviceChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#ffffff',
          borderWidth: 1,
          textStyle: {
            color: '#ffffff'
          }
        },
        series: [{
          name: '生产设备',
          type: 'pie',
          radius: ['30%', '70%'],
          center: ['50%', '55%'],
          data: productionDeviceData,
          emphasis: {
            itemStyle: {
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            },
            scaleSize: 5
          },
          label: {
            fontSize: 12,
            color: '#262626',
            fontWeight: 'bold',
            textShadowColor: 'rgba(255, 255, 255, 0.8)',
            textShadowBlur: 3,
            textShadowOffsetX: 1,
            textShadowOffsetY: 1
          },
          labelLine: {
            lineStyle: {
              color: '#666666',
              width: 2
            }
          },
          itemStyle: {
            borderColor: '#ffffff',
            borderWidth: 2,
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        }]
      }
      productionChart.setOption(option)
    }

    // 设备状态饼图
    if (statusChart.value) {
      statusChartInstance = echarts.init(statusChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#ffffff',
          borderWidth: 1,
          textStyle: {
            color: '#ffffff'
          }
        },
        series: [{
          name: '设备状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          data: [
            { name: '启用', value: enabledDevices.value, itemStyle: { color: '#52c41a' } },
            { name: '停用', value: disabledDevices.value, itemStyle: { color: '#ff4d4f' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          },
          label: {
            show: false
          }
        }]
      }
      statusChartInstance.setOption(option)
    }

    // 柱状图
    initBarChart()
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
  console.log('编辑设备:', record)
  // TODO: 实现编辑逻辑
}

// 窗口大小变化处理
const handleResize = () => {
  generalChart?.resize()
  productionChart?.resize()
  statusChartInstance?.resize()
  barChartInstance?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
  
  // 生成完整的10条模拟数据
  const additionalData = [
    { id: 3, code: 'TY-003', name: '空压机#1', type: '通用设备-空压机', location: '动力中心3F', manufacturer: 'C厂商', model: 'D-300', manager: '孙七', managerPhone: '13700137000', supplier: 'C供应商', supplierContact: '周八', status: 'enabled' },
    { id: 4, code: 'TY-004', name: '冷却塔#1', type: '通用设备-冷却塔', location: '测试中心4F', manufacturer: 'D厂商', model: 'E-400', manager: '吴九', managerPhone: '13600136000', supplier: 'D供应商', supplierContact: '郑十', status: 'disabled' },
    { id: 5, code: 'SC-001', name: '熔炼炉#1', type: '生产设备-熔炼炉', location: '生产车间1F', manufacturer: 'E厂商', model: 'F-500', manager: '钱一', managerPhone: '13500135000', supplier: 'E供应商', supplierContact: '孙二', status: 'enabled' },
    { id: 6, code: 'SC-002', name: '管式过滤器#1', type: '生产设备-管式过滤器', location: '生产车间2F', manufacturer: 'F厂商', model: 'G-600', manager: '李三', managerPhone: '13400134000', supplier: 'F供应商', supplierContact: '王四', status: 'enabled' },
    { id: 7, code: 'SC-003', name: '倾倒炉#1', type: '生产设备-倾倒炉', location: '生产车间3F', manufacturer: 'G厂商', model: 'H-700', manager: '陈五', managerPhone: '13300133000', supplier: 'G供应商', supplierContact: '刘六', status: 'enabled' },
    { id: 8, code: 'SC-004', name: '铸造机#1', type: '生产设备-铸造机', location: '生产车间4F', manufacturer: 'H厂商', model: 'I-800', manager: '杨七', managerPhone: '13200132000', supplier: 'H供应商', supplierContact: '黄八', status: 'disabled' },
    { id: 9, code: 'SC-005', name: '挤压机#1', type: '生产设备-挤压机', location: '生产车间5F', manufacturer: 'I厂商', model: 'J-900', manager: '赵九', managerPhone: '13100131000', supplier: 'I供应商', supplierContact: '吴十', status: 'enabled' },
    { id: 10, code: 'SC-006', name: '均质炉#1', type: '生产设备-均质炉', location: '生产车间6F', manufacturer: 'J厂商', model: 'K-1000', manager: '周一', managerPhone: '13000130000', supplier: 'J供应商', supplierContact: '郑二', status: 'enabled' },
    
  ]
  
  tableData.value.push(...additionalData)
})

// 清理事件监听
const cleanup = () => {
  window.removeEventListener('resize', handleResize)
  generalChart?.dispose()
  productionChart?.dispose()
  statusChartInstance?.dispose()
  barChartInstance?.dispose()
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

    .stat-card {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 24px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      height: 240px;

      .stat-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .stat-icon {
          margin-right: 8px;
          color: #666666;
          filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
        }

        .stat-title {
          font-size: 16px;
          font-weight: 600;
          color: #262626;
          text-shadow: none;
        }
      }

      .total-count {
        font-size: 48px;
        font-weight: bold;
        color: #ff4d4f;
        text-align: center;
        margin: 20px 0;
        text-shadow: none;
      }

      .sub-stats {
        .sub-stat {
          padding: 8px 0;
          font-size: 14px;
          color: #595959;
          font-weight: 500;
          text-shadow: none;
          border-bottom: 1px solid #e8e8e8;

          &:last-child {
            border-bottom: none;
          }
        }
      }
    }

    .chart-card {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      height: 240px;

      .chart-header {
        text-align: center;
        margin-bottom: 8px;

        .chart-title {
          font-size: 14px;
          font-weight: 600;
          color: #262626;
          text-shadow: none;
        }
      }

      .chart-container {
        height: 160px;
      }

      .status-info {
        display: flex;
        justify-content: center;
        gap: 24px;
        margin-top: 8px;

        .status-item {
          display: flex;
          align-items: center;
          font-size: 12px;
          font-weight: 500;
          color: #595959;
          text-shadow: none;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            margin-right: 4px;
            border: 1px solid #d9d9d9;

            &.enabled {
              background: #52c41a;
            }

            &.disabled {
              background: #ff4d4f;
            }
          }
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
    background: white;
    border-radius: 8px;
    padding: 24px;
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

    .bar-chart-container {
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
</style>
