# 本地基础服务

基础服务统一由 `docker-compose.yml` 管理，端口和应用配置先固定下来，后续业务服务按这个约定连接。

## 第一次启动

在 `infra` 目录执行：

```powershell
Copy-Item .env.example .env
docker compose up -d
docker compose ps
```

停止服务：

```powershell
docker compose down
```

删除本地数据卷前先确认不再需要数据：

```powershell
docker compose down -v
```

## 控制台

- Nacos：<http://localhost:8848/nacos>
- RabbitMQ：<http://localhost:15672>
- MinIO：<http://localhost:9001>
- MySQL：`localhost:13306`
- Redis：`localhost:6379`

本机已完成启动验收。MySQL 使用 `13306` 是为了避免占用开发机已有的 `3306` 端口。
