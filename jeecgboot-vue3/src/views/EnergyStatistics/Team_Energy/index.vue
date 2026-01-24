<template>
  <div class="flex h-full">
    <!-- 左侧维度树 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
        <a-tabs v-model:activeKey="activeTabKey" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane v-for="item in dimensionList" :key="item.dimensionCode" :tab="item.dimensionName">
            <a-card :bordered="false" style="height: 100%">
              <!-- 维度树组件，添加key强制刷新 -->
              <DimensionTree
                v-if="activeTabKey === item.dimensionCode"
                :key="item.dimensionCode"
                :nowtype="Number(item.dimensionCode) || 1"
                :select-level="2"
                @select="onTreeSelect"
                style="margin-top:-20px;"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
        <!-- 兜底显示：如果维度列表为空，显示提示或默认内容 -->
        <div v-if="dimensionList.length === 0" class="flex justify-center items-center h-full text-gray-400">
           暂无维度配置
        </div>
      </a-col>
    </div>

    <!-- 右侧班组列表 -->
    <div class="flex-1 mt-4 ml-2" style="min-width: 0;">
      <a-card :bordered="false" style="height: 100%">
        <div class="mb-4">
          <span class="text-lg font-bold">班组列表</span>
          <span v-if="currentDimensionName" class="ml-2 text-gray-500">- {{ currentDimensionName }}</span>
          <!-- 调试信息，正式上线可移除 -->
          <span class="ml-4 text-xs text-gray-400" v-if="currentSelectedNode">
            (调试: 维度={{ activeTabKey }}, 节点ID={{ currentSelectedNode.key }})
          </span>
        </div>
        
        <a-table
          :columns="columns"
          :dataSource="teamList"
          :loading="loading"
          rowKey="id"
          :pagination="false"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a @click="handleDetail(record)">详情</a>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';
import { message } from 'ant-design-vue';

// 维度列表
const dimensionList = ref<any[]>([]);
// 当前选中的Tab Key (即 dimensionCode)
const activeTabKey = ref<string>('1');
// 班组列表数据
const teamList = ref<any[]>([]);
// 加载状态
const loading = ref<boolean>(false);
// 当前选中的树节点信息
const currentSelectedNode = ref<any>(null);
// 当前维度名称（用于显示）
const currentDimensionName = ref<string>('');

// 表格列定义
const columns = [
  {
    title: '班组编码',
    dataIndex: 'teamCode',
    key: 'teamCode',
  },
  {
    title: '班组名称',
    dataIndex: 'teamName',
    key: 'teamName',
  },
  {
    title: '班次类型',
    dataIndex: 'shiftType',
    key: 'shiftType',
  },
  {
    title: '操作',
    key: 'action',
  },
];

// 获取维度配置
function loadDimensionConfigs() {
  defHttp.get({ url: '/energy/team/getDimensionConfigs' })
    .then((res) => {
      if (res.success && res.result && res.result.length > 0) {
        dimensionList.value = res.result;
        activeTabKey.value = dimensionList.value[0].dimensionCode;
      } else {
        useFallbackDimensions();
      }
    })
    .catch((err) => {
      console.error("加载维度配置失败，使用默认配置", err);
      useFallbackDimensions();
    });
}

// 使用默认维度配置（当后端未配置或出错时）
function useFallbackDimensions() {
  dimensionList.value = [
    { dimensionCode: '1', dimensionName: '组织架构' },
    { dimensionCode: '2', dimensionName: '时间维度' },
    { dimensionCode: '3', dimensionName: '设备类型' }
  ];
  activeTabKey.value = '1';
}

// Tab切换处理
function handleTabChange(key: string) {
  activeTabKey.value = key;
  teamList.value = [];
  currentSelectedNode.value = null;
  currentDimensionName.value = '';
}

// 树节点选择处理
// 注意：DimensionTree 组件 emit 'select' 时传递的是 node 数据对象，而不是 keys 数组
function onTreeSelect(data: any) {
  console.log('onTreeSelect', data);
  if (data) {
    currentSelectedNode.value = data;
    currentDimensionName.value = data.title || data.name || data.text;
    // 使用 key 作为 dimensionCode 查询
    loadTeamList(data.key);
  }
}

// 加载班组列表
function loadTeamList(dimensionCodeValue: string) {
  loading.value = true;
  defHttp.get({
    url: '/energy/team/listByDimension',
    params: {
      dimensionCode: dimensionCodeValue,
      dimensionType: activeTabKey.value
    }
  }).then((res) => {
    if (res.success) {
      teamList.value = res.result;
    } else {
      // 如果后端返回错误，清空列表
      teamList.value = [];
      // message.warning(res.message || '暂无数据');
    }
  }).catch(e => {
    console.error("加载班组数据失败", e);
    teamList.value = [];
  }).finally(() => {
    loading.value = false;
  });
}

// 详情点击
function handleDetail(record: any) {
  message.info(`查看班组详情: ${record.teamName}`);
}

onMounted(() => {
  loadDimensionConfigs();
});
</script>

<style scoped>
/* 保持原有样式或按需调整 */
</style>
