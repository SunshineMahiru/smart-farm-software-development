# Gitee规范

## 1. 适用范围

- 本规范适用于仓库 `D:\smart-farm-software-development` 的日常开发、提交、推送、联调和进度同步。
- 本规范以当前项目实际协作方式为准。
- 当前统一要求：**不新建分支，所有成员都直接在 `main` 上拉取、开发、提交、推送。**

## 2. 当前项目协作基线

- 项目名称：智慧农场数字孪生管控平台
- 主分支：`main`
- 远程仓库：`origin`
- 后端目录：`packages/servrer`
- 前端目录：`packages/web`
- 统一数据库脚本：`packages/servrer/db/init_schema.sql`
- 全局进度文档：`总工作进度.md`
- 成员3专项进度文档：`Member3工作进度.md`

## 3. 分支与推送规则

### 3.1 统一规则

- 不新建功能分支。
- 所有成员统一直接在 `main` 上操作。
- 每次开始开发前，必须先同步远端 `main` 最新代码。
- 每次推送前，必须再次确认本地 `main` 没有落后于远端。

### 3.2 标准流程

```bash
git checkout main
git pull origin main
```

完成本次小任务后：

```bash
git add .
git commit -m "feat(iot): 补充传感器历史趋势接口"
git pull --rebase origin main
git push origin main
```

### 3.3 为什么这样做

- 当前项目以快速协作为主，统一在 `main` 上推进。
- 这样可以减少分支管理成本。
- 但代价是每个人都必须更严格地遵守“小步提交、先拉后推、及时同步”的纪律。

## 4. Commit 信息规范

### 4.1 基本格式

```text
type(scope): 简短描述
```

### 4.2 常用类型

- `feat`：新增功能
- `fix`：修复问题
- `docs`：仅文档变更
- `style`：格式调整，不改逻辑
- `refactor`：重构
- `perf`：性能优化
- `test`：测试相关
- `chore`：脚手架、依赖、工具链等杂项修改

### 4.3 scope 建议

- `sys`：登录、权限、用户
- `plot`：地块
- `iot`：传感器、时序、告警、日报、WebSocket
- `supplier`：供应商
- `material`：农资库存
- `purchase`：采购入库
- `plan`：作物、种植计划
- `irrigation`：灌溉
- `farm`：农事日志
- `yield`：产量统计
- `progress`：进度文档
- `project`：项目级调整

### 4.4 本项目推荐示例

- `feat(sys): 新增当前用户信息接口`
- `feat(iot): 补充传感器历史趋势接口`
- `fix(iot): 修复概览统计状态判断错误`
- `feat(supplier): 完成供应商 CRUD`
- `docs(progress): 更新项目总工作进度`
- `chore(project): 补充项目协作规范`

## 5. 开发前、提交前、推送前要求

### 5.1 开发前

- 先执行 `git pull origin main`
- 确认当前任务范围
- 确认不会误改别人正在处理的公共文件

### 5.2 提交前

- 检查本次改动是否只包含当前任务需要的文件
- 检查是否误提交临时文件、编译产物、IDE 文件
- 检查数据库变更是否同步更新到 `packages/servrer/db/init_schema.sql`
- 检查文档变更是否和代码一致

### 5.3 推送前

- 再次执行一次同步，推荐：

```bash
git pull --rebase origin main
```

- 如果出现冲突，先解决冲突再推送
- 禁止跳过同步直接推送

## 6. 文件归属与职责边界

### 6.1 成员1

- 负责：`user`、`plot`
- 重点范围：登录鉴权、用户管理、地块管理、前端基础布局

### 6.2 成员2

- 负责：`crop`、`planting_plan`
- 重点范围：作物字典、种植计划、调度链路

### 6.3 成员3

- 负责：`sensor`、`sensor_data`、`device_alert`、IoT 日报、WebSocket 展示、IoT 可视化
- 重点范围：`packages/servrer/src/main/java/com/smartfarm/modules/iot`
- 成员3每次完成实际代码后，必须同步更新 `Member3工作进度.md`

### 6.4 成员4

- 负责：`agri_material_supplier`、`agri_material`、`agri_purchase_record`
- 重点范围：供应商、农资库存、采购入库

### 6.5 成员5

- 负责：`irrigation_record`、`farm_log`、`yield_stat`
- 重点范围：灌溉、农事、产量统计、大屏分析

## 7. 公共文件修改规范

以下文件属于公共文件，修改前后都应在组内同步：

- `packages/servrer/db/init_schema.sql`
- `总工作进度.md`
- `README.md`
- 后端公共层：`packages/servrer/src/main/java/com/smartfarm/common`
- 前端入口与公共工具：`packages/web/src/main.js`、`packages/web/src/utils`

修改公共文件时要求：

- 先在组内说明谁改、改什么
- 改完后提醒其他成员先 `git pull origin main`
- 不允许静默改完就推送

## 8. 进度文档同步规范

### 8.1 总原则

- 进度文档只能写当前仓库里已经真实完成、能从代码确认的事实。
- 不允许把计划、目标、设想写成“已完成”。

### 8.2 必须同步更新的场景

- 新增接口后
- 新增数据库表、索引、触发器、视图后
- 新增前端页面后
- 修复关键问题后
- 完成一轮联调后

### 8.3 更新要求

- 全局状态写入 `总工作进度.md`
- 成员3相关工作同步写入 `Member3工作进度.md`
- 文档状态必须和当前代码状态一致

## 9. 数据库协作规范

- 全组统一维护 `packages/servrer/db/init_schema.sql`
- 禁止只改本地数据库、不改统一 SQL 脚本
- 禁止重复定义公共基础表，例如 `user`、`plot`
- 新增字段、索引、触发器时，命名尽量保持统一
- 如果实体类启用了逻辑删除，建表脚本中必须有对应字段

## 10. 小步提交原则

- 每次只做一小块明确工作再提交
- 不要把多个无关模块混在一次提交里
- 鼓励一天多次小提交，不要攒一大堆再一次性提交
- 推送到 `main` 前要确保本次提交说明清楚、范围可追踪

## 11. 冲突处理规范

- 如果 `git pull --rebase origin main` 发生冲突，先停下来处理冲突
- 先看冲突文件是不是公共文件、是不是别人刚改过
- 不确定时先沟通，不要直接覆盖
- 冲突解决后再继续提交或推送

## 12. 禁止事项

- 禁止 `git push -f`
- 禁止未拉取远端最新代码就直接推送 `main`
- 禁止把别人的代码包装成自己的提交
- 禁止把占位代码、空文档、计划项写成“已完成”
- 禁止把无关文件一起混入提交
- 禁止未经沟通直接大改公共 SQL、公共配置、公共文档

## 13. 本项目推荐命令模板

### 日常开发

```bash
git checkout main
git pull origin main
```

### 本地提交

```bash
git add .
git commit -m "feat(iot): 补充传感器历史趋势接口"
```

### 推送前再同步

```bash
git pull --rebase origin main
```

### 推送主干

```bash
git push origin main
```

## 14. 最终原则

- 所有人统一在 `main` 上协作
- 每次必须先拉取、后开发、再提交、再同步、再推送
- 提交信息必须规范
- 进度文档必须和代码一致
- 公共文件修改必须先沟通
- 主干历史要清晰，每个人的贡献要能从提交记录看出来
