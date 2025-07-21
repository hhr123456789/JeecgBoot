import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 根据维度获取仪表列表
  getModulesByOrgCode = '/energy/monitor/getModulesByOrgCode',
  // 实时数据查询
  getRealTimeMonitorData = '/energy/monitor/getRealTimeMonitorData',
}

/**
 * 根据维度编码获取仪表列表
 * @param params
 */
export const getModulesByOrgCode = (params: {
  orgCodes: string;  // 后端期望的参数名是orgCodes（复数）
  nowtype: string;   // 维度类型，必填参数
  includeChildren?: boolean;
}) => {
  return defHttp.get({ url: Api.getModulesByOrgCode, params });
};

/**
 * 查询实时监控数据
 * @param data 
 */
export const getRealTimeMonitorData = (data: {
  moduleIds: string[];
  parameters: number[];
  startTime: string;
  endTime: string;
  interval: number;
  displayMode: number;
}) => {
  return defHttp.post({ url: Api.getRealTimeMonitorData, data });
};

/**
 * 仪表信息接口类型定义
 */
export interface ModuleInfo {
  moduleId: string;
  moduleName: string;
  orgCode: string;
  departName: string;
  energyType: number;
  isAction: string;
}

/**
 * 实时监控数据请求参数类型定义
 */
export interface RealTimeMonitorRequest {
  moduleIds: string[];
  parameters: number[];
  startTime: string;
  endTime: string;
  interval: number;
  displayMode: number;
}

/**
 * 实时监控数据响应类型定义
 */
export interface RealTimeMonitorResponse {
  displayMode: string;
  timeRange: {
    startTime: string;
    endTime: string;
    interval: string;
  };
  series: Array<{
    name: string;
    moduleId: string;
    moduleName: string;
    parameter: string;
    parameterCode: string;
    unit: string;
    data: Array<[string, number]>;
  }>;
}

/**
 * 分开显示模式的响应类型定义
 */
export interface SeparateDisplayResponse {
  displayMode: string;
  timeRange: {
    startTime: string;
    endTime: string;
    interval: string;
  };
  charts: Array<{
    moduleId: string;
    moduleName: string;
    parameter: string;
    parameterCode: string;
    unit: string;
    data: Array<[string, number]>;
  }>;
}
