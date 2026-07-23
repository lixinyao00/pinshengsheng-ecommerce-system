# 第 3 天微服务基础记录

## 已完成

- 抽取 `common` 公共模块，统一响应对象由各业务服务依赖使用。
- 引入 Spring Cloud Alibaba 2022.0.0.0。
- Product 服务接入 Nacos 服务发现。
- 新建 Gateway 服务并接入 Nacos。
- 配置 `/api/product/**` 路由到 `lb://product-service`。

## 本机配置

| 服务 | 端口 | Nacos 注册地址 |
| --- | ---: | --- |
| Gateway | 8080 | 127.0.0.1 |
| Product | 8081 | 127.0.0.1 |

本机通过局域网地址访问自身 Java 服务时 HTTP 请求会超时，因此本地开发阶段固定注册为 `127.0.0.1`。部署到不同服务器时需要改为各自可互相访问的内网地址。

## 验收结果

- Product 与 Gateway 均成功注册到 Nacos。
- Nacos 中两个实例的健康状态均为 `true`。
- `GET http://localhost:8080/actuator/health` 返回 `UP`。
- `GET http://localhost:8080/api/product/1` 经过 Gateway 返回商品数据。
- Maven 全部模块测试通过。

## 请求链路

```text
客户端
  -> Gateway:8080
  -> Nacos 查询 product-service
  -> Product:8081
  -> 返回统一响应
```
