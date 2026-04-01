<template>
  <view class="container">
    <!-- 统计卡片 -->
    <view v-if="stats" class="stats-grid">
      <view class="stat-card card">
        <text class="stat-value">{{ stats.totalFiles || 0 }}</text>
        <text class="stat-label">总文件数</text>
      </view>
      <view class="stat-card card">
        <text class="stat-value">{{ stats.todayUpload || 0 }}</text>
        <text class="stat-label">今日上传</text>
      </view>
      <view class="stat-card card">
        <text class="stat-value">{{ stats.processing || 0 }}</text>
        <text class="stat-label">处理中</text>
      </view>
      <view class="stat-card card">
        <text class="stat-value">{{ stats.pendingReview || 0 }}</text>
        <text class="stat-label">待审核</text>
      </view>
    </view>

    <!-- 趋势图 -->
    <view class="card">
      <view class="section-header">
        <text class="section-title">上传趋势（近7天）</text>
      </view>
      <view v-if="trend.length" class="trend-chart">
        <view v-for="item in trend" :key="item.date" class="trend-bar-wrap">
          <view
            class="trend-bar"
            :style="{ height: getBarHeight(item.count) + 'rpx' }"
          />
          <text class="trend-date">{{ formatShortDate(item.date) }}</text>
          <text class="trend-count">{{ item.count }}</text>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无趋势数据</text>
      </view>
    </view>

    <!-- 文件类型分布 -->
    <view class="card">
      <text class="section-title">文档类型分布</text>
      <view v-if="typeDistribution.length" class="type-dist">
        <view v-for="item in typeDistribution" :key="item.type" class="dist-item">
          <view class="dist-label-row">
            <text class="dist-type">{{ item.type }}</text>
            <text class="dist-count">{{ item.count }}</text>
          </view>
          <view class="dist-bar-bg">
            <view
              class="dist-bar"
              :style="{ width: getDistWidth(item.count) + '%' }"
            />
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无分布数据</text>
      </view>
    </view>

    <!-- 置信度分布 -->
    <view class="card">
      <text class="section-title">AI置信度分布</text>
      <view v-if="confidenceDist.length" class="type-dist">
        <view v-for="item in confidenceDist" :key="item.range" class="dist-item">
          <view class="dist-label-row">
            <text class="dist-type">{{ item.range }}</text>
            <text class="dist-count">{{ item.count }}</text>
          </view>
          <view class="dist-bar-bg">
            <view
              class="dist-bar dist-bar-green"
              :style="{ width: getConfidenceWidth(item.count) + '%' }"
            />
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无置信度数据</text>
      </view>
    </view>
  </view>
</template>

<script>
import { dashboardApi } from '@/utils/api'

export default {
  data() {
    return {
      stats: null,
      trend: [],
      typeDistribution: [],
      confidenceDist: []
    }
  },
  onShow() {
    this.loadAll()
  },
  methods: {
    async loadAll() {
      try {
        const [statsRes, trendRes, typeRes, confRes] = await Promise.allSettled([
          dashboardApi.getStats(),
          dashboardApi.getTrend(7),
          dashboardApi.getFileTypeDistribution(),
          dashboardApi.getConfidenceDistribution()
        ])
        if (statsRes.status === 'fulfilled') this.stats = statsRes.value.data
        if (trendRes.status === 'fulfilled') this.trend = trendRes.value.data || []
        if (typeRes.status === 'fulfilled') this.typeDistribution = typeRes.value.data || []
        if (confRes.status === 'fulfilled') this.confidenceDist = confRes.value.data || []
      } catch (e) {}
    },
    getBarHeight(count) {
      const max = Math.max(...this.trend.map(t => t.count), 1)
      return Math.max(10, (count / max) * 120)
    },
    getDistWidth(count) {
      const max = Math.max(...this.typeDistribution.map(t => t.count), 1)
      return Math.max(2, (count / max) * 100)
    },
    getConfidenceWidth(count) {
      const max = Math.max(...this.confidenceDist.map(t => t.count), 1)
      return Math.max(2, (count / max) * 100)
    },
    formatShortDate(dateStr) {
      if (!dateStr) return ''
      const parts = dateStr.split('-')
      return parts.length >= 3 ? `${parts[1]}/${parts[2]}` : dateStr
    }
  }
}
</script>

<style lang="scss" scoped>
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 24rpx;

  .stat-card {
    text-align: center;
    padding: 32rpx 16rpx;

    .stat-value {
      display: block;
      font-size: 56rpx;
      font-weight: bold;
      color: #1a6fc4;
      line-height: 1;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999999;
    }
  }
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333333;
  margin-bottom: 24rpx;
  padding-left: 12rpx;
  border-left: 6rpx solid #1a6fc4;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200rpx;
  padding: 0 8rpx;

  .trend-bar-wrap {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-end;
    gap: 8rpx;

    .trend-bar {
      width: 32rpx;
      background: #1a6fc4;
      border-radius: 4rpx 4rpx 0 0;
      min-height: 10rpx;
    }

    .trend-date {
      font-size: 18rpx;
      color: #999999;
    }

    .trend-count {
      font-size: 18rpx;
      color: #1a6fc4;
    }
  }
}

.type-dist {
  .dist-item {
    margin-bottom: 20rpx;

    .dist-label-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8rpx;

      .dist-type {
        font-size: 26rpx;
        color: #333333;
      }

      .dist-count {
        font-size: 26rpx;
        color: #1a6fc4;
        font-weight: 500;
      }
    }

    .dist-bar-bg {
      height: 16rpx;
      background: #f0f0f0;
      border-radius: 8rpx;
      overflow: hidden;

      .dist-bar {
        height: 100%;
        background: #1a6fc4;
        border-radius: 8rpx;
        transition: width 0.3s;
      }

      .dist-bar-green {
        background: #52c41a;
      }
    }
  }
}
</style>
