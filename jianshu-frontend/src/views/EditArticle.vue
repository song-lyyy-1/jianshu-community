<template>
  <div class="edit-article-page">
    <NavBar :title="isEdit ? '编辑博文' : '写博文'" />
    <div class="title-input">
      <van-field
        v-model="title"
        placeholder="请输入标题"
        :border="false"
        class="title-field"
      />
    </div>
    <div class="content-input">
      <van-field
        v-model="content"
        type="textarea"
        placeholder="请输入博文内容..."
        :border="false"
        autosize
        class="content-field"
      />
    </div>
    <div class="bottom-bar">
      <van-button plain class="draft-btn" @click="save(0)" :loading="submitting">保存草稿</van-button>
      <van-button type="primary" class="publish-btn" @click="save(1)" :loading="submitting">发布</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createArticle, updateArticle, getArticleDetail } from '@/api/article'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()

const title = ref('')
const content = ref('')
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

onMounted(async () => {
  if (isEdit.value) {
    try {
      const res = await getArticleDetail(route.params.id, { skipView: true })
      title.value = res.data.title || ''
      content.value = res.data.content || ''
    } catch {
      showToast('加载文章失败')
    }
  }
})

async function save(status) {
  if (!title.value.trim()) {
    showToast('请输入标题')
    return
  }
  if (!content.value.trim()) {
    showToast('请输入内容')
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
      await updateArticle(route.params.id, data)
      showToast(status === 1 ? '发布成功' : '草稿已保存')
      router.back()
    } else {
      const res = await createArticle(data)
      showToast(status === 1 ? '发布成功' : '草稿已保存')
      if (status === 1) {
        const newId = res.data
        if (newId) {
          router.replace(`/article/${newId}`)
        } else {
          router.back()
        }
      } else {
        title.value = ''
        content.value = ''
      }
    }
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.edit-article-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--jianshu-white);
}

.title-input {
  padding: 0;
  border-bottom: 1px solid var(--jianshu-border);
}

.title-field {
  font-size: 18px;
  font-weight: 600;
}

.title-field :deep(.van-field__control) {
  font-size: 18px;
  font-weight: 600;
}

.title-field :deep(.van-field__body) {
  padding: 12px 16px;
}

.content-input {
  flex: 1;
  overflow-y: auto;
}

.content-field {
  padding: 0;
}

.content-field :deep(.van-field__control) {
  font-size: 15px;
  line-height: 1.8;
  min-height: 300px;
  padding: 16px;
}

.bottom-bar {
  display: flex;
  gap: 12px;
  padding: 10px 16px;
  border-top: 1px solid var(--jianshu-border);
  background: var(--jianshu-white);
}

.draft-btn {
  flex: 1;
  color: var(--jianshu-text-light);
  border-color: var(--jianshu-border);
}

.publish-btn {
  flex: 1;
  background: var(--jianshu-red);
  border-color: var(--jianshu-red);
}
</style>
