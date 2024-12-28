<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import type { Ref } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义props类型
interface Props {
  chartData: {
    xAxis: {
      type: string;
      data: string[];
    };
    yAxis: {
      type: string;
      name: string;
    };
    series: {
      name: string;
      type: string;
      data: number[];
      itemStyle?: {
        color: string;
      };
    }[];
  };
}

// 声明props
const props = defineProps<Props>();

// 图表DOM引用
const chartRef = ref<HTMLElement>();
// 图表实例引用
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    updateChart();
  }
};

// 更新图表
const updateChart = () => {
  if (!chartInstance) return;

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
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
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: props.chartData.xAxis.data
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: props.chartData.yAxis.name,
        nameLocation: 'end',
        nameGap: 15,
        nameTextStyle: {
          align: 'right'
        }
      }
    ],
    series: props.chartData.series.map(item => ({
      name: item.name,
      type: item.type,
      data: item.data,
      itemStyle: item.itemStyle,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      emphasis: {
        focus: 'series'
      }
    }))
  };

  chartInstance.setOption(option);
};

// 监听数据变化
watch(
  () => props.chartData,
  () => {
    updateChart();
  },
  { deep: true }
);

// 监听窗口大小变化
const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  chartInstance?.dispose();
  window.removeEventListener('resize', handleResize);
});
</script> 