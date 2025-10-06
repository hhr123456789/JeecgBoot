// 数据模拟
const deviceRows = [
  { id: 1, type: '干式变压器', model: 'SCB10-315/10', kva: 315, kv: '10/0.4', noLoadA: 4, noLoadLoss: 670, loadLoss: 3650, cool: 'AN' },
  { id: 2, type: '干式变压器', model: 'SCB11-500/10', kva: 500, kv: '10/0.4', noLoadA: 4, noLoadLoss: 920, loadLoss: 5150, cool: 'AN' },
  { id: 3, type: '干式变压器', model: 'SCB11-630/10', kva: 630, kv: '10/0.4', noLoadA: 4, noLoadLoss: 980, loadLoss: 5950, cool: 'AN' },
  { id: 4, type: '干式变压器', model: 'SCB11-800/10', kva: 800, kv: '10/0.4', noLoadA: 6, noLoadLoss: 1200, loadLoss: 7500, cool: 'AN' },
  { id: 5, type: '油浸式变压器', model: 'S11-1000/10', kva: 1000, kv: '10/0.4', noLoadA: 5, noLoadLoss: 1450, loadLoss: 10300, cool: 'ONAN' },
  { id: 6, type: '油浸式变压器', model: 'S13-1250/10', kva: 1250, kv: '10/0.4', noLoadA: 6, noLoadLoss: 1600, loadLoss: 12000, cool: 'ONAF' },
  { id: 7, type: '干式变压器', model: 'SCB11-1600/10', kva: 1600, kv: '10/0.4', noLoadA: 6, noLoadLoss: 1950, loadLoss: 14500, cool: 'AN' },
  { id: 8, type: '干式变压器', model: 'SCB10-2000/10', kva: 2000, kv: '10/0.4', noLoadA: 6, noLoadLoss: 2400, loadLoss: 17000, cool: 'AF' },
  { id: 9, type: '干式变压器', model: 'SCB11-400/10', kva: 400, kv: '10/0.4', noLoadA: 4, noLoadLoss: 850, loadLoss: 4800, cool: 'AN' },
  { id: 10, type: '油浸式变压器', model: 'S11-800/10', kva: 800, kv: '10/0.4', noLoadA: 5, noLoadLoss: 1200, loadLoss: 7500, cool: 'ONAN' },
  { id: 11, type: '干式变压器', model: 'SCB11-1000/10', kva: 1000, kv: '10/0.4', noLoadA: 5, noLoadLoss: 1400, loadLoss: 10000, cool: 'AN' },
  { id: 12, type: '油浸式变压器', model: 'S13-1600/10', kva: 1600, kv: '10/0.4', noLoadA: 6, noLoadLoss: 1800, loadLoss: 14000, cool: 'ONAF' },
  { id: 13, type: '干式变压器', model: 'SCB11-2500/10', kva: 2500, kv: '10/0.4', noLoadA: 7, noLoadLoss: 2800, loadLoss: 20000, cool: 'AF' },
  { id: 14, type: '油浸式变压器', model: 'S11-3150/10', kva: 3150, kv: '10/0.4', noLoadA: 7, noLoadLoss: 3200, loadLoss: 25000, cool: 'ONAF' },
  { id: 15, type: '干式变压器', model: 'SCB11-4000/10', kva: 4000, kv: '10/0.4', noLoadA: 8, noLoadLoss: 3800, loadLoss: 30000, cool: 'AF' },
  { id: 16, type: '油浸式变压器', model: 'S13-5000/10', kva: 5000, kv: '10/0.4', noLoadA: 8, noLoadLoss: 4500, loadLoss: 35000, cool: 'ONAF' },
  { id: 17, type: '干式变压器', model: 'SCB11-6300/10', kva: 6300, kv: '10/0.4', noLoadA: 9, noLoadLoss: 5200, loadLoss: 42000, cool: 'AF' },
  { id: 18, type: '油浸式变压器', model: 'S11-8000/10', kva: 8000, kv: '10/0.4', noLoadA: 9, noLoadLoss: 6200, loadLoss: 50000, cool: 'ONAF' },
];

// 渲染表格 + 分页
const page = { index: 1, size: 10 };
const tbodyEl = document.getElementById('device-tbody');
const pageInfoEl = document.getElementById('page-info');

function renderTable() {
  const start = (page.index - 1) * page.size;
  const end = start + page.size;
  const list = deviceRows.slice(start, end);
  tbodyEl.innerHTML = list.map(r => `
    <tr>
      <td>${r.id}</td>
      <td>${r.type}</td>
      <td>${r.model}</td>
      <td>${r.kva}</td>
      <td>${r.kv}</td>
      <td>${r.noLoadA}</td>
      <td>${r.noLoadLoss}</td>
      <td>${r.loadLoss}</td>
      <td>${r.cool}</td>
    </tr>
  `).join('');
  const total = Math.max(1, Math.ceil(deviceRows.length / page.size));
  pageInfoEl.textContent = `${page.index} / ${total}`;
}

document.getElementById('prev-page').addEventListener('click', () => {
  if (page.index > 1) { page.index--; renderTable(); }
});
document.getElementById('next-page').addEventListener('click', () => {
  const total = Math.max(1, Math.ceil(deviceRows.length / page.size));
  if (page.index < total) { page.index++; renderTable(); }
});

renderTable();

// 饼图
const pie = echarts.init(document.getElementById('pieChart'));
const typeGroup = deviceRows.reduce((acc, r) => {
  let key;
  if (r.kva < 500) key = '<500 kVA';
  else if (r.kva < 1000) key = '500-1000 kVA';
  else if (r.kva < 1500) key = '1000-1500 kVA';
  else key = '≥1500 kVA';
  acc[key] = (acc[key] || 0) + 1; return acc;
}, {});

// 计算总设备数
const totalDevices = deviceRows.length;
document.getElementById('dry-count').textContent = `${deviceRows.filter(r => r.type.includes('干式')).length} 台`;
document.getElementById('oil-count').textContent = `${deviceRows.filter(r => r.type.includes('油浸')).length} 台`;

// 计算企业总容量
const totalCapacity = deviceRows.reduce((sum, r) => sum + r.kva, 0);
document.getElementById('total-capacity').textContent = `${totalCapacity} kVA`;

pie.setOption({
  backgroundColor: 'transparent',
  color: ['#69a6ff', '#1ac6ff', '#7c6dff', '#2fc59e', '#ffa940'],
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255,255,255,.95)',
    borderColor: '#e6ecf5',
    textStyle: { color: '#2b3a55' },
    formatter: '{b}: {c}台 ({d}%)'
  },
  legend: { 
    bottom: 0, 
    textStyle: { color: '#6b778c' },
    formatter: function(name) {
      const value = typeGroup[name] || 0;
      const percentage = ((value / totalDevices) * 100).toFixed(1);
      return `${name} (${value}台, ${percentage}%)`;
    }
  },
  series: [{
    type: 'pie', 
    radius: ['45%', '70%'],
    center: ['50%', '40%'],
    itemStyle: { borderRadius: 6, borderColor: '#ffffff', borderWidth: 2 },
    label: { 
      show: false
    },
    labelLine: {
      show: false
    },
    data: Object.entries(typeGroup).map(([name, value]) => ({ name, value })),
  }]
});

// 堆叠柱 + 折线（参考效果优化）
const barLine = echarts.init(document.getElementById('barLineChart'));
const workshops = ['厂房一', '厂房二', '厂房三', '厂房四', '厂房五', '厂房六', '厂房七'];
const seriesDefs = [
  { name: '500kVA', color: '#69a6ff' },
  { name: '800kVA', color: '#1ac6ff' },
  { name: '1000kVA', color: '#7c6dff' },
  { name: '1250kVA', color: '#2fc59e' },
  { name: '1600kVA', color: '#ffa940' },
];
function randomData(n, base = 0, max = 3) { return Array.from({ length: n }, () => Math.floor(Math.random() * (max + 1)) + base); }

const stackedSeries = seriesDefs.map(def => ({
  name: def.name,
  type: 'bar',
  stack: 'kva',
  barWidth: 34,
  emphasis: { focus: 'series' },
  itemStyle: { color: def.color, borderRadius: [4,4,0,0] },
  label: { show: false, position: 'insideTop', color: '#4b5b77', formatter: v => (v.value ? v.value + '台' : '') },
  data: randomData(workshops.length, 0, 3),
}));

barLine.setOption({
  backgroundColor: 'transparent',
  color: seriesDefs.map(s => s.color),
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    backgroundColor: 'rgba(255,255,255,.95)',
    borderColor: '#e6ecf5',
    textStyle: { color: '#2b3a55' },
    formatter: params => {
      const items = params.filter(p => p.seriesType === 'bar');
      const total = items.reduce((s, p) => s + p.value * parseInt(p.seriesName), 0);
      const count = items.reduce((s, p) => s + (p.value || 0), 0);
      const lines = [`<div style="margin-bottom:6px;font-weight:600">${params[0].axisValueLabel}</div>`];
      items.forEach(p => { if (p.value) lines.push(`${p.marker}${p.seriesName}: <b>${p.value} 台</b>`); });
      lines.push(`<hr style="border:none;border-top:1px solid #eef2f7;margin:6px 0">`);
      lines.push(`设备合计: <b>${count} 台</b>`);
      lines.push(`总容量: <b>${total} kVA</b>`);
      return lines.join('<br>');
    }
  },
  legend: { textStyle: { color: '#6b778c' } },
  grid: { left: 48, right: 24, bottom: 40, top: 54 },
  xAxis: {
    type: 'category', data: workshops,
    axisLine: { lineStyle: { color: '#dfe6f0' } },
    axisTick: { show: false },
    axisLabel: { color: '#6b778c' }
  },
  yAxis: [{
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#eef2f7' } },
    axisLabel: { color: '#6b778c' }
  }],
  series: [
    ...stackedSeries
  ]
});

// 自适应
window.addEventListener('resize', () => { pie.resize(); barLine.resize(); });


