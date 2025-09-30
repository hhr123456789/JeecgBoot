<template>
  <div class="asset-hierarchy-page">
    <div class="page-header">
      <h3 class="page-title">设备结构树</h3>
      <div class="header-actions">
        <a-button type="primary" @click="handleRefresh">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
        <a-button @click="handleExpandAll">
          <template #icon><FolderOpenOutlined /></template>
          展开全部
        </a-button>
        <a-button @click="handleCollapseAll">
          <template #icon><FolderOutlined /></template>
          收起全部
        </a-button>
      </div>
    </div>

    <div class="main-content">
      <a-row :gutter="16">
        <!-- 左侧：设备结构树 -->
        <a-col :span="8">
          <div class="tree-panel card-panel">
            <div class="panel-title">
              <span>设备结构树</span>
              <div class="legend">
                <a-tag color="blue" size="small">
                  <template #icon><BuildOutlined /></template>
                  公司
                </a-tag>
                <a-tag color="orange" size="small">
                  <template #icon><BankOutlined /></template>
                  设备类别
                </a-tag>
                <a-tag color="cyan" size="small">
                  <template #icon><SettingOutlined /></template>
                  设备
                </a-tag>
                <a-tag color="green" size="small">
                  <template #icon><ToolOutlined /></template>
                  器件
                </a-tag>
              </div>
            </div>
            
            <div class="tree-container">
              <a-tree
                v-model:expandedKeys="expandedKeys"
                v-model:selectedKeys="selectedKeys"
                :tree-data="treeData"
                :show-icon="true"
                :show-line="{ showLeafIcon: false }"
                :block-node="true"
                @select="onSelectNode"
                @expand="onExpandNode"
              >
                <template #icon="{ nodeType }">
                  <BuildOutlined v-if="nodeType === 'company'" style="color: #1890ff" />
                  <BankOutlined v-else-if="nodeType === 'category'" style="color: #fa8c16" />
                  <SettingOutlined v-else-if="nodeType === 'device'" style="color: #13c2c2" />
                  <ToolOutlined v-else style="color: #52c41a" />
                </template>
                
                <template #title="{ title, nodeType, status }">
                  <span class="tree-node-title">
                    {{ title }}
                    <a-tag 
                      v-if="status"
                      :color="status === '正常' ? 'success' : (status === '故障' ? 'error' : 'warning')"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ status }}
                    </a-tag>
                  </span>
                </template>
              </a-tree>
            </div>
          </div>
        </a-col>

        <!-- 右侧：设备信息详情 -->
        <a-col :span="16">
          <div class="detail-panel card-panel">
            <div class="panel-title">设备信息</div>
            
            <div v-if="selectedNode" class="device-detail">
              <!-- 设备图片 -->
              <div class="device-image-section">
                <div class="image-container">
                  <img :src="selectedNode.image" :alt="selectedNode.title" class="device-image" />
                </div>
              </div>

              <!-- 设备详细信息 -->
              <div class="device-info-section">
                <a-descriptions 
                  :title="selectedNode.title" 
                  :column="2" 
                  bordered 
                  size="small"
                  class="device-descriptions"
                >
                  <a-descriptions-item label="编号" :span="2">
                    <a-tag color="blue">{{ selectedNode.code }}</a-tag>
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="名称">
                    {{ selectedNode.name }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="状态">
                    <a-tag 
                      :color="selectedNode.status === '正常' ? 'success' : (selectedNode.status === '故障' ? 'error' : 'warning')"
                    >
                      {{ selectedNode.status }}
                    </a-tag>
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="所在位置" :span="2">
                    {{ selectedNode.location }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="品牌">
                    {{ selectedNode.brand }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="类别">
                    {{ selectedNode.category }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="单位">
                    {{ selectedNode.unit }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="型号">
                    {{ selectedNode.model }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="制造商" :span="2">
                    {{ selectedNode.manufacturer }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="安装时间" :span="2">
                    {{ selectedNode.installDate }}
                  </a-descriptions-item>
                  
                  <a-descriptions-item label="备注" :span="2">
                    {{ selectedNode.remark || '无' }}
                  </a-descriptions-item>
                </a-descriptions>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <a-button type="primary" @click="handleEdit">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </a-button>
                  <a-button @click="handleViewHistory">
                    <template #icon><HistoryOutlined /></template>
                    查看历史
                  </a-button>
                  <a-button @click="handleMaintenance">
                    <template #icon><ToolOutlined /></template>
                    维护记录
                  </a-button>
                  <a-button danger @click="handleDelete">
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </div>
              </div>
            </div>
            
            <!-- 未选择设备时的提示 -->
            <div v-else class="no-selection">
              <a-empty description="请在左侧选择设备查看详细信息" />
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  ReloadOutlined, 
  FolderOpenOutlined, 
  FolderOutlined,
  BuildOutlined,
  BankOutlined,
  SettingOutlined,
  ToolOutlined,
  EditOutlined,
  HistoryOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
// 图片资源导入
import scannerImage from '/@/assets/images/扫描仪.jpeg'

defineOptions({ name: 'AssetHierarchy' })

// 树形数据结构
interface TreeNode {
  key: string
  title: string
  nodeType: 'company' | 'category' | 'device' | 'component'
  status?: string
  code?: string
  name?: string
  location?: string
  brand?: string
  category?: string
  unit?: string
  model?: string
  manufacturer?: string
  installDate?: string
  remark?: string
  image?: string
  children?: TreeNode[]
}

// 响应式数据
const expandedKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const selectedNode = ref<TreeNode | null>(null)

// 获取所有节点的key
const getAllKeys = (nodes: TreeNode[]): string[] => {
  let keys: string[] = []
  nodes.forEach(node => {
    keys.push(node.key)
    if (node.children) {
      keys = keys.concat(getAllKeys(node.children))
    }
  })
  return keys
}

// 获取第一个设备节点
const getFirstDeviceKey = (nodes: TreeNode[]): string | null => {
  for (const node of nodes) {
    if (node.nodeType === 'device') {
      return node.key
    }
    if (node.children) {
      const found = getFirstDeviceKey(node.children)
      if (found) return found
    }
  }
  return null
}

// 模拟树形数据
const treeData = ref<TreeNode[]>([
  {
    key: 'company-1',
    title: '软件',
    nodeType: 'company',
    children: [
      {
        key: 'category-1',
        title: '办公设备',
        nodeType: 'category',
        children: [
          {
            key: 'device-1',
            title: '旋尺器(SB20181122000001)',
            nodeType: 'device',
            status: '正常',
            code: 'SB20181122000001',
            name: '旋尺器',
            location: '某部门控制份有限公司',
            brand: 'PEPCO',
            category: 'UTC',
            unit: '台',
            model: 'PEP-2018',
            manufacturer: '某部门控制份有限公司',
            installDate: '2018-11-22',
            image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=旋尺器',
            children: [
              {
                key: 'component-1',
                title: '扫描仪(SB20181122000003)',
                nodeType: 'component',
                status: '正常',
                code: 'SB20181122000003',
                name: '扫描仪',
                location: '某部门控制份有限公司',
                brand: 'PEPCO',
                category: 'UTC',
                unit: '台',
                model: 'PEP-SC-2018',
                manufacturer: '某部门控制份有限公司',
                installDate: '2018-11-22',
                image: scannerImage
              }
            ]
          }
        ]
      },
      {
        key: 'category-2',
        title: '精密',
        nodeType: 'category',
        children: [
          {
            key: 'device-2',
            title: 'AD液晶系统仪(DEV20180000004)',
            nodeType: 'device',
            status: '运行',
            code: 'DEV20180000004',
            name: 'AD液晶系统仪',
            location: '精密车间A区',
            brand: 'SIEMENS',
            category: 'AD系统',
            unit: '台',
            model: 'AD-2018-PRO',
            manufacturer: '西门子(中国)有限公司',
            installDate: '2018-05-15',
            image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=AD液晶系统仪',
            children: [
              {
                key: 'component-2',
                title: '运转车(HC20181213000113)',
                nodeType: 'component',
                status: '正常',
                code: 'HC20181213000113',
                name: '运转车',
                location: '精密车间A区',
                brand: 'SIEMENS',
                category: '运转设备',
                unit: '台',
                model: 'HC-RT-113',
                manufacturer: '西门子(中国)有限公司',
                installDate: '2018-12-13',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=运转车'
              },
              {
                key: 'component-3',
                title: '行重液机械减速调节仪(HC00000027)',
                nodeType: 'component',
                status: '维护',
                code: 'HC00000027',
                name: '行重液机械减速调节仪',
                location: '精密车间A区',
                brand: 'BOSCH',
                category: '调节设备',
                unit: '台',
                model: 'BSH-ADJ-027',
                manufacturer: '博世(中国)投资有限公司',
                installDate: '2018-08-20',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=调节仪'
              },
              {
                key: 'component-4',
                title: '黄昏百可胶缓慢感应系列测量仪(HC00000028)',
                nodeType: 'component',
                status: '故障',
                code: 'HC00000028',
                name: '黄昏百可胶缓慢感应系列测量仪',
                location: '精密车间A区',
                brand: 'HONEYWELL',
                category: '测量设备',
                unit: '台',
                model: 'HW-MSR-028',
                manufacturer: '霍尼韦尔(中国)有限公司',
                installDate: '2018-09-10',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=测量仪'
              },
              {
                key: 'component-5',
                title: '清算行程配件开关(HC00000032)',
                nodeType: 'component',
                status: '正常',
                code: 'HC00000032',
                name: '清算行程配件开关',
                location: '精密车间A区',
                brand: 'SCHNEIDER',
                category: '开关设备',
                unit: '个',
                model: 'SCH-SW-032',
                manufacturer: '施耐德电气(中国)有限公司',
                installDate: '2018-10-05',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=开关'
              }
            ]
          },
          {
            key: 'device-3',
            title: 'AD液晶系统仪2(DEV20180000005)',
            nodeType: 'device',
            status: '停机',
            code: 'DEV20180000005',
            name: 'AD液晶系统仪2',
            location: '精密车间B区',
            brand: 'SIEMENS',
            category: 'AD系统',
            unit: '台',
            model: 'AD-2018-PRO-V2',
            manufacturer: '西门子(中国)有限公司',
            installDate: '2018-06-20',
            image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=AD液晶系统仪2',
            children: [
              {
                key: 'component-6',
                title: '运转车2(HC20181213000114)',
                nodeType: 'component',
                status: '停机',
                code: 'HC20181213000114',
                name: '运转车2',
                location: '精密车间B区',
                brand: 'SIEMENS',
                category: '运转设备',
                unit: '台',
                model: 'HC-RT-114',
                manufacturer: '西门子(中国)有限公司',
                installDate: '2018-12-14',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=运转车2'
              }
            ]
          }
        ]
      }
    ]
  },
  {
    key: 'category-external-1',
    title: '注塑机器件',
    nodeType: 'category',
    children: [
      {
        key: 'device-external-1',
        title: 'product-translator-dept-entity',
        nodeType: 'device',
        status: '正常',
        code: 'PTD-2018-001',
        name: 'product-translator-dept-entity',
        location: '注塑车间',
        brand: 'Generic',
        category: '注塑设备',
        unit: '台',
        model: 'PTD-001',
        manufacturer: '通用设备制造有限公司',
        installDate: '2018-07-15',
        image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=注塑设备'
      },
      {
        key: 'device-external-2',
        title: 're',
        nodeType: 'device',
        status: '维护',
        code: 'RE-2018-002',
        name: 're',
        location: '注塑车间',
        brand: 'Generic',
        category: '回收设备',
        unit: '台',
        model: 'RE-002',
        manufacturer: '通用设备制造有限公司',
        installDate: '2018-08-01',
        image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=回收设备'
      }
    ]
  },
  {
    key: 'company-external',
    title: '各事办公司',
    nodeType: 'company',
    children: []
  }
])

// 方法定义
const onSelectNode = (selectedKeysValue: string[], info: any) => {
  if (selectedKeysValue.length > 0) {
    const key = selectedKeysValue[0]
    selectedNode.value = findNodeByKey(treeData.value, key)
  } else {
    selectedNode.value = null
  }
}

const onExpandNode = (expandedKeysValue: string[]) => {
  expandedKeys.value = expandedKeysValue
}

const findNodeByKey = (nodes: TreeNode[], key: string): TreeNode | null => {
  for (const node of nodes) {
    if (node.key === key) {
      return node
    }
    if (node.children) {
      const found = findNodeByKey(node.children, key)
      if (found) return found
    }
  }
  return null
}

const handleRefresh = () => {
  message.success('数据已刷新')
}

const handleExpandAll = () => {
  expandedKeys.value = getAllKeys(treeData.value)
  message.success('已展开全部节点')
}

const handleCollapseAll = () => {
  expandedKeys.value = []
  message.success('已收起全部节点')
}

const handleEdit = () => {
  if (selectedNode.value) {
    message.info(`编辑设备: ${selectedNode.value.title}`)
  }
}

const handleViewHistory = () => {
  if (selectedNode.value) {
    message.info(`查看历史: ${selectedNode.value.title}`)
  }
}

const handleMaintenance = () => {
  if (selectedNode.value) {
    message.info(`维护记录: ${selectedNode.value.title}`)
  }
}

const handleDelete = () => {
  if (selectedNode.value) {
    message.warning(`删除设备: ${selectedNode.value.title}`)
  }
}

// 组件挂载
onMounted(() => {
  // 默认全部展开
  expandedKeys.value = getAllKeys(treeData.value)
  
  // 默认选中扫描仪(SB20181122000003)
  const defaultDeviceKey = 'component-1' // 扫描仪的key
  selectedKeys.value = [defaultDeviceKey]
  selectedNode.value = findNodeByKey(treeData.value, defaultDeviceKey)
  
  message.success('设备结构树加载完成')
})
</script>

<style scoped>
.asset-hierarchy-page {
  padding: 12px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.main-content {
  height: calc(100vh - 120px);
}

.card-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
  transition: all 0.3s ease;
}

.card-panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.panel-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 16px;
  color: #262626;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.legend {
  display: flex;
  gap: 8px;
}

.tree-container {
  height: calc(100% - 50px);
  overflow-y: auto;
}

.tree-node-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.detail-panel {
  display: flex;
  flex-direction: column;
}

.device-detail {
  height: calc(100% - 50px);
  overflow-y: auto;
}

.device-image-section {
  margin-bottom: 24px;
}

.image-container {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.device-image {
  max-width: 300px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.device-info-section {
  flex: 1;
}

.device-descriptions {
  margin-bottom: 24px;
}

.device-descriptions :deep(.ant-descriptions-title) {
  font-weight: 600;
  color: #262626;
  font-size: 16px;
}

.device-descriptions :deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #595959;
}

.device-descriptions :deep(.ant-descriptions-item-content) {
  color: #262626;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.no-selection {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tree-container :deep(.ant-tree-node-content-wrapper) {
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.tree-container :deep(.ant-tree-node-content-wrapper:hover) {
  background-color: #f5f5f5;
}

.tree-container :deep(.ant-tree-node-content-wrapper.ant-tree-node-selected) {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.tree-container :deep(.ant-tree-treenode) {
  padding: 2px 0;
}

.tree-container :deep(.ant-tree-switcher) {
  color: #595959;
}

.tree-container :deep(.ant-tree-iconEle) {
  margin-right: 8px;
  display: inline-flex;
  align-items: center;
}

.tree-container :deep(.ant-tree-title) {
  display: inline-flex;
  align-items: center;
  flex: 1;
}
</style>