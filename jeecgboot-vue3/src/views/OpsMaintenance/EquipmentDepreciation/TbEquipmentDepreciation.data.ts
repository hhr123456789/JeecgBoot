import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '设备编号',
    align: "center",
    dataIndex: 'equNo'
  },
  {
    title: '设备名称',
    align: "center",
    dataIndex: 'equName'
  },
  {
    title: '折旧年月 ',
    align: "center",
    dataIndex: 'depMonth'
  },
  {
    title: '折旧方法',
    align: "center",
    dataIndex: 'method'
  },
  {
    title: '购置金额',
    align: "center",
    dataIndex: 'purchaseAmount'
  },
  {
    title: '使用寿命(月)',
    align: "center",
    dataIndex: 'usefulLifeMonths'
  },
  {
    title: '净残率%',
    align: "center",
    dataIndex: 'salvageRate'
  },
  {
    title: '初期净值',
    align: "center",
    dataIndex: 'initNetValue'
  },
  {
    title: '本月折旧',
    align: "center",
    dataIndex: 'monthlyDep'
  },
  {
    title: '累计折旧',
    align: "center",
    dataIndex: 'accumulatedDep'
  },
  {
    title: '净值',
    align: "center",
    dataIndex: 'netValue'
  },
];

// 高级查询数据
export const superQuerySchema = {
  equNo: {title: '设备编号',order: 0,view: 'text', type: 'string',},
  equName: {title: '设备名称',order: 1,view: 'text', type: 'string',},
  depMonth: {title: '折旧年月 ',order: 2,view: 'text', type: 'string',},
  method: {title: '折旧方法',order: 3,view: 'text', type: 'string',},
  purchaseAmount: {title: '购置金额',order: 4,view: 'number', type: 'number',},
  usefulLifeMonths: {title: '使用寿命(月)',order: 5,view: 'number', type: 'number',},
  salvageRate: {title: '净残率%',order: 6,view: 'number', type: 'number',},
  initNetValue: {title: '初期净值',order: 7,view: 'number', type: 'number',},
  monthlyDep: {title: '本月折旧',order: 8,view: 'number', type: 'number',},
  accumulatedDep: {title: '累计折旧',order: 9,view: 'number', type: 'number',},
  netValue: {title: '净值',order: 10,view: 'number', type: 'number',},
};
