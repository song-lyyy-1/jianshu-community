<script>
export default {
  onLaunch() {
    console.log('App Launch')
  }
}
</script>

<style>
/* ========== 简书风格配色变量 ========== */
page {
  --jianshu-red: #ea6f5a;
  --jianshu-red-light: #fff0ed;
  --jianshu-bg: #f5f5f5;
  --jianshu-text: #333333;
  --jianshu-text-light: #969696;
  --jianshu-border: #eaeaea;
  --jianshu-white: #ffffff;

  background-color: var(--jianshu-bg);
  font-size: 28rpx;
  color: var(--jianshu-text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 通用工具类 */
.page-container {
  min-height: 100vh;
  background-color: var(--jianshu-bg);
}

.card {
  background: var(--jianshu-white);
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
}

.text-red {
  color: var(--jianshu-red);
}

.text-light {
  color: var(--jianshu-text-light);
}

/* 文本省略 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
</style>
