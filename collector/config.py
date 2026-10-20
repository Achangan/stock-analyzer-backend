"""集中配置：数据源 token、数据库路径、默认采集范围"""
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DB_PATH = os.path.join(BASE_DIR, "data", "stock.db")
# MySQL 配置
MYSQL_HOST = os.environ.get("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.environ.get("MYSQL_PORT", "3306"))
MYSQL_USER = os.environ.get("MYSQL_USER", "root")
MYSQL_PASSWORD = os.environ.get("MYSQL_PASSWORD", "root123456")
MYSQL_DB = os.environ.get("MYSQL_DB", "stock_analyzer")

SCHEMA_PATH = os.path.join(BASE_DIR, "sql", "schema_mysql.sql")

# Tushare Pro token，注册 https://tushare.pro 免费获取后填入
# 也可用环境变量 TUSHARE_TOKEN 覆盖
TUSHARE_TOKEN = os.environ.get("TUSHARE_TOKEN", "2937e74486e412e07320b151d03eb01bc2e2d80d90cb4c0224f7934c")

# 默认跟踪的指数（P1 报告核心标的）
DEFAULT_INDEXES = [
    "000300.SH",  # 沪深300
    "000905.SH",  # 中证500
    "000001.SH",  # 上证指数
    "399006.SZ",  # 创业板指
    "000688.SH",  # 科创50
]

# 历史回填起始日
HISTORY_START = "2023-01-01"
