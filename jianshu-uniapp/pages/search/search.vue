<template>
  <view class="search-page">
    <!-- 顶部导航栏 -->
    <NavBar title="搜索" />

    <!-- 搜索输入栏 -->
    <view class="search-bar">
      <view class="search-bar__input-wrap">
        <text class="search-bar__icon">🔍</text>
        <input
          class="search-bar__input"
          type="text"
          v-model="keyword"
          placeholder="搜索文章"
          placeholder-class="search-bar__placeholder"
          confirm-type="search"
          :focus="autoFocus"
          @confirm="handleSearch"
        />
      </view>
      <text class="search-bar__btn" @click="handleSearch">搜索</text>
      <text class="search-bar__cancel" @click="onCancel">取消</text>
    </view>

    <!-- 热门搜索标签云 -->
    <view class="hot-tags" v-if="!searched">
      <text class="hot-tags__title">热门搜索</text>
      <view class="hot-tags__list">
        <view
          v-for="tag in hotTags"
          :key="tag"
          class="hot-tags__tag"
          @click="searchByTag(tag)"
        >
          <text class="hot-tags__tag-text">{{ tag }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="search-results" v-if="searched">
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
        text="没有找到相关文章"
        icon="🔍"
      />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'
import NavBar from '@/components/NavBar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const keyword = ref('')
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const searched = ref(false)
const page = ref(1)
const autoFocus = ref(true)

const hotTags = ['前端', 'Vue', 'React', 'Java', 'Python', '生活', '读书', '职场']

// 拉取一页数据
async function fetchPage() {
  try {
    const res = await getArticleList({ page: page.value, size: 10, keyword: keyword.value })
    const records = res.data && res.data.records || []
    articleList.value.push(...records)
    const total = res.data && res.data.total || 0
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

// 上拉加载更多
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

function handleSearch() {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }
  searched.value = true
  articleList.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  fetchPage()
}

function searchByTag(tag) {
  keyword.value = tag
  handleSearch()
}

function onCancel() {
  if (searched.value) {
    // 已搜索：清除搜索状态，回到热门搜索
    keyword.value = ''
    searched.value = false
    articleList.value = []
    finished.value = false
    loading.value = false
    page.value = 1
  } else {
    // 未搜索：返回上一页
    uni.navigateBack({ delta: 1 }).catch(() => {})
  }
}

function goDetail(article) {
  uni.navigateTo({
    url: `/pages/article-detail/article-detail?id=${article.id}`
  })
}

onPullDownRefresh(async () => {
  if (!searched.value) {
    uni.stopPullDownRefresh()
    return
  }
  refreshing.value = true
  await onRefresh()
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (searched.value) {
    loadMore()
  }
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
}

.search-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 24rpx;
  background-color: var(--jianshu-white);
  border-bottom: 1rpx solid var(--jianshu-border);
}

.search-bar__input-wrap {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 64rpx;
  padding: 0 20rpx;
  background-color: var(--jianshu-bg);
  border-radius: 32rpx;
}

.search-bar__icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.search-bar__input {
  flex: 1;
  font-size: 28rpx;
  color: var(--jianshu-text);
}

.search-bar__placeholder {
  color: var(--jianshu-text-light);
  font-size: 28rpx;
}

.search-bar__btn {
  margin-left: 20rpx;
  padding: 0 8rpx;
  font-size: 28rpx;
  color: var(--jianshu-red);
  font-weight: 500;
}

.search-bar__cancel {
  margin-left: 16rpx;
  padding: 0 8rpx;
  font-size: 28rpx;
  color: var(--jianshu-text-light);
}

.hot-tags {
  padding: 32rpx 24rpx;
}

.hot-tags__title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--jianshu-text);
  margin-bottom: 24rpx;
}

.hot-tags__list {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.hot-tags__tag {
  padding: 12rpx 28rpx;
  margin: 0 16rpx 16rpx 0;
  background-color: var(--jianshu-red-light);
  border-radius: 28rpx;
  border: 1rpx solid var(--jianshu-red);
}

.hot-tags__tag-text {
  font-size: 26rpx;
  color: var(--jianshu-red);
}

.search-results {
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
