# online-medical-record

临床精准系统全栈原型：Spring Boot API + React 管理端 + PostgreSQL 18。

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
