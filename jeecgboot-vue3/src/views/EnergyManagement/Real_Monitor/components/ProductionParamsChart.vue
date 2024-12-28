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
    data: number[];
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
    radar: {
      indicator: props.chartData.categories.map(name => ({
        name,
        max: 100
      })),
      center: ['50%', '50%'],
      radius: '65%'
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: props.chartData.data,
            name: '参数值',
            areaStyle: {
              opacity: 0.3
            },
            lineStyle: {
              width: 2
            }
          }
        ]
      }
    ]
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