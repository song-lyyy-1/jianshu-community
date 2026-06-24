<template>
  <view class="my-articles-page">
    <NavBar title="我的文章" />

    <!-- Tab 切换 -->
    <view class="tabs">
      <view
        v-for="(tab, index) in tabs"
        :key="tab.key"
        class="tabs__item"
        :class="{ 'tabs__item--active': activeTab === index }"
        @click="onTabChange(index)"
      >
        <text class="tabs__text">{{ tab.label }}</text>
        <view v-if="activeTab === index" class="tabs__line"></view>
      </view>
    </view>

    <!-- 文章列表 -->
    <view class="article-list">
      <view
        v-for="article in articleList"
        :key="article.id"
        class="article-row"
      >
        <view class="article-info" @click="goEdit(article.id)">
          <text class="article-title">{{ article.title || '无标题' }}</text>
          <view class="article-meta">
            <text
              class="article-tag"
              :class="article.status === 1 ? 'article-tag--published' : 'article-tag--draft'"
            >
              {{ article.status === 1 ? '已发布' : '草稿' }}
            </text>
            <text class="article-time">{{ formatDate(article.createdAt) }}</text>
          </view>
        </view>
        <view class="article-actions">
          <text class="article-action" @click="goEdit(article.id)">✏️</text>
          <text class="article-action article-action--danger" @click="handleDelete(article)">🗑️</text>
        </view>
      </view>

      <view v-if="loading" class="list-tip">
        <text class="list-tip__text">加载中...</text>
      </view>
      <view v-else-if="finished && articleList.length" class="list-tip">
        <text class="list-tip__text">没有更多了</text>
      </view>

      <EmptyState
        v-if="!loading && !refreshing && !articleList.length"
        text="还没有写过文章"
        icon="📝"
      />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getMyArticles, deleteArticle } from '@/api/article'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'published', label: '已发布' },
  { key: 'draft', label: '草稿' }
]

const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const activeTab = ref(0)
const inited = ref(false)

function getStatusParam() {
  if (activeTab.value === 1) return 1
  if (activeTab.value === 2) return 0
  return undefined
}

async function fetchPage() {
  try {
    const params = { page: page.value, size: 10 }
    const status = getStatusParam()
    if (status !== undefined) params.status = status
    const res = await getMyArticles(params)
    const records = res.data?.records || []
    articleList.value.push(...records)
    const total = res.data?.total || 0
    if (articleList.value.length >= total || records.length < 10) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  fetchPage()
}

async function onRefresh() {
  page.value = 1
  articleList.value = []
  finished.value = false
  loading.value = true
  await fetchPage()
  refreshing.value = false
}

function onTabChange(index) {
  if (activeTab.value === index) return
  activeTab.value = index
  page.value = 1
  articleList.value = []
  finished.value = false
  loading.value = true
  fetchPage()
}

function goEdit(id) {
  uni.navigateTo({
    url: `/pages/edit-article/edit-article?id=${id}`
  })
}

function handleDelete(article) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${article.title || '无标题'}」吗？`,
    confirmColor: '#ea6f5a',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteArticle(article.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        articleList.value = articleList.value.filter(a => a.id !== article.id)
      } catch (e) {
        // 错误由拦截器处理
      }
    }
  })
}

function formatDate(time) {
  if (!time) return ''
  const date = new Date(time)
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${m}-${d}`
}

onLoad(() => {
  // 登录检查
  if (!userStore.isLogin) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  inited.value = true
  loadMore()
})

onShow(() => {
  if (!inited.value) return
  // 从编辑页返回时刷新数据
  onRefresh()
})

onPullDownRefresh(async () => {
  refreshing.value = true
  await onRefresh()
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  loadMore()
})
</script>

<style scoped>
.my-articles-page {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
}

.tabs {
  display: flex;
  flex-direction: row;
  background-color: var(--jianshu-white);
  border-bottom: 1rpx solid var(--jianshu-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.tabs__item {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
}

.tabs__text {
  font-size: 28rpx;
  color: var(--jianshu-text-light);
}

.tabs__item--active .tabs__text {
  color: var(--jianshu-red);
  font-weight: 600;
}

.tabs__line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background-color: var(--jianshu-red);
  border-radius: 2rpx;
}

.article-list {
  padding-bottom: 40rpx;
}

.article-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  background-color: var(--jianshu-white);
  margin-bottom: 2rpx;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  display: block;
  font-size: 30rpx;
  font-weight: 500;
  color: var(--jianshu-text);
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.article-tag {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  margin-right: 20rpx;
}

.article-tag--published {
  background-color: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.article-tag--draft {
  background-color: rgba(150, 150, 150, 0.1);
  color: var(--jianshu-text-light);
}

.article-time {
  font-size: 24rpx;
  color: var(--jianshu-text-light);
}

.article-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;
  padding-left: 24rpx;
}

.article-action {
  font-size: 36rpx;
  padding: 8rpx 12rpx;
  margin-left: 16rpx;
  line-height: 1;
}

.article-action--danger {
  opacity: 0.9;
}

.list-tip {
  padding: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.list-tip__text {
  font-size: 26rpx;
  color: var(--jianshu-text-light);
}
</style>
