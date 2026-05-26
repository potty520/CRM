import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页' } },
      { path: 'customer', name: 'Customer', component: () => import('@/views/customer/index.vue'), meta: { title: '客户管理' } },
      { path: 'contact', name: 'Contact', component: () => import('@/views/contact/index.vue'), meta: { title: '联系人' } },
      { path: 'business', name: 'Business', component: () => import('@/views/business/index.vue'), meta: { title: '商机管理' } },
      { path: 'contract', name: 'Contract', component: () => import('@/views/contract/index.vue'), meta: { title: '合同管理' } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/statistics/index.vue'), meta: { title: '数据统计' } },
      { path: 'bi', name: 'Bi', component: () => import('@/views/bi/index.vue'), meta: { title: '数据大屏' } },
      { path: 'oa', name: 'Oa', component: () => import('@/views/oa/index.vue'), meta: { title: 'OA审批' } },
      { path: 'oa/detail/:id', name: 'OaDetail', component: () => import('@/views/oa/detail.vue'), meta: { title: '审批详情' } },
      { path: 'erp', name: 'Erp', component: () => import('@/views/erp/index.vue'), meta: { title: 'ERP管理' } },
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role.vue'), meta: { title: '角色管理' } },
      { path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/log.vue'), meta: { title: '操作日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
