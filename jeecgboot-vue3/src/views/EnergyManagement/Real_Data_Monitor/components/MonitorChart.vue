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
      yAxisIndex?: number; // 指定使用哪个Y轴
      paramType?: string; // 参数类型，用于自动分配Y轴
    }>;
  };
  chartId: string;
  activeIndex: number;
  chartType?: string; // 图表类型：line 或 bar
  enableMultiYAxis?: boolean; // 是否启用多Y轴
}>();

// 定义事件
const emit = defineEmits<{
  (e: 'mouseOnIndex', index: number): void;
  (e: 'mouseOut'): void;
}>();

// 默认颜色数组
const defaultColors = ['#1890ff', '#52c41a', '#faad14', '#fa8c16', '#722ed1'];

// 参数类型配置
const paramTypeConfig = {
  current: { name: '电流', unit: 'A', color: '#1890ff', yAxisIndex: 0 },
  voltage: { name: '电压', unit: 'V', color: '#52c41a', yAxisIndex: 1 },
  power: { name: '功率', unit: 'kW', color: '#faad14', yAxisIndex: 2 },
  powerCount: { name: '电量', unit: 'kWh', color: '#ff7875', yAxisIndex: 3 }, // 电量使用不同颜色区分
  powerFactor: { name: '功率因数', unit: '', color: '#fa8c16', yAxisIndex: 4 },
  frequency: { name: '频率', unit: 'Hz', color: '#722ed1', yAxisIndex: 5 },
  temperature: { name: '温度', unit: '℃', color: '#13c2c2', yAxisIndex: 6 },
  pressure: { name: '压力', unit: 'Pa', color: '#eb2f96', yAxisIndex: 7 },
  instantFlow: { name: '瞬时流量', unit: 'm³/h', color: '#52c41a', yAxisIndex: 8 },
  totalFlow: { name: '累计流量', unit: 'm³', color: '#faad14', yAxisIndex: 9 }
};

// 图表DOM引用
const chartRef = ref<HTMLElement | null>(null);
// 图表实例
let chart: echarts.ECharts | null = null;
// 防止重复更新标志
let isUpdating = false;

// 自动分配Y轴索引
const getYAxisConfig = () => {
  if (!props.enableMultiYAxis) {
    // 单Y轴模式，所有系列使用同一个Y轴
    return {
      yAxisConfigs: [{
        type: 'value',
        name: '',
        position: 'left',
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
      }],
      seriesYAxisMap: props.chartData.series.map(() => 0)
    };
  }

  // 多Y轴模式
  const usedParamTypes = new Set<string>();
  const yAxisConfigs: any[] = [];
  const seriesYAxisMap: number[] = [];

  props.chartData.series.forEach((series, index) => {
    const paramType = series.paramType || 'current';
    const config = paramTypeConfig[paramType] || paramTypeConfig.current;

    if (!usedParamTypes.has(paramType)) {
      usedParamTypes.add(paramType);
      const yAxisIndex = yAxisConfigs.length;

      yAxisConfigs.push({
        type: 'value',
        name: `${config.name}${config.unit ? `(${config.unit})` : ''}`,
        position: yAxisIndex % 2 === 0 ? 'left' : 'right',
        offset: Math.floor(yAxisIndex / 2) * 60,
        min: paramType === 'powerFactor' ? 0 : 0, // 功率因数范围0-1
        max: paramType === 'powerFactor' ? 1 : undefined, // 功率因数最大值1
        splitLine: {
          show: yAxisIndex === 0, // 只显示第一个Y轴的分割线
          lineStyle: {
            type: 'dashed',
            color: '#eee'
          }
        },
        axisLabel: {
          color: config.color,
          formatter: (value: number) => {
            // 根据参数类型决定数值格式
            if (paramType === 'powerFactor') {
              return value.toFixed(2); // 功率因数保留2位小数
            } else if (value >= 1000000) {
              return (value / 1000000).toFixed(1) + 'M'; // 百万
            } else if (value >= 1000) {
              return (value / 1000).toFixed(1) + 'k'; // 千
            }
            return value.toString();
          }
        },
        axisLine: {
          lineStyle: {
            color: config.color
          }
        }
      });

      seriesYAxisMap[index] = yAxisIndex;
    } else {
      // 找到已存在的Y轴索引
      const existingIndex = yAxisConfigs.findIndex(axis =>
        axis.name.includes(config.name)
      );
      seriesYAxisMap[index] = existingIndex;
    }
  });

  return { yAxisConfigs, seriesYAxisMap };
};

// 初始化图表
const initChart = async () => {
  try {
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

    // 销毁已存在的图表实例
    if (chart) {
      chart.dispose();
      chart = null;
    }

    // 创建图表实例
    chart = echarts.init(chartRef.value, null, {
      renderer: 'canvas',
      useDirtyRect: false // 禁用脏矩形优化，避免动画问题
    });
    //console.log(`Chart ${props.chartId} initialized:`, chart);

    // 监听窗口大小变化
    window.addEventListener('resize', handleResize);

    // 更新图表
    updateChart();

    // 设置事件监听 - 延迟执行确保图表完全渲染
    setTimeout(() => {
      setupMouseEvents();
    }, 100);
  } catch (error) {
    console.error('图表初始化失败:', error);
  }
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

  // 获取Y轴配置
  const { yAxisConfigs, seriesYAxisMap } = getYAxisConfig();

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

          // 获取对应系列的参数类型和单位
          const seriesData = props.chartData.series.find(s => s.name === seriesName);
          const paramType = seriesData?.paramType || 'current';
          const config = paramTypeConfig[paramType] || paramTypeConfig.current;

          // 根据系列名称确定更准确的单位
          let unit = config.unit;
          const lowerSeriesName = seriesName.toLowerCase();

          // 特殊处理电量单位
          if (lowerSeriesName.includes('电量') || lowerSeriesName.includes('kwh')) {
            unit = 'kWh';
          } else if (lowerSeriesName.includes('功率') && !lowerSeriesName.includes('因数')) {
            unit = 'kW';
          } else if (lowerSeriesName.includes('电流')) {
            unit = 'A';
          } else if (lowerSeriesName.includes('电压')) {
            unit = 'V';
          } else if (lowerSeriesName.includes('温度')) {
            unit = '℃';
          } else if (lowerSeriesName.includes('频率')) {
            unit = 'Hz';
          } else if (lowerSeriesName.includes('功率因数')) {
            unit = '';
          }

          // 格式化数值：保留2位小数
          const formattedValue = typeof value === 'number' ? value.toFixed(2) : value;
          const displayValue = unit ? `${formattedValue}${unit}` : formattedValue;

          html += `
            <div style="display:flex;align-items:center;margin:3px 0;">
              <span style="display:inline-block;width:10px;height:10px;background-color:${color};margin-right:5px;border-radius:50%;"></span>
              <span style="margin-right:15px;font-size:12px;">${seriesName}:</span>
              <span style="font-weight:bold;font-size:13px;">${displayValue}</span>
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
      left: props.enableMultiYAxis ? '8%' : '3%',
      right: props.enableMultiYAxis ? '8%' : '4%',
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
    yAxis: yAxisConfigs,
    series: props.chartData.series.map((item, index) => {
      const paramType = item.paramType || 'current';
      const paramConfig = paramTypeConfig[paramType] || paramTypeConfig.current;
      const color = item.itemStyle?.color || paramConfig.color || defaultColors[index % defaultColors.length];
      const isLineChart = (props.chartType || 'line') === 'line';

      let seriesConfig: any = {
        name: item.name,
        type: isLineChart ? 'line' : 'bar',
        data: item.data,
        yAxisIndex: seriesYAxisMap[index] || 0, // 分配Y轴索引
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

  // 数据验证：确保所有系列数据长度一致
  const series = Array.isArray(option.series) ? option.series : [option.series];
  const dataLengths = series.map((s: any) => s.data?.length || 0);
  const isDataConsistent = dataLengths.every((len: number) => len === dataLengths[0]);

  if (!isDataConsistent) {
    console.warn('⚠️ 系列数据长度不一致，跳过此次更新:', dataLengths);
    isUpdating = false;
    return;
  }

  // 使用合并模式更新图表，避免动画问题
  chart.setOption(option, false, true);

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