# 后端接口文档

## 文档说明

- 服务目录：`backend`
- 基础前缀：`/api`
- 认证方式：基于 Session 的登录态认证
- 权限规则：
  - `POST /api/auth/login`、`/api/auth/password-reset/**` 允许匿名访问
  - `/api/admin/**` 仅 `ADMIN` 可访问
  - 其余 `/api/**` 需要已登录

## 数据模型概览

### User

系统后台账号，包含管理员和医生两类角色。核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 用户主键 |
| `name` | `String` | 用户姓名 |
| `email` | `String` | 登录邮箱，唯一 |
| `role` | `Role` | 角色：`ADMIN` / `DOCTOR` |
| `active` | `boolean` | 账号是否启用 |
| `doctorProfile` | `DoctorProfile` | 医生扩展资料，仅医生角色有值 |

### DoctorProfile

医生扩展资料。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `title` | `String` | 职称 |
| `department` | `String` | 科室 |
| `avatarUrl` | `String` | 头像地址 |

### Patient

患者基础档案。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 患者主键 |
| `name` | `String` | 患者姓名 |
| `gender` | `Gender` | 性别：`MALE` / `FEMALE` / `OTHER` |
| `age` | `Integer` | 年龄 |
| `team` | `String` | 所属团队或分组 |
| `phone` | `String` | 联系电话 |
| `birthDate` | `LocalDate` | 出生日期 |
| `summary` | `String` | 病史摘要 |

### MedicalRecord

一次门诊病历记录。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 病历主键 |
| `patientId` | `Long` | 患者 ID |
| `doctorId` | `Long` | 医生 ID |
| `visitDate` | `LocalDate` | 就诊日期 |
| `visitTime` | `LocalTime` | 就诊时间 |
| `diagnosis` | `String` | 诊断结论 |
| `complaint` | `String` | 主诉 |
| `examination` | `String` | 检查结果 |
| `treatment` | `String` | 治疗方案 |
| `prognosis` | `String` | 预后说明 |
| `notes` | `String` | 备注 |
| `status` | `RecordStatus` | 状态：`IN_PROGRESS` / `COMPLETED` |
| `medications` | `Medication[]` | 用药清单 |

### Medication

病历下的用药明细。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 用药记录主键 |
| `name` | `String` | 药品名称 |
| `dosage` | `String` | 剂量或用法说明 |

## 枚举说明

### Role

| 值 | 说明 |
| --- | --- |
| `ADMIN` | 管理员 |
| `DOCTOR` | 医生 |

### Gender

| 值 | 说明 |
| --- | --- |
| `MALE` | 男 |
| `FEMALE` | 女 |
| `OTHER` | 其他或未说明 |

### RecordStatus

| 值 | 说明 |
| --- | --- |
| `IN_PROGRESS` | 病历编辑中 |
| `COMPLETED` | 病历已完成 |

## 接口列表

### 1. 认证接口

#### 1.1 登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 作用：使用邮箱和密码登录，建立会话并返回当前用户信息
- 是否需要登录：否

请求体：

```json
{
  "email": "admin@aura.local",
  "password": "Admin123!"
}
```

响应体：`UserResponse`

```json
{
  "id": 1,
  "name": "管理员",
  "email": "admin@aura.local",
  "role": "ADMIN",
  "active": true,
  "title": null,
  "department": null,
  "avatarUrl": null
}
```

#### 1.2 退出登录

- 方法：`POST`
- 路径：`/api/auth/logout`
- 作用：销毁当前会话并清理登录态
- 是否需要登录：是

响应体：

```json
{
  "message": "已退出登录"
}
```

#### 1.3 获取当前登录用户

- 方法：`GET`
- 路径：`/api/auth/me`
- 作用：返回当前会话对应的用户信息
- 是否需要登录：是

响应体：`UserResponse`

#### 1.4 请求密码重置令牌

- 方法：`POST`
- 路径：`/api/auth/password-reset/request`
- 作用：为指定邮箱生成密码重置令牌，本地原型环境会直接返回令牌
- 是否需要登录：否

请求体：

```json
{
  "email": "doctor@aura.local"
}
```

响应体：

```json
{
  "message": "已生成本地重置令牌",
  "resetToken": "uuid-token"
}
```

说明：

- 当邮箱不存在时，接口仍返回成功语义，但 `resetToken` 为 `null`

#### 1.5 确认密码重置

- 方法：`POST`
- 路径：`/api/auth/password-reset/confirm`
- 作用：使用重置令牌设置新密码
- 是否需要登录：否

请求体：

```json
{
  "token": "uuid-token",
  "newPassword": "NewPass123!"
}
```

响应体：

```json
{
  "message": "密码已重置"
}
```

### 2. 仪表盘接口

#### 2.1 获取统计概览

- 方法：`GET`
- 路径：`/api/dashboard/summary`
- 作用：返回患者数、病历数、本月病历数、医生数
- 是否需要登录：是

响应体：

```json
{
  "patientCount": 20,
  "recordCount": 88,
  "monthlyRecords": 12,
  "doctorCount": 4
}
```

### 3. 医生接口

#### 3.1 获取医生列表

- 方法：`GET`
- 路径：`/api/doctors`
- 作用：返回所有医生账号，供病历创建时下拉选择
- 是否需要登录：是

响应体：`UserResponse[]`

### 4. 患者接口

#### 4.1 分页查询患者

- 方法：`GET`
- 路径：`/api/patients`
- 作用：分页查询患者，支持按姓名或团队模糊搜索
- 是否需要登录：是

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `query` | `String` | 否 | 姓名或团队关键字 |
| `page` | `int` | 否 | 页码，从 `0` 开始 |
| `size` | `int` | 否 | 每页数量 |
| `sort` | `String` | 否 | Spring Pageable 排序参数 |

响应体：Spring `Page<PatientResponse>`，典型结构：

```json
{
  "content": [
    {
      "id": 1,
      "name": "张三",
      "gender": "MALE",
      "age": 34,
      "team": "一组",
      "phone": "13800000000",
      "birthDate": "1992-05-01",
      "summary": "高血压随访"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### 4.2 新增患者

- 方法：`POST`
- 路径：`/api/patients`
- 作用：创建患者档案
- 是否需要登录：是

请求体：

```json
{
  "name": "张三",
  "gender": "MALE",
  "age": 34,
  "team": "一组",
  "phone": "13800000000",
  "birthDate": "1992-05-01",
  "summary": "高血压随访"
}
```

响应体：`PatientResponse`

#### 4.3 更新患者

- 方法：`PATCH`
- 路径：`/api/patients/{id}`
- 作用：更新指定患者档案
- 是否需要登录：是

路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 患者 ID |

请求体：与“新增患者”一致

响应体：`PatientResponse`

### 5. 病历接口

#### 5.1 分页检索病历

- 方法：`GET`
- 路径：`/api/medical-records`
- 作用：按关键词、医生、日期范围组合查询病历
- 是否需要登录：是

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `query` | `String` | 否 | 患者姓名或诊断关键字 |
| `doctorId` | `Long` | 否 | 医生 ID |
| `fromDate` | `LocalDate` | 否 | 开始日期，格式 `yyyy-MM-dd` |
| `toDate` | `LocalDate` | 否 | 结束日期，格式 `yyyy-MM-dd` |
| `page` | `int` | 否 | 页码，从 `0` 开始 |
| `size` | `int` | 否 | 每页数量 |
| `sort` | `String` | 否 | 排序参数；未传时默认按 `visitDate desc, visitTime desc` |

响应体：Spring `Page<MedicalRecordResponse>`

#### 5.2 查询病历详情

- 方法：`GET`
- 路径：`/api/medical-records/{id}`
- 作用：查询单条病历及其用药明细
- 是否需要登录：是

路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 病历 ID |

响应体示例：

```json
{
  "id": 10,
  "patientId": 1,
  "patientName": "张三",
  "patientGender": "MALE",
  "patientAge": 34,
  "doctorId": 2,
  "doctorName": "李医生",
  "visitDate": "2026-05-16",
  "visitTime": "09:30:00",
  "diagnosis": "上呼吸道感染",
  "complaint": "发热两天",
  "examination": "咽部充血",
  "treatment": "口服药物治疗",
  "prognosis": "预后良好",
  "notes": "三日后复诊",
  "status": "COMPLETED",
  "medications": [
    {
      "id": 1,
      "name": "阿莫西林",
      "dosage": "0.5g 每日三次"
    }
  ]
}
```

#### 5.3 新增病历

- 方法：`POST`
- 路径：`/api/medical-records`
- 作用：创建病历并写入关联用药
- 是否需要登录：是

请求体：

```json
{
  "patientId": 1,
  "doctorId": 2,
  "visitDate": "2026-05-16",
  "visitTime": "09:30:00",
  "diagnosis": "上呼吸道感染",
  "complaint": "发热两天",
  "examination": "咽部充血",
  "treatment": "口服药物治疗",
  "prognosis": "预后良好",
  "notes": "三日后复诊",
  "status": "COMPLETED",
  "medications": [
    {
      "name": "阿莫西林",
      "dosage": "0.5g 每日三次"
    }
  ]
}
```

说明：

- `status` 为空时后端默认写入 `COMPLETED`
- `medications` 可为空或不传

响应体：`MedicalRecordResponse`

### 6. 管理员用户接口

#### 6.1 查询账号列表

- 方法：`GET`
- 路径：`/api/admin/users`
- 作用：查询全部系统账号
- 是否需要登录：是，且必须为 `ADMIN`

响应体：`UserResponse[]`

#### 6.2 创建账号

- 方法：`POST`
- 路径：`/api/admin/users`
- 作用：创建管理员或医生账号；创建医生时会自动初始化医生资料
- 是否需要登录：是，且必须为 `ADMIN`

请求体：

```json
{
  "name": "李医生",
  "email": "doctor@aura.local",
  "password": "Doctor123!",
  "role": "DOCTOR",
  "title": "主治医师",
  "department": "全科门诊",
  "avatarUrl": "https://example.com/avatar.png"
}
```

说明：

- 当 `role = DOCTOR` 且未传 `title`、`department` 时，后端默认分别使用 `主治医师`、`全科门诊`

响应体：`UserResponse`

#### 6.3 更新账号

- 方法：`PATCH`
- 路径：`/api/admin/users/{id}`
- 作用：更新账号名称、启用状态，以及医生的扩展资料
- 是否需要登录：是，且必须为 `ADMIN`

路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 用户 ID |

请求体：

```json
{
  "name": "李主任",
  "active": true,
  "title": "副主任医师",
  "department": "内科门诊",
  "avatarUrl": "https://example.com/avatar-new.png"
}
```

说明：

- 普通管理员账号只会更新 `name` 和 `active`
- 医生账号额外支持更新 `title`、`department`、`avatarUrl`

响应体：`UserResponse`

## 统一异常语义

基于当前服务实现，可见的业务异常主要包括：

| 场景 | 典型提示 |
| --- | --- |
| 用户邮箱已存在 | `邮箱已存在` |
| 账号不存在 | `账号不存在` |
| 患者不存在 | `患者不存在` |
| 医生不存在 | `医生不存在` |
| 病历不存在 | `病历不存在` |
| 重置令牌无效或过期 | `重置令牌无效` / `重置令牌已过期` |

具体 HTTP 状态码以全局异常处理器配置为准。
