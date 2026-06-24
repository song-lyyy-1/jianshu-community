<template>
  <view class="nav-bar">
    <view class="nav-bar__status" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar__content">
      <view class="nav-bar__left" @click="onClickLeft">
        <text v-if="leftArrow" class="nav-bar__arrow">‹</text>
      </view>
      <text class="nav-bar__title">{{ title }}</text>
      <view class="nav-bar__right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
  <!-- 占位，防止内容被导航栏遮挡 -->
  <view class="nav-bar__placeholder" :style="{ height: (statusBarHeight + 44) + 'px' }"></view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  leftArrow: {
    type: Boolean,
    default: true
  },
  useDefaultBack: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['clickLeft'])

const statusBarHeight = ref(20)
try {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
} catch (e) {
  statusBarHeight.value = 20
}

const onClickLeft = () => {
  emit('clickLeft')
  if (props.useDefaultBack) {
    uni.navigateBack({ delta: 1 }).catch(() => {})
  }
}
</script>

<style scoped>
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: var(--jianshu-white);
  border-bottom: 1rpx solid var(--jianshu-border);
}

.nav-bar__status {
  width: 100%;
}

.nav-bar__content {
  position: relative;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-bar__left {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-bar__arrow {
  font-size: 56rpx;
  color: #333333;
  line-height: 1;
}

.nav-bar__title {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--jianshu-red);
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-bar__right {
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.nav-bar__placeholder {
  width: 100%;
}
</style>
