<template>
  <div class="main-layout">
    <div class="main-content" :class="{ 'has-tabbar': showTabBar }">
      <router-view />
    </div>
    <van-tabbar v-model="activeTab" v-if="showTabBar" @change="onTabChange">
      <van-tabbar-item icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item icon="edit">写博文</van-tabbar-item>
      <van-tabbar-item icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref(0)

const showTabBar = computed(() => !route.meta.hideTabBar)

// 路由变化时同步 tab 高亮
watch(() => route.path, (path) => {
  if (path === '/' || path === '') {
    activeTab.value = 0
  } else if (path.startsWith('/edit')) {
    activeTab.value = 1
  } else if (path.startsWith('/profile') || path.startsWith('/my/')) {
    activeTab.value = 2
  }
}, { immediate: true })

// 点击 tab 时手动跳转
function onTabChange(index) {
  const routes = ['/', '/edit', '/profile']
  const target = routes[index]
  if (target && route.path !== target) {
    router.push(target)
  }
}
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
