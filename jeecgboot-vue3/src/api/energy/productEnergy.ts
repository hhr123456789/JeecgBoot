import { defHttp } from '/@/utils/http/axios';

enum Api {
  Statistics = '/energy/product/statistics',
  Distribution = '/energy/product/distribution',
  Trend = '/energy/product/trend',
  Comparison = '/energy/product/comparison',
  Ranking = '/energy/product/ranking',
  DetailList = '/energy/product/detail-list',
  CategoryTree = '/energy/product/category-tree',
}

interface QueryParams {
  timeDimension: string;
  startDate: string;
  endDate: string;
  energyType?: number | null;
  categoryId?: string | null;
}

/**
 * 获取统计数据
 */
export const getProductStatistics = (params: QueryParams) =>
  defHttp.get({ url: Api.Statistics, params }, { successMessageMode: 'none' });

/**
 * 获取能耗分布数据（饼图）
 */
export const getProductDistribution = (params: QueryParams) =>
  defHttp.get({ url: Api.Distribution, params }, { successMessageMode: 'none' });

/**
 * 获取单耗趋势数据（折线图）
 */
export const getProductTrend = (params: QueryParams & { productCodes?: string }) =>
  defHttp.get({ url: Api.Trend, params }, { successMessageMode: 'none' });

/**
 * 获取产量能耗对比数据（柱状图）
 */
export const getProductComparison = (params: QueryParams) =>
  defHttp.get({ url: Api.Comparison, params }, { successMessageMode: 'none' });

/**
 * 获取单耗排名数据（横向柱状图）
 */
export const getProductRanking = (params: QueryParams & { order?: string }) =>
  defHttp.get({ url: Api.Ranking, params }, { successMessageMode: 'none' });

/**
 * 获取明细列表数据（表格）
 */
export const getProductDetailList = (params: QueryParams & { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.DetailList, params }, { successMessageMode: 'none' });

/**
 * 获取产品分类树（左侧树）
 */
export const getProductCategoryTree = () =>
  defHttp.get({ url: Api.CategoryTree }, { successMessageMode: 'none' });
