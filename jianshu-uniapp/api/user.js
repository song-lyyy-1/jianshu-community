import request from '@/utils/request'

export function getProfile() {
  return request.get('/user/profile')
}

export function getUserStats() {
  return request.get('/user/stats')
}

export function updateUserInfo(data) {
  return request.put('/user/update', data)
}
