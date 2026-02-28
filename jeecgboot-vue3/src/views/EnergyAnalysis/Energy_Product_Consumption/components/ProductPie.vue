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
  unitLabel: string;
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
        const unit = props.unitLabel || 'kWh';
        return `${params.seriesName} <br/>${params.name}: ${params.value} ${unit} (${params.percent}%)`;
      }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12
      }
    },
    color: ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272'],
    series: props.chartData.series.map(item => ({
      ...item,
      label: {
        show: true,
        formatter: '{b}: {d}%',
        fontSize: 11
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 13,
          fontWeight: 'bold'
        }
      }
    }))
  };
  
  // 应用配置
  chartInstance.setOption(options);
};

// 监听数据变化
watch(
  [() => props.chartData, () => props.unitLabel],
  () => {
    if (chartInstance) {
      chartInstance.setOption({
        series: props.chartData.series.map(item => ({
          ...item,
          data: item.data
        })),
        tooltip: {
          formatter: (params: any) => {
            const unit = props.unitLabel || 'kWh';
            return `${params.seriesName} <br/>${params.name}: ${params.value} ${unit} (${params.percent}%)`;
          }
        }
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
