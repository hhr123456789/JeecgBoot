<template>
  <div class="flex h-full">
    <!-- 左侧树形菜单 -->
    <div class="w-80 bg-white p-2 mr-2 rounded overflow-auto mt-4" style="width:310px;">
      <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1;height: 100%;background-color: white;padding-left: 10px;">
      <a-tabs defaultActiveKey="dz-info" @change="handleTabChange" style="height: 100%;width:300px;">
          <a-tab-pane tab="按部门（用电）" key="info1" forceRender>
           <a-card :bordered="false" style="height: 100%">
            <DimensionTree @select="onDepartTreeSelect" :nowtype="1" style="margin-top:-20px ;" />
            </a-card>
          </a-tab-pane>
         <a-tab-pane tab="按线路（用电）" key="info2">
            <a-card :bordered="false" style="height: 100%">
            <DimensionTree @select="onDepartTreeSelect" :nowtype="2" style="margin-top:-20px ;" />
            </a-card>
          </a-tab-pane>
          <a-tab-pane tab="天然气" key="info3"    >
            <a-card :bordered="false" style="height: 100%">
            <DimensionTree @select="onDepartTreeSelect" nowtype="3" style="margin-top:-20px ;" />
            </a-card>
          </a-tab-pane>
          <a-tab-pane tab="压缩空气" key="info4"    >
            <a-card :bordered="false" style="height: 100%">
            <DimensionTree @select="onDepartTreeSelect" nowtype="4" style="margin-top:-20px ;" />
            </a-card>
          </a-tab-pane>
          <a-tab-pane tab="企业用水" key="info5"    >
            <a-card :bordered="false" style="height: 100%">
            <DimensionTree @select="onDepartTreeSelect" nowtype="5" style="margin-top:-20px ;" />
            </a-card>
          </a-tab-pane>
          
        </a-tabs>
    </a-col>


    </div>

    <!-- 右侧内容区域 -->
    <div class="flex-1">
      <div class="real-data-monitor-container p-4">
        <!-- 顶部导航栏 -->
         <!--
        <div class="mb-4">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="1" tab="全厂用电"></a-tab-pane>
            <a-tab-pane key="2" tab="全厂用水"></a-tab-pane>
            <a-tab-pane key="3" tab="全厂用天然气"></a-tab-pane>
            <a-tab-pane key="4" tab="全厂用缩空气"></a-tab-pane>
          </a-tabs>
        </div>
        -->

        <!-- 主要内容区域 -->
        <div class="grid grid-cols-2 gap-2">
          <!-- 1#产线1#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线1#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">采集时间</span>
                <span class="text-green-500">2025-07-04 10:00:00</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线2#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线2#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">采集时间</span>
                <span class="text-green-500">2025-07-04 10:00:00</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线3#电表-->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线3#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">采集时间</span>
                <span class="text-green-500">2025-07-04 10:00:00</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线4#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线4#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">采集时间</span>
                <span class="text-green-500">2025-07-04 10:00:00</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>

          <!-- 1#产线5#电表 -->
          <a-card class="monitor-card" :bordered="false">
            <div class="flex items-center mb-2">
              <div class="w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center mr-2">
                <span class="text-white text-xs">O</span>
              </div>
              <span class="text-base font-bold">1#产线5#电表</span>
            </div>
            <div class="space-y-1 data-table">
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷状态</span>
                <span class="text-green-500">正常</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">负荷率</span>
                <div class="flex-1 mx-1">
                  <div class="relative h-1.5 bg-gray-200 rounded">
                    <div class="absolute left-0 top-0 h-full bg-blue-500 rounded" :style="{ width: '63.10%' }"></div>
                  </div>
                </div>
                <span class="text-xs text-gray-600">63.10%</span>
              </div>
              <div class="flex items-center text-sm">
                <span class="text-gray-600 w-16">采集时间</span>
                <span class="text-green-500">2025-07-04 10:00:00</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>总功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>时间：{{ workshop1.l2.toFixed(2) }}</span>
                <span>总有功功率：{{ workshop1.l3.toFixed(2) }} KW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-0.5 data-row">
                <span>A相电流：{{ workshop1.l1.toFixed(2) }} A</span>
                <span>B相电流：{{ workshop1.l2.toFixed(2) }} A</span>
                <span>C相电流：{{ workshop1.l3.toFixed(2) }} A</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相电压：{{ workshop1.l1.toFixed(2) }} V</span>
                <span>B相电压：{{ workshop1.l2.toFixed(2) }} V</span>
                <span>C相电压：{{ workshop1.l3.toFixed(2) }} V</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相功率因素：{{ workshop1.l1.toFixed(2) }}</span>
                <span>B相功率因素：{{ workshop1.l2.toFixed(2) }}</span>
                <span>C相功率因素：{{ workshop1.l3.toFixed(2) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>A相有功功率：{{ workshop1.l1.toFixed(2) }} kW</span>
                <span>B相有功功率：{{ workshop1.l2.toFixed(2) }} kW</span>
                <span>C相有功功率：{{ workshop1.l3.toFixed(2) }} kW</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm mt-1 data-row">
                <span>有功电量：{{ workshop1.l1.toFixed(2) }} kW·h</span>
                <span>无功电量：{{ workshop1.l2.toFixed(2) }} kvar·h</span>
                <span>日用电量：{{ workshop1.l3.toFixed(2) }} kWh</span>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {provide,ref, onMounted, onUnmounted,nextTick  } from 'vue';
import RealTimeChart from './components/RealTimeChart.vue';
import HistoryChart from './components/HistoryChart.vue';
import ProductionParamsChart from './components/ProductionParamsChart.vue';
import DimensionTree from '../../Energy_Depart/components/DimensionTree.vue';


// 定义 currentWname 变量
  const currentWname = ref('');
//定义切换树变量
const selecttag = ref('');
//切换树到默认第一个tab
handleTabChange("info1"); 


// 新增 handleTabChange 方法
function handleTabChange(key) {
  //debugger;
  // 检查当前选中的key（'info1'或'info2'）
  if (key === 'info1') {
    // 触发 onDepartTreeSelect 并传递tab1默认数据
    deafulDepartTreeSelect('A10003A06A01');
    selecttag.value='1';
  } else if (key === 'info2') {
    // 触发 onDepartTreeSelect 并传递tab2默认数据
    deafulDepartTreeSelect('A10004A01A01');
    selecttag.value='2';
  }else if (key === 'info3') {
    // 触发 onDepartTreeSelect 并传递tab3默认数据
    deafulDepartTreeSelect('A10005A01A01');
    selecttag.value='3';
  }else if (key === 'info4') {
    // 触发 onDepartTreeSelect 并传递tab4默认数据
    deafulDepartTreeSelect('A10006A01A01');
    selecttag.value='4';
  }else if (key === 'info5') {
    // 触发 onDepartTreeSelect 并传递tab5默认数据
    deafulDepartTreeSelect('A10007A01A01');
    selecttag.value='5';
  }
}


 // 左侧树选择后触发
 function onDepartTreeSelect(data) {
    //console.log("michael111");
    //console.log(data);
    //console.log(data[0]);
    // 检查data是否为数组且不为空
    if (Array.isArray(data) && data.length > 0) {
      // 使用map提取每个对象的'departName'，然后用join连接成字符串
      const orgCodestr = data.map(item => item.orgCode).join(',');
      //console.log(orgCodestr);

      // 更新changeIframeSrc
       //changeIframeSrc(orgCodestr);
    } else {
      console.log("没有选中任何项目");
      // 处理没有选中任何项目的情况
    }
    //changeIframeSrc(data[0]["departName"]);
    //DepartData.value = data;
  }

  function deafulDepartTreeSelect(Wname){
    console.log(Wname);
  }

interface CurrentData {
  total: number;
  l1: number;
  l2: number;
  l3: number;
}





// 当前激活的标签页
const activeTab = ref('1');

// 各车间电流数据
const workshop1 = ref<CurrentData>({
  total: 84.61,
  l1: 27.87,
  l2: 28.26,
  l3: 28.48
});

const workshop2 = ref<CurrentData>({
  total: 74.24,
  l1: 20.20,
  l2: 47.72,
  l3: 6.32
});

const workshop3 = ref<CurrentData>({
  total: 31.88,
  l1: 1.57,
  l2: 75.19,
  l3: 0.245
});

const workshop4 = ref<CurrentData>({
  total: 80.63,
  l1: 30.63,
  l2: 40.12,
  l3: 9.88
});

const workshop5 = ref<CurrentData>({
  total: 99.28,
  l1: 30.63,
  l2: 44.12,
  l3: 24.53
});

// 模拟实时数据
const powerMonitorData = ref<{
  categories: string[];
  series: Array<{
    name: string;
    data: number[];
  }>;
}>({
  categories: [],
  series: [
    { name: '电流', data: [] },
    { name: '电压', data: [] },
    { name: '功率', data: [] }
  ]
});



// 模拟实时数据更新
let timer: number | null = null;

const updateRealTimeData = () => {
  const now = new Date();
  const timeStr = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
  
  const newCategories = [...powerMonitorData.value.categories, timeStr];
  const newSeries = powerMonitorData.value.series.map(series => ({
    name: series.name,
    data: [...series.data, Number((Math.random() * (series.name === '电压' ? 220 : 100)).toFixed(2))]
  }));

  // 保持最近30个数据点
  if (newCategories.length > 30) {
    newCategories.shift();
    newSeries.forEach(series => series.data.shift());
  }

  powerMonitorData.value = {
    categories: newCategories,
    series: newSeries
  };

  // 更新车间数据
  workshop1.value = updateCurrentData(workshop1.value);
  workshop2.value = updateCurrentData(workshop2.value);
  workshop3.value = updateCurrentData(workshop3.value);
  workshop4.value = updateCurrentData(workshop4.value);
  workshop5.value = updateCurrentData(workshop5.value);
};

// 模拟更新电流数据
const updateCurrentData = (current: CurrentData): CurrentData => {
  const variation = 0.1; // 10% 的变化范围
  const round = (num: number) => Number(num.toFixed(2));
  return {
    l1: round(current.l1 * (1 + (Math.random() - 0.5) * variation)),
    l2: round(current.l2 * (1 + (Math.random() - 0.5) * variation)),
    l3: round(current.l3 * (1 + (Math.random() - 0.5) * variation)),
    total: round(current.total * (1 + (Math.random() - 0.5) * variation))
  };
};

// 处理树节点选择
const handleSelect = (selectedKeys: string[], info: any) => {
  console.log('selected', selectedKeys, info);
  // 根据选中节点更新数据
};

onMounted(() => {
  // 启动实时数据更新
  timer = window.setInterval(updateRealTimeData, 1000);
});

onUnmounted(() => {
  // 清理定时器
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.h-full {
  /*min-height: calc(100vh - 100px);*/
}

.real-data-monitor-container {
  @apply min-h-screen bg-gray-50;
}

.monitor-card {
  @apply bg-white rounded-lg shadow-sm;
}

/* 自定义标签页样式 */
:deep(.ant-tabs-nav) {
  @apply mb-4;
}

:deep(.ant-card-body) {
  @apply p-3;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  @apply w-1;
}

::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded;
}

/* 树形菜单样式 */
:deep(.ant-tree) {
  font-size: 13px;
}

/* 搜索框样式 */
:deep(.ant-input-search) {
  font-size: 13px;
}

/* 进度条样式 */
.progress-bar {
  width: 100px;
  max-width: 100px;
}

.bg-blue-500 {
  max-width: 100px;
}

/* 数据表格样式 */
.data-table {
  margin-top: 8px;
  padding: 8px;
  background-color: rgba(249, 250, 251, 0.5);
  border-radius: 6px;
}

/* 数据行样式 */
.data-row {
  padding: 6px 0;
  border-bottom: 1px solid;
  border-image: linear-gradient(to right, rgba(229, 231, 235, 0.1), rgba(209, 213, 219, 0.8), rgba(229, 231, 235, 0.1));
  border-image-slice: 1;
}

.data-row:last-child {
  border-bottom: none;
}

.data-row span {
  color: #374151;
}
</style>

