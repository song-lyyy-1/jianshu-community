<template>
  <view class="article-detail">
    <NavBar title="文章详情" />

    <template v-if="article">
      <view class="article-header">
        <text class="detail-title">{{ article.title }}</text>
        <view class="author-row">
          <image
            v-if="article.author && article.author.avatar"
            :src="article.author.avatar"
            class="author-avatar"
            mode="aspectFill"
          />
          <view v-else class="author-avatar-placeholder">
            <text class="author-avatar-placeholder__text">👤</text>
          </view>
          <view class="author-info">
            <text class="author-name">{{ article.author && (article.author.nickname || article.author.username) || '匿名' }}</text>
            <text class="publish-time">{{ formatTime(article.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view class="article-body">
        <text class="article-content">{{ article.content }}</text>
      </view>

      <view class="stats-row">
        <view class="stat">
          <text class="stat__icon">👁</text>
          <text class="stat__num">{{ article.viewCount || 0 }}</text>
        </view>
        <view class="stat">
          <text class="stat__icon">❤</text>
          <text class="stat__num">{{ article.likeCount || 0 }}</text>
        </view>
        <view class="stat">
          <text class="stat__icon">⭐</text>
          <text class="stat__num">{{ article.favoriteCount || 0 }}</text>
        </view>
      </view>

      <view class="action-row">
        <view class="action-btn" :class="{ active: isLiked, 'active-like': isLiked }" @click="handleLike">
          <text class="action-btn__icon">{{ isLiked ? '❤' : '🤍' }}</text>
          <text class="action-btn__text">{{ isLiked ? '已赞' : '点赞' }}</text>
        </view>
        <view class="action-btn" :class="{ active: isFavorited, 'active-fav': isFavorited }" @click="handleFavorite">
          <text class="action-btn__icon">{{ isFavorited ? '⭐' : '☆' }}</text>
          <text class="action-btn__text">{{ isFavorited ? '已藏' : '收藏' }}</text>
        </view>
      </view>
    </template>

    <view v-else class="loading-center">
      <text class="loading-center__text">加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getArticleDetail } from '@/api/article'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const article = ref(null)
const isLiked = ref(false)
const isFavorited = ref(false)

let currentId = ''

async function loadArticle(id) {
  if (!id) return
  article.value = null
  try {
    const res = await getArticleDetail(id)
    article.value = res.data
    isLiked.value = res.data.liked || false
    isFavorited.value = res.data.favorited || false
  } catch (e) {
    uni.showToast({ title: '文章加载失败', icon: 'none' })
  }
}

onLoad((options) => {
  currentId = options.id || ''
  loadArticle(currentId)
})

// 页面再次显示时，检查当前 id 是否变化（路由参数变化重新加载）
onShow(() => {
  if (currentId && article.value && article.value.id != currentId) {
    loadArticle(currentId)
  }
})

function requireLogin() {
  if (!userStore.isLogin) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/login/login' })
    return true
  }
  return false
}

async function handleLike() {
  if (requireLogin()) return
  try {
    const res = await toggleLike(currentId)
    const wasLiked = isLiked.value
    isLiked.value = res.data?.liked ?? !wasLiked
    if (article.value) {
      article.value.likeCount = res.data?.likeCount ?? (article.value.likeCount + (isLiked.value ? 1 : -1))
    }
  } catch (e) {
    // 错误由拦截器处理
  }
}

async function handleFavorite() {
  if (requireLogin()) return
  try {
    const res = await toggleFavorite(currentId)
    const wasFavorited = isFavorited.value
    isFavorited.value = res.data?.favorited ?? !wasFavorited
    if (article.value) {
      article.value.favoriteCount = res.data?.favoriteCount ?? (article.value.favoriteCount + (isFavorited.value ? 1 : -1))
    }
  } catch (e) {
    // 错误由拦截器处理
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
</script>

<style scoped>
.article-detail {
  background: var(--jianshu-white);
  min-height: 100vh;
  padding-bottom: 40rpx;
}

.article-header {
  padding: 32rpx;
  border-bottom: 1rpx solid var(--jianshu-border);
}

.detail-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 28rpx;
  line-height: 1.4;
  color: var(--jianshu-text);
}

.author-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.author-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: var(--jianshu-bg);
}

.author-avatar-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: var(--jianshu-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.author-avatar-placeholder__text {
  font-size: 40rpx;
  color: var(--jianshu-text-light);
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 30rpx;
  color: var(--jianshu-red);
  font-weight: 500;
  margin-bottom: 4rpx;
}

.publish-time {
  font-size: 24rpx;
  color: var(--jianshu-text-light);
}

.article-body {
  padding: 32rpx;
}

.article-content {
  font-size: 30rpx;
  line-height: 1.8;
  color: #2f2f2f;
  white-space: pre-wrap;
  word-break: break-word;
}

.stats-row {
  display: flex;
  flex-direction: row;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid var(--jianshu-border);
}

.stat {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-right: 40rpx;
}

.stat__icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.stat__num {
  font-size: 26rpx;
  color: var(--jianshu-text-light);
}

.action-row {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  padding: 28rpx 32rpx;
  border-top: 1rpx solid var(--jianshu-border);
  border-bottom: 16rpx solid var(--jianshu-bg);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-btn__icon {
  font-size: 44rpx;
  margin-bottom: 8rpx;
  color: var(--jianshu-text-light);
}

.action-btn__text {
  font-size: 24rpx;
  color: var(--jianshu-text-light);
}

.action-btn.active-like .action-btn__icon,
.action-btn.active-like .action-btn__text {
  color: var(--jianshu-red);
}

.action-btn.active-fav .action-btn__icon,
.action-btn.active-fav .action-btn__text {
  color: #ffc107;
}

.loading-center {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.loading-center__text {
  font-size: 28rpx;
  color: var(--jianshu-text-light);
}
</style>
