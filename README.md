# 智能简历诊断系统 (IRAS)

> Intelligent Resume Analysis System — 基于 B/S 架构的招聘辅助平台

## 系统架构

```
┌─────────────────────────────────────────────────┐
│                   前端 (Vue 3)                   │
│    Element Plus + Vue Router + Pinia + Axios     │
│         http://localhost:5173                     │
├──────────────────────┬──────────────────────────┤
│                      │                           │
│     Vite Proxy       │      /iras/api/*          │
│                      ▼                           │
│              ┌──────────────┐                    │
│              │  后端 (Spring Boot)  │             │
│              │  Spring Security    │             │
│              │  JWT + MyBatis      │             │
│              │  http://localhost:8080/iras        │
│              └───────┬──────────┘                │
│                      │                           │
│          ┌───────────┼───────────┐               │
│          ▼           ▼           ▼               │
│      ┌──────┐  ┌──────────┐  ┌──────────┐       │
│      │ MySQL │  │ Dify API │  │ Dify API │       │
│      │ iras  │  │ 职能画像  │  │ 简历诊断  │       │
│      └──────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Vue Router + Pinia + Axios |
| 后端 | Spring Boot 3.2 + Spring Security + JWT + MyBatis |
| 数据库 | MySQL 8.0 |
| AI 引擎 | Dify Workflow API |

## 功能模块

### 1. 用户认证

支持用户名或邮箱登录，JWT 无状态会话，Token 有效期 24 小时。

### 2. 职位检索

通过职位名称、工作地点、薪资范围进行多字段组合筛选，结果以分页表格展示，支持查看单个职位的详细信息。默认每页 20 条，支持切换为 50/100 条。

### 3. 职能画像

输入任意职位名称（如：软件工程师、产品经理、建筑师），AI 自动生成该职位的完整能力画像，包含：
- 硬技能要求
- 软技能要求
- 常用工具清单
- 工作经验要求
- 学历要求

分析耗时约 2 分钟。

### 4. 智能诊断

上传简历文件（支持 .txt / .pdf / .doc / .docx 格式，最大 10MB），AI 自动分析简历内容并与岗位库进行匹配，生成诊断报告，包含：
- 匹配岗位及匹配分（百分比）
- 匹配原因分析
- 差距分析
- 面试建议

上传文件后可随时点击「重新上传」替换文件。分析耗时约 3 分钟。

## 快速启动

### 1. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 2. 数据库初始化

```bash
mysql -u root -p < backend/src/main/resources/init.sql
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080/iras

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

## 项目结构

```
iras-project/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 配置
│   └── src/main/
│       ├── java/com/iras/
│       │   ├── IrasApplication.java  # 启动类
│       │   ├── config/               # 安全配置、JWT 过滤器
│       │   ├── controller/           # REST 控制器
│       │   ├── dto/                  # 数据传输对象
│       │   ├── entity/               # 实体类
│       │   ├── mapper/               # MyBatis Mapper
│       │   ├── service/              # 业务逻辑
│       │   └── util/                 # JWT 工具类
│       └── resources/
│           ├── application.yml       # 配置文件
│           ├── init.sql              # 数据库初始化
│           └── mapper/               # MyBatis XML
│
└── frontend/                         # Vue 3 前端
    ├── package.json
    ├── vite.config.js                # Vite 配置（含代理）
    └── src/
        ├── api/                      # Axios 封装
        ├── router/                   # 路由配置
        ├── store/                    # Pinia 状态管理
        └── views/                    # 页面组件
            ├── Login.vue             # 登录/注册
            ├── Layout.vue            # 主布局（侧边栏导航）
            ├── Jobs.vue              # 职位检索
            ├── JobProfile.vue        # 职能画像
            └── Diagnosis.vue         # 智能诊断
```

## API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /iras/api/auth/register | 注册 |
| POST | /iras/api/auth/login | 登录 |

### 职位

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /iras/api/jobs/search?jobName=&city=&salaryMin=&salaryMax=&page=1&size=20 | 搜索 |
| GET | /iras/api/jobs/{id} | 详情 |

### AI

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /iras/api/dify/job-profile | 职能画像（JSON body: { "job_name": "..." }） |
| POST | /iras/api/dify/diagnose | 简历诊断（multipart/form-data，字段名: file） |

## 配置说明

Dify API 配置在 `backend/src/main/resources/application.yml` 中：

```yaml
dify:
  base-url: https://api.dify.ai/v1
  job-profile-key: <your-job-profile-app-key>
  resume-diagnosis-key: <your-resume-diagnosis-app-key>
```

## 注意事项

- Dify AI 推理时间约为 2-3 分钟，前端已设置 5 分钟超时
- 页面使用 `keep-alive` 缓存，跳转后返回会保留之前的内容
- JWT Token 有效期为 24 小时
- 简历诊断的 Dify workflow 要求 `resume_text` 输入变量为文件列表类型（ArrayFiles），后端已做适配
