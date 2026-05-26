import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])

  async function login(form) {
    const res = await request.post('/auth/login', form)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
    router.push('/')
  }

  async function fetchUserInfo() {
    const res = await request.get('/auth/info')
    userInfo.value = res.data
    menus.value = res.data.menus || []
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, userInfo, menus, login, fetchUserInfo, logout }
})
