import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { getDictItems } from '/@/api/common/api';
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
  // 维度标签列表
  const dimensionList = ref<any[]>([]);
  const activeTabKey = ref('info1');
  
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
  // 默认选择月份，显示该月每日的用量
  const timeUnit = ref<'day' | 'month' | 'year'>('month');
  const selectedDate = ref<Dayjs>(dayjs()); // 使用当前日期
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
      case 'month':
        return 'YYYY-MM';
      case 'year':
        return 'YYYY';
      default:
        return 'YYYY-MM';
    }
  });

  const pickerType = computed(() => {
    switch (timeUnit.value) {
      case 'month':
        return 'month';
      case 'year':
        return 'year';
      default:
        return 'month';
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
      console.log('加载组织树数据:', data);
      
      if (!data || data.length === 0) {
        console.warn('后端返回的组织树数据为空');
        message.warning('暂无部门数据，请联系管理员添加部门信息');
        orgTreeData.value = [];
        filteredOrgTreeData.value = [];
        return;
      }
      
      orgTreeData.value = data;
      filteredOrgTreeData.value = data;

      if (data.length > 0) {
        const firstOrg = findFirstLeafOrg(data);
        if (firstOrg) {
          selectedOrgKeys.value = [firstOrg.id];
          selectedOrgInfo.value = firstOrg;
          queryParam.orgCode = firstOrg.orgCode;
          console.log('默认选中组织:', firstOrg);
          const path = findPathById(data, firstOrg.id).map((n) => n.id);
          expandedKeys.value = Array.from(new Set([...(expandedKeys.value || []), ...path]));
        } else {
          console.warn('未找到有效的叶子节点');
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
      case 'month':
        // 按月查询，显示该月每日数据
        queryParam.timeDimension = 'day';
        queryParam.startDate = date.startOf('month').format('YYYY-MM-DD');
        queryParam.endDate = date.endOf('month').format('YYYY-MM-DD');
        break;
      case 'year':
        // 按年查询，显示该年每月数据
        queryParam.timeDimension = 'month';
        queryParam.startDate = date.startOf('year').format('YYYY-MM-DD');
        queryParam.endDate = date.endOf('year').format('YYYY-MM-DD');
        break;
    }
    
    console.log('时间范围已更新:', queryParam.startDate, '至', queryParam.endDate, '(维度:', timeUnit.value, ', 粒度:', queryParam.timeDimension + ')');
  };

  const loadData = async () => {
    if (!queryParam.orgCode) {
      console.warn('未选择部门，跳过数据加载');
      console.warn('当前 queryParam:', JSON.stringify(queryParam));
      console.warn('当前 selectedOrgInfo:', selectedOrgInfo.value);
      message.warning('请先在左侧维度树中选择一个部门');
      return;
    }

    console.log('========== 开始加载数据 ==========');
    console.log('查询参数:', JSON.stringify(queryParam, null, 2));
    console.log('选中部门:', selectedOrgInfo.value);
    loading.value = true;
    try {
      // 使用当前选择的时间维度查询趋势数据，而不是trendType
      const trendParams = { ...queryParam };
      const [summaryResponse, trendResponseRaw] = await Promise.all([
        getSummaryData(queryParam),
        getTrendData(trendParams),
      ]);

      // 验证并处理汇总数据
      console.log('========== 汇总数据响应 ==========');
      console.log('summaryResponse:', JSON.stringify(summaryResponse, null, 2));

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

        console.log('========== 处理后的汇总数据 ==========');
        console.log('统计数据:', validSummary.statisticsData);
        console.log('饼图数据系列数:', validSummary.pieChartData.series.length);
        console.log('表格数据行数:', validSummary.tableData.length);

        // 检查是否所有数据都为0
        const hasData = validSummary.statisticsData.totalConsumption > 0 ||
                       validSummary.statisticsData.electricConsumption > 0 ||
                       validSummary.statisticsData.waterConsumption > 0 ||
                       validSummary.statisticsData.gasConsumption > 0;

        if (!hasData) {
          console.warn('⚠️ 警告：查询到的数据全部为0，可能原因：');
          console.warn('1. 数据库中没有该部门的统计数据');
          console.warn('2. 查询的时间范围内没有数据');
          console.warn('3. 需要运行数据同步任务');
          message.warning('当前查询条件下没有数据，请检查：1.是否选择了正确的部门 2.时间范围是否有数据 3.是否需要同步数据');
        } else {
          console.log('✓ 成功查询到数据');
        }

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

  // 初始化维度列表
  const initDimensionList = async () => {
    try {
      const res = await getDictItems('dimensionCode');
      console.log('加载能源维度字典:', res);
      if (res && Array.isArray(res) && res.length > 0) {
        // 将字典数据转换为维度列表
        dimensionList.value = res.map((item, index) => {
          // 优先使用字典配置的value字段作为energyType
          let energyTypeValue = item.value || 'all';
          const text = item.text || '';
          
          // 智能识别修正：如果字典配置错误，根据标题关键词智能修正
          // 检测电能：用电、按线路
          if (text.includes('用电') || text.includes('按线路')) {
            if (energyTypeValue !== '1') {
              console.warn(`检测到电能相关标签 "${text}"，但字典value="${energyTypeValue}"，强制修正为energyType='1'`);
              energyTypeValue = '1';
            }
          }
          // 检测水能：用水、企业用水
          else if (text.includes('用水') || text.includes('水')) {
            if (energyTypeValue !== '2' && energyTypeValue !== '1') { // 水通常是2,但根据之前的数据库截图可能是1
              console.warn(`检测到水能相关标签 "${text}"，但字典value="${energyTypeValue}"，强制修正为energyType='2'`);
              energyTypeValue = '2';
            }
          }
          // 检测燃气：天然气、燃气
          else if (text.includes('天然气') || text.includes('燃气')) {
            if (energyTypeValue !== '8') {
              console.warn(`检测到燃气相关标签 "${text}"，但字典value="${energyTypeValue}"，强制修正为energyType='8'`);
              energyTypeValue = '8';  // 根据数据库截图,天然气的energy_type=8
            }
          }
          // 检测压缩空气
          else if (text.includes('压缩空气') || text.includes('空气')) {
            if (energyTypeValue !== '5') {
              console.warn(`检测到压缩空气相关标签 "${text}"，但字典value="${energyTypeValue}"，强制修正为energyType='5'`);
              energyTypeValue = '5';  // 根据数据库截图,压缩空气的energy_type=5
            }
          }
          
          console.log(`维度 "${item.text}" 使用energyType="${energyTypeValue}"`);
          
          return {
            key: `info${index + 1}`,
            title: item.text,
            nowtype: Number(index + 1),
            value: item.value || String(index + 1),
            energyType: energyTypeValue
          };
        });
        
        console.log('转换后的维度列表:', dimensionList.value);
        
        // 默认选中第一个标签页
        if (dimensionList.value.length > 0) {
          activeTabKey.value = dimensionList.value[0].key;
          energyType.value = dimensionList.value[0].energyType;
          queryParam.energyType = dimensionList.value[0].energyType;
          console.log('初始化维度列表成功:', dimensionList.value);
        }
      } else {
        // 如果获取字典失败，使用默认维度列表
        setDefaultDimensionList();
      }
    } catch (error) {
      console.warn('加载能源维度字典失败，使用默认配置:', error);
      // 如果API调用失败，使用默认维度列表
      setDefaultDimensionList();
    }
  };

  // 设置默认维度列表
  const setDefaultDimensionList = () => {
    dimensionList.value = [
      { key: 'info1', title: '全部能源', nowtype: 1, value: 'all', energyType: 'all' },
      { key: 'info2', title: '电能', nowtype: 2, value: '1', energyType: '1' },
      { key: 'info3', title: '水能', nowtype: 3, value: '2', energyType: '2' },
      { key: 'info4', title: '燃气', nowtype: 4, value: '3', energyType: '3' }
    ];
    activeTabKey.value = 'info1';
    energyType.value = 'all';
    queryParam.energyType = 'all';
  };

  // 处理标签页切换
  const handleTabChange = (key: string) => {
    console.log('切换标签页:', activeTabKey.value, '->', key);
    
    // 先清空部门选择，避免显示不一致的部门信息
    selectedOrgInfo.value = null;
    queryParam.orgCode = '';
    
    // 清空该标签页的查询结果
    summaryData.value = {
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
    };
    trendData.value = { xAxis: { type: 'category', data: [] }, series: [] };
    tableData.value = [];
    
    // 然后更新标签页key
    activeTabKey.value = key;
    
    // 根据选中的标签页设置当前能源类型
    const selectedDimension = dimensionList.value.find(item => item.key === key);
    if (selectedDimension) {
      energyType.value = selectedDimension.energyType;
      queryParam.energyType = selectedDimension.energyType;
      console.log('切换能源类型:', selectedDimension.energyType);
    }
    
    console.log('标签页切换完成，等待用户在树中选择部门');
  };

  // 树组件引用
  const treeRefs = ref<Record<string, any>>({});
  
  // 存储每个标签页选中的节点信息
  const selectedNodesMap = ref<Record<string, any>>({
    info1: null,
    info2: null,
    info3: null,
    info4: null,
    info5: null
  });

  // 设置树组件引用
  const setTreeRef = (el: any, key: string) => {
    if (el) {
      treeRefs.value[key] = el;
    }
  };

  // 处理部门树选择（来自 DimensionTree 组件）
  const onDepartTreeSelect = (nodeData: any, isAutoSelect: boolean = false) => {
    console.log('选中的部门节点:', nodeData, '是否自动选中:', isAutoSelect);

    // 验证节点数据
    if (!nodeData) {
      console.warn('节点数据为空');
      return;
    }

    if (!nodeData.orgCode) {
      message.warning('所选部门无有效编码，请重新选择');
      return;
    }

    try {
      // 提取真实的文本值
      let orgName = '';

      // 优先使用 orgName 或 departName 字段
      if (nodeData.orgName) {
        orgName = nodeData.orgName;
      } else if (nodeData.departName) {
        orgName = nodeData.departName;
      } else if (nodeData.title) {
        // title 可能是字符串或 VNode 对象
        if (typeof nodeData.title === 'string') {
          orgName = nodeData.title;
        } else if (typeof nodeData.title === 'object' && nodeData.title !== null) {
          // 处理 VNode 对象
          try {
            if (nodeData.title.children) {
              const children = nodeData.title.children;
              if (typeof children === 'string') {
                orgName = children;
              } else if (Array.isArray(children) && children.length > 0) {
                // 提取第一个字符串元素
                const firstChild = children[0];
                if (typeof firstChild === 'string') {
                  orgName = firstChild;
                } else if (firstChild && typeof firstChild === 'object') {
                  orgName = firstChild.text || firstChild.children || '';
                }
              }
            }
          } catch (e) {
            console.warn('解析 title VNode 失败:', e);
            orgName = String(nodeData.title);
          }
        }
      }

      // 如果还是没有提取到名称，使用默认值
      if (!orgName) {
        orgName = '未命名部门';
        console.warn('无法提取部门名称，使用默认值');
      }

      // 创建纯净的对象，不包含任何 VNode 引用
      const orgInfo = {
        id: nodeData.id || '',
        orgCode: nodeData.orgCode,
        orgName: orgName,
        parentId: nodeData.parentId || ''
      };

      // 更新全局选中信息
      selectedOrgInfo.value = orgInfo;
      queryParam.orgCode = nodeData.orgCode;

      // 保存当前标签页的选中节点
      selectedNodesMap.value[activeTabKey.value] = orgInfo;

      console.log('当前选中的部门:', orgInfo.orgName, '部门编码:', orgInfo.orgCode);

      // 页面首次加载时（树组件自动选中第一个节点时）自动查询数据
      // 标签页切换时不自动查询，等待用户手动点击
      if (isAutoSelect) {
        console.log('首次自动选中，立即加载数据');
        nextTick(() => {
          loadData();
        });
      } else {
        console.log('用户手动选中，立即加载数据');
        nextTick(() => {
          loadData();
        });
      }
    } catch (error) {
      console.error('处理部门选择时出错:', error);
      message.error('选择部门失败，请重试');
    }
  };

  // 初始化
  onMounted(async () => {
    try {
      console.log('=== 页面初始化开始 ===');
      // 先初始化维度列表
      await initDimensionList();
      console.log('维度列表初始化完成:', dimensionList.value);

      // 加载能源类型（DimensionTree 组件会自动加载部门树）
      await loadEnergyTypes();
      console.log('能源类型加载完成:', energyTypes.value);

      // 更新时间范围
      updateDateRange();
      console.log('时间范围更新完成:', queryParam.startDate, '-', queryParam.endDate);

      console.log('=== 页面初始化完成，等待用户选择维度树节点 ===');
    } catch (error) {
      console.error('页面初始化失败:', error);
      message.error('页面初始化失败，请刷新重试');
    }
  });

  return {
    // 数据
    dimensionList,
    activeTabKey,
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
    handleTabChange,
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
    setTreeRef,
    onDepartTreeSelect,
  };
}
