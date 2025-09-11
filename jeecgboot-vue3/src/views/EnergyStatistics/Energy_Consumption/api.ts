import { defHttp } from '/@/utils/http/axios';

// 能源消耗统计请求参数
export interface ConsumptionRequest {
  moduleIds: string[];
  startDate: string;
  endDate: string;
  timeType: 'day' | 'month' | 'year';
  displayMode: number;
}

// 能源消耗统计响应数据
export interface ConsumptionResponse {
  displayMode: 'unified' | 'separated';
  timeRange: string;
  series?: Array<{
    name: string;
    metric: string;
    unit: string;
    moduleId: string;
    moduleName: string;
    data: Array<{ x: string; y: number }>;
  }>;
  charts?: Array<{
    moduleId: string;
    moduleName: string;
    title: string;
    series: Array<{
      name: string;
      metric: string;
      unit: string;
      data: Array<{ x: string; y: number }>;
    }>;
  }>;
}

/**
 * 获取能源消耗统计数据
 */
export function getConsumptionData(params: ConsumptionRequest) {
  return defHttp.post<ConsumptionResponse>({
    url: '/energy/consumption/statistics',
    params,
  });
}

/**
 * 导出能源消耗统计数据
 */
export function exportConsumptionData(params: ConsumptionRequest) {
  return defHttp.post({
    url: '/energy/consumption/statistics/export',
    params,
    responseType: 'blob',
  });
}