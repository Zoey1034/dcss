// API配置
const API_BASE_URL = 'http://localhost:8080'

// API接口定义
const API = {
  // 认证相关
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REFRESH_TOKEN: '/auth/refresh',

  // 文件管理
  FILE_UPLOAD: '/file/upload',
  FILE_LIST: '/file/list',
  FILE_DETAIL: '/file/get',
  FILE_DELETE: '/file/remove',
  FILE_DOWNLOAD: '/file/download',

  // 审核相关
  REVIEW_LIST: '/review/list',
  REVIEW_DETAIL: '/review/get',
  REVIEW_SAVE: '/review/save',
  REVIEW_SUBMIT: '/review/submit',

  // 分类相关
  CLASSIFICATION_LIST: '/classification/list',
  CLASSIFICATION_STATS: '/classification/stats',

  // 数据驾驶舱
  DASHBOARD_OVERVIEW: '/dashboard/overview',
  DASHBOARD_TREND: '/dashboard/trend',

  // 用户管理
  USER_INFO: '/user/info',
  USER_UPDATE: '/user/edit',
}

export { API_BASE_URL, API }
