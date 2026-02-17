import { defHttp } from '/@/utils/http/axios';

enum Api {
  GetStatistics = '/energy/shiftEnergy/getStatistics',
  GetTrendData = '/energy/shiftEnergy/getTrendData',
  GetPieData = '/energy/shiftEnergy/getPieData',
  GetTableData = '/energy/shiftEnergy/getTableData',
}

export interface ShiftEnergyQueryParams {
  dimensionCode: string;
  dimensionType?: number;
  timeUnit: string;
  queryDate: string;
  shiftType?: string;
  energyType?: string;
}

export interface ShiftEnergyStatistics {
  totalConsumption: string;
  morningConsumption: string;
  middleConsumption: string;
  nightConsumption: string;
  totalCost: string;
  totalCarbon: string;
  totalCoal: string;
  energyUnit: string;
}

export interface ShiftEnergyTrendData {
  xAxisData: string[];
  seriesData: Array<{
    name: string;
    data: number[];
    color: string;
  }>;
}

export interface ShiftEnergyPieData {
  name: string;
  value: number;
  color: string;
}

export interface ShiftEnergyTableData {
  date: string;
  morningConsumption: number;
  middleConsumption: number;
  nightConsumption: number;
  totalConsumption: number;
  totalCost: number;
  carbon: number;
  coal: number;
}

export const getShiftEnergyStatistics = (params: ShiftEnergyQueryParams) => {
  return defHttp.get<ShiftEnergyStatistics>({ url: Api.GetStatistics, params }, { successMessageMode: 'none' });
};

export const getShiftEnergyTrendData = (params: ShiftEnergyQueryParams) => {
  return defHttp.get<ShiftEnergyTrendData>({ url: Api.GetTrendData, params }, { successMessageMode: 'none' });
};

export const getShiftEnergyPieData = (params: ShiftEnergyQueryParams) => {
  return defHttp.get<ShiftEnergyPieData[]>({ url: Api.GetPieData, params }, { successMessageMode: 'none' });
};

export const getShiftEnergyTableData = (params: ShiftEnergyQueryParams) => {
  return defHttp.get<ShiftEnergyTableData[]>({ url: Api.GetTableData, params }, { successMessageMode: 'none' });
};
