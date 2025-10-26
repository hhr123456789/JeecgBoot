import { defHttp } from '/@/utils/http/axios';
import { 
  ClassificationQueryParam, 
  ClassificationSummaryVO, 
  TrendDataVO, 
  OrgTreeVO, 
  EnergyTypeVO 
} from './types';

enum Api {
  getOrgTree = '/energy/classification/getOrgTree',
  getEnergyTypes = '/energy/classification/getEnergyTypes',
  getSummaryData = '/energy/classification/getSummaryData',
  getTrendData = '/energy/classification/getTrendData',
  exportData = '/energy/classification/exportData'
}

/**
 * 获取部门树形结构
 */
export const getOrgTree = () => {
  return defHttp.get<OrgTreeVO[]>({ url: Api.getOrgTree });
};

/**
 * 获取能源类型列表
 */
export const getEnergyTypes = () => {
  return defHttp.get<EnergyTypeVO[]>({ url: Api.getEnergyTypes });
};

/**
 * 获取汇总数据
 */
export const getSummaryData = (params: ClassificationQueryParam) => {
  return defHttp.post<ClassificationSummaryVO>({ url: Api.getSummaryData, params });
};

/**
 * 获取趋势数据
 */
export const getTrendData = (params: ClassificationQueryParam) => {
  return defHttp.post<TrendDataVO>({ url: Api.getTrendData, params });
};

/**
 * 导出数据
 */
export const exportClassificationData = (params: ClassificationQueryParam) => {
  // 后端为 GET 映射，前端对齐使用 GET 并返回原始 blob
  return defHttp.get(
    { url: Api.exportData, params, responseType: 'blob' },
    { isTransformResponse: false },
  );
};
