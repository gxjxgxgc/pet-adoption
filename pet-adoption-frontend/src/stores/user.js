import { defineStore } from 'pinia'
import { login, register, getUserInfo, logout } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    userRole: localStorage.getItem('userRole') || ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    isAdmin: (state) => state.userRole === 'ADMIN',
    userType: (state) => state.userRole || state.userInfo?.userType || 'USER',
    isShelter: (state) => state.userRole === 'SHELTER' || state.userRole === 'RESCUER' || state.userInfo?.userType === 'SHELTER'
  },
  
  actions: {
    async registerAction(registerForm) {
      try {
        const res = await register(registerForm)
        ElMessage.success('注册成功')
        return res
      } catch (error) {
        ElMessage.error('注册失败')
        throw error
      }
    },
    async loginAction(loginForm) {
      try {
        const res = await login(loginForm)
        this.token = res.data.token
        this.userRole = res.data.role || res.data.userType || 'USER'
        
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userRole', this.userRole)
        
        await this.getUserInfoAction()
        
        ElMessage.success('登录成功')
      } catch (error) {
        ElMessage.error('登录失败')
        throw error
      }
    },
    
    async getUserInfoAction() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        if (res.data?.userType) {
          this.userRole = res.data.userType
          localStorage.setItem('userRole', res.data.userType)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    
    async logoutAction() {
      try {
        await logout()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        this.userRole = ''
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        ElMessage.success('已退出登录')
      }
    }
  }
})