<template>
  <view class="profile-page">
    <NavBar title="我的" :leftArrow="false" />

    <!-- 已登录状态 -->
    <template v-if="userStore.isLogin">
      <view class="user-info card">
        <view class="avatar-wrapper">
          <image
            v-if="userStore.userInfo && userStore.userInfo.avatar"
            :src="userStore.userInfo.avatar"
            class="avatar-img"
            mode="aspectFill"
          />
          <text v-else class="avatar-placeholder">👤</text>
        </view>
        <text class="nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</text>
        <text class="bio">{{ userStore.userInfo?.bio || '这个人很懒，什么都没写...' }}</text>
      </view>

      <view class="stats-card card">
        <view class="stat-item">
          <text class="stat-value">{{ stats.articleCount || 0 }}</text>
          <text class="stat-label">文章</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalViews || 0 }}</text>
          <text class="stat-label">浏览</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalLikes || 0 }}</text>
          <text class="stat-label">点赞</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalFavorites || 0 }}</text>
          <text class="stat-label">收藏</text>
        </view>
      </view>

      <view class="menu-group card">
        <view class="menu-item" @click="goPage('/pages/my-articles/my-articles')">
          <text class="menu-icon">📝</text>
          <text class="menu-title">我的文章</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goPage('/pages/my-likes/my-likes')">
          <text class="menu-icon">👍</text>
          <text class="menu-title">我的点赞</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item menu-item--last" @click="goPage('/pages/my-favorites/my-favorites')">
          <text class="menu-icon">⭐</text>
          <text class="menu-title">我的收藏</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="logout-btn">
        <button class="logout-button" @click="handleLogout">退出登录</button>
      </view>
    </template>

    <!-- 未登录状态 -->
    <template v-else>
      <view class="not-logged">
        <text class="not-logged-icon">👤</text>
        <text class="not-logged-text">请先登录</text>
        <button class="login-btn" @click="goLogin">去登录</button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { getProfile, getUserStats } from '@/api/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const stats = ref({})

async function loadProfileData() {
  if (!userStore.isLogin) return
  // 先拉取统计
  try {
    const res = await getUserStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (e) {
    // ignore
  }
  // 再拉取个人资料（包含 user 和 stats，覆盖更新）
  try {
    const profileRes = await getProfile()
    if (profileRes.data && profileRes.data.user) {
      userStore.updateUserInfo(profileRes.data.user)
    }
    if (profileRes.data && profileRes.data.stats) {
      stats.value = profileRes.data.stats
    }
  } catch (e) {
    // ignore
  }
}

function goPage(url) {
  uni.navigateTo({ url })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#ea6f5a',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        stats.value = {}
        uni.switchTab({ url: '/pages/home/home' })
      }
    }
  })
}

onShow(() => {
  loadProfileData()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
  padding-bottom: 40rpx;
}

.card {
  background: var(--jianshu-white);
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
}

.user-info {
  text-align: center;
  padding: 48rpx 32rpx;
}

.avatar-wrapper {
  width: 144rpx;
  height: 144rpx;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 24rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  font-size: 80rpx;
  color: #ccc;
  line-height: 1;
}

.nickname {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: var(--jianshu-text);
  margin-bottom: 12rpx;
}

.bio {
  display: block;
  font-size: 26rpx;
  color: var(--jianshu-text-light);
  line-height: 1.5;
}

.stats-card {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  text-align: center;
  padding: 32rpx 24rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--jianshu-text);
  line-height: 1.2;
}

.stat-label {
  font-size: 24rpx;
  color: var(--jianshu-text-light);
}

.menu-group {
  padding: 0;
  overflow: hidden;
}

.menu-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid var(--jianshu-border);
}

.menu-item--last {
  border-bottom: none;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 24rpx;
  line-height: 1;
}

.menu-title {
  flex: 1;
  font-size: 30rpx;
  color: var(--jianshu-text);
}

.menu-arrow {
  font-size: 40rpx;
  color: var(--jianshu-text-light);
  line-height: 1;
}

.logout-btn {
  padding: 24rpx 48rpx;
}

.logout-button {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: var(--jianshu-white);
  color: var(--jianshu-red);
  border: 2rpx solid var(--jianshu-red);
  border-radius: 44rpx;
  font-size: 32rpx;
  padding: 0;
}

.logout-button::after {
  border: none;
}

.not-logged {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 32rpx;
}

.not-logged-icon {
  font-size: 160rpx;
  color: #ccc;
  line-height: 1;
}

.not-logged-text {
  font-size: 32rpx;
  color: var(--jianshu-text-light);
}

.login-btn {
  width: 280rpx;
  height: 88rpx;
  line-height: 88rpx;
  background-color: var(--jianshu-red);
  color: var(--jianshu-white);
  border-radius: 44rpx;
  font-size: 32rpx;
  border: none;
  padding: 0;
}

.login-btn::after {
  border: none;
}
</style>
