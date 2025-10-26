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
import type { PieChartDataVO } from '../api/types';

interface Props {
  chartData: PieChartDataVO;
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

    if (!props.chartData || !props.chartData.series || props.chartData.series.length === 0) {
      return;
    }

    chartInstance = echarts.init(chartRef.value, null, {
      renderer: 'canvas',
      width: 'auto',
      height: 'auto',
    });

    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left', top: 'center' },
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
      series: props.chartData.series.map((seriesItem) => ({
        name: seriesItem.name,
        type: seriesItem.type,
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}: {d}%' },
        emphasis: { label: { show: true, fontSize: '14', fontWeight: 'bold' } },
        labelLine: { show: true },
        data: seriesItem.data?.map((item) => ({ value: item.value, name: item.name, percentage: item.percentage })) || [],
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
    if (!newData || !newData.series || newData.series.length === 0) return;
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

