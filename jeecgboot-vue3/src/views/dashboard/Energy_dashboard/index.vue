<template>
  <div class="dashboard-wrapper" ref="wrapperRef">
    <div class="dashboard" ref="dashboardRef">
      <div class="title-bar">
        <div class="title">工业企业能碳数字化管理平台</div>
        <a-tooltip :title="isFullscreen ? '退出全屏' : '全屏显示'" placement="bottom">
          <div class="fullscreen-btn" @click="toggleFullscreen">
            <FullscreenExitOutlined v-if="isFullscreen" />
            <FullscreenOutlined v-else />
          </div>
        </a-tooltip>
      </div>

      <div class="content">
        <div class="left">
          <div class="panel">
            <div class="panel-title">全厂用电</div>
            <div class="energy-grid">
              <div class="energy-item" v-for="e in energyData" :key="e.name">
                <div class="icon-box" :style="{background: e.color}">
                  <component :is="e.icon" />
                </div>
                <div class="name">{{ e.name }}</div>
                <div class="value">{{ e.value }}</div>
                <div class="unit">{{ e.unit }}</div>
              </div>
            </div>
          </div>

          <div class="panel changqukailan">
            <div class="panel-title">厂区概览</div>
            <div ref="areaChart" style="height: 200px;"></div>
          </div>

          <div class="panel">
            <div class="panel-title">能源对标</div>
            <div class="benchmark-grid">
              <div class="benchmark-item" v-for="b in benchmarks" :key="b.label">
                <div class="label"><a-button type="primary" size="small">{{ b.label }}</a-button></div>
                <div class="value">{{ b.value }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="center">
          <div class="top-bar">
            <div class="top-indicators">
              <a-date-picker v-model:value="month" picker="month" class="date-picker" />
              <div class="indicator-item">
                <div class="label"><a-button type="primary" size="small">全部网内消耗</a-button></div>
                <div class="value">3541 <span>kW</span></div>
              </div>
              <div class="indicator-item">
                <div class="label"><a-button type="primary" size="small">电网供电功率</a-button></div>
                <div class="value">3541 <span>kW</span></div>
              </div>
              <div class="indicator-item">
                <div class="label"><a-button type="primary" size="small">能耗总量</a-button></div>
                <div class="value">577.16 <span>吨标煤</span></div>
              </div>
              <div class="indicator-item">
                <div class="label"><a-button type="primary" size="small">碳排总量</a-button></div>
                <div class="value">1630.94 <span>吨CO2</span></div>
              </div>
              <div class="indicator-item">
                <div class="label"><a-button type="primary" size="small">减碳总量</a-button></div>
                <div class="value">640.36 <span>吨CO2</span></div>
              </div>
            </div>
          </div>
          <div class="factory-view">
            <img src="@/assets/images/factory-bg.jpg" alt="工厂" />
            <div class="factory-mask"></div>
            <!-- 厂区标注 -->
            <div class="factory-marker" style="top: 15%; left: 25%;">
              <div class="marker-icon">🏭</div>
              <div class="marker-info">
                <div class="marker-title">一区</div>
                <div class="marker-data">74.88 <span>ZkWh</span></div>
              </div>
            </div>
            <div class="factory-marker" style="top: 20%; right: 28%;">
              <div class="marker-icon">🏭</div>
              <div class="marker-info">
                <div class="marker-title">二区</div>
                <div class="marker-data">21.19 <span>ZkWh</span></div>
              </div>
            </div>
            <div class="factory-marker" style="top: 48%; left: 30%;">
              <div class="marker-icon">🏭</div>
              <div class="marker-info">
                <div class="marker-title">三区</div>
                <div class="marker-data">45.22 <span>ZkWh</span></div>
              </div>
            </div>
            <div class="factory-marker" style="bottom: 25%; right: 25%;">
              <div class="marker-icon">🏭</div>
              <div class="marker-info">
                <div class="marker-title">四区</div>
                <div class="marker-data">32.98 <span>ZkWh</span></div>
              </div>
            </div>
            <div class="factory-marker" style="top: 15%; right: 15%;">
              <div class="marker-icon">⚡</div>
              <div class="marker-info">
                <div class="marker-title">用电量</div>
                <div class="marker-data">18.32 <span>ZkWh</span></div>
              </div>
            </div>
            <div class="factory-marker" style="top: 22%; right: 8%;">
              <div class="marker-icon">⚡</div>
              <div class="marker-info">
                <div class="marker-title">次用电</div>
                <div class="marker-data">38.29 <span>ZkWh</span></div>
              </div>
            </div>
          </div>
          <div class="panel trend-panel">
            <div class="panel-title">能源趋势</div>
            <div class="trend-tabs">
              <a-button type="primary" size="small">用电</a-button>
              <a-button size="small">碳排</a-button>
              <a-button size="small">减碳</a-button>
            </div>
            <div class="trend-content">
              <div class="trend-summary-vertical">
                <div class="summary-item">
                  <div class="label">当月</div>
                  <div class="value">577.16 <span>吨标煤</span></div>
                </div>
                <div class="summary-item">
                  <div class="label">上月</div>
                  <div class="value">698.88 <span>吨标煤</span></div>
                </div>
                <div class="summary-item">
                  <div class="label">环比</div>
                  <div class="value">9.67%</div>
                </div>
              </div>
              <div ref="trendChart" class="trend-chart-container" style="height: 220px;"></div>
            </div>
          </div>
        </div>

        <div class="right">
          <div class="panel">
            <div class="panel-title">用电成本</div>
            <a-select v-model:value="voltage" style="width: 100%; margin-bottom: 10px;">
              <a-select-option value="1">10kV 1区 G1线柜组</a-select-option>
            </a-select>
            <div class="cost-grid">
              <div class="cost-item">
                <div class="label"><a-button type="primary" size="small">当月需量</a-button></div>
                <div class="value">2983 <span>kW</span></div>
              </div>
              <div class="cost-item">
                <div class="label"><a-button type="primary" size="small">上月需量</a-button></div>
                <div class="value">2932 <span>kW</span></div>
              </div>
              <div class="cost-item">
                <div class="label"><a-button type="primary" size="small">环比</a-button></div>
                <div class="value">1.74%</div>
              </div>
              <div class="cost-item">
                <div class="label"><a-button type="primary" size="small">节约</a-button></div>
                <div class="value">61 <span>元</span></div>
              </div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-title">能碳概览</div>
            <a-select v-model:value="company" style="width: 100%; margin-bottom: 10px;">
              <a-select-option value="1">XXX有限公司</a-select-option>
            </a-select>
            <div class="overview-grid">
              <div class="overview-item">
                <FileTextOutlined class="icon" />
                <div>
                  <div class="label"><a-button type="primary" size="small">用能预算</a-button></div>
                  <div class="value">5452.57<span>吨标煤</span></div>
                </div>
              </div>
              <div class="overview-item">
                <PieChartOutlined class="icon" />
                <div>
                  <div class="label"><a-button type="primary" size="small">已用额</a-button></div>
                  <div class="value">2830.68<span>吨标煤</span></div>
                </div>
              </div>
            </div>
            <div class="progress-box">
              <div class="label">进度: 51.91%</div>
              <a-progress :percent="51.91" :show-info="false" stroke-color="#faad14" />
            </div>
          </div>

          <div class="panel">
            <div class="panel-title">光伏发电</div>
            <div class="solar-grid">
              <div class="solar-item">
                <ThunderboltOutlined class="icon" />
                <div>
                  <div class="label"><a-button type="primary" size="small">光伏容量</a-button></div>
                  <div class="value">10 <span>MWp</span></div>
                </div>
              </div>
              <div class="solar-item">
                <FireOutlined class="icon" />
                <div>
                  <div class="label"><a-button type="primary" size="small">累计发电量</a-button></div>
                  <div class="value">23 <span>GWh</span></div>
                </div>
              </div>
            </div>
            <div class="solar-details">
              <div class="detail-row" v-for="d in solarData" :key="d.label">
                <span><a-button type="primary" size="small">{{ d.label }}</a-button></span>
                <span>{{ d.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { ThunderboltOutlined, FireOutlined, FileTextOutlined, PieChartOutlined, FullscreenOutlined, FullscreenExitOutlined } from '@ant-design/icons-vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';

const month = ref(dayjs());
const voltage = ref('1');
const company = ref('1');
const isFullscreen = ref(false);
const dashboardRef = ref<HTMLElement | null>(null);
const wrapperRef = ref<HTMLElement | null>(null);

// 设计稿基准尺寸
const DESIGN_WIDTH = 1920;
const DESIGN_HEIGHT = 1080;

// 计算并应用缩放比例
const applyScale = () => {
  if (!dashboardRef.value || !wrapperRef.value) return;
  
  const wrapperWidth = wrapperRef.value.clientWidth;
  const wrapperHeight = wrapperRef.value.clientHeight;
  
  // 根据宽度计算缩放比例
  const scale = wrapperWidth / DESIGN_WIDTH;
  
  // 应用变换
  dashboardRef.value.style.transformOrigin = 'top left';
  dashboardRef.value.style.transform = `scale(${scale})`;
  
  // 图表重新渲染
  nextTick(() => {
    setTimeout(() => {
      areaChartInstance?.resize();
      trendChartInstance?.resize();
    }, 100);
  });
};

// 切换全屏 - 关键修改：只让看板wrapper全屏
const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      // 只让看板wrapper全屏，而不是整个document
      if (wrapperRef.value) {
        await wrapperRef.value.requestFullscreen();
      }
    } else {
      await document.exitFullscreen();
    }
  } catch (err) {
    console.error('全屏切换失败:', err);
  }
};

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement;
  // 全屏切换后重新计算缩放
  setTimeout(applyScale, 150);
};

// 防抖处理resize
let resizeTimer: ReturnType<typeof setTimeout> | null = null;
const handleResize = () => {
  if (resizeTimer) clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    applyScale();
  }, 100);
};

const energyData = [
  { name: '电', value: '967', unit: '吨CO2', color: '#1890ff', icon: 'ThunderboltOutlined' },
  { name: '蒸汽', value: '66.3', unit: '吨CO2', color: '#52c41a', icon: 'FireOutlined' },
  { name: '天然气', value: '0', unit: '吨CO2', color: '#faad14', icon: 'FireOutlined' },
  { name: '水', value: '261.56', unit: '吨CO2', color: '#13c2c2', icon: 'FireOutlined' }
];

const benchmarks = [
  { label: '单位碳排放强度指标', value: '0.08 吨CO2/元' },
  { label: '单位工业增加值能耗指标', value: '0.08 吨标煤/元' },
  { label: '全员劳动生产率', value: '4857/1077' },
  { label: '3级能耗', value: '45.03' },
  { label: '行业碳排', value: '0.77' },
  { label: '3级能耗', value: '10.96' }
];

const solarData = [
  { label: '当月发电量', value: '910 MWh' },
  { label: '当月自用', value: '698 MWh' },
  { label: '减碳收入', value: '91 吨' },
  { label: '售电', value: '6134 元' }
];

const areaChart = ref();
const trendChart = ref();
let areaChartInstance: echarts.ECharts | null = null;
let trendChartInstance: echarts.ECharts | null = null;

onMounted(() => {
  // 初始化图表
  areaChartInstance = echarts.init(areaChart.value);
  areaChartInstance.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { 
      type: 'category', 
      data: ['一区', '二区', '三区'], 
      axisLine: { lineStyle: { color: '#4a9eff' } }, 
      axisLabel: { color: '#fff' } 
    },
    yAxis: { 
      type: 'value', 
      max: 1000, 
      axisLine: { lineStyle: { color: '#4a9eff' } }, 
      axisLabel: { color: '#fff' }, 
      splitLine: { lineStyle: { color: '#1a4d7a' } } 
    },
    series: [{ 
      data: [820, 993, 750], 
      type: 'bar', 
      barWidth: 30, 
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' },
          { offset: 0.5, color: '#1890ff' },
          { offset: 1, color: '#0050b3' }
        ]),
        borderRadius: [4, 4, 0, 0],
        shadowColor: 'rgba(0, 212, 255, 0.5)',
        shadowBlur: 10,
        shadowOffsetY: 5
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00f0ff' },
            { offset: 0.5, color: '#40a9ff' },
            { offset: 1, color: '#096dd9' }
          ])
        }
      }
    }]
  });

  trendChartInstance = echarts.init(trendChart.value);
  const days = Array.from({ length: 31 }, (_, i) => (i + 1).toString().padStart(2, '0'));
  trendChartInstance.setOption({
    grid: { left: 40, right: 40, top: 30, bottom: 30 },
    legend: { data: ['上月', '当月', '减碳'], textStyle: { color: '#fff' }, top: 0 },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#4a9eff' } }, axisLabel: { color: '#fff', interval: 2 } },
    yAxis: [
      { type: 'value', name: '用电: 吨标煤', position: 'left', axisLine: { lineStyle: { color: '#4a9eff' } }, axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: '#1a4d7a' } } },
      { type: 'value', name: '实现率: %', position: 'right', max: 500, axisLine: { lineStyle: { color: '#4a9eff' } }, axisLabel: { color: '#fff' }, splitLine: { show: false } }
    ],
    series: [
      { name: '上月', data: Array.from({ length: 31 }, () => Math.random() * 20 + 20), type: 'bar', barWidth: 8, itemStyle: { color: '#faad14' } },
      { name: '当月', data: Array.from({ length: 31 }, () => Math.random() * 20 + 15), type: 'bar', barWidth: 8, itemStyle: { color: '#1890ff' } },
      { name: '减碳', data: Array.from({ length: 31 }, () => Math.random() * 15 + 10), type: 'bar', barWidth: 8, itemStyle: { color: '#52c41a' } }
    ]
  });

  // 添加事件监听
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  window.addEventListener('resize', handleResize);
  
  // 初始应用缩放
  setTimeout(applyScale, 100);
});

watch(isFullscreen, async () => {
  await nextTick();
  setTimeout(applyScale, 300);
});

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  window.removeEventListener('resize', handleResize);
  if (resizeTimer) clearTimeout(resizeTimer);
  areaChartInstance?.dispose();
  trendChartInstance?.dispose();
});
</script>

<style scoped lang="less">
// 外层容器
.dashboard-wrapper {
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  background: url('@/assets/images/dashboard-bg.jpg') no-repeat center center;
  background-size: cover;
  position: relative;
  
  // 全屏时的样式
  
  &:fullscreen {
    width: 100vw;
    height: 100vh;
    overflow: hidden;
  }
}

// 主仪表盘 - 固定尺寸
.dashboard {
  width: 1920px;
  height: 1080px;
  padding: 15px;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
}

.title-bar {
  position: relative;
  text-align: center;
  margin-bottom: 55px;
  padding-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;

  .title {
    font-size: 28px;
    line-height: 28px;
    margin-top: -23px;
    font-weight: bold;
    color: #fff;
    text-shadow: 0 0 20px rgba(0, 212, 255, 0.8);
    letter-spacing: 6px;
    background: linear-gradient(180deg, #fff 0%, #00d4ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .fullscreen-btn {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    width: 32px;
    height: 32px;
    background: rgba(0, 100, 180, 0.6);
    border: 1px solid rgba(0, 212, 255, 0.6);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #00d4ff;
    font-size: 16px;
    transition: all 0.3s;
    box-shadow: 0 0 15px rgba(0, 212, 255, 0.3);

    &:hover {
      background: rgba(0, 150, 255, 0.8);
      box-shadow: 0 0 20px rgba(0, 212, 255, 0.6);
    }
  }
}

.top-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;

  .top-indicators {
    display: flex;
    justify-content: center;
    align-items: stretch;
    width: 100%;
    gap: 12px;

    .date-picker {
      flex: 1;
      max-width: 140px;
    }

    .indicator-item {
      flex: 1;
      text-align: center;
      padding: 8px 20px;
      background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
      border: 1px solid rgba(0, 212, 255, 0.35);
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25), 
                  0 0 15px rgba(0, 212, 255, 0.3),
                  inset 0 1px 1px rgba(255, 255, 255, 0.15);

      .label {
        color: #8cc5ff;
        font-size: 12px;
        margin-bottom: 5px;
      }

      .value {
        color: #fff;
        font-size: 20px;
        font-weight: bold;

        span {
          font-size: 12px;
          margin-left: 5px;
        }
      }
    }
  }
}

.content {
  display: flex;
  gap: 15px;
  height: calc(1080px - 130px);
  align-items: stretch;
}

.left,
.right {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

// 让左侧第一个面板（全厂用电）与右侧第一个面板（用电成本）高度一致
.left > .panel:nth-child(1) {
  min-height: 280px;
}

// 让左侧第三个面板（能源对标）和右侧第三个面板（光伏发电）底部与中间能源趋势面板底部对齐
.left > .panel:nth-child(3),
.right > .panel:nth-child(3) {
  min-height: 360px;
}

.center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;

  .top-bar {
    margin-bottom: 15px;
  }

  .factory-view {
    flex: 1;
    min-height: 440px;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.35);
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3), 
                0 0 15px rgba(0, 212, 255, 0.2),
                inset 0 1px 1px rgba(255, 255, 255, 0.2);
    margin-bottom: 15px;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 30px;
      height: 60%;
      border-left: 3px solid rgba(251, 252, 252, 0.6);
      border-top: 3px solid rgba(247, 249, 250, 0.6);
      border-bottom: 3px solid rgba(247, 249, 250, 0.6);
      z-index: 2;
      box-shadow: 0 0 15px rgba(253, 254, 254, 0.5);
    }

    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 30px;
      height: 60%;
      border-right: 3px solid rgba(244, 249, 249, 0.6);
      border-top: 3px solid rgba(246, 250, 250, 0.6);
      border-bottom: 3px solid rgba(247, 251, 252, 0.6);
      z-index: 2;
      box-shadow: 0 0 15px rgba(249, 252, 252, 0.5);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      object-position: center center;
      position: relative;
      z-index: 1;
    }
    
    .factory-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: 2;
      background: 
        linear-gradient(to right, rgba(255, 255, 255, 0.5) 0%, transparent 10%),
        linear-gradient(to left, rgba(255, 255, 255, 0.5) 0%, transparent 10%),
        linear-gradient(to bottom, rgba(255, 255, 255, 0.4) 0%, transparent 8%),
        linear-gradient(to top, rgba(255, 255, 255, 0.4) 0%, transparent 8%);
    }
    
    .factory-marker {
      position: absolute;
      z-index: 3;
      display: flex;
      align-items: center;
      gap: 8px;
      animation: markerPulse 2s ease-in-out infinite;
      
      .marker-icon {
        width: 32px;
        height: 32px;
        background: rgba(0, 150, 255, 0.9);
        border: 2px solid #00d4ff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        box-shadow: 0 0 20px rgba(0, 212, 255, 0.8),
                    0 0 40px rgba(0, 150, 255, 0.4);
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          transform: scale(1.2);
          box-shadow: 0 0 30px rgba(0, 212, 255, 1),
                      0 0 50px rgba(0, 150, 255, 0.6);
        }
      }
      
      .marker-info {
        background: linear-gradient(135deg, rgba(0, 80, 150, 0.95) 0%, rgba(0, 100, 180, 0.9) 100%);
        border: 1px solid rgba(0, 212, 255, 0.6);
        border-radius: 6px;
        padding: 8px 12px;
        min-width: 100px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4),
                    0 0 15px rgba(0, 212, 255, 0.4);
        
        .marker-title {
          color: #8cc5ff;
          font-size: 12px;
          margin-bottom: 4px;
          font-weight: bold;
        }
        
        .marker-data {
          color: #fff;
          font-size: 16px;
          font-weight: bold;
          
          span {
            font-size: 11px;
            color: #00d4ff;
            margin-left: 3px;
          }
        }
      }
    }
  }

  .trend-panel {
    flex: 0 0 auto;
  }
}

.panel {
  background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
  border: 1px solid rgba(0, 212, 255, 0.35);
  border-radius: 8px;
  padding: 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25), 
              0 0 15px rgba(0, 212, 255, 0.3),
              inset 0 1px 1px rgba(255, 255, 255, 0.15);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #00d4ff 0%, transparent 100%);
    border-radius: 4px 0 0 4px;
  }

  .panel-title {
    color: #fdfefe;
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 3px solid #fcfcfc;
    text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
  }
}

.energy-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .energy-item {
    text-align: center;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    padding: 10px;
    border-radius: 6px;
    border: 1px solid rgba(0, 212, 255, 0.35);
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.25), 
                0 0 15px rgba(0, 212, 255, 0.3),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);

    .icon-box {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      margin: 0 auto 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #fff;
      box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
    }

    .name {
      color: #fff;
      font-size: 14px;
      margin-bottom: 5px;
    }

    .value {
      color: #00d4ff;
      font-size: 18px;
      font-weight: bold;
    }

    .unit {
      color: #8cc5ff;
      font-size: 11px;
    }
  }
}

.benchmark-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;

  .benchmark-item {
    padding: 10px 8px; // 减少内边距
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.35);
    border-radius: 6px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.25), 
                0 0 15px rgba(0, 212, 255, 0.3),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);
    min-height: 75px; // 减少最小高度
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
    
    .label {
      color: #fff;
      font-size: 11px; // 稍微减小字体
      margin-bottom: 6px;
      font-weight: bold;
      line-height: 1.3; // 添加行高使多行文本更紧凑
    }

    .value {
      color: #fff;
      font-size: 13px; // 稍微减小字体
      font-weight: bold;
    }
  }
}

.trend-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.trend-content {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.trend-summary-vertical {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 0 0 auto;
  width: 110px;

  .summary-item {
    text-align: center;
    padding: 8px 10px;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.35);
    border-radius: 6px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.25), 
                0 0 15px rgba(0, 212, 255, 0.3),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);

    .label {
      color: #8cc5ff;
      font-size: 11px;
      margin-bottom: 5px;
    }

    .value {
      color: #fff;
      font-size: 15px;
      font-weight: bold;

      span {
        font-size: 10px;
        margin-left: 2px;
      }
    }
  }
}

.trend-chart-container {
  flex: 1;
}

.cost-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;

  .cost-item {
    text-align: center;
    padding: 10px;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.35);
    border-radius: 6px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.25), 
                0 0 15px rgba(0, 212, 255, 0.3),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);

    .label {
      color: #8cc5ff;
      font-size: 12px;
      margin-bottom: 5px;
    }

    .value {
      color: #fff;
      font-size: 16px;
      font-weight: bold;

      span {
        font-size: 11px;
        margin-left: 3px;
      }
    }
  }
}

.overview-grid,
.solar-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 10px;
 
  .overview-item,
  .solar-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.35);
    border-radius: 6px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.25), 
                0 0 15px rgba(0, 212, 255, 0.3),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);

    .icon {
      font-size: 28px;
      color: #fff;
    }

    .label {
      color: #8cc5ff;
      font-size: 11px;
      margin-bottom: 3px;
    }

    .value {
      color: #fff;
      font-size: 14px;
      font-weight: bold;

      span {
        font-size: 10px;
        margin-left: 3px;
      }
    }
  }
}

.progress-box {
  margin-top: 10px;

  .label {
    color: #fff;
    font-size: 12px;
    margin-bottom: 5px;
  }
}

.solar-details {
  margin: 10px 0;

  .detail-row {
    display: flex;
    justify-content: space-between;
    padding: 8px;
    margin-bottom: 5px;
    color: #8cc5ff;
    font-size: 12px;
    background: linear-gradient(135deg, rgba(14,82,117, 0.7) 0%, rgba(14,82,117, 0.6) 100%);
    border: 1px solid rgba(0, 212, 255, 0.3);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 12px rgba(0, 212, 255, 0.25),
                inset 0 1px 1px rgba(255, 255, 255, 0.1);

    span:last-child {
      color: #fff;
      font-weight: bold;
    }
  }
}

:deep(.ant-picker),
:deep(.ant-select-selector) {
  background: linear-gradient(135deg, rgba(0, 80, 120, 0.7) 0%, rgba(0, 100, 150, 0.6) 100%) !important;
  border: 1px solid rgba(0, 180, 220, 0.4) !important;
  border-radius: 6px !important;
  color: #fff !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
              inset 0 1px 1px rgba(255, 255, 255, 0.1);

  input {
    color: #fff;
  }

  .ant-picker-suffix {
    color: rgba(255, 255, 255, 0.65);
  }
}

:deep(.ant-select-selection-item) {
  color: #fff !important;
}

:deep(.ant-select-arrow) {
  color: rgba(255, 255, 255, 0.65) !important;
}

:deep(.ant-btn) {
  background: rgba(0, 60, 120, 0.5);
  border-color: rgba(0, 212, 255, 0.4);
  color: #fff;

  &.ant-btn-primary {
    background: linear-gradient(135deg, #0066cc 0%, #00d4ff 100%);
    border-color: #00d4ff;
  }
}

:deep(.ant-progress-bg) {
  background: linear-gradient(90deg, #faad14 0%, #ff7a00 100%);
}

// 全屏模式样式
.dashboard-wrapper:fullscreen {
  .title {
    margin-top: 0;
  }
}

// 标注呼吸动画
@keyframes markerPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>