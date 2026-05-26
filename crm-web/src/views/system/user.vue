<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.username" placeholder="用户名" clearable style="width:200px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" :placeholder="form.id?'留空不修改':''" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width:100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
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
const roles = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, username: '' })
const dialogVisible = ref(false)
const form = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/system/user/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row) {
  form.value = row ? { ...row, password: '' } : { roleIds: [] }
  dialogVisible.value = true
}

async function handleSave() {
  if (form.value.id) await request.put('/system/user', form.value)
  else await request.post('/system/user', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function toggleStatus(row) {
  await request.put(`/system/user/status/${row.id}`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除?', '提示')
  await request.delete(`/system/user/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  const res = await request.get('/system/role/list')
  roles.value = res.data
  loadData()
})
</script>
