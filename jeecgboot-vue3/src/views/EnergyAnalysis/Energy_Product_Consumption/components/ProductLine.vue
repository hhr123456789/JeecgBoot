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
      type: 'line';
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
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: (params: any) => {
        const unit = props.unitLabel || 'kWh/件';
        let result = `<div style="font-size: 12px;">${params[0].axisValue}</div>`;
        params.forEach((item: any) => {
          result += `<div style="font-size: 12px; margin-top: 4px;">
            ${item.marker} ${item.seriesName}: ${item.value} ${unit}
          </div>`;
        });
        return result;
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
      right: '4%',
      bottom: '3%',
      top: 50,
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
        color: '#666',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: `单耗(${props.unitLabel || 'kWh/件'})`,
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
    color: ['#5470C6', '#91CC75', '#FAC858', '#EE6666'],
    series: props.chartData.series.map(item => ({
      ...item,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2
      },
      areaStyle: {
        opacity: 0.1
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
        xAxis: {
          data: props.chartData.xAxis.data
        },
        yAxis: {
          name: `单耗(${props.unitLabel || 'kWh/件'})`
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
