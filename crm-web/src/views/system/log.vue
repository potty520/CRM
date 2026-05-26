<template>
  <div class="page-container">
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="method" label="方法" width="120" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
      <el-pagination style="margin-top:16px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/system/log/page', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
