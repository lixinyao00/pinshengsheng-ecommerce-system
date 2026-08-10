# 拼省省商城（PinShengSheng Mall）

> 一个面向简历和面试展示的前后端分离电商微服务项目。

## 项目状态

核心业务链路已经完成并通过本地联调：登录、商品、购物车、地址、下单、库存、模拟支付、订单流转和 RabbitMQ 异步清理。

## 项目目标

围绕“登录 → 商品详情 → 购物车 → 下单 → 库存 → 支付状态”的链路，实践真实电商项目中的微服务拆分、缓存、分布式锁和消息可靠性处理。

## 技术栈基线

- 后端：Java 17、Spring Boot 3.0.5、Spring Cloud 2022.0.2、Spring Cloud Alibaba
- 微服务：Nacos、Gateway、OpenFeign、Sentinel
- 持久层：MyBatis-Plus、MySQL
- 中间件：Redis、Redisson、RabbitMQ
- 文件与部署：MinIO、Docker、Nginx
- 前端：Vue 3、Element Plus、Vite

## 核心业务

1. 用户登录与权限
2. 商品管理和商品详情
3. Redis 商品缓存
4. 购物车
5. 订单结算与创建
6. 库存锁定、扣减和释放
7. 模拟支付状态流转；具备条件时接入支付宝沙箱

## 项目文档

- [10天项目计划](docs/project-plan.md)
- [运行验收清单](docs/runtime-checklist.md)

## 运行说明

1. 在 `infra` 目录复制 `.env.example` 为 `.env`，启动 MySQL、Redis、Nacos、RabbitMQ 和 MinIO。
2. 按 `docs/runtime-checklist.md` 的端口和顺序启动后端服务。
3. 在 `frontend` 目录执行 `npm run dev`，访问 `http://localhost:5173`。

Gateway 统一入口为 `http://localhost:8380`，前端开发代理也指向该地址。所有本地密码、支付密钥和内网地址只放在本地配置中，不提交到仓库。
