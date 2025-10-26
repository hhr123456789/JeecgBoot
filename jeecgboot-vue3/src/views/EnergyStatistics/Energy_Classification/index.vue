<template>
  <div class="energy-classification-container flex min-h-screen bg-gray-50 p-4">
    <!-- 左侧部门树 -->
    <div class="w-80 bg-white p-4 mr-4 rounded-lg shadow-sm">
      <div class="flex items-center justify-between mb-4">
        <span class="text-base font-medium text-gray-700">部门列表</span>
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索部门"
          class="w-40"
          size="middle"
          @search="handleSearch"
          @input="handleSearch"
        />
      </div>
      <a-tree
        v-model:selectedKeys="selectedOrgKeys"
        v-model:expandedKeys="expandedKeys"
        :tree-data="filteredOrgTreeData"
        :field-names="{ title: 'orgName', key: 'id', children: 'children' }"
        @select="handleOrgSelect"
        class="org-tree"
      />
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
              <a-radio-button value="day">日</a-radio-button>
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
          />
          
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">能源类型:</span>
            <a-select 
              v-model:value="energyType" 
              class="w-32"
              size="middle"
              @change="handleEnergyTypeChange"
            >
              <a-select-option value="all">全部能源</a-select-option>
              <a-select-option value="1">电能</a-select-option>
              <a-select-option value="2">水能</a-select-option>
              <a-select-option value="3">燃气</a-select-option>
            </a-select>
          </div>
          
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
      <div class="grid grid-cols-4 gap-4 mb-4">
        <div class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">总能耗</div>
          <div class="text-3xl font-bold text-gray-800 mb-2">{{ formatNumber(summaryData?.statisticsData?.totalConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: {{ getUnit() }}</div>
        </div>
        
        <div class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">电能消耗</div>
          <div class="text-3xl font-bold text-blue-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.electricConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: kWh</div>
        </div>
        
        <div class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">水能消耗</div>
          <div class="text-3xl font-bold text-cyan-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.waterConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: m³</div>
        </div>
        
        <div class="bg-white rounded-lg p-6 shadow-sm border border-gray-100 min-h-[170px]">
          <div class="text-gray-600 mb-3 text-sm">燃气消耗</div>
          <div class="text-3xl font-bold text-orange-600 mb-2">{{ formatNumber(summaryData?.statisticsData?.gasConsumption || 0) }}</div>
          <div class="text-sm text-gray-500">单位: m³</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-white rounded-lg p-4 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <span class="text-base font-medium text-gray-800">能源分类占比</span>
            <a-radio-group 
              v-model:value="chartType" 
              button-style="solid" 
              size="middle"
            >
              <a-radio-button value="consumption">能耗量</a-radio-button>
              <a-radio-button value="cost">成本</a-radio-button>
            </a-radio-group>
          </div>
          <EnergyDistributionPie 
            :chartData="summaryData?.pieChartData || { series: [] }" 
            v-if="summaryData?.pieChartData?.series?.length > 0"
          />
          <div v-else class="flex items-center justify-center h-64 text-gray-500">
            暂无数据
          </div>
        </div>

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
import { onMounted, nextTick } from 'vue';
import { useEnergyClassification } from './hooks/useEnergyClassification';
import EnergyDistributionPie from './components/EnergyDistributionPie.vue';
import EnergyTrendLine from './components/EnergyTrendLine.vue';

const {
  // 数据
  orgTreeData,
  filteredOrgTreeData,
  selectedOrgKeys,
  expandedKeys,
  searchText,
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
  handleOrgSelect,
  handleTimeUnitChange,
  handleEnergyTypeChange,
  handleTrendTypeChange,
  handleDateChange,
  loadData,
  handleExport,
  formatNumber,
  getUnit,
  getEnergyTypeName,
  filterOrgTree
} = useEnergyClassification();

const handleSearch = (value: string) => {
  filterOrgTree(value);
};

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
