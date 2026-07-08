# online-medical-record

临床精准系统全栈原型：Spring Boot API + React 管理端 + PostgreSQL 18。

[English](./README.md)

## 项目概述

临床精准系统原型 -- 面向临床医生的患者与病历管理工作台。这是一个全栈 monorepo，包含 Spring Boot API、React 管理端和 PostgreSQL。

### 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 4.0.6、Java 21、Spring Data JPA、Spring Security、Bean Validation |
| 前端 | React 19、TypeScript 5.9、Vite 7、React Router 7、Tailwind CSS 3 |
| 数据库 | PostgreSQL 18（`demo` profile 使用 H2 内存数据库） |
| 测试 | JUnit 5 / MockMvc（后端）、Vitest + Playwright（前端） |
| 基础设施 | Docker Compose（db + backend + frontend） |

### 核心功能

- **认证与授权** -- 基于 HTTP-only Cookie 的会话认证、BCrypt 密码加密、基于角色的访问控制（`ADMIN` / `DOCTOR`）、自助密码重置、头像上传。
- **患者管理** -- 增删改查，支持按姓名和团队分页搜索。
- **病历管理** -- 结构化就诊记录（主诉、查体、诊断、治疗、预后、备注），含一对多用药清单和 `COMPLETED` / `IN_PROGRESS` 状态，可按患者和医生筛选。
- **医生账号管理** -- 管理员创建并管理医生账号，每个账号含资料（科室、头像）。
- **仪表盘** -- 运营摘要（患者数、病历数、本月新增、医生数）。
- **审计日志** -- JPA 审计记录核心实体的创建/更新元数据。

### 架构

后端在 `com.ice.medicalrecord` 下采用分层包结构：

- `api/` -- REST 控制器与 DTO
- `domain/` -- JPA 实体与枚举
- `repository/` -- Spring Data JPA 仓储
- `service/` -- 业务逻辑与映射
- `security/` -- Spring Security 配置与用户详情
- `config/` -- 审计、静态资源与数据初始化

前端（`frontend/src/`）将路由页面放在 `pages/`，可复用 UI 放在 `components/`，共享工具与认证上下文放在源码根目录。设计与 API 参考见 [`docs/DESIGN.md`](./docs/DESIGN.md) 与 [`docs/backend-api.md`](./docs/backend-api.md)。

## Quick Start

### Docker 一键部署整个项目

```bash
./start-docker.sh
```

启动后访问：

- 前端：`http://localhost:5173`
- 后端 API：`http://localhost:8080/api`

如果你想启动后直接跟随日志：

```bash
./start-docker.sh --logs
```

停止并移除容器：

```bash
./stop-docker.sh
```

这只会停止并移除容器，不会删除数据库数据卷。

如果你明确要清空本地数据库数据，再单独执行：

```bash
docker compose down -v
```

### 本地开发启动

```bash
docker compose up -d db
cd backend && mvn spring-boot:run
cd ../frontend && npm ci && npm run dev
```

也可以直接在仓库根目录一键启动前后端：

```bash
chmod +x ./start-local.sh
./start-local.sh
```

默认会优先尝试完整 Docker 环境；如果 Docker 不可用则回退到 `demo` 模式（H2 内存数据库）。如果你想强制连本地 Docker PostgreSQL：

```bash
./start-local.sh docker
```

如果只是本机快速体验，也可以不启动 Docker，使用内存数据库 demo profile：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo
cd ../frontend && npm ci && npm run dev
```

如果 `8080` 已被占用，可改用其他后端端口，并让 Vite 代理跟随：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments=--server.port=8081
cd ../frontend && VITE_API_PROXY_TARGET=http://localhost:8081 npm run dev
```

## PostgreSQL（Docker）

仓库自带的 `docker-compose.yml` 使用 `postgres:18.4-alpine`，默认会启动：

- 容器名：`aura-medical-postgres`
- 数据库：`medical_record`
- 用户名：`medical`
- 密码：`medical`
- 端口：`5432`

为兼容 PostgreSQL 18 官方镜像的数据目录规则，当前配置使用：

- `PGDATA=/var/lib/postgresql/18/docker`
- Docker volume：`postgres18_data`

这是一个新项目，本地没有历史数据库需要迁移。若你需要重建一个干净的本地库，可以直接删除并重建对应 volume。

默认账号：

- 管理员：`admin@aura.local` / `Admin123!`
- 医生：`doctor@aura.local` / `Doctor123!`

前端开发地址：`http://localhost:5173`
后端 API：`http://localhost:8080/api`

## 贡献指南

欢迎贡献！这是一个基于 Apache 2.0 协议的开源项目。

### 贡献方式

- 通过 GitHub Issues 报告 Bug 或提出功能需求
- 提交 Pull Request 修复问题或改进功能
- 完善 `docs/` 下的文档

### 开发流程

1. Fork 仓库并克隆到本地。
2. 按 [快速开始](#quick-start) 搭建开发环境 -- `demo` profile（H2 内存库）最快：`cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo`。
3. 从 `main` 创建特性分支。
4. 完成改动，代码风格与测试保持与 [`AGENTS.md`](./AGENTS.md) 一致。
5. 运行相关测试：
   - 后端：`cd backend && mvn test`
   - 前端单元：`cd frontend && npm test`
   - 前端 e2e：`cd frontend && npm run test:e2e`
6. 使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范的提交信息（如 `feat(records): ...`、`fix(auth): ...`）。
7. 推送并提交 Pull Request，说明改动内容与验证方式。

### 规范

- 遵循 [`AGENTS.md`](./AGENTS.md) 中的代码风格、命名与测试约定。
- 为行为变更添加或更新测试，确保 API、认证与持久化有合理的覆盖。
- **不要**提交真实患者数据、生产密钥或本地凭据 -- 演示账号仅供本地开发使用。
- 保持 PR 聚焦；涉及可见 UI 改动时附上截图。

## 许可证

基于 [Apache License 2.0](./LICENSE) 协议开源。
