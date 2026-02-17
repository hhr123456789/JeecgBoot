import { defHttp } from '/@/utils/http/axios';

enum Api {
  GetConfig = '/energy/benchmark/getConfig',
  GetTargets = '/energy/benchmark/getTargets',
  GetStatistics = '/energy/benchmark/getStatistics',
  ExportData = '/energy/benchmark/exportData',
}

/**
 * 获取对标配置
 */
export const getBenchmarkConfig = (params?: any) => {
  return defHttp.get({ url: Api.GetConfig, params });
};

/**
 * 获取对标目标列表
 */
export const getBenchmarkTargets = (params?: any) => {
  return defHttp.get({ url: Api.GetTargets, params });
};

/**
 * 获取对标统计数据
 */
export const getBenchmarkStatistics = (params: {
  targetCode: string;
  timeUnit: string;
  startTime?: string;
  endTime?: string;
  energyType?: string;
}) => {
  return defHttp.get({ url: Api.GetStatistics, params }, { isTransformResponse: false });
};

/**
 * 导出对标数据
 */
export const exportBenchmarkData = (params: any) => {
  return defHttp.get({ url: Api.ExportData, params, responseType: 'blob' });
};
