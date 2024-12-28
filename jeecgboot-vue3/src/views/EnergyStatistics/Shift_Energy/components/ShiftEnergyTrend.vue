<!-- 班次能耗趋势图组件 -->
<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义props
const props = defineProps<{
  chartData: {
    xAxis: {
      type: string;
      data: string[];
    };
    series: {
      name: string;
      type: 'bar';
      stack?: string;
      data: number[];
      itemStyle?: {
        color: string;
      };
    }[];
  };
}>();

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 创建图表实例
  chartInstance = echarts.init(chartRef.value);
  
  // 设置图表配置
  const options: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: props.chartData.series.map(item => item.name),
      bottom: '0%',
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.chartData.xAxis.data,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '能耗',
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#eee'
        }
      }
    },
    series: props.chartData.series
  };
  
  // 应用配置
  chartInstance.setOption(options);
};

// 监听数据变化
watch(
  () => props.chartData,
  () => {
    if (chartInstance) {
      chartInstance.setOption({
        xAxis: {
          data: props.chartData.xAxis.data
        },
        series: props.chartData.series
      });
    }
  },
  { deep: true }
);

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script> 