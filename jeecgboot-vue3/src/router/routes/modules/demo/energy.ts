import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const energy: AppRouteModule = {
  path: '/energy',
  name: 'Energy',
  component: LAYOUT,
  redirect: '/energy/classification',
  meta: {
    orderNo: 30,
    icon: 'ion:settings-outline',
    title: t('routes.demo.energy.moduleName'),
  },
  children: [
    {
      path: 'classification',
      name: 'EnergyClassification',
      meta: {
        title: t('routes.demo.energy.classification'),
        ignoreKeepAlive: false,
      },
      component: () => import('/@/views/EnergyStatistics/Energy_Classification/index.vue'),
    },
  ],
};

export default energy;