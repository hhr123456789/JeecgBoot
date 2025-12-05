<template>
  <div class="product-carbon-footprint p-4">
    <!-- 顶部卡片：产品信息 + 表单 -->
    <a-card class="main-card">
      <div class="product-grid">
        <!-- 左侧：产品信息 -->
        <div class="product-info-section">
          <img :src="currentProduct.image " :alt="currentProduct.name" class="product-image" />
          
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">产品名称</span>
              <strong class="info-value">{{ currentProduct.name || '——' }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">所属行业</span>
              <strong class="info-value">{{ currentProduct.industry || '——' }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">型号</span>
              <strong class="info-value">{{ currentProduct.model || '——' }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">生命周期阶段</span>
              <strong class="info-value">{{ currentProduct.stage || '——' }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">碎足迹 (kgCO₂e)</span>
              <strong class="info-value">{{ currentProduct.factorValue || '——' }} {{ currentProduct.unit || '' }}</strong>
            </div>
          </div>
        </div>

        <!-- 右侧：产品编辑表单 -->
        <div class="form-section">
          <div class="form-title">新建 / 编辑产品碎足迹记录</div>
          
          <a-form layout="horizontal" :model="productForm" :label-col="{ span: 9 }" :wrapper-col="{ span: 15 }">
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="产品名称" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-input v-model:value="productForm.name" placeholder="例如：高效铝型材" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="所属行业" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-select v-model:value="productForm.industry" placeholder="请选择行业">
                    <a-select-option value="">请选择行业</a-select-option>
                    <a-select-option value="汽车零部件">汽车零部件</a-select-option>
                    <a-select-option value="轨道交通">轨道交通</a-select-option>
                    <a-select-option value="消费电子">消费电子</a-select-option>
                    <a-select-option value="新能源装备">新能源装备</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="产品型号" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-input v-model:value="productForm.model" placeholder="例如：AL-7000 Series" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="生命周期阶段" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-select v-model:value="productForm.stage" placeholder="请选择阶段">
                    <a-select-option value="原料获取">原料获取</a-select-option>
                    <a-select-option value="生产制造">生产制造</a-select-option>
                    <a-select-option value="运输物流">运输物流</a-select-option>
                    <a-select-option value="使用维护">使用维护</a-select-option>
                    <a-select-option value="报废回收">报废回收</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="碎排放因子 (kgCO₂e/单位)" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-input-number 
                    v-model:value="productForm.factorValue" 
                    :precision="2"
                    :step="0.1"
                    placeholder="例如：128.6" 
                    style="width: 100%" 
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="排放因子来源" :label-col="{ span: 8 }"
      :wrapper-col="{ span: 16 }">
                  <a-input v-model:value="productForm.factor" placeholder="例如：ISO 14067 数据库" />
                </a-form-item>
              </a-col>
            </a-row>
            
           
                <a-form-item label="产品说明" :label-col="{ span: 4 }"
  :wrapper-col="{ span: 20 }" >
                  <a-textarea 
                    v-model:value="productForm.desc" 
                    :rows="5"
                    placeholder="记录材料构成、功能描述等" 
                  />
                </a-form-item>
             
            
            <div class="form-actions">
              <a-button @click="resetForm">清空</a-button>
              <a-button type="primary" @click="saveProduct">保存记录</a-button>
            </div>
          </a-form>
        </div>
      </div>
    </a-card>

    <!-- 产品台账表格 -->
    <a-card title="产品碳足迹台账" class="mt-4">
      <div class="filter-bar">
        <a-select v-model:value="filters.industry" style="width: 130px" placeholder="行业筛选">
          <a-select-option value="">行业筛选</a-select-option>
          <a-select-option 
            v-for="industry in industryOptions" 
            :key="industry" 
            :value="industry"
          >
            {{ industry }}
          </a-select-option>
        </a-select>
        
        <a-select v-model:value="filters.stage" style="width: 130px" placeholder="生命周期阶段">
          <a-select-option value="">生命周期阶段</a-select-option>
          <a-select-option 
            v-for="stage in stageOptions" 
            :key="stage" 
            :value="stage"
          >
            {{ stage }}
          </a-select-option>
        </a-select>
        
        <a-input 
          v-model:value="filters.keyword" 
          placeholder="搜索产品名称 / 型号" 
          style="width: 200px"
          @pressEnter="applyFilter"
        />
        
        <a-button type="primary" @click="applyFilter">查询</a-button>
        <a-button @click="resetFilter">重置</a-button>
      </div>
      
      <a-table
        :dataSource="filteredProducts"
        :columns="tableColumns"
        :pagination="{ pageSize: 10 }"
        rowKey="code"
        class="product-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'stage'">
            <a-tag :class="getStageTagClass(record.stage)">{{ record.stage }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'factorValue'">
            <a-tag class="footprint-pill">{{ record.factorValue }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'operation'">
            <a-button size="small" @click="viewProduct(record)" class="view-btn">
              查看
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { 
  Card, Form, Input, Select, Button, Table, InputNumber, 
  Textarea, Tag, message 
} from 'ant-design-vue';

const ACard = Card;
const AForm = Form;
const AFormItem = Form.Item;
const AInput = Input;
const AInputNumber = InputNumber;
const ASelect = Select;
const ASelectOption = Select.Option;
const AButton = Button;
const ATable = Table;
const ATextarea = Textarea;
const ATag = Tag;


import chiller1Image from '/@/assets/images/空压机.png';

// 产品生命周期阶段选项
const stageOptions = ['原料获取', '生产制造', '运输物流', '使用维护', '报废回收'];

// 产品模拟数据
const products = ref([
  { 
    code: 'P-1001', 
    name: '高效铝型材（工装版）', 
    industry: '汽车零部件', 
    model: 'AL-7000', 
    stage: '生产制造', 
    unit: '吨', 
    factorValue: 128.6, 
    factor: 'ISO 14067 数据库', 
    desc: '用于新能源汽车电池托盘结构', 
    image: chiller1Image 
  },
  { 
    code: 'P-1002', 
    name: '轨交制动系统散热片', 
    industry: '轨道交通', 
    model: 'HX-920', 
    stage: '原料获取', 
    unit: '吨', 
    factorValue: 205.3, 
    factor: '企业实验室测算', 
    desc: '采用再生铝锭，回收率 72%', 
    image: chiller1Image 
  },
  { 
    code: 'P-1003', 
    name: '消费电子散热模组', 
    industry: '消费电子', 
    model: 'CM-450', 
    stage: '运输物流', 
    unit: '台', 
    factorValue: 72.9, 
    factor: 'ISO 缺省 + 供应链数据', 
    desc: '小批量出口北美', 
    image: chiller1Image 
  },
  { 
    code: 'P-1004', 
    name: '新能源电池包壳体', 
    industry: '新能源装备', 
    model: 'EV-BP-2024', 
    stage: '生产制造', 
    unit: '台', 
    factorValue: 156.8, 
    factor: '行业标准数据库', 
    desc: '适配三元锂电池模组', 
    image: chiller1Image 
  },
  { 
    code: 'P-1005', 
    name: '汽车座椅骨架', 
    industry: '汽车零部件', 
    model: 'SR-2024A', 
    stage: '原料获取', 
    unit: '套', 
    factorValue: 89.4, 
    factor: '供应商EPD + 企业测算', 
    desc: '主材：钢材、铝合金', 
    image: chiller1Image 
  }
]);

// 当前选中的产品
const currentProduct = ref({
  name: '',
  industry: '',
  model: '',
  stage: '',
  factorValue: '',
  unit: '',
  image: chiller1Image
});

// 产品表单数据
const productForm = reactive({
  name: '',
  industry: '',
  model: '',
  stage: '',
  factorValue: undefined as number | undefined,
  unit: '吨',
  factor: '',
  desc: ''
});

// 筛选条件
const filters = reactive({
  industry: '',
  stage: '',
  keyword: ''
});

// 获取行业选项
const industryOptions = computed(() => {
  return Array.from(new Set(products.value.map(p => p.industry)));
});

// 筛选后的产品列表
const filteredProducts = computed(() => {
  return products.value.filter(product => {
    const matchIndustry = !filters.industry || product.industry === filters.industry;
    const matchStage = !filters.stage || product.stage === filters.stage;
    const matchKeyword = !filters.keyword || 
      product.name.includes(filters.keyword) || 
      product.model.includes(filters.keyword);
    return matchIndustry && matchStage && matchKeyword;
  });
});

// 表格列配置
const tableColumns = [
  { title: '产品编号', dataIndex: 'code', key: 'code', width: 100 },
  { title: '产品名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '行业', dataIndex: 'industry', key: 'industry', width: 120 },
  { title: '型号', dataIndex: 'model', key: 'model', width: 120 },
  { title: '生命周期阶段', dataIndex: 'stage', key: 'stage', width: 120 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '碳排放因子 (kgCO₂e/单位)', dataIndex: 'factorValue', key: 'factorValue', width: 180 },
  { title: '排放因子来源', dataIndex: 'factor', key: 'factor', width: 160 },
  { title: '操作', dataIndex: 'operation', key: 'operation', width: 80 }
];

// 查看产品详情
const viewProduct = (product: any) => {
  currentProduct.value = { ...product };
  
  // 填充表单
  Object.assign(productForm, {
    name: product.name,
    industry: product.industry,
    model: product.model,
    stage: product.stage,
    factorValue: product.factorValue,
    unit: product.unit,
    factor: product.factor,
    desc: product.desc
  });
};

// 保存产品
const saveProduct = () => {
  if (!productForm.name.trim()) {
    message.error('请填写产品名称');
    return;
  }
  
  // 这里可以添加保存到后端的逻辑
  message.success('保存成功！示例页面：保存逻辑可对接后端接口');
};

// 重置表单
const resetForm = () => {
  Object.assign(productForm, {
    name: '',
    industry: '',
    model: '',
    stage: '',
    factorValue: undefined,
    unit: '吨',
    factor: '',
    desc: ''
  });
  
  currentProduct.value = {
    name: '',
    industry: '',
    model: '',
    stage: '',
    factorValue: '',
    unit: '',
    image: '/src/assets/images/kongya1.png'
  };
};

// 应用筛选
const applyFilter = () => {
  message.success('筛选条件已应用');
};

// 重置筛选
const resetFilter = () => {
  Object.assign(filters, {
    industry: '',
    stage: '',
    keyword: ''
  });
  message.info('筛选条件已重置');
};

// 获取生命周期阶段标签样式
const getStageTagClass = (stage: string) => {
  const map: Record<string, string> = {
    '原料获取': 'tag-green',
    '生产制造': 'tag-blue',
    '运输物流': 'tag-orange',
    '使用维护': 'tag-purple',
    '报废回收': 'tag-gray'
  };
  return map[stage] || 'tag-gray';
};

onMounted(() => {
  // 默认显示第一个产品
  if (products.value.length > 0) {
    viewProduct(products.value[0]);
  }
});
</script>

<style lang="less" scoped>
.product-carbon-footprint {
  background: #f0f2f5;
  min-height: 100vh;

  .main-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;

    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .product-grid {
    display: grid;
    grid-template-columns: 450px 1fr;
    gap: 32px;

    // 左侧产品信息
    .product-info-section {
      .product-image {
        width: 100%;
        height: 240px;
        object-fit: contain;
        border-radius: 6px;
        margin-bottom: 16px;
        background: #f5f5f5;
      }

      .info-list {
        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 8px 0;
          border-bottom: 1px solid #f0f0f0;
          font-size: 13px;
          line-height: 1.6;

          &:last-child {
            border-bottom: none;
          }

          .info-label {
            color: #666;
            flex-shrink: 0;
            margin-right: 12px;
          }

          .info-value {
            color: #262626;
            font-weight: 500;
            text-align: right;
            word-break: break-all;
          }
        }
      }
    }

    // 右侧表单
    .form-section {
      .form-title {
        font-size: 15px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 1px solid #e8e8e8;
      }

      :deep(.ant-form) {
        .ant-form-item {
          margin-bottom: 16px;
        }

        .ant-form-item-label {
          > label {
            font-size: 13px;
            color: #595959;
            font-weight: 500;
          }
        }

        .ant-input,
        .ant-select,
        .ant-input-number,
        .ant-input-affix-wrapper {
          font-size: 13px;
          border-radius: 4px;
          border-color: #d9d9d9;

          &:hover {
            border-color: #40a9ff;
          }

          &:focus,
          &.ant-input-focused,
          &.ant-select-focused,
          &.ant-input-number-focused {
            border-color: #40a9ff;
            box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
          }
        }

        .ant-select {
          width: 100%;

          .ant-select-selector {
            border-radius: 4px;
          }
        }

        .ant-input-number {
          width: 100%;
        }

        textarea.ant-input {
          resize: vertical;
          min-height: 64px;
        }
      }

      .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 24px;
        padding-top: 16px;
        border-top: 1px solid #e8e8e8;

        .ant-btn {
          font-size: 13px;
          height: 32px;
          padding: 0 20px;
          border-radius: 4px;

          &:not(.ant-btn-primary) {
            background: #fff;
            border-color: #d9d9d9;
            color: #595959;

            &:hover {
              color: #40a9ff;
              border-color: #40a9ff;
            }
          }
        }
      }
    }
  }

  .filter-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 12px 16px;
    border-bottom: 1px dashed #e6ecf5;
    margin-bottom: 16px;

    .ant-select,
    .ant-input {
      border: 1px solid #d9d9d9;
      border-radius: 4px;
    }

    .ant-btn {
      padding: 4px 15px;
      border-radius: 4px;
      font-size: 14px;
      height: 32px;
      line-height: 1.5715;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      &.ant-btn-primary {
        background: #1890ff;
        border-color: #1890ff;
        color: #fff;
        
        &:hover {
          background: #40a9ff;
          border-color: #40a9ff;
        }
      }
      
      &:not(.ant-btn-primary) {
        background: #f5f5f5;
        border-color: #d9d9d9;
        color: #666;
        
        &:hover {
          background: #e6e6e6;
        }
      }
    }
  }

  .product-table {
    :deep(.ant-table) {
      font-size: 13px;

      .ant-table-thead > tr > th {
        background: #f5f5f5;
        color: #333;
        font-weight: bold;
        border-bottom: 1px solid #d9d9d9;
        text-align: center;
      }

      .ant-table-tbody > tr {
        &:hover {
          > td {
            background: #f0f8ff;
          }
        }
        
        > td {
          border-bottom: 1px solid #f0f2f5;
          padding: 12px 10px;
        }
      }

      .ant-table-tbody > tr > td:first-child {
        font-weight: 500;
        color: #1890ff;
      }
    }
  }

  // 标签样式
  .footprint-pill {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: 999px;
    font-size: 12px;
    background: rgba(22, 119, 255, 0.08);
    color: #1677ff;
    font-weight: 500;
  }

  .tag-green {
    background: rgba(47, 197, 158, 0.12);
    color: #2fc59e;
    border: none;
  }

  .tag-blue {
    background: rgba(22, 119, 255, 0.12);
    color: #1677ff;
    border: none;
  }

  .tag-orange {
    background: rgba(255, 169, 64, 0.12);
    color: #fa8c16;
    border: none;
  }

  .tag-purple {
    background: rgba(114, 46, 209, 0.12);
    color: #722ed1;
    border: none;
  }

  .tag-gray {
    background: rgba(107, 119, 140, 0.12);
    color: #6b778c;
    border: none;
  }

  // 查看按钮样式
  .view-btn {
    background: #fff;
    border: 1px solid #d9d9d9;
    color: #1890ff;
    padding: 4px 15px;
    border-radius: 4px;
    font-size: 13px;
    height: 32px;
    line-height: 1.5715;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    &:hover {
      background: #f5f5f5;
      border-color: #1890ff;
      color: #40a9ff;
    }
  }

  @media (max-width: 1024px) {
    .product-grid {
      grid-template-columns: 1fr;
      gap: 16px;
      
      .form-section {
        .form-body {
          .form-row {
            grid-template-columns: 1fr;
            gap: 12px;
            margin-bottom: 12px;
          }
        }
      }
    }
  }

  @media (max-width: 768px) {
    .product-grid {
      grid-template-columns: 1fr;
      
      .product-info-section,
      .form-section {
        .form-card {
          .form-body {
            padding: 16px;
            
            .form-row {
              grid-template-columns: 1fr;
              gap: 12px;
              margin-bottom: 12px;
            }
          }
        }
      }
    }
    
    .filter-bar {
      flex-direction: column;
      gap: 8px;
      
      .ant-select,
      .ant-input,
      .ant-btn {
        width: 100%;
        margin-bottom: 4px;
      }
    }
  }
}
</style>