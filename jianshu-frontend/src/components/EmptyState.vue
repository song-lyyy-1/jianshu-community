<template>
  <div class="empty-state">
    <van-empty :image="icon" :description="text" />
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '暂无数据'
  },
  icon: {
    type: String,
    default: 'info-o'
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}
</style>
