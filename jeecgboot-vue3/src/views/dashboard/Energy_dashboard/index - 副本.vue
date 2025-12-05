<template>
  <div class="dashboard" :class="{ 'is-fullscreen': isFullscreen }">
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

        <div class="panel">
          <div class="panel-title">厂区概览</div>
          <div ref="areaChart" style="height: 200px;"></div>
        </div>

        <div class="panel">
          <div class="panel-title">能源对标</div>
          <div class="benchmark-grid">
            <div class="benchmark-item" v-for="b in benchmarks" :key="b.label">
              <div class="label">{{ b.label }}</div>
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
              <div class="label">全部网内消耗</div>
              <div class="value">3541 <span>kW</span></div>
            </div>
            <div class="indicator-item">
              <div class="label">电网供电功率</div>
              <div class="value">3541 <span>kW</span></div>
            </div>
            <div class="indicator-item">
              <div class="label">能耗总量</div>
              <div class="value">577.16 <span>吨标煤</span></div>
            </div>
            <div class="indicator-item">
              <div class="label">碳排总量</div>
              <div class="value">1630.94 <span>吨CO2</span></div>
            </div>
            <div class="indicator-item">
              <div class="label">减碳总量</div>
              <div class="value">640.36 <span>吨CO2</span></div>
            </div>
          </div>
        </div>
        <div class="factory-view">
          <img src="@/assets/images/factory-bg.jpg" alt="工厂" />
          <div class="factory-mask"></div>
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
              <div class="label">当月需量</div>
              <div class="value">2983 <span>kW</span></div>
            </div>
            <div class="cost-item">
              <div class="label">上月需量</div>
              <div class="value">2932 <span>kW</span></div>
            </div>
            <div class="cost-item">
              <div class="label">环比</div>
              <div class="value">1.74%</div>
            </div>
            <div class="cost-item">
              <div class="label">节约</div>
              <div class="value">61 <span>元</span></div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">能碳概览</div>
          <a-select v-model:value="company" style="width: 100%; margin-bottom: 10px;">
            <a-select-option value="1">浙江明理水泥有限公司</a-select-option>
          </a-select>
          <div class="overview-grid">
            <div class="overview-item">
              <FileTextOutlined class="icon" />
              <div>
                <div class="label">用能预算</div>
                <div class="value">5452.57 <span>吨标煤</span></div>
              </div>
            </div>
            <div class="overview-item">
              <PieChartOutlined class="icon" />
              <div>
                <div class="label">已用额</div>
                <div class="value">2830.68 <span>吨标煤</span></div>
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
                <div class="label">光伏容量</div>
                <div class="value">10 <span>MWp</span></div>
              </div>
            </div>
            <div class="solar-item">
              <FireOutlined class="icon" />
              <div>
                <div class="label">累计发电量</div>
                <div class="value">23 <span>GWh</span></div>
              </div>
            </div>
          </div>
          <div class="solar-details">
            <div class="detail-row" v-for="d in solarData" :key="d.label">
              <span>{{ d.label }}</span>
              <span>{{ d.value }}</span>
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

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
};

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement;
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
  areaChartInstance = echarts.init(areaChart.value);
  areaChartInstance.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: ['一区', '二区', '三区'], axisLine: { lineStyle: { color: '#4a9eff' } }, axisLabel: { color: '#fff' } },
    yAxis: { type: 'value', max: 1000, axisLine: { lineStyle: { color: '#4a9eff' } }, axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: '#1a4d7a' } } },
    series: [{ data: [0, 993, 0], type: 'bar', barWidth: 30, itemStyle: { color: '#52c41a' } }]
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

  // 添加全屏监听
  document.addEventListener('fullscreenchange', handleFullscreenChange);
});

// 监听全屏状态变化，重新调整图表大小
watch(isFullscreen, async () => {
  await nextTick();
  setTimeout(() => {
    areaChartInstance?.resize();
    trendChartInstance?.resize();
  }, 300);
});

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  areaChartInstance?.dispose();
  trendChartInstance?.dispose();
});
</script>

<style scoped lang="less">
.dashboard {
  width: 100%;
  height: 100vh;
  background: url('@/assets/images/dashboard-bg.jpg') no-repeat center center;
  background-size: cover;
  padding: 15px;
  overflow: hidden;
  position: relative;
}

.title-bar {
  position: relative;
  text-align: center;
  margin-bottom: 35px;
  padding-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;

  // ... existing code ...

  .title {
    font-size: 36px;
    line-height: 36px;
    margin-top: -15px;
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
      transform: translateY(-50%) scale(1.15) translateY(-2px);
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
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.08) 100%);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3), 
                  0 0 10px rgba(0, 212, 255, 0.2),
                  inset 0 1px 1px rgba(255, 255, 255, 0.2);

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
  height: calc(100vh - 110px);
  align-items: stretch;
}

.left,
.right {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  gap: 15px;
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
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.08) 100%);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3), 
                0 0 15px rgba(0, 212, 255, 0.2),
                inset 0 1px 1px rgba(255, 255, 255, 0.2);
    margin-bottom: 15px;

    // 左侧括号装饰
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
      border-radius: 0 0 0 0;
      z-index: 2;
      box-shadow: 0 0 15px rgba(253, 254, 254, 0.5);
    }

    // 右侧括号装饰
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
      border-radius: 0 0 0 0;
      z-index: 2;
      box-shadow: 0 0 15px rgba(249, 252, 252, 0.5);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      position: relative;
      z-index: 1;
    }
    
    // 四周白色模糊渐变遮罩
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
  }

  .trend-panel {
    flex: 0 0 auto;
  }
}

.panel {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3), 
              0 0 15px rgba(0, 212, 255, 0.2),
              inset 0 1px 1px rgba(255, 255, 255, 0.2);
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
    color: #00d4ff;
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 3px solid #00d4ff;
    text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
  }
}

.energy-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .energy-item {
    text-align: center;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 10px;
    border-radius: 6px;
    border: 1px solid rgba(255, 255, 255, 0.18);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
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
    padding: 10px;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);
    min-height: 85px;
    .label {
      color: #8cc5ff;
      font-size: 11px;
      margin-bottom: 5px;
    }

    .value {
      color: #fff;
      font-size: 14px;
      font-weight: bold;
    }
  }
}

.trend-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.trend-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 15px;

  .summary-item {
    text-align: center;
    padding: 8px 15px;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
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
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
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
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
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
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.06) 100%);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2), 
                0 0 8px rgba(0, 212, 255, 0.15),
                inset 0 1px 1px rgba(255, 255, 255, 0.15);

    .icon {
      font-size: 28px;
      color: #00d4ff;
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
    color: #8cc5ff;
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
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 6px;
    box-shadow: 0 1px 5px rgba(0, 0, 0, 0.15), 
                inset 0 1px 1px rgba(255, 255, 255, 0.12);

    span:last-child {
      color: #fff;
      font-weight: bold;
    }
  }
}

:deep(.ant-picker),
:deep(.ant-select-selector) {
  background: linear-gradient(135deg, rgba(0, 80, 120, 0.6) 0%, rgba(0, 100, 150, 0.4) 100%) !important;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
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
.dashboard.is-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  padding: 20px 15px 15px;
  overflow-y: auto;
  overflow-x: hidden;

  .title-bar {
    margin-bottom: 80px;
    padding-top: 0;
  }

  .content {
    height: calc(100vh - 140px);
    align-items: stretch;
  }

  .left,
  .right {
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .panel {
      flex: 1;
      min-height: 0;
    }
  }

  .center {
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .top-bar {
      flex: 0 0 auto;
      margin-bottom: 0;
    }
    
    .factory-view {
      flex: 0 0 auto;
      height: 400px;
      margin-bottom: 0;
    }
    
    .trend-panel {
      flex: 0 0 auto;
      
      .trend-chart-container {
        height: 280px !important;
      }
    }
  }
}
</style>