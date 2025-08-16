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

// 从父组件传递的图表数据中提取基准期和对比期时间
const getTimeAxisData = () => {
  if (!props.chartData?.xAxis?.data) return { baselineDates: [], compareDates: [] };

  // 假设基准期和对比期时间相差7天
  const baselineDates = props.chartData.xAxis.data;
  const compareDates = baselineDates.map(date => {
    try {
      // 将基准期时间加7天得到对比期时间
      const baseDate = new Date(`2025-${date}`);
      const compareDate = new Date(baseDate.getTime() + 7 * 24 * 60 * 60 * 1000);
      return compareDate.toISOString().slice(5, 10); // 返回MM-DD格式
    } catch {
      return date; // 如果转换失败，返回原始值
    }
  });

  return { baselineDates, compareDates };
};

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

  // 获取基准期和对比期时间数据
  const { baselineDates, compareDates } = getTimeAxisData();

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
      formatter: function (params: any) {
        if (!Array.isArray(params) || params.length === 0) return '';

        const idx = params[0].dataIndex;
        const baselineDate = baselineDates[idx] || '';
        const compareDate = compareDates[idx] || '';

        let tooltipContent = `<div style="margin-bottom: 5px; font-weight: bold;">基准期: ${baselineDate} | 对比期: ${compareDate}</div>`;

        params.forEach((param: any) => {
          const color = param.color;
          const value = param.value;
          const unit = 'kWh'; // 可以从props中获取

          if (param.seriesName.includes('节能')) {
            const tag = value >= 0 ? '节约' : '超出';
            tooltipContent += `<span style="color:${color}">●</span> ${param.seriesName}：${tag} ${Math.abs(value).toLocaleString()} ${unit}<br/>`;
          } else {
            tooltipContent += `<span style="color:${color}">●</span> ${param.seriesName}：${value?.toLocaleString()} ${unit}<br/>`;
          }
        });

        return tooltipContent;
      }
    },
    legend: {
      data: props.chartData.series.map(item => item.name),
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: compareDates,
        position: 'top',
        axisLabel: {
          rotate: 45,
          fontSize: 12,
          color: '#666'
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#ddd'
          }
        },
        axisTick: {
          show: true
        }
      },
      {
        type: 'category',
        data: baselineDates,
        position: 'bottom',
        axisLabel: {
          rotate: 45,
          fontSize: 12,
          color: '#666'
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#ddd'
          }
        },
        axisTick: {
          show: true
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '能耗 (kWh)',
        position: 'left',
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
      {
        type: 'value',
        name: '节能量 (kWh)',
        position: 'right',
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
          show: false
        }
      }
    ],
    series: props.chartData.series.map(item => {
      const baseConfig = {
        name: item.name,
        data: item.data,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6
      };

      if (item.type === 'bar' || item.name.includes('节能')) {
        // 节能情况使用柱状图，绑定到底部X轴和右侧Y轴
        return {
          ...baseConfig,
          type: 'bar',
          xAxisIndex: 1,  // 使用底部X轴（基准期时间）
          yAxisIndex: 1,  // 使用右侧Y轴（节能量）
          barWidth: '30%', // 设置柱状图宽度为30%，使其更窄
          itemStyle: {
            color: function(params: any) {
              return params.value >= 0 ? '#52c41a' : '#ff4d4f'; // 正值绿色，负值红色
            }
          }
        };
      } else if (item.name.includes('基准')) {
        // 基准期使用折线图，绑定到底部X轴和左侧Y轴
        return {
          ...baseConfig,
          type: 'line',
          xAxisIndex: 1,  // 使用底部X轴（基准期时间）
          yAxisIndex: 0,  // 使用左侧Y轴（能耗）
          itemStyle: {
            color: '#1890ff'
          },
          lineStyle: {
            color: '#1890ff'
          },
          areaStyle: {
            opacity: 0.1,
            color: '#1890ff'
          }
        };
      } else {
        // 对比期使用折线图，绑定到顶部X轴和左侧Y轴
        return {
          ...baseConfig,
          type: 'line',
          xAxisIndex: 0,  // 使用顶部X轴（对比期时间）
          yAxisIndex: 0,  // 使用左侧Y轴（能耗）
          itemStyle: {
            color: '#52c41a'
          },
          lineStyle: {
            color: '#52c41a'
          },
          areaStyle: {
            opacity: 0.1,
            color: '#52c41a'
          }
        };
      }
    })
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