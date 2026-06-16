<template>
  <van-nav-bar
    :title="title"
    :left-arrow="leftArrow"
    fixed
    placeholder
    @click-left="$emit('clickLeft')"
  />
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  leftArrow: {
    type: Boolean,
    default: true
  }
})

defineEmits(['clickLeft'])
</script>

<style>
.van-nav-bar {
  background: var(--jianshu-white) !important;
}

.van-nav-bar__title {
  color: var(--jianshu-red) !important;
  font-weight: 600 !important;
  font-size: 17px !important;
}

.van-nav-bar .van-icon {
  color: var(--jianshu-text) !important;
}
</style>
