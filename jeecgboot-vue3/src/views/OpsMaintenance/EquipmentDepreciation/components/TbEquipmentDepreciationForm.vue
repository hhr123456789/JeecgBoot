<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="TbEquipmentDepreciationForm">
          <a-row>
						<a-col :span="12">
							<a-form-item label="设备编号" v-bind="validateInfos.equNo" id="TbEquipmentDepreciationForm-equNo" name="equNo">
								<a-input v-model:value="formData.equNo" placeholder="请输入设备编号"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="设备名称" v-bind="validateInfos.equName" id="TbEquipmentDepreciationForm-equName" name="equName">
								<a-input v-model:value="formData.equName" placeholder="请输入设备名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="折旧年月 " v-bind="validateInfos.depMonth" id="TbEquipmentDepreciationForm-depMonth" name="depMonth">
								<a-input v-model:value="formData.depMonth" placeholder="请输入折旧年月 "  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="折旧方法" v-bind="validateInfos.method" id="TbEquipmentDepreciationForm-method" name="method">
								<a-input v-model:value="formData.method" placeholder="请输入折旧方法"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="购置金额" v-bind="validateInfos.purchaseAmount" id="TbEquipmentDepreciationForm-purchaseAmount" name="purchaseAmount">
								<a-input-number v-model:value="formData.purchaseAmount" placeholder="请输入购置金额" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="使用寿命(月)" v-bind="validateInfos.usefulLifeMonths" id="TbEquipmentDepreciationForm-usefulLifeMonths" name="usefulLifeMonths">
								<a-input-number v-model:value="formData.usefulLifeMonths" placeholder="请输入使用寿命(月)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="净残率%" v-bind="validateInfos.salvageRate" id="TbEquipmentDepreciationForm-salvageRate" name="salvageRate">
								<a-input-number v-model:value="formData.salvageRate" placeholder="请输入净残率%" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="初期净值" v-bind="validateInfos.initNetValue" id="TbEquipmentDepreciationForm-initNetValue" name="initNetValue">
								<a-input-number v-model:value="formData.initNetValue" placeholder="请输入初期净值" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="本月折旧" v-bind="validateInfos.monthlyDep" id="TbEquipmentDepreciationForm-monthlyDep" name="monthlyDep">
								<a-input-number v-model:value="formData.monthlyDep" placeholder="请输入本月折旧" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="累计折旧" v-bind="validateInfos.accumulatedDep" id="TbEquipmentDepreciationForm-accumulatedDep" name="accumulatedDep">
								<a-input-number v-model:value="formData.accumulatedDep" placeholder="请输入累计折旧" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="净值" v-bind="validateInfos.netValue" id="TbEquipmentDepreciationForm-netValue" name="netValue">
								<a-input-number v-model:value="formData.netValue" placeholder="请输入净值" style="width: 100%" />
							</a-form-item>
						</a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../TbEquipmentDepreciation.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    equNo: '',   
    equName: '',   
    depMonth: '',   
    method: '',   
    purchaseAmount: undefined,
    usefulLifeMonths: undefined,
    salvageRate: undefined,
    initNetValue: undefined,
    monthlyDep: undefined,
    accumulatedDep: undefined,
    netValue: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    equNo: [{ required: true, message: '请输入设备编号!'},],
    equName: [{ required: true, message: '请输入设备名称!'},],
    depMonth: [{ required: true, message: '请输入折旧年月 !'},],
    purchaseAmount: [{ required: true, message: '请输入购置金额!'}, { pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},],
    usefulLifeMonths: [{ required: true, message: '请输入使用寿命(月)!'},],
    salvageRate: [{ required: false}, { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},],
    initNetValue: [{ required: false}, { pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},],
    netValue: [{ required: false}, { pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      if(props.formData.disabled === false){
        return false;
      }else{
        return true;
      }
    }
    return props.formDisabled;
  });

  
  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
      //赋值
      Object.assign(formData, tmpData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    //时间格式化
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
    }
    //循环数据
    for (let data in model) {
      //如果该数据是数组并且是字符串类型
      if (model[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串类型的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          model[data] = model[data].join(',');
        }
      }
    }
    await saveOrUpdate(model, isUpdate.value)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }


  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
