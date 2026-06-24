<template>
  <view class="article-card" @click="onClick">
    <view class="article-card__bar"></view>
    <view class="article-card__body">
      <text class="article-card__title">{{ article.title || '无标题' }}</text>
      <text class="article-card__summary">{{ article.summary || article.content || '' }}</text>
      <view class="article-card__footer">
        <view class="article-card__author">
          <image
            v-if="article.author && article.author.avatar"
            class="article-card__avatar"
            :src="article.author.avatar"
            mode="aspectFill"
          />
          <view v-else class="article-card__avatar article-card__avatar--default">
            <text class="article-card__avatar-text">{{ (article.author && (article.author.nickname || article.author.username) || '匿名').charAt(0) }}</text>
          </view>
          <text class="article-card__name">{{ article.author && (article.author.nickname || article.author.username) || '匿名用户' }}</text>
        </view>
        <view class="article-card__stats">
          <text class="article-card__stat">👁 {{ article.viewCount || 0 }}</text>
          <text class="article-card__stat">❤ {{ article.likeCount || 0 }}</text>
          <text class="article-card__stat">⭐ {{ article.favoriteCount || 0 }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  article: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['click'])

const onClick = () => {
  emit('click', props.article)
}
</script>

<style scoped>
.article-card {
  display: flex;
  flex-direction: row;
  background-color: var(--jianshu-white);
  border-radius: 16rpx;
  margin: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.article-card__bar {
  width: 8rpx;
  background-color: var(--jianshu-red);
  flex-shrink: 0;
}

.article-card__body {
  flex: 1;
  padding: 28rpx 32rpx;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-card__title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--jianshu-text);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.article-card__summary {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: var(--jianshu-text-light);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.article-card__footer {
  margin-top: 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.article-card__author {
  display: flex;
  flex-direction: row;
  align-items: center;
  min-width: 0;
  flex: 1;
}

.article-card__avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.article-card__avatar--default {
  background-color: var(--jianshu-red-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

.article-card__avatar-text {
  font-size: 24rpx;
  color: var(--jianshu-red);
  font-weight: 600;
}

.article-card__name {
  margin-left: 12rpx;
  font-size: 24rpx;
  color: var(--jianshu-text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-card__stats {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;
}

.article-card__stat {
  margin-left: 20rpx;
  font-size: 24rpx;
  color: var(--jianshu-text-light);
}

.article-card__stat:first-child {
  margin-left: 0;
}
</style>
