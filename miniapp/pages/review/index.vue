<template>
  <view class="container">
    <!-- 待审核文件列表 -->
    <view v-if="loading" class="empty-state">
      <text class="empty-icon">⏳</text>
      <text class="empty-text">加载中...</text>
    </view>

    <view v-else-if="files.length === 0" class="empty-state">
      <text class="empty-icon">✅</text>
      <text class="empty-text">暂无待审核文件</text>
    </view>

    <view v-else>
      <view v-for="file in files" :key="file.id" class="card review-card">
        <view class="file-header">
          <text class="file-name">{{ file.originalName || file.fileName }}</text>
          <view class="tag tag-warning">待审核</view>
        </view>
        <view class="file-meta">
          <text class="meta-item">上传时间：{{ formatDate(file.createTime) }}</text>
          <text class="meta-item">文件大小：{{ formatSize(file.fileSize) }}</text>
        </view>
        <view class="card-actions">
          <button class="btn-primary action-btn" @click="openReview(file)">
            开始审核
          </button>
        </view>
      </view>

      <view class="pagination">
        <text class="page-btn" :class="{ disabled: page <= 1 }" @click="prevPage">上一页</text>
        <text class="page-info">{{ page }} / {{ totalPages }}</text>
        <text class="page-btn" :class="{ disabled: page >= totalPages }" @click="nextPage">下一页</text>
      </view>
    </view>

    <!-- 审核详情弹窗 -->
    <view v-if="currentFile" class="review-modal-mask" @click.self="closeReview">
      <view class="review-modal">
        <view class="modal-header">
          <text class="modal-title">审核文件</text>
          <text class="modal-close" @click="closeReview">✕</text>
        </view>

        <scroll-view scroll-y class="modal-body">
          <view class="section">
            <text class="section-title">AI识别结果</text>
            <view v-if="aiResult" class="ai-result">
              <view class="result-item">
                <text class="result-label">文档类型</text>
                <text class="result-value">{{ aiResult.documentType }}</text>
              </view>
              <view class="result-item">
                <text class="result-label">置信度</text>
                <text class="result-value">{{ (aiResult.confidence * 100).toFixed(1) }}%</text>
              </view>
              <view v-if="aiResult.extractedInfo" class="result-item">
                <text class="result-label">提取信息</text>
                <text class="result-value">{{ JSON.stringify(aiResult.extractedInfo) }}</text>
              </view>
            </view>
            <view v-else class="empty-state">
              <text class="empty-text">暂无AI识别结果</text>
            </view>
          </view>

          <view class="section">
            <text class="section-title">审核意见</text>
            <textarea
              v-model="auditComment"
              class="comment-input"
              placeholder="请输入审核意见（可选）"
              maxlength="500"
            />
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn-default reject-btn" @click="submitReview(2)">拒 绝</button>
          <button class="btn-primary approve-btn" @click="submitReview(1)">通 过</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { fileApi, auditApi } from '@/utils/api'

export default {
  data() {
    return {
      files: [],
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      currentFile: null,
      aiResult: null,
      auditComment: ''
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
        const res = await fileApi.getPage({ page: this.page, pageSize: this.pageSize, status: 3 })
        const data = res.data || res
        this.files = data.records || data.list || []
        this.total = data.total || 0
      } finally {
        this.loading = false
      }
    },
    prevPage() {
      if (this.page > 1) { this.page--; this.loadFiles() }
    },
    nextPage() {
      if (this.page < this.totalPages) { this.page++; this.loadFiles() }
    },
    async openReview(file) {
      this.currentFile = file
      this.auditComment = ''
      this.aiResult = null
      try {
        const res = await auditApi.getResult(file.id)
        this.aiResult = res.data
      } catch (e) {
        // AI结果可能不存在
      }
    },
    closeReview() {
      this.currentFile = null
    },
    async submitReview(auditStatus) {
      uni.showLoading({ title: '提交中...' })
      try {
        await auditApi.submit({
          fileId: this.currentFile.id,
          auditStatus,
          auditComment: this.auditComment
        })
        uni.showToast({ title: auditStatus === 1 ? '审核通过' : '已拒绝', icon: 'success' })
        this.closeReview()
        this.loadFiles()
      } finally {
        uni.hideLoading()
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.split(' ')[0]
    },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
    }
  }
}
</script>

<style lang="scss" scoped>
.review-card {
  .file-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;

    .file-name {
      flex: 1;
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-right: 16rpx;
    }
  }

  .file-meta {
    margin-bottom: 16rpx;

    .meta-item {
      display: block;
      font-size: 22rpx;
      color: #999999;
      margin-bottom: 4rpx;
    }
  }

  .card-actions {
    .action-btn {
      height: 64rpx;
      font-size: 26rpx;
      line-height: 64rpx;
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

.review-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.review-modal {
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;

  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .modal-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }

    .modal-close {
      font-size: 32rpx;
      color: #999999;
    }
  }

  .modal-body {
    flex: 1;
    padding: 24rpx 32rpx;
    overflow-y: auto;

    .section {
      margin-bottom: 32rpx;

      .section-title {
        display: block;
        font-size: 28rpx;
        font-weight: 500;
        color: #333333;
        margin-bottom: 16rpx;
        padding-left: 12rpx;
        border-left: 6rpx solid #1a6fc4;
      }
    }

    .ai-result {
      .result-item {
        display: flex;
        padding: 12rpx 0;
        border-bottom: 2rpx solid #f5f5f5;

        .result-label {
          width: 160rpx;
          font-size: 26rpx;
          color: #999999;
          flex-shrink: 0;
        }

        .result-value {
          flex: 1;
          font-size: 26rpx;
          color: #333333;
        }
      }
    }

    .comment-input {
      width: 100%;
      min-height: 160rpx;
      border: 2rpx solid #e8e8e8;
      border-radius: 8rpx;
      padding: 16rpx;
      font-size: 26rpx;
      color: #333333;
      background: #fafafa;
      box-sizing: border-box;
    }
  }

  .modal-footer {
    display: flex;
    padding: 24rpx 32rpx;
    gap: 24rpx;
    border-top: 2rpx solid #f0f0f0;

    .reject-btn {
      flex: 1;
      height: 80rpx;
      font-size: 28rpx;
      color: #ff4d4f;
      border-color: #ff4d4f;
    }

    .approve-btn {
      flex: 1;
      height: 80rpx;
      font-size: 28rpx;
      line-height: 80rpx;
    }
  }
}
</style>
