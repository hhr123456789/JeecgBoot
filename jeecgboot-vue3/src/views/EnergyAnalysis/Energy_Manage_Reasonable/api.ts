import { defHttp } from '/@/utils/http/axios';

// 接口地址
enum Api {
  getReasonableData = '/energy/analysis/reasonable',
}

// 请求与响应类型
export type TimeType = 'day' | 'month' | 'year';

export interface ReasonableRequest {
  moduleIds: string[];       // 仪表编号集合
  startDate: string;         // 起始日期：与 timeType 对应的格式
  endDate: string;           // 结束日期：与 timeType 对应的格式
  timeType: TimeType;        // 粒度
}

export interface ReasonableSummary {
  cuspCount: number;
  peakCount: number;
  levelCount: number;
  valleyCount: number;
  totalCount: number;
}

export interface RatioItem {
  name: string;      // 尖/峰/平/谷
  value: number;     // 数值
  percent: number;   // 占比
}

export interface TotalTrendItem {
  date: string;
  energyCount: number;
}

export interface XYItem { x: string; y: number; }

export interface ReasonableTouTrend {
  cusp: XYItem[];
  peak: XYItem[];
  level: XYItem[];
  valley: XYItem[];
}

export interface ReasonableResponse {
  summary: ReasonableSummary;
  ratio: RatioItem[];
  totalTrend: TotalTrendItem[];
  touTrend: ReasonableTouTrend;
}

export const getReasonableData = (data: ReasonableRequest) => {
  return defHttp.post<ReasonableResponse>({ url: Api.getReasonableData, data });
};

