import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 根据维度获取仪表列表
  getModulesByDimension = '/energy/analysis/getModulesByDimension',
  // 能源分析对比数据查询
  getCompareData = '/energy/analysis/getCompareData',
  // 获取能源类型配置
  getEnergyTypes = '/energy/analysis/getEnergyTypes',
  // 导出对比数据
  exportCompareData = '/energy/analysis/exportCompareData',
}

/**
 * 仪表信息接口类型定义
 */
export interface ModuleVO {
  moduleId: string;
  moduleName: string;
  energyType: number;
  energyTypeName: string;
  unit: string;
  dimensionName: string;
  dimensionId: string;
  gatewayCode: string;
  meterId: string;
  ratedPower: number;
}

/**
 * 能源分析对比查询请求参数
 */
export interface CompareDataRequest {
  moduleId: string;         // 必填，仪表编号
  timeType: string;         // 必填，时间类型：day/month/year
  baselineStartTime: string; // 必填，基准期-开始时间
  baselineEndTime: string;   // 必填，基准期-结束时间
  compareStartTime: string;  // 必填，对比期-开始时间
  compareEndTime: string;    // 必填，对比期-结束时间
}

/**
 * 汇总数据
 */
export interface SummaryData {
  baselineTotal: number;           // 基准期总能耗
  compareTotal: number;            // 对比期总能耗
  savingTotal: number;             // 节能总量 = 基准 − 对比
  unit: string;                    // 单位
}

/**
 * 图表数据
 */
export interface ChartData {
  baselineDates: string[];         // 基准期时间序列
  compareDates: string[];          // 对比期时间序列
  series: Array<{
    name: string;                  // 系列名称：基准期/对比期/节能情况
    type: string;                  // 图表类型：line/bar
    data: number[];                // 数据值
    unit: string;                  // 单位
  }>;
}

/**
 * 表格数据
 */
export interface TableData {
  baselineDate: string;            // 基准时间
  baselineValue: number;           // 基准能耗
  compareDate: string;             // 对比时间
  compareValue: number;            // 对比能耗
  saving: string;                  // 节能情况（格式化后的字符串）
}

/**
 * 仪表信息
 */
export interface ModuleInfo {
  moduleId: string;
  moduleName: string;
  energyType: number;
  unit: string;
  dimensionName: string;
}

/**
 * 能源分析对比响应数据
 */
export interface CompareDataVO {
  summary: SummaryData;
  chartData: ChartData;
  tableData: TableData[];
  moduleInfo: ModuleInfo;
}

/**
 * 能源类型配置
 */
export interface EnergyTypeVO {
  energyType: number;
  energyTypeName: string;
  unit: string;
  icon: string;
}

/**
 * 根据维度获取仪表列表
 * @param params
 */
export const getModulesByDimension = (params: {
  orgCode: string;              // 必填，sys_depart表的org_code
  energyType?: number;          // 可选，能源类型筛选
  includeChildren?: boolean;    // 可选，是否包含子维度，默认false
}) => {
  return defHttp.get<ModuleVO[]>({ url: Api.getModulesByDimension, params });
};

/**
 * 能源分析对比数据查询
 * @param data
 */
export const getCompareData = (data: CompareDataRequest) => {
  return defHttp.post<CompareDataVO>({ url: Api.getCompareData, data });
};

/**
 * 获取能源类型配置
 */
export const getEnergyTypes = () => {
  return defHttp.get<EnergyTypeVO[]>({ url: Api.getEnergyTypes });
};

/**
 * 导出对比数据
 * @param params
 */
export const exportCompareData = (params: {
  moduleId: string;
  timeType: string;
  baselineStartTime: string;
  baselineEndTime: string;
  compareStartTime: string;
  compareEndTime: string;
  orgCode?: string;
}) => {
  return defHttp.get({
    url: Api.exportCompareData,
    params,
    responseType: 'blob',
    timeout: 60000 // 导出可能需要较长时间
  }, {
    isTransformResponse: false
  });
};
