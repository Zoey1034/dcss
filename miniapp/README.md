# DCSS 小程序端

智能文档处理系统（Document Classification & Storage System）小程序端，基于 [uni-app](https://uniapp.dcloud.net.cn/) 3.x 框架开发，支持微信小程序和 H5 端。

## 功能页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录 | `pages/login/index` | 用户登录 / 注册 |
| 文件管理 | `pages/files/index` | 上传、查看、删除文件，触发 AI 识别 |
| 审核管理 | `pages/review/index` | 审核员对待审核文件进行审批 |
| 文档分类 | `pages/classification/index` | 按文档类型浏览分类结果 |
| 数据面板 | `pages/dashboard/index` | 统计数据、上传趋势、类型分布 |
| 个人中心 | `pages/profile/index` | 用户信息、快速入口、退出登录 |

## 技术栈

- **框架**：uni-app 3.x（Vue 3 Composition-friendly Options API）
- **语言**：JavaScript + SCSS
- **目标平台**：微信小程序（mp-weixin）、H5

## 目录结构

```
miniapp/
├── pages/
│   ├── login/          # 登录页
│   ├── files/          # 文件管理
│   ├── review/         # 审核管理
│   ├── classification/ # 文档分类
│   ├── dashboard/      # 数据面板
│   └── profile/        # 个人中心
├── utils/
│   ├── request.js      # HTTP 请求封装
│   └── api.js          # 业务接口定义
├── App.vue             # 根组件（全局样式 + 登录守卫）
├── main.js             # 入口文件
├── manifest.json       # 应用配置（appid 等）
├── pages.json          # 页面路由 + TabBar 配置
├── uni.scss            # 全局 SCSS 变量
└── package.json        # 依赖配置
```

## 快速开始

### 安装依赖

```bash
cd miniapp
npm install
```

### 开发调试（微信小程序）

```bash
npm run dev:mp-weixin
```

然后用微信开发者工具打开 `dist/dev/mp-weixin` 目录。

### 开发调试（H5）

```bash
npm run dev:h5
```

浏览器访问 `http://localhost:5173`。

### 生产打包

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

## 后端配置

默认连接 `http://localhost:8080`，如需修改请编辑 `utils/request.js` 中的 `BASE_URL`：

```js
const BASE_URL = 'http://localhost:8080'
```

微信小程序还需在 `manifest.json` 中填写正确的 `mp-weixin.appid`，并在微信公众平台配置服务器域名。
