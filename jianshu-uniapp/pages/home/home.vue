<template>
  <view class="home-page">
    <!-- 自定义顶部标题栏 -->
    <view class="home-header">
      <view class="home-header__status" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="home-header__content">
        <text class="home-header__title">简书问答</text>
        <view class="home-header__actions">
          <view class="home-header__write" @click="goEdit">
            <text class="home-header__write-text">写博文</text>
          </view>
          <view class="home-header__search" @click="goSearch">
            <text class="home-header__search-icon">🔍</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 占位，防止内容被固定头部遮挡 -->
    <view class="home-header__placeholder" :style="{ height: (statusBarHeight + 44) + 'px' }"></view>

    <!-- 文章列表 -->
    <view class="home-list">
      <ArticleCard
        v-for="article in articleList"
        :key="article.id"
        :article="article"
        @click="goDetail(article)"
      />

      <view v-if="loading" class="list-tip">
        <text class="list-tip__text">加载中...</text>
      </view>
      <view v-else-if="finished && articleList.length" class="list-tip">
        <text class="list-tip__text">没有更多了</text>
      </view>

      <EmptyState
        v-if="!loading && !refreshing && !articleList.length"
        text="暂无文章"
        icon="📝"
      />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

const statusBarHeight = ref(20)
try {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
} catch (e) {
  statusBarHeight.value = 20
}

// 拉取一页数据（不加 guard，由调用方控制）
async function fetchPage() {
  try {
    const res = await getArticleList({ page: page.value, size: 10 })
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

// 上拉加载更多 / 首次加载
function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  fetchPage()
}

// 下拉刷新
async function onRefresh() {
  page.value = 1
  articleList.value = []
  finished.value = false
  loading.value = true
  await fetchPage()
  refreshing.value = false
}

function goDetail(article) {
  uni.navigateTo({
    url: `/pages/article-detail/article-detail?id=${article.id}`
  })
}

function goSearch() {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

function goEdit() {
  if (!userStore.isLogin) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/edit-article/edit-article' })
}

// 页面显示时，若列表为空则自动加载第一页
onShow(() => {
  if (!articleList.value.length) {
    loadMore()
  }
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
.home-page {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
}

.home-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: var(--jianshu-white);
  border-bottom: 1rpx solid var(--jianshu-border);
}

.home-header__status {
  width: 100%;
}

.home-header__content {
  height: 88rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.home-header__title {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--jianshu-red);
}

.home-header__actions {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.home-header__write {
  padding: 12rpx 28rpx;
  background-color: var(--jianshu-red);
  border-radius: 32rpx;
}

.home-header__write-text {
  font-size: 26rpx;
  color: var(--jianshu-white);
  font-weight: 500;
}

.home-header__search {
  margin-left: 24rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.home-header__search-icon {
  font-size: 36rpx;
}

.home-header__placeholder {
  width: 100%;
}

.home-list {
  padding-bottom: 40rpx;
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
