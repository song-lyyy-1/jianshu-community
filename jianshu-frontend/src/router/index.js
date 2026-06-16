import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue'),
        meta: { title: '文章详情', hideTabBar: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '我的', requireAuth: true }
      },
      {
        path: 'my/articles',
        name: 'MyArticles',
        component: () => import('@/views/MyArticles.vue'),
        meta: { title: '我的文章', requireAuth: true }
      },
      {
        path: 'my/likes',
        name: 'MyLikes',
        component: () => import('@/views/MyLikes.vue'),
        meta: { title: '我的点赞', requireAuth: true }
      },
      {
        path: 'my/favorites',
        name: 'MyFavorites',
        component: () => import('@/views/MyFavorites.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'edit',
        name: 'EditArticle',
        component: () => import('@/views/EditArticle.vue'),
        meta: { title: '写博文', requireAuth: true, hideTabBar: true }
      },
      {
        path: 'edit/:id',
        name: 'EditArticleById',
        component: () => import('@/views/EditArticle.vue'),
        meta: { title: '编辑博文', requireAuth: true, hideTabBar: true }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { title: '搜索', hideTabBar: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 简书问答'
  }

  const token = localStorage.getItem('jianshu_token')

  // 同步 userStore 状态（处理 401 清 token 后 store 不同步的问题）
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()
  if (!token && userStore.token) {
    userStore.logout()
  }

  // 需要认证但未登录
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录用户访问登录/注册页，重定向到首页
  if (token && (to.path === '/login' || to.path === '/register')) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
