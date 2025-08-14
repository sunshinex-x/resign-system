import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    isLoggedIn: !!localStorage.getItem('token')
  }),
  
  getters: {
    hasToken: (state) => !!state.token,
    currentUser: (state) => state.userInfo
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        const response = await request({
          url: '/api/auth/login',
          method: 'post',
          data: loginData
        })
        
        const { data } = response
        const token = data.token
        const userInfo = {
          id: data.id,
          username: data.username,
          nickname: data.nickname,
          email: data.email,
          phone: data.phone,
          avatar: data.avatar,
          roles: data.roles || []
        }
        
        this.token = token
        this.userInfo = userInfo
        this.isLoggedIn = true
        
        localStorage.setItem('token', token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
        return { success: true, data: userInfo }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
        return { success: false, message: error.message }
      }
    },
    
    // 登出
    async logout() {
      try {
        if (this.token) {
          await request({
            url: '/api/auth/logout',
            method: 'post',
            headers: {
              'Authorization': `Bearer ${this.token}`
            }
          })
        }
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        this.token = ''
        this.userInfo = {}
        this.isLoggedIn = false
        
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    },
    
    // 获取用户信息
    async getUserInfo() {
      try {
        if (!this.token) {
          throw new Error('未登录')
        }
        
        const response = await request({
          url: '/api/auth/userinfo',
          method: 'get',
          headers: {
            'Authorization': `Bearer ${this.token}`
          }
        })
        
        const userInfo = response.data
        this.userInfo = userInfo
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
        return { success: true, data: userInfo }
      } catch (error) {
        this.logout()
        return { success: false, message: error.message }
      }
    }
  }
})