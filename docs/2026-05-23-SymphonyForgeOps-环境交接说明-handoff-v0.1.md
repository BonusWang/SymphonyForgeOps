# SymphonyForgeOps 环境交接说明 Handoff v0.1

## 版本索引 Version Index

- 最新版本：v0.1
- 日期：2026-05-23
- 用途：切换到配置好 Java/Maven/Node/Docker 的环境后继续执行。

## 1. 当前仓库位置

主工作目录：

```text
D:\Projects\SymphonyForgeOps
```

参考项目：

```text
D:\Projects\openai-symphony
D:\Projects\WikiForge
D:\Projects\team-workload
```

远端：

```text
https://github.com/BonusWang/SymphonyForgeOps.git
```

分支：

```text
main
```

当前提交：

```text
8e7b7af Align ForgeOps with Symphony workflow model
23e2e58 Bootstrap SymphonyForgeOps mission control
```

本轮文档完成后会有新的提交。

## 2. 本轮已完成内容

代码：

- Spring Boot 后端骨架。
- Vue 3 前端骨架。
- Dashboard 静态/内存数据展示。
- MySQL 8 Compose 配置。
- Flyway migration：
  - `V20260523_001__create_forgeops_control_tables.sql`
  - `V20260523_002__add_symphony_runtime_tables.sql`
- `ApiResponse<T>`。
- 前端 `utils/request.ts`。
- 根目录 `WORKFLOW.md`。

文档：

- 参考项目学习记录。
- 全量 PRD。
- 开发任务计划。
- 检查验收计划。
- 环境交接说明。

## 3. 当前环境限制

当前机器/当前 shell 观察到：

- 全局 Java 是 8，不满足 Java 21。
- `mvn` 不可用。
- `npm` 不可用。
- `node.exe` 执行被拒绝。
- `docker` 不可用。

因此未完成：

- `mvn test`。
- `npm run build`。
- `docker compose config`。
- MySQL Flyway 实跑。
- 浏览器视觉验证。

这属于环境问题，不是已知代码问题。

## 4. 敏感信息处理

用户曾在聊天中提供 GitHub PAT。处理原则：

- 没有写入任何文件。
- 没有写入 Git remote。
- 没有在命令中使用。
- 已用 `rg` 搜索项目内敏感关键词，未发现 token。

建议：

- 在 GitHub 中 revoke 该旧 PAT。
- 重新生成新 PAT。
- 后续只通过环境变量或凭据管理器使用，例如：

```powershell
$env:GITHUB_TOKEN="***"
```

## 5. 下一环境启动顺序

### 5.1 拉取或使用本地仓库

如果 D 盘仓库已拷贝过去：

```powershell
cd D:\Projects\SymphonyForgeOps
git status --short
git log --oneline -5
```

如果从 GitHub 拉取：

```powershell
cd D:\Projects
git clone https://github.com/BonusWang/SymphonyForgeOps.git
cd SymphonyForgeOps
```

### 5.2 环境检查

```powershell
java -version
mvn --version
node --version
npm --version
docker --version
docker compose version
```

### 5.3 数据库启动

```powershell
docker compose -f deploy\docker-compose.dev.yml up -d forgeops-mysql
docker compose -f deploy\docker-compose.dev.yml ps
```

### 5.4 后端验证

```powershell
cd backend
mvn -B test
mvn -pl forgeops-api spring-boot:run
```

另开终端：

```powershell
curl http://localhost:8090/api/v1/health
curl http://localhost:8090/api/v1/dashboard
```

### 5.5 前端验证

```powershell
cd frontend
npm install
npm run build
npm run dev
```

访问：

```text
http://localhost:5173
```

## 6. 如果第一轮验证失败

### Java 版本失败

先安装或切换 Java 21，不改代码。

### Maven 仓库失败

确认：

```text
backend/.mvn/maven.config
```

当前内容：

```text
-Dmaven.repo.local=E:/repository
```

如果新机器没有 E 盘仓库，可以临时改为本机路径，或删除该行使用默认仓库。若要提交变更，需同步 README。

### MySQL 连接失败

检查 `.env.example` 和 Compose 端口：

```text
FORGEOPS_MYSQL_PORT=3308
FORGEOPS_DATASOURCE_URL=jdbc:mysql://localhost:3308/forgeops...
```

Docker 内部 API 连接 MySQL 使用：

```text
jdbc:mysql://forgeops-mysql:3306/forgeops...
```

### 前端 API 失败

确认 `frontend/vite.config.ts` proxy：

```text
/api -> http://localhost:8090
```

前端请求代码使用：

```text
/api/v1/dashboard
```

当前 `request` baseURL 是 `/api`，业务 API 调用 `/v1/dashboard`。

## 7. 下一批建议任务

严格按顺序：

1. `ENV-001` 环境验证。
2. `DB-001` MySQL 与 Flyway 验证。
3. `API-001` 后端启动与 health/dashboard。
4. `FE-001` 前端构建与页面修复。
5. `PERSIST-001` Project Registry 持久化。
6. `PERSIST-002` Work Order 持久化。
7. `WF-001` WORKFLOW.md Parser。

不要先做：

- Codex app-server adapter。
- 多 agent 并发调度。
- GitHub PR 自动 merge。
- 远程 worker。
- 复杂权限系统。

原因：

- 当前还没有完成数据库持久化闭环。
- 先要把 Project / Work Order / Workflow Contract 的控制平面做稳。

## 8. Completion Report 模板

下一环境每完成一批任务，按这个格式记录：

```text
完成内容：
影响服务：
修改文件：
验证结果：
归档文件：
已知风险：
下一步：
```

## 9. 当前风险

- 当前 Dashboard 使用内存数据，不是数据库真实数据。
- `WORKFLOW.md` parser 尚未实现。
- Flyway SQL 尚未在 MySQL 8 实跑。
- 前端深色控制台改版尚未通过 `npm run build`。
- 后端已引入 MySQL/Flyway，测试环境必须有数据库或测试 profile。
- GitHub PAT 已在聊天中出现，建议 revoke。

