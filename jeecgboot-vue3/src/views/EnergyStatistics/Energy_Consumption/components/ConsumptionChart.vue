<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义props
const props = defineProps<{
  chartData: {
    title: string;
    unit: string;
    categories: string[];
    series: Array<{
      name: string;
      unit: string;
      data: number[];
    }>;
  };
  chartId: string;
  chartType: 'line' | 'bar';
}>();

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 销毁已存在的实例
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  
  // 创建图表实例
  chartInstance = echarts.init(chartRef.value);
  
  updateChart();
};

// 更新图表
const updateChart = () => {
  if (!chartInstance || !props.chartData) return;

  const { categories, series } = props.chartData;
  
  // 设置图表配置
  const options: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: props.chartType === 'line' ? 'cross' : 'shadow'
      },
      formatter: function(params: any) {
        let result = `${params[0].axisValue}<br/>`;
        params.forEach((param: any) => {
          const seriesData = series.find(s => s.name === param.seriesName);
          const unit = seriesData?.unit || props.chartData.unit || '';
          result += `${param.marker}${param.seriesName}: ${param.value} ${unit}<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: series.map(s => s.name),
      bottom: '0%',
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: series.length > 1 ? '12%' : '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
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
      name: `能耗(${props.chartData.unit})`,
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
    series: series.map((s, index) => ({
      name: s.name,
      type: props.chartType,
      data: s.data,
      smooth: props.chartType === 'line',
      symbol: props.chartType === 'line' ? 'circle' : undefined,
      symbolSize: props.chartType === 'line' ? 6 : undefined,
      barWidth: props.chartType === 'bar' ? '60%' : undefined,
      lineStyle: props.chartType === 'line' ? {
        width: 2,
        color: getColor(index)
      } : undefined,
      itemStyle: {
        color: getColor(index)
      }
    }))
  };
  
  // 应用配置
  chartInstance.setOption(options, true);
};

// 获取颜色
const getColor = (index: number) => {
  const colors = ['#2ecc71', '#ff9f40', '#3498db', '#e74c3c', '#9b59b6', '#f39c12'];
  return colors[index % colors.length];
};

// 监听数据变化
watch(
  () => props.chartData,
  () => {
    nextTick(() => {
      updateChart();
    });
  },
  { deep: true }
);

// 监听图表类型变化
watch(
  () => props.chartType,
  () => {
    nextTick(() => {
      updateChart();
    });
  }
);

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  nextTick(() => {
    initChart();
  });
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