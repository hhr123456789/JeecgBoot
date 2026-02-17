import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 模板接口
  TemplateList = '/energy/alarm/template/list',
  TemplateAdd = '/energy/alarm/template/add',
  TemplateEdit = '/energy/alarm/template/edit',
  TemplateDelete = '/energy/alarm/template/delete',
  TemplateUsage = '/energy/alarm/template/usage',
  TemplateOptions = '/energy/alarm/template/options',
  TemplateQueryById = '/energy/alarm/template/queryById',
  // 规则接口
  RuleList = '/energy/alarm/rule/list',
  RuleAdd = '/energy/alarm/rule/add',
  RuleEdit = '/energy/alarm/rule/edit',
  RuleDelete = '/energy/alarm/rule/delete',
  RuleDeleteBatch = '/energy/alarm/rule/deleteBatch',
  RuleChangeStatus = '/energy/alarm/rule/changeStatus',
  RuleQueryById = '/energy/alarm/rule/queryById',
}

// ==================== 模板相关接口 ====================

/**
 * 分页查询模板列表
 */
export const getTemplateList = (params: any) =>
  defHttp.get({ url: Api.TemplateList, params }, { successMessageMode: 'none' });

/**
 * 新增模板
 */
export const addTemplate = (data: any) =>
  defHttp.post({ url: Api.TemplateAdd, data });

/**
 * 编辑模板
 */
export const editTemplate = (data: any) =>
  defHttp.put({ url: Api.TemplateEdit, data });

/**
 * 删除模板
 */
export const deleteTemplate = (params: any) =>
  defHttp.delete({ url: Api.TemplateDelete, params });

/**
 * 查询模板使用情况
 */
export const getTemplateUsage = (params: any) =>
  defHttp.get({ url: Api.TemplateUsage, params }, { successMessageMode: 'none' });

/**
 * 获取模板选项列表（用于下拉选择）
 */
export const getTemplateOptions = () =>
  defHttp.get({ url: Api.TemplateOptions }, { successMessageMode: 'none' });

/**
 * 根据ID查询模板详情
 */
export const getTemplateById = (params: any) =>
  defHttp.get({ url: Api.TemplateQueryById, params }, { successMessageMode: 'none' });

// ==================== 规则相关接口 ====================

/**
 * 分页查询规则列表
 */
export const getRuleList = (params: any) =>
  defHttp.get({ url: Api.RuleList, params }, { successMessageMode: 'none' });

/**
 * 新增规则
 */
export const addRule = (data: any) =>
  defHttp.post({ url: Api.RuleAdd, data });

/**
 * 编辑规则
 */
export const editRule = (data: any) =>
  defHttp.put({ url: Api.RuleEdit, data });

/**
 * 删除规则
 */
export const deleteRule = (params: any) =>
  defHttp.delete({ url: Api.RuleDelete, params });

/**
 * 批量删除规则
 */
export const deleteRuleBatch = (params: any) =>
  defHttp.delete({ url: Api.RuleDeleteBatch, params });

/**
 * 修改规则状态
 */
export const changeRuleStatus = (params: any) =>
  defHttp.put({ url: Api.RuleChangeStatus, params });

/**
 * 根据ID查询规则详情
 */
export const getRuleById = (params: any) =>
  defHttp.get({ url: Api.RuleQueryById, params }, { successMessageMode: 'none' });
