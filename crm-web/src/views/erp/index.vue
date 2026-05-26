<template>
  <div class="erp-container">
    <div class="erp-header">
      <h2>ERP管理系统</h2>
      <el-radio-group v-model="activeTab" size="default">
        <el-radio-button label="product">产品管理</el-radio-button>
        <el-radio-button label="inventory">库存管理</el-radio-button>
        <el-radio-button label="financial">财务管理</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 产品管理 -->
    <div v-show="activeTab === 'product'" class="tab-content">
      <div class="toolbar">
        <el-input v-model="productSearch" placeholder="产品名称/编码" style="width:200px" @keyup.enter="loadProductPage" />
        <el-button type="primary" @click="loadProductPage">查询</el-button>
        <el-button type="success" @click="openProductDialog()">新增产品</el-button>
        <el-button @click="exportProduct">导出</el-button>
      </div>
      <el-table :data="productList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productCode" label="产品编码" width="150" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="单价" width="120" formatter="{row=>'¥'+row.price}" />
        <el-table-column prop="cost" label="成本" width="120" formatter="{row=>'¥'+row.cost}" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openProductDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="delProduct(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background layout="total,prev,pager,next"
        :total="productTotal" :page-size="productPageSize" :current-page="productPageNum"
        @current-change="loadProductPage" style="margin-top:15px" />
    </div>

    <!-- 库存管理 -->
    <div v-show="activeTab === 'inventory'" class="tab-content">
      <div class="toolbar">
        <el-input v-model="inventorySearch" placeholder="产品/仓库" style="width:200px" @keyup.enter="loadInventoryPage" />
        <el-button type="primary" @click="loadInventoryPage">查询</el-button>
        <el-button type="success" @click="openInventoryDialog()">新增库存</el-button>
      </div>
      <el-table :data="inventoryList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="warehouse" label="仓库" width="150" />
        <el-table-column prop="stockQty" label="库存数量" width="120" />
        <el-table-column prop="availableQty" label="可用数量" width="120" />
        <el-table-column prop="lockedQty" label="锁定数量" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="lastInbound" label="最后入库" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openInventoryDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="adjustStock(row)">调整</el-button>
            <el-button size="small" type="danger" @click="delInventory(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background layout="total,prev,pager,next"
        :total="inventoryTotal" :page-size="inventoryPageSize" :current-page="inventoryPageNum"
        @current-change="loadInventoryPage" style="margin-top:15px" />
    </div>

    <!-- 财务管理 -->
    <div v-show="activeTab === 'financial'" class="tab-content">
      <div class="toolbar">
        <el-date-picker v-model="financeDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:260px" />
        <el-select v-model="financeDirection" placeholder="方向" clearable style="width:120px">
          <el-option label="收入" value="in" />
          <el-option label="支出" value="out" />
        </el-select>
        <el-button type="primary" @click="loadFinancialPage">查询</el-button>
        <el-button type="success" @click="openFinancialDialog()">新增记录</el-button>
      </div>
      <el-table :data="financialList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="recordType" label="类型" width="100" />
        <el-table-column prop="bizType" label="业务类型" width="120" />
        <el-table-column prop="amount" label="金额" width="130" formatter="{row=>'¥'+row.amount}" />
        <el-table-column prop="direction" label="方向" width="80">
          <template #default="{row}">
            <el-tag :type="row.direction==='in'?'success':'danger'">{{ row.direction==='in'?'收入':'支出' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="150" />
        <el-table-column prop="counterparty" label="交易对手" width="150" />
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="danger" @click="delFinancial(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background layout="total,prev,pager,next"
        :total="financialTotal" :page-size="financialPageSize" :current-page="financialPageNum"
        @current-change="loadFinancialPage" style="margin-top:15px" />
    </div>

    <!-- 产品弹窗 -->
    <el-dialog v-model="productDialogVisible" :title="productForm.id?'编辑产品':'新增产品'" width="500px">
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="产品编码"><el-input v-model="productForm.productCode" /></el-form-item>
        <el-form-item label="产品名称"><el-input v-model="productForm.productName" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="productForm.category" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="productForm.unit" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="productForm.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="成本"><el-input-number v-model="productForm.cost" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="productDialogVisible=false">取消</el-button><el-button type="primary" @click="saveProduct">保存</el-button></template>
    </el-dialog>

    <!-- 库存弹窗 -->
    <el-dialog v-model="inventoryDialogVisible" :title="inventoryForm.id?'编辑库存':'新增库存'" width="500px">
      <el-form :model="inventoryForm" label-width="100px">
        <el-form-item label="产品"><el-input v-model="inventoryForm.productName" /></el-form-item>
        <el-form-item label="仓库"><el-input v-model="inventoryForm.warehouse" /></el-form-item>
        <el-form-item label="库存数量"><el-input-number v-model="inventoryForm.stockQty" :min="0" /></el-form-item>
        <el-form-item label="可用数量"><el-input-number v-model="inventoryForm.availableQty" :min="0" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="inventoryForm.unit" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="inventoryDialogVisible=false">取消</el-button><el-button type="primary" @click="saveInventory">保存</el-button></template>
    </el-dialog>

    <!-- 财务弹窗 -->
    <el-dialog v-model="financialDialogVisible" :title="financialForm.id?'编辑记录':'新增记录'" width="500px">
      <el-form :model="financialForm" label-width="100px">
        <el-form-item label="记录类型"><el-select v-model="financialForm.recordType"><el-option label="开票" value="invoice" /><el-option label="收款" value="payment" /><el-option label="退款" value="refund" /><el-option label="支出" value="expense" /></el-select></el-form-item>
        <el-form-item label="方向"><el-radio-group v-model="financialForm.direction"><el-radio label="in">收入</el-radio><el-radio label="out">支出</el-radio></el-radio-group></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="financialForm.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="科目"><el-input v-model="financialForm.subject" /></el-form-item>
        <el-form-item label="交易对手"><el-input v-model="financialForm.counterparty" /></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="financialForm.recordDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="financialForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="financialDialogVisible=false">取消</el-button><el-button type="primary" @click="saveFinancial">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('product')

// 产品
const productList = ref([])
const productTotal = ref(0)
const productPageNum = ref(1)
const productPageSize = ref(10)
const productSearch = ref('')
const productDialogVisible = ref(false)
const productForm = reactive({ id:null, productCode:'', productName:'', category:'', unit:'', price:0, cost:0 })

// 库存
const inventoryList = ref([])
const inventoryTotal = ref(0)
const inventoryPageNum = ref(1)
const inventoryPageSize = ref(10)
const inventorySearch = ref('')
const inventoryDialogVisible = ref(false)
const inventoryForm = reactive({ id:null, productName:'', warehouse:'', stockQty:0, availableQty:0, unit:'' })

// 财务
const financialList = ref([])
const financialTotal = ref(0)
const financialPageNum = ref(1)
const financialPageSize = ref(10)
const financeDateRange = ref([])
const financeDirection = ref('')
const financialDialogVisible = ref(false)
const financialForm = reactive({ id:null, recordType:'invoice', direction:'in', amount:0, subject:'', counterparty:'', recordDate:'', remark:'' })

function loadProductPage(pn=1) {
  productPageNum.value = pn
  request.get('/erp/product/page', { params:{ pageNum:pn, pageSize:productPageSize.value, keyword:productSearch.value } }).then(r=>{
    if(r.data) { productList.value=r.data.records||r.data; productTotal.value=r.data.total||0 }
  })
}
function openProductDialog(row) {
  if(row) Object.assign(productForm, row)
  else { productForm.id=null; productForm.productCode=''; productForm.productName=''; productForm.category=''; productForm.unit=''; productForm.price=0; productForm.cost=0 }
  productDialogVisible.value=true
}
function saveProduct() {
  const m = productForm.id ? 'put' : 'post'
  request[m]('/erp/product' + (productForm.id?'/'+productForm.id:''), productForm).then(()=>{ productDialogVisible.value=false; loadProductPage(productPageNum.value) })
}
function deleteProduct(id) {
  ElMessageBox.confirm('删除产品？','提示').then(()=>request.delete('/erp/product/'+id).then(()=>loadProductPage()))
}
function exportProduct() { request.get('/erp/product/export', { responseType:'blob' }).then(r=>{ const a=document.createElement('a'); a.href=URL.createObjectURL(r); a.click() }) }

function loadInventoryPage(pn=1) {
  inventoryPageNum.value = pn
  request.get('/erp/inventory/page', { params:{ pageNum:pn, pageSize:inventoryPageSize.value, keyword:inventorySearch.value } }).then(r=>{
    if(r.data) { inventoryList.value=r.data.records||r.data; inventoryTotal.value=r.data.total||0 }
  })
}
function openInventoryDialog(row) {
  if(row) Object.assign(inventoryForm, row)
  else { inventoryForm.id=null; inventoryForm.productName=''; inventoryForm.warehouse=''; inventoryForm.stockQty=0; inventoryForm.availableQty=0; inventoryForm.unit='' }
  inventoryDialogVisible.value=true
}
function saveInventory() {
  const m = inventoryForm.id?'put':'post'
  request[m]('/erp/inventory' + (inventoryForm.id?'/'+inventoryForm.id:''), inventoryForm).then(()=>{ inventoryDialogVisible.value=false; loadInventoryPage(inventoryPageNum.value) })
}
function adjustStock(row) { ElMessage.info('库存调整功能待实现') }
function delInventory(id) {
  ElMessageBox.confirm('删除库存记录？','提示').then(()=>request.delete('/erp/inventory/'+id).then(()=>loadInventoryPage()))
}

function loadFinancialPage(pn=1) {
  financialPageNum.value = pn
  request.get('/erp/financial/page', { params:{ pageNum:pn, pageSize:financialPageSize.value, startDate:financeDateRange.value?.[0], endDate:financeDateRange.value?.[1], direction:financeDirection.value } }).then(r=>{
    if(r.data) { financialList.value=r.data.records||r.data; financialTotal.value=r.data.total||0 }
  })
}
function openFinancialDialog(row) {
  if(row) Object.assign(financialForm, row)
  else { financialForm.id=null; financialForm.recordType='invoice'; financialForm.direction='in'; financialForm.amount=0; financialForm.subject=''; financialForm.counterparty=''; financialForm.recordDate=''; financialForm.remark='' }
  financialDialogVisible.value=true
}
function saveFinancial() {
  const m = financialForm.id?'put':'post'
  request[m]('/erp/financial' + (financialForm.id?'/'+financialForm.id:''), financialForm).then(()=>{ financialDialogVisible.value=false; loadFinancialPage(financialPageNum.value) })
}
function delFinancial(id) {
  ElMessageBox.confirm('删除财务记录？','提示').then(()=>request.delete('/erp/financial/'+id).then(()=>loadFinancialPage()))
}

onMounted(() => {
  loadProductPage()
  loadInventoryPage()
  loadFinancialPage()
})
</script>

<style scoped>
.erp-container { padding: 20px; }
.erp-header { display:flex; align-items:center; gap:20px; margin-bottom:20px; }
.erp-header h2 { margin:0; }
.tab-content { background:#fff; border-radius:4px; padding:20px; }
.toolbar { display:flex; gap:10px; margin-bottom:15px; flex-wrap:wrap; }
</style>