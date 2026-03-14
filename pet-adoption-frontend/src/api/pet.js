import request from './index'

/**
 * 获取宠物列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.type - 宠物类型：dog/cat/other
 * @param {number} params.status - 状态：0-隐藏 1-待领养 2-领养中 3-已领养
 * @returns {Promise<Object>} 宠物列表数据
 */
export function getPetList(params) {
  return request({
    url: '/pets/list',
    method: 'get',
    params
  })
}

/**
 * 获取宠物详情
 * @param {number} id - 宠物 ID
 * @returns {Promise<Object>} 宠物详情数据
 */
export function getPetDetail(id) {
  return request({
    url: `/pets/${id}`,
    method: 'get'
  })
}

/**
 * 发布宠物信息
 * @param {Object} data - 宠物信息
 * @returns {Promise<Object>} 创建的宠物信息
 */
export function publishPet(data) {
  return request({
    url: '/pets',
    method: 'post',
    data
  })
}

/**
 * 更新宠物信息
 * @param {number} id - 宠物 ID
 * @param {Object} data - 更新的宠物信息
 * @returns {Promise<Object>} 更新后的宠物信息
 */
export function updatePet(id, data) {
  return request({
    url: `/pets/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除宠物
 * @param {number} id - 宠物 ID
 * @returns {Promise<void>}
 */
export function deletePet(id) {
  return request({
    url: `/pets/${id}`,
    method: 'delete'
  })
}

/**
 * 上传宠物图片
 * @param {File} file - 图片文件
 * @returns {Promise<{url: string}>} 图片 URL
 */
export function uploadPetImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 收藏宠物
 * @param {number} petId - 宠物 ID
 * @returns {Promise<void>}
 */
export function favoritePet(petId) {
  return request({
    url: `/pets/${petId}/favorite`,
    method: 'post'
  })
}

/**
 * 取消收藏宠物
 * @param {number} petId - 宠物 ID
 * @returns {Promise<void>}
 */
export function unfavoritePet(petId) {
  return request({
    url: `/pets/${petId}/favorite`,
    method: 'delete'
  })
}

/**
 * 获取收藏列表
 * @returns {Promise<Object>} 收藏列表
 */
export function getFavoriteList() {
  return request({
    url: '/favorites',
    method: 'get'
  })
}

/**
 * 获取热门宠物
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<Object>} 热门宠物列表
 */
export function getHotPets(params) {
  return request({
    url: '/pets/hot',
    method: 'get',
    params
  })
}

export function batchUploadPets(formData) {
  return request({
    url: '/pets/batch-upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function exportPets() {
  return request({
    url: '/pets/export',
    method: 'get',
    responseType: 'blob'
  })
}

// 默认导出
export default {
  getPetList,
  getPetDetail,
  publishPet,
  updatePet,
  deletePet,
  uploadPetImage,
  favoritePet,
  unfavoritePet,
  getFavoriteList,
  getHotPets,
  batchUploadPets,
  exportPets
}