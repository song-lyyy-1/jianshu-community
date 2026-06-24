import request from '@/utils/request'

export function toggleFavorite(articleId) {
  return request.post(`/favorite/toggle/${articleId}`)
}

export function getMyFavoriteArticles(params) {
  return request.get('/favorite/my', params)
}
