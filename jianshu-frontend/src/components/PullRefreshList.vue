<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <slot :list="list" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  requestFn: {
    type: Function,
    required: true
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

async function onLoad() {
  try {
    const res = await props.requestFn({ page: page.value, size: props.pageSize })
    const data = res.data
    const records = data?.records || []
    list.value.push(...records)
    const total = data?.total || 0
    if (list.value.length >= total || records.length < props.pageSize) {
      finished.value = true
    } else {
      page.value++
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  page.value = 1
  list.value = []
  finished.value = false
  loading.value = true
  await onLoad()
  refreshing.value = false
}

defineExpose({ list })
</script>
