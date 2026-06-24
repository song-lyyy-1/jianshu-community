<template>
  <view class="my-likes-page">
    <NavBar title="我的点赞" />

    <view class="like-list">
      <ArticleCard
        v-for="item in articleList"
        :key="item.id"
        :article="getArticle(item)"
        @click="goDetail(item)"
      />

      <view v-if="loading" class="list-tip">
        <text class="list-tip__text">加载中...</text>
      </view>
      <view v-else-if="finished && articleList.length" class="list-tip">
        <text class="list-tip__text">没有更多了</text>
      </view>

      <EmptyState
        v-if="!loading && !refreshing && !articleList.length"
        text="还没有点赞过文章"
        icon="❤️"
      />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getMyLikedArticles } from '@/api/like'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import NavBar from '@/components/NavBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const inited = ref(false)

function getArticle(item) {
  return item.article || item
}

function getArticleId(item) {
  return item.article?.id || item.id
}

async function fetchPage() {
  try {
    const res = await getMyLikedArticles({ page: page.value, size: 10 })
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

function goDetail(item) {
  const id = getArticleId(item)
  if (!id) return
  uni.navigateTo({
    url: `/pages/article-detail/article-detail?id=${id}`
  })
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
  // 从详情页返回时刷新数据
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
.my-likes-page {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
  padding-bottom: 40rpx;
}

.like-list {
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
