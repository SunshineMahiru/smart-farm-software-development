# Member1 工作进度

## 成员1长期执行基线

### 1. 身份与边界

- 成员1职责固定为：系统基建、登录鉴权、用户管理、地块台账、WebSocket 服务端基础设施。
- 后端代码优先放在 `packages/server/src/main/java/com/smartfarm/modules/sys` 和 `packages/server/src/main/java/com/smartfarm/modules/farm` 中与成员1职责相关的区域。
- 公共安全、异常、WebSocket 基础设施放在 `packages/server/src/main/java/com/smartfarm/common`。
- 不跨成员业务域实现 IoT、供应链、种植计划、农事产量等业务逻辑，只通过用户和地块等共享基础数据支撑其他模块。

### 2. 必须遵守的项目要求

- 后端技术栈：`Spring Boot 3.x`、`MyBatis-Plus`、`Sa-Token`、`WebSocket`、`Validation`、`springdoc-openapi`。
- 数据库统一使用 `MySQL 8.0.x`，用户与地块复用统一 `init_schema.sql` 中的 `user`、`plot` 表。
- 接口统一返回 `Result<T>`。
- 需要登录的接口必须兼容 Sa-Token，请求头使用 `Authorization`。
- 后端 Controller 与 DTO 需要补 Swagger/OpenAPI 注解，便于联调。

## 2026.6.29

- 补齐用户管理后端接口，覆盖分页查询、详情、新增、修改、重置密码、删除。
- 用户列表和详情统一返回 `UserInfoVO`，不向前端返回密码字段。
- 新增用户、修改用户时校验账号唯一、手机号唯一、角色合法性。
- 删除用户前检查是否被种植计划、灌溉记录、农事日志、采购入库记录引用，避免外键异常直接暴露。
- 禁止删除当前登录用户，并保留至少一个管理员账号。
- 补齐地块台账后端接口，覆盖分页查询、详情、新增、修改、状态流转、删除、状态汇总。
- 地块分页支持关键字、状态、土壤类型、位置筛选。
- 地块状态流转覆盖 `空闲 / 种植中 / 休耕 / 维护中`，非法流转会返回业务错误。
- 删除地块前检查种植计划、灌溉记录、传感器、设备告警引用，避免误删共享基础资产。
- 补齐成员1相关接口 Swagger/OpenAPI 注解。
- 扩展 Sa-Token 权限列表，新增 `user:read`、`plot:read`、`plot:write`。
- 补充权限不足异常处理，`NotPermissionException` 统一返回 403。
- 修正后端 Maven 打包类型为 `jar`，确保 `mvn -DskipTests compile` 会真实编译 Java 源码。
- 数据库整合侧检查 `packages/server/db/init_schema.sql` 与五份原始成员 SQL，确认成员5原始脚本主要包含 `farm_log`、`yield_stat`、索引、视图、过程、触发器，但原始脚本没有初始化数据。
- 补齐统一初始化脚本中的成员5演示数据：`farm_log` 由少量静态记录改为批量生成 180 条，覆盖整地、播种、施肥、灌溉、除草、病虫害巡检、病虫害防治、修剪、采收、巡田 10 类操作。
- 补齐统一初始化脚本中的成员5产量统计数据：`yield_stat` 批量生成 90 条，覆盖 `优 / 良 / 合格` 质量等级，用于分页、质量筛选、产量汇总和大屏趋势展示。
- 补回成员5原始脚本中的 `idx_grade_weight` 复合索引，提升按质量等级和产量重量筛选的查询效率。
- 修复统一初始化脚本导入阻断点：兼容 MySQL 8.0.12 的 `sensor.install_date` 定义、修正种植计划 CTE 插入语法、避开休耕地块灌溉触发器、采购和农事日志人员外键改为动态引用实际存在用户。

## 当前已完成能力清单

### 后端接口

- `POST /sys/user/login`
- `POST /sys/user/logout`
- `GET /sys/user/me`
- `GET /sys/user`
- `GET /sys/user/{userId}`
- `POST /sys/user`
- `PUT /sys/user/{userId}`
- `PATCH /sys/user/{userId}/password`
- `DELETE /sys/user/{userId}`
- `GET /farm/plots`
- `GET /farm/plots/{plotId}`
- `GET /farm/plots/status-summary`
- `POST /farm/plots`
- `PUT /farm/plots/{plotId}`
- `PATCH /farm/plots/{plotId}/status`
- `DELETE /farm/plots/{plotId}`

### 后端基础能力

- 已接入 Sa-Token 登录校验和接口级权限控制。
- 已接入全局异常处理，覆盖参数校验、未登录、角色不足、权限不足、业务异常。
- 已接入 WebSocket 服务端基础设施。
- 已完成成员1绑定的 `user`、`plot` 后端 CRUD 和业务校验。
- 已完成统一数据库初始化脚本检查与成员5数据补齐，`farm_log=180`、`yield_stat=90`。
- 已通过 `mvn -DskipTests compile` 编译验证。

## 当前不足

- 成员1前端登录页、主 Layout、动态菜单、用户权限管理页面、地块台账页面仍未完成。
- 当前用户密码仍按初始化数据保持明文兼容，尚未引入密码哈希迁移方案。
- 当前没有自动化测试代码，后续可补用户管理和地块状态流转的接口测试。

## 常用验证路径

- 编译验证：在 `packages/server` 下执行 `mvn -DskipTests compile`。
- 后端启动：在 `packages/server` 下执行 `mvn spring-boot:run`。
- 登录拿 Token：`POST /sys/user/login`，请求体可使用 `{"username":"admin01","password":"123456"}`。
- 使用管理员 Token 访问用户写接口和地块写接口。
- 使用农技员 Token 验证写接口返回 403。
- SQL 初始化验证：将 `init_schema.sql` 替换为临时库名后导入 MySQL 8.0.12，确认 `farm_log=180`、`yield_stat=90`、`idx_grade_weight` 存在。
