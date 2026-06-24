<template>
  <view class="register-page">
    <NavBar title="注册" />

    <view class="register-header">
      <text class="register-title">注册账号</text>
      <text class="register-subtitle">加入简书问答社区</text>
    </view>

    <view class="register-form">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input
          class="form-input"
          v-model="username"
          placeholder="请输入用户名(3-20字)"
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
          placeholder="请输入密码(6-20字)"
          placeholder-class="form-input__placeholder"
          :maxlength="20"
        />
      </view>
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input
          class="form-input"
          v-model="nickname"
          placeholder="请输入昵称(可选)"
          placeholder-class="form-input__placeholder"
          :maxlength="20"
        />
      </view>

      <view class="register-actions">
        <button
          class="submit-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <view class="link-text">
          <text class="link-text__prefix">已有账号？</text>
          <text class="link-action" @click="goLogin">去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()

const username = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)

function validate() {
  if (!username.value) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return false
  }
  if (username.value.length < 3 || username.value.length > 20) {
    uni.showToast({ title: '用户名需3-20字', icon: 'none' })
    return false
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return false
  }
  if (password.value.length < 6 || password.value.length > 20) {
    uni.showToast({ title: '密码需6-20字', icon: 'none' })
    return false
  }
  return true
}

async function handleRegister() {
  if (loading.value) return
  if (!validate()) return
  loading.value = true
  try {
    await userStore.register({
      username: username.value,
      password: password.value,
      nickname: nickname.value || undefined
    })
    uni.showToast({ title: '注册成功', icon: 'success' })
    // 注册成功后自动登录
    await userStore.login(username.value, password.value)
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/home' })
    }, 1000)
  } catch (e) {
    // 错误由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.navigateBack({ delta: 1 }).catch(() => {
    uni.redirectTo({ url: '/pages/login/login' })
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: var(--jianshu-white);
}

.register-header {
  text-align: center;
  padding: 80rpx 0 60rpx;
}

.register-title {
  display: block;
  font-size: 56rpx;
  color: var(--jianshu-red);
  font-weight: 700;
  line-height: 1.2;
}

.register-subtitle {
  display: block;
  font-size: 28rpx;
  color: var(--jianshu-text-light);
  margin-top: 16rpx;
}

.register-form {
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

.register-actions {
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
