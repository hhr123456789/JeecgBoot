<template>
  <div ref="chartRef" style="width: 100%; height: 100%;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义props
const props = defineProps<{
  chartData: {
    xAxis: {
      type: 'category';
      data: string[];
    };
    series: {
      name: string;
      type: 'bar';
      yAxisIndex: number;
      data: number[];
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
      top: 10,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '8%',
      bottom: '3%',
      top: 50,
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
        fontSize: 12,
        interval: 0,
        rotate: 15
      }
    },
    yAxis: [
      {
        type: 'value',
        name: props.chartData.series[0]?.name || '产量(件)',
        position: 'left',
        nameTextStyle: {
          color: '#666',
          fontSize: 12
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#5470C6'
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
      {
        type: 'value',
        name: props.chartData.series[1]?.name || '能耗',
        position: 'right',
        nameTextStyle: {
          color: '#666',
          fontSize: 12
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#91CC75'
          }
        },
        axisLabel: {
          color: '#666',
          fontSize: 12
        },
        splitLine: {
          show: false
        }
      }
    ],
    color: ['#5470C6', '#91CC75'],
    series: props.chartData.series.map(item => ({
      ...item,
      barWidth: 20,
      itemStyle: {
        borderRadius: [4, 4, 0, 0]
      }
    }))
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
        yAxis: [
          { name: props.chartData.series[0]?.name || '产量(件)' },
          { name: props.chartData.series[1]?.name || '能耗' }
        ],
        series: props.chartData.series.map(item => ({
          ...item,
          data: item.data
        }))
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
