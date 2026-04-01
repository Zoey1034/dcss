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

page {
  background-color: $bg-color;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica,
    sans-serif;
  font-size: 28rpx;
  color: $text-color;
  box-sizing: border-box;
}

.container {
  padding: $spacing-md;
}

.card {
  background: $white;
  border-radius: $border-radius-md;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow;
}

.btn-primary {
  background: $primary-color;
  color: $white;
  border: none;
  border-radius: $border-radius-md;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
  width: 100%;
}

.btn-default {
  background: $white;
  color: $primary-color;
  border: 2rpx solid $primary-color;
  border-radius: $border-radius-md;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;

  &.tag-success {
    background: #f6ffed;
    color: $success-color;
    border: 2rpx solid #b7eb8f;
  }

  &.tag-warning {
    background: #fffbe6;
    color: $warning-color;
    border: 2rpx solid #ffe58f;
  }

  &.tag-danger {
    background: #fff2f0;
    color: $danger-color;
    border: 2rpx solid #ffccc7;
  }

  &.tag-default {
    background: #f5f5f5;
    color: $text-secondary;
    border: 2rpx solid #d9d9d9;
  }

  &.tag-primary {
    background: $primary-light;
    color: $primary-color;
    border: 2rpx solid #91caff;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  color: $text-placeholder;

  .empty-icon {
    font-size: 80rpx;
    margin-bottom: $spacing-md;
  }

  .empty-text {
    font-size: 28rpx;
  }
}
</style>
