// 通用工具函数

/**
 * 格式化日期时间
 * @param {String|Number|Date} date 日期
 * @param {String} format 格式 yyyy-MM-dd HH:mm:ss
 * @returns {String} 格式化后的日期
 */
export function formatDate(date, format = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return ''

  if (typeof date === 'string' || typeof date === 'number') {
    date = new Date(date)
  }

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return format
    .replace('yyyy', year)
    .replace('MM', padZero(month))
    .replace('dd', padZero(day))
    .replace('HH', padZero(hour))
    .replace('mm', padZero(minute))
    .replace('ss', padZero(second))
}

/**
 * 补零
 */
function padZero(num) {
  return num < 10 ? '0' + num : num
}

/**
 * 格式化文件大小
 * @param {Number} size 文件大小（字节）
 * @returns {String} 格式化后的文件大小
 */
export function formatFileSize(size) {
  if (!size) return '0B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0

  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index++
  }

  return size.toFixed(2) + units[index]
}

/**
 * 防抖函数
 * @param {Function} fn 要防抖的函数
 * @param {Number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn 要节流的函数
 * @param {Number} delay 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (!timer) {
      timer = setTimeout(() => {
        fn.apply(this, args)
        timer = null
      }, delay)
    }
  }
}

/**
 * 检查是否登录
 * @returns {Boolean} 是否登录
 */
export function isLogin() {
  return !!uni.getStorageSync('token')
}

/**
 * 获取用户信息
 * @returns {Object} 用户信息
 */
export function getUserInfo() {
  return uni.getStorageSync('userInfo') || {}
}

/**
 * 设置用户信息
 * @param {Object} userInfo 用户信息
 */
export function setUserInfo(userInfo) {
  uni.setStorageSync('userInfo', userInfo)
}

/**
 * 清除用户信息
 */
export function clearUserInfo() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
}

/**
 * 跳转到登录页
 */
export function toLogin() {
  uni.reLaunch({
    url: '/pages/login/login'
  })
}

/**
 * 文档状态映射
 */
export const FILE_STATUS_MAP = {
  0: { text: '待上传', color: '#999' },
  1: { text: '处理中', color: '#ff9900' },
  2: { text: '待审核', color: '#007AFF' },
  3: { text: '已审核', color: '#4cd964' },
  4: { text: '已驳回', color: '#ff3b30' }
}

/**
 * 获取文档状态文本
 */
export function getFileStatusText(status) {
  return FILE_STATUS_MAP[status]?.text || '未知'
}

/**
 * 获取文档状态颜色
 */
export function getFileStatusColor(status) {
  return FILE_STATUS_MAP[status]?.color || '#999'
}
