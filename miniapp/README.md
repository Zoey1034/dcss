# DCSS 小程序端

智能文档分类系统（DCSS）的小程序端应用，使用 uni-app 框架开发，支持微信小程序、H5、APP等多端部署。

## 📋 项目简介

本小程序端提供了以下核心功能：

- **用户认证**：登录、登出、token管理
- **文件管理**：文件列表查看、文件上传、文件详情
- **审核功能**：待审核列表、审核详情、提交审核
- **文档分类**：按类型查看文档、分类统计
- **数据概览**：统计数据展示、处理趋势分析
- **个人中心**：用户信息、系统设置

## 🛠️ 技术栈

| 分类 | 技术 |
|-----|------|
| 框架 | uni-app 3.x |
| 前端框架 | Vue 3 |
| 样式 | SCSS |
| 构建工具 | Vite |
| 开发工具 | HBuilderX / VSCode |

## 📁 项目结构

```
miniapp/
├── pages/                      # 页面目录
│   ├── login/                  # 登录页
│   ├── index/                  # 首页
│   ├── file-list/              # 文件列表
│   ├── file-upload/            # 文件上传
│   ├── review/                 # 待审核列表
│   ├── review-detail/          # 审核详情
│   ├── classification/         # 文档分类
│   ├── dashboard/              # 数据概览
│   └── profile/                # 个人中心
│
├── common/                     # 公共资源
│   ├── api/                    # API接口
│   │   ├── config.js           # API配置
│   │   ├── request.js          # HTTP请求封装
│   │   └── index.js            # API接口定义
│   ├── utils/                  # 工具函数
│   │   └── util.js             # 通用工具
│   └── styles/                 # 全局样式
│       └── common.scss         # 通用样式
│
├── static/                     # 静态资源
│   └── tabbar/                 # 底部导航栏图标
│
├── components/                 # 组件目录
│
├── App.vue                     # 应用配置
├── main.js                     # 入口文件
├── manifest.json               # 应用配置
├── pages.json                  # 页面配置
└── package.json                # 项目依赖

```

## 🚀 快速启动

### 前置条件

- Node.js 14+
- HBuilderX（推荐）或 VSCode + uni-app插件
- 微信开发者工具（如需开发微信小程序）

### 安装依赖

```bash
cd miniapp
npm install
```

### 配置后端地址

编辑 `common/api/config.js`，修改后端API地址：

```javascript
const API_BASE_URL = 'http://your-backend-url:8080'
```

### 运行项目

#### 使用 HBuilderX

1. 打开 HBuilderX
2. 文件 → 打开目录 → 选择 miniapp 目录
3. 运行 → 运行到浏览器/小程序模拟器/真机

#### 使用命令行

```bash
# 运行到H5
npm run dev:h5

# 运行到微信小程序
npm run dev:mp-weixin

# 构建H5
npm run build:h5

# 构建微信小程序
npm run build:mp-weixin
```

### 微信小程序配置

1. 在 `manifest.json` 中填写微信小程序 appid：

```json
{
  "mp-weixin": {
    "appid": "your-appid-here"
  }
}
```

2. 使用微信开发者工具打开 `unpackage/dist/dev/mp-weixin` 目录

3. 配置合法域名（开发时可勾选"不校验合法域名"）

## 📱 页面说明

### 登录页 (pages/login/login)

- 用户名密码登录
- 登录成功后保存 token 和用户信息
- 自动跳转到首页

### 首页 (pages/index/index)

- 显示统计数据卡片（总文件数、待审核、已完成）
- 快捷功能入口
- 最近处理文件列表

### 文件列表 (pages/file-list/file-list)

- 文件列表展示
- 搜索和筛选功能
- 分页加载
- 点击查看文件详情

### 文件上传 (pages/file-upload/file-upload)

- 选择文件上传
- 上传进度显示
- 上传结果反馈

### 待审核列表 (pages/review/review)

- 待审核任务列表
- 显示文件信息和可信度
- 点击进入审核详情

### 审核详情 (pages/review-detail/review-detail)

- 显示文件完整信息
- 查看提取的信息
- 保存草稿
- 提交审核

### 文档分类 (pages/classification/classification)

- 按类型统计文档数量
- 分类文档列表
- 点击查看详情

### 数据概览 (pages/dashboard/dashboard)

- 数据概览卡片
- 处理趋势图
- 效率分析指标

### 个人中心 (pages/profile/profile)

- 用户信息显示
- 功能菜单
- 退出登录

## 🔧 API接口

所有API接口定义在 `common/api/` 目录下：

- **authApi**: 认证相关（登录、登出）
- **fileApi**: 文件管理（上传、列表、详情、删除）
- **reviewApi**: 审核功能（列表、详情、保存、提交）
- **classificationApi**: 分类功能（列表、统计）
- **dashboardApi**: 数据概览（概览、趋势）
- **userApi**: 用户管理（信息、更新）

## 🎨 样式规范

项目使用 SCSS 预处理器，全局样式定义在 `common/styles/common.scss`：

### 颜色变量
- `$primary-color`: 主色调 (#007AFF)
- `$success-color`: 成功色 (#4cd964)
- `$warning-color`: 警告色 (#ff9900)
- `$error-color`: 错误色 (#ff3b30)

### 工具类
- 间距工具类：`.mt-sm`, `.mb-md`, `.pl-lg` 等
- Flex布局：`.flex`, `.flex-center`, `.flex-between` 等
- 文本样式：`.text-primary`, `.text-success`, `.text-lg` 等

## 🔐 权限管理

- 所有API请求自动携带 token（通过请求拦截器）
- token 过期自动跳转登录页
- 登录状态判断工具函数：`isLogin()`

## 📝 开发注意事项

### 跨平台兼容

- 使用 uni-app 提供的API，避免使用平台特定API
- 条件编译：使用 `#ifdef` 和 `#ifndef` 处理平台差异
- 测试多端兼容性

### 性能优化

- 列表使用虚拟滚动或分页加载
- 图片使用懒加载
- 避免频繁的 setData 操作
- 合理使用缓存

### 代码规范

- 组件命名：大写字母开头驼峰命名
- 方法命名：小写字母开头驼峰命名
- 使用 ESLint 进行代码检查
- 添加必要的注释

## 🐛 常见问题

### 1. 请求失败

- 检查后端服务是否启动
- 检查 API_BASE_URL 配置是否正确
- 检查网络连接
- 查看控制台错误信息

### 2. token过期

- 自动跳转到登录页
- 重新登录获取新token

### 3. 微信小程序无法请求

- 在微信开发者工具中勾选"不校验合法域名"（开发环境）
- 生产环境需在微信公众平台配置合法域名

### 4. 图标不显示

- 将实际图标文件放到 `static/tabbar/` 目录
- 或使用 uni-icons 组件

## 📄 License

本项目仅供学习参考。

---

## 💡 后续优化建议

1. **组件化**：抽取公共组件（卡片、列表项、空状态等）
2. **状态管理**：引入 Vuex 或 Pinia 管理全局状态
3. **错误处理**：完善错误边界和错误提示
4. **测试**：添加单元测试和E2E测试
5. **性能监控**：接入性能监控平台
6. **国际化**：支持多语言
7. **主题切换**：支持深色模式
8. **离线支持**：添加离线缓存功能
