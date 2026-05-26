<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">CRM系统</div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#fff" active-text-color="#409eff">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>首页</span></el-menu-item>
        <el-menu-item index="/customer"><el-icon><User /></el-icon><span>客户管理</span></el-menu-item>
        <el-menu-item index="/contact"><el-icon><Phone /></el-icon><span>联系人</span></el-menu-item>
        <el-menu-item index="/business"><el-icon><TrendCharts /></el-icon><span>商机管理</span></el-menu-item>
        <el-menu-item index="/contract"><el-icon><Document /></el-icon><span>合同管理</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/bi"><el-icon><DataAnalysis /></el-icon><span>数据大屏</span></el-menu-item>
        <el-menu-item index="/oa"><el-icon><Operation /></el-icon><span>OA审批</span></el-menu-item>
        <el-menu-item index="/erp"><el-icon><Box /></el-icon><span>ERP管理</span></el-menu-item>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/log">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title || 'CRM系统' }}</span>
        <div class="user-info">
          <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-button type="danger" link @click="userStore.logout()">退出</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background: #001529; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; border-bottom: 1px solid #002140; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.title { font-size: 16px; font-weight: 500; }
.user-info { display: flex; align-items: center; gap: 12px; }
.main { background: #f0f2f5; padding: 0; }
</style>
