<template>
  <view class="container">
    <view class="upload-area">
      <view class="upload-icon">📤</view>
      <text class="upload-text">点击选择文件上传</text>
      <button class="upload-btn" @click="chooseFile">选择文件</button>
    </view>

    <!-- 上传列表 -->
    <view class="upload-list" v-if="uploadList.length > 0">
      <view class="section-title">上传列表</view>
      <view
        class="upload-item"
        v-for="(item, index) in uploadList"
        :key="index"
      >
        <view class="file-info">
          <text class="file-name">{{ item.name }}</text>
          <text class="file-size">{{ item.size }}</text>
        </view>
        <view class="upload-status">
          <text v-if="item.status === 'uploading'">上传中...</text>
          <text v-if="item.status === 'success'" class="text-success">✓ 成功</text>
          <text v-if="item.status === 'error'" class="text-error">✗ 失败</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { fileApi } from '@/common/api/index.js'
import { formatFileSize } from '@/common/utils/util.js'

export default {
  data() {
    return {
      uploadList: []
    }
  },
  methods: {
    chooseFile() {
      uni.chooseMessageFile({
        count: 5,
        type: 'file',
        success: (res) => {
          const files = res.tempFiles
          files.forEach(file => {
            this.uploadFile(file)
          })
        }
      })
    },
    async uploadFile(file) {
      const uploadItem = {
        name: file.name,
        size: formatFileSize(file.size),
        status: 'uploading'
      }
      this.uploadList.push(uploadItem)

      try {
        await fileApi.upload(file.path)
        uploadItem.status = 'success'
        uni.showToast({
          title: '上传成功',
          icon: 'success'
        })
      } catch (error) {
        uploadItem.status = 'error'
        console.error('上传失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  padding: 24rpx;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 80rpx;
  border: 2rpx dashed #ddd;

  .upload-icon {
    font-size: 96rpx;
    margin-bottom: 24rpx;
  }

  .upload-text {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 32rpx;
  }

  .upload-btn {
    width: 240rpx;
    height: 72rpx;
    line-height: 72rpx;
    background-color: #007aff;
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;
    border: none;
  }
}

.upload-list {
  margin-top: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.upload-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 16rpx;

  .file-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .file-name {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 8rpx;
    }

    .file-size {
      font-size: 24rpx;
      color: #999;
    }
  }

  .upload-status {
    font-size: 24rpx;

    .text-success {
      color: #34c759;
    }

    .text-error {
      color: #ff3b30;
    }
  }
}
</style>
