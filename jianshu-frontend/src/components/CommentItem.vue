<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <img v-if="comment.user?.avatar" :src="comment.user.avatar" class="avatar-img" />
      <van-icon v-else name="user-o" size="32" color="#ccc" />
    </div>
    <div class="comment-body">
      <div class="comment-header">
        <span class="nickname">{{ comment.user?.nickname || comment.user?.username || '匿名' }}</span>
        <span class="time">{{ formatTime(comment.createdAt) }}</span>
      </div>
      <p class="comment-content">{{ comment.content }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  comment: {
    type: Object,
    required: true
  }
})

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--jianshu-border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.nickname {
  font-size: 14px;
  color: var(--jianshu-red);
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: var(--jianshu-text-light);
}

.comment-content {
  font-size: 14px;
  color: var(--jianshu-text);
  line-height: 1.6;
  word-break: break-word;
}
</style>
