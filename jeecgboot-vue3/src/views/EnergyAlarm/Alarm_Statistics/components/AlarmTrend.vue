<template>
  <div ref="chartRef" class="w-full h-[300px]"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import type { PropType } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

const props = defineProps({
  chartData: {
    type: Object as PropType<{
      xAxis: { type: 'category'; data: string[] };
      series: Array<{
        name: string;
        type: string;
        data: number[];
        itemStyle: { color: string };
      }>;
    }>,
    required: true
  }
});

const chartRef = ref<HTMLElement>();
let chart: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value);
    setChartOption();
  }
};

// 设置图表配置
const setChartOption = () => {
  if (!chart) return;

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['超负荷', '异常运行', '故障'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: props.chartData.xAxis.data,
        axisLine: {
          lineStyle: {
            color: '#E5E7EB'
          }
        },
        axisLabel: {
          color: '#6B7280'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        axisLine: {
          show: false
        },
        splitLine: {
          lineStyle: {
            color: '#E5E7EB'
          }
        },
        axisLabel: {
          color: '#6B7280'
        }
      }
    ],
    series: props.chartData.series.map(item => ({
      name: item.name,
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: {
        width: 2
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.1
      },
      emphasis: {
        focus: 'series'
      },
      data: item.data,
      itemStyle: item.itemStyle
    }))
  };

  chart.setOption(option);
};

// 监听数据变化
watch(
  () => props.chartData,
  () => {
    setChartOption();
  },
  { deep: true }
);

// 监听窗口大小变化
const handleResize = () => {
  chart?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时清理
const onUnmounted = () => {
  window.removeEventListener('resize', handleResize);
  chart?.dispose();
};
</script> 