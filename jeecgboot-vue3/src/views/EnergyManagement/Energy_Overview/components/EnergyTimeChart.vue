<template>
  <div ref="chartRef" class="h-80 w-full"></div>
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
        type: 'shadow'
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
      data: props.chartData.categories,
      boundaryGap: true,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '能耗 (tce)',
      nameLocation: 'middle',
      nameGap: 50,
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: props.chartData.series.map(item => ({
      name: item.name,
      type: 'bar',
      stack: 'total',
      emphasis: {
        focus: 'series'
      },
      data: item.data
    }))
  };

  chart.setOption(option);
};

// Watch for data changes
watch(
  () => props.chartData,
  () => updateChart(),
  { deep: true }
);

// Handle window resize
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