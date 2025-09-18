<template>
  <div class="analysis-report">
    <!-- 左侧导航菜单 -->
    <div class="left-sidebar">
      <div class="sidebar-title">月度报告</div>
      <div class="menu-section">
        <div class="menu-title">首页</div>
        <div 
          class="menu-item" 
          :class="{ active: activeSection === 'overview' }"
          @click="scrollToSection('overview')"
        >
          系统概况
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'analysis' }"
          @click="scrollToSection('analysis')"
        >
          运行数据分析
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'diagnosis' }"
          @click="scrollToSection('diagnosis')"
        >
          问题诊断与根本原因
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'suggestions' }"
          @click="scrollToSection('suggestions')"
        >
          优化建议与改进方案
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'implementation' }"
          @click="scrollToSection('implementation')"
        >
          实施计划与效益预测
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'conclusion' }"
          @click="scrollToSection('conclusion')"
        >
          结论
        </div>
      </div>
    </div>

    <!-- 右侧主要内容区 -->
    <div class="main-content">
      <!-- 顶部时间选择器 -->
      <div class="top-header">
        <div class="date-info">
          <span class="report-type">月报</span>
          <a-date-picker 
            v-model:value="selectedDate" 
            picker="month" 
            format="YYYY年MM月"
            placeholder="选择月份"
            class="date-selector"
          />
        </div>
      </div>

      <!-- 报告内容区 -->
      <div class="report-content">
        <!-- 报告标题 -->
        <div class="report-header">
          <h1 class="report-title">中央空调运行分析报告</h1>
          <div class="report-meta">
            <div class="meta-item">
              <span class="meta-label">日期：</span>
              <span class="meta-value">2025年9月15日</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">系统类型：</span>
              <span class="meta-value">变频多联机中央空调</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">监测周期：</span>
              <span class="meta-value">2025年6月1日-2025年9月10日</span>
            </div>
          </div>
        </div>

        <!-- 系统概况 -->
        <div class="section" id="overview">
          <div class="section-header">
            <div class="section-number">01</div>
            <h2 class="section-title">系统概况</h2>
          </div>
          
          <div class="subsection">
            <h3 class="subsection-title">1. 设备配置</h3>
            <div class="equipment-list">
              <div class="equipment-item">
                <span class="equipment-type">冷热源：</span>
                <span class="equipment-desc">变频螺杆式冷水机组（额定制冷量：1000RT）</span>
              </div>
              <div class="equipment-item">
                <span class="equipment-type">水系统：</span>
                <span class="equipment-desc">冷冻水泵（变频）×4台，冷却塔×2台</span>
              </div>
              <div class="equipment-item">
                <span class="equipment-type">末端设备：</span>
                <span class="equipment-desc">风机盘管（FCU）120台，新风机组（AHU）8台</span>
              </div>
              <div class="equipment-item">
                <span class="equipment-type">控制系统：</span>
                <span class="equipment-desc">智能云平台实时监测（温度、压力、流量、能耗等参数）</span>
              </div>
            </div>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">2. 监测点布局</h3>
            <div class="monitoring-points">
              <div class="monitoring-item">
                <span class="monitoring-type">冷源侧：</span>
                <span class="monitoring-desc">压缩机吸排气压力/温度、冷凝/蒸发温度、电流</span>
              </div>
              <div class="monitoring-item">
                <span class="monitoring-type">水系统：</span>
                <span class="monitoring-desc">冷冻水进出水温差、流量、压差；冷却塔进出水温差</span>
              </div>
              <div class="monitoring-item">
                <span class="monitoring-type">空气侧：</span>
                <span class="monitoring-desc">送/回风温湿度、滤网压差、室内CO₂浓度</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 运行数据分析 -->
        <div class="section" id="analysis">
          <div class="section-header">
            <div class="section-number">02</div>
            <h2 class="section-title">运行数据分析</h2>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">1. 冷热源设备性能</h3>
            
            <!-- 性能数据表格 -->
            <div class="performance-table">
              <a-table 
                :dataSource="performanceData" 
                :columns="performanceColumns"
                :pagination="false"
                size="small"
                bordered
              />
            </div>
            
            <div class="problem-diagnosis">
              <h4 class="diagnosis-title">问题诊断：</h4>
              <ul class="diagnosis-list">
                <li>冷凝温度持续偏高（+3℃），冷却塔风机转速未随负荷调整，导致散热效率下降</li>
                <li>COP低于设计值，部分负荷下压缩机频繁启停，增加能耗</li>
              </ul>
            </div>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">2. 水系统运行状态</h3>
            
            <div class="water-system-analysis">
              <div class="analysis-group">
                <h4 class="analysis-subtitle">冷冻水系统：</h4>
                <ul class="analysis-points">
                  <li>进出水温差：2.5℃（设计值：3–5℃） → <strong>流量过大</strong>，水泵未按需变频调节</li>
                  <li>水泵平均电流：45A（额定值：50A），但夜间低负荷时仍全速运行，浪费电能</li>
                </ul>
              </div>
              
              <div class="analysis-group">
                <h4 class="analysis-subtitle">冷却水系统：</h4>
                <ul class="analysis-points">
                  <li>冷却塔进出水温差：2℃（设计值：3–5℃） → <strong>布水不均或填料脏堵</strong>，散热效率低</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">3. 空气处理设备问题</h3>
            
            <div class="air-handling-analysis">
              <div class="analysis-group">
                <h4 class="analysis-subtitle">AHU机组：</h4>
                <ul class="analysis-points">
                  <li>滤网压差：80Pa（正常≤50Pa） → <strong>滤网堵塞</strong>，风量下降15%，影响送风效率</li>
                  <li>室内CO₂浓度：1200ppm（标准≤1000ppm） → <strong>新风量不足</strong>，通风策略需调整</li>
                </ul>
              </div>
              
              <div class="analysis-group">
                <h4 class="analysis-subtitle">末端FCU：</h4>
                <ul class="analysis-points">
                  <li>10%设备阀门开度长期＞90%，表明水力失衡，部分区域过冷/过热</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 问题诊断与根本原因 -->
        <div class="section" id="diagnosis">
          <div class="section-header">
            <div class="section-number">03</div>
            <h2 class="section-title">问题诊断与根本原因</h2>
          </div>

          <div class="root-cause-analysis">
            <div class="cause-item">
              <h4 class="cause-title">能效低下（COP 2.8）：</h4>
              <p class="cause-desc">冷却塔散热不足 + 水泵全速运行 → <strong>无效能耗占比25%</strong></p>
            </div>
            
            <div class="cause-item">
              <h4 class="cause-title">温湿度控制波动：</h4>
              <p class="cause-desc">传感器校准偏差（湿度传感器误差±8%），导致加湿/除湿动作滞后</p>
            </div>
            
            <div class="cause-item">
              <h4 class="cause-title">水力失衡：</h4>
              <p class="cause-desc">分支管路缺乏动态平衡阀，高楼层流量不足，低楼层过流量</p>
            </div>
          </div>
        </div>

        <!-- 优化建议与改进方案 -->
        <div class="section" id="suggestions">
          <div class="section-header">
            <div class="section-number">04</div>
            <h2 class="section-title">优化建议与改进方案</h2>
          </div>

          <div class="optimization-suggestions">
            <div class="suggestion-group">
              <h4 class="suggestion-title">1. 冷热源系统优化</h4>
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">冷却塔升级：</h5>
                  <p class="item-desc">清洗填料并加装变频风机，根据湿球温度自动调节转速，目标冷凝温度≤45℃</p>
                </div>
                <div class="suggestion-item">
                  <h5 class="item-title">负荷匹配策略：</h5>
                  <p class="item-desc">启用群控系统，按区域需求动态启停压缩机，预计COP提升至3.0+</p>
                </div>
              </div>
            </div>

            <div class="suggestion-group">
              <h4 class="suggestion-title">2. 水系统节能改造</h4>
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">水泵变频优化：</h5>
                  <p class="item-desc">设定冷冻水最小温差3℃，自动下调水泵频率，预计节电20%</p>
                </div>
                <div class="suggestion-item">
                  <h5 class="item-title">水力平衡调整：</h5>
                  <p class="item-desc">安装动态平衡阀，优先解决高楼层流量不足问题</p>
                </div>
              </div>
            </div>

            <div class="suggestion-group">
              <h4 class="suggestion-title">3. 空气侧与维护提升</h4>
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">智能运维：</h5>
                  <p class="item-desc">更换高精度温湿度传感器（误差±3%），联动新风阀开度，维持CO₂≤800ppm</p>
                  <p class="item-desc">滤网压差＞50Pa时自动报警，缩短清洗周期至每月1次</p>
                </div>
                <div class="suggestion-item">
                  <h5 class="item-title">预测性维护：</h5>
                  <p class="item-desc">基于历史数据训练故障预测模型（如压缩机液击预警），减少突发停机</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 实施计划与效益预测 -->
        <div class="section" id="implementation">
          <div class="section-header">
            <div class="section-number">05</div>
            <h2 class="section-title">实施计划与效益预测</h2>
          </div>

          <div class="implementation-plan">
            <a-table 
              :dataSource="implementationData" 
              :columns="implementationColumns"
              :pagination="false"
              size="small"
              bordered
            />
            
            <div class="expected-benefits">
              <h4 class="benefits-title">总预期效益：</h4>
              <ul class="benefits-list">
                <li><strong>能耗降低</strong>：年节电23万kWh（约18万元），COP提升至3.1</li>
                <li><strong>舒适度提升</strong>：温度波动±0.5℃，湿度控制±5%RH，用户满意度达90%+</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 结论 -->
        <div class="section" id="conclusion">
          <div class="section-header">
            <div class="section-number">06</div>
            <h2 class="section-title">结论</h2>
          </div>

          <div class="conclusion">
            <p class="conclusion-text">
              当前中央空调系统主要问题集中于<strong>能效偏低</strong>（冷却塔与水泵运行策略不当）、<strong>水力失衡</strong>及<strong>传感器精度不足</strong>。通过针对性改造（变频优化、智能控制、维护升级），可实现能耗降低15%以上，同时提升环境舒适度与系统可靠性。建议优先实施冷却塔和水泵改造，并于1个月内完成传感器校准与水力调试。
            </p>
          </div>
        </div>
      </div>

      <!-- 底部工具栏 -->
      <div class="bottom-toolbar">
        <a-button type="primary" @click="downloadReport">
          下载PDF
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import type { TableColumnType } from 'ant-design-vue'
import dayjs from 'dayjs'

// 当前选择的日期
const selectedDate = ref(dayjs('2025-09'))

// 当前激活的章节
const activeSection = ref('overview')

// 跳转到指定章节
const scrollToSection = (sectionId: string) => {
  activeSection.value = sectionId
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 性能数据表格列定义
const performanceColumns: TableColumnType[] = [
  {
    title: '参数',
    dataIndex: 'parameter',
    key: 'parameter',
    width: 150
  },
  {
    title: '实测值',
    dataIndex: 'actualValue',
    key: 'actualValue',
    width: 100
  },
  {
    title: '设计值',
    dataIndex: 'designValue',
    key: 'designValue',
    width: 100
  },
  {
    title: '偏差分析',
    dataIndex: 'deviationAnalysis',
    key: 'deviationAnalysis'
  }
]

// 性能数据
const performanceData = ref([
  {
    key: '1',
    parameter: '压缩机排气温度',
    actualValue: '92℃',
    designValue: '≤90℃',
    deviationAnalysis: '偏高，可能因冷凝器散热不良'
  },
  {
    key: '2',
    parameter: '蒸发温度',
    actualValue: '4℃',
    designValue: '3–5℃',
    deviationAnalysis: '正常范围'
  },
  {
    key: '3',
    parameter: '冷凝温度',
    actualValue: '48℃',
    designValue: '≤45℃',
    deviationAnalysis: '偏高，冷却塔效率不足'
  },
  {
    key: '4',
    parameter: '系统COP',
    actualValue: '2.8',
    designValue: '3.2',
    deviationAnalysis: '低能效，需优化负荷匹配'
  }
])

// 实施计划表格列定义
const implementationColumns: TableColumnType[] = [
  {
    title: '措施',
    dataIndex: 'measure',
    key: 'measure',
    width: 150
  },
  {
    title: '周期',
    dataIndex: 'period',
    key: 'period',
    width: 80
  },
  {
    title: '预计成本',
    dataIndex: 'cost',
    key: 'cost',
    width: 100
  },
  {
    title: '年效益',
    dataIndex: 'benefit',
    key: 'benefit'
  }
]

// 实施计划数据
const implementationData = ref([
  {
    key: '1',
    measure: '冷却塔变频改造',
    period: '2周',
    cost: '8万元',
    benefit: '节电15万kWh，省电费12万'
  },
  {
    key: '2',
    measure: '水泵智能变频策略',
    period: '1周',
    cost: '软件升级',
    benefit: '节电8万kWh'
  },
  {
    key: '3',
    measure: '传感器更换与校准',
    period: '3天',
    cost: '2万元',
    benefit: '提升温湿度控制精度'
  },
  {
    key: '4',
    measure: '水力平衡调试',
    period: '1周',
    cost: '3万元',
    benefit: '消除区域温差问题'
  }
])

// 下载报告
const downloadReport = () => {
  console.log('下载PDF报告')
  // 这里可以实现PDF下载功能
}
</script>

<style scoped>
.analysis-report {
  min-height: 100vh;
  background: #f0f2f5;
  position: relative;
}

/* 左侧导航 */
.left-sidebar {
  position: fixed;
  left: 200px;
  top: 0;
  bottom: 0;
  width: 220px;
  background: #f8f9fa;
  color: #333;
  padding: 20px 0;
  border-right: 1px solid #e8e8e8;
  box-shadow: 2px 0 4px rgba(0,0,0,0.08);
  z-index: 100;
  overflow-y: auto;
}

.sidebar-title {
  color: #262626;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  padding: 0 16px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.menu-section {
  padding: 16px 0;
}

.menu-title {
  color: #8c8c8c;
  font-size: 12px;
  padding: 0 16px 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  color: #262626;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.5;
}

.menu-item:hover {
  background: #1890ff;
  color: white;
  border-left-color: #91caff;
  font-weight: 700;
  transform: translateX(2px);
}

.menu-item.active {
  background: #1890ff;
  color: white;
  border-left-color: #096dd9;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

/* 主要内容区 */
.main-content {
  margin-left: 220px;
  min-height: 100vh;
}

.top-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.report-type {
  background: #1890ff;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.date-selector {
  width: 180px;
}

/* 报告内容 */
.report-content {
  max-width: 1400px;
  margin: 0 auto ！important;
  padding: 24px;
  background: white;
  margin-top: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 报告标题 */
.report-header {
  text-align: center;
  margin-bottom: 40px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 30px;
}

.report-title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 20px;
}

.report-meta {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #8c8c8c;
  margin-right: 8px;
}

.meta-value {
  color: #262626;
  font-weight: 500;
}

/* 章节样式 */
.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.section-number {
  background: #1890ff;
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 12px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

/* 子章节 */
.subsection {
  margin-bottom: 32px;
}

.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #1890ff;
}

/* 设备列表 */
.equipment-list {
  background: #fafafa;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #52c41a;
}

.equipment-item {
  margin-bottom: 12px;
  line-height: 1.6;
}

.equipment-item:last-child {
  margin-bottom: 0;
}

.equipment-type {
  font-weight: 600;
  color: #262626;
  margin-right: 8px;
}

.equipment-desc {
  color: #595959;
}

/* 监测点 */
.monitoring-points {
  background: #fafafa;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #fa8c16;
}

.monitoring-item {
  margin-bottom: 12px;
  line-height: 1.6;
}

.monitoring-item:last-child {
  margin-bottom: 0;
}

.monitoring-type {
  font-weight: 600;
  color: #262626;
  margin-right: 8px;
}

.monitoring-desc {
  color: #595959;
}

/* 性能表格 */
.performance-table {
  margin-bottom: 24px;
}

/* 问题诊断 */
.problem-diagnosis {
  background: #fff2e8;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #fa8c16;
}

.diagnosis-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.diagnosis-list {
  margin: 0;
  padding-left: 20px;
  color: #595959;
  line-height: 1.6;
}

.diagnosis-list li {
  margin-bottom: 8px;
}

/* 分析组 */
.water-system-analysis,
.air-handling-analysis {
  display: grid;
  gap: 20px;
}

.analysis-group {
  background: #f6ffed;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #52c41a;
}

.analysis-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.analysis-points {
  margin: 0;
  padding-left: 20px;
  color: #595959;
  line-height: 1.6;
}

.analysis-points li {
  margin-bottom: 8px;
}

/* 根本原因分析 */
.root-cause-analysis {
  display: grid;
  gap: 20px;
}

.cause-item {
  background: #fff1f0;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #ff4d4f;
}

.cause-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.cause-desc {
  color: #595959;
  line-height: 1.6;
  margin: 0;
}

/* 优化建议 */
.optimization-suggestions {
  display: grid;
  gap: 24px;
}

.suggestion-group {
  background: #f0f9ff;
  padding: 24px;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.suggestion-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
}

.suggestion-items {
  display: grid;
  gap: 16px;
}

.suggestion-item {
  background: white;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e6f7ff;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 8px;
}

.item-desc {
  color: #595959;
  line-height: 1.6;
  margin: 0;
  margin-bottom: 8px;
}

.item-desc:last-child {
  margin-bottom: 0;
}

/* 实施计划 */
.implementation-plan {
  background: #fafafa;
  padding: 24px;
  border-radius: 8px;
}

.expected-benefits {
  margin-top: 24px;
  background: #f6ffed;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #52c41a;
}

.benefits-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.benefits-list {
  margin: 0;
  padding-left: 20px;
  color: #595959;
  line-height: 1.6;
}

.benefits-list li {
  margin-bottom: 8px;
}

/* 结论部分 */
.conclusion {
  background: #f6ffed;
  padding: 24px;
  border-radius: 8px;
  border-left: 4px solid #52c41a;
}

.conclusion-text {
  color: #595959;
  line-height: 1.8;
  margin: 0;
  font-size: 15px;
}

/* 底部工具栏 */
.bottom-toolbar {
  background: white;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: center;
}

/* 数据表格样式优化 */
:deep(.ant-table) {
  border-radius: 6px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  border-bottom: 2px solid #e8e8e8;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
  vertical-align: top;
  padding: 12px 16px;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .report-content {
    margin: 10px;
    padding: 16px;
  }
  
  .report-meta {
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .left-sidebar {
    position: static;
    left: auto;
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .report-meta {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
