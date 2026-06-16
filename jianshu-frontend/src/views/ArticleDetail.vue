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
        <MarkdownRender :content="article.content" />
      </div>
      <div class="stats-row">
        <span class="stat"><van-icon name="eye-o" /> {{ article.viewCount || 0 }}</span>
        <span class="stat"><van-icon name="like-o" /> {{ article.likeCount || 0 }}</span>
        <span class="stat"><van-icon name="star-o" /> {{ article.favoriteCount || 0 }}</span>
        <span class="stat"><van-icon name="chat-o" /> {{ article.commentCount || 0 }}</span>
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
        <div class="action-btn" @click="scrollToComment">
          <van-icon name="chat-o" />
          <span>评论</span>
        </div>
      </div>
      <div class="comment-section" ref="commentSectionRef">
        <h3 class="comment-title">评论 ({{ article.commentCount || 0 }})</h3>
        <div class="comment-input">
          <van-field
            v-model="commentText"
            placeholder="写下你的评论..."
            rows="2"
            type="textarea"
            autosize
          />
          <van-button type="primary" size="small" class="send-btn" @click="handleComment">发送</van-button>
        </div>
        <div class="comment-list">
          <CommentItem
            v-for="comment in commentList"
            :key="comment.id"
            :comment="comment"
          />
          <EmptyState v-if="!commentList.length" text="暂无评论，快来抢沙发" icon="chat-o" />
        </div>
      </div>
    </template>
    <div v-else class="loading-center">
      <van-loading size="36px" vertical>加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { getCommentList, addComment } from '@/api/comment'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import MarkdownRender from '@/components/MarkdownRender.vue'
import CommentItem from '@/components/CommentItem.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const article = ref(null)
const isLiked = ref(false)
const isFavorited = ref(false)
const commentList = ref([])
const commentText = ref('')
const commentSectionRef = ref(null)

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getArticleDetail(id)
    article.value = res.data
    isLiked.value = res.data.liked || false
    isFavorited.value = res.data.favorited || false
    loadComments()
  } catch {
    showToast('文章加载失败')
  }
})

async function loadComments() {
  try {
    const res = await getCommentList(route.params.id)
    commentList.value = res.data || []
  } catch {
    // ignore
  }
}

async function handleLike() {
  try {
    const res = await toggleLike(route.params.id)
    isLiked.value = res.data?.liked ?? !isLiked.value
    if (article.value) {
      article.value.likeCount = res.data?.likeCount ?? article.value.likeCount
    }
  } catch {
    // error handled by interceptor
  }
}

async function handleFavorite() {
  try {
    const res = await toggleFavorite(route.params.id)
    isFavorited.value = res.data?.favorited ?? !isFavorited.value
    if (article.value) {
      article.value.favoriteCount = res.data?.favoriteCount ?? article.value.favoriteCount
    }
  } catch {
    // error handled by interceptor
  }
}

async function handleComment() {
  if (!commentText.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  try {
    await addComment({ articleId: Number(route.params.id), content: commentText.value })
    commentText.value = ''
    showToast('评论成功')
    loadComments()
  } catch {
    // error handled by interceptor
  }
}

function scrollToComment() {
  commentSectionRef.value?.scrollIntoView({ behavior: 'smooth' })
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

.comment-section {
  padding: 16px;
}

.comment-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
}

.comment-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  margin-bottom: 16px;
}

.comment-input .van-field {
  flex: 1;
  background: #f7f8fa;
  border-radius: 8px;
}

.send-btn {
  flex-shrink: 0;
  background: var(--jianshu-red);
  border-color: var(--jianshu-red);
}

.comment-list {
  margin-top: 8px;
}

.loading-center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>
