import request from './index'

export function applyAdoption(data) {
  return request({
    url: '/adoptions',
    method: 'post',
    data
  })
}

export function getMyAdoptions(params) {
  return request({
    url: '/adoptions/my',
    method: 'get',
    params
  })
}

export function reviewAdoption(id, data) {
  return request({
    url: `/adoptions/${id}/review`,
    method: 'put',
    data
  })
}

export function getPendingAdoptions(params) {
  return request({
    url: '/admin/adoptions/pending',
    method: 'get',
    params
  })
}

export function cancelAdoption(id) {
  return request({
    url: `/adoptions/${id}`,
    method: 'delete'
  })
}

export function confirmAdoption(id) {
  return request({
    url: `/adoptions/${id}/confirm`,
    method: 'put'
  })
}

export function rateAdoption(id, data) {
  return request({
    url: `/adoptions/${id}/rate`,
    method: 'post',
    data
  })
}
