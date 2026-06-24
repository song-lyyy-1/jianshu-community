import { getToken, removeToken, removeUserInfo } from '@/utils/auth'

// 后端接口地址 — 根据运行平台动态切换：
// H5 模式使用 devServer proxy（避免跨域），小程序/App 模式直连后端地址
let BASE_URL = 'http://localhost:8080/api'
// #ifdef H5
BASE_URL = '/api'
// #endif

const TIMEOUT = 10000

/**
 * 封装 uni.request 为 Promise，支持请求/响应拦截
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径
 * @param {string} options.method - 请求方法
 * @param {Object} options.data - 请求数据
 * @param {Object} options.header - 自定义请求头
 * @returns {Promise<Object>} 响应数据
 */
function request(options) {
  const { url, method = 'GET', data = {}, header = {} } = options

  // 请求拦截：自动携带 token
  const token = getToken()
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...header
      },
      timeout: TIMEOUT,
      success: (response) => {
        const { statusCode, data: resData } = response

        // HTTP 错误
        if (statusCode === 401) {
          removeToken()
          removeUserInfo()
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 1500)
          reject(new Error('未授权'))
          return
        }

        if (statusCode >= 400) {
          var msg = (resData && resData.message) || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
          return
        }

        // 业务错误：code !== 200
        if (resData && typeof resData.code !== 'undefined' && resData.code !== 200) {
          uni.showToast({ title: resData.message || '请求失败', icon: 'none' })
          reject(new Error(resData.message || '请求失败'))
          return
        }

        resolve(resData)
      },
      fail: (error) => {
        uni.showToast({ title: '网络错误，请检查网络连接', icon: 'none' })
        reject(new Error('网络错误'))
      }
    })
  })
}

export default {
  get(url, params = {}) {
    return request({ url, method: 'GET', data: params })
  },
  post(url, data = {}) {
    return request({ url, method: 'POST', data })
  },
  put(url, data = {}) {
    return request({ url, method: 'PUT', data })
  },
  delete(url, data = {}) {
    return request({ url, method: 'DELETE', data })
  }
}
