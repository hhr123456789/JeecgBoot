import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 根据维度获取仪表列表
  getModulesByDimension = '/energy/realtime/getModulesByDimension',
  // 获取参数配置
  getParameterConfig = '/energy/realtime/getParameterConfig',
  // 时序数据查询
  getTimeSeriesData = '/energy/realtime/getTimeSeriesData',
  // 获取实时状态
  getCurrentStatus = '/energy/realtime/getCurrentStatus',
}

/**
 * 根据维度获取仪表列表
 */
export const getModulesByDimension = (params: {
  dimensionCode: string;
  energyType?: number;
  includeChildren?: boolean;
}) => {
  return defHttp.get({ url: Api.getModulesByDimension, params });
};

/**
 * 获取参数配置
 */
export const getParameterConfig = (params: {
  energyType: number;
}) => {
  return defHttp.get({ url: Api.getParameterConfig, params });
};

/**
 * 查询时序数据
 */
export const getTimeSeriesData = (data: {
  moduleIds: string[];
  parameters: number[];
  timeGranularity: string;
  queryDate: string;
  startTime?: string;
  endTime?: string;
}) => {
  return defHttp.post({ url: Api.getTimeSeriesData, data });
};

/**
 * 获取实时状态
 */
export const getCurrentStatus = (data: {
  moduleIds: string[];
  parameters: number[];
}) => {
  return defHttp.post({ url: Api.getCurrentStatus, data });
};

// 类型定义
export interface ModuleInfo {
  moduleId: string;
  moduleName: string;
  energyType: number;
  energyTypeName: string;
  dimensionCode: string;
  dimensionName: string;
  ratedPower: number;
  isOnline: boolean;
  lastUpdateTime: string;
}

export interface ParameterConfig {
  paramCode: number;
  paramName: string;
  fieldName: string;
  unit: string;
  category: string;
  isDefault: boolean;
}

export interface TimeSeriesData {
  chartData: {
    timeLabels: string[];
    series: Array<{
      moduleId: string;
      moduleName: string;
      paramCode: number;
      paramName: string;
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
      parameters: Array<{
        paramCode: number;
        paramName: string;
        value: number;
        unit: string;
      }>;
    }>;
  }>;
  summary: {
    totalDataPoints: number;
    timeRange: string;
    granularity: string;
    moduleCount: number;
    parameterCount: number;
  };
}

export interface ModuleStatus {
  moduleId: string;
  moduleName: string;
  isOnline: boolean;
  lastUpdateTime: string;
  parameters: Array<{
    paramCode: number;
    paramName: string;
    currentValue: number;
    unit: string;
    status: string;
    updateTime: string;
  }>;
}
