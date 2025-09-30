<template>
  <div class="equipment-overview">
    <!-- 顶部标签页 -->
    <div class="top-tabs">
      <div 
        v-for="tab in mainTabs" 
        :key="tab.key"
        :class="['main-tab', { active: activeMainTab === tab.key }]"
        @click="switchMainTab(tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-layout">
      <!-- 左侧设备树 -->
      <div class="left-panel">
        <div class="search-section">
          <a-input
            v-model:value="searchValue"
            placeholder="输入关键字进行过滤"
            class="search-input"
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </div>
        
        <div class="device-tree">
          <!-- 冷热源设备树 -->
          <div v-if="activeMainTab === 'cooling-source'">
            <div class="tree-group">
              <div class="group-header">水冷/风冷机组</div>
              <div class="device-list">
                <div 
                  v-for="device in coolingDevices" 
                  :key="device.id"
                  :class="['device-item', { selected: selectedDevice?.id === device.id }]"
                  @click="selectDevice(device)"
                >
                  {{ device.name }}
                </div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">液体/冷却塔</div>
              <div class="device-list">
                <div class="device-item">室外冷却塔</div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">暖通/流量计</div>
              <div class="device-list">
                <div class="device-item">室外冷却塔流量计</div>
              </div>
            </div>
          </div>

          <!-- 水系统设备树 -->
          <div v-if="activeMainTab === 'water-system'">
            <div class="tree-group">
              <div class="group-header">冷冻水泵</div>
              <div class="device-list">
                <div 
                  v-for="device in waterDevices" 
                  :key="device.id"
                  :class="['device-item', { selected: selectedDevice?.id === device.id }]"
                  @click="selectDevice(device)"
                >
                  {{ device.name }}
                </div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">冷却水泵</div>
              <div class="device-list">
                <div class="device-item">冷却水泵1#</div>
                <div class="device-item">冷却水泵2#</div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">冷却塔</div>
              <div class="device-list">
                <div class="device-item">冷却塔1#</div>
                <div class="device-item">冷却塔2#</div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">水处理设备</div>
              <div class="device-list">
                <div class="device-item">软化水设备</div>
                <div class="device-item">循环水处理器</div>
              </div>
            </div>
          </div>

          <!-- 空气处理设备树 -->
          <div v-if="activeMainTab === 'air-handling'">
            <div class="tree-group">
              <div class="group-header">组合式空调机组(AHU)</div>
              <div class="device-list">
                <div 
                  v-for="device in airDevices" 
                  :key="device.id"
                  :class="['device-item', { selected: selectedDevice?.id === device.id }]"
                  @click="selectDevice(device)"
                >
                  {{ device.name }}
                </div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">新风机组(FAU)</div>
              <div class="device-list">
                <div class="device-item">新风机组1#</div>
                <div class="device-item">新风机组2#</div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">风机盘管(FCU)</div>
              <div class="device-list">
                <div class="device-item">FCU-1F-01</div>
                <div class="device-item">FCU-1F-02</div>
                <div class="device-item">FCU-2F-01</div>
              </div>
            </div>
            
            <div class="tree-group">
              <div class="group-header">排风设备</div>
              <div class="device-list">
                <div class="device-item">排风机1#</div>
                <div class="device-item">排风机2#</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧整体内容区域 -->
      <div class="right-content">
        <!-- 冷热源设备内容 -->
        <div v-if="activeMainTab === 'cooling-source'">
          <div class="overview-section">
            <!-- 设备概览 -->
            <div class="device-overview">
              <div class="display-header">
                <span class="display-title">设备概览</span>
                <span class="detail-link">详情 ></span>
              </div>
              <div class="device-image-container">
                <img :src="shebei1Image" alt="设备图片" class="main-device-image" />
              </div>
            </div>

            <!-- 数据概览 -->
            <div class="data-overview-section">
              <div class="section-header">数据概览</div>
              <div class="current-device">{{ selectedDevice?.name || '1#水冷/风冷机' }}</div>
              
              <!-- 状态指标卡片 -->
              <div class="status-indicators">
                <div class="indicator-card temperature">
                  <div class="indicator-icon">🏠</div>
                  <div class="indicator-value">{{ deviceStatus.temperature.value }}</div>
                  <div class="indicator-unit">{{ deviceStatus.temperature.unit }}</div>
                  <div class="indicator-label">运行状态</div>
                </div>
                
                <div class="indicator-card efficiency">
                  <div class="indicator-icon">📊</div>
                  <div class="indicator-value">{{ deviceStatus.efficiency.value }}</div>
                  <div class="indicator-unit">{{ deviceStatus.efficiency.unit }}</div>
                  <div class="indicator-label">当前效率</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 参数控制面板 -->
          <div class="parameters-section">
            <div class="param-modules">
              <!-- 压缩机模块 -->
              <div class="param-module">
                <div class="module-header">压缩机</div>
                <div class="module-params">
                  <!-- 第一行：吸排气温度 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">吸气温度</div>
                      <div class="param-value">5.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">排气温度</div>
                      <div class="param-value">48.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                  </div>
                  <!-- 第二行：吸排气压力 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">吸气压力</div>
                      <div class="param-value">0.45</div>
                      <div class="param-unit">MPa</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">排气压力</div>
                      <div class="param-value">1.8</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第三行：电流与功率 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">运行电流</div>
                      <div class="param-value">245.8</div>
                      <div class="param-unit">A</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">运行功率</div>
                      <div class="param-value">320.5</div>
                      <div class="param-unit">kW</div>
                    </div>
                  </div>
                  <!-- 第四行：油温油压 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">油温</div>
                      <div class="param-value">45.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">油压</div>
                      <div class="param-value">0.35</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第五行：制冷剂类型 -->
                  <div class="param-item gray">
                    <div class="param-name">制冷剂类型</div>
                    <div class="param-value">R410A</div>
                    <div class="param-unit"></div>
                  </div>
                </div>
              </div>

              <!-- 蒸发器模块 -->
              <div class="param-module">
                <div class="module-header">蒸发器</div>
                <div class="module-params">
                  <!-- 第一行：蒸发温度与压力 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">蒸发温度</div>
                      <div class="param-value">5.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">蒸发压力</div>
                      <div class="param-value">0.48</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第二行：冷凝温度与压力 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">冷凝温度</div>
                      <div class="param-value">42.8</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">冷凝压力</div>
                      <div class="param-value">1.75</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第三行：冷媒泄漏监测 -->
                  <div class="param-item gray">
                    <div class="param-name">冷媒泄漏监测</div>
                    <div class="param-value">正常</div>
                    <div class="param-unit"></div>
                  </div>
                </div>
              </div>

              <!-- 冷凝器模块 -->
              <div class="param-module">
                <div class="module-header">冷凝器</div>
                <div class="module-params">
                  <!-- 第一行：蒸发温度与压力 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">蒸发温度</div>
                      <div class="param-value">5.0</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">蒸发压力</div>
                      <div class="param-value">0.46</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第二行：冷凝温度与压力 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">冷凝温度</div>
                      <div class="param-value">43.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">冷凝压力</div>
                      <div class="param-value">1.78</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第三行：冷媒泄漏监测 -->
                  <div class="param-item gray">
                    <div class="param-name">冷媒泄漏监测</div>
                    <div class="param-value">正常</div>
                    <div class="param-unit"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 水系统设备内容 -->
        <div v-if="activeMainTab === 'water-system'">
          <div class="overview-section">
            <div class="device-overview">
              <div class="display-header">
                <span class="display-title">水系统设备概览</span>
                <span class="detail-link">详情 ></span>
              </div>
              <div class="device-image-container">
                <img :src="shebei1Image" alt="设备图片" class="main-device-image" />
              </div>
            </div>
            <div class="data-overview-section">
              <div class="section-header">数据概览</div>
              <div class="current-device">{{ selectedDevice?.name || '冷冻水泵1#' }}</div>
              <div class="status-indicators">
                <div class="indicator-card temperature">
                  <div class="indicator-icon">🌊</div>
                  <div class="indicator-value">{{ waterStatus.flow.value }}</div>
                  <div class="indicator-unit">{{ waterStatus.flow.unit }}</div>
                  <div class="indicator-label">水流量</div>
                </div>
                <div class="indicator-card efficiency">
                  <div class="indicator-icon">🌡️</div>
                  <div class="indicator-value">{{ waterStatus.temperature.value }}</div>
                  <div class="indicator-unit">{{ waterStatus.temperature.unit }}</div>
                  <div class="indicator-label">水温</div>
                </div>
              </div>
            </div>
          </div>
          <div class="parameters-section">
            <div class="param-modules">
              <!-- 冷冻水/冷却水循环系统模块 -->
              <div class="param-module">
                <div class="module-header">冷冻水/冷却水循环系统</div>
                <div class="module-params">
                  <!-- 第一行：流量与压差 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">流量</div>
                      <div class="param-value">285.6</div>
                      <div class="param-unit">m³/h</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">压差</div>
                      <div class="param-value">0.25</div>
                      <div class="param-unit">MPa</div>
                    </div>
                  </div>
                  <!-- 第二行：水温与温差 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">水温</div>
                      <div class="param-value">12.5</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">温差</div>
                      <div class="param-value">5.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                  </div>
                  <!-- 第三行：pH值与电导率 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">pH值</div>
                      <div class="param-value">7.2</div>
                      <div class="param-unit"></div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">电导率</div>
                      <div class="param-value">450</div>
                      <div class="param-unit">μS/cm</div>
                    </div>
                  </div>
                  <!-- 第四行：浊度 -->
                  <div class="param-item gray">
                    <div class="param-name">浊度</div>
                    <div class="param-value">2.1</div>
                    <div class="param-unit">NTU</div>
                  </div>
                </div>
              </div>

              <!-- 水泵与冷却塔模块 -->
              <div class="param-module">
                <div class="module-header">水泵与冷却塔</div>
                <div class="module-params">
                  <!-- 第一行：水泵电流与频率 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">水泵电流</div>
                      <div class="param-value">125.6</div>
                      <div class="param-unit">A</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">水泵频率</div>
                      <div class="param-value">45.8</div>
                      <div class="param-unit">Hz</div>
                    </div>
                  </div>
                  <!-- 第二行：进出水温度 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">冷却塔进水温度</div>
                      <div class="param-value">32.5</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">冷却塔出水温度</div>
                      <div class="param-value">27.8</div>
                      <div class="param-unit">°C</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 冷却塔模块 -->
              <div class="param-module">
                <div class="module-header">冷却塔</div>
                <div class="module-params">
                  <!-- 第一行：水泵电流与频率 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">水泵电流</div>
                      <div class="param-value">98.5</div>
                      <div class="param-unit">A</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">水泵频率</div>
                      <div class="param-value">48.2</div>
                      <div class="param-unit">Hz</div>
                    </div>
                  </div>
                  <!-- 第二行：进出水温度 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">进水温度</div>
                      <div class="param-value">35.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">出水温度</div>
                      <div class="param-value">29.8</div>
                      <div class="param-unit">°C</div>
                    </div>
                  </div>
                  <!-- 第三行：进出水温差 -->
                  <div class="param-item gray">
                    <div class="param-name">进出水温差</div>
                    <div class="param-value">5.4</div>
                    <div class="param-unit">°C</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空气处理设备内容 -->
        <div v-if="activeMainTab === 'air-handling'">
          <div class="overview-section">
            <div class="device-overview">
              <div class="display-header">
                <span class="display-title">空气处理设备概览</span>
                <span class="detail-link">详情 ></span>
              </div>
              <div class="device-image-container">
                <img :src="shebei1Image" alt="设备图片" class="main-device-image" />
              </div>
            </div>
            <div class="data-overview-section">
              <div class="section-header">数据概览</div>
              <div class="current-device">{{ selectedDevice?.name || 'AHU-1#' }}</div>
              <div class="status-indicators">
                <div class="indicator-card temperature">
                  <div class="indicator-icon">🌬️</div>
                  <div class="indicator-value">{{ airStatus.airflow.value }}</div>
                  <div class="indicator-unit">{{ airStatus.airflow.unit }}</div>
                  <div class="indicator-label">风量</div>
                </div>
                <div class="indicator-card efficiency">
                  <div class="indicator-icon">🌡️</div>
                  <div class="indicator-value">{{ airStatus.temperature.value }}</div>
                  <div class="indicator-unit">{{ airStatus.temperature.unit }}</div>
                  <div class="indicator-label">送风温度</div>
                </div>
              </div>
            </div>
          </div>
          <div class="parameters-section">
            <div class="param-modules">
              <!-- 组合式空调机组(AHU)模块 -->
              <div class="param-module">
                <div class="module-header">组合式空调机组(AHU)</div>
                <div class="module-params">
                  <!-- 第一行：送风与回风温度 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">送风温度</div>
                      <div class="param-value">18.5</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">回风温度</div>
                      <div class="param-value">24.2</div>
                      <div class="param-unit">°C</div>
                    </div>
                  </div>
                  <!-- 第二行：送风与回风湿度 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">送风湿度</div>
                      <div class="param-value">55.8</div>
                      <div class="param-unit">%</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">回风湿度</div>
                      <div class="param-value">62.5</div>
                      <div class="param-unit">%</div>
                    </div>
                  </div>
                  <!-- 第三行：风量与风速 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">风量</div>
                      <div class="param-value">15800</div>
                      <div class="param-unit">m³/h</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">风速</div>
                      <div class="param-value">3.2</div>
                      <div class="param-unit">m/s</div>
                    </div>
                  </div>
                  <!-- 第四行：过滤网压差与风机电流 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">过滤网压差</div>
                      <div class="param-value">150</div>
                      <div class="param-unit">Pa</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">风机电流</div>
                      <div class="param-value">85.6</div>
                      <div class="param-unit">A</div>
                    </div>
                  </div>
                  <!-- 第五行：轴承温度与振动值 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">轴承温度</div>
                      <div class="param-value">45.8</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">振动值</div>
                      <div class="param-value">2.1</div>
                      <div class="param-unit">mm/s</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 末端设备模块 -->
              <div class="param-module">
                <div class="module-header">末端设备</div>
                <div class="module-params">
                  <!-- 第一行：室内温度与湿度 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">室内温度</div>
                      <div class="param-value">22.5</div>
                      <div class="param-unit">°C</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">室内湿度</div>
                      <div class="param-value">58.2</div>
                      <div class="param-unit">%</div>
                    </div>
                  </div>
                  <!-- 第二行：风速与运行模式 -->
                  <div class="param-row">
                    <div class="param-item gray">
                      <div class="param-name">风速</div>
                      <div class="param-value">中速</div>
                      <div class="param-unit"></div>
                    </div>
                    <div class="param-item gray">
                      <div class="param-name">运行模式</div>
                      <div class="param-value">制冷</div>
                      <div class="param-unit"></div>
                    </div>
                  </div>
                  <!-- 第三行：阀门开度 -->
                  <div class="param-item gray">
                    <div class="param-name">阀门开度</div>
                    <div class="param-value">65</div>
                    <div class="param-unit">%</div>
                  </div>
                </div>
              </div>

              <!-- 额外的第三个模块（空） -->
              <div class="param-module">
                <div class="module-header">系统状态</div>
                <div class="module-params">
                  <!-- 第一行：系统状态相关参数 -->
                  <div class="param-row">
                    <div class="param-item blue">
                      <div class="param-name">系统压力</div>
                      <div class="param-value">1.2</div>
                      <div class="param-unit">kPa</div>
                    </div>
                    <div class="param-item blue">
                      <div class="param-name">运行时间</div>
                      <div class="param-value">8.5</div>
                      <div class="param-unit">h</div>
                    </div>
                  </div>
                  <!-- 第二行：系统状态 -->
                  <div class="param-item gray">
                    <div class="param-name">系统状态</div>
                    <div class="param-value">正常运行</div>
                    <div class="param-unit"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部监控图表区 -->
    <div class="bottom-charts">
      <div class="charts-header">
        <div class="charts-title">关键数据监控</div>
        <div class="charts-controls">
          <span class="date-display">2024-02-22</span>
          <a-select v-model:value="timeRange" class="time-range-selector">
            <a-select-option value="1h">近1小时</a-select-option>
            <a-select-option value="24h">近24小时</a-select-option>
            <a-select-option value="7d">近7天</a-select-option>
          </a-select>
        </div>
      </div>
      
      <div class="charts-legend">
        <div 
          v-for="(legend, index) in chartLegends" 
          :key="index"
          :class="['legend-item', { disabled: legend.disabled }]"
          @click="toggleLegend(index)"
        >
          <span class="legend-dot" :style="{ backgroundColor: legend.disabled ? '#ccc' : legend.color }"></span>
          <span class="legend-text" :style="{ color: legend.disabled ? '#ccc' : '#666' }">{{ legend.name }}</span>
        </div>
      </div>
      
      <div class="chart-container">
        <div id="main-chart" class="main-chart"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import shebei1Image from '/@/assets/images/shebei1.png'
import * as echarts from 'echarts'

// 当前激活的标签页
const activeMainTab = ref('cooling-source')
const activeParamTab = ref('compressor')
const searchValue = ref('')
const timeRange = ref('24h')

// 主标签页配置
const mainTabs = ref([
  { key: 'cooling-source', label: '冷热源设备' },
  { key: 'water-system', label: '水系统设备' },
  { key: 'air-handling', label: '空气处理设备' }
])

// 设备列表
const coolingDevices = ref([
  { id: '1', name: '1#水冷/风冷机' },
  { id: '3', name: '3#水冷/风冷机' },
  { id: '2', name: '2#水冷/风冷机' },
  { id: '4', name: '4#水冷/风冷机' }
])

const waterDevices = ref([
  { id: 'w1', name: '冷冻水泵1#' },
  { id: 'w2', name: '冷冻水泵2#' },
  { id: 'w3', name: '冷冻水泵3#' }
])

const airDevices = ref([
  { id: 'a1', name: 'AHU-1#' },
  { id: 'a2', name: 'AHU-2#' },
  { id: 'a3', name: 'AHU-3#' }
])

// 选中的设备
const selectedDevice = ref(coolingDevices.value[0])

// 设备状态数据
const deviceStatus = reactive({
  temperature: {
    value: '72.05',
    unit: '°C'
  },
  efficiency: {
    value: '37.41',
    unit: '%'
  }
})

// 水系统设备状态数据
const waterStatus = reactive({
  flow: {
    value: '285.6',
    unit: 'm³/h'
  },
  temperature: {
    value: '12.5',
    unit: '°C'
  }
})

// 空气处理设备状态数据
const airStatus = reactive({
  airflow: {
    value: '15800',
    unit: 'm³/h'
  },
  temperature: {
    value: '18.5',
    unit: '°C'
  }
})

// 参数标签页配置
const paramTabs = ref([
  { key: 'compressor', label: '压缩机' },
  { key: 'evaporator', label: '蒸发器' },
  { key: 'condenser', label: '冷凝器' }
])

// 参数数据
const paramData = reactive({
  compressor: [
    { key: 'suction-temp', label: '吸气温度', value: '5.2', unit: '°C', type: 'blue' },
    { key: 'discharge-temp', label: '排气温度', value: '48.2', unit: '°C', type: 'blue' },
    { key: 'suction-pressure', label: '吸气压力', value: '0.45', unit: 'MPa', type: 'blue' },
    { key: 'discharge-pressure', label: '排气压力', value: '1.8', unit: 'MPa', type: 'blue' },
    { key: 'current', label: '运行电流', value: '245.8', unit: 'A', type: 'gray' },
    { key: 'power', label: '运行功率', value: '320.5', unit: 'kW', type: 'gray' },
    { key: 'oil-temp', label: '油温', value: '45.2', unit: '°C', type: 'gray' },
    { key: 'oil-pressure', label: '油压', value: '0.35', unit: 'MPa', type: 'gray' },
    { key: 'refrigerant', label: '制冷剂类型', value: 'R410A', unit: '', type: 'gray' }
  ],
  evaporator: [
    { key: 'evap-temp', label: '蒸发温度', value: '5.2', unit: '°C', type: 'blue' },
    { key: 'evap-pressure', label: '蒸发压力', value: '0.48', unit: 'MPa', type: 'blue' },
    { key: 'cond-temp', label: '冷凝温度', value: '42.8', unit: '°C', type: 'blue' },
    { key: 'cond-pressure', label: '冷凝压力', value: '1.75', unit: 'MPa', type: 'blue' },
    { key: 'leak-monitor', label: '冷媒泄漏监测', value: '正常', unit: '', type: 'gray' }
  ],
  condenser: [
    { key: 'evap-temp', label: '蒸发温度', value: '5.0', unit: '°C', type: 'blue' },
    { key: 'evap-pressure', label: '蒸发压力', value: '0.46', unit: 'MPa', type: 'blue' },
    { key: 'cond-temp', label: '冷凝温度', value: '43.2', unit: '°C', type: 'blue' },
    { key: 'cond-pressure', label: '冷凝压力', value: '1.78', unit: 'MPa', type: 'blue' },
    { key: 'leak-monitor', label: '冷媒泄漏监测', value: '正常', unit: '', type: 'gray' }
  ]
})

// 水系统参数数据
const waterParamData = reactive({
  circulation: [
    { key: 'flow-rate', label: '流量', value: '285.6', unit: 'm³/h', type: 'blue' },
    { key: 'pressure-diff', label: '压差', value: '0.25', unit: 'MPa', type: 'blue' },
    { key: 'water-temp', label: '水温', value: '12.5', unit: '°C', type: 'blue' },
    { key: 'temp-diff', label: '温差', value: '5.2', unit: '°C', type: 'blue' },
    { key: 'ph-value', label: 'pH值', value: '7.2', unit: '', type: 'gray' },
    { key: 'conductivity', label: '电导率', value: '450', unit: 'μS/cm', type: 'gray' },
    { key: 'turbidity', label: '浊度', value: '2.1', unit: 'NTU', type: 'gray' }
  ],
  pump: [
    { key: 'pump-current', label: '水泵电流', value: '125.6', unit: 'A', type: 'blue' },
    { key: 'pump-frequency', label: '水泵频率', value: '45.8', unit: 'Hz', type: 'blue' },
    { key: 'tower-temp-in', label: '冷却塔进水温度', value: '32.5', unit: '°C', type: 'gray' },
    { key: 'tower-temp-out', label: '冷却塔出水温度', value: '27.8', unit: '°C', type: 'gray' }
  ],
  coolingTower: [
    { key: 'tower-current', label: '水泵电流', value: '98.5', unit: 'A', type: 'blue' },
    { key: 'tower-frequency', label: '水泵频率', value: '48.2', unit: 'Hz', type: 'blue' },
    { key: 'inlet-temp', label: '进水温度', value: '35.2', unit: '°C', type: 'gray' },
    { key: 'outlet-temp', label: '出水温度', value: '29.8', unit: '°C', type: 'gray' },
    { key: 'temp-diff', label: '进出水温差', value: '5.4', unit: '°C', type: 'gray' }
  ]
})

// 空气处理设备参数数据
const airParamData = reactive({
  ahu: [
    { key: 'supply-temp', label: '送风温度', value: '18.5', unit: '°C', type: 'blue' },
    { key: 'return-temp', label: '回风温度', value: '24.2', unit: '°C', type: 'blue' },
    { key: 'supply-humidity', label: '送风湿度', value: '55.8', unit: '%', type: 'blue' },
    { key: 'return-humidity', label: '回风湿度', value: '62.5', unit: '%', type: 'blue' },
    { key: 'airflow', label: '风量', value: '15800', unit: 'm³/h', type: 'gray' },
    { key: 'air-speed', label: '风速', value: '3.2', unit: 'm/s', type: 'gray' },
    { key: 'filter-pressure', label: '过滤网压差', value: '150', unit: 'Pa', type: 'gray' },
    { key: 'fan-current', label: '风机电流', value: '85.6', unit: 'A', type: 'gray' },
    { key: 'bearing-temp', label: '轴承温度', value: '45.8', unit: '°C', type: 'gray' },
    { key: 'vibration', label: '振动值', value: '2.1', unit: 'mm/s', type: 'gray' }
  ],
  terminal: [
    { key: 'room-temp', label: '室内温度', value: '22.5', unit: '°C', type: 'blue' },
    { key: 'room-humidity', label: '室内湿度', value: '58.2', unit: '%', type: 'blue' },
    { key: 'fan-speed', label: '风速', value: '中速', unit: '', type: 'gray' },
    { key: 'operation-mode', label: '运行模式', value: '制冷', unit: '', type: 'gray' },
    { key: 'valve-opening', label: '阀门开度', value: '65', unit: '%', type: 'gray' }
  ]
})

// 图表图例配置
const chartLegends = ref([
  { name: '室外温度-工况调用频率1', color: '#1890ff', disabled: false },
  { name: '室外温度-工况调用频率2', color: '#52c41a', disabled: false },
  { name: '室外温度-工况调用频率3', color: '#faad14', disabled: false }
])

// 图表实例
let chartInstance: any = null

// 计算当前参数
const currentParams = computed(() => {
  return paramData[activeParamTab.value] || []
})

// 方法
const selectDevice = (device: any) => {
  selectedDevice.value = device
  console.log('选择设备:', device.name)
}

// 切换主标签页时自动选择对应类型的第一个设备
const switchMainTab = (tabKey: string) => {
  activeMainTab.value = tabKey
  switch (tabKey) {
    case 'cooling-source':
      selectedDevice.value = coolingDevices.value[0]
      break
    case 'water-system':
      selectedDevice.value = waterDevices.value[0]
      break
    case 'air-handling':
      selectedDevice.value = airDevices.value[0]
      break
  }
}

// 切换图例显示/隐藏
const toggleLegend = (index: number) => {
  chartLegends.value[index].disabled = !chartLegends.value[index].disabled
  updateChart()
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) return
  
  const option = chartInstance.getOption()
  chartLegends.value.forEach((legend, index) => {
    if (option.series && option.series[index]) {
      option.series[index].lineStyle.opacity = legend.disabled ? 0 : 1
      option.series[index].itemStyle = {
        opacity: legend.disabled ? 0 : 1
      }
    }
  })
  
  chartInstance.setOption(option)
}

// 初始化图表
const initChart = () => {
  nextTick(() => {
    const chartDom = document.getElementById('main-chart')
    if (chartDom) {
      chartInstance = echarts.init(chartDom)
      
      // 生成模拟数据
      const timePoints: string[] = []
      const data1: number[] = []
      const data2: number[] = []
      const data3: number[] = []
      
      for (let i = 0; i < 100; i++) {
        timePoints.push(i.toString())
        data1.push(Math.sin(i * 0.1) * 200 + Math.random() * 100 + 500)
        data2.push(Math.cos(i * 0.12) * 150 + Math.random() * 80 + 400)
        data3.push(Math.sin(i * 0.08) * 100 + Math.random() * 60 + 300)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(50, 50, 50, 0.9)',
          borderColor: '#1890ff',
          borderWidth: 1,
          textStyle: {
            color: '#fff',
            fontSize: 12
          },
          formatter: function(params: any) {
            let result = '时间点: ' + params[0].axisValue + '<br/>'
            params.forEach((param: any) => {
              if (!chartLegends.value[param.seriesIndex].disabled) {
                result += '<span style="color:' + param.color + '">●</span> ' + 
                         param.seriesName + ': ' + param.value.toFixed(1) + '<br/>'
              }
            })
            return result
          }
        },
        grid: {
          left: 50,
          right: 50,
          top: 30,
          bottom: 50,
          backgroundColor: 'rgba(24, 144, 255, 0.05)'
        },
        xAxis: {
          type: 'category',
          data: timePoints,
          axisLine: {
            lineStyle: { color: '#e8e8e8' }
          },
          axisTick: { show: false },
          axisLabel: { show: false }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 800,
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { 
            color: '#999',
            fontSize: 10
          },
          splitLine: {
            lineStyle: { color: '#f0f0f0' }
          }
        },
        series: [
          {
            name: chartLegends.value[0].name,
            type: 'line',
            data: data1,
            lineStyle: {
              color: chartLegends.value[0].color,
              width: 2,
              shadowColor: chartLegends.value[0].color,
              shadowBlur: 6,
              shadowOffsetY: 2
            },
            symbol: 'none',
            smooth: true,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: chartLegends.value[0].color + '40' },
                  { offset: 1, color: chartLegends.value[0].color + '10' }
                ]
              }
            }
          },
          {
            name: chartLegends.value[1].name,
            type: 'line',
            data: data2,
            lineStyle: {
              color: chartLegends.value[1].color,
              width: 2,
              shadowColor: chartLegends.value[1].color,
              shadowBlur: 6,
              shadowOffsetY: 2
            },
            symbol: 'none',
            smooth: true,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: chartLegends.value[1].color + '40' },
                  { offset: 1, color: chartLegends.value[1].color + '10' }
                ]
              }
            }
          },
          {
            name: chartLegends.value[2].name,
            type: 'line',
            data: data3,
            lineStyle: {
              color: chartLegends.value[2].color,
              width: 2,
              shadowColor: chartLegends.value[2].color,
              shadowBlur: 6,
              shadowOffsetY: 2
            },
            symbol: 'none',
            smooth: true,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: chartLegends.value[2].color + '40' },
                  { offset: 1, color: chartLegends.value[2].color + '10' }
                ]
              }
            }
          }
        ]
      }
      
      chartInstance.setOption(option)
      
      // 窗口大小改变时重新调整图表
      window.addEventListener('resize', () => {
        chartInstance.resize()
      })
    }
  })
}

onMounted(() => {
  console.log('设备概览页面加载完成')
  // 确保页面加载时滚动到顶部
  window.scrollTo(0, 0)
  document.body.scrollTo(0, 0)
  initChart()
})
</script>

<style scoped>
.equipment-overview {
  padding: 16px;
  background: #f0f2f5;
  min-height: 100vh;
}

/* 顶部主标签 */
.top-tabs {
  display: flex;
  gap: 8px;
  margin: 0 20px 12px;
}
.main-tab {
  padding: 8px 14px;
  background: #fff;
  color: #666;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all .2s;
  font-size: 13px;
}
.main-tab:hover {
  color: #1677ff;
  border-color: #1677ff;
}
.main-tab.active {
  color: #1677ff;
  background: #e8f3ff;
  border-color: #91caff;
}

/* 左右两栏主布局 */
.main-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  padding: 0 20px;
}

/* 左侧树 */
.left-panel {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e5e6eb;
}
.search-section {
  margin-bottom: 12px;
}
.search-input {
  border-radius: 6px;
}
.device-tree .tree-group + .tree-group {
  margin-top: 12px;
}
.group-header {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.device-list {
  padding: 8px 0 0;
}
.device-item {
  padding: 8px 10px;
  margin: 6px 0;
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #eef0f3;
  transition: all .2s;
  font-size: 13px;
  color: #444;
}
.device-item:hover {
  background: #f6fbff;
  border-color: #91caff;
}
.device-item.selected {
  background: #e8f3ff;
  border-color: #69b1ff;
  color: #1677ff;
}

/* 右侧整体内容区域 */
.right-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 概览区域（设备概览+数据概览） */
.overview-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  background: transparent;
}

/* 设备概览 */
.device-overview {
  display: flex;
  flex-direction: column;
  background: #e8f3ff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.display-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.display-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.detail-link {
  font-size: 12px;
  color: #8c8c8c;
  cursor: pointer;
}
.detail-link:hover { color: #1677ff; }
.device-image-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}
.main-device-image {
  max-width: 100%;
  max-height: 220px;
  object-fit: contain;
}

/* 数据概览区域 */
.data-overview-section {
  display: flex;
  flex-direction: column;
  background: #e8f3ff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.section-header {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}
.current-device {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

/* 指标徽章 */
.status-indicators {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.indicator-card {
  border: 1px solid #eef0f3;
  border-radius: 8px;
  padding: 8px 10px;
  background: #fafcff;
  display: grid;
  grid-template-columns: 28px 1fr auto;
  align-items: center;
  column-gap: 6px;
}
.indicator-card .indicator-icon {
  width: 28px;height: 28px;border-radius: 50%;
  display: flex;align-items: center;justify-content: center;
  background: #e8f3ff;color: #1677ff;font-size: 14px;
}
.indicator-card .indicator-value {
  font-size: 18px;font-weight: 600;color: #1677ff;margin: 0;
}
.indicator-card .indicator-unit {
  font-size: 11px;color: #999; justify-self: end;
}
.indicator-card .indicator-label {
  grid-column: 2 / 4;
  font-size: 11px;color: #8c8c8c;margin-top: 3px;
}

/* 参数面板 */
.parameters-section {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  padding: 16px;
}

/* 参数模块容器 */
.param-modules {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 单个参数模块 */
.param-module {
  display: flex;
  flex-direction: column;
  background: #fafcff;
  border: 1px solid #e1e8f0;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.module-header {
  font-size: 14px;
  font-weight: 600;
  color: #1677ff;
  padding: 8px 12px;
  background: #e8f3ff;
  border: 1px solid #91caff;
  border-radius: 6px;
  text-align: center;
  margin-bottom: 12px;
}

.module-params {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 参数行容器 */
.param-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.param-item {
  padding: 6px 8px;
  border-radius: 6px;
  background: #fafafa;
  border: 1px solid #eef0f3;
  border-left: 3px solid #e8e8e8;
  min-height: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.param-item.blue {
  border-left-color: #69b1ff;
  background: #f6fbff;
}
.param-item.gray { border-left-color:#bfbfbf; }
.param-name { font-size: 12px;color:#666;margin-bottom:4px; }
.param-value { font-size:16px;font-weight:600;color:#333; }
.param-unit { font-size: 12px;color:#999; }

/* 底部图表 */
.bottom-charts {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  margin: 16px 20px 0;
  padding: 14px 16px 18px;
}
.charts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.charts-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.charts-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}
.date-display { font-size: 12px;color:#8c8c8c; }
.time-range-selector { width: 120px; }
.charts-legend {
  display: flex;
  gap: 24px;
  margin: 8px 0 12px;
  flex-wrap: wrap;
}
.legend-item { 
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 4px;
}
.legend-item:hover {
  background-color: #f0f7ff;
  transform: translateY(-1px);
}
.legend-item.disabled {
  opacity: 0.5;
}
.legend-item.disabled:hover {
  background-color: #f5f5f5;
}
.legend-dot { width:8px;height:8px;border-radius:50%; }
.legend-text { font-size:12px;color:#666; }
.chart-container { width: 100%; height: 290px; }
.main-chart { width: 100%; height: 100%; }

/* 响应式 */
@media (max-width: 1600px) {
  .main-layout { grid-template-columns: 260px 1fr 380px; }
}
@media (max-width: 1400px) {
  .main-layout { grid-template-columns: 1fr; }
  .right-panel { grid-template-rows: auto auto; }
  .param-grid { grid-template-columns: 1fr; }
}
</style>