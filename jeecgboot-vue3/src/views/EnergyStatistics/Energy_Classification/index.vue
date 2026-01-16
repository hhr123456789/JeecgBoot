<template>
  <div class="energy-classification-container flex min-h-screen bg-gray-50 p-4">
    <!-- 左侧维度树 -->
    <div class="left-panel bg-white p-2 mr-4 rounded-lg shadow-sm" style="width:310px; flex-shrink: 0;">
      <a-tabs v-model:activeKey="activeTabKey" @change="handleTabChange" style="height: 100%;">
        <a-tab-pane v-for="item in dimensionList" :key="item.key" :tab="item.title">
          <ClassificationDimensionTree 
            v-if="activeTabKey === item.key"
            :ref="(el) => setTreeRef(el, item.key)" 
            @select="onDepartTreeSelect" 
            :nowtype="item.nowtype" 
            :select-level="2" 
            style="margin-top:-5px;" 
          />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <!-- 查询条件 -->
      <div class="mb-4 bg-white rounded-lg p-6 shadow-sm">
        <div class="flex items-center gap-4 flex-wrap min-h-[56px]">
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">时间维度:</span>
            <a-radio-group 
              v-model:value="timeUnit" 
              @change="handleTimeUnitChange"
              button-style="solid"
              size="middle"
            >
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="year">年</a-radio-button>
            </a-radio-group>
          </div>
          
          <a-date-picker 
            v-model:value="selectedDate"
            :format="dateFormat"
            :picker="pickerType"
            class="w-40"
            size="middle"
            @change="handleDateChange"
            :placeholder="timeUnit === 'month' ? '选择月份(显示每日)' : '选择年份(显示每月)'"
          />
          
          <a-button 
            type="primary" 
            size="middle"
            @click="loadData"
            :loading="loading"
          >
            查询
          </a-button>
          
          <a-button 
            type="default" 
            size="middle"
            @click="handleExport"
            :disabled="!selectedOrgInfo"
          >
            导出数据
          </a-button>
        </div>
        
        <!-- 当前选中部门信息 -->
        <div v-if="selectedOrgInfo" class="mt-2 text-sm text-gray-600">
          当前部门: {{ selectedOrgInfo.orgName }}
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid gap-4 mb-4" :class="energyType === 'all' ? 'grid-cols-4' : 'grid-cols-2'">
        <!-- 全部能源类型时：显示总能耗 -->
        <div v-if="energyType === 'all'" class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">总能耗</div>
          <div class="text-3xl font-bold mb-2 text-gray-800">
            {{ formatNumber(summaryData?.statisticsData?.totalConsumption || 0) }}
          </div>
          <div class="text-sm text-gray-500">单位: {{ getUnit() }}</div>
        </div>
        
        <!-- 全部能源类型时显示电能分项 -->
        <div v-if="energyType === 'all'" class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">电能消耗</div>
          <div class="text-3xl font-bold text-blue-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.electricConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: kWh</div>
        </div>
        
        <!-- 全部能源类型时显示水能分项 -->
        <div v-if="energyType === 'all'" class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">水能消耗</div>
          <div class="text-3xl font-bold text-cyan-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.waterConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: m³</div>
        </div>
        
        <!-- 全部能源类型时显示燃气分项 -->
        <div v-if="energyType === 'all'" class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">燃气消耗</div>
          <div class="text-3xl font-bold text-orange-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.gasConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: m³</div>
        </div>
        
        <!-- 单一能源类型时：显示该能源类型的消耗 -->
        <div v-if="energyType !== 'all'" class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">{{ getEnergyTypeName(energyType) }}消耗</div>
          <div class="text-3xl font-bold mb-2 text-blue-600">
            {{ formatNumber(summaryData?.statisticsData?.totalConsumption || 0) }}
          </div>
          <div class="text-sm text-gray-500">单位: {{ getUnit() }}</div>
        </div>
        
        <!-- 总成本卡片（所有情况都显示） -->
        <div class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">总成本</div>
          <div class="text-3xl font-bold text-green-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.totalCost || 0) }}</div>
          <div class="text-sm text-gray-500">单位: 元</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="mb-4">
        <div class="bg-white rounded-lg p-4 shadow-sm">
          <div class="mb-4">
            <span class="text-base font-medium text-gray-800">能源趋势对比</span>
          </div>
          <EnergyTrendLine 
            :chartData="trendData || { xAxis: { type: 'category', data: [] }, series: [] }" 
            v-if="trendData?.series?.length > 0"
          />
          <div v-else class="flex items-center justify-center h-64 text-gray-500">
            暂无数据
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <span class="text-base font-medium text-gray-800">能源分类数据</span>
          <div class="text-sm text-gray-600">
            共 {{ tableData.length }} 条数据
          </div>
        </div>
        <a-table
          :columns="dynamicColumns"
          :data-source="tableData"
          :pagination="{ 
            pageSize: 10, 
            showSizeChanger: true, 
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`
          }"
          :loading="loading"
          size="middle"
          :scroll="{ x: 1000 }"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, nextTick, ref } from 'vue';
import { useEnergyClassification } from './hooks/useEnergyClassification';
import EnergyDistributionPie from './components/EnergyDistributionPie.vue';
import EnergyTrendLine from './components/EnergyTrendLine.vue';
import ClassificationDimensionTree from './components/ClassificationDimensionTree.vue';

const {
  // 数据
  dimensionList,
  activeTabKey,
  selectedOrgInfo,
  queryParam,
  timeUnit,
  selectedDate,
  energyType,
  energyTypes,
  chartType,
  trendType,
  summaryData,
  trendData,
  tableData,
  loading,
  
  // 计算属性
  dateFormat,
  pickerType,
  dynamicColumns,
  
  // 方法
  handleTabChange,
  handleTimeUnitChange,
  handleEnergyTypeChange,
  handleTrendTypeChange,
  handleDateChange,
  loadData,
  handleExport,
  formatNumber,
  getUnit,
  getEnergyTypeName,
  onDepartTreeSelect,
  setTreeRef
} = useEnergyClassification();

onMounted(() => {
  // 组件挂载时的初始化逻辑已在hook中处理
  nextTick(() => {
    // 确保DOM完全渲染后再执行任何操作
  });
});
</script>

<style scoped lang="less">
.energy-classification-container {
  min-height: calc(100vh - 120px);
}

.left-panel {
  min-height: 600px;
  max-height: calc(100vh - 140px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  :deep(.ant-tabs) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  :deep(.ant-tabs-content) {
    flex: 1;
    overflow: auto;
  }
}

.tab-content {
  padding: 8px 0;
}

.org-tree {
  :deep(.ant-tree-node-content-wrapper) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

/* 图表容器样式 */
:deep(.chart-container) {
  width: 100%;
  height: 300px;
  min-height: 300px;
  position: relative;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

/* 确保图表区域有足够空间 */
:deep(.bg-white) {
  .chart-container {
    margin: 0 -16px; /* 抵消父容器的padding */
    border-radius: 0 0 8px 8px;
  }
}

// 自定义滚动条
:deep(.ant-table-body) {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .energy-classification-container {
    flex-direction: column;
    
    .w-80 {
      width: 100%;
      margin-right: 0;
      margin-bottom: 1rem;
    }
  }
  
  .grid-cols-4 {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .grid-cols-2 {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .grid-cols-4 {
    grid-template-columns: 1fr;
  }
  
  .p-4 {
    padding: 0.5rem;
  }
}
</style>
