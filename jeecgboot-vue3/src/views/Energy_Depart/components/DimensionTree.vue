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
            :checkStrictly="true"
            :clickRowToExpand="false"
            :treeData="treeData"
            :selectedKeys="selectedKeys"
            :expandedKeys="expandedKeys"
            :autoExpandParent="autoExpandParent"
            @select="onSelect"
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
    import { queryMydimensionTreeList, searchByKeywords } from '../depart.user.api';
  
    const prefixCls = inject('prefixCls');
    const props_type = defineProps({
      nowtype: { type: [String, Number], required: true },
      selectLevel: { type: Number, default: 2 }
    });
    const emit = defineEmits(['select']);
    const { createMessage } = useMessage();
    
    let loading = ref<boolean>(false);
    // 部门树列表数据
    let treeData = ref<any[]>([]);
    // 当前展开的项
    let expandedKeys = ref<any[]>([]);
    // 当前选中的项
    let selectedKeys = ref<any[]>([]);
    // 是否自动展开父级
    let autoExpandParent = ref<boolean>(true);
    // 用户身份
    let userIdentity = ref<string>('2');
    // 树组件重新加载
    let treeReloading = ref<boolean>(false);
    // 当前选中的节点数据
    let selectedNodeData = ref<any>(null);
  
    // 加载部门信息
    function loadDepartTreeData() {
      //debugger;
      loading.value = true;
      treeData.value = [];
      queryMydimensionTreeList({ keyWord: props_type.nowtype})
        .then((res) => {
          console.log('reshhr20250630=',res);
          if (res.success) {
            if (Array.isArray(res.result)) {
              
              treeData.value = res.result;
              userIdentity.value = res.message;
              //autoExpandParentNode();
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
      return selectedNodeData.value;
    }
  
    onMounted(() => {
      loadDepartTreeData();
    });
  
    //console.log('hhr2='+props_type.nowtype);
    
  
    // 自动展开父节点，只展开一级
    function autoExpandParentNode() {
      let keys: Array<any> = [];
      treeData.value.forEach((item, index) => {
        if (item.children && item.children.length > 0) {
          keys.push(item.key);
        }
        if (index === 0) {
          // 默认选中第一个
          setSelectedKey(item.id, item);
        }
      });
      if (keys.length > 0) {
        reloadTree();
        expandedKeys.value = keys;
      }
    }
  
  
    //===================================================================
  // 自动展开父节点，并选中指定级别的第一个节点
  async function autoExpandToTargetLevelNode(targetLevel = 2) {
    let foundNodePath: Array<any> = []; // 存储找到的目标节点的路径
    //debugger;
    // 辅助函数，用于递归查找目标级别节点
    function findTargetLevelNode(node: any, level: number, currentPath: any[]) {
      if (level === targetLevel - 1) {
        // 找到了目标级别节点，记录路径并停止递归
        foundNodePath = [...currentPath, node.key];
        return true;
      }
      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          if (findTargetLevelNode(child, level + 1, [...currentPath, node.key])) {
            return true; // 如果在子节点中找到了，直接返回
          }
        }
      }
      return false;
    }
  
    // 从根节点开始查找
    if (treeData.value.length > 0) {
      findTargetLevelNode(treeData.value[0], 0, []);
    }
  
    // 如果找到了目标级别节点，展开并选中
    if (foundNodePath.length > 0) {
      expandedKeys.value = foundNodePath;
      // 使用 findNodeByKey 函数找到目标级别节点
      const targetNodeId = foundNodePath[foundNodePath.length - 1];
      const targetNode = findNodeByKey(treeData.value, targetNodeId);
  
      if (targetNode) {
        // 选中目标级别节点
        setSelectedKey(targetNode.id, targetNode);
      } else {
        console.warn(`Node with key ${targetNodeId} not found in the tree.`);
      }
    }
  }
  
  // 保留原有函数作为兼容
  async function autoExpandToFirstThirdLevelNode() {
    autoExpandToTargetLevelNode(3);
  }
  
  
  function findNodeByKey(treeData: any[], searchKey: any): any | undefined {
    for (const node of treeData) {
      if (node.key === searchKey) {
        return node;
      }
      if (node.children && node.children.length > 0) {
        const foundNode = findNodeByKey(node.children, searchKey);
        if (foundNode) {
          return foundNode;
        }
      }
    }
    return undefined;
  }
  //===================================================================
  
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
    function setSelectedKey(key: string, data?: object) {
      selectedKeys.value = [key];
      if (data) {
        selectedNodeData.value = data;
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
  
    // 树选择事件
    function onSelect(selKeys, event) {
      if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
        setSelectedKey(selKeys[0], event.selectedNodes[0]);
      } else {
        // 这样可以防止用户取消选择
        setSelectedKey(selectedKeys.value[0]);
      }
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
  