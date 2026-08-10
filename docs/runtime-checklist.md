# 运行验收清单

## 基础设施

- [x] MySQL 可连接
- [x] Redis 可连接
- [x] Nacos 控制台可访问
- [x] RabbitMQ 控制台可访问
- [x] MinIO 可访问

## 微服务

- [x] Gateway 启动
- [x] Auth 启动
- [x] Product 启动
- [x] Cart 启动
- [x] Order 启动
- [x] Payment（由 Order 提供模拟支付）

## 业务链路

- [x] 用户登录成功
- [x] 商品详情返回正确数据
- [x] 商品缓存命中和失效行为正确
- [x] 商品加入购物车
- [x] 创建订单
- [x] 库存锁定
- [x] 支付状态更新
- [x] 订单状态最终正确
- [x] RabbitMQ 订单创建消息和购物车异步清理

## Git 与安全

- [ ] 没有提交密码、私钥和真实支付配置
- [x] README 可指导启动
- [ ] 关键功能有对应 commit
- [ ] 关键接口有调用记录或截图

