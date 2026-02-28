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
    yAxis: {
      type: 'category';
      data: string[];
    };
    series: {
      name: string;
      type: 'bar';
      data: number[];
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
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const item = params[0];
        const unit = props.unitLabel || 'kWh/件';
        return `${item.name}<br/>${item.marker} ${item.value} ${unit}`;
      }
    },
    grid: {
      left: '15%',
      right: '8%',
      bottom: '3%',
      top: 30,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: props.unitLabel || 'kWh/件',
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      axisLine: {
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
    yAxis: {
      type: 'category',
      data: props.chartData.yAxis.data,
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
    series: props.chartData.series.map(item => ({
      ...item,
      barWidth: 20,
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: (params: any) => {
          // 根据数值设置渐变色
          const colors = [
            '#91CC75', // 最低 - 绿色
            '#FAC858', // 中等 - 黄色  
            '#EE6666'  // 最高 - 红色
          ];
          const index = params.dataIndex;
          const totalCount = props.chartData.yAxis.data.length;
          const colorIndex = Math.floor((index / totalCount) * colors.length);
          return colors[Math.min(colorIndex, colors.length - 1)];
        }
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}',
        fontSize: 11,
        color: '#666'
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
        yAxis: {
          data: props.chartData.yAxis.data
        },
        xAxis: {
          name: props.unitLabel || 'kWh/件'
        },
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
