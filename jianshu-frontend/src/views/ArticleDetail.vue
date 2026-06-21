<template>
  <div class="article-detail">
    <NavBar title="文章详情" />
    <template v-if="article">
      <div class="article-header">
        <h1 class="detail-title">{{ article.title }}</h1>
        <div class="author-row">
          <img v-if="article.author?.avatar" :src="article.author.avatar" class="author-avatar" />
          <van-icon v-else name="user-o" size="36" color="#ccc" class="author-avatar-placeholder" />
          <div class="author-info">
            <span class="author-name">{{ article.author?.nickname || article.author?.username || '匿名' }}</span>
            <span class="publish-time">{{ formatTime(article.createdAt) }}</span>
          </div>
        </div>
      </div>
      <div class="article-body">
        <div class="article-content">{{ article.content }}</div>
      </div>
      <div class="stats-row">
        <span class="stat"><van-icon name="eye-o" /> {{ article.viewCount || 0 }}</span>
        <span class="stat"><van-icon name="like-o" /> {{ article.likeCount || 0 }}</span>
        <span class="stat"><van-icon name="star-o" /> {{ article.favoriteCount || 0 }}</span>
      </div>
      <div class="action-row">
        <div class="action-btn" :class="{ active: isLiked }" @click="handleLike">
          <van-icon :name="isLiked ? 'like' : 'like-o'" />
          <span>{{ isLiked ? '已赞' : '点赞' }}</span>
        </div>
        <div class="action-btn" :class="{ active: isFavorited }" @click="handleFavorite">
          <van-icon :name="isFavorited ? 'star' : 'star-o'" />
          <span>{{ isFavorited ? '已藏' : '收藏' }}</span>
        </div>
      </div>
    </template>
    <div v-else class="loading-center">
      <van-loading size="36px" vertical>加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const isLiked = ref(false)
const isFavorited = ref(false)

async function loadArticle(id) {
  article.value = null
  try {
    const res = await getArticleDetail(id)
    article.value = res.data
    isLiked.value = res.data.liked || false
    isFavorited.value = res.data.favorited || false
  } catch {
    showToast('文章加载失败')
  }
}

onMounted(() => {
  loadArticle(route.params.id)
})

// 路由参数变化时重新加载
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadArticle(newId)
  }
})

function requireLogin() {
  if (!userStore.isLogin) {
    showToast('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return true
  }
  return false
}

async function handleLike() {
  if (requireLogin()) return
  try {
    const res = await toggleLike(route.params.id)
    const wasLiked = isLiked.value
    isLiked.value = res.data?.liked ?? !wasLiked
    if (article.value) {
      article.value.likeCount = res.data?.likeCount ?? (article.value.likeCount + (isLiked.value ? 1 : -1))
    }
  } catch {
    // error handled by interceptor
  }
}

async function handleFavorite() {
  if (requireLogin()) return
  try {
    const res = await toggleFavorite(route.params.id)
    const wasFavorited = isFavorited.value
    isFavorited.value = res.data?.favorited ?? !wasFavorited
    if (article.value) {
      article.value.favoriteCount = res.data?.favoriteCount ?? (article.value.favoriteCount + (isFavorited.value ? 1 : -1))
    }
  } catch {
    // error handled by interceptor
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
  padding-bottom: 20px;
}

.article-header {
  padding: 16px;
  border-bottom: 1px solid var(--jianshu-border);
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 14px;
  line-height: 1.4;
  color: var(--jianshu-text);
}

.author-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  color: var(--jianshu-red);
  font-weight: 500;
}

.publish-time {
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.article-body {
  padding: 16px;
}

.article-content {
  font-size: 15px;
  line-height: 1.8;
  color: #2f2f2f;
  white-space: pre-wrap;
  word-break: break-word;
}

.stats-row {
  display: flex;
  gap: 20px;
  padding: 12px 16px;
  border-top: 1px solid var(--jianshu-border);
  font-size: 13px;
  color: var(--jianshu-text-light);
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-row {
  display: flex;
  justify-content: space-around;
  padding: 14px 16px;
  border-top: 1px solid var(--jianshu-border);
  border-bottom: 8px solid var(--jianshu-bg);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--jianshu-text-light);
  cursor: pointer;
}

.action-btn .van-icon {
  font-size: 22px;
}

.action-btn.active {
  color: var(--jianshu-red);
}

.loading-center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>
