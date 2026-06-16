<template>
  <div class="main-layout">
    <div class="main-content" :class="{ 'has-tabbar': showTabBar }">
      <router-view />
    </div>
    <van-tabbar v-model="activeTab" v-if="showTabBar" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="edit" to="/edit">写博文</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(0)

const showTabBar = computed(() => !route.meta.hideTabBar)
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--jianshu-bg);
}

.main-content {
  flex: 1;
  overflow-y: auto;
}

.main-content.has-tabbar {
  padding-bottom: 50px;
}
</style>
