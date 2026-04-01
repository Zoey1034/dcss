<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- Logo 和标题区域 -->
    <view class="header-section">
      <view class="logo-wrapper">
        <view class="logo">
          <text class="logo-icon">📄</text>
        </view>
      </view>
      <text class="app-title gradient-text">智能文档处理系统</text>
      <text class="app-subtitle">Document Classification & Storage System</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-section">
      <view class="form-card">
        <view class="form-header">
          <text class="form-title">欢迎登录</text>
          <text class="form-subtitle">请输入您的账号信息</text>
        </view>

        <view class="form-body">
          <view class="input-group">
            <view class="input-icon">👤</view>
            <input
              v-model="form.username"
              class="form-input"
              placeholder="用户名"
              placeholder-class="input-placeholder"
              maxlength="50"
            />
          </view>

          <view class="input-group">
            <view class="input-icon">🔒</view>
            <input
              v-model="form.password"
              class="form-input"
              placeholder="密码"
              placeholder-class="input-placeholder"
              password
              maxlength="50"
            />
          </view>

          <button
            class="login-btn"
            :class="{ 'btn-loading': loading }"
            :disabled="loading"
            @click="handleLogin"
          >
            <text v-if="!loading">登 录</text>
            <text v-else>登录中...</text>
          </button>

          <view class="form-footer">
            <text class="register-link" @click="showRegisterModal">
              还没有账号？立即注册 →
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 注册弹窗 -->
    <view v-if="showRegister" class="modal-mask" @click.self="closeRegisterModal">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">注册新账号</text>
          <text class="modal-close" @click="closeRegisterModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="input-group">
            <view class="input-icon">👤</view>
            <input
              v-model="regForm.username"
              class="form-input"
              placeholder="请输入用户名"
              placeholder-class="input-placeholder"
            />
          </view>

          <view class="input-group">
            <view class="input-icon">🔒</view>
            <input
              v-model="regForm.password"
              class="form-input"
              placeholder="请设置密码"
              placeholder-class="input-placeholder"
              password
            />
          </view>

          <view class="input-group">
            <view class="input-icon">✉️</view>
            <input
              v-model="regForm.email"
              class="form-input"
              placeholder="邮箱（可选）"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn-ghost modal-btn" @click="closeRegisterModal">取消</button>
          <button
            class="btn-primary modal-btn"
            :loading="regLoading"
            @click="handleRegister"
          >
            注册
          </button>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">v1.0.0</text>
    </view>
  </view>
</template>

<script>
import { authApi } from '@/utils/api'

export default {
  data() {
    return {
      statusBarHeight: 0,
      form: { username: '', password: '' },
      regForm: { username: '', password: '', email: '' },
      loading: false,
      regLoading: false,
      showRegister: false
    }
  },
  onLoad() {
    // 获取系统状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 0
  },
  methods: {
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        return uni.showToast({
          title: '请输入用户名和密码',
          icon: 'none',
          duration: 2000
        })
      }

      this.loading = true
      try {
        const res = await authApi.login(this.form)
        const token = res.data?.token || res.token
        uni.setStorageSync('token', token)

        const meRes = await authApi.getMe()
        uni.setStorageSync('userInfo', JSON.stringify(meRes.data))

        uni.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500
        })

        setTimeout(() => {
          uni.reLaunch({ url: '/pages/files/index' })
        }, 1500)
      } catch (err) {
        console.error('Login error:', err)
      } finally {
        this.loading = false
      }
    },
    showRegisterModal() {
      this.showRegister = true
      this.regForm = { username: '', password: '', email: '' }
    },
    closeRegisterModal() {
      this.showRegister = false
    },
    async handleRegister() {
      if (!this.regForm.username || !this.regForm.password) {
        return uni.showToast({
          title: '请填写用户名和密码',
          icon: 'none',
          duration: 2000
        })
      }

      this.regLoading = true
      try {
        await authApi.register(this.regForm)
        uni.showToast({
          title: '注册成功',
          icon: 'success',
          duration: 2000
        })

        this.form.username = this.regForm.username
        this.closeRegisterModal()
      } catch (err) {
        console.error('Register error:', err)
      } finally {
        this.regLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.login-container {
  position: relative;
  min-height: 100vh;
  background: $gradient-blue;
  overflow: hidden;
}

/* ============ 背景装饰 ============ */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;

  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 20s ease-in-out infinite;

    &.circle-1 {
      width: 400rpx;
      height: 400rpx;
      top: -100rpx;
      right: -100rpx;
      animation-delay: 0s;
    }

    &.circle-2 {
      width: 300rpx;
      height: 300rpx;
      bottom: 100rpx;
      left: -80rpx;
      animation-delay: 7s;
    }

    &.circle-3 {
      width: 200rpx;
      height: 200rpx;
      top: 50%;
      right: 20rpx;
      animation-delay: 14s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-60rpx) scale(1.1);
    opacity: 0.6;
  }
}

.status-bar {
  width: 100%;
}

/* ============ 头部区域 ============ */
.header-section {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: $spacing-4xl $spacing-2xl;
  padding-top: $spacing-3xl;

  .logo-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: $spacing-xl;
  }

  .logo {
    width: 160rpx;
    height: 160rpx;
    background: rgba(255, 255, 255, 0.95);
    border-radius: $border-radius-2xl;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $box-shadow-xl;
    animation: bounce 2s ease-in-out infinite;

    .logo-icon {
      font-size: 80rpx;
    }
  }

  .app-title {
    display: block;
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $white;
    margin-bottom: $spacing-sm;
    letter-spacing: 2rpx;
  }

  .app-subtitle {
    display: block;
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.85);
    letter-spacing: 1rpx;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

/* ============ 表单区域 ============ */
.form-section {
  position: relative;
  z-index: 1;
  padding: 0 $spacing-2xl;
}

.form-card {
  background: $white;
  border-radius: $border-radius-2xl;
  padding: $spacing-3xl;
  box-shadow: $box-shadow-xl;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(60rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: $spacing-3xl;

  .form-title {
    display: block;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $text-color;
    margin-bottom: $spacing-sm;
  }

  .form-subtitle {
    display: block;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

/* ============ 输入框 ============ */
.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: $bg-color;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-md;
  padding: 0 $spacing-lg;
  margin-bottom: $spacing-lg;
  transition: all $transition-base $transition-ease;

  &:focus-within {
    border-color: $primary-color;
    background: $primary-lighter;
    box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.1);
  }

  .input-icon {
    font-size: $font-size-xl;
    margin-right: $spacing-md;
    flex-shrink: 0;
  }

  .form-input {
    flex: 1;
    height: 88rpx;
    font-size: $font-size-base;
    color: $text-color;
    border: none;
    background: transparent;
  }

  .input-placeholder {
    color: $text-placeholder;
  }
}

/* ============ 登录按钮 ============ */
.login-btn {
  width: 100%;
  height: 96rpx;
  background: $gradient-blue;
  color: $white;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  box-shadow: $box-shadow-md;
  margin-top: $spacing-lg;
  transition: all $transition-base $transition-ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.2);
    opacity: 0;
    transition: opacity $transition-base $transition-ease;
  }

  &:active::before {
    opacity: 1;
  }

  &.btn-loading {
    opacity: 0.8;
  }

  &[disabled] {
    opacity: 0.6;
  }
}

.form-footer {
  text-align: center;
  margin-top: $spacing-xl;

  .register-link {
    font-size: $font-size-sm;
    color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

/* ============ 注册弹窗 ============ */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: $z-index-modal-backdrop;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  background: $white;
  border-radius: $border-radius-2xl;
  width: 100%;
  max-width: 600rpx;
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xl $spacing-2xl;
  border-bottom: 1rpx solid $border-light;

  .modal-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-color;
  }

  .modal-close {
    font-size: $font-size-2xl;
    color: $text-tertiary;
    padding: $spacing-sm;
    line-height: 1;
  }
}

.modal-body {
  padding: $spacing-2xl;
}

.modal-footer {
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-xl $spacing-2xl;
  border-top: 1rpx solid $border-light;

  .modal-btn {
    flex: 1;
    height: 80rpx;
    font-size: $font-size-base;
    line-height: 80rpx;
    padding: 0;
  }
}

/* ============ 版本信息 ============ */
.version-info {
  position: absolute;
  bottom: $spacing-3xl;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 1;

  .version-text {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>
