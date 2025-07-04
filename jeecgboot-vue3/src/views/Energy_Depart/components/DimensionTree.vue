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
    import { inject, nextTick, ref } from 'vue';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { BasicTree } from '/@/components/Tree';
    import { queryMydimensionTreeList, searchByKeywords } from '../depart.user.api';
  
    const prefixCls = inject('prefixCls');
    const props_type = defineProps(['nowtype']);
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
              autoExpandToFirstThirdLevelNode();
            }
          } else {
            createMessage.warning(res.message);
          }
        })
        .finally(() => (loading.value = false));
    }
  
    loadDepartTreeData();
  
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
  // 自动展开父节点，并选中第一个第三级节点
  async function autoExpandToFirstThirdLevelNode() {
    let foundNodePath: Array<any> = []; // 存储找到的第三级节点的路径
    //debugger;
    // 辅助函数，用于递归查找第三级节点
    function findThirdLevelNode(node: any, level: number, currentPath: any[]) {
      if (level === 2 ) {
        // 找到了第三级节点，记录路径并停止递归
        foundNodePath = [...currentPath, node.key];
        
        return true;
      }
      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          if (findThirdLevelNode(child, level + 1, [...currentPath, node.key])) {
            return true; // 如果在子节点中找到了，直接返回
          }
        }
      }
      return false;
    }
  
    
  
    // 从根节点开始查找
    if (treeData.value.length > 0) {
      findThirdLevelNode(treeData.value[0], 0, []);
    }
  
    // 如果找到了第三级节点，展开并选中
    if (foundNodePath.length > 0) {
      expandedKeys.value = foundNodePath;
      // 使用 findNodeByKey 函数找到第三级节点
    const thirdLevelNodeId = foundNodePath[foundNodePath.length - 1];
    const thirdLevelNode = findNodeByKey(treeData.value, thirdLevelNodeId);
  
    if (thirdLevelNode) {
      // 选中第三级节点
      setSelectedKey(thirdLevelNode.id, thirdLevelNode);
    } else {
      console.warn(`Node with key ${thirdLevelNodeId} not found in the tree.`);
    }
    }
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
  </script>
  <style lang="less" scoped>
    /*升级antd3后，查询框与树贴的太近，样式优化*/
    :deep(.jeecg-tree-header) {
      margin-bottom: 6px;
    }
  </style>
  