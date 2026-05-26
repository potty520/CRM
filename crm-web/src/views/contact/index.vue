<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.contactName" placeholder="联系人姓名" clearable style="width:200px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="contactName" label="姓名" width="120" />
        <el-table-column prop="customerName" label="所属客户" min-width="150" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑联系人' : '新增联系人'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属客户">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="form.position" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
const customers = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, contactName: '' })
const dialogVisible = ref(false)
const form = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/contact/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadCustomers() {
  const res = await request.get('/customer/page', { params: { pageNum: 1, pageSize: 1000 } })
  customers.value = res.data.records
}

function openDialog(row) {
  form.value = row ? { ...row } : {}
  dialogVisible.value = true
}

async function handleSave() {
  if (form.value.id) await request.put('/contact', form.value)
  else await request.post('/contact', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除?', '提示')
  await request.delete(`/contact/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadCustomers() })
</script>
