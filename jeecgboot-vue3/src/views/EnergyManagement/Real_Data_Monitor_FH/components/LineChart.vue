<template>
  <div :id="chartId" class="chart-container" style="width: 100%; height: 400px;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

// Props定义
interface Props {
  chartData: {
    xAxis: {
      type: string;
      data: string[];
    };
    series: Array<{
      name: string;
      type: string;
      data: number[];
      itemStyle?: {
        color: string;
      };
      unit?: string;
      deviceName?: string;
    }>;
  };
  chartId: string;
}

const props = withDefaults(defineProps<Props>(), {
  chartId: 'line-chart'
});

// 图表实例
let chartInstance: echarts.ECharts | null = null;

// 初始化图表
const initChart = () => {
  const chartDom = document.getElementById(props.chartId);
  if (!chartDom) {
    console.error(`Chart container with id "${props.chartId}" not found`);
    return;
  }

  // 销毁已存在的图表实例
  if (chartInstance) {
    chartInstance.dispose();
  }

  chartInstance = echarts.init(chartDom);
  updateChart();
};

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return;

  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function(params: any) {
        let html = `<div style="font-weight: bold; margin-bottom: 5px;">${params[0].axisValue}</div>`;
        params.forEach((param: any) => {
          const unit = param.data?.unit || getUnitFromSeriesName(param.seriesName);
          html += `<div style="margin: 2px 0;">
            ${param.marker}
            <span style="display: inline-block; width: 100px;">${param.seriesName}:</span>
            <span style="font-weight: bold;">${param.value}${unit}</span>
          </div>`;
        });
        return html;
      }
    },
    legend: {
      data: props.chartData.series.map(s => s.name),
      top: 10,
      type: 'scroll'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.chartData.xAxis.data,
      axisLabel: {
        rotate: props.chartData.xAxis.data.length > 12 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: function(value: number) {
          // 根据数据范围自动格式化
          if (value >= 1000) {
            return (value / 1000).toFixed(1) + 'k';
          }
          return value.toString();
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: props.chartData.series.map(seriesItem => ({
      name: seriesItem.name,
      type: 'line',
      data: seriesItem.data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2
      },
      itemStyle: {
        color: seriesItem.itemStyle?.color || getDefaultColor(seriesItem.name)
      },
      emphasis: {
        focus: 'series',
        blurScope: 'coordinateSystem'
      }
    }))
  };

  chartInstance.setOption(option, true);
};

// 根据系列名称获取单位
const getUnitFromSeriesName = (seriesName: string): string => {
  if (seriesName.includes('功率') || seriesName.includes('负荷')) {
    if (seriesName.includes('率')) {
      return '%';
    }
    return 'kW';
  }
  return '';
};

// 获取默认颜色
const getDefaultColor = (seriesName: string): string => {
  const colors = [
    '#1890ff', '#52c41a', '#faad14', '#f5222d', 
    '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'
  ];
  
  // 根据系列名称生成一个稳定的颜色索引
  let hash = 0;
  for (let i = 0; i < seriesName.length; i++) {
    hash = seriesName.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash) % colors.length;
  return colors[index];
};

// 响应式处理
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

// 监听数据变化
watch(() => props.chartData, () => {
  nextTick(() => {
    updateChart();
  });
}, { deep: true });

// 监听chartId变化
watch(() => props.chartId, () => {
  nextTick(() => {
    initChart();
  });
});

onMounted(() => {
  nextTick(() => {
    initChart();
    window.addEventListener('resize', handleResize);
  });
});

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>