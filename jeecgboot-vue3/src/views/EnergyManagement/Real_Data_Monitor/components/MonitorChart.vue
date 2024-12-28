<template>
  <div ref="chartRef" class="h-80"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义组件属性
const props = defineProps<{
  chartData: {
    categories: string[];
    series: Array<{
      name: string;
      data: number[];
    }>;
  };
}>();

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chart: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 创建图表实例
  chart = echarts.init(chartRef.value);
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
  
  // 更新图表
  updateChart();
};

// 处理窗口大小变化
const handleResize = () => {
  chart?.resize();
};

// 更新图表
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
      data: props.chartData.series.map(item => item.name)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.chartData.categories,
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
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
      areaStyle: {
        opacity: 0.1
      },
      lineStyle: {
        width: 2
      },
      itemStyle: {
        color: '#1890ff'
      }
    }))
  };

  chart.setOption(option);
};

// 监听数据变化
watch(
  () => props.chartData,
  () => {
    updateChart();
  },
  { deep: true }
);

// 生命周期钩子
onMounted(() => {
  initChart();
});

onUnmounted(() => {
  // 清理事件监听和图表实例
  window.removeEventListener('resize', handleResize);
  chart?.dispose();
  chart = null;
});
</script> 