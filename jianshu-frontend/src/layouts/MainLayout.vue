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
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref('/')

const showTabBar = computed(() => !route.meta.hideTabBar)

// 根据当前路由路径同步 tab 激活状态
watch(() => route.path, (path) => {
  if (path === '/' || path === '') {
    activeTab.value = '/'
  } else if (path.startsWith('/edit')) {
    activeTab.value = '/edit'
  } else if (path.startsWith('/profile') || path.startsWith('/my/')) {
    activeTab.value = '/profile'
  }
}, { immediate: true })
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
