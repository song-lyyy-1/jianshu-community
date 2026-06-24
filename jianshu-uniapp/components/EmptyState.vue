<template>
  <view class="empty-state">
    <text class="empty-state__icon">{{ icon }}</text>
    <text class="empty-state__text">{{ text }}</text>
  </view>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '暂无数据'
  },
  icon: {
    type: String,
    default: '📭'
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
}

.empty-state__icon {
  font-size: 120rpx;
  line-height: 1;
}

.empty-state__text {
  margin-top: 32rpx;
  font-size: 28rpx;
  color: var(--jianshu-text-light);
}
</style>
