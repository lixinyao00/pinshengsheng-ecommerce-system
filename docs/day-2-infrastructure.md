# 第 2 天基础设施记录

## 已完成

- 统一 MySQL、Redis、Nacos、RabbitMQ、MinIO 的端口约定。
- 增加 `infra/docker-compose.yml`。
- 增加不含本地真实密码的 `infra/.env.example`。
- 增加基础服务启动、停止和控制台地址说明。

## 本机验证结果

- MySQL：Docker 容器健康检查通过，宿主机端口为 13306。
- Redis：Docker 容器健康检查通过，端口为 6379。
- Nacos：控制台可访问，端口为 8848。
- RabbitMQ：管理页面可访问，端口为 15672。
- MinIO：健康检查接口可访问，端口为 9000。

## 待完成验收

后续服务接入 MySQL 时使用 `localhost:13306`。
