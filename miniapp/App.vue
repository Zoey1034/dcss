<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  },
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
@import 'uni.scss';

/* ============ 基础样式 ============ */
page {
  background-color: $bg-color;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue',
    Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-size: $font-size-base;
  color: $text-color;
  line-height: $line-height-normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}

/* ============ 布局容器 ============ */
.container {
  padding: $spacing-lg;
  min-height: 100vh;
}

.card {
  background: $white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $box-shadow-sm;
  transition: all $transition-base $transition-ease;
  border: 1rpx solid $border-light;

  &:hover {
    box-shadow: $box-shadow-md;
  }

  &:active {
    transform: scale(0.98);
  }
}

/* ============ 按钮系统 ============ */
.btn-primary {
  background: $gradient-blue;
  color: $white;
  border: none;
  border-radius: $border-radius-md;
  padding: $spacing-lg $spacing-2xl;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  width: 100%;
  box-shadow: $box-shadow-sm;
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
    background: rgba(255, 255, 255, 0.1);
    opacity: 0;
    transition: opacity $transition-base $transition-ease;
  }

  &:active::before {
    opacity: 1;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-default {
  background: $white;
  color: $primary-color;
  border: 2rpx solid $primary-color;
  border-radius: $border-radius-md;
  padding: $spacing-lg $spacing-2xl;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  transition: all $transition-base $transition-ease;

  &:active {
    background: $primary-lighter;
  }
}

.btn-ghost {
  background: transparent;
  color: $text-secondary;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-md;
  padding: $spacing-lg $spacing-2xl;
  font-size: $font-size-base;
  transition: all $transition-base $transition-ease;

  &:active {
    border-color: $primary-color;
    color: $primary-color;
  }
}

/* ============ 标签系统 ============ */
.tag {
  display: inline-flex;
  align-items: center;
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  line-height: $line-height-tight;
  transition: all $transition-base $transition-ease;

  &.tag-success {
    background: $success-light;
    color: $success-color;
    border: 1rpx solid $success-color;
  }

  &.tag-warning {
    background: $warning-light;
    color: $warning-color;
    border: 1rpx solid $warning-color;
  }

  &.tag-danger {
    background: $danger-light;
    color: $danger-color;
    border: 1rpx solid $danger-color;
  }

  &.tag-default {
    background: $border-light;
    color: $text-secondary;
    border: 1rpx solid $border-color;
  }

  &.tag-primary {
    background: $primary-light;
    color: $primary-color;
    border: 1rpx solid $primary-color;
  }

  &.tag-info {
    background: $info-light;
    color: $info-color;
    border: 1rpx solid $info-color;
  }
}

/* ============ 空状态 ============ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-4xl 0;
  color: $text-tertiary;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-base;
    color: $text-tertiary;
    margin-bottom: $spacing-sm;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $text-placeholder;
  }
}

/* ============ 加载状态 ============ */
.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl 0;

  .loading-spinner {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid $border-color;
    border-top-color: $primary-color;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-left: $spacing-md;
    color: $text-secondary;
    font-size: $font-size-sm;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ============ 输入框 ============ */
.input-wrapper {
  position: relative;
  margin-bottom: $spacing-lg;

  .input-label {
    display: block;
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-sm;
    font-weight: $font-weight-medium;
  }

  .input {
    width: 100%;
    height: 88rpx;
    border: 2rpx solid $border-color;
    border-radius: $border-radius-md;
    padding: 0 $spacing-lg;
    font-size: $font-size-base;
    color: $text-color;
    background: $white;
    transition: all $transition-base $transition-ease;

    &:focus {
      border-color: $primary-color;
      background: $primary-lighter;
    }

    &::placeholder {
      color: $text-placeholder;
    }
  }
}

/* ============ 分割线 ============ */
.divider {
  height: 1rpx;
  background: $border-color;
  margin: $spacing-lg 0;
}

/* ============ 渐变文字 ============ */
.gradient-text {
  background: $gradient-blue;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
