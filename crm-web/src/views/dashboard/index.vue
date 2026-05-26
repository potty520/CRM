<template>
  <div class="page-container">
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover"><div class="stat-card"><div class="label">{{ item.label }}</div><div class="value">{{ item.value }}</div></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="销售漏斗"><div ref="funnelRef" style="height:350px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="销售排行榜"><div ref="rankRef" style="height:350px"></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card header="待跟进客户">
          <el-table :data="pendingList" stripe size="small">
            <el-table-column prop="customerName" label="客户" />
            <el-table-column prop="followType" label="类型" width="80" />
            <el-table-column prop="nextFollowTime" label="下次跟进" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="客户增长趋势"><div ref="trendRef" style="height:300px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const stats = ref([])
const pendingList = ref([])
const funnelRef = ref()
const rankRef = ref()
const trendRef = ref()

onMounted(async () => {
  const [overview, funnel, rank, trend, pending] = await Promise.all([
    request.get('/dashboard/overview'),
    request.get('/dashboard/funnel'),
    request.get('/dashboard/sales-rank'),
    request.get('/dashboard/customer-trend'),
    request.get('/dashboard/pending-follows')
  ])
  stats.value = [
    { label: '今日新增客户', value: overview.data.todayCustomers },
    { label: '客户总数', value: overview.data.totalCustomers },
    { label: '商机总数', value: overview.data.totalBusiness },
    { label: '本月成交金额', value: '¥' + (overview.data.monthAmount || 0) }
  ]
  pendingList.value = pending.data

  const funnelChart = echarts.init(funnelRef.value)
  funnelChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'funnel', left: '10%', width: '80%', data: funnel.data.map(d => ({ name: d.stage, value: d.count })) }]
  })

  const rankChart = echarts.init(rankRef.value)
  rankChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rank.data.map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: rank.data.map(d => d.amount), itemStyle: { color: '#409eff' } }]
  })

  const trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.data.map(d => d.month) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: trend.data.map(d => d.count), smooth: true, areaStyle: {} }]
  })
})
</script>
