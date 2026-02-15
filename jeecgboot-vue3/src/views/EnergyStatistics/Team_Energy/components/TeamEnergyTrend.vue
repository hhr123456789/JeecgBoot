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
      type: 'bar' | 'line';
      stack?: string;
      data: number[];
      itemStyle?: {
        color: string;
      };
      smooth?: boolean;
    }[];
    markLine?: {
      data: {
        type: string;
        name: string;
        lineStyle: {
          color: string;
          type: string;
        };
      }[];
    };
  };
  chartType?: 'bar' | 'line';
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
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      textStyle: {
        color: '#374151'
      }
    },
    legend: {
      data: props.chartData.series.map(item => item.name),
      bottom: '0%',
      textStyle: {
        fontSize: 12,
        color: '#374151'
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
          color: '#d1d5db'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '能耗',
      nameTextStyle: {
        color: '#6b7280',
        fontSize: 12
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#d1d5db'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f3f4f6'
        }
      }
    },
    series: props.chartData.series.map(item => ({
      ...item,
      barWidth: '20%',  // 细长柱形，宽度20%
      barGap: '30%',    // 柱间距
      emphasis: {
        focus: 'series',
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(59, 130, 246, 0.3)'
        }
      }
    })),
    markLine: props.chartData.markLine
  };
  
  // 应用配置
  chartInstance.setOption(options);
};

// 监听数据变化和图表类型变化
watch(
  [() => props.chartData, () => props.chartType],
  ([newData, newType]) => {
    if (chartInstance) {
      const chartType = newType || 'bar';
      chartInstance.setOption({
        xAxis: {
          data: newData.xAxis.data
        },
        legend: {
          data: newData.series.map(item => item.name),
        },
        series: newData.series.map(item => ({
          ...item,
          type: chartType,
          barWidth: '20%',
          barGap: '30%',
          emphasis: {
            focus: 'series',
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(59, 130, 246, 0.3)'
            }
          }
        })),
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