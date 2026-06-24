import request from '@/utils/request'

export function toggleLike(articleId) {
  return request.post(`/like/toggle/${articleId}`)
}

export function getMyLikedArticles(params) {
  return request.get('/like/my', params)
}
