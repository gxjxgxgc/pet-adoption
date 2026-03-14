import request from './index'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取管理员统计数据
 */
export function getAdminStats() {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<Object>} 用户列表数据
 */
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 * @param {number} id - 用户 ID
 * @param {Object} data - 更新数据
 * @param {number} data.status - 状态：0-禁用 1-启用
 * @returns {Promise<void>}
 */
export function updateUserStatus(id, data) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data
  })
}

/**
 * 更新用户标签
 * @param {number} id - 用户 ID
 * @param {Object} data - 更新数据
 * @param {string} data.tags - 标签（逗号分隔）
 * @returns {Promise<void>}
 */
export function updateUserTags(id, data) {
  return request({
    url: `/admin/users/${id}/tags`,
    method: 'put',
    data
  })
}

// 默认导出
export default {
  login,
  register,
  getUserInfo,
  updateProfile,
  logout,
  getAdminStats,
  getUserList,
  updateUserStatus,
  updateUserTags
}