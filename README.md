# online-medical-record

临床精准系统全栈原型：Spring Boot API + React 管理端 + PostgreSQL。

## Quick Start

```bash
docker compose up -d db
cd backend && mvn spring-boot:run
cd ../frontend && npm install && npm run dev
```

如果只是本机快速体验，也可以不启动 Docker，使用内存数据库 demo profile：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo
cd ../frontend && npm install && npm run dev
```

如果 `8080` 已被占用，可改用其他后端端口，并让 Vite 代理跟随：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments=--server.port=8081
cd ../frontend && VITE_API_PROXY_TARGET=http://localhost:8081 npm run dev
```

默认账号：

- 管理员：`admin@aura.local` / `Admin123!`
- 医生：`doctor@aura.local` / `Doctor123!`

前端开发地址：`http://localhost:5173`
后端 API：`http://localhost:8080/api`
