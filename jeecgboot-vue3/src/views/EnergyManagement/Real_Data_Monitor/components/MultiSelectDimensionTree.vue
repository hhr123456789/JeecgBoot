<template>
  <div class="bg-white m-4 mr-0 overflow-hidden">
    <a-spin :spinning="loading">
      <template v-if="userIdentity === '2'">
        <!--组织机构树-->
        <BasicTree
          v-if="!treeReloading"
          title="维度列表"
          toolbar
          search
          showLine
          checkable
          :checkStrictly="false"
          :clickRowToExpand="false"
          :treeData="treeData"
          :checkedKeys="checkedKeys"
          :expandedKeys="expandedKeys"
          :autoExpandParent="autoExpandParent"
          @check="onCheck"
          @expand="onExpand"
          @search="onSearch"
        />
      </template>
      <a-empty v-else description="普通员工无此权限" />
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref, onMounted, defineExpose } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTree } from '/@/components/Tree';
  import { queryMydimensionTreeList, searchByKeywords } from '../../../Energy_Depart/depart.user.api';

  const prefixCls = inject('prefixCls');
  const props_type = defineProps({
    nowtype: { type: [String, Number], required: true },
    selectLevel: { type: Number, default: 2 }
  });

  const emit = defineEmits(['select']);
  const { createMessage } = useMessage();

  const loading = ref(false);
  const treeData = ref<any[]>([]);
  const checkedKeys = ref<string[]>([]);
  const expandedKeys = ref<string[]>([]);
  const autoExpandParent = ref(true);
  const treeReloading = ref(false);
  const userIdentity = ref('2');
  const selectedNodesData = ref<any[]>([]);

  // 加载部门信息
  function loadDepartTreeData() {
    loading.value = true;
    treeData.value = [];
    queryMydimensionTreeList({ keyWord: props_type.nowtype})
      .then((res) => {
        console.log('MultiSelectDimensionTree res=', res);
        if (res.success) {
          if (Array.isArray(res.result)) {
            treeData.value = res.result;
            userIdentity.value = res.message;
            autoExpandToTargetLevelNode(props_type.selectLevel);
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => (loading.value = false));
  }

  // 获取当前选中的节点数据
  function getSelectedNodeData() {
    return selectedNodesData.value;
  }

  onMounted(() => {
    loadDepartTreeData();
  });

  // 自动展开父节点，并选中指定级别的节点
  async function autoExpandToTargetLevelNode(targetLevel = 2) {
    let foundNodePaths: Array<Array<any>> = []; // 存储找到的目标节点的路径数组

    // 辅助函数，用于递归查找目标级别的所有节点
    function findTargetLevelNodes(node: any, level: number, currentPath: any[]) {
      if (level === targetLevel - 1) {
        // 找到了目标级别节点，记录路径
        foundNodePaths.push([...currentPath, node.key]);
        return;
      }
      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          findTargetLevelNodes(child, level + 1, [...currentPath, node.key]);
        }
      }
    }

    // 从根节点开始查找
    if (treeData.value.length > 0) {
      findTargetLevelNodes(treeData.value[0], 0, []);
    }

    // 如果找到了目标级别节点，展开并选中
    if (foundNodePaths.length > 0) {
      // 展开所有找到的路径
      const allExpandKeys = new Set<string>();
      foundNodePaths.forEach(path => {
        path.forEach(key => allExpandKeys.add(key));
      });
      expandedKeys.value = Array.from(allExpandKeys);

      // 根据维度类型决定选择多少个节点
      let selectCount = 1; // 默认选择1个节点

      // 获取要选中的节点
      const targetNodes: any[] = [];
      const targetIds: string[] = [];

      for (let i = 0; i < Math.min(selectCount, foundNodePaths.length); i++) {
        const targetNodeId = foundNodePaths[i][foundNodePaths[i].length - 1];
        const targetNode = findNodeByKey(treeData.value, targetNodeId);
        if (targetNode) {
          targetNodes.push(targetNode);
          targetIds.push(targetNode.id);
        }
      }

      if (targetNodes.length > 0) {
        // 选中目标级别节点
        console.log(`维度类型 ${props_type.nowtype} 默认选中 ${targetNodes.length} 个节点:`, targetNodes.map(n => n.title));
        setCheckedKeys(targetIds, targetNodes);
      } else {
        console.warn('未找到可选中的目标节点');
      }
    }
  }

  // 根据key查找节点
  function findNodeByKey(nodes: any[], key: string): any {
    for (const node of nodes) {
      if (node.key === key || node.id === key) {
        return node;
      }
      if (node.children && node.children.length > 0) {
        const found = findNodeByKey(node.children, key);
        if (found) {
          return found;
        }
      }
    }
    return null;
  }

  // 重新加载树组件，防止无法默认展开数据
  async function reloadTree() {
    await nextTick();
    treeReloading.value = true;
    await nextTick();
    treeReloading.value = false;
  }

  /**
   * 设置当前选中的行
   */
  function setCheckedKeys(keys: string[], data?: any[]) {
    checkedKeys.value = keys;
    if (data) {
      selectedNodesData.value = data;
      emit('select', data);
    }
  }

  // 搜索事件
  function onSearch(value: string) {
    if (value) {
      loading.value = true;
      searchByKeywords({ keyWord: value, myDeptSearch: '1' })
        .then((result) => {
          if (Array.isArray(result)) {
            treeData.value = result;
          } else {
            createMessage.warning('未查询到部门信息');
            treeData.value = [];
          }
        })
        .finally(() => (loading.value = false));
    } else {
      loadDepartTreeData();
    }
  }

  // 树选择事件 - 支持多选
  function onCheck(checkedKeysValue, event) {
    console.log('onCheck:', checkedKeysValue, event);
    
    // 获取选中的节点数据
    const selectedNodes = event.checkedNodes || [];
    const selectedData = selectedNodes.map(node => node);
    
    checkedKeys.value = Array.isArray(checkedKeysValue) ? checkedKeysValue : checkedKeysValue.checked;
    selectedNodesData.value = selectedData;
    
    // 触发选择事件，传递选中的节点数组
    emit('select', selectedData);
  }

  // 树展开事件
  function onExpand(keys) {
    expandedKeys.value = keys;
    autoExpandParent.value = false;
  }
  
  // 向父组件暴露方法
  defineExpose({
    loadDepartTreeData,
    getSelectedNodeData,
    autoExpandToTargetLevelNode
  });
</script>

<style lang="less" scoped>
  /*升级antd3后，查询框与树贴的太近，样式优化*/
  :deep(.jeecg-tree-header) {
    margin-bottom: 6px;
  }
</style>
