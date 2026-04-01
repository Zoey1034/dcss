<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-wrapper">
        <view class="search-icon">🔍</view>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索文件名..."
          confirm-type="search"
          @confirm="loadFiles"
        />
        <text v-if="keyword" class="clear-icon" @click="clearSearch">✕</text>
      </view>
      <button class="search-btn" @click="loadFiles">搜索</button>
    </view>

    <!-- 快速操作栏 -->
    <view class="action-bar">
      <button class="upload-btn" @click="handleUpload">
        <text class="btn-icon">⬆</text>
        <text class="btn-text">上传文件</text>
      </button>
    </view>

    <!-- 统计信息 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-label">总计</text>
        <text class="stat-value">{{ total }}</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-label">当前页</text>
        <text class="stat-value">{{ files.length }}</text>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-wrapper">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-else-if="files.length === 0" class="empty-state">
      <text class="empty-icon">📂</text>
      <text class="empty-text">暂无文件</text>
      <text class="empty-hint">点击上方按钮上传文件</text>
    </view>

    <!-- 文件列表 -->
    <view v-else class="file-list">
      <view
        v-for="file in files"
        :key="file.id"
        class="file-card card"
      >
        <view class="file-header">
          <view class="file-icon">📄</view>
          <view class="file-main">
            <text class="file-name">{{ file.originalName || file.fileName }}</text>
            <view class="file-meta">
              <text class="meta-item">{{ formatSize(file.fileSize) }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-item">{{ formatDate(file.createTime) }}</text>
            </view>
          </view>
        </view>

        <view class="file-status">
          <view :class="['status-badge', getStatusClass(file.status)]">
            {{ getStatusText(file.status) }}
          </view>
        </view>

        <view class="file-actions">
          <button class="action-btn action-ai" @click="handleAI(file)">
            <text class="action-icon">🤖</text>
            <text class="action-text">AI识别</text>
          </button>
          <button class="action-btn action-delete" @click="handleDelete(file)">
            <text class="action-icon">🗑️</text>
            <text class="action-text">删除</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 分页器 -->
    <view v-if="files.length > 0" class="pagination">
      <button
        class="page-btn"
        :class="{ 'page-btn-disabled': page <= 1 }"
        :disabled="page <= 1"
        @click="prevPage"
      >
        ← 上一页
      </button>
      <view class="page-info">
        <text class="page-current">{{ page }}</text>
        <text class="page-divider">/</text>
        <text class="page-total">{{ totalPages }}</text>
      </view>
      <button
        class="page-btn"
        :class="{ 'page-btn-disabled': page >= totalPages }"
        :disabled="page >= totalPages"
        @click="nextPage"
      >
        下一页 →
      </button>
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
    clearSearch() {
      this.keyword = ''
      this.page = 1
      this.loadFiles()
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
      uni.showActionSheet({
        itemList: ['选择图片', '从聊天中选择文件'],
        success: ({ tapIndex }) => {
          if (tapIndex === 0) {
            uni.chooseImage({
              count: 1,
              success: async (res) => {
                const file = res.tempFiles[0]
                uni.showLoading({ title: '上传中...', mask: true })
                try {
                  await fileApi.upload(file.path, { originalName: file.name || 'image.jpg' })
                  uni.showToast({ title: '上传成功', icon: 'success' })
                  this.page = 1
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
                uni.showLoading({ title: '上传中...', mask: true })
                try {
                  await fileApi.upload(file.path, { originalName: file.name })
                  uni.showToast({ title: '上传成功', icon: 'success' })
                  this.page = 1
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
      uni.showLoading({ title: 'AI识别中...', mask: true })
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
        confirmColor: '#ef4444',
        success: async ({ confirm }) => {
          if (confirm) {
            uni.showLoading({ title: '删除中...', mask: true })
            try {
              await fileApi.deleteFile(file.id)
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadFiles()
            } finally {
              uni.hideLoading()
            }
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
      const map = {
        0: '待处理',
        1: '处理中',
        2: '已完成',
        3: '待审核',
        4: '已通过',
        5: '已拒绝',
        9: '处理失败'
      }
      return map[status] || '未知'
    },
    getStatusClass(status) {
      const map = {
        0: 'status-default',
        1: 'status-processing',
        2: 'status-success',
        3: 'status-warning',
        4: 'status-success',
        5: 'status-danger',
        9: 'status-danger'
      }
      return map[status] || 'status-default'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

/* ============ 搜索区域 ============ */
.search-section {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  align-items: center;
}

.search-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background: $white;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-md;
  padding: 0 $spacing-lg;
  height: 80rpx;
  transition: all $transition-base $transition-ease;

  &:focus-within {
    border-color: $primary-color;
    box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.1);
  }

  .search-icon {
    font-size: $font-size-lg;
    margin-right: $spacing-sm;
    opacity: 0.6;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color;
    border: none;
  }

  .clear-icon {
    font-size: $font-size-md;
    color: $text-tertiary;
    padding: $spacing-sm;
  }
}

.search-btn {
  height: 80rpx;
  padding: 0 $spacing-xl;
  background: $gradient-blue;
  color: $white;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  box-shadow: $box-shadow-sm;
}

/* ============ 操作栏 ============ */
.action-bar {
  margin-bottom: $spacing-lg;
}

.upload-btn {
  width: 100%;
  height: 96rpx;
  background: $gradient-blue;
  color: $white;
  border: none;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  box-shadow: $box-shadow-md;

  .btn-icon {
    font-size: $font-size-xl;
  }

  .btn-text {
    font-size: $font-size-lg;
  }
}

/* ============ 统计信息 ============ */
.stats-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xl;
  padding: $spacing-lg;
  background: $white;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $box-shadow-sm;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-label {
      font-size: $font-size-sm;
      color: $text-secondary;
      margin-bottom: $spacing-xs;
    }

    .stat-value {
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: $primary-color;
    }
  }

  .stat-divider {
    width: 2rpx;
    height: 60rpx;
    background: $border-color;
  }
}

/* ============ 文件列表 ============ */
.file-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.file-card {
  padding: $spacing-lg;
  animation: fadeInUp 0.3s ease-out;

  .file-header {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    .file-icon {
      font-size: 56rpx;
      flex-shrink: 0;
    }

    .file-main {
      flex: 1;
      min-width: 0;

      .file-name {
        display: block;
        font-size: $font-size-md;
        font-weight: $font-weight-semibold;
        color: $text-color;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-bottom: $spacing-xs;
      }

      .file-meta {
        display: flex;
        align-items: center;
        gap: $spacing-xs;

        .meta-item {
          font-size: $font-size-sm;
          color: $text-tertiary;
        }

        .meta-dot {
          font-size: $font-size-sm;
          color: $text-placeholder;
        }
      }
    }
  }

  .file-status {
    margin-bottom: $spacing-md;

    .status-badge {
      display: inline-flex;
      align-items: center;
      padding: $spacing-xs $spacing-md;
      border-radius: $border-radius-full;
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;

      &.status-default {
        background: $border-light;
        color: $text-secondary;
      }

      &.status-processing {
        background: $warning-light;
        color: $warning-color;
        animation: pulse 2s ease-in-out infinite;
      }

      &.status-success {
        background: $success-light;
        color: $success-color;
      }

      &.status-warning {
        background: $warning-light;
        color: $warning-color;
      }

      &.status-danger {
        background: $danger-light;
        color: $danger-color;
      }
    }
  }

  .file-actions {
    display: flex;
    gap: $spacing-md;

    .action-btn {
      flex: 1;
      height: 72rpx;
      border-radius: $border-radius-md;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: $spacing-sm;
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      border: 2rpx solid;
      transition: all $transition-base $transition-ease;

      .action-icon {
        font-size: $font-size-lg;
      }

      &.action-ai {
        background: $primary-lighter;
        color: $primary-color;
        border-color: $primary-color;

        &:active {
          background: $primary-light;
        }
      }

      &.action-delete {
        background: $danger-light;
        color: $danger-color;
        border-color: $danger-color;

        &:active {
          background: rgba(239, 68, 68, 0.2);
        }
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* ============ 分页器 ============ */
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xl 0;
  gap: $spacing-lg;

  .page-btn {
    height: 72rpx;
    padding: 0 $spacing-xl;
    background: $white;
    color: $primary-color;
    border: 2rpx solid $primary-color;
    border-radius: $border-radius-md;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    transition: all $transition-base $transition-ease;

    &.page-btn-disabled {
      color: $text-placeholder;
      border-color: $border-color;
      background: $border-light;
    }

    &:active:not(.page-btn-disabled) {
      background: $primary-lighter;
    }
  }

  .page-info {
    display: flex;
    align-items: baseline;
    gap: $spacing-xs;

    .page-current {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $primary-color;
    }

    .page-divider {
      font-size: $font-size-base;
      color: $text-tertiary;
    }

    .page-total {
      font-size: $font-size-base;
      color: $text-secondary;
    }
  }
}
</style>
