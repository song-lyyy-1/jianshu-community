<template>
  <view class="edit-article-page">
    <NavBar :title="isEdit ? '编辑博文' : '写博文'" />

    <view class="title-input">
      <input
        v-model="title"
        class="title-field"
        placeholder="请输入标题"
        placeholder-class="title-placeholder"
        maxlength="100"
      />
    </view>

    <view class="content-input">
      <textarea
        v-model="content"
        class="content-field"
        placeholder="请输入博文内容..."
        placeholder-class="content-placeholder"
        :auto-height="true"
        maxlength="-1"
      />
    </view>

    <view class="bottom-bar">
      <view
        class="btn draft-btn"
        :class="{ 'btn--disabled': submitting }"
        @click="save(0)"
      >
        <text class="btn__text">{{ submitting ? '提交中...' : '保存草稿' }}</text>
      </view>
      <view
        class="btn publish-btn"
        :class="{ 'btn--disabled': submitting }"
        @click="save(1)"
      >
        <text class="btn__text">{{ submitting ? '提交中...' : '发布' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createArticle, updateArticle, getArticleDetail } from '@/api/article'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()

const title = ref('')
const content = ref('')
const submitting = ref(false)

const articleId = ref('')
const isEdit = computed(() => !!articleId.value)

onLoad((options) => {
  // 登录检查
  if (!userStore.isLogin) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  if (options.id) {
    articleId.value = options.id
    loadArticle(options.id)
  }
})

async function loadArticle(id) {
  try {
    const res = await getArticleDetail(id, { skipView: true })
    title.value = res.data.title || ''
    content.value = res.data.content || ''
  } catch (e) {
    uni.showToast({ title: '加载文章失败', icon: 'none' })
  }
}

async function save(status) {
  if (submitting.value) return

  if (!title.value.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const data = {
      title: title.value,
      content: content.value,
      status
    }
    if (isEdit.value) {
      await updateArticle(articleId.value, data)
      uni.showToast({ title: status === 1 ? '发布成功' : '草稿已保存', icon: 'success' })
      if (status === 1) {
        uni.redirectTo({ url: `/pages/article-detail/article-detail?id=${articleId.value}` })
      } else {
        uni.navigateBack()
      }
    } else {
      const res = await createArticle(data)
      uni.showToast({ title: status === 1 ? '发布成功' : '草稿已保存', icon: 'success' })
      if (status === 1) {
        const newId = res.data
        if (newId) {
          uni.redirectTo({ url: `/pages/article-detail/article-detail?id=${newId}` })
        } else {
          uni.navigateBack()
        }
      } else {
        uni.navigateBack()
      }
    }
  } catch (e) {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.edit-article-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--jianshu-white);
}

.title-input {
  border-bottom: 1rpx solid var(--jianshu-border);
  padding: 0 32rpx;
}

.title-field {
  width: 100%;
  height: 96rpx;
  font-size: 36rpx;
  font-weight: 600;
  color: var(--jianshu-text);
}

.title-placeholder {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--jianshu-text-light);
}

.content-input {
  flex: 1;
  padding: 0 32rpx;
}

.content-field {
  width: 100%;
  min-height: 600rpx;
  padding: 32rpx 0;
  font-size: 30rpx;
  line-height: 1.8;
  color: var(--jianshu-text);
}

.content-placeholder {
  font-size: 30rpx;
  color: var(--jianshu-text-light);
}

.bottom-bar {
  display: flex;
  flex-direction: row;
  padding: 20rpx 32rpx;
  border-top: 1rpx solid var(--jianshu-border);
  background: var(--jianshu-white);
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  border-radius: 8rpx;
  margin: 0 12rpx;
}

.btn--disabled {
  opacity: 0.6;
}

.btn__text {
  font-size: 30rpx;
}

.draft-btn {
  border: 1rpx solid var(--jianshu-border);
  background: var(--jianshu-white);
}

.draft-btn .btn__text {
  color: var(--jianshu-text-light);
}

.publish-btn {
  background: var(--jianshu-red);
}

.publish-btn .btn__text {
  color: var(--jianshu-white);
  font-weight: 500;
}
</style>
