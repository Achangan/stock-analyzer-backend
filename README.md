# A股分析系统 - 后端

A股每日自动分析引擎后端，基于 Spring Cloud 微服务架构。

## 技术栈

- **Spring Boot 2.7.18** + **Spring Cloud 2021.0.9** + **Spring Cloud Alibaba 2021.0.5.0**
- **Nacos 2.2.3**（服务注册发现 + 配置中心）
- **Spring Cloud Gateway**（API 网关、路由、CORS）
- **OpenFeign** + **Spring Cloud LoadBalancer**（服务间调用）
- **MySQL 8.4** + **HikariCP**（数据存储）
- **Python 3.14**（数据采集层，独立运行）

## 服务架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Gateway   │────▶│ quote-service│     │ collector   │
│   (8080)    │     │   (8081)    │     │  (Python)   │
└──────┬──────┘     └──────┬──────┘     └─────────────┘
       │                   │
       │     ┌─────────────┘     ┌─────────────┐
       │     │                   │  Nacos      │
       │     ▼                   │  (8848)     │
       │  ┌─────────────┐        └─────────────┘
       │  │analysis-    │
       └──▶│service      │
          │  (8082)     │
          └──────┬──────┘
                 │
                 ▼
          ┌─────────────┐
          │report-      │
          │service      │
          │  (8083)     │
          └─────────────┘
```

| 服务 | 端口 | 职责 |
|------|------|------|
| gateway-service | 8080 | API 网关，路由转发，CORS |
| quote-service | 8081 | 行情数据服务，指数列表、区间/最近日线（含 OHLC、成交量、成交额）、数据覆盖度 |
| analysis-service | 8082 | 分析服务，均线、涨跌幅、波动率、均线排列趋势与 MA20 乖离计算 |
| report-service | 8083 | 报告生成服务，Markdown 格式每日报告 |

## 快速启动

### 前置条件

- JDK 1.8（Temurin）
- Maven 3.9+
- MySQL 8.4+（数据目录 `D:\mysql-data`）
- Nacos Server 2.2.3

### 1. 启动 Nacos

```bash
# Windows（standalone 模式）
cd nacos/bin
startup.cmd -m standalone
```

控制台：http://localhost:8848/nacos（默认账号密码 nacos/nacos）

### 2. 启动 MySQL

```bash
# Windows 服务方式或进程方式
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
```

创建数据库：
```sql
CREATE DATABASE stock_analyzer DEFAULT CHARACTER SET utf8mb4;
-- 执行 sql/schema_mysql.sql 初始化表结构
```

### 3. 启动微服务

```bash
# 从项目根目录启动
mvn -pl gateway-service spring-boot:run
mvn -pl quote-service spring-boot:run
mvn -pl analysis-service spring-boot:run
mvn -pl report-service spring-boot:run
```

或者分别进入各服务目录启动。

### 4. 数据采集（Python）

```bash
cd collector
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
python run_daily.py
```

配置 Tushare Token 见 `collector/config.py`。

## API 列表

通过 Gateway（8080）统一访问：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/quote/index/list` | GET | 指数列表 |
| `/api/quote/index/{code}/recent` | GET | 最近 N 个交易日数据（日期降序，`limit` 默认 61） |
| `/api/quote/index/{code}/range` | GET | 指定区间日线（日期升序，`start`/`end` 可选，用于 K 线图） |
| `/api/quote/coverage` | GET | 各指数数据覆盖度（起止日期、行数） |
| `/api/analysis/index/{code}/summary` | GET | 单指数分析摘要（MA5/20/60、20/60日涨跌幅、20日波动率、均线排列趋势、MA20 乖离率） |
| `/api/analysis/index/summaries` | GET | 批量分析摘要（`codes` 以英文逗号分隔，一次返回多个指数） |
| `/api/report/latest` | GET | 最新每日报告 |
| `/api/report/daily` | POST | 生成当日分析报告 |

指数代码支持 `000300`、`000300.SH`、`000300SH` 三种写法混输，服务端会自动归一化。

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `trade_cal` | 交易日历 |
| `daily_quote` | 个股日线（预留） |
| `index_daily` | 指数日线 |
| `collect_log` | 采集日志 |

## 项目结构

```
stock-analyzer-backend/
├── pom.xml                  # 父 POM，管理依赖版本
├── gateway-service/         # API 网关
├── quote-service/           # 行情服务
├── analysis-service/        # 分析服务
├── report-service/          # 报告服务
├── collector/               # Python 数据采集层
│   ├── tushare_client.py    # Tushare 生产通道
│   ├── baostock_client.py   # Baostock 历史回填
│   └── run_daily.py         # 每日采集入口
└── sql/
    └── schema_mysql.sql     # MySQL 建表脚本
```

## 数据源说明

| 数据源 | 定位 | 说明 |
|--------|------|------|
| Tushare Pro | 生产主力 | 需注册获取 token |
| Baostock | 历史回填兜底 | 免费免注册 |
| AkShare | 补充 | ETF 份额、指数成分等 |

## 风险提示

本项目仅供学习和技术研究，不构成任何投资建议。股市有风险，投资需谨慎。
