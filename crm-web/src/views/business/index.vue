<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.businessName" placeholder="商机名称" clearable style="width:200px" />
        <el-select v-model="query.stage" placeholder="阶段" clearable style="width:140px">
          <el-option v-for="s in stages" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="businessName" label="商机名称" min-width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="stage" label="阶段" width="110">
          <template #default="{ row }"><el-tag>{{ row.stage }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="probability" label="成交概率" width="100">
          <template #default="{ row }">{{ row.probability }}%</template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link @click="openStage(row)">推进</el-button>
            <el-button link type="success" @click="handleConvert(row)">转合同</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商机' : '新增商机'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商机名称"><el-input v-model="form.businessName" /></el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="form.stage" style="width:100%"><el-option v-for="s in stages" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="成交概率"><el-slider v-model="form.probability" :max="100" show-input /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stageVisible" title="推进阶段" width="400px">
      <el-select v-model="newStage" style="width:100%"><el-option v-for="s in stages" :key="s" :label="s" :value="s" /></el-select>
      <template #footer>
        <el-button @click="stageVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStage">确定</el-button>
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
const stages = ref([])
const customers = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, businessName: '', stage: '' })
const dialogVisible = ref(false)
const stageVisible = ref(false)
const form = ref({ probability: 10 })
const newStage = ref('')
const stageId = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/business/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row) {
  form.value = row ? { ...row } : { stage: '初步接触', probability: 10, amount: 0 }
  dialogVisible.value = true
}

function openStage(row) {
  stageId.value = row.id
  newStage.value = row.stage
  stageVisible.value = true
}

async function handleSave() {
  if (form.value.id) await request.put('/business', form.value)
  else await request.post('/business', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleStage() {
  await request.put(`/business/stage/${stageId.value}`, null, { params: { stage: newStage.value } })
  ElMessage.success('阶段已更新')
  stageVisible.value = false
  loadData()
}

async function handleConvert(row) {
  await ElMessageBox.confirm('确认将该商机转为合同?', '提示')
  await request.post(`/business/convert/${row.id}`)
  ElMessage.success('合同已生成')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除?', '提示')
  await request.delete(`/business/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  const [stageRes, customerRes] = await Promise.all([
    request.get('/business/stages'),
    request.get('/customer/page', { params: { pageNum: 1, pageSize: 1000 } })
  ])
  stages.value = stageRes.data
  customers.value = customerRes.data.records
  loadData()
})
</script>
