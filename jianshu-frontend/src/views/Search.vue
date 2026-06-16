<template>
  <div class="search-page">
    <van-search
      v-model="keyword"
      placeholder="搜索文章"
      show-action
      autofocus
      @search="handleSearch"
      @cancel="router.back()"
    />
    <div class="hot-tags" v-if="!searched">
      <div class="hot-title">热门搜索</div>
      <div class="tag-list">
        <van-tag
          v-for="tag in hotTags"
          :key="tag"
          plain
          type="primary"
          size="medium"
          class="hot-tag"
          @click="searchByTag(tag)"
        >
          {{ tag }}
        </van-tag>
      </div>
    </div>
    <div class="search-results" v-if="searched">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <ArticleCard
            v-for="article in articleList"
            :key="article.id"
            :article="article"
            @click="goDetail(article.id)"
          />
          <EmptyState v-if="!loading && !articleList.length" text="没有找到相关文章" icon="search" />
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const keyword = ref('')
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const searched = ref(false)
const page = ref(1)

const hotTags = ['前端', 'Vue', 'React', 'Java', 'Python', '生活', '读书', '职场']

async function onLoad() {
  try {
    const res = await getArticleList({ page: page.value, size: 10, keyword: keyword.value })
    const records = res.data?.records || []
    articleList.value.push(...records)
    const total = res.data?.total || 0
    if (articleList.value.length >= total || records.length < 10) {
      finished.value = true
    } else {
      page.value++
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  page.value = 1
  articleList.value = []
  finished.value = false
  loading.value = true
  await onLoad()
  refreshing.value = false
}

function handleSearch() {
  if (!keyword.value.trim()) return
  searched.value = true
  articleList.value = []
  page.value = 1
  finished.value = false
  // van-list 渲染后会自动触发 @load，无需手动调用
}

function searchByTag(tag) {
  keyword.value = tag
  handleSearch()
}

function goDetail(id) {
  router.push(`/article/${id}`)
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--jianshu-bg);
}

.hot-tags {
  padding: 16px;
}

.hot-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--jianshu-text);
  margin-bottom: 12px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
}

.search-results {
  padding: 0 12px 12px;
}
</style>
