# 第 2 天基础设施记录

## 已完成

- 统一 MySQL、Redis、Nacos、RabbitMQ、MinIO 的端口约定。
- 增加 `infra/docker-compose.yml`。
- 增加不含本地真实密码的 `infra/.env.example`。
- 增加基础服务启动、停止和控制台地址说明。

## 本机验证结果

- MySQL：3306 当前有监听进程。
- Redis：6379 未监听。
- Nacos：8848、9848 未监听。
- RabbitMQ：5672、15672 未监听。
- Docker、Podman：未安装或不在 PATH。

## 待完成验收

安装 Docker 后，在 `infra` 目录执行 `Copy-Item .env.example .env` 和 `docker compose up -d`，再逐项检查端口和控制台。
