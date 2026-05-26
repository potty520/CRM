<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-button type="primary" @click="openDialog()">新建审批</el-button>
      </div>

      <el-tabs v-model="activeTab" class="oa-tabs">
        <!-- 我的申请 -->
        <el-tab-pane label="我的申请" name="my">
          <el-table :data="myApplications" stripe v-loading="loading">
            <el-table-column prop="flowName" label="流程名称" min-width="150" />
            <el-table-column prop="flowType" label="类型" width="120" />
            <el-table-column prop="createTime" label="申请时间" width="160" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goDetail(row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
            :total="total" layout="total, prev, pager, next" @change="loadMyApplications" />
        </el-tab-pane>

        <!-- 待我审批 -->
        <el-tab-pane label="待我审批" name="pending">
          <el-table :data="pendingTasks" stripe v-loading="loading">
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="flowName" label="流程名称" min-width="150" />
            <el-table-column prop="createTime" label="申请时间" width="160" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" @click="goDetail(row.flowId)">审批</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
            :total="total" layout="total, prev, pager, next" @change="loadPending" />
        </el-tab-pane>

        <!-- 审批历史 -->
        <el-tab-pane label="审批历史" name="history">
          <el-table :data="historyList" stripe v-loading="loading">
            <el-table-column prop="flowName" label="流程名称" min-width="150" />
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="{ row }">
                <el-tag :type="resultType(row.result)">{{ row.result }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goDetail(row.flowId)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
            :total="total" layout="total, prev, pager, next" @change="loadHistory" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新建审批弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建审批" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="流程名称">
          <el-input v-model="form.flowName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="form.flowType" placeholder="请选择流程类型" style="width:100%">
            <el-option label="请假申请" value="请假申请" />
            <el-option label="差旅报销" value="差旅报销" />
            <el-option label="采购申请" value="采购申请" />
            <el-option label="加班申请" value="加班申请" />
            <el-option label="离职申请" value="离职申请" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人">
          <el-select v-model="form.approverId" placeholder="请选择审批人" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.name" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('my')
const total = ref(0)
const dialogVisible = ref(false)

const myApplications = ref([])
const pendingTasks = ref([])
const historyList = ref([])
const users = ref([])

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = ref({ flowName: '', flowType: '', approverId: null, remark: '' })

function statusType(s) {
  const map = { '审批中': 'warning', '已通过': 'success', '已驳回': 'danger', '已撤回': 'info' }
  return map[s] || ''
}

function resultType(s) {
  const map = { '通过': 'success', '驳回': 'danger' }
  return map[s] || ''
}

async function loadMyApplications() {
  loading.value = true
  try {
    const res = await request.get('/oa/my-applications', { params: query })
    myApplications.value = res.data.records || res.data || []
    total.value = res.data.total || myApplications.value.length
  } finally { loading.value = false }
}

async function loadPending() {
  loading.value = true
  try {
    const res = await request.get('/oa/pending', { params: query })
    pendingTasks.value = res.data.records || res.data || []
    total.value = res.data.total || pendingTasks.value.length
  } finally { loading.value = false }
}

async function loadHistory() {
  loading.value = true
  try {
    const res = await request.get('/oa/flow/page', { params: { ...query, status: 'completed' } })
    historyList.value = res.data.records || res.data || []
    total.value = res.data.total || historyList.value.length
  } finally { loading.value = false }
}

function openDialog() {
  form.value = { flowName: '', flowType: '', approverId: null, remark: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.flowName) { ElMessage.warning('请输入流程名称'); return }
  if (!form.value.flowType) { ElMessage.warning('请选择流程类型'); return }
  if (!form.value.approverId) { ElMessage.warning('请选择审批人'); return }

  await request.post('/oa/submit', {
    flowName: form.value.flowName,
    flowType: form.value.flowType,
    remark: form.value.remark,
    steps: [{ approverId: form.value.approverId, approverName: users.value.find(u => u.id === form.value.approverId)?.name || '' }]
  })
  ElMessage.success('提交成功')
  dialogVisible.value = false
  loadMyApplications()
}

function goDetail(id) {
  router.push({ path: '/oa/detail', query: { id } })
}

onMounted(async () => {
  // 加载用户列表（用于选择审批人）
  try {
    const res = await request.get('/system/user/page', { params: { pageNum: 1, pageSize: 100 } })
    users.value = res.data.records || res.data || []
  } catch (e) { /* ignore */ }
  loadMyApplications()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.oa-tabs :deep(.el-tabs__header) { margin-bottom: 16px; }
</style>
