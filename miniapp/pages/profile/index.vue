<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="profile-header card">
      <view class="avatar-section">
        <view class="avatar-wrapper">
          <view class="avatar">
            <text class="avatar-text">{{ getInitial() }}</text>
          </view>
          <view class="avatar-badge">✓</view>
        </view>
      </view>

      <view class="user-info-section">
        <text class="username">{{ userInfo.username || '未登录' }}</text>
        <text v-if="userInfo.email" class="user-email">{{ userInfo.email }}</text>
        <view class="user-role">
          <view class="role-badge tag tag-primary">
            {{ userInfo.role || userInfo.roleName || '普通用户' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="section-title">快捷入口</view>
    <view class="menu-grid">
      <view class="menu-item card" @click="navigate('/pages/files/index')">
        <view class="menu-icon">📁</view>
        <text class="menu-text">文件管理</text>
      </view>
      <view class="menu-item card" @click="navigate('/pages/review/index')">
        <view class="menu-icon">📋</view>
        <text class="menu-text">审核管理</text>
      </view>
      <view class="menu-item card" @click="navigate('/pages/classification/index')">
        <view class="menu-icon">🏷️</view>
        <text class="menu-text">文档分类</text>
      </view>
      <view class="menu-item card" @click="navigate('/pages/dashboard/index')">
        <view class="menu-icon">📊</view>
        <text class="menu-text">数据面板</text>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="section-title">设置</view>
    <view class="settings-list card">
      <view class="settings-item">
        <view class="settings-left">
          <text class="settings-icon">🔔</text>
          <text class="settings-text">消息通知</text>
        </view>
        <text class="settings-arrow">›</text>
      </view>
      <view class="divider"></view>
      <view class="settings-item">
        <view class="settings-left">
          <text class="settings-icon">ℹ️</text>
          <text class="settings-text">关于系统</text>
        </view>
        <view class="settings-right">
          <text class="settings-value">v1.0.0</text>
          <text class="settings-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="handleLogout">
      <text class="logout-icon">👋</text>
      <text class="logout-text">退出登录</text>
    </button>

    <!-- 底部信息 -->
    <view class="footer-info">
      <text class="footer-text">智能文档处理系统</text>
      <text class="footer-text">DCSS © 2026</text>
    </view>
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
    getInitial() {
      const username = this.userInfo.username || 'U'
      return username.charAt(0).toUpperCase()
    },
    navigate(url) {
      uni.switchTab({ url })
    },
    handleLogout() {
      uni.showModal({
        title: '退出确认',
        content: '确定要退出登录吗？',
        confirmColor: '#ef4444',
        success: async ({ confirm }) => {
          if (confirm) {
            uni.showLoading({ title: '退出中...', mask: true })
            try {
              await authApi.logout()
            } catch (e) {
              // Ignore logout API error
            }
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.hideLoading()
            uni.reLaunch({ url: '/pages/login/index' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

/* ============ 用户信息卡片 ============ */
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-3xl $spacing-xl;
  margin-bottom: $spacing-xl;
  background: $gradient-blue;
  color: $white;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400rpx;
    height: 400rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }
}

.avatar-section {
  position: relative;
  z-index: 1;
  margin-bottom: $spacing-lg;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $box-shadow-xl;
  border: 6rpx solid rgba(255, 255, 255, 0.5);

  .avatar-text {
    font-size: 64rpx;
    font-weight: $font-weight-bold;
    color: $primary-color;
  }
}

.avatar-badge {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: $success-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-md;
  border: 4rpx solid $white;
  box-shadow: $box-shadow-sm;
}

.user-info-section {
  position: relative;
  z-index: 1;
  text-align: center;

  .username {
    display: block;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $white;
    margin-bottom: $spacing-sm;
    text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  }

  .user-email {
    display: block;
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: $spacing-md;
  }

  .user-role {
    .role-badge {
      background: rgba(255, 255, 255, 0.95);
      border: none;
    }
  }
}

/* ============ 快捷入口 ============ */
.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-color;
  margin: $spacing-xl 0 $spacing-lg;
  padding-left: $spacing-xs;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.menu-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl $spacing-lg;
  min-height: 200rpx;
  transition: all $transition-base $transition-ease;

  &:active {
    transform: scale(0.95);
  }

  .menu-icon {
    font-size: 80rpx;
    margin-bottom: $spacing-md;
  }

  .menu-text {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color;
  }
}

/* ============ 设置列表 ============ */
.settings-list {
  padding: 0;
  margin-bottom: $spacing-xl;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xl $spacing-lg;
  transition: background $transition-base $transition-ease;

  &:active {
    background: $bg-color;
  }

  .settings-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .settings-icon {
      font-size: $font-size-2xl;
    }

    .settings-text {
      font-size: $font-size-base;
      color: $text-color;
    }
  }

  .settings-right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    .settings-value {
      font-size: $font-size-sm;
      color: $text-tertiary;
    }
  }

  .settings-arrow {
    font-size: $font-size-2xl;
    color: $text-placeholder;
    line-height: 1;
  }
}

/* ============ 退出按钮 ============ */
.logout-btn {
  width: 100%;
  height: 96rpx;
  background: $white;
  color: $danger-color;
  border: 2rpx solid $danger-color;
  border-radius: $border-radius-lg;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-3xl;
  transition: all $transition-base $transition-ease;

  &:active {
    background: $danger-light;
  }

  .logout-icon {
    font-size: $font-size-xl;
  }

  .logout-text {
    font-size: $font-size-lg;
  }
}

/* ============ 底部信息 ============ */
.footer-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-2xl 0;

  .footer-text {
    font-size: $font-size-xs;
    color: $text-placeholder;
    line-height: $line-height-relaxed;
  }
}
</style>
