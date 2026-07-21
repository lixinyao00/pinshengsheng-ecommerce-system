# 第 1 天基线记录

## 已完成

- 建立 `backend` Maven 多模块根工程。
- 建立第一个 `product-service` 商品服务。
- 增加 `GET /api/product/health` 健康接口。
- 建立 Vue 3 + Vite 前端骨架，默认端口 5173。
- 配置前端 `/api` 到商品服务 `8081` 的开发代理。
- 记录微服务职责、端口和核心业务链路。

## 当前环境事实

- Java 17.0.5：可用。
- Maven 3.9.11：可用。
- Node.js 24.18.0、npm 11.16.0：可用。
- MySQL 3306：当前有监听进程。
- Docker、Redis、Nacos、RabbitMQ：当前未检测到可用命令或监听端口，放到第 2 天处理。

## 第 1 天验收标准

1. `mvn test` 通过。
2. 商品服务启动后，`GET http://localhost:8081/api/product/health` 返回 `status=UP`。
3. 前端依赖安装后，`npm run build` 通过。
4. 前后端代码和文档形成一次独立 Git 提交。
