<template>
  <div class="article-card" @click="$emit('click')">
    <div class="card-left-bar"></div>
    <div class="card-body">
      <h3 class="article-title">{{ article.title }}</h3>
      <p class="article-summary">{{ article.summary || article.content?.substring(0, 100) }}</p>
      <div class="article-meta">
        <div class="author-info">
          <img v-if="article.author?.avatar" :src="article.author.avatar" class="author-avatar" />
          <van-icon v-else name="user-o" class="author-avatar-placeholder" />
          <span class="author-name">{{ article.author?.nickname || article.author?.username || '匿名' }}</span>
        </div>
        <div class="stats">
          <span class="stat-item"><van-icon name="eye-o" /> {{ article.viewCount || 0 }}</span>
          <span class="stat-item"><van-icon name="like-o" /> {{ article.likeCount || 0 }}</span>
          <span class="stat-item"><van-icon name="star-o" /> {{ article.favoriteCount || 0 }}</span>
          <span class="stat-item"><van-icon name="chat-o" /> {{ article.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  article: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<style scoped>
.article-card {
  display: flex;
  background: var(--jianshu-white);
  border-radius: 8px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.article-card:active {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.card-left-bar {
  width: 4px;
  background: var(--jianshu-red);
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  padding: 14px 16px;
}

.article-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--jianshu-text);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  font-size: 13px;
  color: var(--jianshu-text-light);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-placeholder {
  font-size: 22px;
  color: var(--jianshu-text-light);
}

.author-name {
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
}
</style>
