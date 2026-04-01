<template>
  <view class="container">
    <!-- 文档类型筛选 -->
    <scroll-view scroll-x class="type-scroll">
      <view class="type-list">
        <text
          v-for="type in types"
          :key="type.documentType"
          :class="['type-item', selectedType === type.documentType ? 'type-item-active' : '']"
          @click="selectType(type.documentType)"
        >
          {{ type.documentType }}（{{ type.count }}）
        </text>
      </view>
    </scroll-view>

    <!-- 搜索栏 -->
    <view class="search-bar card">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索关键词..."
        @confirm="loadList"
      />
      <button class="btn-primary search-btn" @click="loadList">搜索</button>
    </view>

    <!-- 列表 -->
    <view v-if="loading" class="empty-state">
      <text class="empty-icon">⏳</text>
      <text class="empty-text">加载中...</text>
    </view>

    <view v-else-if="list.length === 0" class="empty-state">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无数据</text>
    </view>

    <view v-else>
      <view v-for="item in list" :key="item.id" class="card class-card" @click="openDetail(item)">
        <view class="class-header">
          <text class="doc-type tag tag-primary">{{ item.documentType }}</text>
          <text class="confidence">置信度：{{ (item.confidence * 100).toFixed(0) }}%</text>
        </view>
        <text class="file-name">{{ item.originalName || item.fileName }}</text>
        <view class="class-meta">
          <text class="meta-text">识别时间：{{ formatDate(item.processTime || item.createTime) }}</text>
        </view>
      </view>

      <view class="pagination">
        <text class="page-btn" :class="{ disabled: page <= 1 }" @click="prevPage">上一页</text>
        <text class="page-info">{{ page }} / {{ totalPages }}</text>
        <text class="page-btn" :class="{ disabled: page >= totalPages }" @click="nextPage">下一页</text>
      </view>
    </view>

    <!-- 详情弹窗 -->
    <view v-if="detailItem" class="detail-mask" @click.self="detailItem = null">
      <view class="detail-modal">
        <view class="modal-header">
          <text class="modal-title">文档详情</text>
          <text class="modal-close" @click="detailItem = null">✕</text>
        </view>
        <scroll-view scroll-y class="detail-body">
          <view v-if="detailLoading" class="empty-state">
            <text class="empty-text">加载中...</text>
          </view>
          <view v-else-if="detail">
            <view class="detail-section">
              <text class="detail-label">文档类型</text>
              <text class="detail-value">{{ detail.documentType }}</text>
            </view>
            <view class="detail-section">
              <text class="detail-label">置信度</text>
              <text class="detail-value">{{ (detail.confidence * 100).toFixed(1) }}%</text>
            </view>
            <view class="detail-section">
              <text class="detail-label">文件名</text>
              <text class="detail-value">{{ detail.originalName || detail.fileName }}</text>
            </view>
            <view v-if="detail.extractedInfo" class="detail-section">
              <text class="detail-label">提取信息</text>
              <view class="extracted-info">
                <view
                  v-for="(val, key) in detail.extractedInfo"
                  :key="key"
                  class="info-row"
                >
                  <text class="info-key">{{ key }}</text>
                  <text class="info-val">{{ val }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
import { classificationApi } from '@/utils/api'

export default {
  data() {
    return {
      types: [],
      selectedType: '',
      keyword: '',
      list: [],
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      detailItem: null,
      detail: null,
      detailLoading: false
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.total / this.pageSize))
    }
  },
  onShow() {
    this.loadTypes()
    this.loadList()
  },
  methods: {
    async loadTypes() {
      try {
        const res = await classificationApi.getTypes()
        this.types = res.data || []
      } catch (e) {}
    },
    async loadList() {
      this.loading = true
      try {
        const res = await classificationApi.getList({
          page: this.page,
          pageSize: this.pageSize,
          documentType: this.selectedType,
          keyword: this.keyword
        })
        const data = res.data || res
        this.list = data.records || data.list || []
        this.total = data.total || 0
      } finally {
        this.loading = false
      }
    },
    selectType(type) {
      this.selectedType = this.selectedType === type ? '' : type
      this.page = 1
      this.loadList()
    },
    prevPage() {
      if (this.page > 1) { this.page--; this.loadList() }
    },
    nextPage() {
      if (this.page < this.totalPages) { this.page++; this.loadList() }
    },
    async openDetail(item) {
      this.detailItem = item
      this.detail = null
      this.detailLoading = true
      try {
        const res = await classificationApi.getDetail(item.fileId || item.id)
        this.detail = res.data
      } finally {
        this.detailLoading = false
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.split(' ')[0]
    }
  }
}
</script>

<style lang="scss" scoped>
.type-scroll {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.type-list {
  display: inline-flex;
  gap: 16rpx;
  padding: 8rpx 0;
}

.type-item {
  display: inline-block;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  font-size: 24rpx;
  color: #666666;
  background: #ffffff;
  border: 2rpx solid #e8e8e8;
  white-space: nowrap;

  &.type-item-active {
    background: #1a6fc4;
    color: #ffffff;
    border-color: #1a6fc4;
  }
}

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

.class-card {
  cursor: pointer;

  .class-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;

    .confidence {
      font-size: 22rpx;
      color: #999999;
    }
  }

  .file-name {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #333333;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .class-meta {
    .meta-text {
      font-size: 22rpx;
      color: #999999;
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

    &.disabled { color: #cccccc; border-color: #e8e8e8; }
  }

  .page-info { font-size: 26rpx; color: #666666; }
}

.detail-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.detail-modal {
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;

  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .modal-title { font-size: 32rpx; font-weight: bold; color: #333333; }
    .modal-close { font-size: 32rpx; color: #999999; }
  }

  .detail-body {
    flex: 1;
    padding: 24rpx 32rpx;

    .detail-section {
      padding: 16rpx 0;
      border-bottom: 2rpx solid #f5f5f5;

      .detail-label {
        display: block;
        font-size: 24rpx;
        color: #999999;
        margin-bottom: 8rpx;
      }

      .detail-value {
        font-size: 28rpx;
        color: #333333;
      }
    }

    .extracted-info {
      .info-row {
        display: flex;
        padding: 8rpx 0;

        .info-key {
          width: 160rpx;
          font-size: 24rpx;
          color: #999999;
          flex-shrink: 0;
        }

        .info-val {
          flex: 1;
          font-size: 24rpx;
          color: #333333;
        }
      }
    }
  }
}
</style>
