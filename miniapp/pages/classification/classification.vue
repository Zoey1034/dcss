<template>
  <view class="container">
    <!-- 分类统计 -->
    <view class="stats-section">
      <view
        class="stats-item"
        v-for="item in classificationStats"
        :key="item.type"
        @click="viewList(item.type)"
      >
        <view class="stats-count">{{ item.count }}</view>
        <view class="stats-label">{{ item.type }}</view>
      </view>
    </view>

    <!-- 分类列表 -->
    <view class="classification-list">
      <view class="section-title">文档列表</view>
      <scroll-view
        scroll-y
        class="list-scroll"
        @scrolltolower="loadMore"
      >
        <view
          class="list-item"
          v-for="item in documentList"
          :key="item.id"
          @click="viewDetail(item.id)"
        >
          <view class="item-type">{{ item.documentType }}</view>
          <view class="item-info">
            <text class="item-name">{{ item.fileName }}</text>
            <text class="item-time">{{ item.createTime }}</text>
          </view>
        </view>

        <view class="empty" v-if="documentList.length === 0 && !loading">
          <text class="empty-text">暂无数据</text>
        </view>

        <view class="loading" v-if="loading">
          <text>加载中...</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { classificationApi } from '@/common/api/index.js'
import { formatDate } from '@/common/utils/util.js'

export default {
  data() {
    return {
      classificationStats: [],
      documentList: [],
      loading: false,
      currentType: null
    }
  },
  onShow() {
    this.loadStats()
    this.loadList()
  },
  methods: {
    async loadStats() {
      try {
        const stats = await classificationApi.stats()
        this.classificationStats = stats
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    },
    async loadList() {
      this.loading = true
      try {
        const params = {
          page: 1,
          size: 50
        }
        if (this.currentType) {
          params.documentType = this.currentType
        }

        const res = await classificationApi.list(params)
        this.documentList = res.records.map(item => ({
          ...item,
          createTime: formatDate(item.createTime, 'MM-dd HH:mm')
        }))
      } catch (error) {
        console.error('加载列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    viewList(type) {
      this.currentType = type
      this.loadList()
    },
    viewDetail(id) {
      uni.navigateTo({
        url: `/pages/review-detail/review-detail?id=${id}`
      })
    },
    loadMore() {
      // 实现加载更多
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
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  padding: 24rpx;
}

.stats-item {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  text-align: center;

  .stats-count {
    font-size: 40rpx;
    font-weight: bold;
    color: #007aff;
    margin-bottom: 8rpx;
  }

  .stats-label {
    font-size: 24rpx;
    color: #666;
  }
}

.classification-list {
  padding: 0 24rpx 24rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 16rpx;
  }

  .list-scroll {
    height: calc(100vh - 400rpx);
  }

  .list-item {
    display: flex;
    align-items: center;
    background-color: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;

    .item-type {
      width: 120rpx;
      padding: 8rpx 16rpx;
      background-color: #f0f0f0;
      border-radius: 8rpx;
      font-size: 24rpx;
      color: #666;
      text-align: center;
      margin-right: 16rpx;
    }

    .item-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .item-name {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 8rpx;
      }

      .item-time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.empty,
.loading {
  padding: 80rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #999;
}
</style>
