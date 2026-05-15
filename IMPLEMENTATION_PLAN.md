# 临床精准系统全栈实施计划

## Summary

- 从空仓库构建 Spring Boot + React 全栈应用，覆盖登录、患者管理、历史病历列表、录入新病历四个核心页面。
- 技术栈：Java 21 + Maven + Spring Boot 4.0.6、Spring Security 7.0.5、Spring Data JPA、PostgreSQL + Docker；前端使用 React 19.2 + Vite + TypeScript + Tailwind CSS。
- 视觉参考 `/Users/allenj/Documents/medical原型` 中 HTML/PNG，允许复用 HTML 里的热链接图片。

## Implementation Changes

- 新增 `DESIGN.md` 固化界面规范。
- 建立 `backend/` Spring Boot API：账号、医生、患者、病历、用药、审计日志模型与 REST API。
- 建立 `frontend/` React/Vite 管理端：登录页、受保护布局、患者管理、病历列表、录入病历、轻量仪表盘/排班/设置页面。
- 使用 PostgreSQL + `docker-compose.yml`；后端提供种子数据和内置管理员账号。
- 完整账号体系：管理员创建/停用医生账号，BCrypt 密码哈希，cookie session 登录/登出，忘记密码使用本地重置 token 流程。
- 权限规则：医生可查看全院患者和病历，新增/修改写入操作审计。

## Public Interfaces

- Auth：`POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/auth/me`、`POST /api/auth/password-reset/*`
- Admin：`GET/POST/PATCH /api/admin/users`
- Patients：`GET/POST/PATCH /api/patients`
- Medical Records：`GET/POST/GET by id /api/medical-records`
- Dashboard：`GET /api/dashboard/summary`

## Test Plan

- 后端：`mvn test`，覆盖认证、账号停用、患者/病历 CRUD、筛选分页、审计日志。
- 前端：`npm run lint`、`npm run test`、`npm run build`。
- E2E：Playwright 覆盖管理员创建医生、医生登录、新增患者、录入病历、历史列表检索。
- 视觉验证：启动双服务后用浏览器检查桌面和移动布局，对照原型截图确认主要页面结构、间距、颜色和交互状态。

## Assumptions

- 数据库使用 PostgreSQL + Docker。
- 开发期采用双服务：Spring Boot 后端 + Vite 前端代理。
- 医生账号由管理员创建，不开放公开注册。
- 第三方登录、短信验证码、真实邮件发送、HIS/EMR 集成不纳入首版。
