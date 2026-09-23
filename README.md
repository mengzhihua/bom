# BOM 零部件管理系统

项目亮点见 [docs/项目亮点.md](docs/项目亮点.md)。

## 项目简介

本项目是面向主机厂和汽车零部件开发场景的行业标准 BOM 管理系统，覆盖零件主数据、工程 BOM（EBOM）、制造 BOM（MBOM）、服务 BOM（SBOM）、工程变更、工位管理、SAP 集成和开放接口等核心能力。

系统后端提供统一的认证、角色权限、业务接口和审计日志；前端提供中文 Vue 3 管理界面，支持 BOM 结构浏览、配置解析、版本对比、变更流程和成本重量汇总等操作。

## 技术栈

- 后端：Java 8、Spring Boot 2.7.18、MyBatis-Plus 3.5.3.1、H2、Lombok
- 前端：Vue 3、Element Plus、Vite、Axios、Vue Router
- 数据库：H2 文件数据库，默认存储在 `backend/data/bom`
- 接口前缀：`/api`
- 后端端口：`8082`
- 前端开发端口：`5175`

## 功能模块

### 零件主数据与版本

- 车型项目、工厂、供应商、配置特征和特征选项
- 零件主数据：类型、类别、材质、重量、成本、自制/外购、供应商、图号、生命周期等
- 零件生命周期操作：提交、发布、作废、升版、SAP 同步
- 零件版本历史、文档元数据、CSV 导入导出
- 零件反查：查询零件被哪些 BOM 使用

### BOM 管理

- EBOM、MBOM、SBOM 头信息和行项目管理
- 多级 BOM 树、缩排展开、汇总 BOM
- 成本和重量汇总
- 150% BOM 配置解析为配置 BOM，支持 `&`、`|` 和 `!=` 条件
- BOM 版本创建、发布、冻结、作废和版本对比
- EBOM 派生 MBOM
- MBOM 按工位查看
- BOM CSV 导入导出

### 工程变更

- ECR（工程变更申请）：提交、审批、驳回、关闭
- ECR 生成 ECN（工程变更通知）
- ECN 目标 BOM、生效方式、生效日期、生效 VIN 和变更明细
- ECN 提交、审批、实施
- ECN 实施时生成并发布新 BOM 版本

### 工位管理

- 工厂、产线、工位、工位顺序和节拍维护
- MBOM 行项目工位信息及按工位物料清单

### SAP 集成

- 提供 mock 和 HTTP 两种 SAP 客户端模式
- 零件同步 SAP 物料
- 已发布 BOM 同步 SAP BOM
- 集成日志记录和失败重试
- 配置项：
  - `BOM_SAP_MODE`：`mock` 或 `http`，默认 `mock`
  - `BOM_SAP_BASE_URL`：HTTP 模式下的 SAP 基地址
  - `BOM_SAP_USERNAME`、`BOM_SAP_PASSWORD`：HTTP 模式认证信息

### 开放 API

- 为外部系统提供已发布 BOM 的展开接口
- 使用请求头 `X-Api-Key` 进行鉴权
- API key 由 `BOM_OPEN_API_KEY` 配置；未配置时开放 API 拒绝请求

### 角色权限

- `ADMIN`：管理员，拥有完整权限
- `ENGINEER`：产品工程师，可维护零件、EBOM、ECR 和 ECN，并执行相关集成操作
- `PLANNER`：工艺/制造工程师，可维护 MBOM、工位、工厂并实施 ECN
- `VIEWER`：只读角色，不显示或执行写操作

所有登录用户可以读取业务数据；写操作由角色访问策略控制。系统同时记录操作审计日志。

## 快速开始

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8082`。应用首次启动会自动执行 `schema.sql` 和 `data.sql`，创建 H2 数据库及演示数据。

### 启动前端

另开终端执行：

```bash
cd frontend
npm install
npm run dev
```

前端地址为 `http://localhost:5175`，开发服务器会将 `/api` 请求代理到 `http://localhost:8082`。

### 演示账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `eng` | `eng123` | `ENGINEER` |
| `plan` | `plan123` | `PLANNER` |
| `viewer` | `viewer123` | `VIEWER` |

管理员初始密码可以通过环境变量 `BOM_ADMIN_PASSWORD` 覆盖。

## 验证命令

后端编译和测试：

```bash
cd backend
mvn -q compile
mvn -q test
```

启动后端后，在仓库根目录运行完整冒烟测试：

```bash
bash scripts/smoke.sh
```

前端构建：

```bash
cd frontend
npm run build
```

冒烟脚本覆盖登录、零件发布、多级 BOM、成环校验、树和展开、汇总、成本重量、反查、配置 BOM、BOM 发布、MBOM 派生、版本对比、ECR/ECN 流程、SAP mock 同步及开放 API 鉴权。

## 目录结构

```text
.
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/bom/
│       │   ├── bom/             # BOM 头、行项目、树、配置和版本
│       │   ├── change/          # ECR、ECN 和变更实施
│       │   ├── common/          # 统一响应、异常、CSV 和通用配置
│       │   ├── dashboard/       # 工作台汇总
│       │   ├── integration/     # SAP 集成、日志和开放 API
│       │   ├── master/          # 车型、工厂、供应商、特征和零件
│       │   ├── process/         # 工位
│       │   └── system/          # 认证、用户、权限和审计日志
│       └── resources/
│           ├── application.yml
│           ├── schema.sql
│           └── data.sql
├── frontend/
│   └── src/
│       ├── api/                 # 集中式 API 定义和请求封装
│       ├── components/          # 通用组件
│       ├── layout/              # 主布局
│       ├── router/              # 路由和菜单
│       └── views/               # Dashboard、主数据、零件、BOM、变更等页面
├── scripts/
│   └── smoke.sh                 # 后端业务冒烟脚本
└── README.md
```

## 主要 API

所有业务响应使用统一的 `R` 响应结构，以下路径均包含 `/api` 前缀。

### 认证与工作台

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 认证 | POST | `/auth/login` | 登录并获取令牌 |
| 认证 | GET | `/auth/me` | 获取当前用户 |
| 认证 | POST | `/auth/password` | 修改当前用户密码 |
| 认证 | POST | `/auth/logout` | 登出端点 |
| 工作台 | GET | `/dashboard/summary` | 零件、BOM、变更、车型和日志汇总 |

### 主数据与零件

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 车型 | GET/POST | `/master/vehicle-models` | 查询、新建车型 |
| 工厂 | GET/POST | `/master/plants` | 查询、新建工厂 |
| 供应商 | GET/POST | `/master/suppliers` | 查询、新建供应商 |
| 特征 | GET/POST | `/master/features` | 查询、新建配置特征 |
| 特征选项 | GET/POST | `/master/features/{id}/options` | 查询、新增特征选项 |
| 工位 | GET/POST | `/master/workstations` | 查询、新建工位 |
| 零件 | GET/POST/PUT/DELETE | `/parts`、`/parts/{id}` | 零件分页、查询、新建、编辑、删除 |
| 零件生命周期 | POST | `/parts/{id}/submit`、`release`、`obsolete`、`revise` | 零件状态流转和升版 |
| 零件集成 | POST | `/parts/{id}/sync-sap` | 同步 SAP 物料 |
| 零件文档 | GET/POST/DELETE | `/parts/{id}/documents`、`/parts/documents/{id}` | 文档元数据 |
| 零件 CSV | POST/GET | `/parts/import`、`/parts/export` | 导入和导出零件数据 |
| 反查 | GET | `/parts/{partId}/where-used` | 查询零件的 BOM 使用关系 |

### BOM

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST/PUT | `/boms`、`/boms/{id}` | BOM 列表、查询、新建、编辑 |
| GET/POST/PUT/DELETE | `/boms/{id}/items`、`/boms/{id}/items/{itemId}` | BOM 行项目管理 |
| GET | `/boms/{id}/tree` | 嵌套多级 BOM 树 |
| GET | `/boms/{id}/explode` | 缩排展开，可传 `level` |
| GET | `/boms/{id}/summarized` | 汇总 BOM |
| GET | `/boms/{id}/rollup` | 成本和重量汇总 |
| GET | `/boms/{id}/by-station` | MBOM 按工位分组 |
| POST | `/boms/{id}/configure` | 根据 selections 解析配置 BOM |
| GET | `/boms/compare` | 通过 `leftId`、`rightId` 对比两个 BOM |
| POST | `/boms/{id}/release`、`freeze`、`obsolete` | BOM 生命周期操作 |
| POST | `/boms/{id}/new-version` | 创建新版本草稿 |
| POST | `/boms/{id}/derive-mbom` | 从 EBOM 派生 MBOM |
| POST | `/boms/{id}/sync-sap` | 同步已发布 BOM 到 SAP |
| POST/GET | `/boms/{id}/import`、`/boms/{id}/export` | BOM CSV 导入和导出 |

### ECR 与 ECN

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| ECR | GET/POST | `/ecrs` | 查询、新建 ECR |
| ECR 流程 | POST | `/ecrs/{id}/submit`、`approve`、`reject`、`close` | ECR 状态流转 |
| ECR 转 ECN | POST | `/ecrs/{id}/to-ecn` | 从已批准 ECR 创建 ECN |
| ECN | GET/PUT | `/ecns`、`/ecns/{id}` | 查询和编辑 ECN |
| ECN 明细 | GET/POST | `/ecns/{id}/items` | 查询、新增 ECN 变更明细 |
| ECN 流程 | POST | `/ecns/{id}/submit`、`approve`、`implement` | ECN 提交、审批和实施 |

### 集成、开放接口和系统

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 集成日志 | GET | `/integration/logs` | 查询集成调用日志 |
| 集成日志 | POST | `/integration/logs/{id}/retry` | 重试集成调用 |
| 开放 BOM | GET | `/open/boms/{bomNo}/explode` | 使用 `X-Api-Key` 获取已发布 BOM 展开数据 |
| 用户 | GET/POST/PUT/DELETE | `/system/user/page`、`/system/user/list`、`/system/user/{id}` | 用户分页、列表和维护 |
| 操作日志 | GET | `/system/oplog/page` | 分页查询审计操作日志 |

## 控制塔对接

BOM / ECN 快照，以及展开、提交、审批、实施，见 [技术方案](docs/技术方案.md)。

这些指令必须带 API Key：`GET /api/open/ir/snapshots`，`POST /api/open/ir/actions`，专用口 `/explode`、`/submit-ecn`、`/approve-ecn`、`/implement-ecn`。控制塔登录模式不调用本系统。

## 发布包（开箱即用）

前端生产构建打进 Spring Boot 可执行 JAR。三种用法：

### 1. 服务端（任意已装 JDK 17 的机器）

```bash
java -jar bom-backend-1.0.0.jar --server.port=8088
```

Linux systemd 示例见发布包 `README.txt`。

### 2. 便携包（需本机已装 Java）

```bash
bash scripts/package-release.sh
unzip release/bom-1.0.0.zip
cd bom-1.0.0
```

| 系统 | 怎么用 |
| --- | --- |
| Linux | `./start.sh` |
| macOS | 双击 `start.command`，或 `./start.sh` |
| Windows | 双击 `start.bat` |

### 3. 原生包（捆绑 JRE，不必装 Java）

合并到默认分支且便携包冒烟通过后，GitHub Actions 自动发布 GitHub Release（也可在 Actions 里手动 `workflow_dispatch`）。分别在 Ubuntu / Windows / macOS 生成：

- `bom-1.0.0-linux-x64.zip` → `bin/bom`
- `bom-1.0.0-windows-x64.zip` → 双击 `bom.exe`
- `bom-1.0.0-macos-arm64.zip` → Apple Silicon（M 系列），双击 `bom.app`
- `bom-1.0.0-macos-x64.zip` → Intel Mac，双击 `bom.app`

浏览器访问 `http://127.0.0.1:8088`。原生包默认数据目录为用户主目录下的 `.bom/data`，可用 `-Dbom.data.dir` 或 `BOM_DATA_DIR` 覆盖。默认账号 `admin / admin123`。

十二套系统可同时启动：OMS 8081 / WMS 8082 / TMS 8083 / BMS 8084 / SAP 8085 / OA 8086 / SRM 8087 / BOM 8088 / INV 8089 / IR 8090 / CRM 8091 / DMS 8092。

