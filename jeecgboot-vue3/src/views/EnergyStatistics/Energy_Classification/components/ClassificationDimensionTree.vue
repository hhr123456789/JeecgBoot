<template>
  <div class="bg-white overflow-hidden">
    <a-spin :spinning="loading">
      <template v-if="userIdentity === '2'">
        <!--组织机构树 - 只显示到二级-->
        <BasicTree
          v-if="!treeReloading"
          title="维度列表"
          toolbar
          search
          showLine
          :checkStrictly="true"
          :clickRowToExpand="false"
          :selectable="true"
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
  import { inject, nextTick, ref, onMounted, defineExpose, h, watch } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTree } from '/@/components/Tree';
  import { queryMydimensionTreeList, searchByKeywords } from '../../../Energy_Depart/depart.user.api';
  import {
    BuildOutlined,
    BankOutlined,
    SettingOutlined,
    ToolOutlined
  } from '@ant-design/icons-vue';

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

  // 加载部门信息 - 只显示到二级
  function loadDepartTreeData() {
    loading.value = true;
    treeData.value = [];
    queryMydimensionTreeList({ keyWord: props_type.nowtype})
      .then((res) => {
        console.log('能源分类-维度树数据:', res);
        if (res.success) {
          if (Array.isArray(res.result)) {
            // 过滤数据，只保留到二级节点
            const filteredData = filterToSecondLevel(res.result);
            // 为树数据添加图标和类型标识
            treeData.value = enhanceTreeDataWithIcons(filteredData);
            console.log('处理后的树数据（只到二级）:', treeData.value);
            userIdentity.value = res.message;
            // 自动展开到二级并选中
            autoExpandToTargetLevelNode(props_type.selectLevel);
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => (loading.value = false));
  }
  
  // 过滤树数据，只保留到二级节点
  function filterToSecondLevel(data: any[]): any[] {
    return data.map(node => {
      const newNode = { ...node };
      
      // 如果有子节点
      if (newNode.children && newNode.children.length > 0) {
        // 只保留第一级子节点，移除第三级及以下
        newNode.children = newNode.children.map((child: any) => {
          const newChild = { ...child };
          // 移除第三级子节点
          delete newChild.children;
          // 标记为叶子节点，这样就不会显示展开图标
          newChild.isLeaf = true;
          return newChild;
        });
      }
      
      return newNode;
    });
  }
  
  // 获取当前选中的节点数据
  function getSelectedNodeData() {
    return selectedNodeData.value;
  }

  onMounted(() => {
    loadDepartTreeData();
  });

  // 监听 nowtype 变化，重新加载数据
  watch(() => props_type.nowtype, (newVal, oldVal) => {
    if (newVal !== oldVal && oldVal !== undefined) {
      console.log('维度类型切换:', oldVal, '->', newVal);
      // 清空选中状态，避免自动触发查询
      selectedKeys.value = [];
      selectedNodeData.value = null;
      treeData.value = [];
      expandedKeys.value = [];
      // 重新加载数据（会自动选中第一个节点，但由于父组件已清空，不会触发查询）
      loadDepartTreeData();
    }
  });

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

  // 自动展开父节点，并选中指定级别的第一个节点
  async function autoExpandToTargetLevelNode(targetLevel = 2) {
    let foundNodePath: Array<any> = []; // 存储找到的目标节点的路径
    
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
      console.log('ClassificationDimensionTree - 触发select事件:', data);
      // 使用 nextTick 确保在 DOM 更新后触发事件
      nextTick(() => {
        emit('select', data);
      });
    }
  }

  // 搜索事件
  function onSearch(value: string) {
    if (value) {
      loading.value = true;
      searchByKeywords({ keyWord: value, myDeptSearch: '1' })
        .then((result) => {
          if (Array.isArray(result)) {
            // 过滤搜索结果，只保留到二级
            const filteredResult = filterToSecondLevel(result);
            // 为搜索结果添加图标和类型标识
            treeData.value = enhanceTreeDataWithIcons(filteredResult);
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
    if (selKeys.length > 0) {
      // 获取选中的节点数据
      const selectedNode = event.selectedNodes && event.selectedNodes.length > 0 ? event.selectedNodes[0] : null;
      
      if (selectedNode) {
        setSelectedKey(selKeys[0], selectedNode);
      } else {
        // 尝试直接从 treeData 中查找
        const nodeData = findNodeByKey(treeData.value, selKeys[0]);
        if (nodeData) {
          setSelectedKey(selKeys[0], nodeData);
        }
      }
    }
  }

  // 树展开事件
  function onExpand(keys) {
    expandedKeys.value = keys;
    autoExpandParent.value = false;
  }

  // 为树节点数据添加图标和类型标识
  function enhanceTreeDataWithIcons(data: any[], level: number = 0): any[] {
    return data.map(node => {
      const enhancedNode = { ...node };

      // 先删除所有可能影响图标显示的字段
      delete enhancedNode['icon'];
      delete enhancedNode['slots'];
      delete enhancedNode['scopedSlots'];

      // 根据节点层级和特征确定类型
      if (!node.nodeType) {
        // 第一层级：公司
        if (level === 0) {
          enhancedNode.nodeType = 'company';
        }
        // 第二层级：部门/类别
        else if (level === 1) {
          enhancedNode.nodeType = 'category';
        }
      }

      // 也可以根据标题关键字进行判断（作为补充）
      if (node.title) {
        if (node.title.includes('公司') || node.title.includes('总部') || node.title.includes('企业')) {
          enhancedNode.nodeType = 'company';
        } else if (node.title.includes('部门') || node.title.includes('部') || node.title.includes('厂') || node.title.includes('车间')) {
          enhancedNode.nodeType = 'category';
        }
      }

      // 根据 nodeType 设置新图标
      const nodeType = enhancedNode.nodeType;
      if (nodeType === 'company') {
        enhancedNode.icon = 'ant-design:build-outlined';
      } else if (nodeType === 'category') {
        enhancedNode.icon = 'ant-design:bank-outlined';
      } else {
        enhancedNode.icon = 'ant-design:folder-outlined';
      }

      // 递归处理子节点，传递下一层级（但只处理二级以内）
      if (enhancedNode.children && enhancedNode.children.length > 0 && level < 1) {
        enhancedNode.children = enhanceTreeDataWithIcons(enhancedNode.children, level + 1);
      }

      return enhancedNode;
    });
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

  /* 隐藏所有可能的重复图标 */
  :deep(.ant-tree-iconEle) {
    display: none !important;
  }

  /* 只隐藏叶子节点的文件图标，保留有子节点的展开/收起箭头 */
  :deep(.ant-tree-switcher-noop) {
    .ant-tree-switcher-line-icon {
      display: none !important;
    }
  }

  /* 隐藏复选框图标 */
  :deep(.ant-tree-checkbox) {
    margin-right: 4px; /* 只调整间距，不隐藏 */
  }

  /* 隐藏文件夹默认图标 */
  :deep(.ant-tree-node-content-wrapper) {
    .ant-tree-iconEle::before {
      display: none !important;
    }
  }

  /* 确保自定义图标正常显示 */
  :deep(.anticon) {
    font-size: 14px;
  }
</style>
