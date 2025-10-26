<template>
  <div class="chart-container" ref="chartRef"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
  min-height: 300px;
  position: relative;
}
</style>

<script setup lang="ts">
import * as echarts from 'echarts';
import { onMounted, watch, ref, nextTick, onUnmounted } from 'vue';
import type { TrendDataVO } from '../api/types';

interface Props {
  chartData: TrendDataVO;
}

const props = defineProps<Props>();
const chartRef = ref<HTMLElement>();
let chartInstance: echarts.ECharts | null = null;

const initChart = () => {
  try {
    if (!chartRef.value) return;

    const container = chartRef.value;
    if (container.clientWidth === 0 || container.clientHeight === 0) {
      setTimeout(() => initChart(), 100);
      return;
    }

    if (chartInstance) {
      try {
        const oldHandler = (chartInstance as any).__resizeHandler;
        if (oldHandler) window.removeEventListener('resize', oldHandler);
      } catch {}
      chartInstance.dispose();
      chartInstance = null;
    }

    const xAxisObj: any = (props.chartData as any).xAxis || (props.chartData as any).xaxis;
    if (!props.chartData || !xAxisObj || !props.chartData.series || props.chartData.series.length === 0) {
      return;
    }

    chartInstance = echarts.init(chartRef.value, null, {
      renderer: 'canvas',
      width: 'auto',
      height: 'auto',
    });

    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } },
      },
      legend: { data: props.chartData.series.map((s) => s.name), top: 10 },
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: (xAxisObj.type || 'category'), boundaryGap: false, data: xAxisObj.data || [] },
      yAxis: { type: 'value', name: '用量', nameLocation: 'end', nameGap: 20 },
      series: props.chartData.series.map((seriesItem) => ({
        name: seriesItem.name,
        type: seriesItem.type || 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3 },
        emphasis: { focus: 'series' },
        data: seriesItem.data || [],
      })),
    };

    chartInstance.setOption(option);
    setTimeout(() => chartInstance && chartInstance.resize(), 100);

    const resizeHandler = () => chartInstance && chartInstance.resize();
    window.addEventListener('resize', resizeHandler);
    (chartInstance as any).__resizeHandler = resizeHandler;
  } catch (e) {
    // swallow
  }
};

onMounted(() => nextTick(() => initChart()));

watch(
  () => props.chartData,
  (newData) => {
    const xAxisObj: any = (newData as any)?.xAxis || (newData as any)?.xaxis;
    const hasValid = !!newData && !!xAxisObj && !!newData.series && newData.series.length > 0;
    if (!hasValid) return;
    nextTick(() => initChart());
  },
  { deep: true },
);

onUnmounted(() => {
  if (chartInstance) {
    try {
      const oldHandler = (chartInstance as any).__resizeHandler;
      if (oldHandler) window.removeEventListener('resize', oldHandler);
    } catch {}
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>
