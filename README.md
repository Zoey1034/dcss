# 智能文档分类系统（DCSS）

> **D**ocument **C**lassification & **S**mart **S**ystem  
> 基于 AI 的智能文档处理、分类与审核平台

---

## 📋 项目简介

智能文档分类系统（DCSS）是一套面向档案/证件类文档的自动化处理平台，集成了 **OCR 识别**、**NLP 信息提取**、**AI 分类** 和 **人工审核** 等核心能力，实现从文件上传到归档的全流程自动化。

主要功能包括：

| 功能模块 | 描述 |
|---------|------|
| 文件管理 | 上传/下载/删除文件，MinIO 对象存储 |
| AI 智能处理 | OCR 识别、NLP 信息提取、文档分类 |
| 数据提取 | 结构化信息提取与编辑 |
| 人工审核 | 待审队列、草稿保存、批量审核 |
| 文档分类 | 按类型统计、检索、查看详情 |
| 数据驾驶舱 | 概览统计、趋势分析、效率指标 |
| 导出报表 | 文件列表、审核记录、统计报表 Excel 导出 |
| 用户管理 | 用户 CRUD、角色分配、密码管理 |
| 角色管理 | 角色 CRUD、权限配置 |
| 模板管理 | 文档模板配置与状态管理 |

---

## 🛠️ 技术栈

| 分类 | 技术 |
|-----|------|
| 后端框架 | Spring Boot 3.x |
| 持久层 | MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 对象存储 | MinIO |
| AI 接入 | Python AI Service（HTTP 调用） |
| 安全认证 | Spring Security + JWT |
| 文档处理 | Apache POI（Excel 导出） |
| 工具库 | Lombok, Jackson, Hutool |
| 构建工具 | Maven |

---

## 📁 项目结构

```
src/main/java/cn/masu/dcs/
├── business/                   # 业务编排层（多 Service 协调）
│   ├── AuthBusiness.java       # 认证业务：UserService + JWT + Redis
│   ├── DocumentProcessBusiness.java  # 文档处理：AiFileService + AI Client + Persistence
│   └── ExportBusiness.java     # 导出报表：ExportService + DashboardService
│
├── common/
│   ├── client/                 # 外部服务客户端
│   ├── config/                 # 配置类（Minio、Redis、Security 等）
│   ├── constant/               # 公共常量
│   │   ├── AiConstants.java    # AI 处理相关常量
│   │   ├── FileConstants.java  # 文件类型/MIME 常量
│   │   └── RedisConstants.java # Redis 键常量
│   ├── exception/              # 全局异常处理
│   ├── filter/                 # JWT 过滤器
│   ├── result/                 # 统一返回结果（R、PageResult、ErrorCode）
│   └── util/                   # 工具类（JWT、MinIO、雪花ID）
│
├── controller/                 # HTTP 接口层
│   ├── AuthController.java
│   ├── FileController.java
│   ├── ReviewController.java
│   ├── AuditController.java
│   ├── ExtractController.java
│   ├── ClassificationController.java
│   ├── DashboardController.java
│   ├── ExportController.java
│   ├── UserController.java
│   ├── RoleController.java
│   ├── TemplateController.java
│   ├── TaskController.java
│   └── AiProcessController.java
│
├── dto/                        # 请求数据传输对象
├── entity/                     # 数据库实体类
├── mapper/                     # MyBatis-Plus Mapper 接口
│
├── service/                    # 服务接口
│   ├── impl/                   # 服务实现
│   ├── AiFileService.java
│   ├── AiProcessorService.java
│   ├── AiResultPersistenceService.java
│   ├── AiService.java
│   ├── AuditService.java
│   ├── AuthService.java
│   ├── ClassificationService.java
│   ├── DashboardService.java
│   ├── ExportService.java
│   ├── ExtractService.java
│   ├── FileService.java
│   ├── ReviewService.java
│   ├── RoleService.java
│   ├── TaskLogService.java
│   ├── TemplateService.java
│   └── UserService.java
│
└── vo/                         # 响应视图对象
```

---

## 📐 分层架构

```
┌─────────────────────────────────────────────────┐
│               前端（Browser/App）                │
└──────────────────────┬──────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────┐
│            Controller 层（HTTP 接口）             │
│  接口命名：get / list / add / edit / remove 前缀  │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│         Business 层（多 Service 编排）            │
│  AuthBusiness / DocumentProcessBusiness /        │
│  ExportBusiness                                  │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│            Service 层（单一业务逻辑）             │
│  接口命名：query / insert / update / delete 前缀  │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│         Mapper 层（MyBatis-Plus CRUD）            │
│  接口命名：select / insert / update / delete 前缀 │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│       数据存储层（MySQL / MinIO / Redis）         │
└─────────────────────────────────────────────────┘
```

---

## 🗂️ 数据库表

| 表名 | 说明 |
|-----|------|
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_user_role` | 用户-角色关联表 |
| `sys_doc_template` | 文档模板表 |
| `sys_task_log` | 任务日志表 |
| `document_file` | 文档文件表 |
| `document_extract_main` | 文档提取主信息表 |
| `document_extract_detail` | 文档提取明细表 |
| `audit_record` | 审核记录表 |

---

## 🚀 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- MinIO（或兼容的 S3 存储）

### 配置文件

复制 `src/main/resources/application.yml.example`（如有）并填写数据库、Redis、MinIO 连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dcss?useUnicode=true&characterEncoding=utf8
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: dcss-files
```

### 构建并运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/dcss-*.jar
```

---

## 📊 系统设计图

所有设计图使用 [draw.io](https://app.diagrams.net/) 绘制，文件存放于 `draw/` 目录：

| 文件 | 内容 |
|-----|------|
| `draw/system-architecture.drawio` | 系统分层架构图 |
| `draw/document-process-flow.drawio` | 文档处理流程图 |
| `draw/database-er-diagram.drawio` | 数据库 ER 图 |

在 [draw.io](https://app.diagrams.net/) 中选择 **File → Open from → Device** 即可打开任意 `.drawio` 文件。

---

## 📜 代码规范

本项目遵循以下命名与编码规范：

- **类名**：大写字母开头的驼峰命名法（UpperCamelCase）
- **方法名 / 变量名**：小写字母开头的驼峰命名法（lowerCamelCase）
- **常量名**：全大写字母，下划线分隔（`UPPER_SNAKE_CASE`），统一放在 `common/constant/` 包下
- **包名**：全部小写
- **Controller 接口**：`get / list / add / edit / remove` 前缀
- **Service 接口**：`query / insert / update / delete` 前缀
- **Mapper 接口**：`select / insert / update / delete` 前缀（继承 `BaseMapper` 默认满足）
- **注释**：类/方法使用 `/** */` 格式，行内逻辑使用 `//`
- **异常处理**：统一使用 `try-catch-finally`，捕获后必须处理或重新抛出
- **业务层**：当一个业务需要调用多个 Service 时，在 `business/` 层进行编排

---

## 📄 License

本项目仅供学习参考。
