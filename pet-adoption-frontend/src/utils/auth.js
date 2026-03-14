// src/utils/auth.js

const TOKEN_KEY = 'token'
const USER_ROLE_KEY = 'userRole'
const USER_INFO_KEY = 'userInfo'

// 获取 Token
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// 设置 Token
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

// 清除所有认证信息
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_ROLE_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

// 检查是否已登录
export function isLoggedIn() {
  return !!getToken()
}