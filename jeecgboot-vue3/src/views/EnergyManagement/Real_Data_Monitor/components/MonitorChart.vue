<template>
  <div ref="chartRef" class="h-80 chart-container"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

// 定义组件属性
const props = defineProps<{
  chartData: {
    categories: string[];
    series: Array<{
      name: string;
      data: number[];
      itemStyle?: {
        color: string;
      };
    }>;
  };
  chartId: string;
  activeIndex: number;
  chartType?: string; // 图表类型：line 或 bar
}>();

// 定义事件
const emit = defineEmits<{
  (e: 'mouseOnIndex', index: number): void;
  (e: 'mouseOut'): void;
}>();

// 默认颜色数组
const defaultColors = ['#1890ff', '#52c41a', '#faad14', '#fa8c16', '#722ed1'];

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chart: echarts.ECharts | null = null;
// 防止重复更新标志
let isUpdating = false;

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) {
    console.log('Chart container not found');
    return;
  }
  
  //console.log(`Initializing chart: ${props.chartId}`);
  
  // 确保容器有尺寸
  if (chartRef.value.offsetWidth === 0 || chartRef.value.offsetHeight === 0) {
    //console.log('Chart container has no size, waiting...');
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  
  // 创建图表实例
  chart = echarts.init(chartRef.value);
  //console.log(`Chart ${props.chartId} initialized:`, chart);
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
  
  // 更新图表
  updateChart();
  
  // 设置事件监听 - 延迟执行确保图表完全渲染
  setTimeout(() => {
    setupMouseEvents();
  }, 100);
};

// 设置鼠标事件监听
const setupMouseEvents = () => {
  if (!chart) {
    console.log('Chart not available for event setup');
    return;
  }
  
  //console.log(`Setting up mouse events for chart: ${props.chartId}`);
  
  // 清除之前的事件监听
  chart.getZr().off('mousemove');
  chart.getZr().off('globalout');
  
  // 监听整个图表区域的鼠标移动
  chart.getZr().on('mousemove', (e) => {
    // 防止在更新过程中触发事件
    if (isUpdating) return;
    
    // 获取鼠标在图表中的位置
    const pointInPixel = [e.offsetX, e.offsetY];
    
    // 判断是否在绘图区域内
    if (chart!.containPixel('grid', pointInPixel)) {
      // 转换为数据索引
      const pointInGrid = chart!.convertFromPixel({ gridIndex: 0 }, pointInPixel);
      if (pointInGrid) {
        const xIndex = Math.round(pointInGrid[0]);
        if (xIndex >= 0 && xIndex < props.chartData.categories.length) {
          emit('mouseOnIndex', xIndex);
        }
      }
    }
  });
  
  // 监听鼠标离开图表
  chart.getZr().on('globalout', () => {
    if (!isUpdating) {
      emit('mouseOut');
    }
  });
  
  // 额外的 mouseleave 监听
  if (chartRef.value) {
    chartRef.value.addEventListener('mouseleave', () => {
      if (!isUpdating) {
        emit('mouseOut');
      }
    });
  }
};

// 处理窗口大小变化
const handleResize = () => {
  chart?.resize();
};

// 更新图表
const updateChart = () => {
  if (!chart || isUpdating) {
    return;
  }

  isUpdating = true;
  //console.log(`Updating chart: ${props.chartId}`);

  const option: EChartsOption = {
    backgroundColor: '#ffffff',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        animation: false,
        crossStyle: {
          color: '#999'
        }
      },
      confine: true,
      enterable: true,
      formatter: (params: any) => {
        if (!Array.isArray(params)) return '';
        
        const time = params[0].name;
        let html = `<div style="font-weight:bold;margin-bottom:5px;">${time}</div>`;
        
        params.forEach((item: any) => {
          const color = item.color;
          const seriesName = item.seriesName;
          const value = item.value;
          html += `
            <div style="display:flex;align-items:center;margin:3px 0;">
              <span style="display:inline-block;width:10px;height:10px;background-color:${color};margin-right:5px;border-radius:50%;"></span>
              <span style="margin-right:15px;font-size:12px;">${seriesName}:</span>
              <span style="font-weight:bold;font-size:13px;">${value}</span>
            </div>
          `;
        });
        
        return html;
      }
    },
    legend: {
      data: props.chartData.series.map(item => item.name),
      textStyle: {
        color: '#333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: (props.chartType || 'line') === 'bar', // 柱状图需要边界间隙
      data: props.chartData.categories,
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#eee'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    series: props.chartData.series.map((item, index) => {
      const color = item.itemStyle?.color || defaultColors[index % defaultColors.length];
      const isLineChart = (props.chartType || 'line') === 'line';

      let seriesConfig: any = {
        name: item.name,
        type: isLineChart ? 'line' : 'bar',
        data: item.data,
        emphasis: {
          focus: 'series',
          itemStyle: {
            borderWidth: 3,
            borderColor: '#fff',
            shadowBlur: 10,
            shadowColor: color
          }
        },
        itemStyle: {
          color: color,
          borderWidth: 2,
          borderColor: '#fff'
        }
      };

      if (isLineChart) {
        // 曲线图特有配置
        seriesConfig.smooth = true;
        seriesConfig.symbol = 'circle';
        seriesConfig.symbolSize = 8;
        seriesConfig.showSymbol = false;
        seriesConfig.areaStyle = {
          opacity: 0.2,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${color}4D` }, // 30% opacity
            { offset: 1, color: `${color}0D` }  // 5% opacity
          ])
        };
        seriesConfig.lineStyle = {
          width: 3,
          color: color
        };
      } else {
        // 柱状图特有配置
        seriesConfig.barWidth = '60%';
        seriesConfig.itemStyle.borderRadius = [4, 4, 0, 0];
      }

      return seriesConfig;
    })
  };

  chart.setOption(option, true);
  
  // 延迟重置更新标志
  setTimeout(() => {
    isUpdating = false;
  }, 100);
};

// 显示指定索引的提示
const showTooltip = (index: number) => {
  if (!chart || index < 0 || index >= props.chartData.categories.length || isUpdating) {
    return;
  }
  
  //console.log(`Showing tooltip for chart ${props.chartId} at index ${index}`);
  
  chart.dispatchAction({
    type: 'showTip',
    seriesIndex: 0,
    dataIndex: index
  });
};

// 隐藏提示框
const hideTooltip = () => {
  if (!chart || isUpdating) return;
  
  //console.log(`Hiding tooltip for chart ${props.chartId}`);
  
  chart.dispatchAction({
    type: 'hideTip'
  });
};

// 监听数据变化 - 只在真正的数据变化时更新
watch(
  () => JSON.stringify(props.chartData),
  (newData, oldData) => {
    if (newData !== oldData) {
      //console.log(`Chart data changed for ${props.chartId}`);
      nextTick(() => {
        updateChart();
        // 重新设置事件监听
        setTimeout(() => {
          setupMouseEvents();
        }, 200);
      });
    }
  }
);

// 监听图表类型变化
watch(
  () => props.chartType,
  (newType, oldType) => {
    if (newType !== oldType) {
      //console.log(`Chart type changed for ${props.chartId} from ${oldType} to ${newType}`);
      nextTick(() => {
        updateChart();
      });
    }
  }
);

// 监听activeIndex变化 - 不触发数据更新
watch(
  () => props.activeIndex,
  (newIndex, oldIndex) => {
    // 只有在非更新状态下才处理
    if (!isUpdating && newIndex !== oldIndex) {
      //console.log(`Chart ${props.chartId} activeIndex changed from ${oldIndex} to ${newIndex}`);
      if (newIndex >= 0) {
        showTooltip(newIndex);
      } else {
        hideTooltip();
      }
    }
  }
);

// 生命周期钩子
onMounted(() => {
  //console.log(`Mounting chart component: ${props.chartId}`);
  nextTick(() => {
    initChart();
  });
});

onUnmounted(() => {
  //console.log(`Unmounting chart component: ${props.chartId}`);
  window.removeEventListener('resize', handleResize);
  
  // 清理图表事件监听
  if (chart) {
    chart.getZr().off('mousemove');
    chart.getZr().off('globalout');
    chart.dispose();
  }
  
  // 清理DOM事件监听
  if (chartRef.value) {
    chartRef.value.removeEventListener('mouseleave', () => {});
  }
  
  chart = null;
});
</script>

<style scoped>
.chart-container {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
</style>