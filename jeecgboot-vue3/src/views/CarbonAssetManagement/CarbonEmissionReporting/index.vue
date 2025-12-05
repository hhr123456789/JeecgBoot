<template>
  <div class="carbon-emission-reporting p-4">
    <!-- 报告封面 -->
    <div class="report-cover" id="cover">
      <div class="cover-left">
        <div class="cover-tag">
          <Tag color="blue">温室气体排放报告</Tag>
        </div>
        <h1 class="cover-title">{{ reportData.title }}</h1>
        <p class="cover-desc">{{ reportData.description }}</p>
      </div>
      <div class="cover-meta">
        <div class="meta-item">
          <span>报告周期</span>
          <strong>{{ reportData.period }}</strong>
        </div>
        <div class="meta-item">
          <span>适用范围</span>
          <strong>{{ reportData.scope }}</strong>
        </div>
        <div class="meta-item">
          <span>累计排放</span>
          <strong>{{ reportData.total }}</strong>
        </div>
        <div class="meta-item">
          <span>生成日期</span>
          <strong>{{ reportData.updateTime }}</strong>
        </div>
      </div>
    </div>

    <!-- 报告布局 -->
    <div class="report-layout">
      <!-- 报告导航 -->
      <aside class="report-nav">
        <a-card class="nav-card">
          <div class="nav-title">报告大纲</div>
          <div class="nav-items">
            <a
              v-for="(item, index) in navItems"
              :key="index"
              :class="['nav-item', { active: activeNav === item.href }]"
              :href="`#${item.href}`"
              @click="setActiveNav(item.href)"
            >
              {{ item.text }}
            </a>
          </div>
        </a-card>
      </aside>

      <!-- 报告内容 -->
      <div class="report-content">
        <!-- 报告条件设置 -->
        <a-card title="报告条件设置" id="conditions" class="mb-4">
          <div class="report-toolbar">
            <div class="toolbar-item">
              <label>开始日期</label>
              <!-- @ts-ignore -->
              <a-date-picker
                v-model:value="startDate"
                style="width: 130px"
                format="YYYY-MM-DD"
              />
            </div>
            <div class="toolbar-item">
              <label>结束日期</label>
              <!-- @ts-ignore -->
              <a-date-picker
                v-model:value="endDate"
                style="width: 130px"
                format="YYYY-MM-DD"
              />
            </div>
            <div class="toolbar-item">
              <label>适用范围</label>
              <a-select v-model:value="selectedScope" style="width: 130px">
                <a-select-option value="all">全厂</a-select-option>
                <a-select-option value="电解车间">电解车间</a-select-option>
                <a-select-option value="铸造车间">铸造车间</a-select-option>
                <a-select-option value="轧制车间">轧制车间</a-select-option>
                <a-select-option value="动力站">动力站</a-select-option>
              </a-select>
            </div>
            <div class="toolbar-buttons">
              <a-button type="primary" @click="generateReport">生成报告</a-button>
              <a-button @click="exportReport">导出 PDF</a-button>
            </div>
          </div>
        </a-card>

        <!-- 汇总卡片 -->
        <div class="report-grid" id="summaryCards">
          <a-card class="report-card">
            <div class="report-label">报告时间范围</div>
            <div class="report-value">{{ summaryData.period }}</div>
            <div class="report-sub">{{ summaryData.scope }}</div>
          </a-card>
          
          <a-card class="report-card">
            <div class="report-label">温室气体排放总量 (tCO₂e)</div>
            <div class="report-value">{{ summaryData.total }}</div>
            <div class="report-sub">涵盖三大范围排放，已扣除绿色电力抵扣</div>
          </a-card>
          
          <a-card class="report-card">
            <div class="report-label">碳强度 (kgCO₂e / 万元产值)</div>
            <div class="report-value">{{ summaryData.intensity }}</div>
            <div class="report-sub">较上一报告期</div>
          </a-card>
        </div>

        <!-- 企业基本情况 -->
        <section class="report-section" id="section-basic">
          <div class="section-title">一、企业基本情况</div>
          <div class="text-block">
            <strong>基本信息：</strong>{{ basicInfo }}
          </div>
        </section>

        <!-- 温室气体排放情况 -->
        <section class="report-section" id="section-ghg">
          <div class="section-title">二、温室气体排放情况</div>
          <div class="chart-grid">
            <a-card class="chart-card">
              <template #title>范围排放量占比</template>
              <div ref="scopePieRef" class="chart"></div>
            </a-card>
            
            <a-card class="chart-card">
              <template #title>车间排放占比</template>
              <div ref="workshopPieRef" class="chart"></div>
            </a-card>
            
            <a-card class="chart-card">
              <template #title>排放趋势</template>
              <div ref="trendChartRef" class="chart"></div>
            </a-card>
            
            <a-card class="chart-card">
              <template #title>范围排放柱状图</template>
              <div ref="scopeBarRef" class="chart"></div>
            </a-card>
          </div>
        </section>

        <!-- 报告总结与建议 -->
        <section class="report-section" id="section-summary">
          <div class="section-title">三、报告总结与建议</div>
          <div class="text-block">
            <strong>报告总结：</strong>
            <ul>
              <li v-for="(item, index) in summaryPoints" :key="index">{{ item }}</li>
            </ul>
            <strong>建议措施：</strong>继续推进能效改善、绿色采购及数字化碳管理，确保年度碳目标达成。
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import { Card, Row, Col, Tag, Button, Select, DatePicker, message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const ATag = Tag;
const AButton = Button;
const ASelect = Select;
const ASelectOption = Select.Option;
const ADatePicker = DatePicker;

// 图表引用
const scopePieRef = ref<HTMLDivElement | null>(null);
const workshopPieRef = ref<HTMLDivElement | null>(null);
const trendChartRef = ref<HTMLDivElement | null>(null);
const scopeBarRef = ref<HTMLDivElement | null>(null);

// @ts-ignore
const { setOptions: setScopePieOptions } = useECharts(scopePieRef as any);
// @ts-ignore
const { setOptions: setWorkshopPieOptions } = useECharts(workshopPieRef as any);
// @ts-ignore
const { setOptions: setTrendChartOptions } = useECharts(trendChartRef as any);
// @ts-ignore
const { setOptions: setScopeBarOptions } = useECharts(scopeBarRef as any);

// 活动导航
const activeNav = ref('cover');

// 报告筛选条件
const startDate = ref<Dayjs>();
const endDate = ref<Dayjs>();
const selectedScope = ref<string>('all');

const reportFilters = reactive({
  startDate,
  endDate,
  scope: selectedScope
});

// 导航项
const navItems = [
  { text: '封面', href: 'cover' },
  { text: '报告条件', href: 'conditions' },
  { text: '企业基本情况', href: 'section-basic' },
  { text: '温室气体排放', href: 'section-ghg' },
  { text: '总结与建议', href: 'section-summary' }
];

// 数据集
const datasets = {
  all: {
    total: 18920,
    intensity: 51.6,
    basic: '报告期内，全厂产量 42.6 万吨，产值 36.7 亿元，综合能耗 9.8 PJ。能源结构以天然气、电力与蒸汽为主，已建成 15MWp 分布式光伏。',
    scope: [
      { name: '范围一（直接排放）', value: 10380 },
      { name: '范围二（购电/购热）', value: 6020 },
      { name: '范围三（供应链）', value: 2520 }
    ],
    workshop: [
      { name: '电解车间', value: 4980 },
      { name: '铸造车间', value: 3760 },
      { name: '轧制车间', value: 3180 },
      { name: '动力站', value: 2840 },
      { name: '精加工', value: 2160 }
    ],
    trend: {
      months: ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10'],
      actual: [1820, 1760, 1746, 1724, 1690, 1682, 1640, 1605, 1584, 1569],
      target: [1890, 1860, 1830, 1800, 1770, 1740, 1710, 1680, 1650, 1620]
    },
    summary: [
      '范围一排放占总量 54.8%，主要来源于动力站锅炉、铸造燃料及电解过程。',
      '范围二同比下降 6.2%，得益于绿电交易与能效优化。',
      '范围三排放因外协物流与供应商来料略有增加，需要与供应链伙伴共同推进。'
    ]
  },
  '电解车间': {
    total: 4980,
    intensity: 62.4,
    basic: '电解车间拥有 420 槽位，平均电流效率 92.4%。报告期内使用可再生电力 52 GWh，占车间购电的 21%。',
    scope: [
      { name: '范围一（阳极消耗、炭素烘烤）', value: 3120 },
      { name: '范围二（购电）', value: 1660 },
      { name: '范围三（辅材供应）', value: 200 }
    ],
    workshop: [
      { name: '电解一期', value: 1820 },
      { name: '电解二期', value: 1860 },
      { name: '电解三期', value: 1300 }
    ],
    trend: {
      months: ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10'],
      actual: [520, 505, 498, 492, 486, 482, 476, 470, 466, 460],
      target: [540, 528, 516, 504, 492, 480, 468, 456, 444, 432]
    },
    summary: [
      '电解槽阳极效应次数平均 0.12 次/槽·天，处在行业优秀水平。',
      '得益于直流系统优化，单位电耗同比下降 1.3%。',
      '后续建议继续推进废阳极炭粉回收及槽室余热利用项目。'
    ]
  }
};

// 当前报告数据
const reportData = reactive({
  title: '',
  description: '',
  period: '',
  scope: '',
  total: '',
  updateTime: ''
});

// 汇总数据
const summaryData = reactive({
  period: '',
  scope: '',
  total: '',
  intensity: ''
});

// 基础信息
const basicInfo = ref('');

// 总结要点
const summaryPoints = ref<string[]>([]);

// 设置活动导航
const setActiveNav = (href: string) => {
  activeNav.value = href;
};

// 生成报告
const generateReport = () => {
  const currentScope = selectedScope.value;
  const currentData = datasets[currentScope as keyof typeof datasets] || datasets.all;
  const startDateText = startDate.value?.format('YYYY-MM-DD') || '2025-01-01';
  const endDateText = endDate.value?.format('YYYY-MM-DD') || '2025-10-31';
  const periodText = `${startDateText} ~ ${endDateText}`;
  
  // 更新报告数据
  Object.assign(reportData, {
    title: `温室气体排放报告 · ${currentScope === 'all' ? '全厂' : currentScope}`,
    description: `本报告依据 ${periodText} 的能源与生产活动数据，自动汇总温室气体排放情况，并对范围一至范围三的主要来源进行分析。`,
    period: periodText,
    scope: currentScope === 'all' ? '全厂' : currentScope,
    total: `${currentData.total.toLocaleString()} t`,
    updateTime: new Date().toLocaleDateString()
  });

  // 更新汇总数据
  Object.assign(summaryData, {
    period: periodText,
    scope: currentScope === 'all' ? '范围：全厂' : `范围：${currentScope}`,
    total: currentData.total.toLocaleString(),
    intensity: currentData.intensity.toFixed(1)
  });

  // 更新基础信息和总结
  basicInfo.value = currentData.basic;
  summaryPoints.value = currentData.summary;

  // 初始化图表
  initCharts(currentData);

  message.success('报告生成成功');
};

// 导出报告
const exportReport = () => {
  message.info('导出功能开发中，后续将支持 PDF / Excel 导出。');
};

// 初始化图表
const initCharts = (data: any) => {
  try {
    // 范围排放饼图
    setScopePieOptions({
      tooltip: { trigger: 'item' },
      color: ['#1677ff', '#2fc59e', '#ffa940', '#722ed1'],
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        label: { formatter: '{b}\n{d}%' },
        data: data.scope
      }]
    });

    // 车间排放饼图
    setWorkshopPieOptions({
      tooltip: { trigger: 'item' },
      color: ['#5b8ff9', '#5ad8a6', '#5d7092', '#f6bd16', '#e86452'],
      series: [{
        type: 'pie',
        roseType: 'radius',
        radius: ['30%', '70%'],
        label: { formatter: '{b}\n{d}%' },
        data: data.workshop
      }]
    });

    // 排放趋势图
    setTrendChartOptions({
      tooltip: { trigger: 'axis' },
      legend: { data: ['实际排放', '目标排放'] },
      grid: { left: 60, right: 20, top: 50, bottom: 50 },
      xAxis: {
        type: 'category',
        data: data.trend.months,
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
          data: data.trend.actual,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#1677ff', width: 3 },
          areaStyle: { color: 'rgba(22,119,255,0.12)' }
        },
        {
          name: '目标排放',
          type: 'line',
          data: data.trend.target,
          smooth: true,
          lineStyle: { color: '#2fc59e', type: 'dashed', width: 2 }
        }
      ]
    });

    // 范围排放柱状图
    setScopeBarOptions({
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 20, top: 30, bottom: 50 },
      xAxis: {
        type: 'category',
        data: data.scope.map((item: any) => item.name),
        axisLabel: { color: '#6b778c' },
        axisLine: { lineStyle: { color: '#dfe6f0' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#6b778c' },
        splitLine: { lineStyle: { color: '#eef2f7' } }
      },
      series: [{
        type: 'bar',
        data: data.scope.map((item: any) => item.value),
        barWidth: 40,
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
        label: { show: true, position: 'top', color: '#6b778c' }
      }]
    });
  } catch (error) {
    console.error('图表初始化错误:', error);
    message.error('图表初始化失败，请刷新页面重试');
  }
};

onMounted(() => {
  try {
    // 初始化默认日期
    startDate.value = dayjs('2025-01-01');
    endDate.value = dayjs('2025-10-31');
    
    // 生成默认报告
    generateReport();

    // 监听导航点击
    const navLinks = document.querySelectorAll('.nav-item');
    navLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const href = (e.target as HTMLElement).getAttribute('href')?.substring(1);
        if (href) {
          setActiveNav(href);
          document.getElementById(href)?.scrollIntoView({ behavior: 'smooth' });
        }
      });
    });
  } catch (error) {
    console.error('组件初始化错误:', error);
    message.error('组件加载失败，请刷新页面重试');
  }
});
</script>

<style lang="less" scoped>
.carbon-emission-reporting {
  .report-cover {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 20px;
    padding: 28px;
    border-radius: 18px;
    background: linear-gradient(120deg, rgba(15,98,254,0.15), rgba(32,203,255,0.08));
    border: 1px solid rgba(15,98,254,0.2);
    box-shadow: 0 20px 50px rgba(15,98,254,0.08);

    .cover-left {
      flex: 1 1 360px;

      .cover-tag {
        display: inline-flex;
        align-items: center;
        gap: 6px;

        .ant-tag {
          border-radius: 999px;
          background: rgba(255, 255, 255, 0.5);
          color: #0f62fe;
          font-weight: 600;
          border: none;
        }
      }

      .cover-title {
        font-size: clamp(26px, 4vw, 40px);
        margin: 14px 0 10px;
        color: #0f1f52;
        line-height: 1.2;
      }

      .cover-desc {
        font-size: 15px;
        color: #2b3a55;
        line-height: 1.7;
      }
    }

    .cover-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      min-width: 260px;

      .meta-item {
        flex: 1 1 120px;
        background: rgba(255, 255, 255, 0.85);
        border-radius: 12px;
        padding: 14px;
        border: 1px solid rgba(255, 255, 255, 0.5);

        span {
          display: block;
          font-size: 12px;
          color: #6b778c;
        }

        strong {
          display: block;
          font-size: 18px;
          color: #0f65e6;
          margin-top: 6px;
        }
      }
    }
  }

  .report-layout {
    display: flex;
    gap: 24px;
    margin-top: 20px;

    .report-nav {
      width: 220px;
      position: sticky;
      top: 84px;
      align-self: flex-start;

      .nav-card {
        border-radius: 14px;

        :deep(.ant-card-body) {
          padding: 16px;
        }
      }

      .nav-title {
        font-weight: 700;
        margin-bottom: 12px;
        color: #1f3a72;
      }

      .nav-items {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .nav-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 6px;
          border-radius: 8px;
          color: #6b778c;
          text-decoration: none;
          font-size: 14px;
          cursor: pointer;

          &::before {
            content: '•';
            color: #d0d7e6;
          }

          &.active {
            background: rgba(22, 119, 255, 0.1);
            color: #1677ff;
            font-weight: 600;

            &::before {
              color: #1677ff;
            }
          }

          &:hover:not(.active) {
            background: rgba(22, 119, 255, 0.08);
          }
        }
      }
    }

    .report-content {
      flex: 1;
    }
  }

  .report-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px;
    border-bottom: 1px dashed #e6ecf5;

    .toolbar-item {
      display: flex;
      flex-direction: column;
      gap: 4px;

      label {
        font-size: 12px;
        color: #6b778c;
      }

      .ant-select,
      .ant-picker {
        min-width: 130px;
      }
    }

    .toolbar-buttons {
      display: flex;
      gap: 8px;
      margin-left: auto;

      .ant-btn {
        padding-inline: 22px;
      }
    }
  }

  .report-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin: 18px 0;

    .report-card {
      border-radius: 14px;

      :deep(.ant-card-body) {
        padding: 18px;
      }

      .report-label {
        font-size: 13px;
        color: #6b778c;
      }

      .report-value {
        font-size: 28px;
        font-weight: 800;
        margin: 8px 0;
        color: #1677ff;
      }

      .report-sub {
        font-size: 12px;
        color: #6b778c;
      }
    }
  }

  .report-section {
    margin-top: 18px;

    .section-title {
      font-size: 18px;
      font-weight: 700;
      color: #1f3a72;
      margin-bottom: 12px;
    }

    .text-block {
      background: #f8fafc;
      border: 1px solid #e6ecf5;
      border-radius: 12px;
      padding: 18px;
      line-height: 1.6;
      font-size: 14px;
      color: #2b3a55;

      strong {
        color: #0f65e6;
      }

      ul {
        margin: 6px 0 0 18px;
        padding: 0;

        li {
          margin-bottom: 4px;
        }
      }
    }
  }

  .chart-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
    margin-top: 16px;

    .chart-card {
      border-radius: 12px;
      overflow: hidden;

      :deep(.ant-card-head-title) {
        font-size: 14px;
      }

      :deep(.ant-card-body) {
        padding: 0;
      }

      .chart {
        height: 320px;
        width: 100%;
      }
    }
  }

  @media (max-width: 1280px) {
    .report-layout {
      flex-direction: column;

      .report-nav {
        width: 100%;
        position: static;

        .nav-items {
          flex-direction: row;
          flex-wrap: wrap;
        }
      }
    }
  }

  @media (max-width: 1080px) {
    .report-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .chart-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 768px) {
    .report-grid {
      grid-template-columns: 1fr;
    }

    .report-toolbar {
      flex-direction: column;

      .toolbar-buttons {
        margin-left: 0;
        justify-content: flex-start;
      }
    }
  }
}
</style>
