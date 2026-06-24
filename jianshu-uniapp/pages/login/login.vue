<template>
  <view class="login-page">
    <NavBar title="登录" />

    <view class="login-header">
      <text class="logo-title">简书问答</text>
      <text class="logo-subtitle">创作你的创作</text>
    </view>

    <view class="login-form">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input
          class="form-input"
          v-model="username"
          placeholder="请输入用户名"
          placeholder-class="form-input__placeholder"
          :maxlength="20"
        />
      </view>
      <view class="form-item">
        <text class="form-label">密码</text>
        <input
          class="form-input"
          password
          v-model="password"
          placeholder="请输入密码"
          placeholder-class="form-input__placeholder"
          :maxlength="20"
        />
      </view>

      <view class="login-actions">
        <button
          class="submit-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <view class="link-text">
          <text class="link-text__prefix">还没有账号？</text>
          <text class="link-action" @click="goRegister">去注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const redirect = ref('')

// tabBar 页面列表，用于判断跳转方式
const tabbarPages = ['/pages/home/home', '/pages/profile/profile']

onLoad((options) => {
  if (options && options.redirect) {
    redirect.value = decodeURIComponent(options.redirect)
  }
})

async function handleLogin() {
  if (loading.value) return
  if (!username.value) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login(username.value, password.value)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      navigateAfterLogin()
    }, 1000)
  } catch (e) {
    // 错误由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

function navigateAfterLogin() {
  const target = redirect.value
  if (target && tabbarPages.includes(target)) {
    uni.switchTab({ url: target })
  } else if (target) {
    uni.redirectTo({ url: target })
  } else {
    uni.switchTab({ url: '/pages/home/home' })
  }
}

function goRegister() {
  const url = redirect.value
    ? `/pages/register/register?redirect=${encodeURIComponent(redirect.value)}`
    : '/pages/register/register'
  uni.navigateTo({ url })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: var(--jianshu-white);
}

.login-header {
  text-align: center;
  padding: 80rpx 0 60rpx;
}

.logo-title {
  display: block;
  font-size: 56rpx;
  color: var(--jianshu-red);
  font-weight: 700;
  line-height: 1.2;
}

.logo-subtitle {
  display: block;
  font-size: 28rpx;
  color: var(--jianshu-text-light);
  margin-top: 16rpx;
}

.login-form {
  padding: 0 48rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: var(--jianshu-text);
  margin-bottom: 16rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid var(--jianshu-border);
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: var(--jianshu-text);
  box-sizing: border-box;
  background-color: var(--jianshu-white);
}

.form-input__placeholder {
  color: var(--jianshu-text-light);
  font-size: 28rpx;
}

.login-actions {
  margin-top: 48rpx;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: var(--jianshu-red);
  color: var(--jianshu-white);
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  padding: 0;
}

.submit-btn::after {
  border: none;
}

.submit-btn[disabled] {
  background-color: #f3b0a6;
  color: var(--jianshu-white);
}

.link-text {
  text-align: center;
  margin-top: 32rpx;
  font-size: 28rpx;
  color: var(--jianshu-text-light);
}

.link-text__prefix {
  color: var(--jianshu-text-light);
}

.link-action {
  color: var(--jianshu-red);
  margin-left: 8rpx;
}
</style>
