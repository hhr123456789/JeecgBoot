/**
 * 部门树VO
 */
export interface OrgTreeVO {
  id: string;
  orgCode: string;
  orgName: string;
  parentId: string;
  children?: OrgTreeVO[];
}

/**
 * 能源类型VO
 */
export interface EnergyTypeVO {
  energyType: number;
  energyName: string;
  energyUnit: string;
  pricePerUnit: number;
  carbonFactor: number;
  coalFactor: number;
}

/**
 * 分类分区查询参数
 */
export interface ClassificationQueryParam {
  orgCode: string;
  energyType: string;
  timeDimension: 'day' | 'month' | 'year';
  startDate: string;
  endDate: string;
  includeChildren: boolean;
}

/**
 * 分类分区统计汇总
 */
export interface ClassificationSummaryVO {
  statisticsData: StatisticsDataVO;
  pieChartData: PieChartDataVO;
  tableData: TableDataVO[];
}

/**
 * 统计数据VO
 */
export interface StatisticsDataVO {
  totalConsumption: number;
  electricConsumption: number;
  waterConsumption: number;
  gasConsumption: number;
  totalCost: number;
  totalCarbonEmission: number;
}

/**
 * 饼图数据VO
 */
export interface PieChartDataVO {
  series: SeriesDataVO[];
}

export interface SeriesDataVO {
  name: string;
  type: string;
  data: DataItemVO[];
}

export interface DataItemVO {
  value: number;
  name: string;
  percentage: number;
}

/**
 * 趋势数据VO
 */
export interface TrendDataVO {
  xAxis: XAxisDataVO;
  series: TrendSeriesDataVO[];
}

export interface XAxisDataVO {
  type: string;
  data: string[];
}

export interface TrendSeriesDataVO {
  name: string;
  type: string;
  data: number[];
}

/**
 * 表格数据VO
 */
export interface TableDataVO {
  time: string;
  electric?: number;
  water?: number;
  gas?: number;
  electricCost?: number;
  waterCost?: number;
  gasCost?: number;
  totalCost?: number;
  consumption?: number;
  cost?: number;
  carbonEmission?: number;
}