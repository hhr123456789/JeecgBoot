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

    <!-- 编辑设备弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="`编辑设备 - ${selectedNode?.name || ''}`"
      width="800px"
      @ok="handleSaveDevice"
      @cancel="handleCancelEdit"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="11">
            <a-form-item label="设备编号">
              <a-input v-model:value="editForm.code" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="11">
            <a-form-item label="设备名称">
              <a-input v-model:value="editForm.name" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="11">
            <a-form-item label="设备状态">
              <a-select v-model:value="editForm.status">
                <a-select-option value="正常">正常</a-select-option>
                <a-select-option value="运行">运行</a-select-option>
                <a-select-option value="维护">维护</a-select-option>
                <a-select-option value="故障">故障</a-select-option>
                <a-select-option value="停机">停机</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="11">
            <a-form-item label="所在位置">
              <a-input v-model:value="editForm.location" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="11">
            <a-form-item label="品牌">
              <a-input v-model:value="editForm.brand" />
            </a-form-item>
          </a-col>
          <a-col :span="11">
            <a-form-item label="类别">
              <a-input v-model:value="editForm.category" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="11">
            <a-form-item label="单位">
              <a-input v-model:value="editForm.unit" />
            </a-form-item>
          </a-col>
          <a-col :span="11">
            <a-form-item label="型号">
              <a-input v-model:value="editForm.model" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="11">
            <a-form-item label="制造商">
              <a-input v-model:value="editForm.manufacturer" />
            </a-form-item>
          </a-col>
          <a-col :span="11">
            <a-form-item label="安装时间">
              <a-date-picker v-model:value="editForm.installDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row>
          <a-col :span="22">
            <a-form-item label="备注" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
              <a-textarea v-model:value="editForm.remark" :rows="3" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 查看历史弹窗 -->
    <a-modal
      v-model:open="historyModalVisible"
      :title="`${selectedNode?.name || ''} - 设备历史记录`"
      width="800px"
      :footer="null"
    >
      <div class="history-timeline" style="padding:10px">
        <div v-for="(item, index) in historyData" :key="index" class="timeline-item">
          <div class="timeline-date">{{ item.date }}</div>
          <div class="timeline-content">
            <div class="timeline-title">{{ item.title }}</div>
            <div class="timeline-desc">{{ item.description }}</div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 维护记录弹窗 -->
    <a-modal
      v-model:open="maintenanceModalVisible"
      :title="`${selectedNode?.name || ''} - 维护记录`"
      width="800px"
      :footer="null"
    >
      <div class="maintenance-list" style="padding:15px">
        <div v-for="(item, index) in maintenanceData" :key="index" class="maintenance-item">
          <div class="maintenance-header">
            <div class="maintenance-date">{{ item.date }}</div>
            <a-tag :color="item.status === '已完成' ? 'success' : 'processing'">
              {{ item.status }}
            </a-tag>
          </div>
          <div class="maintenance-content">
            <div class="maintenance-title">{{ item.title }}</div>
            <div class="maintenance-desc">{{ item.description }}</div>
            <div class="maintenance-person">维护人员：{{ item.person }}</div>
          </div>
        </div>
      </div>
    </a-modal>
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
import { message, Modal } from 'ant-design-vue'
import dayjs, { Dayjs } from 'dayjs'
// 图片资源导入
import scannerImage from '/@/assets/images/空压机.png'

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

// 弹窗相关状态
const editModalVisible = ref(false)
const historyModalVisible = ref(false)
const maintenanceModalVisible = ref(false)

// 编辑表单
const editForm = reactive({
  code: '',
  name: '',
  status: '',
  location: '',
  brand: '',
  category: '',
  unit: '',
  model: '',
  manufacturer: '',
  installDate: null as Dayjs | null,
  remark: ''
})

// 历史记录数据
const historyData = ref([
  {
    date: '2024-03-15',
    title: '设备状态变更',
    description: '状态从“维护”变更为“正常”'
  },
  {
    date: '2024-03-10',
    title: '定期维护',
    description: '完成润滑系统维护，更换滤芯'
  },
  {
    date: '2024-02-20',
    title: '设备检查',
    description: '检查压缩机运行状态，各项参数正常'
  },
  {
    date: '2024-01-15',
    title: '定期保养',
    description: '更换滤芯，清洁冷却系统'
  },
  {
    date: '2021-03-01',
    title: '设备安装',
    description: '设备安装完成，开始投入使用'
  }
])

// 维护记录数据
const maintenanceData = ref([
  {
    date: '2024-03-10',
    status: '已完成',
    title: '润滑系统维护',
    description: '计划对空压机润滑系统进行全面检查和维护',
    person: '张师傅'
  },
  {
    date: '2024-02-20',
    status: '已完成',
    title: '压缩机运行状态检查',
    description: '检查压缩机运行状态，各项参数正常，无异常',
    person: '李师傅'
  },
  {
    date: '2024-01-15',
    status: '已完成',
    title: '定期保养 - 更换滤芯',
    description: '更换空气滤芯和油滤芯，清洁冷却系统',
    person: '王师傅'
  },
  {
    date: '2023-12-01',
    status: '已完成',
    title: '年度大保养',
    description: '年度全面保养，包括电机、压缩机、冷却系统等',
    person: '张师傅、李师傅'
  }
])

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
    title: '公司总部',
    nodeType: 'company',
    children: [
      {
        key: 'category-1',
        title: '生产部',
        nodeType: 'category',
        children: [
          {
            key: 'device-1',
            title: '空压机房(SB20181122000001)',
            nodeType: 'device',
            status: '正常',
            code: 'SB20181122000001',
            name: '空压机房',
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
                title: '空压机(SB20181122000003)',
                nodeType: 'component',
                status: '正常',
                code: 'SB20181122000003',
                name: '空压机',
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
        title: '挤压',
        nodeType: 'category',
        children: [
          {
            key: 'device-2',
            title: '挤压一厂(DEV20180000004)',
            nodeType: 'device',
            status: '运行',
            code: 'DEV20180000004',
            name: '挤压一厂',
            location: '挤压',
            brand: 'SIEMENS',
            category: '挤压系统',
            unit: '台',
            model: 'AD-2018-PRO',
            manufacturer: 'xxxx(中国)有限公司',
            installDate: '2018-05-15',
            image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=AD液晶系统仪',
            children: [
              {
                key: 'component-2',
                title: '1#挤压机(HC20181213000113)',
                nodeType: 'component',
                status: '正常',
                code: 'HC20181213000113',
                name: '1#挤压机',
                location: '挤压一厂A区',
                brand: 'SIEMENS',
                category: '挤压设备',
                unit: '台',
                model: 'HC-RT-113',
                manufacturer: 'xxx(中国)有限公司',
                installDate: '2018-12-13',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=运转车'
              },
              {
                key: 'component-3',
                title: '2#挤压机(HC00000027)',
                nodeType: 'component',
                status: '维护',
                code: 'HC00000027',
                name: '2#挤压机',
                location: '挤压一厂A区',
                brand: 'BOSCH',
                category: '挤压设备',
                unit: '台',
                model: 'BSH-ADJ-027',
                manufacturer: 'xxx(中国)投资有限公司',
                installDate: '2018-08-20',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=调节仪'
              },
              {
                key: 'component-4',
                title: '3#挤压机(HC00000027)',
                nodeType: 'component',
                status: '维护',
                code: 'HC00000027',
                name: '3#挤压机',
                location: '挤压一厂A区',
                brand: 'BOSCH',
                category: '挤压设备',
                unit: '台',
                model: 'BSH-ADJ-027',
                manufacturer: 'xxx(中国)投资有限公司',
                installDate: '2018-08-20',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=调节仪'
              },
              {
                key: 'component-5',
                title: '4#挤压机(HC00000027)',
                nodeType: 'component',
                status: '维护',
                code: 'HC00000027',
                name: '4#挤压机',
                location: '挤压一厂A区',
                brand: 'BOSCH',
                category: '挤压设备',
                unit: '台',
                model: 'BSH-ADJ-027',
                manufacturer: 'xxx(中国)投资有限公司',
                installDate: '2018-08-20',
                image: 'https://via.placeholder.com/300x200/f0f0f0/333?text=调节仪'
              }
            ]
          }
        ]
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
    // 填充编辑表单
    editForm.code = selectedNode.value.code || ''
    editForm.name = selectedNode.value.name || ''
    editForm.status = selectedNode.value.status || ''
    editForm.location = selectedNode.value.location || ''
    editForm.brand = selectedNode.value.brand || ''
    editForm.category = selectedNode.value.category || ''
    editForm.unit = selectedNode.value.unit || ''
    editForm.model = selectedNode.value.model || ''
    editForm.manufacturer = selectedNode.value.manufacturer || ''
    editForm.installDate = selectedNode.value.installDate ? dayjs(selectedNode.value.installDate) : null
    editForm.remark = selectedNode.value.remark || ''
    
    editModalVisible.value = true
  } else {
    message.warning('请先选择设备')
  }
}

const handleViewHistory = () => {
  if (selectedNode.value) {
    historyModalVisible.value = true
  } else {
    message.warning('请先选择设备')
  }
}

const handleMaintenance = () => {
  if (selectedNode.value) {
    maintenanceModalVisible.value = true
  } else {
    message.warning('请先选择设备')
  }
}

const handleDelete = () => {
  if (selectedNode.value) {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除设备 ${selectedNode.value.name} 吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk() {
        message.success(`已删除设备: ${selectedNode.value?.name}`)
      }
    })
  } else {
    message.warning('请先选择设备')
  }
}

// 弹窗相关方法
const handleSaveDevice = () => {
  if (!selectedNode.value) return
  
  // 更新设备数据
  if (selectedNode.value.code) selectedNode.value.code = editForm.code
  if (selectedNode.value.name) selectedNode.value.name = editForm.name
  if (selectedNode.value.status) selectedNode.value.status = editForm.status
  if (selectedNode.value.location) selectedNode.value.location = editForm.location
  if (selectedNode.value.brand) selectedNode.value.brand = editForm.brand
  if (selectedNode.value.category) selectedNode.value.category = editForm.category
  if (selectedNode.value.unit) selectedNode.value.unit = editForm.unit
  if (selectedNode.value.model) selectedNode.value.model = editForm.model
  if (selectedNode.value.manufacturer) selectedNode.value.manufacturer = editForm.manufacturer
  if (editForm.installDate) selectedNode.value.installDate = editForm.installDate.format('YYYY-MM-DD')
  if (selectedNode.value.remark !== undefined) selectedNode.value.remark = editForm.remark
  
  editModalVisible.value = false
  message.success('设备信息已保存')
}

const handleCancelEdit = () => {
  editModalVisible.value = false
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

/* 弹窗样式 */
.history-timeline {
  position: relative;
  padding-left: 20px;
}

.history-timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e6ecf5;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding-left: 20px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1677ff;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e6ecf5;
}

.timeline-date {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  font-weight: 500;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #2b3a55;
  margin-bottom: 4px;
}

.timeline-desc {
  font-size: 13px;
  color: #6b778c;
  line-height: 1.4;
}

/* 维护记录样式 */
.maintenance-list {
  max-height: 800px;
  overflow-y: auto;
}

.maintenance-item {
  padding: 16px;
  margin-bottom: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #1677ff;
}

.maintenance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.maintenance-date {
  font-size: 12px;
  color: #8c8c8c;
  font-weight: 500;
}

.maintenance-title {
  font-size: 14px;
  font-weight: 600;
  color: #2b3a55;
  margin-bottom: 6px;
}

.maintenance-desc {
  font-size: 13px;
  color: #6b778c;
  line-height: 1.4;
  margin-bottom: 6px;
}

.maintenance-person {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
