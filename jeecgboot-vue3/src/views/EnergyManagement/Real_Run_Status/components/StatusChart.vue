<template>
  <div ref="chartRef" class="h-64"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义组件属性
const props = defineProps<{
  data: any; // 图表数据
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

  let option: EChartsOption;

  // 判断数据类型并设置相应的图表配置
  if (props.data.running !== undefined) {
    // 状态分布饼图
    option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '14',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: props.data.running, name: '运行中', itemStyle: { color: '#52c41a' } },
            { value: props.data.stopped, name: '已停止', itemStyle: { color: '#d9d9d9' } },
            { value: props.data.fault, name: '故障', itemStyle: { color: '#ff4d4f' } }
          ]
        }
      ]
    };
  } else {
    // 参数趋势折线图
    option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: props.data.series.map((item: any) => item.name)
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
        data: props.data.categories
      },
      yAxis: {
        type: 'value'
      },
      series: props.data.series.map((item: any) => ({
        name: item.name,
        type: 'line',
        data: item.data,
        smooth: true
      }))
    };
  }

  chart.setOption(option);
};

// 监听数据变化
watch(
  () => props.data,
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