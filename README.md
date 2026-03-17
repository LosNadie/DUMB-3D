# DUMB-3D 项目说明

## 项目简介

DUMB-3D 是一个前后端分离的内容社区项目，包含：

- `dumb-frontend`：基于 Vue 3 + Vite 的前端应用
- `dumb-backend`：基于 Spring Boot 3 + MyBatis-Plus 的后端服务

项目支持评论、影评、乐评等内容展示与管理（含后台管理相关模块）。

## 技术栈

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios
- Three.js

### 后端

- Spring Boot 3.3.x
- Spring Security
- MyBatis-Plus
- MySQL
- JWT
- Maven
- Java 17+

## 目录结构

```text
.
├─ dumb-frontend/   # 前端工程
├─ dumb-backend/    # 后端工程
└─ README.md        # 项目说明
```

## 环境要求

- Node.js 18+
- npm 9+
- Java 17+（必须，Java 8 无法启动 Spring Boot 3）
- Maven 3.9+
- MySQL 8+

## 快速开始

### 1. 启动后端

进入后端目录：

```bash
cd dumb-backend
```

启动命令：

```bash
mvn spring-boot:run
```

默认端口：`8080`

### 2. 启动前端

进入前端目录：

```bash
cd dumb-frontend
```

安装依赖：

```bash
npm install
```

开发模式启动：

```bash
npm run dev
```

默认访问地址：`http://localhost:5173/`

## 后端默认配置说明

后端 `application.yml` 中默认连接参数如下（可通过环境变量覆盖）：

- `DB_URL`: `jdbc:mysql://localhost:3306/dumb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `DB_USERNAME`: `root`
- `DB_PASSWORD`: `123456`

建议本地开发时通过环境变量配置数据库与 JWT 密钥，避免将真实生产配置写入仓库。

## 常见问题

### 1) 后端启动报 Java 版本错误

如果出现类似 `class file version 61.0` 错误，说明当前 Java 版本过低。  
请切换到 Java 17 或更高版本后再启动。

### 2) 前端启动正常但接口报错

优先检查：

- 后端是否已启动并监听 `8080`
- 数据库是否可连接
- 接口地址与跨域配置是否正确

## 演示视频
https://www.bilibili.com/video/BV1iUcfzmE5W/?share_source=copy_web&vd_source=f6a340ce315008577bb1ae8f966708e0

## GitHub

仓库地址：<https://github.com/LosNadie/DUMB-3D>
