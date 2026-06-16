<template>
  <div class="register-page">
    <div class="register-header">
      <h1 class="register-title">注册账号</h1>
      <p class="register-subtitle">加入简书问答社区</p>
    </div>
    <van-form @submit="handleRegister" class="register-form">
      <van-cell-group inset>
        <van-field
          v-model="username"
          name="username"
          label="用户名"
          left-icon="user-o"
          placeholder="请输入用户名(3-20字)"
          :rules="[
            { required: true, message: '请输入用户名' },
            { validator: (val) => val.length >= 3 && val.length <= 20, message: '用户名需3-20字' }
          ]"
        />
        <van-field
          v-model="password"
          type="password"
          name="password"
          label="密码"
          left-icon="lock"
          placeholder="请输入密码(6-20字)"
          :rules="[
            { required: true, message: '请输入密码' },
            { validator: (val) => val.length >= 6 && val.length <= 20, message: '密码需6-20字' }
          ]"
        />
        <van-field
          v-model="nickname"
          name="nickname"
          label="昵称"
          left-icon="contact"
          placeholder="请输入昵称(可选)"
        />
      </van-cell-group>
      <div class="register-actions">
        <van-button round block type="primary" native-type="submit" :loading="loading" class="submit-btn">
          注册
        </van-button>
        <p class="link-text">
          已有账号？<router-link to="/login">去登录</router-link>
        </p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)

async function handleRegister() {
  loading.value = true
  try {
    await register({
      username: username.value,
      password: password.value,
      nickname: nickname.value || undefined
    })
    showToast('注册成功')
    // Auto login after register
    await userStore.login(username.value, password.value)
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
.register-page {
  min-height: 100vh;
  background: var(--jianshu-white);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 16px;
}

.register-header {
  text-align: center;
  padding-bottom: 40px;
}

.register-title {
  font-size: 28px;
  color: var(--jianshu-red);
  font-weight: 700;
}

.register-subtitle {
  font-size: 14px;
  color: var(--jianshu-text-light);
  margin-top: 8px;
}

.register-form {
  margin-top: 20px;
}

.register-actions {
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
