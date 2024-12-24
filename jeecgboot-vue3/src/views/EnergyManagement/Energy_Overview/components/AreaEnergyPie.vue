<template>
  <div ref="chartRef" class="h-80 w-full"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import type { EChartsOption } from 'echarts';
import * as echarts from 'echarts';

const props = defineProps<{
  chartData: Array<{
    name: string;
    value: number;
    percentage: number;
  }>;
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
      trigger: 'item',
      formatter: (params: any) => {
        const data = props.chartData.find(item => item.name === params.name);
        if (data) {
          return `${data.name}<br/>数值: ${data.value} tce<br/>占比: ${data.percentage}%`;
        }
        return '';
      }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      formatter: (name: string) => {
        const data = props.chartData.find(item => item.name === name);
        if (data) {
          return `${name} (${data.percentage}%)`;
        }
        return name;
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: props.chartData.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: getAreaColor(item.name)
          }
        }))
      }
    ]
  };

  chart.setOption(option);
};

// Custom color mapping for different areas
const getAreaColor = (areaName: string): string => {
  const colorMap: Record<string, string> = {
    '1号车间': '#1890ff',
    '2号车间': '#13c2c2',
    '3号车间': '#52c41a',
    '4号车间': '#faad14',
    '5号车间': '#722ed1'
  };
  return colorMap[areaName] || '#d9d9d9';
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