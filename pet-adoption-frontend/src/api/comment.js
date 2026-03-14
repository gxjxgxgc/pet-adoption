import request from './index'

export function addComment(data) {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

export function getComments(petId, params) {
  return request({
    url: `/comments/pet/${petId}`,
    method: 'get',
    params
  })
}

export function deleteComment(id) {
  return request({
    url: `/comments/${id}`,
    method: 'delete'
  })
}
