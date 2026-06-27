# Member3工作进度

## 成员3长期执行基线

### 1. 身份与边界

- 成员3职责固定为：`sensor`、`sensor_data`、`device_alert` 三个业务域，以及与其直接相关的 AI 农情分析、时序聚合、WebSocket 主动推送、数字孪生可视化页面。
- 后端代码只能放在 `packages/servrer/src/main/java/com/smartfarm/modules/iot` 下继续扩展。
- 前端代码只能优先放在 `packages/web/src/views/iot`、`packages/web/src/components` 下的 IoT 相关区域，不要越界去改其他成员的主业务页面。
- 如需复用系统能力，只能复用现有公共层，例如 `common/utils`、`common/websocket`、`common/security`，不要跨模块直接硬耦合别人的业务 Service。

### 2. 必须严格遵守的总项目要求

- 项目架构：前后端分离单体架构，不拆微服务。
- 后端技术栈：`Spring Boot 3.x`、`MyBatis-Plus`、`Sa-Token`、`WebSocket`、`Spring AI`。
- 前端技术栈：`Vue 3`、`Vite`、`Element Plus`、`Pinia`、`Vue Router`、`ECharts`、`CSS Grid / Canvas / 3D Transform`。
- 数据库：统一使用 `MySQL 8.0.x`，不得私自切到 PostgreSQL、SQLite 或其他数据库。
- Java 版本：固定 `JDK 17`。
- Node 版本：固定 `Node.js 18.x`。
- 接口风格：统一走 RESTful JSON 接口，统一使用项目已有 `Result<T>` 返回结构。
- 鉴权方式：必须兼容现有 `Sa-Token` 体系，请求头使用 `Authorization`。
- 文档要求：后续新增 Controller、DTO、接口说明时，要满足 Swagger / Knife4j 或 Apifox 可联调要求。
- Git 要求：在个人 `feat-*` 分支开发，禁止直接推送 `main/master`。
- 数据库要求：统一维护 `packages/servrer/db/init_schema.sql`，如果需要增表、增索引、增视图，必须在统一脚本里维护，不能只改本地库不改脚本。

### 3. 成员3最终目标

- 完成传感器、时序数据、告警记录三类核心接口。
- 完成近 24 小时环境聚合、AI 农情日报生成、日报查询、日报推送。
- 完成 WebSocket 消息接收与推送闭环。
- 完成 2.5D 大棚热力图页面。
- 完成双 Y 轴温湿度历史曲线图页面。
- 完成 IoT 管理页面，包括传感器管理、时序数据查询、告警记录管理。
- 完成 Spring AI 真正接入，而不是只停留在规则化文案。
- 完成时序数据查询性能优化，并能支撑 README 里要求的 `2.8 万条` 数据量演示。

## 2026.6.26

- 在 `packages/servrer/src/main/java/com/smartfarm/modules/iot` 新增 IoT 后端模块第一批接口。
- 实现 `GET /iot/overview`，用于返回传感器总数、在线数、离线数、今日采集条数、告警总数、未处理告警数、最近采集时间。
- 实现 `GET /iot/recent-data?sensorId=...&limit=...`，用于查询指定传感器的最近时序数据。
- 实现 `GET /iot/alerts/latest?limit=...`，用于查询最新设备告警列表，并联表返回地块名称。
- 以上接口直接复用现有 `sensor`、`sensor_data`、`device_alert`、`plot` 表结构，作为成员3 IoT 监测与预警能力的第一部分落地。
- 重新对齐成员3原始分工文档后，补充了 AI 农情微气候日报能力。
- 在数据库脚本中新增 `iot_daily_report` 日报表，并增加日报生成时间索引。
- 新增 `POST /iot/reports/generate` 手动生成日报接口与 `GET /iot/reports/latest` 最新日报查询接口。
- 新增 `IotDailyReportScheduler`，按每天 `7:00` 自动聚合近 24 小时时序数据与告警数据，生成农情日报。
- 日报生成后自动调用 WebSocket 广播给在线用户，先以规则化自然语言完成“AI 风格诊断”骨架，后续可继续接 Spring AI 真正模型接口。

## 2026.6.27

- 新增 `GET /iot/history/trend?sensorId=1&hours=24`，用于返回某个传感器近若干小时的温湿度历史曲线数据。
- 返回结构补充为图表友好格式，直接包含 `sensorId`、`sensorName`、`sensorType`、时间范围和按时间升序排列的 `points` 列表，减少前端二次转换。
- 查询语句严格使用 `sensor_id + collect_time` 范围条件，直接服务后续双 Y 轴温湿度曲线图页面。

## 当前已完成能力清单

### 后端接口

- `GET /iot/overview`
- `GET /iot/recent-data?sensorId=...&limit=...`
- `GET /iot/alerts/latest?limit=...`
- `GET /iot/history/trend?sensorId=...&hours=24`
- `POST /iot/reports/generate`
- `GET /iot/reports/latest`

### 后端基础能力

- 已建立 `modules/iot` 包结构。
- 已建立 IoT 查询 Service / Mapper / VO 基础组织方式。
- 已建立日报聚合、日报落库、日报广播推送的第一版骨架。
- 已补某传感器近 24 小时温湿度曲线接口，可直接服务前端折线图。
- 已在数据库初始化脚本中加入 `iot_daily_report` 表。
- 已通过 `mvn -DskipTests compile` 编译验证。

### 当前不足

- 还没有完成 `sensor`、`sensor_data`、`device_alert` 的完整 CRUD。
- 还没有实现“写入时序数据后自动判定是否触发告警”的业务逻辑。
- 还没有真正接入 `Spring AI` 大模型接口。
- 还没有实现前端 `2.5D` 热力图页面。
- 还没有实现前端双 Y 轴 ECharts 页面。
- 还没有完成 IoT 模块的 Swagger 注解补齐。
- 还没有补测试数据校验、异常边界验证、接口联调说明。

## 后续任务总清单

### 第一阶段：把成员3后端做完整

- 任务 1：补齐 `sensor` 传感器管理 CRUD。
说明：需要有新增、分页查询、详情、修改、删除接口，字段至少覆盖 `plot_id`、`sensor_name`、`sensor_type`、`install_date`、`status`。
完成标准：前端可以对传感器做完整维护；接口返回统一 `Result<T>`；具备基础参数校验。

- 任务 2：补齐 `device_alert` 告警记录 CRUD 与查询接口。
说明：除标准 CRUD 外，至少要支持按时间范围、地块、告警状态筛选。
完成标准：能用于后续告警管理页面；能够清楚区分“未处理/已处理”。

- 任务 3：补齐 `sensor_data` 时序数据查询接口。
说明：时序数据一般不建议做任意删除编辑，但至少要支持分页查询、按 `sensor_id` 查询、按时间范围查询、近 24 小时查询。
完成标准：能够给折线图和热力图页面提供稳定数据源。

- 任务 4：实现“写入时序数据 -> 规则判定 -> 自动生成告警”链路。
说明：新增一个写入传感器数据接口，插入 `sensor_data` 后按阈值判定是否生成 `device_alert`。阈值可以先按温度、湿度、土壤湿度做固定规则。
完成标准：调用一次数据写入接口后，如果数据越界，数据库中能新增一条告警记录。

- 任务 5：补齐成员3后端注解和接口文档。
说明：Controller、请求参数、返回对象补 Swagger / OpenAPI 注解说明。
完成标准：Swagger 页面里能清楚看到 IoT 接口的用途、参数和返回值。

### 第二阶段：把 AI 与调度链路做成项目亮点

- 任务 6：真正接入 `Spring AI`。
说明：当前日报内容是规则化生成，只是占位骨架。后续需要引入项目要求中的 `Spring AI`，把近 24 小时聚合结果组装成 Prompt，再调用模型生成结构化农情诊断。
完成标准：日报内容来源于模型；支持失败降级；模型异常时不能阻塞主流程。

- 任务 7：设计统一 Prompt。
说明：Prompt 要限制模型只输出结构化农情诊断，不允许无关寒暄。建议输出结构为“环境概览 / 异常分析 / 风险判断 / 管理建议”。
完成标准：同一批输入数据，多次输出风格稳定，便于答辩展示。

- 任务 8：补齐 AI 失败重试与降级方案。
说明：若模型接口超时或失败，应回退到规则化报告文案，不影响日报落库和 WebSocket 推送。
完成标准：即使 AI 服务不可用，`/iot/reports/generate` 也能成功返回并写库。

- 任务 9：完善定时任务日志与幂等控制。
说明：当前日报按日期防重复生成，但还可以补充日志说明、异常捕获、失败记录。
完成标准：每天重复触发也不会插入多条同日报；出错时日志能定位问题。

### 第三阶段：把时序查询和性能优化做扎实

- 任务 10：优化 `sensor_data` 查询方式。
说明：严格使用时间范围查询、索引命中查询，不要做全表扫描；避免深度分页导致性能差。
完成标准：常用查询能稳定走 `idx_collect_time` 或 `idx_sensor_time`。

- 任务 11（已完成）：补充按时间范围和传感器维度的接口。
说明：至少补一个接口用于“某传感器近 24 小时温湿度曲线”，必要时返回前端图表友好的结构。
完成标准：前端折线图不需要自己再做大量数据转换。

- 任务 12：整理性能验证说明。
说明：结合 `2.8 万条` 初始化数据，记录查询方式、SQL 索引、分页策略、预期响应。
完成标准：答辩时能讲清楚为什么你的查询不会慢。

### 第四阶段：完成成员3前端页面

- 任务 13：新建 IoT 模块页面目录。
说明：优先规划 `packages/web/src/views/iot`，建议至少分为 `SensorManage.vue`、`SensorHistory.vue`、`AlertManage.vue`、`IotTwinDashboard.vue`。
完成标准：目录和职责清晰，不和其他成员页面混杂。

- 任务 14：完成“传感器管理”页面。
说明：表格 + 弹窗表单，支持新增、编辑、删除、状态切换。
完成标准：和后端 `sensor` CRUD 联调通过。

- 任务 15：完成“告警记录管理”页面。
说明：支持表格查看、状态筛选、时间筛选、标记处理。
完成标准：和 `device_alert` 接口联调通过。

- 任务 16：完成“时序数据查询”页面。
说明：支持按传感器、按时间范围查询，支持表格和图表切换。
完成标准：可作为答辩时展示“2.8 万条数据查询”的页面入口。

- 任务 17：完成双 Y 轴 ECharts 折线图组件。
说明：一条轴展示温度，一条轴展示湿度；支持缩放、时间轴滑动。
完成标准：点击某地块或某传感器后可直接打开该图表。

- 任务 18：完成 2.5D 大棚热力图页面。
说明：按照总项目要求，用 `CSS Grid / Canvas / 3D Transform` 实现，不要退化成普通二维表格。颜色要根据实时温度或综合状态动态变化。
完成标准：能直观展示多个地块/大棚的空间感和颜色热度差异；点击后联动侧边栏或图表。

- 任务 19：接入 WebSocket 客户端通知。
说明：项目已有前端 `websocket.js` 基础代码，成员3页面需要确保能接收到日报与告警广播。
完成标准：手动触发日报生成后，前端能弹出 Notification。

### 第五阶段：联调、验收、答辩材料

- 任务 20：补完整的成员3接口联调清单。
说明：把登录、拿 Token、访问 IoT 接口、生成日报、查看日报、触发通知的顺序写清楚。
完成标准：任何人照步骤都能复现你的成果。

- 任务 21：补异常场景验证。
说明：包括非法 `sensorId`、超出范围的 `limit`、AI 接口失败、WebSocket 未连接、数据库无数据等情况。
完成标准：能说明系统在异常情况下不会直接崩。

- 任务 22：准备答辩讲解顺序。
说明：建议顺序为“数据表设计 -> 时序查询 -> 告警生成 -> AI 日报 -> WebSocket 推送 -> 2.5D 热力图 -> 双 Y 轴曲线图”。
完成标准：讲解逻辑连贯，突出成员3是项目核心亮点。

## 推荐执行顺序

- 第一步：先补完整后端 CRUD，尤其是 `sensor` 和 `device_alert`。
- 第二步：补“写时序数据自动触发告警”逻辑。
- 第三步：补 Spring AI 真接入和降级方案。
- 第四步：补前端 IoT 管理页面。
- 第五步：补 2.5D 热力图和双 Y 轴图表，作为答辩视觉亮点。
- 第六步：补验证文档和接口联调说明。

## 后续开发时必须自检的规则

- 新增数据库结构时，必须同步修改 `packages/servrer/db/init_schema.sql`。
- 新增后端类时，必须放在 `modules/iot` 下，不要破坏目录规范。
- 新增接口时，必须复用统一返回体 `Result<T>`。
- 新增需要登录的接口时，必须兼容现有 `Sa-Token` 鉴权体系。
- 任何“AI 相关功能”都必须保留降级方案，不能让外部模型成为单点故障。
- 时序数据相关查询必须优先考虑索引与范围查询，不能为了省事写全表扫描。
- 前端可视化必须符合项目要求，热力图不能偷换成普通列表，图表不能只做静态截图。
- 每完成一块，都要能通过“启动后端 -> 登录 -> 调接口 -> 看数据库/页面结果”这条链路验证。

## 常用验证路径

- 编译验证：在 `packages/servrer` 下执行 `mvn -DskipTests compile`。
- 后端启动：在 `packages/servrer` 下执行 `mvn spring-boot:run`。
- 登录拿 Token：`POST /sys/user/login?username=admin&password=123456`
- 已完成接口：
  - `GET /iot/overview`
  - `GET /iot/recent-data?sensorId=1&limit=10`
  - `GET /iot/alerts/latest?limit=5`
  - `GET /iot/history/trend?sensorId=1&hours=24`
  - `POST /iot/reports/generate`
  - `GET /iot/reports/latest`
- 数据库验证重点：
  - `sensor`
  - `sensor_data`
  - `device_alert`
  - `iot_daily_report`

## 以后更新这个文件的规则

- 每次完成实际代码后，都在对应日期下追加“今天完成了什么”。
- 如果新增了数据库表、索引、视图、定时任务、前端页面，也要单独记一条。
- 如果某个后续任务已经完成，就从“后续任务总清单”里标记为已完成，或者在标题后补 `已完成`。
- 以后优先以这个文件为准，不再依赖原始 Word 分工文档，但前提是这里的内容必须始终遵守总项目 README 的技术栈、架构、数据库和协作规范。
