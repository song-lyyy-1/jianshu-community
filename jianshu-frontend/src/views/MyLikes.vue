<template>
  <div class="my-likes-page">
    <NavBar title="我的点赞" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <ArticleCard
          v-for="item in articleList"
          :key="item.id"
          :article="item.article || item"
          @click="goDetail(item.article?.id || item.id)"
        />
        <EmptyState v-if="!loading && !refreshing && !articleList.length" text="还没有点赞过文章" icon="like-o" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyLikedArticles } from '@/api/like'
import ArticleCard from '@/components/ArticleCard.vue'
import NavBar from '@/components/NavBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

async function onLoad() {
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
.my-likes-page {
  min-height: 100vh;
  background: var(--jianshu-bg);
  padding: 0 12px 12px;
}
</style>
