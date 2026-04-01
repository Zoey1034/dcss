<template>
  <view class="login-page">
    <view class="login-header">
      <view class="logo">📄</view>
      <text class="title">智能文档处理系统</text>
      <text class="subtitle">Document Classification & Storage System</text>
    </view>

    <view class="login-form card">
      <view class="form-item">
        <text class="label">用户名</text>
        <input
          v-model="form.username"
          class="input"
          placeholder="请输入用户名"
          placeholder-class="placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input
          v-model="form.password"
          class="input"
          placeholder="请输入密码"
          placeholder-class="placeholder"
          password
        />
      </view>
      <button class="btn-primary" :loading="loading" @click="handleLogin">
        登 录
      </button>
      <view class="register-link">
        <text class="link-text" @click="showRegister = true">没有账号？立即注册</text>
      </view>
    </view>

    <!-- 注册弹窗 -->
    <uni-popup ref="registerPopup" type="center">
      <view class="register-modal card" v-if="showRegister">
        <text class="modal-title">注册账号</text>
        <view class="form-item">
          <text class="label">用户名</text>
          <input v-model="regForm.username" class="input" placeholder="请输入用户名" />
        </view>
        <view class="form-item">
          <text class="label">密码</text>
          <input v-model="regForm.password" class="input" placeholder="请设置密码" password />
        </view>
        <view class="form-item">
          <text class="label">邮箱</text>
          <input v-model="regForm.email" class="input" placeholder="请输入邮箱（可选）" />
        </view>
        <view class="modal-btns">
          <button class="btn-default" style="flex:1;margin-right:16rpx" @click="showRegister = false">取消</button>
          <button class="btn-primary" style="flex:1" :loading="regLoading" @click="handleRegister">注册</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { authApi } from '@/utils/api'

export default {
  data() {
    return {
      form: { username: '', password: '' },
      regForm: { username: '', password: '', email: '' },
      loading: false,
      regLoading: false,
      showRegister: false
    }
  },
  watch: {
    showRegister(val) {
      if (val) this.$refs.registerPopup.open()
      else this.$refs.registerPopup.close()
    }
  },
  methods: {
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        return uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
      }
      this.loading = true
      try {
        const res = await authApi.login(this.form)
        const token = res.data?.token || res.token
        uni.setStorageSync('token', token)
        const meRes = await authApi.getMe()
        uni.setStorageSync('userInfo', JSON.stringify(meRes.data))
        uni.reLaunch({ url: '/pages/files/index' })
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      if (!this.regForm.username || !this.regForm.password) {
        return uni.showToast({ title: '请填写用户名和密码', icon: 'none' })
      }
      this.regLoading = true
      try {
        await authApi.register(this.regForm)
        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        this.showRegister = false
        this.form.username = this.regForm.username
      } finally {
        this.regLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a6fc4 0%, #2196f3 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 60rpx;

  .logo {
    font-size: 100rpx;
    display: block;
    margin-bottom: 20rpx;
  }

  .title {
    display: block;
    font-size: 42rpx;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 12rpx;
  }

  .subtitle {
    display: block;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.login-form {
  width: 100%;
  padding: 48rpx;
}

.form-item {
  margin-bottom: 32rpx;

  .label {
    display: block;
    font-size: 26rpx;
    color: #666666;
    margin-bottom: 12rpx;
  }

  .input {
    width: 100%;
    height: 80rpx;
    border: 2rpx solid #e8e8e8;
    border-radius: 8rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333333;
    background: #fafafa;
    box-sizing: border-box;
  }
}

.placeholder {
  color: #999999;
}

.btn-primary {
  background: #1a6fc4;
  color: #ffffff;
  border: none;
  border-radius: 8rpx;
  height: 88rpx;
  font-size: 32rpx;
  width: 100%;
  margin-top: 16rpx;
}

.btn-default {
  background: #ffffff;
  color: #1a6fc4;
  border: 2rpx solid #1a6fc4;
  border-radius: 8rpx;
  height: 80rpx;
  font-size: 28rpx;
}

.register-link {
  text-align: center;
  margin-top: 24rpx;

  .link-text {
    color: #1a6fc4;
    font-size: 26rpx;
  }
}

.register-modal {
  width: 600rpx;
  padding: 48rpx;

  .modal-title {
    display: block;
    font-size: 34rpx;
    font-weight: bold;
    color: #333333;
    text-align: center;
    margin-bottom: 40rpx;
  }

  .modal-btns {
    display: flex;
    margin-top: 16rpx;
  }
}
</style>
