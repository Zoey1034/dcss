<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="card user-card">
      <view class="avatar-wrap">
        <text class="avatar-icon">👤</text>
      </view>
      <view class="user-info">
        <text class="username">{{ userInfo.username || '未登录' }}</text>
        <text class="user-email">{{ userInfo.email || '' }}</text>
        <view class="tag tag-primary" style="margin-top:8rpx">
          {{ userInfo.role || userInfo.roleName || '普通用户' }}
        </view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="card menu-card">
      <view class="menu-item" @click="navigate('/pages/files/index')">
        <text class="menu-icon">📁</text>
        <text class="menu-text">文件管理</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="navigate('/pages/review/index')">
        <text class="menu-icon">📋</text>
        <text class="menu-text">审核管理</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="navigate('/pages/classification/index')">
        <text class="menu-icon">🏷️</text>
        <text class="menu-text">文档分类</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="navigate('/pages/dashboard/index')">
        <text class="menu-icon">📊</text>
        <text class="menu-text">数据面板</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 关于 -->
    <view class="card menu-card">
      <view class="menu-item">
        <text class="menu-icon">ℹ️</text>
        <text class="menu-text">关于系统</text>
        <text class="menu-value">智能文档处理系统 v1.0.0</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script>
import { authApi } from '@/utils/api'

export default {
  data() {
    return {
      userInfo: {}
    }
  },
  onShow() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      const stored = uni.getStorageSync('userInfo')
      if (stored) {
        try {
          this.userInfo = JSON.parse(stored)
        } catch (e) {
          this.userInfo = {}
        }
      }
    },
    navigate(url) {
      uni.switchTab({ url })
    },
    handleLogout() {
      uni.showModal({
        title: '退出确认',
        content: '确定要退出登录吗？',
        success: async ({ confirm }) => {
          if (confirm) {
            try {
              await authApi.logout()
            } catch (e) {}
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({ url: '/pages/login/index' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-card {
  display: flex;
  align-items: center;
  padding: 32rpx;
  margin-bottom: 24rpx;

  .avatar-wrap {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    background: #e8f0fe;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;
    flex-shrink: 0;

    .avatar-icon {
      font-size: 56rpx;
    }
  }

  .user-info {
    flex: 1;

    .username {
      display: block;
      font-size: 34rpx;
      font-weight: bold;
      color: #333333;
      margin-bottom: 8rpx;
    }

    .user-email {
      display: block;
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 8rpx;
    }
  }
}

.menu-card {
  padding: 0;
  margin-bottom: 24rpx;
  overflow: hidden;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 32rpx;
    border-bottom: 2rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .menu-icon {
      font-size: 40rpx;
      margin-right: 20rpx;
    }

    .menu-text {
      flex: 1;
      font-size: 28rpx;
      color: #333333;
    }

    .menu-arrow {
      font-size: 36rpx;
      color: #cccccc;
    }

    .menu-value {
      font-size: 24rpx;
      color: #999999;
    }
  }
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #fff2f0;
  color: #ff4d4f;
  border: 2rpx solid #ffccc7;
  border-radius: 12rpx;
  font-size: 30rpx;
  margin-top: 24rpx;
}
</style>
