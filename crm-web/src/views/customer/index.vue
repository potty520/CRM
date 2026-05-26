<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.customerName" placeholder="客户名称" clearable style="width:200px" />
        <el-select v-model="query.poolStatus" placeholder="客户类型" clearable style="width:120px">
          <el-option label="私海" :value="0" /><el-option label="公海" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-upload :show-file-list="false" :http-request="handleImport" accept=".xlsx,.xls" style="display:inline-block">
          <el-button>导入</el-button>
        </el-upload>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="level" label="等级" width="80">
          <template #default="{ row }"><el-tag>{{ ['','普通','重要','VIP'][row.level] || '普通' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column prop="ownerName" label="所属销售" width="100" />
        <el-table-column prop="poolStatus" label="类型" width="80">
          <template #default="{ row }">{{ row.poolStatus === 1 ? '公海' : '私海' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openAssign(row)">分配</el-button>
            <el-button link @click="handlePool(row)">{{ row.poolStatus === 1 ? '领取' : '放入公海' }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, sizes, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '新增客户'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level"><el-option label="普通" :value="1" /><el-option label="重要" :value="2" /><el-option label="VIP" :value="3" /></el-select>
        </el-form-item>
        <el-form-item label="来源"><el-input v-model="form.source" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配客户" width="400px">
      <el-select v-model="assignOwnerId" placeholder="选择销售" style="width:100%">
        <el-option v-for="u in userList" :key="u.id" :label="u.realName" :value="u.id" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, customerName: '', poolStatus: null })
const dialogVisible = ref(false)
const assignVisible = ref(false)
const assignOwnerId = ref(null)
const assignCustomerId = ref(null)
const userList = ref([])
const form = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/customer/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row) {
  form.value = row ? { ...row } : { level: 1 }
  dialogVisible.value = true
}

async function handleSave() {
  if (form.value.id) await request.put('/customer', form.value)
  else await request.post('/customer', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该客户?', '提示')
  await request.delete(`/customer/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

async function openAssign(row) {
  assignCustomerId.value = row.id
  assignOwnerId.value = row.ownerId
  const res = await request.get('/system/user/list')
  userList.value = res.data
  assignVisible.value = true
}

async function handleAssign() {
  await request.put('/customer/assign', null, { params: { customerId: assignCustomerId.value, ownerId: assignOwnerId.value } })
  ElMessage.success('分配成功')
  assignVisible.value = false
  loadData()
}

async function handlePool(row) {
  if (row.poolStatus === 1) {
    await request.put(`/customer/claim/${row.id}`)
    ElMessage.success('领取成功')
  } else {
    await request.put(`/customer/pool/${row.id}`)
    ElMessage.success('已放入公海')
  }
  loadData()
}

function handleExport() {
  window.open(`/api/customer/export?customerName=${query.customerName || ''}&token=${localStorage.getItem('token')}`)
}

async function handleImport({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await request.post('/customer/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success(`成功导入 ${res.data.count} 条`)
  loadData()
}

onMounted(loadData)
</script>
