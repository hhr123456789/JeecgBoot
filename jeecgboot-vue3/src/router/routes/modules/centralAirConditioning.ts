import type { AppRouteModule } from '/@/router/types';
import { getParentLayout, LAYOUT } from '/@/router/constant';

const centralAirConditioning: AppRouteModule = {
  path: '/central-air-conditioning',
  name: 'CentralAirConditioning',
  component: LAYOUT,
  redirect: '/central-air-conditioning/equipment-ins',
  meta: {
    orderNo: 30,
    icon: 'ion:hardware-chip-outline',
    title: '中央空调系统',
  },
  children: [
    {
      path: 'equipment-ins',
      name: 'EquipmentIns',
      component: () => import('/@/views/CentralAirConditioning/Equipment_Ins/index.vue'),
      meta: {
        title: '设备概览',
        icon: 'ion:desktop-outline',
      },
    },
    {
      path: 'cooling-system',
      name: 'CoolingSystem',
      component: () => import('/@/views/CentralAirConditioning/CoolingSystem/index.vue'),
      meta: {
        title: '制冷系统',
        icon: 'ion:snow-outline',
      },
    },
  ],
};

export default centralAirConditioning;