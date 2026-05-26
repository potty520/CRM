<template>
  <div class="page-container">
    <el-button @click="$router.back()" style="margin-bottom:16px">返回</el-button>

    <el-card v-loading="loading">
      <!-- 流程基本信息 -->
      <template #header>
        <div class="card-header">
          <span>流程详情</span>
          <el-tag :type="statusType(flowData.status)">{{ flowData.status }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="流程名称">{{ flowData.flowName }}</el-descriptions-item>
        <el-descriptions-item label="流程类型">{{ flowData.flowType }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ flowData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ flowData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="当前步骤">{{ flowData.currentStep }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ flowData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 审批步骤 -->
    <el-card style="margin-top:16px">
      <template #header>
        <div class="card-header"><span>审批步骤</span></div>
      </template>
      <el-steps :active="activeStepIndex" finish-status="success" align-center>
        <el-step v-for="(step, idx) in steps" :key="idx"
          :title="step.approverName"
          :description="step.status"
          :status="stepStatus(step.status)" />
      </el-steps>
    </el-card>

    <!-- 操作日志 -->
    <el-card style="margin-top:16px">
      <template #header>
        <div class="card-header"><span>操作日志</span></div>
      </template>
      <el-timeline>
        <el-timeline-item v-for="(log, idx) in logs" :key="idx"
          :timestamp="log.createTime"
          :type="logType(log.action)"
          :hollow="log.action === 'pending'">
          <p>
            <strong>{{ log.userName }}</strong>
            {{ actionText(log.action) }}
            <span v-if="log.opinion">，意见：{{ log.opinion }}</span>
          </p>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 审批操作按钮 -->
    <el-card style="margin-top:16px" v-if="canApprove">
      <div style="text-align:center">
        <el-input v-model="opinion" type="textarea" :rows="2" placeholder="请输入审批意见（可选）" style="margin-bottom:16px;max-width:400px" />
        <br />
        <el-button type="success" @click="handleApprove">通过</el-button>
        <el-button type="danger" @click="handleReject">驳回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const loading = ref(false)
const flowData = ref({})
const steps = ref([])
const logs = ref([])
const opinion = ref('')
const currentUserId = ref(null)

const activeStepIndex = computed(() => {
  const idx = steps.value.findIndex(s => s.status === 'pending')
  return idx >= 0 ? idx : steps.value.length
})

const canApprove = computed(() => {
  if (!flowData.value.status || flowData.value.status !== '审批中') return false
  const pendingStep = steps.value.find(s => s.status === 'pending')
  if (!pendingStep) return false
  return pendingStep.approverId === currentUserId.value
})

function statusType(s) {
  const map = { '审批中': 'warning', '已通过': 'success', '已驳回': 'danger', '已完成': 'success' }
  return map[s] || ''
}

function stepStatus(s) {
  const map = { '已通过': 'success', 'pending': 'wait', '驳回': 'error' }
  return map[s] || 'wait'
}

function logType(action) {
  const map = { 'submit': 'primary', 'approve': 'success', 'reject': 'danger' }
  return map[action] || 'info'
}

function actionText(action) {
  const map = { 'submit': '提交了申请', 'approve': '审批通过', 'reject': '驳回了申请', 'pending': '待审批' }
  return map[action] || action
}

async function loadDetail() {
  loading.value = true
  try {
    const id = route.query.id
    const res = await request.get(`/oa/flow/${id}`)
    flowData.value = res.data || {}
    steps.value = res.data.steps || []
    logs.value = res.data.records || []
  } finally { loading.value = false }
}

async function handleApprove() {
  const id = route.query.id
  const step = steps.value.find(s => s.status === 'pending')
  if (!step) return
  await request.put(`/oa/approve/${id}/${step.id}`, null, { params: { opinion: opinion.value } })
  ElMessage.success('审批通过')
  loadDetail()
}

async function handleReject() {
  const id = route.query.id
  const step = steps.value.find(s => s.status === 'pending')
  if (!step) return
  await request.put(`/oa/reject/${id}/${step.id}`, null, { params: { opinion: opinion.value } })
  ElMessage.success('已驳回')
  loadDetail()
}

onMounted(async () => {
  // 获取当前登录用户ID（假设从localStorage或store中获取）
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    currentUserId.value = user.id
  } catch (e) { /* ignore */ }
  loadDetail()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
