<template>
  <div class="profile-page">
    <!-- Logged in state -->
    <template v-if="userStore.isLogin">
      <div class="user-info card">
        <div class="avatar-wrapper">
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
          <van-icon v-else name="user-o" size="50" color="#ccc" class="avatar-placeholder" />
        </div>
        <h3 class="nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</h3>
        <p class="bio">{{ userStore.userInfo?.bio || '这个人很懒，什么都没写...' }}</p>
      </div>

      <div class="stats-card card">
        <div class="stat-item">
          <span class="stat-value">{{ stats.articleCount || 0 }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.totalViews || 0 }}</span>
          <span class="stat-label">浏览</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.totalLikes || 0 }}</span>
          <span class="stat-label">点赞</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.totalFavorites || 0 }}</span>
          <span class="stat-label">收藏</span>
        </div>
      </div>

      <van-cell-group inset class="menu-group">
        <van-cell title="我的文章" icon="notes-o" is-link to="/my/articles" />
        <van-cell title="我的点赞" icon="like-o" is-link to="/my/likes" />
        <van-cell title="我的收藏" icon="star-o" is-link to="/my/favorites" />
      </van-cell-group>

      <div class="logout-btn">
        <van-button round block plain type="danger" @click="handleLogout">退出登录</van-button>
      </div>
    </template>

    <!-- Not logged in state -->
    <template v-else>
      <div class="not-logged">
        <van-icon name="user-o" size="80" color="#ccc" />
        <p class="not-logged-text">请先登录</p>
        <van-button round type="primary" class="login-btn" to="/login">去登录</van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProfile, getUserStats } from '@/api/user'
import { showDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const stats = ref({})

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      const res = await getUserStats()
      stats.value = res.data || {}
    } catch {
      // ignore
    }
    try {
      const profileRes = await getProfile()
      if (profileRes.data?.user) {
        userStore.userInfo = { ...userStore.userInfo, ...profileRes.data.user }
      }
      if (profileRes.data?.stats) {
        stats.value = profileRes.data.stats
      }
    } catch {
      // ignore
    }
  }
})

function handleLogout() {
  showDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  }).then(() => {
    userStore.logout()
    router.push('/')
  })
}
</script>

<style scoped>
.profile-page {
  padding-bottom: 20px;
}

.card {
  background: var(--jianshu-white);
  border-radius: 8px;
  margin: 12px;
  padding: 20px;
}

.user-info {
  text-align: center;
  padding: 30px 20px;
}

.avatar-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 12px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nickname {
  font-size: 20px;
  font-weight: 600;
  color: var(--jianshu-text);
  margin-bottom: 6px;
}

.bio {
  font-size: 13px;
  color: var(--jianshu-text-light);
  line-height: 1.5;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 20px 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--jianshu-text);
}

.stat-label {
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.menu-group {
  margin: 12px 0;
}

.logout-btn {
  padding: 20px 28px;
}

.not-logged {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
}

.not-logged-text {
  font-size: 16px;
  color: var(--jianshu-text-light);
}

.login-btn {
  background: var(--jianshu-red);
  border-color: var(--jianshu-red);
  padding: 0 40px;
}
</style>
