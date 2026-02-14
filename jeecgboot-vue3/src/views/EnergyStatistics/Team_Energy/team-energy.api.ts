import { defHttp } from '/@/utils/http/axios';

enum Api {
  GetTeamList = '/energy/teamEnergy/getTeamList',
  GetStatistics = '/energy/teamEnergy/getStatistics',
  GetTrendData = '/energy/teamEnergy/getTrendData',
  GetRankingData = '/energy/teamEnergy/getRankingData',
  GetTableData = '/energy/teamEnergy/getTableData',
}

/**
 * 根据维度获取班组列表
 */
export const getTeamListByDimension = (params: { dimensionCode: string; dimensionType: number }) => {
  return defHttp.get({ url: Api.GetTeamList, params });
};

/**
 * 获取班组能源统计数据
 */
export const getTeamEnergyStatistics = (params: any) => {
  return defHttp.get({ url: Api.GetStatistics, params });
};

/**
 * 获取班组能源趋势图数据
 */
export const getTeamEnergyTrendData = (params: any) => {
  return defHttp.get({ url: Api.GetTrendData, params });
};

/**
 * 获取班组能源排名数据
 */
export const getTeamEnergyRankingData = (params: any) => {
  return defHttp.get({ url: Api.GetRankingData, params });
};

/**
 * 获取班组能源明细表数据
 */
export const getTeamEnergyTableData = (params: any) => {
  return defHttp.get({ url: Api.GetTableData, params });
};
