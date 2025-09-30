import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const opsMaintenance: AppRouteModule = {
  path: '/ops-maintenance',
  name: 'OpsMaintenance',
  component: LAYOUT,
  redirect: '/ops-maintenance/asset-overview',
  meta: {
    orderNo: 50,
    icon: 'ant-design:setting-outlined',
    title: t('routes.demo.opsMaintenance.moduleName'),
  },
  children: [
    {
      path: 'asset-overview',
      name: 'AssetOverview',
      component: () => import('/@/views/OpsMaintenance/AssetOverview/index.vue'),
      meta: {
        title: t('routes.demo.opsMaintenance.assetOverview'),
        icon: 'ant-design:appstore-outlined',
      },
    },
  ],
};

export default opsMaintenance;