const TOKEN_KEY = 'jianshu_token'
const USER_KEY = 'jianshu_user'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getUserInfo() {
  const data = uni.getStorageSync(USER_KEY)
  return data || null
}

export function setUserInfo(user) {
  uni.setStorageSync(USER_KEY, user)
}

export function removeUserInfo() {
  uni.removeStorageSync(USER_KEY)
}
