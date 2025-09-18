<template>
  <div class="device-process">
    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">中央空调系统工艺流程</h2>
        <div class="system-status">
          <div class="status-item">
            <span class="status-dot running"></span>
            <span class="status-text">系统运行中</span>
          </div>
          <div class="status-item">
            <span class="status-label">当前负载：</span>
            <span class="status-value">78.5%</span>
          </div>
        </div>
      </div>
      <div class="toolbar-right">
        <a-button type="primary" @click="refreshData">
          <template #icon><ReloadOutlined /></template>
          刷新数据
        </a-button>
        <a-button @click="exportProcess">
          <template #icon><DownloadOutlined /></template>
          导出工艺
        </a-button>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧工艺流程图 -->
      <div class="process-diagram">
        <div class="diagram-header">
          <h3 class="diagram-title">工艺流程图</h3>
          <div class="diagram-controls">
            <a-tooltip title="放大">
              <a-button size="small" class="zoom-btn zoom-in" @click="zoomIn">
                <template #icon><ZoomInOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="缩小">
              <a-button size="small" class="zoom-btn zoom-out" @click="zoomOut">
                <template #icon><ZoomOutOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="适应窗口">
              <a-button size="small" class="zoom-btn fit-window" @click="fitToWindow">
                <template #icon><BorderOutlined /></template>
              </a-button>
            </a-tooltip>
          </div>
        </div>
        
        <div class="diagram-container">
          <div class="process-image-wrapper" :style="{ transform: `scale(${zoomLevel})` }">
            <img 
              :src="processImage"
              alt="中央空调工艺流程图" 
              class="process-image"
              @load="onImageLoad"
            />
            
            <!-- 工艺点位标注 -->
            <div class="process-points">
              <!-- 冷却塔区域 -->
              <div class="process-point cooling-tower-1" style="top: 15%; left: 20%;">
                <div class="point-marker cooling-tower">
                  <div class="point-icon">
                    <div class="cooling-tower-icon">🌊</div>
                  </div>
                  <div class="point-label">CT-01</div>
                  <div class="point-status running"></div>
                </div>
                <div class="point-tooltip">
                  <div class="tooltip-content">
                    <div class="tooltip-header">
                      <div class="tooltip-title">1#冷却塔</div>
                      <div class="tooltip-badge cooling-tower">冷却设备</div>
                    </div>
                    <div class="tooltip-data">
                      <div class="data-item">
                        <span class="data-label">进水温度：</span>
                        <span class="data-value">{{ coolingTowerData.inlet_temp }}°C</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">出水温度：</span>
                        <span class="data-value">{{ coolingTowerData.outlet_temp }}°C</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">流量：</span>
                        <span class="data-value">{{ coolingTowerData.flow }}m³/h</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 主机区域 -->
              <div class="process-point chiller-1" style="top: 35%; left: 15%;">
                <div class="point-marker chiller">
                  <div class="point-icon">
                    <div class="chiller-icon">❄️</div>
                  </div>
                  <div class="point-label">CH-01</div>
                  <div class="point-status running"></div>
                </div>
                <div class="point-tooltip">
                  <div class="tooltip-content">
                    <div class="tooltip-header">
                      <div class="tooltip-title">1#冷水机组</div>
                      <div class="tooltip-badge chiller">主机设备</div>
                    </div>
                    <div class="tooltip-data">
                      <div class="data-item">
                        <span class="data-label">制冷量：</span>
                        <span class="data-value">{{ chillerData.cooling_capacity }}kW</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">COP：</span>
                        <span class="data-value">{{ chillerData.cop }}</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">运行状态：</span>
                        <span class="data-value status-running">{{ chillerData.status }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 水泵区域 -->
              <div class="process-point pump-1" style="top: 55%; left: 45%;">
                <div class="point-marker pump">
                  <div class="point-icon">
                    <div class="pump-icon">⚡</div>
                  </div>
                  <div class="point-label">P-01</div>
                  <div class="point-status running"></div>
                </div>
                <div class="point-tooltip">
                  <div class="tooltip-content">
                    <div class="tooltip-header">
                      <div class="tooltip-title">1#冷冻水泵</div>
                      <div class="tooltip-badge pump">泵类设备</div>
                    </div>
                    <div class="tooltip-data">
                      <div class="data-item">
                        <span class="data-label">流量：</span>
                        <span class="data-value">{{ pumpData.flow }}m³/h</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">扬程：</span>
                        <span class="data-value">{{ pumpData.head }}m</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">频率：</span>
                        <span class="data-value">{{ pumpData.frequency }}Hz</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 末端设备区域 -->
              <div class="process-point ahu-1" style="top: 70%; left: 80%;">
                <div class="point-marker ahu">
                  <div class="point-icon">
                    <div class="ahu-icon">🌀</div>
                  </div>
                  <div class="point-label">AHU-01</div>
                  <div class="point-status running"></div>
                </div>
                <div class="point-tooltip">
                  <div class="tooltip-content">
                    <div class="tooltip-header">
                      <div class="tooltip-title">1#空调机组</div>
                      <div class="tooltip-badge ahu">末端设备</div>
                    </div>
                    <div class="tooltip-data">
                      <div class="data-item">
                        <span class="data-label">送风温度：</span>
                        <span class="data-value">{{ ahuData.supply_temp }}°C</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">回风温度：</span>
                        <span class="data-value">{{ ahuData.return_temp }}°C</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">风量：</span>
                        <span class="data-value">{{ ahuData.airflow }}m³/h</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧参数监控面板 -->
      <div class="monitoring-panel">
        <div class="panel-header">
          <h3 class="panel-title">实时监控</h3>
          <div class="update-time">
            最后更新：{{ updateTime }}
          </div>
        </div>

        <!-- 系统概况卡片 -->
        <div class="monitor-card">
          <div class="card-header">
            <div class="card-title">系统概况</div>
            <div class="card-status running">运行中</div>
          </div>
          <div class="card-content">
            <div class="overview-metrics">
              <div class="metric-item">
                <div class="metric-label">总制冷量</div>
                <div class="metric-value">{{ systemOverview.total_cooling }}kW</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">总功耗</div>
                <div class="metric-value">{{ systemOverview.total_power }}kW</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">系统COP</div>
                <div class="metric-value">{{ systemOverview.system_cop }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">运行台数</div>
                <div class="metric-value">{{ systemOverview.running_count }}台</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 设备状态列表 -->
        <div class="monitor-card">
          <div class="card-header">
            <div class="card-title">设备状态</div>
            <a-button size="small" type="link" @click="showAllDevices">查看全部</a-button>
          </div>
          <div class="card-content">
            <div class="device-list">
              <div 
                v-for="device in deviceList" 
                :key="device.id"
                class="device-item"
                @click="selectDevice(device)"
              >
                <div class="device-info">
                  <div class="device-name">{{ device.name }}</div>
                  <div class="device-type">{{ device.type }}</div>
                </div>
                <div class="device-status">
                  <div class="status-indicator" :class="device.status"></div>
                  <div class="status-text">{{ getStatusText(device.status) }}</div>
                </div>
                <div class="device-value">
                  <div class="value-number">{{ device.value }}</div>
                  <div class="value-unit">{{ device.unit }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 报警信息 -->
        <div class="monitor-card">
          <div class="card-header">
            <div class="card-title">报警信息</div>
            <div class="alarm-count">{{ alarmList.length }}</div>
          </div>
          <div class="card-content">
            <div class="alarm-list" v-if="alarmList.length > 0">
              <div 
                v-for="alarm in alarmList" 
                :key="alarm.id"
                class="alarm-item"
                :class="alarm.level"
              >
                <div class="alarm-icon">
                  <WarningOutlined v-if="alarm.level === 'warning'" />
                  <ExclamationCircleOutlined v-else-if="alarm.level === 'error'" />
                  <InfoCircleOutlined v-else />
                </div>
                <div class="alarm-content">
                  <div class="alarm-title">{{ alarm.title }}</div>
                  <div class="alarm-time">{{ alarm.time }}</div>
                </div>
              </div>
            </div>
            <div v-else class="no-alarms">
              <CheckCircleOutlined class="no-alarm-icon" />
              <div class="no-alarm-text">系统运行正常</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  ReloadOutlined, 
  DownloadOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  BorderOutlined,
  WarningOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import processImage from '@/assets/images/device_process.png'

// 缩放控制
const zoomLevel = ref(1)

// 实时数据
const updateTime = ref('')

// 冷却塔数据
const coolingTowerData = reactive({
  inlet_temp: 32.5,
  outlet_temp: 28.2,
  flow: 485
})

// 冷水机组数据
const chillerData = reactive({
  cooling_capacity: 1250,
  cop: 4.2,
  status: '运行'
})

// 水泵数据
const pumpData = reactive({
  flow: 450,
  head: 35,
  frequency: 48.5
})

// 空调机组数据
const ahuData = reactive({
  supply_temp: 16.5,
  return_temp: 24.8,
  airflow: 8500
})

// 系统概况
const systemOverview = reactive({
  total_cooling: 2850,
  total_power: 680,
  system_cop: 4.19,
  running_count: 3
})

// 设备列表
const deviceList = ref([
  {
    id: '1',
    name: '1#冷水机组',
    type: '螺杆式',
    status: 'running',
    value: 1250,
    unit: 'kW'
  },
  {
    id: '2',
    name: '1#冷冻水泵',
    type: '离心泵',
    status: 'running',
    value: 450,
    unit: 'm³/h'
  },
  {
    id: '3',
    name: '1#冷却塔',
    type: '横流式',
    status: 'running',
    value: 485,
    unit: 'm³/h'
  },
  {
    id: '4',
    name: '2#冷水机组',
    type: '螺杆式',
    status: 'standby',
    value: 0,
    unit: 'kW'
  },
  {
    id: '5',
    name: '1#空调机组',
    type: 'AHU',
    status: 'running',
    value: 8500,
    unit: 'm³/h'
  }
])

// 报警列表
const alarmList = ref([
  {
    id: '1',
    title: '1#冷却塔水位偏低',
    time: '10:35',
    level: 'warning'
  },
  {
    id: '2',
    title: '2#冷冻水泵频率异常',
    time: '09:28',
    level: 'info'
  }
])

// 方法
const refreshData = () => {
  message.success('数据已刷新')
  updateCurrentTime()
}

const exportProcess = () => {
  message.info('导出工艺流程图')
}

const zoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.2, 3)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.2, 0.5)
}

const fitToWindow = () => {
  zoomLevel.value = 1
}

const onImageLoad = () => {
  console.log('工艺流程图加载完成')
}

const selectDevice = (device: any) => {
  message.info(`选择设备：${device.name}`)
}

const showAllDevices = () => {
  message.info('显示所有设备')
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    running: '运行',
    standby: '待机',
    fault: '故障',
    maintenance: '维护'
  }
  return statusMap[status] || status
}

const updateCurrentTime = () => {
  const now = new Date()
  updateTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
}

onMounted(() => {
  updateCurrentTime()
  // 每30秒更新一次时间
  setInterval(updateCurrentTime, 30000)
})
</script>

<style scoped>
.device-process {
  height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

/* 顶部工具栏 */
.top-toolbar {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
}

.status-dot.running {
  background: #52c41a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.status-text {
  font-size: 14px;
  color: #52c41a;
  font-weight: 500;
}

.status-label {
  font-size: 14px;
  color: #666;
}

.status-value {
  font-size: 14px;
  color: #1890ff;
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

/* 主要内容区 */
.main-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 16px;
  padding: 16px 24px;
  overflow: hidden;
}

/* 工艺流程图区域 */
.process-diagram {
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.diagram-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.diagram-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.diagram-controls {
  display: flex;
  gap: 8px;
}

/* 缩放按钮样式 */
.zoom-btn {
  transition: all 0.3s ease;
  border-radius: 6px;
  font-weight: 500;
}

/* 放大按钮 - 绿色主题 */
.zoom-btn.zoom-in {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-color: #52c41a;
  color: white;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.3);
}

.zoom-btn.zoom-in:hover {
  background: linear-gradient(135deg, #73d13d 0%, #95de64 100%);
  border-color: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(82, 196, 26, 0.4);
}

/* 缩小按钮 - 橙色主题 */
.zoom-btn.zoom-out {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  border-color: #fa8c16;
  color: white;
  box-shadow: 0 2px 4px rgba(250, 140, 22, 0.3);
}

.zoom-btn.zoom-out:hover {
  background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
  border-color: #ffa940;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(250, 140, 22, 0.4);
}

/* 适应窗口按钮 - 紫色主题 */
.zoom-btn.fit-window {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  border-color: #722ed1;
  color: white;
  box-shadow: 0 2px 4px rgba(114, 46, 209, 0.3);
}

.zoom-btn.fit-window:hover {
  background: linear-gradient(135deg, #9254de 0%, #b37feb 100%);
  border-color: #9254de;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(114, 46, 209, 0.4);
}

/* 按钮按下效果 */
.zoom-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.diagram-container {
  flex: 1;
  position: relative;
  overflow: auto;
  background: #f8f9fa;
}

.process-image-wrapper {
  position: relative;
  width: 100%;
  min-height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.3s ease;
}

.process-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

/* 工艺点位标注 */
.process-points {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.process-point {
  position: absolute;
  pointer-events: all;
}

.point-marker {
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.point-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  transition: all 0.3s ease;
  font-size: 20px;
  border: 3px solid white;
}

/* 冷却塔样式 */
.point-marker.cooling-tower .point-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  animation: pulse-water 3s ease-in-out infinite;
}

.cooling-tower-icon {
  filter: drop-shadow(0 0 2px rgba(255,255,255,0.8));
}

@keyframes pulse-water {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 6px 20px rgba(79, 172, 254, 0.5); }
}

/* 主机样式 */
.point-marker.chiller .point-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: pulse-cold 3s ease-in-out infinite 0.5s;
}

.chiller-icon {
  filter: drop-shadow(0 0 2px rgba(255,255,255,0.8));
}

@keyframes pulse-cold {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5); }
}

/* 水泵样式 */
.point-marker.pump .point-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  animation: pulse-energy 3s ease-in-out infinite 1s;
}

.pump-icon {
  filter: drop-shadow(0 0 2px rgba(0,0,0,0.5));
}

@keyframes pulse-energy {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(252, 182, 159, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 6px 20px rgba(252, 182, 159, 0.5); }
}

/* 空调机组样式 */
.point-marker.ahu .point-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  animation: pulse-air 3s ease-in-out infinite 1.5s;
}

.ahu-icon {
  filter: drop-shadow(0 0 2px rgba(255,255,255,0.8));
}

@keyframes pulse-air {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(168, 237, 234, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 6px 20px rgba(168, 237, 234, 0.5); }
}

/* 状态指示器 */
.point-status {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.point-status.running {
  background: #52c41a;
  animation: blink-status 2s ease-in-out infinite;
}

.point-status.standby {
  background: #faad14;
}

.point-status.fault {
  background: #ff4d4f;
  animation: blink-urgent 1s ease-in-out infinite;
}

@keyframes blink-status {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes blink-urgent {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.point-label {
  margin-top: 8px;
  background: rgba(0,0,0,0.8);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);
}

.point-tooltip {
  position: absolute;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  padding: 0;
  min-width: 240px;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.process-point:hover .point-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(-5px);
}

.tooltip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px 12px 0 0;
}

.tooltip-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.tooltip-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  color: white;
}

.tooltip-badge.cooling-tower {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.tooltip-badge.chiller {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tooltip-badge.pump {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #333;
}

.tooltip-badge.ahu {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
}

.tooltip-data {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-label {
  font-size: 12px;
  color: #666;
}

.data-value {
  font-size: 12px;
  color: #262626;
  font-weight: 500;
}

.data-value.status-running {
  color: #52c41a;
}

/* 监控面板 */
.monitoring-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
}

.panel-header {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.update-time {
  font-size: 12px;
  color: #666;
}

.monitor-card {
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.card-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.card-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.alarm-count {
  background: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

.card-content {
  padding: 16px;
}

/* 系统概况指标 */
.overview-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.metric-item {
  text-align: center;
}

.metric-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

/* 设备列表 */
.device-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.device-item:hover {
  border-color: #1890ff;
  background: #f6ffed;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 2px;
}

.device-type {
  font-size: 12px;
  color: #666;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 12px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator.running {
  background: #52c41a;
}

.status-indicator.standby {
  background: #faad14;
}

.status-indicator.fault {
  background: #ff4d4f;
}

.status-indicator.maintenance {
  background: #722ed1;
}

.device-value {
  text-align: right;
}

.value-number {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  line-height: 1;
}

.value-unit {
  font-size: 12px;
  color: #666;
}

.status-text {
  font-size: 12px;
  color: #666;
}

/* 报警列表 */
.alarm-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alarm-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid transparent;
}

.alarm-item.warning {
  background: #fff7e6;
  border-left-color: #faad14;
}

.alarm-item.error {
  background: #fff2f0;
  border-left-color: #ff4d4f;
}

.alarm-item.info {
  background: #e6f7ff;
  border-left-color: #1890ff;
}

.alarm-icon {
  font-size: 16px;
}

.alarm-item.warning .alarm-icon {
  color: #faad14;
}

.alarm-item.error .alarm-icon {
  color: #ff4d4f;
}

.alarm-item.info .alarm-icon {
  color: #1890ff;
}

.alarm-content {
  flex: 1;
}

.alarm-title {
  font-size: 13px;
  color: #262626;
  margin-bottom: 2px;
}

.alarm-time {
  font-size: 12px;
  color: #666;
}

.no-alarms {
  text-align: center;
  padding: 20px;
  color: #666;
}

.no-alarm-icon {
  font-size: 32px;
  color: #52c41a;
  margin-bottom: 8px;
}

.no-alarm-text {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
  }
  
  .monitoring-panel {
    flex-direction: row;
    overflow-x: auto;
  }
  
  .monitor-card {
    min-width: 300px;
  }
}
</style>