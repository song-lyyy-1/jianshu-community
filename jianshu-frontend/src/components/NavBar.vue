<template>
  <van-nav-bar
    :title="title"
    :left-arrow="leftArrow"
    fixed
    placeholder
    @click-left="handleClickLeft"
  />
</template>

<script setup>
import { useRouter } from 'vue-router'

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
const router = useRouter()

function handleClickLeft() {
  emit('clickLeft')
  if (props.useDefaultBack) {
    router.back()
  }
}
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
