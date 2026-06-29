# Member2 工作进度

## 成员2长期执行基线

### 1. 身份与边界

- 成员2固定负责 `crop`、`planting_plan`、定时调度与生命周期联动。
- 后端代码主要落在 `packages/server/src/main/java/com/smartfarm/modules/plan`。
- 前端代码主要落在 `packages/web/src/views/plan` 与 `packages/web/src/api/member2.js`。
- 与其他成员的共享边界只复用既有 `plot`、`user`、`WebSocket`、统一 `Result<T>` 和登录态，不跨模块硬编码调用其他业务 Service。

### 2. 必须满足的项目要求

- 作物字典和种植计划都要提供真实 CRUD，而不是占位页。
- 种植计划必须根据 `start_date + growth_cycle_days` 自动推算 `expected_harvest`。
- 地块处于“休耕”时必须驳回计划创建或更新。
- 同一地块不可出现时间区间重叠的有效计划。
- 需要具备 `@Scheduled` 定时调度能力，并能打通到 WebSocket 通知。
- 前端首页和顶部导航中的成员2入口必须进入真实业务页面。

## 2026.6.29

- 新增后端业务包 `modules/plan`，补齐 `Crop`、`PlantingPlan` 的 `entity / dto / vo / mapper / service / controller / job` 完整结构。
- 新增 `GET /plan/crops`、`GET /plan/crops/options`、`GET /plan/crops/{cropId}`、`POST /plan/crops`、`PUT /plan/crops/{cropId}`、`DELETE /plan/crops/{cropId}`。
- 新增 `GET /plan/plans`、`GET /plan/plans/{planId}`、`GET /plan/plans/calendar`、`GET /plan/plans/reminders`、`POST /plan/plans`、`PUT /plan/plans/{planId}`、`DELETE /plan/plans/{planId}`。
- 作物删除前增加计划引用校验，避免删除已被种植计划使用的作物字典。
- 种植计划保存时增加联动校验：校验地块、作物、创建人是否存在；校验地块不是“休耕”；校验种植面积不超过地块面积；校验同一地块不存在时间重叠的有效计划。
- 种植计划保存时自动按作物生长周期推算预计采收日，不依赖前端手填。
- 种植计划状态实现自动生命周期推断：按当前日期自动落到 `未开始 / 进行中 / 已完成`，仅允许手动指定 `已取消`。
- 新增 `PlanLifecycleScheduler`，每天 `07:00` 执行计划生命周期刷新，并通过现有 `WebSocketServer.broadcastAll` 推送种植调度摘要。
- 新增地块状态联动：存在有效计划时自动置为 `种植中`，无有效计划时回落为 `空闲`，但不覆盖“休耕/维护中”。
- 新增前端 `packages/web/src/api/member2.js`，封装成员2页面所需的作物、计划、日历、提醒、地块接口。
- 新增前端页面 `packages/web/src/views/plan/Member2PlanView.vue`，落地成员2真实页面，包含计划筛选表格、作物字典表格、月度种植日历、摘要卡片、增删改弹窗。
- 页面进入时初始化 WebSocket 连接，用于接收成员2定时调度和成员3日报/告警广播通知。
- 将路由 `/member2` 从 `ModulePlaceholderView` 替换为真实页面，并补齐顶部导航成员2下拉入口。

## 当前已完成能力清单

### 后端接口

- `GET /plan/crops`
- `GET /plan/crops/options`
- `GET /plan/crops/{cropId}`
- `POST /plan/crops`
- `PUT /plan/crops/{cropId}`
- `DELETE /plan/crops/{cropId}`
- `GET /plan/plans`
- `GET /plan/plans/{planId}`
- `GET /plan/plans/calendar`
- `GET /plan/plans/reminders`
- `POST /plan/plans`
- `PUT /plan/plans/{planId}`
- `DELETE /plan/plans/{planId}`

### 前端页面

- `/member2`
- 首页成员2卡片跳转到真实成员2页面
- 顶部导航成员2入口与下拉菜单已接入真实页面

### 已落地业务能力

- 作物字典真实 CRUD
- 种植计划真实 CRUD
- 预计采收日期自动推算
- 地块休耕拦截
- 计划时间重叠校验
- 地块状态联动
- 月度种植日历视图
- 成员2定时调度 + WebSocket 摘要通知

## 当前不足

- 当前日历视图为自绘月度排期板，不是第三方 FullCalendar 组件，但已能满足计划排期展示和答辩演示。
- 暂未补成员2自动化测试代码，仍以编译和构建校验为主。

## 常用验证路径

- 后端编译：`cd packages/server && mvn -DskipTests compile`
- 前端构建：`cd packages/web && npm run build`
- 成员2页面：`/member2`
- 计划提醒接口：`GET /plan/plans/reminders`
- 计划日历接口：`GET /plan/plans/calendar?startDate=2026-06-01&endDate=2026-08-31`
