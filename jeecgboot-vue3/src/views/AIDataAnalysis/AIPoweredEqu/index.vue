<template>
  <div class="ai-analysis-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <SettingFilled class="me-2" />
        设备选择
      </div>
      
      <!-- 设备筛选 -->
      <div class="p-3">
        <div class="filter-section-title">设备类型</div>
        <a-radio-group v-model:value="currentFilter" @change="filterDevices" class="device-filter-group">
          <div v-for="filter in deviceFilters" :key="filter.value" class="filter-item">
            <a-radio :value="filter.value">{{ filter.label }}</a-radio>
          </div>
        </a-radio-group>
      </div>
      
      <!-- 搜索框 -->
      <div class="p-3 border-top">
        <div class="filter-section-title">设备搜索</div>
        <a-input 
          v-model:value="searchKeyword"
          placeholder="搜索设备..."
          @input="searchDevices"
          size="default"
          style="height: 40px;"
        >
          <template #prefix>
            <SearchOutlined style="color: #666666" />
          </template>
        </a-input>
      </div>
      
      <!-- 设备列表 -->
      <div class="p-2">
        <div class="filter-section-title" style="margin-left: 0.5rem; margin-bottom: 0.5rem;">设备列表（可多选）</div>
        <div 
          v-for="(device, index) in filteredDevices" 
          :key="device.id"
          class="device-item"
          :class="{ active: selectedDevices.includes(device.id) }"
          @click="toggleDevice(device)"
        >
          <a-checkbox 
            :checked="selectedDevices.includes(device.id)"
            @click.stop
            @change="(e) => handleDeviceCheck(e.target.checked, device)"
            class="device-checkbox"
          />
          <span class="device-name">{{ device.name }}</span>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="main-content-inner">
        <!-- 面包屑导航 -->
        <a-breadcrumb class="breadcrumb">
          <a-breadcrumb-item style="padding-bottom: 15px;">
            <a href="#">AI智能分析</a>
          </a-breadcrumb-item>
          <a-breadcrumb-item>
            {{ selectedDevice?.name || '冰水机#01' }}
          </a-breadcrumb-item>
        </a-breadcrumb>

        <!-- 图表卡片 -->
        <div class="chart-card">
        <!-- 时间筛选条 -->
        <div class="time-filter-bar">
          <a-space wrap align="center" size="middle">
            <span class="filter-label">
              <CalendarOutlined class="me-1" />
              日期范围：
            </span>
            <a-range-picker 
              v-model:value="dateRange" 
              format="YYYY-MM-DD"
              style="min-width: 240px"
            />
            <a-button type="primary" @click="queryData" :loading="loading">
              <SearchOutlined class="me-1" />
              查询
            </a-button>
            <a-button type="primary" @click="openAIModal">
              <RobotOutlined class="me-1" />
              AI智能分析
            </a-button>
          </a-space>
        </div>
        
        <!-- 图表标题 -->
        <h6 class="chart-title">
          <BarChartOutlined class="me-2" />
          {{ chartTitle }}
        </h6>
        
        <!-- 图表容器 -->
        <div class="chart-container" ref="chartRef"></div>
      </div>

        <!-- 数据表格 -->
        <div class="table-responsive">
          <a-table 
            :columns="tableColumns" 
            :data-source="tableData" 
            :pagination="false"
            :show-header="true"
            class="data-table"
            :scroll="{ x: 1200 }"
            bordered
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'average'">
                <span class="highlight">{{ record.average }}</span>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>

    <!-- AI分析弹窗 -->
    <a-modal
      v-model:open="aiModalVisible"
      title=""
      width="1200px"
      :footer="null"
      class="ai-modal"
      :bodyStyle="{ padding: 0 }"
    >
      <template #title>
        <div class="modal-header-custom">
          <div class="modal-title-text">
            <RobotOutlined class="me-2" />
            AI智能分析
          </div>
          <a-button type="text" @click="aiModalVisible = false" class="close-btn">
            <CloseOutlined style="color: white; font-size: 16px;" />
          </a-button>
        </div>
      </template>
      
      <!-- 分析内容区域 -->
      <div class="modal-body-custom">
        <div v-if="aiLoading" class="loading-container">
          <a-spin size="large" />
          <p class="loading-text">AI正在分析中...</p>
        </div>
        <div v-else class="analysis-content" v-html="aiAnalysisContent"></div>
      </div>
      
      <!-- 底部操作区 -->
      <div class="modal-footer-custom">
        <div class="footer-left">
          <!-- 快捷问题区域 -->
          <div class="mb-3">
            <div class="footer-section-title">
              <MessageOutlined class="me-2" />
              常见问题快捷查询：
            </div>
            <div class="quick-questions">
              <a-button
                v-for="(question, index) in quickQuestions" 
                :key="question.text"
                size="small"
                class="question-btn"
                @click="askQuestion(question.text)"
              >
                <component :is="getQuestionIcon(question.text)" class="question-icon" />
                {{ question.text }}
              </a-button>
            </div>
          </div>
          
          <!-- 自定义提问区域 -->
          <div class="mb-3">
            <div class="footer-section-title">
              <EditOutlined class="me-2" />
              自定义提问：
            </div>
            <div class="custom-input-group">
              <a-input 
                v-model:value="customQuestion" 
                placeholder="请输入您的问题，例如：如何降低能耗？"
                class="custom-input"
                @pressEnter="askCustomQuestion"
              />
            </div>
          </div>
        </div>
        
        <!-- 右侧按钮组 -->
        <div class="footer-right">
          <a-button 
            type="primary" 
            @click="askCustomQuestion" 
            class="action-btn"
            :loading="aiLoading"
            size="large"
          >
            <SendOutlined class="me-1" />
            提问
          </a-button>
          <a-button 
            @click="aiModalVisible = false"
            class="action-btn"
            size="large"
          >
            关闭
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { 
  SettingFilled,
  CalendarOutlined,
  SearchOutlined,
  RobotOutlined,
  BarChartOutlined,
  MessageOutlined,
  EditOutlined,
  SendOutlined,
  ThunderboltOutlined,
  ToolOutlined,
  WarningOutlined,
  SettingOutlined,
  DollarOutlined,
  BulbOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import dayjs, { Dayjs } from 'dayjs'

// 响应式数据
const loading = ref(false)
const aiLoading = ref(false)
const aiModalVisible = ref(false)
const chartRef = ref<HTMLElement>()
const searchKeyword = ref('')
const currentFilter = ref('all')
const customQuestion = ref('')
const aiAnalysisContent = ref('')
const chartTitle = ref('冰水机组能耗统计（9月20日-30日）')

// 日期范围
const dateRange = ref<[Dayjs, Dayjs]>([
  dayjs('2025-09-20'),
  dayjs('2025-09-30')
])

// 设备筛选器
const deviceFilters = [
  { value: 'all', label: '全部设备' },
  { value: 'chiller', label: '冰水机组' },
  { value: 'central', label: '中央空调' }
]

// 设备列表
const devices = [
  { id: 1, name: '冰水机#01（冰机房）', type: 'chiller' },
  { id: 2, name: '冰水机#05（地暖房）', type: 'chiller' },
  { id: 3, name: '冰水机#08（3楼机房）', type: 'chiller' },
  { id: 4, name: '中央空调#07（N区）', type: 'central' }
]

const selectedDevice = ref(devices[0])
const selectedDevices = ref([devices[0].id]) // 支持多选
const filteredDevices = ref([...devices])

// 快捷问题
const quickQuestions = [
  { text: '如何降低能耗？', icon: 'ThunderboltOutlined' },
  { text: '设备维护建议', icon: 'ToolOutlined' },
  { text: '故障预警分析', icon: 'WarningOutlined' },
  { text: '运行参数优化', icon: 'SettingOutlined' },
  { text: '季节性调整方案', icon: 'ThunderboltOutlined' },
  { text: '成本效益分析', icon: 'DollarOutlined' }
]

// 获取问题对应的图标组件
const getQuestionIcon = (text: string) => {
  const iconMap: Record<string, any> = {
    '如何降低能耗？': ThunderboltOutlined,
    '设备维护建议': ToolOutlined,
    '故障预警分析': WarningOutlined,
    '运行参数优化': SettingOutlined,
    '季节性调整方案': ThunderboltOutlined,
    '成本效益分析': DollarOutlined
  }
  return iconMap[text] || ThunderboltOutlined
}

// 表格列配置
const tableColumns = [
  { title: '设备', dataIndex: 'device', key: 'device', fixed: 'left', width: 120 },
  { title: '9月20日', dataIndex: 'day20', key: 'day20', width: 80 },
  { title: '9月21日', dataIndex: 'day21', key: 'day21', width: 80 },
  { title: '9月22日', dataIndex: 'day22', key: 'day22', width: 80 },
  { title: '9月23日', dataIndex: 'day23', key: 'day23', width: 80 },
  { title: '9月24日', dataIndex: 'day24', key: 'day24', width: 80 },
  { title: '9月25日', dataIndex: 'day25', key: 'day25', width: 80 },
  { title: '9月26日', dataIndex: 'day26', key: 'day26', width: 80 },
  { title: '9月27日', dataIndex: 'day27', key: 'day27', width: 80 },
  { title: '9月28日', dataIndex: 'day28', key: 'day28', width: 80 },
  { title: '9月29日', dataIndex: 'day29', key: 'day29', width: 80 },
  { title: '9月30日', dataIndex: 'day30', key: 'day30', width: 80 },
  { title: '平均值', dataIndex: 'average', key: 'average', width: 80 }
]

// 表格数据
const tableData = ref([
  {
    key: '1',
    device: '冰水机A',
    day20: 420, day21: 435, day22: 465, day23: 440, day24: 380,
    day25: 395, day26: 410, day27: 425, day28: 440, day29: 455, day30: 470,
    average: '430.5'
  },
  {
    key: '2',
    device: '冰水机B',
    day20: 280, day21: 295, day22: 310, day23: 330, day24: 350,
    day25: 365, day26: 380, day27: 395, day28: 410, day29: 425, day30: 440,
    average: '361.8'
  },
  {
    key: '3',
    device: '冰水机C',
    day20: 250, day21: 265, day22: 280, day23: 295, day24: 310,
    day25: 325, day26: 340, day27: 355, day28: 370, day29: 385, day30: 400,
    average: '325.0'
  }
])

let chart: echarts.ECharts | null = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  
  const dates = ['20日', '21日', '22日', '23日', '24日', '25日', '26日', '27日', '28日', '29日', '30日']
  const dataA = [420, 435, 465, 440, 380, 395, 410, 425, 440, 455, 470]
  const dataB = [280, 295, 310, 330, 350, 365, 380, 395, 410, 425, 440]
  const dataC = [250, 265, 280, 295, 310, 325, 340, 355, 370, 385, 400]

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['冰水机A', '冰水机B', '冰水机C'],
      top: 10
    },
    grid: {
      left: '3%', right: '4%', bottom: '3%', top: '15%',
      containLabel: true
    },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '能耗(kWh)' },
    series: [
      {
        name: '冰水机A', type: 'bar', data: dataA,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#69b7ff' },
            { offset: 1, color: '#4a90e2' }
          ])
        },
        barWidth: '20%'
      },
      {
        name: '冰水机B', type: 'bar', data: dataB,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#95de64' },
            { offset: 1, color: '#7cb342' }
          ])
        },
        barWidth: '20%'
      },
      {
        name: '冰水机C', type: 'bar', data: dataC,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffd666' },
            { offset: 1, color: '#ffa726' }
          ])
        },
        barWidth: '20%'
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart?.resize())
}

// 过滤设备
const filterDevices = () => {
  let filtered = devices
  if (currentFilter.value !== 'all') {
    filtered = devices.filter(device => device.type === currentFilter.value)
  }
  if (searchKeyword.value) {
    filtered = filtered.filter(device => 
      device.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  filteredDevices.value = filtered
}

const searchDevices = () => filterDevices()
const selectDevice = (device: any) => { selectedDevice.value = device }

// 新增多选支持方法
const toggleDevice = (device: any) => {
  const index = selectedDevices.value.indexOf(device.id)
  if (index > -1) {
    selectedDevices.value.splice(index, 1)
  } else {
    selectedDevices.value.push(device.id)
  }
  // 更新当前选中设备为最后一个选中的
  if (selectedDevices.value.length > 0) {
    const lastSelectedId = selectedDevices.value[selectedDevices.value.length - 1]
    const foundDevice = devices.find(d => d.id === lastSelectedId)
    if (foundDevice) {
      selectedDevice.value = foundDevice
    }
  }
}

const handleDeviceCheck = (checked: boolean, device: any) => {
  if (checked) {
    if (!selectedDevices.value.includes(device.id)) {
      selectedDevices.value.push(device.id)
    }
  } else {
    const index = selectedDevices.value.indexOf(device.id)
    if (index > -1) {
      selectedDevices.value.splice(index, 1)
    }
  }
  // 更新当前选中设备
  if (selectedDevices.value.length > 0) {
    const lastSelectedId = selectedDevices.value[selectedDevices.value.length - 1]
    const foundDevice = devices.find(d => d.id === lastSelectedId)
    if (foundDevice) {
      selectedDevice.value = foundDevice
    }
  }
}

// 查询数据
const queryData = async () => {
  loading.value = true
  
  try {
    // 显示加载状态
    if (chart) {
      chart.showLoading({
        text: '数据加载中...',
        color: '#4a7bc8',
        textColor: '#000',
        maskColor: 'rgba(255, 255, 255, 0.8)',
        zlevel: 0
      })
    }
    
    // 模拟数据加载延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新图表标题
    if (dateRange.value && dateRange.value.length === 2) {
      const startDate = dateRange.value[0].format('M月D日')
      const endDate = dateRange.value[1].format('M月D日')
      chartTitle.value = `冰水机组能耗统计（${startDate}-${endDate}）`
    }
    
    // 生成随机数据（模拟不同时间段的数据）
    const randomData = () => Array.from({ length: 11 }, () => Math.floor(Math.random() * 200 + 250))
    const dates = ['20日', '21日', '22日', '23日', '24日', '25日', '26日', '27日', '28日', '29日', '30日']
    const dataA = randomData()
    const dataB = randomData()
    const dataC = randomData()
    
    // 更新图表数据
    if (chart) {
      chart.hideLoading()
      chart.setOption({
        xAxis: { type: 'category', data: dates },
        series: [
          {
            name: '冰水机A',
            type: 'bar',
            data: dataA,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#69b7ff' },
                { offset: 1, color: '#4a90e2' }
              ])
            },
            barWidth: '20%'
          },
          {
            name: '冰水机B',
            type: 'bar',
            data: dataB,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#95de64' },
                { offset: 1, color: '#7cb342' }
              ])
            },
            barWidth: '20%'
          },
          {
            name: '冰水机C',
            type: 'bar',
            data: dataC,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#ffd666' },
                { offset: 1, color: '#ffa726' }
              ])
            },
            barWidth: '20%'
          }
        ]
      })
    }
    
    // 更新表格数据
    tableData.value = [
      {
        key: '1',
        device: '冰水机A',
        day20: dataA[0], day21: dataA[1], day22: dataA[2], day23: dataA[3], day24: dataA[4],
        day25: dataA[5], day26: dataA[6], day27: dataA[7], day28: dataA[8], day29: dataA[9], day30: dataA[10],
        average: (dataA.reduce((a, b) => a + b, 0) / dataA.length).toFixed(1)
      },
      {
        key: '2',
        device: '冰水机B',
        day20: dataB[0], day21: dataB[1], day22: dataB[2], day23: dataB[3], day24: dataB[4],
        day25: dataB[5], day26: dataB[6], day27: dataB[7], day28: dataB[8], day29: dataB[9], day30: dataB[10],
        average: (dataB.reduce((a, b) => a + b, 0) / dataB.length).toFixed(1)
      },
      {
        key: '3',
        device: '冰水机C',
        day20: dataC[0], day21: dataC[1], day22: dataC[2], day23: dataC[3], day24: dataC[4],
        day25: dataC[5], day26: dataC[6], day27: dataC[7], day28: dataC[8], day29: dataC[9], day30: dataC[10],
        average: (dataC.reduce((a, b) => a + b, 0) / dataC.length).toFixed(1)
      }
    ]
  } finally {
    loading.value = false
  }
}

// 打开AI分析弹窗
const openAIModal = async () => {
  aiModalVisible.value = true
  await showDefaultAnalysis()
}

const showDefaultAnalysis = async () => {
  aiLoading.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    aiLoading.value = false
    aiAnalysisContent.value = ''
    
    // 等待DOM更新
    await nextTick()
    
    const container = document.querySelector('.analysis-content') as HTMLElement
    if (!container) return
    
    container.innerHTML = ''
    
    // 第一部分：标题
    await typeSection(container, '<div class="section-title">一、运行状况分析</div>')
    
    // 第二部分：引言
    await typeSection(container, '<div class="analysis-text"><p>基于楼栋数据，冰水机系统的主要运行指标如下：</p></div>')
    
    // 第三部分：列表项逐条显示
    const ul = document.createElement('ul')
    ul.className = 'analysis-text'
    const lastElement = container.lastElementChild as HTMLElement
    if (lastElement) {
      lastElement.appendChild(ul)
    }
    
    const items = [
      '<strong>负载率波动</strong>：冰机平均负载率为<span class="highlight-number">75%</span>，但高峰时段负载持续超过<span class="highlight-number">90%</span>，供水主管温度频繁超出设定值，表明高峰时段制冷能力不足。',
      '<strong>能效比下降</strong>：系统COP从<span class="highlight-number">4.2</span>降至<span class="highlight-number">3.5</span>，主要因冷凝器结垢和压缩机磨损。',
      '<strong>设备健康状态</strong>：冷凝器温差扩大，压缩机振动值超标。',
      '<strong>季节性影响</strong>：冬季能耗降低<span class="highlight-number">30%</span>，夏季制冷量下降<span class="highlight-number">20%</span>。'
    ]
    
    for (let item of items) {
      const li = document.createElement('li')
      ul.appendChild(li)
      await typeHTML(li, item, 15)
      await new Promise(r => setTimeout(r, 300))
    }
    
    // 第四部分：第二个标题
    await new Promise(r => setTimeout(r, 400))
    await typeSection(container, '<div class="section-title">二、节能措施</div>')
    
    // 第五部分：节能措施列表
    const ul2 = document.createElement('ul')
    ul2.className = 'analysis-text'
    container.appendChild(ul2)
    
    const items2 = [
      '<strong>优化运行策略</strong>：高峰时段启动备用机组，根据负载动态调整运行台数',
      '<strong>定期维护保养</strong>：每季度清洗冷凝器，保持良好的换热效率',
      '<strong>智能控制系统</strong>：引入AI预测算法，提前调整运行参数',
      '<strong>能效监控</strong>：建立实时监控系统，及时发现异常情况'
    ]
    
    for (let item of items2) {
      const li = document.createElement('li')
      ul2.appendChild(li)
      await typeHTML(li, item, 15)
      await new Promise(r => setTimeout(r, 300))
    }
  } catch (error) {
    console.error('AI分析显示错误:', error)
    aiLoading.value = false
  }
}

// 打字机效果 - 显示标题和段落
const typeSection = async (parent: HTMLElement, html: string) => {
  const temp = document.createElement('div')
  temp.innerHTML = html
  const element = temp.firstElementChild as HTMLElement
  parent.appendChild(element)
  
  const text = element.textContent || ''
  element.textContent = ''
  
  for (let i = 0; i < text.length; i++) {
    element.textContent += text[i]
    await new Promise(r => setTimeout(r, 30))
  }
}

// 打字机效果 - 逐字显示HTML内容
const typeHTML = async (element: HTMLElement, html: string, speed = 15) => {
  const temp = document.createElement('div')
  temp.innerHTML = html
  
  const nodes = Array.from(temp.childNodes)
  for (let node of nodes) {
    if (node.nodeType === 3) {
      // 文本节点 - 逐字显示
      const text = node.textContent || ''
      for (let char of text) {
        element.appendChild(document.createTextNode(char))
        await new Promise(r => setTimeout(r, speed))
      }
    } else if (node.nodeType === 1) {
      // 元素节点 - 创建元素后递归显示内容
      const newEl = document.createElement((node as Element).tagName)
      const attrs = (node as Element).attributes
      for (let i = 0; i < attrs.length; i++) {
        newEl.setAttribute(attrs[i].name, attrs[i].value)
      }
      element.appendChild(newEl)
      await typeHTML(newEl, (node as Element).innerHTML, speed)
    }
  }
}

// 问题答案映射
const questionAnswers: Record<string, string[]> = {
  '如何降低能耗？': [
    '<strong>负载优化</strong>：根据实际需求动态调整运行机组数量',
    '<strong>温度设定</strong>：适当提高供水温度设定值',
    '<strong>变频控制</strong>：采用变频技术调节压缩机转速',
    '<strong>冷却塔优化</strong>：保持冷却水温度在合理范围'
  ],
  '设备维护建议': [
    '<strong>日常检查</strong>：每日检查运行参数和异常情况',
    '<strong>月度保养</strong>：检查制冷剂压力、清洗过滤器',
    '<strong>季度维护</strong>：清洗冷凝器和蒸发器',
    '<strong>年度大修</strong>：全面检测系统性能'
  ],
  '故障预警分析': [
    '<strong>压缩机异常</strong>：振动值超标需立即检修',
    '<strong>制冷效率下降</strong>：COP下降表明换热器结垢',
    '<strong>温度波动</strong>：可能是控制系统故障',
    '<strong>能耗异常</strong>：需检查制冷剂泄漏'
  ],
  '运行参数优化': [
    '<strong>供水温度</strong>：建议设定在7-8°C',
    '<strong>冷却水温度</strong>：控制在32-37°C',
    '<strong>流量调节</strong>：采用变流量控制',
    '<strong>压力控制</strong>：维持合理压力差值'
  ],
  '季节性调整方案': [
    '<strong>夏季策略</strong>：增加运行机组，延长预冷时间',
    '<strong>冬季策略</strong>：利用自然冷源，减少运行时间',
    '<strong>过渡季节</strong>：灵活调整运行模式',
    '<strong>湿度控制</strong>：根据季节调整除湿策略'
  ],
  '成本效益分析': [
    '<strong>节能收益</strong>：优化运行可降低能耗15-25%',
    '<strong>维护成本</strong>：定期保养延长设备寿命',
    '<strong>投资回报</strong>：变频改造回收期约2-3年',
    '<strong>综合效益</strong>：提升系统可靠性'
  ]
}

const askQuestion = async (question: string) => {
  aiLoading.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    aiLoading.value = false
    aiAnalysisContent.value = ''
    
    await nextTick()
    
    const container = document.querySelector('.analysis-content') as HTMLElement
    if (!container) return
    
    container.innerHTML = ''
    
    // 显示问题标题
    await typeSection(container, `<div class="section-title">${question}</div>`)
    
    // 获取对应答案
    const answers = questionAnswers[question] || [
      `<strong>问题理解</strong>：您询问的是"${question}"，这是一个很好的问题。`,
      '<strong>数据分析</strong>：根据历史运行数据和当前系统状态进行综合分析。',
      '<strong>专业建议</strong>：建议采取针对性的优化措施，包括参数调整和设备维护。',
      '<strong>预期效果</strong>：实施建议措施后，预计可改善系统性能并降低运行成本。'
    ]
    
    // 创建列表容器
    const ul = document.createElement('ul')
    ul.className = 'analysis-text'
    container.appendChild(ul)
    
    // 逐条显示答案
    for (let answer of answers) {
      const li = document.createElement('li')
      ul.appendChild(li)
      await typeHTML(li, answer, 15)
      await new Promise(r => setTimeout(r, 300))
    }
  } catch (error) {
    console.error('问题回答显示错误:', error)
    aiLoading.value = false
  }
}

const askCustomQuestion = async () => {
  if (!customQuestion.value.trim()) {
    return
  }
  const question = customQuestion.value
  customQuestion.value = ''
  await askQuestion(question)
}

// 导出分析报告
const exportAnalysis = () => {
  // 模拟导出功能
  console.log('导出分析报告')
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
:root {
  --primary-blue: #2c5aa0;
  --light-blue: #4a7bc8;
  --lighter-blue: #e8f1ff;
  --hover-blue: #1e4278;
}

.ai-analysis-container {
  display: flex;
  height: 100vh;
  font-family: "Microsoft YaHei", Arial, sans-serif;
  background: #f8f9fa;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 1.2rem 1.2rem 1.2rem 1.5rem;
  background: linear-gradient(135deg, #0050b3, #1890ff);
  color: white;
  font-weight: 600;
  border-radius: 0 0 8px 8px;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.device-item {
  padding: 0.8rem;
  margin: 0.5rem;
  background: #ffffff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #262626;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-checkbox {
  flex-shrink: 0;
}

.device-name {
  flex: 1;
  user-select: none;
}

.device-item:hover {
  background: linear-gradient(135deg, #e8f1ff, #f0f7ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #1890ff;
}

.device-item.active {
  background: linear-gradient(135deg, #0050b3, #1890ff);
  color: white;
  border-color: #0050b3;
  box-shadow: 0 4px 12px rgba(0, 80, 179, 0.3);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  height: 100vh;
}

.main-content-inner {
  padding: 1.5rem;
  padding-top: 1.2rem;
  
}

.chart-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.time-filter-bar {
  background: linear-gradient(135deg, #e8f1ff, #f0f7ff);
  padding: 1.2rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  border: 1px solid #d9e9ff;
}

.chart-title {
  font-size: 17px;
  font-weight: 600;
  margin: 1rem 0;
  color: var(--primary-blue);
}

.chart-container {
  height: 400px;
  width: 100%;
}

.data-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.data-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, var(--primary-blue), var(--light-blue)) !important;
  color: rgb(15, 15, 15) !important;
  font-weight: 600;
  border: none !important;
  height: 48px;
  line-height: 1.5;
  font-size: 14px;
  text-align: center;
}

.data-table :deep(.ant-table-thead) {
  display: table-header-group !important;
}

.data-table :deep(.ant-table-thead > tr) {
  display: table-row !important;
}

.data-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 16px;
  text-align: center;
}

.data-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f8f9fa;
}

.highlight {
  color: #dc3545 !important;
  font-weight: 600 !important;
}

/* AI弹窗样式 - 参考DEMO2布局 */
.ai-modal :deep(.ant-modal-content) {
  border-radius: 8px !important;
  overflow: hidden !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3) !important;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.ai-modal :deep(.ant-modal-body) {
  padding: 0 !important;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.ai-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #2c5aa0, #4a7bc8) !important;
  border-bottom: none !important;
  padding: 0 !important;
  border-radius: 8px 8px 0 0 !important;
}

.ai-modal :deep(.ant-modal-title) {
  color: white !important;
}

.modal-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem 1.5rem;
  color: white !important;
  background: transparent;
}

.modal-title-text {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  color: white !important;
}

.modal-title-text :deep(.anticon) {
  font-size: 20px;
  margin-right: 8px;
  color: white !important;
}

.close-btn {
  background: none !important;
  border: none !important;
  box-shadow: none !important;
  color: white !important;
  padding: 4px 8px;
  height: auto;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.15) !important;
}

.close-btn :deep(.anticon) {
  color: white;
  font-size: 16px;
}

.modal-body-custom {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
  min-height: 300px;
  max-height: calc(90vh - 300px);
  background: white;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.loading-text {
  margin-top: 16px;
  color: #555;
  font-size: 16px;
}

.analysis-content {
  line-height: 1.8;
  color: #555;
  min-height: 200px;
}

.analysis-content :deep(.section-title) {
  font-size: 18px;
  font-weight: 600;
  margin: 1.5rem 0 1rem;
  color: var(--primary-blue);
  display: flex;
  align-items: center;
}

.analysis-content :deep(.section-title:first-child) {
  margin-top: 0;
}

.analysis-content :deep(.analysis-text) {
  line-height: 1.8;
}

.analysis-content :deep(.analysis-text p) {
  margin-bottom: 1rem;
  color: #555;
  font-size: 15px;
  line-height: 1.8;
}

.analysis-content :deep(.analysis-text ul) {
  margin: 1rem 0;
  padding-left: 1.5rem;
  list-style-type: disc;
}

.analysis-content :deep(.analysis-text li) {
  margin-bottom: 1rem;
  color: #555;
  font-size: 15px;
  line-height: 1.8;
}

.analysis-content :deep(.analysis-text strong) {
  color: #262626;
  font-weight: 600;
}

.analysis-content :deep(.highlight-number) {
  color: #ff4d4f;
  font-weight: 600;
  font-size: 1.1em;
}

/* 底部区域标题样式 */
.footer-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
}

.footer-section-title :deep(.anticon) {
  color: var(--primary-blue);
  font-size: 16px;
}

/* 快捷问题样式 */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.question-btn {
  font-size: 13px;
  padding: 6px 14px;
  height: auto;
  border-radius: 20px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  border: 1px solid #d9d9d9;
}

.question-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-color: #4a7bc8;
}

.question-icon {
  margin-right: 4px;
  font-size: 13px;
}

/* 自定义提问输入组 */
.custom-input-group {
  display: flex;
}

.custom-input {
  width: 100%;
}

/* 底部样式 */
.modal-footer-custom {
  padding: 1.2rem 1.5rem;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
  display: flex;
  gap: 1.5rem;
}

.footer-left {
  flex: 1;
}

.footer-right {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  justify-content: flex-end;
  min-width: 100px;
}

.action-btn {
  width: 100%;
  font-weight: 500;
}

.mb-3 {
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .ai-analysis-container {
    flex-direction: column;
    height: auto;
  }
  
  .sidebar {
    width: 100%;
    order: 2;
  }
  
  .main-content {
    order: 1;
  }
  
  .ai-modal {
    width: 95% !important;
    margin: 10px auto;
  }
  
  .ai-modal :deep(.ant-modal-content) {
    margin: 0;
  }
  
  .quick-questions {
    gap: 6px;
  }
  
  .question-btn {
    font-size: 12px;
    padding: 4px 10px;
  }
}
</style>

<style>
/* 全局样式 - 确保Modal标题栏背景色生效 */
.ai-modal .ant-modal-header {
  background: linear-gradient(135deg, #2c5aa0, #4a7bc8) !important;
  border-bottom: none !important;
  padding: 0 !important;
  border-radius: 8px 8px 0 0 !important;
}

.ai-modal .ant-modal-title {
  color: white !important;
}

.ai-modal .ant-modal-content {
  border-radius: 8px !important;
  overflow: hidden !important;
}
</style>
