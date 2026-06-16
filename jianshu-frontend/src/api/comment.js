import request from '@/utils/request'

export function getCommentList(articleId) {
  return request.get(`/comment/list/${articleId}`)
}

export function addComment(data) {
  return request.post('/comment/add', data)
}
