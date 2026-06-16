import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useArticleStore = defineStore('article', () => {
  const currentDraft = ref(null)
  const articleList = ref([])

  function saveDraft(article) {
    currentDraft.value = { ...article }
  }

  function clearDraft() {
    currentDraft.value = null
  }

  return {
    currentDraft,
    articleList,
    saveDraft,
    clearDraft
  }
})
