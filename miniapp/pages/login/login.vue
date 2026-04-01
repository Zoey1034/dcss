<template>
  <view class="login-container">
    <view class="login-card">
      <view class="logo">
        <text class="logo-text">智能文档分类系统</text>
        <text class="logo-subtitle">DCSS</text>
      </view>

      <view class="form">
        <view class="form-item">
          <view class="form-label">用户名</view>
          <input
            class="form-input"
            v-model="username"
            placeholder="请输入用户名"
            placeholder-style="color: #999;"
          />
        </view>

        <view class="form-item">
          <view class="form-label">密码</view>
          <input
            class="form-input"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            placeholder-style="color: #999;"
            @confirm="handleLogin"
          />
        </view>

        <button class="login-btn" @click="handleLogin" :loading="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { authApi } from '@/common/api/index.js'
import { setUserInfo } from '@/common/utils/util.js'

export default {
  data() {
    return {
      username: '',
      password: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.username) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return
      }

      if (!this.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await authApi.login({
          username: this.username,
          password: this.password
        })

        // 保存token和用户信息
        uni.setStorageSync('token', res.token)
        setUserInfo(res.userInfo)

        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })

        // 跳转到首页
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }, 1500)
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
}

.login-card {
  width: 100%;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
}

.logo {
  text-align: center;
  margin-bottom: 60rpx;

  .logo-text {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }

  .logo-subtitle {
    display: block;
    font-size: 28rpx;
    color: #999;
  }
}

.form {
  .form-item {
    margin-bottom: 32rpx;

    .form-label {
      font-size: 28rpx;
      color: #666;
      margin-bottom: 12rpx;
    }

    .form-input {
      width: 100%;
      height: 88rpx;
      padding: 0 24rpx;
      background-color: #f5f5f5;
      border-radius: 12rpx;
      font-size: 32rpx;
    }
  }

  .login-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 12rpx;
    font-size: 32rpx;
    margin-top: 40rpx;
    border: none;

    &:active {
      opacity: 0.8;
    }
  }
}
</style>
