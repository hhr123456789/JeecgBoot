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
      type: string;
      data: number[];
      itemStyle: {
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

  // 更新图表配置
  updateChart();
};

// 更新图表配置
const updateChart = () => {
  if (!chartInstance) return;

  console.log('📊 CompareChart 更新图表数据:', props.chartData);

  // 检查数据是否有效
  if (!props.chartData || !props.chartData.xAxis || !props.chartData.series) {
    console.warn('⚠️ 图表数据无效:', props.chartData);
    return;
  }

  // 设置图表配置
  const options: EChartsOption = {
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
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.chartData.xAxis.data,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      name: '用电量(kWh)',
      nameTextStyle: {
        color: '#666'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#eee'
        }
      }
    },
    series: props.chartData.series.map(item => ({
      name: item.name,
      type: 'line',
      data: item.data,
      itemStyle: item.itemStyle,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        opacity: 0.1
      }
    }))
  };

  console.log('📈 ECharts配置:', options);

  // 应用配置
  chartInstance.setOption(options, true); // 第二个参数true表示不合并，完全替换
};

// 监听数据变化
watch(
  () => props.chartData,
  (newData) => {
    console.log('👀 CompareChart 监听到数据变化:', newData);
    updateChart();
  },
  { deep: true, immediate: true }
);

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  console.log('🚀 CompareChart 组件挂载，初始化图表');
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