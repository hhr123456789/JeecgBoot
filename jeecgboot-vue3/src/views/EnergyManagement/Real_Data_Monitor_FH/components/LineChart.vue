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
      unit?: string;
      deviceName?: string;
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
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function(params: any) {
        if (Array.isArray(params) && params.length > 0) {
          const param = params[0];
          const seriesData = props.chartData.series.find((s: any) => s.name === param.seriesName);
          const unit = seriesData?.unit || '';
          const deviceName = seriesData?.deviceName || '1号设备';

          // 根据不同的数据类型显示不同的格式
          let displayText = '';
          if (param.seriesName === '负荷率') {
            displayText = `负荷：${param.value}${unit}`;
          } else if (param.seriesName === '有功功率') {
            displayText = `有功功率：${param.value}${unit}`;
          } else {
            displayText = `${param.seriesName}：${param.value}${unit}`;
          }

          return `
            <div style="padding: 6px 10px; background: rgba(50, 50, 50, 0.9); border-radius: 4px; color: white; font-size: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.2);">
              <div style="margin-bottom: 2px;">${deviceName}</div>
              <div>${displayText}</div>
            </div>
          `;
        }
        return '';
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
        series: props.chartData.series.map(item => ({
          name: item.name,
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