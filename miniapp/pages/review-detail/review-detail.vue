<template>
  <view class="container">
    <view class="detail-card" v-if="detail">
      <!-- 文件信息 -->
      <view class="section">
        <view class="section-title">文件信息</view>
        <view class="info-row">
          <text class="label">文件名:</text>
          <text class="value">{{ detail.fileName }}</text>
        </view>
        <view class="info-row">
          <text class="label">文档类型:</text>
          <text class="value">{{ detail.documentType }}</text>
        </view>
        <view class="info-row">
          <text class="label">上传时间:</text>
          <text class="value">{{ detail.createTime }}</text>
        </view>
        <view class="info-row">
          <text class="label">可信度:</text>
          <text class="value confidence">{{ detail.confidence }}%</text>
        </view>
      </view>

      <!-- 提取信息 -->
      <view class="section" v-if="detail.extractInfo">
        <view class="section-title">提取信息</view>
        <view
          class="info-row"
          v-for="(value, key) in detail.extractInfo"
          :key="key"
        >
          <text class="label">{{ key }}:</text>
          <text class="value">{{ value }}</text>
        </view>
      </view>

      <!-- 审核操作 -->
      <view class="actions" v-if="detail.status === 2">
        <button class="btn btn-save" @click="handleSave">保存草稿</button>
        <button class="btn btn-submit" @click="handleSubmit">提交审核</button>
      </view>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
import { reviewApi } from '@/common/api/index.js'
import { formatDate } from '@/common/utils/util.js'

export default {
  data() {
    return {
      id: '',
      detail: null,
      loading: false
    }
  },
  onLoad(options) {
    this.id = options.id
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      this.loading = true
      try {
        const res = await reviewApi.detail(this.id)
        this.detail = {
          ...res,
          createTime: formatDate(res.createTime, 'yyyy-MM-dd HH:mm:ss'),
          confidence: res.confidence ? (res.confidence * 100).toFixed(1) : 0
        }
      } catch (error) {
        console.error('加载详情失败:', error)
      } finally {
        this.loading = false
      }
    },
    async handleSave() {
      try {
        await reviewApi.save({
          id: this.id,
          extractInfo: this.detail.extractInfo
        })
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('保存失败:', error)
      }
    },
    async handleSubmit() {
      uni.showModal({
        title: '确认提交',
        content: '确定要提交审核吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await reviewApi.submit({
                id: this.id,
                extractInfo: this.detail.extractInfo,
                approved: true
              })
              uni.showToast({
                title: '提交成功',
                icon: 'success'
              })
              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            } catch (error) {
              console.error('提交失败:', error)
            }
          }
        }
      })
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

.detail-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section {
  margin-bottom: 32rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }

  .info-row {
    display: flex;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 200rpx;
      font-size: 28rpx;
      color: #666;
    }

    .value {
      flex: 1;
      font-size: 28rpx;
      color: #333;

      &.confidence {
        color: #ff9500;
        font-weight: bold;
      }
    }
  }
}

.actions {
  display: flex;
  margin-top: 32rpx;
  gap: 16rpx;

  .btn {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 12rpx;
    font-size: 32rpx;
    border: none;

    &.btn-save {
      background-color: #f5f5f5;
      color: #666;
    }

    &.btn-submit {
      background-color: #007aff;
      color: #fff;
    }
  }
}

.loading {
  padding: 80rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #999;
}
</style>
