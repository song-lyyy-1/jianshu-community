import request from '@/utils/request'

export function getArticleList(params) {
  return request.get('/article/list', { params })
}

export function getArticleDetail(id, params) {
  return request.get(`/article/detail/${id}`, { params })
}

export function createArticle(data) {
  return request.post('/article/create', data)
}

export function updateArticle(id, data) {
  return request.put(`/article/update/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/article/delete/${id}`)
}

export function getMyArticles(params) {
  return request.get('/article/my', { params })
}
