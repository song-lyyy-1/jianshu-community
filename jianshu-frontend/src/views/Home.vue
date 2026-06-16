<template>
  <div class="home-page">
    <div class="home-header">
      <h1 class="home-title">简书问答</h1>
      <van-icon name="search" size="22" color="#333" @click="router.push('/search')" class="search-icon" />
    </div>
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
        <EmptyState v-if="!loading && !refreshing && !articleList.length" text="暂无文章" icon="notes-o" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

async function onLoad() {
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

function goDetail(id) {
  router.push(`/article/${id}`)
}
</script>

<style scoped>
.home-page {
  padding: 0 12px 12px;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 4px 12px;
}

.home-title {
  font-size: 20px;
  color: var(--jianshu-red);
  font-weight: 700;
}

.search-icon {
  cursor: pointer;
}
</style>
