<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.contractName" placeholder="合同名称" clearable style="width:200px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
          <el-option label="草稿" value="草稿" /><el-option label="审批中" value="审批中" />
          <el-option label="已生效" value="已生效" /><el-option label="已驳回" value="已驳回" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-upload :show-file-list="false" :http-request="handleImport" accept=".xlsx,.xls" style="display:inline-block">
          <el-button>导入</el-button>
        </el-upload>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="contractNo" label="合同编号" width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link @click="handleApprove(row, '审批中')" v-if="row.status==='草稿'">提交审批</el-button>
            <el-button link type="success" @click="handleApprove(row, '已生效')" v-if="row.status==='审批中'">通过</el-button>
            <el-button link type="danger" @click="handleApprove(row, '已驳回')" v-if="row.status==='审批中'">驳回</el-button>
            <el-button link type="warning" @click="openPayment(row)">回款</el-button>
            <el-button link type="info" @click="openInvoice(row)">发票</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑合同' : '新增合同'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="合同编号"><el-input v-model="form.contractNo" /></el-form-item>
        <el-form-item label="合同名称"><el-input v-model="form.contractName" /></el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="签订日期"><el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentVisible" title="回款登记" width="500px">
      <el-form :model="paymentForm" label-width="80px">
        <el-form-item label="回款金额"><el-input-number v-model="paymentForm.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="回款日期"><el-date-picker v-model="paymentForm.paymentDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="paymentForm.remark" /></el-form-item>
      </el-form>
      <el-divider>回款记录</el-divider>
      <el-table :data="payments" size="small">
        <el-table-column prop="amount" label="金额" /><el-table-column prop="paymentDate" label="日期" /><el-table-column prop="remark" label="备注" />
      </el-table>
      <template #footer>
        <el-button @click="paymentVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePayment">登记</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="invoiceVisible" title="发票管理" width="500px">
      <el-form :model="invoiceForm" label-width="80px">
        <el-form-item label="发票号"><el-input v-model="invoiceForm.invoiceNo" /></el-form-item>
        <el-form-item label="发票金额"><el-input-number v-model="invoiceForm.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="开票日期"><el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="invoiceForm.remark" /></el-form-item>
      </el-form>
      <el-divider>发票记录</el-divider>
      <el-table :data="invoices" size="small">
        <el-table-column prop="invoiceNo" label="发票号" /><el-table-column prop="amount" label="金额" /><el-table-column prop="invoiceDate" label="日期" /><el-table-column prop="remark" label="备注" />
      </el-table>
      <template #footer>
        <el-button @click="invoiceVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleInvoice">开票</el-button>
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
const payments = ref([])
const invoices = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, contractName: '', status: '' })
const dialogVisible = ref(false)
const paymentVisible = ref(false)
const invoiceVisible = ref(false)
const form = ref({})
const paymentForm = ref({ amount: 0 })
const invoiceForm = ref({ amount: 0 })

function statusType(s) {
  return { '草稿': 'info', '审批中': 'warning', '已生效': 'success', '已驳回': 'danger' }[s] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/contract/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row) {
  form.value = row ? { ...row } : { status: '草稿', amount: 0 }
  dialogVisible.value = true
}

async function handleSave() {
  if (form.value.id) await request.put('/contract', form.value)
  else await request.post('/contract', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleApprove(row, status) {
  await request.put(`/contract/approve/${row.id}`, null, { params: { status } })
  ElMessage.success('操作成功')
  loadData()
}

async function openPayment(row) {
  paymentForm.value = { contractId: row.id, amount: 0 }
  const res = await request.get(`/contract/payment/${row.id}`)
  payments.value = res.data
  paymentVisible.value = true
}

async function openInvoice(row) {
  invoiceForm.value = { contractId: row.id, amount: 0 }
  const res = await request.get(`/contract/invoice/${row.id}`)
  invoices.value = res.data
  invoiceVisible.value = true
}

async function handleInvoice() {
  await request.post('/contract/invoice', invoiceForm.value)
  ElMessage.success('开票成功')
  const res = await request.get(`/contract/invoice/${invoiceForm.value.contractId}`)
  invoices.value = res.data
}

async function handlePayment() {
  await request.post('/contract/payment', paymentForm.value)
  ElMessage.success('回款登记成功')
  const res = await request.get(`/contract/payment/${paymentForm.value.contractId}`)
  payments.value = res.data
}

function handleExport() {
  window.open(`/api/contract/export?contractName=${query.contractName || ''}&token=${localStorage.getItem('token')}`)
}

async function handleImport({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await request.post('/contract/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success(`成功导入 ${res.data.count} 条`)
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除?', '提示')
  await request.delete(`/contract/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  const res = await request.get('/customer/page', { params: { pageNum: 1, pageSize: 1000 } })
  customers.value = res.data.records
  loadData()
})
</script>
