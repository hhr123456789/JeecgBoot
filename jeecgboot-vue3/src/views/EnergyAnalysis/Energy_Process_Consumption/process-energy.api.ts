import { defHttp } from '/@/utils/http/axios';

enum Api {
  GetProductionLineTree = '/energy/processEnergy/getProductionLineTree',
  GetStatistics = '/energy/processEnergy/getStatistics',
  GetPieData = '/energy/processEnergy/getPieData',
  GetTrendData = '/energy/processEnergy/getTrendData',
  GetTableData = '/energy/processEnergy/getTableData',
  ExportData = '/energy/processEnergy/exportData',
}

/**
 * 获取生产线树形数据
 */
export const getProductionLineTree = () => {
  return defHttp.get({ url: Api.GetProductionLineTree }, { isTransformResponse: false });
};

/**
 * 获取工序能耗统计数据
 */
export const getProcessEnergyStatistics = (params: {
  lineId: string;
  timeUnit: string;
  date: string;
  energyType: string;
}) => {
  return defHttp.get({ url: Api.GetStatistics, params }, { isTransformResponse: false });
};

/**
 * 获取过程能耗分布饼图数据
 */
export const getProcessEnergyPieData = (params: {
  lineId: string;
  timeUnit: string;
  date: string;
  energyType: string;
}) => {
  return defHttp.get({ url: Api.GetPieData, params }, { isTransformResponse: false });
};

/**
 * 获取过程能耗趋势数据
 */
export const getProcessEnergyTrendData = (params: {
  lineId: string;
  timeUnit: string;
  date: string;
  energyType: string;
}) => {
  return defHttp.get({ url: Api.GetTrendData, params }, { isTransformResponse: false });
};

/**
 * 获取过程能耗表格数据
 */
export const getProcessEnergyTableData = (params: {
  lineId: string;
  timeUnit: string;
  date: string;
  energyType: string;
}) => {
  return defHttp.get({ url: Api.GetTableData, params }, { isTransformResponse: false });
};

/**
 * 导出数据
 */
export const exportProcessEnergyData = (params: {
  lineId: string;
  timeUnit: string;
  date: string;
  energyType: string;
}) => {
  return defHttp.get(
    { url: Api.ExportData, params, responseType: 'blob' },
    { isTransformResponse: false }
  );
};
