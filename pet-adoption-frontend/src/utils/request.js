import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, clearAuth } from '@/utils/auth'
import { useLoadingStore } from '@/stores/loading'

const request = axios.create({
  baseURL: '/api',  // 基础路径
  timeout: 10000    // 超时时间 10 秒
})

// 存储请求取消令牌
const cancelTokens = new Map()

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 生成请求标识
    const requestKey = `${config.method}:${config.url}`
    
    // 取消之前的相同请求
    if (cancelTokens.has(requestKey)) {
      cancelTokens.get(requestKey).cancel('取消重复请求')
    }
    
    // 创建新的取消令牌
    const source = axios.CancelToken.source()
    config.cancelToken = source.token
    cancelTokens.set(requestKey, source)
    
    // 添加token
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳
    config.headers['X-Request-Time'] = new Date().getTime()
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error('请求发送失败，请检查网络连接')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 移除请求取消令牌
    const requestKey = `${response.config.method}:${response.config.url}`
    cancelTokens.delete(requestKey)
    
    const res = response.data
    
    if (res.code !== 200) {
      // 处理业务错误
      ElMessage.error(res.message || '请求失败')
      
      // 401 未授权，跳转到登录页
      if (res.code === 401) {
        clearAuth()
        ElMessage.warning('登录已过期，请重新登录')
        router.push('/login')
      }
      
      // 403 权限不足
      if (res.code === 403) {
        ElMessage.warning('权限不足，无法操作')
      }
      
      // 404 资源不存在
      if (res.code === 404) {
        ElMessage.warning('请求的资源不存在')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 移除请求取消令牌
    if (error.config) {
      const requestKey = `${error.config.method}:${error.config.url}`
      cancelTokens.delete(requestKey)
    }
    
    // 处理网络错误
    if (axios.isCancel(error)) {
      // 取消请求，不显示错误
      return Promise.reject(error)
    }
    
    if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查网络连接')
    } else if (error.response) {
      // 服务器返回错误
      const status = error.response.status
      switch (status) {
        case 401:
          clearAuth()
          ElMessage.warning('登录已过期，请重新登录')
          router.push('/login')
          break
        case 403:
          ElMessage.warning('权限不足，无法操作')
          break
        case 404:
          ElMessage.warning('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(`请求失败 (${status})`)
      }
    } else {
      ElMessage.error('请求失败，请稍后重试')
    }
    
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default request
