import { get, post, put, del } from './request'
import { BASE_URL } from './request'

// 认证模块
export const authApi = {
  login: (data) => post('/api/auth/login', data),
  register: (data) => post('/api/auth/register', data),
  logout: () => post('/api/auth/logout'),
  getMe: () => get('/api/auth/me'),
  refreshToken: () => post('/api/auth/refresh')
}

// 文件管理模块
export const fileApi = {
  getPage: (params) => get('/api/file/page', params),
  getDetail: (id) => get(`/api/file/${id}`),
  deleteFile: (id) => del(`/api/file/${id}`),
  upload: (filePath, formData) => {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: BASE_URL + '/api/file/upload',
        filePath,
        name: 'file',
        formData,
        header: { Authorization: `Bearer ${token}` },
        success(res) {
          const data = JSON.parse(res.data)
          if (data.code === 200 || data.success === true) {
            resolve(data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        },
        fail: reject
      })
    })
  }
}

// AI处理模块
export const aiApi = {
  process: (data) => post('/api/v1/ai/process', data),
  uploadAndProcess: (filePath) => {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: BASE_URL + '/api/v1/ai/upload-and-process',
        filePath,
        name: 'file',
        header: { Authorization: `Bearer ${token}` },
        success(res) {
          const data = JSON.parse(res.data)
          if (data.code === 200 || data.success === true) {
            resolve(data)
          } else {
            reject(new Error(data.message || '处理失败'))
          }
        },
        fail: reject
      })
    })
  }
}

// 审核模块
export const auditApi = {
  getResult: (fileId) => get(`/api/audit/result/${fileId}`),
  getPreview: (fileId) => get(`/api/audit/preview/${fileId}`),
  submit: (data) => post('/api/audit/submit', data)
}

// 分类模块
export const classificationApi = {
  getTypes: () => get('/api/classification/types'),
  getList: (params) => get('/api/classification/list', params),
  getDetail: (fileId) => get(`/api/classification/detail/${fileId}`)
}

// 数据面板模块
export const dashboardApi = {
  getStats: () => get('/api/dashboard/stats'),
  getTrend: (days) => get('/api/dashboard/trend', { days }),
  getFileTypeDistribution: () => get('/api/dashboard/file-type-distribution'),
  getConfidenceDistribution: () => get('/api/dashboard/confidence-distribution')
}

// 用户管理模块
export const userApi = {
  getPage: (params) => get('/api/user/page', params),
  getDetail: (id) => get(`/api/user/${id}`),
  create: (data) => post('/api/user', data),
  update: (id, data) => put(`/api/user/${id}`, data),
  deleteUser: (id) => del(`/api/user/${id}`)
}
