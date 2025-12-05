<template>
  <div class="p-4 carbon-overview">
    <!-- KPI 卡片 -->
    <div class="carbon-hero mb-4">
      <a-card class="hero-card" :style="{ background: 'linear-gradient(135deg,#0f62fe,#5ab2ff)' }">
        <div class="hero-content">
          <div class="hero-label">本月碳排放（tCO₂e）</div>
          <div class="hero-value">{{ kpiData.monthEmission }}</div>
          <div class="hero-sub">
            同比
            <a-tag class="trend-tag trend-down">{{ kpiData.monthYoY }}</a-tag>
          </div>
        </div>
      </a-card>
      
      <a-card class="hero-card" :style="{ background: 'linear-gradient(135deg,#2fc59e,#8ae2c4)' }">
        <div class="hero-content">
          <div class="hero-label">碳强度（kgCO₂e / 万元产值）</div>
          <div class="hero-value">{{ kpiData.carbonIntensity }}</div>
          <div class="hero-sub">
            较去年
            <a-tag class="trend-tag trend-down">{{ kpiData.intensityDrop }}</a-tag>
          </div>
        </div>
      </a-card>
      
      <a-card class="hero-card" :style="{ background: 'linear-gradient(135deg,#ffa940,#ffd9a6)' }">
        <div class="hero-content">
          <div class="hero-label">新能源替代率</div>
          <div class="hero-value">{{ kpiData.renewableRate }}</div>
          <div class="hero-sub">本月新增 210MWh 绿电</div>
        </div>
      </a-card>
      
      <a-card class="hero-card" :style="{ background: 'linear-gradient(135deg,#722ed1,#b692f6)' }">
        <div class="hero-content">
          <div class="hero-label">减排项目完成率</div>
          <div class="hero-value">{{ kpiData.projectCompletion }}</div>
          <div class="hero-sub">年度目标 11 / 15 个</div>
        </div>
      </a-card>
    </div>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="mb-4 second-row">
      <a-col :span="16">
        <a-card title="碳排放趋势（近 12 个月）" :loading="loading" class="equal-height-card">
          <div ref="trendChartRef" class="chart-large"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="排放构成（范围一 / 范围二 / 范围三）" :loading="loading" class="equal-height-card">
          <div ref="scopeChartRef" class="chart"></div>
          <div class="legend-list mt-2">
            <div v-for="item in scopeData" :key="item.name" class="legend-row">
              <strong>{{ item.name }}</strong>
              <span>{{ item.value }} t · {{ item.percent }}%</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="third-row">
      <a-col :span="8">
        <a-card title="能源来源排放贡献" :loading="loading" class="equal-height-card">
          <div ref="sourceChartRef" class="chart equal-height-chart"></div>
        </a-card>
      </a-col>
      <a-col :span="8" style="padding-right: 16px;">
        <a-card title="高排放车间 TOP5" :loading="loading" class="table-card equal-height-card">
          <a-table
            :dataSource="topWorkshops"
            :columns="workshopColumns"
            :pagination="false"
            size="small"
            rowKey="name"
            class="equal-height-table"
          />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="减排行动跟踪" :loading="loading" class="equal-height-card">
          <a-list :dataSource="actionList" size="small" class="equal-height-list">
            <template #renderItem="{ item }">
              <a-list-item class="action-item">
                <div class="action-content" style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                  <div class="action-info" style="flex: 1; margin-right: 12px;">
                    <strong>{{ item.title }}</strong>
                    <span>{{ item.dept }} · {{ item.plan }} · {{ item.saving }}</span>
                  </div>
                  <a-tag :color="item.status === '已完成' ? 'green' : 'orange'" class="action-tag" style="flex-shrink: 0; align-self: center;">
                    {{ item.status }}
                  </a-tag>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import { Card, Row, Col, Tag, Table, List } from 'ant-design-vue';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const ATag = Tag;
const ATable = Table;
const AList = List;
const AListItem = List.Item;

const loading = ref(true);

// KPI 数据
const kpiData = ref({
  monthEmission: '1,688',
  carbonIntensity: '52.6',
  renewableRate: '37.8%',
  projectCompletion: '73%',
  monthYoY: '-4.2%',
  intensityDrop: '-6.8%'
});

// 排放构成数据
const scopeData = ref([
  { name: '范围一（直接排放）', value: 920, percent: '54.5' },
  { name: '范围二（购电购热）', value: 530, percent: '31.4' },
  { name: '范围三（供应链）', value: 238, percent: '14.1' }
]);

// 高排放车间数据
const topWorkshops = ref([
  { name: '电解车间', emission: 312, mom: '+3.8%', driver: '直流能耗' },
  { name: '铸造车间', emission: 265, mom: '+1.6%', driver: '熔炼天然气' },
  { name: '轧制车间', emission: 241, mom: '-2.1%', driver: '热处理工序优化' },
  { name: '压铸车间', emission: 226, mom: '-4.6%', driver: '余热回收' },
  { name: '动力站', emission: 188, mom: '-1.4%', driver: '锅炉负荷调度' }
]);

const workshopColumns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', customRender: ({ index }: any) => index + 1 },
  { title: '车间', dataIndex: 'name', key: 'name' },
  { title: '本月排放 (t)', dataIndex: 'emission', key: 'emission' },
  { title: '环比', dataIndex: 'mom', key: 'mom' },
  { title: '主要驱动', dataIndex: 'driver', key: 'driver' }
];

// 减排行动数据
const actionList = ref([
  { title: '铝熔炉余热回收', dept: '熔炼车间', status: '进行中', plan: 'Q4 完成', saving: '预计减排 1,200 t/年' },
  { title: '屋顶光伏扩容 (5MWp)', dept: '能源中心', status: '已完成', plan: '9 月并网', saving: '预计减排 2,300 t/年' },
  { title: '电机变频改造', dept: '设备部', status: '进行中', plan: '阶段 2', saving: '预计减排 620 t/年' },
  { title: '绿色物流合同', dept: '供应链', status: '已完成', plan: '合同已签', saving: '预计减排 410 t/年' }
]);

// 图表引用
const trendChartRef = ref<HTMLDivElement | null>(null);
const scopeChartRef = ref<HTMLDivElement | null>(null);
const sourceChartRef = ref<HTMLDivElement | null>(null);

const { setOptions: setTrendOptions } = useECharts(trendChartRef);
const { setOptions: setScopeOptions } = useECharts(scopeChartRef);
const { setOptions: setSourceOptions } = useECharts(sourceChartRef);

// 生成最近12个月
const months = computed(() => {
  return Array.from({ length: 12 }, (_, i) => {
    const d = new Date();
    d.setMonth(d.getMonth() - (11 - i));
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  });
});

const monthEmission = [2280, 2140, 2105, 2050, 1988, 1950, 1902, 1874, 1820, 1765, 1720, 1688];
const baseline = monthEmission.map(v => Math.round(v * 1.06));

onMounted(() => {
  setTimeout(() => {
    loading.value = false;
    initCharts();
  }, 500);
});

function initCharts() {
  // 碳排放趋势图
  setTrendOptions({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      confine: true,
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#333',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: (params: any) => {
        const [cur, base] = params;
        return `${cur.axisValue}<br/>实际排放：${cur.data} t<br/>目标排放：${base.data} t`;
      }
    },
    legend: { data: ['实际排放', '目标排放'], top: 10 },
    grid: { left: 40, right: 20, top: 50, bottom: 30 },
    xAxis: {
      type: 'category',
      data: months.value,
      axisLine: { lineStyle: { color: '#dfe6f0' } },
      axisLabel: { color: '#6b778c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eef2f7' } },
      axisLabel: { color: '#6b778c' }
    },
    series: [
      {
        name: '实际排放',
        type: 'line',
        data: monthEmission,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#1677ff', width: 3 },
        areaStyle: { color: 'rgba(22,119,255,0.12)' }
      },
      {
        name: '目标排放',
        type: 'line',
        data: baseline,
        smooth: true,
        lineStyle: { color: '#2fc59e', width: 2, type: 'dashed' }
      }
    ]
  });

  // 排放构成饼图
  setScopeOptions({
    tooltip: {
      trigger: 'item',
      confine: true,
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#333',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      formatter: '{b}: {c} t ({d}%)'
    },
    color: ['#1677ff', '#2fc59e', '#ffa940'],
    series: [{
      name: '排放范围',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '55%'],
      label: { formatter: '{b}\n{d}%' },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: scopeData.value.map(item => ({ name: item.name, value: item.value }))
    }]
  });

  // 能源来源排放贡献柱状图
  setSourceOptions({
    grid: { left: 50, right: 10, top: 30, bottom: 40 },
    tooltip: {
      trigger: 'axis',
      confine: false,
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#333',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(22, 119, 255, 0.1)'
        }
      },
      formatter: (params: any) => {
        const item = params[0];
        return `${item.axisValue}<br/>${item.value} tCO₂e`;
      }
    },
    xAxis: {
      type: 'category',
      data: ['锅炉天然气', '购网电', '工艺蒸汽', '柴油叉车', '废水处理'],
      axisLabel: { color: '#6b778c' },
      axisLine: { lineStyle: { color: '#dfe6f0' } }
    },
    yAxis: {
      type: 'value',
      name: 'tCO₂e',
      axisLabel: { color: '#6b778c' },
      splitLine: { lineStyle: { color: '#eef2f7' } }
    },
    series: [{
      type: 'bar',
      data: [580, 420, 265, 132, 98],
      barWidth: 36,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#69a6ff' },
            { offset: 1, color: '#1677ff' }
          ]
        }
      },
      emphasis: {
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#8bb9ff' },
              { offset: 1, color: '#4899ff' }
            ]
          }
        }
      },
      label: { show: true, position: 'top', color: '#6b778c' }
    }]
  });
}
</script>

<style lang="less" scoped>
.carbon-overview {
  .carbon-hero {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;

    .hero-card {
      border-radius: 16px;
      color: #fff;
      position: relative;
      overflow: hidden;
      height: 100%;

      &::after {
        content: '';
        position: absolute;
        inset: 0;
        background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, .25), transparent 55%);
        pointer-events: none;
      }

      :deep(.ant-card-body) {
        padding: 0;
        height: 100%;
      }

      .hero-content {
        padding: 18px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 100%;
      }

      .hero-label {
        font-size: 13px;
        opacity: .9;
      }

      .hero-value {
        font-size: 32px;
        font-weight: 800;
        margin: 12px 0 8px;
      }

      .hero-sub {
        font-size: 12px;
        opacity: .8;
      }

      .trend-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 999px;
        background: rgba(255, 255, 255, .16);
        color: #fff;
        border: none;

        &.trend-down::before {
          content: '▼';
          font-size: 10px;
        }

        &.trend-up::before {
          content: '▲';
          font-size: 10px;
        }
      }
    }
  }

  .chart {
    width: 100%;
    height: 260px;
    position: relative;
    z-index: 1;
  }

  .chart-large {
    width: 100%;
    height: 400px;
    position: relative;
    z-index: 1;
  }

  // 第二行卡片高度统一
  .second-row {
    .ant-col {
      display: flex;
      flex-direction: column;
    }

    .equal-height-card {
      flex: 1;
      display: flex;
      flex-direction: column;
      position: relative;

      :deep(.ant-card-body) {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 16px;
        position: relative;
        overflow: visible;
      }
    }
  }

  // 统一第三行模块高度
  .third-row {
    .ant-col {
      display: flex;
      flex-direction: column;
    }
  }

  .equal-height-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    position: relative;
    z-index: 997;

    :deep(.ant-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 16px;
      height: calc(100% - 57px); // 减去标题高度
      position: relative;
      overflow: visible;
      z-index: 998;
    }

    :deep(.ant-card) {
      pointer-events: auto;
    }
  }

  .equal-height-chart {
    flex: 1;
    min-height: 280px !important;
    width: 100% !important;
    position: relative !important;
    z-index: 999 !important;
    pointer-events: auto !important;
    background: transparent !important;
    overflow: visible !important;
    transform: translateZ(0);
    
    :deep(canvas) {
      pointer-events: auto !important;
    }
  }

  .equal-height-table {
    height: 100%;

    .ant-table-content {
      height: 100%;
    }

    .ant-table-body {
      max-height: 280px !important;
      height: 280px !important;
      overflow-y: auto;
    }
  }

  .equal-height-list {
    flex: 1;
    height: 100%;

    :deep(.ant-list-items) {
      max-height: 280px;
      height: 280px;
      overflow-y: auto;
      padding: 0 16px;
    }
  }

  // 修复对齐问题 - 移除，已在上方统一处理
  // .third-row {
  //   .ant-col:nth-child(2) {
  //     padding-right: 16px !important;
  //   }
  // }

  // 表格样式调整
  .table-card {
    :deep(.ant-table-wrapper) {
      height: 100%;

      .ant-spin-nested-loading,
      .ant-spin-container {
        height: 100%;
      }

      .ant-table {
        height: 100%;
        display: flex;
        flex-direction: column;

        .ant-table-container {
          flex: 1;
          height: auto;
        }
      }
    }
  }

  .legend-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 16px;
    position: relative;
    z-index: 0;

    .legend-row {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #6b778c;

      strong {
        color: #2b3a55;
      }
    }
  }

  .action-item {
    padding: 18px 0;
    border-bottom: 1px dashed #e6ecf5;

    &:last-child {
      border-bottom: none;
    }

    :deep(.ant-list-item-meta) {
      align-items: center !important;
    }

    .action-content {
      display: flex !important;
      align-items: center !important;
      justify-content: space-between !important;
      width: 100% !important;
    }

    .action-info {
      flex: 1 !important;
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-width: 0 !important;

      strong {
        font-size: 14px;
        line-height: 1.6;
      }

      span {
        font-size: 12px;
        color: #6b778c;
        line-height: 1.6;
      }
    }

    .action-tag {
      margin-left: auto !important;
      flex-shrink: 0 !important;
    }
  }

  // 强制应用的样式 - 已移至 .action-item 中统一处理
  :deep(.ant-list-item) {
    padding: 0 !important;
  }

  // 确保 ECharts 容器不被遮挡
  :deep(.ant-card) {
    position: relative;
    overflow: visible !important;
  }

  :deep(.ant-card-body) {
    overflow: visible !important;
  }

  :deep(.ant-table-small) {
    font-size: 12px;
  }

  @media (max-width: 1200px) {
    .carbon-hero {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .carbon-hero {
      grid-template-columns: 1fr;
    }
  }
}
</style>
