import { ref, reactive, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';

// ==================== 类型定义 ====================
export interface TeamInfo {
  code: string;
  name: string;
  shiftType?: string;
  orgCode?: string;
}

export interface StatsData {
  totalConsumption: string;
  totalCost: string;
  carbonEmission: string;
  standardCoal: string;
}

export interface ChartSeriesItem {
  name: string;
  type: 'bar' | 'line';
  stack?: string;
  data: number[];
  itemStyle?: {
    color: string;
  };
  smooth?: boolean;
}

export interface TrendChartData {
  xAxis: {
    type: string;
    data: string[];
  };
  series: ChartSeriesItem[];
}

export interface RankingItem {
  name: string;
  value: number;
  unit: string;
}

export interface PieChartData {
  series: {
    name: string;
    type: 'pie';
    radius: string[];
    data: {
      value: number;
      name: string;
    }[];
  }[];
}

export interface TableRecord {
  key: string;
  teamName: string;
  shiftType: string;
  statTime: string;
  consumption: string;
  cost: string;
  carbon: string;
  coal: string;
  peak: string;
  flat: string;
  valley: string;
}

// ==================== useTeamEnergy 组合式函数 ====================
export function useTeamEnergy() {
  // ========== 查询参数 ==========
  const selectedOrgCode = ref<string>('A01B03');
  const selectedTeamCode = ref<string>('all');
  const timeUnit = ref<'day' | 'month' | 'year'>('day');
  const selectedDate = ref<Dayjs>(dayjs());
  const energyType = ref<string>('all');
  const loading = ref<boolean>(false);

  // ========== 图表控制 ==========
  const trendChartType = ref<'bar' | 'line'>('bar');
  const trendMetric = ref<string>('consumption');

  // ========== 计算属性 ==========
  const pickerType = computed(() => {
    switch (timeUnit.value) {
      case 'month':
        return 'month';
      case 'year':
        return 'year';
      default:
        return 'date';
    }
  });

  const dateFormat = computed(() => {
    switch (timeUnit.value) {
      case 'day':
        return 'YYYY-MM-DD';
      case 'month':
        return 'YYYY-MM';
      case 'year':
        return 'YYYY';
      default:
        return 'YYYY-MM-DD';
    }
  });

  // ========== 统计数据 ==========
  const statsData = reactive<StatsData>({
    totalConsumption: '162.00',
    totalCost: '129.60',
    carbonEmission: '161.51',
    standardCoal: '19.92'
  });

  // ========== 趋势图数据 ==========
  const trendChartData = computed<TrendChartData>(() => {
    // 科技蓝色系配色方案(单色、无渐变、扁平化)
    const teamColors = {
      'A-1': '#3b82f6',  // 蓝色
      'A-2': '#06b6d4',  // 青色
      'B-1': '#8b5cf6',  // 紫色
      '1-A': '#0ea5e9',  // 天蓝
      '2-A': '#6366f1',  // 靛蓝
      '3-A': '#a855f7'   // 紫罗兰
    };
    
    if (timeUnit.value === 'day') {
      // 日维度：按小时统计
      const hourData = ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', 
               '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', 
               '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'];
      
      if (selectedTeamCode.value === 'all') {
        return {
          xAxis: { type: 'category', data: hourData },
          series: [
            {
              name: 'A-1班',
              type: trendChartType.value,
              stack: trendChartType.value === 'bar' ? 'total' : undefined,
              data: [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
              itemStyle: { color: teamColors['A-1'] },
              smooth: trendChartType.value === 'line'
            },
            {
              name: 'A-2班',
              type: trendChartType.value,
              stack: trendChartType.value === 'bar' ? 'total' : undefined,
              data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0, 0],
              itemStyle: { color: teamColors['A-2'] },
              smooth: trendChartType.value === 'line'
            },
            {
              name: 'B-1班',
              type: trendChartType.value,
              stack: trendChartType.value === 'bar' ? 'total' : undefined,
              data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 7, 7],
              itemStyle: { color: teamColors['B-1'] },
              smooth: trendChartType.value === 'line'
            }
          ]
        };
      } else {
        return {
          xAxis: { type: 'category', data: hourData },
          series: [
            {
              name: selectedTeamCode.value,
              type: trendChartType.value,
              data: [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
              itemStyle: { color: teamColors[selectedTeamCode.value as keyof typeof teamColors] || teamColors['A-1'] },
              smooth: trendChartType.value === 'line'
            }
          ]
        };
      }
    } else if (timeUnit.value === 'month') {
      // 月维度：按日统计
      const days = Array.from({ length: 30 }, (_, i) => `${i + 1}日`);
      return {
        xAxis: { type: 'category', data: days },
        series: [
          {
            name: 'A-1班',
            type: trendChartType.value,
            data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 70)),
            itemStyle: { color: teamColors['A-1'] },
            smooth: trendChartType.value === 'line'
          },
          {
            name: 'A-2班',
            type: trendChartType.value,
            data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 60)),
            itemStyle: { color: teamColors['A-2'] },
            smooth: trendChartType.value === 'line'
          },
          {
            name: 'B-1班',
            type: trendChartType.value,
            data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 75)),
            itemStyle: { color: teamColors['B-1'] },
            smooth: trendChartType.value === 'line'
          }
        ]
      };
    } else {
      // 年维度：按月统计
      const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
      return {
        xAxis: { type: 'category', data: months },
        series: [
          {
            name: 'A-1班',
            type: trendChartType.value,
            data: [2100, 2200, 2150, 2300, 2250, 2400, 2350, 2450, 2380, 2500, 2420, 2550],
            itemStyle: { color: teamColors['A-1'] },
            smooth: trendChartType.value === 'line'
          },
          {
            name: 'A-2班',
            type: trendChartType.value,
            data: [1900, 2000, 1950, 2100, 2050, 2200, 2150, 2250, 2180, 2300, 2220, 2350],
            itemStyle: { color: teamColors['A-2'] },
            smooth: trendChartType.value === 'line'
          },
          {
            name: 'B-1班',
            type: trendChartType.value,
            data: [2200, 2300, 2250, 2400, 2350, 2500, 2450, 2550, 2480, 2600, 2520, 2650],
            itemStyle: { color: teamColors['B-1'] },
            smooth: trendChartType.value === 'line'
          }
        ]
      };
    }
  });

  // ========== 排名数据 ==========
  const rankingData = computed<RankingItem[]>(() => {
    const unit = energyType.value === 'all' ? 'tce' : getEnergyUnit();
    return [
      { name: 'B-1班', value: 42.53, unit },
      { name: 'A-1班', value: 41.65, unit },
      { name: 'A-2班', value: 40.15, unit }
    ];
  });

  // ========== 饼图数据 ==========
  const pieChartData = computed<PieChartData>(() => ({
    series: [
      {
        name: '班组用能占比',
        type: 'pie',
        radius: ['50%', '70%'],
        data: [
          { value: 33.5, name: 'A-1班' },
          { value: 32.29, name: 'A-2班' },
          { value: 34.21, name: 'B-1班' }
        ]
      }
    ]
  }));

  // ========== 表格数据 ==========
  const tableData = ref<TableRecord[]>([
    {
      key: '1',
      teamName: 'A-1班',
      shiftType: '早班',
      statTime: '2026-01-15',
      consumption: '84.00',
      cost: '67.20',
      carbon: '83.75',
      coal: '10.33',
      peak: '20.00',
      flat: '40.00',
      valley: '24.00'
    },
    {
      key: '2',
      teamName: 'A-2班',
      shiftType: '中班',
      statTime: '2026-01-15',
      consumption: '36.00',
      cost: '28.80',
      carbon: '35.89',
      coal: '4.43',
      peak: '12.00',
      flat: '18.00',
      valley: '6.00'
    },
    {
      key: '3',
      teamName: 'B-1班',
      shiftType: '晚班',
      statTime: '2026-01-15',
      consumption: '42.00',
      cost: '33.60',
      carbon: '41.87',
      coal: '5.16',
      peak: '14.00',
      flat: '21.00',
      valley: '7.00'
    }
  ]);

  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 3,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条`
  });

  // ========== 工具函数 ==========
  function getEnergyUnit() {
    switch (energyType.value) {
      case '1':
        return 'kWh';
      case '2':
        return 'm³';
      case '8':
        return 'm³';
      case '5':
        return 'm³';
      case 'all':
        return 'tce';
      default:
        return 'kWh';
    }
  }

  function getTrendSubtitle() {
    if (timeUnit.value === 'day') {
      return '按小时统计';
    } else if (timeUnit.value === 'month') {
      return '按日统计';
    } else {
      return '按月统计';
    }
  }

  function getShiftColor(shiftType: string) {
    const colorMap: Record<string, string> = {
      '早班': 'blue',
      '中班': 'green',
      '晚班': 'orange',
      '夜班': 'purple'
    };
    return colorMap[shiftType] || 'default';
  }

  // ========== 事件处理 ==========
  function handleOrgChange(value: string) {
    console.log('部门切换:', value);
    // TODO: 根据部门加载班组列表
    // 调用后端接口: GET /energy/team/listByOrgCode?orgCode=${value}
  }

  function handleTeamChange(value: string) {
    console.log('班组切换:', value);
    selectedTeamCode.value = value;
  }

  async function handleQuery() {
    const params = {
      orgCode: selectedOrgCode.value,
      teamCode: selectedTeamCode.value,
      timeUnit: timeUnit.value,
      date: selectedDate.value.format(dateFormat.value),
      energyType: energyType.value
    };
    
    console.log('查询参数:', params);
    
    loading.value = true;
    try {
      // TODO: 调用后端接口查询数据
      // 1. 查询统计汇总数据: POST /energy/team/getStats
      // 2. 查询趋势图数据: POST /energy/team/getTrendData
      // 3. 查询排名数据: POST /energy/team/getRankingData
      // 4. 查询明细表数据: POST /energy/team/getTableData
      
      // 模拟请求延迟
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      message.success('查询成功');
    } catch (error) {
      message.error('查询失败，请稍后重试');
      console.error('查询错误:', error);
    } finally {
      loading.value = false;
    }
  }

  // ========== 返回值 ==========
  return {
    // 查询参数
    selectedOrgCode,
    selectedTeamCode,
    timeUnit,
    selectedDate,
    energyType,
    loading,
    
    // 图表控制
    trendChartType,
    trendMetric,
    
    // 计算属性
    pickerType,
    dateFormat,
    
    // 数据
    statsData,
    trendChartData,
    rankingData,
    pieChartData,
    tableData,
    pagination,
    
    // 工具函数
    getEnergyUnit,
    getTrendSubtitle,
    getShiftColor,
    
    // 事件处理
    handleOrgChange,
    handleTeamChange,
    handleQuery
  };
}
