# SymphonyForgeOps 检查验收计划 Check Plan v0.1

## 版本索引 Version Index

- 最新版本：v0.1
- 日期：2026-05-23
- 范围：环境切换后的检查、构建、测试、部署和安全验收。

## 1. 检查分层

检查分为七层：

1. 环境检查。
2. Git 与敏感信息检查。
3. 后端构建与测试。
4. 数据库迁移检查。
5. 前端构建与页面检查。
6. Docker Compose 检查。
7. 集成与人工审核检查。

## 2. 环境检查

命令：

```powershell
java -version
mvn --version
node --version
npm --version
docker --version
docker compose version
git --version
```

通过标准：

- Java 为 21。
- Maven 可用。
- Node 为 20/22。
- Docker Compose 可用。
- Git 可用。

失败处理：

- Java 不是 21：先修环境，不改代码。
- Maven 不可用：安装 Maven 或确认 IDE bundled Maven。
- Node 不可用：安装 Node 20/22。
- Docker 不可用：先启用 Docker Desktop。

## 3. Git 与敏感信息检查

命令：

```powershell
git status --short
git remote -v
rg "github_pat|ghp_|TOKEN|API_KEY|SECRET|PASSWORD" -n .
git diff --check
```

通过标准：

- 工作区变更可解释。
- remote 不包含 token。
- 没有 GitHub PAT、模型 key、数据库密码明文泄露到非模板文件。
- `.env` 未提交。
- `git diff --check` 无错误。

注意：

- `.env.example` 可以包含开发默认密码，但不能包含真实 PAT。
- 用户曾在聊天中提供过 PAT，建议在 GitHub 侧 revoke 并重新生成。

## 4. Maven 仓库检查

当前本地 Maven 仓库：

```text
E:\repository
```

检查：

```powershell
cd D:\Projects\SymphonyForgeOps\backend
mvn -B help:effective-settings
```

通过标准：

- 输出中使用 `E:/repository` 或 Maven 实际解析到该本地仓库。
- 依赖可以正常下载或复用。

## 5. 后端检查

基础命令：

```powershell
cd D:\Projects\SymphonyForgeOps\backend
mvn -B test
mvn -B -DskipTests package
```

通过标准：

- 测试通过。
- jar 生成成功。
- 无 Java 版本不匹配。

接口检查：

```powershell
mvn -pl forgeops-api spring-boot:run
curl http://localhost:8090/api/v1/health
curl http://localhost:8090/api/v1/dashboard
curl http://localhost:8090/actuator/health
```

通过标准：

- `/api/v1/health` 返回 `success=true`。
- `/api/v1/dashboard` 返回项目、工作单、运行和审核数据。
- `/actuator/health` 返回 UP。

## 6. 数据库检查

启动 MySQL：

```powershell
cd D:\Projects\SymphonyForgeOps
docker compose -f deploy\docker-compose.dev.yml up -d forgeops-mysql
docker compose -f deploy\docker-compose.dev.yml ps
```

检查配置：

```powershell
docker compose -f deploy\docker-compose.dev.yml config
```

通过标准：

- `forgeops-mysql` healthy。
- 使用 `mysql:8.0`。
- 数据库名为 `forgeops`。
- 端口默认 `3308`。

Flyway 检查：

```powershell
cd D:\Projects\SymphonyForgeOps\backend
mvn -B -pl forgeops-api -am test
```

通过标准：

- `V20260523_001__create_forgeops_control_tables.sql` 执行成功。
- `V20260523_002__add_symphony_runtime_tables.sql` 执行成功。
- MySQL JSON 字段兼容。

## 7. 前端检查

命令：

```powershell
cd D:\Projects\SymphonyForgeOps\frontend
npm install
npm run build
npm run dev
```

通过标准：

- TypeScript 无错误。
- Vite build 成功。
- 本地页面可打开。
- Dashboard 请求 `/api/v1/dashboard` 成功。

视觉检查：

- 左侧导航不挤压。
- 统计卡片在桌面和窄屏不重叠。
- 表格文字不明显溢出。
- 错误提示可见。
- 页面不是营销页，而是操作型控制台。

## 8. Docker 检查

命令：

```powershell
cd D:\Projects\SymphonyForgeOps
docker compose -f deploy\docker-compose.dev.yml config
docker compose -f deploy\docker-compose.dev.yml build
docker compose -f deploy\docker-compose.dev.yml up -d
docker compose -f deploy\docker-compose.dev.yml ps
```

通过标准：

- config 成功。
- build 成功。
- MySQL、API、UI 容器启动。
- API health 可访问。
- UI 可访问。

清理：

```powershell
docker compose -f deploy\docker-compose.dev.yml down
```

不要默认删除 volume，除非明确要重置数据库。

## 9. Workflow Contract 检查

文件：

```text
D:\Projects\SymphonyForgeOps\WORKFLOW.md
```

检查项：

- YAML front matter 有 `tracker`、`polling`、`workspace`、`hooks`、`agent`、`codex`。
- workspace root 为绝对路径。
- prompt body 清晰说明 agent 执行规则。
- 不包含真实 token。
- 不包含只适用于某一台机器且不可配置的敏感路径。

后续 parser 完成后，需要增加自动测试：

- 正常 front matter。
- 无 front matter。
- 非 map YAML。
- 缺失文件。
- `$VAR` 解析。
- 相对路径解析。

## 10. 安全检查

路径安全：

- workspace root 必须绝对路径。
- workspace 创建、删除、命令执行前必须校验 root containment。
- 删除命令禁止在 root 未确认时执行。

命令安全：

- destructive command 必须 requires_approval。
- 日志必须脱敏 token。
- 不自动 merge。
- 不自动删除用户项目主目录。

凭据安全：

- GitHub PAT 只从环境变量读取。
- 模型 API key 只从环境变量或本机安全配置读取。
- Docker image 内不包含 key。

## 11. Review 检查

每个 PR 或重要提交前检查：

```powershell
git status --short
git diff --check
```

后端：

```powershell
cd backend
mvn -B test
```

前端：

```powershell
cd frontend
npm run build
```

Docker：

```powershell
docker compose -f deploy\docker-compose.dev.yml config
```

人工 Review 必看：

- 是否误提交 `.env`、`data/`、`logs/`、`target/`、`node_modules/`、`dist/`。
- 是否改动高冲突串行区。
- 是否同步文档。
- 是否说明未验证原因。

## 12. 发布前验收

发布前必须满足：

- 后端测试通过。
- 前端构建通过。
- Docker Compose config 通过。
- MySQL 空库 migration 通过。
- Dashboard 可用。
- README 和 handoff 文档更新。
- GitHub token 未泄露。
- 当前提交可从 clean clone 启动。

