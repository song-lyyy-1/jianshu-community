<template>
  <div class="login-page">
    <div class="login-header">
      <h1 class="logo-title">简书问答</h1>
      <p class="logo-subtitle">创作你的创作</p>
    </div>
    <van-form @submit="handleLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="username"
          name="username"
          label="用户名"
          left-icon="user-o"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="password"
          type="password"
          name="password"
          label="密码"
          left-icon="lock"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>
      <div class="login-actions">
        <van-button round block type="primary" native-type="submit" :loading="loading" class="submit-btn">
          登录
        </van-button>
        <p class="link-text">
          还没有账号？<router-link to="/register">去注册</router-link>
        </p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(username.value, password.value)
    showToast('登录成功')
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--jianshu-white);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 16px;
}

.login-header {
  text-align: center;
  padding-bottom: 40px;
}

.logo-title {
  font-size: 28px;
  color: var(--jianshu-red);
  font-weight: 700;
}

.logo-subtitle {
  font-size: 14px;
  color: var(--jianshu-text-light);
  margin-top: 8px;
}

.login-form {
  margin-top: 20px;
}

.login-actions {
  padding: 24px 16px;
}

.submit-btn {
  background: var(--jianshu-red);
  border-color: var(--jianshu-red);
  height: 44px;
  font-size: 16px;
}

.link-text {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--jianshu-text-light);
}

.link-text a {
  color: var(--jianshu-red);
}
</style>
