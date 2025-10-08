<template>
  <div class="asset-ledger-page">
    <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
      <a-tab-pane key="transformer" tab="变压器" />
      <a-tab-pane key="centralAC" tab="中央空调" />
      <a-tab-pane key="compressor" tab="空压机" />
      <a-tab-pane key="coolingTower" tab="冷却塔" />
    </a-tabs>

    <div class="top-panels">
      <a-row :gutter="16">
        <!-- 左：设备图片 -->
        <a-col :span="6">
          <div class="card-panel image-panel">
            <div class="panel-title">设备图片</div>
            <img :src="currentImage" class="device-image" alt="device" />
          </div>
        </a-col>

        <!-- 中：设备参数表 -->
        <a-col :span="12">
          <div class="card-panel table-panel">
            <div class="panel-title">设备参数列表</div>
            <a-table
              :columns="currentColumns"
              :data-source="pagedTableData"
              :pagination="false"
              size="middle"
              bordered
              rowKey="_rid"
            >
              <template #bodyCell="{ column, index, record }">
                <template v-if="column.key === 'seq'">
                  {{ (pageIndex - 1) * pageSize + index + 1 }}
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="record.status === '运行' ? 'green' : (record.status === '停机' ? 'red' : 'blue')">
                    {{ record.status }}
                  </a-tag>
                </template>
              </template>
            </a-table>

            <div class="simple-pager">
              <a-button size="small" @click="prevPage" :disabled="pageIndex === 1">上一页</a-button>
              <span>{{ pageIndex }} / {{ totalPages }}</span>
              <a-button size="small" @click="nextPage" :disabled="pageIndex === totalPages">下一页</a-button>
            </div>
          </div>
        </a-col>

        <!-- 右：汇总 + 环形图 -->
        <a-col :span="6">
          <div class="card-panel side-summary">
            <div class="panel-title">{{ summary.mainTitle }}</div>
            <div class="total-capacity">
              <div class="capacity-value">{{ summary.totalValue }}</div>
            </div>
            <div class="panel-title chart-title">{{ summary.chartTitle }}</div>
            <div ref="donutRef" class="donut-chart"></div>
            <div class="legend-extra">
              <div v-for="(item, i) in summary.donutData" :key="i">
                <span class="dot" :class="`dot-${String.fromCharCode(97 + i)}`"></span>
                {{ item.name }} <b>{{ item.value }}台 {{ ((item.value / summary.donutData.reduce((sum, d) => sum + d.value, 0)) * 100).toFixed(1) }}%</b>
              </div>
            </div>
            <div class="type-legend">
              <div v-for="(item, i) in summary.legendItems" :key="i">
                <span class="dot" :class="`dot-type-${String.fromCharCode(97 + i)}`"></span>
                {{ item.name }} <b>{{ item.count }}</b>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 下：堆叠柱状图 -->
    <div class="bottom-chart">
      <div class="card-panel">
        <div class="panel-title">{{ barTitle }}</div>
        <div ref="stackedRef" class="stacked-chart"></div>
      </div>
    </div>
  </div>
  
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

defineOptions({ name: 'AssetLedger' })

// 当前激活的 Tab
const activeKey = ref<'transformer' | 'centralAC' | 'compressor' | 'coolingTower'>('transformer')

// 简易分页
const pageIndex = ref(1)
const pageSize = 6

// 图片资源
import transformerImage from '/@/assets/images/变压器.png'
import centralACImage from '/@/assets/images/中央空调.png'
import compressorImage from '/@/assets/images/空压机.png'
import coolingTowerImage from '/@/assets/images/冷却塔.png'

const images = {
  transformer: transformerImage,
  centralAC: centralACImage,
  compressor: compressorImage,
  coolingTower: coolingTowerImage
}

// 表格列配置（按类型）
const columnsMap: Record<string, any[]> = {
  centralAC: [
    { title: '序号', key: 'seq', width: 70 },
    { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '型号', dataIndex: 'model', key: 'model', width: 140 },
    { title: '制冷量(kW)', dataIndex: 'cooling', key: 'cooling', width: 110 },
    { title: '制热量(kW)', dataIndex: 'heating', key: 'heating', width: 110 },
    { title: '功率(kW)', dataIndex: 'power', key: 'power', width: 100 },
    { title: '能效比', dataIndex: 'eer', key: 'eer', width: 90 },
    { title: '风量(m³/h)', dataIndex: 'airflow', key: 'airflow', width: 110 },
    { title: '运行状态', dataIndex: 'status', key: 'status', width: 100 }
  ],
  compressor: [
    { title: '序号', key: 'seq', width: 70 },
    { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '型号', dataIndex: 'model', key: 'model', width: 140 },
    { title: '排气量(m³/min)', dataIndex: 'flow', key: 'flow', width: 120 },
    { title: '排气压力(bar)', dataIndex: 'pressure', key: 'pressure', width: 120 },
    { title: '功率(kW)', dataIndex: 'power', key: 'power', width: 100 },
    { title: '转速(rpm)', dataIndex: 'rpm', key: 'rpm', width: 110 },
    { title: '噪音(dB)', dataIndex: 'noise', key: 'noise', width: 90 },
    { title: '运行状态', dataIndex: 'status', key: 'status', width: 100 }
  ],
  transformer: [
    { title: '序号', key: 'seq', width: 70 },
    { title: '变压器类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '型号', dataIndex: 'model', key: 'model', width: 140 },
    { title: '额定容量(kVA)', dataIndex: 'capacity', key: 'capacity', width: 120 },
    { title: '电压等级(kV)', dataIndex: 'voltage', key: 'voltage', width: 120 },
    { title: '空载电流(%)', dataIndex: 'noLoadA', key: 'noLoadA', width: 110 },
    { title: '空载损耗(W)', dataIndex: 'noLoadLoss', key: 'noLoadLoss', width: 110 },
    { title: '负载损耗(W)', dataIndex: 'loadLoss', key: 'loadLoss', width: 110 },
    { title: '冷却方式', dataIndex: 'cooling', key: 'cooling', width: 100 }
  ],
  coolingTower: [
    { title: '序号', key: 'seq', width: 70 },
    { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '型号', dataIndex: 'model', key: 'model', width: 140 },
    { title: '冷却水量(m³/h)', dataIndex: 'water', key: 'water', width: 130 },
    { title: '进水温度(°C)', dataIndex: 'inT', key: 'inT', width: 110 },
    { title: '出水温度(°C)', dataIndex: 'outT', key: 'outT', width: 110 },
    { title: '湿球温度(°C)', dataIndex: 'wbT', key: 'wbT', width: 110 },
    { title: '风机功率(kW)', dataIndex: 'power', key: 'power', width: 110 },
    { title: '运行状态', dataIndex: 'status', key: 'status', width: 100 }
  ]
}

// 模拟数据（每类至少 12 条，分页展示 6 条）
const tableMap: Record<string, any[]> = {
  centralAC: [
    { _rid: 1, type: '水冷机组', model: 'YORK-YCIV0155', cooling: 155, heating: 170, power: 45, eer: 3.4, airflow: 28000, status: '运行' },
    { _rid: 2, type: '水冷机组', model: 'YORK-YCIV0200', cooling: 200, heating: 220, power: 58, eer: 3.5, airflow: 36000, status: '运行' },
    { _rid: 3, type: '风冷机组', model: 'CARRIER-30HXC', cooling: 105, heating: 115, power: 32, eer: 3.3, airflow: 22000, status: '运行' },
    { _rid: 4, type: '风冷机组', model: 'CARRIER-30HXC', cooling: 140, heating: 155, power: 42, eer: 3.3, airflow: 28000, status: '运行' },
    { _rid: 5, type: '水冷机组', model: 'YORK-YCIV0250', cooling: 250, heating: 275, power: 72, eer: 3.5, airflow: 45000, status: '待机' },
    { _rid: 6, type: '风冷机组', model: 'TRANE-RTAD', cooling: 175, heating: 190, power: 52, eer: 3.4, airflow: 32000, status: '运行' },
    { _rid: 7, type: '水冷机组', model: 'TRANE-CGV', cooling: 300, heating: 330, power: 85, eer: 3.6, airflow: 50000, status: '运行' },
    { _rid: 8, type: '风冷机组', model: 'DAIKIN-EWY', cooling: 200, heating: 220, power: 60, eer: 3.2, airflow: 36000, status: '停机' },
    { _rid: 9, type: '水冷机组', model: 'MCQUAY-CENTRIF', cooling: 350, heating: 380, power: 96, eer: 3.7, airflow: 52000, status: '运行' },
    { _rid: 10, type: '风冷机组', model: 'HITACHI-RAS', cooling: 120, heating: 135, power: 36, eer: 3.1, airflow: 24000, status: '运行' },
    { _rid: 11, type: '水冷机组', model: 'YORK-YPW', cooling: 220, heating: 245, power: 66, eer: 3.4, airflow: 39000, status: '运行' },
    { _rid: 12, type: '风冷机组', model: 'TRANE-CGA', cooling: 155, heating: 168, power: 47, eer: 3.3, airflow: 30000, status: '运行' }
  ],
  compressor: [
    { _rid: 1, type: '螺杆式', model: 'ATLAS-COPCO-GA22', flow: 3.7, pressure: 8, power: 22, rpm: 3000, noise: 68, status: '运行' },
    { _rid: 2, type: '螺杆式', model: 'ATLAS-COPCO-GA37', flow: 6.2, pressure: 8, power: 37, rpm: 3000, noise: 70, status: '运行' },
    { _rid: 3, type: '活塞式', model: 'INGERSOLL-RAND-UP6', flow: 1.8, pressure: 10, power: 11, rpm: 1450, noise: 75, status: '待机' },
    { _rid: 4, type: '螺杆式', model: 'ATLAS-COPCO-GA55', flow: 9.8, pressure: 8, power: 55, rpm: 3000, noise: 72, status: '运行' },
    { _rid: 5, type: '活塞式', model: 'INGERSOLL-RAND-UP15', flow: 4.2, pressure: 10, power: 15, rpm: 1450, noise: 78, status: '运行' },
    { _rid: 6, type: '螺杆式', model: 'ATLAS-COPCO-GA75', flow: 13.1, pressure: 8, power: 75, rpm: 3000, noise: 74, status: '运行' },
    { _rid: 7, type: '离心式', model: 'FS-TRIDENT-110', flow: 22.0, pressure: 7.5, power: 110, rpm: 3600, noise: 76, status: '运行' },
    { _rid: 8, type: '螺杆式', model: 'KAESER-SX7', flow: 1.1, pressure: 8, power: 7.5, rpm: 3000, noise: 65, status: '停机' },
    { _rid: 9, type: '螺杆式', model: 'KAESER-SX11', flow: 1.8, pressure: 8, power: 11, rpm: 3000, noise: 66, status: '运行' },
    { _rid: 10, type: '螺杆式', model: 'KAESER-ASD37', flow: 6.1, pressure: 8, power: 37, rpm: 3000, noise: 70, status: '运行' },
    { _rid: 11, type: '活塞式', model: 'IR-SSR-55', flow: 5.5, pressure: 10, power: 55, rpm: 1450, noise: 77, status: '运行' },
    { _rid: 12, type: '离心式', model: 'MAN-TS110', flow: 20.0, pressure: 7.5, power: 110, rpm: 3600, noise: 76, status: '运行' }
  ],
  transformer: [
    { _rid: 1, type: '干式变压器', model: 'SCB10-315/10', capacity: 315, voltage: '10/0.4', noLoadA: 4, noLoadLoss: 670, loadLoss: 3650, cooling: 'AN' },
    { _rid: 2, type: '干式变压器', model: 'SCB11-500/10', capacity: 500, voltage: '10/0.4', noLoadA: 4, noLoadLoss: 920, loadLoss: 5150, cooling: 'AN' },
    { _rid: 3, type: '干式变压器', model: 'SCB11-630/10', capacity: 630, voltage: '10/0.4', noLoadA: 4, noLoadLoss: 980, loadLoss: 5950, cooling: 'AN' },
    { _rid: 4, type: '干式变压器', model: 'SCB11-800/10', capacity: 800, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1200, loadLoss: 7500, cooling: 'AN' },
    { _rid: 5, type: '油浸式变压器', model: 'S11-1000/10', capacity: 1000, voltage: '10/0.4', noLoadA: 5, noLoadLoss: 1450, loadLoss: 10300, cooling: 'ONAN' },
    { _rid: 6, type: '油浸式变压器', model: 'S13-1250/10', capacity: 1250, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1600, loadLoss: 12000, cooling: 'ONAF' },
    { _rid: 7, type: '干式变压器', model: 'SCB11-1000/10', capacity: 1000, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1500, loadLoss: 10000, cooling: 'AN' },
    { _rid: 8, type: '干式变压器', model: 'SCB10-1250/10', capacity: 1250, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1600, loadLoss: 12000, cooling: 'AN' },
    { _rid: 9, type: '油浸式变压器', model: 'S11-1600/10', capacity: 1600, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1850, loadLoss: 15000, cooling: 'ONAF' },
    { _rid: 10, type: '油浸式变压器', model: 'S11-800/10', capacity: 800, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1200, loadLoss: 8000, cooling: 'ONAN' },
    { _rid: 11, type: '干式变压器', model: 'SCB11-500/10', capacity: 500, voltage: '10/0.4', noLoadA: 4, noLoadLoss: 920, loadLoss: 5150, cooling: 'AN' },
    { _rid: 12, type: '油浸式变压器', model: 'S13-1000/10', capacity: 1000, voltage: '10/0.4', noLoadA: 6, noLoadLoss: 1500, loadLoss: 10300, cooling: 'ONAF' }
  ],
  coolingTower: [
    { _rid: 1, type: '逆流式', model: 'BAC-CTI-150', water: 150, inT: 37, outT: 32, wbT: 28, power: 5.5, status: '运行' },
    { _rid: 2, type: '横流式', model: 'BAC-CTI-200', water: 200, inT: 37, outT: 32, wbT: 28, power: 7.5, status: '运行' },
    { _rid: 3, type: '横流式', model: 'BAC-CTI-300', water: 300, inT: 37, outT: 32, wbT: 28, power: 11, status: '运行' },
    { _rid: 4, type: '逆流式', model: 'BAC-CTI-400', water: 400, inT: 37, outT: 32, wbT: 28, power: 15, status: '待机' },
    { _rid: 5, type: '横流式', model: 'BAC-CTI-500', water: 500, inT: 37, outT: 32, wbT: 28, power: 18.5, status: '运行' },
    { _rid: 6, type: '逆流式', model: 'BAC-CTI-600', water: 600, inT: 37, outT: 32, wbT: 28, power: 22, status: '运行' },
    { _rid: 7, type: '逆流式', model: 'BAC-CTI-750', water: 750, inT: 37, outT: 32, wbT: 28, power: 30, status: '运行' },
    { _rid: 8, type: '横流式', model: 'BAC-CTI-120', water: 120, inT: 37, outT: 32, wbT: 28, power: 5.5, status: '运行' },
    { _rid: 9, type: '逆流式', model: 'BAC-CTI-260', water: 260, inT: 37, outT: 32, wbT: 28, power: 11, status: '运行' },
    { _rid: 10, type: '横流式', model: 'BAC-CTI-320', water: 320, inT: 37, outT: 32, wbT: 28, power: 15, status: '运行' },
    { _rid: 11, type: '逆流式', model: 'BAC-CTI-520', water: 520, inT: 37, outT: 32, wbT: 28, power: 18.5, status: '运行' },
    { _rid: 12, type: '横流式', model: 'BAC-CTI-680', water: 680, inT: 37, outT: 32, wbT: 28, power: 22, status: '运行' }
  ]
}

// 表格分页相关
const currentColumns = computed(() => columnsMap[activeKey.value])
const currentTable = computed(() => tableMap[activeKey.value])
const totalPages = computed(() => Math.max(1, Math.ceil(currentTable.value.length / pageSize)))
const pagedTableData = computed(() => {
  const start = (pageIndex.value - 1) * pageSize
  return currentTable.value.slice(start, start + pageSize)
})

function prevPage() {
  if (pageIndex.value > 1) pageIndex.value--
}
function nextPage() {
  if (pageIndex.value < totalPages.value) pageIndex.value++
}

// 顶部图片
const currentImage = computed(() => images[activeKey.value])

// 右侧汇总及饼图数据
type Summary = { 
  title: string; 
  kpi1Label: string; 
  kpi1Value: string; 
  kpi2Label: string; 
  kpi2Value: string; 
  donutData: { name: string; value: number }[]; 
  legendText: string[];
  mainTitle: string;
  totalValue: string;
  chartTitle: string;
  legendItems: { name: string; count: string }[];
}
const summary = reactive<Summary>({ 
  title: '', 
  kpi1Label: '', 
  kpi1Value: '', 
  kpi2Label: '', 
  kpi2Value: '', 
  donutData: [], 
  legendText: [],
  mainTitle: '',
  totalValue: '',
  chartTitle: '',
  legendItems: []
})

function buildSummary() {
  const rows = currentTable.value
  if (activeKey.value === 'centralAC') {
    const totalPower = rows.reduce((s, r) => s + Number(r.power), 0)
    const totalCooling = rows.reduce((s, r) => s + Number(r.cooling), 0)
    const typeCounts = countBy(rows, 'type')
    setSummary({
      title: '设备总功率 & 总制冷量',
      kpi1Label: '总功率',
      kpi1Value: `${totalPower} kW`,
      kpi2Label: '总制冷量',
      kpi2Value: `${totalCooling} kW`,
      donutData: Object.keys(typeCounts).map(k => ({ name: k, value: typeCounts[k] })),
      legendText: Object.keys(typeCounts).map(k => `${k} ${typeCounts[k]} 台`),
      mainTitle: '设备总功率 & 总制冷量',
      totalValue: `${totalPower} kW`,
      chartTitle: '功率占比',
      legendItems: Object.keys(typeCounts).map(k => ({ name: k, count: `${typeCounts[k]} 台` }))
    })
  } else if (activeKey.value === 'compressor') {
    const totalPower = rows.reduce((s, r) => s + Number(r.power), 0)
    const totalFlow = rows.reduce((s, r) => s + Number(r.flow), 0)
    const flowGroup = {
      '<10m³/min': rows.filter(r => r.flow < 10).length,
      '≥10m³/min': rows.filter(r => r.flow >= 10).length
    }
    const typeCounts = countBy(rows, 'type')
    setSummary({
      title: '总功率 & 总排气量',
      kpi1Label: '总功率',
      kpi1Value: `${totalPower} kW`,
      kpi2Label: '总排气量',
      kpi2Value: `${totalFlow.toFixed(1)} m³/min`,
      donutData: Object.keys(flowGroup).map(k => ({ name: k, value: (flowGroup as any)[k] })),
      legendText: Object.keys(typeCounts).map(k => `${k} ${typeCounts[k]} 台`),
      mainTitle: '总功率 & 总排气量',
      totalValue: `${totalPower} kW`,
      chartTitle: '排气量分组占比',
      legendItems: Object.keys(typeCounts).map(k => ({ name: k, count: `${typeCounts[k]} 台` }))
    })
  } else if (activeKey.value === 'transformer') {
    const totalCap = rows.reduce((s, r) => s + Number(r.capacity), 0)
    const capGroups = {
      '≤500 kVA': rows.filter(r => r.capacity <= 500).length,
      '500-1000 kVA': rows.filter(r => r.capacity > 500 && r.capacity <= 1000).length,
      '1000-1500 kVA': rows.filter(r => r.capacity > 1000 && r.capacity <= 1500).length,
      '≥1500 kVA': rows.filter(r => r.capacity > 1500).length
    }
    const typeCounts = countBy(rows, 'type')
    setSummary({
      title: '变压器总容量',
      kpi1Label: '总容量',
      kpi1Value: `${totalCap} kVA`,
      kpi2Label: '台数',
      kpi2Value: `${rows.length} 台`,
      donutData: Object.keys(capGroups).map(k => ({ name: k, value: (capGroups as any)[k] })),
      legendText: Object.keys(typeCounts).map(k => `${k} ${typeCounts[k]} 台`),
      mainTitle: '变压器总容量',
      totalValue: `${totalCap} kVA`,
      chartTitle: '额定容量占比',
      legendItems: Object.keys(typeCounts).map(k => ({ name: k, count: `${typeCounts[k]} 台` }))
    })
  } else if (activeKey.value === 'coolingTower') {
    const totalPower = rows.reduce((s, r) => s + Number(r.power), 0)
    const totalWater = rows.reduce((s, r) => s + Number(r.water), 0)
    const waterGroups = {
      '≤500m³/h': rows.filter(r => r.water <= 500).length,
      '≥500m³/h': rows.filter(r => r.water > 500).length
    }
    const typeCounts = countBy(rows, 'type')
    setSummary({
      title: '总功率 & 总冷却水量',
      kpi1Label: '总功率',
      kpi1Value: `${totalPower} kW`,
      kpi2Label: '总冷却水量',
      kpi2Value: `${totalWater} m³/h`,
      donutData: Object.keys(waterGroups).map(k => ({ name: k, value: (waterGroups as any)[k] })),
      legendText: Object.keys(typeCounts).map(k => `${k} ${typeCounts[k]} 台`),
      mainTitle: '总功率 & 总冷却水量',
      totalValue: `${totalPower} kW`,
      chartTitle: '冷却水量分组占比',
      legendItems: Object.keys(typeCounts).map(k => ({ name: k, count: `${typeCounts[k]} 台` }))
    })
  }
}

function countBy(arr: any[], key: string) {
  return arr.reduce((acc: Record<string, number>, cur) => {
    const k = String(cur[key])
    acc[k] = (acc[k] || 0) + 1
    return acc
  }, {})
}

function setSummary(s: Summary) {
  summary.title = s.title
  summary.kpi1Label = s.kpi1Label
  summary.kpi1Value = s.kpi1Value
  summary.kpi2Label = s.kpi2Label
  summary.kpi2Value = s.kpi2Value
  summary.donutData = s.donutData
  summary.legendText = s.legendText
  summary.mainTitle = s.mainTitle
  summary.totalValue = s.totalValue
  summary.chartTitle = s.chartTitle
  summary.legendItems = s.legendItems
}

// 环形图与堆叠图实例
const donutRef = ref<HTMLElement>()
const stackedRef = ref<HTMLElement>()
let donutChart: echarts.ECharts | null = null
let stackedChart: echarts.ECharts | null = null

const barTitle = computed(() => {
  switch (activeKey.value) {
    case 'centralAC':
      return '各区域空调功率分布图'
    case 'compressor':
      return '各车间空压机功率分布图'
    case 'transformer':
      return '各车间变压器容量分布图'
    case 'coolingTower':
      return '各站点冷却塔功率分布图'
  }
})

// 堆叠柱状图数据
const stackedMap = {
  centralAC: {
    x: ['办公区', '生产区', '仓储区', '研发区', '会议室', '餐厅', '机房'],
    series: [
      { name: '105kW', data: [1, 2, 0, 0, 1, 1, 0] },
      { name: '155kW', data: [2, 2, 1, 0, 2, 1, 1] },
      { name: '200kW', data: [1, 0, 1, 0, 2, 1, 0] },
      { name: '250kW', data: [3, 3, 2, 0, 1, 2, 1] },
      { name: '300kW', data: [0, 0, 0, 2, 1, 0, 0] }
    ]
  },
  compressor: {
    x: ['车间A', '车间B', '车间C', '车间D', '车间E', '车间F', '车间G'],
    series: [
      { name: '22kW', data: [1, 1, 1, 0, 1, 1, 1] },
      { name: '37kW', data: [1, 1, 1, 0, 2, 2, 1] },
      { name: '55kW', data: [1, 1, 2, 1, 3, 1, 2] },
      { name: '75kW', data: [0, 1, 0, 2, 1, 0, 0] },
      { name: '110kW', data: [0, 0, 1, 0, 1, 1, 1] }
    ]
  },
  transformer: {
    x: ['厂房一', '厂房二', '厂房三', '厂房四', '厂房五', '厂房六', '厂房七'],
    series: [
      { name: '500kVA', data: [2, 1, 1, 1, 0, 0, 0] },
      { name: '800kVA', data: [1, 2, 1, 2, 0, 2, 2] },
      { name: '1000kVA', data: [1, 1, 0, 2, 1, 1, 1] },
      { name: '1250kVA', data: [0, 0, 1, 1, 1, 1, 0] },
      { name: '1600kVA', data: [1, 0, 0, 2, 1, 2, 2] }
    ]
  },
  coolingTower: {
    x: ['冷站A', '冷站B', '冷站C', '冷站D', '冷站E', '冷站F', '冷站G'],
    series: [
      { name: '5.5kW', data: [1, 2, 1, 1, 0, 1, 1] },
      { name: '11kW', data: [1, 1, 1, 1, 2, 2, 1] },
      { name: '15kW', data: [1, 1, 1, 2, 1, 1, 2] },
      { name: '22kW', data: [0, 1, 0, 1, 1, 1, 0] },
      { name: '30kW', data: [0, 0, 1, 1, 2, 1, 1] }
    ]
  }
}

function initDonut() {
  if (!donutRef.value) return
  donutChart = echarts.init(donutRef.value)
  updateDonut()
}

function updateDonut() {
  if (!donutChart) return
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item' },
    legend: { show: false },
    series: [
      {
        type: 'pie',
        radius: ['45%', '85%'],  // 调整环形图半径，使其更宽
        center: ['50%', '50%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: summary.donutData
      }
    ]
  }
  donutChart.setOption(option)
}

function initStacked() {
  if (!stackedRef.value) return
  stackedChart = echarts.init(stackedRef.value)
  updateStacked()
}

function updateStacked() {
  if (!stackedChart) return
  const conf = (stackedMap as any)[activeKey.value]
  
  // 按照图片颜色配置的纯色方案（无渐变）
  const solidColors = [
    '#5B9BD5', // 蓝色 (500kVA)
    '#70C4F4', // 浅蓝色 (800kVA)
    '#9F7AEA', // 紫色 (1000kVA)
    '#4CAF50', // 绿色 (1250kVA)
    '#FF9800'  // 橙色 (1600kVA)
  ]
  
  const option: echarts.EChartsOption = {
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { 
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(0,0,0,0.08)'
        }
      },
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: {
        color: '#262626',
        fontSize: 12
      },
      padding: [8, 12]
    },
    legend: { 
      top: 4,
      textStyle: {
        color: '#262626',
        fontWeight: 600,
        fontSize: 12
      },
      itemWidth: 14,
      itemHeight: 14,
      itemGap: 16
    },
    grid: { left: '3%', right: '3%', bottom: '8%', top: 40, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: conf.x,
      axisLine: {
        lineStyle: {
          color: '#d9d9d9',
          width: 1
        }
      },
      axisLabel: {
        color: '#262626',
        fontWeight: 500,
        fontSize: 12
      },
      axisTick: {
        show: false
      }
    },
    yAxis: { 
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#595959',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      },
      axisTick: {
        show: false
      }
    },
    series: conf.series.map((s: any, idx: number) => ({
      name: s.name,
      type: 'bar',
      stack: 'sum',
      barWidth: '20%', // 用户偏好：柱形宽度控制在20%左右
      itemStyle: { 
        color: solidColors[idx % solidColors.length], // 使用纯色
        borderRadius: [0, 0, 0, 0], // 用户偏好：无圆角设计
        // 用户偏好：无立体阴影
      },
      emphasis: {
        itemStyle: {
          color: solidColors[idx % solidColors.length], // 保持纯色
          borderWidth: 0 // 用户偏好：简洁设计
        }
      },
      data: s.data
    }))
  }
  stackedChart.setOption(option)
}

function onTabChange() {
  pageIndex.value = 1
  buildSummary()
  nextTick(() => {
    donutChart && donutChart.dispose()
    stackedChart && stackedChart.dispose()
    initDonut()
    initStacked()
  })
}

// 首次加载
onMounted(() => {
  buildSummary()
  initDonut()
  initStacked()
  window.addEventListener('resize', handleResize)
})

// 监听窗口大小变化，实现响应式重绘
function handleResize() {
  if (donutChart) donutChart.resize()
  if (stackedChart) stackedChart.resize()
}

// 当 summary 数据变化时，更新环形图
watch(
  () => summary.donutData,
  () => updateDonut(),
  { deep: true }
)

// 组件卸载时移除监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (donutChart) {
    donutChart.dispose()
    donutChart = null
  }
  if (stackedChart) {
    stackedChart.dispose()
    stackedChart = null
  }
})

</script>

<style scoped>
.asset-ledger-page {
  padding: 12px 12px 24px 12px;
}

.top-panels {
  margin-top: 8px;
}

.card-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
  min-height: 400px;
}

.panel-title {
  font-weight: 1000;
  font-size: 16px;
  margin-bottom: 8px;
}

.image-panel {
  display: flex;
  flex-direction: column;
}

.device-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 6px;
}

.table-panel .simple-pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding-top: 8px;
}

.side-summary .kpi-box {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.kpi-item {
  background: #f6f9ff;
  border: 1px solid #e6f0ff;
  border-radius: 8px;
  padding: 8px 12px;
  flex: 1;
}
.kpi-item + .kpi-item { margin-left: 8px; }
.kpi-label { color: #888; font-size: 12px; }
.kpi-value { font-size: 20px; font-weight: 600; color: #3b82f6; }

.total-capacity {
  text-align: center;
  margin: 20px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.capacity-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  line-height: 1.2;
  text-align: center;
}

.chart-title {
  margin-top: 20px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  text-align: center;
}

.donut-chart {
  width: 100%;
  height: 200px;
  margin: 16px 0;
}

.legend-text {
  padding-top: 8px;
  color: #666;
}

.legend-extra {
  margin-top: 12px;
  padding: 12px;
  font-size: 13px;
  color: #666;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.legend-extra div {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  line-height: 1.4;
}

.legend-extra div:last-child {
  margin-bottom: 0;
}

.type-legend {
  margin-top: 8px;
  padding: 8px 0;
  font-size: 12px;
  color: #666;
}

.type-legend div {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  line-height: 1.3;
}

.type-legend div:last-child {
  margin-bottom: 0;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  display: inline-block;
}

.dot-a {
  background-color: #5470c6;
}

.dot-b {
  background-color: #91cc75;
}

.dot-c {
  background-color: #fac858;
}

.dot-d {
  background-color: #ee6666;
}

.dot-e {
  background-color: #73c0de;
}

.dot-type-a {
  background-color: #4CAF50;
}

.dot-type-b {
  background-color: #FF9800;
}

.dot-type-c {
  background-color: #9C27B0;
}

.dot-type-d {
  background-color: #607D8B;
}

.dot-type-e {
  background-color: #795548;
}

.bottom-chart { margin-top: 12px; }
.stacked-chart { width: 100%; height: 320px; }
</style>
