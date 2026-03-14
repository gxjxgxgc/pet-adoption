import request from './index'

export function addFavorite(petId) {
  return request({
    url: '/favorites',
    method: 'post',
    data: { petId }
  })
}

export function removeFavorite(petId) {
  return request({
    url: `/favorites/${petId}`,
    method: 'delete'
  })
}

export function getMyFavorites(params) {
  return request({
    url: '/favorites/my',
    method: 'get',
    params
  })
}
