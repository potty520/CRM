<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="销售漏斗"><div ref="funnelRef" style="height:400px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="销售排行榜"><div ref="rankRef" style="height:400px"></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card header="客户增长趋势"><div ref="trendRef" style="height:400px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="回款统计"><div ref="paymentRef" style="height:400px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const funnelRef = ref()
const rankRef = ref()
const trendRef = ref()
const paymentRef = ref()

onMounted(async () => {
  const [funnel, rank, trend, payment] = await Promise.all([
    request.get('/dashboard/funnel'),
    request.get('/dashboard/sales-rank'),
    request.get('/dashboard/customer-trend'),
    request.get('/dashboard/payment-stats')
  ])

  echarts.init(funnelRef.value).setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'funnel', left: '10%', width: '80%', data: funnel.data.map(d => ({ name: d.stage, value: d.count })) }]
  })

  echarts.init(rankRef.value).setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rank.data.map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: rank.data.map(d => Number(d.amount)), itemStyle: { color: '#67c23a' } }]
  })

  echarts.init(trendRef.value).setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.data.map(d => d.month) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: trend.data.map(d => d.count), itemStyle: { color: '#409eff' } }]
  })

  echarts.init(paymentRef.value).setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: payment.data.map(d => d.month) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: payment.data.map(d => Number(d.amount)), smooth: true, areaStyle: { color: 'rgba(64,158,255,0.2)' } }]
  })
})
</script>
