<template>
  <div ref="chartRef" class="h-full w-full"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import type { EChartsOption } from 'echarts';
import * as echarts from 'echarts';

const props = defineProps<{
  chartData: {
    categories: string[];
    series: Array<{
      name: string;
      data: number[];
    }>;
  };
}>();

const chartRef = ref<HTMLElement>();
let chart: echarts.ECharts | null = null;

const initChart = () => {
  if (!chartRef.value) return;
  
  chart = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chart) return;

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: props.chartData.series.map(item => item.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.chartData.categories,
      axisLabel: {
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: props.chartData.series.map(item => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      showSymbol: false,
      emphasis: {
        focus: 'series'
      }
    }))
  };

  chart.setOption(option);
};

// 监听数据变化
watch(
  () => props.chartData,
  () => updateChart(),
  { deep: true }
);

// 处理窗口大小变化
const handleResize = () => {
  chart?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  chart?.dispose();
  window.removeEventListener('resize', handleResize);
});
</script> 