<template>
  <div ref="chartRef" style="width: 100%; height: 300px;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义props
const props = defineProps<{
  chartData: {
    series: {
      name: string;
      type: 'pie';
      radius: string[];
      data: {
        value: number;
        name: string;
      }[];
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
      trigger: 'item',
      formatter: (params: any) => {
        return `${params.name}<br/>占比: ${params.value}%`;
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      textStyle: {
        color: '#374151'
      }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12,
        color: '#374151'
      },
      formatter: (name: string) => {
        const item = props.chartData.series[0].data.find(d => d.name === name);
        return `${name} ${item?.value}%`;
      }
    },
    color: ['#3b82f6', '#06b6d4', '#8b5cf6'],  // 科技蓝色系
    series: [
      {
        ...props.chartData.series[0],
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#374151'
          },
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(59, 130, 246, 0.3)'
          }
        },
        itemStyle: {
          borderColor: '#ffffff',
          borderWidth: 2
        }
      }
    ]
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
        series: [
          {
            ...props.chartData.series[0],
            data: props.chartData.series[0].data
          }
        ]
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