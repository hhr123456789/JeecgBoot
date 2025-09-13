<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>

<script lang="ts" setup>
import { ref, reactive, watchEffect, PropType } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import { cloneDeep } from 'lodash-es';

interface ChartDataItem {
  name: string;
  value: number;
  type: string;
  seriesType: string;
  yAxisIndex?: number;
}

const props = defineProps({
  chartData: {
    type: Array as PropType<ChartDataItem[]>,
    default: () => [],
  },
  option: {
    type: Object,
    default: () => ({}),
  },
  width: {
    type: String as PropType<string>,
    default: '100%',
  },
  height: {
    type: String as PropType<string>,
    default: '400px',
  },
});

const chartRef = ref<HTMLDivElement | null>(null);
const { setOptions } = useECharts(chartRef as any);

const defaultOption: any = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    formatter: function(params: any) {
      let result = params[0].name + '<br/>';
      params.forEach(function(item: any) {
        if (item.seriesName === '能耗') {
          result += '● ' + item.seriesName + ': ' + item.value + ' kWh<br/>';
        } else if (item.seriesName === 'COP') {
          result += '● ' + item.seriesName + ': ' + item.value + '<br/>';
        }
      });
      return result;
    }
  },
  legend: {
    data: ['能耗', 'COP'],
    bottom: 10
  },
  grid: {
    left: '6%',
    right: '6%',
    top: '12%',
    bottom: '18%',
    containLabel: true
  },
  xAxis: {
    type: 'category' as const,
    data: [] as string[],
    axisLabel: {
      fontSize: 12,
      color: '#666'
    },
    axisLine: {
      lineStyle: {
        color: '#e8e8e8'
      }
    }
  },
  yAxis: [
    {
      type: 'value' as const,
      name: '能耗 (kWh)',
      position: 'left',
      axisLabel: {
        formatter: '{value}',
        fontSize: 12,
        color: '#666'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#1890ff'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    {
      type: 'value' as const,
      name: 'COP',
      position: 'right',
      min: 3.0,
      max: 4.2,
      axisLabel: {
        formatter: '{value}',
        fontSize: 12,
        color: '#666'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#ff7875'
        }
      },
      splitLine: {
        show: false
      }
    }
  ],
  series: [] as any[]
});

watchEffect(() => {
  if (props.chartData && props.chartData.length > 0) {
    initCharts();
  }
});

function initCharts() {
  const option = cloneDeep(defaultOption);
  
  // 合并传入的配置
  if (props.option) {
    Object.assign(option, cloneDeep(props.option));
  }
  
  // 获取所有系列类型
  const typeArr = Array.from(new Set(props.chartData.map((item) => item.type)));
  
  // 获取X轴数据
  const xAxisData = Array.from(new Set(props.chartData.map((item) => item.name)));
  
  // 生成系列数据
  const seriesData: any[] = [];
  
  typeArr.forEach((type, index) => {
    const chartArr = props.chartData.filter((item) => type === item.type);
    if (chartArr.length > 0) {
      const seriesItem: any = {
        name: type,
        type: chartArr[0].seriesType,
        data: chartArr.map((item) => item.value),
        yAxisIndex: chartArr[0].yAxisIndex || 0
      };
      
      // 为不同系列设置不同样式
      if (type === '能耗') {
        seriesItem.color = '#1890ff';
        seriesItem.barMaxWidth = 60; // 设置柱子最大宽度
        seriesItem.barCategoryGap = '40%'; // 设置柱子间距
        seriesItem.itemStyle = {
          borderRadius: [4, 4, 0, 0], // 圆角柱状图
          shadowColor: 'rgba(24, 144, 255, 0.2)',
          shadowBlur: 10,
          shadowOffsetY: 3
        };
        seriesItem.emphasis = {
          itemStyle: {
            color: '#40a9ff',
            shadowColor: 'rgba(24, 144, 255, 0.4)',
            shadowBlur: 15
          }
        };
      } else if (type === 'COP') {
        seriesItem.color = '#ff7875';
        seriesItem.lineStyle = {
          width: 3,
          shadowColor: 'rgba(255, 120, 117, 0.3)',
          shadowBlur: 8,
          shadowOffsetY: 2
        };
        seriesItem.symbol = 'circle';
        seriesItem.symbolSize = 8;
        seriesItem.itemStyle = {
          borderWidth: 2,
          borderColor: '#fff',
          shadowColor: 'rgba(255, 120, 117, 0.3)',
          shadowBlur: 5
        };
        seriesItem.emphasis = {
          scale: true,
          scaleSize: 12,
          itemStyle: {
            shadowColor: 'rgba(255, 120, 117, 0.5)',
            shadowBlur: 10
          }
        };
        seriesItem.smooth = true; // 平滑曲线
      }
      
      seriesData.push(seriesItem);
    }
  });
  
  option.series = seriesData;
  option.xAxis.data = xAxisData;
  
  setOptions(option);
}
</script>