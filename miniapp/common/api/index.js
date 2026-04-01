import http from './request.js'
import { API } from './config.js'

// 认证API
export const authApi = {
  // 登录
  login(data) {
    return http.post(API.LOGIN, data)
  },
  // 登出
  logout() {
    return http.post(API.LOGOUT)
  },
  // 刷新token
  refreshToken() {
    return http.post(API.REFRESH_TOKEN)
  }
}

// 文件API
export const fileApi = {
  // 上传文件
  upload(filePath) {
    return http.upload(API.FILE_UPLOAD, filePath)
  },
  // 获取文件列表
  list(params) {
    return http.get(API.FILE_LIST, params)
  },
  // 获取文件详情
  detail(id) {
    return http.get(API.FILE_DETAIL, { id })
  },
  // 删除文件
  delete(id) {
    return http.delete(API.FILE_DELETE, { id })
  }
}

// 审核API
export const reviewApi = {
  // 获取待审核列表
  list(params) {
    return http.get(API.REVIEW_LIST, params)
  },
  // 获取审核详情
  detail(id) {
    return http.get(API.REVIEW_DETAIL, { id })
  },
  // 保存草稿
  save(data) {
    return http.post(API.REVIEW_SAVE, data)
  },
  // 提交审核
  submit(data) {
    return http.post(API.REVIEW_SUBMIT, data)
  }
}

// 分类API
export const classificationApi = {
  // 获取分类列表
  list(params) {
    return http.get(API.CLASSIFICATION_LIST, params)
  },
  // 获取分类统计
  stats() {
    return http.get(API.CLASSIFICATION_STATS)
  }
}

// 数据驾驶舱API
export const dashboardApi = {
  // 获取概览数据
  overview() {
    return http.get(API.DASHBOARD_OVERVIEW)
  },
  // 获取趋势数据
  trend(params) {
    return http.get(API.DASHBOARD_TREND, params)
  }
}

// 用户API
export const userApi = {
  // 获取用户信息
  info() {
    return http.get(API.USER_INFO)
  },
  // 更新用户信息
  update(data) {
    return http.put(API.USER_UPDATE, data)
  }
}
