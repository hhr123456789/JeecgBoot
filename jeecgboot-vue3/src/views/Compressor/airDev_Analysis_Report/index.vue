<template>
  <div class="analysis-report">
    <!-- 左侧导航菜单 -->
    <div class="left-sidebar">
      <div class="sidebar-title">月度分析报告</div>
      <div class="menu-section">
        <div class="menu-title">目录</div>
        <div 
          class="menu-item" 
          :class="{ active: activeSection === 'overview' }"
          @click="scrollToSection('overview')"
        >
          01.运行效率分析
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'health' }"
          @click="scrollToSection('health')"
        >
          02.设备健康状态评估
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'energy' }"
          @click="scrollToSection('energy')"
        >
          03.能耗结构分析
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'optimization' }"
          @click="scrollToSection('optimization')"
        >
          04.节能措施与优化建议
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'benefits' }"
          @click="scrollToSection('benefits')"
        >
          05.预期效益与实施路线图
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'risks' }"
          @click="scrollToSection('risks')"
        >
          06.风险与注意事项
        </div>
        <div 
          class="menu-item"
          :class="{ active: activeSection === 'conclusion' }"
          @click="scrollToSection('conclusion')"
        >
          07.总结
        </div>
      </div>
    </div>

    <!-- 右侧主要内容区 -->
    <div class="main-content">
      <!-- 顶部时间选择器 -->
      <div class="top-header">
        <div class="date-info">
          <span class="report-type">空压机分析</span>
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
          <h1 class="report-title">空压机运行状况分析报告</h1>
          <div class="report-meta">
            <div class="meta-item">
              <span class="meta-label">分析日期：</span>
              <span class="meta-value">2025年10月6日</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">设备类型：</span>
              <span class="meta-value">螺杆式空压机系统</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">监测周期：</span>
              <span class="meta-value">2025年9月1日-2025年9月30日</span>
            </div>
          </div>
        </div>

        <!-- 运行效率分析 -->
        <div class="section" id="overview">
          <div class="section-header">
            <div class="section-number">01</div>
            <h2 class="section-title">运行效率分析</h2>
          </div>
          
          <div class="subsection">
            <h3 class="subsection-title">1. 平均负载率</h3>
            <div class="analysis-content">
              <p class="analysis-text">
                模拟数据显示，空压机平均负载率为<strong class="highlight-value">68.3%</strong>，但<strong class="warning-text">低负载运行时间（负载率<50%）占比高达37%</strong>。
                在低负载时段（如夜间），单位能耗显著升高至<strong class="energy-value">0.115–0.125 kWh/Nm³</strong>，
                而高效负载区间（75%–85%）的单位能耗可降至<strong class="efficient-value">0.102–0.108 kWh/Nm³</strong>。
              </p>
            </div>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">2. 能效比（COP）</h3>
            <div class="analysis-content warning-block">
              <p class="analysis-text">
                当前COP为<strong class="highlight-value">3.2</strong>，低于行业基准值（3.5–4.0）。主要原因是
                <strong class="warning-text">排气压力设定偏高</strong>（模拟值为0.75–0.85 MPa）和
                <strong class="warning-text">冷却系统效率下降</strong>。
              </p>
            </div>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">3. 压力稳定性</h3>
            <div class="analysis-content info-block">
              <p class="analysis-text">
                出口压力波动范围为<strong class="normal-value">±0.15 bar</strong>，符合标准（≤±0.2 bar），
                但用气高峰时段（如早8–9点）波动加剧，反映控制策略需优化。
              </p>
            </div>
          </div>
        </div>

        <!-- 设备健康状态评估 -->
        <div class="section" id="health">
          <div class="section-header">
            <div class="section-number">02</div>
            <h2 class="section-title">设备健康状态评估</h2>
          </div>

          <div class="subsection">
            <h3 class="subsection-title">1. 关键部件状态</h3>
            
            <div class="health-items">
              <div class="health-item good">
                <h4 class="health-title">主机振动值：</h4>
                <p class="health-desc">
                  <strong class="normal-value">4.2 mm/s</strong>（低于警报阈值7.1 mm/s），转子系统运行平稳。
                </p>
              </div>
              
              <div class="health-item warning">
                <h4 class="health-title">排气温度：</h4>
                <p class="health-desc">
                  平均<strong class="highlight-value">78°C</strong>，但峰值时段可达<strong class="warning-text">95°C</strong>，
                  接近高温跳机临界点（110°C）。
                </p>
              </div>
              
              <div class="health-item normal">
                <h4 class="health-title">油气分离器压差：</h4>
                <p class="health-desc">
                  当前压差<strong class="normal-value">0.52 bar</strong>，未超限（最大0.8 bar），
                  但若持续上升需更换滤芯。
                </p>
              </div>
              
              <div class="health-item risk">
                <h4 class="health-title">潜在故障风险：</h4>
                <p class="health-desc">
                  电流谐波畸变率（THD）达<strong class="warning-text">8.7%</strong>（标准值<5%），
                  长期可能导致电机绝缘老化。
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 能耗结构分析 -->
        <div class="section" id="energy">
          <div class="section-header">
            <div class="section-number">03</div>
            <h2 class="section-title">能耗结构分析</h2>
          </div>

          <div class="subsection">
            <div class="energy-structure-table">
              <a-table 
                :dataSource="energyStructureData" 
                :columns="energyStructureColumns"
                :pagination="false"
                size="small"
                bordered
              />
            </div>
          </div>
        </div>

        <!-- 节能措施与优化建议 -->
        <div class="section" id="optimization">
          <div class="section-header">
            <div class="section-number">04</div>
            <h2 class="section-title">节能措施与优化建议</h2>
          </div>

          <div class="optimization-suggestions">
            <div class="suggestion-group short-term">
              <h4 class="suggestion-title">1. 短期措施（1–3个月可实现）</h4>
              
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">治理泄漏与优化压力设定：</h5>
                  <p class="item-desc">
                    修复管道泄漏点，预计可降低总能耗<strong class="benefit-value">5%–8%</strong>。
                  </p>
                  <p class="item-desc">
                    <strong class="action-text">降低排气压力</strong>：在满足生产前提下，将压力从0.75 MPa降至0.65 MPa，
                    每降低0.1 MPa可省电<strong class="benefit-value">6%–8%</strong>。
                  </p>
                </div>
                
                <div class="suggestion-item">
                  <h5 class="item-title">运行策略调整：</h5>
                  <p class="item-desc">
                    避免单台空压机低负载运行，启用多台联动控制，确保负载率维持在
                    <strong class="efficient-value">70%–85%高效区间</strong>。
                  </p>
                  <p class="item-desc">
                    非生产时段关闭非必要空压机，减少空载损耗。
                  </p>
                </div>
              </div>
            </div>

            <div class="suggestion-group medium-term">
              <h4 class="suggestion-title">2. 中期措施（3–12个月需投资）</h4>
              
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">设备升级与改造：</h5>
                  <p class="item-desc">
                    <strong class="action-text">变频改造</strong>：加装永磁变频器（如S350系统），
                    使电机频率在25–50 Hz自适应调节，预计节电<strong class="benefit-value">18%–35%</strong>。
                  </p>
                  <p class="item-desc">
                    <strong class="action-text">余热回收</strong>：安装热交换器，将90°C以上的油气热能用于加热工艺用水，
                    可回收<strong class="benefit-value">85%的废热</strong>。
                  </p>
                </div>
                
                <div class="suggestion-item">
                  <h5 class="item-title">智能控制系统：</h5>
                  <p class="item-desc">
                    引入集中控制系统（如FineBI平台），根据用气需求自动启停空压机，
                    减少阶梯式压力上升造成的浪费。
                  </p>
                </div>
              </div>
            </div>

            <div class="suggestion-group long-term">
              <h4 class="suggestion-title">3. 长期措施（1年以上系统性优化）</h4>
              
              <div class="suggestion-items">
                <div class="suggestion-item">
                  <h5 class="item-title">管网优化：</h5>
                  <p class="item-desc">
                    将部分铁质管道更换为铝管（摩擦系数更低），减少压降损失。
                  </p>
                </div>
                
                <div class="suggestion-item">
                  <h5 class="item-title">预测性维护：</h5>
                  <p class="item-desc">
                    建立数字孪生模型，监测轴承振动、油液颗粒等参数，提前预警故障。
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 预期效益与实施路线图 -->
        <div class="section" id="benefits">
          <div class="section-header">
            <div class="section-number">05</div>
            <h2 class="section-title">预期效益与实施路线图</h2>
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
                <li>年度电费降低<strong class="benefit-value">20%–30%</strong></li>
                <li>碳排放减少约<strong class="benefit-value">120吨</strong></li>
                <li>设备寿命延长<strong class="benefit-value">3–5年</strong></li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 风险与注意事项 -->
        <div class="section" id="risks">
          <div class="section-header">
            <div class="section-number">06</div>
            <h2 class="section-title">风险与注意事项</h2>
          </div>

          <div class="risk-analysis">
            <div class="risk-item">
              <h4 class="risk-title">变频改造风险：</h4>
              <p class="risk-desc">可能引发电网谐波干扰，需同步安装滤波装置。</p>
            </div>
            
            <div class="risk-item">
              <h4 class="risk-title">维护优先级：</h4>
              <p class="risk-desc">优先更换压差>0.6 bar的油分滤芯，避免爆裂风险。</p>
            </div>
          </div>
        </div>

        <!-- 总结 -->
        <div class="section" id="conclusion">
          <div class="section-header">
            <div class="section-number">07</div>
            <h2 class="section-title">总结</h2>
          </div>

          <div class="conclusion">
            <p class="conclusion-text">
              通过<strong class="action-text">治理泄漏、优化负载分配、变频改造</strong>三大核心策略，可显著提升空压机能效。
              建议立即启动短期措施，并规划中期改造项目。同时，采用FineBI等数据平台实现持续监控，
              将空压机系统纳入企业能源管理体系，实现长期降本增效。
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

// 能耗结构表格列定义
const energyStructureColumns: TableColumnType[] = [
  {
    title: '能耗来源',
    dataIndex: 'source',
    key: 'source',
    width: 200
  },
  {
    title: '占比',
    dataIndex: 'percentage',
    key: 'percentage',
    width: 100
  },
  {
    title: '问题描述',
    dataIndex: 'description',
    key: 'description'
  }
]

// 能耗结构数据
const energyStructureData = ref([
  {
    key: '1',
    source: '压缩机主电机',
    percentage: '68%',
    description: '低负载时效率骤降，卸载运行能耗占比高。'
  },
  {
    key: '2',
    source: '冷却系统',
    percentage: '15%',
    description: '冷却水泵定速运行，未随负载调整流量。'
  },
  {
    key: '3',
    source: '管道泄漏及压降损耗',
    percentage: '12%',
    description: '系统泄漏导致年损耗约相当于4000元/平方毫米漏点。'
  },
  {
    key: '4',
    source: '辅助系统',
    percentage: '5%',
    description: '控制柜待机功耗偏高。'
  }
])

// 实施计划表格列定义
const implementationColumns: TableColumnType[] = [
  {
    title: '阶段',
    dataIndex: 'stage',
    key: 'stage',
    width: 100
  },
  {
    title: '关键措施',
    dataIndex: 'measures',
    key: 'measures',
    width: 200
  },
  {
    title: '投资估算',
    dataIndex: 'investment',
    key: 'investment',
    width: 120
  },
  {
    title: '预期节能效果',
    dataIndex: 'effect',
    key: 'effect',
    width: 120
  },
  {
    title: '投资回收期',
    dataIndex: 'payback',
    key: 'payback',
    width: 120
  }
]

// 实施计划数据
const implementationData = ref([
  {
    key: '1',
    stage: '短期',
    measures: '泄漏治理、压力优化、运行调整',
    investment: '<5万元',
    effect: '5%–8%',
    payback: '6–12个月'
  },
  {
    key: '2',
    stage: '中期',
    measures: '变频改造、余热回收、智能控制',
    investment: '10–20万元',
    effect: '15%–25%',
    payback: '1.5–2年'
  },
  {
    key: '3',
    stage: '长期',
    measures: '管网改造、预测性维护平台',
    investment: '>30万元',
    effect: '8%–12%',
    payback: '2–3年'
  }
])

// 下载报告
const downloadReport = () => {
  console.log('下载空压机PDF报告')
  // 这里可以实现PDF下载功能
}
</script>

<style scoped>
.analysis-report {
  min-height: 100vh;
  background: #f0f2f5;
  position: relative;
  margin-top: 15px;
}

/* 左侧导航 */
.left-sidebar {
  position: fixed;
  left: 200px;
  top: 113px;
  bottom: 0;
  width: 220px;
  background: #f8f9fa;
  color: #333;
  padding: 20px 0;
  border-right: 1px solid #e8e9ea;
  box-shadow: 2px 0 4px rgba(0,0,0,0.08);
  z-index: 100;
  overflow-y: auto;
  margin-top: 15px;
}

.sidebar-title {
  color: #262626;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  padding: 0 16px 16px;
  border-bottom: 1px solid #e8e9ea;
}

.menu-section {
  padding: 16px 0;
}

.menu-title {
  color: #8c8c8c;
  font-size: 16px;
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
  border-bottom: 1px solid #e8e9ea;
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
  background: #fa8c16;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
}

.date-selector {
  width: 180px;
}

/* 报告内容 */
.report-content {
  max-width: 1900px;
  margin: 0 auto;
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
  background: #fa8c16;
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
  border-left: 4px solid #fa8c16;
}

/* 分析内容 */
.analysis-content {
  background: #fafafa;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #52c41a;
  line-height: 1.8;
}

.analysis-content.warning-block {
  background: #fff7e6;
  border-left-color: #fa8c16;
}

.analysis-content.info-block {
  background: #e6f7ff;
  border-left-color: #1890ff;
}

.analysis-text {
  color: #595959;
  margin: 0;
  font-size: 15px;
}

/* 高亮数值样式 */
.highlight-value {
  color: #1890ff;
  font-weight: 700;
  font-size: 16px;
}

.warning-text {
  color: #fa8c16;
  font-weight: 700;
}

.energy-value {
  color: #ff4d4f;
  font-weight: 700;
}

.efficient-value {
  color: #52c41a;
  font-weight: 700;
}

.normal-value {
  color: #1890ff;
  font-weight: 600;
}

.benefit-value {
  color: #52c41a;
  font-weight: 700;
  font-size: 16px;
}

.action-text {
  color: #fa8c16;
  font-weight: 700;
}

/* 健康状态项目 */
.health-items {
  display: grid;
  gap: 20px;
}

.health-item {
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #52c41a;
}

.health-item.good {
  background: #f6ffed;
  border-left-color: #52c41a;
}

.health-item.warning {
  background: #fff7e6;
  border-left-color: #fa8c16;
}

.health-item.normal {
  background: #e6f7ff;
  border-left-color: #1890ff;
}

.health-item.risk {
  background: #fff2f0;
  border-left-color: #ff4d4f;
}

.health-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.health-desc {
  color: #595959;
  line-height: 1.6;
  margin: 0;
}

/* 能耗结构表格 */
.energy-structure-table {
  background: #fafafa;
  padding: 24px;
  border-radius: 8px;
  border: 2px solid #e8e9ea;
}

/* 优化建议 */
.optimization-suggestions {
  display: grid;
  gap: 24px;
}

.suggestion-group {
  padding: 24px;
  border-radius: 8px;
  border: 2px solid #e8e9ea;
}

.suggestion-group.short-term {
  background: #f6ffed;
  border-left: 4px solid #52c41a;
}

.suggestion-group.medium-term {
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
}

.suggestion-group.long-term {
  background: #f9f0ff;
  border-left: 4px solid #722ed1;
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
  border: 1px solid #e8e9ea;
  transition: all 0.3s ease;
}

.suggestion-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #fa8c16;
  margin-bottom: 8px;
}

.item-desc {
  color: #595959;
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.item-desc:last-child {
  margin-bottom: 0;
}

/* 实施计划 */
.implementation-plan {
  background: #fafafa;
  padding: 24px;
  border-radius: 8px;
  border: 2px solid #e8e9ea;
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

/* 风险分析 */
.risk-analysis {
  display: grid;
  gap: 20px;
}

.risk-item {
  background: #fff2f0;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #ff4d4f;
}

.risk-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.risk-desc {
  color: #595959;
  line-height: 1.6;
  margin: 0;
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
  border-top: 1px solid #e8e9ea;
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
  border-bottom: 2px solid #e8e9ea;
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
    border-bottom: 1px solid #e8e9ea;
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