import { defHttp } from '/@/utils/http/axios';

// 接口地址
enum Api {
  getTrendData = '/energy/analysis/trend',
  exportTrendData = '/energy/analysis/trend/export',
}

export type TimeType = 'day' | 'month' | 'year';

export interface TrendRequest {
  moduleIds: string[];       // 仪表编号集合（tb_module.module_id）
  startDate: string;         // 起始：day=YYYY-MM-DD, month=YYYY-MM, year=YYYY
  endDate: string;           // 结束：同上
  timeType: TimeType;        // 粒度
  displayMode: number;       // 1=统一显示, 2=分开显示（对应字典 queryMethod）
  metrics?: string[];        // 默认 ["standardCoal","carbon"]
}

export interface UnifiedSeriesItem {
  name: string;              // 系列名称（如：折标煤、碳排放）
  metric: string;            // 指标编码 standardCoal | carbon | energy
  unit: string;              // 单位（kgce / kgCO2e / kWh等）
  data: Array<[string, number]>; // [label, value]
}

export interface UnifiedTrendResponse {
  displayMode: 'unified';
  timeRange: string | { start: string; end: string; type: string };
  series: UnifiedSeriesItem[];
}

export interface SeparatedChartItem {
  title: string;             // 图标题（如：折标煤）
  metric: string;            // 指标编码
  unit: string;              // 单位
  series: Array<{
    name: string;            // 系列名（通常是“合计”或设备名）
    moduleId?: string;
    data: Array<[string, number]>;
  }>;
}

export interface SeparatedTrendResponse {
  displayMode: 'separated';
  timeRange: string | { start: string; end: string; type: string };
  charts: SeparatedChartItem[];
}

export type TrendResponse = UnifiedTrendResponse | SeparatedTrendResponse;

export const getTrendData = (data: TrendRequest) => {
  return defHttp.post<TrendResponse>({ url: Api.getTrendData, data });
};

export const exportTrendData = (data: TrendRequest) => {
  return defHttp.post(
    { url: Api.exportTrendData, data, responseType: 'blob', timeout: 60000 },
    { isTransformResponse: false }
  );
};

