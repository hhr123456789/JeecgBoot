import { defHttp } from '/@/utils/http/axios';

// 仪表信息接口
export interface ModuleInfo {
  moduleId: string;
  moduleName: string;
  energyType: number;
  dimensionCode: string;
  dimensionName: string;
  ratedPower: number;
  currentPower: number;
  loadRate: number;
  isOnline: boolean;
  isAction: string;
  updateTime: string;
}

// 负荷时序数据查询请求参数
export interface LoadTimeSeriesRequest {
  moduleIds: string[];
  timeGranularity: string; // day/month/year
  queryDate: string;
  startTime?: string;
  endTime?: string;
}

// 负荷时序数据响应
export interface LoadTimeSeriesData {
  powerChartData: {
    title: string;
    timeLabels: string[];
    series: Array<{
      moduleId: string;
      moduleName: string;
      unit: string;
      data: number[];
      color: string;
    }>;
  };
  loadRateChartData: {
    title: string;
    timeLabels: string[];
    series: Array<{
      moduleId: string;
      moduleName: string;
      unit: string;
      data: number[];
      color: string;
    }>;
  };
  tableData: Array<{
    time: string;
    timeLabel: string;
    modules: Array<{
      moduleId: string;
      moduleName: string;
      ratedPower: number;
      currentPower: number;
      loadRate: number;
      powerUnit: string;
      loadRateUnit: string;
    }>;
  }>;
  summary: {
    totalDataPoints: number;
    timeRange: string;
    granularity: string;
    moduleCount: number;
    dataType: string;
  };
}

// 仪表负荷状态
export interface ModuleLoadStatus {
  moduleId: string;
  moduleName: string;
  isOnline: boolean;
  lastUpdateTime: string;
  ratedPower: number;
  currentPower: number;
  loadRate: number;
  powerUnit: string;
  loadRateUnit: string;
  status: string;
  loadLevel: string;
}

/**
 * 根据维度获取电力仪表列表
 */
export function getModulesByDimension(params: {
  dimensionCode: string;
  energyType: number;
  includeChildren?: boolean;
}) {
  return defHttp.get<ModuleInfo[]>({
    url: '/energy/realtime/getModulesByDimension',
    params
  });
}

/**
 * 获取负荷时序数据
 */
export function getLoadTimeSeriesData(data: LoadTimeSeriesRequest) {
  return defHttp.post<LoadTimeSeriesData>({
    url: '/energy/realtime/getLoadTimeSeriesData',
    data
  });
}

/**
 * 获取实时负荷状态
 */
export function getCurrentLoadStatus(data: { moduleIds: string[] }) {
  return defHttp.post<ModuleLoadStatus[]>({
    url: '/energy/realtime/getCurrentLoadStatus',
    data
  });
}

/**
 * 获取负荷数据表格
 */
export function getLoadTableData(data: {
  moduleIds: string[];
  timeType: string;
  startTime: string;
  endTime: string;
  pageNum?: number;
  pageSize?: number;
}) {
  return defHttp.post({
    url: '/energy/realtime/getLoadTableData',
    data
  });
}

/**
 * 导出负荷数据
 */
export function exportLoadData(data: {
  moduleIds: string[];
  timeGranularity: string;
  queryDate: string;
  fileName?: string;
}) {
  return defHttp.post({
    url: '/energy/realtime/exportLoadData',
    data,
    responseType: 'blob'
  });
}