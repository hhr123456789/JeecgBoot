import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import type {
  OrgTreeVO,
  EnergyTypeVO,
  ClassificationQueryParam,
  ClassificationSummaryVO,
  TrendDataVO,
  TableDataVO,
} from '../api/types';
import { getOrgTree, getEnergyTypes, getSummaryData, getTrendData, exportClassificationData } from '../api';

export function useEnergyClassification() {
  // 左侧树
  const orgTreeData = ref<OrgTreeVO[]>([]);
  const selectedOrgKeys = ref<string[]>([]);
  const expandedKeys = ref<string[]>(['1']);
  const searchText = ref<string>('');
  const filteredOrgTreeData = ref<OrgTreeVO[]>([]);
  const selectedOrgInfo = ref<OrgTreeVO | null>(null);

  // 查询参数
  const queryParam = reactive<ClassificationQueryParam>({
    orgCode: '',
    energyType: 'all',
    timeDimension: 'month',
    startDate: '',
    endDate: '',
    includeChildren: true,
  });

  // 时间、能源类型
  const timeUnit = ref<'day' | 'month' | 'year'>('month');
  const selectedDate = ref<Dayjs>(dayjs('2025-11-07')); // 修改为有数据的日期
  const energyType = ref<string>('all');
  const energyTypes = ref<EnergyTypeVO[]>([]);

  // 图表与数据
  const chartType = ref<'consumption' | 'cost'>('consumption');
  const trendType = ref<string>('month');

  const summaryData = ref<ClassificationSummaryVO>({
    statisticsData: {
      totalConsumption: 0,
      electricConsumption: 0,
      waterConsumption: 0,
      gasConsumption: 0,
      totalCost: 0,
      totalCarbonEmission: 0,
    },
    pieChartData: { series: [] },
    tableData: [],
  });

  const trendData = ref<TrendDataVO>({ xAxis: { type: 'category', data: [] }, series: [] });
  const tableData = ref<TableDataVO[]>([]);
  const loading = ref(false);

  // 计算属性
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

  const pickerType = computed(() => {
    switch (timeUnit.value) {
      case 'day':
        return 'date';
      case 'month':
        return 'month';
      case 'year':
        return 'year';
      default:
        return 'date';
    }
  });

  const dynamicColumns = computed(() => {
    const baseColumns = [{ title: '时间', dataIndex: 'time', width: 120, fixed: 'left' as const }];

    if (energyType.value === 'all') {
      return [
        ...baseColumns,
        { title: '电能消耗(kWh)', dataIndex: 'electric', width: 150, align: 'right' as const },
        { title: '水能消耗(m3)', dataIndex: 'water', width: 150, align: 'right' as const },
        { title: '燃气消耗(m3)', dataIndex: 'gas', width: 150, align: 'right' as const },
        { title: '电能成本(元)', dataIndex: 'electricCost', width: 150, align: 'right' as const },
        { title: '水能成本(元)', dataIndex: 'waterCost', width: 150, align: 'right' as const },
        { title: '燃气成本(元)', dataIndex: 'gasCost', width: 150, align: 'right' as const },
        { title: '总成本(元)', dataIndex: 'totalCost', width: 150, align: 'right' as const, fixed: 'right' as const },
      ];
    } else {
      const type = energyTypes.value.find((t) => t.energyType === parseInt(energyType.value));
      return [
        ...baseColumns,
        { title: `${type?.energyName || '能源'}消耗(${type?.energyUnit || ''})`, dataIndex: 'consumption', width: 180, align: 'right' as const },
        { title: '成本(元)', dataIndex: 'cost', width: 150, align: 'right' as const },
        { title: '碳排放(kg)', dataIndex: 'carbonEmission', width: 150, align: 'right' as const, fixed: 'right' as const },
      ];
    }
  });

  // 树和字典加载
  const loadOrgTree = async () => {
    try {
      const data = await getOrgTree();
      orgTreeData.value = data;
      filteredOrgTreeData.value = data;

      if (data.length > 0) {
        const firstOrg = findFirstLeafOrg(data);
        if (firstOrg) {
          selectedOrgKeys.value = [firstOrg.id];
          selectedOrgInfo.value = firstOrg;
          queryParam.orgCode = firstOrg.orgCode;
          const path = findPathById(data, firstOrg.id).map((n) => n.id);
          expandedKeys.value = Array.from(new Set([...(expandedKeys.value || []), ...path]));
        }
      }
    } catch (error) {
      console.error('加载组织树失败:', error);
      message.error('加载组织树失败');
    }
  };

  // 搜索部门树
  const filterOrgTree = (searchValue: string) => {
    if (!searchValue || searchValue.trim() === '') {
      filteredOrgTreeData.value = [...orgTreeData.value];
      return;
    }
    
    const filterNode = (node: OrgTreeVO): OrgTreeVO | null => {
      if (node.orgName && node.orgName.toLowerCase().includes(searchValue.toLowerCase())) {
        return { ...node };
      }
      
      if (node.children && node.children.length > 0) {
        const filteredChildren = node.children
          .map(child => filterNode(child))
          .filter(child => child !== null) as OrgTreeVO[];
        
        if (filteredChildren.length > 0) {
          return { ...node, children: filteredChildren };
        }
      }
      
      return null;
    };
    
    const filtered = orgTreeData.value
      .map(node => filterNode(node))
      .filter(node => node !== null) as OrgTreeVO[];
    
    filteredOrgTreeData.value = filtered;
    
    // 展开所有匹配节点的路径
    const expandMatchedNodes = (nodes: OrgTreeVO[]) => {
      const keysToExpand: string[] = [];
      
      const collectKeys = (node: OrgTreeVO) => {
        keysToExpand.push(node.id);
        if (node.children && node.children.length > 0) {
          node.children.forEach(collectKeys);
        }
      };
      
      nodes.forEach(collectKeys);
      expandedKeys.value = Array.from(new Set([...expandedKeys.value, ...keysToExpand]));
    };
    
    if (searchValue.trim() !== '') {
      expandMatchedNodes(filtered);
    }
  };

  const loadEnergyTypes = async () => {
    try {
      const data = await getEnergyTypes();
      energyTypes.value = data;
    } catch (error) {
      console.error('加载能源类型失败:', error);
      message.error('加载能源类型失败');
    }
  };

  // 交互
  const handleOrgSelect = (selectedKeys: string[]) => {
    if (selectedKeys.length > 0) {
      const key = selectedKeys[0];
      // 在原始树数据中查找节点，确保能找到正确的orgCode
      const node = findNodeById(orgTreeData.value, key);
      if (node && node.orgCode) {
        selectedOrgInfo.value = node;
        queryParam.orgCode = node.orgCode;
        const path = findPathById(orgTreeData.value, key).map((n) => n.id);
        expandedKeys.value = Array.from(new Set([...(expandedKeys.value || []), ...path]));
        // 强制重新加载数据
        nextTick(() => {
          loadData();
        });
      } else {
        message.warning('所选部门无有效编码，请重新选择');
      }
    }
  };

  const handleTimeUnitChange = () => {
    queryParam.timeDimension = timeUnit.value;
    updateDateRange();
    // 立即加载数据以确保响应
    nextTick(() => {
      loadData();
    });
  };

  const handleEnergyTypeChange = () => {
    queryParam.energyType = energyType.value;
    // 立即加载数据以确保响应
    nextTick(() => {
      loadData();
    });
  };

  const handleDateChange = () => {
    updateDateRange();
    // 立即加载数据以确保响应
    nextTick(() => {
      loadData();
    });
  };

  const handleTrendTypeChange = () => {
    // 立即加载数据以确保响应
    nextTick(() => {
      loadData();
    });
  };

  const updateDateRange = () => {
    if (!selectedDate.value) return;

    const date = selectedDate.value;
    switch (timeUnit.value) {
      case 'day':
        queryParam.startDate = date.format('YYYY-MM-DD');
        queryParam.endDate = date.format('YYYY-MM-DD');
        break;
      case 'month':
        queryParam.startDate = date.startOf('month').format('YYYY-MM-DD');
        queryParam.endDate = date.endOf('month').format('YYYY-MM-DD');
        break;
      case 'year':
        queryParam.startDate = date.startOf('year').format('YYYY-MM-DD');
        queryParam.endDate = date.endOf('year').format('YYYY-MM-DD');
        break;
    }
  };

  const loadData = async () => {
    if (!queryParam.orgCode) {
      message.warning('请选择部门');
      return;
    }

    loading.value = true;
    try {
      // 使用当前选择的时间维度查询趋势数据，而不是trendType
      const trendParams = { ...queryParam };
      const [summaryResponse, trendResponseRaw] = await Promise.all([
        getSummaryData(queryParam),
        getTrendData(trendParams),
      ]);

      // 验证并处理汇总数据
      if (summaryResponse && typeof summaryResponse === 'object') {
        // 确保数据结构完整
        const validSummary = {
          statisticsData: {
            totalConsumption: Number(summaryResponse.statisticsData?.totalConsumption || 0),
            electricConsumption: Number(summaryResponse.statisticsData?.electricConsumption || 0),
            waterConsumption: Number(summaryResponse.statisticsData?.waterConsumption || 0),
            gasConsumption: Number(summaryResponse.statisticsData?.gasConsumption || 0),
            totalCost: Number(summaryResponse.statisticsData?.totalCost || 0),
            totalCarbonEmission: Number(summaryResponse.statisticsData?.totalCarbonEmission || 0),
          },
          pieChartData: {
            series: Array.isArray(summaryResponse.pieChartData?.series) ? summaryResponse.pieChartData.series : []
          },
          tableData: Array.isArray(summaryResponse.tableData) ? summaryResponse.tableData : []
        };
        //console.log("hhr1026="  + JSON.stringify(validSummary)); 
        summaryData.value = validSummary;
        tableData.value = validSummary.tableData;
      } else {
        console.warn('汇总数据响应格式不正确:', summaryResponse);
        throw new Error('汇总数据格式不正确');
      }

      // 验证并处理趋势数据
      if (trendResponseRaw && typeof trendResponseRaw === 'object') {
        // 兼容后端返回 xaxis/xAxis
        const xr: any = (trendResponseRaw as any).xAxis || (trendResponseRaw as any).xaxis;
        const validXAxis = {
          type: xr?.type || 'category',
          data: Array.isArray(xr?.data) ? xr.data : []
        };
        
        const validSeries = Array.isArray((trendResponseRaw as any).series) 
          ? (trendResponseRaw as any).series.map((s: any) => ({
              name: s?.name || '未知',
              type: s?.type || 'line',
              data: Array.isArray(s?.data) ? s.data : []
            }))
          : [];
          
        trendData.value = {
          xAxis: validXAxis,
          series: validSeries,
        };
      } else {
        console.warn('趋势数据响应格式不正确:', trendResponseRaw);
        // 设置默认空数据而不是抛出错误，避免影响汇总数据
        trendData.value = { xAxis: { type: 'category', data: [] }, series: [] };
      }

      nextTick(() => {
        // 触发响应式，强制子图表刷新
        if (summaryData.value) {
          // 创建新对象以触发响应式更新
          summaryData.value = {
            statisticsData: { ...summaryData.value.statisticsData },
            pieChartData: { 
              series: summaryData.value.pieChartData.series.map(s => ({ 
                ...s, 
                data: s.data.map(d => ({ ...d })) 
              }))
            },
            tableData: [...summaryData.value.tableData]
          };
        }
        if (trendData.value) {
          trendData.value = {
            xAxis: { ...trendData.value.xAxis, data: [...trendData.value.xAxis.data] },
            series: trendData.value.series.map(s => ({ 
              ...s, 
              data: [...s.data] 
            }))
          };
        }
      });
    } catch (error) {
      console.error('数据加载失败:', error);
      message.error(`数据加载失败: ${error instanceof Error ? error.message : '未知错误'}`);
      // 设置默认空数据
      summaryData.value = { 
        statisticsData: { totalConsumption: 0, electricConsumption: 0, waterConsumption: 0, gasConsumption: 0, totalCost: 0, totalCarbonEmission: 0 }, 
        pieChartData: { series: [] }, 
        tableData: [] 
      };
      trendData.value = { xAxis: { type: 'category', data: [] }, series: [] };
      tableData.value = [];
    } finally {
      loading.value = false;
    }
  };

  const handleExport = async () => {
    if (!queryParam.orgCode) {
      message.warning('请选择部门');
      return;
    }
    try {
      const response = await exportClassificationData(queryParam);
      const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `企业分类分区统计_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  const formatNumber = (num: number) => {
    return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  const getUnit = () => {
    if (energyType.value === 'all') return '综合单位';
    const type = energyTypes.value.find((t) => t.energyType === parseInt(energyType.value));
    return type?.energyUnit || '';
  };

  const getEnergyTypeName = (type: string) => {
    const energyTypeObj = energyTypes.value.find((t) => t.energyType === parseInt(type));
    return energyTypeObj?.energyName || type;
  };

  // 工具函数：取第一个叶子节点（有 orgCode）
  const findFirstLeafOrg = (treeData: OrgTreeVO[]): OrgTreeVO | null => {
    if (!treeData || treeData.length === 0) return null;
    for (const node of treeData) {
      const hasChildren = !!(node.children && node.children.length > 0);
      if (!hasChildren && node.orgCode) return node;
      if (hasChildren) {
        const leaf = findFirstLeafOrg(node.children!);
        if (leaf) return leaf;
      }
    }
    for (const node of treeData) {
      if (node.orgCode) return node;
      if (node.children && node.children.length > 0) {
        const any = findFirstLeafOrg(node.children!);
        if (any) return any;
      }
    }
    return null;
  };

  // 工具函数：按 id 找节点
  const findNodeById = (treeData: OrgTreeVO[], id: string): OrgTreeVO | null => {
    for (const node of treeData) {
      if (node.id === id) return node;
      if (node.children && node.children.length > 0) {
        const found = findNodeById(node.children, id);
        if (found) return found;
      }
    }
    return null;
  };

  // 工具函数：查找根到 id 的路径
  const findPathById = (treeData: OrgTreeVO[], id: string, path: OrgTreeVO[] = []): OrgTreeVO[] => {
    for (const node of treeData) {
      const newPath = [...path, node];
      if (node.id === id) return newPath;
      if (node.children && node.children.length > 0) {
        const childPath = findPathById(node.children, id, newPath);
        if (childPath.length) return childPath;
      }
    }
    return [];
  };

  // 初始化
  onMounted(async () => {
    await Promise.all([loadOrgTree(), loadEnergyTypes()]);
    updateDateRange();
    if (queryParam.orgCode) {
      loadData();
    }
  });

  return {
    // 数据
    orgTreeData,
    filteredOrgTreeData,
    selectedOrgKeys,
    expandedKeys,
    searchText,
    selectedOrgInfo,
    queryParam,
    timeUnit,
    selectedDate,
    energyType,
    energyTypes,
    chartType,
    trendType,
    summaryData,
    trendData,
    tableData,
    loading,

    // 计算
    dateFormat,
    pickerType,
    dynamicColumns,

    // 方法
    handleOrgSelect,
    handleTimeUnitChange,
    handleEnergyTypeChange,
    handleTrendTypeChange,
    handleDateChange,
    loadData,
    handleExport,
    formatNumber,
    getUnit,
    getEnergyTypeName,
    filterOrgTree,
  };
}
