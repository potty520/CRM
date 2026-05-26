<template>
  <div class="bi-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="title">CRM数据大屏</div>
      <div class="datetime">{{ currentDateTime }}</div>
    </div>

    <!-- 第一行统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon blue"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-label">客户总数</div>
            <el-statistic :value="stats.totalCustomers" :precision="0" />
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon green"><el-icon><Money /></el-icon></div>
          <div class="stat-content">
            <div class="stat-label">合同总额</div>
            <el-statistic :value="stats.totalContractAmount" :precision="0" prefix="¥" />
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orange"><el-icon><Sell /></el-icon></div>
          <div class="stat-content">
            <div class="stat-label">本月成交</div>
            <el-statistic :value="stats.monthDeals" :precision="0" prefix="¥" />
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon purple"><el-icon><Briefcase /></el-icon></div>
          <div class="stat-content">
            <div class="stat-label">商机数量</div>
            <el-statistic :value="stats.businessCount" :precision="0" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行：漏斗图 + 客户来源饼图 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">销售漏斗</div>
          <div ref="funnelRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">客户来源分布</div>
          <div ref="customerSourceRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 第三行：月度趋势 + 团队业绩排行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">月度趋势（近6个月）</div>
          <div ref="trendRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">团队业绩排行</div>
          <div ref="teamRankingRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 第四行：商机转化率 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">商机转化率</div>
          <div ref="winRateRef" class="chart gauge-chart"></div>
        </div>
      </el-col>
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-title">客户等级分布</div>
          <div ref="customerLevelRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { User, Money, Sell, Briefcase } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 统计数据
const stats = reactive({
  totalCustomers: 0,
  totalContractAmount: 0,
  monthDeals: 0,
  businessCount: 0
})

// 图表 ref
const funnelRef = ref()
const customerSourceRef = ref()
const trendRef = ref()
const teamRankingRef = ref()
const winRateRef = ref()
const customerLevelRef = ref()

// 当前日期时间
const currentDateTime = ref('')
let timer = null

const updateDateTime = () => {
  const now = new Date()
  currentDateTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 初始化图表
const initFunnelChart = (data) => {
  const chart = echarts.init(funnelRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272'],
    series: [{
      type: 'funnel',
      left: '10%',
      top: 40,
      bottom: 40,
      width: '80%',
      min: 0,
      max: data[0]?.value || 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 4,
      label: { show: true, position: 'inside', color: '#fff', fontSize: 12 },
      labelLine: { show: false },
      itemStyle: { borderColor: '#1a1a2e', borderWidth: 2, borderRadius: 4 },
      data: data.map(d => ({ name: d.stage, value: d.count }))
    }]
  })
}

const initCustomerSourceChart = (data) => {
  const chart = echarts.init(customerSourceRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#ccc' } },
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: data.map(d => ({ name: d.source, value: d.count }))
    }]
  })
}

const initTrendChart = (data) => {
  const chart = echarts.init(trendRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.month),
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#222' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#5470c6', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(84,112,198,0.5)' },
          { offset: 1, color: 'rgba(84,112,198,0.1)' }
        ])
      },
      itemStyle: { color: '#5470c6' },
      data: data.map(d => d.count)
    }]
  })
}

const initTeamRankingChart = (data) => {
  const chart = echarts.init(teamRankingRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999', rotate: 30 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#222' } }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#5470c6' },
          { offset: 1, color: '#91cc75' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      data: data.map(d => d.amount)
    }]
  })
}

const initWinRateChart = (value) => {
  const chart = echarts.init(winRateRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -160,
      min: 0,
      max: 100,
      splitNumber: 5,
      radius: '90%',
      center: ['50%', '60%'],
      axisLine: {
        lineStyle: {
          width: 20,
          color: [
            [0.3, '#91cc75'],
            [0.7, '#5470c6'],
            [1, '#ee6666']
          ]
        }
      },
      pointer: {
        itemStyle: { color: '#5470c6' },
        width: 5,
        length: '60%'
      },
      axisTick: { distance: -20, length: 6, lineStyle: { color: '#fff', width: 1 } },
      splitLine: { distance: -24, length: 14, lineStyle: { color: '#fff', width: 2 } },
      axisLabel: { color: '#999', distance: 28, fontSize: 11 },
      detail: {
        valueAnimation: true,
        formatter: '{value}%',
        color: '#eee',
        fontSize: 28,
        offsetCenter: [0, '70%']
      },
      data: [{ value: value }]
    }]
  })
}

const initCustomerLevelChart = (data) => {
  const chart = echarts.init(customerLevelRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.level),
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#222' } }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#fac858' },
          { offset: 1, color: '#ee6666' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      data: data.map(d => d.count)
    }]
  })
}

// 加载数据
const loadData = async () => {
  try {
    const [statsRes, funnelRes, sourceRes, trendRes, rankingRes, winRateRes, levelRes] = await Promise.all([
      request.get('/bi/stats'),
      request.get('/bi/funnel'),
      request.get('/bi/customer-source'),
      request.get('/bi/trend', { params: { months: 6 } }),
      request.get('/bi/team-ranking'),
      request.get('/bi/business-winrate'),
      request.get('/bi/customer-level')
    ])

    // 填充统计数据
    const s = statsRes.data || statsRes
    stats.totalCustomers = s.totalCustomers || 0
    stats.totalContractAmount = s.totalContractAmount || 0
    stats.monthDeals = s.monthDeals || 0
    stats.businessCount = s.businessCount || 0

    // 初始化图表
    if (funnelRes.data || funnelRes) {
      initFunnelChart((funnelRes.data || funnelRes) || [])
    }
    if (sourceRes.data || sourceRes) {
      initCustomerSourceChart((sourceRes.data || sourceRes) || [])
    }
    if (trendRes.data || trendRes) {
      initTrendChart((trendRes.data || trendRes) || [])
    }
    if (rankingRes.data || rankingRes) {
      initTeamRankingChart((rankingRes.data || rankingRes) || [])
    }
    if (winRateRes.data || winRateRes) {
      const rate = winRateRes.data?.winRate ?? winRateRes.winRate ?? 0
      initWinRateChart(rate)
    }
    if (levelRes.data || levelRes) {
      initCustomerLevelChart((levelRes.data || levelRes) || [])
    }
  } catch (err) {
    console.error('加载BI数据失败', err)
  }
}

// 窗口 resize 时重绘图表
const handleResize = () => {
  funnelRef.value && echarts.getInstanceByDom(funnelRef.value)?.resize()
  customerSourceRef.value && echarts.getInstanceByDom(customerSourceRef.value)?.resize()
  trendRef.value && echarts.getInstanceByDom(trendRef.value)?.resize()
  teamRankingRef.value && echarts.getInstanceByDom(teamRankingRef.value)?.resize()
  winRateRef.value && echarts.getInstanceByDom(winRateRef.value)?.resize()
  customerLevelRef.value && echarts.getInstanceByDom(customerLevelRef.value)?.resize()
}

onMounted(() => {
  updateDateTime()
  timer = setInterval(updateDateTime, 1000)
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.bi-container {
  min-height: 100vh;
  background: #1a1a2e;
  color: #fff;
  padding: 16px 24px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0 20px;
  border-bottom: 1px solid #2d2d4a;
  margin-bottom: 20px;
}

.title {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 4px;
}

.datetime {
  font-size: 18px;
  color: #888;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #16213e;
  border: 1px solid #2d2d4a;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-icon.blue { background: rgba(64, 158, 255, 0.2); color: #409eff; }
.stat-icon.green { background: rgba(145, 204, 117, 0.2); color: #91cc75; }
.stat-icon.orange { background: rgba(250, 200, 88, 0.2); color: #fac858; }
.stat-icon.purple { background: rgba(204, 153, 255, 0.2); color: #c699ff; }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #888;
  margin-bottom: 4px;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  background: #16213e;
  border: 1px solid #2d2d4a;
  border-radius: 8px;
  padding: 16px;
  height: 340px;
}

.chart-title {
  font-size: 16px;
  color: #409eff;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #2d2d4a;
}

.chart {
  height: calc(100% - 40px);
  min-height: 280px;
}

.gauge-chart {
  height: calc(100% - 40px);
  min-height: 280px;
}

/* Element Plus statistic 字体颜色覆盖 */
:deep(.el-statistic__content) {
  color: #fff;
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-statistic__prefix) {
  color: #fff;
}

:deep(.el-col) {
  max-width: 25%;
}
</style>
