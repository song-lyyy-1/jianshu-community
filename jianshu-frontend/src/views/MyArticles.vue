<template>
  <div class="my-articles-page">
    <NavBar title="我的文章" />
    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky offset-top="46">
      <van-tab title="全部" />
      <van-tab title="已发布" />
      <van-tab title="草稿" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          v-for="article in articleList"
          :key="article.id"
          class="article-row"
        >
          <div class="article-info" @click="goEdit(article.id)">
            <h4 class="article-title">{{ article.title || '无标题' }}</h4>
            <div class="article-meta">
              <van-tag :type="article.status === 1 ? 'success' : 'default'" size="medium">
                {{ article.status === 1 ? '已发布' : '草稿' }}
              </van-tag>
              <span class="article-time">{{ formatDate(article.createdAt) }}</span>
            </div>
          </div>
          <div class="article-actions">
            <van-icon name="edit" size="20" color="#666" @click="goEdit(article.id)" />
            <van-icon name="delete-o" size="20" color="#ee0a24" @click="handleDelete(article)" />
          </div>
        </div>
        <EmptyState v-if="!loading && !refreshing && !articleList.length" text="还没有写过文章" icon="notes-o" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { getMyArticles, deleteArticle } from '@/api/article'
import { showDialog, showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const activeTab = ref(0)
const needRefresh = ref(false)

// 从编辑页返回时刷新
onActivated(() => {
  if (needRefresh.value) {
    needRefresh.value = false
    onRefresh()
  }
})

function getStatusParam() {
  if (activeTab.value === 1) return 1
  if (activeTab.value === 2) return 0
  return undefined
}

async function onLoad() {
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

function onTabChange() {
  page.value = 1
  articleList.value = []
  finished.value = false
  // 不需要手动设 loading 和调用 onLoad，van-list 会自动触发
}

function goEdit(id) {
  needRefresh.value = true
  router.push(`/edit/${id}`)
}

function handleDelete(article) {
  showDialog({
    title: '确认删除',
    message: `确定要删除「${article.title || '无标题'}」吗？`
  }).then(async () => {
    try {
      await deleteArticle(article.id)
      showToast('删除成功')
      articleList.value = articleList.value.filter(a => a.id !== article.id)
    } catch {
      // error handled by interceptor
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
</script>

<style scoped>
.my-articles-page {
  min-height: 100vh;
  background: var(--jianshu-bg);
}

.article-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: var(--jianshu-white);
  margin-bottom: 1px;
}

.article-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.article-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--jianshu-text);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.article-time {
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.article-actions {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
  padding-left: 12px;
}
</style>
