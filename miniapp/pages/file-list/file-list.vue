<template>
  <view class="container">
    <!-- 搜索和筛选 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索文件名..."
        @confirm="handleSearch"
      />
      <button class="filter-btn" @click="showFilter = true">筛选</button>
    </view>

    <!-- 文件列表 -->
    <scroll-view
      class="file-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view
        class="file-item"
        v-for="item in fileList"
        :key="item.id"
        @click="viewDetail(item.id)"
      >
        <view class="file-icon">📄</view>
        <view class="file-info">
          <text class="file-name">{{ item.fileName }}</text>
          <text class="file-meta">{{ item.fileSize }} | {{ item.createTime }}</text>
        </view>
        <view class="file-status" :style="{ color: getStatusColor(item.status) }">
          {{ getStatusText(item.status) }}
        </view>
      </view>

      <view class="empty" v-if="fileList.length === 0 && !loading">
        <text class="empty-text">暂无文件</text>
      </view>

      <view class="loading" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <view class="no-more" v-if="noMore && fileList.length > 0">
        <text class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 筛选弹窗 -->
    <view class="filter-popup" v-if="showFilter" @click="showFilter = false">
      <view class="filter-content" @click.stop>
        <view class="filter-title">筛选条件</view>

        <view class="filter-item">
          <text class="filter-label">文件状态</text>
          <picker
            mode="selector"
            :range="statusOptions"
            range-key="text"
            :value="filterStatus"
            @change="onStatusChange"
          >
            <view class="picker">{{ statusOptions[filterStatus].text }}</view>
          </picker>
        </view>

        <view class="filter-actions">
          <button class="filter-reset" @click="handleReset">重置</button>
          <button class="filter-confirm" @click="handleConfirm">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { fileApi } from '@/common/api/index.js'
import { formatDate, formatFileSize, getFileStatusText, getFileStatusColor } from '@/common/utils/util.js'

export default {
  data() {
    return {
      searchKeyword: '',
      fileList: [],
      loading: false,
      noMore: false,
      page: 1,
      pageSize: 20,
      showFilter: false,
      filterStatus: 0,
      statusOptions: [
        { value: null, text: '全部' },
        { value: 1, text: '处理中' },
        { value: 2, text: '待审核' },
        { value: 3, text: '已审核' },
        { value: 4, text: '已驳回' }
      ]
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
          size: this.pageSize
        }

        if (this.searchKeyword) {
          params.fileName = this.searchKeyword
        }

        const status = this.statusOptions[this.filterStatus].value
        if (status !== null) {
          params.status = status
        }

        const res = await fileApi.list(params)
        const formattedList = res.records.map(item => ({
          ...item,
          createTime: formatDate(item.createTime, 'yyyy-MM-dd HH:mm'),
          fileSize: formatFileSize(item.fileSize)
        }))

        if (isLoadMore) {
          this.fileList = [...this.fileList, ...formattedList]
          this.page++
        } else {
          this.fileList = formattedList
          this.page = 1
        }

        this.noMore = res.records.length < this.pageSize
      } catch (error) {
        console.error('加载文件列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      this.loadData(true)
    },
    handleSearch() {
      this.loadData()
    },
    handleReset() {
      this.filterStatus = 0
      this.searchKeyword = ''
      this.showFilter = false
      this.loadData()
    },
    handleConfirm() {
      this.showFilter = false
      this.loadData()
    },
    onStatusChange(e) {
      this.filterStatus = e.detail.value
    },
    viewDetail(id) {
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
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.search-bar {
  display: flex;
  padding: 24rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .search-input {
    flex: 1;
    height: 64rpx;
    padding: 0 24rpx;
    background-color: #f5f5f5;
    border-radius: 32rpx;
    font-size: 28rpx;
  }

  .filter-btn {
    width: 120rpx;
    height: 64rpx;
    line-height: 64rpx;
    margin-left: 16rpx;
    background-color: #007aff;
    color: #fff;
    border-radius: 32rpx;
    font-size: 28rpx;
    border: none;
  }
}

.file-list {
  flex: 1;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: #fff;
  margin-bottom: 16rpx;

  .file-icon {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    margin-right: 16rpx;
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

    .file-meta {
      font-size: 24rpx;
      color: #999;
    }
  }

  .file-status {
    font-size: 24rpx;
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

.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.filter-content {
  width: 100%;
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;

  .filter-title {
    font-size: 32rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 32rpx;
  }

  .filter-item {
    margin-bottom: 24rpx;

    .filter-label {
      display: block;
      font-size: 28rpx;
      color: #666;
      margin-bottom: 12rpx;
    }

    .picker {
      height: 72rpx;
      line-height: 72rpx;
      padding: 0 24rpx;
      background-color: #f5f5f5;
      border-radius: 12rpx;
      font-size: 28rpx;
    }
  }

  .filter-actions {
    display: flex;
    margin-top: 32rpx;

    button {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      border-radius: 12rpx;
      font-size: 32rpx;
      border: none;

      &.filter-reset {
        background-color: #f5f5f5;
        color: #666;
        margin-right: 16rpx;
      }

      &.filter-confirm {
        background-color: #007aff;
        color: #fff;
      }
    }
  }
}
</style>
