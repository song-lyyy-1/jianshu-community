<template>
  <div class="markdown-render">
    <MdPreview :model-value="content" />
  </div>
</template>

<script setup>
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

defineProps({
  content: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.markdown-render {
  font-size: 15px;
  line-height: 1.8;
  color: var(--jianshu-text);
}

.markdown-render :deep(.md-preview) {
  padding: 0 !important;
}

.markdown-render :deep(.md-preview-wrapper) {
  padding: 0 !important;
}

.markdown-render :deep(h1) {
  font-size: 24px;
  margin: 20px 0 12px;
  border-bottom: 1px solid var(--jianshu-border);
  padding-bottom: 8px;
}

.markdown-render :deep(h2) {
  font-size: 20px;
  margin: 18px 0 10px;
  border-bottom: 1px solid var(--jianshu-border);
  padding-bottom: 6px;
}

.markdown-render :deep(h3) {
  font-size: 17px;
  margin: 16px 0 8px;
}

.markdown-render :deep(h4) {
  font-size: 15px;
  margin: 14px 0 6px;
}

.markdown-render :deep(p) {
  margin: 10px 0;
  line-height: 1.8;
  color: #2f2f2f;
}

.markdown-render :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 16px;
  border-left: 4px solid var(--jianshu-red);
  background: #fff8f5;
  color: #666;
}

.markdown-render :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
  color: #c7254e;
}

.markdown-render :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.markdown-render :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.markdown-render :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 8px 0;
}

.markdown-render :deep(a) {
  color: var(--jianshu-red);
}

.markdown-render :deep(ul),
.markdown-render :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}

.markdown-render :deep(li) {
  margin: 4px 0;
  line-height: 1.8;
}

.markdown-render :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.markdown-render :deep(th),
.markdown-render :deep(td) {
  border: 1px solid var(--jianshu-border);
  padding: 8px 12px;
  text-align: left;
}

.markdown-render :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

.markdown-render :deep(hr) {
  border: none;
  border-top: 1px solid var(--jianshu-border);
  margin: 20px 0;
}
</style>
