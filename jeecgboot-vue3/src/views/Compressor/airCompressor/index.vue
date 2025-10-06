<template>
  <div class="page-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <a-button type="text" @click="toggleSidebar" class="sidebar-toggle">
          <template #icon>
            <MenuOutlined />
          </template>
        </a-button>
        <span v-if="!sidebarCollapsed" class="sidebar-title">空压机系统结构</span>
      </div>
      
      <div class="sidebar-content">
        <div class="nav-group" v-for="group in systemGroups" :key="group.key">
          <div class="group-title" @click="toggleGroup(group.key)" :class="{ expanded: group.expanded }">
            <span v-if="!sidebarCollapsed">{{ group.title }}</span>
          </div>
          <div v-if="group.expanded && !sidebarCollapsed" class="group-items">
            <div 
              v-for="item in group.items" 
              :key="item.key"
              class="nav-item"
              :class="{ active: activeNavItem === item.key }"
              @click="selectNavItem(item.key)"
            >
              {{ item.title }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部统计卡片 -->
      <div class="stats-section">
        <a-row :gutter="[16, 16]">
          <a-col :span="6" v-for="stat in statsData" :key="stat.label">
            <div class="stat-card">
              <div class="stat-content">
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-value" :style="{ color: stat.valueColor || '#1677ff' }">{{ stat.value }}</div>
                <div class="stat-unit">{{ stat.unit }}</div>
                <div class="stat-compare">
                  <span class="compare-label">{{ stat.compareLabel }}</span>
                  <span class="compare-value">{{ stat.compareValue }}</span>
                  <span class="compare-trend" :class="stat.trend">{{ stat.trendText }}</span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <div class="chart-card">
              <div class="card-title">设备运行状态分布</div>
              <div ref="statusPieRef" class="chart-container"></div>
            </div>
          </a-col>
          <a-col :span="10">
            <div class="chart-card">
              <div class="card-title">功率消耗趋势</div>
              <div ref="powerTrendRef" class="chart-container"></div>
            </div>
          </a-col>
          <a-col :span="6">
            <div class="chart-card">
              <div class="card-title">实时报警</div>
              <div class="alarm-container">
                <div v-if="alarmList.length === 0" class="alarm-empty">当前无报警</div>
                <div v-else class="alarm-list">
                  <div v-for="alarm in alarmList" :key="alarm.id + alarm.type" class="alarm-item">
                    <div class="alarm-dot" :style="{ backgroundColor: alarm.color }"></div>
                    <div class="alarm-content">
                      <div class="alarm-title">{{ alarm.id }} - {{ alarm.type }}</div>
                      <div class="alarm-meta">
                        <span class="alarm-level">级别：{{ alarm.level }}</span>
                        <span class="alarm-time">时间：{{ alarm.time }}</span>
                      </div>
                      <div v-if="alarm.value" class="alarm-value">当前值：{{ alarm.value }}</div>
                    </div>
                    <div class="alarm-action">
                      <a-button type="primary" size="small" @click="showDetail(getDeviceById(alarm.id))">
                        查看
                      </a-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 设备详情表格 -->
      <div class="table-section">
        <div class="table-card">
          <div class="card-title">空压机设备详细参数</div>
          <a-table 
            :columns="columns" 
            :data-source="deviceData" 
            :pagination="false"
            :scroll="{ x: 1200 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">{{ record.status }}</a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="primary" size="small" @click="showDetail(record)">查看</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>

    <!-- 设备详情弹窗 -->
    <a-modal v-model:open="modalVisible" :title="modalTitle" width="1200px">
      <div v-if="currentDevice" class="device-modal">
        <a-row :gutter="[24, 24]">
          <a-col :span="10">
            <div class="device-image-section">
              <img src="/src/assets/images/空压机.png" alt="空压机" class="device-image" />
            </div>
          </a-col>
          <a-col :span="14">
            <a-descriptions :column="2" bordered size="small">
              <a-descriptions-item label="设备编号">{{ currentDevice.id }}</a-descriptions-item>
              <a-descriptions-item label="设备类型">{{ currentDevice.type }}</a-descriptions-item>
              <a-descriptions-item label="型号" :span="2">{{ currentDevice.model }}</a-descriptions-item>
              <a-descriptions-item label="排气量">{{ currentDevice.flow }} m³/min</a-descriptions-item>
              <a-descriptions-item label="排气压力">{{ currentDevice.pressure }} bar</a-descriptions-item>
              <a-descriptions-item label="排气温度">{{ currentDevice.temp }} °C</a-descriptions-item>
              <a-descriptions-item label="功率">{{ currentDevice.power }} kW</a-descriptions-item>
              <a-descriptions-item label="运行状态">
                <a-tag :color="getStatusColor(currentDevice.status)">{{ currentDevice.status }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="效率">{{ currentDevice.efficiency }}%</a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        
        <div class="energy-chart-section">
          <h4 style="margin-left: 16px;">能耗分析</h4>
          <div ref="energyChartRef" class="energy-chart"></div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup name="air-compressor">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { MenuOutlined } from '@ant-design/icons-vue';
import * as echarts from 'echarts';

// 侧边栏相关状态
const sidebarCollapsed = ref(false);
const activeNavItem = ref('A-compressors');
const systemGroups = ref([
  {
    key: 'A',
    title: 'A厂房空压机系统',
    expanded: true,
    items: [
      { key: 'A-compressors', title: '空压机组' },
      { key: 'A-dryers', title: '干燥器/过滤器' },
      { key: 'A-pipelines', title: '管网与储气罐' }
    ]
  },
  {
    key: 'B',
    title: 'B厂房空压机系统',
    expanded: true,
    items: [
      { key: 'B-compressors', title: '空压机组' },
      { key: 'B-dryers', title: '干燥器/过滤器' },
      { key: 'B-pipelines', title: '管网与储气罐' }
    ]
  }
]);

const statsData = ref([
  { 
    label: '总排气量', 
    value: '156.8', 
    unit: 'm³/min', 
    compareLabel: '昨日', 
    compareValue: '148.2', 
    trend: 'trend-up', 
    trendText: '+5.8%',
    valueColor: '#1677ff'
  },
  { 
    label: '总功率', 
    value: '452', 
    unit: 'kW', 
    compareLabel: '昨日最大', 
    compareValue: '438', 
    trend: 'trend-up', 
    trendText: '+3.2%',
    valueColor: '#1677ff'
  },
  { 
    label: '运行效率', 
    value: '87.5', 
    unit: '%', 
    compareLabel: '昨日最大', 
    compareValue: '85.2', 
    trend: 'trend-up', 
    trendText: '+2.7%',
    valueColor: '#1677ff'
  },
  { 
    label: '整体能耗', 
    value: '3,240', 
    unit: 'kWh', 
    compareLabel: '昨日', 
    compareValue: '3,156', 
    trend: 'trend-up', 
    trendText: '+2.7%',
    valueColor: '#1677ff'
  }
]);

const deviceData = ref([
  { id: 'AC-001', type: '螺杆式', model: 'ATLAS-COPCO-GA22', flow: 3.7, pressure: 8, temp: 92, power: 22, status: '运行', efficiency: 89.2 },
  { id: 'AC-002', type: '螺杆式', model: 'ATLAS-COPCO-GA37', flow: 6.2, pressure: 8, temp: 88, power: 37, status: '运行', efficiency: 87.5 },
  { id: 'AC-003', type: '活塞式', model: 'INGERSOLL-RAND-UP6', flow: 1.8, pressure: 10, temp: 96, power: 11, status: '运行', efficiency: 85.3 },
  { id: 'AC-004', type: '螺杆式', model: 'ATLAS-COPCO-GA55', flow: 9.8, pressure: 8, temp: 102, power: 55, status: '待机', efficiency: 0 },
  { id: 'AC-005', type: '活塞式', model: 'INGERSOLL-RAND-UP15', flow: 4.2, pressure: 10, temp: 91, power: 15, status: '运行', efficiency: 88.1 },
  { id: 'AC-006', type: '螺杆式', model: 'ATLAS-COPCO-GA75', flow: 13.1, pressure: 8, temp: 95, power: 75, status: '运行', efficiency: 90.2 },
  { id: 'AC-007', type: '螺杆式', model: 'ATLAS-COPCO-GA110', flow: 19.2, pressure: 8, temp: 99, power: 110, status: '运行', efficiency: 91.5 },
  { id: 'AC-008', type: '活塞式', model: 'INGERSOLL-RAND-UP30', flow: 8.5, pressure: 10, temp: 104, power: 30, status: '维护', efficiency: 0 },
  { id: 'AC-009', type: '螺杆式', model: 'ATLAS-COPCO-GA132', flow: 22.5, pressure: 8, temp: 93, power: 132, status: '运行', efficiency: 92.1 },
  { id: 'AC-010', type: '螺杆式', model: 'ATLAS-COPCO-GA160', flow: 27.8, pressure: 8, temp: 97, power: 160, status: '运行', efficiency: 88.7 }
]);

const columns = [
  { title: '设备编号', dataIndex: 'id', key: 'id', width: 120 },
  { title: '设备类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '型号', dataIndex: 'model', key: 'model', width: 180 },
  { title: '排气量(m³/min)', dataIndex: 'flow', key: 'flow', width: 120 },
  { title: '排气压力(bar)', dataIndex: 'pressure', key: 'pressure', width: 120 },
  { title: '排气温度(°C)', dataIndex: 'temp', key: 'temp', width: 120 },
  { title: '功率(kW)', dataIndex: 'power', key: 'power', width: 100 },
  { title: '运行状态', key: 'status', width: 100 },
  { title: '效率(%)', dataIndex: 'efficiency', key: 'efficiency', width: 100 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' }
];

const alarmList = ref<any[]>([]);

// 侧边栏交互方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

const toggleGroup = (groupKey: string) => {
  const group = systemGroups.value.find(g => g.key === groupKey);
  if (group) {
    group.expanded = !group.expanded;
  }
};

const selectNavItem = (itemKey: string) => {
  activeNavItem.value = itemKey;
  // 这里可以添加根据导航项筛选设备的逻辑
};

const getDeviceById = (deviceId: string) => {
  return deviceData.value.find(device => device.id === deviceId);
};
const modalVisible = ref(false);
const modalTitle = ref('');
const currentDevice = ref<any>(null);
const statusPieRef = ref<HTMLElement>();
const powerTrendRef = ref<HTMLElement>();
const energyChartRef = ref<HTMLElement>();
let statusPieChart: echarts.ECharts | null = null;
let powerTrendChart: echarts.ECharts | null = null;
let energyChart: echarts.ECharts | null = null;
let alarmTimer: any = null;

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = { '运行': 'success', '待机': 'warning', '维护': 'error' };
  return colorMap[status] || 'default';
};

const generateAlarms = () => {
  const list: any[] = [];
  const types = [
    { name: '过滤器堵塞', level: '低', color: '#1677ff' },
    { name: '电机故障', level: '高', color: '#ff6b6b' },
    { name: '润滑油位过低', level: '中', color: '#ffa940' }
  ];
  
  // 根据图片显示的报警信息
  const fixedAlarms = [
    {
      id: 'AC-005',
      type: '过滤器堵塞',
      level: '低',
      color: '#1677ff',
      time: '15:15:45',
      value: '72%堵塞'
    },
    {
      id: 'AC-007',
      type: '电机故障',
      level: '高',
      color: '#ff6b6b',
      time: '15:15:45',
      value: ''
    },
    {
      id: 'AC-010',
      type: '电机故障',
      level: '高',
      color: '#ff6b6b',
      time: '15:15:45',
      value: ''
    }
  ];
  
  alarmList.value = fixedAlarms;
};

const initStatusPie = () => {
  if (!statusPieRef.value) return;
  statusPieChart = echarts.init(statusPieRef.value);
  
  // 根据图片显示的状态分布：运行8台(80.0%)，待机1台(10.0%)，维护1台(10.0%)
  const statusData = [
    { name: '运行', value: 8, color: '#2fc59e' },
    { name: '待机', value: 1, color: '#ffa940' },
    { name: '维护', value: 1, color: '#ff6b6b' }
  ];
  
  statusPieChart.setOption({
    backgroundColor: 'transparent',
    color: ['#2fc59e', '#ffa940', '#ff6b6b'],
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c}台 ({d}%)'
    },
    legend: { 
      bottom: 0,
      formatter: function(name: string) {
        const item = statusData.find(d => d.name === name);
        if (!item) return name;
        const total = statusData.reduce((sum, d) => sum + d.value, 0);
        const percentage = ((item.value / total) * 100).toFixed(1);
        return `${name} (${item.value}台, ${percentage}%)`;
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: {
        borderRadius: 6,
        borderColor: '#ffffff',
        borderWidth: 2
      },
      label: { show: false },
      data: statusData
    }]
  });
};

const initPowerTrend = () => {
  if (!powerTrendRef.value) return;
  powerTrendChart = echarts.init(powerTrendRef.value);
  
  // 根据图片生成符合趋势的数据
  const hours = ['0:00', '4:00', '8:00', '12:00', '16:00', '20:00', '24:00'];
  const powerData = [400, 380, 410, 350, 430, 380, 390];
  
  powerTrendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis',
      formatter: '{b}: {c}kW'
    },
    grid: {
      left: 40,
      right: 20,
      bottom: 40,
      top: 20
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#dfe6f0' } },
      axisLabel: { color: '#6b778c' }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 500,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eef2f7' } },
      axisLabel: { color: '#6b778c' }
    },
    series: [{
      type: 'line',
      data: powerData,
      smooth: true,
      lineStyle: { color: '#1677ff', width: 3 },
      itemStyle: { color: '#1677ff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(22,119,255,0.3)' },
            { offset: 1, color: 'rgba(22,119,255,0.05)' }
          ]
        }
      }
    }]
  });
};

const initEnergyChart = (device: any) => {
  if (!energyChartRef.value) return;
  energyChart = echarts.init(energyChartRef.value);
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`);
  const todayData = Array.from({ length: 24 }, () => Math.round((device.power * 0.8 + Math.random() * device.power * 0.3) * 10) / 10);
  const yesterdayData = Array.from({ length: 24 }, () => Math.round((device.power * 0.75 + Math.random() * device.power * 0.25) * 10) / 10);
  energyChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['今日能耗', '昨日能耗'] },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '能耗 (kWh)' },
    series: [
      { name: '今日能耗', type: 'bar', data: todayData },
      { name: '昨日能耗', type: 'line', data: yesterdayData }
    ]
  });
};

const showDetail = (device: any) => {
  currentDevice.value = device;
  modalTitle.value = `${device.id} 设备详情`;
  modalVisible.value = true;
  nextTick(() => initEnergyChart(device));
};

watch(modalVisible, (val) => {
  if (!val && energyChart) {
    energyChart.dispose();
    energyChart = null;
  }
});

onMounted(() => {
  nextTick(() => {
    initStatusPie();
    initPowerTrend();
    generateAlarms();
  });
  // 启动定时刷新报警（可选）
  // alarmTimer = setInterval(generateAlarms, 30000); // 30秒刷新一次
  
  window.addEventListener('resize', () => {
    statusPieChart?.resize();
    powerTrendChart?.resize();
    energyChart?.resize();
  });
});

onUnmounted(() => {
  if (alarmTimer) clearInterval(alarmTimer);
  statusPieChart?.dispose();
  powerTrendChart?.dispose();
  energyChart?.dispose();
});
</script>

<style scoped>
/* 页面容器 */
.page-container {
  display: flex;
  height: calc(100vh - 64px);
  background: #f5f6fa;
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background: #ffffff;
  border: 2px solid #e8e9ea;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin: 16px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
}

.sidebar.collapsed {
  width: 56px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #e8e9ea;
  background: linear-gradient(90deg, rgba(22,119,255,0.1), rgba(26,198,255,0.06));
  border-radius: 6px 6px 0 0;
}

.sidebar-toggle {
  border: none;
  box-shadow: none;
}

.sidebar-title {
  font-weight: 700;
  color: #1f3a72;
  font-size: 16px;
}

.sidebar-content {
  padding: 16px 8px;
  overflow-y: auto;
  flex: 1;
}

.nav-group {
  margin-bottom: 12px;
  border: 1px solid #e8e9ea;
  border-radius: 8px;
  background: #ffffff;
}

.group-title {
  padding: 12px 16px;
  font-weight: 600;
  color: #262626;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.group-title:hover {
  background: #f5f9ff;
  color: #1677ff;
}

.group-title.expanded::after {
  content: '▼';
  position: absolute;
  right: 16px;
  font-size: 12px;
}

.group-title:not(.expanded)::after {
  content: '▶';
  position: absolute;
  right: 16px;
  font-size: 12px;
}

.group-items {
  padding: 4px 8px 12px 8px;
}

.nav-item {
  padding: 10px 12px;
  margin: 4px 0;
  border-radius: 6px;
  color: #595959;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.nav-item:hover {
  background: #f5f9ff;
  color: #1677ff;
}

.nav-item.active {
  background: rgba(22,119,255,0.08);
  color: #1677ff;
  font-weight: 600;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

/* 统计卡片样式 */
.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border: 2px solid #e8e9ea;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-content {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #595959;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-unit {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 12px;
}

.stat-compare {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
}

.compare-label {
  color: #8c8c8c;
}

.compare-value {
  color: #262626;
  font-weight: 600;
}

.compare-trend {
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
}

.trend-up {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

/* 图表区域样式 */
.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  background: #ffffff;
  border: 2px solid #e8e9ea;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 380px;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-container {
  flex: 1;
  min-height: 300px;
}

/* 报警容器样式 */
.alarm-container {
  flex: 1;
  padding: 8px 0;
}

.alarm-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c8c8c;
  font-size: 14px;
}

.alarm-list {
  height: 100%;
  overflow-y: auto;
}

.alarm-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 4px solid #1677ff;
  transition: all 0.2s;
}

.alarm-item:hover {
  background: #f0f9ff;
}

.alarm-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.alarm-content {
  flex: 1;
  min-width: 0;
}

.alarm-title {
  font-weight: 600;
  color: #262626;
  font-size: 13px;
  margin-bottom: 4px;
  line-height: 1.3;
}

.alarm-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 11px;
  color: #8c8c8c;
}

.alarm-value {
  font-size: 11px;
  color: #1677ff;
  font-weight: 500;
  margin-top: 2px;
}

.alarm-action {
  flex-shrink: 0;
}

/* 表格区域样式 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  background: #ffffff;
  border: 2px solid #e8e9ea;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 弹窗样式 */
.device-modal {
  padding: 0;
}

.device-image-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.device-image {
  width: 100%;
  max-width: 320px;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e8e9ea;
}

.energy-chart-section {
  margin-top: 24px;
  padding: 0 16px;
}

.energy-chart-section h4 {
  margin: 0 0 16px 0;
  color: #262626;
  font-size: 16px;
  font-weight: 600;
}

.energy-chart {
  height: 350px;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .page-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    margin: 16px 16px 8px 16px;
  }
  
  .sidebar.collapsed {
    width: 100%;
  }
  
  .main-content {
    margin-top: 0;
  }
}

@media (max-width: 768px) {
  .device-image {
    height: 150px;
  }
  
  .energy-chart {
    height: 250px;
  }
}

/* Ant Design 组件自定义 */
:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(90deg, rgba(22,119,255,0.05), rgba(26,198,255,0.02));
  font-weight: 600;
  color: #262626;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f0f9ff;
}

:deep(.ant-descriptions-item-label) {
  color: #595959;
  font-weight: 600;
}

:deep(.ant-descriptions-item-content) {
  color: #262626;
  font-weight: 500;
}
</style>
