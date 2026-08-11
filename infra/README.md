# 基础服务与部署配置

基础服务统一由 `docker-compose.yml` 管理。`.env` 只保存当前机器或服务器的真实配置，不提交到 Git。

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

## 开发环境端口

- Nacos：<http://localhost:8848/nacos>；应用注册使用 `localhost:18848`
- RabbitMQ：<http://localhost:15672>
- MinIO：<http://localhost:9001>
- MySQL：`localhost:13380`
- Redis：`localhost:16379`

MySQL 使用 `13380` 是为了避免占用开发机已有的 `3306` 端口。

## 部署时需要调整的变量

复制 `.env.example` 为 `.env` 后，至少修改以下内容：

- `MYSQL_ROOT_PASSWORD`、`MINIO_ROOT_PASSWORD`、`PSS_RABBITMQ_PASSWORD`：替换为强密码。
- `PSS_MYSQL_HOST`、`PSS_REDIS_HOST`、`PSS_NACOS_HOST`、`PSS_RABBITMQ_HOST`：填写部署环境中对应服务的地址。
- `PSS_MINIO_ENDPOINT`：填写客户端可访问的 MinIO 地址。
- `PSS_PRODUCT_SERVICE_URL`：填写订单服务访问商品服务的内部地址。

各后端服务会优先读取这些变量；没有设置时仍使用当前本机开发默认值。

## Docker 生产部署

服务器首次部署时，在项目根目录执行：

```bash
cp infra/.env.production.example infra/.env
vim infra/.env
bash infra/deploy.sh
```

`.env` 中必须设置强密码，并将 `PSS_MINIO_PUBLIC_ENDPOINT` 改为服务器公网 IP 或域名，例如 `http://1.2.3.4/files`。

启动完成后访问 `http://服务器公网IP`。对外只需要开放 `80` 端口；MySQL、Redis、Nacos、RabbitMQ 和 MinIO 都不会映射到公网。
