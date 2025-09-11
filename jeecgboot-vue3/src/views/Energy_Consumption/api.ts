import { defHttp } from '/@/utils/http/axios';

// 接口地址
enum Api {
  getConsumptionData = '/energy/consumption/statistics',
  exportConsumptionData = '/energy/consumption/statistics/export',
}

export type TimeType = 'day' | 'month' | 'year';

export interface ConsumptionRequest {
  moduleIds: string[];       // 仪表编号集合（tb_module.module_id）
  startDate: string;         // 起始：day=YYYY-MM-DD, month=YYYY-MM, year=YYYY
  endDate: string;           // 结束：同上
  timeType: TimeType;        // 粒度
  displayMode: number;       // 1=统一显示, 2=分开显示
}

export interface UnifiedSeriesItem {
  name: string;              // 系列名称（如：1#空压机-能耗）
  metric: string;            // 指标编码 energy
  unit: string;              // 单位（kWh / m³等）
  moduleId: string;          // 设备ID
  moduleName: string;        // 设备名称
  data: Array<{x: string, y: number}>; // [{x: label, y: value}]
}

export interface UnifiedConsumptionResponse {
  displayMode: 'unified';
  timeRange: string;
  series: UnifiedSeriesItem[];
}

export interface SeparatedChartItem {
  moduleId: string;          // 设备ID
  moduleName: string;        // 设备名称
  title: string;             // 图标题
  series: Array<{
    name: string;            // 系列名（通常是"能耗"）
    metric: string;          // 指标编码 energy
    unit: string;            // 单位
    data: Array<{x: string, y: number}>;
  }>;
}

export interface SeparatedConsumptionResponse {
  displayMode: 'separated';
  timeRange: string;
  charts: SeparatedChartItem[];
}

export type ConsumptionResponse = UnifiedConsumptionResponse | SeparatedConsumptionResponse;

export const getConsumptionData = (data: ConsumptionRequest) => {
  return defHttp.post<ConsumptionResponse>({ url: Api.getConsumptionData, data });
};

export const exportConsumptionData = (data: ConsumptionRequest) => {
  return defHttp.post(
    { url: Api.exportConsumptionData, data, responseType: 'blob', timeout: 60000 },
    { isTransformResponse: false }
  );
};