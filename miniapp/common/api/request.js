import { API_BASE_URL } from './config.js'

// HTTP请求封装
class HttpRequest {
  constructor() {
    this.baseUrl = API_BASE_URL
    this.timeout = 30000
  }

  // 获取token
  getToken() {
    return uni.getStorageSync('token') || ''
  }

  // 请求拦截器
  interceptor(options) {
    // 添加token
    const token = this.getToken()
    if (token) {
      options.header = {
        ...options.header,
        'Authorization': `Bearer ${token}`
      }
    }

    // 添加Content-Type
    if (!options.header['Content-Type']) {
      options.header['Content-Type'] = 'application/json'
    }

    return options
  }

  // 统一请求方法
  request(options = {}) {
    options.url = this.baseUrl + options.url
    options.timeout = options.timeout || this.timeout
    options.header = options.header || {}

    // 执行拦截器
    options = this.interceptor(options)

    return new Promise((resolve, reject) => {
      uni.request({
        ...options,
        success: (res) => {
          // 根据后端统一返回格式处理
          if (res.statusCode === 200) {
            const data = res.data
            if (data.code === 200 || data.code === 0) {
              resolve(data.data)
            } else {
              // 业务错误
              uni.showToast({
                title: data.message || '请求失败',
                icon: 'none'
              })
              reject(data)
            }
          } else if (res.statusCode === 401) {
            // token过期，跳转登录
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({
              url: '/pages/login/login'
            })
            reject(res)
          } else {
            // HTTP错误
            uni.showToast({
              title: `请求失败(${res.statusCode})`,
              icon: 'none'
            })
            reject(res)
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  }

  // GET请求
  get(url, data = {}, options = {}) {
    return this.request({
      url,
      data,
      method: 'GET',
      ...options
    })
  }

  // POST请求
  post(url, data = {}, options = {}) {
    return this.request({
      url,
      data,
      method: 'POST',
      ...options
    })
  }

  // PUT请求
  put(url, data = {}, options = {}) {
    return this.request({
      url,
      data,
      method: 'PUT',
      ...options
    })
  }

  // DELETE请求
  delete(url, data = {}, options = {}) {
    return this.request({
      url,
      data,
      method: 'DELETE',
      ...options
    })
  }

  // 文件上传
  upload(url, filePath, formData = {}) {
    const token = this.getToken()

    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: this.baseUrl + url,
        filePath: filePath,
        name: 'file',
        formData: formData,
        header: {
          'Authorization': `Bearer ${token}`
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200 || data.code === 0) {
              resolve(data.data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } else {
            uni.showToast({
              title: `上传失败(${res.statusCode})`,
              icon: 'none'
            })
            reject(res)
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  }
}

export default new HttpRequest()
