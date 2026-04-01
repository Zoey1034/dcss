<template>
  <view class="container">
    <scroll-view
      class="review-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view
        class="review-item"
        v-for="item in reviewList"
        :key="item.id"
        @click="viewDetail(item.id)"
      >
        <view class="review-header">
          <text class="file-name">{{ item.fileName }}</text>
          <text class="file-type">{{ item.documentType }}</text>
        </view>
        <view class="review-info">
          <text class="info-item">上传时间: {{ item.createTime }}</text>
          <text class="info-item">处理状态: {{ item.processStatus }}</text>
        </view>
        <view class="review-footer">
          <text class="confidence">可信度: {{ item.confidence }}%</text>
          <view class="action-btn">去审核 →</view>
        </view>
      </view>

      <view class="empty" v-if="reviewList.length === 0 && !loading">
        <text class="empty-text">暂无待审核任务</text>
      </view>

      <view class="loading" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <view class="no-more" v-if="noMore && reviewList.length > 0">
        <text class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { reviewApi } from '@/common/api/index.js'
import { formatDate } from '@/common/utils/util.js'

export default {
  data() {
    return {
      reviewList: [],
      loading: false,
      noMore: false,
      page: 1,
      pageSize: 20
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    async loadData(isLoadMore = false) {
      if (this.loading) return
      if (isLoadMore && this.noMore) return

      this.loading = true
      try {
        const params = {
          page: isLoadMore ? this.page + 1 : 1,
          size: this.pageSize,
          status: 2 // 待审核状态
        }

        const res = await reviewApi.list(params)
        const formattedList = res.records.map(item => ({
          ...item,
          createTime: formatDate(item.createTime, 'yyyy-MM-dd HH:mm'),
          confidence: item.confidence ? (item.confidence * 100).toFixed(1) : 0
        }))

        if (isLoadMore) {
          this.reviewList = [...this.reviewList, ...formattedList]
          this.page++
        } else {
          this.reviewList = formattedList
          this.page = 1
        }

        this.noMore = res.records.length < this.pageSize
      } catch (error) {
        console.error('加载待审核列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      this.loadData(true)
    },
    viewDetail(id) {
      uni.navigateTo({
        url: `/pages/review-detail/review-detail?id=${id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  background-color: #f5f5f5;
}

.review-list {
  height: 100%;
  padding: 24rpx;
}

.review-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;

  .review-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16rpx;

    .file-name {
      flex: 1;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .file-type {
      padding: 4rpx 16rpx;
      background-color: #f0f0f0;
      border-radius: 8rpx;
      font-size: 24rpx;
      color: #666;
    }
  }

  .review-info {
    display: flex;
    flex-direction: column;
    margin-bottom: 16rpx;

    .info-item {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 8rpx;
    }
  }

  .review-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .confidence {
      font-size: 28rpx;
      color: #ff9500;
    }

    .action-btn {
      color: #007aff;
      font-size: 28rpx;
    }
  }
}

.empty,
.loading,
.no-more {
  padding: 80rpx 0;
  text-align: center;

  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
