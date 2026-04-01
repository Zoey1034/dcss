<template>
  <view class="container">
    <!-- 头部统计卡片 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-value">{{ stats.totalFiles || 0 }}</text>
          <text class="stats-label">总文件数</text>
        </view>
        <view class="stats-item">
          <text class="stats-value text-warning">{{ stats.pendingReview || 0 }}</text>
          <text class="stats-label">待审核</text>
        </view>
        <view class="stats-item">
          <text class="stats-value text-success">{{ stats.completed || 0 }}</text>
          <text class="stats-label">已完成</text>
        </view>
      </view>
    </view>

    <!-- 快捷功能 -->
    <view class="quick-actions">
      <view class="section-title">快捷功能</view>
      <view class="action-grid">
        <view class="action-item" @click="navigateTo('/pages/file-upload/file-upload')">
          <view class="action-icon" style="background: #007AFF;">📤</view>
          <text class="action-text">上传文件</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/review/review')">
          <view class="action-icon" style="background: #FF9500;">✓</view>
          <text class="action-text">待审核</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/classification/classification')">
          <view class="action-icon" style="background: #34C759;">📁</view>
          <text class="action-text">分类查看</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/dashboard/dashboard')">
          <view class="action-icon" style="background: #5856D6;">📊</view>
          <text class="action-text">数据概览</text>
        </view>
      </view>
    </view>

    <!-- 最近处理 -->
    <view class="recent-section">
      <view class="section-title">最近处理</view>
      <view class="file-list">
        <view
          class="file-item"
          v-for="item in recentFiles"
          :key="item.id"
          @click="viewFileDetail(item.id)"
        >
          <view class="file-info">
            <text class="file-name">{{ item.fileName }}</text>
            <text class="file-date">{{ item.createTime }}</text>
          </view>
          <view class="file-status" :style="{ color: getStatusColor(item.status) }">
            {{ getStatusText(item.status) }}
          </view>
        </view>

        <view class="empty" v-if="recentFiles.length === 0">
          <text class="empty-text">暂无数据</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { dashboardApi, fileApi } from '@/common/api/index.js'
import { formatDate, getFileStatusText, getFileStatusColor } from '@/common/utils/util.js'

export default {
  data() {
    return {
      stats: {},
      recentFiles: []
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        // 加载统计数据
        const overview = await dashboardApi.overview()
        this.stats = {
          totalFiles: overview.totalFiles,
          pendingReview: overview.pendingReview,
          completed: overview.reviewedCount
        }

        // 加载最近文件
        const fileList = await fileApi.list({
          page: 1,
          size: 5
        })
        this.recentFiles = fileList.records.map(item => ({
          ...item,
          createTime: formatDate(item.createTime, 'MM-dd HH:mm')
        }))
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    viewFileDetail(id) {
      uni.navigateTo({
        url: `/pages/review-detail/review-detail?id=${id}`
      })
    },
    getStatusText(status) {
      return getFileStatusText(status)
    },
    getStatusColor(status) {
      return getFileStatusColor(status)
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-section {
  padding: 24rpx;
}

.stats-card {
  display: flex;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .stats-item {
    flex: 1;
    text-align: center;

    .stats-value {
      display: block;
      font-size: 48rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;

      &.text-warning {
        color: #ff9500;
      }

      &.text-success {
        color: #34c759;
      }
    }

    .stats-label {
      display: block;
      font-size: 24rpx;
      color: #999;
    }
  }
}

.quick-actions,
.recent-section {
  padding: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  background-color: #fff;
  border-radius: 12rpx;

  .action-icon {
    width: 96rpx;
    height: 96rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    font-size: 40rpx;
    margin-bottom: 12rpx;
  }

  .action-text {
    font-size: 24rpx;
    color: #666;
  }
}

.file-list {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .file-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .file-name {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 8rpx;
    }

    .file-date {
      font-size: 24rpx;
      color: #999;
    }
  }

  .file-status {
    font-size: 24rpx;
  }
}

.empty {
  padding: 80rpx 0;
  text-align: center;

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
