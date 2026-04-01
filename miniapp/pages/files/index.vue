<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar card">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索文件名..."
        confirm-type="search"
        @confirm="loadFiles"
      />
      <button class="btn-primary search-btn" @click="loadFiles">搜索</button>
    </view>

    <!-- 上传按钮 -->
    <button class="btn-primary upload-btn" @click="handleUpload">
      + 上传文件
    </button>

    <!-- 文件列表 -->
    <view v-if="loading" class="empty-state">
      <text class="empty-icon">⏳</text>
      <text class="empty-text">加载中...</text>
    </view>

    <view v-else-if="files.length === 0" class="empty-state">
      <text class="empty-icon">📂</text>
      <text class="empty-text">暂无文件，点击上传</text>
    </view>

    <view v-else>
      <view v-for="file in files" :key="file.id" class="card file-card">
        <view class="file-info">
          <text class="file-name">{{ file.originalName || file.fileName }}</text>
          <view class="file-meta">
            <text class="meta-text">{{ formatSize(file.fileSize) }}</text>
            <text class="meta-sep">·</text>
            <text class="meta-text">{{ formatDate(file.createTime) }}</text>
          </view>
          <view :class="['tag', getStatusClass(file.status)]">
            {{ getStatusText(file.status) }}
          </view>
        </view>
        <view class="file-actions">
          <text class="action-btn action-ai" @click="handleAI(file)">AI识别</text>
          <text class="action-btn action-delete" @click="handleDelete(file)">删除</text>
        </view>
      </view>

      <!-- 分页 -->
      <view class="pagination">
        <text
          class="page-btn"
          :class="{ disabled: page <= 1 }"
          @click="prevPage"
        >上一页</text>
        <text class="page-info">{{ page }} / {{ totalPages }}</text>
        <text
          class="page-btn"
          :class="{ disabled: page >= totalPages }"
          @click="nextPage"
        >下一页</text>
      </view>
    </view>
  </view>
</template>

<script>
import { fileApi, aiApi } from '@/utils/api'

export default {
  data() {
    return {
      files: [],
      keyword: '',
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.total / this.pageSize))
    }
  },
  onShow() {
    this.loadFiles()
  },
  methods: {
    async loadFiles() {
      this.loading = true
      try {
        const res = await fileApi.getPage({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.keyword
        })
        const data = res.data || res
        this.files = data.records || data.list || []
        this.total = data.total || 0
      } finally {
        this.loading = false
      }
    },
    prevPage() {
      if (this.page > 1) {
        this.page--
        this.loadFiles()
      }
    },
    nextPage() {
      if (this.page < this.totalPages) {
        this.page++
        this.loadFiles()
      }
    },
    handleUpload() {
      // Use chooseImage for images, fall back to chooseMessageFile for other files
      uni.showActionSheet({
        itemList: ['选择图片', '从聊天中选择文件'],
        success: ({ tapIndex }) => {
          if (tapIndex === 0) {
            uni.chooseImage({
              count: 1,
              success: async (res) => {
                const file = res.tempFiles[0]
                uni.showLoading({ title: '上传中...' })
                try {
                  await fileApi.upload(file.path, { originalName: file.name || 'image.jpg' })
                  uni.showToast({ title: '上传成功', icon: 'success' })
                  this.loadFiles()
                } finally {
                  uni.hideLoading()
                }
              }
            })
          } else {
            // #ifdef MP-WEIXIN
            uni.chooseMessageFile({
              count: 1,
              type: 'file',
              success: async (res) => {
                const file = res.tempFiles[0]
                uni.showLoading({ title: '上传中...' })
                try {
                  await fileApi.upload(file.path, { originalName: file.name })
                  uni.showToast({ title: '上传成功', icon: 'success' })
                  this.loadFiles()
                } finally {
                  uni.hideLoading()
                }
              }
            })
            // #endif
            // #ifndef MP-WEIXIN
            uni.showToast({ title: '请使用"选择图片"上传', icon: 'none' })
            // #endif
          }
        }
      })
    },
    async handleAI(file) {
      uni.showLoading({ title: 'AI识别中...' })
      try {
        await aiApi.process({ fileId: file.id })
        uni.showToast({ title: 'AI识别已提交', icon: 'success' })
        this.loadFiles()
      } finally {
        uni.hideLoading()
      }
    },
    handleDelete(file) {
      uni.showModal({
        title: '确认删除',
        content: `确定删除文件"${file.originalName || file.fileName}"？`,
        success: async ({ confirm }) => {
          if (confirm) {
            await fileApi.deleteFile(file.id)
            uni.showToast({ title: '删除成功', icon: 'success' })
            this.loadFiles()
          }
        }
      })
    },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.split(' ')[0]
    },
    getStatusText(status) {
      const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '待审核', 4: '已通过', 5: '已拒绝', 9: '处理失败' }
      return map[status] || '未知'
    },
    getStatusClass(status) {
      const map = { 0: 'tag-default', 1: 'tag-warning', 2: 'tag-primary', 3: 'tag-warning', 4: 'tag-success', 5: 'tag-danger', 9: 'tag-danger' }
      return map[status] || 'tag-default'
    }
  }
}
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  align-items: center;
  padding: 16rpx;

  .search-input {
    flex: 1;
    height: 64rpx;
    border: 2rpx solid #e8e8e8;
    border-radius: 8rpx;
    padding: 0 20rpx;
    font-size: 26rpx;
    margin-right: 16rpx;
  }

  .search-btn {
    width: auto;
    height: 64rpx;
    font-size: 26rpx;
    padding: 0 32rpx;
    line-height: 64rpx;
  }
}

.upload-btn {
  margin-bottom: 24rpx;
  height: 80rpx;
  font-size: 30rpx;
  line-height: 80rpx;
}

.file-card {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .file-info {
    flex: 1;
    overflow: hidden;

    .file-name {
      display: block;
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 8rpx;
    }

    .file-meta {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .meta-text {
        font-size: 22rpx;
        color: #999999;
      }

      .meta-sep {
        margin: 0 8rpx;
        color: #cccccc;
      }
    }
  }

  .file-actions {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    margin-left: 16rpx;

    .action-btn {
      font-size: 24rpx;
      padding: 8rpx 16rpx;
      border-radius: 6rpx;
      text-align: center;
    }

    .action-ai {
      background: #e8f0fe;
      color: #1a6fc4;
    }

    .action-delete {
      background: #fff2f0;
      color: #ff4d4f;
    }
  }
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
  gap: 32rpx;

  .page-btn {
    font-size: 26rpx;
    color: #1a6fc4;
    padding: 12rpx 24rpx;
    border: 2rpx solid #1a6fc4;
    border-radius: 8rpx;

    &.disabled {
      color: #cccccc;
      border-color: #e8e8e8;
    }
  }

  .page-info {
    font-size: 26rpx;
    color: #666666;
  }
}
</style>
