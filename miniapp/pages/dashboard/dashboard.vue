<template>
  <view class="container">
    <!-- 概览卡片 -->
    <view class="overview-card">
      <view class="card-title">数据概览</view>
      <view class="overview-grid">
        <view class="overview-item">
          <text class="item-label">总文件数</text>
          <text class="item-value">{{ overview.totalFiles || 0 }}</text>
        </view>
        <view class="overview-item">
          <text class="item-label">待审核</text>
          <text class="item-value text-warning">{{ overview.pendingReview || 0 }}</text>
        </view>
        <view class="overview-item">
          <text class="item-label">已审核</text>
          <text class="item-value text-success">{{ overview.reviewedCount || 0 }}</text>
        </view>
        <view class="overview-item">
          <text class="item-label">审核率</text>
          <text class="item-value">{{ overview.reviewRate || 0 }}%</text>
        </view>
      </view>
    </view>

    <!-- 趋势图 -->
    <view class="trend-card">
      <view class="card-title">处理趋势</view>
      <view class="trend-list">
        <view class="trend-item" v-for="item in trendData" :key="item.date">
          <text class="trend-date">{{ item.date }}</text>
          <view class="trend-bar-wrapper">
            <view class="trend-bar" :style="{ width: item.percentage + '%' }"></view>
          </view>
          <text class="trend-count">{{ item.count }}</text>
        </view>
      </view>
    </view>

    <!-- 效率分析 -->
    <view class="efficiency-card">
      <view class="card-title">效率分析</view>
      <view class="efficiency-item">
        <text class="efficiency-label">平均处理时间</text>
        <text class="efficiency-value">{{ overview.avgProcessTime || '-' }}</text>
      </view>
      <view class="efficiency-item">
        <text class="efficiency-label">AI准确率</text>
        <text class="efficiency-value">{{ overview.aiAccuracy || '-' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { dashboardApi } from '@/common/api/index.js'

export default {
  data() {
    return {
      overview: {},
      trendData: []
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        // 加载概览数据
        const overview = await dashboardApi.overview()
        this.overview = overview

        // 加载趋势数据
        const trend = await dashboardApi.trend({ days: 7 })
        const maxCount = Math.max(...trend.map(item => item.count), 1)
        this.trendData = trend.map(item => ({
          ...item,
          percentage: (item.count / maxCount) * 100
        }))
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  padding: 24rpx;
  background-color: #f5f5f5;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.overview-card,
.trend-card,
.efficiency-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;

  .item-label {
    font-size: 24rpx;
    color: #666;
    margin-bottom: 12rpx;
  }

  .item-value {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;

    &.text-warning {
      color: #ff9500;
    }

    &.text-success {
      color: #34c759;
    }
  }
}

.trend-list {
  .trend-item {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .trend-date {
      width: 120rpx;
      font-size: 24rpx;
      color: #666;
    }

    .trend-bar-wrapper {
      flex: 1;
      height: 32rpx;
      background-color: #f0f0f0;
      border-radius: 16rpx;
      overflow: hidden;
      margin: 0 16rpx;

      .trend-bar {
        height: 100%;
        background: linear-gradient(to right, #667eea, #764ba2);
        border-radius: 16rpx;
      }
    }

    .trend-count {
      width: 80rpx;
      text-align: right;
      font-size: 24rpx;
      color: #333;
      font-weight: bold;
    }
  }
}

.efficiency-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .efficiency-label {
    font-size: 28rpx;
    color: #666;
  }

  .efficiency-value {
    font-size: 28rpx;
    color: #333;
    font-weight: bold;
  }
}
</style>
