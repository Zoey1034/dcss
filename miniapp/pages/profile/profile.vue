<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="profile-header">
      <view class="avatar">👤</view>
      <view class="user-info">
        <text class="username">{{ userInfo.username || '未登录' }}</text>
        <text class="user-role">{{ userInfo.roleName || '-' }}</text>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-section">
        <view class="menu-item" @click="navigateTo('/pages/dashboard/dashboard')">
          <text class="menu-icon">📊</text>
          <text class="menu-title">数据概览</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="navigateTo('/pages/file-list/file-list')">
          <text class="menu-icon">📁</text>
          <text class="menu-title">我的文件</text>
          <text class="menu-arrow">→</text>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @click="handleAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-title">关于系统</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="handleLogout">
          <text class="menu-icon">🚪</text>
          <text class="menu-title">退出登录</text>
          <text class="menu-arrow">→</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version">
      <text class="version-text">Version 1.0.0</text>
    </view>
  </view>
</template>

<script>
import { authApi } from '@/common/api/index.js'
import { getUserInfo, clearUserInfo } from '@/common/utils/util.js'

export default {
  data() {
    return {
      userInfo: {}
    }
  },
  onShow() {
    this.userInfo = getUserInfo()
  },
  methods: {
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    handleAbout() {
      uni.showModal({
        title: '关于系统',
        content: '智能文档分类系统（DCSS）小程序端\n版本: 1.0.0',
        showCancel: false
      })
    },
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await authApi.logout()
              clearUserInfo()
              uni.reLaunch({
                url: '/pages/login/login'
              })
            } catch (error) {
              console.error('退出登录失败:', error)
              // 即使失败也清除本地信息
              clearUserInfo()
              uni.reLaunch({
                url: '/pages/login/login'
              })
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
  background-color: #f5f5f5;
}

.profile-header {
  display: flex;
  align-items: center;
  padding: 48rpx 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .avatar {
    width: 120rpx;
    height: 120rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 60rpx;
    font-size: 56rpx;
    margin-right: 24rpx;
  }

  .user-info {
    display: flex;
    flex-direction: column;

    .username {
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
      margin-bottom: 8rpx;
    }

    .user-role {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.menu-list {
  padding: 24rpx;
}

.menu-section {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .menu-icon {
    width: 48rpx;
    font-size: 36rpx;
    margin-right: 16rpx;
  }

  .menu-title {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }

  .menu-arrow {
    font-size: 32rpx;
    color: #999;
  }
}

.version {
  padding: 32rpx;
  text-align: center;

  .version-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
